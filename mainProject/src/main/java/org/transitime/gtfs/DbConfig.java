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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.db.hibernate.HibernateUtils;
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

/**
 * Reads all the configuration data from the database. The data is based on GTFS
 * but is heavily processed into things like TripPatterns and better Paths to 
 * make the data far easier to use.
 *  
 * @author SkiBu Smith
 *
 */
public class DbConfig {

	private final String projectId;

	// Keeps track of which revision of config data was read in
	private int configRev;
	
	// Following is for all the data read from the database
	private List<Block> blocks;
	// So can access blocks by session ID and block ID easily
	Map<String, Map<String, Block>> blocksByServiceMap = null;
	
	private List<Route> routes;
	// Keyed on routeId
	private Map<String, Route> routesMap;
	
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

	// Database revision is the configuration being modified. Once finished then
	// it will be copied to a working revision.
	public static final int SANDBOX_REV = 0;
	
	private static final Logger logger = 
			LoggerFactory.getLogger(DbConfig.class);

	/********************** Member Functions **************************/

	/**
	 * Constructor
	 * 
	 * @param projectId
	 */
	public DbConfig(String projectId) {
		this.projectId = projectId;
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
		logger.info("Reading configuration database for configRev={}...", configRev);

		// Remember which revision of data is being used
		this.configRev = configRev;
		
		// Open up Hibernate session so can read in data
		SessionFactory sessionFactory = HibernateUtils.getSessionFactory(projectId);
		Session session = sessionFactory.openSession();		
		
		// Do the low-level processing
		try {
			actuallyReadData(session, configRev);
		} catch (HibernateException e) {
			logger.error("Error reading configuration data from db for configRev={}.", 
					configRev);
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
	
	/*
	 * Creates a map of a map so that blocks can be looked up easily by service and 
	 * block IDs.
	 * 
	 * @param blocks List of blocks to be put into map
	 * @return Map keyed on service ID of map keyed on block ID of blocks
	 */
	private static Map<String, Map<String, Block>> putBlocksIntoMap(List<Block> blocks) {
		Map<String, Map<String, Block>> blocksByServiceMap = 
				new HashMap<String, Map<String, Block>>();
		
		for (Block block : blocks) {
			Map<String, Block> blocksByBlockIdMap = blocksByServiceMap.get(block.getServiceId());
			if (blocksByBlockIdMap == null) {
				blocksByBlockIdMap = new HashMap<String, Block>();
				blocksByServiceMap.put(block.getServiceId(), blocksByBlockIdMap);
			}
			
			blocksByBlockIdMap.put(block.getId(), block);
		}
		
		return blocksByServiceMap;
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
	
	/*
	 * Creates a map of routes keyed by route ID so that can easily find a 
	 * route using its ID.
	 *  
	 * @param routes
	 * @return
	 */
	private static Map<String, Route> putRoutesIntoMap(List<Route> routes) {
		// Convert list of routes to a map keyed on routeId
		Map<String, Route> routesMap = new HashMap<String, Route>();
		for (Route route : routes) {
			routesMap.put(route.getId(), route);
		}
		return routesMap;
	}
	
	/**
	 * Reads the individual data structures from the database.
	 * 
	 * @param session
	 * @param configRev
	 */
	private void actuallyReadData(Session session, int configRev) {
		IntervalTimer timer;

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
		blocks = Block.getBlocks(session, configRev);
		blocksByServiceMap = putBlocksIntoMap(blocks);
		logger.debug("Reading blocks took {} msec", timer.elapsedMsec());
		
		timer = new IntervalTimer();
		routes = Route.getRoutes(session, configRev);
		routesMap = putRoutesIntoMap(routes);
		logger.debug("Reading routes took {} msec", timer.elapsedMsec());

		timer = new IntervalTimer();
		List<Stop> stopsList = Stop.getStops(session, configRev);
		stopsMap = putStopsIntoMap(stopsList);
		logger.debug("Reading stops took {} msec", timer.elapsedMsec());

		timer = new IntervalTimer();
		agencies = Agency.getAgencies(session, configRev);
		calendars = Calendar.getCalendars(session, configRev);
		calendarDates = CalendarDate.getCalendarDates(session, configRev);
		fareAttributes= FareAttribute.getFareAttributes(session, configRev);
		fareRules = FareRule.getFareRules(session, configRev);
		frequencies = Frequency.getFrequencies(session, configRev);
		transfers = Transfer.getTransfers(session, configRev);
		
		logger.debug("Reading everything else took {} msec", 
				timer.elapsedMsec());
	}
	
	/************************** Getter Methods ***************************/
	/**
	 * Returns the block specified by the service and block ID parameters.
	 * 
	 * @param serviceId
	 * @param blockId
	 * @return
	 */
	public Block getBlock(String serviceId, String blockId) {
		Map<String, Block> blocksMap = blocksByServiceMap.get(serviceId);
		return blocksMap.get(blockId);
	}
	
	public List<Block> getBlocks() {
		return Collections.unmodifiableList(blocks);
	}
	
	/**
	 * Returns Map of routesMap keyed on the routeId.
	 * 
	 * @return
	 */
	public Map<String, Route> getRoutesMap() {
		return Collections.unmodifiableMap(routesMap);
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
	 * @return
	 */
	public Route getRoute(String routeId) {
		return routesMap.get(routeId);
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
		String projectId = "sf-muni";
		
		// FIXME Use the sandbox config rev for now but this should be configurable!
		int configRev = DbConfig.SANDBOX_REV;
		
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
		outputCollection("Routes", dbConfig.routesMap.values());
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
