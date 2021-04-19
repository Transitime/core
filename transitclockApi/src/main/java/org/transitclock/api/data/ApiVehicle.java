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

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.transitclock.api.rootResources.TransitimeApi.UiMode;
import org.transitclock.ipc.data.IpcVehicle;

/**
 * Contains the data for a single vehicle.
 * <p>
 * Note: @XmlType(propOrder=""...) is used to get the elements to be output in
 * desired order instead of the default of alphabetical. This makes the
 * resulting JSON/XML more readable.
 * 
 * @author SkiBu Smith
 * 
 */
@XmlRootElement
@XmlType(propOrder = { "id", "routeId", "routeShortName", "headsign",
		"directionId", "vehicleType", "uiType", "schedBasedPreds", "loc" })
public class ApiVehicle extends ApiVehicleAbstract {

	/**
	 * Need a no-arg constructor for Jersey. Otherwise get really obtuse
	 * "MessageBodyWriter not found for media type=application/json" exception.
	 */
	protected ApiVehicle() {
	}

	/**
	 * Takes a Vehicle object for client/server communication and constructs a
	 * ApiVehicle object for the API.
	 * 
	 * @param vehicle
	 * @param uiType
	 *            If should be labeled as "minor" in output for UI.
	 */
	public ApiVehicle(IpcVehicle vehicle, UiMode uiType, SpeedFormat speedFormat) {
		super(vehicle, uiType, speedFormat);
	}

	/**
	 * Takes a Vehicle object for client/server communication and constructs a
	 * ApiVehicle object for the API. Sets UiMode to UiMode.NORMAL.
	 * 
	 * @param vehicle
	 */
	public ApiVehicle(IpcVehicle vehicle) {
		super(vehicle, UiMode.NORMAL, SpeedFormat.MS);
	}
}
