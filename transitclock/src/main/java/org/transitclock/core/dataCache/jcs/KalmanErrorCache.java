package org.transitclock.core.dataCache.jcs;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.core.Indices;
import org.transitclock.core.dataCache.ErrorCache;
import org.transitclock.core.dataCache.KalmanError;
import org.transitclock.core.dataCache.KalmanErrorCacheKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
/**
 * @author Sean Og Crudden
 * 
 */
public class KalmanErrorCache implements ErrorCache  {
	final private static String cacheName = "KalmanErrorCache";
	final private static String dwellCacheName = "KalmanDwellErrorCache";
	
	private static final Logger logger = LoggerFactory
			.getLogger(KalmanErrorCache.class);

	private CacheAccess<KalmanErrorCacheKey, KalmanError>  cache = null;
	private CacheAccess<KalmanErrorCacheKey, KalmanError>  dwellCache = null;
	
	public KalmanErrorCache() {
		cache = JCS.getInstance(cacheName);
		dwellCache = JCS.getInstance(dwellCacheName);
	}
	
	/* (non-Javadoc)
	 * @see org.transitclock.core.dataCache.ErrorCache#getErrorValue(org.transitclock.core.Indices)
	 */
	@Override
	@SuppressWarnings("unchecked")
	synchronized public KalmanError getErrorValue(Indices indices) {		
		KalmanErrorCacheKey key=new KalmanErrorCacheKey(indices);
		return cache.get(key);
	}
	/* (non-Javadoc)
	 * @see org.transitclock.core.dataCache.ErrorCache#getErrorValue(org.transitclock.core.dataCache.KalmanErrorCacheKey)
	 */
	@Override
	@SuppressWarnings("unchecked")
	synchronized public KalmanError getErrorValue(KalmanErrorCacheKey key) {		
		 return cache.get(key);
	}

	@Override
	public KalmanError getDwellErrorValue(Indices indices) {
		KalmanErrorCacheKey key=new KalmanErrorCacheKey(indices);
		return dwellCache.get(key);

	}

	@Override
	public KalmanError getDwellErrorValue(KalmanErrorCacheKey key) {
		return dwellCache.get(key);

	}

	/* (non-Javadoc)
	 * @see org.transitclock.core.dataCache.ErrorCache#putErrorValue(org.transitclock.core.Indices, java.lang.Double)
	 */
	@Override
	@SuppressWarnings("unchecked")
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
	public void putDwellErrorValue(Indices indices, Double value) {
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


	public List<KalmanErrorCacheKey> getKeys() {
		ArrayList<KalmanErrorCacheKey> fulllist=new ArrayList<KalmanErrorCacheKey>();
		Set<String> names = JCS.getGroupCacheInstance(cacheName).getGroupNames();
		
		for(String name:names)
		{
			Set<Object> keys = JCS.getGroupCacheInstance(cacheName).getGroupKeys(name);
			
			for(Object key:keys)
			{
				fulllist.add((KalmanErrorCacheKey)key);
			}
		}
		return fulllist;
	}			
}
