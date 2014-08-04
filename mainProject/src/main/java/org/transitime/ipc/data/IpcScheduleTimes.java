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

package org.transitime.ipc.data;

import java.io.Serializable;

import org.transitime.applications.Core;
import org.transitime.db.structs.ScheduleTime;
import org.transitime.db.structs.Stop;
import org.transitime.utils.Time;

/**
 * Configuration information for a ScheduleTimes for IPC.
 *
 * @author SkiBu Smith
 *
 */
public class IpcScheduleTimes implements Serializable {

	private final Integer arrivalTime;
	private final Integer departureTime;
	private final String stopId;
	private final String stopName;

	private static final long serialVersionUID = 2469869322769172736L;

	/********************** Member Functions **************************/

	public IpcScheduleTimes(ScheduleTime dbScheduleTime, String stopId) {
		this.arrivalTime = dbScheduleTime.getArrivalTime();
		this.departureTime = dbScheduleTime.getDepartureTime();
		this.stopId = stopId;
		Stop stop = Core.getInstance().getDbConfig().getStop(stopId);
		this.stopName = stop.getName();
	}

	@Override
	public String toString() {
		return "IpcScheduleTimes [" 
				+ "arrivalTime=" + Time.timeOfDayStr(arrivalTime)
				+ ", departureTime=" + Time.timeOfDayStr(departureTime)
				+ ", stopId=" + stopId
				+ ", stopName=" + stopName
				+ "]";
	}

	public Integer getArrivalTime() {
		return arrivalTime;
	}

	public Integer getDepartureTime() {
		return departureTime;
	}

	public String getStopId() {
		return stopId;
	}
	
	public String getStopName() {
		return stopName;
	}
	
}
