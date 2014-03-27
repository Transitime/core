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
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.db.hibernate.HibernateUtils;
import org.transitime.db.structs.StopPath;
import org.transitime.db.structs.ScheduleTime;
import org.transitime.db.structs.TravelTimesForStopPath;
import org.transitime.db.structs.TravelTimesForTrip;
import org.transitime.db.structs.TravelTimesForTrip.HowSet;
import org.transitime.db.structs.Trip;
import org.transitime.db.structs.TripPattern;
import org.transitime.gtfs.gtfsStructs.GtfsStopTime;
import org.transitime.utils.IntervalTimer;
import org.transitime.utils.Time;

/**
 * For setting travel times to default values as needed when GTFS data read
 * in. The idea is that when GTFS data read in sometimes there will be
 * new routes or stopPaths. For these want some kind of travel times so that
 * the prediction software will work. But don't have any AVL data yet
 * for these stopPaths since they are new. Therefore need to either use 
 * data from another service Id or nearby time.
 * 
 * @author SkiBu Smith
 * 
 */
public class TravelTimesProcessor {

	private final int defaultWaitTimeAtStopMsec; 
	private final double maxTravelTimeSegmentLength;
	
	// For when determining when to flush when writing data
	private int counter = 0;
	
	// For keeping track of which project working with
	private final String projectId;
	
	// For reading and writing to db
	private final SessionFactory sessionFactory;
	
	// 0.036m/msec = 36.0m/s = 80mph
	private static final double MAX_TRAVEL_SPEED_IN_METERS_PER_MSEC = 0.036; 
	
	private static final Logger logger = 
			LoggerFactory.getLogger(TravelTimesProcessor.class);


	/********************** Member Functions **************************/

	public TravelTimesProcessor(String projectId,
			double maxTravelTimeSegmentLength, int defaultWaitTimeAtStopMsec) {
		this.projectId = projectId;
		this.sessionFactory = HibernateUtils.getSessionFactory(projectId);
		this.maxTravelTimeSegmentLength = maxTravelTimeSegmentLength;
		this.defaultWaitTimeAtStopMsec = defaultWaitTimeAtStopMsec;
	}
	
	/**
	 * Returns the GTFS stop time for the specified trip and stop. For
	 * processing travel times need to use GTFS stop times instead of the trip
	 * times since trip times can be filtered and only contain times for
	 * schedule adherence stops.
	 * 
	 * NOTE: this method simply does a linear search for the appropriate stop
	 * through the list of travel times associated with the trip. Therefore it
	 * could be rather slow! Might need to have GtfsData provide a ordered map
	 * of GtfsStopTimes instead of just a List of them.
	 * 
	 * @param tripId
	 * @param stopId
	 * @param gtfsData
	 * @return
	 */
	private ScheduleTime getGtfsScheduleTime(String tripId, String stopId,
			GtfsData gtfsData) {
		// Go through list of stops for the tripId
		List<GtfsStopTime> gtfsStopTimesList = 
				gtfsData.getGtfsStopTimesForTrip(tripId);
		for (GtfsStopTime gtfsStopTime : gtfsStopTimesList) {
			if (gtfsStopTime.getStopId().equals(stopId)) {
				// Found the stop. 
				Integer arr = gtfsStopTime.getArrivalTimeSecs();
				Integer dep = gtfsStopTime.getDepartureTimeSecs();
				if (arr == null && dep == null)
					return null;
				else
					return new ScheduleTime(arr, dep);
			}
		}
		
		// Didn't find the stop so can't return SchduleTime
		return null;
	}
	
