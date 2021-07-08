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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.transitclock.ipc.data.IpcSchedTime;
import org.transitclock.ipc.data.IpcSchedTrip;

/**
 * Contains the schedule times for a trip. For when outputting stops
 * horizontally.
 * 
 * @author Michael
 *
 */
public class ApiScheduleTimesForTrip {
	@XmlAttribute
	private String tripShortName;

	@XmlAttribute
	private String tripId;

	@XmlAttribute
	private String tripHeadsign;
	
	@XmlAttribute
	private String blockId;
	
	@XmlElement(name = "time")
	private List<ApiScheduleTime> times;

	/**
	 * Need a no-arg constructor for Jersey. Otherwise get really obtuse
	 * "MessageBodyWriter not found for media type=application/json" exception.
	 */
	protected ApiScheduleTimesForTrip() {		
	}
	
	public ApiScheduleTimesForTrip(IpcSchedTrip ipcSchedTrip) {
		this.tripShortName = ipcSchedTrip.getTripShortName();
		this.tripId = ipcSchedTrip.getTripId();
		this.tripHeadsign = ipcSchedTrip.getTripHeadsign();
		this.blockId = ipcSchedTrip.getBlockId();
		
		times = new ArrayList<ApiScheduleTime>();
		for (IpcSchedTime ipcSchedTime : ipcSchedTrip.getSchedTimes()) {
			times.add(new ApiScheduleTime(ipcSchedTime.getTimeOfDay()));
		}
	}

}
