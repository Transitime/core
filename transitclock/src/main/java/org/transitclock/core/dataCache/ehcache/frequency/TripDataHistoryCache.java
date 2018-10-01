/**
 * 
 */
package org.transitclock.core.dataCache.ehcache.frequency;

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
import org.transitclock.applications.Core;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.core.dataCache.ArrivalDepartureComparator;
import org.transitclock.core.dataCache.TripDataHistoryCacheFactory;
import org.transitclock.core.dataCache.TripDataHistoryCacheInterface;
import org.transitclock.core.dataCache.TripKey;
import org.transitclock.core.dataCache.frequency.FrequencyBasedHistoricalAverageCache;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Block;
import org.transitclock.db.structs.Trip;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.gtfs.GtfsData;
import org.transitclock.utils.Time;

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
public class TripDataHistoryCache implements TripDataHistoryCacheInterface{
	private static TripDataHistoryCacheInterface singleton = new TripDataHistoryCache();
	
	private static boolean debug = false;

	final private static String cacheByTrip = "arrivalDeparturesByTrip";
	

	private static final Logger logger = LoggerFactory
			.getLogger(TripDataHistoryCache.class);

	private Cache cache = null;

	/**
	 * Default is 4 as we need 3 days worth for Kalman Filter implementation
	 */
	private static final IntegerConfigValue tripDataCacheMaxAgeSec = new IntegerConfigValue(
			"transitclock.tripdatacache.tripDataCacheMaxAgeSec",
			15 * Time.SEC_PER_DAY,
			"How old an arrivaldeparture has to be before it is removed from the cache ");

	/**
	 * Gets the singleton instance of this class.
	 * 
	 * @return
	 */
	public static TripDataHistoryCacheInterface getInstance() {
		return singleton;
	}

	public TripDataHistoryCache() {
		CacheManager cm = CacheManager.getInstance();
		EvictionAgePolicy evictionPolicy = null;
		if(tripDataCacheMaxAgeSec!=null)
		{
			evictionPolicy = new EvictionAgePolicy(
				tripDataCacheMaxAgeSec.getValue() * Time.MS_PER_SEC);
		}else
		{
			evictionPolicy = new EvictionAgePolicy(
					15 * Time.SEC_PER_DAY *Time.MS_PER_SEC);
		}

		if (cm.getCache(cacheByTrip) == null) {
			cm.addCache(cacheByTrip);
		}
		cache = cm.getCache(cacheByTrip);
		
		//CacheConfiguration config = cache.getCacheConfiguration();							
		/*TODO We need to refine the eviction policy. */
		cache.setMemoryStoreEvictionPolicy(evictionPolicy);
	}
	/* (non-Javadoc)
	 * @see org.transitclock.core.dataCache.TripDataHistoryCacheInterface#getKeys()
	 */
	@Override
	public List<TripKey> getKeys()
	{
		return cache.getKeys();
	}
	/* (non-Javadoc)
	 * @see org.transitclock.core.dataCache.TripDataHistoryCacheInterface#logCache(org.slf4j.Logger)
	 */
	@Override
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
				logger.info("Key: "+key.toString());
				@SuppressWarnings("unchecked")
				
				List<ArrivalDeparture> ads=(List<ArrivalDeparture>) result.getObjectValue();
												
				for(ArrivalDeparture ad : ads)
				{
					logger.info(ad.toString());
				}
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see org.transitclock.core.dataCache.TripDataHistoryCacheInterface#getTripHistory(org.transitclock.core.dataCache.TripKey)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<ArrivalDeparture> getTripHistory(TripKey tripKey) {

		//logger.debug(cache.toString());
		logger.debug("Looking for TripDataHistoryCache cache element using key {}.", tripKey);
		
		Element result = cache.get(tripKey);

