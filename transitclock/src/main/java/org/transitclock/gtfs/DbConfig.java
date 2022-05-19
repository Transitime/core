/*
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License (GPL) as published by the
 * Free Software Foundation, either version 3 of the License, or any later
 * version.
 * 
 * Transitime.org is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Transitime.org . If not, see <http://www.gnu.org/licenses/>.
 */
package org.transitclock.gtfs;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.BooleanConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.core.ServiceUtilsImpl;

import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.structs.Calendar;
import org.transitclock.db.structs.*;
import org.transitclock.utils.IntervalTimer;
import org.transitclock.utils.MapKey;
import org.transitclock.utils.Time;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Reads all the configuration data from the database. The data is based on GTFS
 * but is heavily processed into things like TripPatterns and better Paths to
 * make the data far easier to use.
 * <p>
 * DbConfig is intended for the core application such that the necessary top
 * level data can be read in at system startup. This doesn't read in all the
 * low-level data such as paths and travel times. Those items are very
 * voluminous and are therefore lazy loaded.
 * 
 * @author SkiBu Smith
 *
 */
public class DbConfig {

	public static final int MAX_PREVIOUS_CONFIG_REV_LOAD = 5;
	private final String agencyId;

	// Keeps track of which revision of config data was read in
	private int configRev;

	// Following is for all the data read from the database
	private List<Block> blocks;

	/**
	 * cache of known "configRev" ids.  ActiveRevision will be the
	 * currently loaded one.
	 */
	private List<ConfigRevision> configRevisions;

	// So can access blocks by service ID and block ID easily.
	// Keyed on serviceId. Submap keyed on blockId
	private Map<String, Map<String, Block>> blocksByServiceMap = null;

	/**
	 * For unit tests populate blocksByServiceMap.
	 * @param serviceId
	 * @param blockId
	 * @param block
	 */
	public void addBlockToServiceMap(String serviceId, String blockId, Block block) {
		Map<String, Block> blockMap = blocksByServiceMap.get(serviceId);
		if (blockMap == null) {
			blockMap = new HashMap<>();
			blocksByServiceMap.put(serviceId, blockMap);
		}
		blockMap.put(blockId, block);
	}

	// So can access blocks by service ID and route ID easily
	private Map<RouteServiceMapKey, List<Block>> blocksByRouteMap = null;

	// Ordered list of routes
	private List<Route> routes;
	// Keyed on routeId
	private Map<String, Route> routesByRouteIdMap;
	// Keyed on routeShortName
	private Map<String, Route> routesByRouteShortNameMap;
	// Keyed on stopiD
	private Map<String, Collection<Route>> routesListByStopIdMap;
	
	// Keyed on routeId
	private Map<String, List<TripPattern>> tripPatternsByRouteMap;

	// Keyed on tripPatternId
	private Map<String, TripPattern> tripPatternsByIdMap;

	private Map<String, List<String>> tripIdsByTripPatternMap;

	// For when reading in all trips from db. Keyed on tripId
	private Map<String, Trip> tripsMap;
	// For trips that have been read in individually. Keyed on tripId.
	private Map<String, Trip> individualTripsMap = new HashMap<String, Trip>();
	// For trips that have been read in individually. Keyed on trip short name.
	// Contains
	private Map<String, List<Trip>> individualTripsByShortNameMap =
			new HashMap<String, List<Trip>>();

	// cache of all tripIds to prevent queries for nonexistant trips
	private Set<String> tripIdSet = null;
	// cache of all tripNames to prevent queries for nonexistent trips
	private Set<String> tripNameSet = null;


	private List<Agency> agencies;
	private List<Calendar> calendars;
	private List<CalendarDate> calendarDates;
	// So can efficiently look up calendar dates
	private Map<Long, List<CalendarDate>> calendarDatesMap;
	private Map<String, Calendar> calendarByServiceIdMap;
	private List<FareAttribute> fareAttributes;
	private List<FareRule> fareRules;
	private List<Frequency> frequencies;
	private List<Transfer> transfers;
	private List<FeedInfo> feedInfo;
	private Map<String, Map<String, RouteDirection>> routeDirectionsByRoute;


	// Keyed by stop_id.
	private Map<String, Stop> stopsMap;

	/**
	 * for unit tests, set the underlying list of stops
	 * @param stops
	 */
	public void setStopsMap(Map<String, Stop> stops) {
		this.stopsMap = stops;
	}

	/**
	 * for unit tests, to test if setup is necessar;
	 * @return
	 */
	public boolean isEmptyStopsMap() {
		return stopsMap == null || stopsMap.isEmpty();
	}


	// Keyed by stop_code
	private Map<Integer, Stop> stopsByStopCode;
	
	// Remember the session. This is a bit odd because usually
	// close sessions but want to keep it open so can do lazy loading
	// and so that can read in TripPatterns later using the same session.
	private Session globalSession;

	private static final Logger logger = LoggerFactory
			.getLogger(DbConfig.class);

	private StringConfigValue validateTestQuery 
	= new StringConfigValue("transitclock.db.validateQuery", 
			"SELECT 1", 
			"query to validate database connection");

	public String getValidateTestQuery() {
		return validateTestQuery.getValue();
	}

	private BooleanConfigValue serviceIdSuffix = new BooleanConfigValue("transitclock.avl.serviceIdSuffix",
			false,"suffix tripId with serviceId");
	public boolean getServiceIdSuffix() { return serviceIdSuffix.getValue(); }
	
	/********************** Member Functions **************************/

