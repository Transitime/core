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

import javax.xml.bind.annotation.XmlAttribute;

import org.transitclock.utils.Time;

/**
 * Represents a schedule time for a stop. Intended to be used for displaying a
 * schedule for a route.
 *
 * @author SkiBu Smith
 *
 */
public class ApiScheduleTime {

    @XmlAttribute
    private String timeStr;

    @XmlAttribute
    private Integer timeSecs;

	/********************** Member Functions **************************/

	/**
	 * Need a no-arg constructor for Jersey. Otherwise get really obtuse
	 * "MessageBodyWriter not found for media type=application/json" exception.
	 */
	protected ApiScheduleTime() {
	}
	
	public ApiScheduleTime(Integer time) {
		this.timeStr = time == null ? null : Time.timeOfDayShortStr(time);
		this.timeSecs = time;
	}

}
