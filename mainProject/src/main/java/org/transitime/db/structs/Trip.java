/* 
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Transitime.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Transitime.org .  If not, see <http://www.gnu.org/licenses/>.
 */
package org.transitime.db.structs;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.transitime.applications.Core;
import org.transitime.db.hibernate.HibernateUtils;
import org.transitime.gtfs.DbConfig;
import org.transitime.gtfs.TitleFormatter;
import org.transitime.gtfs.gtfsStructs.GtfsTrip;
import org.transitime.utils.Time;


/**
 *
 * @author SkiBu Smith
 *
 */
@Entity @DynamicUpdate @Table(name="Trips")
public class Trip implements Serializable {

	@Column 
	@Id	
	private final int configRev;

	@Column(length=HibernateUtils.DEFAULT_ID_SIZE) 
	@Id 
	private final String tripId;
	
	// The startTime needs to be an Id column because GTFS frequencies.txt
	// file can be used to define multiple trips with the same trip ID. 
	// It is in number of seconds into the day.
	@Column
	@Id 
	private Integer startTime;
	
	// Number of seconds into the day
	@Column
	private Integer endTime;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String directionId;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String routeId;
	
	// Route short name is also needed because some agencies such as SFMTA
	// change the route IDs when making schedule changes. But we need a 
	// consistent route identifier for certain things, such as bookmarking
	// prediction pages or for running schedule adherence reports over
	// time. For where need a route identifier that is consistent over time
	// it can be best to use the routeShortName.
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String routeShortName;
	
	// So can determine all the stops and stopPaths associated with trip
	// Note that needs to be FetchType.EAGER because otherwise get a 
	// Javassist HibernateException.
	@ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	private TripPattern tripPattern;

	// Use FetchType.EAGER so that all travel times are efficiently read in
	// when system is started up.
	// Note that needs to be FetchType.EAGER because otherwise get a Javassist 
	// HibernateException.
	//
	// We are sharing travel times so need a ManyToOne mapping
	@ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	private TravelTimesForTrip travelTimes;
	
	// NOTE: since trying to use serialization need to use ArrayList<> instead
	// of List<> since List<> doesn't implement Serializable. Serialization 
	// makes the data not readable in the db using regular SQL but it means
	// that don't need separate table and the data can be read and written
	// much faster.
	// Keyed on stopId
	@Column(length=2000)// @ElementCollection
	private final HashMap<String, ScheduleTime> scheduledTimesMap = 
			new HashMap<String, ScheduleTime>(); 
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String serviceId;
	
	@Column
	private final String name;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String blockId;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String shapeId;

	// Note: though trip_short_name and wheelchair_accessible are available
	// as part of the GTFS spec and in a GtfsTrip object, they are not
	// included here because currently don't understand how best to use them
	// for RTPI. Route and shape can be determined from the TripPattern so don't 
	// need to be here.
	
	// Hibernate requires class to be serializable because has composite Id
	private static final long serialVersionUID = -23135771144135812L;

	/********************** Member Functions **************************/

	/**
	 * Constructs Trip object from GTFS data.
	 * 
	 * @param gtfsTrip
	 *            The GTFS data describing the trip
	 * @param properRouteId
	 *            The routeId, but can actually be the ID of the parent route.
	 * @param routeShortName
	 *            Needed to provide a route identifier that is consistent over
	 *            schedule changes.
	 * @param titleFormatter
	 *            So can fix titles associated with trip
	 */
	public Trip(GtfsTrip gtfsTrip, String properRouteId, String routeShortName, 
			TitleFormatter titleFormatter) {
		// Because will be writing data to the sandbox rev in the db
		this.configRev = DbConfig.SANDBOX_REV;

		this.tripId = gtfsTrip.getTripId();
		this.directionId = gtfsTrip.getDirectionId();
		this.routeId = properRouteId!=null? properRouteId : gtfsTrip.getRouteId();
		this.routeShortName = routeShortName;
		this.serviceId = gtfsTrip.getServiceId();
		this.name = gtfsTrip.getTripHeadsign();
		// block column is optional in GTFS trips.txt file. Best can do for
		// this situation is to use the tripId as the block.
		this.blockId = gtfsTrip.getBlockId()!=null? gtfsTrip.getBlockId() : gtfsTrip.getTripId();
		this.shapeId = gtfsTrip.getShapeId();
	}
	
