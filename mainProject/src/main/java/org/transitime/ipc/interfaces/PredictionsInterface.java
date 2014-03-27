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
package org.transitime.ipc.interfaces;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import org.transitime.ipc.data.Prediction;

/**
 * Defines the RMI interface used for obtaining predictions. 
 * 
 * @author SkiBu Smith
 *
 */
public interface PredictionsInterface extends Remote {

	/**
	 * This class is for use as key into hash maps that store prediction data.
	 */
	public static class RouteStop implements Serializable{
		private final String routeIdOrShortName;
		private final String stopId;
		
		private static final long serialVersionUID = -4558270897399749207L;

		public RouteStop(String routeIdOrShortName, String stopId) {
			this.routeIdOrShortName = routeIdOrShortName;
			this.stopId = stopId;
		}

		public String getRouteIdOrShortName() {
			return routeIdOrShortName;
		}

		public String getStopId() {
			return stopId;
		}

		@Override
		public String toString() {
			return "RouteStop [routeIdOrShortName=" + routeIdOrShortName + ", stopId=" + stopId + "]";
		}
	}
	
	/**
	 * Returns list of current predictions for the specified route/stop.
	 * 
	 * @param routeShortName
	 * @param stopId
	 * @param predictionsPerStop
	 *            Max number of predictions to return for route/stop
	 * @return List of predictions for the route/stop
	 * @throws RemoteException
	 */
	public List<Prediction> get(
			String routeShortName, String stopId, int predictionsPerStop) 
				throws RemoteException;
	
	/**
	 * Returns list of current predictions for the specified route/stop.
	 * 
	 * @param routeId
	 * @param stopId
	 * @param predictionsPerStop
	 *            Max number of predictions to return for route/stop
	 * @return List of predictions for the route/stop
	 * @throws RemoteException
	 */
	public List<Prediction> getUsingRouteId(
			String routeId, String stopId, int predictionsPerStop) 
				throws RemoteException;
	
	/**
	 * For each route/stop specified returns a list of predictions for that
	 * stop. Since expensive RMI calls are being done this method is much
	 * more efficient for obtaining multiple predictions then if a separate
	 * get() call is done for each route/stop.
	 * 
	 * @param routeStops
	 *            List of route/stops to return predictions for
	 * @param predictionsPerStop
	 *            Max number of predictions to return per route/stop
	 * @return List of List of PredictionsInterface for the route/stop.
	 * @throws RemoteException
	 */
	public List<List<Prediction>> get(
			List<RouteStop> routeStops,	int predictionsPerStop)
				throws RemoteException;
	
	/**
	 * For each route/stop specified returns a list of predictions for that
	 * stop. Since expensive RMI calls are being done this method is much
	 * more efficient for obtaining multiple predictions then if a separate
	 * get() call is done for each route/stop.
	 * 
	 * @param routeStops
	 *            List of route/stops to return predictions for
	 * @param predictionsPerStop
	 *            Max number of predictions to return per route/stop
	 * @return List of List of PredictionsInterface for the route/stop.
	 * @throws RemoteException
	 */
	public List<List<Prediction>> getUsingRouteId(
			List<RouteStop> routeStops,	int predictionsPerStop)
				throws RemoteException;
	
	/**
	 * Returns all predictions, grouped by vehicle. This is intended for
	 * clients such as the GTFS-RT vehicle update feed that outputs all
	 * predictions by trip.
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public List<List<Prediction>> getPredictionsByVehicle(int predictionMaxFutureSecs) throws RemoteException;
}
