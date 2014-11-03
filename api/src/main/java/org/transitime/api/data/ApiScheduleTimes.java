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

import javax.xml.bind.annotation.XmlElement;

import org.transitime.ipc.data.IpcScheduleTimes;
import org.transitime.ipc.data.IpcTrip;

/**
 * Represents a group of schedule times for a trip
 *
 * @author SkiBu Smith
 *
 */
public class ApiScheduleTimes {

    @XmlElement(name="scheduleTime")
    private List<ApiScheduleTime> scheduleTimes;

    /********************** Member Functions **************************/

    public ApiScheduleTimes(IpcTrip ipcTrip) {
	scheduleTimes = new ArrayList<ApiScheduleTime>();
	for (IpcScheduleTimes ipcScheduleTimes : ipcTrip.getScheduleTimes()) {
	    scheduleTimes.add(new ApiScheduleTime(ipcScheduleTimes));
	}

    }
}
