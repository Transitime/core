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

import java.util.ArrayList;
import java.util.Collection;

import org.transitime.db.structs.Vector;

/**
 *
 *
 * @author SkiBu Smith
 *
 */
public class Route extends RouteSummary {

	private Collection<Stop> stops;
	private Collection<Vector> segments;
	
	private static final long serialVersionUID = -227901807027962547L;

	/********************** Member Functions **************************/

	public Route(org.transitime.db.structs.Route dbRoute) {
		super(dbRoute);
		
		// Create Collection of Stop objects
		Collection<org.transitime.db.structs.Stop> dbStops = dbRoute.getStops();
		stops = new ArrayList<Stop>(dbStops.size());
		for (org.transitime.db.structs.Stop dbStop : dbStops) {
			stops.add(new Stop(dbStop));
		}
		
		// Create Collection of path segment vector objects
		segments = dbRoute.getPathSegments();
	}

	@Override
	public String toString() {
		return "Route [" 
				+ "stops=" + stops 
				+ ", id=" + id 
				+ ", shortName=" + shortName 
				+ ", name=" + name 
				+ ", extent=" + extent
				+ ", type=" + type 
				+ ", color=" + color 
				+ ", textColor=" + textColor 
				+ ", stops=" + stops
				+ ", segments=" + segments
				+ "]";
	}

	public Collection<Stop> getStops() {
		return stops;
	}

	public Collection<Vector> getSegments() {
		return segments;
	}
}
