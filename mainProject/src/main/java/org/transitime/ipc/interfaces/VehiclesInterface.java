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

import org.transitime.ipc.data.IpcExtVehicle;
import org.transitime.ipc.data.IpcVehicle;

/**
 * Defines the RMI interface used for obtaining vehicle information.
 * 
 * @author SkiBu Smith
 * 
 */
public interface VehiclesInterface extends Remote {

	/**
	 * Gets from server IpcVehicle info for all vehicles.
	 * 
	 * @return Collection of Vehicle objects
	 * @throws RemoteException
	 */
	public Collection<IpcVehicle> get() throws RemoteException;

	/**
	 * Gets from server IpcSiriVehicle info for all vehicles.
	 * 
	 * @return Collection of Vehicle objects
	 * @throws RemoteException
	 */
	public Collection<IpcExtVehicle> getExt() throws RemoteException;

	/**
	 * Gets from server IpcVehicle info for specified vehicle.
	 * 
	 * @param vehicleId
	 *            ID of vehicle to get data for
	 * @return info for specified vehicle
	 * @throws RemoteException
	 */
	public IpcVehicle get(String vehicleId) throws RemoteException;

	/**
	 * Gets from server IpcExtVehicle info for specified vehicle.
	 * 
	 * @param vehicleId
	 *            ID of vehicle to get data for
	 * @return info for specified vehicle
	 * @throws RemoteException
	 */
	public IpcExtVehicle getExt(String vehicleId) throws RemoteException;

	/**
	 * Gets from server IpcVehicle info for vehicles specified by vehicles
	 * parameter.
	 * 
	 * @param vehicleIds
	 *            Collection of vehicle IDs to get Vehicle data for.
	 * @return Collection of Vehicle objects
	 * @throws RemoteException
	 */
	public Collection<IpcVehicle> get(List<String> vehicleIds) 
			throws RemoteException;

	/**
	 * Gets from server IpcSiriVehicle info for vehicles specified by vehicles
	 * parameter.
	 * 
	 * @param vehicleIds
	 *            Collection of vehicle IDs to get Vehicle data for.
	 * @return Collection of Vehicle objects
	 * @throws RemoteException
	 */
	public Collection<IpcExtVehicle> getExt(List<String> vehicleIds) 
			throws RemoteException;

	/**
	 * Gets from server IpcVehicle info for all vehicles currently. associated
	 * with route.
	 * 
	 * @param routeIdOrShortName
	 *            Specifies which route to get Vehicle data for
	 * @return Collection of Vehicle objects
	 * @throws RemoteException
	 */
	public Collection<IpcVehicle> getForRoute(String routeIdOrShortName) 
			throws RemoteException;

	/**
	 * Gets from server IpcExtVehicle info for all vehicles currently. associated
	 * with route.
	 * 
	 * @param routeIdOrShortName
	 *            Specifies which route to get Vehicle data for
	 * @return Collection of Vehicle objects
	 * @throws RemoteException
	 */
	public Collection<IpcExtVehicle> getExtForRoute(String routeIdOrShortName) 
			throws RemoteException;

	/**
	 * Gets from server IpcVehicle info for all vehicles currently. associated
	 * with route.
	 * 
	 * @param routeIdsOrShortNames
	 *            Specifies which routes to get Vehicle data for
	 * @return Collection of Vehicle objects
	 * @throws RemoteException
	 */
	public Collection<IpcVehicle> getForRoute(List<String> routeIdsOrShortNames)
			throws RemoteException;

	/**
	 * Gets from server IpcSiriVehicle info for all vehicles currently. associated
	 * with route.
	 * 
	 * @param routeIdsOrShortNames
	 *            Specifies which routes to get Vehicle data for
	 * @return Collection of Vehicle objects
	 * @throws RemoteException
	 */
	public Collection<IpcExtVehicle> getExtForRoute(List<String> routeIdsOrShortNames) 
			throws RemoteException;

}
