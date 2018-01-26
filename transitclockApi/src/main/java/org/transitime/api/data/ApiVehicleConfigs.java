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

package org.transitime.api.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.transitclock.ipc.data.IpcVehicleConfig;

/**
 * For when have collection of ApiVehicleConfig
 *
 * @author SkiBu Smith
 *
 */
@XmlRootElement(name = "vehicleConfigs")
public class ApiVehicleConfigs {

	// Need to use @XmlElementRef so that the element name used for each
	// ApiVehicle object will be what is specified in the ApiVehicle class.
	@XmlElementRef
	private List<ApiVehicleConfig> vehicleConfigs;
	
	/********************** Member Functions **************************/

	/**
	 * Need a no-arg constructor for Jersey. Otherwise get really obtuse
	 * "MessageBodyWriter not found for media type=application/json" exception.
	 */
	protected ApiVehicleConfigs() {
	}

	/**
	 * Constructs a ApiVehicleConfigs object using IpcVehicleConfig data
	 * obtained via IPC.
	 * 
	 * @param vehicles
	 */
	public ApiVehicleConfigs(Collection<IpcVehicleConfig> vehicles) {
		// Sort the vehicles by vehicle ID
		List<IpcVehicleConfig> vehiclesList =
				new ArrayList<IpcVehicleConfig>(vehicles);
		Collections.sort(vehiclesList, new Comparator<IpcVehicleConfig>() {
			@Override
			public int compare(final IpcVehicleConfig o1,
					final IpcVehicleConfig o2) {
				return o1.getId().compareTo(o2.getId());
			}
		});

		// Create list of Api objects from the Ipc objects
		vehicleConfigs = new ArrayList<ApiVehicleConfig>();
		for (IpcVehicleConfig vehicle : vehiclesList) {
			vehicleConfigs.add(new ApiVehicleConfig(vehicle));
		}
	}
}
