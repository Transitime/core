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
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.core.travelTimes.TravelTimeInfoMap;
import org.transitime.core.travelTimes.TravelTimeInfoWithHowSet;
import org.transitime.core.travelTimes.TravelTimesProcessor;
import org.transitime.db.hibernate.HibernateUtils;
import org.transitime.db.structs.ActiveRevisions;
import org.transitime.db.structs.TravelTimesForStopPath;
import org.transitime.db.structs.TravelTimesForTrip;
import org.transitime.db.structs.Trip;
import org.transitime.gtfs.DbWriter;
import org.transitime.utils.IntervalTimer;
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
	
	private static final Logger logger = 
			LoggerFactory.getLogger(UpdateTravelTimes.class);

	/********************** Member Functions **************************/

	/**
	 * For each trip it finds and sets the best travel times. Then the Trip
	 * objects can be stored in db and the corresponding travel times will also
	 * be stored.
	 * 
	 * @param projectId
	 * @param tripMap
	 *            Map of all of the trips. Keyed on tripId.
	 * @param travelTimeInfoMap
	 *            Contains travel times that are available by trip pattern ID
	 */
	private static void setTravelTimesForAllTrips(String projectId,
			Map<String, Trip> tripMap, TravelTimeInfoMap travelTimeInfoMap) {
		// Determine which travel times rev is currently being used and which
		// rev should be used for the new travel times.
		ActiveRevisions activeRevisions = ActiveRevisions.get(projectId);
		int currentTravelTimesRev = activeRevisions.getTravelTimesRev();
		int newTravelTimesRev = currentTravelTimesRev + 1;
		
		// For every single trip that is configured...
		for (Trip trip : tripMap.values()) {			
			// Create a new TravelTimesForTrip object to be used since the
			// old one will have different travel time rev and different
			// values. 
			TravelTimesForTrip travelTimesForTrip = new TravelTimesForTrip(
					trip.getConfigRev(), // Same so don't have to create whole new config
					newTravelTimesRev, trip);
			
			// For every single stop path for the trip...
			int numStopsInTrip = trip.getTripPattern().getNumberStopPaths();
			for (int stopIdx=0; stopIdx<numStopsInTrip; ++stopIdx) {
				// Get historic data for this trip/stop
				TravelTimeInfoWithHowSet travelTimeInfo =
						travelTimeInfoMap.getBestMatch(trip, stopIdx);
				
				// Determine original travel times
				TravelTimesForStopPath originalTravelTimes =
						trip.getTravelTimesForStopPath(stopIdx);

				// Determine the travel times to use for the stop path.
				// If there was historic data then use it to create travel time
				// info object. But if no data then use old values that are 
				// based on the schedule.
				TravelTimesForStopPath travelTimesForStopPathToUse;
				if (travelTimeInfo != null) {
					// Create and add the travel time for this stop path
					travelTimesForStopPathToUse = 
							new TravelTimesForStopPath(
									originalTravelTimes.getStopPathId(), 
									travelTimeInfo.getTravelTimeSegLength(),
									travelTimeInfo.getTravelTimes(),
									travelTimeInfo.getStopTime(),
									-1,  // daysOfWeekOverride
									travelTimeInfo.howSet());
				} else {
					// No historic data so use old travel time info based on
					// schedule. Therefore need to use old travel times.
					// Create copy of the original travel times but update the 
					// travel time rev.
					travelTimesForStopPathToUse = 
							(TravelTimesForStopPath) originalTravelTimes.clone();
				}
				
				// Update the travel times so that the travel times for path 
				// will be stored to db
				travelTimesForTrip.add(travelTimesForStopPathToUse);
			}
			
			// Store the new travel times as part of the trip
			trip.setTravelTimes(travelTimesForTrip);
		} // End of for each trip that is configured		
	}
	
	private static void writeNewTripDataToDb(Session session,
			Map<String, Trip> tripMap) {
		// Write out the trips to the database
		// FIXME Test to make sure this also storing the travel times???
		DbWriter.writeTrips(session, tripMap.values());		
	}
	
	/**
	 * Read in the current Trips that are configured for the active rev of the
	 * configuration. This should be done after the historical data is read in
	 * so that less memory is used at once.
	 * 
	 * @param projectId
	 * @param session
	 * @return
	 */
	private static Map<String, Trip> readTripsFromDb(String projectId, Session session) {
		ActiveRevisions activeRevisions = ActiveRevisions.get(projectId);
		IntervalTimer timer = new IntervalTimer();
		logger.info("Reading in trips from db...");
		Map<String, Trip> tripMap = 
				Trip.getTrips(session, activeRevisions.getConfigRev());
		logger.info("Reading in trips from db took {} msec", timer.elapsedMsec());	
		
		// Return results
		return tripMap;
	}
	
	/**
	 * Reads historic data from db and processes it, putting it all into a
	 * TravelTimeInfoMap. Then stores the travel times for all of the trips.
	 * 
	 * @param projectId
	 * @param maxTravelTimeSegmentLength
	 * @param specialDaysOfWeek
	 * @param beginTime
	 * @param endTime
	 */
	private static void processTravelTimes(String projectId,
			double maxTravelTimeSegmentLength, List<Integer> specialDaysOfWeek,
			Date beginTime, Date endTime) {
		// Get a database session
		Session session = HibernateUtils.getSession(projectId);

		// Read in historic data from db and put it into maps so that it can
		// be processed.
		TravelTimesProcessor processor = 
				new TravelTimesProcessor(maxTravelTimeSegmentLength);
		processor.readAndProcessHistoricData(projectId, specialDaysOfWeek,
				beginTime, endTime);

		// Read in the current Trips. This is done after the historical data
		// is read in so that less memory is used at once.
		Map<String, Trip> tripMap = readTripsFromDb(projectId, session);
		
		// Process the historic data into a simple TravelTimeInfoMap
		TravelTimeInfoMap travelTimeInfoMap = 
				processor.createTravelTimesFromMaps(tripMap);
		
		// Update also the Trip objects with the new travel times
		setTravelTimesForAllTrips(projectId, tripMap, travelTimeInfoMap);
		
		// Write out the trip objects, which also writes out the travel times
		writeNewTripDataToDb(session, tripMap);
		
		// Close up db connection
		session.close();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Determine the parameters
		// FIXME These are hard coded simply to get things going
		String projectId = "sf-muni";
		String startDateStr = "4-4-2014";
		String endDateStr = "4-10-14";
		double maxTravelTimeSegmentLength = 120.0;
		
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

		// Do all the work...
		processTravelTimes(projectId, maxTravelTimeSegmentLength,
				specialDaysOfWeek, beginTime, endTime);
		
//		// this is just for debugging
//		Trip trip5889634 = tripMap.get("5889634");
//		Trip trip5889635 = tripMap.get("5889635");
//		trip5889634.setName(trip5889634.getName() + "foo");
//		trip5889635.setName(trip5889635.getName() + "foo");
//		List<Trip> tripList = new ArrayList<Trip>();
//		tripList.add(trip5889634);
//		tripList.add(trip5889635);
//		DbWriter.writeTrips(session, tripList);
	}

}
