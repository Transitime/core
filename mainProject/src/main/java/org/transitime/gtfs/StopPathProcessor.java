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
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.db.structs.Location;
import org.transitime.db.structs.StopPath;
import org.transitime.db.structs.Stop;
import org.transitime.db.structs.TripPattern;
import org.transitime.db.structs.Vector;
import org.transitime.gtfs.gtfsStructs.GtfsShape;
import org.transitime.utils.Geo;
import org.transitime.utils.IntervalTimer;

/**
 * Part of GtfsData class. Processes the shapes.txt data and converts
 * into the segments for the associated stopPaths between the stops.
 * 
 * @author SkiBu Smith
 * 
 */
public class StopPathProcessor {

	// Data passed in to constructor
	private final Map<String, List<GtfsShape>> gtfsShapesMap;  // Keyed on shapeId
	private final Map<String, Stop> stopsMap;
	private final Collection<TripPattern> tripPatterns;
	private final double offsetDistance;
	private final double maxStopToPathDistance;
	private final double maxDistanceForEliminatingVertices;
	
	private static final Logger logger = 
			LoggerFactory.getLogger(StopPathProcessor.class);

	/********************** Member Functions **************************/

	/**
	 * 
	 * @param gtfsShapes
	 * @param stopsMap
	 * @param tripPatterns
	 * @param offsetDistance How much to the right the stopPaths should
	 * be offset. If negative then the stopPaths are offset to the left.
	 * If 0.0 then of course the stopPaths are not offset at all.
	 */
	public StopPathProcessor(List<GtfsShape> gtfsShapes, 
			Map<String, Stop> stopsMap, 
			Collection<TripPattern> tripPatterns,
			double offsetDistance,
			double maxStopToPathDistance,
			double maxDistanceForEliminatingVertices) {
		// Create a GtfsShapes Map where can look up
		// GtfsShapes by shapeId.
		gtfsShapesMap = new HashMap<String, List<GtfsShape>>(gtfsShapes.size());
		for (GtfsShape gtfsShape : gtfsShapes) {
			String shapeIdKey = gtfsShape.getShapeId();
			List<GtfsShape> shapesList = gtfsShapesMap.get(shapeIdKey);
			if (shapesList == null) {
				shapesList = new ArrayList<GtfsShape>();
				gtfsShapesMap.put(shapeIdKey, shapesList);
			}				
			shapesList.add(gtfsShape);
		}
		// The shapes might not be in the right order so need to sort them.
		// This way can step through the shape points in the proper order.
		for (String shapeIdKey : gtfsShapesMap.keySet()) {
			List<GtfsShape> shapesList = gtfsShapesMap.get(shapeIdKey);
			Collections.sort(shapesList);
		}
		
		this.stopsMap = stopsMap;
		this.tripPatterns = tripPatterns;
		this.offsetDistance = offsetDistance;
		this.maxStopToPathDistance = maxStopToPathDistance;
		this.maxDistanceForEliminatingVertices = maxDistanceForEliminatingVertices;
	}
	
	/**
	 * For determining the stopPaths for a trip pattern when there is no shape
	 * defined in the GTFS shapes.txt file for that trip pattern. Simply
	 * connects the stops in the trip pattern with a straight line. The first
	 * stop gets a path that has a segment of length 0.
	 * 
	 * @param tripPattern
	 */
	private void connectStopsSinceNoShapes(TripPattern tripPattern) {
		// Create a path segment to the first stop
		StopPath firstPath = tripPattern.getStopPath(0);
		String firstStopIdForTrip = tripPattern.getStopId(0);
		Stop firstStopForTrip = stopsMap.get(firstStopIdForTrip);
		ArrayList<Location> locList = new ArrayList<Location>();
		locList.add(firstStopForTrip.getLoc());
		locList.add(firstStopForTrip.getLoc());		
		firstPath.setLocations(locList);
		
		// Create stopPaths for all the other stops, one to connect each pair of stops
		for (int stopIndex=0; stopIndex<tripPattern.getStopPaths().size()-1; ++stopIndex) {
			// Determine the locations of the two stops that define the path
			String stopId0 = tripPattern.getStopId(stopIndex);
			Stop stop0 = stopsMap.get(stopId0);
			Location loc0 = stop0.getLoc();
			
			String stopId1 = tripPattern.getStopId(stopIndex+1);
			Stop stop1 = stopsMap.get(stopId1);
			Location loc1 = stop1.getLoc();

			locList = new ArrayList<Location>();
			locList.add(loc0);
			locList.add(loc1);
			
			// Filter the segments since some might be longer than desired
			StopPath path = tripPattern.getStopPath(stopIndex+1);
			filterSegments(locList, path.getId(), tripPattern.getId(), 
					tripPattern.getRouteId());
			
			path.setLocations(locList);
		}
	}
	
