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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.transitime.db.structs.Location;
import org.transitime.db.structs.StopPath;
import org.transitime.db.structs.Route;
import org.transitime.db.structs.ScheduleTime;
import org.transitime.db.structs.Stop;
import org.transitime.db.structs.Transfer;
import org.transitime.db.structs.Trip;
import org.transitime.db.structs.TripPattern;
import org.transitime.db.structs.TripPatternBase;
import org.transitime.gtfs.gtfsStructs.GtfsAgency;
import org.transitime.gtfs.gtfsStructs.GtfsCalendar;
import org.transitime.gtfs.gtfsStructs.GtfsCalendarDate;
import org.transitime.gtfs.gtfsStructs.GtfsFareAttribute;
import org.transitime.gtfs.gtfsStructs.GtfsFareRule;
import org.transitime.gtfs.gtfsStructs.GtfsFrequency;
import org.transitime.gtfs.gtfsStructs.GtfsRoute;
import org.transitime.gtfs.gtfsStructs.GtfsShape;
import org.transitime.gtfs.gtfsStructs.GtfsStop;
import org.transitime.gtfs.gtfsStructs.GtfsStopTime;
import org.transitime.gtfs.gtfsStructs.GtfsTransfer;
import org.transitime.gtfs.gtfsStructs.GtfsTrip;
import org.transitime.gtfs.readers.GtfsAgencyReader;
import org.transitime.gtfs.readers.GtfsCalendarDatesReader;
import org.transitime.gtfs.readers.GtfsCalendarReader;
import org.transitime.gtfs.readers.GtfsFareAttributesReader;
import org.transitime.gtfs.readers.GtfsFareRulesReader;
import org.transitime.gtfs.readers.GtfsFrequenciesReader;
import org.transitime.gtfs.readers.GtfsRoutesReader;
import org.transitime.gtfs.readers.GtfsRoutesSupplementReader;
import org.transitime.gtfs.readers.GtfsShapesReader;
import org.transitime.gtfs.readers.GtfsStopTimesReader;
import org.transitime.gtfs.readers.GtfsStopsReader;
import org.transitime.gtfs.readers.GtfsStopsSupplementReader;
import org.transitime.gtfs.readers.GtfsTransfersReader;
import org.transitime.gtfs.readers.GtfsTripsReader;
import org.transitime.utils.Geo;
import org.transitime.utils.IntervalTimer;
import org.transitime.utils.Time;

/**
 * Contains all the GTFS data processed into Java lists and such.
 * Also combines in info from supplemental routes.txt file if there is one.
 * 
 * @author SkiBu Smith
 * 
 */
public class GtfsData {

	// Set by constructor. Specifies where to find data files
	private final String gtfsDirectoryName;
	private final String supplementDir;

	// Various params set by constructor
	private final String projectId;
	private final boolean shouldCombineShortAndLongNamesForRoutes;
	private final double pathOffsetDistance;
	private final double maxStopToPathDistance;
	private final double maxDistanceForEliminatingVertices;
	private final int defaultWaitTimeAtStopMsec;
	private final double maxTravelTimeSegmentLength;
	
	// So can make the titles more readable
	private final TitleFormatter titleFormatter;
	
	// Where the data is stored.
	// From main and supplement routes.txt files. Key is route_id.
	private Map<String, GtfsRoute> gtfsRoutesMap;
	private List<Route> routes;
	// For keeping track of which routes are just sub-routes of a parent.
	// For these the route will not be configured separately and the
	// route IDs from trips.txt and fare_rules.txt will be set to the
	// parent ID.
	private Map<String, String> properRouteIdMap = 
			new HashMap<String, String>();
	
	// From main and supplement stops.txt files. Key is stop_id.
	private Map<String, Stop> stopsMap;
	
	// From trips.txt file. Key is trip_id.
	private Map<String, GtfsTrip> gtfsTripsMap;
	
	// From stop_times.txt file
	private Map<String, List<GtfsStopTime>> gtfsStopTimesForTripMap; // Key is trip_id
	private Collection<Trip> tripsCollection;
	
	// Want to lookup trip patterns and only keep around
	// unique ones. Also, want to have each trip pattern
	// know which trips use it. This means that TripPattern 
	// needs list of GTFS trips. To do all of this need to
	// use a map. The key needs to be a TripPatternBase so can
	// make sure they are unique. But then need to also
	// contain TripPatterns as the values so that can update
	// the _trips list when another Trip is found to
	// use that TripPattern.
    private Map<TripPatternBase, TripPattern> tripPatternMap;
	
	// Also need to be able to get trip patterns associated
	// with a route so can be included in Route object.
	// Key is routeId.
	private Map<String, List<TripPattern>> tripPatternsByRouteIdMap;
	
	// So can convert from a Trip to a TripPattern. The key
	// is tripId.
	private Map<String, TripPattern> tripPatternsByTripIdMap;
	
	// So can make sure that each tripPattern gets a unique ID
	// even when really screwy things are done such as use the same
	// shapeId for different trip patterns.
	private Set<String> tripPatternIdSet;
	
	// List of all the blocks, random order
	private List<Block> blocks;

	// Keyed on tripPatternId and pathId using getPathMapKey(tripPatternId, pathId)
	private HashMap<String, StopPath> pathsMap;
	
	// Data for the other GTFS files
	// Key for frequencyMap is trip_id. Values are a List of Frequency objects
	// since each trip can be listed in frequencies.txt file multiple times in
	// order to define a different headway for different time ranges.
	private Map<String, List<Frequency>> frequencyMap;
	private List<Agency> agencies;
	private List<Calendar> calendars;
	private List<CalendarDate> calendarDates;
	private List<FareAttribute> fareAttributes;
	private List<FareRule> fareRules;
	private List<Transfer> transfers;
	
	// Logging
	public static final Logger logger = 
			LoggerFactory.getLogger(GtfsData.class);

	/********************** Member Functions **************************/

	public GtfsData(String projectId,
			String gtfsDirectoryName, 
			String supplementDir, 
			boolean shouldCombineShortAndLongNamesForRoutes,
			double pathOffsetDistance,
			double maxStopToPathDistance,
			double maxDistanceForEliminatingVertices,
			int defaultWaitTimeAtStopMsec,
			double maxTravelTimeSegmentLength,
			TitleFormatter titleFormatter) {
		this.projectId = projectId;
		this.gtfsDirectoryName = gtfsDirectoryName;
		this.supplementDir = supplementDir;
		this.shouldCombineShortAndLongNamesForRoutes = shouldCombineShortAndLongNamesForRoutes;
		this.pathOffsetDistance = pathOffsetDistance;
		this.maxStopToPathDistance = maxStopToPathDistance;
		this.maxDistanceForEliminatingVertices = maxDistanceForEliminatingVertices;
		this.defaultWaitTimeAtStopMsec = defaultWaitTimeAtStopMsec;
		this.maxTravelTimeSegmentLength = maxTravelTimeSegmentLength;
		this.titleFormatter = titleFormatter;
	}
	
