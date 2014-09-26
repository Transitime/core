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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.core.VehicleState;
import org.transitime.db.structs.Route;
import org.transitime.ipc.data.IpcExtVehicle;
import org.transitime.utils.Time;

/**
 * For storing and retrieving vehicle information that can be used by clients.
 * Is updated every time VehicleState is changed. The info is stored in an
 * immutable IpcExtVehicle object since VehicleState changes dynamically and is
 * therefore not always coherent. This way the info transmitted to clients will
 * always be coherent without having to synchronize VehicleState for when
 * converting to a IpcExtVehicle. Organizes vehicles info by vehicle ID but also
 * by route so can easily determine which vehicles are associated with a route.
 * 
 * @author SkiBu Smith
 */
public class VehicleDataCache {

    // Make this class available as a singleton
    private static VehicleDataCache singleton = new VehicleDataCache();

    // Keyed by vehicle ID
    private Map<String, IpcExtVehicle> vehiclesMap = 
    		new HashMap<String, IpcExtVehicle>();

    // Keyed by route_short_name. For each route there is a submap
    // that is keyed by vehicle.
    private Map<String, Map<String, IpcExtVehicle>> vehiclesByRouteMap = 
    		new HashMap<String, Map<String, IpcExtVehicle>>();

    // So can determine vehicles associated with a block ID. Keyed on
    // block ID. Each block can have a list of vehicles. Though rare
    // there are situations where multiple vehicles might have the
    // same assignment, such as for unscheduled assignments. 
    private Map<String, List<IpcExtVehicle>> vehiclesByBlockMap =
    		new HashMap<String, List<IpcExtVehicle>>();
    
	// For filtering out info more than MAX_AGE since it means that the AVL info is
	// obsolete and shouldn't be displayed.
    private static final int MAX_AGE_MSEC = 15 * Time.MS_PER_MIN;
    
    private static final Logger logger = LoggerFactory
	    .getLogger(VehicleDataCache.class);

    /********************** Member Functions **************************/

	/**
	 * Gets the singleton instance of this class.
	 * 
	 * @return
	 */
	public static VehicleDataCache getInstance() {
		return singleton;
	}

    /*
     * Constructor declared private to enforce only access to this singleton
     * class being getInstance()
     */
    private VehicleDataCache() {
    }

    /**
     * Filters out vehicle info if it is too old.
     * @param vehicles
     * @return
     */
    private Collection<IpcExtVehicle> filtered(
    		Collection<IpcExtVehicle> vehicles) {
    	Collection<IpcExtVehicle> filteredVehicles = 
				new ArrayList<IpcExtVehicle>(vehicles.size());
    	
    	long timeCutoff = System.currentTimeMillis() - MAX_AGE_MSEC;
    	for (IpcExtVehicle vehicle : vehicles) {
    		if (vehicle.getAvl().getTime() > timeCutoff) {
    			filteredVehicles.add(vehicle);
    		}
    	}
    	
    	// Return only vehicles whose AVL report not too old.
    	return filteredVehicles;
    }

	/**
	 * Returns Collection of Vehicles currently associated with specified route.
	 * Filters out info more than MAX_AGE_MSEC since it means that the info is
	 * obsolete and shouldn't be displayed. Returns null if no vehicles for
	 * specified route.
	 * 
	 * @param routeIdOrShortName
	 * @return Collection of IpcExtVehicle for vehicles on route, or null if no
	 *         vehicles for the route.
	 */
	public Collection<IpcExtVehicle> getVehiclesForRoute(
			String routeIdOrShortName) {
		// Try getting vehicles using routeShortName
		String routeShortName = routeIdOrShortName;
		Map<String, IpcExtVehicle> vehicleMapForRoute = vehiclesByRouteMap
				.get(routeShortName);
		// If couldn't get vehicles by route short name try using
		// the route ID.
		if (vehicleMapForRoute == null) {
			Route route = Core.getInstance().getDbConfig()
					.getRouteById(routeIdOrShortName);
			vehicleMapForRoute = vehiclesByRouteMap.get(route.getShortName());
		}

		if (vehicleMapForRoute != null)
			return filtered(vehicleMapForRoute.values());
		else
			return null;
	}

	/**
	 * Returns Collection of Vehicles currently associated with specified
	 * routes. Filters out info more than MAX_AGE_MSEC since it means that the
	 * info is obsolete and shouldn't be displayed. Needs to return a collection
	 * because there are situations, such as unscheduled assignments, where
	 * multiple vehicles can be assigned to a block.
	 * 
	 * @param routeIdsOrShortNames
	 * @return Collection of vehicles for the route. Empty collection if there
	 *         are none.
	 */
	public Collection<IpcExtVehicle> getVehiclesForRoute(
			List<String> routeIdsOrShortNames) {
		// If there is just a single route specified then use a shortcut
		if (routeIdsOrShortNames.size() == 1)
			return getVehiclesForRoute(routeIdsOrShortNames.get(0));

		Collection<IpcExtVehicle> vehicles = new ArrayList<IpcExtVehicle>();
		for (String routeIdOrShortName : routeIdsOrShortNames) {
			Collection<IpcExtVehicle> vehiclesForRoute = 
					getVehiclesForRoute(routeIdOrShortName);
			if (vehiclesForRoute != null)
				vehicles.addAll(vehiclesForRoute);
		}
		return vehicles;
	}
      
