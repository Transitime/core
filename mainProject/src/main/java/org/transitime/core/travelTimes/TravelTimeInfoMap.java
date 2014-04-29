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

package org.transitime.core.travelTimes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.transitime.db.structs.TravelTimesForStopPath.HowSet;
import org.transitime.db.structs.Trip;
import org.transitime.utils.MapKey;
import org.transitime.utils.Time;

/**
 * For keeping track of the historic data such that if no data is
 * available for a trip then can find closest trip. Keyed by trip pattern
 *
 * @author SkiBu Smith
 *
 */
public class TravelTimeInfoMap {
	
	// Special key just for travelTimesByTripPattern map
	public static class TripPatternStopMapKey extends MapKey {
		private TripPatternStopMapKey(String tripPatternId, int stopIndex) {
			super(tripPatternId, stopIndex);
		}
	}

	// For keeping track of the historic data such that if no data is
	// available for a trip then can find closest trip. Keyed by trip pattern ID
	// and stop path index.
	private Map<TripPatternStopMapKey, List<TravelTimeInfo>> travelTimesByTripPattern =
			new HashMap<TripPatternStopMapKey, List<TravelTimeInfo>>();

	/********************** Member Functions **************************/
	
	/**
	 * Adds the new TravelTimeInfo object to the static travelTimesByTripPattern
	 * map so that can find best match for trip for when there is no data for
	 * the actual trip.
	 * 
	 * @param travelTimeInfo
	 */
	public void add(TravelTimeInfo travelTimeInfo) {
		String tripPatternId = 
				travelTimeInfo.getTrip().getTripPattern().getId();
		int stopPathIndex = travelTimeInfo.getStopPathIndex();
		TripPatternStopMapKey mapKey =
				new TripPatternStopMapKey(tripPatternId, stopPathIndex);
		
		// Get the list of travel times for the trip pattern. If haven't
		// created the list for the trip pattern ID yet then do so now.
		List<TravelTimeInfo> travelTimeInfosForTripPattern = 
				travelTimesByTripPattern.get(mapKey);
		if (travelTimeInfosForTripPattern == null) {
			travelTimeInfosForTripPattern = new ArrayList<TravelTimeInfo>();
			travelTimesByTripPattern.put(mapKey,
					travelTimeInfosForTripPattern);
		}

		// Actually add the travelTimeInfo to the proper list in the map
		travelTimeInfosForTripPattern.add(travelTimeInfo);
	}
	
	/**
	 * For when don't have historic data for a trip. This method determines the
	 * trip with historic data for the same service class that has the nearest
	 * time. If no historic data for a trip with the same service ID then will
	 * look at trips with other service IDs.
	 * 
	 * @param trip
	 *            The Trip that needs travel time info
	 * @param stopPathIndex
	 *            The stop in the trip that needs travel time info
	 * @return The best match for the trip.
	 */
	public TravelTimeInfoWithHowSet getBestMatch(Trip trip, int stopPathIndex) {
		String tripPatternId = trip.getTripPattern().getId();
		TripPatternStopMapKey mapKey =
				new TripPatternStopMapKey(tripPatternId, stopPathIndex);

		// Get the list of historic travel times for the specified
		// trip pattern.
		List<TravelTimeInfo> travelTimeInfosForTripPattern =
				travelTimesByTripPattern.get(mapKey);
				
		// Go through times and find best one, if there are any. First go 
		// for same service Id
		int bestDifference = Integer.MAX_VALUE;
		TravelTimeInfo bestMatch = null;
		HowSet howSet = null;
		for (TravelTimeInfo travelTimeInfo : travelTimeInfosForTripPattern) {
			Trip thisTrip = travelTimeInfo.getTrip();
			// If for desired service ID...
			if (thisTrip.getServiceId().equals(trip.getServiceId())) {
				int timeDiffOfTripSchedTime = Time.getTimeDifference(
						thisTrip.getStartTime(), trip.getStartTime());
				if (timeDiffOfTripSchedTime < bestDifference) {
					bestDifference = timeDiffOfTripSchedTime;
					bestMatch = travelTimeInfo;
					if (timeDiffOfTripSchedTime == 0) {
						howSet = HowSet.AVL_DATA;
					} else {
						howSet = HowSet.AVL_OTHER_TRIP;
					}
				}
			}
		}
		
		// If didn't find match for the same service class then go for other
		// service classes
		if (bestMatch == null) {
			for (TravelTimeInfo travelTimeInfo : travelTimeInfosForTripPattern) {
				Trip thisTrip = travelTimeInfo.getTrip();
				int timeDiffOfTripSchedTime = Time.getTimeDifference(
						thisTrip.getStartTime(), trip.getStartTime());
				if (timeDiffOfTripSchedTime < bestDifference) {
					bestDifference = timeDiffOfTripSchedTime;
					bestMatch = travelTimeInfo;
					howSet = HowSet.AVL_OTHER_SERVICE;
				}
			}			
		}
		
		// Return the best match. Can be null.
		if (bestMatch != null) {
			TravelTimeInfoWithHowSet result = 
					new TravelTimeInfoWithHowSet(bestMatch, howSet);
			return result;
		} else 
			return null;
	}

}
