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
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.core.VehicleState;
import org.transitime.db.structs.Route;
import org.transitime.ipc.data.Avl;
import org.transitime.ipc.data.Vehicle;

/**
 * For storing and retrieving Vehicle information that can be used by clients.
 * 
 * @author SkiBu Smith
 */
public class VehicleDataCache {

	// Make this class available as a singleton
	private static VehicleDataCache singleton = new VehicleDataCache();
	
	// Keyed by vehicle ID
	private Map<String, Vehicle> vehiclesMap =
			new HashMap<String, Vehicle>();
	
	// Keyed by route_short_name. For each route there is a submap
	// that is keyed by vehicle.
	private Map<String, Map<String, Vehicle>> vehiclesByRouteMap =
			new HashMap<String, Map<String, Vehicle>>();			

	private static final Logger logger = 
			LoggerFactory.getLogger(VehicleDataCache.class);

	/********************** Member Functions **************************/

	/**
	 * Gets the singleton instance of this class.
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
	 * Returns Collection of Vehicles currently associated with specified route.
	 * 
	 * @param routeShortName
	 * @return
	 */
	public Collection<Vehicle> getVehiclesForRoute(String routeShortName) {
		Map<String, Vehicle> vehicleMapForRoute = 
				vehiclesByRouteMap.get(routeShortName);
		if (vehicleMapForRoute != null)
			return vehicleMapForRoute.values();
		else 
			return null;
	}
	
	/**
	 * Returns Collection of Vehicles currently associated with specified route.
	 * 
	 * @param routeId
	 * @return
	 */
	public Collection<Vehicle> getVehiclesForRouteUsingRouteId(String routeId) {
		String routeShortName = null;
		Route route = Core.getInstance().getDbConfig().getRoute(routeId);
		if (route != null)
			routeShortName = route.getShortName();
		return getVehiclesForRoute(routeShortName);
	}
	
	/**
	 * Returns Collection of VehiclesInterface whose vehicleIds were specified using
	 * the vehiclesIds parameter.
	 * 
	 * @param vehicleIds Specifies which vehicles should return.
	 * @return
	 */
	public Collection<Vehicle> getVehicles(String[] vehicleIds) {
		Collection<Vehicle> vehicles = new ArrayList<Vehicle>();
		for (String vehicleId : vehicleIds) {
			Vehicle vehicle = vehiclesMap.get(vehicleId);
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
	public Vehicle getVehicle(String vehicleId) {
		return vehiclesMap.get(vehicleId);
	}
	
	/**
	 * Returns Vehicle info for all vehicles.
	 * 
	 * @return
	 */
	public Collection<Vehicle> getVehicles() {
		return vehiclesMap.values();
	}

	/*
	 * Updates the maps containing the vehicle info.
	 *  
	 * @param vehicle
	 */
	private void updateVehicle(Vehicle vehicle) {
		logger.debug("Adding to VehicleDataCache vehicle={}", vehicle);
		
		Vehicle originalVehicle = vehiclesMap.get(vehicle.getId());
		
		// If the route has changed then remove the vehicle from the old map 
		// for that route.
		if (originalVehicle != null && 
				originalVehicle.getRouteShortName() != vehicle.getRouteShortName() &&
				!originalVehicle.getRouteShortName().equals(vehicle.getRouteShortName())) {
			Map<String, Vehicle> vehicleMapForRoute = 
					vehiclesByRouteMap.get(originalVehicle.getRouteShortName());
			vehicleMapForRoute.remove(vehicle.getId());
		}
		
		// Add Vehicle to the vehiclesByRouteMap
		Map<String, Vehicle> vehicleMapForRoute = 
				vehiclesByRouteMap.get(vehicle.getRouteShortName());
		if (vehicleMapForRoute == null) {
			vehicleMapForRoute = new HashMap<String, Vehicle>();
			vehiclesByRouteMap.put(vehicle.getRouteShortName(), vehicleMapForRoute);			
		}		
		vehicleMapForRoute.put(vehicle.getId(), vehicle);
		
		// Add vehicle to vehiclesMap
		vehiclesMap.put(vehicle.getId(), vehicle);
	}
	
	/**
	 * Updates the maps containing the vehicle info.
	 * 
	 * @param vs The current VehicleState
	 */
	public void updateVehicle(VehicleState vs) {
		Vehicle vehicle = new Vehicle(new Avl(
				vs.getLastAvlReport()), vs.getRouteId(),
				vs.getRouteShortName(), vs.getTrip().getId(),
				vs.isPredictable());
		updateVehicle(vehicle);
	}
}
