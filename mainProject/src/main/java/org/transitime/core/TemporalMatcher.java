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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.transitime.applications.Core;
import org.transitime.configData.CoreConfig;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.Block;
import org.transitime.db.structs.Location;
import org.transitime.db.structs.Trip;
import org.transitime.utils.Geo;
import org.transitime.utils.Time;

/**
 * Singleton class that does the temporal matching to determine where 
 * an AVL report matches to the block assignment. Looks at spatial
 * matches and determines which one makes the most sense temporally.
 * 
 * @author SkiBu Smith
 */
public class TemporalMatcher {

	// Singleton class
	private static TemporalMatcher singleton = new TemporalMatcher();
	
	private static final Logger logger = 
			LoggerFactory.getLogger(TemporalMatcher.class);

	/********************** Member Functions **************************/

	/**
	 * Declaring constructor as private since singleton class
	 */
	private TemporalMatcher() {}
	
	/**
	 * Returns the TemporalMatcher singleton
	 * @return
	 */
	public static TemporalMatcher getInstance() {
		return singleton;
	}
	
	/**
	 * If the trip is active at the secsInDayForAvlReport then it is
	 * added to the tripsThatMatchTime list.
	 * 
	 * @param vehicleId for logging messages
	 * @param secsInDayForAvlReport
	 * @param trip
	 * @param tripsThatMatchTime
	 * @return
	 */
	private static boolean addTripIfActive(String vehicleId, 
			int secsInDayForAvlReport, Trip trip, List<Trip> tripsThatMatchTime) {
		int startTime = trip.getStartTime();
		int endTime = trip.getEndTime();

		if (secsInDayForAvlReport > startTime - CoreConfig.getAllowableEarlyForLayoverSeconds() &&
				secsInDayForAvlReport < endTime + CoreConfig.getAllowableLateSeconds()) {
			tripsThatMatchTime.add(trip);

			if (logger.isDebugEnabled()) {
				logger.debug("Determined that for blockId={} that a trip is " +
						"considered to be active for AVL time. " + 
						"TripId={}, tripIndex={} AVLTime={}, " + 
						"startTime={}, endTime={}, " + 
						"allowableEarlyForLayover={} secs, allowableLate={} secs, " +
						"vehicleId={}",
						trip.getBlock().getId(),
						trip.getId(), 
						trip.getBlock().getTripIndex(trip.getId()),
						Time.timeOfDayStr(secsInDayForAvlReport),
						Time.timeOfDayStr(trip.getStartTime()),
						Time.timeOfDayStr(trip.getEndTime()),
						CoreConfig.getAllowableEarlyForLayoverSeconds(),
						CoreConfig.getAllowableLateSeconds(),
						vehicleId);
			}
			
			return true;
		}
		
		// Not a match so return false
		return false;
	}
	
	/**
	 * For the specified block determines which trips are currently active.
	 * Should work even for trips that start before midnight or go till after
	 * midnight.
	 * 
	 * @param avlReport
	 * @param block
	 * @return
	 */
	public List<Trip> getTripsCurrentlyActive(
			AvlReport avlReport, Block block) {
		// Set for returning results
		List<Trip> tripsThatMatchTime = new ArrayList<Trip>();
		
		// Convenience variable
		String vehicleId = avlReport.getVehicleId();
		
		// Go through trips and find ones 
		List<Trip> trips = block.getTrips();
		for (Trip trip : trips) {
			
			// If time of avlReport is within reasonable time of the trip
			// time then this trip should be returned.  
			int secsInDayForAvlReport = 
					Core.getInstance().getTime().getSecondsIntoDay(avlReport.getDate());

			// If the trip is active then add it to the list of active trips 
			boolean tripIsActive = 
					addTripIfActive(vehicleId, secsInDayForAvlReport, trip, tripsThatMatchTime);
			
			// if trip wasn't active might be because trip actually starts before
			// midnight so should check for that special case.
			if (!tripIsActive)
				tripIsActive = 
					addTripIfActive(vehicleId, 
							secsInDayForAvlReport - Time.SEC_PER_DAY, 
							trip, tripsThatMatchTime);
			
			// if trip still wasn't active might be because trip goes past
			// midnight so should check for that special case.
			if (!tripIsActive)
				tripIsActive = 
					addTripIfActive(vehicleId, 
							secsInDayForAvlReport + Time.SEC_PER_DAY, trip, 
							tripsThatMatchTime);
		}

		// Returns results
		return tripsThatMatchTime;
	}
	
