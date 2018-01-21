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
import java.util.List;

import org.transitime.applications.Core;
import org.transitime.core.dataCache.PredictionDataCache;
import org.transitime.db.structs.Location;
import org.transitime.db.structs.Route;
import org.transitime.db.structs.StopPath;
import org.transitime.db.structs.TripPattern;
import org.transitime.ipc.data.IpcPredictionsForRouteStopDest;
import org.transitime.utils.Geo;

/**
 * For determining which stops are near a location. This information
 * can then be used to provide predictions for a location.
 *
 * @author SkiBu Smith
 *
 */
public class StopsByLoc {

	// When looking for nearest stop should bias a bit to the next one
	// in the trip pattern. This way if a user is in between two stops
	// it will match to the second one, giving the passenger a bit more
	// time to get there plus less travel time on the bus. 
	private final static double BIAS_TO_NEXT_STOP_OFFSET = 40.0;
	
	/********************** Member Functions **************************/

	/**
	 * For describing a stop. Can be used to look up corresponding predictions.
	 */
	public static class StopInfo {
		public TripPattern tripPattern;
		public String routeShortName;
		public String stopId;
		public double distanceToStop;
		
		public StopInfo(TripPattern tripPattern, String routeShortName,
				String stopId, double distanceToStop) {
			this.tripPattern = tripPattern;
			this.routeShortName = routeShortName;
			this.stopId = stopId;
			this.distanceToStop = distanceToStop;
		}
		
		@Override
		public String toString() {
			return "StopInfo [" 
					+ "tripPatternID=" + tripPattern.getId() 
					+ ", routeShortName=" + routeShortName 
					+ ", stopId=" + stopId
					+ ", distanceToStop=" + Geo.distanceFormat(distanceToStop) 
					+ "]";
		}
	}
	
	/**
	 * For the specified trip pattern, determines the closest stop to the
	 * location. Will return null if the best stop is further away than
	 * maxDistance. Will return null if stop is last one for trip pattern,
	 * indicating that passengers cannot board there.
	 * 
	 * @param tripPattern
	 * @param loc
	 * @param maxDistance
	 * @return The closest stop for the trip pattern that is not the last stop
	 *         of the trip pattern and is within maxDistance of the loc.
	 */
	private static StopInfo determineClosestStop(TripPattern tripPattern,
			Location loc, double maxDistance) {
		// Determine the closest stop for the specified trip pattern.
		// Don't look at last stop for the trip pattern because
		// passenger can't board at that stop so not point providing
		// predictions for such a stop.
		double bestDistance = Double.MAX_VALUE;
		StopPath bestStopPath = null;
		List<StopPath> stopPaths = tripPattern.getStopPaths();
		for (int i=0; i<stopPaths.size(); ++i) {
			StopPath stopPath = stopPaths.get(i);
			double distanceToStop = stopPath.getStopLocation().distance(loc);
			// If this is the closest stop for the trip pattern remember it
			// as such. Bias to the later stop since then passenger will have
			// more time to get there and a shorter transit ride.
			if (distanceToStop < bestDistance + BIAS_TO_NEXT_STOP_OFFSET) {
				// If this stop that is the closest is the last stop of the
				// trip then simply return null since having arrival info
				// at terminal is confusing
				if (i == stopPaths.size()-1)
					return null;
				
				// Not last stop of trip so remember it as best one
				bestDistance = distanceToStop;
				bestStopPath = stopPath;
			}
		}
		
		// If too far away then no match
		if (bestDistance > maxDistance)
			return null;
		
		// Found the best stop so return the info
		return new StopInfo(tripPattern, tripPattern.getRouteShortName(),
				bestStopPath.getStopId(), bestDistance);
	}
	
