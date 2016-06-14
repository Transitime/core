/**
 * 
 */
package org.transitime.core.dataCache;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.Policy;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.config.IntegerConfigValue;
import org.transitime.db.structs.ArrivalDeparture;
import org.transitime.db.structs.Block;
import org.transitime.db.structs.Trip;
import org.transitime.gtfs.DbConfig;
import org.transitime.utils.Time;

/**
 * @author Sean Og Crudden 
 * 		   This is a Cache to hold historical arrival departure data for trips. It
 *         is intended to look up a trips historical data when a trip starts and
 *         place in cache for use in generating predictions based on a Kalman
 *         filter. Uses Ehcache for caching rather than just using a concurrent
 *         hashmap. This approach to holding data in memory for transitime needs
 *         to be proven.
 *         
 *         TODO this could do with an interface, factory class, and alternative implementations, perhaps using Infinispan.
 */
public class TripDataHistoryCache {
	private static TripDataHistoryCache singleton = new TripDataHistoryCache();
	
	private static boolean debug = false;

	final private static String cacheName = "arrivalDepartures";

	private static final Logger logger = LoggerFactory
			.getLogger(TripDataHistoryCache.class);

	private Cache cache = null;

	/**
	 * Default is 4 as we need 3 days worth for Kalman Filter implementation
	 */
	private static final IntegerConfigValue tripDataCacheMaxAgeSec = new IntegerConfigValue(
			"transitime.tripdatacache.tripDataCacheMaxAgeSec",
			4 * Time.SEC_PER_DAY,
			"How old an arrivaldeparture has to be before it is removed from the cache ");

	/**
	 * Gets the singleton instance of this class.
	 * 
	 * @return
	 */
	public static TripDataHistoryCache getInstance() {
		return singleton;
	}

	private TripDataHistoryCache() {
		CacheManager cm = CacheManager.getInstance();
		EvictionAgePolicy evictionPolicy = null;
		if(tripDataCacheMaxAgeSec!=null)
		{
			evictionPolicy = new EvictionAgePolicy(
				tripDataCacheMaxAgeSec.getValue() * Time.MS_PER_SEC);
		}else
		{
			evictionPolicy = new EvictionAgePolicy(
					4 * Time.SEC_PER_DAY *Time.MS_PER_SEC);
		}

		if (cm.getCache(cacheName) == null) {
			cm.addCache(cacheName);
		}
		cache = cm.getCache(cacheName);
		
		//CacheConfiguration config = cache.getCacheConfiguration();							
		
		cache.setMemoryStoreEvictionPolicy(evictionPolicy);
	}
	public void logCache(Logger logger)
	{
		logger.debug("Cache content log.");
		@SuppressWarnings("unchecked")
		List<TripKey> keys = cache.getKeys();
		
		for(TripKey key : keys)
		{
			Element result=cache.get(key);
			if(result!=null)
			{
				logger.debug("Key: "+key.toString());
				@SuppressWarnings("unchecked")
				
				List<ArrivalDeparture> ads=(List<ArrivalDeparture>) result.getObjectValue();
												
				for(ArrivalDeparture ad : ads)
				{
					logger.debug(ad.toString());
				}
			}
		}
		
	}

