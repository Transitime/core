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

import javax.xml.bind.annotation.XmlAttribute;

import org.transitime.ipc.data.IpcSchedTimes;
import org.transitime.utils.Time;

/**
 * Represents a schedule time for a stop. Contains both arrival and departure
 * time and is intended to be used for displaying the details of a trip.
 *
 * @author SkiBu Smith
 *
 */
public class ApiScheduleArrDepTime {

	@XmlAttribute
	private String arrivalTime;

	@XmlAttribute
	private String departureTime;

	@XmlAttribute
	private String stopId;

	@XmlAttribute
	private String stopName;

	/********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse
     * "MessageBodyWriter not found for media type=application/json" exception.
     */
	protected ApiScheduleArrDepTime() {}
	
	public ApiScheduleArrDepTime(IpcSchedTimes ipcScheduleTimes) {
		Integer arrivalInt = ipcScheduleTimes.getArrivalTime();
		arrivalTime = arrivalInt == null ? null : Time.timeOfDayStr(arrivalInt);

		Integer departureInt = ipcScheduleTimes.getDepartureTime();
		departureTime = departureInt == null ? null : Time
				.timeOfDayStr(departureInt);

		stopId = ipcScheduleTimes.getStopId();
		stopName = ipcScheduleTimes.getStopName();
	}
}