	/**
	 * For the spatial match, determines how far off in time the vehicle is from
	 * what is expected based on the start time of the trip plus the travel time
	 * from the beginning of the trip. This can be used to determine which
	 * spatial match has the best temporal match.
	 * 
	 * @param for logging messages
	 * @param date
	 * @param spatialMatch
	 * @return
	 */
	private static TemporalDifference determineHowFarOffScheduledTime(
			String vehicleId, Date date, SpatialMatch spatialMatch) {
		// Determine how long it should take to travel along trip to the match. 
		// Can add this time to the trip scheduled start time to determine
		// when the vehicle is predicted to be at the match. 
		SpatialMatch beginningOfTrip = new SpatialMatch(vehicleId,
				spatialMatch.getBlock(), 
				spatialMatch.getTripIndex(), 
				0,    //  stopPathIndex 
				0,    // segmentIndex 
				0.0,  // distanceToSegment
				0.0); // distanceAlongSegment
		int tripStartTimeSecs = spatialMatch.getTrip().getStartTime();
		int travelTimeForCurrentTrip = 
				TravelTimes.getInstance().expectedTravelTimeBetweenMatches(vehicleId, 
						tripStartTimeSecs, 
						beginningOfTrip, spatialMatch);
		
		int msecIntoDayVehicleExpectedToBeAtMatch = 
				tripStartTimeSecs * 1000 + travelTimeForCurrentTrip;
				
		// Need to convert everything to Date or to secsInDay so can do comparison
		int avlTimeIntoDayMsec = Core.getInstance().getTime().getMsecsIntoDay(date);		
		int earlyMsec = msecIntoDayVehicleExpectedToBeAtMatch - avlTimeIntoDayMsec;

		// Also check if 24 hours early late so that can work even for when 
		// trip starts before midnight or goes after midnight.
		TemporalDifference deltaFromSchedule = 
				new TemporalDifference(earlyMsec);
		TemporalDifference beforeMidnightExpectedTimeDelta = 
				new TemporalDifference(earlyMsec - Time.MS_PER_DAY);
		TemporalDifference afterMidnightExpectedTimeDelta = 
				new TemporalDifference(earlyMsec + Time.MS_PER_DAY);
		if (beforeMidnightExpectedTimeDelta.betterThan(deltaFromSchedule))
			deltaFromSchedule = beforeMidnightExpectedTimeDelta;
		if (afterMidnightExpectedTimeDelta.betterThan(deltaFromSchedule))
			deltaFromSchedule = afterMidnightExpectedTimeDelta;
			
		// Return adherence but return null if adherence is beyond limits
		if (deltaFromSchedule.isWithinBounds()) {
			logger.debug("For vehicleId={} determineHowFarOffScheduledTime() "
					+ "returning expectedTimeDelta={} for {}", 
					vehicleId, deltaFromSchedule, spatialMatch);
			return deltaFromSchedule;
		} else {
			logger.debug("For vehicleId={} in determineHowFarOffScheduledTime() "
					+ "expectedTimeDelta={} is not within bounds of "
					+ "getAllowableEarlySeconds()={} and "
					+ "getAllowableLateSeconds()={} so returning null for {}",
					vehicleId, deltaFromSchedule, 
					CoreConfig.getAllowableEarlySeconds(), 
					CoreConfig.getAllowableLateSeconds(),
					spatialMatch);
			return null;
		}
	}
	