	/**
	 * Reads routes.txt files from both gtfsDirectoryName and supplementDir
	 * and combines them together. End result is that gtfsRoutesMap is created
	 * and filled in.
	 */
	private void processGtfsRouteData() {
		// For logging how long things take
		IntervalTimer timer = new IntervalTimer();

		// Let user know what is going on
		logger.info("Processing routes.txt data...");
		
		// Read in standard route data
		GtfsRoutesReader routesReader = new GtfsRoutesReader(gtfsDirectoryName);
		List<GtfsRoute> gtfsRoutes = routesReader.get();		

		// Put GtfsRoute objects in Map so easy to find the right ones.
		// HashMap is keyed on the route_id.
		gtfsRoutesMap = new HashMap<String, GtfsRoute>(gtfsRoutes.size());
		for (GtfsRoute r : gtfsRoutes) {
			// If this route is just a subset of another route
			// then can ignore it. But put in the map so that
			// can adjust trips and fare_rules to use the proper
			// parent route ID.
			if (r.getParentRouteId() != null) {
				properRouteIdMap.put(r.getRouteId(), r.getParentRouteId());
			} else {
				// Use normal route ID
				properRouteIdMap.put(r.getRouteId(), r.getRouteId());
			}
			
			// Add the gtfs route to the map now that we can get the proper
			// route ID.
			gtfsRoutesMap.put(getProperIdOfRoute(r.getRouteId()), r);			
		}
		
		// Read in supplemental route data
		if (supplementDir != null) {
			// Read in the supplemental route data
			GtfsRoutesSupplementReader routesSupplementReader = 
					new GtfsRoutesSupplementReader(supplementDir);
			List<GtfsRoute> gtfsRoutesSupplement = routesSupplementReader.get();
			
			// Modify the main GtfsRoute objects using the supplemental data
			for (GtfsRoute supplementRoute : gtfsRoutesSupplement) {
				// Determine the original GtfsRoute that the supplemental
				// data corresponds to
				GtfsRoute originalGtfsRoute = null;
				if (supplementRoute.getRouteId() != null) {
					// Using regular route_id in supplemental routes.txt file.
					// Look up the GTFS route by the proper route ID.
					String routeMapKey = 
							getProperIdOfRoute(supplementRoute.getRouteId());
					originalGtfsRoute = gtfsRoutesMap.get(routeMapKey);
				} else {
					// Must be using route short name as ID. Therefore
					// cannot use the routes hashmap directly.
					String routeShortName = supplementRoute.getRouteShortName();
					for (GtfsRoute gtfsRoute : gtfsRoutes) {
						if (gtfsRoute.getRouteShortName().equals(routeShortName)) {
							originalGtfsRoute = gtfsRoute;
							break;
						}
					}
				}
				
				
				if (originalGtfsRoute != null) {
					// Found the original GTFS route that the supplemental data 
					// is associated with so create a new GTFSRoute object that 
					// combines the original data with the supplemental data.
					GtfsRoute combinedRoute = 
							new GtfsRoute(originalGtfsRoute, supplementRoute);
					
					// Store that combined data route in the map 
					gtfsRoutesMap.put(combinedRoute.getRouteId(), combinedRoute);
				} else {
					// Didn't find the original route with the same ID as the
					// supplemental route so log warning that supplemental file
					// is not correct.
					logger.warn("Found route data in supplemental file but " +
							"there is no such route with the ID in the " +
							"main routes.txt file. Therefore could not use " +
							"the supplemental data for this route. " +
							"supplementRoute={}", supplementRoute);
				}
			}
		}
		
		// Let user know what is going on
		logger.info("processGtfsRouteData() finished processing routes.txt " +
				"data. Took {} msec.", 
				timer.elapsedMsec());
	}
	
	/**
	 * Takes data from gtfsRoutesMap and creates corresponding Route map.
	 * Processes all of the titles to make them more readable. Creates 
	 * corresponding Route objects and stores them into the database.
	 * This method is separated out from processGtfsRouteData() since reading
	 * trips needs the gtfs route info but reading Routes requires trips.
	 */
	private void processRouteData() {
		// For logging how long things take
		IntervalTimer timer = new IntervalTimer();
		
		// Let user know what is going on
		logger.info("Processing Routes objects data in processRouteData()...");
		
		// Make sure needed data is already read in. This method uses
		// trips and trip patterns from the stop_time.txt file. This objects
		// need to know lat & lon so can figure out bounding box. Therefore
		// stops.txt file must be read in first. 
		if (gtfsTripsMap == null || gtfsTripsMap.isEmpty()) {
			logger.error("processTripsData() must be called before " + 
					"GtfsData.processRouteData() is. Exiting.");
			System.exit(-1);
		}
		if (gtfsRoutesMap == null || gtfsRoutesMap.isEmpty()) {
			logger.error("processGtfsRouteData() must be called before " + 
					"GtfsData.processRouteData() is. Exiting.");
			System.exit(-1);
		}

		// Now that the GtfsRoute objects have been created, create 
		// the corresponding map of Route objects
		routes = new ArrayList<Route>();
		Set<String> routeIds = gtfsRoutesMap.keySet();
		int numberOfRoutesWithoutTrips = 0;
		for (String routeId : routeIds) {
			GtfsRoute gtfsRoute = gtfsRoutesMap.get(routeId);
			
			// If route is to be ignored then continue
			if (gtfsRoute.shouldRemove())
				continue;
			
			// If this route is just a subset of another route
			// then can ignore it. 
			if (gtfsRoute.getParentRouteId() != null) {
				continue;
			}
			
			// Determine the trip patterns for the route so that they
			// can be included when constructing the route object.
			// If there aren't any then can't 
			List<TripPattern> tripPatternsForRoute = getTripPatterns(routeId);
			if (tripPatternsForRoute == null || tripPatternsForRoute.isEmpty()) {
				logger.warn("Route \"{}\" route_id={} was defined on line #{} in " + 
						"routes.txt file but does not have any associated trips " + 
						"defined in the stop_times.txt file. Therefore that route " +
						"has been removed from the configuration.",
						gtfsRoute.getRouteLongName()!=null ?
								gtfsRoute.getRouteLongName() : gtfsRoute.getRouteShortName() , 
						routeId,
						gtfsRoute.getLineNumber());
				++numberOfRoutesWithoutTrips;
				continue;
			}
			
			// Create the route object and add it to the container
			Route route = new Route(gtfsRoute,
									tripPatternsForRoute,
									titleFormatter, 
									shouldCombineShortAndLongNamesForRoutes);
			routes.add(route);
		}
		
		// Summarize how many problem routes there are that don't have trips
		if (numberOfRoutesWithoutTrips > 0) {
			logger.warn("Found {} routes without trips in stop_times.txt " +
					"out of a total of {} routes defined in the routes.txt file.", 
					numberOfRoutesWithoutTrips, routeIds.size());
		}
		
		// Let user know what is going on
		logger.info("Finished processing Routes objects data in processRouteData(). Took {} msec.", 
				timer.elapsedMsec());
	}
	
