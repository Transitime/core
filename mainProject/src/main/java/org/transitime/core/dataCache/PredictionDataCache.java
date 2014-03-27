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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.core.VehicleState;
import org.transitime.db.structs.Route;
import org.transitime.ipc.data.Prediction;
import org.transitime.ipc.interfaces.PredictionsInterface.RouteStop;
import org.transitime.ipc.servers.PredictionsServer;
import org.transitime.utils.MapKey;
import org.transitime.utils.SettableSystemTime;
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
	
	// Contains lists of predictions per route/stop. 
	// Keyed by MapKey using routeId/stopId.
	// ConcurrentHashMap is used so that can associate a route/stop with a 
	// list of predictions in a threadsafe way. Will always use same List 
	// for a route/stop and synchronize any changes and access to it so
	// if multiple threads are making changes on a route/stop those
	// changes will be coherent and information will not be lost.
	// Specifying ArrayList instead of List so can use clone() for when
	// returning the list.
	private final ConcurrentHashMap<MapKey, ArrayList<Prediction>> predictionsMap =
			new ConcurrentHashMap<MapKey, ArrayList<Prediction>>(1000);
	
	// For determining when prediction is obsolete (it is for before the
	// current system time)
	private final SystemTime systemTime;
	
	// How big the prediction arrays for the route/stops can be. Really doesn't
	// need to be all that large. Might generate predictions further into the
	// future but when a user requests predictions they really just need 
	// a few.
	private final static int MAX_PREDICTIONS = 5;
	
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
	 * Returns copy of the List<Prediction> objects. A clone is used so that it
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
	public List<Prediction> getPredictions(String routeShortName, String stopId,
			int maxPredictionsPerStop) {
		// Get the prediction list from the map
		ArrayList<Prediction> predictions = getPredictionsList(routeShortName, stopId);
		
		// While it is safely synchronized, return a clone of the prediction list
		synchronized (predictions) {
			// Clone the list
			int size = Math.min(predictions.size(), maxPredictionsPerStop);
			List<Prediction> clonedPredictions = new ArrayList<Prediction>(size);			
			for (int i=0; i<size; ++i)
				clonedPredictions.add(i, predictions.get(i));
			
			// Return the cloned list
			logger.debug("Returning predictions for routeShortName={} stopId={} " +
					"predictions={}",
					routeShortName, stopId, clonedPredictions);
			return clonedPredictions;
		}
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
	public List<Prediction> getPredictionsUsingRouteId(String routeId, 
			String stopId, int maxPredictionsPerStop) {
		String routeShortName = null;
		if (routeId != null) {
			Route route = Core.getInstance().getDbConfig().getRoute(routeId);
			if (route != null)
				routeShortName = route.getShortName();
		}
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
	public List<Prediction> getPredictions(String routeShortName, String stopId) {
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
	public List<Prediction> getPredictionsUsingRouteId(String routeId, String stopId) {
		return getPredictionsUsingRouteId(routeId, stopId, Integer.MAX_VALUE);
	}

	/**
	 * Returns copy of List<Prediction> objects for each route/stop specified.
	 * 
	 * @param routeStops Specified using route_short_name instead of route_id
	 * @param predictionsPerStop
	 * @return
	 */
	public List<List<Prediction>> getPredictions(List<RouteStop> routeStops,
			int predictionsPerStop) {
		List<List<Prediction>> listOfPredictions = 
				new ArrayList<List<Prediction>>();
		for (RouteStop routeStop : routeStops) {
			List<Prediction> listForStop = getPredictions(
					routeStop.getRouteIdOrShortName(), routeStop.getStopId(),
					predictionsPerStop);
			listOfPredictions.add(listForStop);
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
	public List<List<Prediction>> getPredictionsUsingRouteId(List<RouteStop> routeStops,
			int predictionsPerStop) {
		List<List<Prediction>> listOfPredictions = 
				new ArrayList<List<Prediction>>();
		for (RouteStop routeStop : routeStops) {
			List<Prediction> listForStop = getPredictionsUsingRouteId(
					routeStop.getRouteIdOrShortName(), routeStop.getStopId(),
					predictionsPerStop);
			listOfPredictions.add(listForStop);
		}
		return listOfPredictions;
	}
	
	/**
	 * Returns all predictions currently associated for each route/stop specified.
	 * 
	 * @param routeStops
	 * @return
	 */
	public List<List<Prediction>> getPredictions(List<RouteStop> routeStops) {
		return getPredictions(routeStops, Integer.MAX_VALUE);
	}
	
	/**
	 * Returns all predictions currently associated for each route/stop specified.
	 * Uses route_id instead of route_short_name to identify which predictions
	 * to return.
	 * 
	 * @param routeStops
	 * @return
	 */
	public List<List<Prediction>> getPredictionsUsingRouteId(
			List<RouteStop> routeStops) {
		return getPredictionsUsingRouteId(routeStops, Integer.MAX_VALUE);
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
	public void updatePredictions(List<Prediction> oldPredictionsForVehicle,
			List<Prediction> newPredictionsForVehicle) {
		// Handle null being passed in for newPredictionsForVehicle
		if (newPredictionsForVehicle == null)
			newPredictionsForVehicle = new ArrayList<Prediction>();
		
		// Can have several predictions for a route/stop for a vehicle if
		// the route is a relatively short loop. And if have unscheduled
		// trips then won't have a unique trip identifier. Therefore to
		// atomically update the predictions for this vehicle for this
		// route/stop need to delete all predictions for the vehicle
		// and add all new ones in one synchronized operation.
		// First step is to group the new predictions by route/stop so
		// can deal with them all at once. Those are put into
		// newPredictionsByRouteStop.
		Map<MapKey, List<Prediction>> newPredsForVehicleByRouteStopMap =
				new HashMap<MapKey, List<Prediction>>();
		
		for (Prediction newPrediction : newPredictionsForVehicle) {
			MapKey key = new MapKey(newPrediction.getRouteShortName(), 
					newPrediction.getStopId());
			List<Prediction> predsForRouteStopList = 
					newPredsForVehicleByRouteStopMap.get(key);
			if (predsForRouteStopList == null) {
				predsForRouteStopList = new ArrayList<Prediction>();
				newPredsForVehicleByRouteStopMap.put(key, predsForRouteStopList);
			}
			predsForRouteStopList.add(newPrediction);
		}
		
		// Go through the new predictions grouped by route/stop and
		// process them.
		for (List<Prediction> newPredsForVehicleForRouteStop : 
				newPredsForVehicleByRouteStopMap.values()) {
			updatePredictionsForVehicle(newPredsForVehicleForRouteStop);
		}
		
		// Remove old predictions that are not in newPredictionsForVehicle 
		if (oldPredictionsForVehicle != null) {
			for (Prediction oldPrediction : oldPredictionsForVehicle) {
				// If there is no new prediction for the old prediction route/stop...
				MapKey key = new MapKey(oldPrediction.getRouteShortName(), 
						oldPrediction.getStopId());
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
		List<Prediction> oldPredictions = vehicleState.getPredictions();
		
		updatePredictions(oldPredictions, null);
	}

	/**
	 * Removes old prediction from the map. For when there is no new prediction
	 * for the vehicle for the route/stop.
	 * 
	 * @param oldPrediction
	 */
	private void removePrediction(Prediction oldPrediction) {
		logger.debug("Removing prediction={}", oldPrediction);
		
		// Get the prediction list from the map
		List<Prediction> predictions = getPredictionsList(oldPrediction);
		
		// Not sure if really need to synchronize removal of predictions
		// from list since it is only a single operation. But synching
		// it is certainly the safe thing to do.
		synchronized (predictions) {
			predictions.remove(oldPrediction);
		}
	}

	/**
	 * Gets the prediction list for the route/stop, synchronizes it so that
	 * changes are threadsafe, and then updates the list with the new
	 * predictions. Each route/stop will usually get only a single prediction
	 * but there are situations where a vehicle will hit a stop more than once
	 * with the max time that predictions are generated for. For such a case
	 * need to add all of those predictions at once.
	 * 
	 * @param newPredsForVehicleForRouteStop
	 *            the new predictions to be set for the route/stop.
	 */
	private void updatePredictionsForVehicle(
			List<Prediction> newPredsForVehicleForRouteStop) {
		// If no predictions then nothing to do so return.
		if (newPredsForVehicleForRouteStop == null || 
				newPredsForVehicleForRouteStop.isEmpty())
			return;
		
		logger.debug("Adding predictions for the route/stop: {}", 
				newPredsForVehicleForRouteStop);

		// Get the current prediction list from the map
		List<Prediction> currentPredsForRouteStop = 
				getPredictionsList(newPredsForVehicleForRouteStop.get(0));
		
		// While safely synchronized update the predictions for the route/stop
		synchronized (currentPredsForRouteStop) {
			updatePredictionsForVehicleForRouteStop(currentPredsForRouteStop, 
					newPredsForVehicleForRouteStop);
		}
	}
	
	/**
	 * Low-level method that takes in list of predictions for a route/stop and
	 * updates them. The list of predictions for the route stop must already be
	 * synchronized.
	 * 
	 * @param currentPredsForRouteStop
	 *            synchronized list of predictions currently associated with the
	 *            route/stop.
	 * @param newPredsForRouteStop
	 *            the new predictions to be used for the vehicle for the
	 *            route/stop.
	 */
	private void updatePredictionsForVehicleForRouteStop(
			List<Prediction> currentPredsForRouteStop, 
			List<Prediction> newPredsForRouteStop) {
		// If no predictions then nothing to do so return.
		if (newPredsForRouteStop == null || newPredsForRouteStop.isEmpty())
			return;
		
		// Determine which vehicle we are updating predictions for
		String vehicleId = newPredsForRouteStop.get(0).getVehicleId();
		
		// Go through current predictions and get rid of existing ones for
		// this vehicle or ones that have expired
		Iterator<Prediction> iterator = currentPredsForRouteStop.iterator();
		while (iterator.hasNext()) {
			Prediction currentPrediction = iterator.next();

			// Remove existing predictions for this vehicle
			if (currentPrediction.getVehicleId().equals(vehicleId)) {
				iterator.remove();
				continue;
			}
			
			// Remove predictions that are expired. It makes sense to do this here
			// when adding predictions since only need to take out predictions if
			// more are being added.
			if (currentPrediction.getTime() < systemTime.get()) {
				iterator.remove();
				continue;
			}
		}
		
		// Go through list and insert the new predictions into the 
		// appropriate places
		for (Prediction newPredForRouteStop : newPredsForRouteStop) {
			boolean insertedPrediction = false;
			for (int i=0; i<currentPredsForRouteStop.size(); ++i) {
				// If the new prediction is before the previous prediction
				// in currentPredsForRouteStop then insert it.
				if (newPredForRouteStop.getTime() < 
						currentPredsForRouteStop.get(i).getTime()) {
					// Add the new prediction to the list. If the list already
					// has the max number of predictions then first remove the
					// last one so that the array doesn't need to grow to 
					// accommodate the new one.
					int arraySize = currentPredsForRouteStop.size();
					if (arraySize == MAX_PREDICTIONS)
						currentPredsForRouteStop.remove(arraySize-1);
					
					// Now that definitely have room, actually add the 
					// prediction to the list
					currentPredsForRouteStop.add(i, newPredForRouteStop);
					insertedPrediction = true;
					
					// Done with the inner for loop so break out of loop
					// and continue with next prediction.
					break;
				}
			}
			
			// If didn't find that the prediction was before one of the 
			// existing ones then insert it onto the end if there is still
			// some space in the array.
			if (!insertedPrediction) {
				if (currentPredsForRouteStop.size() < MAX_PREDICTIONS) {
					currentPredsForRouteStop.add(currentPredsForRouteStop.size(), 
							newPredForRouteStop);
				} else {
					// Didn't insert prediction because it was greater than
					// the others but there is no space at end. This means that
					// done with the new predictions. Don't need to look at 
					// anymore because the remaining ones will have an even
					// higher prediction time and therefore also don't need
					// to be added.
					break;
				}
			}
		}

	}
		
	/**
	 * Returns List of Prediction objects associated with the specified
	 * route/stop. First time accessed the list will be empty. The returned
	 * list needs to be synchronized when accessed. Otherwise might access
	 * temporarily corrupt state when a prediction is being updated.
	 * 
	 * @param routeShortName
	 * @param stopId
	 * @return
	 */
	private ArrayList<Prediction> getPredictionsList(String routeShortName, 
			String stopId) {
		MapKey key = MapKey.create(routeShortName, stopId);
		ArrayList<Prediction> predictionsForRouteStop = predictionsMap.get(key);
		if (predictionsForRouteStop == null) {
			predictionsForRouteStop = new ArrayList<Prediction>(MAX_PREDICTIONS);
			predictionsMap.putIfAbsent(key, predictionsForRouteStop);
		} 
		return predictionsForRouteStop;
	}
	
	/**
	 * Returns List of Prediction objects associated with the specified
	 * route/stop specified by the prediction parameter.
	 * 
	 * @param prediction
	 *            for specifying route and stop for which to get the prediction
	 *            list for.
	 * @return
	 */
	private ArrayList<Prediction> getPredictionsList(Prediction prediction) {
		return getPredictionsList(prediction.getRouteShortName(), prediction.getStopId());
	}
	
	/**
	 * For debugging
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		String projectId = args.length > 0 ? args[0] : "testProjectId";

		SystemTime systemTime = new SettableSystemTime(0);
		PredictionDataCache testManager = new PredictionDataCache(systemTime);
		PredictionsServer.start(projectId, testManager);
		List<Prediction> predsV1 = new ArrayList<Prediction>();
		predsV1.add(new Prediction("v1", "s1", 1, null, 100, 99, 0, false, null, 0, 0.0f, true));
		predsV1.add(new Prediction("v1", "s2", 2, null, 200, 99, 0, false, null, 0, 0.0f, true));
		predsV1.add(new Prediction("v1", "s1", 1, null, 300, 99, 0, false, null, 0, 0.0f, true));
		testManager.updatePredictions(null, predsV1);
		
		List<Prediction> predsForS1 = testManager.getPredictions(null, "s1");
		System.err.println("after v1 predsForS1=" + predsForS1);
		
		List<Prediction> predsV2 = new ArrayList<Prediction>();
		predsV2.add(new Prediction("v2", "s1", 1, null, 150, 99, 0, false, null, 0, 0.0f, true));
		predsV2.add(new Prediction("v2", "s2", 2, null, 250, 99, 0, false, null, 0, 0.0f, true));
		predsV2.add(new Prediction("v2", "s1", 1, null, 350, 99, 0, false, null, 0, 0.0f, true));
		testManager.updatePredictions(null, predsV2);

		predsForS1 = testManager.getPredictions(null, "s1");
		System.err.println("after v2 predsForS1=" + predsForS1);

		List<Prediction> predsV1new = new ArrayList<Prediction>();
		predsV1new.add(new Prediction("v1", "s1", 1, null, 110, 99, 0, false, null, 0, 0.0f, true));
		predsV1new.add(new Prediction("v1", "s2", 2, null, 210, 99, 0, false, null, 0, 0.0f, true));
		predsV1new.add(new Prediction("v1", "s1", 1, null, 310, 99, 0, false, null, 0, 0.0f, true));
		testManager.updatePredictions(predsV1, predsV1new);
		
		predsForS1 = testManager.getPredictions(null, "s1");
		System.err.println("after v1 update predsForS1=" + predsForS1);

		List<Prediction> predsV3 = new ArrayList<Prediction>();
		predsV3.add(new Prediction("v3", "s1", 1, null, 120, 99, 0, false, null, 0, 0.0f, true));
		predsV3.add(new Prediction("v3", "s2", 2, null, 220, 99, 0, false, null, 0, 0.0f, true));
		predsV3.add(new Prediction("v3", "s1", 1, null, 310, 99, 0, false, null, 0, 0.0f, true));
		predsV3.add(new Prediction("v3", "s1", 1, null, 410, 99, 0, false, null, 0, 0.0f, true));
		predsV3.add(new Prediction("v3", "s1", 1, null, 510, 99, 0, false, null, 0, 0.0f, true));
		testManager.updatePredictions(null, predsV3);

		predsForS1 = testManager.getPredictions(null, "s1");
		System.err.println("after v3 predsForS1=" + predsForS1);
	}
}
