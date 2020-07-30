package org.transitclock.core.dataCache.scheduled;

import org.apache.commons.lang3.time.DateUtils;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.core.DwellTimeDetails;
import org.transitclock.core.TravelTimeDetails;
import org.transitclock.core.dataCache.*;
import org.transitclock.core.dataCache.ehcache.CacheManagerFactory;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Trip;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.ipc.data.IpcArrivalDeparture;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
/**
 * @author Sean Óg Crudden
 * 
 */
public class ScheduleBasedHistoricalAverageCache {
	final private static String cacheName = "HistoricalAverageCache";
	private static ScheduleBasedHistoricalAverageCache singleton = new ScheduleBasedHistoricalAverageCache();
	private static final Logger logger = LoggerFactory
			.getLogger(ScheduleBasedHistoricalAverageCache.class);
	private Cache<StopPathCacheKey, HistoricalAverage> cache = null;
	/**
	 * Gets the singleton instance of this class.
	 * 
	 * @return
	 */
	public static ScheduleBasedHistoricalAverageCache getInstance() {
		return singleton;
	}
	
	private ScheduleBasedHistoricalAverageCache() {
		CacheManager cm = CacheManagerFactory.getInstance();
							
		cache = cm.getCache(cacheName, StopPathCacheKey.class, HistoricalAverage.class);										
	}
	
	public void logCache(Logger logger)
	{
		logger.debug("Cache content log. Not implemented.");
				
	}
	public void logCacheSize(Logger logger)
	{
		logger.debug("Log cache size. Not implemented.");
	}
	
	public HistoricalAverage getAverage(StopPathCacheKey key) {
						
		 return cache.get(key);
	}
 	public void putAverage(StopPathCacheKey key, HistoricalAverage average) {
			
		logger.debug("Putting: {} in cache with values : {}", key.toString(), average);
						
		cache.put(key, average);				
		// logCache(logger);
	}
	public void putArrivalDeparture(ArrivalDeparture arrivalDeparture) throws Exception
	{										
		DbConfig dbConfig = Core.getInstance().getDbConfig();
		
		Trip trip=dbConfig.getTrip(arrivalDeparture.getTripId());
		
		if(trip!=null && !trip.isNoSchedule())
		{					
			logger.debug("Putting :"+arrivalDeparture.toString() + " in HistoricalAverageCache cache.");
			
			TravelTimeDetails travelTimeDetails=getLastTravelTimeDetails(new IpcArrivalDeparture(arrivalDeparture), trip);
			
			if(travelTimeDetails!=null&&travelTimeDetails.sanityCheck())
			{			
				if(!trip.isNoSchedule())
				{
					StopPathCacheKey historicalAverageCacheKey=new StopPathCacheKey(trip.getId(), arrivalDeparture.getStopPathIndex(), true);

					synchronized (cache) {
						HistoricalAverage average = ScheduleBasedHistoricalAverageCache.getInstance().getAverage(historicalAverageCacheKey);

						if (average == null)
							average = new HistoricalAverage();
						logger.trace("Updating historical averege for : {} with {}", historicalAverageCacheKey, travelTimeDetails);
						average.update(travelTimeDetails.getTravelTime());

						ScheduleBasedHistoricalAverageCache.getInstance().putAverage(historicalAverageCacheKey, average);
					}
				}
			}		
			
			DwellTimeDetails dwellTimeDetails=getLastDwellTimeDetails(new IpcArrivalDeparture(arrivalDeparture), trip);
			if(dwellTimeDetails!=null&&dwellTimeDetails.sanityCheck())
			{
				StopPathCacheKey historicalAverageCacheKey=new StopPathCacheKey(trip.getId(), arrivalDeparture.getStopPathIndex(), false);

				synchronized (cache) {
					HistoricalAverage average = ScheduleBasedHistoricalAverageCache.getInstance().getAverage(historicalAverageCacheKey);

					if (average == null)
						average = new HistoricalAverage();

					logger.trace("Updating historical averege for : {} with {}", historicalAverageCacheKey, dwellTimeDetails);
					average.update(dwellTimeDetails.getDwellTime());

					ScheduleBasedHistoricalAverageCache.getInstance().putAverage(historicalAverageCacheKey, average);
				}
			}
		}
	}
	private TravelTimeDetails getLastTravelTimeDetails(IpcArrivalDeparture arrivalDeparture, Trip trip)
	{
		Date nearestDay = DateUtils.truncate(new Date(arrivalDeparture.getTime().getTime()), Calendar.DAY_OF_MONTH);
		TripKey tripKey = new TripKey(arrivalDeparture.getTripId(),
				nearestDay,
				trip.getStartTime());
						
		List<IpcArrivalDeparture> arrivalDepartures=(List<IpcArrivalDeparture>) TripDataHistoryCacheFactory.getInstance().getTripHistory(tripKey);
		
		if(arrivalDepartures!=null && arrivalDepartures.size()>0 && arrivalDeparture.isArrival())
		{			
			IpcArrivalDeparture previousEvent = TripDataHistoryCacheFactory.getInstance().findPreviousDepartureEvent(arrivalDepartures, arrivalDeparture);
			
			if(previousEvent!=null && arrivalDeparture!=null && previousEvent.isDeparture())
			{
				return new TravelTimeDetails(previousEvent, arrivalDeparture);				
			}
		}
					
		return null;
	}
	
	private DwellTimeDetails getLastDwellTimeDetails(IpcArrivalDeparture arrivalDeparture, Trip trip)
	{
		Date nearestDay = DateUtils.truncate(new Date(arrivalDeparture.getTime().getTime()), Calendar.DAY_OF_MONTH);
		TripKey tripKey = new TripKey(arrivalDeparture.getTripId(),
				nearestDay,
				trip.getStartTime());
						
		List<IpcArrivalDeparture> arrivalDepartures=(List<IpcArrivalDeparture>) TripDataHistoryCacheFactory.getInstance().getTripHistory(tripKey);
		
		if(arrivalDepartures!=null && arrivalDepartures.size()>0 && arrivalDeparture.isDeparture())
		{			
			IpcArrivalDeparture previousEvent = TripDataHistoryCacheFactory.getInstance().findPreviousArrivalEvent(arrivalDepartures, arrivalDeparture);
			
			if(previousEvent!=null && arrivalDeparture!=null && previousEvent.isArrival())
			{
				return new DwellTimeDetails(previousEvent, arrivalDeparture);
					
			}
		}
		return null;
	}
	public void populateCacheFromDb(Session session, Date startDate, Date endDate) throws Exception 
	{
		Criteria criteria =session.createCriteria(ArrivalDeparture.class);
		List<ArrivalDeparture> results=criteria.add(Restrictions.between("time", startDate, endDate)).list();
		Collections.sort(results, new ArrivalDepartureComparator());

		int counter = 0;
		for(ArrivalDeparture result : results)
		{
			if(counter % 1000 == 0){
				logger.info("{} out of {} ScheduleBased Historical Records for period {} to {} ({}%)", counter, results.size(), startDate, endDate, (int)((counter * 100.0f) / results.size()));
			}
			ScheduleBasedHistoricalAverageCache.getInstance().putArrivalDeparture(result);
			counter++;
		}		
	}

	public List<StopPathCacheKey> getKeys() {
		// TODO Auto-generated method stub
		return null;
	}
}