	/**
	 * Constructor
	 * 
	 * @param agencyId
	 */
	public DbConfig(String agencyId) {
		this.agencyId = agencyId;
		new Thread(new ValidateSessionThread(this)).start();
	}

	/**
	 * Returns the global session used for lazy loading data. Useful for
	 * determining if the global session has changed.
	 * 
	 * @return the global session used for lazy loading of data
	 */
	public final Session getGlobalSession() {
		return globalSession;
	}
	
	/**
	 * For when the session dies, which happens when db failed over or rebooted.
	 * Idea is to create a new session that can be attached to persistent
	 * objects so can lazy load data.
	 */
	public void createNewGlobalSession() {
		logger.info("Creating a new session for agencyId={}", agencyId);
		HibernateUtils.clearSessionFactory();
		globalSession = HibernateUtils.getSession(agencyId);
	}
	
	/**
	 * Initiates the reading of the configuration data from the database. Calls
	 * actuallyReadData() which does all the work.
	 * <p>
	 * NOTE: exits system if config data could not be read in. This is done so
	 * that action will be taken to fix this issue.
	 * 
	 * @param configRev
	 */
	public void read(int configRev) {
		// For logging how long things take
		IntervalTimer timer = new IntervalTimer();

		// Let user know what is going on
		logger.info("Reading configuration database for configRev={}...",
				configRev);

		// Remember which revision of data is being used
		this.configRev = configRev;

		// Do the low-level processing
		try {
			actuallyReadData(configRev);
		} catch (HibernateException e) {
			logger.error("Error reading configuration data from db for "
					+ "configRev={}. NOTE: Exiting because could not read in "
					+ "data!!!!", configRev, e);
			
			System.exit(-1);
		} finally {
			// Usually would always make sure session gets closed. But
			// don't close session for now so can use lazy initialization
			// for Blocks->Trips. This way app starts up much faster which is
			// great
			// for testing.
			// session.close();
		}

		// Let user know what is going on
		logger.info("Finished reading configuration data from database . "
				+ "Took {} msec.", timer.elapsedMsec());
	}

	/**
	 * Creates a map of a map so that blocks can be looked up easily by service
	 * and block IDs.
	 * 
	 * @param blocks
	 *            List of blocks to be put into map
	 * @return Map keyed on service ID of map keyed on block ID of blocks
	 */
	private static Map<String, Map<String, Block>> putBlocksIntoMap(
			List<Block> blocks) {
		Map<String, Map<String, Block>> blocksByServiceMap =
				new HashMap<String, Map<String, Block>>();

		for (Block block : blocks) {
			Map<String, Block> blocksByBlockIdMap =
					blocksByServiceMap.get(block.getServiceId());
			if (blocksByBlockIdMap == null) {
				blocksByBlockIdMap = new HashMap<String, Block>();
				blocksByServiceMap
						.put(block.getServiceId(), blocksByBlockIdMap);
			}

			blocksByBlockIdMap.put(block.getId(), block);
		}

		return blocksByServiceMap;
	}

	private static class RouteServiceMapKey extends MapKey {
		private RouteServiceMapKey(String serviceId, String routeId) {
			super(serviceId, routeId);
		}

		@Override
		public String toString() {
			return "RouteServiceMapKey [" + "serviceId=" + o1 + ", routeId="
					+ o2 + "]";
		}
	}

	/**
	 * To be used by putBlocksIntoMapByRoute().
	 * 
	 * @param serviceId
	 * @param routeId
	 * @param block
	 */
	private static void addBlockToMapByRouteMap(
			Map<RouteServiceMapKey, List<Block>> blocksByRouteMap,
			String serviceId, String routeId, Block block) {
		RouteServiceMapKey key = new RouteServiceMapKey(serviceId, routeId);
		List<Block> blocksList = blocksByRouteMap.get(key);
		if (blocksList == null) {
			blocksList = new ArrayList<Block>();
			blocksByRouteMap.put(key, blocksList);
		}
		blocksList.add(block);
	}

	/**
	 * Takes in List of Blocks read from db and puts them into the
	 * blocksByRouteMap so that getBlocksForRoute() can be used to retrieve the
	 * list of blocks that are associated with a route for a specified service
	 * ID.
	 * 
	 * @param blocks
	 * @return the newly created blocksByRouteMap
	 */
	private static Map<RouteServiceMapKey, List<Block>>
			putBlocksIntoMapByRoute(List<Block> blocks) {
		Map<RouteServiceMapKey, List<Block>> blocksByRouteMap =
				new HashMap<RouteServiceMapKey, List<Block>>();

		for (Block block : blocks) {
			String serviceId = block.getServiceId();

			Collection<String> routeIdsForBlock = block.getRouteIds();
			for (String routeId : routeIdsForBlock) {
				// Add the block to the map by keyed serviceId and routeId
				addBlockToMapByRouteMap(blocksByRouteMap, serviceId, routeId,
						block);

				// Also add block to map using serviceId of null so that
				// can retrieve blocks for all service classes for a route
				// by using a service ID of null.
				addBlockToMapByRouteMap(blocksByRouteMap, null, routeId, block);
			}
		}

		return blocksByRouteMap;
	}

	/**
	 * Returns List of Blocks associated with the serviceId and routeId.
	 * 
	 * @param serviceId
	 *            Specified service ID that want blocks for. Can set to null to
	 *            blocks for all service IDs for the route.
	 * @param routeId
	 * @return List of Blocks. Null of no blocks for the serviceId and routeId
	 */
	public List<Block> getBlocksForRoute(String serviceId, String routeId) {
		RouteServiceMapKey key = new RouteServiceMapKey(serviceId, routeId);
		List<Block> blocksList = blocksByRouteMap.get(key);
		return blocksList;
	}