	/**
	 * Creates a copy of the Trip object but adjusts the startTime, endTime, and
	 * scheduledTimesMap according to the frequenciesBasedStartTime. This is
	 * used when the frequencies.txt file specifies exact_times for a trip.
	 * 
	 * @param tripFromStopTimes
	 * @param frequenciesBasedStartTime
	 */
	public Trip(Trip tripFromStopTimes, int frequenciesBasedStartTime) {
		this.configRev = tripFromStopTimes.configRev;
		this.tripId = tripFromStopTimes.tripId;
		this.directionId = tripFromStopTimes.directionId;
		this.routeId = tripFromStopTimes.routeId;
		this.routeShortName = tripFromStopTimes.routeShortName;
		this.serviceId = tripFromStopTimes.serviceId;
		this.name = tripFromStopTimes.name;
		this.blockId = tripFromStopTimes.blockId;
		this.shapeId = tripFromStopTimes.shapeId;
		this.tripPattern = tripFromStopTimes.tripPattern;
		this.travelTimes = tripFromStopTimes.travelTimes;
		
		// Set the updated start and end times by using the frequencies
		// based start time.
		this.startTime = tripFromStopTimes.startTime + frequenciesBasedStartTime;
		this.endTime = tripFromStopTimes.endTime + frequenciesBasedStartTime;
		
		// Set the scheduledTimesMap by using the frequencies based start time
		for (String stopIdKey : tripFromStopTimes.scheduledTimesMap.keySet()) {
			ScheduleTime schedTimeFromStopTimes = 
					tripFromStopTimes.scheduledTimesMap.get(stopIdKey);
			Integer arrivalTime = null;
			if (schedTimeFromStopTimes.getArrivalTime() != null)
				arrivalTime = schedTimeFromStopTimes.getArrivalTime() + 
					frequenciesBasedStartTime;
			Integer departureTime = null;
			if (schedTimeFromStopTimes.getDepartureTime() != null)
				departureTime = schedTimeFromStopTimes.getDepartureTime() + 
					frequenciesBasedStartTime;
			
			ScheduleTime schedTimeFromFrequency = 
					new ScheduleTime(arrivalTime, departureTime); 
			this.scheduledTimesMap.put(stopIdKey, schedTimeFromFrequency);
		}
	}
	
	/**
	 * Hibernate requires no-arg constructor
	 */
	@SuppressWarnings("unused")
	private Trip() {
		configRev = -1;
		tripId = null;
		directionId = null;
		routeId = null;
		routeShortName = null;
		serviceId = null;
		name = null;
		blockId = null;
		shapeId = null;
	}

	/**
	 * For adding a ScheduleTime for a stop to Trip. Updates scheduledTimesMap,
	 * startTime, and endTime.
	 * 
	 * @param stopId
	 * @param scheduleTime
	 */
	public void add(String stopId, ScheduleTime scheduleTime) {
		scheduledTimesMap.put(stopId, scheduleTime);
		
		// Determine the begin and end time. Assumes that times are added in order
		if (startTime == null || 
				(scheduleTime.getDepartureTime() != null && 
				 scheduleTime.getDepartureTime() < startTime))
			startTime = scheduleTime.getDepartureTime();
		if (endTime == null || 
				(scheduleTime.getArrivalTime() != null &&  
				 scheduleTime.getArrivalTime() > endTime))
			endTime = scheduleTime.getArrivalTime();
	}

	/**
	 * TripPattern is created after the Trip. Therefore it cannot
	 * be set in constructor and instead needs this set method.
	 * @param tripPattern
	 */
	public void setTripPattern(TripPattern tripPattern) {
		this.tripPattern = tripPattern;		
	}
	
	/**
	 * TravelTimesForTrip created after the Trip. Therefore it cannot
	 * be set in constructor and instead needs this set method.
	 * @param travelTimes
	 */
	public void setTravelTimes(TravelTimesForTrip travelTimes) {
		this.travelTimes = travelTimes;
	}
	
	/**
	 * Returns list of Trip objects for the specified configRev
	 * 
	 * @param session
	 * @param configRev
	 * @return
	 * @throws HibernateException
	 */
	@SuppressWarnings("unchecked")
	public static List<Trip> getTrips(Session session, int configRev) 
			throws HibernateException {
		String hql = "FROM Trip " +
				"    WHERE configRev = :configRev";
		Query query = session.createQuery(hql);
		query.setInteger("configRev", configRev);
		return query.list();
	}

