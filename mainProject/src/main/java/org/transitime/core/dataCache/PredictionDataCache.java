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
package org.transitime.core.dataCache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.core.VehicleState;
import org.transitime.db.structs.Route;
import org.transitime.db.structs.Trip;
import org.transitime.ipc.data.IpcPrediction;
import org.transitime.ipc.data.IpcPredictionsForRouteStopDest;
import org.transitime.ipc.interfaces.PredictionsInterface.RouteStop;
import org.transitime.utils.MapKey;
import org.transitime.utils.SystemCurrentTime;
import org.transitime.utils.SystemTime;

/**
 * For storing and retrieving predictions by stop.
 * <p>
 * Concurrency and thread safety is very important in this class since will
 * have multiple threads writing and reading predictions. Need to make sure
 * that the predictions obtained are always complete, even if another thread
 * is simultaneously updating predictions by removing one and then adding
 * an updated prediction. Otherwise might obtain predictions when the
 * old one has been removed but the new one has not yet been written, causing
 * a prediction to be missed.
 * <p>
 * For concurrency and thread safety using ConcurrentHashMap instead of 
 * HashMap. 
 * <p> 
 * getPredictions() returns a copy of the list of predictions for the
 * route/stop. This way the contents will always be coherent and the 
 * caller does not need to synchronize, which would be difficult to
 * enforce.
 * 
 * @author SkiBu Smith
 */
public class PredictionDataCache {

	// This is a singleton class
	private static PredictionDataCache singleton = 
			new PredictionDataCache(new SystemCurrentTime());
	
	// Contains lists of predictions per route/stop. Also want to group
	// predictions by destination/trip head sign together so that can
	// show such predictions separately. This is important for routes
	// like the SFMTA routes 30 and 38 that have multiple distinct
	// destinations. When some people want predictions for those routes
	// they might need the predictions for the different destinations
	// to be separate from each other so they can get predictions for the
	// vehicles actually going all the way where they want. Therefore
	// for each route/stop have a List of PredictionsForRouteStop, one
	// for each destination/trip head sign.
	// Keyed by MapKey using routeId/stopId.
	// ConcurrentHashMap is used so that can associate a route/stop with a 
	// PredictionsForRouteStop in a threadsafe way. Will always use same 
	// PredictionsForRouteStop for a route/stop and synchronize any changes and 
	// access to it so if multiple threads are making changes on a route/stop 
	// those changes will be coherent and information will not be lost.
	private final ConcurrentHashMap<MapKey, List<IpcPredictionsForRouteStopDest>> 
		predictionsMap =
			new ConcurrentHashMap<MapKey, List<IpcPredictionsForRouteStopDest>>(1000);
	
	// For determining when prediction is obsolete (it is for before the
	// current system time)
	private final SystemTime systemTime;
	
	private static final Logger logger = 
			LoggerFactory.getLogger(PredictionDataCache.class);

	/********************** Member Functions **************************/
	
	/**
	 * Returns singleton object for this class. It will use the regular
	 * SystemCurrentTime class for determining the time and whether any 
	 * predictions are obsolete.
	 * 
	 * @return
	 */
	public static PredictionDataCache getInstance() {
		return singleton;		
	}
	
	/*
	 * Can pass in either a regular SystemCurrentTime object or a 
	 * SettableSystemTime if debugging or doing unit testing. Declared
	 * private since this is a singleton class.
	 * 
	 * @param systemTime
	 */
	private PredictionDataCache(SystemTime systemTime) {
		this.systemTime = systemTime;
	}
		
	/**
	 * Returns the current time. Can be based on the systems clock
	 * but when in playback mode will be based on last AVL report.
	 * @return
	 */
	public long getSystemTime() {
		return systemTime.get();
	}
	
	/**
	 * Returns copy of the PredictionsForRouteStop object. A clone is used so that it
	 * can be accessed as needed without worrying about another thread writing
	 * to it. The list of predictions should be relatively small so cloning is
	 * not very costly. And this way the caller of this method doesn't have to
	 * synchronize or such.
	 * 
	 * @param routeShortName
	 * @param stopId
	 * @param maxPredictionsPerStop
	 * @return
	 */
	public List<IpcPredictionsForRouteStopDest> getPredictions(String routeShortName,
			String stopId, int maxPredictionsPerStop) {
		// Get the predictions from the map
		List<IpcPredictionsForRouteStopDest> predictionsForRouteStop = 
				getPredictionsForRouteStop(routeShortName, stopId);
		
		// Make a copy of the prediction objects so that they cannot be
		// modified by another thread while they are being accessed. This
		// is important because when the predictions are modified they are
		// temporarily not coherent. 
		List<IpcPredictionsForRouteStopDest> clonedPredictions = 
				new ArrayList<IpcPredictionsForRouteStopDest>(predictionsForRouteStop.size());
		for (IpcPredictionsForRouteStopDest predictions : predictionsForRouteStop) {
			clonedPredictions.add(predictions.getClone(maxPredictionsPerStop));
		}
		
		// Return the safe cloned predictions
		return clonedPredictions;
	}
	
