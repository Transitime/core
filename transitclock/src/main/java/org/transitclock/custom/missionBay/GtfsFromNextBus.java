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

package org.transitclock.custom.missionBay;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.StringConfigValue;
import org.transitclock.config.StringListConfigValue;
import org.transitclock.db.structs.Location;
import org.transitclock.gtfs.gtfsStructs.GtfsRoute;
import org.transitclock.gtfs.gtfsStructs.GtfsShape;
import org.transitclock.gtfs.gtfsStructs.GtfsStop;
import org.transitclock.gtfs.gtfsStructs.GtfsStopTime;
import org.transitclock.gtfs.gtfsStructs.GtfsTrip;
import org.transitclock.gtfs.writers.GtfsRoutesWriter;
import org.transitclock.gtfs.writers.GtfsShapesWriter;
import org.transitclock.gtfs.writers.GtfsStopTimesWriter;
import org.transitclock.gtfs.writers.GtfsStopsWriter;
import org.transitclock.gtfs.writers.GtfsTripsWriter;
import org.transitclock.utils.Time;

/**
 * For generating some of the GTFS files for an agency by reading data from the
 * NextBus API.
 *
 * @author SkiBu Smith
 *
 */
public class GtfsFromNextBus {

	/******************* Parameters *************************/
	
	private static StringConfigValue agencyId = 
			new StringConfigValue("transitime.gtfs.agencyId", 
					"missionBay",
					"Agency name used in resulting GTFS files.");

	private static StringConfigValue nextBusAgencyId = 
			new StringConfigValue("transitime.gtfs.nextBusAgencyId", 
					"sf-mission-bay",
					"Agency name that NextBus uses.");

	private static StringConfigValue nextBusFeedUrl = 
			new StringConfigValue("transitime.gtfs.nextbusFeedUrl", 
					"http://webservices.nextbus.com/service/publicXMLFeed",
					"The URL of the NextBus feed to use.");

	private static StringConfigValue gtfsDirectory = 
			new StringConfigValue("transitime.gtfs.gtfsDirectory",
					"C:/GTFS/",
					"Directory where resulting GTFS files are to be written.");
					
	private static StringConfigValue gtfsRouteType = 
			new StringConfigValue("transitime.gtfs.gtfsRouteType",
					"3",
					"GTFS definition of the route type. 1=subway 2=rail "
					+ "3=buses.");
	
	private static StringConfigValue serviceId = 
			new StringConfigValue("transitime.gtfs.serviceId",
					"wkd",
					"The service_id to use for the trips.txt file. Currently "
					+ "only can handle a single service ID.");
	
	private static StringListConfigValue validBlockIds =
			new StringListConfigValue("transitime.gtfs.validBlockIds", 
					"Only the block IDs from this list will be process from "
					+ "the schedule from the NextBus API. Useful since NextBus "
					+ "doesn't always clean out obsolete blocks");;
	
	/******************* Members ************************/
	
	// Contains all stops. Keyed on stopId
	private static Map<String, GtfsStop> gtfsStopsMap = 
			new HashMap<String, GtfsStop>();
	
	// Contains list of paths IDs for every trip pattern.
	// Keyed on shape ID.
	private static Map<String, List<String>> shapeIdsMap;
	
	/******************* Logging ************************/
	
	private static final Logger logger = LoggerFactory
			.getLogger(GtfsFromNextBus.class);

	/********************** Member Functions **************************/

	private static String getNextBusUrl(String command, String options) {
		return nextBusFeedUrl.getValue() + "?command=" + command 
				+ "&a=" + nextBusAgencyId.getValue()
				+ "&" + options;
	}
	
	/**
	 * Opens up InputStream for the routeConfig command for the NextBus API.
	 * 
	 * @param command
	 *            The NextBus API command
	 * @param options
	 *            Query string options to be appended to request to NextBus API
	 * @return XML Document to be parsed
	 * @throws IOException
	 * @throws JDOMException 
	 */
	private static Document getNextBusApiInputStream(String command,
			String options) throws IOException, JDOMException {
		String fullUrl = getNextBusUrl(command, options);
		
		// Create the connection
		URL url = new URL(fullUrl);
		URLConnection con = url.openConnection();
		
		// Request compressed data to reduce bandwidth used
		con.setRequestProperty("Accept-Encoding", "gzip,deflate");
		
		// Create appropriate input stream depending on whether content is 
		// compressed or not
		InputStream in = con.getInputStream();
		if ("gzip".equals(con.getContentEncoding())) {
		    in = new GZIPInputStream(in);
		    logger.debug("Returned XML data is compressed");
		} else {
		    logger.debug("Returned XML data is NOT compressed");			
		}

		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(in);

		// Handle any error message
		Element rootNode = doc.getRootElement();
		Element error = rootNode.getChild("Error");
		if (error != null) {
			String errorStr = error.getTextNormalize();
			logger.error("While processing data in GtfsFromNextBus: " 
					+ errorStr);
			return null;
		}

		return doc;
	}
	
