package org.transitime.core.dataCache;
import org.transitime.config.StringConfigValue;
import org.transitime.utils.ClassInstantiator;

/**
 * @author Sean Óg Crudden
 * Factory that will provide cache to hold Kalman error values.
 *
 */
public class ErrorCacheFactory {
	private static StringConfigValue className = 
			new StringConfigValue("transitime.core.cache.errorCacheClass", 
					"org.transitime.core.dataCache.jcs.KalmanErrorCache",
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
