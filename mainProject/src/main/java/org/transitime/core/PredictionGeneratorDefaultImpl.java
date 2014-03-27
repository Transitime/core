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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.configData.CoreConfig;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.StopPath;
import org.transitime.db.structs.Trip;
import org.transitime.ipc.data.Prediction;
import org.transitime.utils.Geo;
import org.transitime.utils.Time;

/**
 * When a new match based on AVL data is made for a vehicle the methods in this
 * class are used to generate the corresponding predictions.
 * <p>
 * Layovers are the most complicated part. At a layover a vehicle is expected to
 * leave at the scheduled departure time. But if a vehicle is late then it will
 * stop just for the max of the stop time or the break time.
 * <p>
 * The stop time should be based on historic AVL data but I'm not sure how that
 * can be determined for a layover. Perhaps just need to use a default time and
 * not update it via historic AVL data? 
 * <p>
 * The break time is a value per stop, though it will default to a system wide
 * setting since most of the time won't have specific break time data per stop.
 * <p>
 * Also need to take into account that vehicles frequently don't leave right at
 * the scheduled departure time. The AVL data can be used to determine when they
 * really depart. This could be stored as the stop time for the stop. This
 * overloads the meaning of stop time because then it is used both for
 * determining 1) how late relative to the schedule time a vehicle departs a
 * layover; and 2) how long a vehicle will stop for if vehicle arrives late and
 * the stop time is greater than the break time.
 * 
 * @author SkiBu Smith
 * 
 */
public class PredictionGeneratorDefaultImpl implements PredictionGenerator {

	private static final Logger logger = 
			LoggerFactory.getLogger(PredictionGeneratorDefaultImpl.class);

	/********************** Member Functions **************************/

