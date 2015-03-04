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
import org.transitime.ipc.data.IpcSchedTime;
import org.transitime.ipc.data.IpcSchedTrip;

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

	@XmlElement(name = "time")
	private List<ApiScheduleTimesForStop> timesForStop;
	
	/********************** Member Functions **************************/

	/**
	 * Need a no-arg constructor for Jersey. Otherwise get really obtuse
	 * "MessageBodyWriter not found for media type=application/json" exception.
	 */
	protected ApiSchedule() {
	}
	
	public ApiSchedule(IpcSchedule ipcSched) {
		serviceId = ipcSched.getServiceId();
		serviceName = ipcSched.getServiceName();
		directionId = ipcSched.getDirectionId();
		routeId = ipcSched.getRouteId();
		routeName = ipcSched.getRouteName();
		
		// Create the trips element which contains list of all the trips
		// for the schedule for the route/direction/service
		trips = new ArrayList<ApiScheduleTrip>();
		for (IpcSchedTrip ipcSchedTrip : ipcSched.getIpcSchedTrips()) {
			trips.add(new ApiScheduleTrip(ipcSchedTrip));
		}
		
		// Determine all the times for each stop 
		timesForStop = new ArrayList<ApiScheduleTimesForStop>();
		// Use first trip to determine which stops are covered
		List<IpcSchedTime> schedTimesForFirstTrip = 
				ipcSched.getIpcSchedTrips().get(0).getSchedTimes();
		// For each stop. Use schedule times for first trip to determine
		// list of stops...
		for (IpcSchedTime firstTripSchedTime : schedTimesForFirstTrip) {
			String stopId = firstTripSchedTime.getStopId();
			String stopName = firstTripSchedTime.getStopName();
			ApiScheduleTimesForStop apiSchedTimesForStop = 
					new ApiScheduleTimesForStop(stopId, stopName);
			timesForStop.add(apiSchedTimesForStop);
			// For each trip find the time for the current stop...
			for (IpcSchedTrip ipcSchedTrip : ipcSched.getIpcSchedTrips()) {
				// For the current trip find the time for the current stop...
				for (IpcSchedTime ipcSchedTime : ipcSchedTrip.getSchedTimes()) {
					if (ipcSchedTime.getStopId().equals(
							firstTripSchedTime.getStopId())) {
						// Found the time for the stop so add it to list for 
						// the stop
						apiSchedTimesForStop.add(ipcSchedTime.getTimeOfDay());						
						break;
					}
				}
			}
		}
	}
}
