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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.configData.CoreConfig;
import org.transitime.core.dataCache.PredictionDataCache;
import org.transitime.core.dataCache.VehicleDataCache;
import org.transitime.db.structs.ArrivalDeparture;
import org.transitime.db.structs.DbPrediction;
import org.transitime.db.structs.Match;
import org.transitime.ipc.data.Prediction;
import org.transitime.utils.Time;

/**
 * For generating predictions, arrival/departure times, headways etc. This class
 * is used once a vehicle is successfully matched to an assignment.
 * 
 * @author SkiBu Smith
 * 
 */
public class MatchProcessor {

	// Singleton class
	private static MatchProcessor singleton = new MatchProcessor();

	private static final Logger logger = 
			LoggerFactory.getLogger(MatchProcessor.class);

	/********************** Member Functions **************************/

	/**
	 * Constructor declared private because singleton class
	 */
	private MatchProcessor() {
	}

	/**
	 * Returns singleton MatchProcessor
	 * 
	 * @return
	 */
	public static MatchProcessor getInstance() {
		return singleton;
	}

	/**
	 * Generates the new predictions for the vehicle based on the new match
	 * stored in the vehicle state.
	 * 
	 * @param vehicleState
	 */
	private void processPredictions(VehicleState vehicleState) {
		logger.debug("Processing predictions for vehicleId={}",
				vehicleState.getVehicleId());

		// Generate the new predictions for the vehicle
		List<Prediction> newPredictions = 
				PredictionGeneratorFactory.getInstance().generate(vehicleState);

		// Store the predictions in database if so configured
		if (CoreConfig.getMaxPredictionsTimeForDbSecs() > 0) {
			for (Prediction prediction : newPredictions) {
				// If prediction not too far into the future then ...
				if (prediction.getTime() - prediction.getAvlTime() < CoreConfig
						.getMaxPredictionsTimeForDbSecs() * Time.MS_PER_SEC) {
					// Store the prediction into db
					DbPrediction dbPrediction = new DbPrediction(prediction);
					Core.getInstance().getDbLogger().add(dbPrediction);
				}
			}
		}

		// Update the predictions cache to use the new predictions for the
		// vehicle
		List<Prediction> oldPredictions = vehicleState.getPredictions();
		PredictionDataCache.getInstance().updatePredictions(oldPredictions,
				newPredictions);

		// Update predictions for vehicle
		vehicleState.setPredictions(newPredictions);
	}

	/**
	 * Generates the headway info based on the new match stored in the vehicle
	 * state.
	 * 
	 * @param vehicleState
	 */
	private void processHeadways(VehicleState vehicleState) {
		logger.debug("Processing headways for vehicleId={}",
				vehicleState.getVehicleId());

		HeadwayGeneratorFactory.getInstance().generate(vehicleState);
	}
	
	/**
	 * Generates the arrival/departure info based on the new match stored in the
	 * vehicle state. Stores the arrival/departure info into database.
	 * 
	 * @param vehicleState
	 */
	private void processArrivalDepartures(VehicleState vehicleState) {
		logger.debug("Processing arrivals/departures for vehicleId={}",
				vehicleState.getVehicleId());
		
		List<ArrivalDeparture> arrivalDepartures = 
				ArrivalDepartureGeneratorFactory.getInstance().generate(vehicleState);
		
		for (ArrivalDeparture arrivalDeparture : arrivalDepartures) {
			Core.getInstance().getDbLogger().add(arrivalDeparture);
		}
	}
	
	/**
	 * Stores the spatial match in log file and to database so can be
	 * processed later to determine expected travel times.
	 * @param vehicleState
	 */
	private void processSpatialMatch(VehicleState vehicleState) {
		logger.debug("Processing spatial match for vehicleId={}",
				vehicleState.getVehicleId());
		
		Match match = new Match(vehicleState);
		
		// Store match in database
		Core.getInstance().getDbLogger().add(match);
	}
	
	/**
	 * Called when vehicle is matched successfully. Generates predictions
	 * arrival/departure times, headways and such.
	 * 
	 * @param vehicleState
	 */
	public void generateResultsOfMatch(VehicleState vehicleState) {
		// Make sure everything ok
		if (!vehicleState.isPredictable()) {
			logger.error("Vehicle was unpredictable when "
					+ "MatchProcessor.generateResultsOfMatch() was called. {}",
					vehicleState);
			return;
		}

		logger.debug("Processing results for match for {}", vehicleState);

		// Process predictions, headways, arrivals/departures, and and spatial
		// matches
		processPredictions(vehicleState);
		processHeadways(vehicleState);
		processArrivalDepartures(vehicleState);
		processSpatialMatch(vehicleState);
		
		// Update the VehicleDataCache so that client can access vehicle data
		VehicleDataCache.getInstance().updateVehicle(vehicleState);
	}
}