	/**
	 * Reads routes.txt files from both gtfsDirectoryName and supplementDir
	 * and combines them together. Processes all of the titles to make them
	 * more readable. Creates corresponding Route objects and stores them
	 * into the database.
	 */
	private void processStopData() {
		// For logging how long things take
		IntervalTimer timer = new IntervalTimer();

		// Let user know what is going on
		logger.info("Processing stops.txt data...");
		
		// Read in standard route data
		GtfsStopsReader stopsReader = new GtfsStopsReader(gtfsDirectoryName);
		List<GtfsStop> gtfsStops = stopsReader.get();		

		// Put GtfsStop objects in Map so easy to find the right ones
		Map<String, GtfsStop> gtfsStopsMap = new HashMap<String, GtfsStop>(gtfsStops.size());
		for (GtfsStop gtfsStop : gtfsStops)
			gtfsStopsMap.put(gtfsStop.getStopId(), gtfsStop);
		
		// Read in supplemental stop data
		if (supplementDir != null) {
			// Read in the supplemental stop data
			GtfsStopsSupplementReader stopsSupplementReader = 
					new GtfsStopsSupplementReader(supplementDir);
			List<GtfsStop> gtfsStopsSupplement = stopsSupplementReader.get();
			
			// Modify the main GtfsStop objects using the supplemental data
			for (GtfsStop supplementStop : gtfsStopsSupplement) {
				GtfsStop gtfsStop = gtfsStopsMap.get(supplementStop.getStopId());
				
				// Create a new GTFSStop object that combines the original
				// data with the supplemental data
				GtfsStop combinedStop = new GtfsStop(gtfsStop, supplementStop);
				
				// Store that combined data stop in the map 
				gtfsStopsMap.put(combinedStop.getStopId(), combinedStop);
			}
		}
		
		// Create the map of the Stop objects
		stopsMap = new HashMap<String, Stop>(gtfsStops.size());
		for (GtfsStop gtfsStop : gtfsStopsMap.values()) {
			Stop stop = new Stop(gtfsStop, titleFormatter);
			stopsMap.put(stop.getId(), stop);
		}
		
		// Let user know what is going on
		logger.info("Finished processing stops.txt data. Took {} msec.", 
				timer.elapsedMsec());
	}
				
	/**
	 * Reads data from trips.txt and puts it into gtfsTripsMap.
	 */
	private void processTripsData() {
		// For logging how long things take
		IntervalTimer timer = new IntervalTimer();

		// Make sure needed data is already read in. This method uses
		// GtfsRoutes info to make sure all trips reference a route.
		if (gtfsRoutesMap == null || gtfsRoutesMap.isEmpty()) {
			logger.error("processGtfsRouteData() must be called before " + 
					"GtfsData.processTripsData() is. Exiting.");
			System.exit(-1);
		}

		// Let user know what is going on
		logger.info("Processing trips.txt data...");
		
		// Create the map where the data is going to go
		gtfsTripsMap = new HashMap<String, GtfsTrip>();
		
		// Read in the trips.txt GTFS data from file
		GtfsTripsReader tripsReader = new GtfsTripsReader(gtfsDirectoryName);
		List<GtfsTrip> gtfsTrips = tripsReader.get();
		
		for (GtfsTrip gtfsTrip : gtfsTrips) {	
			// Make sure that each trip references a valid route. If it does
			// not then something is fishy with the data so output a warning.
			if (getGtfsRoute(gtfsTrip.getRouteId()) != null) {
				// Refers to a valid route so we should process this trip
				gtfsTripsMap.put(gtfsTrip.getTripId(), gtfsTrip);
			} else {
				logger.warn("The trip id={} in the trips.txt file at " + 
						"line # {} refers to route id={} but that route is " +
						"not in the routes.txt file. Therefore " +
						"that trip is being discarded.", 
						gtfsTrip.getTripId(),
						gtfsTrip.getLineNumber(),
						gtfsTrip.getRouteId());
			}
		}		
					
		// Let user know what is going on
		logger.info("Finished processing trips.txt data. Took {} msec.", 
				timer.elapsedMsec());
	}
	
	/**
	 * Sorts gtfsStopTimesForTrip and then goes through the data to make sure
	 * it is OK. If data is a real problem, like a duplicate stop, it is 
	 * removed. Any problems found are logged.
	 * 
	 * @param gtfsStopTimesForTrip
	 */
	private void processStopTimesForTrip(List<GtfsStopTime> gtfsStopTimesForTrip) {
		// Sort the list so that the stop times are in sequence order.
		// This way can treat first and last stop times for a trip
		// specially. Plus want them in order to determine trip patterns
		// and such.
		Collections.sort(gtfsStopTimesForTrip);

		// Iterate over stop times for trip and remove inappropriate ones.
		// Also, log any warning or error messages.
		String previousStopId = null;
		boolean firstStopInTrip = true;
		int previousTimeForTrip = 0;			
		Iterator<GtfsStopTime> iterator = gtfsStopTimesForTrip.iterator();
		while (iterator.hasNext()) {
			// Get the GtfsStopTime to examine
			GtfsStopTime gtfsStopTime = iterator.next();
			
			// Convenience variable
			String tripId = gtfsStopTime.getTripId();
			
			// If the GtfsStopTime refers to a non-existent stop than log an error
			if (getStop(gtfsStopTime.getStopId()) == null) {
				logger.error("In stop_times.txt line {} refers to stop_id {} but " + 
						"it is not defined in the stops.txt file. Therefore this " + 
						"stop will be ignored for trip {}.", 
						gtfsStopTime.getLineNumber(),
						gtfsStopTime.getStopId(),
						gtfsStopTime.getTripId());
				iterator.remove();
				continue;
			}
			
			// Make sure that not listing the same stop twice in a row.
			// Yes, SFMTA actually has done this!
			if (gtfsStopTime.getStopId().equals(previousStopId)) {
				logger.warn("Encountered stopId={} twice in a row for tripId={} " + 
						"in stop_times.txt at line {}. The second stop will not " + 
						"be included.",
						gtfsStopTime.getStopId(),
						gtfsStopTime.getTripId(),
						gtfsStopTime.getLineNumber());
				iterator.remove();
				continue;
			}
			
			// Make sure arrival/departure times OK.
			Integer arr = gtfsStopTime.getArrivalTimeSecs();
			Integer dep = gtfsStopTime.getDepartureTimeSecs();
			// Make sure that first stop has a departure time and the
			// last one has an arrival time.
			if (firstStopInTrip && dep == null) {
				logger.error("First stop in trip {} does not have a departure " 
						+ "time. The problem is in the stop_times.txt file at line {}.",
						tripId, getGtfsTrip(tripId).getLineNumber());
			}
			boolean lastStopInTrip = !iterator.hasNext();
			if (lastStopInTrip && arr == null) {
				logger.error("Last stop in trip {} does not have an arrival " 
						+ "time. The problem is in the stop_times.txt file at line {}.",
						tripId, getGtfsTrip(tripId).getLineNumber());				
			}
			
			// Make sure departure time >= arrival time.
			// Of course either one can be null so bit more complicated.
			if (arr != null && dep != null && dep < arr) {
				logger.error("The departure time {} is before the arrival time {} " + 
						"in the stop_times.txt file at line {}", 
						Time.timeOfDayStr(dep), Time.timeOfDayStr(arr), gtfsStopTime.getLineNumber());				
			}
			
			// Now make sure that arrival/departures times never go backwards in time
			if (arr != null && arr < previousTimeForTrip) {
				logger.error("The arrival time {} is before the time {} for " + 
						"a previous stop for the trip. " + 
						"See stop_times.txt file line {}", 
						Time.timeOfDayStr(arr), 
						Time.timeOfDayStr(previousTimeForTrip), 
						gtfsStopTime.getLineNumber());				
			}
			if (dep != null && dep < previousTimeForTrip) {
				logger.error("The departure time {} is before the time {} for " + 
						"a previous stop for the trip. " + 
						"See stop_times.txt file line {}", 
						Time.timeOfDayStr(dep), 
						Time.timeOfDayStr(previousTimeForTrip), 
						gtfsStopTime.getLineNumber());				
			}
			// Update previous time so can check the next stop for the trip
			if (arr != null)
				previousTimeForTrip = arr;
			if (dep != null)
				previousTimeForTrip = dep;

			// For next time through loop
			previousStopId = gtfsStopTime.getStopId();
			firstStopInTrip = false;
		}

	}
	