	@SuppressWarnings("unchecked")
	synchronized public List<ArrivalDeparture> getTripHistory(TripKey tripKey) {

		logger.debug(cache.toString());

		Element result = cache.get(tripKey);

		if(result!=null)
		{						
			return (List<ArrivalDeparture>) result.getObjectValue();
		}
		else
		{
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	synchronized public TripKey putArrivalDeparture(ArrivalDeparture arrivalDeparture) {
		
		logger.debug("Putting :"+arrivalDeparture.toString() + " in cache.");
		/* just put todays time in for last three days to aid development. This means it will kick in in 1 days rather than 3. Perhaps be a good way to start rather than using default transiTime method but I doubt it. */
		int days_back=1;
		if(debug)
			days_back=2;		
		TripKey tripKey=null;
		
		for(int i=0;i < days_back;i++)
		{
			Date nearestDay = DateUtils.truncate(new Date(arrivalDeparture.getTime()), Calendar.DAY_OF_MONTH);
									
			nearestDay=DateUtils.addDays(nearestDay, i*-1);
			
			DbConfig dbConfig = Core.getInstance().getDbConfig();
			
			Trip trip=dbConfig.getTrip(arrivalDeparture.getTripId());
			
			tripKey = new TripKey(arrivalDeparture.getTripId(),
					nearestDay,
					trip.getStartTime());
			
			List<ArrivalDeparture> list = null;
	
			Element result = cache.get(tripKey);
	
			if (result != null && result.getObjectValue() != null) {
				list = (List<ArrivalDeparture>) result.getObjectValue();
				cache.remove(tripKey);
			} else {
				list = new ArrayList<ArrivalDeparture>();
			}
			
			list.add(arrivalDeparture);			
			
			Element arrivalDepartures = new Element(tripKey, Collections.synchronizedList(list));
						
			cache.put(arrivalDepartures);															
			
			HistoricalAverageCacheKey historicalAverageCacheKey=new HistoricalAverageCacheKey(arrivalDeparture.getBlockId(),trip.getId(), arrivalDeparture.getStopPathIndex());
			
			HistoricalAverage average = HistoricalAverageCache.getInstance().getAverage(historicalAverageCacheKey);
						
			double duration=getLastPathDuration(arrivalDeparture, trip);
			
			if(duration>0)
			{
				if(average==null)				
					average=new HistoricalAverage();
				
				average.update(duration);
			
				HistoricalAverageCache.getInstance().putAverage(historicalAverageCacheKey, average);
			}
		}
				
		return tripKey;
	}
	private double getLastPathDuration(ArrivalDeparture arrivalDeparture, Trip trip)
	{
		Date nearestDay = DateUtils.truncate(new Date(arrivalDeparture.getTime()), Calendar.DAY_OF_MONTH);
		TripKey tripKey = new TripKey(arrivalDeparture.getTripId(),
				nearestDay,
				trip.getStartTime());
						
		List<ArrivalDeparture> arrivalDepartures=(List<ArrivalDeparture>) getTripHistory(tripKey);
		
		if(arrivalDepartures!=null && arrivalDepartures.size()>0 && arrivalDeparture.isArrival())
		{			
			ArrivalDeparture previousEvent = findPreviousDepartureEvent(arrivalDepartures, arrivalDeparture);
			
			if(previousEvent!=null && arrivalDeparture!=null )
					return Math.abs(previousEvent.getTime()-arrivalDeparture.getTime());
		}
					
		return -1;
	}
	private ArrivalDeparture findPreviousDepartureEvent(List<ArrivalDeparture> arrivalDepartures,ArrivalDeparture current)
	{
		ArrivalDeparture previous=null;
		
		if(arrivalDepartures!=null && arrivalDepartures.size()>0)
		{												
			for (ArrivalDeparture tocheck : emptyIfNull(arrivalDepartures)) 
			{
				if(tocheck.getStopPathIndex()==(current.getStopPathIndex()-1) && (current.isArrival() && tocheck.isDeparture()))
				{
					previous=tocheck;
				}			
			}
		}
		return previous;		
	}

	private static <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
		return iterable == null ? Collections.<T> emptyList() : iterable;
	}
	public void populateCacheFromDb(Session session, Date startDate, Date endDate)
	{
 		Criteria criteria =session.createCriteria(ArrivalDeparture.class);				
		
		@SuppressWarnings("unchecked")
		List<ArrivalDeparture> results=criteria.add(Restrictions.between("time", startDate, endDate)).list();
						
		for(ArrivalDeparture result : results)
		{
			TripDataHistoryCache.getInstance().putArrivalDeparture(result);
		}		
	}
	/**
	 * 	This policy evicts arrival departures from the cache
	 *  when they are X (age) number of milliseconds old
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
			if (arg0.getObjectKey() instanceof TripKey
					&& arg1.getObjectKey() instanceof TripKey) {
				if (((TripKey) arg0.getObjectKey()).getTripStartDate().after(
						((TripKey) arg1.getObjectKey()).getTripStartDate())) {
					return true;
				}
				if (((TripKey) arg0.getObjectKey()).getTripStartDate()
						.compareTo(
								((TripKey) arg1.getObjectKey())
										.getTripStartDate()) == 0) {
					if (((TripKey) arg0.getObjectKey()).getStartTime() > ((TripKey) arg1
							.getObjectKey()).getStartTime()) {
						return true;
					}
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
					TripKey key = (TripKey) arg0[i].getObjectKey();

					if (Calendar.getInstance().getTimeInMillis()
							- key.getTripStartDate().getTime()
							+ (key.getStartTime().intValue() * 1000) > age) {
						return arg0[i];
					}
				}
			}
			return null;
		}		
	}
}