	/**
	 * Offsets the stopPaths to the right the distance specified. Useful for when
	 * shape data is street center line. By offsetting the data the stopPaths in the
	 * different directions won't overlap on the map when zoomed in adequately.
	 * Also, makes debugging easier since won't have confusing overlapping
	 * stopPaths.
	 * 
	 * @param gtfsShapes
	 *            List of GtfsShape objects that represent the original path for
	 *            the shape.
	 * @return
	 */
	private List<Location> getOffsetLocations(List<GtfsShape> gtfsShapes) {
		// The array where the offset path goes
		List<Location> offsetLocations = 
				new ArrayList<Location>(gtfsShapes.size());
		
		// If offsetDistance is 0.0 don't need to go through all the calculations
		if (offsetDistance == 0.0) {
			for (GtfsShape gtfsShape : gtfsShapes) {
				offsetLocations.add(gtfsShape.getLocation());
			}
			return offsetLocations;
		}
		
		// Deal with the first location specially since it isn't a vertex between
		// two vectors.
		GtfsShape gtfsShape0 = gtfsShapes.get(0);
		GtfsShape gtfsShape1 = gtfsShapes.get(1);
		Location beginningOffsetLoc = 
				Geo.rightOffsetBeginningLoc(gtfsShape0.getLocation(), 
						gtfsShape1.getLocation(), 
						offsetDistance);
		offsetLocations.add(beginningOffsetLoc);
		
		// Deal with all of the middle points of the path
		for (int i=0; i<gtfsShapes.size()-2; ++i) {
			GtfsShape g1 = gtfsShapes.get(i);
			GtfsShape g2 = gtfsShapes.get(i+1);
			GtfsShape g3 = gtfsShapes.get(i+2);
			Location offsetVertex = 
					Geo.rightOffsetVertex(g1.getLocation(),  
							g2.getLocation(),  
							g3.getLocation(), 
							offsetDistance);
			offsetLocations.add(offsetVertex);
		}
		
		// Deal with the last location specially since it isn't
		gtfsShape0 = gtfsShapes.get(gtfsShapes.size()-2);
		gtfsShape1 = gtfsShapes.get(gtfsShapes.size()-1);
		Location endOffsetLoc = 
				Geo.rightOffsetEndLoc(gtfsShape0.getLocation(), 
						gtfsShape1.getLocation(), 
						offsetDistance);
		offsetLocations.add(endOffsetLoc);	
		
		// Return the resulting offset stopPaths
		return offsetLocations;
	}
	
