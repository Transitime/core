package org.transitclock.core.dataCache.scheduled;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.core.DwellTimeDetails;
import org.transitclock.core.TravelTimeDetails;
import org.transitclock.core.dataCache.ArrivalDepartureComparator;
import org.transitclock.core.dataCache.HistoricalAverage;
import org.transitclock.core.dataCache.StopPathCacheKey;
import org.transitclock.core.dataCache.TripDataHistoryCacheFactory;
import org.transitclock.core.dataCache.TripKey;
import org.transitclock.core.dataCache.ehcache.scheduled.TripDataHistoryCache;
import org.transitclock.core.dataCache.frequency.FrequencyBasedHistoricalAverageCache;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Trip;
import org.transitclock.gtfs.DbConfig;
/**
 * @author Sean Óg Crudden
 * 
 */
public class ScheduleBasedHistoricalAverageCache {
	final private static String cacheName = "HistoricalAverageCache";
	private static ScheduleBasedHistoricalAverageCache singleton = new ScheduleBasedHistoricalAverageCache();
	private static final Logger logger = LoggerFactory
			.getLogger(ScheduleBasedHistoricalAverageCache.class);

	private Cache cache = null;
	/**
	 * Gets the singleton instance of this class.
	 * 
	 * @return
	 */
	public static ScheduleBasedHistoricalAverageCache getInstance() {
		return singleton;
	}
	
	private ScheduleBasedHistoricalAverageCache() {
		CacheManager cm = CacheManager.getInstance();
		
		if (cm.getCache(cacheName) == null) {
			cm.addCache(cacheName);
		}
		cache = cm.getCache(cacheName);											
	}
	public List<StopPathCacheKey> getKeys()
	{
		@SuppressWarnings("unchecked")
		List<StopPathCacheKey> keys = cache.getKeys();
		return keys;
	}
	public void logCache(Logger logger)
	{
		logger.debug("Cache content log.");
		@SuppressWarnings("unchecked")
		List<StopPathCacheKey> keys = cache.getKeys();
		
		for(StopPathCacheKey key : keys)
		{
			Element result=cache.get(key);
			if(result!=null)
			{
				logger.debug("Key: "+key.toString());
								
				HistoricalAverage value=(HistoricalAverage) result.getObjectValue();
												
				logger.debug("Average: "+value);
			}
		}		
	}
	public void logCacheSize(Logger logger)
	{
		@SuppressWarnings("unchecked")
		List<StopPathCacheKey> keys = cache.getKeys();
		
		if(keys!=null)
			logger.debug("Number of entries in HistoricalAverageCache : "+keys.size());
	}
	
