package org.transitclock.core.dataCache;
import org.transitclock.config.StringConfigValue;
import org.transitclock.utils.ClassInstantiator;

/**
 * @author Sean Óg Crudden
 *
 *
 */
public class StopArrivalDepartureCacheFactory {
	private static StringConfigValue className = 
			new StringConfigValue("transitclock.core.cache.stopArrivalDepartureCache", 
					"org.transitclock.core.dataCache.ehcache.StopArrivalDepartureCache",
					"Specifies the class used to cache the arrival and departures for a stop.");
	
	private static StopArrivalDepartureCacheInterface singleton = null;
	
	public static StopArrivalDepartureCacheInterface getInstance() {
		
		if (singleton == null) {
			singleton = ClassInstantiator.instantiate(className.getValue(), 
					StopArrivalDepartureCacheInterface.class);
		}
		
		return singleton;
	}
}
