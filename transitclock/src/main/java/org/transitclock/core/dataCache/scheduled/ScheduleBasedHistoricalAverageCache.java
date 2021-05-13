package org.transitclock.core.dataCache.scheduled;

import org.apache.commons.lang3.time.DateUtils;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.BooleanConfigValue;
import org.transitclock.core.DwellTimeDetails;
import org.transitclock.core.TravelTimeDetails;
import org.transitclock.core.dataCache.*;
import org.transitclock.core.dataCache.ehcache.CacheManagerFactory;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Trip;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.ipc.data.IpcArrivalDeparture;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sean Óg Crudden
 * 
 */
public class ScheduleBasedHistoricalAverageCache {
	final private static String cacheName = "HistoricalAverageCache";

	public static BooleanConfigValue enableHistoricalCaches =
					new BooleanConfigValue(
									"transitclock.core.cache.enableHistoricalCaches",
									false,
									"Enable experimental historical caches");

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
		if (enableHistoricalCaches.getValue())
			return singleton;
		return null;
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
	public void putAverageInMemory(Map map, StopPathCacheKey key, HistoricalAverage average) {
		map.put(key, average);
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

					HistoricalAverage empty = new HistoricalAverage(travelTimeDetails.getTravelTime());
					synchronized (cache) {
						HistoricalAverage average = getAverage(historicalAverageCacheKey);

						if (average == null) {
							putAverage(historicalAverageCacheKey, empty);
						} else{
							average = average.copyUpdate(travelTimeDetails.getTravelTime());
							putAverage(historicalAverageCacheKey, average);
						}
					}
				}
			}		
			
			DwellTimeDetails dwellTimeDetails=getLastDwellTimeDetails(new IpcArrivalDeparture(arrivalDeparture), trip);
			if(dwellTimeDetails!=null&&dwellTimeDetails.sanityCheck())
			{
				StopPathCacheKey historicalAverageCacheKey=new StopPathCacheKey(trip.getId(), arrivalDeparture.getStopPathIndex(), false);

				HistoricalAverage empty = new HistoricalAverage(dwellTimeDetails.getDwellTime());
				synchronized (cache) {
					HistoricalAverage average = getAverage(historicalAverageCacheKey);

					if (average == null) {
						putAverage(historicalAverageCacheKey, empty);
					} else {
						logger.trace("Updating historical averege for : {} with {}", historicalAverageCacheKey, dwellTimeDetails);
						average = average.copyUpdate(dwellTimeDetails.getDwellTime());
						putAverage(historicalAverageCacheKey, average);
					}
				}
			}
		}
	}

	public void putArrivalDepartureInMemory(Map map, ArrivalDeparture arrivalDeparture) throws Exception
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

					HistoricalAverage empty = new HistoricalAverage(0, 0);
					empty = empty.copyUpdate(travelTimeDetails.getTravelTime());
					HistoricalAverage average = getAverage(historicalAverageCacheKey);

					if (average == null) {
						putAverageInMemory(map, historicalAverageCacheKey, empty);
					} else{
						average = average.copyUpdate(travelTimeDetails.getTravelTime());
						putAverageInMemory(map, historicalAverageCacheKey, average);
					}
				}
			}

			DwellTimeDetails dwellTimeDetails=getLastDwellTimeDetails(new IpcArrivalDeparture(arrivalDeparture), trip);
			if(dwellTimeDetails!=null&&dwellTimeDetails.sanityCheck())
			{
				StopPathCacheKey historicalAverageCacheKey=new StopPathCacheKey(trip.getId(), arrivalDeparture.getStopPathIndex(), false);

				HistoricalAverage empty = new HistoricalAverage(0, 0);
				empty = empty.copyUpdate(dwellTimeDetails.getDwellTime());
				HistoricalAverage average = getAverage(historicalAverageCacheKey);

				if (average == null) {
					putAverageInMemory(map, historicalAverageCacheKey, empty);
				} else {
					logger.trace("Updating historical averege for : {} with {}", historicalAverageCacheKey, dwellTimeDetails);
					average = average.copyUpdate(dwellTimeDetails.getDwellTime());
					putAverageInMemory(map, historicalAverageCacheKey, average);
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


		List<IpcArrivalDeparture> arrivalDepartures = new ArrayList<>(emptyIfNull(TripDataHistoryCacheFactory.getInstance().getTripHistory(tripKey)));
		
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

		// copy this list in case it changes underneath us
		List<IpcArrivalDeparture> arrivalDepartures = new ArrayList<>(emptyIfNull(TripDataHistoryCacheFactory.getInstance().getTripHistory(tripKey)));
		
		if(arrivalDepartures.size() > 0 && arrivalDeparture.isDeparture())
		{			
			IpcArrivalDeparture previousEvent = TripDataHistoryCacheFactory.getInstance().findPreviousArrivalEvent(arrivalDepartures, arrivalDeparture);
			
			if(previousEvent!=null && arrivalDeparture!=null && previousEvent.isArrival())
			{
				return new DwellTimeDetails(previousEvent, arrivalDeparture);
					
			}
		}
		return null;
	}

	private List<IpcArrivalDeparture> emptyIfNull(List<IpcArrivalDeparture> tripHistory) {
		if (tripHistory == null) return new ArrayList<>();
		return tripHistory;
	}

	public void populateCacheFromDb(List<ArrivalDeparture> resultsUnsafe) throws Exception
	{
		Map<StopPathCacheKey, HistoricalAverage> map = new HashMap<>();
		try {
			if (resultsUnsafe == null) return;
			List<ArrivalDeparture> results = new ArrayList<>(resultsUnsafe);
			Collections.sort(results, new ArrivalDepartureComparator());

			int counter = 0;
			for (ArrivalDeparture result : results) {
				if (counter % 1000 == 0) {
					logger.info("{} out of {} ScheduleBased Historical Records ({}%)", counter, results.size(), (int) ((counter * 100.0f) / results.size()));
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

	public List<StopPathCacheKey> getKeys() {
		// TODO Auto-generated method stub
		return null;
	}
}
