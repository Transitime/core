package org.transitclock.core.dataCache.ehcache;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.core.Indices;
import org.transitclock.core.dataCache.ErrorCache;
import org.transitclock.core.dataCache.KalmanError;
import org.transitclock.core.dataCache.KalmanErrorCacheKey;

import java.util.List;
/**
 * @author Sean Óg Crudden
 * 
 */
public class KalmanErrorCache implements ErrorCache {
	final private static String cacheName = "KalmanErrorCache";

	private static final Logger logger = LoggerFactory
			.getLogger(KalmanErrorCache.class);

	private Cache<KalmanErrorCacheKey, KalmanError> cache = null;
	/**
	 * Gets the singleton instance of this class.
	 * 
	 * @return
	 */
	
	public KalmanErrorCache() {
					
		CacheManager cm = CacheManagerFactory.getInstance();
									
		cache = cm.getCache(cacheName, KalmanErrorCacheKey.class, KalmanError.class);									
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
	synchronized public KalmanError getErrorValue(Indices indices) {		
		
		KalmanErrorCacheKey key=new KalmanErrorCacheKey(indices);
		
		KalmanError result = (KalmanError)cache.get(key);
		
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
	synchronized public KalmanError getErrorValue(KalmanErrorCacheKey key) {		
						
		KalmanError result = (KalmanError)cache.get(key);
		
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
		putErrorValue(key,value);		
	}				
		
	@Override
	public void putErrorValue(KalmanErrorCacheKey key, Double value) {
		
		KalmanError error= (KalmanError)cache.get(key);
		
		if(error==null)
		{
			error=new KalmanError(value);			
		}else
		{
			error.setError(value);	
		}
			
								
		cache.put(key,error);
	}

	@Override
	public List<KalmanErrorCacheKey> getKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	
}