	/**
	 * Returns List of Blocks associated with the routeId for all service IDs.
	 * 
	 * @param routeId
	 * @return
	 */
	public List<Block> getBlocksForRoute(String routeId) {
		return getBlocksForRoute(null, routeId);
	}

	/**
	 * Converts the stops list into a map.
	 * 
	 * @param stopsList
	 *            To be converted
	 * @return The map, keyed on stop_id
	 */
	private static Map<String, Stop> putStopsIntoMap(List<Stop> stopsList) {
		Map<String, Stop> map = new HashMap<String, Stop>();
		for (Stop stop : stopsList) {
			map.put(stop.getId(), stop);
		}
		return map;
	}

	/**
	 * Converts the stops list into a map keyed by stop code.
	 * 
	 * @param stopsList
	 *            To be converted
	 * @return The map, keyed on stop_code
	 */
	private static Map<Integer, Stop> putStopsIntoMapByStopCode(List<Stop> stopsList) {
		Map<Integer, Stop> map = new HashMap<Integer, Stop>();
		for (Stop stop : stopsList) {
			Integer stopCode = stop.getCode();
			if (stopCode != null)
				map.put(stopCode, stop);
		}
		return map;
	}

	/**
	 * Returns the stop IDs for the specified route. Stop IDs can be included
	 * multiple times.
	 * 
	 * @param routeId
	 * @return collection of stop IDs for route
	 */
	private Collection<String> getStopIdsForRoute(String routeId) {
		Collection<String> stopIds = new ArrayList<String>(100);
		
		List<TripPattern> tripPatternsForRoute = tripPatternsByRouteMap.get(routeId);
		for (TripPattern tripPattern : tripPatternsForRoute) {
			for (String stopId : tripPattern.getStopIds()) {
				stopIds.add(stopId);
			}
		}
		
		return stopIds;
	}
	
	/**
	 * Returns map, keyed on stopId, or collection of routes. Allows one to
	 * determine all routes associated with a stop.
	 * 
	 * @param routes
	 * @return map, keyed on stopId, or collection of routes
	 */
	private Map<String, Collection<Route>> putRoutesIntoMapByStopId(
			List<Route> routes) {
		Map<String, Collection<Route>> map =
				new HashMap<String, Collection<Route>>();
		for (Route route : routes) {
			for (String stopId : getStopIdsForRoute(route.getId())) {
				Collection<Route> routesForStop = map.get(stopId);
				if (routesForStop == null) {
					routesForStop = new HashSet<Route>();
					map.put(stopId, routesForStop);
				}
				routesForStop.add(route);
			}
		}

		// Return the created map
		return map;
	}

	private static Map<String, TripPattern> putTripPatternsIntoMap(List<TripPattern> tripPatterns){
		Map<String, TripPattern> tripPatternMap = tripPatterns.stream()
				.collect(Collectors.toMap(TripPattern::getId, tripPattern -> tripPattern));

		return tripPatternMap;
	}


	/**
	 * Converts trip patterns into map keyed on route ID
	 *
	 * @param tripPatterns
	 * @return
	 */
	private static Map<String, List<TripPattern>> putTripPatternsIntoRouteMap(List<TripPattern> tripPatterns) {
		Map<String, List<TripPattern>> map =
				new HashMap<String, List<TripPattern>>();
		for (TripPattern tripPattern : tripPatterns) {
			String routeId = tripPattern.getRouteId();
			List<TripPattern> tripPatternsForRoute = map.get(routeId);
			if (tripPatternsForRoute == null) {
				tripPatternsForRoute = new ArrayList<TripPattern>();
				map.put(routeId, tripPatternsForRoute);
			}
			tripPatternsForRoute.add(tripPattern);
		}

		return map;
	}

	/**
	 * Returns the list of trip patterns associated with the specified route.
	 * Reads the trip patterns from the database and stores them in cache so
	 * that subsequent calls get them directly from the cache. The first time
	 * this is called it can take a few seconds. Therefore this is not done at
	 * startup since want startup to be quick.
	 * 
	 * @param routeId
	 * @return List of TripPatterns for the route, or null if no such route
	 */
	public List<TripPattern> getTripPatternsForRoute(String routeId) {
		// If haven't read in the trip pattern data yet, do so now and cache it
		if (tripPatternsByRouteMap == null) {
			logger.error("tripPatternsByRouteMap not set when "
					+ "getTripPatternsForRoute() called. Exiting!");
			System.exit(-1);
		}

		// Return cached trip pattern data
		return tripPatternsByRouteMap.get(routeId);
	}

	public TripPattern getTripPatternForId(String tripPatternId) {
		// If haven't read in the trip pattern data yet, do so now and cache it
		if (tripPatternsByIdMap == null) {
			logger.error("tripPatternsByIdMap not set when "
					+ "getTripPatternForId() called. Exiting!");
			System.exit(-1);
		}

		// Return cached trip pattern data
		return tripPatternsByIdMap.get(tripPatternId);
	}

