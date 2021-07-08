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

import org.transitclock.ipc.data.IpcTrip;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Specifies how trip data along with travel times is formatted for the API.
 *
 * @author SkiBu Smith
 *
 */
@XmlRootElement(name = "trip")
public class ApiTripWithTravelTimes extends ApiTrip {

	@XmlElement
	private ApiTravelTimes travelTimes;

	/********************** Member Functions **************************/

	/**
	 * No args constructor needed for Jersey since this class is a @XmlRootElement
	 */
	protected ApiTripWithTravelTimes() {
	}

	/**
	 * Constructor
	 * 
	 * @param ipcTrip
	 * @param includeStopPaths
	 */
	public ApiTripWithTravelTimes(IpcTrip ipcTrip, boolean includeStopPaths) {
		super(ipcTrip, includeStopPaths);

		travelTimes = new ApiTravelTimes(ipcTrip.getTravelTimes());
	}
}