	/**
	 * Creates a TravelTimesForTrip from the schedule times from the Trip
	 * passed in.
	 * 
	 * @param tripPattern
	 * @return
	 */
	private TravelTimesForTrip determineTravelTimesBasedOnSchedule(Trip trip, 
			GtfsData gtfsData) {
		// Convenience variable 
		TripPattern tripPattern = trip.getTripPattern();
		
		// Create the TravelTimesForTrip object to be returned
		TravelTimesForTrip travelTimes = 
				new TravelTimesForTrip(DbConfig.SANDBOX_REV, trip, HowSet.SCHEDULE_TIMES);
		
		// Handle first path specially since it is a special case where it is
		// simply a stub path. It therefore has no travel or stop time.
		ArrayList<Integer> firstPathTravelTimesMsec = new ArrayList<Integer>();
		StopPath firstPath = trip.getStopPath(0);
		for (int i=0; i<firstPath.getLocations().size()-1; ++i) { 
			firstPathTravelTimesMsec.add(0);
		}
		String firstPathId = tripPattern.getStopPathId(0);
		TravelTimesForStopPath firstPathTravelTimesForPath = 
				new TravelTimesForStopPath(
						firstPathId, firstPath.length(), firstPathTravelTimesMsec, 
						0,   // stopTimeMsec
						-1); // daysOfWeekOverride
		travelTimes.add(firstPathTravelTimesForPath);
		
		// Go through the schedule times for the trip pattern.
		// Start at index 1 since the first stub path is a special case
		int previousStopPathWithScheduleTimeIndex = 0;
		ScheduleTime previousScheduleTime = trip.getScheduleTime(tripPattern.getStopId(0));
		int numberOfPaths = trip.getTripPattern().getNumberStopPaths();
		for (int stopPathIndex = 1; stopPathIndex < numberOfPaths; ++stopPathIndex) {
			// Determine the schedule time for the stop using the GTFS data directly.
			// Can't use the trip stop times because those can be filtered to just
			// be for schedule adherence stops, which could be a small subset of the 
			// schedule times from the stop_times.txt GTFS file.
			ScheduleTime scheduleTime = getGtfsScheduleTime(trip.getId(), 
					tripPattern.getStopId(stopPathIndex), gtfsData);
			if (scheduleTime != null) {
				// Determine time elapsed between schedule times for
				// each path between the schedule times
				int newTimeInSecs = scheduleTime.getTime();
				int oldTimeInSecs = previousScheduleTime.getTime();
				int elapsedScheduleTimeInSecs = newTimeInSecs - oldTimeInSecs;
				
				// Determine distance traveled
				double distanceBetweenScheduleStops = 0.0;
				for (int i=previousStopPathWithScheduleTimeIndex+1; 
						i<=stopPathIndex; 
						++i) {
					String pathId = tripPattern.getStopPathId(i);					
					StopPath path = gtfsData.getPath(tripPattern.getId(), pathId);
					distanceBetweenScheduleStops += path.length();
				}
				
				// Determine averageSpeedMetersPerSecs. Do this by looking at  
				// the total scheduled travel time between the scheduled stops 
				// and subtracting out the time that will at stopped at stops.
				int numberOfStops = 
						stopPathIndex - previousStopPathWithScheduleTimeIndex;
				int msecSpentStopped = numberOfStops * defaultWaitTimeAtStopMsec;
				if (msecSpentStopped > elapsedScheduleTimeInSecs*1000)
					msecSpentStopped = elapsedScheduleTimeInSecs*1000;
				int msecForTravelBetweenScheduleTimes = 
						elapsedScheduleTimeInSecs*1000 - msecSpentStopped;
				// Make sure that speed is reasonable. An agency might 
				// mistakenly only provide a few seconds of travel time and
				// that could be completely eaten away by expected stop times.
				if (msecForTravelBetweenScheduleTimes == 0 || 
						distanceBetweenScheduleStops / msecForTravelBetweenScheduleTimes > 
						MAX_TRAVEL_SPEED_IN_METERS_PER_MSEC) {
					msecForTravelBetweenScheduleTimes = 
							(int) (distanceBetweenScheduleStops / 
									MAX_TRAVEL_SPEED_IN_METERS_PER_MSEC);
				}
				
				// For each stop path between the last schedule stop and the 
				// current one...
				for (int i=previousStopPathWithScheduleTimeIndex+1; 
						i<=stopPathIndex; 
						++i) {
					// Determine the stop path
					String stopPathId = tripPattern.getStopPathId(i);
					StopPath path = gtfsData.getPath(tripPattern.getId(), stopPathId);

					// For this path determine the number of travel times 
					// segments and their lengths. They will be no longer 
					// than maxTravelTimeSegmentLength.
					double pathLength = path.length();
					int numberTravelTimeSegments;
					double travelTimeSegmentsLength;
					if (pathLength > maxTravelTimeSegmentLength) {
						// The stop path is longer then the max so divide it into
						// shorter travel time segments
						numberTravelTimeSegments = (int) (pathLength /
								maxTravelTimeSegmentLength + 0.999999999);
						travelTimeSegmentsLength = 
								pathLength / numberTravelTimeSegments;
					} else {
						// The stop path length is less then the max so can use
						// just a single travel time segment
						numberTravelTimeSegments = 1;
						travelTimeSegmentsLength = pathLength;
					}
					
					// For this stop path determine the travel times. 				
					ArrayList<Integer> travelTimesMsec = new ArrayList<Integer>();
					int travelTimeForSegmentMsec = (int) 
							((travelTimeSegmentsLength / distanceBetweenScheduleStops) * 
									msecForTravelBetweenScheduleTimes);
					for (int j = 0; j < numberTravelTimeSegments; ++j) {
						// Add the travel time for the segment to the list
						travelTimesMsec.add(travelTimeForSegmentMsec);
					}
					
					TravelTimesForStopPath travelTimesForStopPath = 
							new TravelTimesForStopPath(
									stopPathId, 
									travelTimeSegmentsLength,
									travelTimesMsec, 
									defaultWaitTimeAtStopMsec,    // stopTimeMsec
									-1);  // daysOfWeekOverride
					travelTimes.add(travelTimesForStopPath);
				}
				
				// For next iteration in for loop
				previousStopPathWithScheduleTimeIndex = stopPathIndex;
				previousScheduleTime = scheduleTime;
			}
		}

		// Return the results
		return travelTimes;
	}
	