	/**
	 * Returns the list of trip patterns associated with the specified route and headsign.
	 * Reads the trip patterns from the database and stores them in cache so
	 * that subsequent calls get them directly from the cache. The first time
	 * this is called it can take a few seconds. Therefore this is not done at
	 * startup since want startup to be quick.
	 *
	 * @param routeId
	 * @return List of TripPatterns for the route, or null if no such route
	 */
	public List<TripPattern> getTripPatternsForRouteAndHeadSign(String routeId, String headSign) {
		// If haven't read in the trip pattern data yet, do so now and cache it
		if (tripPatternsByRouteMap == null) {
			logger.error("tripPatternsByRouteMap not set when "
					+ "getTripPatternsForRoute() called. Exiting!");
			System.exit(-1);
		}

		// Return cached trip pattern data
		return tripPatternsByRouteMap.get(routeId).stream()
				.filter(tp -> tp.getHeadsign().equalsIgnoreCase(headSign))
				.collect(Collectors.toList());
	}

	public List<String> getTripIdsForTripPattern(String tripPatternId){
		if(tripIdsByTripPatternMap == null){
			getTrips();
		}
		return tripIdsByTripPatternMap.get(tripPatternId);
	}

	/**
	 * Returns cached map of all Trips. Can be slow first time accessed because
	 * it can take a while to read in all trips including all sub-data.
	 * 
	 * @return
	 */
	public Map<String, Trip> getTrips() {
		if (tripsMap == null) {
			IntervalTimer timer = new IntervalTimer();

			// Need to sync such that block data, which includes trip
			// pattern data, is only read serially (not read simultaneously
			// by multiple threads). Otherwise get a "force initialize loading
			// collection" error.
			synchronized (Block.getLazyLoadingSyncObject()) {
				logger.debug("About to load trips...");

				// Use the global session so that don't need to read in any
				// trip patterns that have already been read in as part of
				// reading in block assignments. This makes reading of the
				// trip pattern data much faster.
				tripsMap = Trip.getTrips(globalSession, configRev);
				tripIdsByTripPatternMap = new HashMap<>();
				for(Map.Entry<String, Trip> tripEntry: tripsMap.entrySet()){
					String tripPatternId = tripEntry.getValue().getTripPattern().getId();
					List<String> tripIdsForTripPattern = tripIdsByTripPatternMap.get(tripPatternId);
					if(tripIdsForTripPattern == null) {
						tripIdsForTripPattern = new ArrayList<>();
						tripIdsByTripPatternMap.put(tripPatternId, tripIdsForTripPattern);
					}
					tripIdsForTripPattern.add(tripEntry.getKey());
				}
			}
			logger.debug("Reading trips took {} msec", timer.elapsedMsec());
		}

		// Return cached trip data
		return tripsMap;
	}

	/**
	 * For more quickly getting a trip. If trip not already read in yet it only
	 * reads in the specific trip from the db, not all trips like getTrips().
	 * If trip ID not found then sees if can match to a trip short name.
	 * 
	 * @param tripIdOrShortName
	 * @return The trip, or null if no such trip
	 */
	public Trip getTrip(String tripIdOrShortName) {
		return getTripFromCurrentOrPreviousConfigRev(tripIdOrShortName, 0);
	}

	public Trip getTripFromCurrentOrPreviousConfigRev(String tripIdOrShortName) {
		return getTripFromCurrentOrPreviousConfigRev(tripIdOrShortName, MAX_PREVIOUS_CONFIG_REV_LOAD);
	}

	public Trip getTripFromCurrentOrPreviousConfigRev(String tripIdOrShortName, int previousConfigRevsToCheck) {
		Trip trip = individualTripsMap.get(tripIdOrShortName);

		// If trip not read in yet, do so now
		if (trip == null) {

			// make sure this trip really exists before we try to load
			if (!getTripNameSet().contains(tripIdOrShortName)) {
				if (!getTripIdSet().contains(tripIdOrShortName)) {
					// perhaps it was a previous configRev
					logger.debug("requested {} trip no longer exists", tripIdOrShortName);
					return null;
				}
			}

			logger.debug("Trip for tripIdOrShortName={} not read from db yet "
					+ "so reading it now.", tripIdOrShortName);
			
			// Need to sync such that block data, which includes trip
			// pattern data, is only read serially (not read simultaneously
			// by multiple threads). Otherwise get a "force initialize loading
			// collection" error.
			synchronized (Block.getLazyLoadingSyncObject()) {
				trip = Trip.getTrip(globalSession, configRev, tripIdOrShortName);
				int configRevToTry = 0;
				while (trip == null && configRevToTry < previousConfigRevsToCheck) {
					 configRevToTry++;
					 trip = Trip.getTrip(globalSession, configRev - configRevToTry , tripIdOrShortName);
					 if (trip != null) {
						 logger.info("loaded trip {} that was {} rev behind current", tripIdOrShortName, configRevToTry);
						 getTripNameSet().add(tripIdOrShortName);
						 getTripIdSet().add(tripIdOrShortName);
					 }
				}
			}
			if (trip != null)
				individualTripsMap.put(tripIdOrShortName, trip);
		}

		// If couldn't get trip by tripId then see if using the trip short name.
		if (trip == null) {
			logger.debug("Could not find tripId={} so seeing if there is a "
					+ "tripShortName with that ID.", tripIdOrShortName);
			trip = getTripUsingTripShortName(tripIdOrShortName);
			
			// If the trip successfully read in it also needs to be added to 
			// individualTripsMap so that it doesn't need to be read in next
			// time getTrip() is called.
			if (trip != null) {
				logger.debug("Read tripIdOrShortName={} from db", tripIdOrShortName);
				individualTripsMap.put(trip.getId(), trip);
			}				
		}
		
		return trip;
	}

