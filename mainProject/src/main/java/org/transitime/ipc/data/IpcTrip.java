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

import org.transitime.db.structs.Trip;

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
	private final IpcTripPattern tripPattern;
	private final String serviceId;
	private final String headsign;
	private final String blockId;
	private final String shapeId;
	
	private final List<IpcScheduleTimes> scheduleTimes;

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
		routeShortName = dbTrip.getRouteName();
		tripPattern = new IpcTripPattern(dbTrip.getTripPattern());
		serviceId = dbTrip.getServiceId();
		headsign = dbTrip.getHeadsign();
		blockId = dbTrip.getBlockId();
		shapeId = dbTrip.getShapeId();
		
		scheduleTimes = new ArrayList<IpcScheduleTimes>();
		for (int i=0; i<dbTrip.getNumberStopPaths(); ++i) {
			String stopId = dbTrip.getTripPattern().getStopId(i);
			scheduleTimes.add(new IpcScheduleTimes(dbTrip.getScheduleTime(stopId), stopId));
		}
	}

	@Override
	public String toString() {
		return "IpcTrip [" 
				+ "configRev=" + configRev 
				+ ", id=" + id
				+ ", shortName=" + shortName
				+ ", startTime=" + startTime 
				+ ", endTime=" + endTime
				+ ", directionId=" + directionId 
				+ ", routeId=" + routeId
				+ ", routeShortName=" + routeShortName 
				+ ", tripPattern=" + tripPattern.getId() 
				+ ", serviceId=" + serviceId 
				+ ", headsign="	+ headsign 
				+ ", blockId=" + blockId 
				+ ", shapeId=" + shapeId
				+ ", scheduleTimes=" + scheduleTimes 
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

	public List<IpcScheduleTimes> getScheduleTimes() {
		return scheduleTimes;
	}
}
