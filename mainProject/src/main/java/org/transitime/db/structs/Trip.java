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
import javax.persistence.Transient;

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
	// Not final because only used for frequency based trips.
	@Column
	@Id 
	private Integer startTime;
	
	// Number of seconds into the day.
	// Not final because only used for frequency based trips.
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
	
	// Contains schedule time for each stop as obtained from GTFS 
	// stop_times.txt file. Useful for determining schedule adherence.
	// NOTE: trying to use serialization. Serialization 
	// makes the data not readable in the db using regular SQL but it means
	// that don't need separate table and the data can be read and written
	// much faster.
	// Keyed on stopId
	@Column(length=2000)// @ElementCollection
	private final HashMap<String, ScheduleTime> scheduledTimesMap = 
			new HashMap<String, ScheduleTime>(); 
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String serviceId;
	
	// The GTFS trips.txt trip_headsign if set. Otherwise null.
	@Column
	private final String headsign;
	
	// From GTFS trips.txt block_id if set. Otherwise the trip_id.
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String blockId;
	
	// The GTFS trips.txt shape_id
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String shapeId;

	@Transient
	private Route route;
	
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
		this.headsign = gtfsTrip.getTripHeadsign();
		// block column is optional in GTFS trips.txt file. Best can do when
		// block ID is not set is to use the trip short name or the trip id 
		// as the block. MBTA uses trip short name in the feed so start with
		// that.
		String theBlockId = gtfsTrip.getBlockId();
		if (theBlockId == null) {
			theBlockId = gtfsTrip.getTripShortName();
			if (theBlockId == null)
				theBlockId = gtfsTrip.getTripId();
		}
		this.blockId = theBlockId;
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
		this.headsign = tripFromStopTimes.headsign;
		this.shapeId = tripFromStopTimes.shapeId;
		this.tripPattern = tripFromStopTimes.tripPattern;
		this.travelTimes = tripFromStopTimes.travelTimes;
		
		// Set the updated start and end times by using the frequencies
		// based start time.
		this.startTime = tripFromStopTimes.startTime + frequenciesBasedStartTime;
		this.endTime = tripFromStopTimes.endTime + frequenciesBasedStartTime;
		
		// Since frequencies being used for configuration we will have multiple
		// trips with the same ID. But need a different block ID for each one.
		// Therefore use the original trip's block ID but then append the 
		// start time as a string to make it unique. 
		this.blockId = tripFromStopTimes.blockId + "_" + 
				Time.timeOfDayStr(this.startTime);

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
		headsign = null;
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
	public void addScheduleTime(String stopId, ScheduleTime scheduleTime) {
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
	 * Returns map of Trip objects for the specified configRev. The
	 * map is keyed on the trip IDs.
	 * 
	 * @param session
	 * @param configRev
	 * @return
	 * @throws HibernateException
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Trip> getTrips(Session session, int configRev) 
			throws HibernateException {
		// Setup the query
		String hql = "FROM Trip " +
				"    WHERE configRev = :configRev";
		Query query = session.createQuery(hql);
		query.setInteger("configRev", configRev);

		// Actually perform the query
		List<Trip> tripsList = query.list();

		// Now put the Trips into a map and return it
		Map<String, Trip> tripsMap = new HashMap<String, Trip>();
		for (Trip trip : tripsList) {
			tripsMap.put(trip.getId(), trip);
		}
		return tripsMap;
	}

	/**
	 * Returns specified Trip object for the specified configRev. 
	 * 
	 * @param session
	 * @param configRev
	 * @param tripId
	 * @return
	 * @throws HibernateException
	 */
	public static Trip getTrip(Session session, int configRev, String tripId) 
			throws HibernateException {
		// Setup the query
		String hql = "FROM Trip " +
				"    WHERE configRev = :configRev" +
				"      AND tripId = :tripId";
		Query query = session.createQuery(hql);
		query.setInteger("configRev", configRev);
		query.setString("tripId", tripId);
		
		// Actually perform the query
		Trip trip = (Trip) query.uniqueResult();

		return trip;
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
				+ ", tripPatternId=" 
					+ (tripPattern != null ? tripPattern.getId() : "null")
				+ ", tripIndex=" + getIndex()
				+ ", startTime=" + Time.timeOfDayStr(startTime)
				+ ", endTime=" + Time.timeOfDayStr(endTime)
				+ ", name=\"" + headsign + "\""
				+ ", directionId=" + directionId
				+ ", routeId=" + routeId
				+ ", routeShortName=" + routeShortName
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
				+ ", tripPatternId=" 
					+ (tripPattern != null ? tripPattern.getId() : "null")
				+ ", tripIndex=" + getIndex()
				+ ", startTime=" + Time.timeOfDayStr(startTime)
				+ ", endTime=" + Time.timeOfDayStr(endTime)
				+ ", name=\"" + headsign + "\""
				+ ", directionId=" + directionId
				+ ", routeId=" + routeId
				+ ", routeShortName=" + routeShortName
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
		result = prime * result + ((headsign == null) ? 0 : headsign.hashCode());
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
		if (headsign == null) {
			if (other.headsign != null)
				return false;
		} else if (!headsign.equals(other.headsign))
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
	 * Returns the routeShortName. If it is null then returns the full route
	 * name.
	 * 
	 * @return the routeShortName
	 */
	public String getRouteShortName() {
		return routeShortName != null ? routeShortName : getRouteName();
	}

	/**
	 * Returns the Route object for this trip. This object is determined
	 * and cached when first accessed.
	 * @return
	 */
	public Route getRoute() {
		if (route == null)
			route = Core.getInstance().getDbConfig().getRouteById(routeId);
		return route;
	}
	
	/**
	 * Returns route name. Gets it from the database configuration.
	 * @return
	 */
	public String getRouteName() {
		return getRoute().getName();
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
	public String getHeadsign() {
		return headsign;
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
	 * Returns the Block that the Trip is associated with. Only valid
	 * when running the core application where can use Core.getInstance().
	 * Otherwise returns null.
	 * 
	 * @return
	 */
	public Block getBlock() {
		// If not part of the core project where DbConfig is available
		// then just return null.
		Core core = Core.getInstance();
		if (core == null)
			return null;
		DbConfig dbConfig = core.getDbConfig();
		if (dbConfig == null)
			return null;
		
		// Part of core project so return the Block
		return dbConfig.getBlock(serviceId, blockId);
	}
	
	/**
	 * Returns the index of the trip in the block. Uses DbConfig from the core
	 * project to determine the Block. If not part of the core project the Block
	 * info is not available and -1 is returned.
	 * 
	 * @return The index of the trip in the block or -1 if block info not
	 *         available.
	 */
	public int getIndex() {
		// If block info no available then simply return -1
		Block block = getBlock();
		if (block == null)
			return -1;
		
		// Block info available so return the trip index
		return block.getTripIndex(getId());
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
	 * Returns the ScheduleTime object for the stopId. Will return null if there
	 * are no schedule times associated with that stop for this trip. Useful for
	 * determining schedule adherence.
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
	 * Returns length of the trip from the first terminal to the last.
	 * 
	 * @return
	 */
	public double getLength() {
		return getTripPattern().getLength();
	}
	
	/**
	 * Returns the List of the stop paths for the trip pattern
	 * 
	 * @return
	 */
	public List<StopPath> getStopPaths() {
		return tripPattern.getStopPaths();
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
	
	/**
	 * Returns number of stop paths defined for this trip.
	 * 
	 * @return Number of stop paths
	 */
	public int getNumberStopPaths() {
		return getTripPattern().getStopPaths().size();
	}
	
}