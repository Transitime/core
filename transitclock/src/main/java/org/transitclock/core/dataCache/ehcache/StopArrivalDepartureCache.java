/**
 * 
 */
package org.transitclock.core.dataCache.ehcache;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.core.dataCache.*;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.utils.Time;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sean Og Crudden This is a Cache to hold a sorted list of all arrival departure events
 *         for each stop in a cache. We can use this to look up all event for a
 *         stop for a day. The date used in the key should be the start of the
 *         day concerned.
 * 
 *         TODO this could do with an interface, factory class, and alternative
 *         implementations, perhaps using Infinispan.
 */
public class StopArrivalDepartureCache extends StopArrivalDepartureCacheInterface {


	private static boolean debug = false;

	final private static String cacheByStop = "arrivalDeparturesByStop";

	private static final Logger logger = LoggerFactory.getLogger(StopArrivalDepartureCache.class);

	private Cache<StopArrivalDepartureCacheKey, StopEvents> cache = null;
	/**
	 * Default is 4 as we need 3 days worth for Kalman Filter implementation
	 */
	private static final IntegerConfigValue tripDataCacheMaxAgeSec = new IntegerConfigValue(
			"transitclock.tripdatacache.tripDataCacheMaxAgeSec", 4 * Time.SEC_PER_DAY,
			"How old an arrivaldeparture has to be before it is removed from the cache ");


	public StopArrivalDepartureCache() {		
		CacheManager cm = CacheManagerFactory.getInstance();
									
		cache = cm.getCache(cacheByStop, StopArrivalDepartureCacheKey.class, StopEvents.class);	
	}
	
	public void logCache(Logger logger) {
		logger.debug("Cache content log. Not implemented.");			
	}

	/* (non-Javadoc)
	 * @see org.transitclock.core.dataCache.ehcache.StopArrivalDepartureCacheInterface#getStopHistory(org.transitclock.core.dataCache.StopArrivalDepartureCacheKey)
	 */
	
	@SuppressWarnings("unchecked")
	public List<IpcArrivalDeparture> getStopHistory(StopArrivalDepartureCacheKey key) {

		//logger.debug(cache.toString());
		Calendar date = Calendar.getInstance();
		date.setTime(key.getDate());

		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		key.setDate(date.getTime());
		// cache retrieval should be synchronized as Kyro is not thread safe
		synchronized (cache) {
		StopEvents result = cache.get(key);

		if (result != null) {
			return (List<IpcArrivalDeparture>) result.getEvents();
		} else {
			return null;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.transitclock.core.dataCache.ehcache.StopArrivalDepartureCacheInterface#putArrivalDeparture(org.transitclock.db.structs.ArrivalDeparture)
	 */
	@SuppressWarnings("unchecked")
	public StopArrivalDepartureCacheKey putArrivalDeparture(ArrivalDeparture arrivalDeparture) {

		logger.trace("Putting :" + arrivalDeparture.toString() + " in StopArrivalDepartureCache cache.");
	
		Calendar date = Calendar.getInstance();
		date.setTime(arrivalDeparture.getDate());

		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		if(arrivalDeparture.getStop() == null) return null;
		StopArrivalDepartureCacheKey key = new StopArrivalDepartureCacheKey(arrivalDeparture.getStop().getId(),
				date.getTime());
		IpcArrivalDeparture ipc;
		StopEvents empty = new StopEvents();
		try {
			ipc = new IpcArrivalDeparture(arrivalDeparture);
		} catch (Exception e) {
			logger.error("Exception adding " + arrivalDeparture.toString() + " event to StopArrivalDepartureCache.", e);
			return key;
		}
		empty.addEvent(ipc);

		synchronized (cache) {
			StopEvents element = cache.get(key);
			if (element == null) {
				cache.put(key, empty);
			} else {
				element.addEvent(ipc);
				cache.put(key, element);
			}
			return key;
		}
	}

	public StopArrivalDepartureCacheKey putArrivalDepartureInMemory(Map<StopArrivalDepartureCacheKey, StopEvents> map,
																																	ArrivalDeparture arrivalDeparture) {

		Calendar date = Calendar.getInstance();
		date.setTime(arrivalDeparture.getDate());

		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		if(arrivalDeparture.getStop() == null) return null;
		StopArrivalDepartureCacheKey key = new StopArrivalDepartureCacheKey(arrivalDeparture.getStop().getId(),
						date.getTime());
		IpcArrivalDeparture ipc;
		StopEvents empty = new StopEvents();
		try {
			ipc = new IpcArrivalDeparture(arrivalDeparture);
		} catch (Exception e) {
			logger.error("Exception adding " + arrivalDeparture.toString() + " event to StopArrivalDepartureCache.", e);
			return key;
		}
		empty.addEvent(ipc);


		StopEvents element = map.get(key);
		if (element == null) {
			map.put(key, empty);
		} else {
			element.addEvent(ipc);
			map.put(key, element);
		}
		return key;

	}

	private static <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
		return iterable == null ? Collections.<T> emptyList() : iterable;
	}

	public void populateCacheFromDb(List<ArrivalDeparture> results) {
		Map<StopArrivalDepartureCacheKey, StopEvents> map = new HashMap<>();
		try {
			int counter = 0;

			for (ArrivalDeparture result : results) {
				if (counter % 1000 == 0) {
					logger.info("{} out of {} Stop Arrival Departure Records ({}%)", counter, results.size(), (int) ((counter * 100.0f) / results.size()));
				}

				putArrivalDepartureInMemory(map, result);

				counter++;
			}
		} catch (Throwable t) {
			logger.error("Exception in populateCacheFromDb {}", t, t);
		}
		synchronized (cache) {
			cache.putAll(map);
		}
	}

	@Override
	protected void putAll(Map<StopArrivalDepartureCacheKey, StopEvents> map) {
		synchronized (cache) {
			cache.putAll(map);
		}
	}


}
