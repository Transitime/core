package org.transitclock.core.dataCache.ehcache;

import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.core.Indices;
import org.transitclock.core.dataCache.ErrorCache;

import org.transitclock.core.dataCache.KalmanErrorCacheKey;
/**
 * @author Sean Óg Crudden
 * 
 */
public class KalmanErrorCache implements ErrorCache  {
	final private static String cacheName = "KalmanErrorCache";

	private static final Logger logger = LoggerFactory
			.getLogger(KalmanErrorCache.class);

	private Cache cache = null;
	/**
	 * Gets the singleton instance of this class.
	 * 
	 * @return
	 */
	
	public KalmanErrorCache() {
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
		List<KalmanErrorCacheKey> keys = cache.getKeys();
		
		for(KalmanErrorCacheKey key : keys)
		{
			Element result=cache.get(key);
			if(result!=null)
			{
				logger.debug("Key: "+key.toString());
								
				Double value=(Double) result.getObjectValue();
												
				logger.debug("Error value: "+value);
			}
		}		
	}
	
	/* (non-Javadoc)
	 * @see org.transitime.core.dataCache.ErrorCache#getErrorValue(org.transitime.core.Indices)
	 */
	@Override
	@SuppressWarnings("unchecked")
	synchronized public Double getErrorValue(Indices indices) {		
		
		KalmanErrorCacheKey key=new KalmanErrorCacheKey(indices);
		
		Element result = cache.get(key);
		
		if(result==null)
			return null;
		else
			return (Double)result.getObjectValue();		
	}
	/* (non-Javadoc)
	 * @see org.transitime.core.dataCache.ErrorCache#getErrorValue(org.transitime.core.dataCache.KalmanErrorCacheKey)
	 */
	@Override
	@SuppressWarnings("unchecked")
	synchronized public Double getErrorValue(KalmanErrorCacheKey key) {		
						
		Element result = cache.get(key);
		
		if(result==null)
			return null;
		else
			return (Double)result.getObjectValue();		
	}
	/* (non-Javadoc)
	 * @see org.transitime.core.dataCache.ErrorCache#putErrorValue(org.transitime.core.Indices, java.lang.Double)
	 */
	@Override
	@SuppressWarnings("unchecked")
	synchronized public void putErrorValue(Indices indices,  Double value) {
		
		KalmanErrorCacheKey key=new KalmanErrorCacheKey(indices);
		Element errorElement = new Element(key, value);
		
		cache.put(errorElement);
	}				

	public List<KalmanErrorCacheKey> getKeys()
	{
		@SuppressWarnings("unchecked")
		List<KalmanErrorCacheKey> keys = cache.getKeys();
		return keys;
	}

	@Override
	public void putErrorValue(KalmanErrorCacheKey key, Double value) {
		
		
		Element errorElement = new Element(key, value);
		
		cache.put(errorElement);
	}

	
}
