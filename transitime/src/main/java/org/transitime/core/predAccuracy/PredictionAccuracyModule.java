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

package org.transitime.core.predAccuracy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.config.IntegerConfigValue;
import org.transitime.core.dataCache.PredictionDataCache;
import org.transitime.db.structs.ArrivalDeparture;
import org.transitime.db.structs.PredictionAccuracy;
import org.transitime.db.structs.Route;
import org.transitime.db.structs.TripPattern;
import org.transitime.ipc.data.IpcPrediction;
import org.transitime.ipc.data.IpcPredictionsForRouteStopDest;
import org.transitime.modules.Module;
import org.transitime.utils.IntervalTimer;
import org.transitime.utils.MapKey;
import org.transitime.utils.Time;

/**
 * Reads internal predictions every transitime.predAccuracy.pollingRateMsec and
 * stores the predictions into memory. Then when arrivals/departures occur the
 * prediction accuracy can be determined and stored.
 *
 *
 * @author SkiBu Smith
 *
 */
public class PredictionAccuracyModule extends Module {

	// The map that contains all of the predictions to be used for prediction 
	// accuracy analysis. Each value is a list of predictions because can have
	// more than a single prediction stored in memory for a vehicle/stop.
	// Declared static because want to be able to access it from another
	// class by using the static method handleArrivalDeparture().
	private static ConcurrentHashMap<PredictionKey, List<PredAccuracyPrediction>> predictionMap =
			new ConcurrentHashMap<PredictionAccuracyModule.PredictionKey, List<PredAccuracyPrediction>>();
	
	private static final Logger logger = LoggerFactory
			.getLogger(PredictionAccuracyModule.class);

	/********************** Config Params **************************/
	
	private static final IntegerConfigValue timeBetweenPollingPredictionsMsec = 
			new IntegerConfigValue("transitime.predAccuracy.pollingRateMsec", 
					4 * Time.MS_PER_MIN,
					"How frequently to query predictions for determining "
					+ "prediction accuracy.");
	
	private static int getTimeBetweenPollingPredictionsMsec() {
		return timeBetweenPollingPredictionsMsec.getValue();
	}

	private static final IntegerConfigValue maxPredTimeMinutes = 
			new IntegerConfigValue("transitime.predAccuracy.maxPredTimeMinutes", 
					15,
					"Maximum time into the future for a pediction for it to "
					+ "be stored in memory for prediction accuracy analysis.");
	
	private static int getMaxPredTimeMinutes() {
		return maxPredTimeMinutes.getValue();
	}

	private static final IntegerConfigValue maxPredStalenessMinutes = 
			new IntegerConfigValue("transitime.predAccuracy.maxPredStalenessMinutes", 
					15,
					"Maximum time in minutes a prediction cam be into the "
					+ "past before it is removed from memory because no "
					+ "corresponding arrival/departure time was determined.");
	
	private static int getMaxPredStalenessMinutes() {
		return maxPredStalenessMinutes.getValue();
	}

	private static final IntegerConfigValue stopsPerTrip = 
			new IntegerConfigValue("transitime.predAccuracy.stopsPerTrip", 
					5,
					"Number of stops per trip pattern that should collect "
					+ "prediction data for each polling cycle.");
	
	private static int getStopsPerTrip() {
		return stopsPerTrip.getValue();
	}

	private static final IntegerConfigValue maxLatenessComparedToPredictionMsec = 
			new IntegerConfigValue("transitime.predAccuracy.maxLatenessComparedToPredictionMsec", 
					25 * Time.MS_PER_MIN,
					"How late in msec a vehicle can arrive/departure a stop "
					+ "compared to the prediction and still have the prediction "
					+ "be considered a match.");
	
	private static int getMaxLatenessComparedToPredictionMsec() {
		return maxLatenessComparedToPredictionMsec.getValue();
	}

	private static final IntegerConfigValue maxEarlynessComparedToPredictionMsec = 
			new IntegerConfigValue("transitime.predAccuracy.maxEarlynessComparedToPredictionMsec", 
					15 * Time.MS_PER_MIN,
					"How early in msec a vehicle can arrive/departure a stop "
					+ "compared to the prediction and still have the prediction "
					+ "be considered a match.");
	
	private static int getMaxEarlynessComparedToPredictionMsec() {
		return maxEarlynessComparedToPredictionMsec.getValue();
	}

