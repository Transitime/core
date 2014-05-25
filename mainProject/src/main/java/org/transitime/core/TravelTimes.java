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
package org.transitime.core;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.configData.CoreConfig;
import org.transitime.db.structs.ScheduleTime;
import org.transitime.db.structs.TravelTimesForStopPath;
import org.transitime.utils.Time;

/**
 * Singleton class that contains methods for determining how long a vehicle is
 * expected to take to get from one point to another on the assignment. Heavily
 * used both for doing temporal matching and for generating predictions.
 * 
 * @author SkiBu Smith
 */
public class TravelTimes {
	
	// Singleton class
	private static TravelTimes singleton = new TravelTimes();
	
	private static final Logger logger = 
			LoggerFactory.getLogger(TravelTimes.class);

	/********************** Member Functions **************************/

	/**
	 * Constructor declared private because singleton class
	 */
	private TravelTimes() {}
	
	/**
	 * Returns singleton MatchProcessor
	 * @return
	 */
	public static TravelTimes getInstance() {
		return singleton;
	}
	
	/**
	 * Determines travel time as the crows flies to cover the distance. Intended
	 * to be used for seeing if have enough time to deadhead to a breakpoint.
	 * Just a very crude approximation since the distance is as the crow flies
	 * but vehicle will actually be traveling on various roads. And we don't
	 * know how fast it can really travel on those roads. But doing the best we
	 * can here.
	 * 
	 * Uses a two step approach since sometimes vehicles can travel fast on
	 * highways if they are far away but will likely travel slower when closer.
	 * 
	 * @param distance
	 *            As the crow flies
	 * @return Msec would take to travel the distance
	 */
	public static int travelTimeAsTheCrowFlies(double distance) {
		// Convenience variables
		float SHORT_DISTANCE = 
				CoreConfig.getDeadheadingShortVersusLongDistance();
		float SHORT_DISTANCE_SPEED = 
				CoreConfig.getShortDistanceDeadheadingSpeed();
		float LONG_DISTANCE_SPEED = 
				CoreConfig.getLongDistanceDeadheadingSpeed();
		
		double shortDistanceTravel = 0.0;
		double longDistanceTravel = 0.0;
		if (distance > SHORT_DISTANCE) {
			shortDistanceTravel = SHORT_DISTANCE;
			longDistanceTravel = distance - SHORT_DISTANCE;
		} else {
			shortDistanceTravel = distance;
			longDistanceTravel = 0.0;
		}
		
		// Determine roughly how long it might take to travel the distance
		double veryRoughTravelTimeSecs = 
				shortDistanceTravel/SHORT_DISTANCE_SPEED +
				longDistanceTravel/LONG_DISTANCE_SPEED;
		
		// Return time to travel the distance
		return (int) (veryRoughTravelTimeSecs * Time.MS_PER_SEC);
	}
	

	/**
	 * If at a waitStop then the travel time should be adjusted to this amount.
	 * 
	 * @param timeOfDaySecs
	 * @param travelTimeMsec
	 * @param indices
	 * @return
	 */
	private static int adjustTravelTimeForWaitStop(int timeOfDaySecs, 
			int travelTimeMsec, Indices indices) {
		ScheduleTime scheduleTime = indices.getScheduleTime();
		if (scheduleTime != null) {
			Integer scheduledDepartureTime = scheduleTime.getDepartureTime();
			if (scheduledDepartureTime != null) {
				// If affected by waitStop...
				if (timeOfDaySecs * 1000 + travelTimeMsec < 
						scheduledDepartureTime * 1000) {
					// Take waitStop time into account
					int updatedTravelTimeMsec = 
							(scheduledDepartureTime - timeOfDaySecs) * 1000;
					return updatedTravelTimeMsec;
				}
			}
		}			
		// Not affected by waitStop so return the original travel time
		return travelTimeMsec;
	}
	