	/**
	 * Generates prediction for the stop specified by the indices parameter. It
	 * will be an arrival prediction if at the end of the trip or the
	 * useArrivalTimes parameter is set to true, and it is not at a waitStop
	 * (since at waitStop always want a departure prediction. For departures the
	 * prediction time will be the sum of the parameters predictionTime and
	 * timeStoppedMsec.
	 * 
	 * @param avlReport
	 *            So can get information such as vehicleId and AVL time
	 * @param indices
	 *            Describes the stop that should generate prediction for
	 * @param predictionTime
	 *            The expected arrival time at the stop.
	 * @param useArrivalTimes
	 *            For when not at end of trip or at a waitStop we have the
	 *            choice of either generating arrival or departure times. For
	 *            such stops this parameter whether to generate an arrival or a
	 *            departure time for such a stop.
	 * @param affectedByWaitStop
	 *            to indicate if prediction is not as accurate because it
	 *            depends on driver leaving waitStop according to schedule.
	 * @return The generated Prediction
	 */
	private Prediction generatePredictionForStop(AvlReport avlReport,
			Indices indices, long predictionTime, boolean useArrivalTimes, 
			boolean affectedByWaitStop) {
		// Determine additional parameters for the prediction to be generated
		StopPath path = indices.getStopPath();
		String stopId = path.getStopId();
		int stopSequence = path.getStopSequence();
		Trip trip = indices.getTrip();
		int expectedStopTimeMsec =
				TravelTimes.getInstance().expectedStopTimeForStopPath(indices);
		
		// If should generate arrival time...
		if ((indices.atEndOfTrip() || useArrivalTimes) && !indices.isWaitStop()) {
			// Create and return arrival time for this stop
			return new Prediction(avlReport.getVehicleId(), stopId,
					stopSequence, trip, predictionTime, 
					avlReport.getTime(), avlReport.getTimeProcessed(), 
					affectedByWaitStop, avlReport.getDriverId(),
					avlReport.getPassengerCount(), avlReport.getPassengerFullness(),
					true);  // isArrival. True to indicate arrival
		} else {
			// Generate a departure time
			// If at a layover then need to handle specially...
			if (indices.isWaitStop()) {
				logger.debug("For vehicleId={} the original arrival time " +
						"for waitStop stopId={} is {}",
						avlReport.getVehicleId(), path.getStopId(),
						Time.dateTimeStrMsec(predictionTime));
				
				// At layover so need to look at if it has enough time to get 
				// to the waitStop, the scheduled departure time, the expected
				// stop time since vehicles often don't depart on time, and 
				// whether driver getting enough break time.
				long arrivalTime = predictionTime;
				boolean deadheadingSoNoDriverBreak = false;
				
				// Make sure have enough time to get to the waitStop for the
				// departure.
				double distanceToWaitStop = Geo.distance(avlReport.getLocation(), 
						indices.getStopPath().getStopLocation());
				int crowFliesTimeToWaitStop = 
						TravelTimes.travelTimeAsTheCrowFlies(distanceToWaitStop);
				if (avlReport.getTime() + crowFliesTimeToWaitStop > arrivalTime) {
					arrivalTime = avlReport.getTime() + crowFliesTimeToWaitStop;
					deadheadingSoNoDriverBreak = true;
					logger.debug("For vehicleId={} adjusted the arrival time " +
							"for layover stopId={} since crowFliesTimeToLayover={} " +
							"but predictionTime-avlReport.getTime()={}msec. The " +
							"arrival time is now {}",
							avlReport.getVehicleId(), path.getStopId(),
							crowFliesTimeToWaitStop, 
							predictionTime-avlReport.getTime(),
							Time.dateTimeStrMsec(arrivalTime));
				}
				
				// If it is currently before the scheduled departure time then use
				// the schedule time. But if after the scheduled time then use
				// the prediction time.
				long adjustedDeparatureTime = TravelTimes
						.adjustTimeAccordingToSchedule(arrivalTime, indices) + 
						expectedStopTimeMsec;
				if (logger.isDebugEnabled() && adjustedDeparatureTime>arrivalTime)
					logger.debug("For vehicleId={} adjusted departure time for " +
							"layover stopId={} to adjustedDeparatureTime={}", 
							avlReport.getVehicleId(), path.getStopId(), 
							Time.dateTimeStrMsec(adjustedDeparatureTime));
				
				// Make sure there is enough break time for the driver to get
				// their break. But only giving drivers a break if vehicle not
				// limited by deadheading time. Thought is that if deadheading
				// then driver just starting assignment or already got break
				// elsewhere, so won't take a break at this layover.
				if (!deadheadingSoNoDriverBreak &&
						adjustedDeparatureTime < 
							predictionTime + path.getBreakTimeSec()*Time.MS_PER_SEC) {
					adjustedDeparatureTime = predictionTime + path.getBreakTimeSec()*Time.MS_PER_SEC;
					logger.debug("For vehicleId={} adjusted departure time for " +
							"layover stopId={} to adjustedDeparatureTime={} to " +
							"to ensure driver gets break of path.getBreakTimeSec()={}",
							avlReport.getVehicleId(), path.getStopId(),
							Time.dateTimeStrMsec(adjustedDeparatureTime), 
							path.getBreakTimeSec());
				}
								
				// Create and return the departure prediction for this layover.
				// Add in expected stop time since vehicles often don't depart
				// on time.
				return new Prediction(avlReport.getVehicleId(), stopId,
						stopSequence, trip, 
						adjustedDeparatureTime + expectedStopTimeMsec,
						avlReport.getTime(), avlReport.getTimeProcessed(),
						affectedByWaitStop,
						avlReport.getDriverId(), avlReport.getPassengerCount(),
						avlReport.getPassengerFullness(),
						false);  // isArrival. False to indicate departure			
			} else {
				// Create and return the departure prediction for this 
				// non-layover stop
				return new Prediction(avlReport.getVehicleId(), stopId,
						stopSequence, trip, 
						predictionTime + expectedStopTimeMsec,
						avlReport.getTime(), avlReport.getTimeProcessed(),
						affectedByWaitStop,
						avlReport.getDriverId(), avlReport.getPassengerCount(),
						avlReport.getPassengerFullness(),
						false);  // isArrival. False to indicate departure
			}
		}			
	}
		