	/**
	 * cache of all trip names for this configRev.
	 * @return Set of tripNames
	 */
	private Set<String> getTripNameSet() {
		if (tripNameSet == null) {
			synchronized (Block.getLazyLoadingSyncObject()) {
				// check to see if we won the lock
				if (tripNameSet != null) return tripNameSet;
				IntervalTimer tick = new IntervalTimer();
				logger.info("loading tripShortName Cache....");
				String hql = "select tripShortName FROM Trip t " +
						"    WHERE t.configRev in (" + generateConfigRevStr(configRev) + ")";
				Query query = globalSession.createQuery(hql);


				// Actually perform the query
				tripNameSet = new HashSet<String>(query.list());
				logger.info("tripShortName cache loaded in {}", tick.elapsedMsec());
			}
		}
		return tripNameSet;
	}

	private String generateConfigRevStr(int configRev) {
		int i = 0;
		StringBuffer sb = new StringBuffer();
		while (i < MAX_PREVIOUS_CONFIG_REV_LOAD) {
			i++;
			sb.append(configRev - i)
							.append(",");
		}
		return sb.substring(0, sb.length()-1);
	}

	/**
	 * cache of all trip ids for this configRev.
	 * @return Set of tripIds
	 */

	private Set<String> getTripIdSet() {
		if (tripIdSet == null) {
			synchronized (Block.getLazyLoadingSyncObject()) {
				// check to see if we won the lock
				if (tripIdSet != null) return tripIdSet;
				IntervalTimer tick = new IntervalTimer();
				logger.info("loading tripId Cache....");
				String hql = "select tripId FROM Trip t " +
						"    WHERE t.configRev = :configRev";
				Query query = globalSession.createQuery(hql);
				query.setInteger("configRev", configRev);

				// Actually perform the query
				tripIdSet = new HashSet<String>(query.list());
				logger.info("tripId cache loaded in {}", tick.elapsedMsec());
			}
		}
		return tripIdSet;
	}

	/**
	 * Looks through the trips passed in and returns the one that has
	 * a service ID that is currently valid.
	 * 
	 * @param trips
	 * @return trip whose service ID is currently valid
	 */
	private static Trip getTripForCurrentService(List<Trip> trips) {
		Date now = Core.getInstance().getSystemDate();
		Collection<String> currentServiceIds = 
				Core.getInstance().getServiceUtils().getServiceIds(now);
		for (Trip trip : trips) {
			for (String serviceId : currentServiceIds) {
				if (trip.getServiceId().equals(serviceId)) {
					// Found a service ID match so return this trip 
					return trip;
				}
			}
		}
		
		// No such trip is currently active
		return null;
	}
	
	/**
	 * For more quickly getting a trip. If trip not already read in yet it only
	 * reads in the specific trip from the db, not all trips like getTrips().
	 * 
	 * @param tripShortName
	 * @return
	 */
	public Trip getTripUsingTripShortName(String tripShortName) {
		// Find trip with the tripShortName with a currently active service ID
		// from the map. If found, return it.
		List<Trip> trips = individualTripsByShortNameMap.get(tripShortName);
		if (trips != null) {
			Trip trip = getTripForCurrentService(trips);
			if (trip != null) {
				logger.debug("Read in trip using tripShortName={}", tripShortName);
				
				return trip;
			} else {
				// Trips for the tripShortName already read in but none valid
				// for the current service IDs so return null
				logger.debug("When reading tripShortName={} found trips "
						+ "but not for current service.", tripShortName);
				return null;
			}
		}

		// if we got this far, its possible the trip doesn't exist.  Check cache
		// before making expensive call to database
		if (!getTripNameSet().contains(tripShortName)) {
			logger.debug("request for non-existant trip {}", tripShortName);
			return null;
		}
		logger.info("FIXME tripShortName={} not yet read from db so reading it in now ",
				tripShortName);
		
		// Trips for the short name not read in yet, do so now
		// Need to sync such that block data, which includes trip
		// pattern data, is only read serially (not read simultaneously
		// by multiple threads). Otherwise get a "force initialize loading
		// collection" error.
		synchronized (Block.getLazyLoadingSyncObject()) {
			trips =	Trip.getTripByShortName(globalSession, configRev,
							tripShortName);
		}

		// Add the newly read trips to the map
		individualTripsByShortNameMap.put(tripShortName, trips);

		return getTripForCurrentService(trips);
	}

	/**
	 * Creates a map of routes keyed by route ID so that can easily find a route
	 * using its ID.
	 * 
	 * @param routes
	 * @return
	 */
	private static Map<String, Route> putRoutesIntoMapByRouteId(
			List<Route> routes) {
		// Convert list of routes to a map keyed on routeId
		Map<String, Route> routesMap = new HashMap<String, Route>();
		for (Route route : routes) {
			routesMap.put(route.getId(), route);
		}
		return routesMap;
	}

	/**
	 * Creates a map of routes keyed by route short name so that can easily find
	 * a route.
	 * 
	 * @param routes
	 * @return
	 */
	private static Map<String, Route> putRoutesIntoMapByRouteShortName(
			List<Route> routes) {
		// Convert list of routes to a map keyed on routeId
		Map<String, Route> routesMap = new HashMap<String, Route>();
		for (Route route : routes) {
			routesMap.put(route.getShortName(), route);
		}
		return routesMap;
	}

