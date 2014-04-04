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
import org.transitime.db.structs.Arrival;
import org.transitime.db.structs.ArrivalDeparture;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.Block;
import org.transitime.db.structs.Departure;
import org.transitime.utils.Time;

/**
 * For determining Arrival/Departure times based on a new GPS report and
 * corresponding TemporalMatch.
 * 
 * @author SkiBu Smith
 * 
 */
public class ArrivalDepartureGeneratorDefaultImpl 
	implements ArrivalDepartureGenerator {

	// If vehicle just became predictable as indicated by no previous match 
	// then still want to determine arrival/departure times for earlier 
	// stops so that won't miss recording data for them them. But only want to
	// go so far. Otherwise could be generating fake arrival/departure times
	// when vehicle did not actually traverse that stop.
	private static final int MAX_STOPS_WHEN_NO_PREVIOUS_MATCH = 6;
	
	private static final Logger logger = 
			LoggerFactory.getLogger(ArrivalDepartureGeneratorDefaultImpl.class);

	/********************** Member Functions **************************/

	/**
	 * Returns whether going from oldMatch to newMatch traverses so many stops
	 * during the elapsed AVL time that it isn't reasonable to think that the
	 * vehicle did so. If too many stops would be traversed then logs an error
	 * message indicating that this isn't reasonable. When true is returned then
	 * shouldn't generate arrival/departure times. This situation can happen
	 * when a vehicle does a short turn, there are problems with the AVL data
	 * such as the heading is not accurate causing the vehicle to match to the
	 * wrong direction, or if there is a software problem that causes an
	 * improper match.
	 * 
	 * @param oldMatch
	 * @param newMatch
	 * @param previousAvlReport
	 * @param avlReport
	 * @return
	 */
	private boolean tooManyStopsTraversed(SpatialMatch oldMatch,
			SpatialMatch newMatch, AvlReport previousAvlReport,
			AvlReport avlReport) {
		// If there is no old match then we are fine
		if (oldMatch == null)
			return false;
		
		// Determine how much time elapsed
		long avlTimeDeltaMsec = avlReport.getTime() - previousAvlReport.getTime();
		
		// Determine number of stops traversed
		Indices indices = oldMatch.getIndices();
		Indices newMatchIndices = newMatch.getIndices();
		int stopsTraversedCnt=0;
		while (!indices.pastEndOfBlock() && indices.earlierStopPathThan(newMatchIndices)) {
			indices.incrementStopPath();
			++stopsTraversedCnt;
		}
		
		// If traversing more than a stop every 15 seconds then there must be 
		// a problem. Also use a minimum of 4 stops to make sure that don't get
		// problems due to problematic GPS data causing issues
		if (stopsTraversedCnt >= 4 && 
				stopsTraversedCnt > avlTimeDeltaMsec/15*Time.MS_PER_SEC) {
			logger.error("vehicleId={} traversed {} stops in {} seconds " +
					"which seems like too many stops for that amount of time. " +
					"oldMatch={} , newMatch={}, previousAvlReport={}, " +
					"avlReport={}",
					avlReport.getVehicleId(), stopsTraversedCnt,
					avlTimeDeltaMsec / Time.MS_PER_SEC, oldMatch, newMatch,
					previousAvlReport, avlReport);
			return true;
		} else
			return false;
	}
	
	/**
	 * Determines if need to determine arrival/departure times due to 
	 * vehicle having traversed a stop. 
	 * 
	 * @param oldMatch
	 * @param newMatch
	 * @return
	 */
	private boolean shouldProcessArrivedOrDepartedStops(SpatialMatch oldMatch, 
			SpatialMatch newMatch) {
		// If there is no old match at all then we likely finally got a 
		// AVL report after vehicle had left terminal. Still want to 
		// determine arrival/departure times for the first stops of the 
		// block assignment. And this makes sure don't get a NPE in the
		// next statements.
		if (oldMatch == null)
			return true;

		VehicleAtStopInfo oldStopInfo = oldMatch.getAtStop();
		VehicleAtStopInfo newStopInfo = newMatch.getAtStop();
		
		if (oldStopInfo != null && newStopInfo != null) {
			// Vehicle at stop for both old and new. Determine if they
			// are different stops. If different then return true.
			return oldStopInfo.getTripIndex() != newStopInfo.getTripIndex() ||
					oldStopInfo.getStopPathIndex() != newStopInfo.getStopPathIndex();
		} else if (oldStopInfo == null || newStopInfo == null) {
			// Just one but not both of the vehicle stop infos is null which means 
			// they are different. Therefore must have arrived or departed stops.
			return true;
		} else {
			// See if matches indicate that now on a new path
			return oldMatch.getTripIndex() != newMatch.getTripIndex() ||
					oldMatch.getStopPathIndex() != newMatch.getStopPathIndex();
		}
	}
	
	/**
	 * Writes out departure time to database
	 * 
	 * @param vehicleState
	 * @param departureTime
	 * @param block
	 * @param tripIndex
	 * @param stopPathIndex
	 */
	private ArrivalDeparture createDepartureTime(VehicleState vehicleState,
			long departureTime, Block block, int tripIndex, int pathIndex) {		
		// Store the departure in the database via the db logger
		Departure departure = new Departure(vehicleState.getVehicleId(), 
				new Date(departureTime),
				vehicleState.getAvlReport().getDate(),
				block,
				tripIndex,
				pathIndex);
		logger.debug("Creating departure: {}", departure);
		return departure;
	}

	/**
	 * Writes out arrival time to database
	 * 
	 * @param vehicleState
	 * @param arrivalTime
	 * @param block
	 * @param tripIndex
	 * @param stopPathIndex
	 */
	private ArrivalDeparture createArrivalTime(VehicleState vehicleState,
			long arrivalTime, Block block, int tripIndex, int pathIndex) {
		// Store the arrival in the database via the db logger
		Arrival arrival = new Arrival(vehicleState.getVehicleId(), 
				new Date(arrivalTime),
				vehicleState.getAvlReport().getDate(),
				block,
				tripIndex,
				pathIndex);
		logger.debug("Creating arrival: {}", arrival);
		return arrival;
	}

	/**
	 * For when there is a new match but not an old match. This means that 
	 * cannot interpolate the arrival/departure times. Instead need to
	 * look backwards and use travel and stop times to determine the
	 * arrival/departure times.
	 * <p>
	 * Only does this if on the first trip of a block. The thought is
	 * that if vehicle becomes predictable for subsequent trips that
	 * vehicle might have actually started service mid-block, meaning that
	 * it didn't traverse the earlier stops and so shouldn't fake
	 * arrival/departure times for the earlier stops since there is a
	 * good chance they never happened. 
	 * 
	 * @param vehicleState
	 * @return List of ArrivalDepartures created
	 */
	private List<ArrivalDeparture> estimateArrivalsDeparturesWithoutPreviousMatch(
			VehicleState vehicleState) {
		// Create list to be returned
		List<ArrivalDeparture> arrivalDepartures = 
				new ArrayList<ArrivalDeparture>();
		
		// Couple of convenience variables
		SpatialMatch newMatch = vehicleState.getMatch();
		String vehicleId = vehicleState.getVehicleId();
		
		if (newMatch.getTripIndex() == 0 && 
				newMatch.getStopPathIndex() > 0 &&
				newMatch.getStopPathIndex() < MAX_STOPS_WHEN_NO_PREVIOUS_MATCH) {
			// Couple more convenience variables
			Date avlReportTime = vehicleState.getAvlReport().getDate();
			Block block = newMatch.getBlock();
			final int tripIndex = 0;
			
			// Determine departure time for first stop of trip
			SpatialMatch beginningOfTrip = 
					new SpatialMatch(vehicleId, block, tripIndex, 0, 0, 0.0, 0.0);
			long travelTimeFromFirstStopToMatch =
					TravelTimes.getInstance().expectedTravelTimeBetweenMatches(
							vehicleId, avlReportTime, beginningOfTrip, 
					newMatch);
			long departureTime = 
					avlReportTime.getTime() - travelTimeFromFirstStopToMatch;
			
			// Create departure time for first stop of trip
			arrivalDepartures.add(createDepartureTime(vehicleState,
					departureTime, block, tripIndex, 0)); // stopPathIndex
			
			// Go through remaining stops to determine arrival/departure times
			for (int pathIndex = 1; pathIndex < newMatch.getStopPathIndex(); ++pathIndex) {
				long arrivalTime = departureTime
						+ block.getStopPathTravelTime(tripIndex, pathIndex);
				arrivalDepartures.add(createArrivalTime(vehicleState,
						arrivalTime, block, tripIndex, pathIndex));

				int stopTime = block.getPathStopTime(tripIndex, pathIndex);
				departureTime = arrivalTime + stopTime;
				arrivalDepartures.add(createDepartureTime(vehicleState,
						departureTime, block, tripIndex, pathIndex));
			}
			
			// Need to add final arrival time if newMatch is at a stop
			VehicleAtStopInfo atStopInfo = newMatch.getAtStop();
			if (atStopInfo != null) {
				arrivalDepartures.add(createArrivalTime(vehicleState,
						avlReportTime.getTime(), block, tripIndex,
						newMatch.getStopPathIndex()));
			}
		} else {
			logger.debug("For vehicleId={} no old match but the new " +
					"match is too far along so not determining " +
					"arrival/departure times without previous match.", 
					vehicleId);
		}
		
		// Return list of created arrival/departures
		return arrivalDepartures;
	}
	
	/**
	 * Handles the case where the old match indicates that vehicle has
	 * departed a stop. Determines the appropriate departure time.
	 * 
	 * @param vehicleState
	 *            For obtaining match and AVL info
	 * @param arrivalDepartures
	 *            New departure time is added to this array
	 * @return The time to be used as the beginTime for determining
	 *         arrivals/departures for intermediate stops. Will be the time of
	 *         the new AVL report if vehicle is not at a stop. If it is at a
	 *         stop then it will be the expected departure time at that stop.
	 */
	private long handleVehicleDepartingStop(VehicleState vehicleState,
			List<ArrivalDeparture> arrivalDepartures) {	
		// If vehicle wasn't departing a stop then simply return the
		// previous AVL time as the beginTime.
		SpatialMatch oldMatch = vehicleState.getPreviousMatch();
		VehicleAtStopInfo oldVehicleAtStopInfo = oldMatch.getAtStop();
		AvlReport previousAvlReport = vehicleState.getPreviousAvlReport();
		if (oldVehicleAtStopInfo == null)
			return previousAvlReport.getTime();
		
		// Vehicle departed previous stop...
		logger.debug("vehicleId={} was at stop {}  previous AVL report " +
				"and departed so determining departure time", 
				vehicleState.getVehicleId(), oldVehicleAtStopInfo);

		// Use match right at the stop. This way we are including the
		// time it takes to get from the actual stop to the new match.]
		SpatialMatch matchAtStop = oldMatch.getMatchBeforeStop();
		
		// Determine departure info for the old stop
		SpatialMatch newMatch = vehicleState.getMatch();
		int travelTimeMsec = 
				TravelTimes.getInstance().expectedTravelTimeBetweenMatches(
						vehicleState.getVehicleId(), 
						previousAvlReport.getDate(), 
						matchAtStop, newMatch);
		AvlReport avlReport = vehicleState.getAvlReport();
		long departureTime = avlReport.getTime() - travelTimeMsec;
		
		// Make sure departure time is after the previous arrival time
		Date arrivalTime = vehicleState.getPreviousArrivalTime();
		if (arrivalTime != null && departureTime <= arrivalTime.getTime()) {
			// Adjust the departure time so that it is greater than the
			// arrival time. Don't want them even to be equal so that
			// can be sure they will be listed in expected order in
			// management reports.
			if (logger.isDebugEnabled()) {
				logger.debug("vehicleId={} determined departure time was " +
						"{} which is less than or equal to the previous " +
						"arrival time of {}. Therefore the departure time " +
						"adjusted to {}.",
						vehicleState.getVehicleId(), 
						Time.dateTimeStrMsec(departureTime),
						Time.dateTimeStrMsec(arrivalTime),
						Time.dateTimeStrMsec(arrivalTime.getTime() + 1));
			}
			
			departureTime = arrivalTime.getTime() + 1;
		}
		
		// Write out the departure time
		arrivalDepartures.add(createDepartureTime(vehicleState,
				departureTime, oldVehicleAtStopInfo.getBlock(),
				oldVehicleAtStopInfo.getTripIndex(),
				oldVehicleAtStopInfo.getStopPathIndex()));
		
		// The new beginTime to be used to determine arrival/departure
		// times at intermediate stops
		return departureTime;
	}
	
	/**
	 * Handles the case where the new match indicates that vehicle has
	 * arrived at a stop. Determines the appropriate arrival time.
	 * 
	 * @param vehicleState
	 *            For obtaining match and AVL info
	 * @param beginTime
	 *            The time of the previous AVL report or the departure time if
	 *            vehicle previously was at a stop.
	 * @param arrivalDepartures
	 *            New arrival time is added to this array
	 * @return The time to be used as the endTime for determining
	 *         arrivals/departures for intermediate stops. Will be the time of
	 *         the new AVL report if vehicle is not at a stop. If it is at a
	 *         stop then it will be the expected arrival time at that stop.
	 */
	private long handleVehicleArrivingAtStop(VehicleState vehicleState,
			long beginTime, 
			List<ArrivalDeparture> arrivalDepartures) {		
		// If vehicle hasn't arrived at a stop then simply return the
		// AVL time as the endTime.
		SpatialMatch newMatch = vehicleState.getMatch();
		VehicleAtStopInfo newVehicleAtStopInfo = newMatch.getAtStop();
		AvlReport avlReport = vehicleState.getAvlReport();
		if (newVehicleAtStopInfo == null)
			return avlReport.getTime();
		
		// Vehicle has arrived at a stop...
		logger.debug("vehicleId={} arrived at stop {} with new AVL " +
				"report so determining arrival time", 
				vehicleState.getVehicleId(), newVehicleAtStopInfo);

		// Use match right at the stop. This way we are including the
		// time it takes to get from the new match to the actual
		// stop and not just to some distance before the stop.
		SpatialMatch matchAtStop = newMatch.getMatchAfterStop();
		
		// Determine arrival info for the new stop
		SpatialMatch oldMatch = vehicleState.getPreviousMatch();
		int travelTimeMsec = 
				TravelTimes.getInstance().expectedTravelTimeBetweenMatches(
						vehicleState.getVehicleId(), avlReport.getDate(), 
						oldMatch, matchAtStop);
		// At first it appears that should use the time of the previous AVL
		// report plus the travel time. But since vehicle might have just 
		// departed the previous stop should use that departure time instead.
		// By using beginTime we are using the correct value.
		long arrivalTime = beginTime + travelTimeMsec;
		
		// Write out the arrival time
		arrivalDepartures.add(createArrivalTime(vehicleState, arrivalTime, 
				newVehicleAtStopInfo.getBlock(), 
				newVehicleAtStopInfo.getTripIndex(), 
				newVehicleAtStopInfo.getStopPathIndex()));

		// Remember the arrival time so that can make sure that subsequent
		// departure time is after the arrival time. 
		vehicleState.setPreviousArrivalTime(new Date(arrivalTime));
		
		// The new endTime to be used to determine arrival/departure
		// times at intermediate stops
		return arrivalTime;
	}
	
	/**
	 * Determine arrival/departure info for in between stops.
	 * 
	 * @param vehicleState
	 *            For obtaining match and AVL info
	 * @param beginTime
	 *            Time of previous AVL report or the departure time if vehicle
	 *            was at a stop
	 * @param endTime
	 *            Time of the new AVL report or the arrival time if vehicle
	 *            arrived at a stop
	 * @param arrivalDepartures
	 *            New arrival and departures times are added to this array
	 */
	private void handleIntermediateStops(VehicleState vehicleState,
			long beginTime, long endTime,
			List<ArrivalDeparture> arrivalDepartures) {
		// Convenience variables
		String vehicleId = vehicleState.getVehicleId();
		SpatialMatch oldMatch = vehicleState.getPreviousMatch();
		SpatialMatch newMatch = vehicleState.getMatch();
		Date previousAvlDate = vehicleState.getPreviousAvlReport().getDate();
		Date avlDate = vehicleState.getAvlReport().getDate();
		
		// Determine how fast vehicle was traveling compared to what is  
		// expected. Then can use proportional travel and stop times to  
		// determine the arrival and departure times. Note that this part of 
		// the code doesn't need to be very efficient because usually will get
		// frequent enough AVL reports such that there will be at most only
		// a single stop that is crossed. Therefore it is OK to determine
		// travel times for same segments over and over again.
		int totalExpectedTravelTimeMsec = TravelTimes.getInstance()
				.expectedTravelTimeBetweenMatches(vehicleId, previousAvlDate,
						oldMatch, newMatch);
		long elapsedAvlTime = endTime - beginTime;
		
		// speedRatio is how much time vehicle took to travel compared to the 
		// expected travel time. A value greater than 1.0 means that vehicle
		// is taking longer than expected and the expected travel times should
		// therefore be increased accordingly. There are situations where 
		// totalExpectedTravelTimeMsec can be zero or really small, such as 
		// when using schedule based travel times and the schedule doesn't 
		// provide enough time to even account for the 10 or seconds expected
		// for wait time stops. Need to make sure that don't divide by zero
		// for this situation, where expected travel time is 5 msec or less,
		// use a speedRatio of 1.0. 
		double speedRatio;
		if (totalExpectedTravelTimeMsec > 5)
			speedRatio = (double) elapsedAvlTime / totalExpectedTravelTimeMsec;
		else
			speedRatio = 1.0;
		
		// To determine which path use the stopInfo if available since that 
		// way won't use the wrong path index if the vehicle is matching to
		// just beyond the stop.
		VehicleAtStopInfo oldVehicleAtStopInfo = oldMatch.getAtStop();
		Indices indices = 
				oldVehicleAtStopInfo != null ? 
						oldVehicleAtStopInfo.clone().incrementStopPath() : oldMatch.getIndices();
		
		VehicleAtStopInfo newVehicleAtStopInfo = newMatch.getAtStop();				
		Indices endIndices =
				newVehicleAtStopInfo != null ?
						newVehicleAtStopInfo.clone() : newMatch.getIndices();
						
		// Determine time to first stop
		SpatialMatch matchAtNextStop = oldMatch.getMatchAtNextStop();	
		long travelTimeToFirstStop = TravelTimes.getInstance()
				.expectedTravelTimeBetweenMatches(vehicleId,
						avlDate, oldMatch, matchAtNextStop);
		long time = beginTime + Math.round(travelTimeToFirstStop * speedRatio);

		// Go through each stop between the old match and the new match and
		// determine the arrival and departure times...
		logger.debug("For vehicleId={} determining if it traversed stops " +
				"in between the new and the old AVL report...", 
				vehicleId);
		Block block = indices.getBlock();
		while (indices.earlierStopPathThan(endIndices)) {
			// Determine arrival time for current stop
			long arrivalTime = time;
			arrivalDepartures.add(createArrivalTime(vehicleState, 
					arrivalTime, 
					newMatch.getBlock(), 
					indices.getTripIndex(), 
					indices.getStopPathIndex()));
			
			// Determine departure time for current stop
			int stopTime = block.getPathStopTime(indices.getTripIndex(), 
					indices.getStopPathIndex());
			long departureTime = time + Math.round(stopTime * speedRatio);
			arrivalDepartures.add(createDepartureTime(vehicleState, 
					departureTime, 
					newMatch.getBlock(), 
					indices.getTripIndex(), 
					indices.getStopPathIndex()));
			
			// Determine travel time to next time for next time through 
			// the while loop
			indices.incrementStopPath();
			int pathTravelTime = block.getStopPathTravelTime(indices.getTripIndex(), 
					indices.getStopPathIndex());
			time = departureTime + Math.round(pathTravelTime * speedRatio);
		}
		
		logger.debug("For vehicleId={} done determining if it traversed stops " +
				"in between the new and the old AVL report.", 
				vehicleId);
	}
	
	/**
	 * Processes updated vehicleState to generate associated arrival and
	 * departure times. Looks at both the previous match and the current
	 * match to determine which stops need to generate times for.
	 * 
	 * @param vehicleState
	 * @return List of generated ArrivalDeparture times
	 */
	@Override
	public List<ArrivalDeparture> generate(VehicleState vehicleState) {
		// Create list to be returned
		List<ArrivalDeparture> arrivalDepartures = 
				new ArrayList<ArrivalDeparture>();

		// Make sure vehicle state is OK
		if (!vehicleState.isPredictable()) {
			logger.error("Vehicle was not predictable when trying to process " +
					"arrival/departure times. {}", vehicleState);
			return arrivalDepartures;
		}
		SpatialMatch newMatch = vehicleState.getMatch();
		if (newMatch == null) {
			logger.error("Vehicle was not matched when trying to process " +
					"arrival/departure times. {}", vehicleState);
			return arrivalDepartures;			
		}
		
		// If no old match then can determine the stops traversed between the
		// old match and the new one. But this will frequently happen because
		// sometimes won't get matches until vehicle has gone past the initial
		// stop of the block due to not getting assignment right away or some
		// kind of AVL issue. For this situation still want to estimate the
		// arrival/departure times for the previous stops. 
		SpatialMatch oldMatch = vehicleState.getPreviousMatch();
		if (oldMatch == null) {
			logger.debug("For vehicleId={} there was no previous match " +
					"so seeing if can generate arrivals/departures for " +
					"beginning of block", vehicleState.getVehicleId());
			// Done since don't have an oldMatch. 
			return estimateArrivalsDeparturesWithoutPreviousMatch(vehicleState);
		}

		// If too many stops were traversed given the AVL time then there must
		// be something wrong so return
		AvlReport previousAvlReport = vehicleState.getPreviousAvlReport();
		AvlReport avlReport = vehicleState.getAvlReport();
		if (tooManyStopsTraversed(oldMatch, newMatch, previousAvlReport, avlReport))
			return arrivalDepartures;
		
		// If no stops were traversed simply return
		if (!shouldProcessArrivedOrDepartedStops(oldMatch, newMatch)) 
			return arrivalDepartures;

		// Process the arrival/departure times since traversed at least one stop
		logger.debug("vehicleId={} traversed at least one stop so " +
				"determining arrival/departure times. oldMatch={} newMatch={}", 
				vehicleState.getVehicleId(), oldMatch, newMatch);
			
		// If vehicle was at a stop with the old match and has now departed
		// then determine the departure time. Update the beginTime
		// accordingly since it should be the departure time instead of
		// the time of previous AVL report.
		long beginTime = handleVehicleDepartingStop(vehicleState,
				arrivalDepartures);
		
		// If vehicle arrived at a stop then determine the arrival
		// time. Update the endTime accordingly since should use the time
		// that vehicle actually arrived instead of the AVL time.
		long endTime = handleVehicleArrivingAtStop(vehicleState, beginTime, 
				arrivalDepartures);
					
		// Determine arrival/departure info for in between stops.
		handleIntermediateStops(vehicleState, beginTime, endTime,
				arrivalDepartures);

		// Return the list of generated arrivals/departures
		return arrivalDepartures;
	}
}
