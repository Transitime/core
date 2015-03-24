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
	 * Constructor. Goes through the complete ordered list of stops for the
	 * direction and creates the corresponding IpcSchedTime objects for each
	 * one, even if there is no scheduled time for that stop for the specified
	 * trip.
	 * 
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
		List<String> orderedStopIds = trip.getRoute().getOrderedStopsByDirection()
				.get(trip.getDirectionId());

		// Set the schedule times for each ordered stop for the route/direction.
		// If no scheduled time for the stop then use a null time.
		int currentStopIdxInOrderedStops = 0;
		for (int stopIdxInTrip=0; 
				stopIdxInTrip<trip.getNumberStopPaths(); 
				++stopIdxInTrip) {
			StopPath stopPathInTrip = trip.getStopPath(stopIdxInTrip);
			String stopIdInTrip = stopPathInTrip.getStopId();
			
			// Find the stop from the schedules for the trip in the list of 
			// ordered stops...
			int stopIdxInOrderedStops;
			for (stopIdxInOrderedStops=currentStopIdxInOrderedStops; 
					stopIdxInOrderedStops<orderedStopIds.size(); 
					++stopIdxInOrderedStops) {
				// If found the schedule time in the trip then use it
				if (orderedStopIds.get(stopIdxInOrderedStops).equals(
						stopIdInTrip)) {					
					// Add a null schedule times needed for when there is an ordered
					// stop but no corresponding schedule time
					for (int i=currentStopIdxInOrderedStops; i<stopIdxInOrderedStops; ++i) {
						// Create a IpcScheduleTime with a time of null so can still
						// be added to the schedule trip.
						String stopId = orderedStopIds.get(i);
						addNullScheduleTime(stopId);
					}
					
					// Update currentStopIdxInOrderedStops since have dealt with up to 
					// this stop
					currentStopIdxInOrderedStops = stopIdxInOrderedStops + 1;

					ScheduleTime scheduleTime = trip
							.getScheduleTime(stopIdxInTrip);
					IpcSchedTime ipcScheduleTime = new IpcSchedTime(stopIdInTrip,
							stopPathInTrip.getStopName(),
							scheduleTime.getTime());
					scheduleTimes.add(ipcScheduleTime);
					
					// Done with this stop in the trip so continue to the 
					// next one
					break;
				}
			}
		}
			
		// For remaining ordered stops where there wasn't a schedule time add a 
		// null schedule times
		for (int i = currentStopIdxInOrderedStops; i < orderedStopIds.size(); ++i) {
			// Create a IpcScheduleTime with a time of null so can still
			// be added to the schedule trip.
			String stopId = orderedStopIds.get(i);
			addNullScheduleTime(stopId);
		}

		//FIXME
//		// Go through all the ordered stops for the route/direction and
//		// create corresponding IpcScheduleTime
//		int stopIdxInTrip = 0;
//		for (String stopId : orderedStopIds) {
//			// Find stop in schedule for trip that corresponds to the
//			// current stop from the ordered stops by direction. If
//			// no corresponding stop then ipcScheduleTime will be null.
//			IpcSchedTime ipcScheduleTime = null;
//			// Go through stops in trip to find corresponding one to get 
//			// schedule time
//			while (stopIdxInTrip < trip.getNumberStopPaths()) {
//				StopPath stopPathInTrip = trip.getStopPath(stopIdxInTrip);
//				String stopIdInTrip = stopPathInTrip.getStopId();
//				
//				// If found corresponding stop in schedule...
//				if (stopId.equals(stopIdInTrip)) {
//					// If corresponding stop in schedule actually has a time...
//					ScheduleTime scheduleTime = trip
//							.getScheduleTime(stopIdxInTrip);
//					if (scheduleTime != null) {
//						ipcScheduleTime = new IpcSchedTime(stopId,
//								stopPathInTrip.getStopName(),
//								scheduleTime.getTime());
//					}
//					
//					// Determined corresponding schedule time for stop so 
//					// continue on to next stop in the ordered stops for 
//					// direction for the route.
//					++stopIdxInTrip;
//					break;
//				}
//
//				// Corresponding stop in trip not found so try next one
//				++stopIdxInTrip;
//			}
//			
//			// If the stop from the ordered stops from the route/direction
//			// didn't have a corresponding stop in the trip then still add
//			// add a IpcScheduleTime to the trip, but with a null time.
//			// This way all trips for a schedule will have the same stops.
//			// Just some will have a null time.
//			if (ipcScheduleTime == null) {
//				// Create a IpcScheduleTime with a time of null so can still
//				// be added to the schedule trip.
//				String stopName = Core.getInstance().getDbConfig()
//						.getStop(stopId).getName();
//				ipcScheduleTime = new IpcSchedTime(stopId, stopName, null);
//			}
//			
//			// Add the (possibly null) schedule time
//			scheduleTimes.add(ipcScheduleTime);
//		}
	}

	private void addNullScheduleTime(String stopId) {
		// Create a IpcScheduleTime with a time of null so can still
		// be added to the schedule trip.
		String stopName = Core.getInstance().getDbConfig()
				.getStop(stopId).getName();
		IpcSchedTime ipcScheduleTime = new IpcSchedTime(stopId, stopName, null);
		scheduleTimes.add(ipcScheduleTime);				

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
