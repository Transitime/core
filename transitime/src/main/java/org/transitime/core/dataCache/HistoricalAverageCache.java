package org.transitime.core.dataCache;

import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.core.Indices;

public class HistoricalAverageCache {
	final private static String cacheName = "HistoricalAverageCache";
	private static HistoricalAverageCache singleton = new HistoricalAverageCache();
	private static final Logger logger = LoggerFactory
			.getLogger(HistoricalAverageCache.class);

	private Cache cache = null;
	/**
	 * Gets the singleton instance of this class.
	 * 
	 * @return
	 */
	public static HistoricalAverageCache getInstance() {
		return singleton;
	}
	
	private HistoricalAverageCache() {
		CacheManager cm = CacheManager.getInstance();
		
		if (cm.getCache(cacheName) == null) {
			cm.addCache(cacheName);
		}
		cache = cm.getCache(cacheName);
		
		CacheConfiguration config = cache.getCacheConfiguration();
		
		config.setEternal(true);
		
		config.setMaxEntriesLocalHeap(10000);
		
		config.setMaxEntriesLocalDisk(1000000);								
	}
	public void logCache(Logger logger)
	{
		logger.debug("Cache content log.");
		@SuppressWarnings("unchecked")
		List<HistoricalAverageCacheKey> keys = cache.getKeys();
		
		for(HistoricalAverageCacheKey key : keys)
		{
			Element result=cache.get(key);
			if(result!=null)
			{
				logger.debug("Key: "+key.toString());
								
				HistoricalAverage value=(HistoricalAverage) result.getObjectValue();
												
				logger.debug("Average: "+value);
			}
		}		
	}
	
	synchronized public HistoricalAverage getAverage(HistoricalAverageCacheKey key) {		
						
		Element result = cache.get(key);
		
		if(result==null)
			return null;
		else
			return (HistoricalAverage)result.getObjectValue();		
	}
	synchronized public void putAverage(HistoricalAverageCacheKey key, HistoricalAverage average) {
			
		logger.debug("Putting: "+key.toString()+" in cache with values : "+average);
		
		Element averageElement = new Element(key, average);
		
		cache.put(averageElement);
				
		// logCache(logger);
	}				
}
