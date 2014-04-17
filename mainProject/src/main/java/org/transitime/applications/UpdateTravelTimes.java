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

package org.transitime.applications;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.core.travelTimes.DataFetcher;
import org.transitime.core.travelTimes.DataFetcher.DbDataMapKey;
import org.transitime.db.hibernate.HibernateUtils;
import org.transitime.db.structs.ArrivalDeparture;
import org.transitime.db.structs.Match;
import org.transitime.db.structs.TravelTimesForTrip;
import org.transitime.gtfs.DbConfig;
import org.transitime.utils.MapKey;
import org.transitime.utils.Time;

/**
 * Uses AVL based data of arrival/departure times and matches from the database
 * to update the expected travel and stop times.
 * <p>
 * NOTE: This could probably be made less resource/memory intensive by
 * processing a days worth of data at a time. Another possibility would be to
 * try to process the data while it is being read in instead of reading it all
 * in at the beginning. But that would likely be quite difficult to implement.
 * Processing one day of data at a time would likely be far simpler and
 * therefore a better choice.
 * 
 * @author SkiBu Smith
 * 
 */
public class UpdateTravelTimes {

	// For determining stop time for first stop in trip. Need to limit it 
	// because if vehicle is really late then perhaps it is matched to
	// wrong trip or such. At the very minimum it is an anomaly. In such a 
	// case the data would only skew the stop time.
	private final static int MAX_SCHED_ADH_FOR_FIRST_STOP_TIME = 
			10*Time.MS_PER_MIN;
	
	private final static int MAX_SCHED_ADH =
			30*Time.MS_PER_MIN;
	
	private static double maxTravelTimeSegmentLength;
	
	// DataFetcher needs to be a member so that can get the map key for 
	// retrieving matches. 
	private static DataFetcher dataFetcher;
	
	// The historic data from the database
	private static Map<DbDataMapKey, List<Match>> matchesMap;
	private static Map<DbDataMapKey, List<ArrivalDeparture>> arrivalDepartureMap;		

	// The aggregate data processed from the historic db data.
	// MapKey combines String serviceId, String tripId, String stopIndex in 
	// order to combine data for a particular serviceId, tripId, and stopIndex.
	private static Map<ProcessedDataMapKey, List<Integer>> stopTimesMap = 
			new HashMap<ProcessedDataMapKey, List<Integer>>();	
	private static Map<ProcessedDataMapKey, List<List<Integer>>> travelTimesMap = 
			new HashMap<ProcessedDataMapKey, List<List<Integer>>>();
	
	private static final Logger logger = 
			LoggerFactory.getLogger(UpdateTravelTimes.class);

	/********************** Member Functions **************************/

	/**
	 * Special MapKey class so that can make sure using the proper one for the
	 * associated maps in this class.
	 */
	public static class ProcessedDataMapKey extends MapKey {
		private ProcessedDataMapKey(String serviceId, String tripId, 
				int stopIndex) {
			super(serviceId, tripId, stopIndex);
		}
	}

	private static ProcessedDataMapKey getKey(String serviceId, String tripId,
			int stopIndex) {
		return new ProcessedDataMapKey(serviceId, tripId, stopIndex);
	}
	
	/**
	 * Adds stop times for a stop path for a single trip to the stopTimesMap
	 * @param mapKey
	 * @param stopTimeMsec
	 */
	private static void addStopTimeToMap(ProcessedDataMapKey mapKey,
			int stopTimeMsec) {
		List<Integer> stopTimesForStop = stopTimesMap.get(mapKey);
		if (stopTimesForStop == null) {
			stopTimesForStop = new ArrayList<Integer>();
			stopTimesMap.put(mapKey, stopTimesForStop);
		}
		stopTimesForStop.add(stopTimeMsec);
	}
	
	/**
	 * Adds travel times for stop path for a single trip to the travelTimesMap
	 * 
	 * @param mapKey
	 * @param travelTimesForStopPath
	 */
	private static void addTravelTimesToMap(ProcessedDataMapKey mapKey, 
			List<Integer> travelTimesForStopPath) {
		List<List<Integer>> travelTimesForStop = travelTimesMap.get(mapKey);
		if (travelTimesForStop == null) {
			travelTimesForStop = new ArrayList<List<Integer>>();
			travelTimesMap.put(mapKey, travelTimesForStop);
		}
		travelTimesForStop.add(travelTimesForStopPath);
	}
	
	/**
	 * Just for debugging. Logs raw data for trip.
	 * 
	 * @param arrDepList
	 */
	private static void debugLogTrip(List<ArrivalDeparture> arrDepList) {
		System.err.println("====================");
		for (ArrivalDeparture arrivalDeparture : arrDepList) {
			System.err.println(arrivalDeparture);
		}
	}
	