	/**
	 * Reads the data from stop_times.txt and puts it into
	 * gtfsStopTimesForTripMap map. Also processes the data to determine Trips
	 * (_tripMap) and TripPatterns (_tripPatterns). When processing Trips uses
	 * frequency.txt data to determine if each trip ID is actually for multiple
	 * trips with unique start times defined by the headway.
	 */
	private void processStopTimesData() {
		// Make sure needed data is already read in. This method determines
		// trips and trip patterns from the stop_time.txt file. This objects
		// need to know lat & lon so can figure out bounding box. Therefore
		// stops.txt file must be read in first. Also, need to know which route
		// is associated with a trip determined in stop_time.txt file. This
		// info is in trips.txt so it needs to be processed first.
		if (stopsMap == null || stopsMap.isEmpty()) {
			logger.error("processStopData() must be called before " + 
					"GtfsData.processStopTimesData() is. Exiting.");
			System.exit(-1);
		}
		if (gtfsTripsMap == null || gtfsTripsMap.isEmpty()) {
			logger.error("processTripsData() must be called before " + 
					"GtfsData.processStopTimesData() is. Exiting.");
			System.exit(-1);
		}

		// For logging how long things take
		IntervalTimer timer = new IntervalTimer();
		
		// Let user know what is going on
		logger.info("Processing stop_times.txt data...");
		
		// Read in the stop_times.txt GTFS data from file. Use a large initial
		// array size so when reading in data won't have to constantly increase
		// array size and do array copying. SFMTA for example has 1,100,000
		// stop times so starting with a value of 500,000 certainly should be reasonable.
		GtfsStopTimesReader stopTimesReader = new GtfsStopTimesReader(gtfsDirectoryName);
		List<GtfsStopTime> gtfsStopTimes = stopTimesReader.get(500000);

		// The GtfsStopTimes are put into this map and then can create Trips
		// and TripPatterns. Keyed by tripId
		gtfsStopTimesForTripMap = new HashMap<String, List<GtfsStopTime>>();

		// Put the GtfsStopTimes into the map
		for (GtfsStopTime gtfsStopTime : gtfsStopTimes) {
			String tripId = gtfsStopTime.getTripId();
			
			// Add the GtfsStopTime to the map so later can create Trips and TripPatterns
			List<GtfsStopTime> gtfsStopTimesForTrip = gtfsStopTimesForTripMap.get(tripId);
			if (gtfsStopTimesForTrip == null) {
				gtfsStopTimesForTrip = new ArrayList<GtfsStopTime>();
				gtfsStopTimesForTripMap.put(tripId, gtfsStopTimesForTrip);
			}
			gtfsStopTimesForTrip.add(gtfsStopTime);
		}
		
		// Go through the stop times for each tripId. Sort them and look for
		// any problems with the data.
		Set<String> tripIds = gtfsStopTimesForTripMap.keySet();
		for (String tripId : tripIds) {
			List<GtfsStopTime> gtfsStopTimesForTrip =
					gtfsStopTimesForTripMap.get(tripId);			
			processStopTimesForTrip(gtfsStopTimesForTrip);
		}
		
		// Log if a trip is defined in the trips.txt file but not in 
		// stop_times.txt
		int numberOfProblemTrips = 0;
		for (String tripIdFromTripsFile : gtfsTripsMap.keySet()) {
			if (gtfsStopTimesForTripMap.get(tripIdFromTripsFile) == null) {
				++numberOfProblemTrips;
				logger.warn("trip_id={} was defined on line #{} in trips.txt " +
						"but there was no such trip defined in the " +
						"stop_times.txt file",
						tripIdFromTripsFile, 
						gtfsTripsMap.get(tripIdFromTripsFile).getLineNumber());
			}
		}
		if (numberOfProblemTrips > 0)
			logger.warn("Found {} trips were defined in trips.txt but not in " +
					"stop_times.txt out of a total of {} trips in trips.txt",
					numberOfProblemTrips, gtfsTripsMap.size());
		
		// Now that have all the stop times gtfs data create the trips
		// and the trip patterns.
		createTripsAndTripPatterns(gtfsStopTimesForTripMap);
				
		// Let user know what is going on
		logger.info("Finished processing stop_times.txt data. Took {} msec.", 
				timer.elapsedMsec());
	}
	