	/********************** Internal Classes **************************/
	
	/**
	 * For keeping track of which routes and stops to get predictions for.
	 */
	public static class RouteAndStops {
		public String routeId;
		// Keyed on direction ID
		public Map<String, Collection<String>> stopIds = 
				new HashMap<String, Collection<String>>();
		
		@Override
		public String toString() {
			return "RouteAndStops [" 
					+ "routeId=" + routeId + ", stopIds=" + stopIds
					+ "]";
		}
	}
	
	/**
	 * Key for map of predictions
	 */
	protected static class PredictionKey extends MapKey {
		private PredictionKey(String vehicleId, String directionId, String stopId) {
			super(vehicleId, directionId, stopId);
		}

		@Override
		public String toString() {
			return "PredictionKey [" + "vehicleId=" + o1 + ", directionId=" + o2
					+ ", stopId=" + o3 + "]";
		}
	}

	/********************** Member Functions **************************/

	/**
	 * The constructor for the module. Called automatically if the module
	 * is configured.
	 * 
	 * @param agencyId
	 */
	public PredictionAccuracyModule(String agencyId) {
		super(agencyId);
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// Log that module successfully started
		logger.info("Started module {} for agencyId={}", 
				getClass().getName(), getAgencyId());
		
		// No need to run at startup since internal predictions won't be
		// generated yet. So sleep a bit first.
		Time.sleep(getTimeBetweenPollingPredictionsMsec());
		
		// Run forever
		while (true) {
			try {
				IntervalTimer timer = new IntervalTimer();
								
				// Process data
				getAndProcessData(getRoutesAndStops(), new Date());
				
				// Make sure old predictions that were never matched to an
				// arrival/departure don't stick around taking up memory.
				clearStalePredictions();
				
				// Wait appropriate amount of time till poll again
				long elapsedMsec = timer.elapsedMsec();
				long sleepTime = 
						getTimeBetweenPollingPredictionsMsec() - elapsedMsec;
				if (sleepTime > 0)
					Time.sleep(sleepTime);
			} catch (Exception e) {
				logger.error("Error accessing predictions feed", e);
			}
		}
	}

	/**
	 * Returns the routes and stops that should store predictions in memory for.
	 * Usually will be all routes for an agency, with a sampling of stops.
	 * 
	 * @return
	 */
	protected List<RouteAndStops> getRoutesAndStops() {
		// The value to be returned
		List<RouteAndStops> list = new ArrayList<RouteAndStops>();
		
		// For each route...
		List<Route> routes = Core.getInstance().getDbConfig().getRoutes();
		for (Route route : routes) {
			RouteAndStops routeStopInfo = new RouteAndStops();
			list.add(routeStopInfo);
			
			routeStopInfo.routeId = route.getId();
			
			// For each direction for the route...
			List<TripPattern> tripPatterns = 
					route.getLongestTripPatternForEachDirection();
			for (TripPattern tripPattern : tripPatterns) {
				List<String> stopIdsForTripPattern = tripPattern.getStopIds();
				
				// If not that many stops for the trip then use all of them.
				if (getStopsPerTrip() >= stopIdsForTripPattern.size()) {
					// Use all stops for this trip pattern
					routeStopInfo.stopIds.put(tripPattern.getDirectionId(), 
							stopIdsForTripPattern);
				} else {
					// Get stops for direction randomly
					Set<String> stopsSet = new HashSet<String>();
					while (stopsSet.size() < getStopsPerTrip()) {
						// Randomly get a stop ID for the trip pattern
						int index = (int) (stopIdsForTripPattern.size() * 
								Math.random());
						String stopId = stopIdsForTripPattern.get(index);
						if (!stopsSet.contains(stopId)) {
							stopsSet.add(stopId);
						}
					}
					routeStopInfo.stopIds.put(tripPattern.getDirectionId(), 
							stopsSet);
				}
			}
		}
		
		// Return the routes/stops that predictions should be stored in 
		// memory for
		logger.debug("getRoutesAndStops() returning {}", list);
		return list;		
	}
	
