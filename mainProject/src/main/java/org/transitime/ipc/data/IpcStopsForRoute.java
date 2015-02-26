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

import org.transitime.db.structs.Route;

/**
 *
 *
 * @author SkiBu Smith
 *
 */
public class IpcStopsForRoute implements Serializable {

	private List<IpcDirection> directions;
	
	private static final long serialVersionUID = -3112277760645758349L;

	/********************** Member Functions **************************/

	public IpcStopsForRoute(Route dbRoute) {
		directions = new ArrayList<IpcDirection>();
		
		// Determine the directions
		List<String> directionIds = dbRoute.getDirectionIds();
		
		// For each directionId...
		for (String directionId : directionIds) {
			IpcDirection ipcDirection = new IpcDirection(dbRoute, directionId);
			directions.add(ipcDirection);
		}
	}
	
	public IpcStopsForRoute(List<IpcDirection> directions) {
		this.directions = directions;
	}
	
	@Override
	public String toString() {
		return "IpcStopsForRoute [" 
				+ "directions=" + directions 
				+ "]";
	}

	public List<IpcDirection> getDirections() {
		return directions;
	}

}
