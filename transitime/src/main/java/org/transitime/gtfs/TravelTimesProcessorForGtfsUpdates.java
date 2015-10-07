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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.db.structs.ActiveRevisions;
import org.transitime.db.structs.StopPath;
import org.transitime.db.structs.ScheduleTime;
import org.transitime.db.structs.TravelTimesForStopPath;
import org.transitime.db.structs.TravelTimesForStopPath.HowSet;
import org.transitime.db.structs.TravelTimesForTrip;
import org.transitime.db.structs.Trip;
import org.transitime.db.structs.TripPattern;
import org.transitime.gtfs.gtfsStructs.GtfsStopTime;
import org.transitime.utils.Geo;
import org.transitime.utils.IntervalTimer;
import org.transitime.utils.Time;

/**
 * For setting travel times when processing GTFS configuration. Tries to use
 * existing travel times from database. If data for trip exists it is used. If
 * data for trip doesn't exist tries to use data for similar trip (see
 * adequateMatch()). For when there is no matching historic travel times data
 * then uses default values based on the schedule. The idea is that when GTFS
 * data read in sometimes there will be new routes or stopPaths. For these want
 * some kind of travel times so that the prediction software will work. But
 * don't have any AVL data yet for these stopPaths since they are new.
 * 
 * @author SkiBu Smith
 * 
 */
public class TravelTimesProcessorForGtfsUpdates {

	// Which config and travel times revs to write data for
	private final ActiveRevisions activeRevisions;
	
	// The original active travel time revision, as read from db
	private final int originalTravelTimesRev;
	
	private final int defaultWaitTimeAtStopMsec; 
	private final double maxSpeedKph;
	private final double maxSpeedMetersPerMsec;
	private final double maxTravelTimeSegmentLength;
	
	private static final Logger logger = 
			LoggerFactory.getLogger(TravelTimesProcessorForGtfsUpdates.class);


	/********************** Member Functions **************************/

