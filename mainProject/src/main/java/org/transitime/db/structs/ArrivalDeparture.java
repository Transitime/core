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
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.db.hibernate.HibernateUtils;

/**
 * For persisting an Arrival or a Departure time. Should use Arrival or 
 * Departure subclasses.
 *  
 * @author SkiBu Smith
 */
@Entity @DynamicUpdate 
@Table(name="ArrivalsDepartures") 
@org.hibernate.annotations.Table(appliesTo = "ArrivalsDepartures", 
   indexes = { @Index(name="indexTest", 
                      columnNames={"time"} ) } )
public class ArrivalDeparture implements Serializable {
	
	@Id 
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String vehicleId;
	
	// Note that not specifying that need fractional seconds for arrival/
	// departure times since they are only estimates and therefore should
	// not be considered to be accurate to within a second. Therefore the
	// @Column definition does not include (columnDefinition="datetime(3)")
	@Id 
	@Column	
	@Temporal(TemporalType.TIMESTAMP)
	private final Date time;

	@Id 
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String stopId;
	
	// From the GTFS stop_times.txt file for the trip. The stopSequence can
	// be different from stopPathIndex. The stopIndex is included here so that
	// it is easy to find the corresponding stop in the stop_times.txt file.
	// It needs to be part of the @Id because can have loops for a route
	// such that a stop is served twice on a trip. Otherwise would get a
	// constraint violation.
	@Id
	@Column
	private final int stopSequence;
	
	@Id 
	@Column
	private final boolean isArrival;

	@Id 
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String tripId;
	
	// tripStartTime needs to be an Id because when using frequencies.txt 
	// exact_times then a tripId alone cannot identify a Trip. Will have
	// multiple Trip objects for the tripId, one for each start time.
	@Id
	@Column
	private final Integer tripStartTime;

	// The revision of the configuration data that was being used
	@Column 
	final int configRev;
	
	// So can match the ArrivalDeparture time to the AvlReport that
	// generated it by using vehicleId and avlTime.
	@Column	
	@Temporal(TemporalType.TIMESTAMP)
	private final Date avlTime;
	
	// The schedule time will only be set if the schedule info was available
	// from the GTFS data and it is the proper type of arrival or departure 
	// stop (there is an arrival schedule time and this is the last stop for
	// a trip and and this is an arrival time OR there is a departure schedule
	// time and this is not the last stop for a trip and this is a departure 
	// time.
	@Column	
	@Temporal(TemporalType.TIMESTAMP)
	private final Date scheduledTime;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String blockId;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String routeId;
	
	// routeShortName is included because for some agencies the
	// route_id changes when there are schedule updates. But the
	// routeShortName is more likely to stay consistent. Therefore
	// it is better for when querying for arrival/departure data
	// over a timespan.
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String routeShortName;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String serviceId;
		
	// The index of which trip this is within the block.
	@Column 
	private final int tripIndex;
	
	// The index of which path this is within the trip.
	// Different from the stopSequence. The stopPathIndex starts
	// at 0 and increments by one for every stop. The stopSequence
	// on the other hand doesn't need to be sequential.
	@Column 
	private final int pathIndex;
	
	// Needed because some methods need to know if dealing with arrivals or 
	// departures.
	public enum ArrivalsOrDepartures {ARRIVALS, DEPARTURES};

	private static final Logger logger = 
			LoggerFactory.getLogger(ArrivalDeparture.class);

	// Needed because Hibernate objects must be serializable
	private static final long serialVersionUID = 6511713704337986699L;

	/********************** Member Functions **************************/

