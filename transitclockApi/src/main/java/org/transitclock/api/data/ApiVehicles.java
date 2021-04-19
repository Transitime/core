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

package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.transitclock.api.rootResources.TransitimeApi.UiMode;
import org.transitclock.ipc.data.IpcVehicle;

/**
 * For when have list of Vehicles. By using this class can control the element
 * name when data is output.
 *
 * @author SkiBu Smith
 *
 */
@XmlRootElement
public class ApiVehicles {

	@XmlElement(name = "vehicles")
	private List<ApiVehicle> vehiclesData;

	/********************** Member Functions **************************/

	/**
	 * Need a no-arg constructor for Jersey. Otherwise get really obtuse
	 * "MessageBodyWriter not found for media type=application/json" exception.
	 */
	public ApiVehicles() {
	}

	/**
	 * For constructing a ApiVehicles object from a Collection of Vehicle
	 * objects.
	 * 
	 * @param vehicles
	 * @param uiTypesForVehicles
	 *            Specifies how vehicles should be drawn in UI. Can be NORMAL,
	 *            SECONDARY, or MINOR
	 */
	public ApiVehicles(Collection<IpcVehicle> vehicles,
			Map<String, UiMode> uiTypesForVehicles, SpeedFormat speedFormat) {
		vehiclesData = new ArrayList<ApiVehicle>();
		for (IpcVehicle vehicle : vehicles) {
			// Determine UI type for vehicle
			UiMode uiType = uiTypesForVehicles.get(vehicle.getId());

			// Add this vehicle to the ApiVehicle list
			vehiclesData.add(new ApiVehicle(vehicle, uiType, speedFormat));
		}
	}
	
	/**
	 * For constructing a ApiVehicles object from a Collection of Vehicle
	 * objects. Sets UiMode to UiMode.NORMAL.
	 * 
	 * @param vehicles
	 */
	public ApiVehicles(Collection<IpcVehicle> vehicles) {
		vehiclesData = new ArrayList<ApiVehicle>();
		for (IpcVehicle vehicle : vehicles) {
			// Add this vehicle to the ApiVehicle list
			vehiclesData.add(new ApiVehicle(vehicle, UiMode.NORMAL, SpeedFormat.MS));
		}
	}

}
