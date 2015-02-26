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
import java.util.Collection;
import java.util.List;

import org.transitime.applications.Core;
import org.transitime.db.structs.Route;
import org.transitime.db.structs.Stop;
import org.transitime.db.structs.TripPattern;

/**
 *
 *
 * @author SkiBu Smith
 *
 */
public class IpcDirection implements Serializable {

	private String directionId;
	private String directionTitle;
	private Collection<IpcStop> stops;

	private static final long serialVersionUID = -7843832825147865279L;

	/********************** Member Functions **************************/

	public IpcDirection(Route dbRoute, String directionId) {
		this.directionId = directionId;

		// Use the headsign name for the longest trip pattern for the 
		// specified direction. Note: this isn't necessarily the best thing
		// to use but there is no human readable direction name specified in 
		// GTFS.
		TripPattern longestTripPattern = 
				dbRoute.getLongestTripPatternForDirection(directionId);
		this.directionTitle = "To " + longestTripPattern.getHeadsign();
		
		// Determine ordered list of stops
		this.stops = new ArrayList<IpcStop>();
		List<String> stopIds = dbRoute.getOrderedStopsByDirection().get(directionId);
		for (String stopId : stopIds) {
			Stop stop = Core.getInstance().getDbConfig().getStop(stopId);
			this.stops.add(new IpcStop(stop, directionId));
		}
	}
	
	@Override
	public String toString() {
		return "IpcDirection [" 
				+ "directionId=" + directionId 
				+ ", directionTitle=" + directionTitle 
				+ ", stops=" + stops 
				+ "]";
	}

	public String getDirectionId() {
		return directionId;
	}

	public String getDirectionTitle() {
		return directionTitle;
	}

	public Collection<IpcStop> getStops() {
		return stops;
	}
}