	/**
	 * For each trip pattern goes through the stops and matches them to 
	 * the shapes from the shapes.txt file. StopPath segments are created for
	 * each stop. 
	 * 
	 * @param shapeLocs List of Locations that represent the shapes that
	 * matching the stops to.
	 * @param stopIdsForTripPattern List of IDs of the stops that need
	 * to match to shapes.
	 * @param tripPattern so can get routeId, tripPatternId, and shapeId
	 * when creating the actual StopPath objects.
	 */
	private void determinePathSegmentsMatchingStopsToShapes(List<Location> shapeLocs,
			TripPattern tripPattern) {	
		int previousShapeIndex = 0;
		Stop previousStop = null;
		double previousShapeDistance = 0.0; // How far into the segment the previous match was
		Location previousLocation = null;
		int numberOfStopsTooFarAway = 0;
		
		// For each stop for trip pattern...
		for (int stopIndex=0; stopIndex<tripPattern.getStopPaths().size(); ++stopIndex) {
			String pathId = tripPattern.getStopPathId(stopIndex);
			String stopId = tripPattern.getStopId(stopIndex);
			Stop stop = stopsMap.get(stopId);
			
			// Determine distance between stops so can figure when have
			// looked far enough along shapes.
			double distanceBetweenStops = 
					previousStop==null? 
							Double.MAX_VALUE :
							(new Vector(previousStop.getLoc(), stop.getLoc())).length();
				
			// There are shapes defined from shapes.txt so use them.
			// For each shape see if it is the best match for the current stop
			double bestStopToShapeDistance = Double.MAX_VALUE;
			int shapeIndexOfBestMatch = -1;
			// distanceAlongShapesExamined is for determining when have looked
			// at enough shapes. Need to start at -previousShapeDistance so
			// when add in length of current vector it indeed shows how
			// far along shapes been looking to match current stop.
			double distanceAlongShapesExamined = -previousShapeDistance;
			for (int shapeIndex = previousShapeIndex; 
				 shapeIndex < shapeLocs.size()-1; 
				 ++shapeIndex) {
				// Determine Vector that connects the points for this shape 
				Location loc0 = shapeLocs.get(shapeIndex);	
				Location loc1 = shapeLocs.get(shapeIndex+1);
				Vector shapeVector = new Vector(loc0, loc1);
				
				// Determine distance of stop to the current shape
				double stopToShapeDistance = stop.getLoc().distance(shapeVector);
				
				// If this is the best fit so far, but 
				// If this is the best fit so far, remember such.
				// The 0.0001 is to make sure that don't think found 
				// a better match when actually it is the same, but
				// just looks better due to rounding error.
				if (stopToShapeDistance < bestStopToShapeDistance - 0.0001) {					
					bestStopToShapeDistance = stopToShapeDistance;
					shapeIndexOfBestMatch = shapeIndex;
				}
				
				// Keep track of how far along the shapes have examined. If
				// have looked for much further than the distance between the
				// stops then have looked far enough. Don't want to look too
				// far ahead because routes do weird things like loop back and 
				// such and we don't want to find a closer but inappropriate
				// match that is further along the route. Since sometimes stops
				// might be really close together should add in 200.0m just to 
				// be safe 
				distanceAlongShapesExamined += shapeVector.length();
				if (distanceAlongShapesExamined > 3.0 * distanceBetweenStops + 200.0)
					break;				
			} // End of for each shape (finding best match)
			
			// Now have the best match of the stop to a shape.
			// If stop really far away from path log an warning
			// but still use the match. Logging a warning instead
			// of an error because the system will still try to
			// create appropriate path by using the stop location.
			// This means that most of the time this will be adequate
			// so not a fatal error.
			if (bestStopToShapeDistance > maxStopToPathDistance) {
				++numberOfStopsTooFarAway;
				if (bestStopToShapeDistance < 250.0) {
					logger.warn("Stop {} (stop_id={}) at lat={} lon={} for " + 
							"route_id={} stop_sequence={} is located {} away " +
							"from the shapes for shape_id={}. This is further " +
							"than allowed distance of {} and means that either " +
							"the location of the stop needs to be improved, " +
							"the stop is out of sequence, the stop should not be " +
							"included for the trips in stop_times.txt or the shape " +
							"needs to be improved. {}",
							stop.getName(), 
							stop.getId(),  
							Geo.format(stop.getLoc().getLat()), 
							Geo.format(stop.getLoc().getLon()),
							tripPattern.getRouteId(),
							stopIndex+1,
							Geo.distanceFormat(bestStopToShapeDistance),
							tripPattern.getShapeId(),
							Geo.distanceFormat(maxStopToPathDistance),
							tripPattern.toStringListingTripIds() );
				} else {
					// Really far off so mark it as an error and use a stronger 
					// message
					logger.error("Stop {} (stop_id={}) at lat={} lon={} for " + 
							"route_id={} stop_sequence={} is located {} away " +
							"from the shapes for shape_id={}. This is MUCH further " +
							"than allowed distance of {} and means that either " +
							"the location of the stop needs to be improved, " +
							"the stop is out of sequence, the stop should not be " +
							"included for the trips in stop_times.txt or the shape " +
							"needs to be improved. {}",
							stop.getName(), 
							stop.getId(),  
							Geo.format(stop.getLoc().getLat()), 
							Geo.format(stop.getLoc().getLon()),
							tripPattern.getRouteId(),
							stopIndex+1,
							Geo.distanceFormat(bestStopToShapeDistance),
							tripPattern.getShapeId(),
							Geo.distanceFormat(maxStopToPathDistance),
							tripPattern.toStringListingTripIds() );
				}
			}

			// Determine how far into the best match vector the match is
			Location loc0 = shapeLocs.get(shapeIndexOfBestMatch);
			Location loc1 = shapeLocs.get(shapeIndexOfBestMatch+1);
			Vector shapeVector = new Vector(loc0, loc1);
			double shapeDistance = stop.getLoc().matchDistanceAlongVector(shapeVector);
			
			// The list of locations defining the path
			ArrayList<Location> locList = new ArrayList<Location>();

			// Start location is the end of the match for the previous stop.
			// If at the beginning of the shapes because looking the
			// first stop for the trip pattern then first set the
			// previousLocation to the first point in the shapes.
			if (previousLocation == null)
				previousLocation = shapeLocs.get(0);
			locList.add(previousLocation);

			// Now gather together the segments that go from the previous 
			// path to the current one. 
			// If on same shape...
			if (shapeIndexOfBestMatch == previousShapeIndex) {
				// If the stop was to far away from the shapes then
				// also add the stop location to the location list
				// for the path. This way will have hopefully a 
				// reasonable path that goes by the stop even if the
				// traces are not adequate.
				if (bestStopToShapeDistance > maxStopToPathDistance) {
					locList.add(stop.getLoc());
				} else {
					// Since both matches on same shape need to create
					// sub vector that goes from previous distance to
					// the new distance on the vector.
					Vector vector = shapeVector.beginning(shapeDistance);
					locList.add(vector.getL2());					
				}
				
				// Remember location for when looking at next shape
				previousLocation = locList.get(locList.size()-1);
			} else {
				// Current and previous match are on different shapes so piece
				// together the shapes into Vectors.
								
				// Add the intermediate shapes points
				for (int shapeIndex = previousShapeIndex + 1; 
						shapeIndex <= shapeIndexOfBestMatch; 
						++shapeIndex) {
					locList.add(shapeLocs.get(shapeIndex));
				}
				
				// If the stop was to far away from the shapes then
				// also add the stop location to the location list
				// for the path. This way will have hopefully a 
				// reasonable path that goes by the stop even if the
				// traces are not adequate.
				if (bestStopToShapeDistance > maxStopToPathDistance) {
					locList.add(stop.getLoc());
				} else {
					// The stop is not too far away from the shapes so
					// add the match location as the end of the locList
					// Add the end point
					Vector vector = shapeVector.beginning(shapeDistance);
					Location endPoint = vector.getL2();
					locList.add(endPoint);
				}

				// Remember location for when looking at next shape
				previousLocation = locList.get(locList.size()-1);
			}
			
			// Filter the segments and then associate them with the path 
			filterSegments(locList, pathId, tripPattern.getId(), 
					tripPattern.getRouteId());
			StopPath path = tripPattern.getStopPath(stopIndex);
			path.setLocations(locList);
			
			// Prepare for looking at next stop
			previousShapeIndex = shapeIndexOfBestMatch;
			previousStop = stop;
			previousShapeDistance = shapeDistance;
		} // End of for each stop		
		
		// If there errors with stops being too far away from the stopPaths then
		// log total number of such errors so person responsible for GTFS
		// can see how much work they have to do.
		if (numberOfStopsTooFarAway > 0) {
			logger.warn("Found {} stops that {} further than the allowable " +
					"distance of {}m from the shape for shape_id={}", 
					numberOfStopsTooFarAway, 
					numberOfStopsTooFarAway == 1 ? "is" : "are",
					maxStopToPathDistance, 
					tripPattern.getShapeId());
		}
	}
	
