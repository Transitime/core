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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.core.VehicleState;
import org.transitime.db.structs.Route;
import org.transitime.ipc.data.IpcExtVehicle;
import org.transitime.ipc.data.IpcVehicle;
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

	// For filtering out info more than MAX_AGE since it means that the info is
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
	 * info is obsolete and shouldn't be displayed.
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
	 * Returns Vehicle info for all vehicles.
	 * 
	 * @return
	 */
	public Collection<IpcExtVehicle> getVehicles() {
		return vehiclesMap.values();
	}

	/**
	 * Updates the maps containing the vehicle info.
	 * 
	 * @param vehicle
	 */
	private void updateVehicle(IpcExtVehicle vehicle) {
		logger.debug("Adding to VehicleDataCache vehicle={}", vehicle);

		IpcVehicle originalVehicle = vehiclesMap.get(vehicle.getId());

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

		// Add vehicle to vehiclesMap
		vehiclesMap.put(vehicle.getId(), vehicle);
	}

	/**
	 * Updates the maps containing the vehicle info. Should be called every time
	 * vehicle state changes.
	 * 
	 * @param vs
	 *            The current VehicleState
	 */
	public void updateVehicle(VehicleState vs) {
		IpcExtVehicle vehicle = new IpcExtVehicle(vs);
		updateVehicle(vehicle);
	}
}