	/**
	 * Sees if the travel times for the trip from the database are an adequate
	 * match for the trip pattern such that existing travel times can be reused.
	 * They are an adequate match if the path IDs are the same and the travel
	 * times were generated via GPS or the schedule times match the new ones
	 * within 60 seconds. Also, number of stopPaths and number of segments per path
	 * for the travel times need to match that for the trip patterns.
	 * 
	 * @param trip
	 * @param newTravelTimes
	 * @param alreadyExistingTravelTimes
	 * @param gtfsData
	 * @return
	 */
	private boolean adequateMatch(Trip trip, TravelTimesForTrip newTravelTimes, 
			TravelTimesForTrip alreadyExistingTravelTimes, GtfsData gtfsData) {
		// Convenience variable
		TripPattern tripPattern = trip.getTripPattern();
		
		// See if stopPaths match. If they don't then return false.
		// First check if trip patterns have same number of stops.
		if (tripPattern.getStopPaths().size() != 
				alreadyExistingTravelTimes.numberOfStopPaths()) {
			logger.error("In TravelTimesProcessor.adequateMatch(), for tripId={} " + 
					"using tripPatternId={} has different number of stops than " +
					"for another tripId={} even though it is associated with the " + 
					"same trip pattern.", 
					trip.getId(), 
					tripPattern.getId(), 
					alreadyExistingTravelTimes.getTripCreatedForId()); 
			return false;
		}
		for (int i=0; i<tripPattern.getStopPaths().size(); ++i) {
			// Make sure the path IDs match
			String pathIdFromTripPattern = 
					tripPattern.getStopPathId(i);
			String pathIdFromTravelTimes = 
					alreadyExistingTravelTimes.getTravelTimesForStopPaths().get(i).getStopPathId();
			if (!pathIdFromTripPattern.equals(pathIdFromTravelTimes)) {
				logger.error("In TravelTimesProcessor.adequateMatch(), for tripId={} " + 
						"using tripPatternId={} has different stopPaths than " +
						"for another tripId={} even though it is associated with the " + 
						"same trip pattern.",
						trip.getId(), 
						tripPattern.getId(), 
						alreadyExistingTravelTimes.getTripCreatedForId()); 
				return false;
			}
		}
		
		// If the travel times from the database are based on GPS then use them. 
		// TODO It would be better to try to match trip ID so using the right 
		// travel times. But that would be complicated so need to put it off
		// until later.
		if (!alreadyExistingTravelTimes.getHowSet().isScheduleBased())
			return true;
		
		// Travel times are based on schedule. See if schedule time is close to
		// the same. If it is, then can use these times
		// Determine the travel times for this trip based on the GTFS
		// schedule times. Then can compare with travel times in database.
		int newTravelTimeMsec = 0;
		int dbTravelTimeMsec = 0;
		for (int i=0; i<newTravelTimes.numberOfStopPaths(); ++i) {
			TravelTimesForStopPath newElement = 
					newTravelTimes.getTravelTimesForStopPath(i);
			TravelTimesForStopPath oldElement = 
					alreadyExistingTravelTimes.getTravelTimesForStopPath(i);
			newTravelTimeMsec += newElement.getStopPathTravelTimeMsec(); 
			dbTravelTimeMsec += oldElement.getStopPathTravelTimeMsec();
			// If travel time to this stop differ by more than a minute
			// then the travel times are not an adequate match.
			if (Math.abs(dbTravelTimeMsec - newTravelTimeMsec) >= 60 * Time.MS_PER_SEC) {
				logger.debug("While looking for already existing usable travel " + 
						"times for tripId={} for tripPatternId={} found that the " +
						"travel times differed by more than 60 seconds for stop " +
						"path index {}, which is for pathId={} for the old tripId={}", 
						trip.getId(), tripPattern.getId(), i, newElement.getStopPathId(),
						alreadyExistingTravelTimes.getTripCreatedForId());
				return false;
			}
		}

		// Travel times didn't vary so it is a good match
		return true;
	}
	