	/**
	 * Constructor
	 * 
	 * @param activeRevisions
	 * @param originalTravelTimesRev
	 * @param maxTravelTimeSegmentLength
	 * @param defaultWaitTimeAtStopMsec
	 * @param maxSpeedKph
	 */
	public TravelTimesProcessorForGtfsUpdates(ActiveRevisions activeRevisions,
			int originalTravelTimesRev, double maxTravelTimeSegmentLength,
			int defaultWaitTimeAtStopMsec, double maxSpeedKph) {
		this.activeRevisions = activeRevisions;
		this.maxTravelTimeSegmentLength = maxTravelTimeSegmentLength;
		this.defaultWaitTimeAtStopMsec = defaultWaitTimeAtStopMsec;	
		this.maxSpeedKph = maxSpeedKph;
		this.maxSpeedMetersPerMsec = maxSpeedKph * Geo.KPH_TO_MPS / Time.MS_PER_SEC;
		this.originalTravelTimesRev = originalTravelTimesRev;
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
	 * @param tripPattern
	 * @param stopPathIndex
	 * @param gtfsData
	 * @return
	 */
	private ScheduleTime getGtfsScheduleTime(String tripId,
			TripPattern tripPattern, int stopPathIndex, GtfsData gtfsData) {
		String stopId = tripPattern.getStopId(stopPathIndex);
		
		// Go through list of stops for the tripId. 
		// Note: bit tricky handling trips where stop is encountered twice.
		// But at least can start the for loop at 1 so that don't
		// look at all the stop times. At least this will properly handle the
		// situations where the first and last stop for a trip are the same.
		List<GtfsStopTime> gtfsStopTimesList = 
				gtfsData.getGtfsStopTimesForTrip(tripId);
		for (int stopTimeIdx = stopPathIndex>0 ? 1 : 0; 
				stopTimeIdx < gtfsStopTimesList.size(); 
				++stopTimeIdx) {
			GtfsStopTime gtfsStopTime = gtfsStopTimesList.get(stopTimeIdx);
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
		TravelTimesForTrip travelTimes = new TravelTimesForTrip(
				activeRevisions.getConfigRev(),
				activeRevisions.getTravelTimesRev(), trip);
		
		// Handle first path specially since it is a special case where it is
		// simply a stub path. It therefore has no travel or stop time.
		ArrayList<Integer> firstPathTravelTimesMsec = new ArrayList<Integer>();
		firstPathTravelTimesMsec.add(0);

		StopPath firstPath = trip.getStopPath(0);
		TravelTimesForStopPath firstPathTravelTimesForPath = 
				new TravelTimesForStopPath(
						activeRevisions.getConfigRev(), 
						activeRevisions.getTravelTimesRev(),
						firstPath.getId(), firstPath.length(), 
						firstPathTravelTimesMsec, 
						0,   // stopTimeMsec
						-1,  // daysOfWeekOverride
						HowSet.SCHED,
						trip); 
		travelTimes.add(firstPathTravelTimesForPath);
		
		// Go through the schedule times for the trip pattern.
		// Start at index 1 since the first stub path is a special case
		int previousStopPathWithScheduleTimeIndex = 0;
		ScheduleTime previousScheduleTime = getGtfsScheduleTime(trip.getId(), 
				tripPattern, 0, gtfsData);
		int numberOfPaths = trip.getTripPattern().getNumberStopPaths();
		for (int stopPathWithScheduleTimeIndex = 1; 
				stopPathWithScheduleTimeIndex < numberOfPaths; 
				++stopPathWithScheduleTimeIndex) {
			// Determine the schedule time for the stop using the GTFS data directly.
			// Can't use the trip stop times because those can be filtered to just
			// be for schedule adherence stops, which could be a small subset of the 
			// schedule times from the stop_times.txt GTFS file.
			ScheduleTime scheduleTime = getGtfsScheduleTime(trip.getId(), 
					tripPattern, stopPathWithScheduleTimeIndex, gtfsData);
			if (scheduleTime == null) 
				continue;
			
			// Determine time elapsed between schedule times for
			// each path between the schedule times
			int newTimeInSecs = scheduleTime.getTime();
			int oldTimeInSecs = previousScheduleTime.getTime();
			int elapsedScheduleTimeInSecs = newTimeInSecs - oldTimeInSecs;
			
			// Determine distance traveled
			double distanceBetweenScheduleStops = 0.0;
			for (int i=previousStopPathWithScheduleTimeIndex+1; 
					i<=stopPathWithScheduleTimeIndex; 
					++i) {
				String pathId = tripPattern.getStopPathId(i);					
				StopPath path = gtfsData.getPath(tripPattern.getId(), pathId);
				distanceBetweenScheduleStops += path.length();
			}
			
			// Determine averageSpeedMetersPerSecs. Do this by looking at  
			// the total scheduled travel time between the scheduled stops 
			// and subtracting out the time that will at stopped at stops.
			int numberOfStopsWithStopTime = 
					stopPathWithScheduleTimeIndex - previousStopPathWithScheduleTimeIndex;
			// Don't need stop time for last stop of trip
			if (stopPathWithScheduleTimeIndex == numberOfPaths - 1)
				--numberOfStopsWithStopTime;
			int msecSpentStopped = numberOfStopsWithStopTime * defaultWaitTimeAtStopMsec;
			if (msecSpentStopped > elapsedScheduleTimeInSecs*1000)
				msecSpentStopped = elapsedScheduleTimeInSecs*1000;
			int msecForTravelBetweenScheduleTimes = 
					elapsedScheduleTimeInSecs*1000 - msecSpentStopped;
			// Make sure that speed is reasonable, taking default wait 
			// stop times into account. An agency might 
			// mistakenly only provide a few seconds of travel time and
			// that could be completely eaten away by expected stop times.
			if (msecForTravelBetweenScheduleTimes == 0
					|| distanceBetweenScheduleStops
							/ msecForTravelBetweenScheduleTimes > maxSpeedMetersPerMsec) {
				// It might be that for a non-rush hour bus route the agency
				// doesn't expect the bus to stop at a stop. So first see if
				// travel time is reasonable if there is no wait time.
				if (elapsedScheduleTimeInSecs * Time.MS_PER_SEC > 0
						&& distanceBetweenScheduleStops
								/ (elapsedScheduleTimeInSecs * Time.MS_PER_SEC) <= maxSpeedMetersPerMsec) {
					// Travel speed is OK if wait time is considered to 
					// be less than the default time. So reduce wait time 
					// such that travel speed limit is not violated.
					msecSpentStopped =
							elapsedScheduleTimeInSecs
									* Time.MS_PER_SEC
									- (int) (distanceBetweenScheduleStops / maxSpeedMetersPerMsec);
					
					logger.warn("When determining schedule based travel "
							+ "times for routeId={} " 
							+ "tripId={} stopPathIndex={} "
							+ "stopId={} stopName=\"{}\" "
							+ "distanceBetweenScheduleStops={} "
							+ "msecForTravelBetweenScheduleTimes={} "
							+ "reducing stop wait time to {} msec "
							+ "per stop so that maxSpeedKph={} kph is "
							+ "not violated.", 
							trip.getRouteId(),
							trip.getId(), stopPathWithScheduleTimeIndex, 
							tripPattern.getStopId(stopPathWithScheduleTimeIndex), 
							gtfsData.getStop(tripPattern.getStopId(stopPathWithScheduleTimeIndex)).getName(),
							Geo.distanceFormat(distanceBetweenScheduleStops),
							msecForTravelBetweenScheduleTimes,
							msecSpentStopped/numberOfStopsWithStopTime, 
							Geo.oneDigitFormat(maxSpeedKph));
				} else {
					// There is a real problem with the schedule time since 
					// even if wait time was reduced to 0 the travel time 
					// based on schedule would be too high.
					logger.error("When determining schedule based travel "
							+ "times for routeId={} " 
							+ "tripId={} stopPathIndex={} "
							+ "stopId={} stopName=\"{}\" "
							+ "distanceBetweenScheduleStops={} " 
							+ "msecForTravelBetweenScheduleTimes={} "
							+ "clamping schedule based travel "
							+ "speed to {} kph for instead of the "
							+ "calculated speed={} kph", 
							trip.getRouteId(), 
							trip.getId(), stopPathWithScheduleTimeIndex, 
							tripPattern.getStopId(stopPathWithScheduleTimeIndex), 
							gtfsData.getStop(tripPattern.getStopId(stopPathWithScheduleTimeIndex)).getName(),
							Geo.distanceFormat(distanceBetweenScheduleStops),
							msecForTravelBetweenScheduleTimes,
							Geo.oneDigitFormat(maxSpeedKph),								
							Geo.oneDigitFormat((distanceBetweenScheduleStops / msecForTravelBetweenScheduleTimes)
									* Time.MS_PER_SEC / Geo.KPH_TO_MPS));

					msecForTravelBetweenScheduleTimes =
							(int) (distanceBetweenScheduleStops / maxSpeedMetersPerMsec);

				}
			}
			
			// For each stop path between the last schedule stop and the 
			// current one...
			for (int stopPathIndex = previousStopPathWithScheduleTimeIndex + 1; 
					stopPathIndex <= stopPathWithScheduleTimeIndex; 
					++stopPathIndex) {
				// Determine the stop path
				String stopPathId = tripPattern.getStopPathId(stopPathIndex);
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
					numberTravelTimeSegments = 
							(int) (pathLength /	maxTravelTimeSegmentLength +
									1.0);
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
				
				// Determine the stop time. Use the default value except
				// use zero for the last stop in the trip since stop time
				// is not used for such a stop. Instead, schedule time and
				// layover time is used.
				int stopTimeMsec = (stopPathIndex < numberOfPaths-1) ? 
						msecSpentStopped/numberOfStopsWithStopTime : 0;
				
				// Create and add the travel time for this stop path
				TravelTimesForStopPath travelTimesForStopPath = 
						new TravelTimesForStopPath(
								activeRevisions.getConfigRev(), 
								activeRevisions.getTravelTimesRev(),
								stopPathId, 
								travelTimeSegmentsLength,
								travelTimesMsec, 
								stopTimeMsec,
								-1,  // daysOfWeekOverride
								HowSet.SCHED,
								trip);
				travelTimes.add(travelTimesForStopPath);
			}
			
			// For next iteration in for loop
			previousStopPathWithScheduleTimeIndex = 
					stopPathWithScheduleTimeIndex;
			previousScheduleTime = scheduleTime;
		} /* End of for loop for each stop path */

		// Return the results
		return travelTimes;
	}

	/**
	 * Sees if the travel times for the trip from the database are an adequate
	 * match for the trip pattern such that existing travel times can be reused.
	 * They are an adequate match if the path IDs are the same
	 * 
	 * @param trip
	 *            The trip current processing travel times for
	 * @param ttForTripFromDb
	 *            Travel times obtained for trip from the database
	 * @return true if the ttForTripFromDb passed in can be used for the trip
	 */
	private boolean	stopPathsMatch(Trip trip, 
			TravelTimesForTrip ttForTripFromDb) {
		// Convenience variable
		TripPattern tripPattern = trip.getTripPattern();
		
		// See if stopPaths match. If they don't then return false.
		// First check if trip patterns have same number of stops.
		if (tripPattern.getStopPaths().size() != 
				ttForTripFromDb.numberOfStopPaths()) {
			logger.warn("In ScheduleBasedTravelTimesProcessor.adequateMatch(), "
					+ "for tripId={} tripPatternId={} has {} stops while "
					+ "already existing travel times created for tripId={} are "
					+ "for {} stops even though it is associated with the same "
					+ "trip pattern. Therefore the old travel times cannot be "
					+ "reused for this trip. "
					+ "NOTE tripPattern={} "
					+ "NOTE ttForTripFromDb={}", 
					trip.getId(), 
					tripPattern.getId(), 
					tripPattern.getStopPaths().size(),
					ttForTripFromDb.getTripCreatedForId(), 
					ttForTripFromDb.numberOfStopPaths(),
					tripPattern,
					ttForTripFromDb); 
			return false;
		}
		// Make sure the path IDs match
		for (int i=0; i<tripPattern.getStopPaths().size(); ++i) {
			String pathIdFromTripPattern = 
					tripPattern.getStopPathId(i);
			String pathIdFromTravelTimes =
					ttForTripFromDb.getTravelTimesForStopPaths()
							.get(i).getStopPathId();
			if (!pathIdFromTripPattern.equals(pathIdFromTravelTimes)) {
				logger.error("In ScheduleBasedTravelTimesProcessor."
						+ "adequateMatch(), for tripId={} using "
						+ "tripPatternId={} has different stopPaths than "
						+ "for another tripId={} even though it is associated "
						+ "with the same trip pattern. pathIdFromTripPattern={} "
						+ "but pathIdFromTravelTimes={}",
						trip.getId(), 
						tripPattern.getId(), 
						ttForTripFromDb.getTripCreatedForId(),
						pathIdFromTripPattern, pathIdFromTravelTimes); 
				return false;
			}
		}
		
		// Didn't find problem so the new trip being created matches the 
		// travel time data in the db
		return true;
	}
	
	/**
	 * Given list of travel times for the trip pattern looks to see one is for
	 * the trip specified. If so, the travel times from the database for that
	 * trip.
	 * 
	 * @param trip
	 *            which trip trying to get travel times for
	 * @param ttForTripFromDbList
	 *            list of travel times from the db for the trip pattern. Can be
	 *            null.
	 * @param scheduleBasedOk
	 *            set to true if OK to use schedule based travel times
	 * @return travel times for the trip from the database, or null if no such
	 *         travel times available
	 */
	private TravelTimesForTrip travelTimesForTripFromDb(Trip trip,
			List<TravelTimesForTrip> ttForTripFromDbList,
			boolean scheduleBasedOk) {
		// If no travel times from db then we don't have a match
		if (ttForTripFromDbList == null)
			return null;
		
		// Determine if already have non-schedule based data in database 
		// for the trip ID because that would be the preferable match.
		for (TravelTimesForTrip ttForTripFromDb : ttForTripFromDbList) {
			if (ttForTripFromDb.getTripCreatedForId().equals(trip.getId())) {
				if (stopPathsMatch(trip, ttForTripFromDb)
						&& (scheduleBasedOk || !ttForTripFromDb
								.purelyScheduleBased())) {
					// Found good match of travel times from db
					logger.debug("Found exact travel time match for " + 
							"tripId={} from database.",	trip.getId());
					return ttForTripFromDb;
				}
			}
		}

		// Never found a match
		return null;
	}
	
	/**
	 * Given list of travel times for the trip pattern looks to see one is for
	 * the trip pattern specified. If so, the travel times from the database for
	 * that trip pattern are used.
	 * 
	 * @param trip
	 *            which trip trying to get travel times for
	 * @param ttForTripFromDbList
	 *            list of travel times from the db for the trip pattern. Can be
	 *            null.
	 * @param scheduleBasedOk
	 *            set to true if OK to use schedule based travel times
	 * @return travel times for the trip from the database, or null if no such
	 *         travel times available
	 */
	private TravelTimesForTrip travelTimesForTripPatternFromDb(Trip trip,
			List<TravelTimesForTrip> ttForTripFromDbList,
			boolean scheduleBasedOk) {
		// If no travel times from db then we don't have a match
		if (ttForTripFromDbList == null)
			return null;
		
		// Go through list and look for suitable match
		for (TravelTimesForTrip ttForTripFromDb : ttForTripFromDbList) {
			// If suitable travel times already in db then use them
			if (stopPathsMatch(trip, ttForTripFromDb)
					&& (scheduleBasedOk || !ttForTripFromDb
							.purelyScheduleBased())) {
				// Found good match of travel times from db
				logger.debug("Found adequate travel time match for " + 
						"tripId={} which is for tripPatternId={} so " +
						"will use the old one created for tripId={}",
						trip.getId(), trip.getTripPattern().getId(),
						ttForTripFromDb.getTripCreatedForId());
				return ttForTripFromDb;
			}
		}
		
		// Never found a match
		return null;
	}
	
	/**
	 * Creates new schedule based travel times for the trip. Also adds the
	 * schedule based travel times to travelTimesFromDbMap so that it can
	 * possibly be used by other trips. For when historic non-schedule based
	 * travel times are not available from the database.
	 * 
	 * @param trip
	 *            which trip trying to get travel times for
	 * @param gtfsData
	 *            for getting schedule info
	 * @param ttForTripFromDbList
	 *            list of travel times from the db for the trip pattern. Can be
	 *            null.
	 * @param travelTimesFromDbMap
	 *            so can add newly created schedule based travel times so can
	 *            also be used by other trips
	 * @return schedule based travel times
	 */
	private TravelTimesForTrip scheduleBasedTravelTimes(Trip trip,
			GtfsData gtfsData, List<TravelTimesForTrip> ttForTripFromDbList,
			Map<String, List<TravelTimesForTrip>> travelTimesFromDbMap) {
		logger.debug("There was not an adequate travel time match for "
				+ "tripId={} which is for tripPatternId={} so will "
				+ "use newly created schedule based one.",
		trip.getId(), trip.getTripPattern().getId());

		// Determine the schedule based travel times
		TravelTimesForTrip scheduleBasedTravelTimes =
				determineTravelTimesBasedOnSchedule(trip, gtfsData);

		// If list of travel times doesn't exist for this trip pattern yet
		// then create it
		if (ttForTripFromDbList == null) {
			ttForTripFromDbList = new ArrayList<TravelTimesForTrip>();
			travelTimesFromDbMap.put(trip.getTripPattern().getId(),
					ttForTripFromDbList);
		}
		ttForTripFromDbList.add(scheduleBasedTravelTimes);

		return scheduleBasedTravelTimes;
	}

	/**
	 * Determines if schedule from gtfsData is close enough to the one from
	 * ttForTripFromDb. If so then true is returned. This method is for trying
	 * to reuse schedule based travel times.
	 * 
	 * @param trip
	 *            which trip trying to get travel times for
	 * @param ttForTripFromDb
	 *            schedule based travel times from db that checking to see if
	 *            close enough. If null then returns false since no schedule
	 *            to be close to.
	 * @param gtfsData
	 *            for getting schedule info
	 * @return true if schedule from gtfsData is close enough to the one from
	 *         ttForTripFromDb
	 */
	private boolean scheduleCloseEnough(Trip trip,
			TravelTimesForTrip ttForTripFromDb, GtfsData gtfsData) {
		// If no travel times from database then return false
		if (ttForTripFromDb == null)
			return false;
		
		// Determine schedule based travel times
		TravelTimesForTrip scheduleBasedTravelTimes =
				determineTravelTimesBasedOnSchedule(trip, gtfsData);

		// Travel times from db are based on schedule. See if schedule time is 
		// close to the same. If it is, then can use these times
		// Determine the travel times for this trip based on the GTFS
		// schedule times. Then can compare with travel times in database.
		int newTravelTimeMsec = 0;
		int dbTravelTimeMsec = 0;
		for (int i=0; i<scheduleBasedTravelTimes.numberOfStopPaths(); ++i) {
			TravelTimesForStopPath newElement = 
					scheduleBasedTravelTimes.getTravelTimesForStopPath(i);
			TravelTimesForStopPath oldElement = 
					ttForTripFromDb.getTravelTimesForStopPath(i);
			newTravelTimeMsec += newElement.getStopPathTravelTimeMsec(); 
			dbTravelTimeMsec += oldElement.getStopPathTravelTimeMsec();
			// If travel time to this stop differ by more than a minute
			// then the travel times are not an adequate match.
			if (Math.abs(dbTravelTimeMsec - newTravelTimeMsec) >= 60 * Time.MS_PER_SEC) {
				logger.debug("While looking for already existing usable travel " + 
						"times for tripId={} for tripPatternId={} found that the " +
						"travel times differed by more than 60 seconds for stop " +
						"path index {}, which is for pathId={} for the old tripId={}", 
						trip.getId(), trip.getTripPattern().getId(), i, 
						newElement.getStopPathId(),
						ttForTripFromDb.getTripCreatedForId());
				return false;
			}
		}

		// schedule is close enough
		return true;
	}
	
	/**
	 * Goes through every trip and and associates schedule based travel times
	 * with trip if don't have GPS data for it.
	 * 
	 * @param gtfsData
	 * @param travelTimesFromDbMap
	 *            Map keyed by tripPatternId of Lists of TripPatterns
	 * @throws HibernateException
	 */
	private void processTrips(GtfsData gtfsData, 
			Map<String, List<TravelTimesForTrip>> travelTimesFromDbMap) {
		// For trip read from GTFS data..
		for (Trip trip : gtfsData.getTrips()) {
			TripPattern tripPattern = trip.getTripPattern();
			
			logger.debug("Processing travel times for tripId={} which " + 
					"is for tripPatternId={} for routeId={}.", 
					trip.getId(), tripPattern.getId(), trip.getRouteId());

			// For determining if can use existing TravelTimesForTrip from 
			// database. Get list of all travel times for the same trip pattern
			// so that can find which one is best.
			List<TravelTimesForTrip> ttForTripFromDbList = 
					travelTimesFromDbMap.get(tripPattern.getId());			

			// See if have historic non-schedule based travel times from db for 
			// the trip
			TravelTimesForTrip travelTimesToUse =
					travelTimesForTripFromDb(trip, ttForTripFromDbList, false);

			// If didn't find travel times for trip see if have non-schedule 
			// based ones for same trip pattern.
			if (travelTimesToUse == null) {
				travelTimesToUse =
						travelTimesForTripPatternFromDb(trip,
								ttForTripFromDbList, false);
			}
			
			// If didn't find non-schedule based travel times from db then see 
			// if can reuse existing schedule based travel time
			if (travelTimesToUse == null) {
				TravelTimesForTrip ttForTripFromDb =
						travelTimesForTripFromDb(trip, ttForTripFromDbList, true);
				if (scheduleCloseEnough(trip, ttForTripFromDb, gtfsData))
					travelTimesToUse = ttForTripFromDb;
			}

			// If didn't find non-schedule based travel times from db then see 
			// if can reuse existing schedule based travel time from another trip
			if (travelTimesToUse == null) {
				TravelTimesForTrip ttForTripFromDb =
						travelTimesForTripPatternFromDb(trip, ttForTripFromDbList, true);
				if (scheduleCloseEnough(trip, ttForTripFromDb, gtfsData))
					travelTimesToUse = ttForTripFromDb;
			}
			
			// If didn't have any usable travel times from db then create new
			// schedule based travel times for this trip
			if (travelTimesToUse == null) {
				travelTimesToUse =
						scheduleBasedTravelTimes(trip, gtfsData,
								ttForTripFromDbList, travelTimesFromDbMap);
			}
			
			// Set the resulting TravelTimesForTrip for the Trip so travel times 
			// will be stored as part of the trip when the trip is stored to 
			// the database.
			trip.setTravelTimes(travelTimesToUse);
		}			
	}
	
	/**
	 * For determining how many travel times have been processed into the
	 * travelTimesFromDbMap. There is a travel time for each stop path for each
	 * trip.
	 * 
	 * @param travelTimesFromDbMap
	 *            Keyed on tripPatternId, contains list of TravelTimesForTrip
	 *            for each tripPatternId
	 * @return Total number of travel times (one per stop path) already
	 *         processed)
	 */
	private static int numberOfTravelTimes(
			Map<String, List<TravelTimesForTrip>> travelTimesFromDbMap) {
		int count = 0;
		for (List<TravelTimesForTrip> travelTimes : travelTimesFromDbMap.values()) {
			count += travelTimes.size();
		}
		return count;
	}
	
	/**
	 * For trips where travel times not set in database via GPS data
	 * default travel times are created by looking at the schedule
	 * times and interpolating.
	 * 
	 * @param gtfsData
	 */
	public void process(Session session, GtfsData gtfsData) {
		if (!gtfsData.isTripsReadIn()) {
			logger.error("tripsMap not yet read in by GtfsData before " + 
					"ScheduleBasedTravelTimesProcessor.process() called. Software " +
					"needs to be fixed");
			System.exit(-1);
		}
		if (!gtfsData.isStopTimesReadIn()) {
			logger.error("GTFS stop times not read in before " + 
					"ScheduleBasedTravelTimesProcessor.process() called. " + 
					"Software neds to be fixed.");
			System.exit(-1);
		}
		
		// For logging how long things take
		IntervalTimer timer = new IntervalTimer();

		// Let user know what is going on
		logger.info("Processing travel time data...");

		// Read existing data from db and put into travelTimesFromDbMap member.
		// Map is keyed on trip pattern ID.
		Map<String, List<TravelTimesForTrip>> travelTimesFromDbMap = 
				TravelTimesForTrip.getTravelTimesForTrips(session, 
						originalTravelTimesRev);

		int originalNumberTravelTimes =
				numberOfTravelTimes(travelTimesFromDbMap);
		
		// Do the low-level processing
		processTrips(gtfsData, travelTimesFromDbMap);
							
		// Let user know what is going on
		logger.info("Finished processing travel time data. " + 
				"Number of travel times read from db={}. " + 
				"Total number of travel times needed to cover each trip={}. " + 
				"Total number of trips={}.  Took {} msec.", 
				originalNumberTravelTimes,
				numberOfTravelTimes(travelTimesFromDbMap),
				gtfsData.getTrips().size(),
				timer.elapsedMsec());
	}

}