	/**
	 * Returns Collection of VehiclesInterface whose vehicleIds were specified
	 * using the vehiclesIds parameter.
	 * 
	 * @param vehicleIds
	 *            Specifies which vehicles should return.
	 * @return
	 */
	public Collection<IpcExtVehicle> getVehicles(List<String> vehicleIds) {
		Collection<IpcExtVehicle> vehicles = new ArrayList<IpcExtVehicle>();
		for (String vehicleId : vehicleIds) {
			IpcExtVehicle vehicle = vehiclesMap.get(vehicleId);
			if (vehicle != null)
				vehicles.add(vehicle);
		}
		return vehicles;
	}

	/**
	 * Returns Vehicle info for the vehicleId specified.
	 * 
	 * @param vehicleId
	 * @return
	 */
	public IpcExtVehicle getVehicle(String vehicleId) {
		return vehiclesMap.get(vehicleId);
	}

	/**
	 * Returns list of vehicles that are currently assigned to the specified
	 * block. Will return empty list if no vehicles assigned to that block
	 * (won't return null). Usually there will only be a single vehicle
	 * associated with a block assignment but there are cases, such as
	 * unscheduled assignments, where there could be multiple vehicles.
	 * Therefore this method returns a List.
	 * 
	 * @param blockId
	 * @return List of vehicles associated with the specified block Id. Returns
	 *         empty list instead of null if no vehicles associated with the
	 *         block ID.
	 */
	public List<IpcExtVehicle> getVehiclesByBlockId(String blockId) {
		List<IpcExtVehicle> vehicles = vehiclesByBlockMap.get(blockId);
		if (vehicles != null)
			return vehicles;
		else
			return new ArrayList<IpcExtVehicle>(0);
	}
	
	/**
	 * Returns Vehicle info for all vehicles.
	 * 
	 * @return
	 */
	public Collection<IpcExtVehicle> getVehicles() {
		return vehiclesMap.values();
	}

	/**
	 * Updates the vehiclesByBlockMap
	 * 
	 * @param originalVehicle
	 *            For getting the previous block ID for the vehicle. Can be null
	 *            if there was no previous vehicle info
	 * @param vehicle
	 *            For getting the current block ID for the vehicle.
	 */
	private void updateVehiclesByBlockMap(IpcExtVehicle originalVehicle,
			IpcExtVehicle vehicle) {
		// Handle old assignment		
		if (originalVehicle != null) {
			// If block assignment is same as before don't need to update the 
			// block map
			if (Objects.equals(originalVehicle.getBlockId(), 
					vehicle.getBlockId()))
				return;
				
			// Block assignment has changed for vehicle so remove the old one 
			// from the map
			List<IpcExtVehicle> vehiclesForOldBlock = 
					vehiclesByBlockMap.get(originalVehicle.getBlockId());
			if (vehiclesForOldBlock != null)
				vehiclesForOldBlock.remove(originalVehicle);
		}
		
		// Add the new block assignment to the map
		List<IpcExtVehicle> vehiclesForNewBlock = 
				vehiclesByBlockMap.get(vehicle.getBlockId());
		if (vehiclesForNewBlock == null) {
			vehiclesForNewBlock = new ArrayList<IpcExtVehicle>();
			vehiclesByBlockMap.put(vehicle.getBlockId(), vehiclesForNewBlock);
		}
		vehiclesForNewBlock.add(vehicle);
	}
	
	/**
	 * Updates vehiclesByRouteMap containing the vehicle info.
	 * 
	 * @param originalVehicle
	 * @param vehicle
	 */
	private void updateVehiclesByRouteMap(IpcExtVehicle originalVehicle, IpcExtVehicle vehicle) {
		// If the route has changed then remove the vehicle from the old map for
		// that route. Watch out for getRouteShortName() sometimes being null
		if (originalVehicle != null
				&& originalVehicle.getRouteShortName() != 
						vehicle.getRouteShortName()
				&& (originalVehicle.getRouteShortName() == null 
					|| !originalVehicle.getRouteShortName().equals(
						vehicle.getRouteShortName()))) {
			Map<String, IpcExtVehicle> vehicleMapForRoute = vehiclesByRouteMap
					.get(originalVehicle.getRouteShortName());
			vehicleMapForRoute.remove(vehicle.getId());
		}

		// Add IpcExtVehicle to the vehiclesByRouteMap
		Map<String, IpcExtVehicle> vehicleMapForRoute = 
				vehiclesByRouteMap.get(vehicle.getRouteShortName());
		if (vehicleMapForRoute == null) {
			vehicleMapForRoute = new HashMap<String, IpcExtVehicle>();
			vehiclesByRouteMap.put(vehicle.getRouteShortName(),
					vehicleMapForRoute);
		}
		vehicleMapForRoute.put(vehicle.getId(), vehicle);				
	}

	/**
	 * Updates vehiclesMap
	 * 
	 * @param vehicle
	 */
	private void updateVehiclesMap(IpcExtVehicle vehicle) {
		// Add vehicle to vehiclesMap
		vehiclesMap.put(vehicle.getId(), vehicle);
	}
	
	/**
	 * Updates the maps containing the vehicle info. Should be called every time
	 * vehicle state changes.
	 * 
	 * @param vehicleState
	 *            The current VehicleState
	 */
	public void updateVehicle(VehicleState vehicleState) {
		IpcExtVehicle vehicle = new IpcExtVehicle(vehicleState);
		IpcExtVehicle originalVehicle = vehiclesMap.get(vehicle.getId());
		
		logger.debug("Adding to VehicleDataCache vehicle={}", vehicle);

		updateVehiclesByRouteMap(originalVehicle, vehicle);
		updateVehiclesByBlockMap(originalVehicle, vehicle);
		updateVehiclesMap(vehicle);
	}
}