		if(result!=null)
		{						
			logger.debug("Found TripDataHistoryCache cache element using key {}.", tripKey);
			return (List<ArrivalDeparture>) result.getObjectValue();
		}
		else
		{
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.transitclock.core.dataCache.TripDataHistoryCacheInterface#putArrivalDeparture(org.transitclock.db.structs.ArrivalDeparture)
	 */
	@Override
	@SuppressWarnings("unchecked")
	synchronized public TripKey putArrivalDeparture(ArrivalDeparture arrivalDeparture) {
		
		Block block=null;
		if(arrivalDeparture.getBlock()==null)
		{
			DbConfig dbConfig = Core.getInstance().getDbConfig();
			block=dbConfig.getBlock(arrivalDeparture.getServiceId(), arrivalDeparture.getBlockId());								
		}else
		{
			block=arrivalDeparture.getBlock();
		}
		
		/* just put todays time in for last three days to aid development. This means it will kick in in 1 days rather than 3. Perhaps be a good way to start rather than using default transiTime method but I doubt it. */
		int days_back=1;
		if(debug)
			days_back=3;		
		TripKey tripKey=null;
		
		for(int i=0;i < days_back;i++)
		{
			Date nearestDay = DateUtils.truncate(new Date(arrivalDeparture.getTime()), Calendar.DAY_OF_MONTH);
									
			nearestDay=DateUtils.addDays(nearestDay, i*-1);
			
			DbConfig dbConfig = Core.getInstance().getDbConfig();
			
			Trip trip=dbConfig.getTrip(arrivalDeparture.getTripId());
			
			// TODO need to set start time based on start of bucket
			if(arrivalDeparture.getFreqStartTime()!=null)
			{
				Integer time=FrequencyBasedHistoricalAverageCache.secondsFromMidnight(arrivalDeparture.getFreqStartTime(),2);
				
				time=FrequencyBasedHistoricalAverageCache.round(time, FrequencyBasedHistoricalAverageCache.getCacheIncrementsForFrequencyService());
				
				if(trip!=null)							
				{			
										
					tripKey = new TripKey(arrivalDeparture.getTripId(),
							nearestDay,
							time);
					
					logger.debug("Putting :{} in TripDataHistoryCache cache using key {}.", arrivalDeparture, tripKey);
					
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
				}
			}								
			else
			{
				logger.error("Cannot add event to TripDataHistoryCache as it has no freqStartTime set. {}", arrivalDeparture);
			}
		}				
		return tripKey;
	}

	/* (non-Javadoc)
	 * @see org.transitclock.core.dataCache.TripDataHistoryCacheInterface#populateCacheFromDb(org.hibernate.Session, java.util.Date, java.util.Date)
	 */
	
	@Override
	public void populateCacheFromDb(Session session, Date startDate, Date endDate)
	{
 		Criteria criteria =session.createCriteria(ArrivalDeparture.class);				
		
		@SuppressWarnings("unchecked")
		List<ArrivalDeparture> results=criteria.add(Restrictions.between("time", startDate, endDate)).list();
						
		for(ArrivalDeparture result : results)		
		{						
			// TODO this might be better done in the database.						
			if(GtfsData.routeNotFiltered(result.getRouteId()))
			{
				TripDataHistoryCacheFactory.getInstance().putArrivalDeparture(result);
			}
		}		
	}
		
	/* (non-Javadoc)
	 * @see org.transitclock.core.dataCache.ehcache.test#findPreviousArrivalEvent(java.util.List, org.transitclock.db.structs.ArrivalDeparture)
	 */
	@Override
	public ArrivalDeparture findPreviousArrivalEvent(List<ArrivalDeparture> arrivalDepartures,ArrivalDeparture current)
	{
		Collections.sort(arrivalDepartures, new ArrivalDepartureComparator());
		for (ArrivalDeparture tocheck : emptyIfNull(arrivalDepartures)) 
		{
			if(tocheck.getStopId().equals(current.getStopId()) && (current.isDeparture() && tocheck.isArrival()))
			{
				return tocheck;
			}			
		}
		return null;
	}
	/* (non-Javadoc)
	 * @see org.transitclock.core.dataCache.ehcache.test#findPreviousDepartureEvent(java.util.List, org.transitclock.db.structs.ArrivalDeparture)
	 */
	@Override
	public ArrivalDeparture findPreviousDepartureEvent(List<ArrivalDeparture> arrivalDepartures,ArrivalDeparture current)
	{	
		Collections.sort(arrivalDepartures, new ArrivalDepartureComparator());							
		for (ArrivalDeparture tocheck : emptyIfNull(arrivalDepartures)) 
		{
			try {
				
				if(tocheck.getStopPathIndex()==(current.getStopPathIndex()-1) 
						&& (current.isArrival() && tocheck.isDeparture())
							&& current.getFreqStartTime().equals(tocheck.getFreqStartTime()))
				{					
					return tocheck;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}		
		return null;		
	}
	
	private static <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
		return iterable == null ? Collections.<T> emptyList() : iterable;
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