	/**
	 * For when the arrival/departure is for first stop of trip. If the schedule
	 * adherence isn't too bad adds the stop time to the stop wait map.
	 * 
	 * @param arrDep
	 */
	private static void processFirstStopOfTrip(ArrivalDeparture arrDep) {
		// Only need to handle departure for first stop in trip
		if (arrDep.getStopPathIndex() != 0) 
			return;
		
		// Should only process departures for the first stop, so make sure.
		// If not a departure then continue to the next stop
		if (arrDep.isArrival())
			return;

		// First stop in trip so just deal with departure time.
		// Don't need to deal with travel time.
		int lateTimeMsec = 
				(int) (arrDep.getScheduledTime() - arrDep.getTime());

		// If schedule adherence is really far off then ignore the data
		// point because it would skew the results.
		if (Math.abs(lateTimeMsec) > MAX_SCHED_ADH_FOR_FIRST_STOP_TIME) 
			return;
		
		// Get the MapKey so can put stop time into map
		ProcessedDataMapKey mapKeyForTravelTimes = 
				getKey(arrDep.getServiceId(), arrDep.getTripId(), 
						arrDep.getStopPathIndex());

		// Add this stop time to map so it can be averaged
		addStopTimeToMap(mapKeyForTravelTimes, lateTimeMsec);		
	}
	
	/**
	 * Returns the matches for the particular stopPath for the service ID and
	 * trip.
	 * <P>
	 * TODO Note: This is pretty inefficient. Seems that should be able to get
	 * list of matches for the stopPath directly from the map instead of getting
	 * all the matches for the trip and then filtering them.
	 * 
	 * @param arrDep
	 * @return List of Match objects. Never returns null.
	 */
	private static List<Match> getMatchesForStopPath(ArrivalDeparture arrDep) {
		// For returning the results
		List<Match> matchesForStopPath = new ArrayList<Match>();


		// Get the matches for the entire trip since that is
		// how the data is available
		DbDataMapKey mapKey = dataFetcher.getKey(arrDep.getServiceId(),
				arrDep.getDate(), arrDep.getTripId(), arrDep.getVehicleId());
		List<Match> matchesForTrip = matchesMap.get(mapKey);
		
		// If no matches were found for this trip then return empty
		// array (don't continue since would get NPE).
		if (matchesForTrip == null)
			return matchesForStopPath;
		
		for (Match match : matchesForTrip) {
			if (match.getStopPathIndex() == arrDep.getStopPathIndex())
				matchesForStopPath.add(match);
			else {
				// If looking at matches past the stop then done. Breaking
				// out of the for loop makes the code more efficient.
				if (match.getStopPathIndex() > arrDep.getStopPathIndex())
					break;
			}
		}
		
		return matchesForStopPath;
	}
	
	/**
	 * Internal structure for keeping track of matches that are between an
	 * departure and an arrival. Needed for when there are multiple travel
	 * time segments within a stop path. 
	 */
	private static class MatchPoint {
		private long time;
		private float distance;
		private MatchPoint(long time, float distance) {
			this.time = time;
			this.distance = distance;
		}
	}
	
	/**
	 * Number of travel time segments for the stop path specified by the
	 * arrDep param.
	 * 
	 * @param arrDep
	 * @return
	 */
	private static int getNumTravelTimeSegments(ArrivalDeparture arrDep) {
		int numberTravelTimeSegments = 
				(int) (arrDep.getStopPathLength() / maxTravelTimeSegmentLength +
						0.99999999);
		
		return numberTravelTimeSegments;
	}
	
	/**
	 * The travel time length is the length of the stop path divided into equal
	 * segments such that the segments are no longer than
	 * maxTravelTimeSegmentLength.
	 * 
	 * @param arrDep
	 *            The arrival stop for which to determine the travel time length
	 * @return travel time length for the specified arrival
	 */
	private static double getTravelTimeSegmentLength(ArrivalDeparture arrDep) {
		double segLength =
				arrDep.getStopPathLength() / getNumTravelTimeSegments(arrDep);
		return segLength;
	}
	
	/**
	 * Gets the Matches that are associated with the stop path specified by
	 * arrDep2 (the path leading up to that stop).
	 * 
	 * @param arrDep1
	 *            The departure stop
	 * @param arrDep2
	 *            The arrival stop. Also defines which stop path working with.
	 * @return List of MatchPoints, which contain the basic Match info needed
	 *         for determining travel times.
	 */
	private static List<MatchPoint> getMatchPoints(ArrivalDeparture arrDep1,
			ArrivalDeparture arrDep2) {
		// The array to be returned
		List<MatchPoint> matchPoints = new ArrayList<MatchPoint>();
		
		// Add the departure time to the list of data points
		matchPoints.add(new MatchPoint(arrDep1.getTime(), 0.0f));
		
		// Stop path is long enough such that have more than one travel
		// time segment. Get the corresponding matches
		List<Match> matchesForStopPath = getMatchesForStopPath(arrDep2);

		// Add the matches that are in between the arrival and the departure.
		for (Match match : matchesForStopPath) {
			matchPoints.add(new MatchPoint(match.getTime(), 
					match.getDistanceAlongStopPath()));
		}
		
		// Add the arrival time to the list of data points
		matchPoints.add(new MatchPoint(arrDep2.getTime(), 
				arrDep2.getStopPathLength()));

		// Return the list of time/distance points
		return matchPoints;
	}

