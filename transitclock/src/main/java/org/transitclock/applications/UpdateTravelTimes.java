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

package org.transitclock.applications;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.ConfigFileReader;
import org.transitclock.configData.AgencyConfig;
import org.transitclock.core.travelTimes.TravelTimeInfoMap;
import org.transitclock.core.travelTimes.TravelTimeInfoWithHowSet;
import org.transitclock.core.travelTimes.TravelTimesProcessor;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.structs.*;
import org.transitclock.utils.IntervalTimer;
import org.transitclock.utils.Time;

import java.text.ParseException;
import java.util.*;

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

	public static final int MAX_TRAVEL_TIME = 20 * Time.MS_PER_MIN;
	public static final int MAX_STOP_TIME = 20 * Time.MS_PER_MIN;
	
	// Read in configuration files. This should be done statically before
	// the logback LoggerFactory.getLogger() is called so that logback can
	// also be configured using a transitclock config file. The files are
	// specified using the java system property -Dtransitclock.configFiles .
	static {
		ConfigFileReader.processConfig();
	}
	
	private static final Logger logger = 
			LoggerFactory.getLogger(UpdateTravelTimes.class);

	/********************** Member Functions **************************/

	/**
	 * For each trip it finds and sets the best travel times. Then the Trip
	 * objects can be stored in db and the corresponding travel times will also
	 * be stored.
	 * <p>
	 * Also updates ActiveRevisions so that new value will be written to db
	 * when the session is closed.
	 * 
	 * @param session
	 * @param tripMap
	 *            Map of all of the trips. Keyed on tripId.
	 * @param travelTimeInfoMap
	 *            Contains travel times that are available by trip pattern ID
	 * @return the newly created travelTimesRev
	 */
	private static int setTravelTimesForAllTrips(Session session,
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
		ActiveRevisions activeRevisions = ActiveRevisions.get(session);
		int currentTravelTimesRev = activeRevisions.getTravelTimesRev();
		int newTravelTimesRev = currentTravelTimesRev + 1;
		
		// Update travel time rev in activeRevisions so that will be written
		// to db when session is flushed.
		activeRevisions.setTravelTimesRev(newTravelTimesRev);
		logger.info("Revisions being set in database to {}", activeRevisions);
				
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
					// Determine travel times to use. There are situations 
					// where won't determine proper travel times, such as
					// for first stop of trip. For this case should use
					// previous value.
					List<Integer> travelTimes;
					if (travelTimeInfo.areTravelTimesValid(MAX_TRAVEL_TIME)) {
						travelTimes = travelTimeInfo.getTravelTimes();
					} else {
						// Travel times not valid so use old values
						TravelTimesForStopPath originalTravelTimes =
								trip.getTravelTimesForStopPath(stopIdx);
						travelTimes = originalTravelTimes.getTravelTimesMsec();
						logger.error("For trip={} stop={} invalid travel times from {} so falling back "
								+ "on old travel times from {}", trip, stopIdx, travelTimeInfo, originalTravelTimes);
					}
					
					// Determine stop time to use. There are situations where
					// only get an arrival time and no departure time for a stop
					// so don't get stop time. This can happen at end of 
					// assignment, if vehicle goes off route, if vehicle doesn't
					// continue, stop getting AVL data for vehicle, etc. For 
					// this situation use the old stop time.
					int stopTime;
					if (travelTimeInfo.isStopTimeValid(MAX_STOP_TIME)) {
						stopTime = travelTimeInfo.getStopTime();
					} else {
						// Stop time not valid so use old time
						TravelTimesForStopPath originalTravelTimes =
								trip.getTravelTimesForStopPath(stopIdx);
						stopTime = originalTravelTimes.getStopTimeMsec();
						logger.error("For trip={} stop={} invalid stop times from {} so falling back "
								+ "on old stop time from {}", trip, stopIdx, travelTimeInfo, originalTravelTimes);
					}
					// Create and add the travel time for this stop path
					ttForStopPathToUse = 
							new TravelTimesForStopPath(
									trip.getConfigRev(),
									newTravelTimesRev,
									trip.getStopPath(stopIdx).getId(), 
									travelTimeInfo.getTravelTimeSegLength(),
									clampTravelTimes(travelTimes),
									clampStopTime(stopTime),
									-1,  // daysOfWeekOverride
									travelTimeInfo.howSet(),
									trip);
				} else {
					// No historic data so use old travel time info based on
					// schedule. Therefore need to use old travel times.
					// Determine original travel times
					TravelTimesForStopPath originalTravelTimes =
							trip.getTravelTimesForStopPath(stopIdx);
					
					logger.error("No historic data so using old travel times {}", originalTravelTimes);

					// Create copy of the original travel times but update the 
					// travel time rev.
					// while copying ensure the values are sane / prune out invalid values
					ttForStopPathToUse = 
							originalTravelTimes.cloneAndClamp(newTravelTimesRev, MAX_STOP_TIME, MAX_TRAVEL_TIME);
				}

				// If already have created the exact same TravelTimesForStopPath 
				// then use the existing one so don't generate too many db 
				// objects.
				// if we've clamped above this should return null
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
			
			// If already created the exact same TravelTimesForTrip 
			// then use the existing one so don't generate too many db objects
			TravelTimesForTrip cachedTTForTrip = ttForTripCache.get(ttForTrip);
			if (cachedTTForTrip == null) {
				// Haven't encountered this TravelTimesForStopPath so add it
				// to the cache. Will end up storing this in db.
				ttForTripCache.put(ttForTrip, ttForTrip);
			} else {
				// Already created equivalent TravelTimesForStopPath so use it.
				ttForTrip = cachedTTForTrip;
			}
			
			// Log old and new travel times so can compare them
			if (logger.isDebugEnabled()) {
				TravelTimesForTrip originalTravelTimes = trip.getTravelTimes();
				logger.debug("For tripId={} \n" 
						+ "originalTravelTimes={}\n\n"
						+ "newTravelTimes={}", 
						trip.getId(), 
						originalTravelTimes.toStringWithNewlines(), 
						ttForTrip.toStringWithNewlines());
			}
			
			// Store the new travel times as part of the trip
			trip.setTravelTimes(ttForTrip);
		} // End of for each trip that is configured	
		return newTravelTimesRev;
	}

	// Make sure travelTimes aren't unreasonably large
	private static List<Integer> clampTravelTimes(List<Integer> travelTimes) {
		List<Integer> clampedTravelTimes = new ArrayList<>(travelTimes.size());
		for (Integer tt : travelTimes) {
			if (tt > MAX_TRAVEL_TIME) {
				clampedTravelTimes.add(0);
			} else {
				clampedTravelTimes.add(tt);
			}
		}
		return clampedTravelTimes;
	}

	// Make sure stopTime isn't unreasonably large
	private static int clampStopTime(int stopTime) {
		if (stopTime > MAX_STOP_TIME) {
			stopTime = 0;
		}
		return stopTime;
	}

	/**
	 * Writes the trips to the database.
	 * 
	 * @param session
	 * @param tripMap
	 */
	private static void writeNewTripDataToDb(Session session,
			Map<String, Trip> tripMap) {
		// Log the trips. Only do this if debug enabled because the
		// trip info along with travel times is really verbose.
		if (logger.isDebugEnabled()) {
			logger.debug("The trips with the new trip times are:");
			for (Trip trip : tripMap.values()) {
				logger.debug(trip.toLongString());
			}
		}
		
		// Write out the trips to the database. This also writes out the
		// cascading data which includes the new travel times.
		logger.info("Flushing data to database...");
		session.flush();
		logger.info("Done flushing");
	}
	
	/**
	 * Read in the current Trips that are configured for the active rev of the
	 * configuration. This should be done after the historical data is read in
	 * so that less memory is used at once.
	 * 
	 * @param agencyId
	 * @param session
	 * @return
	 */
	private static Map<String, Trip> readTripsFromDb(String agencyId,
			Session session) {
	  Map<String, Trip> tripMap = new HashMap<String, Trip>() ;
	  IntervalTimer timer = new IntervalTimer();
	  try {
  		ActiveRevisions activeRevisions = ActiveRevisions.get(session); 
  		logger.info("Reading in trips from db...");
  		tripMap = 
  				Trip.getTrips(session, activeRevisions.getConfigRev());
	  } finally {
	    logger.info("Reading in trips from db took {} msec", timer.elapsedMsec());
	  }
		// Return results
		return tripMap;
	}
	
	/**
	 * Reads historic data from db and processes it, putting it all into a
	 * TravelTimeInfoMap. Then stores the travel times for all of the trips.
	 * <p>
	 * Also updates ActiveRevisions so that new value will be written to db when
	 * the session is closed.
	 * 
	 * @param session
	 * @param agencyId
	 * @param specialDaysOfWeek
	 *            Not fully implemented. Should therefore be null for now.
	 * @param beginTime
	 * @param endTime
	 * @return the newly created travelTimeRev
	 */
	private static int processTravelTimes(Session session, String agencyId,
			List<Integer> specialDaysOfWeek, Date beginTime, Date endTime) {
		// Read in historic data from db and put it into maps so that it can
		// be processed.
		TravelTimesProcessor processor = new TravelTimesProcessor();
		processor.readAndProcessHistoricData(agencyId, specialDaysOfWeek,
				beginTime, endTime);

		if (processor.isEmpty()) {
		  logger.info("Exiting...");
		  return -1;
		}
		
		// Read in the current Trips. This is done after the historical data
		// is read in so that less memory is used at once.
		logger.info("reading trips...");
		Map<String, Trip> tripMap = readTripsFromDb(agencyId, session);
		
		logger.info("processing travel times...");
		// Process the historic data into a simple TravelTimeInfoMap
		TravelTimeInfoMap travelTimeInfoMap = 
				processor.createTravelTimesFromMaps(tripMap);
		
		logger.info("assigning travel times...");
		// Update all the Trip objects with the new travel times
		int travelTimesRev = setTravelTimesForAllTrips(session, tripMap, travelTimeInfoMap);

		logger.info("saving travel times...");
		// Write out the trip objects, which also writes out the travel times
		writeNewTripDataToDb(session, tripMap);
		
		logger.info("committing....");
		return travelTimesRev;
	}

	/**
	 * Creates a session and reads historic data from db and processes it,
	 * putting it all into a TravelTimeInfoMap. Then stores the travel times for
	 * all of the trips.
	 * <p>
	 * Also updates ActiveRevisions so that new value will be written to db when
	 * the session is closed.
	 * 
	 * @param agencyId
	 * @param specialDaysOfWeek
	 *            Not fully implemented. Should therefore be null for now.
	 * @param beginTime
	 * @param endTime
	 */
	public static void manageSessionAndProcessTravelTimes(String agencyId,
			List<Integer> specialDaysOfWeek, Date beginTime, Date endTime) {
	  int newTravelTimesRev = -2;
		// Get a database session
		Session session = HibernateUtils.getSession(agencyId);
		Transaction tx = null;
		try {
			// Put db access into a transaction 
			tx = session.beginTransaction();

			// Actually do all the data processing
			newTravelTimesRev = processTravelTimes(session, agencyId, specialDaysOfWeek, beginTime,
					endTime);
			
			// Make sure that everything actually written out to db
			tx.commit();
		} catch (Exception e) {
			if (tx != null)
				tx.rollback();
			logger.error("Unexpected exception occurred", e);
			throw e;
		} finally {
			// Close up db connection
			session.close();
		}
		
		logger.info("Done processing travel times. Changes successfully "
				+ "committed to database.  Querying for metrics....");
		HibernateUtils.clearSessionFactory();
    Session statsSession = HibernateUtils.getSession(agencyId);
    try {
      TravelTimesProcessor processor = new TravelTimesProcessor();
      Long inserts = processor.updateMetrics(statsSession, newTravelTimesRev);
      logger.info("{} succesfully inserted for travelTimesRev={}", inserts, newTravelTimesRev);
    } catch (Exception e) {
      logger.error("exception querying for statistics for tavelTimesRev={}.  Update most likely failed!", newTravelTimesRev, e);
    }
    
	}
	
	/**
	 * arg[0] specifies both the start date and end date. If an addition
	 * argument is specified it is used as the end date. Otherwise the data is
	 * processed for just a single day.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		logger.info("Starting update travel times");
		// Determine the parameters
		String agencyId = AgencyConfig.getAgencyId();
		
		String startDateStr = args[0];
		String endDateStr = args.length > 1 ? args[1] : startDateStr;

		logger.info("Starting Date {}", startDateStr);
		logger.info("End Date {}", endDateStr);
		
		// Some params are hard coded simply to get things going
//		List<Integer> specialDaysOfWeek = new ArrayList<Integer>();
//		specialDaysOfWeek.add(java.util.Calendar.FRIDAY);
		List<Integer> specialDaysOfWeek = null;
		
		int configRev = ActiveRevisions.get(agencyId).getConfigRev();
		if (args.length > 2) {
			configRev = Integer.parseInt(args[2]);
		}

		logger.info("Config Revision {}", configRev);
		// Set the timezone for the application. Must be done before
		// determine begin and end time so that get the proper time of day.
		TimeZone timezone =
				Agency.getAgencies(agencyId, configRev).get(0).getTimeZone();
		TimeZone.setDefault(timezone);

		// Determine beginTime and endTime
		Date beginTime = null;
		Date endTime = null;
		try {
			logger.info("Parse Date");
			beginTime = Time.parseDate(startDateStr);
			endTime = new Date(Time.parseDate(endDateStr).getTime() + 
					Time.MS_PER_DAY);
		} catch (ParseException e) {
			logger.error("Problem parsing date", e);
			e.printStackTrace();
			System.exit(-1);
		}

		// Log params used right at top of log file
		logger.info("Processing travel times for beginTime={} endTime={}",
				startDateStr, endDateStr);
		
		// Do all the work...
		manageSessionAndProcessTravelTimes(agencyId, specialDaysOfWeek,
				beginTime, endTime);
		
		// program won't just exit on its own, probably due to their being
		// another thread still running. Not sure why. Probably has to do
		// with changes to how Core is constructed. For now simply exit.
		System.exit(0);
		
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
