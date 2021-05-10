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

import org.transitclock.applications.Core;
import org.transitclock.db.structs.Extent;
import org.transitclock.db.structs.StopPath;
import org.transitclock.db.structs.TripPattern;

/**
 * Configuration information for a TripPattern. For IPC.
 *
 * @author SkiBu Smith
 *
 */
public class IpcTripPattern implements Serializable {

	private final int configRev;
	private final String id;
	private final String headsign;
	private final String directionId;
	private final String directionName;
	private final String routeId;
	private final String routeShortName;
	private final Extent extent;
	private final String shapeId;
	private final String firstStopName;
	private final String lastStopName;
	private final List<IpcStopPath> stopPaths;

	private static final long serialVersionUID = 5631162916487757340L;

	/********************** Member Functions **************************/

	public IpcTripPattern(TripPattern dbTripPattern) {
		this.configRev = dbTripPattern.getConfigRev();
		this.id = dbTripPattern.getId();
		this.headsign = dbTripPattern.getHeadsign();
		this.directionId = dbTripPattern.getDirectionId();
		this.directionName = getDirectionName(dbTripPattern.getRouteShortName(), dbTripPattern.getDirectionId());
		this.routeId = dbTripPattern.getRouteId();
		this.routeShortName = dbTripPattern.getRouteShortName();
		this.extent = dbTripPattern.getExtent();
		this.shapeId = dbTripPattern.getShapeId();
		this.firstStopName = dbTripPattern.getStopName(0);
		this.lastStopName = dbTripPattern.getStopName(dbTripPattern.getNumberStopPaths() - 1);

		
		this.stopPaths = new ArrayList<IpcStopPath>();
		for (StopPath stopPath : dbTripPattern.getStopPaths())
			this.stopPaths.add(new IpcStopPath(stopPath));
	}

	private String getDirectionName(String routeShortName, String directionId) {
		return Core.getInstance().getDbConfig().getDirectionName(routeShortName, directionId);
	}

	@Override
	public String toString() {
		return "IpcTripPattern [" 
				+ "configRev=" + configRev 
				+ ", id=" + id
				+ ", headsign=" + headsign 
				+ ", directionId=" + directionId
				+ ", directionName" + directionName
				+ ", routeId=" + routeId 
				+ ", routeShortName=" + routeShortName
				+ ", extent=" + extent 
				+ ", shapeId=" + shapeId
				+ ", stopPaths=" + stopPaths 
				+ "]";
	}

	public int getConfigRev() {
		return configRev;
	}

	public String getId() {
		return id;
	}

	public String getHeadsign() {
		return headsign;
	}

	public String getDirectionId() {
		return directionId;
	}

	public String getDirectionName() {
		return directionName;
	}

	public String getRouteId() {
		return routeId;
	}

	public String getRouteShortName() {
		return routeShortName;
	}

	public Extent getExtent() {
		return extent;
	}
	
	public String getShapeId() {
		return shapeId;
	}

	public String getFirstStopName() {
		return firstStopName;
	}

	public String getLastStopName() {
		return lastStopName;
	}

	public List<IpcStopPath> getStopPaths() {
		return stopPaths;
	}

	
}
