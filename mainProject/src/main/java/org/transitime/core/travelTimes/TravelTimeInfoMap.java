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
import org.transitime.utils.Time;

/**
 * For keeping track of the historic data such that if no data is
 * available for a trip then can find closest trip. Keyed by trip pattern
 * and stop path index.
 * @author SkiBu Smith
 *
 */
public class TravelTimeInfoMap {
	
	/*
	 * TravelTimesByStop is to be used as the value in travelTimesByTripPattern.
	 * By declaring it a class can make the code more readable. 
	 */
	@SuppressWarnings("serial")
	private static class TravelTimesByStopMap 
		extends HashMap<Integer, List<TravelTimeInfo>> {		
	}
		
	// For keeping track of the historic data such that if no data is
	// available for a trip then can find closest trip. Keyed by trip pattern
	// ID.
	private Map<String, TravelTimesByStopMap> travelTimesByTripPatternMap =
			new HashMap<String, TravelTimesByStopMap>();

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
		
		// Get the map of travel times by stop map. If haven't
		// created the map for the trip pattern ID yet then do so now.
		TravelTimesByStopMap travelTimesByStopMap =
				travelTimesByTripPatternMap.get(tripPatternId);
		if (travelTimesByStopMap == null) {
			travelTimesByStopMap = new TravelTimesByStopMap();
			travelTimesByTripPatternMap.put(tripPatternId, travelTimesByStopMap);
		}
		
		// Now get the list of travel times for the trip pattern/stop.
		// If haven't created the list yet then do so now.
		List<TravelTimeInfo> travelTimeInfosForTripPatternAndStop
			= travelTimesByStopMap.get(stopPathIndex);
		if (travelTimeInfosForTripPatternAndStop == null) {
			travelTimeInfosForTripPatternAndStop = new ArrayList<TravelTimeInfo>();
			travelTimesByStopMap.put(stopPathIndex, travelTimeInfosForTripPatternAndStop);
		}

		// Actually add the travelTimeInfo to the proper list in the map
		travelTimeInfosForTripPatternAndStop.add(travelTimeInfo);
	}
	
	/**
	 * Returns true if there is at least some historic data for the specified
	 * trip pattern.
	 * 
	 * @param tripPatternId
	 *            Trip pattern to see if there is data for
	 * @return True if there is historic data
	 */
	public boolean dataExists(String tripPatternId) {
		TravelTimesByStopMap travelTimesByStopMap = 
				travelTimesByTripPatternMap.get(tripPatternId);
		return travelTimesByStopMap != null;
	}
	
	/**
	 * Returns the list, containing one TravelTimeInfo for each vehicle that
	 * traveled on a trip.
	 * 
	 * @param tripPatternId
	 * @param stopPathIndex
	 * @return List of TravelTimeInfo data obtained from historic info. Returns
	 *         null if no data available for the trip pattern/stop.
	 */
	private List<TravelTimeInfo> getTravelTimeInfos(String tripPatternId,
			int stopPathIndex) {
		TravelTimesByStopMap travelTimesByStopMap = 
				travelTimesByTripPatternMap.get(tripPatternId);
		if (travelTimesByStopMap == null)
			return null;

		return travelTimesByStopMap.get(stopPathIndex);
	}
	
	/**
	 * For when don't have historic data for a trip. This method first
	 * determines the trip with historic data for the same service ID that has
	 * the nearest time. If no historic data for a trip with the same service ID
	 * then will look at trips with other service IDs.
	 * 
	 * @param trip
	 *            The Trip that needs travel time info
	 * @param stopPathIndex
	 *            The stop in the trip that needs travel time info
	 * @return The best match for the trip or null if there was no historic info
	 *         for the tripPatternId/stopPathIndex.
	 */
	public TravelTimeInfoWithHowSet getBestMatch(Trip trip, int stopPathIndex) {
		// Get the list of historic travel times for the specified
		// trip pattern.
		String tripPatternId = trip.getTripPattern().getId();
		List<TravelTimeInfo> timesForTripPatternAndStop =
				getTravelTimeInfos(tripPatternId, stopPathIndex);
				
		// If no historic data at all for this tripPatternId/stopPathIndex
		// then return null.
		if (timesForTripPatternAndStop == null)
			return null;
		
		// Go through times and find best one, if there are any. First go 
		// for same service Id
		int bestDifference = Integer.MAX_VALUE;
		TravelTimeInfo bestMatch = null;
		HowSet howSet = null;
		for (TravelTimeInfo travelTimeInfo : timesForTripPatternAndStop) {
			Trip thisTrip = travelTimeInfo.getTrip();
			// If for desired service ID...
			if (thisTrip.getServiceId().equals(trip.getServiceId())) {
				int timeDiffOfTripSchedTime = Time.getTimeDifference(
						thisTrip.getStartTime(), trip.getStartTime());
				if (timeDiffOfTripSchedTime < bestDifference) {
					bestDifference = timeDiffOfTripSchedTime;
					bestMatch = travelTimeInfo;
					if (timeDiffOfTripSchedTime == 0) {
						howSet = HowSet.AVL;
						
						// Since found perfect match (same exact trip) don't 
						// need to look at the other travel times for the 
						// trip pattern, so continue.
						continue;
					} else {
						// Found a reasonable match, but for another trip.
						// Remember this by setting howSet to TRIP.
						howSet = HowSet.TRIP;
					}
				}
			}
		}
		
		// If didn't find match for the same service class then go for other
		// service classes
		if (bestMatch == null) {
			for (TravelTimeInfo travelTimeInfo : timesForTripPatternAndStop) {
				Trip thisTrip = travelTimeInfo.getTrip();
				int timeDiffOfTripSchedTime = Time.getTimeDifference(
						thisTrip.getStartTime(), trip.getStartTime());
				if (timeDiffOfTripSchedTime < bestDifference) {
					bestDifference = timeDiffOfTripSchedTime;
					bestMatch = travelTimeInfo;
					howSet = HowSet.SERVC;
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
