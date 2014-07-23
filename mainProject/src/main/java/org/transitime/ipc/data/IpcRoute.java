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
import java.util.Set;

import org.transitime.applications.Core;
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

	// Note that there are additional members in IpcRouteSummary superclass
	private Collection<IpcStop> stops;
	private Collection<IpcShape> shapes;
	
	private static final long serialVersionUID = -227901807027962547L;

	/********************** Member Functions **************************/

	/**
	 * Create an IpcRoute that contains all stops and paths but separates out
	 * information for the remaining part of the trip specified by stopId and
	 * tripPatternId.
	 * 
	 * @param dbRoute
	 * @param stopId
	 * @param tripPatternId
	 */
	public IpcRoute(Route dbRoute, String stopId,
			String tripPatternId) {
		// Construct the core part of the route, everything but
		// the stops and paths.
		super(dbRoute);
		
		// Create Collections of stops and paths
		stops = createStops(dbRoute, stopId, tripPatternId);
		shapes = createShapes(dbRoute, stopId, tripPatternId);
	}

	/**
	 * Returns all of the stops for the route. If stopId and tripPatternId are
	 * specified then the ones remaining for the specified trip are considered
	 * to be UI stops that can be indicated specially in the UI.
	 * 
	 * @param dbRoute
	 * @param stopId
	 * @param tripPatternId
	 * @return
	 */
	private static Collection<IpcStop> createStops(Route dbRoute, String stopId,
			String tripPatternId) {
		// Create the stops and shapes arrays for this object
		Collection<IpcStop> stops = new ArrayList<IpcStop>(100);
		
		// For keeping track of which stops were handled
		// as part of the UI stops.
		Set<String> uiStopsIds = new HashSet<String>();
		
		// First determine the stops remaining in the specified trip
		// pattern.	Add these as UI stops and paths.	
		List<TripPattern> tripPatternsForRoute = Core.getInstance()
				.getDbConfig().getTripPatternsForRoute(dbRoute.getId());
		for (TripPattern tripPattern : tripPatternsForRoute) {
			// If this is the UI trip pattern...
			if (tripPattern.getId().equals(tripPatternId)) {			
				boolean stopFound = false;
				// Go through each stop path for the UI trip pattern
				for (StopPath stopPath : tripPattern.getStopPaths()) {
					// If found desired start of UI portion of trip pattern
					if (stopPath.getStopId().equals(stopId))
						stopFound = true;
					
					// If this stop path is part of the UI portion of the
					// trip pattern then remember it as such
					if (stopFound) {
						uiStopsIds.add(stopPath.getStopId());
						Stop stop = Core.getInstance().getDbConfig()
								.getStop(stopPath.getStopId());
						stops.add(new IpcStop(stop, true /* isUiStop */));
					}
				}
				
				// Done with finding and processing UI portion of specified 
				// trip pattern
				break;
			}
		}
		
		// Add all of the non-UI stops for the route
		Collection<Stop> allStopsForRoute = dbRoute.getStops();
		for (Stop dbStop : allStopsForRoute) {
			// If not already added as a UI stop...
			if (!uiStopsIds.contains(dbStop.getId())) {
				stops.add(new IpcStop(dbStop, false /* isUiStop */));
			}
		}
		
		// Return results
		return stops;
	}

	/**
	 * Returns all of the shapes for the route. If stopId and tripPatternId are
	 * specified then the shape remaining for the specified trip is considered
	 * to be UI shape that can be indicated specially in the UI.
	 * 
	 * @param dbRoute
	 * @param stopId
	 * @param tripPatternId
	 * @return
	 */
	private static Collection<IpcShape> createShapes(Route dbRoute, String stopId,
			String tripPatternId) {
		// Create the stops and shapes arrays for this object
		Collection<IpcShape> shapes = new ArrayList<IpcShape>();
		
		// For keeping track of which stop paths were handled
		// as part of the UI stop paths.
		Set<String> uiStopPathIds = new HashSet<String>();
		
		// First determine the paths remaining in the specified trip
		// pattern.	Add these as UI stops and paths.	
		List<TripPattern> tripPatternsForRoute = Core.getInstance()
				.getDbConfig().getTripPatternsForRoute(dbRoute.getId());
		for (TripPattern tripPattern : tripPatternsForRoute) {
			// If this is the UI trip pattern...
			if (tripPattern.getId().equals(tripPatternId)) {
				// Create a special UI shape for the part of this trip pattern
				// that is after the specified stop.
				IpcShape ipcShapeForUi = 
						new IpcShape(tripPattern, true /* isUiShape */);
				shapes.add(ipcShapeForUi);
				
				boolean stopFound = false;
				// Go through each stop for the UI trip pattern
				for (StopPath stopPath : tripPattern.getStopPaths()) {
					// If found desired start of UI portion of trip pattern
					if (stopPath.getStopId().equals(stopId))
						stopFound = true;
					
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
				
				// Done with finding and processing UI portion of specified 
				// trip pattern
				break;
			}
		}
		
		// Add all the non-UI paths for the route
		IpcShape ipcShape = null;
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
				+ "]";
	}

	public Collection<IpcStop> getStops() {
		return stops;
	}

	public Collection<IpcShape> getShapes() {
		return shapes;
	}
}