	/**
	 * Processes XML data for route and extracts stop information. Adds the
	 * resulting GTFS stop to the gtfsStopsMap.
	 * 
	 * @param doc
	 *            Document for routeConfig data for a route
	 */
	static void processStopsXml(Document doc) {
		Element rootNode = doc.getRootElement();

		Element route = rootNode.getChild("route");
		List<Element> stops = route.getChildren("stop");
		for (Element stop : stops) {
			String stopId = stop.getAttributeValue("tag");
			String stopCodeStr = stop.getAttributeValue("stopId");
			Integer stopCode = stopCodeStr != null ? Integer.parseInt(stopCodeStr) : null;
			String stopName = stop.getAttributeValue("title");
			String latStr = stop.getAttributeValue("lat");
			double lat = latStr != null ? Double.parseDouble(latStr) : Double.NaN;
			String lonStr = stop.getAttributeValue("lon");
			double lon = lonStr != null ? Double.parseDouble(lonStr) : Double.NaN;
			GtfsStop gtfsStop = 
					new GtfsStop(stopId, stopCode, stopName, lat, lon);
			gtfsStopsMap.put(stopId, gtfsStop);
		}
	}
	
	/**
	 * For each route gets routeConfig input stream from NextBus API and creates
	 * and processes an XML Document object to read the data for the stops. Then
	 * writes the stops.txt GTFS file.
	 * 
	 * @param routeIds
	 */
	private static void processStops(List<String> routeIds) {
		logger.info("Processing stops...");
		
		// Add all the stops for all the routes to the gtfsStopsMap
		for (String routeId : routeIds) {
			try {
				Document doc = 
						getNextBusApiInputStream("routeConfig", "r=" + routeId);

				processStopsXml(doc);
			} catch (IOException | JDOMException e) {
				logger.error("Problem processing data for route {}", 
						routeId, e);
			}
		}
		
		// Write the stops to the GTFS stops file
		String fileName = gtfsDirectory.getValue() + "/" + agencyId + "/stops.txt";
		GtfsStopsWriter stopsWriter = new GtfsStopsWriter(fileName);
		for (GtfsStop gtfsStop : gtfsStopsMap.values()) {
			stopsWriter.write(gtfsStop);
		}
		stopsWriter.close();
	}
	
	private static GtfsRoute getGtfsRoute(Document doc) {
		// Get the params from the XML doc
		Element rootNode = doc.getRootElement();
		Element route = rootNode.getChild("route");
		String routeId = route.getAttributeValue("tag");
		String title = route.getAttributeValue("title");
		String color = route.getAttributeValue("color");
		String oppositeColor = route.getAttributeValue("oppositeColor");
		
		// Create and return the GtfsRoute
		GtfsRoute gtfsRoute = new GtfsRoute(routeId, 
				agencyId.getValue(), null, title, 
				gtfsRouteType.getValue(), color, oppositeColor);
		return gtfsRoute;
	}
	
	/**
	 * Reads from NextBus API all the routes configured and returns
	 * list of their identifiers. Also writes the routes.txt file.
	 * 
	 * @return
	 */
	private static List<String> processRoutes() {
		logger.info("Processing routes...");

		List<String> routeIds = new ArrayList<String>();
		
		// For writing the routes to the GTFS routes file
		String fileName = gtfsDirectory.getValue() + "/" + agencyId + "/routes.txt";
		GtfsRoutesWriter routesWriter = new GtfsRoutesWriter(fileName);

		try {
			Document doc = 
					getNextBusApiInputStream("routeList", null);

			Element rootNode = doc.getRootElement();
			List<Element> routes = rootNode.getChildren("route");
			for (Element route : routes) {
				// Read params from API for the route
				String routeId = route.getAttributeValue("tag");

				// Read in the route info from API and add resulting
				// GtfsRoute to routes file
				try {
					// Get the routeConfig XML document
					Document routeDoc = 
							getNextBusApiInputStream("routeConfig", "r=" + routeId);

					// Get GtfsRoute from XML document
					GtfsRoute gtfsRoute = getGtfsRoute(routeDoc);

					// Add the GTFS route to the GTFS routes file
					routesWriter.write(gtfsRoute);
				} catch (IOException | JDOMException e) {
					logger.error("Problem processing data for route {}", 
							routeId, e);
				}
				
				// Keep track of routeId so list of them can be returned
				routeIds.add(routeId);
			}
		} catch (IOException | JDOMException e) {
			logger.error("Problem determining routes", e);
		}
		
		// Wrap up
		routesWriter.close();
		
		return routeIds;
	}
	
