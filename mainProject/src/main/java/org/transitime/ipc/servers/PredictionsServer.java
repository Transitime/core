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
package org.transitime.ipc.servers;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.core.VehicleState;
import org.transitime.core.VehicleStateManager;
import org.transitime.core.dataCache.PredictionDataCache;
import org.transitime.ipc.data.Prediction;
import org.transitime.ipc.interfaces.PredictionsInterface;
import org.transitime.ipc.rmi.AbstractServer;
import org.transitime.utils.Time;

/**
 * Implements the PredictionsInterface interface on the server side such that a 
 * PredictionsInterfaceFactory can make RMI calls in order to obtain prediction 
 * information.
 *   
 * @author SkiBu Smith
 * 
 */
public class PredictionsServer extends AbstractServer implements PredictionsInterface {

	// Should only be accessed as singleton class
	private static PredictionsServer singleton;
	
	// The PredictionDataCache associated with the singleton.
	private PredictionDataCache predictionManager;
	
	private static final Logger logger = 
			LoggerFactory.getLogger(PredictionsServer.class);

	/********************** Member Functions **************************/

	/**
	 * Starts up the PredictionsServer so that RMI calls can query for
	 * predictions. This will automatically cause the object to continue to run
	 * and serve requests.
	 * 
	 * @param projectId
	 * @param predictionManager
	 * @return the singleton PredictionsServer object. Usually does not need to
	 *         used since the server will be fully running.
	 */
	public static PredictionsServer start(
			String projectId, PredictionDataCache predictionManager) {
		if (singleton == null) {
			singleton = new PredictionsServer(projectId);
			singleton.predictionManager = predictionManager;
		}
		
		if (!singleton.getProjectId().equals(projectId)) {
			logger.error("Tried calling PredictionsServer.getInstance() for " +
					"projectId={} but the singleton was created for projectId={}", 
					projectId, singleton.getProjectId());
			return null;
		}
		
		return singleton;
	}
	
	/*
	 * Constructor. Made private so that can only be instantiated by
	 * get(). Doesn't actually do anything since all the work is done in
	 * the superclass constructor.
	 * 
	 * @param projectId
	 *            for registering this object with the rmiregistry
	 */
	private PredictionsServer(String projectId) {
		super(projectId, PredictionsInterface.class.getSimpleName());
	}

	/* (non-Javadoc)
	 * @see org.transitime.ipc.interfaces.PredictionsInterface#get(java.lang.String, java.lang.String, int)
	 */
	@Override
	public List<Prediction> get(String routeId, String stopId,
			int predictionsPerStop) throws RemoteException {
		return predictionManager.getPredictions(routeId, stopId,
				predictionsPerStop);
	}

	/* (non-Javadoc)
	 * @see org.transitime.ipc.interfaces.PredictionsInterface#getUsingRouteId(java.lang.String, java.lang.String, int)
	 */
	@Override
	public List<Prediction> getUsingRouteId(String routeId, String stopId,
			int predictionsPerStop) throws RemoteException {
		return predictionManager.getPredictionsUsingRouteId(routeId, stopId,
				predictionsPerStop);
	}

	/* (non-Javadoc)
	 * @see org.transitime.ipc.interfaces.PredictionsInterface#get(java.util.List, int)
	 */
	@Override
	public List<List<Prediction>> get(List<RouteStop> routeStops,
			int predictionsPerStop) throws RemoteException {
		return predictionManager.getPredictions(routeStops, predictionsPerStop);
	}

	/* (non-Javadoc)
	 * @see org.transitime.ipc.interfaces.PredictionsInterface#getUsingRouteId(java.util.List, int)
	 */
	@Override
	public List<List<Prediction>> getUsingRouteId(List<RouteStop> routeStops,
			int predictionsPerStop) throws RemoteException {
		return predictionManager.getPredictionsUsingRouteId(routeStops, 
				predictionsPerStop);
	}

	/* (non-Javadoc)
	 * @see org.transitime.ipc.interfaces.PredictionsInterface#getPredictionsByVehicle()
	 */
	@Override
	public List<List<Prediction>> getPredictionsByVehicle(
			int predictionMaxFutureSecs)
			throws RemoteException {
		// The predictions that will be returned by this method
		List<List<Prediction>> predictions = new ArrayList<List<Prediction>>();
		
		// How far in future in absolute time should get predictions for
		long maxSystemTimeForPrediction = predictionManager.getSystemTime() + 
				predictionMaxFutureSecs*Time.MS_PER_SEC;
		
		// Get vehicle state info, including predictions, for all vehicles
		Collection<VehicleState> vehiclesState = 
				VehicleStateManager.getInstance().getVehiclesState();
		
		// Go through all predictions and add them to the list that will
		// be returned by this method.
		for (VehicleState vehicleState : vehiclesState) {
			List<Prediction> predictionsForVehicle =
					vehicleState.getPredictions();
			
			// If there are no predictions for this vehicle then continue
			// on to the next one.
			if (predictionsForVehicle == null)
				continue;
			
			// Only use predictions that are not to far in future
			List<Prediction> filteredPredictions = 
					new ArrayList<Prediction>(predictionsForVehicle.size());
			for (Prediction pred : predictionsForVehicle) {
				if (pred.getTime() < maxSystemTimeForPrediction) {
					filteredPredictions.add(pred);
				} else {
					// Went too far into future. Since rest of predictions for
					// this vehicle will be even further into the future we
					// are done finding the filtered ones for this vehicle.
					// Therefore break out of loop.
					break;
				}
			}
			
			// Add the filtered list of predictions for the vehicle
			predictions.add(filteredPredictions);
		}
		return predictions;
	}
	
}
