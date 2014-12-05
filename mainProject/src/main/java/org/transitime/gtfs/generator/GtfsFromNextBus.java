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

package org.transitime.gtfs.generator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.StringConfigValue;
import org.transitime.db.structs.Location;
import org.transitime.gtfs.gtfsStructs.GtfsRoute;
import org.transitime.gtfs.gtfsStructs.GtfsShape;
import org.transitime.gtfs.gtfsStructs.GtfsStop;
import org.transitime.gtfs.gtfsStructs.GtfsStopTime;
import org.transitime.gtfs.gtfsStructs.GtfsTrip;
import org.transitime.gtfs.writers.GtfsRoutesWriter;
import org.transitime.gtfs.writers.GtfsShapesWriter;
import org.transitime.gtfs.writers.GtfsStopTimesWriter;
import org.transitime.gtfs.writers.GtfsStopsWriter;
import org.transitime.gtfs.writers.GtfsTripsWriter;

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
	
	/******************* Members ************************/
	
	// Contains all stops. Keyed on stopId
	private static Map<String, GtfsStop> gtfsStopsMap = 
			new HashMap<String, GtfsStop>();
	
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
				agencyId.getValue(), title, title, 
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
	private static class Dir {
		String tag;
		List<String> stopIds = new ArrayList<String>();
	}
	
	/**
	 * A path from the NextBus API
	 */
	private static class Path {
		String tag;
		List<String> lats = new ArrayList<String>();
		List<String> lons = new ArrayList<String>();
	}
	
	/**
	 * Gets list of directions that specify which stops are for a trip.
	 * 
	 * @param doc
	 * @return
	 */
	private static List<Dir> getDirections(Document doc) {
		List<Dir> dirList = new ArrayList<Dir>();
		
		// Get the params from the XML doc
		Element rootNode = doc.getRootElement();
		Element route = rootNode.getChild("route");
		List<Element> directions = route.getChildren("direction");
		for (Element direction : directions) {
			Dir dir = new Dir();
			dir.tag = direction.getAttributeValue("tag");
			
			// Get the stop IDs
			List<Element> stops = direction.getChildren("stop");
			for (Element stop : stops) {
				String stopTag = stop.getAttributeValue("tag");
				dir.stopIds.add(stopTag);
			}
			
			// Add the new Dir to the list to be returned
			dirList.add(dir);
		}
		
		return dirList;
	}
	
	/**
	 * Gets list of all the paths (except for the stub paths) for
	 * the route document.
	 * 
	 * @param doc
	 * @return
	 */
	private static List<Path> getPaths(Document doc) {
		List<Path> pathList = new ArrayList<Path>();
		
		// Get the paths from the XML doc
		Element rootNode = doc.getRootElement();
		Element route = rootNode.getChild("route");
		List<Element> paths = route.getChildren("path");
		for (Element path : paths) {
			Element pathTag = path.getChild("tag");		
			String pathId = pathTag.getAttributeValue("id");
			
			// Ignore stub paths since those locations are also 
			// provided by the regular paths
			if (pathId.endsWith("_d"))
				continue;
			
			// Create new Path and add to list
			Path p = new Path();
			pathList.add(p);
			String stopSpanName = pathId.substring(pathId.indexOf("_") + 1);
			p.tag = stopSpanName;

			List<Element> points = path.getChildren("point");
			for (Element point : points) {
				String lat = point.getAttributeValue("lat");
				String lon = point.getAttributeValue("lon");
			
				p.lats.add(lat);
				p.lons.add(lon);
			}			
		}
		
		return pathList;
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
	 * Returns the shapeId to use based on routeId and directionId.
	 * 
	 * @param routeId
	 * @param directionId
	 * @return
	 */
	private static String getShapeId(String routeId, String directionId) {
		return routeId + "_" + directionId;
	}
	
	/**
	 * Generates headsign using "To " + name of last stop. Requires that stops
	 * are processed first.
	 * 
	 * @param lastStopTag
	 *            Specifies the last stop of trip that is to be used as part of
	 *            the headsign.
	 * @return The headsign to use for the trip.
	 */
	private static String getTripHeadsign(String lastStopTag) {
		if (gtfsStopsMap.isEmpty()) {
			logger.error("Trying to create trip headsign before stops were processed.");
			return null;
		}
		
		return "To " + gtfsStopsMap.get(lastStopTag).getStopName();
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
		
		// Get the NextBus directions, which specify list of stops for a trip
		List<Dir> directions = getDirections(routeDoc);
		
		// Get the paths that are used for the directions
		List<Path> paths = getPaths(routeDoc);
		
		// Now go through directions and determine the shapes associated
		for (Dir dir : directions) {
			int shapePtSequence = 0;
			String shapeId = getShapeId(routeId, dir.tag);
			double shapeDistTraveled = 0.0;
			Location previousLoc = null;
			
			// For each stop for the current direction add corresponding 
			// locations
			for (String stopId : dir.stopIds) {
				// Find the corresponding path
				Path foundPath = null;
				for (Path path : paths) {
					if (path.tag.startsWith(stopId)) {
						// Found the appropriate path for this stop
						foundPath = path;
						break;
					}										
				}
				
				// If no valid path found then just continue to next one
				if (foundPath == null) 
					continue;
				
				// Write info about path
				for (int i=0; i<foundPath.lats.size(); ++i) {
					String latStr = foundPath.lats.get(i);
					double shapePtLat = Double.parseDouble(latStr);
					
					String lonStr = foundPath.lons.get(i);
					double shapePtLon = Double.parseDouble(lonStr);
					
					// Determine shape distance traveled
					Location newLoc = new Location(shapePtLat, shapePtLon);
					if (previousLoc != null) {
						shapeDistTraveled += previousLoc.distance(newLoc);
					}
					previousLoc = newLoc;
					
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
	 * Generates the trip and stop times data using XML document from NextBus
	 * API schedule command.
	 * 
	 * @param tripsWriter
	 * @param stopTimesWriter
	 * @param scheduleDoc
	 */
	private static void processTripsAndStopTimesForRoute(
			GtfsTripsWriter tripsWriter, GtfsStopTimesWriter stopTimesWriter,
			Document scheduleDoc) {
		Element rootNode = scheduleDoc.getRootElement();
		Element route = rootNode.getChild("route");
		String routeId = route.getAttributeValue("tag");
		String directionId = route.getAttributeValue("direction");
		
		List<Element> trips = route.getChildren("tr");
		for (Element trip : trips) {
			String blockId = trip.getAttributeValue("blockID");
			
			String tripId = null;
			String lastStopTag = null;
			List<Element> stops = trip.getChildren("stop");
			int stopSequence = 0;
			for (Element stop : stops) {
				String stopTag = stop.getAttributeValue("tag");
				lastStopTag = stopTag;
				String timeStr = stop.getText();
				
				// For now if stop doesn't have valid time then it is not considered
				// to be part of trip
				if (!timeStr.contains(":"))
					continue;
				
				// Use route + block + time of first stop for the trip ID
				if (tripId == null)
					tripId = getTripId(routeId, blockId, timeStr);
				
				// Create the GtfsStopTime
				Boolean timepointStop = null;
				GtfsStopTime gtfsStopTime = new GtfsStopTime(tripId, timeStr,
						timeStr, stopTag, stopSequence++,
						timepointStop);
				stopTimesWriter.write(gtfsStopTime);				
			}

			// For the trip there is no headsign info available from NextBus
			// API. Therefore just use "To " + lastStopName.
			String tripHeadsign = getTripHeadsign(lastStopTag);
			
			// Create the trip info
			String tripShortName = null;
			String shapeId = getShapeId(routeId, directionId);
			GtfsTrip gtfsTrip = new GtfsTrip(routeId, serviceId.getValue(),
					tripId, tripHeadsign, tripShortName, directionId, blockId,
					shapeId);
			tripsWriter.write(gtfsTrip);			
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
						scheduleDoc);
			} catch (IOException | JDOMException e) {
				logger.error("Problem processing data for route {}", 
						routeId, e);
			}
		}

		// Close the files
		tripsWriter.close();
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
