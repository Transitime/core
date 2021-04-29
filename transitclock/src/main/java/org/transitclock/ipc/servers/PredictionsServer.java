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
package org.transitclock.ipc.servers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.core.dataCache.canceledTrip.CanceledTripCache;
import org.transitclock.core.dataCache.canceledTrip.CanceledTripKey;
import org.transitclock.core.dataCache.SkippedStopsManager;
import org.transitclock.ipc.data.IpcCanceledTrip;
import org.transitclock.core.dataCache.canceledTrip.CanceledTripAndVehicleCache;
import org.transitclock.core.dataCache.PredictionDataCache;
import org.transitclock.db.structs.Location;
import org.transitclock.gtfs.StopsByLoc;
import org.transitclock.gtfs.StopsByLoc.StopInfo;
import org.transitclock.ipc.data.IpcPredictionsForRouteStopDest;
import org.transitclock.ipc.data.IpcSkippedStop;
import org.transitclock.ipc.interfaces.PredictionsInterface;
import org.transitclock.ipc.rmi.AbstractServer;
import org.transitclock.utils.IntervalTimer;
import org.transitclock.utils.Time;

import java.rmi.RemoteException;
import java.util.*;

/**
 * Implements the PredictionsInterface interface on the server side such that a
 * PredictionsInterfaceFactory can make RMI calls in order to obtain prediction
 * information. The prediction information is provided using
 * org.transitclock.ipc.data.Prediction objects.
 * 
 * @author SkiBu Smith
 * 
 */
