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

	private Collection<IpcStop> stops;
	private Collection<IpcShape> shapes;
	
	private static final long serialVersionUID = -227901807027962547L;

	/********************** Member Functions **************************/

	public IpcRoute(Route dbRoute, String stopId,
			String tripPatternId) {
		// Construct the core part of the route, everything but
		// the stops and paths.
		super(dbRoute);
		
		// Create Collections of stops and paths
		createStopsAndPaths(dbRoute, stopId,
				tripPatternId);
	}

	private void createStopsAndPaths(Route dbRoute, String stopId,
			String tripPatternId) {
		// Create the stops and shapes arrays for this object
		stops = new ArrayList<IpcStop>(100);
		shapes = new ArrayList<IpcShape>();
		
		// For keeping track of which stops and stop paths were handled
		// as part of the UI stops and paths.
		Set<String> uiStopsIds = new HashSet<String>();
		Set<String> uiStopPathIds = new HashSet<String>();
		
		// First determine the stops and paths remaining in the specified trip
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
					if (stopPath.getStopId().equals(stopId)) {
						stopFound = true;
					}
					
					// If this stop path is part of the UI portion of the
					// trip pattern then remember it as such
					if (stopFound) {
						uiStopsIds.add(stopPath.getId());
						Stop stop = Core.getInstance().getDbConfig()
								.getStop(stopPath.getStopId());
						stops.add(new IpcStop(stop, true /* isUiStop */));
						
						// Add the segments for this path if it is beyond the
						// the specified stop. Don't include the specified stop
						// because don't want to include the path leading up to
						// it as part of the UI shapes.
						if (!stopPath.getId().equals(stopId)) {
							// Remember that this stop path was dealt with as a UI stop path
							uiStopPathIds.add(stopPath.getId());
							
							List<Location> locs = stopPath.getLocations();
							// Only add the first location if it is the beginning
							// of the UI shape
							if (ipcShapeForUi.getLocations().isEmpty())
								ipcShapeForUi.add(locs.get(0));
							
							// Add the rest of the locations to the shape
							for (int i=1; i<locs.size(); ++i) {
								ipcShapeForUi.add(locs.get(i));
							}
						}
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
						
						// Add start of path
						ipcShape.add(stopPath.getLocation(0));
					}					
					begginingOfSpan = false;
										
					// Add the rest of the locations to the shape
					List<Location> locs = stopPath.getLocations();
					for (int i=1; i<locs.size(); ++i) {
						ipcShape.add(locs.get(i));
					}
				} else {
					// Already dealt with this stop path. This means the next 
					// time get an unhandled stop path it will be the beginning
					// of a span.
					begginingOfSpan = true;
				}
			}
		}
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
