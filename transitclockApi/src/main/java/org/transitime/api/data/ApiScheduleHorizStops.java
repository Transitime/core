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

import org.transitclock.ipc.data.IpcSchedTime;
import org.transitclock.ipc.data.IpcSchedTrip;
import org.transitclock.ipc.data.IpcSchedule;

/**
 * Represents a schedule for a route for a specific direction and service class.
 * Stops are listed horizontally in the matrix.
 *
 * @author SkiBu Smith
 *
 */
public class ApiScheduleHorizStops {

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

	@XmlElement(name = "stop")
	private List<ApiScheduleStop> stops;

	@XmlElement
	private List<ApiScheduleTimesForTrip> timesForTrip;
	
	/********************** Member Functions **************************/

	/**
	 * Need a no-arg constructor for Jersey. Otherwise get really obtuse
	 * "MessageBodyWriter not found for media type=application/json" exception.
	 */
	protected ApiScheduleHorizStops() {
	}
	
	public ApiScheduleHorizStops(IpcSchedule ipcSched) {
		serviceId = ipcSched.getServiceId();
		serviceName = ipcSched.getServiceName();
		directionId = ipcSched.getDirectionId();
		routeId = ipcSched.getRouteId();
		routeName = ipcSched.getRouteName();
		
		// Create the list of stops to be first row of output
		stops = new ArrayList<ApiScheduleStop>();
		IpcSchedTrip firstIpcSchedTrip = ipcSched.getIpcSchedTrips().get(0);
		for (IpcSchedTime ipcSchedTime : firstIpcSchedTrip.getSchedTimes()) {
			stops.add(new ApiScheduleStop(ipcSchedTime.getStopId(),
					ipcSchedTime.getStopName()));
		}

		// Create the schedule row for each trip
		timesForTrip = new ArrayList<ApiScheduleTimesForTrip>();
		for (IpcSchedTrip ipcSchedTrip : ipcSched.getIpcSchedTrips()) {
			timesForTrip.add(new ApiScheduleTimesForTrip(ipcSchedTrip));
		}
	}
}