	/**
	 * Goes through the locations for the StopPath and filters them to make sure
	 * that they are not too short or too long. Don't want lots of tiny segments
	 * for curves cluttering up the database and mucking travel times more
	 * complicated.
	 * 
	 * Previously also made sure that the segments are not too long because the
	 * travel speeds were on a per path segment basis and wanted to account for
	 * variations in travel speeds along a long stretch of road. But now the
	 * travel speed segments are independent from the path segments so this is
	 * not necessary anymore.
	 * 
	 * @param path
	 *            the StopPath that locations are to be filtered for.
	 * @param pathId
	 *            for logging statements
	 * @param tripPatternId
	 *            for logging statements
	 * @param routeId
	 *            for logging statements
	 */
	private void filterSegments(List<Location> locations, String pathId, 
			String tripPatternId, String routeId) {	
		// Combine short stopPaths that are don't include significant distance
		// orthogonal to travel. Ideally would like to look at just two
		// line segments at a time and filter out those whose vertex is
		// not significantly out of line. But if look at just two segments
		// at a time then could shave a curve and then shave it some more
		// and some more since each two segments wouldn't make much of
		// a difference. But this could lead to some vertexes being taken
		// out even though they would be greater than the max distance
		// away. So instead of how to repeatedly go through the segments
		// to find out which ones can be taken out without any vertex 
		// ending up too far away from the resulting segments.
		for (int beginIndex = 0; beginIndex <locations.size()-2; ++beginIndex) {
			for (int endIndex = beginIndex+2; endIndex < locations.size(); ++endIndex) {
				// Determine the new possible vector that might be able to eliminate
				// some vertices without violating the max distance.
				Vector possibleNewVector = 
						new Vector(locations.get(beginIndex), locations.get(endIndex));
				
				// Check each vertex spanned by the possibleNewVector to see if any
				// would end up being to far away and violating the max distance. Also
				// check to see if checked all vertices and they are OK. Either way
				// need to erase some vertices.
				for (int vertexIndex=beginIndex+1; vertexIndex < endIndex; ++vertexIndex) {
					Location vertex = locations.get(vertexIndex);
					double distanceOfVertexToNewVector = possibleNewVector.distance(vertex);
					// If need to erase vertices...
					if (distanceOfVertexToNewVector >= maxDistanceForEliminatingVertices ||
							vertexIndex == locations.size() - 2) {
						// Things bit complicated here because can be here for two
						// different reasons: 1) because distance violated meaning
						// have to remove previous vertices; or 2) distance not 
						// violated but got to end locations so need to erase some.
						// The difference is with the last vertex that should be
						// erased.
						int indexToEraseUpTo;
						if (distanceOfVertexToNewVector >= maxDistanceForEliminatingVertices)
							// Current vector doesn't work but previous one might. Therefore
							// back up 2.
							indexToEraseUpTo = endIndex - 2;
						else
							// At end of vector so can erase to end.
							indexToEraseUpTo = locations.size() - 2;
						
						// The current possibleNewVector wouldn't work because then the
						// current vertex would be too far away. But this means that
						// can eliminate previous vertices that were within range.
						for (int eraseIndex = beginIndex + 1; eraseIndex <= indexToEraseUpTo; ++eraseIndex) {
							logger.debug("Removing vertex #{} {} from pathId={} " + 
									"tripPatternId={} routeId={}",
									eraseIndex,
									locations.get(beginIndex + 1),
									pathId,
									tripPatternId,
									routeId);

							// Erase the vertex. Since all subsequent elements in 
							// _locations array then shift over should keep removing
							// the element beginIndex + 1.
							locations.remove(beginIndex + 1);							
						}
						
						// Continue in outer loop by updating endIndex so the endIndex for loop
						// will exit and then breaking out of the inner vertexIndex for loop.
						endIndex = locations.size();
						break;
					}
				}
			}
		}
		
// NOTE: this taken out because separated out travel time segments from path segments.
//
//		// Don't want stopPaths that are too long. If they are too long then
//		// cut them down to max size.
//		for (int i=0; i<locations.size()-1; ++i) {
//			Vector v = new Vector(locations.get(i), locations.get(i+1));
//			double segmentLength = v.length();
//			if (segmentLength  > maxSegmentLength) {
//				// Segment length is too long so divide it
//				int numberSubsegments = 1 + (int) (segmentLength/maxSegmentLength);
//				double newSegmentLength = segmentLength / numberSubsegments;
//				for (int j=1; j<numberSubsegments; ++j) {
//					// Determine the new vertex to divide the segment into 
//					// acceptable lengths
//					Location intermediateLoc = v.locAlongVector(j*newSegmentLength);
//
//					logger.debug("Adding vertex #{} {} from pathId={} " + 
//							"tripPatternId={} routeId={}",
//							i + j,
//							intermediateLoc,
//							pathId,
//							tripPatternId,
//							routeId);
//					
//					// Insert the additional location into _locations
//					locations.add(i+j, intermediateLoc);
//				}				
//				// Increment i since added elements to _locations array
//				i += numberSubsegments-1;
//			}
//		}
	}