	/**
	 * For the spatial matches passed in determines the one that temporally
	 * makes the most sense. Compares the time elapsed between AVL reports and
	 * compares that to the expected travel time between the previous and the
	 * current spatial match. The spatial match which corresponds most to the
	 * expected travel time is returned as the best temporal match.
	 * 
	 * @param vehicleState
	 * @param spatialMatches
	 * @return The best temporal match for the spatial matches passed in
	 */
	public TemporalMatch getBestTemporalMatch(VehicleState vehicleState,
			List<SpatialMatch> spatialMatches) {
		// Convenience variables		
		SpatialMatch previousMatch = vehicleState.getLastMatch();
		Date previousAvlTime =
				vehicleState.getPreviousToLastAvlReport().getDate();
		Date avlTime = vehicleState.getLastAvlReport().getDate();
		long avlTimeDifferenceMsec = avlTime.getTime() - previousAvlTime.getTime();

		// Find best temporal match of the spatial matches
		TemporalMatch bestTemporalMatch = null;
		for (SpatialMatch spatialMatch : spatialMatches) {
			logger.debug("Examining spatial match {}", spatialMatch);
			
			// Determine how long would expect it to take to get from previous
			// match to the new match.
			int expectedTravelTimeMsec = 
					TravelTimes.getInstance().expectedTravelTimeBetweenMatches(
							vehicleState.getVehicleId(), 
							previousAvlTime, 
							previousMatch, spatialMatch);
			
			// Determine how far off the expected travel time. If match is
			// for a layover and the vehicle had enough time to make it from
			// the previous match to the layover then it is a special
			// case. Yes, this part is quite complicated but tried to show
			// clearly in comments what all the important factors are.
			TemporalDifference differenceFromExpectedTime;
			if (spatialMatch.atLayover() && 
					avlTimeDifferenceMsec > expectedTravelTimeMsec) {
				// If already was at this layover then the expected travel
				// time will be 0. For this situation need to determine
				// if still within the layover time, meaning that the 
				// time difference is 0, or if the vehicle is instead
				// late since the layover time has already passed.
				if (expectedTravelTimeMsec == 0) {
					// Determine layover departure time. If it is before that 
					// time then it is still at layover and the temporal
					// difference should be 0. But if it is after the departure
					// time then the vehicle is behind where it should be.
					int departureTimeSecs = 
							spatialMatch.getScheduledLayoverTime();
					long scheduledDepartureTime =	
							Core.getInstance().getTime().getEpochTime(
									departureTimeSecs, avlTime);
					if (avlTime.getTime() > scheduledDepartureTime) {
						// Vehicle should have already left so it is late
						differenceFromExpectedTime = 
								new TemporalDifference(scheduledDepartureTime -
										avlTime.getTime());
						logger.debug("For vehicleId={} match at layover but " +
								"currently after the layover time so indicating " +
								"that the vehicle is behind where it should be. " +
								"avlTime={}, scheduledDepTime={}",
								vehicleState.getVehicleId(), avlTime, 
								new Date(scheduledDepartureTime));

					} else {
						// Still at the layover so use time difference of 0
						differenceFromExpectedTime = new TemporalDifference(0);
						logger.debug("For vehicleId={} match is for layover but " +
								"this match is at layover but had enough time " +
								"to get there so temporalDifference=0. ",
								vehicleState.getVehicleId());
					}
				} else {
					// Wasn't already at layover. But since had enough time to
					// get to the layover the time difference is 0.
					differenceFromExpectedTime = new TemporalDifference(0);
					logger.debug("For vehicleId={} wasn't at layover but " +
							"this match is at layover yet had enough time " +
							"to get there so temporalDifference=0. " +
							"avlTimeDifferenceMsec={}, " +
							"expectedTravelTimeMsec={}",
							vehicleState.getVehicleId(), avlTimeDifferenceMsec, 
							expectedTravelTimeMsec);
				}
			} else {
				// Not the special layover case so determine how far off travel
				// time is from expected time the normal way
				differenceFromExpectedTime = new TemporalDifference(
						expectedTravelTimeMsec - avlTimeDifferenceMsec);
				logger.debug("For vehicleId={} not at layover so returning " +
						"normal temporal difference. " +
						"avlTimeDifferenceMsec={}, " +
						"expectedTravelTimeMsec={}",
						vehicleState.getVehicleId(), avlTimeDifferenceMsec, 
						expectedTravelTimeMsec);
			}
			
			logger.debug("For vehicleId={} temporal match " +
					"differenceFromExpectedTime={}",
					vehicleState.getVehicleId(), differenceFromExpectedTime);
			
			// If this temporal match is better than the previous best one
			// then remember it.
			if (differenceFromExpectedTime != null && 
					(bestTemporalMatch == null ||
					differenceFromExpectedTime.betterThanOrEqualTo(
							bestTemporalMatch.getTemporalDifference()))) {
				bestTemporalMatch = new TemporalMatch(spatialMatch, 
								differenceFromExpectedTime);
			} else {
				// This temporal match was not better. If already found a 
				// temporal match then can stop looking since it will only
				// get worse for the remaining spatial matches.
				if (bestTemporalMatch != null) {
					logger.debug("For vehicleId={} temporal match is getting " +
							"worse which means that don't need to look at " +
							"additional spatial matches since it would only " +
							"continue to get worse.",
							vehicleState.getVehicleId());
					break;
				}
			}
		}
		
		logger.debug("For vehicleId={} best temporal match was found to be {}",
				vehicleState.getVehicleId(), bestTemporalMatch);
		
		// Return the best temporal match (if there is one)
		return bestTemporalMatch;
	}
	