	synchronized public HistoricalAverage getAverage(StopPathCacheKey key) {		
						
		Element result = cache.get(key);
		
		if(result==null)
			return null;
		else
			return (HistoricalAverage)result.getObjectValue();		
	}
	synchronized public void putAverage(StopPathCacheKey key, HistoricalAverage average) {
			
		logger.debug("Putting: "+key.toString()+" in cache with values : "+average);
		
		Element averageElement = new Element(key, average);
		
		cache.put(averageElement);
			
		logCacheSize(logger);
		// logCache(logger);
	}
	synchronized public void putArrivalDeparture(ArrivalDeparture arrivalDeparture) 
	{										
		DbConfig dbConfig = Core.getInstance().getDbConfig();
		
		Trip trip=dbConfig.getTrip(arrivalDeparture.getTripId());
		
		if(trip!=null && !trip.isNoSchedule())
		{					
			logger.debug("Putting :"+arrivalDeparture.toString() + " in HistoricalAverageCache cache.");
			
			TravelTimeDetails travelTimeDetails=getLastTravelTimeDetails(arrivalDeparture, trip);
			
			if(travelTimeDetails!=null&&travelTimeDetails.sanityCheck())
			{			
				if(!trip.isNoSchedule())
				{
					StopPathCacheKey historicalAverageCacheKey=new StopPathCacheKey(trip.getId(), arrivalDeparture.getStopPathIndex(), true);
					
					HistoricalAverage average = ScheduleBasedHistoricalAverageCache.getInstance().getAverage(historicalAverageCacheKey);
					
					if(average==null)				
						average=new HistoricalAverage();
					logger.debug("Updating historical averege for : {} with {}",historicalAverageCacheKey, travelTimeDetails);
					average.update(travelTimeDetails.getTravelTime());
					
					ScheduleBasedHistoricalAverageCache.getInstance().putAverage(historicalAverageCacheKey, average);
				}
			}		
			
			DwellTimeDetails dwellTimeDetails=getLastDwellTimeDetails(arrivalDeparture, trip);
			if(dwellTimeDetails!=null&&dwellTimeDetails.sanityCheck())
			{
				StopPathCacheKey historicalAverageCacheKey=new StopPathCacheKey(trip.getId(), arrivalDeparture.getStopPathIndex(), false);
				
				HistoricalAverage average = ScheduleBasedHistoricalAverageCache.getInstance().getAverage(historicalAverageCacheKey);
				
				if(average==null)				
					average=new HistoricalAverage();
				
				logger.debug("Updating historical averege for : {} with {}",historicalAverageCacheKey, dwellTimeDetails );
				average.update(dwellTimeDetails.getDwellTime());
			
				ScheduleBasedHistoricalAverageCache.getInstance().putAverage(historicalAverageCacheKey, average);
			}
		}
	}
	private TravelTimeDetails getLastTravelTimeDetails(ArrivalDeparture arrivalDeparture, Trip trip)
	{
		Date nearestDay = DateUtils.truncate(new Date(arrivalDeparture.getTime()), Calendar.DAY_OF_MONTH);
		TripKey tripKey = new TripKey(arrivalDeparture.getTripId(),
				nearestDay,
				trip.getStartTime());
						
		List<ArrivalDeparture> arrivalDepartures=(List<ArrivalDeparture>) TripDataHistoryCacheFactory.getInstance().getTripHistory(tripKey);
		
		if(arrivalDepartures!=null && arrivalDepartures.size()>0 && arrivalDeparture.isArrival())
		{			
			ArrivalDeparture previousEvent = TripDataHistoryCacheFactory.getInstance().findPreviousDepartureEvent(arrivalDepartures, arrivalDeparture);
			
			if(previousEvent!=null && arrivalDeparture!=null && previousEvent.isDeparture())
			{
				return new TravelTimeDetails(previousEvent, arrivalDeparture);				
			}
		}
					
		return null;
	}
	
	private DwellTimeDetails getLastDwellTimeDetails(ArrivalDeparture arrivalDeparture, Trip trip)
	{
		Date nearestDay = DateUtils.truncate(new Date(arrivalDeparture.getTime()), Calendar.DAY_OF_MONTH);
		TripKey tripKey = new TripKey(arrivalDeparture.getTripId(),
				nearestDay,
				trip.getStartTime());
						
		List<ArrivalDeparture> arrivalDepartures=(List<ArrivalDeparture>) TripDataHistoryCacheFactory.getInstance().getTripHistory(tripKey);
		
		if(arrivalDepartures!=null && arrivalDepartures.size()>0 && arrivalDeparture.isDeparture())
		{			
			ArrivalDeparture previousEvent = TripDataHistoryCacheFactory.getInstance().findPreviousArrivalEvent(arrivalDepartures, arrivalDeparture);
			
			if(previousEvent!=null && arrivalDeparture!=null && previousEvent.isArrival())
			{
				return new DwellTimeDetails(previousEvent, arrivalDeparture);
					
			}
		}
		return null;
	}
	public void populateCacheFromDb(Session session, Date startDate, Date endDate) 
	{
		Criteria criteria =session.createCriteria(ArrivalDeparture.class);				
		
		@SuppressWarnings("unchecked")
		List<ArrivalDeparture> results=criteria.add(Restrictions.between("time", startDate, endDate)).list();
		
		Collections.sort(results, new ArrivalDepartureComparator());
						
		for(ArrivalDeparture result : results)
		{
			ScheduleBasedHistoricalAverageCache.getInstance().putArrivalDeparture(result);			
		}		
	}
}
