package org.transitclock.core.dataCache;
import org.transitclock.config.StringConfigValue;
import org.transitclock.utils.ClassInstantiator;

/**
 * @author Sean Óg Crudden
 * Factory that will provide cache to hold arrival and departures for a trip.
 *
 */
public class TripDataHistoryCacheFactory {
	private static StringConfigValue className = 
			new StringConfigValue("transitclock.core.cache.tripDataHistoryCache", 
					"org.transitclock.core.dataCache.ehcache.scheduled.TripDataHistoryCache",
					"Specifies the class used to cache the arrival and departures for a trip.");
	
	public static TripDataHistoryCacheInterface singleton = null;
	
	public static TripDataHistoryCacheInterface getInstance() {
		
		if (singleton == null) {
			singleton = ClassInstantiator.instantiate(className.getValue(), 
					TripDataHistoryCacheInterface.class);
		}
		
		return singleton;
	}
}