	/**
	 * Constructor called when creating an ArrivalDeparture object to be 
	 * stored in db.
	 * 
	 * @param vehicleId
	 * @param time
	 * @param avlTime
	 * @param block
	 * @param tripIndex
	 * @param stopPathIndex
	 * @param isArrival
	 */
	protected ArrivalDeparture(String vehicleId, Date time, Date avlTime, Block block, 
			int tripIndex, int pathIndex, boolean isArrival) {
		this.vehicleId = vehicleId;
		this.time = time;
		this.avlTime = avlTime;
		this.tripIndex = tripIndex;
		this.pathIndex = pathIndex;
		this.isArrival = isArrival;
		this.configRev = Core.getInstance().getDbConfig().getConfigRev();
		
		// Some useful convenience variables
		Trip trip = block.getTrip(tripIndex);
		StopPath path = trip.getStopPath(pathIndex);
		String stopId = path.getStopId();
		
		// Determine the schedule time, which is a bit complicated.
		// The schedule time will only be set if the schedule info was available
		// from the GTFS data and it is the proper type of arrival or departure 
		// stop (there is an arrival schedule time and this is the last stop for
		// a trip and and this is an arrival time OR there is a departure schedule
		// time and this is not the last stop for a trip and this is a departure 
		// time.
		ScheduleTime scheduleTime = trip.getScheduleTime(stopId);
		Date scheduledEpochTime = null;
		if (path.isLastStopInTrip() && scheduleTime.getArrivalTime() != null
				&& isArrival) {
			long epochTime = Core.getInstance().getTime()
					.getEpochTime(scheduleTime.getArrivalTime(), time);
			scheduledEpochTime = new Date(epochTime);
		} else if (!path.isLastStopInTrip()
				&& scheduleTime.getDepartureTime() != null && !isArrival) {
			long epochTime = Core.getInstance().getTime()
					.getEpochTime(scheduleTime.getDepartureTime(), time);
			scheduledEpochTime = new Date(epochTime);
		}
		this.scheduledTime = scheduledEpochTime;
		
		this.blockId = block.getId();
		this.tripId = trip.getId();
		this.tripStartTime = trip.getStartTime();
		this.stopId = stopId;
		this.stopSequence = path.getStopSequence();
		this.routeId = trip.getRouteId();
		this.routeShortName = trip.getRouteShortName();
		this.serviceId = block.getServiceId();
				
		// Log each creation of an ArrivalDeparture
		logger.info(this.toString());
	}
	
	/**
	 * Hibernate requires a no-arg constructor for reading objects
	 * from database.
	 */
	protected ArrivalDeparture() {
		this.vehicleId = null;
		this.time = null;
		this.avlTime = null;
		this.tripIndex = -1;
		this.pathIndex = -1;
		this.isArrival = false;
		this.configRev = -1;
		this.scheduledTime = null;
		this.blockId = null;
		this.tripId = null;
		this.tripStartTime = null;
		this.stopId = null;
		this.stopSequence = -1;
		this.routeId = null;
		this.routeShortName = null;
		this.serviceId = null;
	}

