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
import javax.xml.bind.annotation.XmlRootElement;

import org.transitclock.ipc.data.IpcTrip;
import org.transitclock.utils.Time;

/**
 * A shorter version of ApiTrip for when all the detailed info is not
 * needed.
 *
 * @author SkiBu Smith
 *
 */
@XmlRootElement(name = "trip")
public class ApiTripTerse {

	@XmlAttribute
	private String id;

	@XmlAttribute
	private String shortName;

	@XmlAttribute
	private String startTime;

	@XmlAttribute
	private String endTime;

	@XmlAttribute
	private String directionId;

	@XmlAttribute
	private String headsign;

	@XmlAttribute
	private String routeId;

	@XmlAttribute
	private String routeShortName;

	/********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse
     * "MessageBodyWriter not found for media type=application/json" exception.
     */
	protected ApiTripTerse() {}
	
	public ApiTripTerse(IpcTrip ipcTrip) {
		id = ipcTrip.getId();
		shortName = ipcTrip.getShortName();
		startTime = Time.timeOfDayStr(ipcTrip.getStartTime());
		endTime = Time.timeOfDayStr(ipcTrip.getEndTime());
		directionId = ipcTrip.getDirectionId();
		headsign = ipcTrip.getHeadsign();
		routeId = ipcTrip.getRouteId();
		routeShortName = ipcTrip.getRouteShortName();
	}
	
	public String getRouteId() {
		return routeId;
	}
}
