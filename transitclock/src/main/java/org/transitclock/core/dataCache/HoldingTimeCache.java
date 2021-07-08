package org.transitclock.core.dataCache;

import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.structs.HoldingTime;

import org.ehcache.Cache;
import org.ehcache.CacheManager;

import org.ehcache.Status;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;
/**
 * @author Sean Óg Crudden
 * 
 */
public class HoldingTimeCache {
	final private static String cacheName = "HoldingTimeCache";
	private static HoldingTimeCache singleton = new HoldingTimeCache();
	private static final Logger logger = LoggerFactory
			.getLogger(HoldingTimeCache.class);
	final URL xmlConfigUrl = getClass().getResource("/ehcache.xml");
	private Cache<HoldingTimeCacheKey, HoldingTime>  cache = null;
	/**
	 * Gets the singleton instance of this class.
	 * 
	 * @return
	 */
	public static HoldingTimeCache getInstance() {
		return singleton;
	}
	private HoldingTimeCache() {
	XmlConfiguration xmlConfig = new XmlConfiguration(xmlConfigUrl);
		
		CacheManager cm = CacheManagerBuilder.newCacheManager(xmlConfig);
		
		if(cm.getStatus().compareTo(Status.AVAILABLE)!=0)
			cm.init();
							
		cache = cm.getCache(cacheName, HoldingTimeCacheKey.class, HoldingTime.class);						
	}
	public void logCache(Logger logger)
	{
		logger.debug("Cache content log. Not implemented");
				
	}
	public void putHoldingTime(HoldingTime holdingTime)
	{
		HoldingTimeCacheKey key=new HoldingTimeCacheKey(holdingTime);	
		
		cache.put(key, holdingTime);		
	}

	public HoldingTime getHoldingTime(HoldingTimeCacheKey key)
	{
		return cache.get(key);
		
			
	}
	public List<HoldingTimeCacheKey> getKeys() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