	/**
	 * A direction from the NextBus API
	 */
	public static class Dir {
		String tag;
		String shapeId;
		String gtfsDirection; // Either "0" or "1"
		List<String> stopIds = new ArrayList<String>();
	}
	
	/**
	 * A path from the NextBus API
	 */
	private static class Path {
		List<String> lats = new ArrayList<String>();
		List<String> lons = new ArrayList<String>();
	}

	/**
	 * Gets list of all the paths for
	 * the route document.
	 * 
	 * @param doc
	 * @return Map of Path objects, keyed on the NextBus path ID
	 */
	private static Map<String, Path> getPaths(Document doc) {
		// Keyed on path ID
		Map<String, Path> pathMap = new HashMap<String, Path>();
		
		// Get the paths from the XML doc
		Element rootNode = doc.getRootElement();
		Element route = rootNode.getChild("route");
		List<Element> paths = route.getChildren("path");
		for (Element path : paths) {
			Element pathTag = path.getChild("tag");		
			String pathId = pathTag.getAttributeValue("id");
			
			// Create new Path and add to list
			Path p = new Path();
			List<Element> points = path.getChildren("point");
			for (Element point : points) {
				String lat = point.getAttributeValue("lat");
				String lon = point.getAttributeValue("lon");
			
				p.lats.add(lat);
				p.lons.add(lon);
			}			
			pathMap.put(pathId, p);
		}
		
		return pathMap;
	}

	/**
	 * Returns the trip ID to used. A concatenation of routeId, blockId, and
	 * timeStr. Could use something shorter, such as just the blockId and time,
	 * but it is nice to see the full context of the trip in the trips.txt file.
	 * 
	 * @param routeId
	 * @param blockId
	 * @param timeStr
	 * @return
	 */
	private static String getTripId(String routeId, String blockId,
			String timeStr) {
		return routeId + "_" + blockId + "_" + timeStr;
	}
	
	/**
	 * Generates headsign info. Previously used "To " + name of last stop but the
	 * last stop for trip is not available via the NextBus API.
	 *  
	 * @return The headsign to use for the trip.
	 */
	private static String getTripHeadsign() {
		return "to Mission Bay Loop";
	}
	
	/**
	 * Gets the list of path IDs associated with the route and direction ID.
	 * 
	 * @param shapeId
	 * @return
	 */
	private static List<String> getPathIds(String shapeId) {
		if (shapeIdsMap == null)
			shapeIdsMap = MissionBayConfig.getPathData();
		
		return shapeIdsMap.get(shapeId);
	}
	
