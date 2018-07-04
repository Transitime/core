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
package org.transitclock.core.dataCache;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.transitclock.core.VehicleState;

/**
 * For keeping track of vehicle state. This is used by the main predictor code,
 * not for RMI clients. For RMI clients the VehicleDataCache is used. This way
 * making the system threadsafe is simpler since VehicleDataCache can handle
 * thread safety completely independently.
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
	 * Constructor made private because this is singleton class where
	 * getInstance() should be used to get the VehicleStateManager.
	 */
	private VehicleStateManager() {	
	}
	
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
	 * Returns vehicle state for the specified vehicle. Vehicle state is kept in
	 * a map. If VehicleState not yet created for the vehicle then this method
	 * will create it. If there was no VehicleState already created for the
	 * vehicle then it is created. This way this method never returns null.
	 * <p>
	 * VehicleState is a large object with multiple collections as members.
	 * Since it might be getting modified when there is a new AVL report when
	 * this method is called need to synchronize on the returned VehicleState
	 * object if accessing any information that is not atomic, such as the
	 * avlReportHistory.
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
