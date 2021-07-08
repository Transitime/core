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

/**
 * Contains minimal for a stop for a schedule
 * 
 * @author Michael
 *
 */
public class ApiScheduleStop {
	@XmlAttribute
	private String stopId;
	
	@XmlAttribute
	private String stopName;
	
	/**
	 * Need a no-arg constructor for Jersey. Otherwise get really obtuse
	 * "MessageBodyWriter not found for media type=application/json" exception.
	 */
	protected ApiScheduleStop() {
	}
	
	public ApiScheduleStop(String stopId, String stopName) {
		this.stopId = stopId;
		this.stopName = stopName;
	}
}
