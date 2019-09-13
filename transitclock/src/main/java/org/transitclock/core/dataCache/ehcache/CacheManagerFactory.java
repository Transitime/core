package org.transitclock.core.dataCache.ehcache;

import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;
import org.transitclock.config.StringConfigValue;

import java.net.URL;

public class CacheManagerFactory {
	
	public static CacheManager singleton = null;

	private static StringConfigValue ehcacheConfigFile =
			new StringConfigValue("transitclock.cache.ehcacheConfigFile", "ehcache.xml",
					"Specifies the ehcache config file location.");
		 	
	public static CacheManager getInstance() {
															
		if (singleton == null) {
			URL xmlConfigUrl = CacheManagerFactory.class.getClassLoader().getResource(ehcacheConfigFile.getValue());
			XmlConfiguration xmlConfig = new XmlConfiguration(xmlConfigUrl);
			
			singleton = CacheManagerBuilder.newCacheManager(xmlConfig);
			singleton.init();				
		}
		
		return singleton;
	}
}