	/**
	 * Processes shape data for specified route
	 * 
	 * @param shapesWriter
	 * @param routeDoc
	 */
	private static void processShapesForRoute(GtfsShapesWriter shapesWriter,
			Document routeDoc) {
		// Determine route ID so can use it to construct the shape ID
		Element rootNode = routeDoc.getRootElement();
		Element route = rootNode.getChild("route");
		String routeId = route.getAttributeValue("tag");
		
		// Get the paths that are used for the directions
		Map<String, Path> pathsForRoute = getPaths(routeDoc);
		
		// Get the NextBus directions, which specify list of stops for a trip
		List<Dir> directions = MissionBayConfig.getSpecialCaseDirections(routeId);

		// A shape can be for multiple directions but only want to process
		// a shape once.
		Set<String> shapesProcessed = new HashSet<String>();
		
		// Go through all the directions to get all the shapes
		for (Dir dir : directions) {
			double shapeDistTraveled = 0.0;
			Location previousLoc = null;
			int shapePtSequence = 0;
			String shapeId = dir.shapeId;
			
			// If already handled this shape, then continue to the next
			// direction
			if (shapesProcessed.contains(shapeId))
				continue;
			shapesProcessed.add(shapeId);
			
			List<String> nextBusPathIds = getPathIds(shapeId);
			for (String nextBusPathId : nextBusPathIds) {
				Path path = pathsForRoute.get(nextBusPathId);
				if (path == null) {
					logger.error("The nextBusPathId={} was not found for "
							+ "routeId={}. Therefore skipping this path.",	
							nextBusPathId, routeId);
					continue;
				}
				
				// For each point in the path
				for (int i=0; i<path.lats.size(); ++i) {
					String latStr = path.lats.get(i);
					double shapePtLat = Double.parseDouble(latStr);
					
					String lonStr = path.lons.get(i);
					double shapePtLon = Double.parseDouble(lonStr);
					
					// Determine shape distance traveled
					Location newLoc = new Location(shapePtLat, shapePtLon);
					if (previousLoc != null) {
						shapeDistTraveled += previousLoc.distance(newLoc);
					}
					previousLoc = newLoc;

					// For each path for the direction
					// Create and write the GTFS shape
					GtfsShape gtfsShape = new GtfsShape(shapeId,
							shapePtLat, shapePtLon, shapePtSequence++,
							shapeDistTraveled);
					shapesWriter.write(gtfsShape);			
				}
			}
		}

  }	
	
	/**
	 * Processes shape data for all routes
	 * 
	 * @param routeIds
	 */
	private static void processShapes(List<String> routeIds) {
		logger.info("Processing shapes...");

		// Create the shapes file
		String fileName = gtfsDirectory.getValue() + "/" + agencyId + "/shapes.txt";
		GtfsShapesWriter shapesWriter = new GtfsShapesWriter(fileName);

		for (String routeId : routeIds) {
			// Read in the route info from API and add resulting
			// GtfsRoute to routes file
			try {
				// Get the routeConfig XML document. Use verbose=true so
				// that get path names.
				Document routeDoc = getNextBusApiInputStream("routeConfig",
						"r=" + routeId + "&verbose=true");

				// Process shapes for route
				processShapesForRoute(shapesWriter, routeDoc);
			} catch (IOException | JDOMException e) {
				logger.error("Problem processing data for route {}", 
						routeId, e);
			}
		}

		// Done so wrap up the shapes file
		shapesWriter.close();
	}
	
	/**
	 * Determines the direction for the route that best matches the stops
	 * specified in the schedule for the trip. All stops from schedule must be
	 * listed in the direction. If there are multiple matches uses the direction
	 * with the fewest stops so that can handle case where a schedule trip could
	 * match both a direction and a direction that contains additional stops
	 * that are not in the schedule.
	 * 
	 * @param stopIdsInSchedForTrip
	 *            So can determine if trip goes to calt4th stop
	 * @param directionsForRoute
	 * @param routeId
	 * @param tripStartTimeStr
	 *            Needed so can differentiate between morning and afternoon
	 *            trips, which an be different for Mission Bay west route.
	 * @return
	 */
	private static Dir determineDirection(
			List<String> stopIdsInSchedForTrip, List<Dir> directionsForRoute,
			String routeId, String tripStartTimeStr) {
//		Dir bestDir = null;
//		
//		// This is a terrible hack to deal with the Mission Bay west route
//		// where there is a different trip pattern in the morning and the
//		// afternoon, but cannot determine the trip pattern from the stops
//		// alone. Need to look at the schedule time as well.
//		int tripStartTimeSecs = Time.parseTimeOfDay(tripStartTimeStr);
//		String directionNameComponent = null;
//		if (routeId.equals("west")) {
//			if (tripStartTimeSecs < 12 * Time.SEC_PER_HOUR)
//				directionNameComponent = "morning";
//			else
//				directionNameComponent = "afternoon";
//		}
//			
//		boolean schedContainsCalt4thStop = 
//				stopIdsInSchedForTrip.contains("calt4th");
		
		for (Dir dir : directionsForRoute) {
			// See if all stops in the schedule are in the current direction
			boolean allStopsAreInDirection = true;
			for (String stopIdFromSched : stopIdsInSchedForTrip) {
				// If stop from schedule is not in the direction
				// then skip to the next direction
				if (!dir.stopIds.contains(stopIdFromSched)) {
					allStopsAreInDirection = false;
					break;
				}
			}
			
			if (allStopsAreInDirection) {
				// Handle caltrain route specially
				if (routeId.equals("caltrain")) {
					// For caltrain route have two different directions with the
					// same stops, but with a different start of the trip. One
					// direction is for mornings and the other for afternoons.
					// for special caltrain route therefore make sure that first
					// stop of the trip matches the first stop of the direction
					// for the direction to be used. But the calttown stop is
					// an exception to the exception since a trip starts there.
					if (dir.stopIds.get(0).equals(stopIdsInSchedForTrip.get(0)) 
							|| stopIdsInSchedForTrip.get(0).equals("calttown"))
						return dir;
				} else
					return dir;
			}
			
//			// If all the stops in schedule were in the direction then remember
//			// this direction as the best if it is the direction with the
//			// fewest stops
//			if (allStopsAreInDirection) {
//				// If need to deal with special "morning"/"afternoon" name in
//				// direction, check it
//				if (directionNameComponent == null 
//						|| dir.tag.contains(directionNameComponent)) {
//					// Need to get proper direction depending on whether calt4th is
//					// a stop or not
//					if ((schedContainsCalt4thStop && dir.tag.contains("calt4th"))
//							|| (!schedContainsCalt4thStop && !dir.tag.contains("calt4th"))) {
//						// If this is best match with respect to number of stops 
//						// then remember it as such
//						if (bestDir == null || 
//							dir.stopIds.size() < bestDir.stopIds.size()) {
//							bestDir = dir;
//						}
//					}
//				}
//			}
		}
		
		// Never found matching direction
		return null;
	}
	
