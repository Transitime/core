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
package org.transitime.db.structs;

import java.io.Serializable;

import org.transitime.utils.Time;


/**
 * For keeping track of schedule times from GTFS data. Either arrival
 * time or departure could be null.
 * 
 * @author SkiBu Smith
 */
public class ScheduleTime implements Serializable {

	// Times are in seconds
	private final Integer arrivalTime;
	private final Integer departureTime;

	// Because using serialization to store array of ScheduleTimes
	// this class needs to be Serializable.
	private static final long serialVersionUID = 7480539886372288095L;

	/********************** Member Functions **************************/

	public ScheduleTime(Integer arrivalTime, Integer departureTime) {
		this.arrivalTime = arrivalTime;
		this.departureTime = departureTime;
	}
	
	public Integer getTime() { 
		if (departureTime != null)
			return departureTime;
		return arrivalTime;
	}
	
	/**
	 * Time of day in seconds. Can be null.
	 * @return
	 */
	public Integer getArrivalTime() {
		return arrivalTime;
	}
	
	/**
	 * Time of day in seconds. Can be null.
	 * @return
	 */
	public Integer getDepartureTime() {
		return departureTime;
	}
	
	@Override
	public String toString() {
		return "ScheduleTime [" + 
				(arrivalTime != null? "a=" + Time.timeOfDayStr(arrivalTime) : "") +
				(arrivalTime != null && departureTime != null ? ", " : "") +
				(departureTime != null? "d=" + Time.timeOfDayStr(departureTime) : "") +
				"]";
	}
	
	
}