	/**
	 * 
	 * @param gtfsStopTimesForTripMap Keyed by tripId. List of GtfsStopTimes for the tripId.
	 */
	private void createTripsAndTripPatterns(Map<String, List<GtfsStopTime>> gtfsStopTimesForTripMap) {
		if (frequencyMap == null) {
			logger.error("processFrequencies() must be called before " + 
					"GtfsData.createTripsAndTripPatterns() is. Exiting.");
			System.exit(-1);
		}

		// Create the necessary collections for trips
		tripPatternMap = new HashMap<TripPatternBase, TripPattern>();
		tripPatternsByTripIdMap = new HashMap<String, TripPattern>();
		tripPatternsByRouteIdMap = new HashMap<String, List<TripPattern>>();
		tripPatternIdSet = new HashSet<String>();
		
		// Create the Paths lookup table
		pathsMap = new HashMap<String, StopPath>();
		
		// Create the map where the trip info is going to be stored
		Map<String, Trip> tripsInStopTimesFileMap = new HashMap<String, Trip>();
		
		// For each trip in the stop_times.txt file ...
		for (String tripId : gtfsStopTimesForTripMap.keySet()) {
			// Determine the gtfs stop times for this trip
			List<GtfsStopTime> gtfsStopTimesForTrip = gtfsStopTimesForTripMap.get(tripId);

			// Create list of Paths for creating trip pattern
			List<StopPath> paths = new ArrayList<StopPath>();
			
			// Create set of path IDs for this trip so can tell if looping back on path
			// such that need to create a unique path ID
			Set<String> pathIdsForTrip = new HashSet<String>();
			
			// Determine the Trip element for the trip ID. Create new one if need to.
			Trip trip = tripsInStopTimesFileMap.get(tripId);
			if (trip == null) {
				// Determine the GtfsTrip for the id so can be used
				// to construct the Trip object.
				GtfsTrip gtfsTrip = getGtfsTrip(tripId);
				
				// If resulting gtfsTrip is null because it wasn't defined in trips.txt
				// then need to log this problem (and log this only once) and continue
				if (gtfsTrip == null) {
					logger.warn("Encountered trip_id={} in the stop_times.txt " +
							"file but that trip_id is not in the trips.txt file. " + 
							"Therefore this trip cannot be configured and has been discarded.",
							tripId);
					
					// Can't deal with this trip Id so skip to next trip
					continue;
				}
				
				// If this route is actually a sub-route of a parent then use the
				// parent ID.
				String properRouteId = getProperIdOfRoute(gtfsTrip.getRouteId());
				String routeShortName = 
						gtfsRoutesMap.get(gtfsTrip.getRouteId()).getRouteShortName();
				trip = new Trip(gtfsTrip, properRouteId, routeShortName, titleFormatter);
				tripsInStopTimesFileMap.put(tripId, trip);
			}
			
			// For each stop time for the trip...
			String previousStopId = null;			
			for (int i=0; i<gtfsStopTimesForTrip.size(); ++i) {
				// The current gtfsStopTime
				GtfsStopTime gtfsStopTime = gtfsStopTimesForTrip.get(i);
				
				// Convenience variables
				Integer arrTime = gtfsStopTime.getArrivalTimeSecs();
				Integer depTime = gtfsStopTime.getDepartureTimeSecs();
				boolean firstStopInTrip = i==0;
				boolean lastStopInTrip =  i==gtfsStopTimesForTrip.size()-1;
				String stopId = gtfsStopTime.getStopId();
				
				// Add the schedule time to the Trip object. Some agencies configure the
				// same arrival and departure time for every stop. That is just silly
				// and overly verbose. If the times are the same should just use
				// departure time, except for last stop for trip where should use 
				// arrival time.
				Integer filteredArr = arrTime;
				Integer filteredDep = depTime;
				if (arrTime != null && depTime != null && arrTime.equals(depTime)) {
					if (lastStopInTrip)
						filteredDep = null;
					else
						filteredArr = null;
				}
				ScheduleTime scheduleTime = new ScheduleTime(filteredArr, filteredDep);
				trip.add(stopId, scheduleTime);
				
				// Create StopPath so it can be used to create TripPattern. 
				// First determine attributes layoverStop, 
				// waitStop, and scheduleAdherenceStop. They are true 
				// if there is a departure time and they are configured or 
				// are first stop in trip.
				Stop stop = getStop(stopId);

				// Determine if layover stop
				boolean layoverStop = false;
				if (depTime != null) {
					if (stop.isLayoverStop() == null) {
						layoverStop = firstStopInTrip;
					} else {
						layoverStop = stop.isLayoverStop();
					}
				}
					
				// Determine if it is a waitStop
				boolean waitStop = false;
				if (depTime != null) {
					if (stop.isWaitStop() == null) {
						waitStop = firstStopInTrip;
					} else {
						waitStop = stop.isWaitStop();
					}
				}
				
				// This one is a bit complicated. Should be a scheduleAdherenceStop
				// if there is an associated time and it is configured to be such.
				// But should also be true if there is associated time and it is
				// first or last stop of the trip.
				boolean scheduleAdherenceStop = 
						(depTime != null && 
							(firstStopInTrip || stop.isScheduleAdherenceStop())) ||
						(arrTime != null && lastStopInTrip);
				
				// Determine the pathId. Make sure that use a unique path ID by
				// appending "_loop" if looping over the same stops
				String pathId = StopPath.determinePathId(previousStopId, stopId);
				while (pathIdsForTrip.contains(pathId))
					pathId += "_loop";
				pathIdsForTrip.add(pathId);
				
				// Determine the GtfsRoute so that can get break time
				GtfsRoute gtfsRoute = gtfsRoutesMap.get(trip.getRouteId());
				
				// Create the new StopPath and add it to the list
				// for this trip.
				StopPath path = new StopPath(pathId, stopId,
						gtfsStopTime.getStopSequence(), lastStopInTrip,
						trip.getRouteId(), layoverStop, waitStop,
						scheduleAdherenceStop, gtfsRoute.getBreakTime());
				paths.add(path);
				
				previousStopId = stopId;
			} // End of for each stop_time for trip
			
			// Now that have Paths defined for the trip, if need to, 
			// also create new trip pattern
			updateTripPatterns(trip, paths);			
		}  // End of for each trip ID
		
		// Now that have the tripsInStopTimesFileMap need to create
		// collection of all trips if headways are defined in the
		// frequencies.txt file.
		createTripsCollection(tripsInStopTimesFileMap);
	}
	
	/**
	 * Now that have the tripsInStopTimesFileMap need to create collection of
	 * all trips if headways are defined in frequencyMap from the
	 * frequencies.txt file. Creates and fills in tripsCollection.
	 * 
	 * @param tripsInStopTimesFileMap
	 */
	private void createTripsCollection(Map<String, Trip> tripsInStopTimesFileMap) {
		tripsCollection = new ArrayList<Trip>();
		for (Trip tripFromStopTimes : tripsInStopTimesFileMap.values()) {
			// Handle depending on whether a regular Trip or one defined
			// in the frequencies.txt file.
			String tripId = tripFromStopTimes.getId();
			List<Frequency> frequencyListForTripId = frequencyMap.get(tripId);
			if (frequencyListForTripId == null || 
					!frequencyListForTripId.get(0).getExactTimes()) {
				// This is an actual Trip that is not affected by the
				// frequencies.txt data. Therefore simply add it to
				// the collection.
				tripsCollection.add(tripFromStopTimes);
			} else {
				// For this trip ID there is an entry in the frequencies.txt
				// file with exact_times set indicating that need to create
				// a separate Trip for each actual trip.  
				for (Frequency frequency : frequencyListForTripId) {
					for (int tripStartTime = frequency.getStartTime(); 
							tripStartTime < frequency.getEndTime();
							tripStartTime += frequency.getHeadwaySecs()) {
						Trip frequencyBasedTrip = 
								new Trip(tripFromStopTimes,	tripStartTime);
						tripsCollection.add(frequencyBasedTrip);
					}
				}
			}
		}
	}
	
	/**
	 * This method is called for each trip. Determines if
	 * the corresponding trip pattern is already in 
	 * tripPatternMap. If it is then it updates the trip
	 * pattern to include this trip as a member. If this
	 * trip pattern not already encountered then it
	 * adds it to the tripPatternMap.
	 * 
	 * @param trip 
	 * @param stopPaths List of StopPath objects that define the trip pattern
	 */
	private void updateTripPatterns(Trip trip, List<StopPath> paths) {
		// Create a TripPatternBase from the Trip object
		TripPatternBase tripPatternBase = new TripPatternBase(trip.getShapeId(), paths);
		
		// Determine if the TripPattern is already stored. 
		TripPattern tripPatternFromMap = tripPatternMap.get(tripPatternBase);
		// If not already stored then create and store the trip pattern.
		if (tripPatternFromMap == null) {
			// Create the trip pattern
			TripPattern tripPattern = new TripPattern(tripPatternBase, trip, this);
			
			// Add the new trip pattern to the maps
			tripPatternMap.put(tripPatternBase, tripPattern);
			tripPatternsByTripIdMap.put(trip.getId(), tripPattern);
			tripPatternIdSet.add(tripPattern.getId());

			// Also add the new TripPattern to tripPatternsByRouteIdMap
			List<TripPattern> tripPatternsForRoute = 
					tripPatternsByRouteIdMap.get(tripPattern.getRouteId());
			// If haven't dealt with this route, create the List now
			if (tripPatternsForRoute == null) {
				tripPatternsForRoute = new ArrayList<TripPattern>();
				tripPatternsByRouteIdMap.put(tripPattern.getRouteId(), tripPatternsForRoute);
			}
			tripPatternsForRoute.add(tripPattern);

			// Update the Trip to indicate which TripPattern it is for
			trip.setTripPattern(tripPattern);
			
			// Now that we have the trip pattern ID update the map of Paths
			for (StopPath path : tripPattern.getStopPaths())
				putPath(tripPattern.getId(), path.getId(), path);
		} else {
			// This trip pattern already in map so just add the Trip
			// to the list of trips that refer to it.
			tripPatternFromMap.addTrip(trip);
			
			// Add it to tripPatternsByTripIdMap as well
			tripPatternsByTripIdMap.put(trip.getId(), tripPatternFromMap);
			
			// Update the Trip to indicate which TripPattern it is for
			trip.setTripPattern(tripPatternFromMap);
		}

	}
	

