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
import java.util.ArrayList;
import java.util.List;

import org.transitime.db.structs.ScheduleTime;
import org.transitime.db.structs.StopPath;
import org.transitime.db.structs.Trip;

/**
 * For describing a trip as part of a schedule
 *
 * @author SkiBu Smith
 *
 */
public class IpcScheduleTrip implements Serializable {

	private final String blockId;
	private final String tripId;
	private final List<IpcScheduleTime> scheduleTimes;
	
	private static final long serialVersionUID = 4410014384520957092L;

	/********************** Member Functions **************************/

	/**
	 * @param blockId
	 * @param tripId
	 * @param scheduleTimes
	 */
	public IpcScheduleTrip(Trip trip) {
		super();
		
		this.blockId = trip.getBlockId();
		this.tripId = trip.getId();
		this.scheduleTimes = new ArrayList<IpcScheduleTime>();

		// Actually fill in the schedule times		
		// First, get list of ordered stop IDs for the direction
		List<String> stopIds = trip.getRoute().getOrderedStopsByDirection()
				.get(trip.getDirectionId());

		// Go through all the ordered stops for the route/direction and
		// create corresponding IpcScheduleTime
		for (String stopId : stopIds) {
			// Find stop in schedule for trip that corresponds to the
			// current stop from the ordered stops by direction. If
			// no corresponding stop then ipcScheduleTime will be null.
			IpcScheduleTime ipcScheduleTime = null;
			// Go through stops in trip to find corresponding one to get schedule time
			for (int i=0; i<trip.getNumberStopPaths(); ++i) {
				StopPath stopPathInTrip = trip.getStopPath(i);
				String stopIdInTrip = stopPathInTrip.getStopId();
				
				// If found corresponding stop in schedule...
				if (stopId.equals(stopIdInTrip)) {
					// If corresponding stop in schedule actually has a time...
					ScheduleTime scheduleTime = trip.getScheduleTime(i);
					if (scheduleTime != null) {
						ipcScheduleTime = new IpcScheduleTime(stopId,
								stopPathInTrip.getStopName(),
								scheduleTime.getTime());
					}
					
					// Determined corresponding schedule time for stop so 
					// continue on to next stop in the ordered stops for 
					// direction for the route.
					break;
				}
			}
			
			// Add the (possibly null) schedule time
			scheduleTimes.add(ipcScheduleTime);
		}
	}

	@Override
	public String toString() {
		return "IpcScheduleTrip [" 
				+ "blockId=" + blockId 
				+ ", tripId=" + tripId
				+ ", scheduleTimes=" + scheduleTimes 
				+ "]";
	}

	public String getBlockId() {
		return blockId;
	}

	public String getTripId() {
		return tripId;
	}

	public List<IpcScheduleTime> getScheduleTimes() {
		return scheduleTimes;
	}

	
}