	/**
	 * Because using a composite Id Hibernate wants this member.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((blockId == null) ? 0 : blockId.hashCode());
		result = prime * result + configRev;
		result = prime * result + (isArrival ? 1231 : 1237);
		result = prime * result + pathIndex;
		result = prime * result + stopSequence;
		result = prime * result + ((routeId == null) ? 0 : routeId.hashCode());
		result = prime * result + 
				((routeShortName == null) ? 0 : routeShortName.hashCode());
		result = prime * result
				+ ((serviceId == null) ? 0 : serviceId.hashCode());
		result = prime * result + ((stopId == null) ? 0 : stopId.hashCode());
		result = prime * result + ((time == null) ? 0 : time.hashCode());
		result = prime * result + ((tripId == null) ? 0 : tripId.hashCode());
		result = prime * result + tripIndex;
		result = prime * result
				+ ((tripStartTime == null) ? 0 : tripStartTime.hashCode());
		result = prime * result
				+ ((vehicleId == null) ? 0 : vehicleId.hashCode());
		return result;
	}

	/**
	 * Because using a composite Id Hibernate wants this member.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ArrivalDeparture other = (ArrivalDeparture) obj;
		if (blockId == null) {
			if (other.blockId != null)
				return false;
		} else if (!blockId.equals(other.blockId))
			return false;
		if (configRev != other.configRev)
			return false;
		if (isArrival != other.isArrival)
			return false;
		if (pathIndex != other.pathIndex)
			return false;
		if (stopSequence != other.stopSequence)
			return false;
		if (routeId == null) {
			if (other.routeId != null)
				return false;
		} else if (!routeId.equals(other.routeId))
			return false;
		if (routeShortName == null) {
			if (other.routeShortName != null)
				return false;
		} else if (!routeShortName.equals(other.routeShortName))
			return false;
		if (serviceId == null) {
			if (other.serviceId != null)
				return false;
		} else if (!serviceId.equals(other.serviceId))
			return false;
		if (stopId == null) {
			if (other.stopId != null)
				return false;
		} else if (!stopId.equals(other.stopId))
			return false;
		if (time == null) {
			if (other.time != null)
				return false;
		} else if (!time.equals(other.time))
			return false;
		if (tripId == null) {
			if (other.tripId != null)
				return false;
		} else if (!tripId.equals(other.tripId))
			return false;
		if (tripIndex != other.tripIndex)
			return false;
		if (tripStartTime == null) {
			if (other.tripStartTime != null)
				return false;
		} else if (!tripStartTime.equals(other.tripStartTime))
			return false;
		if (vehicleId == null) {
			if (other.vehicleId != null)
				return false;
		} else if (!vehicleId.equals(other.vehicleId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ArrivalDeparture [" 
				+ "vehicleId=" + vehicleId 
				+ ", time=" + time
				+ ", stopId=" + stopId 
				+ ", gtfsStopSequence=" + stopSequence
				+ ", isArrival=" + isArrival
				+ ", configRev=" + configRev
				+ ", tripId=" + tripId 
				+ ", blockId=" + blockId 
				+ ", routeId="	+ routeId 
				+ ", routeShortName=" + routeShortName
				+ ", serviceId=" + serviceId
				+ ", tripIndex=" + tripIndex 
				+ ", stopPathIndex=" + pathIndex 
				+ "]";
	}
	
	/**
	 * For querying large amount of data. With a Hibernate Iterator not
	 * all the data is read in at once. This means that can iterate over
	 * a large dataset without running out of memory. But this can be slow
	 * because when using iterate() an initial query is done to get all of
	 * Id column data and then a separate query is done when iterating 
	 * over each row. Doing an individual query per row is of course
	 * quite time consuming. Better to use getArrivalsDeparturesFromDb()
	 * with a fairly large batch size of ~50000.
	 * <p>
	 * Note that the session needs to be closed externally once done with
	 * the Iterator.
	 * 
	 * @param session
	 * @param beginTime
	 * @param endTime
	 * @return
	 * @throws HibernateException
	 */
	public static Iterator<ArrivalDeparture> getArrivalsDeparturesDbIterator(
			Session session, Date beginTime, Date endTime) 
			throws HibernateException {
		// Create the query. Table name is case sensitive and needs to be the
		// class name instead of the name of the db table.
		String hql = "FROM ArrivalDeparture " +
				"    WHERE time >= :beginDate " +
				"      AND time < :endDate"; 
		Query query = session.createQuery(hql);
		
		// Set the parameters
		query.setTimestamp("beginDate", beginTime);
		query.setTimestamp("endDate", endTime);

		@SuppressWarnings("unchecked")
		Iterator<ArrivalDeparture> iterator = query.iterate(); 
		return iterator;
	}
	
	/**
	 * Reads the arrivals/departures for the timespan specified. All of the 
	 * data is read in at once so could present memory issue if reading
	 * in a very large amount of data. For that case probably best to instead
	 * use getArrivalsDeparturesDb() where one specifies the firstResult and 
	 * maxResult parameters.
	 * 
	 * @param projectId
	 * @param beginTime
	 * @param endTime
	 * @return
	 */
	public static List<ArrivalDeparture> getArrivalsDeparturesFromDb(
			String projectId, Date beginTime, Date endTime) {
		// Get the database session. This is supposed to be pretty light weight
		SessionFactory sessionFactory = 
				HibernateUtils.getSessionFactory(projectId);
		Session session = sessionFactory.openSession();

		// Create the query. Table name is case sensitive and needs to be the
		// class name instead of the name of the db table.
		String hql = "FROM ArrivalDeparture " +
				"    WHERE time >= :beginDate " +
				"      AND time < :endDate"; 
		Query query = session.createQuery(hql);
		
		// Set the parameters
		query.setTimestamp("beginDate", beginTime);
		query.setTimestamp("endDate", endTime);
		
		try {
			@SuppressWarnings("unchecked")
			List<ArrivalDeparture> arrivalsDeparatures = query.list();
			return arrivalsDeparatures;
		} catch (HibernateException e) {
			// Log error to the Core logger
			Core.getLogger().error(e.getMessage(), e);
			return null;
		} finally {
			// Clean things up. Not sure if this absolutely needed nor if
			// it might actually be detrimental and slow things down.
			session.close();
		}
	}