	/**
	 * Actually writes a trip to the GTFS trips file.
	 * 
	 * @param tripsWriter
	 * @param tripId
	 * @param routeId
	 * @param blockId
	 * @param tripStartTimeStr
	 * @param directionsForRoute
	 * @param stopIdsInSchedForTrip
	 */
	private static void writeGtfsTrip(GtfsTripsWriter tripsWriter,
			String tripId, String routeId, String blockId, 
			String tripStartTimeStr, List<Dir> directionsForRoute,
			List<String> stopIdsInSchedForTrip) {
		// Determine which direction to use that best corresponds to
		// the stops specified in the schedule.
		Dir dir = determineDirection(stopIdsInSchedForTrip,
				directionsForRoute, routeId, tripStartTimeStr);
		
		// For the trip there is no headsign info available from NextBus
		// API. Therefore just use "To " + lastStopName.
		String tripHeadsign = getTripHeadsign();
		
		// Create the trip info
		String tripShortName = null;
		GtfsTrip gtfsTrip = new GtfsTrip(routeId, serviceId.getValue(), tripId,
				tripHeadsign, tripShortName, dir.gtfsDirection, blockId,
				dir.shapeId);
		tripsWriter.write(gtfsTrip);			
	}
	
	private static class StopTime implements Comparable<StopTime> {
		String stopId;
		String stopTimeStr;
		int stopTime;
		
		private StopTime(String stopId, String stopTimeStr) {
			this.stopId = stopId; 
			this.stopTimeStr = stopTimeStr;
			this.stopTime = Time.parseTimeOfDay(stopTimeStr);
		}

		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(StopTime o) {
			return new Integer(stopTime).compareTo(o.stopTime);
		}
	}
	
