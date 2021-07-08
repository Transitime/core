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

package org.transitclock.ipc.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.transitclock.db.structs.TravelTimesForTrip;
import org.transitclock.db.structs.Trip;
import org.transitclock.utils.Time;

/**
 * Configuration information for a Trip for IPC.
 *
 * @author SkiBu Smith
 *
 */
public class IpcTrip implements Serializable {

	private final int configRev;
	private final String id;
	private final String shortName;
	private Integer startTime;
	private Integer endTime;
	private final String directionId;
	private final String routeId;
	private final String routeShortName;
	private final String routeName;
	private final IpcTripPattern tripPattern;
	private final String serviceId;
	private final String headsign;
	private final String blockId;
	private final String shapeId;
	
	private final boolean noSchedule;
	private final List<IpcSchedTimes> scheduleTimes;
	private final TravelTimesForTrip travelTimes;
	
	private static final long serialVersionUID = 6369021345710182247L;

	/********************** Member Functions **************************/

	public IpcTrip(Trip dbTrip) {
		configRev = dbTrip.getConfigRev();
		id = dbTrip.getId();
		shortName = dbTrip.getShortName();
		startTime = dbTrip.getStartTime();
		endTime = dbTrip.getEndTime();
		directionId = dbTrip.getDirectionId();
		routeId = dbTrip.getRouteId();
		routeShortName = dbTrip.getRouteShortName();
		routeName = dbTrip.getRouteName();
		tripPattern = new IpcTripPattern(dbTrip.getTripPattern());
		serviceId = dbTrip.getServiceId();
		headsign = dbTrip.getHeadsign();
		blockId = dbTrip.getBlockId();
		shapeId = dbTrip.getShapeId();
		
		noSchedule = dbTrip.isNoSchedule();
		scheduleTimes = new ArrayList<IpcSchedTimes>();
		for (int i=0; i<dbTrip.getNumberStopPaths(); ++i) {
			String stopId = dbTrip.getTripPattern().getStopId(i);
			scheduleTimes.add(new IpcSchedTimes(dbTrip.getScheduleTime(i), stopId));
		}
		travelTimes = dbTrip.getTravelTimes();
	}

	@Override
	public String toString() {
		return "IpcTrip [" 
				+ "configRev=" + configRev 
				+ ", id=" + id
				+ ", shortName=" + shortName
				+ ", startTime=" + Time.timeOfDayStr(startTime) 
				+ ", endTime=" + Time.timeOfDayStr(endTime)
				+ ", directionId=" + directionId 
				+ ", routeId=" + routeId
				+ ", routeShortName=" + routeShortName
				+ ", routeName=" + routeName 
				+ ", tripPattern=" + tripPattern.getId() 
				+ ", serviceId=" + serviceId 
				+ ", headsign="	+ headsign 
				+ ", blockId=" + blockId 
				+ ", shapeId=" + shapeId
				+ ", noSchedule=" + noSchedule
				+ ", scheduleTimes=" + scheduleTimes
				+ ", travelTimes=" + travelTimes
				+ "]";
	}

	public int getConfigRev() {
		return configRev;
	}

	public String getId() {
		return id;
	}

	public String getShortName() {
		return shortName;
	}
	
	public Integer getStartTime() {
		return startTime;
	}

	public Integer getEndTime() {
		return endTime;
	}

	public String getDirectionId() {
		return directionId;
	}

	public String getRouteId() {
		return routeId;
	}

	public String getRouteShortName() {
		return routeShortName;
	}
	
	public String getRouteName() {
		return routeName;
	}

	public IpcTripPattern getTripPattern() {
		return tripPattern;
	}

	public String getServiceId() {
		return serviceId;
	}

	public String getHeadsign() {
		return headsign;
	}

	public String getBlockId() {
		return blockId;
	}

	public String getShapeId() {
		return shapeId;
	}

	public boolean isNoSchedule() {
		return noSchedule;
	}
	
	public List<IpcSchedTimes> getScheduleTimes() {
		return scheduleTimes;
	}
	
	public TravelTimesForTrip getTravelTimes() {
	    return travelTimes;
	}
	
}