	/**
	 * Returns copy of the predictions associated with the route/stop specified.
	 * Note that this method uses the routeId instead of the routeShortName.
	 * 
	 * @param routeId
	 * @param stopId
	 * @param maxPredictionsPerStop
	 * @return
	 */
	public List<IpcPredictionsForRouteStopDest> getPredictionsUsingRouteId(String routeId, 
			String stopId, int maxPredictionsPerStop) {
		// Determine the routeShortName
		String routeShortName = null;
		if (routeId != null) {
			Route route = Core.getInstance().getDbConfig().getRouteById(routeId);
			if (route != null)
				routeShortName = route.getShortName();
		}
		
		// Get and return the associated predictions
		return getPredictions(routeShortName, stopId, maxPredictionsPerStop);
	}
	
	/**
	 * Returns copy of all predictions currently associated with the stop. Uses
	 * routeShortName instead of the GTFS routeId to identify the stop.
	 * 
	 * @param routeShortName
	 * @param stopId
	 * @return
	 */
	public List<IpcPredictionsForRouteStopDest> getPredictions(String routeShortName,
			String stopId) {
		return getPredictions(routeShortName, stopId, Integer.MAX_VALUE);
	}

	/**
	 * Returns copy of all predictions currently associated with the stop. Uses
	 * GTFS routeId instead of the routeShortName to identify the stop.
	 * 
	 * @param routeId
	 * @param stopId
	 * @return
	 */
	public List<IpcPredictionsForRouteStopDest> getPredictionsUsingRouteId(
			String routeId, String stopId) {
		return getPredictionsUsingRouteId(routeId, stopId, Integer.MAX_VALUE);
	}

	/**
	 * Returns copy of List<PredictionsForRouteStop> objects for each route/stop
	 * specified.
	 * 
	 * @param routeStops
	 *            Specified using route_short_name instead of route_id
	 * @param predictionsPerStop
	 * @return
	 */
	public List<IpcPredictionsForRouteStopDest> getPredictions(List<RouteStop> routeStops,
			int predictionsPerStop) {
		List<IpcPredictionsForRouteStopDest> listOfPredictions = 
				new ArrayList<IpcPredictionsForRouteStopDest>();
		for (RouteStop routeStop : routeStops) {
			List<IpcPredictionsForRouteStopDest> predsForStop = getPredictions(
					routeStop.getRouteIdOrShortName(), routeStop.getStopId(),
					predictionsPerStop);
			for (IpcPredictionsForRouteStopDest predictions : predsForStop)
				listOfPredictions.add(predictions);
		}
		return listOfPredictions;
	}
	
	/**
	 * Returns copy of List<Prediction> objects for each route/stop specified.
	 * Uses route_id instead of route_short_name to identify which predictions
	 * to return.
	 * 
	 * @param routeStops Specified using route_id instead of route_short_name
	 * @param predictionsPerStop
	 * @return
	 */
	public List<IpcPredictionsForRouteStopDest> getPredictionsUsingRouteId(
			List<RouteStop> routeStops, int predictionsPerStop) {
		List<IpcPredictionsForRouteStopDest> listOfPredictions = 
				new ArrayList<IpcPredictionsForRouteStopDest>();
		for (RouteStop routeStop : routeStops) {
			List<IpcPredictionsForRouteStopDest> predsForStop = getPredictionsUsingRouteId(
					routeStop.getRouteIdOrShortName(), routeStop.getStopId(),
					predictionsPerStop);
			for (IpcPredictionsForRouteStopDest predictions : predsForStop)
				listOfPredictions.add(predictions);
		}
		return listOfPredictions;
	}
	
	/**
	 * Returns copy of all predictions currently associated for each route/stop specified.
	 * 
	 * @param routeStops
	 * @return
	 */
	public List<IpcPredictionsForRouteStopDest> getPredictions(
			List<RouteStop> routeStops) {
		return getPredictions(routeStops, Integer.MAX_VALUE);
	}
	