	/**
	 * Stores prediction in memory so that when arrival/departure generated
	 * can compare with the stored prediction. Will only store prediction
	 * if it is less then transitime.predAccuracy.maxPredTimeMinutes into
	 * the future.
	 * 
	 * @param pred
	 */
	protected void storePrediction(PredAccuracyPrediction pred) {
		// If prediction too far into the future then don't store it in
		// memory. This is important because need to limit how much 
		// memory is used for prediction accuracy data collecting.
		if (pred.getPredictedTime().getTime() > 
			System.currentTimeMillis() + getMaxPredTimeMinutes()*Time.MS_PER_MIN) {
			logger.debug("Prediction is too far into future so not storing "
					+ "it in memory for prediction accuracy analysis. {}", 
					pred);
			return;
		}
		
		PredictionKey key = new PredictionKey(pred.getVehicleId(), 
				pred.getDirectionId(), pred.getStopId());
		List<PredAccuracyPrediction> predsList = predictionMap.get(key);
		if (predsList == null) {
			predictionMap.putIfAbsent(key,
					new ArrayList<PredAccuracyPrediction>(1));
			predsList = predictionMap.get(key);
		}
		logger.debug("Adding prediction to memory for prediction accuracy "
				+ "analysis. {}", pred);
		predsList.add(pred);
	}
	
	/**
	 * This method should be called every once in a while need to clear out old
	 * predictions that were never matched to an arrival/departure. This is
	 * needed because sometimes a vehicle will never arrive at a stop and so
	 * will not be removed from memory. In order to prevent memory use from
	 * building up need to clear out the old predictions.
	 */
	private void clearStalePredictions() {
		int numPredictionsInMemory = 0;
		int numPredictionsRemoved = 0;
		
		// Go through all predictions in memory...
		Collection<List<PredAccuracyPrediction>> allPreds = 
				predictionMap.values();
		for (List<PredAccuracyPrediction> predsForVehicleStop : allPreds) {
			Iterator<PredAccuracyPrediction> iter = predsForVehicleStop.iterator();
			while (iter.hasNext()) {
				PredAccuracyPrediction pred = iter.next();
				if (pred.getPredictedTime().getTime() < 
						System.currentTimeMillis() - 
						getMaxPredStalenessMinutes()*Time.MS_PER_MIN) {
					// Prediction was too old so remove it from memory
					++numPredictionsRemoved;
					logger.info("Removing prediction accuracy prediction "
							+ "from memory because it is too old. {}", pred);
					iter.remove();
					
					// Store prediction accuracy info so can note that 
					// a bad prediction was made
					storePredictionAccuracyInfo(pred, null);
				} else {
					++numPredictionsInMemory;					
				}
			}
		}
		
		logger.debug("There are now {} predictions in memory after removing {}.",
				numPredictionsInMemory, numPredictionsRemoved);
	}
	
	/**
	 * Gets and processes predictions from Transitime system. To be called every
	 * polling cycle to process internal predictions. To be overridden if
	 * getting predictions from external feed.
	 * 
	 * @param routesAndStops
	 * @param predictionsReadTime
	 *            For keeping track of when the predictions read in. Used for
	 *            determining length of predictions. Should be the same for all
	 *            predictions read in during a polling cycle even if the
	 *            predictions are read at slightly different times. By using the
	 *            same time can easily see from data in db which internal and
	 *            external predictions are associated with each other.
	 */
	protected void getAndProcessData(List<RouteAndStops> routesAndStops,
			Date predictionsReadTime) {
		logger.debug("Calling PredictionReaderModule.getAndProcessData() "
				+ "to process internal prediction.");

		// Get internal predictions from core and store them in memory
		for (RouteAndStops routeAndStop : routesAndStops) {
			String routeId = routeAndStop.routeId;

			Set<String> directionIds = routeAndStop.stopIds.keySet();
			for (String directionId : directionIds) {
				Collection<String> stopIds = 
						routeAndStop.stopIds.get(directionId);
				for (String stopId : stopIds) {
					List<IpcPredictionsForRouteStopDest> predictions = 
							PredictionDataCache.getInstance().getPredictions(
									routeId, stopId, directionId);
					boolean predictionsFound = false;
					for (IpcPredictionsForRouteStopDest predList : predictions) {
						for (IpcPrediction pred : predList
								.getPredictionsForRouteStop()) {
							PredAccuracyPrediction accuracyPred = 
									new PredAccuracyPrediction(
											routeId, directionId, stopId,
											pred.getTripId(), 
											pred.getVehicleId(),
											new Date(pred.getPredictionTime()),
											predictionsReadTime,
											pred.isArrival(),
											pred.isAffectedByWaitStop(),
											"Transitime");
							storePrediction(accuracyPred);
							predictionsFound = true;
						}
					}
					
					// Nice to log when predictions for stop not found so can 
					// see if not getting predictions when should be.
					if (!predictionsFound)
						logger.debug("No predictions found for routeId={} "
								+ "directionId={} stopId={}", 
								routeId, directionId, stopId);
				}
			}
		}
	}
	
