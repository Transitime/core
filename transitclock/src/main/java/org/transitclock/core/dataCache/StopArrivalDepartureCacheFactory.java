package org.transitclock.core.dataCache;
import org.transitclock.config.StringConfigValue;
import org.transitclock.utils.ClassInstantiator;

/**
 * @author Sean Óg Crudden
 * Factory that will provide cache to hold Kalman error values.
 *
 */
public class StopArrivalDepartureCacheFactory {
	private static StringConfigValue className = 
			new StringConfigValue("transitime.core.cache.stopArrivalDepartureCache", 
					"org.transitime.core.dataCache.ehcache.StopArrivalDepartureCache",
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
