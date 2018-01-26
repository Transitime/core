package org.transitclock.core.dataCache;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.structs.HoldingTime;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
/**
 * @author Sean Óg Crudden
 * 
 */
public class HoldingTimeCache {
	final private static String cacheName = "HoldingTimeCache";
	private static HoldingTimeCache singleton = new HoldingTimeCache();
	private static final Logger logger = LoggerFactory
			.getLogger(HoldingTimeCache.class);

	private Cache cache = null;
	/**
	 * Gets the singleton instance of this class.
	 * 
	 * @return
	 */
	public static HoldingTimeCache getInstance() {
		return singleton;
	}
	private HoldingTimeCache() {
		CacheManager cm = CacheManager.getInstance();
		
		if (cm.getCache(cacheName) == null) {
			cm.addCache(cacheName);
		}
		cache = cm.getCache(cacheName);
		
		CacheConfiguration config = cache.getCacheConfiguration();
		
		config.setEternal(true);
		
		config.setMaxEntriesLocalHeap(10000);
		
		config.setMaxEntriesLocalDisk(10000);								
	}
	public void logCache(Logger logger)
	{
		logger.debug("Cache content log.");
		@SuppressWarnings("unchecked")
		List<HoldingTimeCacheKey> keys = cache.getKeys();
		
		for(HoldingTimeCacheKey key : keys)
		{
			Element result=cache.get(key);
			if(result!=null)
			{
				logger.debug("Key: "+key.toString());
								
				Object value=result.getObjectValue();
												
				logger.debug("Value: " + value.toString());
			}
		}		
	}
	public void putHoldingTime(HoldingTime holdingTime)
	{
		HoldingTimeCacheKey key=new HoldingTimeCacheKey(holdingTime);
		
		Element errorElement = new Element(key, holdingTime);
		
		cache.put(errorElement);
		
	}
	public void putHoldingTimeExlusiveByStop(HoldingTime holdingTime, Date currentTime)
	{
		List<HoldingTimeCacheKey> keys = getKeys();
		boolean add=true;
		
		// Only add only if all other holding times for the stop have expired
		for(HoldingTimeCacheKey key:keys)
		{
			if(key.getStopid().equals(holdingTime.getStopId())&& !(key.getVehicleId().equals(holdingTime.getVehicleId())))
			{
				if(getHoldingTime(key).getHoldingTime().before(currentTime))
				{
					add=false;
				}else
				{
					cache.remove(key);
				}
			}
		}
		if(add)
		{
			putHoldingTime(holdingTime);
		}
	}
	public HoldingTime getHoldingTime(HoldingTimeCacheKey key)
	{
		Element result = cache.get(key);
		
		if(result==null)
			return null;
		else
			return (HoldingTime)result.getObjectValue();			
	}
	public List<HoldingTimeCacheKey> getKeys()
	{
		@SuppressWarnings("unchecked")
		List<HoldingTimeCacheKey> keys = cache.getKeys();
		return keys;
	}
}