	/**
	 * Goes through every trip and writes out TravelTimesForStopPath object to
	 * db if don't have GPS data for it.
	 * 
	 * @param gtfsData
	 * @param travelTimesFromDbMap Map keyed by tripPatternId of Lists of TripPatterns
	 * @throws HibernateException
	 */
	private void processTrips(GtfsData gtfsData,
			Map<String, List<TravelTimesForTrip>> travelTimesFromDbMap, 
			Session session)
					throws HibernateException {
		// For trip read from GTFS data..
		for (Trip trip : gtfsData.getTrips()) {
			TripPattern tripPattern = trip.getTripPattern();
			
			logger.debug("Processing travel times for tripId={} which " + 
					"is for tripPatternId={} for routeId={}.", 
					trip.getId(), tripPattern.getId(), trip.getRouteId());

			// For determining if can use existing TravelTimesForTrip from database. 
			List<TravelTimesForTrip> travelTimesForTripFromDbList = 
					travelTimesFromDbMap.get(tripPattern.getId());			
				
			// See if any of the existing travel times from the db are adequate.
			// If so, use them.
			TravelTimesForTrip scheduleBasedTravelTimes =
					determineTravelTimesBasedOnSchedule(trip, gtfsData);
			TravelTimesForTrip travelTimesToUse = scheduleBasedTravelTimes;
			
			boolean adequateMatchFoundInDb = false;
			if (travelTimesForTripFromDbList != null) {
				// Go through list and look for suitable match
				for (TravelTimesForTrip travelTimesForTripFromDb : travelTimesForTripFromDbList) {
					// If suitable travel times already in db then use them
					if (adequateMatch(trip, scheduleBasedTravelTimes, travelTimesForTripFromDb, gtfsData)) {
						travelTimesToUse = travelTimesForTripFromDb;
						adequateMatchFoundInDb = true;
						logger.debug("Found adequate travel time match for " + 
								"tripId={} which is for tripPatternId={} so will " + 
								"use the old one created for tripId={}", 
							trip.getId(), tripPattern.getId(), travelTimesForTripFromDb.getTripCreatedForId());
						break;
					}
				}
			}

			// If couldn't find an existing match then will be using the
			// travel times created for this trip. Add these new travel
			// times to the list of travel times from the db so that
			// they can be used for subsequent trips.
			if (!adequateMatchFoundInDb) {
				logger.debug("There was not an adequate travel time match for " + 
						"tripId={} which is for tripPatternId={} so will use new one.", 
					trip.getId(), tripPattern.getId());
				// If list of travel times doesn't exist for this trip pattern yet
				// then create it
				if (travelTimesForTripFromDbList == null) {
					travelTimesForTripFromDbList = new ArrayList<TravelTimesForTrip>();
					travelTimesFromDbMap.put(tripPattern.getId(), travelTimesForTripFromDbList);
				}
				travelTimesForTripFromDbList.add(scheduleBasedTravelTimes);					
			}
			
			// Add the resulting TravelTimesForTrip to the Trip so it can be stored
			// as part of the trip
			trip.setTravelTimes(travelTimesToUse);
		}			
	}
	
