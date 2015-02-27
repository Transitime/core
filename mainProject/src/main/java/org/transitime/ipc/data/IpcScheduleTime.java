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

/**
 *
 *
 * @author SkiBu Smith
 *
 */
public class IpcScheduleTime implements Serializable {

	private final String stopId;
	private final String stopName;
	private final int timeOfDay;
	
	private static final long serialVersionUID = 5022156970470667431L;

	/********************** Member Functions **************************/

	/**
	 * @param stopId
	 * @param timeOfDay
	 */
	public IpcScheduleTime(String stopId, String stopName, int timeOfDay) {
		super();
		this.stopId = stopId;
		this.stopName = stopName;
		this.timeOfDay = timeOfDay;
	}

	@Override
	public String toString() {
		return "IpcScheduleTime [" 
				+ "stopId=" + stopId 
				+ ", stopName=" + stopName
				+ ", timeOfDay=" + timeOfDay
				+ "]";
	}

	public String getStopId() {
		return stopId;
	}

	public String getStopName() {
		return stopName;
	}
	
	public int getTimeOfDay() {
		return timeOfDay;
	}

}