	/**
	 * Goes through all the trips and constructs block assignments from them.
	 * Also, goes through the frequencies and created unscheduled blocks
	 * for them.
	 */
	private void processBlocks() {
		// For logging how long things take
		IntervalTimer timer = new IntervalTimer();

		// Let user know what is going on
		logger.info("Processing blocks...");

		// Actually process the block info and get back list of blocks
		blocks = new BlocksProcessor(this).process();
		
		// Let user know what is going on
		logger.info("Finished processing blocks. Took {} msec.", 
				timer.elapsedMsec());
	}
	
	/**
	 * Reads frequencies.txt file and puts data into _frequencies list.
	 */
	private void processFrequencies() {
		// Make sure needed data is already read in. 
		if (gtfsTripsMap == null || gtfsTripsMap.isEmpty()) {
			logger.error("processTripsData() must be called before " + 
					"GtfsData.processFrequencies() is. Exiting.");
			System.exit(-1);
		}

		// For logging how long things take
		IntervalTimer timer = new IntervalTimer();

		// Let user know what is going on
		logger.info("Processing frequencies.txt data...");
		
		// Create the map where the data is going to go
		frequencyMap = new HashMap<String, List<Frequency>>();

		// Read in the frequencies.txt GTFS data from file
		GtfsFrequenciesReader frequenciesReader = new GtfsFrequenciesReader(gtfsDirectoryName);
		List<GtfsFrequency> gtfsFrequencies = frequenciesReader.get();
		
		for (GtfsFrequency gtfsFrequency : gtfsFrequencies) {
			// Make sure this Frequency is in trips.txt
			GtfsTrip gtfsTrip = gtfsTripsMap.get(gtfsFrequency.getTripId());
			if (gtfsTrip == null) {
				logger.error("The frequency from line # {} of frequencies.txt" + 
						"refers to trip_id={} but that trip is not in the trips.txt " + 
						"file. Therefore this frequency will be ignored.",
						gtfsFrequency.getLineNumber(),
						gtfsFrequency.getTripId());
				continue;
			}
			
			// Create the Frequency object and put it into the frequenctMap
			Frequency frequency = new Frequency(gtfsFrequency);
			String key = frequency.getTripId();
			List<Frequency> frequenciesForTripId = frequencyMap.get(key);
			if (frequenciesForTripId == null) {
				frequenciesForTripId = new ArrayList<Frequency>();
				frequencyMap.put(key, frequenciesForTripId);
			}
			frequenciesForTripId.add(frequency);
		}		
					
		// Let user know what is going on
		logger.info("Finished processing frequencies.txt data. Took {} msec.", 
				timer.elapsedMsec());
	}
	
	/**
	 * Reads in shapes.txt file and processes the information into 
	 * StopPath objects. Using the term "StopPath" instead of "Shape" to
	 * be more descriptive of what the data is really for. 
	 */
	private void processPaths() {
		// Make sure needed data is already read in. This method 
		// converts the shapes into Paths such that each path ends
		// at a stop. Therefore need to have read in stop info first.
		if (stopsMap == null || stopsMap.isEmpty()) {
			logger.error("processStopData() must be called before " + 
					"GtfsData.processPaths() is. Exiting.");
			System.exit(-1);
		}

		// For logging how long things take
		IntervalTimer timer = new IntervalTimer();

		// Let user know what is going on
		logger.info("Processing shapes.txt data...");
		
		// Read in the shapes.txt GTFS data from file
		GtfsShapesReader shapesReader = new GtfsShapesReader(gtfsDirectoryName);
		List<GtfsShape> gtfsShapes = shapesReader.get();
		
		// Process all the shapes into stopPaths
		StopPathProcessor pathProcessor = 
				new StopPathProcessor(
						Collections.unmodifiableList(gtfsShapes), 
						Collections.unmodifiableMap(stopsMap), 
						Collections.unmodifiableCollection(tripPatternMap.values()),
						pathOffsetDistance,
						maxStopToPathDistance, 
						maxDistanceForEliminatingVertices);
		pathProcessor.processPathSegments();
						
		// Let user know what is going on
		logger.info("Finished processing shapes.txt data. Took {} msec.",
				timer.elapsedMsec());		
	}
	
	/**
	 * Reads agency.txt file and puts data into agencies list.
	 */
	private void processAgencies() {
		// Let user know what is going on
		logger.info("Processing agency.txt data...");
		
		// Create the map where the data is going to go
		agencies = new ArrayList<Agency>();

		// Read in the agency.txt GTFS data from file
		GtfsAgencyReader agencyReader = new GtfsAgencyReader(gtfsDirectoryName);
		List<GtfsAgency> gtfsAgencies = agencyReader.get();
		
		for (GtfsAgency gtfsAgency : gtfsAgencies) {
			// Create the Agency object and put it into the array
			Agency agency = new Agency(gtfsAgency, getRoutes());
			agencies.add(agency);
		}		
					
		// Let user know what is going on
		logger.info("Finished processing agencies.txt data. ");
	}

	/**
	 * Reads calendar.txt file and puts data into calendars list.
	 */
	private void processCalendars() {
		// Let user know what is going on
		logger.info("Processing calendar.txt data...");
		
		// Create the map where the data is going to go
		calendars = new ArrayList<Calendar>();

		// Read in the calendar.txt GTFS data from file
		GtfsCalendarReader calendarReader = new GtfsCalendarReader(gtfsDirectoryName);
		List<GtfsCalendar> gtfsCalendars = calendarReader.get();
		
		for (GtfsCalendar gtfsCalendar : gtfsCalendars) {
			// Create the Calendar object and put it into the array
			Calendar calendar = new Calendar(gtfsCalendar);
			calendars.add(calendar);
		}		
					
		// Let user know what is going on
		logger.info("Finished processing calendar.txt data. ");
	}

	/**
	 * Reads calendar_dates.txt file and puts data into calendarDates list.
	 */
	private void processCalendarDates() {
		// Let user know what is going on
		logger.info("Processing calendar_dates.txt data...");
		
		// Create the map where the data is going to go
		calendarDates = new ArrayList<CalendarDate>();

		// Read in the calendar_dates.txt GTFS data from file
		GtfsCalendarDatesReader calendarDatesReader = new GtfsCalendarDatesReader(gtfsDirectoryName);
		List<GtfsCalendarDate> gtfsCalendarDates = calendarDatesReader.get();
		
		for (GtfsCalendarDate gtfsCalendarDate : gtfsCalendarDates) {
			// Create the CalendarDate object and put it into the array
			CalendarDate calendarDate = new CalendarDate(gtfsCalendarDate);
			calendarDates.add(calendarDate);
		}		
					
		// Let user know what is going on
		logger.info("Finished processing calendar_dates.txt data. ");
	}

	/**
	 * Reads fare_attributes.txt file and puts data into fareAttributes list.
	 */
	private void processFareAttributes() {
		// Let user know what is going on
		logger.info("Processing fare_attributes.txt data...");
		
		// Create the map where the data is going to go
		fareAttributes = new ArrayList<FareAttribute>();

		// Read in the fare_attributes.txt GTFS data from file
		GtfsFareAttributesReader fareAttributesReader = new GtfsFareAttributesReader(gtfsDirectoryName);
		List<GtfsFareAttribute> gtfsFareAttributes = fareAttributesReader.get();
		
		for (GtfsFareAttribute gtfsFareAttribute : gtfsFareAttributes) {
			// Create the FareAttribute object and put it into the array
			FareAttribute FareAttribute = new FareAttribute(gtfsFareAttribute);
			fareAttributes.add(FareAttribute);
		}		
					
		// Let user know what is going on
		logger.info("Finished processing fare_attributes.txt data. ");
	}

