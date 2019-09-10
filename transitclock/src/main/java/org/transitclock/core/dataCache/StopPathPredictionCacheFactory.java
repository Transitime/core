package org.transitclock.core.dataCache;
import org.transitclock.config.StringConfigValue;
import org.transitclock.utils.ClassInstantiator;

/**
 * @author Sean Óg Crudden
 * Factory that will provide cache to hold stop path model class instances for each stop.
 *
 */
public class StopPathPredictionCacheFactory {
	private static StringConfigValue className = 
			new StringConfigValue("transitclock.core.cache.stopPathPredictionCache",
					"org.transitclock.core.dataCache.StopPathPredictionCache",
					"Specifies the class used to cache RLS data for a stop.");
	
	private static StopPathPredictionCacheInterface singleton = null;
	
	public static StopPathPredictionCacheInterface getInstance() {
		
		if (singleton == null) {
			singleton = ClassInstantiator.instantiate(className.getValue(),
					StopPathPredictionCacheInterface.class);
		}
		
		return singleton;
	}
}