	/**
	 * From the list of spatial matches passed in, determines which
	 * one has the best valid temporal match.
	 * 
	 * @param spatialMatches The spatial matches to examine
	 * @return The match passed in that best matches temporally where vehicle 
	 * should be. Returns null if no adequate temporal match.
	 */
	public TemporalMatch getBestTemporalMatchComparedToSchedule(
			AvlReport avlReport, List<SpatialMatch> spatialMatches) {
		TemporalDifference bestDifferenceFromExpectedTime = null;
		SpatialMatch bestSpatialMatch= null;
		
		for (SpatialMatch spatialMatch : spatialMatches) {			
			// If not at layover then determine temporal match based on 
			// how long it should take vehicle to travel from the beginning
			// of the trip to the spatial match.
			TemporalDifference differenceFromExpectedTime = 
					determineHowFarOffScheduledTime(avlReport.getVehicleId(), 
							avlReport.getDate(), spatialMatch);			
			
			// If this is the best differenceFromExpectedTime so far then use it.
			if (differenceFromExpectedTime != null && 
					(bestDifferenceFromExpectedTime == null || 
					 differenceFromExpectedTime.betterThan(bestDifferenceFromExpectedTime))) {				
				bestDifferenceFromExpectedTime = differenceFromExpectedTime;
				bestSpatialMatch = spatialMatch;
				logger.debug("For vehicleId={} differenceFromExpectedTime={} " +
						"is best yet so remembering such",
						avlReport.getVehicleId(), differenceFromExpectedTime);
			}
		}
		
		// If found a suitable and best match, return it
		if (bestSpatialMatch != null && bestDifferenceFromExpectedTime != null) {
			TemporalMatch result = 
					new TemporalMatch(bestSpatialMatch, 
							bestDifferenceFromExpectedTime);
			logger.debug("For vehicleId={} getBestTemporalMatch() returning " +
					"temporalMatch={}", 
					avlReport.getVehicleId(), result);
			return result;
		} else {
			logger.debug("For vehicleId={} getBestTemporalMatch() returning " +
					"null because no adequate temporal match was found", 
					avlReport.getVehicleId());
			return null;
		}
	}
	
	/**
	 * Determines if can make it to beginning of trip in time.
	 * 
	 * @param avlReport
	 * @param trip
	 * @return
	 */
	private static boolean canDeadheadToBeginningOfTripInTime(
			AvlReport avlReport, Trip trip) {
		long tripStartTimeMsecs = trip.getStartTime() * 1000;
		long msecsIntoDay = 
				Core.getInstance().getTime().getMsecsIntoDay(avlReport.getDate(), 
						tripStartTimeMsecs);
		// If AVL report is from before the start time of the trip
		// then see if have enough time to travel there.
		if (msecsIntoDay < tripStartTimeMsecs) {
			// How much time available to get to layover
			long availableTimeMsec = tripStartTimeMsecs - msecsIntoDay;
			
			// How far as the crow flies to the layover
			Location tripStartLoc = trip.getStopPath(0).getEndOfPathLocation();
			double distance = avlReport.getLocation().distance(tripStartLoc);
			int crowFliesTimeMsec = TravelTimes.travelTimeAsTheCrowFlies(distance);
			boolean canDeadhead = crowFliesTimeMsec < availableTimeMsec;
			trip.getBlock().getTripIndex(trip.getId());
			logger.debug("For vehicleId={} determining if can deadhead to "
					+ "beginning of tripIndex={} tripId={}. msecsIntoDay={} "
					+ "tripStartTimeMsecs={} distance={} availableTimeMsec={} "
					+ "crowFliesTimeMsec={} canDeadhead={}",
					avlReport.getVehicleId(), trip.getIndex(), trip.getId(),
					Time.timeOfDayStr(msecsIntoDay / 1000),
					Time.timeOfDayStr(tripStartTimeMsecs / 1000),
					Geo.distanceFormat(distance), availableTimeMsec, 
					crowFliesTimeMsec, canDeadhead);
			return canDeadhead;
		}
		
		// Can't make it in time 
		logger.debug("For vehicleId={} tripIndex={} tripId={} is not in "
				+ "future so can't deadhead to it. msecsIntoDay={} "
				+ "tripStartTimeMsecs={}", avlReport.getVehicleId(),
				trip.getIndex(), trip.getId(),
				Time.timeOfDayStr(msecsIntoDay / 1000),
				Time.timeOfDayStr(tripStartTimeMsecs / 1000));
		return false;
	}
	
	/**
	 * Returns the next trip that the vehicle could get to in time. Returns null
	 * if can't get there.
	 * 
	 * Don't want to look through all trips for a block because then might match
	 * to first block the next day, which would be a problem. Instead need to
	 * only look at trips that are considered to be valid. Therefore should only
	 * pass in upcoming trips as the potentialTrips parameter.
	 * 
	 * @param avlReport
	 * @param potentialTrips
	 *            specifies which trips to examine
	 * @return
	 */
	public Trip matchToLayoverEvenIfOffRoute(
			AvlReport avlReport, List<Trip> potentialTrips) {
		// Determine upcoming layover
		for (Trip trip : potentialTrips) {
			if (canDeadheadToBeginningOfTripInTime(avlReport, trip))
				return trip;
		}
		
		// Didn't find a layover that it could get to in time so return null
		return null;
	}
	
}
