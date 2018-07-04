/**
 * 
 */
package org.transitclock.core.dataCache.ehcache;

import java.util.Collections;
import java.util.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.store.Policy;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.core.dataCache.ArrivalDepartureComparator;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheFactory;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheInterface;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheKey;
import org.transitclock.core.dataCache.TripKey;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.utils.Time;

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

	private Cache cache = null;

	/**
	 * Default is 4 as we need 3 days worth for Kalman Filter implementation
	 */
	private static final IntegerConfigValue tripDataCacheMaxAgeSec = new IntegerConfigValue(
			"transitime.tripdatacache.tripDataCacheMaxAgeSec", 4 * Time.SEC_PER_DAY,
			"How old an arrivaldeparture has to be before it is removed from the cache ");


	public StopArrivalDepartureCache() {
		CacheManager cm = CacheManager.getInstance();
		EvictionAgePolicy evictionPolicy = null;
		if (tripDataCacheMaxAgeSec != null) {
			evictionPolicy = new EvictionAgePolicy(tripDataCacheMaxAgeSec.getValue() * Time.MS_PER_SEC);
		} else {
			evictionPolicy = new EvictionAgePolicy(4 * Time.SEC_PER_DAY * Time.MS_PER_SEC);
		}

		if (cm.getCache(cacheByStop) == null) {
			cm.addCache(cacheByStop);
		}
		cache = cm.getCache(cacheByStop);

		// CacheConfiguration config = cache.getCacheConfiguration();

		// cache.setMemoryStoreEvictionPolicy(evictionPolicy);
	}

	@SuppressWarnings("unchecked")
	public List<StopArrivalDepartureCacheKey> getKeys() {
		return (List<StopArrivalDepartureCacheKey>) cache.getKeys();
	}

	public void logCache(Logger logger) {
		logger.debug("Cache content log.");
		@SuppressWarnings("unchecked")
		List<StopArrivalDepartureCacheKey> keys = cache.getKeys();

		for (StopArrivalDepartureCacheKey key : keys) {
			Element result = cache.get(key);
			if (result != null) {
				logger.debug("Key: " + key.toString());
				@SuppressWarnings("unchecked")

				List<ArrivalDeparture> ads = (List<ArrivalDeparture>) result.getObjectValue();

				for (ArrivalDeparture ad : ads) {
					logger.debug(ad.toString());
				}
			}
		}

	}

	/* (non-Javadoc)
	 * @see org.transitime.core.dataCache.ehcache.StopArrivalDepartureCacheInterface#getStopHistory(org.transitime.core.dataCache.StopArrivalDepartureCacheKey)
	 */
	
	@SuppressWarnings("unchecked")
	synchronized public List<ArrivalDeparture> getStopHistory(StopArrivalDepartureCacheKey key) {

		//logger.debug(cache.toString());
		Calendar date = Calendar.getInstance();
		date.setTime(key.getDate());

		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		key.setDate(date.getTime());
		Element result = cache.get(key);
		
		if (result != null) {
			return (List<ArrivalDeparture>) result.getObjectValue();
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.transitime.core.dataCache.ehcache.StopArrivalDepartureCacheInterface#putArrivalDeparture(org.transitime.db.structs.ArrivalDeparture)
	 */
	
	@SuppressWarnings("unchecked")
	synchronized public StopArrivalDepartureCacheKey putArrivalDeparture(ArrivalDeparture arrivalDeparture) {

		logger.debug("Putting :" + arrivalDeparture.toString() + " in StopArrivalDepartureCache cache.");
	
		Calendar date = Calendar.getInstance();
		date.setTime(arrivalDeparture.getDate());

		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);

		StopArrivalDepartureCacheKey key = new StopArrivalDepartureCacheKey(arrivalDeparture.getStop().getId(),
				date.getTime());

		List<ArrivalDeparture> list = null;

		Element result = cache.get(key);

		if (result != null && result.getObjectValue() != null) {
			list = (List<ArrivalDeparture>) result.getObjectValue();
			cache.remove(key);
		} else {
			list = new ArrayList<ArrivalDeparture>();
		}
		
		list.add(arrivalDeparture);
		
		Collections.sort(list, new ArrivalDepartureComparator());
		
		// This is java 1.8 list.sort(new ArrivalDepartureComparator());
		
		Element arrivalDepartures = new Element(key, Collections.synchronizedList(list));

		cache.put(arrivalDepartures);

		return key;
	}

	private static <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
		return iterable == null ? Collections.<T> emptyList() : iterable;
	}

	public void populateCacheFromDb(Session session, Date startDate, Date endDate) {
		Criteria criteria = session.createCriteria(ArrivalDeparture.class);

		@SuppressWarnings("unchecked")
		List<ArrivalDeparture> results = criteria.add(Restrictions.between("time", startDate, endDate)).list();

		for (ArrivalDeparture result : results) {
			StopArrivalDepartureCacheFactory.getInstance().putArrivalDeparture(result);
		}
	}

	/**
	 * This policy evicts arrival departures from the cache when they are X
	 * (age) number of milliseconds old
	 * 
	 */
	private class EvictionAgePolicy implements Policy {
		private String name = "AGE";

		private long age = 0L;

		public EvictionAgePolicy(long age) {
			super();
			this.age = age;
		}

		@Override
		public boolean compare(Element arg0, Element arg1) {
			if (arg0.getObjectKey() instanceof StopArrivalDepartureCacheKey
					&& arg1.getObjectKey() instanceof StopArrivalDepartureCacheKey) {
				if (((StopArrivalDepartureCacheKey) arg0.getObjectKey()).getDate()
						.after(((StopArrivalDepartureCacheKey) arg1.getObjectKey()).getDate())) {
					return true;
				}

			}
			return false;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Element selectedBasedOnPolicy(Element[] arg0, Element arg1) {

			for (int i = 0; i < arg0.length; i++) {

				if (arg0[i].getObjectKey() instanceof TripKey) {
					StopArrivalDepartureCacheKey key = (StopArrivalDepartureCacheKey) arg0[i].getObjectKey();

					if (Calendar.getInstance().getTimeInMillis() - key.getDate().getTime() > age) {
						return arg0[i];
					}
				}
			}
			return null;
		}
	}
}
