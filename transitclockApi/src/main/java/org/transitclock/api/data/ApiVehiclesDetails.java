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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.transitclock.api.rootResources.TransitimeApi.UiMode;
import org.transitclock.db.structs.Agency;
import org.transitclock.db.webstructs.WebAgency;
import org.transitclock.ipc.data.IpcVehicle;
import org.transitclock.utils.Time;

/**
 * For when have list of VehicleDetails. By using this class can control the
 * element name when data is output.
 *
 * @author SkiBu Smith
 *
 */
@XmlRootElement
public class ApiVehiclesDetails {

	@XmlElement(name = "responseTime")
	private long responseTime;

	@XmlElement(name = "vehicles")
	private List<ApiVehicleDetails> vehiclesData;

	/********************** Member Functions **************************/

	/**
	 * Need a no-arg constructor for Jersey. Otherwise get really obtuse
	 * "MessageBodyWriter not found for media type=application/json" exception.
	 */
	protected ApiVehiclesDetails() {
	}

	/**
	 * For constructing a ApiVehiclesDetails object from a Collection of Vehicle
	 * objects.
	 *
	 * @param vehicles
	 * @param agencyId
	 * @param uiTypesForVehicles
	 *            Specifies how vehicles should be drawn in UI. Can be NORMAL,
	 *            SECONDARY, or MINOR
	 * @param assigned
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	public ApiVehiclesDetails(Collection<IpcVehicle> vehicles,
							  String agencyId,
							  Map<String, UiMode> uiTypesForVehicles,
							  boolean assigned,
							  SpeedFormat speedFormat) throws IllegalAccessException, InvocationTargetException {
		// Get Time object based on timezone for agency
		WebAgency webAgency = WebAgency.getCachedWebAgency(agencyId);
		Agency agency = webAgency.getAgency();
		Time timeForAgency = agency != null ?
				agency.getTime() : new Time((String) null);

		responseTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

		// Process each vehicle
		vehiclesData = new ArrayList<ApiVehicleDetails>();
		for (IpcVehicle vehicle : vehicles) {
			// Determine UI type for vehicle
			UiMode uiType = uiTypesForVehicles.get(vehicle.getId());
			if((assigned  && vehicle.getTripId()!=null ) || !assigned)
				vehiclesData.add(new ApiVehicleDetails(vehicle, timeForAgency,
						speedFormat, uiType));
		}
	}

}
