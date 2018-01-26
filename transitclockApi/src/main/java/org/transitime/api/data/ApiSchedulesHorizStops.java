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

/**
 * Represents a collection of ApiScheduleHorizStops objects for a route. There
 * is one ApiScheduleHoriztStops for each direction/service for a route. The
 * stops are listed horizontally in the matrix. For when there are many trips in
 * a day, such as for bus routes.
 *
 * @author SkiBu Smith
 *
 */
package org.transitime.api.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.transitclock.ipc.data.IpcSchedule;

@XmlRootElement(name = "schedules")
public class ApiSchedulesHorizStops {

	@XmlAttribute
	private String routeId;

	@XmlAttribute
	private String routeName;

	@XmlElement(name = "schedule")
	private List<ApiScheduleHorizStops> schedules;
	
	/********************** Member Functions **************************/

	/**
	 * Need a no-arg constructor for Jersey. Otherwise get really obtuse
	 * "MessageBodyWriter not found for media type=application/json" exception.
	 */
	protected ApiSchedulesHorizStops() {
	}
	
	public ApiSchedulesHorizStops(List<IpcSchedule> schedules) {
		this.routeId = schedules.get(0).getRouteId();
		this.routeName = schedules.get(0).getRouteName();
		
		this.schedules = new ArrayList<ApiScheduleHorizStops>(schedules.size());
		for (IpcSchedule ipcSchedule : schedules) {
			this.schedules.add(new ApiScheduleHorizStops(ipcSchedule));
		}
	}

}
