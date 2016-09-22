package org.transitime.core.dataCache;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.db.structs.ArrivalDeparture;
import org.transitime.db.structs.Trip;
import org.transitime.gtfs.DbConfig;
/**
 * @author Sean Og Crudden
 * 
 */
public class HistoricalAverageCache {
	final private static String cacheName = "HistoricalAverageCache";
	private static HistoricalAverageCache singleton = new HistoricalAverageCache();
	private static final Logger logger = LoggerFactory
			.getLogger(HistoricalAverageCache.class);

	private Cache cache = null;
	/**
	 * Gets the singleton instance of this class.
	 * 
	 * @return
	 */
	public static HistoricalAverageCache getInstance() {
		return singleton;
	}
	
	private HistoricalAverageCache() {
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
		logger.debug("Putting :"+arrivalDeparture.toString() + " in HistoricalAverageCache cache.");
		
		DbConfig dbConfig = Core.getInstance().getDbConfig();
		
		Trip trip=dbConfig.getTrip(arrivalDeparture.getTripId());
		
		double pathDuration=getLastPathDuration(arrivalDeparture, trip);
		
		if(pathDuration>0)
		{
			
			StopPathCacheKey historicalAverageCacheKey=new StopPathCacheKey(trip.getId(), arrivalDeparture.getStopPathIndex(), true);
			
			HistoricalAverage average = HistoricalAverageCache.getInstance().getAverage(historicalAverageCacheKey);
			
			if(average==null)				
				average=new HistoricalAverage();
			
			average.update(pathDuration);
		
			HistoricalAverageCache.getInstance().putAverage(historicalAverageCacheKey, average);
		}		
		
		double stopDuration=getLastStopDuration(arrivalDeparture, trip);
		if(stopDuration>0)
		{
			StopPathCacheKey historicalAverageCacheKey=new StopPathCacheKey(trip.getId(), arrivalDeparture.getStopPathIndex(), false);
			
			HistoricalAverage average = HistoricalAverageCache.getInstance().getAverage(historicalAverageCacheKey);
			
			if(average==null)				
				average=new HistoricalAverage();
			
			average.update(stopDuration);
		
			HistoricalAverageCache.getInstance().putAverage(historicalAverageCacheKey, average);
		}
	}
	private double getLastPathDuration(ArrivalDeparture arrivalDeparture, Trip trip)
	{
		Date nearestDay = DateUtils.truncate(new Date(arrivalDeparture.getTime()), Calendar.DAY_OF_MONTH);
		TripKey tripKey = new TripKey(arrivalDeparture.getTripId(),
				nearestDay,
				trip.getStartTime());
						
		List<ArrivalDeparture> arrivalDepartures=(List<ArrivalDeparture>) TripDataHistoryCache.getInstance().getTripHistory(tripKey);
		
		if(arrivalDepartures!=null && arrivalDepartures.size()>0 && arrivalDeparture.isArrival())
		{			
			ArrivalDeparture previousEvent = TripDataHistoryCache.findPreviousDepartureEvent(arrivalDepartures, arrivalDeparture);
			
			if(previousEvent!=null && arrivalDeparture!=null && previousEvent.isDeparture())
					return Math.abs(previousEvent.getTime()-arrivalDeparture.getTime());
		}
					
		return -1;
	}
	private double getLastStopDuration(ArrivalDeparture arrivalDeparture, Trip trip)
	{
		Date nearestDay = DateUtils.truncate(new Date(arrivalDeparture.getTime()), Calendar.DAY_OF_MONTH);
		TripKey tripKey = new TripKey(arrivalDeparture.getTripId(),
				nearestDay,
				trip.getStartTime());
						
		List<ArrivalDeparture> arrivalDepartures=(List<ArrivalDeparture>) TripDataHistoryCache.getInstance().getTripHistory(tripKey);
		
		if(arrivalDepartures!=null && arrivalDepartures.size()>0 && arrivalDeparture.isDeparture())
		{			
			ArrivalDeparture previousEvent = TripDataHistoryCache.findPreviousArrivalEvent(arrivalDepartures, arrivalDeparture);
			
			if(previousEvent!=null && arrivalDeparture!=null && previousEvent.isArrival())
					return Math.abs(previousEvent.getTime()-arrivalDeparture.getTime());
		}
		return -1;
	}
}