	/**
	 * Deletes rev 0 from the Trips table
	 * 
	 * @param session
	 * @return Number of rows deleted
	 * @throws HibernateException
	 */
	public static int deleteFromSandboxRev(Session session) throws HibernateException {
		int rowsUpdated = 0;
		rowsUpdated += 
				session.createSQLQuery("DELETE FROM Trips " + 
									   "WHERE configRev=0").
				executeUpdate();
		return rowsUpdated;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		// Note: the '\n' at beginning is so that when output list of trips each will be on new line
		return "\n    Trip [" 
				+ "configRev=" + configRev
				+ ", tripId=" + tripId 
				+ ", tripPatternId=" + tripPattern.getId()
				+ ", tripIndex=" + getIndex()
				+ ", startTime=" + Time.timeOfDayStr(startTime)
				+ ", endTime=" + Time.timeOfDayStr(endTime)
				+ ", dirName=\"" + name + "\""
				+ ", directionId=" + directionId
				+ ", routeId=" + routeId
				+ ", serviceId=" + serviceId
				+ ", blockId=" + blockId
				+ ", shapeId=" + shapeId
				+ ", scheduledTimesMap=" + scheduledTimesMap 
				+ "]";
	}
	
	/**
	 * Similar to toString() but doesn't include scheduledTimesMap which
	 * can be quite verbose since it often contains times for many stops.
	 * @return
	 */
	public String toShortString() {
		return "Trip ["
				+ "tripId=" + tripId 
				+ ", tripPatternId=" + tripPattern.getId()
				+ ", tripIndex=" + getIndex()
				+ ", startTime=" + Time.timeOfDayStr(startTime)
				+ ", endTime=" + Time.timeOfDayStr(endTime)
				+ ", dirName=\"" + name + "\""
				+ ", directionId=" + directionId
				+ ", routeId=" + routeId
				+ ", serviceId=" + serviceId
				+ ", blockId=" + blockId
				+ ", shapeId=" + shapeId
				+ "]";
	}

	/**
	 * Required by Hibernate
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((blockId == null) ? 0 : blockId.hashCode());
		result = prime * result + configRev;
		result = prime * result
				+ ((directionId == null) ? 0 : directionId.hashCode());
		result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((routeId == null) ? 0 : routeId.hashCode());
		result = prime
				* result
				+ ((scheduledTimesMap == null) ? 0 : scheduledTimesMap
						.hashCode());
		result = prime * result
				+ ((serviceId == null) ? 0 : serviceId.hashCode());
		result = prime * result + ((shapeId == null) ? 0 : shapeId.hashCode());
		result = prime * result
				+ ((startTime == null) ? 0 : startTime.hashCode());
		result = prime * result
				+ ((travelTimes == null) ? 0 : travelTimes.hashCode());
		result = prime * result + ((tripId == null) ? 0 : tripId.hashCode());
		result = prime * result
				+ ((tripPattern == null) ? 0 : tripPattern.hashCode());
		return result;
	}

	/**
	 * Required by Hibernate
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Trip other = (Trip) obj;
		if (blockId == null) {
			if (other.blockId != null)
				return false;
		} else if (!blockId.equals(other.blockId))
			return false;
		if (configRev != other.configRev)
			return false;
		if (directionId == null) {
			if (other.directionId != null)
				return false;
		} else if (!directionId.equals(other.directionId))
			return false;
		if (endTime == null) {
			if (other.endTime != null)
				return false;
		} else if (!endTime.equals(other.endTime))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (routeId == null) {
			if (other.routeId != null)
				return false;
		} else if (!routeId.equals(other.routeId))
			return false;
		if (scheduledTimesMap == null) {
			if (other.scheduledTimesMap != null)
				return false;
		} else if (!scheduledTimesMap.equals(other.scheduledTimesMap))
			return false;
		if (serviceId == null) {
			if (other.serviceId != null)
				return false;
		} else if (!serviceId.equals(other.serviceId))
			return false;
		if (shapeId == null) {
			if (other.shapeId != null)
				return false;
		} else if (!shapeId.equals(other.shapeId))
			return false;
		if (startTime == null) {
			if (other.startTime != null)
				return false;
		} else if (!startTime.equals(other.startTime))
			return false;
		if (travelTimes == null) {
			if (other.travelTimes != null)
				return false;
		} else if (!travelTimes.equals(other.travelTimes))
			return false;
		if (tripId == null) {
			if (other.tripId != null)
				return false;
		} else if (!tripId.equals(other.tripId))
			return false;
		if (tripPattern == null) {
			if (other.tripPattern != null)
				return false;
		} else if (!tripPattern.equals(other.tripPattern))
			return false;
		return true;
	}

	/**
	 * Returns departure time of first stop of trip. 
	 * Gtfs requires departure time of first stop of trip and
	 * arrival time of last stop of trip to be set, even
	 * for unscheduled blocks. That way they can determine
	 * running time of trip.
	 * @return departure time of first stop of trip. Time is seconds into the day.
	 */
	public Integer getStartTime() {
		return startTime;
	}
	
