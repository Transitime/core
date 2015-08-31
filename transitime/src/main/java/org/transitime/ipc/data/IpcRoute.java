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

package org.transitime.ipc.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.transitime.applications.Core;
import org.transitime.core.dataCache.PredictionDataCache;
import org.transitime.core.dataCache.VehicleDataCache;
import org.transitime.db.structs.Location;
import org.transitime.db.structs.Route;
import org.transitime.db.structs.Stop;
import org.transitime.db.structs.StopPath;
import org.transitime.db.structs.TripPattern;

/**
 * Describes a route such that it can e displayed in a UI. Consists of all the
 * info in a IpcRouteSummary it adds in stops and shapes information.
 *
 * @author SkiBu Smith
 *
 */
public class IpcRoute extends IpcRouteSummary {

	// Note that there are additional members in IpcRouteSummary superclass.
	private IpcDirectionsForRoute stops;
	private Collection<IpcShape> shapes;
	private Location locationOfNextPredictedVehicle;
	
	private static final long serialVersionUID = -227901807027962547L;

	/********************** Member Functions **************************/

	/**
	 * Create an IpcRoute that contains all stops and paths but separates out
	 * information for the remaining part of the trip specified by stopId and
	 * tripPatternId.
	 * 
	 * @param dbRoute
	 *            The route object as read from the db
	 * @param directionId
	 *            Set if want to know which part of route is major and which is
	 *            minor. Otherwise set to null.
	 * @param stopId
	 *            Set if want to know which part of route is major and which is
	 *            minor. Otherwise set to null.
	 * @param tripPatternId
	 *            Set if want to know which part of route is major and which is
	 *            minor. Otherwise set to null.
	 */
	public IpcRoute(Route dbRoute, String directionId, String stopId,
			String tripPatternId) {
		// Construct the core part of the route, everything but
		// the stops and paths.
		super(dbRoute);
		
		// Create Collections of stops and paths
		stops = createStops(dbRoute, directionId, stopId, tripPatternId);
		shapes = createShapes(dbRoute, directionId, stopId, tripPatternId);
		locationOfNextPredictedVehicle = 
				getLocationOfNextPredictedVehicle(dbRoute, directionId, stopId);
	}

	/**
	 * If stop specified then returns the location of the next predicted vehicle
	 * for that stop. Returns null if stop not specified or no predictions for
	 * stop.
	 * 
	 * @param dbRoute
	 * @param directionId
	 *            Set if want to know which part of route is major and which is
	 *            minor. Otherwise set to null.
	 * @param stopId
	 * @return
	 */
	private static Location getLocationOfNextPredictedVehicle(Route dbRoute,
			String directionId, String stopId) {
		// If no stop specified then can't determine next predicted vehicle
		if (stopId == null)
			return null;
		
		// Determine the first IpcPrediction for the stop
		List<IpcPredictionsForRouteStopDest> predsList =
				PredictionDataCache.getInstance().getPredictions(
						dbRoute.getShortName(), directionId, stopId);
		if (predsList.isEmpty())
			return null;
		
		List<IpcPrediction> ipcPreds = predsList.get(0).getPredictionsForRouteStop();
		if (ipcPreds.isEmpty())
			return null;
		
		// Based on the first prediction determine the current IpcVehicle info
		String vehicleId = ipcPreds.get(0).getVehicleId();
		
		IpcVehicleComplete vehicle = VehicleDataCache.getInstance().getVehicle(vehicleId);
		
		return new Location(vehicle.getLatitude(), vehicle.getLongitude());
	}
	
