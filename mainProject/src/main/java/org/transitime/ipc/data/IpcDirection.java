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
import org.transitime.utils.OrderedCollection;

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
		TripPattern longestTripPattern = 
				dbRoute.getLongestTripPatternForDirection(directionId);

		this.directionId = directionId;
		// Use the headsign name for the longest trip pattern for the 
		// specified direction. Note: this isn't necessarily the best thing
		// to use but there is no human readable direction name specified in 
		// GTFS.
		this.directionTitle = "To " + longestTripPattern.getHeadsign(); 
		this.stops = 
				getOrderedStopList(dbRoute, directionId, longestTripPattern);
	}
	
	/**
	 * Goes through all the trip patterns for the specified direction and
	 * returns list of IpcStops in the proper order. 
	 * 
	 * @param dbRoute
	 * @param directionId
	 * @param longestTripPattern
	 * @return
	 */
	private List<IpcStop> getOrderedStopList(Route dbRoute,
			String directionId, TripPattern longestTripPattern) {
		OrderedCollection orderedCollection = new OrderedCollection();
		
		// Start with the stops for the longest trip pattern
		orderedCollection.addOriginal(longestTripPattern.getStopIds());
		
		// For other trip patterns for the direction that are not the longest
		// add any missing stops.
		for (TripPattern tripPattern : dbRoute.getTripPatterns(directionId)) {
			// If this isn't the longest trip pattern then add any new stops
			if (tripPattern.getId() != longestTripPattern.getId()) {
				orderedCollection.add(tripPattern.getStopIds());
			}
		}

		// Convert list of stop IDs to list of IpcStops	
		List<IpcStop> stops = new ArrayList<IpcStop>(); 
		for (String stopId : orderedCollection.get()) {
			Stop stop = Core.getInstance().getDbConfig().getStop(stopId);
			if (!stop.isHidden())
				stops.add(new IpcStop(stop));
		}
		
		// Return result
		return stops;
	}

	@Override
	public String toString() {
		return "IpcDirection [" 
				+ "directionId=" + directionId 
				+ ", directionTitle=" + directionTitle 
				+ ", stops=" + stops 
				+ "]";
	}
}
