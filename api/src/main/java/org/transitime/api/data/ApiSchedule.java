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
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.transitime.ipc.data.IpcSchedule;
import org.transitime.ipc.data.IpcScheduleTrip;

/**
 * Represents a schedule for a route for a specific direction and service class.
 *
 * @author SkiBu Smith
 *
 */
public class ApiSchedule {

	@XmlAttribute
	private String serviceId;

	@XmlAttribute
	private String serviceName;

	@XmlAttribute
	private String directionId;

	@XmlAttribute
	private String routeId;

	@XmlAttribute
	private String routeName;

	@XmlElement(name = "trip")
	private List<ApiScheduleTrip> trips;

	/********************** Member Functions **************************/

	/**
	 * Need a no-arg constructor for Jersey. Otherwise get really obtuse
	 * "MessageBodyWriter not found for media type=application/json" exception.
	 */
	protected ApiSchedule() {
	}
	
	public ApiSchedule(IpcSchedule ipcSchedule) {
		serviceId = ipcSchedule.getServiceId();
		serviceName = ipcSchedule.getServiceName();
		directionId = ipcSchedule.getDirectionId();
		routeId = ipcSchedule.getRouteId();
		routeName = ipcSchedule.getRouteName();
		
		trips = new ArrayList<ApiScheduleTrip>();
		for (IpcScheduleTrip ipcScheduleTrip : ipcSchedule.getIpcScheduleTrips()) {
			trips.add(new ApiScheduleTrip(ipcScheduleTrip));
		}
	}
}
