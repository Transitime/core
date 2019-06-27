/**
 * 
 */
package org.transitclock.core.dataCache.ehcache;

import java.util.Collections;
import java.util.Date;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.Status;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.core.dataCache.ArrivalDepartureComparator;
import org.transitclock.core.dataCache.DwellTimeModelCacheFactory;
import org.transitclock.core.dataCache.IpcArrivalDepartureComparator;
import org.transitclock.core.dataCache.KalmanErrorCacheKey;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheFactory;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheInterface;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheKey;
import org.transitclock.core.dataCache.StopEvents;
import org.transitclock.core.dataCache.TripKey;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.ipc.data.IpcArrivalDeparture;
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

	private Cache<StopArrivalDepartureCacheKey, StopEvents> cache = null;
	final URL xmlConfigUrl = getClass().getResource("/ehcache.xml");
	/**
	 * Default is 4 as we need 3 days worth for Kalman Filter implementation
	 */
	private static final IntegerConfigValue tripDataCacheMaxAgeSec = new IntegerConfigValue(
			"transitime.tripdatacache.tripDataCacheMaxAgeSec", 4 * Time.SEC_PER_DAY,
			"How old an arrivaldeparture has to be before it is removed from the cache ");


	public StopArrivalDepartureCache() {
		XmlConfiguration xmlConfig = new XmlConfiguration(xmlConfigUrl);
		
		CacheManager cm = CacheManagerBuilder.newCacheManager(xmlConfig);
		
		if(cm.getStatus().compareTo(Status.AVAILABLE)!=0)
			cm.init();
							
		cache = cm.getCache(cacheByStop, StopArrivalDepartureCacheKey.class, StopEvents.class);	

		// CacheConfiguration config = cache.getCacheConfiguration();

		// cache.setMemoryStoreEvictionPolicy(evictionPolicy);
	}
	
	public void logCache(Logger logger) {
		logger.debug("Cache content log. Not implemented.");			
	}

	/* (non-Javadoc)
	 * @see org.transitime.core.dataCache.ehcache.StopArrivalDepartureCacheInterface#getStopHistory(org.transitime.core.dataCache.StopArrivalDepartureCacheKey)
	 */
	
	@SuppressWarnings("unchecked")
	synchronized public List<IpcArrivalDeparture> getStopHistory(StopArrivalDepartureCacheKey key) {

		//logger.debug(cache.toString());
		Calendar date = Calendar.getInstance();
		date.setTime(key.getDate());

		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		key.setDate(date.getTime());
		StopEvents result = cache.get(key);
		
		if (result != null) {
			return (List<IpcArrivalDeparture>) result.getEvents();
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
		if(arrivalDeparture.getStop()!=null)
		{
			StopArrivalDepartureCacheKey key = new StopArrivalDepartureCacheKey(arrivalDeparture.getStop().getId(),
					date.getTime());
	
			List<IpcArrivalDeparture> list = null;
	
			StopEvents element = cache.get(key);
	
			if (element != null && element.getEvents() != null) {
				list = (List<IpcArrivalDeparture>) element.getEvents();
				cache.remove(key);
			} else {
				list = new ArrayList<IpcArrivalDeparture>();
			}
			
			try {
				list.add(new IpcArrivalDeparture(arrivalDeparture));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Collections.sort(list, new IpcArrivalDepartureComparator());

			element.setEvents(list);
							
			cache.put(key,element);
	
			return key;
		}else
		{
			return null;
		}
	}

	private static <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
		return iterable == null ? Collections.<T> emptyList() : iterable;
	}

	public void populateCacheFromDb(Session session, Date startDate, Date endDate) {
		Criteria criteria = session.createCriteria(ArrivalDeparture.class);

		@SuppressWarnings("unchecked")
		List<ArrivalDeparture> results = criteria.add(Restrictions.between("time", startDate, endDate)).addOrder(Order.asc("time")).list();	

		for (ArrivalDeparture result : results) {
			StopArrivalDepartureCacheFactory.getInstance().putArrivalDeparture(result);
			//TODO might be better with its own populateCacheFromdb
			try
			{
			DwellTimeModelCacheFactory.getInstance().addSample(result);
			}catch(Exception Ex)
			{
				Ex.printStackTrace();
			}
		}
	}


}
