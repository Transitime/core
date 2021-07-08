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

import org.transitclock.db.structs.Route;

/**
 * Contains each direction for route, along with each stop for each direction.
 *
 * @author SkiBu Smith
 *
 */
public class IpcDirectionsForRoute implements Serializable {

	private String routeId;

	private String routeShortName;

	private List<IpcDirection> directions;
	
	private static final long serialVersionUID = -3112277760645758349L;

	/********************** Member Functions **************************/

	public IpcDirectionsForRoute(Route dbRoute) {
		this.routeId = dbRoute.getId();
		this.routeShortName = dbRoute.getShortName();

		directions = new ArrayList<IpcDirection>();
		
		// Determine the directions
		List<String> directionIds = dbRoute.getDirectionIds();
		
		// For each directionId...
		for (String directionId : directionIds) {
			IpcDirection ipcDirection = new IpcDirection(dbRoute, directionId);
			directions.add(ipcDirection);
		}
	}
	
	public IpcDirectionsForRoute(List<IpcDirection> directions) {
		this.directions = directions;
	}
	
	@Override
	public String toString() {
		return "IpcDirectionsForRoute [" 
				+ "directions=" + directions 
				+ "]";
	}

	public List<IpcDirection> getDirections() {
		return directions;
	}

	public String getRouteId() {
		return routeId;
	}

	public String getRouteShortName() {
		return routeShortName;
	}
}