public class PredictionsServer 
	extends AbstractServer implements PredictionsInterface {

	// Should only be accessed as singleton class
	private static PredictionsServer singleton;
	
	// The PredictionDataCache associated with the singleton.
	private PredictionDataCache predictionDataCache;
	
	private static final Logger logger = 
			LoggerFactory.getLogger(PredictionsServer.class);

	/********************** Member Functions **************************/

	/**
	 * Starts up the PredictionsServer so that RMI calls can query for
	 * predictions. This will automatically cause the object to continue to run
	 * and serve requests.
	 * 
	 * @param agencyId
	 * @param predictionDataCache
	 * @return the singleton PredictionsServer object. Usually does not need to
	 *         used since the server will be fully running.
	 */
	public static PredictionsServer start(
			String agencyId, PredictionDataCache predictionDataCache) {
		if (singleton == null) {
			singleton = new PredictionsServer(agencyId);
			singleton.predictionDataCache = predictionDataCache;
		}
		
		if (!singleton.getAgencyId().equals(agencyId)) {
			logger.error("Tried calling PredictionsServer.start() for " +
					"agencyId={} but the singleton was created for agencyId={}", 
					agencyId, singleton.getAgencyId());
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
	 * @see org.transitclock.ipc.interfaces.PredictionsInterface#get(java.lang.String, java.lang.String, int)
	 */
	@Override
	public List<IpcPredictionsForRouteStopDest> get(String routeIdOrShortName,
			String stopId, int predictionsPerStop) throws RemoteException {
		return predictionDataCache.getPredictions(routeIdOrShortName, null,
				stopId, predictionsPerStop);
	}

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.PredictionsInterface#get(java.util.List, int)
	 */
	@Override
	public List<IpcPredictionsForRouteStopDest> get(List<RouteStop> routeStops,
			int predictionsPerStop) throws RemoteException {
		return predictionDataCache.getPredictions(routeStops, predictionsPerStop);
	}

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.PredictionsInterface#getPredictionsByVehicle()
	 */
	@Override
	public List<IpcPredictionsForRouteStopDest> getAllPredictions(
			int predictionMaxFutureSecs) {
		// How far in future in absolute time should get predictions for
		long maxSystemTimeForPrediction = Core.getInstance().getSystemTime() + 
				predictionMaxFutureSecs*Time.MS_PER_SEC;

		return predictionDataCache.getAllPredictions(Integer.MAX_VALUE, 
				maxSystemTimeForPrediction);
	}

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.PredictionsInterface#getAllCanceledTrips()
	 */
	@Override
	public HashMap<String, IpcCanceledTrip> getAllCanceledTrips(){
		return CanceledTripCache.getInstance().getAll();
	}

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.PredictionsInterface#getAllSkippedStops()
	 */
	@Override
	public HashMap<String, HashSet<IpcSkippedStop>> getAllSkippedStops(){
		return SkippedStopsManager.getInstance().getAll();
	}


	// If stops are relatively close then should order routes based on route
	// order instead of distance.
	private static double DISTANCE_AT_WHICH_ROUTES_GROUPED = 80.0;
	
	/**
	 * For sorting resulting predictions so that they are by how close
	 * the stop is away, and then by route order, and then by direction.
	 * This way the most likely useful stops are displayed first.
	 */
	private static Comparator<IpcPredictionsForRouteStopDest> predsByLocComparator =
			new Comparator<IpcPredictionsForRouteStopDest>() {
		
		public int compare(IpcPredictionsForRouteStopDest pred1, 
				IpcPredictionsForRouteStopDest pred2) {
			// If route order indicates pred1 before pred2...
			int routeOrderDifference = 
					pred1.getRouteOrder() - pred2.getRouteOrder();
			if (routeOrderDifference < 0) {
				// route order indicates pred1 before pred2.
				// Pred1 should be before pred2 unless much closer to pred2
				if (pred1.getDistanceToStop() > pred2.getDistanceToStop()
						+ DISTANCE_AT_WHICH_ROUTES_GROUPED)
					return 1; // pred2 is much closer
				else
					return -1; // pred2 not much closer so use route order
			} else if (routeOrderDifference == 0) {
				// Route order indicates that pred1 order same as pred2
				return pred1.getDirectionId().compareTo(pred2.getDirectionId());
			} else {
				// Route order indicates pred1 after pred2.
				// Pred2 should be before pred1 unless much closer to pred1
				if (pred2.getDistanceToStop() > pred1.getDistanceToStop()
						+ DISTANCE_AT_WHICH_ROUTES_GROUPED)
					return -1; // pred1 is much closer
				else
					return 1; // pred1 not much closer so use route order
			}
		}
		
	};

	/* (non-Javadoc)
	 * @see org.transitclock.ipc.interfaces.PredictionsInterface#get(org.transitclock.db.structs.Location, double, int)
	 */
	@Override
	public List<IpcPredictionsForRouteStopDest> get(Location loc,
			double maxDistance, int predictionsPerStop) throws RemoteException {
		IntervalTimer timer = new IntervalTimer();
		
		// For returning the results
		List<IpcPredictionsForRouteStopDest> results = 
				new ArrayList<IpcPredictionsForRouteStopDest>();
		
		// Determine which stops are near the location
		List<StopInfo> stopInfos = StopsByLoc.getStops(loc, maxDistance);
		
		// Gather predictions for all of those stops
		for (StopInfo stopInfo : stopInfos) {
			// Get the predictions for the stop
			List<IpcPredictionsForRouteStopDest> predictionsForStop =
					predictionDataCache.getPredictions(stopInfo.routeShortName,
							stopInfo.tripPattern.getDirectionId(),
							stopInfo.stopId,
							predictionsPerStop, stopInfo.distanceToStop);
			
			// Add info from this stop to the results
			if (predictionsForStop.isEmpty()) {
				// No predictions for this stop but should still add it to 
				// results in case the user interface wants to show nearby stops
				// for routes that are not currently in service. This could be
				// useful to show messages, such as there being no service for
				// the route due to a parade.
				IpcPredictionsForRouteStopDest emptyPredsForStop = 
						new IpcPredictionsForRouteStopDest(
								stopInfo.tripPattern, stopInfo.stopId,
								stopInfo.distanceToStop);
				results.add(emptyPredsForStop);
			} else {
				// There are predictions for this stop so add them to the results
				results.addAll(predictionsForStop);
			}
		}
		
		// Sort the predictions so that nearby stops output first, stops of 
		// similar distance are output in route order, and direction "0"
		// is output first for each route.
		Collections.sort(results, predsByLocComparator);
		
		logger.info("Determined predictions for stops near {}. Took {} msec",
				loc, timer.elapsedMsec());
		
		// Return all of the predictions
		return results;
	}
	
}