	/**
	 * For the route that the scheduleDoc is for reads in all stop times and
	 * puts them into map so that can determine trips.
	 * 
	 * @param scheduleDoc
	 * @return map of stop times, keyed on block ID
	 */
	private static Map<String, List<StopTime>> getStopTimesMap(
			Document scheduleDoc) {
		Map<String, List<StopTime>> stopTimesMap = 
				new HashMap<String, List<StopTime>>();

		List<String> validBlockIdsList = validBlockIds.getValue();
		
		Element rootNode = scheduleDoc.getRootElement();
		Element route = rootNode.getChild("route");

		List<Element> scheduleRows = route.getChildren("tr");
		for (Element scheduleRow : scheduleRows) {
			String blockId = scheduleRow.getAttributeValue("blockID");
			
			// If not one of the configured block IDs then ignore. This is 
			// important because sometimes NextBus leaves old block definitions
			// in the config.
			if (!validBlockIdsList.contains(blockId))
				continue;
			
			// Get list of stops times for this block
			List<StopTime> stopTimesForBlock = stopTimesMap.get(blockId);
			if (stopTimesForBlock == null) {
				stopTimesForBlock = new ArrayList<StopTime>();
				stopTimesMap.put(blockId, stopTimesForBlock);
			}
			
			List<Element> stopsForScheduleRow = scheduleRow.getChildren("stop");
			for (Element stop : stopsForScheduleRow) {
				String stopTag = stop.getAttributeValue("tag");
				String timeStr = stop.getText();
				
				// If stop doesn't have valid time then it is not 
				// considered to be part of trip
				if (!timeStr.contains(":"))
					continue;

//				// KLUDGE! Need west route to start with 1500owen stop instead of
//				// the 1650owen one so that the stop at beginning of trip is only
//				// once in trip. This way can successfully figure out when trip ends.
//				// But the schedule API data only deals with 1650owen stop. So
//				// if encountering stop berr5th_s stop, which is after the owen
//				// stops, then use schedule time for the 1500owen stop instead
//				// of the 1650owen 1500owen one.
//				String routeId = route.getAttributeValue("tag");
//				if (routeId.equals("west")) {
//					if (stopTag.equals("berr5th_s")) {
//						StopTime previousStopTime = 
//								stopTimesForBlock.get(stopTimesForBlock.size()-1);
//						if (previousStopTime.stopId.equals("1650owen")) {
//							// Encountered schedule times for 1650owen and then
//							// berr5th_s so replace the stopId for the 1650owen
//							// schedule time with 1500owen so that it is for the
//							// stop for the beginning of the trip
//							previousStopTime.stopId = "1500owen";
//						}
//					}
//				}
				
				// Arrival stops are a nuisance. Don't really want them because
				// they are not GTFS, but if defined then still need the stop
				// to define the very end of a block. To do that need to use
				// the regular stop tag instead of the arrival one.
				if (stopTag.endsWith("_a"))
					stopTag = stopTag.substring(0, stopTag.length()-2);

				// Add this stop time to the map
				StopTime stopTime = new StopTime(stopTag, timeStr);
				stopTimesForBlock.add(stopTime);
				
				// Turn out the schedule times from the NextBus API can be out 
				// of order so sort them. Yes, it is inefficient to sort every
				// time adding a new time but easier to do it this way.	
				Collections.sort(stopTimesForBlock);
			}
		}
		
		return stopTimesMap;
	}
	
	/**
	 * Returns true if this specified stop is the first one of next trip.
	 * 
	 * @param i
	 *            Index into stopTimesForBlock
	 *            @param routeId
	 * @param beginIndex
	 *            So can determine first stop of this trip so can determine when
	 *            wrap around
	 * @param stopTimesForBlock
	 *            The stop times for the block
	 * @param dirsForRoute
	 *            All of the Dirs for the route
	 * @return True if first stop of trip
	 */
	private static boolean firstStopInNextTrip(int i, String routeId, int beginIdx,
			List<StopTime> stopTimesForBlock, List<Dir> dirsForRoute) {
		// If first stop of block then first stop in trip
		if (i == 0)
			return true;

		String currentStopId = stopTimesForBlock.get(i).stopId;

		// KLUDGE
		// For caltrans route can't determine first stop in trip effectively
		// for the morning routes. Therefore handle special case.
		// This is a terrible hack to deal with the routes
		// where there is a different trip pattern in the morning and the
		// afternoon, but cannot determine the trip pattern from the stops
		// alone. Need to look at the schedule time as well.
//		if (routeId.equals("caltrans")) {
//			String tripStartTimeStr = stopTimesForBlock.get(beginIdx).stopTimeStr;
//			int tripStartTimeSecs = Time.parseTimeOfDay(tripStartTimeStr);
//			if (tripStartTimeSecs < 12 * Time.SEC_PER_HOUR) {
//				// It is morning caltrans trip so handle specially
//				if (currentStopId.equals("calttown") || currentStopId.equals("trans390"))
//					return true;
//			}
//		}
		// KLUDGE
		// The caltrain route is a mess. Need to simply hardcode which stops
		// indicate the end of a trip.
		if (routeId.equals("caltrain")) {
			String tripStartTimeStr = stopTimesForBlock.get(beginIdx).stopTimeStr;
			int tripStartTimeSecs = Time.parseTimeOfDay(tripStartTimeStr);
			if (tripStartTimeSecs < 12 * Time.SEC_PER_HOUR) {
				// morning trip
				if (currentStopId.equals("1650owen"))
					return true;
			} else {
				// Afternoon trip
				if (currentStopId.equals("nektar"))
					return true;
			}
		}
		
		// KLUDGE
		// east loop route is also messy. Need to always end trips at powell stop at bart station
		if (routeId.equals("east")) {
			if (currentStopId.equals("powell"))
				return true;
		}

		// For simple route with only single direction look for first stop
		// of that direction
		if (dirsForRoute.size() == 1) {
			String firstStopId = dirsForRoute.get(0).stopIds.get(0);
			if (currentStopId.equals(firstStopId))
				return true;
		}
		
		// If wrapped around to first stop of trip then return true
		String firstStopId = stopTimesForBlock.get(beginIdx).stopId;
		if (currentStopId.equals(firstStopId))
			return true;
		
		// If gap between times is greater than 2 hours then true
		if (stopTimesForBlock.get(i).stopTime > 
				stopTimesForBlock.get(i-1).stopTime + 2*Time.SEC_PER_HOUR)
			return true;
		
		// Not one of the special cases so return false
		return false;
	}
	