	/**
	 * For trips where travel times not set in database via GPS data
	 * default travel times are created by looking at the schedule
	 * times and interpolating.
	 * 
	 * @param gtfsData
	 */
	public void process(GtfsData gtfsData) {
		if (!gtfsData.isTripsReadIn()) {
			logger.error("tripsMap not yet read in by GtfsData before " + 
					"TravelTimesProcessor.process() called. Software " +
					"needs to be fixed");
			System.exit(-1);
		}
		if (!gtfsData.isStopTimesReadIn()) {
			logger.error("GTFS stop times not read in before " + 
					"TravelTimesProcessor.process() called. " + 
					"Software neds to be fixed.");
			System.exit(-1);
		}
		
		// For logging how long things take
		IntervalTimer timer = new IntervalTimer();

		// Let user know what is going on
		logger.info("Processing travel time data...");

		// Read existing data from db and put into travelTimesFromDbMap member
		Map<String, List<TravelTimesForTrip>> travelTimesFromDbMap = 
				TravelTimesForTrip.getTravelTimesForTrip(projectId, 
						DbConfig.SANDBOX_REV);

		int originalNumberTravelTimes = numberTravelTimes(travelTimesFromDbMap);
		
		// Will likely be storing data in db so start transaction
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		
		// Do the low-level processing
		try {
			processTrips(gtfsData, travelTimesFromDbMap, session);
			
			// Done writing data so commit it
			tx.commit();
		} catch (HibernateException e) {
			logger.error("Error writing TravelTimesForTrip to db. " + 
					e.getMessage(), e);
		} 
		catch (Exception e) {
			logger.error("Exception occurred when processing travel times", e);
		}
		finally {
			// Always make sure session gets closed
			session.close();
		}
				
		// Let user know what is going on
		logger.info("Finished processing travel time data. " + 
				"Number of travel times read from db={}. " + 
				"Total number of travel times needed to cover each trip={}. " + 
				"Total number of trips={}. " + 
				"Wrote {} records. Took {} msec.", 
				originalNumberTravelTimes,
				numberTravelTimes(travelTimesFromDbMap),
				gtfsData.getTrips().size(),
				counter, 
				timer.elapsedMsec());
	}
	
	/**
	 * For determining how many travel times generated.
	 * @param travelTimesFromDbMap
	 * @return
	 */
	private static int numberTravelTimes(Map<String, List<TravelTimesForTrip>> travelTimesFromDbMap) {
		int count = 0;
		for (List<TravelTimesForTrip> travelTimes : travelTimesFromDbMap.values()) {
			count += travelTimes.size();
		}
		return count;
	}
}