	/**
	 * Determines the path segments for each trip pattern. If shapes.txt
	 * GTFS file has a shape for the trip pattern then that data is used. 
	 * Otherwise will simply connect the stops with straight line stopPaths.
	 * 
	 * When shapes.txt data is used a lot of processing is done. The
	 * path segments are offset orthogonally by the offsetDistance which 
	 * is useful if the shapes.txt info is simply street centerline
	 * data. By offsetting the path segments for the different directions won't
	 * completely overlap on the map. Then the stops are matched to the 
	 * shapes in order to create the Paths. Each path is for a separate
	 * stop and the path ends at the stop. The path segments are also
	 * filtered so that segments that are too short or too long are 
	 * adjusted.
	 */
	public void processPathSegments() {
		// For logging how long things take
		IntervalTimer timer = new IntervalTimer();

		// Let user know what is going on
		logger.info("Processing and filtering path segment data...");
		
		// Need to process stopPaths for every trip pattern...
		for (TripPattern tripPattern : tripPatterns) {
			// Determine the GtfsShape associated with the TripPattern
			String shapeId = tripPattern.getShapeId();
			List<GtfsShape> gtfsShapesForTripPattern = gtfsShapesMap.get(shapeId);
			
			// If no shape defined then simply connect the stops
			if (gtfsShapesForTripPattern == null) {
				// Create stopPaths by connecting the stops
				connectStopsSinceNoShapes(tripPattern);
			} else {
				// Determine list of shapes associated with the trip pattern.
				// The stopPaths are offset to the right by the offsetDistance
				// if needed. This is useful if the shapes.txt data is street
				// centerline data.
				List<Location> offsetLocations = 
						getOffsetLocations(gtfsShapesMap.get(shapeId));
						
				// Create stopPaths by finding best match to shapes
				determinePathSegmentsMatchingStopsToShapes(offsetLocations, 
						tripPattern);
			}
		}
		
		// Let user know what is going on
		logger.info("Finished processing and filtering path segment data. " +
				"Took {} msec.",
				timer.elapsedMsec());		
	}
	
}
