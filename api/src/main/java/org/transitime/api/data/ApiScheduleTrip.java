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

import org.transitime.ipc.data.IpcScheduleTime;
import org.transitime.ipc.data.IpcScheduleTrip;

/**
 * Represents a single trip for an ApiSchedule
 *
 * @author SkiBu Smith
 *
 */
public class ApiScheduleTrip {

	@XmlAttribute
	private String tripId;

	@XmlAttribute
	private String blockId;

	@XmlElement(name = "time")
	private List<ApiScheduleTime> times;
	
	/********************** Member Functions **************************/

	public ApiScheduleTrip(IpcScheduleTrip ipcScheduleTrip) {
		this.tripId = ipcScheduleTrip.getTripId();
		this.blockId = ipcScheduleTrip.getBlockId();
		
		this.times = new ArrayList<ApiScheduleTime>();
		for (IpcScheduleTime ipcScheduleTime : ipcScheduleTrip.getScheduleTimes()) {
			this.times.add(new ApiScheduleTime(ipcScheduleTime));
		}
	}
}
