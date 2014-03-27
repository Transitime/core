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
import org.transitime.ipc.data.Vehicle;

/**
 * Defines the RMI interface used for obtaining vehicle information.
 * 
 * @author SkiBu Smith
 * 
 */
public interface VehiclesInterface extends Remote {

	/**
	 * Gets from server Vehicle info for specified vehicle.
	 * 
	 * @param vehicleId
	 *            ID of vehicle to get data for
	 * @return info for specified vehicle
	 * @throws RemoteException
	 */
	public Vehicle get(String vehicleId) throws RemoteException;

	/**
	 * Gets from server Vehicle info for all vehicles.
	 * 
	 * @return Collection of Vehicle objects
	 * @throws RemoteException
	 */
	public Collection<Vehicle> get() throws RemoteException;

	/**
	 * Gets from server Vehicle info for all vehicles currently. associated with
	 * route.
	 * 
	 * @param routeShortName
	 *            Specifies which route to get Vehicle data for
	 * @return Collection of Vehicle objects
	 * @throws RemoteException
	 */
	public Collection<Vehicle> getForRoute(String routeShortName) 
			throws RemoteException;

	/**
	 * Gets from server Vehicle info for all vehicles currently. associated with
	 * route.
	 * 
	 * @param routeId
	 *            Specifies which route to get Vehicle data for
	 * @return Collection of Vehicle objects
	 * @throws RemoteException
	 */
	public Collection<Vehicle> getForRouteUsingRouteId(String routeId) 
			throws RemoteException;

	/**
	 * Gets from server Vehicle info for vehicles specified by vehicles
	 * parameter.
	 * 
	 * @param vehicleIds
	 *            Array of vehicle IDs to get Vehicle data for.
	 * @return Collection of Vehicle objects
	 * @throws RemoteException
	 */
	public Collection<Vehicle> get(String[] vehicleIds) 
			throws RemoteException;
}
