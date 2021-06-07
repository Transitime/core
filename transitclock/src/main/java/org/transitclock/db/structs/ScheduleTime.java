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
package org.transitclock.db.structs;

import javax.persistence.Embeddable;

import org.transitclock.utils.Time;

import java.io.Serializable;


/**
 * For keeping track of schedule times from GTFS data. Either arrival
 * time or departure could be null.
 * 
 * @author SkiBu Smith
 */
@Embeddable
public class ScheduleTime implements Serializable {

	// Times are in seconds. arrivalTime only set for last
	// stop in trip. Otherwise only departure time is set.
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

    protected ScheduleTime() {
        arrivalTime = null;
        departureTime = null;
    }

    /**
	 * Returns departure time if there is one. Otherwise returns arrival time if
	 * there is one. Otherwise returns null.
	 * 
	 * @return
	 */
	public Integer getTime() { 
		if (departureTime != null)
			return departureTime;
		return arrivalTime;
	}

	/**
	 * Returns arrival time if there is one. Otherwise returns departure time if
	 * there is one. Otherwise returns null.
	 *
	 * @return
	 */
	public Integer getArrivalOrDepartureTime(){
		if (arrivalTime != null)
			return arrivalTime;
		return departureTime;
	}
	
	/**
	 * Time of day in seconds. Will be null if there is no arrival time (even
	 * if there is a departure time). There will be no arrival time unless
	 * it is last stop in trip.
	 * 
	 * @return
	 */
	public Integer getArrivalTime() {
		return arrivalTime;
	}
	
	/**
	 * Time of day in seconds. Will be null if there is no departure time (even
	 * if there is an arrival time). There will be no departure time if last
	 * stop of trip.
	 * 
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