	/**
	 * Returns copy of all predictions currently associated for each route/stop specified.
	 * Uses route_id instead of route_short_name to identify which predictions
	 * to return.
	 * 
	 * @param routeStops
	 * @return
	 */
	public List<IpcPredictionsForRouteStopDest> getPredictionsUsingRouteId(
			List<RouteStop> routeStops) {
		return getPredictionsUsingRouteId(routeStops, Integer.MAX_VALUE);
	}
	
	/**
	 * Returns copy of all predictions for system, limited by
	 * maxPredictionsPerStop and maxPredictionTime.
	 * 
	 * @param maxPredictionsPerStop
	 *            Maximum number of predictions per route/stop/destination to
	 *            clone.
	 * @param maxSystemTimeForPrediction
	 *            Max point in future want predictions for. This way can limit
	 *            predictions when requesting a large number of them.
	 * @return
	 */
	public List<IpcPredictionsForRouteStopDest> getAllPredictions(
			int maxPredictionsPerStop, long maxSystemTimeForPrediction) {
		List<IpcPredictionsForRouteStopDest> allPredictions = 
				new ArrayList<IpcPredictionsForRouteStopDest>(5000);
		
		// Go through all PredictionsForRouteStop objects
		Collection<List<IpcPredictionsForRouteStopDest>> predictionsByRouteStop = 
				predictionsMap.values();		
		for (List<IpcPredictionsForRouteStopDest> predictionsForRouteStop : predictionsByRouteStop) {
			for (IpcPredictionsForRouteStopDest predictionForRouteStopDest : predictionsForRouteStop) {
				IpcPredictionsForRouteStopDest clonedPrediction = 
						predictionForRouteStopDest.getClone(
								maxPredictionsPerStop, maxSystemTimeForPrediction);
				// If there were valid predictions then include it in array to
				// be returned
				if (!clonedPrediction.getPredictionsForRouteStop().isEmpty())
					allPredictions.add(clonedPrediction);
			}
		}
		
		return allPredictions;
	}
	
	/**
	 * Updates predictions in the the cache that are associated with a vehicle.
	 * Removes any that are in oldPredictionsForVehicle and adds all the ones in
	 * newPredictionsForVehicle.
	 * 
	 * @param oldPredictionsForVehicle
	 *            The old predictions to be removed.
	 * @param newPredictionsForVehicle
	 *            The new predictions. Can be null if only removing old
	 *            predictions
	 */
	public void updatePredictions(List<IpcPrediction> oldPredictionsForVehicle,
			List<IpcPrediction> newPredictionsForVehicle) {
		// Handle null being passed in for newPredictionsForVehicle
		if (newPredictionsForVehicle == null)
			newPredictionsForVehicle = new ArrayList<IpcPrediction>();
		
		// Can have several predictions for a route/stop for a vehicle if
		// the route is a relatively short loop. And if have unscheduled
		// trips then won't have a unique trip identifier. Therefore to
		// atomically update the predictions for this vehicle for this
		// route/stop need to delete all predictions for the vehicle
		// and add all new ones in one synchronized operation.
		// First step is to group the new predictions by route/stop so
		// can deal with them all at once. Those are put into
		// newPredictionsByRouteStop.
		Map<MapKey, List<IpcPrediction>> newPredsForVehicleByRouteStopMap =
				new HashMap<MapKey, List<IpcPrediction>>();
		
		for (IpcPrediction newPrediction : newPredictionsForVehicle) {
			MapKey key = new MapKey(newPrediction.getRouteShortName(),
					newPrediction.getStopId(), newPrediction.getTrip()
							.getName());
			List<IpcPrediction> predsForRouteStopList = 
					newPredsForVehicleByRouteStopMap.get(key);
			if (predsForRouteStopList == null) {
				predsForRouteStopList = new ArrayList<IpcPrediction>();
				newPredsForVehicleByRouteStopMap
						.put(key, predsForRouteStopList);
			}
			predsForRouteStopList.add(newPrediction);
		}
		
		// Go through the new predictions grouped by route/stop/destination and
		// process them.
		for (List<IpcPrediction> newPredsForVehicleForRouteStop : 
				newPredsForVehicleByRouteStopMap.values()) {
			updatePredictionsForVehicle(newPredsForVehicleForRouteStop);
		}
		
		// Remove old predictions that are not in newPredictionsForVehicle 
		if (oldPredictionsForVehicle != null) {
			for (IpcPrediction oldPrediction : oldPredictionsForVehicle) {
				// If there is no new prediction for the old prediction 
				// route/stop...
				MapKey key = new MapKey(oldPrediction.getRouteShortName(),
						oldPrediction.getStopId(), oldPrediction.getTrip()
								.getName());
				if (newPredsForVehicleByRouteStopMap.get(key) == null) {
					// Remove the old prediction
					removePrediction(oldPrediction);
				}
			}
		}
	}
	
