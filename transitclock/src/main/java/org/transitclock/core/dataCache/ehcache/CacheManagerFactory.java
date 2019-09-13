package org.transitclock.core.dataCache.ehcache;

import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.StringConfigValue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class CacheManagerFactory {
	
	public static CacheManager singleton = null;

	private static final String DEFAULT_CONFIG = "ehcache.xml";

	private static final Logger logger =
			LoggerFactory.getLogger(CacheManagerFactory.class);

	private static StringConfigValue ehcacheConfigFile =
			new StringConfigValue("transitclock.cache.ehcacheConfigFile", DEFAULT_CONFIG,
					"Specifies the ehcache config file location.");
		 	
	public static CacheManager getInstance() {
															
		if (singleton == null) {
			URL xmlConfigUrl = getConfigPath();
			XmlConfiguration xmlConfig = new XmlConfiguration(xmlConfigUrl);
			
			singleton = CacheManagerBuilder.newCacheManager(xmlConfig);
			singleton.init();				
		}
		
		return singleton;
	}

	private static URL getConfigPath() {
		URL defaultConfig = CacheManagerFactory.class.getClassLoader().getResource(ehcacheConfigFile.getValue());
		if(ehcacheConfigFile.getValue().equalsIgnoreCase(DEFAULT_CONFIG)){
			return defaultConfig;
		} else {
			try {
				File file = new File(ehcacheConfigFile.getValue());
				return  file.toURI().toURL();
			} catch (MalformedURLException e) {
				logger.error("Unable to load ehcache config file: " + ehcacheConfigFile.getValue(), e);
				return defaultConfig;
			}
		}
	}
}