	/**
	 * Reads the individual data structures from the database.
	 * 
	 * @param configRev
	 */
	private void actuallyReadData(int configRev) {
		IntervalTimer timer;

		// Open up Hibernate session so can read in data. Remember this
		// session as a member variable. This is a bit odd because usually
		// close sessions but want to keep it open so can do lazy loading
		// and so that can read in TripPatterns later using the same session.
		globalSession = HibernateUtils.getSession(agencyId);

		// // NOTE. Thought that it might speed things up if would read in
		// // trips, trip patterns, and stopPaths all at once so that can use a
		// single
		// // query instead of one for each trip or trip pattern when block data
		// is
		// // read in. But surprisingly it didn't speed up the overall queries.
		// // Yes, reading in trips and trip patterns first means that reading
		// // in blocks takes far less time. But the total time for reading
		// // everything in stays the same. The tests were done on a laptop
		// // that both contained DbConfig program plus the database. So
		// // should conduct this test again with the database on a different
		// // server because perhaps then reading in trips and trip patterns
		// // first might make a big difference.
		// timer = new IntervalTimer();
		// List<Trip> trips = Trip.getTrips(session, configRev);
		// System.out.println("Reading trips took " + timer.elapsedMsec() +
		// " msec");
		//
		// timer = new IntervalTimer();
		// tripPatterns = TripPattern.getTripPatterns(session, configRev);
		// System.out.println("Reading trip patterns took " +
		// timer.elapsedMsec() + " msec");
		//
		// timer = new IntervalTimer();
		// stopPaths = StopPath.getPaths(session, configRev);
		// logger.debug("Reading stopPaths took {} msec", timer.elapsedMsec());

		timer = new IntervalTimer();
		blocks = Block.getBlocks(globalSession, configRev);
		for (Block block : blocks) {
			for (Trip trip : block.getTrips()) {
				// build up internal cache now to avoid lazy-load lag
				individualTripsMap.put(trip.getId(), trip);
			}
		}
		configRevisions = ConfigRevision.getConfigRevisions(globalSession, configRev);
		blocksByServiceMap = putBlocksIntoMap(blocks);
		blocksByRouteMap = putBlocksIntoMapByRoute(blocks);
		logger.debug("Reading blocks took {} msec", timer.elapsedMsec());

		timer = new IntervalTimer();
		routes = Route.getRoutes(globalSession, configRev);
		routesByRouteIdMap = putRoutesIntoMapByRouteId(routes);
		routesByRouteShortNameMap = putRoutesIntoMapByRouteShortName(routes);
		logger.debug("Reading routes took {} msec", timer.elapsedMsec());

		timer = new IntervalTimer();
		List<TripPattern> tripPatterns = TripPattern.getTripPatterns(globalSession, configRev);
		tripPatternsByRouteMap = putTripPatternsIntoRouteMap(tripPatterns);
		tripPatternsByIdMap = putTripPatternsIntoMap(tripPatterns);
		logger.debug("Reading trip patterns took {} msec", timer.elapsedMsec());
		
		timer = new IntervalTimer();
		List<Stop> stopsList = Stop.getStops(globalSession, configRev);
		stopsMap = putStopsIntoMap(stopsList);
		stopsByStopCode = putStopsIntoMapByStopCode(stopsList);
		routesListByStopIdMap = putRoutesIntoMapByStopId(routes);
		logger.debug("Reading stops took {} msec", timer.elapsedMsec());

		timer = new IntervalTimer();
		agencies = Agency.getAgencies(globalSession, configRev);
		calendars = Calendar.getCalendars(globalSession, configRev);
		calendarDates = CalendarDate.getCalendarDates(globalSession, configRev);

		calendarByServiceIdMap = new HashMap<>();
		for (Calendar calendar : calendars) {
			if(calendarByServiceIdMap.get(calendar.getServiceId()) == null){
				calendarByServiceIdMap.put(calendar.getServiceId(), calendar);
			} else{
				logger.warn("Duplicate Service Id {} in Calendar", calendar.getServiceId());
			}
		}

		calendarDatesMap = new HashMap<>();
		for (CalendarDate calendarDate : calendarDates) {
			Long time = calendarDate.getTime();
			List<CalendarDate> calendarDatesForDate = calendarDatesMap.get(time);
			if (calendarDatesForDate == null) {
				calendarDatesForDate = new ArrayList<CalendarDate>(1);
				calendarDatesMap.put(time, calendarDatesForDate);
			}
			calendarDatesForDate.add(calendarDate);
		}
		
		fareAttributes =
				FareAttribute.getFareAttributes(globalSession, configRev);
		fareRules = FareRule.getFareRules(globalSession, configRev);
		frequencies = Frequency.getFrequencies(globalSession, configRev);
		transfers = Transfer.getTransfers(globalSession, configRev);
		feedInfo = FeedInfo.getFeedInfo(globalSession, configRev);


		List<RouteDirection> routeDirections =  RouteDirection.getRouteDirection(globalSession, configRev);
		routeDirectionsByRoute = new HashMap<>();

		for(RouteDirection routeDirection : routeDirections){
			Map<String, RouteDirection> routeDirectionByDirection =
					routeDirectionsByRoute.get(routeDirection.getRouteShortName());
			if(routeDirectionByDirection == null){
				routeDirectionByDirection = new HashMap<>();
				routeDirectionsByRoute.put(routeDirection.getRouteShortName(), routeDirectionByDirection);
			}
			routeDirectionByDirection.put(routeDirection.getDirectionId(),routeDirection);
		}

		logger.debug("Reading everything else took {} msec",
				timer.elapsedMsec());
	}

	/************************** Getter Methods ***************************/

