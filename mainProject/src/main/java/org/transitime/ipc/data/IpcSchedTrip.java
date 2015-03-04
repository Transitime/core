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

import org.transitime.applications.Core;
import org.transitime.db.structs.ScheduleTime;
import org.transitime.db.structs.StopPath;
import org.transitime.db.structs.Trip;

/**
 * For describing a trip as part of a schedule
 *
 * @author SkiBu Smith
 *
 */
public class IpcSchedTrip implements Serializable {

	private final String blockId;
	private final String tripId;
	private final String tripShortName;
	private final String tripHeadsign;
	private final List<IpcSchedTime> scheduleTimes;
	
	private static final long serialVersionUID = 4410014384520957092L;

	/********************** Member Functions **************************/

	/**
	 * @param blockId
	 * @param tripId
	 * @param scheduleTimes
	 */
	public IpcSchedTrip(Trip trip) {
		super();
		
		this.blockId = trip.getBlockId();
		this.tripId = trip.getId();
		this.tripShortName = trip.getShortName();
		this.tripHeadsign = trip.getHeadsign();
		this.scheduleTimes = new ArrayList<IpcSchedTime>();

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
			IpcSchedTime ipcScheduleTime = null;
			// Go through stops in trip to find corresponding one to get schedule time
			for (int i=0; i<trip.getNumberStopPaths(); ++i) {
				StopPath stopPathInTrip = trip.getStopPath(i);
				String stopIdInTrip = stopPathInTrip.getStopId();
				
				// If found corresponding stop in schedule...
				if (stopId.equals(stopIdInTrip)) {
					// If corresponding stop in schedule actually has a time...
					ScheduleTime scheduleTime = trip.getScheduleTime(i);
					if (scheduleTime != null) {
						ipcScheduleTime = new IpcSchedTime(stopId,
								stopPathInTrip.getStopName(),
								scheduleTime.getTime());
					}
					
					// Determined corresponding schedule time for stop so 
					// continue on to next stop in the ordered stops for 
					// direction for the route.
					break;
				}
			}
			
			// If the stop from the ordered stops from the route/direction
			// didn't have a corresponding stop in the trip then still add
			// add a IpcScheduleTime to the trip, but with a null time.
			// This way all trips for a schedule will have the same stops.
			// Just some will have a null time.
			if (ipcScheduleTime == null) {
				// Create a IpcScheduleTime with a time of null so can still
				// be added to the schedule trip.
				String stopName = Core.getInstance().getDbConfig()
						.getStop(stopId).getName();
				ipcScheduleTime = new IpcSchedTime(stopId, stopName, null);
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
				+ ", tripShortName=" + tripShortName
				+ ", scheduleTimes=" + scheduleTimes 
				+ "]";
	}

	public String getBlockId() {
		return blockId;
	}

	public String getTripId() {
		return tripId;
	}

	public String getTripShortName() {
		return tripShortName;
	}
	
	public String getTripHeadsign() {
		return tripHeadsign;
	}
	
	public List<IpcSchedTime> getSchedTimes() {
		return scheduleTimes;
	}

	
}