	/**
	 * Does really complicated merger of directions and schedule times and
	 * determines both the GTFS trips and the GTFS stop times.
	 * 
	 * @param tripsWriter
	 * @param stopTimesWriter
	 * @param routeId
	 * @param scheduleDoc
	 */
	private static void processTripsAndStopTimesForRoute(
			GtfsTripsWriter tripsWriter, GtfsStopTimesWriter stopTimesWriter,
			String routeId, Document scheduleDoc) {
		List<Dir> dirsForRoute = 
				MissionBayConfig.getSpecialCaseDirections(routeId);
		
		Map<String, List<StopTime>> stopTimesByBlock = getStopTimesMap(scheduleDoc);

		// For every block in route...
		for (String blockId : stopTimesByBlock.keySet()) {
			List<StopTime> stopTimesForBlock = stopTimesByBlock.get(blockId);
			// For every trip in block...
			int beginIdx=0; // Index into stopTimesForBlock
			do {
				// Determine endIdx, the last stop of the trip. It will 
				// usually be the first stop of the next trip, but it
				// can also simply be the end of the trip if there is a
				// time gap or if the end of the block has been reached.
				int endIdx;  // Index into stopTimesForBlock
				for (endIdx = beginIdx + 1; 
						endIdx < stopTimesForBlock.size()
						&& !firstStopInNextTrip(endIdx, routeId, beginIdx,
								stopTimesForBlock, dirsForRoute); 
						++endIdx) {
				}
				
				// Handle end of block properly
				if (endIdx >= stopTimesForBlock.size())
					endIdx = stopTimesForBlock.size()-1;
				
				// Handle timegap between trips properly
				boolean deadheading = false;
				if (stopTimesForBlock.get(endIdx).stopTime > 
					stopTimesForBlock.get(endIdx-1).stopTime + 2*Time.SEC_PER_HOUR) {
					--endIdx;
					deadheading = true;
				}
				
				// Handle special case where deadheading
				if (routeId.equals("caltrain")
						&& stopTimesForBlock.get(endIdx).stopId.equals("trans390")
						&& stopTimesForBlock.get(endIdx-1).stopId.equals("1650owen")) {
					--endIdx;
					deadheading = true;					
				}

				
				// Get list of stops in schedule for the current trip.
				// Ignore arrival stops since they don't fit in with the
				// GTFS model
				List<String> scheduledStopIdsForTrip = new ArrayList<String>();
				for (int i = beginIdx; i <= endIdx; ++i) {
					// If not an arrival stop indicated by stop ID ending 
					// with "_a" then add it to list of stops
					if (!stopTimesForBlock.get(i).stopId.endsWith("_a"))
						scheduledStopIdsForTrip.add(stopTimesForBlock.get(i).stopId);
				}
				
				// Continue to process trip but only if it has more than just a single
				// stop. This is important to check because sometimes the schedule
				// will return just a single stop when it is just an arrival stop.
				boolean tripWithOnlySingleStop = scheduledStopIdsForTrip.size() <= 2
						&& scheduledStopIdsForTrip
								.get(0)
								.equals(scheduledStopIdsForTrip
										.get(scheduledStopIdsForTrip.size() - 1));
				if (!tripWithOnlySingleStop) {
					// Create the GTFS trip
					String tripStartTimeStr = 
							stopTimesForBlock.get(beginIdx).stopTimeStr;
					String tripId = getTripId(routeId, blockId, tripStartTimeStr);				
					writeGtfsTrip(tripsWriter, tripId, routeId, blockId,
							tripStartTimeStr, dirsForRoute, scheduledStopIdsForTrip);
									
					// Determine the Dir that matches to the scheduled times for 
					// the current trip in the schedule
					Dir matchingDir = determineDirection(scheduledStopIdsForTrip,
							dirsForRoute, routeId, tripStartTimeStr);
					
					// Find stop in the Dir that matches the first
					// schedule time for the trip, since might be looking at a
					// partial trip that doesn't start at beginning of the Dir.
					int dirIdx; // Index into matchingDir
					String firstScheduleStopForTrip = 
							stopTimesForBlock.get(beginIdx).stopId;
					for (dirIdx = 0; dirIdx < matchingDir.stopIds.size(); ++dirIdx) {
						if (matchingDir.stopIds.get(dirIdx).equals(firstScheduleStopForTrip)) {
							break;
						}
					}
					
					// Go through stops in the matching Dir and add all of them
					// to the trip.				
					int prevStopTimeIdx = beginIdx;
					while (dirIdx < matchingDir.stopIds.size() && prevStopTimeIdx < endIdx) {
						String stopId = matchingDir.stopIds.get(dirIdx);
						
						// Find scheduled time for the stop if there is one. If there
						// isn't one then timeStr will be null. Need to be careful here
						// because a trip will often have a stop defined twice. For 
						// example, if the trip loops around then the same stop will
						// be at beginning and end of trip. And sometimes a trip
						// covers the same stop twice due to a figure-8 configuration.
						String timeStr = null;
						for (int stopTimeIdx = prevStopTimeIdx; 
								stopTimeIdx <= endIdx; 
								++stopTimeIdx) {
							StopTime stopTime = stopTimesForBlock.get(stopTimeIdx);
							if (stopTime.stopId.equals(stopId)) {
								timeStr = stopTime.stopTimeStr;
								prevStopTimeIdx = stopTimeIdx;
								break;
							}
						}
						
						// First stop of trip is a timepoint stop, though of course
						// that is automatically done anyways by the core system.
						Boolean timepointStop = dirIdx==0;
						GtfsStopTime gtfsStopTime = new GtfsStopTime(tripId,
								timeStr, timeStr, stopId, dirIdx,
								timepointStop);
						stopTimesWriter.write(gtfsStopTime);
						
						++dirIdx;
					}
				}
				
				// Continue on to next trip. If trip ended with timegap then
				// need to go to the next stop. If reached end of block then
				// done endIdx will point to last stop for block
				beginIdx = endIdx;
				if (deadheading)
					beginIdx++;
			} while (beginIdx < stopTimesForBlock.size()-1);
		}
	}
	