	/**
	 * Allows batch retrieval of data. This is likely the best way to read in
	 * large amounts of data. Using getArrivalsDeparturesDbIterator() reads in
	 * only data as needed so good with respect to memory usage but it does a
	 * separate query for each row. Reading in list of all data is quick but can
	 * cause memory problems if reading in a very large amount of data. This
	 * method is a good compromise because it only reads in a batch of data at a
	 * time so is not as memory intensive yet it is quite fast. With a batch
	 * size of 50k found it to run in under 1/4 the time as with the iterator
	 * method.
	 * 
	 * @param projectId
	 * @param beginTime
	 * @param endTime
	 * @param sqlClause
	 *            The clause is added to the SQL for retrieving the
	 *            arrival/departures. Useful for ordering the results. Can be
	 *            null.
	 * @param firstResult
	 * @param maxResults
	 * @param arrivalOrDeparture
	 *            Enumeration specifying whether to read in just arrivals or
	 *            just departures. Set to null to read in both.
	 * @return
	 */
	public static List<ArrivalDeparture> getArrivalsDeparturesFromDb(
			String projectId, Date beginTime, Date endTime, 
			String sqlClause,
			final int firstResult, final int maxResults,
			ArrivalsOrDepartures arrivalOrDeparture) {
		// Get the database session. This is supposed to be pretty light weight
		SessionFactory sessionFactory = 
				HibernateUtils.getSessionFactory(projectId);
		Session session = sessionFactory.openSession();

		// Create the query. Table name is case sensitive and needs to be the
		// class name instead of the name of the db table.
		String hql = "FROM ArrivalDeparture " +
				"    WHERE time >= :beginDate " +
				"      AND time < :endDate";
		if (arrivalOrDeparture != null) {
			if (arrivalOrDeparture == ArrivalsOrDepartures.ARRIVALS)
				hql += " AND isArrival = true";
			else 
				hql += " AND isArrival = false";
		}
		if (sqlClause != null)
			hql += " " + sqlClause;
		Query query = session.createQuery(hql);
		
		// Set the parameters for the query
		query.setTimestamp("beginDate", beginTime);
		query.setTimestamp("endDate", endTime);
		
		// Only get a batch of data at a time
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);
		
		try {
			@SuppressWarnings("unchecked")
			List<ArrivalDeparture> arrivalsDeparatures = query.list();
			return arrivalsDeparatures;
		} catch (HibernateException e) {
			// Log error to the Core logger
			Core.getLogger().error(e.getMessage(), e);
			return null;
		} finally {
			// Clean things up. Not sure if this absolutely needed nor if
			// it might actually be detrimental and slow things down.
			session.close();
		}
	}

	public String getVehicleId() {
		return vehicleId;
	}

	public Date getTime() {
		return time;
	}

	public String getStopId() {
		return stopId;
	}

	public boolean isArrival() {
		return isArrival;
	}

	public String getTripId() {
		return tripId;
	}

	public String getBlockId() {
		return blockId;
	}

	public String getRouteId() {
		return routeId;
	}

	public String getServiceId() {
		return serviceId;
	}

	public int getConfigRev() {
		return configRev;
	}

	public int getTripIndex() {
		return tripIndex;
	}

	public int getPathIndex() {
		return pathIndex;
	}

}
