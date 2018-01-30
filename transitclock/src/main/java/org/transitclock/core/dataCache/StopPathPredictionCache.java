package org.transitclock.core.dataCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.core.Indices;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.PredictionForStopPath;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;

public class StopPathPredictionCache {
	final private static String cacheName = "StopPathPredictionCache";
	private static StopPathPredictionCache singleton = new StopPathPredictionCache();
	private static final Logger logger = LoggerFactory
			.getLogger(StopPathPredictionCache.class);
	
	private Cache cache = null;
	
	public static StopPathPredictionCache getInstance() {
		return singleton;
	}
	private StopPathPredictionCache() {
		CacheManager cm = CacheManager.getInstance();
		
		if (cm.getCache(cacheName) == null) {
			cm.addCache(cacheName);
		}
		cache = cm.getCache(cacheName);
		
		CacheConfiguration config = cache.getCacheConfiguration();
		
		config.setEternal(true);
		
		config.setMaxEntriesLocalHeap(1000000);
		
		config.setMaxEntriesLocalDisk(1000000);								
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
				List<PredictionForStopPath> predictions = (List<PredictionForStopPath>) result.getObjectValue();
				
				for(PredictionForStopPath prediction: predictions)
				{
					logger.debug(prediction.toString());
				}
			
			}
		}		
	}
	@SuppressWarnings("unchecked")
	synchronized public List<PredictionForStopPath> getPredictions(StopPathCacheKey key) {		
						
		Element result = cache.get(key);
		logCache(logger);
		if(result==null)
			return null;
		else
			return (List<PredictionForStopPath>) result.getObjectValue();		
	}
	public void putPrediction(PredictionForStopPath prediction)
	{
		StopPathCacheKey key=new StopPathCacheKey(prediction.getTripId(), prediction.getStopPathIndex());
		putPrediction(key,prediction);
	}
	@SuppressWarnings("unchecked")
	synchronized public void putPrediction(StopPathCacheKey key,  PredictionForStopPath prediction) {
		
		List<PredictionForStopPath> list = null;
		Element result = cache.get(key);
		
		if (result != null && result.getObjectValue() != null) {
			list = (List<PredictionForStopPath>) result.getObjectValue();
			cache.remove(key);
		} else {
			list = new ArrayList<PredictionForStopPath>();
		}
		list.add(prediction);
		
		Element predictions = new Element(key, Collections.synchronizedList(list));

		cache.put(predictions);				
	}		
	public List<StopPathCacheKey> getKeys()
	{
		@SuppressWarnings("unchecked")
		List<StopPathCacheKey> keys = cache.getKeys();
		return keys;
	}
}
