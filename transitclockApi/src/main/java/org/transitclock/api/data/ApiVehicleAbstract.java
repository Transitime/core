/*
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License (GPL) as published by the
 * Free Software Foundation, either version 3 of the License, or any later
 * version.
 * 
 * Transitime.org is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Transitime.org . If not, see <http://www.gnu.org/licenses/>.
 */

package org.transitclock.api.data;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.transitclock.api.rootResources.TransitimeApi.UiMode;
import org.transitclock.ipc.data.IpcVehicle;

/**
 * This class exists so that can have multiple subclasses that inherent from
 * each other while still being able to set the propOrder for each class.
 * Specifically, ApiVehicleDetails is supposed to be a subclass of ApiVehicle.
 * But want the vehicle id to be output as the first attribute. But the
 * attributes for the subclass are output first and one can't normally set the
 * propOrder of parent class attributes. One gets an internal error if one tries
 * to do so.
 * <p>
 * The solution is to use the abstract class ApiVehicleAbstract. Then can
 * implement ApiVehicle and ApiVehicleDetails to inherit from ApiVehicleAbstract
 * and those classes can each set propOrder as desired. Yes, this is rather
 * complicated, but it works.
 *
 * @author SkiBu Smith
 *
 */
@XmlTransient
public abstract class ApiVehicleAbstract {

	@XmlAttribute
	protected String id;

	@XmlElement
	protected ApiGpsLocation loc;

	@XmlAttribute
	protected String routeId;

	@XmlAttribute
	protected String routeShortName;

	@XmlAttribute
	protected String headsign;

	@XmlAttribute(name = "direction")
	protected String directionId;

	@XmlAttribute
	protected String vehicleType;

	// Whether NORMAL, SECONDARY, or MINOR. Specifies how vehicle should
	// be drawn in the UI
	@XmlAttribute
	protected String uiType;

	@XmlAttribute(name = "scheduleBased")
	protected Boolean schedBasedPreds;

	/********************** Member Functions **************************/

	/**
	 * Need a no-arg constructor for Jersey. Otherwise get really obtuse
	 * "MessageBodyWriter not found for media type=application/json" exception.
	 */
	protected ApiVehicleAbstract() {
	}

	/**
	 * Takes a Vehicle object for client/server communication and constructs a
	 * ApiVehicle object for the API.
	 * 
	 * @param vehicle
	 * @param uiType
	 *            If should be labeled as "minor" in output for UI.
	 */
	public ApiVehicleAbstract(IpcVehicle vehicle, UiMode uiType, SpeedFormat speedFormat) {
		id = vehicle.getId();
		loc = new ApiGpsLocation(vehicle, speedFormat);
		routeId = vehicle.getRouteId();
		routeShortName = vehicle.getRouteShortName();
		headsign = vehicle.getHeadsign();
		directionId = vehicle.getDirectionId();

		// Set GTFS vehicle type. If it was not set in the config then use
		// default value of "3" which is for buses.
		vehicleType = vehicle.getVehicleType();
		if (vehicleType == null)
			vehicleType = "3";

		// Determine UI type. Usually will be displaying vehicles
		// as NORMAL. To simplify API use null for this case.
		this.uiType = null;
		if (uiType == UiMode.SECONDARY)
			this.uiType = "secondary";
		else if (uiType == UiMode.MINOR)
			this.uiType = "minor";

		this.schedBasedPreds = vehicle.isForSchedBasedPred() ? true : null;
	}
}
