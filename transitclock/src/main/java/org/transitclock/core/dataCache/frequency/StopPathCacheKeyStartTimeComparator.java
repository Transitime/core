package org.transitclock.core.dataCache.frequency;

import java.util.Comparator;

import org.transitclock.core.dataCache.StopPathCacheKey;

public class StopPathCacheKeyStartTimeComparator implements Comparator<StopPathCacheKey>{

	@Override
	public int compare(StopPathCacheKey key1, StopPathCacheKey key2) {
		return key1.getStartTime().compareTo(key2.getStartTime());			
	}

}
