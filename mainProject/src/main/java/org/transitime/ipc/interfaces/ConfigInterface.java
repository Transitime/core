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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;

import org.transitime.ipc.data.IpcBlock;
import org.transitime.ipc.data.IpcRoute;
import org.transitime.ipc.data.IpcRouteSummary;
import org.transitime.ipc.data.IpcStopsForRoute;
import org.transitime.ipc.data.IpcTrip;
import org.transitime.ipc.data.IpcTripPattern;

/**
 *
 *
 * @author SkiBu Smith
 *
 */
public interface ConfigInterface extends Remote {

	/**
	 * Obtains list of routes configured.
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public Collection<IpcRouteSummary> getRoutes() throws RemoteException;

	/**
	 * Obtains data for single route.
	 * 
	 * @param routeShortName
	 *            Specifies which route to provide data for. routeShortName is
	 *            used instead of routeId since routeIds unfortunately often
	 *            change when there is a schedule change.
	 * @param stopId
	 *            If want UI to highlight the remaining stops and paths left in
	 *            trip then stopId is used to return which stops remain in trip.
	 *            If this additional info not needed for UI then null can be
	 *            specified.
	 * @param tripPatternId
	 *            If want UI to highlight the remaining stops and paths left in
	 *            trip then stopId is used to determine which trip pattern to
	 *            highlight. If this additional info not needed for UI then null
	 *            can be specified.
	 * @return
	 * @throws RemoteException
	 */
	public IpcRoute getRoute(String routeShortName, String stopId,
			String tripPatternId) throws RemoteException;
	
	/**
	 * Returns stops for each direction for a route.
	 * 
	 * @param routeShortName
	 *            Specifies which route to provide data for. routeShortName is
	 *            used instead of routeId since routeIds unfortunately often
	 *            change when there is a schedule change.
	 * @return
	 * @throws RemoteException
	 */
	public IpcStopsForRoute getStops(String routeShortName)  
			throws RemoteException;
	
	/**
	 * Returns block info for specified blockId and serviceId. Includes all trip
	 * and trip pattern info associated with the block.
	 * 
	 * @param blockId
	 * @param serviceId
	 * @return
	 * @throws RemoteException
	 */
	public IpcBlock getBlock(String blockId, String serviceId) 
			throws RemoteException;
	
	/**
	 * Returns trip info for specified tripId. Includes all trip pattern info
	 * associated with the trip.
	 * 
	 * @param tripId
	 * @return
	 * @throws RemoteException
	 */
	public IpcTrip getTrip(String tripId) 
			throws RemoteException;
	
	/**
	 * Returns trip patterns for specified routeShortName.
	 * 
	 * @param routeShortName
	 * @return
	 * @throws RemoteException
	 */
	public List<IpcTripPattern> getTripPatterns(String routeShortName) 
			throws RemoteException;

	/**
	 * Returns trip patterns for specified routeId.
	 * 
	 * @param routeId
	 * @return
	 * @throws RemoteException
	 */
	public List<IpcTripPattern> getTripPatternsByRouteId(String routeId) 
			throws RemoteException;

}
