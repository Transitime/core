/**
 * 
 */
package org.transitclock.core.dataCache.ehcache;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.core.dataCache.*;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.utils.Time;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
					
			StopEvents element = cache.get(key);
	
			if (element == null) {														
				element=new StopEvents();	
			}
			
			try {
				element.addEvent(new IpcArrivalDeparture(arrivalDeparture));
			} catch (Exception e) {				
				logger.error("Error adding "+arrivalDeparture.toString()+" event to StopArrivalDepartureCache.", e);				
			}			
									
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
