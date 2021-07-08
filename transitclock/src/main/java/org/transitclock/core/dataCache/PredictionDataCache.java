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
package org.transitclock.core.dataCache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.BooleanConfigValue;
import org.transitclock.core.PredictionGeneratorDefaultImpl;
import org.transitclock.core.VehicleState;
import org.transitclock.db.structs.Route;
import org.transitclock.db.structs.Stop;
import org.transitclock.db.structs.Trip;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.ipc.data.IpcPrediction;
import org.transitclock.ipc.data.IpcPredictionsForRouteStopDest;
import org.transitclock.ipc.interfaces.PredictionsInterface.RouteStop;
import org.transitclock.utils.MapKey;
import org.transitclock.utils.Time;

import static org.transitclock.core.PredictionGeneratorDefaultImpl.isHistoricalPredictionForFutureStop;

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
			new PredictionDataCache();
	
	protected static BooleanConfigValue returnArrivalPredictionForEndOfTrip = new BooleanConfigValue("transitclock.prediction.returnArrivalPredictionForEndOfTrip", 
			false,
			"This set to false will not return arrival predictions of the last stop on a trip.");
	
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
	
	/**
	 * Returns the current time. Can be based on the systems clock
	 * but when in playback mode will be based on last AVL report.
	 * @return
	 */
	private long getSystemTime() {
		return Core.getInstance().getSystemTime();
	}
	
	/**
	 * Returns copy of the PredictionsForRouteStop object. This is the low-level
	 * method that actually gets the appropriate predictions. A clone is used so
	 * that it can be accessed as needed without worrying about another thread
	 * writing to it. The list of predictions should be relatively small so
	 * cloning is not very costly. And this way the caller of this method
	 * doesn't have to synchronize or such.
	 * 
	 * @param routeIdOrShortName  
	 *            route_id or route_short_name, or null to specify all routes
	 *            for stop.
	 * @param directionId
	 *            Optional parameter for specifying that only want predictions
	 *            for a specific directionId. Set to null if want predictions
	 *            for all directions.
	 * @param stopIdOrCode
	 *            stop_id or stop_code
	 * @param maxPredictionsPerStop
	 * @param distanceToStop
	 *            For when getting predictions by location
	 * @return List of IpcPredictionsForRouteStopDest. Can be empty but will not
	 *         be null.
	 */
	public List<IpcPredictionsForRouteStopDest> getPredictions(
			String routeIdOrShortName, String directionId, String stopIdOrCode,
			int maxPredictionsPerStop, double distanceToStop) {
		DbConfig dbConfig = Core.getInstance().getDbConfig();
		
		// Determine the routeShortName so can be used for maps in
		// the low-level methods of this class. Can be null to specify
		// getting data for all routes.
		String routeShortName = routeIdOrShortName;
		if (routeIdOrShortName != null) {
			// See if it is route ID or a short name
			Route route = dbConfig.getRouteById(routeIdOrShortName);
			if (route != null)
				// routeIdOrShortName was route ID so get the short name
				routeShortName = route.getShortName();
			else {
				// A routeIdOrShortName was specified but is wasn't a route ID
				if (dbConfig.getRouteByShortName(routeIdOrShortName) == null) {
					// That route doesn't exist so error
					throw new IllegalArgumentException("Route "
							+ routeIdOrShortName + " not valid");
				}
			}
		}
		
		// Determine the stop ID since can pass in stopIdOrCode
		String stopId = stopIdOrCode;
		if (dbConfig.getStop(stopIdOrCode) == null) {
			try {
				Integer stopCode = Integer.parseInt(stopIdOrCode);
				Stop stop = Core.getInstance().getDbConfig().getStop(stopCode);
				
				// If no such stop then complain
				if (stop == null)
					throw new IllegalArgumentException("Stop " + stopIdOrCode 
							+ " not valid");
				
				stopId = stop.getId();
			} catch (NumberFormatException e) {
				// The stopIdOrCode was not an integer so give up
				throw new IllegalArgumentException("Stop " + stopIdOrCode 
						+ " not a valid integer");
			}
		}
		
		// Get the predictions from the map
		List<IpcPredictionsForRouteStopDest> predictionsForRouteStop = 
				getPredictionsForRouteStop(routeShortName, stopId);
		
		// Remove old predictions so that they are not provided through the 
		// API and such
		for (IpcPredictionsForRouteStopDest preds : predictionsForRouteStop) {
			preds.removeExpiredPredictions(getSystemTime());
		}

		// Want to limit predictions to max time in future since if using
		// schedule based predictions then generating predictions far into the 		
		// future.
		long maxPredictionEpochTime =
						getSystemTime()
						+ PredictionGeneratorDefaultImpl
								.getMaxPredictionsTimeSecs()
						* Time.SEC_IN_MSECS;
				
		// Want to filter out arrivals at terminal if also getting departures 
		// for that stop. Otherwise if user selects a terminal stop they could 
		// see both departures and (useless) arrivals and be confused with too 
		// much info. But if only have arrivals then should provide that info 
		// because it could be useful to user.
		boolean endOfTripPredFound = false;
		boolean nonEndOfTripPredFound = false;
		for (IpcPredictionsForRouteStopDest predictions : predictionsForRouteStop) {
			for (IpcPrediction preds : predictions.getPredictionsForRouteStop()) {
				if (preds.isAtEndOfTrip())
					endOfTripPredFound = true;
				else
					nonEndOfTripPredFound = true;
			}
		}
		/* Is this the best place to filter out predictions. Would it be better to allow the consumer filter? */
		boolean shouldFilterOutEndOfTripPreds = 
				(endOfTripPredFound && nonEndOfTripPredFound && !returnArrivalPredictionForEndOfTrip.getValue());
		
		// Make a copy of the prediction objects so that they cannot be
		// modified by another thread while they are being accessed. This
		// is important because when the predictions are modified they are
		// temporarily not coherent. 
		List<IpcPredictionsForRouteStopDest> clonedPredictions = 
				new ArrayList<IpcPredictionsForRouteStopDest>(
						predictionsForRouteStop.size());
		for (IpcPredictionsForRouteStopDest predictions : predictionsForRouteStop) {
			// If supposed to return only predictions for specific direction and 
			// the current predictions are for the wrong direction then simply
			// continue to the next predictions.
			if (directionId != null 
					&& !directionId.equals(predictions.getDirectionId()))
				continue;
			
			// If determined that should filter out end of trip predictions, 
			// do so if all of the predictions for this stop are end of trip 
			// predictions. Yes, this is a bit complicated.
			if (shouldFilterOutEndOfTripPreds) {
				if (predictions.getPredictionsForRouteStop().size() > 0) {
					boolean allPredsForEndOfTrip = true;
					for (IpcPrediction preds : predictions
							.getPredictionsForRouteStop()) {
						if (!preds.isAtEndOfTrip()) {
							allPredsForEndOfTrip = false;
							continue;
						}
					}
					if (allPredsForEndOfTrip)
						continue;
				}
			}
			
			// Direction ID is OK so clone prediction and add to list
			IpcPredictionsForRouteStopDest clone =
					predictions.getClone(maxPredictionsPerStop,
							maxPredictionEpochTime, PredictionGeneratorDefaultImpl.getTerminatePredictionsAtTripEnd(), distanceToStop);
			clonedPredictions.add(clone);
		}
		
		// If no predictions should still return a IpcPredictionsForRouteStopDest
		// object so that the client can get route, stop, and direction info to
		// display in the UI.
		// except if no routeShortName - Ipc requires this!
		if (clonedPredictions.size() == 0  && routeShortName != null) {
			IpcPredictionsForRouteStopDest pred =
					new IpcPredictionsForRouteStopDest(routeShortName,
							directionId, stopIdOrCode, distanceToStop);
			clonedPredictions.add(pred);
		}
		
		// Will frequently get info for trip patterns that are not currently in
		// service. If only have no predictions, then that should be returned. 
		// But if do have predictions for a destination then should filter out
		// the destinations that don't have any predictions so that useful
		// info doesn't clutter the screen. Since need to make sure that don't
		// modify clonedPredictions while removing elements need to be careful
		// about how go about doing this.
		boolean hasDestinationWithPredictions = false;
		for (IpcPredictionsForRouteStopDest pred : clonedPredictions) {
			// If at least one of the destinations has predictions...
			if (pred.getPredictionsForRouteStop().size() > 0) {
				hasDestinationWithPredictions = true;
				break;
			}
		}
		if (hasDestinationWithPredictions) {
			// Filter out destination info where there are no predictions.
			// Use iterator since possibly removing elements in loop
			Iterator<IpcPredictionsForRouteStopDest> iterator =
					clonedPredictions.iterator();
			while (iterator.hasNext()) {
				IpcPredictionsForRouteStopDest predsForRouteStopDest =
						iterator.next();
				// If found destination with no predictions remove it
				if (predsForRouteStopDest.getPredictionsForRouteStop()
						.isEmpty()) {
					iterator.remove();
				}
			}
		}
		
		// Return the safe cloned predictions
		return clonedPredictions;
	}

	
	/**
	 * Returns copy of the PredictionsForRouteStop object. A clone is used so
	 * that it can be accessed as needed without worrying about another thread
	 * writing to it. The list of predictions should be relatively small so
	 * cloning is not very costly. And this way the caller of this method
	 * doesn't have to synchronize or such.
	 * 
	 * @param routeIdOrShortName
	 *            route_id or route_short_name, or null to specify all routes
	 *            for stop.
	 * @param directionId
	 *            Optional parameter for specifying that only want predictions
	 *            for a specific directionId. Set to null if want predictions
	 *            for all directions.
	 * @param stopIdOrCode
	 *            stop_id or stop_code
	 * @param maxPredictionsPerStop
	 * @return List of IpcPredictionsForRouteStopDest. Can be empty but will not
	 *         be null.
	 */
	public List<IpcPredictionsForRouteStopDest> getPredictions(
			String routeIdOrShortName, String directionId, String stopIdOrCode,
			int maxPredictionsPerStop) {
		return getPredictions(routeIdOrShortName, directionId, stopIdOrCode,
				maxPredictionsPerStop, Double.NaN);
	}
	
	/**
	 * Returns copy of all predictions currently associated with the stop. Uses
	 * routeShortName instead of the GTFS routeId to identify the stop.
	 * 
	 * @param routeIdOrShortName
	 * @param directionId
	 * @param stopId
	 * @return List of IpcPredictionsForRouteStopDest. Can be empty but will not
	 *         be null.
	 */
	public List<IpcPredictionsForRouteStopDest> getPredictions(
			String routeIdOrShortName, String directionId, String stopId) {
		return getPredictions(routeIdOrShortName, directionId, stopId,
				Integer.MAX_VALUE);
	}
	
	/**
	 * Returns copy of all predictions currently associated with the stop. Uses
	 * routeShortName instead of the GTFS routeId to identify the stop.
	 * 
	 * @param routeIdOrShortName
	 * @param stopId
	 * @return List of IpcPredictionsForRouteStopDest. Can be empty but will not
	 *         be null.
	 */
	public List<IpcPredictionsForRouteStopDest> getPredictions(
			String routeIdOrShortName, String stopId) {
		return getPredictions(routeIdOrShortName, null, stopId, Integer.MAX_VALUE);
	}

	/**
	 * Returns copy of List<PredictionsForRouteStop> objects for each route/stop
	 * specified.
	 * 
	 * @param routeStops
	 *            Specified using route_short_name or route_id, and stop_id or
	 *            stop_code
	 * @param predictionsPerStop
	 * @return List of IpcPredictionsForRouteStopDest. Can be empty but will not
	 *         be null.
	 */
	public List<IpcPredictionsForRouteStopDest> getPredictions(
			List<RouteStop> routeStops, int predictionsPerStop) {
		List<IpcPredictionsForRouteStopDest> listOfPredictions = 
				new ArrayList<IpcPredictionsForRouteStopDest>();
		for (RouteStop routeStop : routeStops) {
			List<IpcPredictionsForRouteStopDest> predsForStop =
					getPredictions(routeStop.getRouteIdOrShortName(), null,
							routeStop.getStopIdOrCode(), predictionsPerStop);
			for (IpcPredictionsForRouteStopDest predictions : predsForStop)
				listOfPredictions.add(predictions);
		}
		return listOfPredictions;
	}
	
	/**
	 * Returns copy of all predictions currently associated for each route/stop
	 * specified.
	 * 
	 * @param routeStops
	 * @return List of IpcPredictionsForRouteStopDest. Can be empty but will not
	 *         be null.
	 */
	public List<IpcPredictionsForRouteStopDest> getPredictions(
			List<RouteStop> routeStops) {
		return getPredictions(routeStops, Integer.MAX_VALUE);
	}
	
	/**
	 * Gets predictions for specified route/stop and returns the first one for
	 * the specified vehicle.
	 * 
	 * @param vehicleId
	 * @param routeShortName
	 * @param stopId
	 * @return IpcPrediction for the specified vehicle/route/stop or null if
	 *         there are no such predictions
	 */
	public IpcPrediction getPredictionForVehicle(String vehicleId,
			String routeShortName, String stopId) {
		// Get all the predictions for the specified stop. Get a bunch (5) 
		// of predictions in case there are a bunch of vehicles that will be
		// leaving the stop.
		List<IpcPredictionsForRouteStopDest> predsList = getPredictions(
				routeShortName, null, stopId, 5 /* maxPredictionsPerStop */);
		
		// Go through all the predictions and find the ones for the specified vehicle
		for (IpcPredictionsForRouteStopDest predsForRouteStop : predsList) {
			for (IpcPrediction preds : predsForRouteStop.getPredictionsForRouteStop()) {
				if (preds.getVehicleId().equals(vehicleId)) {
					// Found prediction for specified vehicle so return it
					return preds;
				}
			}
		}
		
		// Couldn't find predictions for the vehicle at the route/stop
		return null;
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
	 * @return List of IpcPredictionsForRouteStopDest. Can be empty but will not
	 *         be null.
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
				// with the historical prediction / future stop checks we need to clean up the
				// cache more often
				predictionForRouteStopDest.removeExpiredPredictions(getSystemTime());
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
	 * Updates predictions in the cache that are associated with a vehicle.
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
		
		// Can have several predictions for a route/stop/dest for a vehicle if
		// the route is a relatively short loop. And if have unscheduled
		// trips then won't have a unique trip identifier. Therefore to
		// atomically update the predictions for this vehicle for this
		// route/stop need to delete all predictions for the vehicle
		// and add all new ones in one synchronized operation.
		// First step is to group the new predictions by route/stop/dest so
		// can deal with them all at once. Those are put into
		// newPredsForVehicleByRouteStopDestMap.
		Map<MapKey, List<IpcPrediction>> newPredsForVehicleByRouteStopDestMap =
				new HashMap<MapKey, List<IpcPrediction>>();
		
		for (IpcPrediction newPrediction : newPredictionsForVehicle) {
			MapKey key = new MapKey(newPrediction.getRouteShortName(),
							newPrediction.getStopId(), 
							newPrediction.getTrip().getHeadsign());
			List<IpcPrediction> predsForRouteStopDestList = 
					newPredsForVehicleByRouteStopDestMap.get(key);
			if (predsForRouteStopDestList == null) {
				predsForRouteStopDestList = new ArrayList<IpcPrediction>();
				newPredsForVehicleByRouteStopDestMap
						.put(key, predsForRouteStopDestList);
			}
			predsForRouteStopDestList.add(newPrediction);
		}
		
		// Go through the new predictions grouped by route/stop/destination and
		// process them.
		for (List<IpcPrediction> newPredsForVehicleForRouteStopDest : 
				newPredsForVehicleByRouteStopDestMap.values()) {
			updatePredictionsForVehicle(newPredsForVehicleForRouteStopDest);
		}
		
		// Remove old predictions that are not in newPredictionsForVehicle 
		if (oldPredictionsForVehicle != null) {
			for (IpcPrediction oldPrediction : oldPredictionsForVehicle) {
				// If there is no new prediction for the old prediction 
				// route/stop...
				MapKey key = new MapKey(oldPrediction.getRouteShortName(),
						oldPrediction.getStopId(), 
						oldPrediction.getTrip().getHeadsign());
				if (newPredsForVehicleByRouteStopDestMap.get(key) == null) {
					long currentTime = getSystemTime();
					// here we hang onto old prediction if its scheduled arrival is in the future
					if (!isHistoricalPredictionForFutureStop(oldPrediction, currentTime)) {
						// Remove the old prediction
						removePrediction(oldPrediction);
					}
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
				newPredsForVehicleForRouteStopDest, getSystemTime());
	}
	
	/**
	 * Returns List of PredictionsForRouteStop objects associated with the
	 * specified route/stop. Returns a list because there is a separate
	 * PredictionsForRouteStop for each destination and some route directions
	 * have multiple destinations.
	 * 
	 * @param routeShortName
	 *            The route short name. Set to null to get predictions for all
	 *            routes for the stop.
	 * @param stopId
	 * @return list of predictions. Can be empty array but never null.
	 */
	private List<IpcPredictionsForRouteStopDest> getPredictionsForRouteStop(
			String routeShortName, String stopId) {
		List<IpcPredictionsForRouteStopDest> predictionsForStop;
		
		// If routeShortName specified then get predictions for that route.
		// If not then get predictions for all routes that serve the stop.
		if (routeShortName != null) {
			// Determine the predictions for all destinations for the route/stop
			MapKey key = MapKey.create(routeShortName, stopId);
			predictionsForStop = predictionsMap.get(key);

			if (predictionsForStop == null) {
				// No predictions so return empty array instead of null
				predictionsForStop = 
						new ArrayList<IpcPredictionsForRouteStopDest>(1);
				
				// Need to update the predictions map with the 
				// predictionsForStop list for this route/stop so that
				// when this list of predictions is updated it will be
				// kept around.
				predictionsMap.putIfAbsent(key, predictionsForStop);
			}
		} else {
			// No route specified so get predictions for all routes for the stop
			predictionsForStop =
					new ArrayList<IpcPredictionsForRouteStopDest>();
			Collection<Route> routes = 
					Core.getInstance().getDbConfig().getRoutesForStop(stopId);
			for (Route route : routes) {
				MapKey key = MapKey.create(route.getShortName(), stopId);
				 List<IpcPredictionsForRouteStopDest> predsForRoute = 
						 predictionsMap.get(key);
				 if (predsForRoute != null)
					 predictionsForStop.addAll(predsForRoute);
			}
		}		

		return predictionsForStop;
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
				if (preds.getHeadsign() == null
						|| preds.getHeadsign().equals(trip.getHeadsign()))
					return preds;
			}

			// The PredictionsForRouteStopDest was not yet created for the
			// route/stop/destination so create it now and add it to list
			// of PredictionsForRouteStopDest objects for the route/stop.
			IpcPredictionsForRouteStopDest preds = 
					new IpcPredictionsForRouteStopDest(trip, stopId, Double.NaN);
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
