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

/**
 *
 *
 * @author SkiBu Smith
 *
 */
public class ApiScheduleTimesForStop {

	@XmlAttribute
	private String stopId;

	@XmlAttribute
	private String stopName;

	@XmlElement(name = "time")
	private List<ApiScheduleTime> times;

	/********************** Member Functions **************************/

	/**
	 * Need a no-arg constructor for Jersey. Otherwise get really obtuse
	 * "MessageBodyWriter not found for media type=application/json" exception.
	 */
	protected ApiScheduleTimesForStop() {
	}

	public ApiScheduleTimesForStop(String stopId, String stopName) {
		this.stopId = stopId;
		this.stopName = stopName;
		this.times = new ArrayList<ApiScheduleTime>();
	}
	
	public void add(Integer time) {
		this.times.add(new ApiScheduleTime(time));
	}
}