	/**
	 * Returns the block specified by the service and block ID parameters.
	 * 
	 * @param serviceId
	 *            Specified which service class to look for block. If null then
	 *            will use the first service class for the current time that has
	 *            the specified block.
	 * @param blockId
	 *            Specifies which block to return
	 * @return The block for the service and block IDs specified, or null if no
	 *         such block.
	 */
	public Block getBlock(String serviceId, String blockId) {
		// If service ID not specified then use today's. This way
		// makes it easier to find block info
		if (serviceId != null) {
			// For determining blocks for the service
			Map<String, Block> blocksMap = blocksByServiceMap.get(serviceId);

			// If no such service class defined for the blocks then return
			// null. This can happen if service classes are defined that
			// even though no blocks use that service class, such as when
			// working with a partial configuration.
			if (blocksMap == null)
				return null;

			return blocksMap.get(blockId);
		} else {
			// Service ID was not specified so determine current ones for now
			Date now = Core.getInstance().getSystemDate();

			Collection<String> currentServiceIds =
					Core.getInstance().getServiceUtils().getServiceIds(now);
			for (String currentServiceId : currentServiceIds) {
				Block block = getBlock(currentServiceId, blockId);
				if (block != null)
					return block;
			}

			// Couldn't find that block ID for any of the current service IDs
			return null;
		}

	}

    public int getBlockCount(){
        int blockCount = 0;
        for(String serviceId : blocksByServiceMap.keySet()){
            blockCount += (blocksByServiceMap.get(serviceId) != null
                    ? blocksByServiceMap.get(serviceId).size() : 0);
        }
        return blockCount;
    }

	/**
	 * Returns blocks for the specified blockId for all service IDs.
	 * 
	 * @param blockId
	 *            Which blocks to return
	 * @return Collection of blocks
	 */
	public Collection<Block> getBlocksForAllServiceIds(String blockId) {
		Collection<Block> blocks = new ArrayList<Block>();

		Collection<String> serviceIds = blocksByServiceMap.keySet();
		for (String serviceId : serviceIds) {
			Block block = getBlock(serviceId, blockId);
			if (block != null)
				blocks.add(block);
		}

		return blocks;
	}

	/**
	 * Returns unmodifiable collection of blocks associated with the specified
	 * serviceId.
	 * 
	 * @param serviceId
	 * @return Blocks associated with service ID. If no blocks then an empty
	 *         collection is returned instead of null.
	 */
	public Collection<Block> getBlocks(String serviceId) {
		Map<String, Block> blocksForServiceMap =
                blocksByServiceMap.get(serviceId);
		if (blocksForServiceMap != null) {
			Collection<Block> blocksForService = blocksForServiceMap.values();
			return Collections.unmodifiableCollection(blocksForService);
		} else {
			return new ArrayList<Block>(0);
		}
	}

	/**
	 * Returns unmodifiable list of blocks for the agency.
	 * 
	 * @return blocks for the agency
	 */
	public List<Block> getBlocks() {
		return Collections.unmodifiableList(blocks);
	}

	/**
	 * Expose metadata about data loaded.
	 * @return
	 */
	public List<ConfigRevision> getConfigRevisions() {
		return Collections.unmodifiableList(configRevisions);
	}

	/**
	 * Returns Map of routesMap keyed on the routeId.
	 * 
	 * @return
	 */
	public Map<String, Route> getRoutesByRouteIdMap() {
		return Collections.unmodifiableMap(routesByRouteIdMap);
	}

	/**
	 * Returns ordered list of routes.
	 * 
	 * @return
	 */
	public List<Route> getRoutes() {
		return Collections.unmodifiableList(routes);
	}

	/**
	 * Returns the Route with the specified routeId.
	 * 
	 * @param routeId
	 * @return The Route specified by the ID, or null if no such route
	 */
	public Route getRouteById(String routeId) {
		return routesByRouteIdMap.get(routeId);
	}

	/**
	 * Returns the Route with the specified routeShortName
	 * 
	 * @param routeShortName
	 * @return The route, or null if route doesn't exist
	 */
	public Route getRouteByShortName(String routeShortName) {
		return routesByRouteShortNameMap.get(routeShortName);
	}

	/**
	 * Returns the Stop with the specified stopId.
	 * 
	 * @param stopId
	 * @return The stop, or null if no such stop
	 */
	public Stop getStop(String stopId) {
		return stopsMap.get(stopId);
	}

	/**
	 * Returns the Stop with the specified stopCode.
	 * 
	 * @param stopCode
	 * @return The stop, or null if no such stop
	 */
	public Stop getStop(Integer stopCode) {
		return stopsByStopCode.get(stopCode);
	}
	
	/**
	 * Returns collection of routes that use the specified stop.
	 * 
	 * @param stopId
	 * @return collection of routes for the stop
	 */
	public Collection<Route> getRoutesForStop(String stopId) {
		return routesListByStopIdMap.get(stopId);
	}
	
	/**
	 * Returns list of all calendars
	 * @return calendars
	 */
	public List<Calendar> getCalendars() {
		return Collections.unmodifiableList(calendars);
	}

	/**
	 * Returns list of calendars that are currently active
	 * @return current calendars
	 */
	public List<Calendar> getCurrentCalendars() {
		// Get list of currently active calendars
		ServiceUtilsImpl serviceUtils = Core.getInstance().getServiceUtils();
		List<Calendar> calendarList =
				serviceUtils.getCurrentCalendars(Core.getInstance().getSystemTime());
		return calendarList;
	}
	
	/**
	 * Returns list of all calendar dates from the GTFS calendar_dates.txt file.
	 * 
	 * @return list of calendar dates
	 */
	public List<CalendarDate> getCalendarDates() {
		return Collections.unmodifiableList(calendarDates);
	}