	/**
	 * Returns arrival time of last stop of trip. 
	 * Gtfs requires departure time of first stop of trip and
	 * arrival time of last stop of trip to be set, even
	 * for unscheduled blocks. That way they can determine
	 * running time of trip.
	 * @return arrival time of last stop of trip. Time is seconds into the day.
	 */
	public Integer getEndTime() {
		return endTime;
	}
	
	/********************** Getter Methods **************************/
	
	/**
	 * @return the configRev
	 */
	public int getConfigRev() {
		return configRev;
	}

	/**
	 * @return the tripId
	 */
	public String getId() {
		return tripId;
	}
	
	/**
	 * @return the routeId
	 */
	public String getRouteId() {
		return routeId;
	}
	
	/**
	 * @return the routeShortName
	 */
	public String getRouteShortName() {
		return routeShortName;
	}
	
	/**
	 * @return the directionId
	 */
	public String getDirectionId() {
		return directionId;
	}

	/**
	 * @return the serviceId
	 */
	public String getServiceId() {
		return serviceId;
	}

	/**
	 * The trip_headsign value from the trips.txt file. But
	 * it has been processed by the TitleFormatter to correct
	 * capitalization and such.
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * The block_id is an optional element in GTFS trips.txt file.
	 * If it is available it is used. But if it is not then
	 * the trip_id is used as next best thing.
	 * @return blockId
	 */
	public String getBlockId() {
		return blockId;
	}
	
	/**
	 * Returns the Block that the Trip is associated with
	 * @return
	 */
	public Block getBlock() {
		return Core.getInstance().getDbConfig().getBlock(serviceId, blockId);
	}
	
	/**
	 * Returns the index of the trip in the block.
	 * 
	 * @return
	 */
	public int getIndex() {
		return getBlock().getTripIndex(getId());
	}
	
	/**
	 * @return the shapeId
	 */
	public String getShapeId() {
		return shapeId;		
	}
	
	/**
	 * @return the tripPattern
	 */
	public TripPattern getTripPattern() {
		return tripPattern;
	}
	
	/**
	 * @return the getScheduleTimesMap
	 */
	public Map<String, ScheduleTime> getScheduleTimesMap() {
		return scheduledTimesMap;
	}
	
	/**
	 * Returns the ScheduleTime object for the stopId. Will 
	 * return null if there are no schedule times associated with
	 * that stop for this trip.
	 * 
	 * @param stopId
	 * @return
	 */
	public ScheduleTime getScheduleTime(String stopId) {
		return scheduledTimesMap.get(stopId);
	}
	
	/**
	 * @return the travelTimes
	 */
	public TravelTimesForTrip getTravelTimes() {
		return travelTimes;
	}
	
	/**
	 * Returns the travel time info for the path specified by the stopPathIndex.
	 * 
	 * @param stopPathIndex
	 * @return
	 */
	public TravelTimesForStopPath getTravelTimesForStopPath(int stopPathIndex) {
		return travelTimes.getTravelTimesForStopPath(stopPathIndex);
	}
	
	/**
	 * Returns the StopPath for the stopPathIndex specified
	 * 
	 * @param stopPathIndex
	 * @return the path specified or null if index out of range
	 */
	public StopPath getStopPath(int stopPathIndex) {
		return tripPattern.getStopPath(stopPathIndex);
	}
}
