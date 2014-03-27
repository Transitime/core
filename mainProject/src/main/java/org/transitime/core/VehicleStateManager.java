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
package org.transitime.core;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * For keeping track of vehicle state. This is used by the main predictor code,
 * not for RMI clients.
 * 
 * @author SkiBu Smith
 *
 */
public class VehicleStateManager {

	// Keyed by vehicle ID. Need to use ConcurrentHashMap instead of HashMap
	// since getVehiclesState() returns values() of the map which can be
	// accessed while the map is being modified with new data via another
	// thread. Otherwise could get a ConcurrentModificationException.
	private Map<String, VehicleState> vehicleMap = 
			new ConcurrentHashMap<String, VehicleState>();
	
	// This is a singleton class
	private static VehicleStateManager singleton = new VehicleStateManager();
	
	/********************** Member Functions **************************/

	/**
	 * Returns the singleton VehicleStateManager
	 * @return
	 */
	public static VehicleStateManager getInstance() {
		return singleton;
	}
	
	/**
	 * Adds VehicleState for the vehicle to the map so that it can be retrieved
	 * later.
	 * 
	 * @param state
	 *            The VehicleState to be added to the map
	 */
	private void putVehicleState(VehicleState state) {
		vehicleMap.put(state.getVehicleId(), state);
	}

	/**
	 * Returns vehicle state for the specified vehicle. Vehicle state is
	 * kept in a map. If VehicleState not yet created for the vehicle then
	 * this method will create it. 
	 * 
	 * @param vehicleId
	 * @return the VehicleState for the vehicle
	 */
	public VehicleState getVehicleState(String vehicleId) {
		 VehicleState vehicleState = vehicleMap.get(vehicleId);
		 if (vehicleState == null) {
			 vehicleState = new VehicleState(vehicleId);
			 putVehicleState(vehicleState);
		 }
		 return vehicleState;
	}
	
	/**
	 * Returns VehicleState for all vehicles.
	 * 
	 * @return Collection of VehicleState objects for all vehicles.
	 */
	public Collection<VehicleState> getVehiclesState() {
		return vehicleMap.values();
	}
}