	/**
	 * Generates the predictions for the vehicle. Updates the vehicle state with
	 * those predictions. Also sends the predictions to the PredictionDataCache.
	 * 
	 * @param vehicleState
	 *            Contains the new match for the vehicle that the predictions
	 *            are to be based on.
	 */
	@Override
	public List<Prediction> generate(VehicleState vehicleState) {	
		// For layovers always use arrival time for end of trip and
		// departure time for anything else. But for non-layover stops
		// can use either arrival or departure times, depending on what
		// the agency wants. Therefore make this configurable.
		boolean useArrivalPredictionsForNormalStops = 
				CoreConfig.getUseArrivalPredictionsForNormalStops();
		
		// If prediction is based on scheduled departure time for a layover
		// then the predictions are likely not as accurate. Therefore this
		// information needs to be part of a prediction.
		boolean affectedByWaitStop = false;
		
		// For storing the new predictions
		List<Prediction> newPredictions = new ArrayList<Prediction>();

		// Get the new match for the vehicle that predictions are to be based on
		TemporalMatch match = vehicleState.getLastMatch();
		Indices indices = match.getIndices();
		
		// Initialize predictionTime to the time of the AVL report.
		AvlReport avlReport = vehicleState.getLastAvlReport();
		long avlTime = avlReport.getTime();
		
		// Get time to end of first path and thereby determine prediction for 
		// first stop.
		TravelTimes travelTimes = TravelTimes.getInstance();
		long predictionTime = 
				avlTime + travelTimes.expectedTravelTimeFromMatchToEndOfStopPath(match);
		
		// Continue through block until end of block or limit on how far
		// into the future should generate predictions reached.
		while (predictionTime < 
					avlTime + CoreConfig.getMaxPredictionsTimeMsecs()) {
			// Keep track of whether prediction is affected by layover 
			// scheduled departure time since those predictions might not
			// be a accurate. Once a layover encountered then all subsequent
			// predictions are affected by a layover.
			if (indices.isWaitStop())
				affectedByWaitStop = true;
			
			// Determine the new prediction
			Prediction predictionForStop = generatePredictionForStop(avlReport,
					indices, predictionTime,
					useArrivalPredictionsForNormalStops, affectedByWaitStop);
			logger.debug("For vehicleId={} generated prediction {}",
					vehicleState.getVehicleId(), predictionForStop);
			
			// If prediction ended up being too far in the future (which can 
			// happen if it is a departure prediction where the time at the 
			// stop is added to the arrival time) then don't add the prediction
			// and break out of the loop.
			if (predictionForStop.getTime() > 
					avlTime + CoreConfig.getMaxPredictionsTimeMsecs())
				break;
			
			// The prediction is not too far into the future so add it to the list
			newPredictions.add(predictionForStop);			

			// Determine prediction time for the departure. For layovers
			// the prediction time can be adjusted by deadhead time,
			// schedule time, break time, etc. For arrival predictions
			// need to add the expected stop time.
			predictionTime = predictionForStop.getTime();
			if (predictionForStop.isArrival())
				predictionTime += indices.getStopTimeForPath();
			
			// Increment indices so can generate predictions for next path
			indices.incrementStopPath();
			
			// If reached end of block then done
			if (indices.pastEndOfBlock()) {
				logger.debug("For vehicleId={} reached end of block when " +
						"generating predictions.", 
						vehicleState.getVehicleId());
				break;
			}
			
			// Add in travel time for the next path to get to predicted 
			// arrival time of this stop
			predictionTime += indices.getTravelTimeForPath();
		}

		// Return the results
		return newPredictions;
	}
	
}