	/**
	 * For when the path between stops is long enough such that there are
	 * multiple travel time segments. For the particular stop path, as specified
	 * by the arrival stop, looks up the associated Matches in order to
	 * determine how the vehicle travels along the stop path. Determines when
	 * the travel time segment vertices are crossed and uses the vertices, along
	 * with the departure time and the arrival times at the ends of the stop
	 * path, to determine the travel time for each travel time segment for this
	 * particular trip.
	 * 
	 * @param arrDep1
	 *            The departure stop
	 * @param arrDep2
	 *            The arrival stop. Also defines which stop path working with.
	 * @return List of travel times in msec. There is a separate travel time for
	 *         each travel time segment.
	 */
	private static List<Integer> addTravelTimesForMultiSegments(
			ArrivalDeparture arrDep1, ArrivalDeparture arrDep2) {
		double travelTimeSegmentLength = getTravelTimeSegmentLength(arrDep2);

		List<MatchPoint> matchPoints = getMatchPoints(arrDep1, arrDep2);
		
		List<Long> vertexTimes = new ArrayList<Long>();
		
		// Add departure time from first stop
		vertexTimes.add(arrDep1.getTime());
		
		for (int i=0; i<matchPoints.size()-1; ++i) {
			MatchPoint pt1 = matchPoints.get(i);
			MatchPoint pt2 = matchPoints.get(i+1);
			
			int segIndex1 = (int) (pt1.distance / travelTimeSegmentLength);
			int segIndex2 = (int) (pt2.distance / travelTimeSegmentLength);
			
			// If the two matches span a travel time segment vertex...
			if (segIndex1 != segIndex2) {
				// Determine speed traveled between the two matches
				long timeBtwnMatches = pt2.time - pt1.time;
				float distanceBtwnMatches = pt2.distance - pt1.distance;
				double speed = distanceBtwnMatches / timeBtwnMatches;
				
				// Determine when crossed the first vertex between the match points
				// and add the time to the vertex times
				double distanceOfFirstVertex = 
						(segIndex1+1) * travelTimeSegmentLength;
				double distanceToFirstVertex = distanceOfFirstVertex - pt1.distance;
				long timeAtVertex = pt1.time + (long) (distanceToFirstVertex/speed);
				vertexTimes.add(timeAtVertex);
				
				// Add any subsequent vertices crossed between the match points
				for (int segIndex=segIndex1+1; segIndex<segIndex2; ++segIndex) {
					timeAtVertex += travelTimeSegmentLength / speed;
					vertexTimes.add(timeAtVertex);
				}
			}			
		}
		// Deal with the final travel time segment that goes to the arrival stop
		vertexTimes.add(arrDep2.getTime());
		
		// Now that we have all the vertex times for the stop path determine the
		// travel times and add them to the travel time map.
		List<Integer> travelTimesForStopPath = new ArrayList<Integer>();
		for (int i=0; i<vertexTimes.size()-1; ++i) {
			long vertexTime1 = vertexTimes.get(i);
			long vertexTime2 = vertexTimes.get(i+1);
			travelTimesForStopPath.add((int) (vertexTime2 - vertexTime1));
		}
		return travelTimesForStopPath;
	}
	
