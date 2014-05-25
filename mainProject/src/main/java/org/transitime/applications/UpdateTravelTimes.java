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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.configData.CoreConfig;
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
	 * @param session
	 * @param projectId
	 * @param tripMap
	 *            Map of all of the trips. Keyed on tripId.
	 * @param travelTimeInfoMap
	 *            Contains travel times that are available by trip pattern ID
	 */
	private static void setTravelTimesForAllTrips(Session session, String projectId,
			Map<String, Trip> tripMap, TravelTimeInfoMap travelTimeInfoMap) {
		// For caching TravelTimesForTrip and TravelTimesForStopPaths that are
		// created. This way won't store duplicate objects. Caching both 
		// because want to reduce object use as much as possible. Of course
		// this won't matter if processing travel times for a couple of weeks
		// because then will get unique data for almost every trip/stop. 
		// But at least it will speed things up initially when working
		// with smaller data sets.
		Map<TravelTimesForTrip, TravelTimesForTrip> ttForTripCache =
				new HashMap<TravelTimesForTrip, TravelTimesForTrip>();
		Map<TravelTimesForStopPath, TravelTimesForStopPath> ttForStopPathCache = 
				new HashMap<TravelTimesForStopPath, TravelTimesForStopPath>();
		
		// Determine which travel times rev is currently being used and which
		// rev should be used for the new travel times.
		ActiveRevisions activeRevisions = ActiveRevisions.get(projectId);
		int currentTravelTimesRev = activeRevisions.getTravelTimesRev();
		int newTravelTimesRev = currentTravelTimesRev + 1;
		
		// Store the new travelTimesRev but don't actually do so until
		// the session is committed.
		activeRevisions.setTravelTimesRev(session, newTravelTimesRev);
		
		// For every single trip that is configured...
		for (Trip trip : tripMap.values()) {			
			// Create a new TravelTimesForTrip object to be used since the
			// old one will have different travel time rev and different
			// values. 
			TravelTimesForTrip ttForTrip = new TravelTimesForTrip(
					trip.getConfigRev(), // Not creating whole new config
					newTravelTimesRev, trip);
			
			// For every single stop path for the configured trip...
			int numStopsInTrip = trip.getTripPattern().getNumberStopPaths();
			for (int stopIdx=0; stopIdx<numStopsInTrip; ++stopIdx) {
				// Get historic AVL based data for this trip/stop
				TravelTimeInfoWithHowSet travelTimeInfo =
						travelTimeInfoMap.getBestMatch(trip, stopIdx);
				
				// Determine the travel times to use for the stop path.
				// If there was historic data then use it to create travel time
				// info object. But if no data then use old values that are 
				// based on the schedule.
				TravelTimesForStopPath ttForStopPathToUse;
				if (travelTimeInfo != null) {
					// Determine stop time to use. There are situations where
					// only get an arrival time and no departure time for a stop
					// so don't get stop time. This can happen at end of 
					// assignment, if vehicle goes off route, if vehicle doesn't
					// continue, stop getting AVL data for vehicle, etc. For 
					// this situation use the old stop time.
					int stopTime;
					if (travelTimeInfo.isStopTimeValid()) {
						stopTime = travelTimeInfo.getStopTime();
					} else {
						// Stop time not valid so use old time
						TravelTimesForStopPath originalTravelTimes =
								trip.getTravelTimesForStopPath(stopIdx);
						stopTime = originalTravelTimes.getStopTimeMsec();
					}
					// Create and add the travel time for this stop path
					ttForStopPathToUse = 
							new TravelTimesForStopPath(
									trip.getStopPath(stopIdx).getId(), 
									travelTimeInfo.getTravelTimeSegLength(),
									travelTimeInfo.getTravelTimes(),
									stopTime,
									-1,  // daysOfWeekOverride
									travelTimeInfo.howSet());
				} else {
					// No historic data so use old travel time info based on
					// schedule. Therefore need to use old travel times.
					// Determine original travel times
					TravelTimesForStopPath originalTravelTimes =
							trip.getTravelTimesForStopPath(stopIdx);

					// Create copy of the original travel times but update the 
					// travel time rev.
					ttForStopPathToUse = 
							(TravelTimesForStopPath) originalTravelTimes.clone();
				}

				// FIXME test this!
				// If already have created the exact same TravelTimesForStopPath 
				// then use the existing one so don't generate too many db 
				// objects.
				TravelTimesForStopPath cachedTTForStopPath =
						ttForStopPathCache.get(ttForStopPathToUse);
				if (cachedTTForStopPath == null) {
					// Haven't encountered this TravelTimesForStopPath so add it
					// to the cache. Will end up storing this in db.
					ttForStopPathCache.put(ttForStopPathToUse,
							ttForStopPathToUse);
				} else {
					// Already created equivalent TravelTimesForStopPath so use it.
					ttForStopPathToUse = cachedTTForStopPath;
				}
				
				// Update the travel times so that the travel times for path 
				// will be stored to db
				ttForTrip.add(ttForStopPathToUse);
			}
			
			// FIXME test this!!
			// If already created the exact same TravelTimesForTrip 
			// then use the existing one so don't generate too many db 
			TravelTimesForTrip cachedTTForTrip =
					ttForTripCache.get(ttForTrip);
			if (cachedTTForTrip == null) {
				// Haven't encountered this TravelTimesForStopPath so add it
				// to the cache. Will end up storing this in db.
				ttForTripCache.put(ttForTrip, ttForTrip);
			} else {
				// Already created equivalent TravelTimesForStopPath so use it.
				ttForTrip = cachedTTForTrip;
			}
			
			
			// Store the new travel times as part of the trip
			trip.setTravelTimes(ttForTrip);
		} // End of for each trip that is configured		
	}
	
	/**
	 * Writes the trips to the session so that they will be stored when
	 * the session is committed.
	 * 
	 * @param session
	 * @param tripMap
	 */
	private static void writeNewTripDataToDb(Session session,
			Map<String, Trip> tripMap) {
		// Write out the trips to the database. This also writes out the
		// cascading data which includes the new travel times.
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
		setTravelTimesForAllTrips(session, projectId, tripMap, travelTimeInfoMap);
		
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
		String projectId = CoreConfig.getProjectId();
		
		String startDateStr = "5-23-2014";
		String endDateStr = "5-23-14";
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