	/**
	 * Determines from the matchesForDirection parameter the stop that is
	 * nearest that actually hash predictions. This way won't return a stop
	 * for a trip pattern that is not currently in service. Yet will still
	 * return closest viable stop.
	 * 
	 * @param matchesForDirection
	 * @return
	 */
	private static StopInfo determineBestStopBasedOnPredictions(
			List<StopInfo> matchesForDirection) {
		StopInfo nearestStopWithPrediction = null;
		
		// There are multiple trip matches with a match so 
		// determine best one by looking at the predictions
		for (StopInfo stopInfo : matchesForDirection) {
			List<IpcPredictionsForRouteStopDest> predictionsForStop = 
					PredictionDataCache.getInstance().getPredictions(
							stopInfo.routeShortName, stopInfo.stopId);
			
			// Is this the nearest stop with a prediction?
			if (!predictionsForStop.isEmpty()
					&& (nearestStopWithPrediction == null 
						|| stopInfo.distanceToStop < 
							nearestStopWithPrediction.distanceToStop)) {
				nearestStopWithPrediction = stopInfo;
			}
		}

		return nearestStopWithPrediction;
	}
	
	/**
	 * Returns true if all of the matches are for the same stop ID.
	 * 
	 * @param matchesForDirection
	 * @return True if all of the matches are for the same stop ID.
	 */
	private static boolean matchesAreForSameStop(
			List<StopInfo> matchesForDirection) {
		if (matchesForDirection.size() == 1)
			return true;
		
		String firstStopId = matchesForDirection.get(0).stopId;
		for (StopInfo stopInfo : matchesForDirection) {
			if (!stopInfo.stopId.equals(firstStopId))
				return false;
		}
		
		return true;
	}
	
	/**
	 * Gets list of stops that are within maxDistance of the specified location.
	 * Looks at every trip pattern so can deal with complicated cases such as
	 * routes with school service stops just for part of the day.
	 * 
	 * @param loc
	 * @param maxDistance
	 * @return
	 */
	public static List<StopInfo> getStops(Location loc, double maxDistance) {
		// For returning the results
		List<StopInfo> results = new ArrayList<StopInfo>();
		
		// Find closest stops for every route...
		DbConfig dbConfig = Core.getInstance().getDbConfig();
		for (Route route : dbConfig.getRoutes()) {
			// If the specified location is not within the distance of the route
			// then can skip this route
			if (!route.getExtent().isWithinDistance(loc, maxDistance))
				continue;
			
			// Need to look at trip patterns separately since don't just want
			// to match to a closest stop that happens to not be in service
			// at the time (such as a special school stop) and then not get
			// predictions for the route. So for each direction for each
			// trip pattern find closest stop. Then look at predictions
			// for those stops. Use the stop that provides the most useful
			// predictions.
			for (String directionId : route.getDirectionIds()) {
				// So can look at matches for all trip patterns for direction
				// at once.
				List<StopInfo> matchesForDirection = 
						new ArrayList<StopInfo>();
				
				List<TripPattern> tripPatternsForDirection = 
						route.getTripPatterns(directionId);
				for (TripPattern tripPattern : tripPatternsForDirection) {
					// Determine the closest stop for the trip pattern
					StopInfo stopInfo = 
							determineClosestStop(tripPattern, loc, maxDistance);
					
					// If valid stop found then go on to next trip pattern
					if (stopInfo == null)
						continue;
					
					// So can look at matches for all trip patterns for direction
					// at once.					
					matchesForDirection.add(stopInfo);
				}
				
				// Now that have matches for all trip patterns for the direction
				// need to determine which is the best one.
				if (matchesForDirection.size() >= 1 
						&& matchesAreForSameStop(matchesForDirection)) {
					// There is just a single stop so use it
					results.add(matchesForDirection.get(0));
				} else if (matchesForDirection.size() > 1) {
					// Matches are for different stops so determine best stop 
					// based on predictions
					StopInfo stopInfo = determineBestStopBasedOnPredictions(
							matchesForDirection);
					
					if (stopInfo != null)
						results.add(stopInfo);
				}
			}
		}
		
		// Ah, done
		return results;
	}
	
}
