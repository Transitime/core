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

package org.transitclock.custom.sfmta.delayTimes;

import java.util.List;

import org.transitclock.db.structs.Location;
import org.transitclock.db.structs.Vector;
import org.transitclock.utils.Geo;

/**
 *
 *
 * @author SkiBu Smith
 *
 */
public class IntersectionMatcher {

	/********************** Member Functions **************************/

	public static void match(double lat, double lon, double allowableDistance,
			List<Intersection> intersections) {
		Location loc = new Location(lat, lon);
		for (Intersection i : intersections) {
			Location l1 = new Location(i.lat1, i.lon1);
			Location lStop = new Location(i.latStop, i.lonStop);
			Location l2 = new Location(i.lat2, i.lon2);

			Vector v1 = new Vector(l1, lStop);
			Vector v2 = new Vector(lStop, l2);
			double distanceToV1 = Geo.distanceIfMatch(loc, v1);
			if (!Double.isNaN(distanceToV1) && distanceToV1 < allowableDistance) {
				// Matches to v1
			}
			double distanceToV2 = Geo.distanceIfMatch(loc, v2);
			if (!Double.isNaN(distanceToV2) && distanceToV2 < allowableDistance) {
				// Matches to v2
			}
		}
	}
}
