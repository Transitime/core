package org.transitime.core.dataCache.jcs;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.core.Indices;
import org.transitime.core.dataCache.ErrorCache;
import org.transitime.core.dataCache.KalmanErrorCacheKey;
/**
 * @author Sean Og Crudden
 * 
 */
public class KalmanErrorCache implements ErrorCache  {
	final private static String cacheName = "KalmanErrorCache";
	private static ErrorCache singleton = new KalmanErrorCache();
	
	
	private static final Logger logger = LoggerFactory
			.getLogger(KalmanErrorCache.class);

	private CacheAccess<KalmanErrorCacheKey, Double>  cache = null;
	/**
	 * Gets the singleton instance of this class.
	 * 
	 * @return
	 */
	public static ErrorCache getInstance() {
		return singleton;
	}
	
	public KalmanErrorCache() {
		cache = JCS.getInstance(cacheName);																			
	}
	
	
	/* (non-Javadoc)
	 * @see org.transitime.core.dataCache.ErrorCache#getErrorValue(org.transitime.core.Indices)
	 */
	@Override
	@SuppressWarnings("unchecked")
	synchronized public Double getErrorValue(Indices indices) {		
		
		KalmanErrorCacheKey key=new KalmanErrorCacheKey(indices);
		
		Double result = cache.get(key);
		
		return result;
					
	}
	/* (non-Javadoc)
	 * @see org.transitime.core.dataCache.ErrorCache#getErrorValue(org.transitime.core.dataCache.KalmanErrorCacheKey)
	 */
	@Override
	@SuppressWarnings("unchecked")
	synchronized public Double getErrorValue(KalmanErrorCacheKey key) {		
						
		 Double result = cache.get(key);
		
		 return result;	
	}
	/* (non-Javadoc)
	 * @see org.transitime.core.dataCache.ErrorCache#putErrorValue(org.transitime.core.Indices, java.lang.Double)
	 */
	@Override
	@SuppressWarnings("unchecked")
	synchronized public void putErrorValue(Indices indices,  Double value) {
		
		KalmanErrorCacheKey key=new KalmanErrorCacheKey(indices);
				
		cache.put(key, value);
		
	}				
	
}