	/**
	 * To be called when vehicle is being made unpredictable. Removes the 
	 * predictions.
	 * 
	 * @param vehicleState
	 */
	public void removePredictions(VehicleState vehicleState) {
		logger.info("Removing predictions for vehicleId={}", 
				vehicleState.getVehicleId());
		List<IpcPrediction> oldPredictions = vehicleState.getPredictions();
		
		updatePredictions(oldPredictions, null);
	}

	/**
	 * Removes old prediction from the map. For when there is no new prediction
	 * for the vehicle for the route/stop.
	 * 
	 * @param oldPrediction
	 */
	private void removePrediction(IpcPrediction oldPrediction) {
		logger.debug("Removing prediction={}", oldPrediction);
		
		// Get the prediction list from the map
		IpcPredictionsForRouteStopDest predictions = 
				getPredictionsForRouteStopDestination(oldPrediction);
		predictions.removePrediction(oldPrediction);
	}

	/**
	 * Gets the prediction list for the route/stop/destination, synchronizes it
	 * so that changes are threadsafe, and then updates the list with the new
	 * predictions. Each route/stop will usually get only a single prediction
	 * but there are situations where a vehicle will hit a stop more than once
	 * with the max time that predictions are generated for. For such a case
	 * need to add all of those predictions at once.
	 * 
	 * @param newPredsForVehicleForRouteStopDest
	 *            the new predictions to be set for the route/stop/destination.
	 */
	private void updatePredictionsForVehicle(
			List<IpcPrediction> newPredsForVehicleForRouteStopDest) {
		// If no predictions then nothing to do so return.
		if (newPredsForVehicleForRouteStopDest == null || 
				newPredsForVehicleForRouteStopDest.isEmpty())
			return;
		
		logger.debug("Adding predictions for the route/stop/destination: {}", 
				newPredsForVehicleForRouteStopDest);

		// Get the current predictions for the route/stop/destination
		IpcPrediction pred = newPredsForVehicleForRouteStopDest.get(0);
		IpcPredictionsForRouteStopDest currentPredsForRouteStopDest = 
				getPredictionsForRouteStopDestination(pred);
		
		// Update the predictions for the route/stop/destination
		currentPredsForRouteStopDest.updatePredictionsForVehicle(
				newPredsForVehicleForRouteStopDest, systemTime.get());
	}
	
	/**
	 * Returns List of PredictionsForRouteStop objects associated with the
	 * specified route/stop. Returns a list because there is a separate
	 * PredictionsForRouteStop for each destination and some route directions
	 * have multiple destinations.
	 * 
	 * @param routeShortName
	 * @param stopId
	 * @return
	 */
	private List<IpcPredictionsForRouteStopDest> getPredictionsForRouteStop(
			String routeShortName, String stopId) {
		// Determine the predictions for all destinations for the route/stop
		MapKey key = MapKey.create(routeShortName, stopId);
		List<IpcPredictionsForRouteStopDest> predictionsForRouteStop = 
				predictionsMap.get(key);
		if (predictionsForRouteStop == null) {
			predictionsForRouteStop = new ArrayList<IpcPredictionsForRouteStopDest>(1);
			predictionsMap.putIfAbsent(key, predictionsForRouteStop);
		} 

		return predictionsForRouteStop;
	}
	
	/**
	 * Returns PredictionsForRouteStop object associated with the specified
	 * route/stop/destination specified by the trip and stopId parameters.
	 * <p>
	 * The returned PredictionsForRouteStop objects needs to be synchronized
	 * when predictions accessed. Otherwise might access temporarily corrupt
	 * state when a prediction is being updated.
	 * 
	 * @param trip
	 * @param stopId
	 * @return The PredictionsForRouteStop for the specified
	 *         route/stop/destination
	 */
	private IpcPredictionsForRouteStopDest getPredictionsForRouteStopDestination(
			Trip trip, String stopId) {
		// Determine the predictions for all destinations for the route/stop
		List<IpcPredictionsForRouteStopDest> predictionsForRouteStop = 
				getPredictionsForRouteStop(trip.getRouteShortName(), stopId);
		
		// Sync on the array so that it can't change between when check to
		// find out if the PredictionsForRouteStopDest already exists for
		// the destination till the time insert new one.
		synchronized (predictionsForRouteStop) {
			// From the list of predictions return the one that is for the
			// specified destination.
			for (IpcPredictionsForRouteStopDest preds : predictionsForRouteStop) {
				if (preds.getDestination() == null
						|| preds.getDestination().equals(trip.getName()))
					return preds;
			}

			// The PredictionsForRouteStopDest was not yet created for the
			// route/stop/destination so create it now and add it to list
			// of PredictionsForRouteStopDest objects for the route/stop.
			IpcPredictionsForRouteStopDest preds = 
					new IpcPredictionsForRouteStopDest(trip, stopId);
			predictionsForRouteStop.add(preds);
			return preds;
		}

	}
	
