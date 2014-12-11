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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.JDBCException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.DynamicUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.configData.CoreConfig;
import org.transitime.core.SpatialMatch;
import org.transitime.db.hibernate.HibernateUtils;
import org.transitime.utils.IntervalTimer;
import org.transitime.utils.Time;

/**
 * Represents assignment for a vehicle for a day. Obtained by combining
 * data from multiple GTFS files.
 * 
 * Thought a good deal about how to represent all the stops in the block.
 * Wanted to have a list of trip patterns instead of trips because that
 * would be more efficient. But then realized that for each trip need
 * different times and such. Therefore decided to use full list of Trips.
 * This will make the data in the database unfortunately quite large.
 * 
 * @author SkiBu Smith
 */
@Entity(name="Blocks") @DynamicUpdate @Table(name="Blocks")
public final class Block implements Serializable {
	
	@Column 
	@Id
	private final int configRev;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE) 
	@Id
	private final String blockId;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE) 
	@Id
	private final String serviceId;

	@Column
	private final int startTime; // In seconds from midnight
	
	@Column
	private final int endTime;   // In seconds from midnight

	// Need to have a ManyToMany instead of OneToMany relationship
	// for the List of Trips because several Blocks can refer to the 
	// same trip. This is because "UNSCHEDULED" blocks can be created
	// using regular trips. For this situation don't want Hibernate to
	// try to store the same trip twice because then would get a uniqueness
	// violation.
	//
	// Use CascadeType.SAVE_UPDATE so that when the TripPattern is stored   
	// the Paths are automatically stored.
	//
	// Use FetchType.LAZY so that don't read in all trip data at once since
	// that in turn reads in trip pattern and travel time info, which can
	// be voluminous and therefore slow. The trips will be read in when
	// getTrips() is called.
	@ManyToMany(fetch=FetchType.LAZY)
	@JoinTable(name="Block_to_Trip_joinTable")
	@OrderColumn(name="listIndex")
	@Cascade({CascadeType.SAVE_UPDATE})
	private final List<Trip> trips;
	
	@Column
	private final int headwaySecs; // For non-scheduled blocks

	// Sometimes will get vehicle assignment by routeId. This means that need
	// to know which blocks are associated with a route. Getting the routeIds
	// from the Trip objects is problematic because then all the Trip data
	// needs to be lazy loaded such that if need to look at all blocks then
	// need to load in all trips. That takes too long for when doing debugging.
	// So to speed things up the routeIds for a block are stored here.
	// NOTE: since trying to use serialization need to use ArrayList<> instead
	// of List<> since List<> doesn't implement Serializable.
	@Column(length=500)
	private final HashSet<String> routeIds;
	
	// For making sure only lazy load trips collection via one thread
	// at a time.
	private static final Object lazyLoadingSyncObject = new Object();
	
	// Hibernate requires class to be serializable because has composite Id
	private static final long serialVersionUID = 6511242755235485004L;

	private static final Logger logger = LoggerFactory.getLogger(Block.class);

	/********************** Member Functions **************************/

	/**
	 * This constructor called when processing GTFS data and creating a Block to
	 * be stored in the database. Note: startTime and endTime could in theory be
	 * determined here by looking at first and last TripElements. But what if
	 * they haven't been set in GTFS? Want the calling function to do this kind
	 * of error checking because it does other error checking and can log issues
	 * appropriately.
	 * 
	 * @param configRev
	 * @param blockId
	 * @param startTime
	 * @param endTime
	 * @param trips
	 * @param headwaySecs
	 *            If less than 0 then it is schedule based block. If 0 then it
	 *            is unscheduled block where vehicles run whenever. If greater
	 *            than 0 then vehicles are to run on this specified headway
	 */
	public Block(int configRev, String blockId, String serviceId,
			int startTime, int endTime, List<Trip> trips, int headwaySecs) {
		this.configRev = configRev;
		this.blockId = blockId;
		this.serviceId = serviceId;
		this.startTime = startTime;
		this.endTime = endTime;
		this.trips = trips;
		this.headwaySecs = headwaySecs;
		
		// Obtain the set of route IDs from the trips
		this.routeIds = new HashSet<String>();
		for (Trip trip : trips) {
			this.routeIds.add(trip.getRouteId());
		}
	}

	/**
	 * Hibernate requires no-arg constructor
	 */
	@SuppressWarnings("unused")
	private Block() {
		this.configRev = -1;
		this.blockId = null;
		this.serviceId = null;
		this.startTime = -1;
		this.endTime = -1;
		this.trips = null;
		this.headwaySecs = -1;	
		this.routeIds = null;
	}
	
	/**
	 * Returns list of Block objects for the specified configRev
	 * 
	 * @param session
	 * @param configRev
	 * @return List of Block objects
	 * @throws HibernateException
	 */
	@SuppressWarnings("unchecked")
	public static List<Block> getBlocks(Session session, int configRev) 
			throws HibernateException {
		String hql = "FROM Blocks " +
				"    WHERE configRev = :configRev";
		Query query = session.createQuery(hql);
		query.setInteger("configRev", configRev);
		return query.list();
	}

	/**
	 * Deletes rev from the Blocks, Trips, and Block_to_Trip_joinTable
	 * 
	 * @param session
	 * @param configRev
	 * @return Number of rows deleted
	 * @throws HibernateException
	 */
	public static int deleteFromRev(Session session, int configRev) 
			throws HibernateException {
		// In a perfect Hibernate world one would simply call on session.delete()
		// for each block and the block to trip join table and the associated
		// trips would be automatically deleted by using the magic of Hibernate.
		// But this means that would have to read in all the Blocks and sub-objects
		// first, which of course takes lots of time and memory, often causing
		// program to crash due to out of memory issue. And since reading in the
		// Trips is supposed to automatically read in associated travel times
		// we would be reading in data that isn't even needed for deletion since
		// don't want to delete travel times (want to reuse them!). Therefore
		// using the much, much faster solution of direct SQL calls. Can't use
		// HQL on the join table since it is not a regularly defined table. 
		//
		// Note: Would be great to see if can actually use HQL and delete the 
		// appropriate Blocks and have the join table and the trips table
		// be automatically updated. I doubt this would work but would be
		// interesting to try if had the time.
		int totalRowsUpdated = 0;

		// Delete configRev data from Block_to_Trip_joinTable
		int rowsUpdated = session.
				createSQLQuery("DELETE FROM Block_to_Trip_joinTable "
						+ "WHERE Blocks_configRev=" + configRev).
				executeUpdate();
		logger.info("Deleted {} rows from Block_to_Trip_joinTable for "
				+ "configRev={}", rowsUpdated, configRev);
		totalRowsUpdated += rowsUpdated;

		// Delete configRev data from Trips
		rowsUpdated = session.
				createSQLQuery("DELETE FROM Trips WHERE configRev=" 
						+ configRev).
				executeUpdate();
		logger.info("Deleted {} rows from Trips for configRev={}",
				rowsUpdated, configRev);
		totalRowsUpdated += rowsUpdated;

		// Delete configRev data from Blocks
		rowsUpdated = session.
				createSQLQuery("DELETE FROM Blocks WHERE configRev=" 
						+ configRev).
				executeUpdate();
		logger.info("Deleted {} rows from Blocks for configRev={}",
				rowsUpdated, configRev);
		totalRowsUpdated += rowsUpdated;
		
		return totalRowsUpdated;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		// Want to better explain what headwaySecs means
		String headwaySecsStr = "" + headwaySecs;
		if (headwaySecs < 0)
			headwaySecsStr += " (schedule based)";
		else if (headwaySecs == 0)
			headwaySecsStr += " (unscheduled and no specified frequency)";
		else
			headwaySecsStr += " (headway for unscheduled block)";
		
		return "Block [" 
				+ "configRev=" + configRev
				+ ", blockId=" + blockId
				+ ", serviceId=" + serviceId
				+ ", startTime=" + Time.timeOfDayStr(startTime) 
				+ ", endTime=" + Time.timeOfDayStr(endTime) 
				// Use getTrips() instead of trips to deal with possible lazy 
				// initialization issues
				+ ", trips=" + getTrips() 
				+ ", headwaySecs=" + headwaySecsStr 
				+ "]";
	}
	
	/**
	 * Like toString() but only outputs core info. Doesn't output
	 * the trips except to identify them. This is a big difference
	 * from toString() because each Trip has lots of info to output.
	 * @return
	 */
	public String toShortString() {
		// Want to better explain what headwaySecs means
		String headwaySecsStr = "" + headwaySecs;
		if (headwaySecs < 0)
			headwaySecsStr += " (schedule based)";
		else if (headwaySecs == 0)
			headwaySecsStr += " (unscheduled and no specified frequency)";
		else
			headwaySecsStr += " (headway for unscheduled block)";

		// Create shortened version of Trip info that only includes the trip_id
		String tripsStr = "Trip [";
		for (Trip trip : getTrips()) {
			tripsStr += trip.getId() + ", ";
		}
		tripsStr += "]";
		return "Block [" 
				+ "blockId=" + blockId
				+ ", serviceId=" + serviceId
				+ ", configRev=" + configRev
				+ ", startTime=" + Time.timeOfDayStr(startTime) 
				+ ", endTime=" + Time.timeOfDayStr(endTime) 
				+ ", trips=" + tripsStr // Use the shortened version
				+ ", headwaySecs=" + headwaySecsStr 
				+ "]";
	}
	
	
	/**
	 * Required by Hibernate
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + blockId.hashCode();
		result = prime * result + serviceId.hashCode();
		result = prime * result + configRev;
		result = prime * result + endTime;
		result = prime * result + headwaySecs;
		result = prime * result + startTime;
		result = prime * result + ((trips == null) ? 0 : trips.hashCode());
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
		Block other = (Block) obj;
		if (!blockId.equals(other.blockId))
			return false;
		if (!serviceId.equals(other.serviceId))
			return false;
		if (configRev != other.configRev)
			return false;
		if (endTime != other.endTime)
			return false;
		if (headwaySecs != other.headwaySecs)
			return false;
		if (startTime != other.startTime)
			return false;
		if (trips == null) {
			if (other.trips != null)
				return false;
		} else if (!trips.equals(other.trips))
			return false;
		return true;
	}
	
	/**
	 * Returns true if the time of day of the date passed in is between
	 * allowableBeforeTimeSecs before the startTime and the endTime for the
	 * block. No leeway is provided for the end time. Note: does not look to see
	 * if the service associated with the block is active. Only looks at time of
	 * day.
	 * 
	 * @param date
	 * @return True if the block is active.
	 */
	public boolean isActive(Date date, int allowableBeforeTimeSecs) {
		int secsInDayForAvlReport = 
				Core.getInstance().getTime().getSecondsIntoDay(date);

		return (secsInDayForAvlReport > startTime - allowableBeforeTimeSecs
						&& secsInDayForAvlReport < endTime)
				// also handle where date before midnight but start time is after
				|| (secsInDayForAvlReport > startTime+Time.SEC_PER_DAY - allowableBeforeTimeSecs
						&& secsInDayForAvlReport < endTime+Time.SEC_PER_DAY);		
	}

	/**
	 * Returns true if the time of day of the date passed in is between the
	 * startTime and the endTime for the block. No leeway is provided. Note:
	 * does not look to see if the service associated with the block is active.
	 * Only looks at time of day.
	 * 
	 * @param date
	 * @return True if the block is active.
	 */
	public boolean isActive(Date date) {
		return isActive(date, 0);
	}
	
	/**
	 * Returns true if the time of day of the date passed in is between
	 * allowableBeforeTimeSecs before the start time and the start time.
	 * 
	 * @param date
	 * @param allowableBeforeTimeSecs
	 * @return true if within allowableBeforeTimeSecs before the start time of
	 *         the block
	 */
	public boolean isBeforeStartTime(Date date, int allowableBeforeTimeSecs) {
		int secsInDayForAvlReport = 
				Core.getInstance().getTime().getSecondsIntoDay(date);

		return (secsInDayForAvlReport > startTime - allowableBeforeTimeSecs
						&& secsInDayForAvlReport < startTime)
				// also handle where date before midnight but start time is after
				|| (secsInDayForAvlReport > startTime+Time.SEC_PER_DAY - allowableBeforeTimeSecs
						&& secsInDayForAvlReport < startTime+Time.SEC_PER_DAY);
	}
	
	/**
	 * If the trip is active at the secsInDayForAvlReport then it is
	 * added to the tripsThatMatchTime list. Trip is considered active
	 * if it is within start time of trip minus 
	 * CoreConfig.getAllowableEarlyForLayoverSeconds() and within the end
	 * time of the trip. No leniency is made for the end time since once
	 * a trip is over really don't want to assign vehicle to that trip.
	 * Yes, vehicles often run late, but that should only be taken account
	 * when matching to already predictable vehicle.
	 * 
	 * @param vehicleId for logging messages
	 * @param secsInDayForAvlReport
	 * @param trip
	 * @param tripsThatMatchTime
	 * @return
	 */
	private static boolean addTripIfActive(String vehicleId,
			int secsInDayForAvlReport, Trip trip, 
			List<Trip> tripsThatMatchTime) {
		int startTime = trip.getStartTime();
		int endTime = trip.getEndTime();

		int allowableEarlyTimeSecs = 
				CoreConfig.getAllowableEarlyForLayoverSeconds();
		if (secsInDayForAvlReport > startTime - allowableEarlyTimeSecs 
				&& secsInDayForAvlReport < endTime) {
			tripsThatMatchTime.add(trip);

			if (logger.isDebugEnabled()) {
				logger.debug("Determined that for blockId={} that a trip is " +
						"considered to be active for AVL time. " + 
						"TripId={}, tripIndex={} AVLTime={}, " + 
						"startTime={}, endTime={}, " + 
						"allowableEarlyForLayover={} secs, allowableLate={} secs, " +
						"vehicleId={}",
						trip.getBlock().getId(),
						trip.getId(), 
						trip.getBlock().getTripIndex(trip.getId()),
						Time.timeOfDayStr(secsInDayForAvlReport),
						Time.timeOfDayStr(trip.getStartTime()),
						Time.timeOfDayStr(trip.getEndTime()),
						CoreConfig.getAllowableEarlyForLayoverSeconds(),
						CoreConfig.getAllowableLateSeconds(),
						vehicleId);
			}
			
			return true;
		}
		
		// Not a match so return false
		return false;
	}
	
	/**
	 * For this block determines which trips are currently active. Should work
	 * even for trips that start before midnight or go till after midnight. Trip
	 * is considered active if it is within start time of trip minus
	 * CoreConfig.getAllowableEarlyForLayoverSeconds() and within the end time
	 * of the trip. No leniency is made for the end time since once a trip is
	 * over really don't want to assign vehicle to that trip.
	 * 
	 * @param avlReport
	 * @return List of Trips that are active. If none are active an empty list
	 *         is returned.
	 */
	public List<Trip> getTripsCurrentlyActive(AvlReport avlReport) {
		// Set for returning results
		List<Trip> tripsThatMatchTime = new ArrayList<Trip>();
		
		// Convenience variable
		String vehicleId = avlReport.getVehicleId();
		
		// Go through trips and find ones 
		List<Trip> trips = getTrips();
		for (Trip trip : trips) {			
			// If time of avlReport is within reasonable time of the trip
			// time then this trip should be returned.  
			int secsInDayForAvlReport = 
					Core.getInstance().getTime().getSecondsIntoDay(avlReport.getDate());

			// If the trip is active then add it to the list of active trips 
			boolean tripIsActive = 
					addTripIfActive(vehicleId, secsInDayForAvlReport, trip, tripsThatMatchTime);
			
			// if trip wasn't active might be because trip actually starts before
			// midnight so should check for that special case.
			if (!tripIsActive)
				tripIsActive = 
					addTripIfActive(vehicleId, 
							secsInDayForAvlReport - Time.SEC_PER_DAY, 
							trip, tripsThatMatchTime);
			
			// if trip still wasn't active might be because trip goes past
			// midnight so should check for that special case.
			if (!tripIsActive)
				tripIsActive = 
					addTripIfActive(vehicleId, 
							secsInDayForAvlReport + Time.SEC_PER_DAY, trip, 
							tripsThatMatchTime);
		}

		// Returns results
		return tripsThatMatchTime;
	}
	

	/***************************** Getter Methods ************************/

	/**
	 * @return the blockId
	 */
	public String getId() {
		return blockId;
	}

	/**
	 * @return the configRev
	 */
	public int getConfigRev() {
		return configRev;
	}

	/**
	 * @return the serviceId
	 */
	public String getServiceId() {
		return serviceId;
	}
	
	/**
	 * @return the startTime
	 */
	public int getStartTime() {
		return startTime;
	}

	/**
	 * @return the endTime
	 */
	public int getEndTime() {
		return endTime;
	}

	/**
	 * @return the trips
	 */
	public List<Trip> getTrips() {
		// It appears that lazy initialization is problematic when have multiple 
		// simultaneous threads. Get "org.hibernate.AssertionFailure: force 
		// initialize loading collection". Therefore need to make sure that 
		// only loading lazy sub-data serially. Since it is desirable to have
		// trips collection be lazy loaded so that app starts right away without
		// loading all the sub-data for every block assignment need to make
		// sure this is done in a serialized way. Having app not load all data
		// at startup is especially important when debugging.
		if (!Hibernate.isInitialized(trips)) {
			// trips not yet initialized so synchronize so only a single
			// thread can initialize at once and then access something
			// in trips that will cause it to be lazy loaded.
			synchronized (lazyLoadingSyncObject) {
				logger.debug("About to do lazy load for trips data for " + 
						"blockId={} serviceId={}...",
						blockId, serviceId);
				IntervalTimer timer = new IntervalTimer();
				
				// Access the collection so that it is lazy loaded.
				// Problems can be difficult to debug so log error along
				// with the SQL.
				try {
					trips.get(0);
				} catch (JDBCException e) {
					logger.error("In Block.getTrips() got JDBCException. "
							+ "SQL=\"{}\" msg={}", 
							e.getSQL(), e.getSQLException().getMessage());
					throw e;
				}
				logger.debug("Finished lazy load for trips data for " + 
						"blockId={} serviceId={}. Took {} msec",
						blockId, serviceId, timer.elapsedMsec());
			}
		}
		
		return Collections.unmodifiableList(trips);
	}
	
	/**
	 * So can sync up loading of trip and trip pattern data when trips are all
	 * read at once in another class as opposed to through Block.getTrips().
	 * 
	 * @return
	 */
	public static Object getLazyLoadingSyncObject() {
		return lazyLoadingSyncObject;
	}
	
	/**
	 * Returns the trip specified by the tripIndex
	 * 
	 * @param tripIndex
	 * @return the Trip specified by tripIndex. If index out of range returns
	 *         null
	 */
	public Trip getTrip(int tripIndex) {
		// If index out of range return null
		if (tripIndex < 0 || tripIndex >= getTrips().size())  {
			return null;
		}
		
		// Return the specified trip
		return getTrips().get(tripIndex);
	}
	
	/**
	 * Returns the trip for the block as specified by the tripId parameter
	 * @param tripId Which trip is to be returned
	 * @return The Trip that matches the tripId
	 */
	public Trip getTrip(String tripId) {
		for (Trip trip : getTrips())
			if (trip.getId().equals(tripId))
				return trip;
		
		// The tripId was not found for this block so return null
		return null;
	}

	/**
	 * Returns the index into the trips list of the trip specified
	 * by the tripId parameter.
	 * @param tripId Specifies which trip looking for
	 * @return Index into trips of the specified trip
	 */
	public int getTripIndex(String tripId) {
		List<Trip> tripsList = getTrips();
		
		for (int i=0; i<tripsList.size(); ++i) {
			Trip trip = tripsList.get(i);
			if (trip.getId().equals(tripId))
				return i;
		}
		
		// The tripId was not found for this block so return -1
		return -1;		
	}
	
	/**
	 * Returns the index into the trips list of the specified trip.
	 * 
	 * @param trip Specifies which trip looking for
	 * @return Index into trips of the specified trip
	 */
	public int getTripIndex(Trip trip) {
		return getTripIndex(trip.getId());
	}
	
	/**
	 * Returns collection of route IDs associated with the Block. 
	 * Each route ID will only be included once.
	 * @return
	 */
	public Collection<String> getRoutes() {
		return routeIds;
//      Note: previously was getting them from trips but this requires a call
//      to getTrips() which loads in all the data from db for the trips for
//      the block, which is quite slow. By generating the routeIds when the
//      GTFS data is processed and storing them in the db then don't need
//      the application to load in all trip data, which is great for debugging.		
//		Set<String> routeIdsSet = new HashSet<String>();		
//		for (Trip trip : getTrips()) {
//			routeIdsSet.add(trip.getRouteId());
//		}		
//		return routeIdsSet;
	}
	
	/**
	 * Returns the trip patterns associated with the block. Note
	 * that these are not in any kind of fixed order since trip
	 * patterns are used multiple times in a block. 
	 * @return
	 */
	public Collection<TripPattern> getTripPatterns() {
		// Create map of trip patterns for this block so that can
		// return just a single copy of each one. Keyed on trip pattern ID
		Map<String, TripPattern> tripPatternsMap = new HashMap<String, TripPattern>();
		for (Trip trip : getTrips()) {
			TripPattern tripPattern = trip.getTripPattern();
			tripPatternsMap.put(tripPattern.getId(), tripPattern);
		}
		
		// Return the collection of unique trip patterns
		return tripPatternsMap.values();
	}

	/**
	 * Returns the travel time for the specified path. Does not include stop 
	 * times.
	 * 
	 * @param tripIndex
	 * @param stopPathIndex
	 * @return
	 */
	public int getStopPathTravelTime(int tripIndex, int stopPathIndex) {
		Trip trip = getTrip(tripIndex);
		TravelTimesForStopPath travelTimesForPath = 
				trip.getTravelTimesForStopPath(stopPathIndex);
		
		return travelTimesForPath.getStopPathTravelTimeMsec();
	}
	
	/**
	 * Returns the time in msec for how long expected to be at the stop
	 * at the end of the stop path.
	 * 
	 * @param tripIndex
	 * @param stopPathIndex
	 * @return
	 */
	public int getPathStopTime(int tripIndex, int stopPathIndex) {
		Trip trip = getTrip(tripIndex);
		TravelTimesForStopPath travelTimesForPath = 
				trip.getTravelTimesForStopPath(stopPathIndex);
		return travelTimesForPath.getStopTimeMsec();
	}
	
	/**
	 * Returns the Vector of the segment specified.
	 * 
	 * @param tripIndex
	 * @param stopPathIndex
	 * @param segmentIndex
	 * @return The Vector for the specified segment or null if the indices are
	 * out of range.
	 */
	public Vector getSegmentVector(int tripIndex, int stopPathIndex, 
			int segmentIndex) {
		Trip trip = getTrip(tripIndex);
		if (trip == null)
			return null;
		StopPath stopPath = trip.getStopPath(stopPathIndex);
		if (stopPath == null)
			return null;
		Vector segmentVector = stopPath.getSegmentVector(segmentIndex);
		return segmentVector;
	}

	/**
	 * Returns number of segments for the path specified
	 * 
	 * @param tripIndex
	 * @param stopPathIndex
	 * @return Number of segments for specified path or -1 if
	 * an index is out of range.
	 */
	public int numSegments(int tripIndex, int stopPathIndex) {
		// Determine number of segments
		Trip trip = getTrip(tripIndex);
		if (trip == null)
			return -1;
		StopPath path = trip.getStopPath(stopPathIndex);
		if (path == null)
			return -1;
		return path.getSegmentVectors().size();		
	}
	
	/**
	 * Returns number of stopPaths for the trip specified.
	 * 
	 * @param tripIndex
	 * @return Number of stopPaths for specified trip or -1 if index
	 * is out of range.
	 */
	public int numStopPaths(int tripIndex) {
		Trip trip = getTrip(tripIndex);
		if (trip == null)
			return -1;
		
		TravelTimesForTrip travelTimesForTrip = trip.getTravelTimes();
		List<TravelTimesForStopPath> travelTimesForStopPaths = 
				travelTimesForTrip.getTravelTimesForStopPaths();
		return travelTimesForStopPaths.size();		
	}

	/**
	 * Returns the number of trips.
	 * 
	 * @return
	 */
	public int numTrips() {
		return getTrips().size();
	}
	
	/**
	 * Returns true if path is for a stop that is configured to be layover stop
	 * for the trip pattern. A layover stop is when a vehicle can leave route
	 * path before departing this stop since the driver is taking a break.
	 * 
	 * @param tripIndex
	 * @param stopPathIndex
	 * @return
	 */
	public boolean isLayover(int tripIndex, int stopPathIndex) {
		return getStopPath(tripIndex, stopPathIndex).isLayoverStop();
	}
	
	/**
	 * Indicates that vehicle is not supposed to depart the stop until the
	 * scheduled departure time.
	 * 
	 * @return true if a wait stop
	 */
	public boolean isWaitStop(int tripIndex, int stopPathIndex) {
		return getStopPath(tripIndex, stopPathIndex).isWaitStop();
	}
	
	/**
	 * Returns the path specified by tripIndex and stopPathIndex params.
	 * 
	 * @param tripIndex
	 * @param stopPathIndex
	 * @return the StopPath or null if tripIndex or stopPathIndex are out of
	 *         range.
	 */
	public StopPath getStopPath(int tripIndex, int stopPathIndex) {
		// Get the trip
		Trip trip = getTrip(tripIndex);
		if (trip == null) {
			logger.error("In Block.getStopPath() tripIndex={} is out of range "
					+ "(stopPathIndex={}) for block={}", 
					tripIndex, stopPathIndex, this.toShortString());
			return null;
		}
			
		// Get the stop path
		StopPath stopPath = trip.getStopPath(stopPathIndex);
		if (stopPath == null) {
			logger.error("In Block.getStopPath() stopPathIndex={} is out of "
					+ "range for tripIndex={} trip={} of block={}",
					stopPathIndex, tripIndex, trip.toShortString(),
					this.toShortString());
			return null;
		}
		
		// Return the stop path
		return stopPath;
	}
	
	/**
	 * Returns the location of the first stop of the block.
	 * 
	 * @return
	 */
	public Location getStartLoc() {
		StopPath firstStopPath = getStopPath(0, 0);
		return firstStopPath.getEndOfPathLocation();
	}
	
	/**
	 * Returns the previous path specified by tripIndex and stopPathIndex
	 * params. If wrapping back past beginning of block (where
	 * tripIndex becomes negative) then returns null.
	 * 
	 * @param tripIndex
	 * @param stopPathIndex
	 * @return
	 */
	public StopPath getPreviousPath(int tripIndex, int stopPathIndex) {
		// First, determine trip and path index for the previous path
		--stopPathIndex;
		if (stopPathIndex < 0) {
			--tripIndex;
			if (tripIndex < 0)
				return null;
			stopPathIndex = 
					getTrip(tripIndex).getTripPattern().getStopPaths().size()-1;
		}
		
		// Return the previous path
		return getStopPath(tripIndex, stopPathIndex);
	}
	
	/**
	 * Returns the ScheduleTime for that stop specified by the trip and path
	 * indices.
	 * 
	 * @param tripIndex
	 * @param stopPathIndex
	 * @return the schedule time for the specified stop. Returns null if no
	 * schedule time associated with stop
	 */
	public ScheduleTime getScheduleTime(int tripIndex, int stopPathIndex) {
		Trip trip = getTrip(tripIndex);
		return trip.getScheduleTime(stopPathIndex);
	}
	
	/**
	 * Returns the specified headway for unscheduled blocks. A block is unscheduled
	 * if its trips are defined in the frequencies.txt file. If headway is 0 then
	 * there is no planned headway. The vehicles will run when they run. If 
	 * headway is less than 0 then it is a schedule based assignment.
	 * @return the headwaySecs
	 */
	public int getHeadwaySecs() {
		return headwaySecs;
	}
	
	/**
	 * @return true if block is schedule based as opposed to headway based
	 */
	public boolean isScheduleBased() {
		return headwaySecs < 0;
	}

	/**
	 * Returns true if on last trip of block and within the specified distance
	 * of the end of that last trip.
	 * 
	 * @param match
	 * @param distance
	 * @return True if within distance of end of block
	 */
	public boolean nearEndOfBlock(SpatialMatch match, double distance) {
		// If not last trip of block then not considered near end
		// so return false.
		if (match.getTripIndex() != trips.size()-1)
			return false;
		
		return match.withinDistanceOfEndOfTrip(distance);
	}
	
	/**
	 * Returns true if this block assignment should be exclusive, such that when
	 * a vehicle is assigned to this block any other vehicles assigned to this
	 * block will have their assignments removed.
	 * <p>
	 * Current it is configured using Java property instead of in the database.
	 * 
	 * @return True if this block assignment should be exclusively assigned to
	 *         only a single vehicle at a time
	 */
	public boolean shouldBeExclusive() {
		return CoreConfig.exclusiveBlockAssignments();
	}
	
}
