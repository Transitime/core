package org.transitclock.core.dataCache;
import org.transitclock.config.StringConfigValue;
import org.transitclock.utils.ClassInstantiator;

/**
 * @author Sean Óg Crudden
 * Factory that will provide cache to hold RLS class instance for each stop.
 *
 */
public class DwellTimeModelCacheFactory {
	private static StringConfigValue className = 
			new StringConfigValue("transitclock.core.cache.dwellTimeModelCache", 
					"org.transitclock.core.dataCache.jcs.scheduled.DwellTimeModelCache",
					"Specifies the class used to cache RLS data for a stop.");
	
	private static DwellTimeModelCacheInterface singleton = null;
	
	public static DwellTimeModelCacheInterface getInstance() {
		
		if (singleton == null) {
			singleton = ClassInstantiator.instantiate(className.getValue(), 
					DwellTimeModelCacheInterface.class);
		}
		
		return singleton;
	}
}