	/**
	 * Returns trip pattern specified by tripPatternId or stopId. If
	 * tripPatternId is null but stopId is set then returns the longest trip
	 * pattern that serves the specified stopId. If both tripPatternId and
	 * stopId are null then returns longest trip pattern each direction.
	 * 
	 * @param dbRoute
	 *            The route to get the trip patterns for
	 * @param directionId
	 *            Set if want to know which part of route is major and which is
	 *            minor. Otherwise set to null.
	 * @param stopId
	 *            Used only when tripPatternId is null. In that case determines
	 *            which trip patterns are appropriate.
	 * @param tripPatternId
	 *            The specified trip pattern to get. If null then will use
	 *            longest trip pattern for each direction.
	 * @return List of trip patterns for specified stopId and tripPatternId
	 */
	private static List<TripPattern> getUiTripPatterns(Route dbRoute,
			String directionId, String stopId, String tripPatternId) {
		// For returning results
		List<TripPattern> tripPatterns = new ArrayList<TripPattern>();

		// If tripPatternId specified then return that trip pattern
		if (tripPatternId != null) {
			TripPattern tripPattern = dbRoute.getTripPattern(tripPatternId);
			if (tripPattern != null)
				tripPatterns.add(tripPattern);
			return tripPatterns;
		} else {
			// tripPatternId not specified. If stop specified but direction
			// is not (if direction specified then should use longest
			// trip pattern for direction)...
			if (directionId == null && stopId != null) {
				// Determine longest trip pattern that serves the stop
				List<TripPattern> longestTripPatterns = 
						dbRoute.getLongestTripPatternForEachDirection();
				for (TripPattern tripPattern : longestTripPatterns) {
					if (tripPattern.servesStop(stopId)) {
						tripPatterns.add(tripPattern);
						return tripPatterns;
					}
				}
			} else {
				// so return longest trip pattern for appropriate directions
				if (directionId != null) {
					// directionId specified so return longest trip pattern 
					// just for this direction
					tripPatterns.add(dbRoute
							.getLongestTripPatternForDirection(directionId));
					return tripPatterns;
				} else {
					// directionId not specified so return longest trip 
					// patterns for each direction
					return dbRoute.getLongestTripPatternForEachDirection();
				}
			}
		}
		
		// Couldn't find the proper trip pattern specified by the stopId
		// and tripPatternId so return empty array
		return tripPatterns;
	}

	/**
	 * Returns all of the stops for the route. If stopId and directionId or
	 * tripPatternId are specified then the ones remaining for the specified
	 * trip are considered to be UI stops that can be indicated specially in the
	 * UI.
	 * 
	 * @param dbRoute
	 * @param directionId
	 *            Set if want to know which part of route is major and which is
	 *            minor. Otherwise set to null.
	 * @param stopId
	 * @param tripPatternId
	 * @return
	 */
	private static IpcDirectionsForRoute createStops(Route dbRoute,
			String directionId, String stopId, String tripPatternId) {
		// Get the ordered stop IDs per direction for the route
		Map<String, List<String>> orderedStopsByDirection =
				dbRoute.getOrderedStopsByDirection();

		// For determining which stops are part of the UI trip patterns
		List<TripPattern> uiTripPatterns = 
				getUiTripPatterns(dbRoute, directionId, stopId, tripPatternId);

		// Go through all the stops for the route and put corresponding IpcStops
		// into IpcDirections.
		List<IpcDirection> ipcDirections = new ArrayList<IpcDirection>();
		for (String currentDirectionId : orderedStopsByDirection.keySet()) {
			List<IpcStop> ipcStopsForDirection = new ArrayList<IpcStop>();			
			for (String currentStopId : 
					orderedStopsByDirection.get(currentDirectionId)) {
				// Determine if UI stop. It is a UI stop if the stopId parameter
				// specified and the current stop is after the stopId for a UI
				// trip pattern.
				boolean isUiStop = true;
				if (stopId != null) {
					isUiStop = false;
					for (TripPattern tripPattern : uiTripPatterns) {
						if (tripPattern.isStopAtOrAfterStop(stopId,
								currentStopId)) {
							isUiStop = true;
							break;
						}
					}
				}
				
				// Create the IpcStop and add it to the list of stops for the 
				// current direction
				Stop stop = 
						Core.getInstance().getDbConfig().getStop(currentStopId);
				IpcStop ipcStop =
						new IpcStop(stop, isUiStop, currentDirectionId);
				ipcStopsForDirection.add(ipcStop);
			}
			ipcDirections.add(new IpcDirection(dbRoute, currentDirectionId,
					ipcStopsForDirection));
		}
		
		// Returns results
		return new IpcDirectionsForRoute(ipcDirections);
	}

