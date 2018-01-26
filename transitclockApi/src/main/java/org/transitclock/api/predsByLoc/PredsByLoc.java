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
package org.transitclock.api.predsByLoc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.transitclock.db.structs.Agency;
import org.transitclock.db.structs.Extent;
import org.transitclock.db.structs.Location;
import org.transitclock.db.webstructs.WebAgency;
import org.transitclock.utils.Time;

/**
 * For determining predictions by location for when agency is not specified so
 * need to look through all agencies.
 * 
 * @author Michael
 *
 */
public class PredsByLoc {
	
	// The cache of extents. Keyed on agencyId. Should not be accessed directly.
	// Should instead use getAgencyExtents().
	private static Map<String, Extent> agencyExtentsCache =
			new HashMap<String, Extent>();
	private static long cacheUpdatedTime = 0;
	
	// The maximum allowable maxDistance for getting predictions by location
	public final static double MAX_MAX_DISTANCE = 2000.0;

	private static long CACHE_VALID_MSEC = 4 * Time.MS_PER_HOUR;
	
	/************************ Methods *********************/
	
	/**
	 * Returns the cache of agency extents. If haven't read in extents from the
	 * servers in more than 4 hours then the cache is updated before it is
	 * returned.
	 * 
	 * @return cache of extents
	 */
	private static Map<String, Extent> getAgencyExtents() {
		// If updated cache recently then simply return it
		if (System.currentTimeMillis() < cacheUpdatedTime + CACHE_VALID_MSEC)
			return agencyExtentsCache;
		
		// Haven't updated cache in a while so update it now
		Collection<WebAgency> webAgencies =
				WebAgency.getCachedOrderedListOfWebAgencies();
		
		// For each agency get the extent
		for (WebAgency webAgency : webAgencies) {
			Agency agency = webAgency.getAgency();
			if (agency != null) {
				agencyExtentsCache.put(webAgency.getAgencyId(),
						agency.getExtent());
			}
		}
		
		// Return the update cache
		return agencyExtentsCache;
	}
	
	/**
	 * Returns list of agencies that are with the specified distance of the
	 * latitude and longitude.
	 * 
	 * @param latitude
	 * @param longitude
	 * @param distance
	 * @return List of agencies that are nearby
	 */
	public static List<String> getNearbyAgencies(double latitude,
			double longitude, double distance) {
		// For results of method
		List<String> nearbyAgencies = new ArrayList<String>();
		
		// Determine which agencies are nearby and add them to list
		Location loc = new Location(latitude, longitude);		
		Map<String, Extent> agencyExtents = getAgencyExtents();
		for (String agencyId : agencyExtents.keySet()) {
			Extent agencyExtent = agencyExtents.get(agencyId);
			if (agencyExtent.isWithinDistance(loc, distance))
				nearbyAgencies.add(agencyId);
		}
		
		// Return agencies that are nearby
		return nearbyAgencies;
	}
}
