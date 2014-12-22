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
package org.transitime.gtfs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.db.hibernate.HibernateUtils;
import org.transitime.db.structs.ActiveRevisions;
import org.transitime.db.structs.Agency;
import org.transitime.db.structs.Block;
import org.transitime.db.structs.Calendar;
import org.transitime.db.structs.CalendarDate;
import org.transitime.db.structs.FareAttribute;
import org.transitime.db.structs.FareRule;
import org.transitime.db.structs.Frequency;
import org.transitime.db.structs.StopPath;
import org.transitime.db.structs.Route;
import org.transitime.db.structs.Stop;
import org.transitime.db.structs.Transfer;
import org.transitime.db.structs.TravelTimesForStopPath;
import org.transitime.db.structs.TravelTimesForTrip;
import org.transitime.db.structs.Trip;
import org.transitime.db.structs.TripPattern;
import org.transitime.utils.IntervalTimer;
import org.transitime.utils.MapKey;

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

	private final String agencyId;

	// Keeps track of which revision of config data was read in
	private int configRev;
	
	// Following is for all the data read from the database
	private List<Block> blocks;
	
	// So can access blocks by service ID and block ID easily
	private Map<String, Map<String, Block>> blocksByServiceMap = null;
	
	// So can access blocks by service ID and route ID easily
	private Map<RouteServiceMapKey, List<Block>> blocksByRouteMap = null;
	
	private List<Route> routes;
	// Keyed on routeId
	private Map<String, Route> routesByRouteIdMap;
	// Keyed on routeShortName
	private Map<String, Route> routesByRouteShortNameMap;
	// Keyed on routeId
	private Map<String, List<TripPattern>> tripPatternsByRouteMap;
	// For when reading in all trips from db. Keyed on tripId
	private Map<String, Trip> tripsMap;
	// For trips that have been read in individually. Keyed on tripId.
	private Map<String, Trip> individualTripsMap = new HashMap<String, Trip>();
	// For trips that have been read in individually. Keyed on trip short name
	private Map<String, Trip> individualTripsByShortNameMap = 
			new HashMap<String, Trip>();
	
	private List<Agency> agencies;
	private List<Calendar> calendars;
	private List<CalendarDate> calendarDates;
	private List<FareAttribute> fareAttributes;
	private List<FareRule> fareRules;
	private List<Frequency> frequencies;
	private List<Transfer> transfers;

	// Seems that stops not really needed because already in stopPaths. Unless 
	// trying to determine nearest stops.
	private Map<String, Stop> stopsMap;

	// Remember the session. This is a bit odd because usually
	// close sessions but want to keep it open so can do lazy loading
	// and so that can read in TripPatterns later using the same session.
	private Session globalSession;

	private static final Logger logger = 
			LoggerFactory.getLogger(DbConfig.class);

	/********************** Member Functions **************************/

	/**
	 * Constructor
	 * 
	 * @param agencyId
	 */
	public DbConfig(String agencyId) {
		this.agencyId = agencyId;
	}
		
	/**
	 * Initiates the reading of the configuration data from the database.
	 * Calls actuallyReadData() which does all the work.
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
			logger.error("Error reading configuration data from db for " +
					"configRev={}.", configRev);
		} finally {
			// Usually would always make sure session gets closed. But
			// don't close session for now so can use lazy initialization
			// for Blocks->Trips. This way app starts up much faster which is great
			// for testing.
			//session.close();
		}

		// Let user know what is going on
		logger.info("Finished reading configuration data from database . " +
				"Took {} msec.", timer.elapsedMsec());		
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
			return "RouteServiceMapKey [" 
					+ "serviceId=" + o1 
					+ ", routeId=" + o2 
					+ "]";
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
	private static Map<RouteServiceMapKey, List<Block>> putBlocksIntoMapByRoute(
			List<Block> blocks) {
		Map<RouteServiceMapKey, List<Block>> blocksByRouteMap = 
				new HashMap<RouteServiceMapKey, List<Block>>();
		
		for (Block block : blocks) {
			String serviceId = block.getServiceId();
			
			Collection<String> routeIdsForBlock = block.getRoutes();
			for (String routeId : routeIdsForBlock)
				addBlockToMapByRouteMap(blocksByRouteMap, serviceId, routeId,
						block);
		}
		
		return blocksByRouteMap;
	}

	/**
	 * Returns List of Blocks associated with the serviceId and routeId.
	 * 
	 * @param serviceId
	 * @param routeId
	 * @return List of Blocks. Null of no blocks for the serviceId and routeId
	 */
	public List<Block> getBlocksForRoute(String serviceId, String routeId) {
		// Read in data if it hasn't been read in yet. This isn't done at 
		// startup since it causes all trips and travel times to be read in,
		// which of course takes a while. Therefore only want to do this
		// if the blocksByRouteMap is actually being used, which is probably
		// only for situations where AVL feed provides route IDs instead
		// block assignments.
		if (blocksByRouteMap == null) {
			blocksByRouteMap = putBlocksIntoMapByRoute(blocks);
		}

		RouteServiceMapKey key = new RouteServiceMapKey(serviceId, routeId);
		List<Block> blocksList = blocksByRouteMap.get(key);
		return blocksList;
	}
	
	/**
	 * Converts the stops list into a map.
	 * 
	 * @param stopsList
	 *            To be converted
	 * @return The map
	 */
	private static Map<String, Stop> putStopsIntoMap(List<Stop> stopsList) {
		Map<String, Stop> map = new HashMap<String, Stop>();
		for (Stop stop : stopsList) {
			map.put(stop.getId(), stop);
		}
		return map;
	}
	
	/**
	 * Converts trip patterns into map keyed on route ID
	 * @param tripPatterns
	 * @return
	 */
	private static Map<String, List<TripPattern>> putTripPatternsIntoMap(
			List<TripPattern> tripPatterns) {
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
			IntervalTimer timer = new IntervalTimer();

			// Need to sync such that block data, which includes trip
			// pattern data, is only read serially (not read simultaneously
			// by multiple threads). Otherwise get a "force initialize loading
			// collection" error. 
			synchronized (Block.getLazyLoadingSyncObject()) {
				logger.debug("About to load trip patterns...");
				
				// Use the global session so that don't need to read in any
				// trip patterns that have already been read in as part of
				// reading in block assignments. This makes reading of the
				// trip pattern data much faster.
				List<TripPattern> tripPatterns = TripPattern.getTripPatterns(
						globalSession, configRev);
				tripPatternsByRouteMap = putTripPatternsIntoMap(tripPatterns);
			}
			logger.debug("Reading trip patterns took {} msec", 
					timer.elapsedMsec());
		}
		
		// Return cached trip pattern data
		return tripPatternsByRouteMap.get(routeId);
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
			}
			logger.debug("Reading trips took {} msec", timer.elapsedMsec());
		}
		
		// Return cached trip data
		return tripsMap;
	}
	
	/**
	 * For more quickly getting a trip. If trip not already read in yet it only
	 * reads in the specific trip from the db, not all trips like getTrips().
	 * 
	 * @param tripId
	 * @return The trip, or null if no such trip
	 */
	public Trip getTrip(String tripId) {
		Trip trip = individualTripsMap.get(tripId);
		
		// If trip not read in yet, do so now
		if (trip == null) {
			// Need to sync such that block data, which includes trip
			// pattern data, is only read serially (not read simultaneously
			// by multiple threads). Otherwise get a "force initialize loading
			// collection" error. 
			synchronized (Block.getLazyLoadingSyncObject()) {
				trip = Trip.getTrip(globalSession, configRev, tripId);
			}
			individualTripsMap.put(tripId, trip);
		}
		
		return trip;
	}
	
	/**
	 * For more quickly getting a trip. If trip not already read in yet it only
	 * reads in the specific trip from the db, not all trips like getTrips().
	 * 
	 * @param tripShortName
	 * @return
	 */
	public Trip getTripUsingTripShortName(String tripShortName) {
		Trip trip = individualTripsByShortNameMap.get(tripShortName);
		
		// If trip not read in yet, do so now
		if (trip == null) {
			// Need to sync such that block data, which includes trip
			// pattern data, is only read serially (not read simultaneously
			// by multiple threads). Otherwise get a "force initialize loading
			// collection" error. 
			synchronized (Block.getLazyLoadingSyncObject()) {
				trip = Trip.getTripByShortName(globalSession, configRev, tripShortName);
			}
			individualTripsByShortNameMap.put(tripShortName, trip);
		}
		
		return trip;		
	}
	
	/**
	 * Creates a map of routes keyed by route ID so that can easily find a 
	 * route using its ID.
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
	private static Map<String, Route> putRoutesIntoMapByRouteShortName(List<Route> routes) {
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

//		// NOTE. Thought that it might speed things up if would read in
//		// trips, trip patterns, and stopPaths all at once so that can use a single 
//		// query instead of one for each trip or trip pattern when block data is
//		// read in. But surprisingly it didn't speed up the overall queries.
//		// Yes, reading in trips and trip patterns first means that reading
//		// in blocks takes far less time. But the total time for reading
//		// everything in stays the same. The tests were done on a laptop
//		// that both contained DbConfig program plus the database. So 
//		// should conduct this test again with the database on a different
//		// server because perhaps then reading in trips and trip patterns
//		// first might make a big difference.
//		timer = new IntervalTimer();
//		List<Trip> trips = Trip.getTrips(session, configRev);
//		System.out.println("Reading trips took " + timer.elapsedMsec() + " msec");
//		
//		timer = new IntervalTimer();
//		tripPatterns = TripPattern.getTripPatterns(session, configRev);
//		System.out.println("Reading trip patterns took " + timer.elapsedMsec() + " msec");
//
//		timer = new IntervalTimer();
//		stopPaths = StopPath.getPaths(session, configRev);
//		logger.debug("Reading stopPaths took {} msec", timer.elapsedMsec());

		
		timer = new IntervalTimer();
		blocks = Block.getBlocks(globalSession, configRev);
		blocksByServiceMap = putBlocksIntoMap(blocks);
		logger.debug("Reading blocks took {} msec", timer.elapsedMsec());
		
		timer = new IntervalTimer();
		routes = Route.getRoutes(globalSession, configRev);
		routesByRouteIdMap = putRoutesIntoMapByRouteId(routes);
		routesByRouteShortNameMap = putRoutesIntoMapByRouteShortName(routes);
		logger.debug("Reading routes took {} msec", timer.elapsedMsec());

		timer = new IntervalTimer();
		List<Stop> stopsList = Stop.getStops(globalSession, configRev);
		stopsMap = putStopsIntoMap(stopsList);
		logger.debug("Reading stops took {} msec", timer.elapsedMsec());

		timer = new IntervalTimer();
		agencies = Agency.getAgencies(globalSession, configRev);
		calendars = Calendar.getCalendars(globalSession, configRev);
		calendarDates = CalendarDate.getCalendarDates(globalSession, configRev);
		fareAttributes= FareAttribute.getFareAttributes(globalSession, configRev);
		fareRules = FareRule.getFareRules(globalSession, configRev);
		frequencies = Frequency.getFrequencies(globalSession, configRev);
		transfers = Transfer.getTransfers(globalSession, configRev);
		
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
			
			List<String> currentServiceIds = 
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
	 * @return
	 */
	public Stop getStop(String stopId) {
		return stopsMap.get(stopId);
	}
	
	public List<Calendar> getCalendars() {
		return Collections.unmodifiableList(calendars);
	}
	
	public List<CalendarDate> getCalendarDates() {
		return Collections.unmodifiableList(calendarDates);
	}
		
	/**
	 * There can be multiple agencies but usually there will be just one. 
	 * For getting timezone and such want to be able to easily access
	 * the main agency, hence this method.
	 * @return
	 */
	public Agency getFirstAgency() {
		return agencies.get(0);
	}
	
	public List<Agency> getAgencies() {
		return Collections.unmodifiableList(agencies);
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
	
	
	/**
	 * For debugging.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		String projectId = "sfmta";
		
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
	}
}