	/**
	 * Returns all of the shapes for the route. If stopId and tripPatternId are
	 * specified then the shape remaining for the specified trip is considered
	 * to be UI shape that can be indicated specially in the UI.
	 * 
	 * @param dbRoute
	 * @param directionId
	 *            Set if want to know which part of route is major and which is
	 *            minor. Otherwise set to null.
	 * @param stopId
	 * @param tripPatternId
	 * @return
	 */
	private static Collection<IpcShape> createShapes(Route dbRoute,
			String directionId, String stopId, String tripPatternId) {
		// Create the stops and shapes arrays for this object
		Collection<IpcShape> shapes = new ArrayList<IpcShape>();
		
		// For keeping track of which stop paths were handled
		// as part of the UI stop paths.
		Set<String> uiStopPathIds = new HashSet<String>();
		
		// First determine the paths remaining in the specified trip
		// pattern.	Add these as UI stops and paths. If tripPatternId not 
		// specified then use the longest trip pattern for each direction
		// as a UI trip pattern.
		List<TripPattern> uiTripPatterns = 
				getUiTripPatterns(dbRoute, directionId, stopId, tripPatternId);
		for (TripPattern tripPattern : uiTripPatterns) {
			// Create a special UI shape for the part of this trip pattern
			// that is after the specified stop.
			IpcShape ipcShapeForUi = 
					new IpcShape(tripPattern, true /* isUiShape */);
			shapes.add(ipcShapeForUi);
			
			boolean stopFound = false;
			// Go through each stop for the UI trip pattern
			for (StopPath stopPath : tripPattern.getStopPaths()) {
				// If not stopId specified or found desired start of UI 
				// portion of trip pattern
				if (stopId == null || stopPath.getStopId().equals(stopId)) {
					stopFound = true;
					// Don't want to actually include this stop path since
					// the passenger wouldn't be taking it since it only leads
					// up to the stop simply continue to next stop path
					if (stopId != null)
						continue;
				}
				
				// If this stop path is part of the UI portion of the
				// trip pattern then remember it as such
				if (stopFound) {
					// Add the segments for this path if it is beyond the
					// the specified stop. Don't include the specified stop
					// because don't want to include the path leading up to
					// it as part of the UI shapes.
					if (!stopPath.getId().equals(stopId)) {
						// Remember that this stop path was dealt with as a UI stop path
						uiStopPathIds.add(stopPath.getId());
						
						// Add the locations to the shape
						ipcShapeForUi.add(stopPath.getLocations());
					}
				}
			}
		}
		
		// Add all the non-UI paths for the route
		IpcShape ipcShape = null;
		List<TripPattern> tripPatternsForRoute = Core.getInstance()
				.getDbConfig().getTripPatternsForRoute(dbRoute.getId());
		for (TripPattern tripPattern : tripPatternsForRoute) {
			boolean begginingOfSpan = true;
			for (StopPath stopPath : tripPattern.getStopPaths()) {
				// If haven't handled this particular stop path yet then do 
				// so now
				if (!uiStopPathIds.contains(stopPath.getId())) {
					// If beginning of continuous span then need to create a new
					// shape and record the beginning point of the stop path.
					if (begginingOfSpan) {
						// Create a new ipc shape
						ipcShape = new IpcShape(tripPattern, false /* isUiShape */);
						shapes.add(ipcShape);
					}					
					begginingOfSpan = false;
										
					// Add the rest of the locations to the shape
					List<Location> locs = stopPath.getLocations();
					ipcShape.add(locs);
				} else {
					// Already dealt with this stop path. This means the next 
					// time get an unhandled stop path it will be the beginning
					// of a span.
					begginingOfSpan = true;
				}
			}
		}
		
		// Return results
		return shapes;
	}

	@Override
	public String toString() {
		return "IpcRoute [" 
				+ "id=" + id 
				+ ", shortName=" + shortName 
				+ ", name=" + name 
				+ ", extent=" + extent
				+ ", type=" + type 
				+ ", color=" + color 
				+ ", textColor=" + textColor 
				+ ", stops=" + stops
				+ ", shapes=" + shapes
				+ ", locationOfNextPredictedVehicle=" 
					+ locationOfNextPredictedVehicle
				+ "]";
	}

	public IpcDirectionsForRoute getStops() {
		return stops;
	}

	public Collection<IpcShape> getShapes() {
		return shapes;
	}

	public Location getLocationOfNextPredictedVehicle() {
		return locationOfNextPredictedVehicle;
	}
	
}