	/**
	 * Returns PredictionsForRouteStop object associated with the
	 * specified route/stop/destination specified by the prediction parameter.
	 * 
	 * @param prediction
	 *            for specifying route and stop for which to get the prediction
	 *            list for.
	 * @return The PredictionsForRouteStop for the specified route/stop/destination
	 */
	private IpcPredictionsForRouteStopDest getPredictionsForRouteStopDestination(
			IpcPrediction prediction) {
		return getPredictionsForRouteStopDestination(prediction.getTrip(),
				prediction.getStopId());
	}
	
//	/**
//	 * For debugging
//	 * 
//	 * @param args
//	 */
//	public static void main(String args[]) {
//		String projectId = args.length > 0 ? args[0] : "testProjectId";
//
//		SystemTime systemTime = new SettableSystemTime(0);
//		PredictionDataCache testManager = new PredictionDataCache(systemTime);
//		PredictionsServer.start(projectId, testManager);
//		List<Prediction> predsV1 = new ArrayList<Prediction>();
//		predsV1.add(new Prediction("v1", "s1", 1, null, 100, 99, 0, false, null, 0, 0.0f, true));
//		predsV1.add(new Prediction("v1", "s2", 2, null, 200, 99, 0, false, null, 0, 0.0f, true));
//		predsV1.add(new Prediction("v1", "s1", 1, null, 300, 99, 0, false, null, 0, 0.0f, true));
//		testManager.updatePredictions(null, predsV1);
//		
//		List<Prediction> predsForS1 = testManager.getPredictions(null, "s1");
//		System.err.println("after v1 predsForS1=" + predsForS1);
//		
//		List<Prediction> predsV2 = new ArrayList<Prediction>();
//		predsV2.add(new Prediction("v2", "s1", 1, null, 150, 99, 0, false, null, 0, 0.0f, true));
//		predsV2.add(new Prediction("v2", "s2", 2, null, 250, 99, 0, false, null, 0, 0.0f, true));
//		predsV2.add(new Prediction("v2", "s1", 1, null, 350, 99, 0, false, null, 0, 0.0f, true));
//		testManager.updatePredictions(null, predsV2);
//
//		predsForS1 = testManager.getPredictions(null, "s1");
//		System.err.println("after v2 predsForS1=" + predsForS1);
//
//		List<Prediction> predsV1new = new ArrayList<Prediction>();
//		predsV1new.add(new Prediction("v1", "s1", 1, null, 110, 99, 0, false, null, 0, 0.0f, true));
//		predsV1new.add(new Prediction("v1", "s2", 2, null, 210, 99, 0, false, null, 0, 0.0f, true));
//		predsV1new.add(new Prediction("v1", "s1", 1, null, 310, 99, 0, false, null, 0, 0.0f, true));
//		testManager.updatePredictions(predsV1, predsV1new);
//		
//		predsForS1 = testManager.getPredictions(null, "s1");
//		System.err.println("after v1 update predsForS1=" + predsForS1);
//
//		List<Prediction> predsV3 = new ArrayList<Prediction>();
//		predsV3.add(new Prediction("v3", "s1", 1, null, 120, 99, 0, false, null, 0, 0.0f, true));
//		predsV3.add(new Prediction("v3", "s2", 2, null, 220, 99, 0, false, null, 0, 0.0f, true));
//		predsV3.add(new Prediction("v3", "s1", 1, null, 310, 99, 0, false, null, 0, 0.0f, true));
//		predsV3.add(new Prediction("v3", "s1", 1, null, 410, 99, 0, false, null, 0, 0.0f, true));
//		predsV3.add(new Prediction("v3", "s1", 1, null, 510, 99, 0, false, null, 0, 0.0f, true));
//		testManager.updatePredictions(null, predsV3);
//
//		predsForS1 = testManager.getPredictions(null, "s1");
//		System.err.println("after v3 predsForS1=" + predsForS1);
//	}
}
