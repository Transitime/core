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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.db.structs.ScheduleTime;
import org.transitime.db.structs.StopPath;
import org.transitime.db.structs.Trip;

/**
 * For determining the real-time schedule adherence for a predictable vehicle.
 *
 * @author SkiBu Smith
 *
 */
public class RealTimeSchedAdhProcessor {

	private static final Logger logger = 
			LoggerFactory.getLogger(RealTimeSchedAdhProcessor.class);

	/********************** Member Functions **************************/

	/**
	 * Determines the current schedule adherence for the vehicle. If vehicle at
	 * a stop with a scheduled departure time then the schedule adherence for
	 * that stop is returned. Otherwise will look at when the vehicle is
	 * expected to be at the next stop with a scheduled time and provide the
	 * expected schedule adherence for that stop. Doing it this way is useful
	 * because it allows the schedule adherence to be updated while the vehicle
	 * is in between stops.
	 * 
	 * @param vehicleState
	 * @return The real-time schedule adherence for the vehicle, or null if
	 *         vehicle is not predictable or there are no upcoming stops with a
	 *         schedule time.
	 */
	public static TemporalDifference generate(VehicleState vehicleState) {
		// If vehicle not matched/predictable then cannot provide schedule
		// adherence
		if (!vehicleState.isPredictable())
			return null;
		
		// Convenience variables
		TemporalMatch match = vehicleState.getMatch();		
		Trip trip = match.getTrip();
		Date avlTime = vehicleState.getAvlReport().getDate();
		String vehicleId = vehicleState.getVehicleId();
		
		// If vehicle at a stop with a scheduled departure time then the 
		// schedule adherence is either 0 because the departure time hasn't 
		// been reached yet or the vehicle is late.
		VehicleAtStopInfo stopInfo = match.getAtStop();
		if (stopInfo != null) {
			ScheduleTime schedTime = stopInfo.getScheduleTime();	
			
			if (schedTime != null && schedTime.getDepartureTime() != null) {
				// Determine the scheduled departure time in epoch time
				long departureEpochTime = Core.getInstance().getTime()
						.getEpochTime(schedTime.getDepartureTime(), avlTime);
				
				// Wait stops are handled specially since if before the 
				// departure time then schedule adherence is 0. The scheduled
				// arrival time doesn't matter.
				if (stopInfo.isWaitStop()) {
					// If departure time hasn't been reached yet...
					if (avlTime.getTime() < departureEpochTime) {
						// Departure time not yet reached so perfectly on time!
						logger.debug("For vehicleId={} vehicle at wait stop " +
								"but haven't reached departure time yet so " +
								"returning 0 as the schedule adherence. " +
								"avlTime={} and scheduled departure time={}",
								vehicleId, avlTime, schedTime);
						return new TemporalDifference(0);
					} else {
						TemporalDifference scheduleAdherence = 
								new TemporalDifference(departureEpochTime - 
										avlTime.getTime());
	
						// Already past departure time so return that vehicle 
						// is late
						logger.debug("For vehicleId={} vehicle at wait stop " +
								"but have reached departure time so returning " +
								"schedule adherence={}. avlTime={} and " +
								"scheduled departure time={}",
								vehicleId, scheduleAdherence, avlTime, 
								schedTime);
						return scheduleAdherence;
					}					
				} else { 
					// Not a wait stop where vehicle is supposed to wait
					// to depart until scheduled time. Therefore simply
					// return difference between scheduled departure
					// time and the AVL time.
					TemporalDifference scheduleAdherence = 
							new TemporalDifference(departureEpochTime - 
									avlTime.getTime());

					// Already past departure time so return that vehicle 
					// is late
					logger.debug("For vehicleId={} vehicle at stop but " +
							"have reached departure time so returning " +
							"schedule adherence={}. avlTime={} and " +
							"scheduled time={}",
							vehicleId, scheduleAdherence, avlTime, 
							schedTime);
					return scheduleAdherence;
				}
			}
		}
		
		// Vehicle wasn't at a stop with a schedule time so determine the
		// schedule adherence by looking at when it is expected to arrive
		// at the next stop with a scheduled time.
		
		// Determine next stop with a schedule time (arrival or departure)		
		// If there is no such stop then return null. 
		List<StopPath> stopPaths = 
				match.getTrip().getTripPattern().getStopPaths();
		StopPath stopPathWithScheduleTime = null;
		int stopPathIndex = 0;
		for (int i=match.getStopPathIndex(); i<stopPaths.size(); ++i) {
			StopPath stopPath = stopPaths.get(i);
			ScheduleTime scheduleTime = 
					trip.getScheduleTime(stopPath.getStopId());
			if (scheduleTime.getTime() != null) {
				stopPathWithScheduleTime = stopPath;
				stopPathIndex = i;
				break;
			}
		}
		if (stopPathWithScheduleTime == null) 
			return null;

		// Determine the appropriate match to use for the upcoming stop where
		// there is a schedule time.		
		Indices indicesAtStopWithScheduleTime = new Indices(match.getBlock(),
				match.getTripIndex(), stopPathIndex, 
				stopPathWithScheduleTime.getNumberSegments());
		SpatialMatch matchAtStopWithScheduleTime = new SpatialMatch(match, 
				indicesAtStopWithScheduleTime,
				stopPathWithScheduleTime.getLength());

		// Determine how long it is expected to take for vehicle to get to 
		// that stop
		int travelTimeToStopMsec = TravelTimes.getInstance()
				.expectedTravelTimeBetweenMatches(vehicleId, avlTime,
						match, matchAtStopWithScheduleTime);
		
		// If using departure time then add in expected stop wait time
		ScheduleTime scheduleTime = 
				trip.getScheduleTime(stopPathWithScheduleTime.getStopId());
		if (scheduleTime.getDepartureTime() != null) {
			//TravelTimesForStopPath 
			int stopTime = trip.getTravelTimesForStopPath(stopPathIndex).getStopTimeMsec();
			travelTimeToStopMsec += stopTime;
		}
		
		// Return the schedule adherence
		long expectedTime = avlTime.getTime() + travelTimeToStopMsec;
		long departureEpochTime = Core.getInstance().getTime()
				.getEpochTime(scheduleTime.getTime(), avlTime);
		TemporalDifference scheduleAdherence = 
				new TemporalDifference(departureEpochTime - expectedTime);
		logger.debug("For vehicleId={} vehicle not at stop returning " +
				"schedule adherence={}. avlTime={} and scheduled time={}",
				vehicleId, scheduleAdherence, avlTime, scheduleTime);
		return scheduleAdherence;
	}
}
