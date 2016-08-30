package org.transitime.core.dataCache;

import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author Sean Og Crudden
 * 
 */
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
	}
	public List<StopPathCacheKey> getKeys()
	{
		@SuppressWarnings("unchecked")
		List<StopPathCacheKey> keys = cache.getKeys();
		return keys;
	}
	public void logCache(Logger logger)
	{
		logger.debug("Cache content log.");
		@SuppressWarnings("unchecked")
		List<StopPathCacheKey> keys = cache.getKeys();
		
		for(StopPathCacheKey key : keys)
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
	public void logCacheSize(Logger logger)
	{
		@SuppressWarnings("unchecked")
		List<StopPathCacheKey> keys = cache.getKeys();
		
		if(keys!=null)
			logger.debug("Number of entries in HistoricalAverageCache : "+keys.size());
	}
	
	synchronized public HistoricalAverage getAverage(StopPathCacheKey key) {		
						
		Element result = cache.get(key);
		
		if(result==null)
			return null;
		else
			return (HistoricalAverage)result.getObjectValue();		
	}
	synchronized public void putAverage(StopPathCacheKey key, HistoricalAverage average) {
			
		logger.debug("Putting: "+key.toString()+" in cache with values : "+average);
		
		Element averageElement = new Element(key, average);
		
		cache.put(averageElement);
			
		logCacheSize(logger);
		// logCache(logger);
	}				
}
