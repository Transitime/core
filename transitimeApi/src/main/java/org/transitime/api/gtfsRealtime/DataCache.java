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

package org.transitime.api.gtfsRealtime;

import java.util.HashMap;
import java.util.Map;

import org.transitime.utils.Time;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;

/**
 * For caching GTFS-realtime messages. Useful because the messages are huge and
 * take a lot of resources so if get multiple requests not too far apart then it
 * makes sense to return a cached version.
 *
 * @author SkiBu Smith
 *
 */
public class DataCache {

    private Map<String, CacheEntry> cacheMap = new HashMap<String, CacheEntry>();

    /********************** Member Functions **************************/

    private static class CacheEntry {
	private long timeCreated;
	private FeedMessage cachedFeedMessage;
    }

    public FeedMessage get(String agencyId, int maxCacheSeconds) {
	CacheEntry cacheEntry = cacheMap.get(agencyId);
	if (cacheEntry == null)
	    return null;
	if (cacheEntry.timeCreated < System.currentTimeMillis() - maxCacheSeconds * Time.MS_PER_SEC) {
	    cacheMap.remove(agencyId);
	    return null;
	}
	return cacheEntry.cachedFeedMessage;
    }
    
    public void put(String agencyId, FeedMessage feedMessage) {
	CacheEntry cacheEntry = new CacheEntry();
	cacheEntry.timeCreated = System.currentTimeMillis();
	cacheEntry.cachedFeedMessage = feedMessage;
	cacheMap.put(agencyId, cacheEntry);
    }
}