	/**
	 * Takes the currentTime passed in and determines if vehicle will be
	 * departing at the waitStop time instead of currentTime. If so, then the
	 * layover time is returned. Does not take break time or expected stop time
	 * into account.
	 * 
	 * @param timeWithoutWaitStop
	 *            The time vehicle is expected to depart if there was no
	 *            waitStop
	 * @param indices
	 *            Describes which stop
	 * @return
	 */
	public static long adjustTimeAccordingToSchedule(
			final long timeWithoutWaitStop, Indices indices) {
		ScheduleTime scheduleTime = indices.getScheduleTime();
		if (scheduleTime != null) {
			Integer scheduledDepartureTimeSecs = 
					scheduleTime.getDepartureTime();
			if (scheduledDepartureTimeSecs != null) {
				// If affected by waitStop...
				Time time = Core.getInstance().getTime();
				int departureTimeWithoutLayoverSecs = 
						time.getSecondsIntoDay(timeWithoutWaitStop);
				if (departureTimeWithoutLayoverSecs < scheduledDepartureTimeSecs) {
					// Take waitStop time into account
					long scheduleEpochTime = 
							time.getEpochTime(scheduledDepartureTimeSecs, 
									new Date(timeWithoutWaitStop));
					return scheduleEpochTime;
				}
			}
		}

		// Not affected by waitStop so return the original time
		return timeWithoutWaitStop;
	}

	/**
	 * This class is so that travelTimeIndexForPartialPath() can return more
	 * than a single piece of information.
	 */
	private static class TimeTravelInfo {
		private TimeTravelInfo(int indexOfPartialSegment,
				double fractionCompleted) {
			this.indexOfPartialSegment = indexOfPartialSegment;
			this.fractionCompleted = fractionCompleted;
		}
		
		private int indexOfPartialSegment;
		private double fractionCompleted;
	}
	
	/**
	 * Returns information on the travel time segment that the match corresponds
	 * to.
	 * 
	 * @param match
	 * @return TimeTravelInfo containing information on the match
	 */
	private TimeTravelInfo travelTimeInfoForPartialPath(SpatialMatch match) {
		// Get the travel times for this stop path
		TravelTimesForStopPath travelTimesForStopPath = 
				match.getTrip().getTravelTimesForStopPath(match.getStopPathIndex());
		
		// Use a segment length of the StopPath length divided by number of
		// travel times. This way even if the paths change a bit can still
		// use the old travel times.
		double stopPathLength =
				match.getStopPath().getLength();
		int numTravelTimeSegmentsInPath = 
				travelTimesForStopPath.getNumberTravelTimeSegments();
		double travelTimeSegmentLength = 
				stopPathLength / numTravelTimeSegmentsInPath;

		double distanceAlongStopPath = 
				match.getDistanceAlongStopPath();
		int indexOfPartialTimeTravelSegment =
				(int) (distanceAlongStopPath / travelTimeSegmentLength);
		
		// Make sure that don't go beyond the ends of the travel time segments
		// array. This could in theory happen if at the end of the path and
		// there was rounding error.
		if (indexOfPartialTimeTravelSegment >= numTravelTimeSegmentsInPath) {
			indexOfPartialTimeTravelSegment = numTravelTimeSegmentsInPath-1;
		}

		// Determine how far into the travel time segment the match is
		double distanceCompletedOfTimeTravelSegment = distanceAlongStopPath - 
				indexOfPartialTimeTravelSegment*travelTimeSegmentLength;
		double fractionOfTravelSegmentCompleted = 
				distanceCompletedOfTimeTravelSegment/travelTimeSegmentLength;
		
		// Return results
		TimeTravelInfo timeTravelInfo = 
				new TimeTravelInfo(indexOfPartialTimeTravelSegment, 
						fractionOfTravelSegmentCompleted);		
		return timeTravelInfo;
	}
	
	/**
	 * Returns the time vehicle is expected to take to travel from the
	 * spatialMatch to the stop at the end of the current stop path. Does not
	 * include the stop time for the stop at the end of the stop path.
	 * 
	 * @param match
	 *            The starting point
	 * @return Expected travel time in msec
	 */
	public int expectedTravelTimeFromMatchToEndOfStopPath(SpatialMatch match) {
		// Get the travel times for this stop path
		TravelTimesForStopPath travelTimesForStopPath = 
				match.getTrip().getTravelTimesForStopPath(match.getStopPathIndex());

		// Determine how match corresponds to travel time segments
		TimeTravelInfo timeTravelInfo = travelTimeInfoForPartialPath(match);

		// Determine travel time to go from the match to the end of the 
		// current travel time segment.
		int travelTimeForPartialSegment = travelTimesForStopPath
				.getTravelTimeSegmentMsec(timeTravelInfo.indexOfPartialSegment);
		int travelTimeRemainingInPartialSegment = (int) (travelTimeForPartialSegment * 
				(1-timeTravelInfo.fractionCompleted));
				
		// Sum up the travel times for the remaining full travel time segments 
		// in the path.
		int travelTimeMsec = travelTimeRemainingInPartialSegment;
		for (int i=timeTravelInfo.indexOfPartialSegment+1; 
				i<travelTimesForStopPath.getNumberTravelTimeSegments(); 
				++i) {
			travelTimeMsec += travelTimesForStopPath.getTravelTimeSegmentMsec(i);
		}
		return travelTimeMsec; 
	}
	
