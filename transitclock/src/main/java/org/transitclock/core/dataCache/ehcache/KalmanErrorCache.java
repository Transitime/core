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
	final private static String dwellCacheName = "KalmanDwellErrorCache";

	private static final Logger logger = LoggerFactory
			.getLogger(KalmanErrorCache.class);

	private Cache<KalmanErrorCacheKey, KalmanError> cache = null;
	private Cache<KalmanErrorCacheKey, KalmanError> dwellCache = null;
	/**
	 * Gets the singleton instance of this class.
	 * 
	 * @return
	 */
	
	public KalmanErrorCache() {
					
		CacheManager cm = CacheManagerFactory.getInstance();
									
		cache = cm.getCache(cacheName, KalmanErrorCacheKey.class, KalmanError.class);
		dwellCache = cm.getCache(dwellCacheName, KalmanErrorCacheKey.class, KalmanError.class);
	}
	
	public void logCache(Logger logger)
	{
		logger.debug("Cache content log. Not implemented.");
		
	}
	
	/* (non-Javadoc)
	 * @see org.transitclock.core.dataCache.ErrorCache#getErrorValue(org.transitclock.core.Indices)
	 */
	@Override
	@SuppressWarnings("unchecked")
	synchronized public KalmanError getErrorValue(Indices indices) {		
		KalmanErrorCacheKey key=new KalmanErrorCacheKey(indices);
		return (KalmanError)cache.get(key);
	}
	/* (non-Javadoc)
	 * @see org.transitclock.core.dataCache.ErrorCache#getErrorValue(org.transitclock.core.dataCache.KalmanErrorCacheKey)
	 */
	@Override
	@SuppressWarnings("unchecked")
	synchronized public KalmanError getErrorValue(KalmanErrorCacheKey key) {		
		return (KalmanError)cache.get(key);
	}

	@Override
	public KalmanError getDwellErrorValue(Indices indices) {
		KalmanErrorCacheKey key=new KalmanErrorCacheKey(indices);
		return (KalmanError)dwellCache.get(key);
	}

	@Override
	public KalmanError getDwellErrorValue(KalmanErrorCacheKey key) {
		return (KalmanError)dwellCache.get(key);
	}

	/* (non-Javadoc)
	 * @see org.transitclock.core.dataCache.ErrorCache#putErrorValue(org.transitclock.core.Indices, java.lang.Double)
	 */
	@Override	
	synchronized public void putErrorValue(Indices indices,  Double value) {
		KalmanErrorCacheKey key=new KalmanErrorCacheKey(indices);		
		putErrorValue(key,value);		
	}				
		
	@Override
	public void putErrorValue(KalmanErrorCacheKey key, Double value) {
		KalmanError error= (KalmanError)cache.get(key);
		if(error==null) {
			error=new KalmanError(value);			
		} else {
			error.setError(value);	
		}
		cache.put(key,error);
	}

	@Override
	public synchronized void putDwellErrorValue(Indices indices, Double value) {
		KalmanErrorCacheKey key=new KalmanErrorCacheKey(indices);
		putDwellErrorValue(key,value);
	}

	@Override
	public void putDwellErrorValue(KalmanErrorCacheKey key, Double value) {
		KalmanError error= (KalmanError)dwellCache.get(key);
		if(error==null) {
			error=new KalmanError(value);
		} else {
			error.setError(value);
		}
		dwellCache.put(key,error);
	}

	@Override
	public List<KalmanErrorCacheKey> getKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	
}