	/**
	 * Reads fare_rules.txt file and puts data into fareRules list.
	 */
	private void processFareRules() {
		// Let user know what is going on
		logger.info("Processing fare_rules.txt data...");
		
		// Create the map where the data is going to go
		fareRules = new ArrayList<FareRule>();

		// Read in the fare_rules.txt GTFS data from file
		GtfsFareRulesReader fareRulesReader = new GtfsFareRulesReader(gtfsDirectoryName);
		List<GtfsFareRule> gtfsFareRules = fareRulesReader.get();
		
		for (GtfsFareRule gtfsFareRule : gtfsFareRules) {
			// If this route is actually a sub-route of a parent then use the
			// parent ID.
			String parentRouteId = getProperIdOfRoute(gtfsFareRule.getRouteId());
			
			// Create the CalendarDate object and put it into the array
			FareRule fareRule = new FareRule(gtfsFareRule, parentRouteId);
			fareRules.add(fareRule);
		}		
					
		// Let user know what is going on
		logger.info("Finished processing fare_rules.txt data. ");
	}

	/**
	 * Reads transfers.txt file and puts data into transfers list.
	 */
	private void processTransfers() {
		// Let user know what is going on
		logger.info("Processing transfers.txt data...");
		
		// Create the map where the data is going to go
		transfers = new ArrayList<Transfer>();

		// Read in the transfers.txt GTFS data from file
		GtfsTransfersReader transfersReader = new GtfsTransfersReader(gtfsDirectoryName);
		List<GtfsTransfer> gtfsTransfers = transfersReader.get();
		
		for (GtfsTransfer gtfsTransfer : gtfsTransfers) {
			// Create the CalendarDate object and put it into the array
			Transfer transfer = new Transfer(gtfsTransfer);
			transfers.add(transfer);
		}		
					
		// Let user know what is going on
		logger.info("Finished processing transfers.txt data. ");
	}

	/******************** Getter Methods ****************************/

	/**
	 * @return projectId
	 */
	public String getProjectId() {
		return projectId;		
	}
	
	public Map<String, GtfsRoute> getGtfsRoutesMap() {
		return gtfsRoutesMap;
	}
	
	/**
	 * @param routeId
	 * @return the GtfsRoute from the trips.txt file for the specified routeId, 
	 * null if that route Id not defined in the file. 
	 */
	public GtfsRoute getGtfsRoute(String routeId) {
		GtfsRoute gtfsRoute = gtfsRoutesMap.get(routeId);
		return gtfsRoute;
	}
	

	/**
	 * @param tripId
	 * @return the GtfsTrip from the trips.txt file for the specified tripId, 
	 * null if that trip Id not defined in the file. 
	 */
	public GtfsTrip getGtfsTrip(String tripId) {
		GtfsTrip gtfsTrip = gtfsTripsMap.get(tripId);
		return gtfsTrip;
	}
	
	/**
	 * Returns true if GTFS stop times read in and are available
	 * @return
	 */
	public boolean isStopTimesReadIn() {
		return gtfsStopTimesForTripMap != null && !gtfsStopTimesForTripMap.isEmpty();
	}
	
	/**
	 * Returns list of GtfsStopTimes for the trip specified
	 * @param tripId
	 * @return
	 */
	public List<GtfsStopTime> getGtfsStopTimesForTrip(String tripId) {
		return gtfsStopTimesForTripMap.get(tripId);
	}
		
	/**
	 * @return Collection of all the Trip objects
	 */
	public Collection<Trip> getTrips() {
		return tripsCollection;
	}
	
	/**
	 * @return True if tripsMap read in and is usable
	 */
	public boolean isTripsReadIn() {
		return tripsCollection != null && !tripsCollection.isEmpty();
	}
	
	public Collection<Stop> getStops() {
		return stopsMap.values();
	}
	/**
	 * @param stopId
	 * @return The Stop for the specified stopId
	 */
	public Stop getStop(String stopId) {
		return stopsMap.get(stopId);
	}
	
	/**
	 * @return Collection of all TripPatterns
	 */
	public Collection<TripPattern> getTripPatterns() {
		return tripPatternsByTripIdMap.values();
	}
	
	/**
	 * @param routeId
	 * @return List of TripPatterns for the routeId
	 */
	public List<TripPattern> getTripPatterns(String routeId) {
		return tripPatternsByRouteIdMap.get(routeId);
	}
	
	/**
	 * @param tripId The trip ID to return the TripPattern for
	 * @return The TripPattern for the specified trip ID
	 */
	public TripPattern getTripPatternByTripId(String tripId) {
		return tripPatternsByTripIdMap.get(tripId);
	}
	
	public boolean isTripPatternIdAlreadyUsed(String tripPatternId) {
		return tripPatternIdSet.contains(tripPatternId);
	}
	
	public Map<TripPatternBase, TripPattern> getTripPatternMap() {
		return tripPatternMap;
	}
	
	/**
	 * Returns the specified trip pattern. Not very efficient
	 * because does a linear search through the set of trip
	 * patterns but works well enough for debugging.
	 * 
	 * @param tripPatternId
	 * @return
	 */
	public TripPattern getTripPattern(String tripPatternId) {
		for (TripPattern tp : tripPatternMap.values()) {
			if (tp.getId().equals(tripPatternId))
				return tp;
		}
		
		// Couldn't find the specified trip pattern so return null;
		return null;
	}
	
	/**
	 * For use with pathsMap member.
	 * 
	 * @param tripPatternId
	 * @param pathId
	 * @return
	 */
	public static String getPathMapKey(String tripPatternId, String pathId) {
		return tripPatternId + "|" + pathId;
	}
	
	/**
	 * Returns the StopPath for the specified tripPatternId and pathId.
	 * Can't just use pathId since lots of trip patterns will traverse
	 * the same stops, resulting in identical pathIds. And don't want
	 * to make the pathIds themselves unique because then wouldn't be
	 * able to reuse travel time data as much.
	 * 
	 * @param tripPatternId
	 * @param pathId
	 * @return
	 */
	public StopPath getPath(String tripPatternId, String pathId) {
		String key = getPathMapKey(tripPatternId, pathId);
		return pathsMap.get(key);
	}
	
	/**
	 * @return Collection of all the Paths
	 */
	public Collection<StopPath> getPaths() {
		return pathsMap.values();
	}
	
	/**
	 * Adds the StopPath object to the pathMap.
	 * 
	 * @param tripPatternId
	 * @param pathId
	 * @param path
	 */
	public void putPath(String tripPatternId, String pathId, StopPath path) {
		String key = getPathMapKey(tripPatternId, pathId);
		pathsMap.put(key, path);
	}
	
	/**
	 * @return List of routes that can be stored in db. The result
	 * is not ordered by route_order since that isn't needed as part
	 * of processing GTFS data.
	 */
	public List<Route> getRoutes() {
		return routes;
	}
	