	/**
	 * Returns the time vehicle is expected to take to travel from the beginning
	 * of the stop path to the spatialMatch. Only includes travel time, not time
	 * expected to be at the stop.
	 * 
	 * @param match
	 *            The end point
	 * @return Expected travel time in msec
	 */
	public int expectedTravelTimeFromBeginningOfStopPathToMatch(SpatialMatch match) {
		// Get the travel times for this stop path
		TravelTimesForStopPath travelTimesForStopPath = 
				match.getTrip().getTravelTimesForStopPath(match.getStopPathIndex());

		// Determine how match corresponds to travel time segments
		TimeTravelInfo timeTravelInfo = travelTimeInfoForPartialPath(match);
		
		// Sum up the travel times for the full travel time segments up to but
		// not including the segment that the match is on.
		int travelTimeMsec = 0;
		for (int i=0; i<timeTravelInfo.indexOfPartialSegment; ++i) {
			travelTimeMsec += travelTimesForStopPath.getTravelTimeSegmentMsec(i);
		}

		// Determine travel time to go from the the beginning of the 
		// current travel time segment to the match.
		int travelTimeForPartialSegment = 
				travelTimesForStopPath.getTravelTimeSegmentMsec(
						timeTravelInfo.indexOfPartialSegment);
		int travelTimeInPartialSegmentToMatch = (int) (travelTimeForPartialSegment * 
				timeTravelInfo.fractionCompleted);

		travelTimeMsec += travelTimeInPartialSegmentToMatch;		
		return travelTimeMsec;
	}
	
	/**
	 * Returns travel time for the path specified by the indices parameter. Does
	 * not include the stop time.
	 * 
	 * @param indices
	 *            Specifies which path
	 * @return Expected travel time in msec
	 */
	public int expectedTravelTimeForStopPath(Indices indices) {
		TravelTimesForStopPath travelTimesForPath = 
				indices.getTrip().getTravelTimesForStopPath(indices.getStopPathIndex());
		return travelTimesForPath.getStopPathTravelTimeMsec();
	}

	/**
	 * Returns how long vehicle is expected to stop for the stop at the end of
	 * the path specified by the indices parameter.
	 * 
	 * @param indices
	 *            Which path to return stop time for.
	 * @return Stop time in msec
	 */
	public int expectedStopTimeForStopPath(Indices indices) {
		TravelTimesForStopPath travelTimesForPath = 
				indices.getTrip().getTravelTimesForStopPath(indices.getStopPathIndex());
		return travelTimesForPath.getStopTimeMsec();
	}
	
