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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.core.dataCache.PredictionDataCache;
import org.transitime.ipc.data.IpcPredictionsForRouteStopDest;
import org.transitime.ipc.interfaces.PredictionsInterface;
import org.transitime.ipc.rmi.AbstractServer;
import org.transitime.utils.Time;

/**
 * Implements the PredictionsInterface interface on the server side such that a
 * PredictionsInterfaceFactory can make RMI calls in order to obtain prediction
 * information. The prediction information is provided using
 * org.transitime.ipc.data.Prediction objects.
 * 
 * @author SkiBu Smith
 * 
 */
public class PredictionsServer 
	extends AbstractServer implements PredictionsInterface {

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
	public List<IpcPredictionsForRouteStopDest> get(String routeShortName, String stopId,
			int predictionsPerStop) throws RemoteException {
		return predictionManager.getPredictions(routeShortName, stopId,
				predictionsPerStop);
	}

	/* (non-Javadoc)
	 * @see org.transitime.ipc.interfaces.PredictionsInterface#getUsingRouteId(java.lang.String, java.lang.String, int)
	 */
	@Override
	public List<IpcPredictionsForRouteStopDest> getUsingRouteId(String routeId, String stopId,
			int predictionsPerStop) throws RemoteException {
		return predictionManager.getPredictionsUsingRouteId(routeId, stopId,
				predictionsPerStop);
	}

	/* (non-Javadoc)
	 * @see org.transitime.ipc.interfaces.PredictionsInterface#get(java.util.List, int)
	 */
	@Override
	public List<IpcPredictionsForRouteStopDest> get(List<RouteStop> routeStops,
			int predictionsPerStop) throws RemoteException {
		return predictionManager.getPredictions(routeStops, predictionsPerStop);
	}

	/* (non-Javadoc)
	 * @see org.transitime.ipc.interfaces.PredictionsInterface#getUsingRouteId(java.util.List, int)
	 */
	@Override
	public List<IpcPredictionsForRouteStopDest> getUsingRouteId(List<RouteStop> routeStops,
			int predictionsPerStop) throws RemoteException {
		return predictionManager.getPredictionsUsingRouteId(routeStops, 
				predictionsPerStop);
	}

	/* (non-Javadoc)
	 * @see org.transitime.ipc.interfaces.PredictionsInterface#getPredictionsByVehicle()
	 */
	@Override
	public List<IpcPredictionsForRouteStopDest> getAllPredictions(int predictionMaxFutureSecs) {
		// How far in future in absolute time should get predictions for
		long maxSystemTimeForPrediction = predictionManager.getSystemTime() + 
				predictionMaxFutureSecs*Time.MS_PER_SEC;

		return predictionManager.getAllPredictions(Integer.MAX_VALUE, maxSystemTimeForPrediction);
	}
	
}
