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
import java.util.Collection;
import java.util.List;

import org.transitclock.applications.Core;
import org.transitclock.db.structs.Route;
import org.transitclock.db.structs.Stop;
import org.transitclock.db.structs.TripPattern;

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

	/**
	 * Constructor. All IpcStops are marked as being a normal UiMode stop.
	 * 
	 * @param dbRoute
	 * @param directionId
	 */
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
	
	/**
	 * Constructor for when already have list of IpcStops. Useful for when have
	 * already determined whether an IpcStop is for UiMode or note.
	 * 
	 * @param dbRoute
	 * @param directionId
	 * @param ipcStops
	 */
	public IpcDirection(Route dbRoute, String directionId, List<IpcStop> ipcStops) {
		this.directionId = directionId;

		// Use the headsign name for the longest trip pattern for the 
		// specified direction. Note: this isn't necessarily the best thing
		// to use but there is no human readable direction name specified in 
		// GTFS.
		TripPattern longestTripPattern = 
				dbRoute.getLongestTripPatternForDirection(directionId);
		this.directionTitle = "To " + longestTripPattern.getHeadsign();
		this.stops = ipcStops;
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
