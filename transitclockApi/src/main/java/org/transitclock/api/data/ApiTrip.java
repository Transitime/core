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
import javax.xml.bind.annotation.XmlRootElement;

import org.transitclock.ipc.data.IpcSchedTimes;
import org.transitclock.ipc.data.IpcTrip;
import org.transitclock.utils.Time;

/**
 * Specifies how trip data is formatted for the API.
 *
 * @author SkiBu Smith
 *
 */
@XmlRootElement(name = "trip")
public class ApiTrip {

	@XmlAttribute
	private int configRev;

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
	private String routeId;

	@XmlAttribute
	private String routeShortName;

	@XmlElement
	private ApiTripPattern tripPattern;

	@XmlAttribute
	private String serviceId;

	@XmlAttribute
	private String headsign;

	@XmlAttribute
	private String blockId;

	@XmlAttribute
	private String shapeId;

	// Using a Boolean so that will be output only if true
	@XmlAttribute
	private Boolean noSchedule;
	
	@XmlElement(name = "schedule")
	private List<ApiScheduleArrDepTime> scheduleTimes;

	/********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse
     * "MessageBodyWriter not found for media type=application/json" exception.
     */
	protected ApiTrip() {
	}

	/**
	 * 
	 * @param ipcTrip
	 * @param includeStopPaths
	 *            Stop paths are only included in output if this param set to
	 *            true.
	 */
	public ApiTrip(IpcTrip ipcTrip, boolean includeStopPaths) {
		configRev = ipcTrip.getConfigRev();
		id = ipcTrip.getId();
		shortName = ipcTrip.getShortName();
		startTime = Time.timeOfDayStr(ipcTrip.getStartTime());
		endTime = Time.timeOfDayStr(ipcTrip.getEndTime());
		directionId = ipcTrip.getDirectionId();
		routeId = ipcTrip.getRouteId();
		routeShortName = ipcTrip.getRouteShortName();
		tripPattern = new ApiTripPattern(ipcTrip.getTripPattern(),
				includeStopPaths);
		serviceId = ipcTrip.getServiceId();
		headsign = ipcTrip.getHeadsign();
		blockId = ipcTrip.getBlockId();
		shapeId = ipcTrip.getShapeId();

		noSchedule = ipcTrip.isNoSchedule() ? true : null;

		scheduleTimes = new ArrayList<ApiScheduleArrDepTime>();
		for (IpcSchedTimes ipcScheduleTimes : ipcTrip.getScheduleTimes()) {
			scheduleTimes.add(new ApiScheduleArrDepTime(ipcScheduleTimes));
		}

	}
}
