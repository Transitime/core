package org.transitclock.core.dataCache;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.Status;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.structs.PredictionForStopPath;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class StopPathPredictionCache implements StopPathPredictionCacheInterface{
	final private static String cacheName = "StopPathPredictionCache";
	private static StopPathPredictionCache singleton = new StopPathPredictionCache();
	private static final Logger logger = LoggerFactory
			.getLogger(StopPathPredictionCache.class);
	
	private Cache<StopPathCacheKey, StopPredictions> cache = null;
	final URL xmlConfigUrl = getClass().getResource("/ehcache.xml");

	public static StopPathPredictionCache getInstance() {
		return singleton;
	}

	public StopPathPredictionCache() {

		XmlConfiguration xmlConfig = new XmlConfiguration(xmlConfigUrl);
		
		CacheManager cm = CacheManagerBuilder.newCacheManager(xmlConfig);
		
		if(cm.getStatus().compareTo(Status.AVAILABLE)!=0)
			cm.init();
							
		cache = cm.getCache(cacheName, StopPathCacheKey.class, StopPredictions.class);						
	}

    @Override
	public void logCache(Logger logger)
	{
		logger.debug("Cache content log. Not implemented.");
				
	}
	@SuppressWarnings("unchecked")
    @Override
	synchronized public List<PredictionForStopPath> getPredictions(StopPathCacheKey key) {		
						
		StopPredictions result = cache.get(key);
		logCache(logger);
		if(result==null)
			return null;
		else
			return (List<PredictionForStopPath>) result.getPredictions();		
	}

	@Override
	public void putPrediction(PredictionForStopPath prediction)
	{
		StopPathCacheKey key=new StopPathCacheKey(prediction.getTripId(), prediction.getStopPathIndex());
		putPrediction(key,prediction);
	}

	@SuppressWarnings("unchecked")
    @Override
	synchronized public void putPrediction(StopPathCacheKey key,  PredictionForStopPath prediction) {
		
		List<PredictionForStopPath> list = null;
		StopPredictions element = cache.get(key);
		
		if (element != null && element.getPredictions() != null) {
			list = (List<PredictionForStopPath>) element.getPredictions();
			cache.remove(key);
		} else {
			list = new ArrayList<PredictionForStopPath>();
		}
		list.add(prediction);
		
		element.setPredictions(list);
				
		cache.put(key, element);				
	}		

}