	/**
	 * Looks for corresponding prediction in memory. If found then prediction
	 * accuracy information for that prediction is stored in the database.
	 * <p>
	 * This method is to be called when an arrival or a departure is created.
	 * 
	 * @param arrivalDeparture
	 *            The arrival or departure that was generated
	 */
	public static void handleArrivalDeparture(
			ArrivalDeparture arrivalDeparture) {
		// Get the List of predictions for the vehicle/direction/stop 
		PredictionKey key = new PredictionKey(arrivalDeparture.getVehicleId(), 
				arrivalDeparture.getDirectionId(), arrivalDeparture.getStopId());
		List<PredAccuracyPrediction> predsList = predictionMap.get(key);
		if (predsList == null || predsList.isEmpty())
			return;
		
		// Go through list of predictions for vehicle, direction, stop and handle
		// the ones that match fully including being appropriate arrival or
		// departure.
		Iterator<PredAccuracyPrediction> predIterator = predsList.iterator();
		while (predIterator.hasNext()) {
			PredAccuracyPrediction pred = predIterator.next();
			
			// If not correct arrival/departure type continue to next prediction
			if (pred.isArrival() != arrivalDeparture.isArrival())
				continue;
			
			// Make sure it is for the proper trip. This is important in case a
			// vehicle is reassigned after a prediction is made. For example, a
			// prediction could be made for a trip to leave at 10am but then the
			// vehicle is reassigned to leave at 9:50am or 10:10am. That 
			// shouldn't be counted against vehicle accuracy since likely 
			// another vehicle substituted in for the original assignment. This 
			// is especially true for MBTA Commuter Rail
			if (!pred.getTripId().equals(arrivalDeparture.getTripId()))
				continue;
			
			// Make sure predicted time isn't too far away from the 
			// arrival/departure time so that don't match to something really
			// inappropriate. First determine how late vehicle arrived 
			// at stop compared to the original prediction time.
			long latenessComparedToPrediction = arrivalDeparture.getTime() 
					- pred.getPredictedTime().getTime();
			if (latenessComparedToPrediction > getMaxLatenessComparedToPredictionMsec()
					|| latenessComparedToPrediction < -getMaxEarlynessComparedToPredictionMsec())
				continue;
			
			// There is a match so store the prediction accuracy info into the 
			// database
			storePredictionAccuracyInfo(pred, arrivalDeparture);
			
			// Remove the prediction that was matched
			predIterator.remove();
		}
	}

	/**
	 * Combine the arrival/departure with the corresponding prediction and
	 * creates PredictionAccuracy object and stores it in database.
	 * 
	 * @param pred
	 * @param arrivalDeparture
	 *            The corresponding arrival/departure information. Can be null
	 *            to indicate that for a prediction no corresponding
	 *            arrival/departure was ever determined.
	 */
	private static void storePredictionAccuracyInfo(
			PredAccuracyPrediction pred, ArrivalDeparture arrivalDeparture) {
		// If no corresponding arrival/departure found for prediction
		// then use null for arrival/departure time to indicate such.
		Date arrivalDepartureTime = arrivalDeparture!=null ? 
				new Date(arrivalDeparture.getTime()) : null;
				
		// Combine the arrival/departure with the corresponding prediction
		// and create PredictionAccuracy object
		PredictionAccuracy predAccuracy = new PredictionAccuracy(
				pred.getRouteId(), pred.getDirectionId(), pred.getStopId(),
				pred.getTripId(), arrivalDepartureTime,
				pred.getPredictedTime(), pred.getPredictionReadTime(),
				pred.getSource(), pred.getVehicleId(), pred.isAffectedByWaitStop());
		
		// Add the prediction accuracy object to the db logger so that
		// it gets written to database
		logger.debug("Storing prediction accuracy object to db. {}",
				predAccuracy);
		Core.getInstance().getDbLogger().add(predAccuracy);
	}
}
