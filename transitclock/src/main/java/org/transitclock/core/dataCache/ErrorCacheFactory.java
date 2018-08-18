package org.transitclock.core.dataCache;
import org.transitclock.config.StringConfigValue;
import org.transitclock.utils.ClassInstantiator;

/**
 * @author Sean Óg Crudden
 * Factory that will provide cache to hold Kalman error values.
 *
 */
public class ErrorCacheFactory {
	private static StringConfigValue className = 
			new StringConfigValue("transitclock.core.cache.errorCacheClass", 
					"org.transitclock.core.dataCache.ehcache.KalmanErrorCache",
					"Specifies the class used to cache the Kalamn error values.");
	
	private static ErrorCache singleton = null;
	
	public static ErrorCache getInstance() {
		
		if (singleton == null) {
			singleton = ClassInstantiator.instantiate(className.getValue(), 
					ErrorCache.class);
		}
		
		return singleton;
	}
}