	/**
	 * For looking at travel time between two stops.
	 * 
	 * @param departure
	 * @param arrival
	 */
	private static void processDataBetweenTwoArrivalDepartures(
			ArrivalDeparture arrDep1, ArrivalDeparture arrDep2) {
		
		// If schedule adherence is really far off then ignore the data
		// point because it would skew the results.
		Date scheduleDate = arrDep1.getScheduledDate();
		if (scheduleDate == null)
			scheduleDate = arrDep2.getScheduledDate();
		if (scheduleDate != null) {
			int lateTimeMsec = 
					(int) (scheduleDate.getTime() - arrDep2.getTime());
			if (Math.abs(lateTimeMsec) > MAX_SCHED_ADH) 
				return;
		}
		
		// Determine the key for storing the data into appropriate map
		ProcessedDataMapKey mapKeyForTravelTimes = 
				getKey(arrDep2.getServiceId(), arrDep2.getTripId(), 
						arrDep2.getStopPathIndex());

		// If looking at arrival and departure for same stop then determine
		// the stop time.
		if (arrDep1.getStopPathIndex() == arrDep2.getStopPathIndex() 
				&& arrDep1.isArrival() 
				&& arrDep2.isDeparture()) {
			// Determine time at stop
			int dwellTimeMsec = (int) (arrDep2.getTime() - arrDep1.getTime());

			// Add this stop time to map so it can be averaged
			addStopTimeToMap(mapKeyForTravelTimes, dwellTimeMsec);		

			return;
		}
		
		// If looking at departure from one stop to the arrival time at the
		// very next stop then can determine the travel times between the stops.
		if (arrDep1.getStopPathIndex() - arrDep2.getStopPathIndex() != 1
				&& arrDep1.isDeparture()
				&& arrDep2.isArrival()) {
			// If the stopPath is short enough such that there will be
			// only a single travel segment...
			if (arrDep2.getStopPathLength() < maxTravelTimeSegmentLength) {
				// Determine the travel time between the stops
				int travelTimeBetweenStopsMsec = 
						(int) (arrDep2.getTime() - arrDep1.getTime());
				List<Integer> travelTimesForStopPath = new ArrayList<Integer>();
				travelTimesForStopPath.add(travelTimeBetweenStopsMsec);
				addTravelTimesToMap(mapKeyForTravelTimes, travelTimesForStopPath);
			} else {
				// The stop path is long enough such that there will be more
				// than a single travel time segment.
				//
				// Go through the matches for this stop path and use them to
				// determine the travel times for each travel time segment
				// FIXME
				int xxx=9;
				List<Integer> travelTimesForStopPath = addTravelTimesForMultiSegments(arrDep1,
						arrDep2);
				addTravelTimesToMap(mapKeyForTravelTimes, travelTimesForStopPath);
			}
				
			return;
		}
	}
	
	/**
	 * Process historic data from database for single trip
	 * 
	 * @param arrDepList
	 */
	private static void processTripData(List<ArrivalDeparture> arrDepList) {
		
		for (int i=0; i<arrDepList.size()-1; ++i) {
			ArrivalDeparture arrDep1 = arrDepList.get(i);
			
			// Handle first stop in trip specially
			if (arrDep1.getStopPathIndex() == 0) {
				// Don't need to deal with first arrival stop for trip since
				// don't care when vehicle arrives at layover
				if (arrDep1.isArrival())
					continue;

				// Handle first stop
				processFirstStopOfTrip(arrDep1);
			} 
			
			// Deal with normal travel times
			ArrivalDeparture arrDep2 = arrDepList.get(i+1);				
			processDataBetweenTwoArrivalDepartures(arrDep1, arrDep2);							
		}		
	}
	
	/**
	 * Process all the historic data read from the database.
	 */
	private static void processData() {
		for (List<ArrivalDeparture> arrDepList : arrivalDepartureMap.values()) {
			debugLogTrip(arrDepList);
			processTripData(arrDepList);
		}
	}
	
	private static void readHistoricDataFromDb(String projectId, 
			List<Integer> specialDaysOfWeek, Date beginTime, Date endTime) {
		// Initialize calendar information
		dataFetcher = 
				new DataFetcher(projectId, specialDaysOfWeek);

		// Read in arrival/departure times and matches from db
		matchesMap =
				dataFetcher.readMatches(projectId, beginTime, endTime);		
		arrivalDepartureMap =
				dataFetcher.readArrivalsDepartures(projectId, beginTime, endTime);		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// FIXME These are hard coded simply to get things going
		String projectId = "sf-muni";
		String startDateStr = "4-4-2014";
		String endDateStr = "4-10-14";
		maxTravelTimeSegmentLength = 120.0;
		
		List<Integer> specialDaysOfWeek = new ArrayList<Integer>();
		specialDaysOfWeek.add(java.util.Calendar.FRIDAY);
		
		Date beginTime = null;
		Date endTime = null;
		try {
			beginTime = Time.parseDate(startDateStr);
			endTime = new Date(Time.parseDate(endDateStr).getTime() + 
					Time.MS_PER_DAY);
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(-1);
		}
				
		readHistoricDataFromDb(projectId, specialDaysOfWeek, beginTime, endTime);
		
		processData();
				
		// Read in existing travel times from database
		Map<String, List<TravelTimesForTrip>> travelTimesByTripPatternMap = 
				TravelTimesForTrip.getTravelTimesForTrip(projectId, 
						DbConfig.SANDBOX_REV);

		// Get a database session
		SessionFactory sessionFactory = 
				HibernateUtils.getSessionFactory(projectId);
		Session session = sessionFactory.openSession();

		// Write out the data
		// FIXME
		
		// Close up db connection
		session.close();
	}

}