	/**
	 * Returns CalendarDate for the current day. This method is pretty quick
	 * since it looks through a hashmap, instead of doing a linear search
	 * through a possibly very large number of dates.
	 * 
	 * @return CalendarDate for current day if there is one, otherwise null.
	 */
	public List<CalendarDate> getCalendarDatesForNow() {
		long startOfDay = 
				Time.getStartOfDay(Core.getInstance().getSystemDate());
		return calendarDatesMap.get(startOfDay);
	}
	
	/**
	 * Returns CalendarDate for the current day. This method is pretty quick
	 * since it looks through a hashmap, instead of doing a linear search
	 * through a possibly very large number of dates.
	 * 
	 * @param epochTime
	 *            the time that want calendar dates for
	 * @return CalendarDate for current day if there is one, otherwise null.
	 */
	public List<CalendarDate> getCalendarDates(Date epochTime) {
		long startOfDay = Time.getStartOfDay(epochTime);
		return calendarDatesMap.get(startOfDay);
	}

	/**
	 * Returns list of all service IDs
	 * @return service IDs
	 */
	public List<String> getServiceIds() {
		List<String> serviceIds = new ArrayList<String>();
		for (Calendar calendar : getCalendars()) {
			serviceIds.add(calendar.getServiceId());
		}
		return serviceIds;
	}
	
	/**
	 * Returns list of service IDs that are currently active
	 * @return current service IDs
	 */
	public List<String> getCurrentServiceIds() {
		List<String> serviceIds = new ArrayList<String>();
		for (Calendar calendar : getCurrentCalendars()) {
			serviceIds.add(calendar.getServiceId());
		}
		return serviceIds;
	}

	public Calendar getCalendarByServiceId(String serviceId) {
		return calendarByServiceIdMap.get(serviceId);
	}

	/**
	 * There can be multiple agencies but usually there will be just one. For
	 * getting timezone and such want to be able to easily access the main
	 * agency, hence this method.
	 * 
	 * @return The first agency, or null if no agencies configured
	 */
	public Agency getFirstAgency() {		
		return agencies.size() > 0 ? agencies.get(0) : null;
	}

	public List<Agency> getAgencies() {
		return Collections.unmodifiableList(agencies);
	}

	public String getDirectionName(String routeShortName, String directionId){
		Map<String, RouteDirection> routeDirectionByDirectionId = routeDirectionsByRoute.get(routeShortName);
		if(routeDirectionByDirectionId != null){
			RouteDirection routeDirection = routeDirectionByDirectionId.get(directionId);
			if(routeDirection != null){
				return routeDirection.getDirectionName();
			}
		}
		return null;
	}

	/**
	 * Returns the database revision of the configuration data that was read in.
	 * 
	 * @return The db rev
	 */
	public int getConfigRev() {
		return configRev;
	}

	/**
	 * Output contents of collection to stdout. For debugging.
	 * 
	 * @param name
	 * @param list
	 */
	private static void outputCollection(String name, Collection<?> list) {
		if (list == null)
			return;

		System.out.println("\n" + name + ": ");
		for (Object o : list) {
			System.out.println(" " + o);
		}
	}

	@SuppressWarnings("unused")
	private static class ValidateSessionThread implements Runnable {

		private DbConfig service;
		public ValidateSessionThread(DbConfig service) {
			this.service = service;
		}
		
		@Override
		public void run() {
			DbConfig dbConfig = Core.getInstance().getDbConfig();
			
			while (!Thread.interrupted()) {
				Time.sleep(60 * 1000);
				try {
					SQLQuery query = service.getGlobalSession().createSQLQuery(dbConfig.getValidateTestQuery());
					query.list();
					logger.debug("session test success");
				} catch (Throwable t) {
					logger.error("session test failure: {} {}", t, t);
					// the only reason this validate query should fail is if
					// our db connnection is invalid 
					// log the issue for now
					// eventually flush connection pool or give other hints
				}
				
			}
		}
	}
	
	/**
	 * For debugging.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		String projectId = "1";

		int configRev = ActiveRevisions.get(projectId).getConfigRev();

		DbConfig dbConfig = new DbConfig(projectId);
		dbConfig.read(configRev);

		Block block0 = dbConfig.blocks.get(0);
		Trip trip0 = block0.getTrips().get(0);
		TravelTimesForTrip ttft = trip0.getTravelTimes();
		@SuppressWarnings("unused")
		TravelTimesForStopPath tte0 = ttft.getTravelTimesForStopPath(0);
		TripPattern tp = trip0.getTripPattern();
		@SuppressWarnings("unused")
		StopPath path0 = tp.getStopPath(0);

		outputCollection("Blocks", dbConfig.blocks);
		outputCollection("Routes", dbConfig.routesByRouteIdMap.values());
		outputCollection("Stops", dbConfig.stopsMap.values());
		outputCollection("Agencies", dbConfig.agencies);
		outputCollection("Calendars", dbConfig.calendars);
		outputCollection("CalendarDates", dbConfig.calendarDates);
		outputCollection("FareAttributes", dbConfig.fareAttributes);
		outputCollection("FareRules", dbConfig.fareRules);
		outputCollection("Frequencies", dbConfig.frequencies);
		outputCollection("Transfers", dbConfig.transfers);
		outputCollection("FeedInfo", dbConfig.feedInfo);
		outputCollection("RouteDirection", dbConfig.routeDirectionsByRoute.values());
	}
}