	/**
	 * Determines expected travel time in msec between the two matches based on
	 * the travel times from the database.
	 * 
	 * @param vehicleId
	 *            for logging messages
	 * @param timeOfDaySecs
	 *            so can take layovers into account
	 * @param match1
	 * @param match2
	 * @return travel time in msec between matches. Returns 0 if match2 is
	 *         before match1
	 */
	public int expectedTravelTimeBetweenMatches(String vehicleId,
			int timeOfDaySecs, SpatialMatch match1, SpatialMatch match2) {
		logger.debug("For vehicleId={} determining travel time between " +
				"following two matches: \n" +
				"  match1={}\n" +
				"  match2={}", 
				vehicleId, match1, match2);
		
		// Determine the indices for both match1 and match2 so that can see if
		// need to add stop/layover time.
		Indices indices = match1.getIndices();
		Indices endIndices = match2.getIndices();

		// If the indices are not increasing then can simply return a travel 
		// time of 0msec. This shouldn't happen so log an error.
		if (!indices.lessThan(endIndices)) {
			if (!indices.equals(endIndices)
					|| match1.getDistanceAlongSegment() > 
						match2.getDistanceAlongSegment()) {
				logger.error("For vehicleId={} match1AfterStop is after " +
						"match2BeforeStop so returning travel time of 0. " +
						"match1AfterStop={}, match2BeforeStop={}",
						vehicleId, match1, match2);
				return 0;
			}
		}
		
		// Start with travel time from beginning location to end of first 
		// stop path.
		int travelTimeMsec = 
				expectedTravelTimeFromMatchToEndOfStopPath(match1);
		logger.debug("For vehicleId={} travel time for first partial " +
				"stop path is {} msec. {}", 
				vehicleId, travelTimeMsec, match1);
		
		// For special case where both matches are on the same stop path 
		// need to subtract out the travel time for the travel time 
		// stop path since adding the begin time and end time together. This 
		// is a bit odd but draw it out to understand what is going on.
		if (indices.equalStopPath(endIndices)) {
			int pathTravelTime = expectedTravelTimeForStopPath(indices);
			travelTimeMsec -= pathTravelTime; 
			logger.debug("For vehicleId={} both matches are on same stop " +
					"path so subtracted travel time for stop path of {} " +
					"msec so travel time is now {} msec",
					vehicleId, pathTravelTime, travelTimeMsec);
		}
		
		// If will be going to next stop path then need to include the stop
		// time for the first stop path.
		if (!indices.equalStopPath(endIndices)) {
			int stopTimeMsec = indices.getStopTimeForPath();
			travelTimeMsec += stopTimeMsec;
			logger.debug("For vehicleId={} adding stop time={} msec so " +
					"travel time now is {} msec for stop at indices={}",
					vehicleId, stopTimeMsec, travelTimeMsec, indices);
		}
		
		// TODO make sure this is tested 
		// If layover then take that into account. For such a case the travel 
		// time will then be the scheduled departure time minus the start time.
		if (!indices.equals(endIndices) && indices.isWaitStop()) {
			travelTimeMsec = adjustTravelTimeForWaitStop(timeOfDaySecs,
					travelTimeMsec, indices);
		}
		
		// Already dealt with first partial stop path so increment indices
		indices.incrementStopPath();
		
		// For all segments between the begin and end ones...
		while (indices.isEarlierStopPathThan(endIndices)) {
			int stopPathTravelTime = expectedTravelTimeForStopPath(indices);
			travelTimeMsec += stopPathTravelTime;
			logger.debug("For vehicleId={} adding stop path travel time={} " +
					"msec so travel time now is {} for {}", 
					vehicleId, stopPathTravelTime, travelTimeMsec, indices);

			int stopTimeMsec = indices.getStopTimeForPath();
			travelTimeMsec += stopTimeMsec;
			logger.debug("For vehicleId={} adding stop time={} msec so " +
					"travel time now is {} msec for {}",
					vehicleId, stopTimeMsec, travelTimeMsec, indices);
			
			// TODO make sure this is tested 
			// If layover then take that into account. For such a case the 
			// travel time will then be the scheduled departure time minus 
			// the start time.
			if (indices.isLayover()) {
				travelTimeMsec = 
						adjustTravelTimeForWaitStop(timeOfDaySecs, 
								travelTimeMsec, indices);
			}
			
			// Increment for next time through while loop
			indices.incrementStopPath();
		}
		
		// Add travel time for last partial segment
		int travelTimeForPartialLastStopPath =
				expectedTravelTimeFromBeginningOfStopPathToMatch(match2);
		travelTimeMsec += travelTimeForPartialLastStopPath;
		logger.debug("For vehicleId={} added travel time for last " +
				"partial stop path of {} msec so now travel time is {} msec", 
				vehicleId, travelTimeForPartialLastStopPath, travelTimeMsec);
		
		// Return the results
		logger.debug("For vehicleId={} returning total travel time={} msec. {}" , 
				vehicleId, travelTimeMsec, match2);
		return travelTimeMsec;
	}
	
	/**
	 * Determines expected travel time in msec between the two matches based on
	 * the travel times from the database. Same as other
	 * travelTimeBetweenMatches() method but uses Date to determine seconds into
	 * day. Therefore it can be simpler to use since AvlReports and such have a
	 * Date. Returns travel time in msec.
	 * 
	 * @param vehicleId
	 *            for logging messages
	 * @param time
	 * @param match1
	 * @param match2
	 * @return travel time in msec between matches. Returns 0 if match2 is
	 *         before match1
	 */
	public int expectedTravelTimeBetweenMatches(String vehicleId,
			Date time, SpatialMatch match1, SpatialMatch match2) {
		int timeOfDaySecs = Core.getInstance().getTime().getSecondsIntoDay(time);
		return expectedTravelTimeBetweenMatches(vehicleId, timeOfDaySecs, match1, match2);
	}
	
}
