package org.transitclock.core.dataCache.ehcache;
import java.net.URL;
import java.util.List;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.Status;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.xml.XmlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.core.Indices;
import org.transitclock.core.dataCache.ErrorCache;

import org.transitclock.core.dataCache.KalmanErrorCacheKey;
/**
 * @author Sean Óg Crudden
 * 
 */
public class KalmanErrorCache implements ErrorCache {
	final private static String cacheName = "KalmanErrorCache";
	
	final URL xmlConfigUrl = getClass().getResource("/ehcache.xml");

	private static final Logger logger = LoggerFactory
			.getLogger(KalmanErrorCache.class);

	private Cache<KalmanErrorCacheKey, Double> cache = null;
	/**
	 * Gets the singleton instance of this class.
	 * 
	 * @return
	 */
	
	public KalmanErrorCache() {
					
		CacheManager cm = CacheManagerFactory.getInstance();
									
		cache = cm.getCache(cacheName, KalmanErrorCacheKey.class, Double.class);									
	}
	
	public void logCache(Logger logger)
	{
		logger.debug("Cache content log. Not implemented.");
		
	}
	
	/* (non-Javadoc)
	 * @see org.transitime.core.dataCache.ErrorCache#getErrorValue(org.transitime.core.Indices)
	 */
	@Override
	@SuppressWarnings("unchecked")
	synchronized public Double getErrorValue(Indices indices) {		
		
		KalmanErrorCacheKey key=new KalmanErrorCacheKey(indices);
		
		Double result = (Double)cache.get(key);
		
		if(result==null)
			return null;
		else
			return result;		
	}
	/* (non-Javadoc)
	 * @see org.transitime.core.dataCache.ErrorCache#getErrorValue(org.transitime.core.dataCache.KalmanErrorCacheKey)
	 */
	@Override
	@SuppressWarnings("unchecked")
	synchronized public Double getErrorValue(KalmanErrorCacheKey key) {		
						
		Double result = (Double)cache.get(key);
		
		if(result==null)
			return null;
		else
			return result;				
	}
	/* (non-Javadoc)
	 * @see org.transitime.core.dataCache.ErrorCache#putErrorValue(org.transitime.core.Indices, java.lang.Double)
	 */
	@Override	
	synchronized public void putErrorValue(Indices indices,  Double value) {
		
		KalmanErrorCacheKey key=new KalmanErrorCacheKey(indices);		
		cache.put(key, value);		
	}				
		
	@Override
	public void putErrorValue(KalmanErrorCacheKey key, Double value) {
								
		cache.put(key,value);
	}

	@Override
	public List<KalmanErrorCacheKey> getKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	
}
