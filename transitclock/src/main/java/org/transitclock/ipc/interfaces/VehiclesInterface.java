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

package org.transitclock.ipc.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

import org.transitclock.ipc.data.IpcActiveBlock;
import org.transitclock.ipc.data.IpcVehicle;
import org.transitclock.ipc.data.IpcVehicleComplete;
import org.transitclock.ipc.data.IpcVehicleConfig;
import org.transitclock.ipc.data.IpcVehicleGtfsRealtime;

/**
 * Defines the RMI interface used for obtaining vehicle information.
 * 
 * @author SkiBu Smith
 * 
 */
public interface VehiclesInterface extends Remote {

  /**
   * Gets from the server IpcActiveBlocks for blocks that are currently
   * active without vehicle data.
   * 
   * @param routeIds
   *            List of routes that want data for. Can also be null or empty.
   * @param allowableBeforeTimeSecs
   *            How much before the block time the block is considered to be
   *            active
   * @return Collection of blocks that are active
   * @throws RemoteException
   */
  public Collection<IpcActiveBlock> getActiveBlocksWithoutVehicles(
     Collection<String> routeIds, int allowableBeforeTimeSecs, boolean includeCanceledTrips)
     throws RemoteException;

  /**
   * Gets from the server IpcActiveBlocks for blocks that are currently
   * active with vehicle data for a particular route.
   * 
   * @param routeId
   *            Route that want data for. Can also be null or empty.
   * @param allowableBeforeTimeSecs
   *            How much before the block time the block is considered to be
   *            active
   * @return Collection of blocks that are active
   * @throws RemoteException
   */
  public Collection<IpcActiveBlock> getActiveBlocksAndVehiclesByRouteId(
     String routeId, int allowableBeforeTimeSecs, boolean includeCanceledTrips)
     throws RemoteException;

  /**
   * Gets from the server IpcActiveBlocks for blocks that are currently
   * active with vehicle data for all routes with given route name.
   * 
   * @param routeName
   *            Route name that want data for. Can also be null or empty.
   * @param allowableBeforeTimeSecs
   *            How much before the block time the block is considered to be
   *            active
   * @return Collection of blocks that are active
   * @throws RemoteException
   */
  public Collection<IpcActiveBlock> getActiveBlocksAndVehiclesByRouteName(
     String routeName, int allowableBeforeTimeSecs, boolean includeCanceledTrips)
     throws RemoteException;
    
    /**
	 * For getting configuration information for all vehicles. Useful for
	 * determining IDs of all vehicles in system
	 * 
	 * @return Collection of IpcVehicleConfig objects
	 * @throws RemoteException
	 */
	public Collection<IpcVehicleConfig> getVehicleConfigs() 
			throws RemoteException;
	
	/**
	 * Gets from server IpcVehicle info for all vehicles.
	 * 
	 * @return Collection of IpcVehicle objects
	 * @throws RemoteException
	 */
	public Collection<IpcVehicle> get() throws RemoteException;

	/**
	 * Gets from server IpcCompleteVehicle info for all vehicles.
	 * 
	 * @return Collection of Vehicle objects
	 * @throws RemoteException
	 */
	public Collection<IpcVehicleComplete> getComplete() throws RemoteException;

	/**
	 * Gets from server IpcCompleteVehicle info for all vehicles.
	 * 
	 * @return Collection of Vehicle objects
	 * @throws RemoteException
	 */
	public Collection<IpcVehicleGtfsRealtime> getGtfsRealtime()
			throws RemoteException;

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
	 * Gets from server IpcCompleteVehicle info for specified vehicle.
	 * 
	 * @param vehicleId
	 *            ID of vehicle to get data for
	 * @return info for specified vehicle
	 * @throws RemoteException
	 */
	public IpcVehicleComplete getComplete(String vehicleId)
			throws RemoteException;

	/**
	 * Gets from server IpcVehicle info for vehicles specified by vehicles
	 * parameter.
	 * 
	 * @param vehicleIds
	 *            Collection of vehicle IDs to get Vehicle data for.
	 * @return Collection of Vehicle objects
	 * @throws RemoteException
	 */
	public Collection<IpcVehicle> get(Collection<String> vehicleIds)
			throws RemoteException;

	/**
	 * Gets from server IpcCompleteVehicle info for vehicles specified by
	 * vehicles parameter.
	 * 
	 * @param vehicleIds
	 *            Collection of vehicle IDs to get Vehicle data for.
	 * @return Collection of Vehicle objects
	 * @throws RemoteException
	 */
	public Collection<IpcVehicleComplete> getComplete(
			Collection<String> vehicleIds) throws RemoteException;

	/**
	 * Gets from server IpcVehicle info for all vehicles currently associated
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
   * Gets from server IpcVehicle info for all vehicles currently associated
   * with a block. Each block should have a IpcVehicle in the returned collection.
   * 
   * @return Collection of Vehicle objects
   * @throws RemoteException
   */
  public Collection<IpcVehicle> getVehiclesForBlocks()
      throws RemoteException;

	/**
	 * Gets from server IpcCompleteVehicle info for all vehicles currently.
	 * associated with route.
	 * 
	 * @param routeIdOrShortName
	 *            Specifies which route to get Vehicle data for
	 * @return Collection of Vehicle objects
	 * @throws RemoteException
	 */
	public Collection<IpcVehicleComplete> getCompleteForRoute(
			String routeIdOrShortName) throws RemoteException;

	/**
	 * Gets from server IpcVehicle info for all vehicles currently. associated
	 * with route.
	 * 
	 * @param routeIdsOrShortNames
	 *            Specifies which routes to get Vehicle data for
	 * @return Collection of Vehicle objects
	 * @throws RemoteException
	 */
	public Collection<IpcVehicle> getForRoute(
			Collection<String> routeIdsOrShortNames) throws RemoteException;

	/**
	 * Gets from server IpcCompleteVehicle info for all vehicles currently.
	 * associated with route.
	 * 
	 * @param routeIdsOrShortNames
	 *            Specifies which routes to get Vehicle data for
	 * @return Collection of Vehicle objects
	 * @throws RemoteException
	 */
	public Collection<IpcVehicleComplete> getCompleteForRoute(
			Collection<String> routeIdsOrShortNames) throws RemoteException;

	/**
	 * Gets from the server IpcActiveBlocks for blocks that are currently
	 * active.
	 * 
	 * @param routeIds
	 *            List of routes that want data for. Can also be null or empty.
	 * @param allowableBeforeTimeSecs
	 *            How much before the block time the block is considered to be
	 *            active
	 * @return Collection of blocks that are active
	 * @throws RemoteException
	 */
	public Collection<IpcActiveBlock> getActiveBlocks(
			Collection<String> routeIds, int allowableBeforeTimeSecs) 
					throws RemoteException;
	
	 /**
   * Gets from the server the number of blocks that are currently active.
   * 
   * @param routeIds
   *            List of routes that want data for. Can also be null or empty.
   * @param allowableBeforeTimeSecs
   *            How much before the block time the block is considered to be
   *            active
   * @return Number of blocks that are active.
   * @throws RemoteException
   */
  public int getNumActiveBlocks(
      Collection<String> routeIds, int allowableBeforeTimeSecs) 
          throws RemoteException;

  
}
