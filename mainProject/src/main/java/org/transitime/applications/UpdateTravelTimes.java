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
	// wrong trip or such. In that case the data would only skew the
	// stop time.
	private final static int MAX_SCHED_ADH_FOR_FIRST_STOP_TIME = 
			15*Time.MS_PER_MIN;
	
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
	 * Process historic data from database for single trip
	 * 
	 * @param arrDepList
	 */
	private static void processTripData(List<ArrivalDeparture> arrDepList) {
		
		for (int i=0; i<arrDepList.size()-1; ++i) {
			ArrivalDeparture arrDep1 = arrDepList.get(i);
			
			// If first stop of trip handle specially
			if (arrDep1.getStopPathIndex() == 0) {
				// Should only process departures for the first stop, so make sure.
				// If not a departure then continue to the next stop
				if (arrDep1.isArrival())
					continue;

				// First stop in trip so just deal with departure time.
				// Don't need to deal with travel time.
				int lateTimeMsec = (int) (arrDep1.getScheduledTime().getTime() - 
						arrDep1.getTime().getTime());

				// If schedule adherence is really far off then ignore the data
				// point because it would skew the results.
				if (Math.abs(lateTimeMsec) > MAX_SCHED_ADH_FOR_FIRST_STOP_TIME) 
					continue;
				
				// Get the MapKey so can put stop time into map
				ProcessedDataMapKey mapKeyForTravelTimes = 
						getKey(arrDep1.getServiceId(), arrDep1.getTripId(), arrDep1.getStopPathIndex());

				// Add this stop time to map so it can be averaged
				addStopTimeToMap(mapKeyForTravelTimes, lateTimeMsec);
			} else {
				// Not the first stop in trip so deal with travel times
				ArrivalDeparture arrDep2 = arrDepList.get(i+1);
				
				// If the path index between two stops
				if (arrDep2.getStopPathIndex() - arrDep1.getStopPathIndex() != 1)
					continue;
				
				// FIXME
			}
		}		
	}
	
	/**
	 * Process all the historic data read from the database.
	 * 
	 * @param arrDepMap
	 * @param matchesMap
	 */
	private static void processData(
			Map<DbDataMapKey, List<ArrivalDeparture>> arrDepMap,
			Map<DbDataMapKey, List<Match>> matchesMap) {
		for (List<ArrivalDeparture> arrDepList : arrDepMap.values()) {
			debugLogTrip(arrDepList);
			processTripData(arrDepList);
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// FIXME These are hard coded simply to get things going
		String projectId = "sf-muni";
		String startDateStr = "4-4-2014";
		String endDateStr = "4-10-14";
		
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
		
		// Initialize calendar information
		DataFetcher dataFetcher = 
				new DataFetcher(projectId, specialDaysOfWeek);
		
		// Read in arrival/departure times and matches from db
		Map<DbDataMapKey, List<Match>> matchesMap =
				dataFetcher.readMatches(projectId, beginTime, endTime);
		
		Map<DbDataMapKey, List<ArrivalDeparture>> arrivalDepartureMap =
				dataFetcher.readArrivalsDepartures(projectId, beginTime, endTime);
		
		processData(arrivalDepartureMap, matchesMap);
				
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