	/**
	 * Uses NextBus API schedule command to determine trips and stop times
	 * and create the corresponding GTFS files.
	 * 
	 * @param routeIds
	 */
	private static void processTripsAndStopTimes(List<String> routeIds) {
		logger.info("Processing trips and stop times...");

		// Create the trips and stop_times GTFS files
		String fileName = gtfsDirectory.getValue() + "/" + agencyId + "/trips.txt";
		GtfsTripsWriter tripsWriter = new GtfsTripsWriter(fileName);
		fileName = gtfsDirectory.getValue() + "/" + agencyId + "/stop_times.txt";
		GtfsStopTimesWriter stopTimesWriter = new GtfsStopTimesWriter(fileName);
		
		for (String routeId : routeIds) {
			// Read in the route info from API and add resulting
			// GtfsRoute to routes file
			try {
				// Get the schedule XML document
				Document scheduleDoc = getNextBusApiInputStream("schedule",
						"r=" + routeId);

				// Process trips and stop_times for route
				processTripsAndStopTimesForRoute(tripsWriter, stopTimesWriter,
						routeId, scheduleDoc);
			} catch (IOException | JDOMException e) {
				logger.error("Problem processing data for route {}", 
						routeId, e);
			}
		}

		// Close the files
		tripsWriter.close();
		stopTimesWriter.close();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List<String> routeIds = processRoutes();
		processStops(routeIds);
		processShapes(routeIds);
		processTripsAndStopTimes(routeIds);
	}

}