	/**
	 * If a route is configured to be a sub-route of a parent then this
	 * method will return the route ID of the parent route. Otherwise
	 * returns null.
	 * 
	 * @param routeId
	 * @return route ID of parent route if there is one. Otherwise, null.
	 */
	public String getProperIdOfRoute(String routeId) {
		if (routeId == null)
			return null;
		return properRouteIdMap.get(routeId);
	}
	
	public List<Block> getBlocks() {
		return blocks;
	}
	
	public List<Agency> getAgencies() {
		return agencies;
	}
	
	/**
	 * @param tripId
	 * @return The Frequency list specified by tripId param
	 */
	public List<Frequency> getFrequencyList(String tripId) {
		return frequencyMap.get(tripId);
	}

	/**
	 * Returns collection of all the Frequency objects. This method goes
	 * through the internal frequencyMap and compiles the collection each
	 * time this member is called.
	 * @return
	 */
	public Collection<Frequency> getFrequencies() {
		Collection<Frequency> collection = new ArrayList<Frequency>();
		for (List<Frequency> frequencyListForTripId : frequencyMap.values()) {
			for (Frequency frequency : frequencyListForTripId) {
				collection.add(frequency);
			}
		}
		return collection;
	}
	
	public List<Calendar> getCalendars() {
		return calendars;
	}
	
	public List<CalendarDate> getCalendarDates() {
		return calendarDates;
	}
	
	public List<FareAttribute> getFareAttributes() {
		return fareAttributes;
	}
	
	public List<FareRule> getFareRules() {
		return fareRules;
	}
	
	public List<Transfer> getTransfers() {
		return transfers;
	}
	
	/*************************** Main Public Methods **********************/
	
	/**
	 * Outputs data for specified route grouped by trip pattern.
	 * The resulting data can be visualized on a map by cutting
	 * and pasting it in to http://www.gpsvisualizer.com/map_input .
	 * @param routeId
	 */
	public void outputPathsAndStopsForGraphing(String routeId) {
		System.err.println("\nPaths for routeId=" + routeId);
		
		// Also need to be able to get trip patterns associated
		// with a route so can be included in Route object.
		// Key is routeId.
		List<TripPattern> tripPatterns = tripPatternsByRouteIdMap.get(routeId);
		for (TripPattern tripPattern : tripPatterns) {
			System.err.println("\n\n================= TripPatternId=" + tripPattern.getId() +
					" shapeId=" + tripPattern.getShapeId() +
					"=======================\n");

			// Output the header info
			System.err.println("name,symbol,color,label,latitude,longitude");
			
			// Output the stop locations so can see where they are relative to path
			for (StopPath path : tripPattern.getStopPaths()) {
				String stopId = path.getStopId();
				Stop stop = getStop(stopId);
				System.err.println(", pin, red, stop " + 
						stopId + ", " + 
						Geo.format(stop.getLoc().getLat()) + ", " 
						+ Geo.format(stop.getLoc().getLon()));
			}
			
			int pathCnt = 0;
			for (StopPath path : tripPattern.getStopPaths()) {
				// Use different colors and symbols so can tell how things are progressing
				++pathCnt;
				String symbolAndColor;
				switch (pathCnt%13) {
					case 0: symbolAndColor = "star, blue"; break;
					case 1: symbolAndColor = "googlemini, green"; break;
					case 2: symbolAndColor = "diamond, blue"; break;
					case 3: symbolAndColor = "square, green"; break;
					case 4: symbolAndColor = "triangle, blue"; break;
					case 5: symbolAndColor = "cross, green"; break;
					case 6: symbolAndColor = "circle, blue"; break;
					
					case 7: symbolAndColor = "star, red"; break;
					case 8: symbolAndColor = "googlemini, yellow"; break;
					case 9: symbolAndColor = "diamond, red"; break;
					case 10: symbolAndColor = "square, yellow"; break;
					case 11: symbolAndColor = "triangle, red"; break;
					case 12: symbolAndColor = "cross, yellow"; break;
					default: symbolAndColor = "circle, red"; break;
				}
				
				// Output the path info for this trip pattern
				int i=0;
				for (Location loc : path.getLocations()) {
					String popupName = "" + i + " lat=" + Geo.format(loc.getLat()) + 
							" lon=" + Geo.format(loc.getLon());
					String label = "" + i;
					System.err.println(popupName + ", " + symbolAndColor + ", " + 
							label + ", " + 
							Geo.format(loc.getLat()) + ", " 
							+ Geo.format(loc.getLon()));
					++i;
				}							
			}
		}
	}
	
	/**
	 * Does all the work. Processes the data and store it in internal structures
	 */
	public void processData() {
		// For logging how long things take
		IntervalTimer timer = new IntervalTimer();

		// Let user know what is going on
		logger.info("Processing GTFS data from {} ...",
				gtfsDirectoryName);

		// Note. The order of how these are processed in important because
		// some datasets rely on others in order to be fully processed.
		// If the order is wrong then the methods below will log an error and
		// exit.
		processGtfsRouteData();
		processStopData();		
		processTripsData();	
		processFrequencies();
		processStopTimesData();		
		processRouteData(); 
		processBlocks();
		processPaths();
		
		// Following are simple objects that don't require combining tables
		processAgencies();
		processCalendars();
		processCalendarDates();
		processFareAttributes();
		processFareRules();
		processTransfers();
		
		// FIXME debugging
		//outputPathsAndStopsForGraphing("8699");
		
		SessionFactory sessionFactory = 
				HibernateUtils.getSessionFactory(getProjectId());

		// Now process travel times and update the Trip objects. 
		TravelTimesProcessor travelTimesProcesssor = 
				new TravelTimesProcessor(getProjectId(),  
						maxTravelTimeSegmentLength,	defaultWaitTimeAtStopMsec);
		travelTimesProcesssor.process(this);
		
		// Try allowing garbage collector to free up some memory since
		// don't need the GTFS structures anymore.
		gtfsRoutesMap = null;
		gtfsTripsMap = null;
		gtfsStopTimesForTripMap = null;
		
		// Now that have read in all the data into collections can output it
		// to database.
		DbWriter dbWriter = new DbWriter(this);
		dbWriter.write(sessionFactory);		
		
		// Let user know what is going on
		logger.info("Finished processing GTFS data from {} . Took {} msec.",
				gtfsDirectoryName, timer.elapsedMsec());		

		// FIXME just for debugging
//		GtfsLoggingAppender.outputMessagesToSysErr();
//		
//		//outputShapesForGraphing("102589" /*"102829"*/);
//		
//		outputPathsAndStopsForGraphing("8701");
//		
//		System.err.println("\nPaths:");
//		for (StopPath p : getPathsMap().values()) {
//			TripPattern tp = getTripPattern(p.getTripPatternId());
//			System.err.println("\npathId=" + p.getPathId() + 
//					" routeId=" + p.getRouteId() +
//					" tripPattern=" + tp.toStringListingTripIds());
//			for (Location l : p.getLocations()) {
//				System.err.println("" + Geo.format(l.getLat()) + ", " + Geo.format(l.getLon()));
//			}
//		}
//		
//		System.err.println("\nPaths:");
//		for (StopPath p : getPathsMap().values()) {
//			System.err.println("  " + p);
//		}
//
//		System.err.println("\nBlocks:");
//		for (Block b : getBlocks()) {
//			System.err.println("  " + b.toShortString());
//		}
//		
//		System.err.println("\nRoutes:");
//		for (Route r : getRoutes()) {
//			System.err.println("  " + r);
//		}
	}
}
