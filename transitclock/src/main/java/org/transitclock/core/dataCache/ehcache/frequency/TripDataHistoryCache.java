/**
 * 
 */
package org.transitclock.core.dataCache.ehcache.frequency;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.core.dataCache.*;
import org.transitclock.core.dataCache.ehcache.CacheManagerFactory;
import org.transitclock.core.dataCache.frequency.FrequencyBasedHistoricalAverageCache;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Block;
import org.transitclock.db.structs.Trip;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.gtfs.GtfsData;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.utils.DateUtils;
import org.transitclock.utils.Time;

import java.util.*;

/**
 * @author Sean Og Crudden 
 * 		   This is a Cache to hold historical arrival departure data for trips. It
 *         is intended to look up a trips historical data when a trip starts and
 *         place in cache for use in generating predictions based on a Kalman
 *         filter. Uses Ehcache for caching rather than just using a concurrent
 *         hashmap. This approach to holding data in memory for transitclock needs
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

	private Cache<TripKey, TripEvents> cache = null;

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
		CacheManager cm = CacheManagerFactory.getInstance();
		

							
		cache = cm.getCache(cacheByTrip, TripKey.class, TripEvents.class);
	}
	
	
	public void logCache(Logger logger)
	{
		logger.debug("Cache content log. Not implemented.");
				
	}

	/* (non-Javadoc)
	 * @see org.transitclock.core.dataCache.TripDataHistoryCacheInterface#getTripHistory(org.transitclock.core.dataCache.TripKey)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<IpcArrivalDeparture> getTripHistory(TripKey tripKey) {

		//logger.debug(cache.toString());
		logger.debug("Looking for TripDataHistoryCache cache element using key {}.", tripKey);
		
		TripEvents result = cache.get(tripKey);

		if(result!=null)
		{						
			logger.debug("Found TripDataHistoryCache cache element using key {}.", tripKey);
			return (List<IpcArrivalDeparture>) result.getEvents();
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
	public TripKey putArrivalDeparture(ArrivalDeparture arrivalDeparture) {
		
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
		
		for(int i=0;i < days_back;i++) {
			Date nearestDay = DateUtils.truncate(new Date(arrivalDeparture.getTime()), Calendar.DAY_OF_MONTH);
									
			nearestDay=DateUtils.addDays(nearestDay, i*-1);
			
			DbConfig dbConfig = Core.getInstance().getDbConfig();
			
			Trip trip=dbConfig.getTrip(arrivalDeparture.getTripId());
			
			// TODO need to set start time based on start of bucket
			if(arrivalDeparture.getFreqStartTime()!=null) {
				Integer time = FrequencyBasedHistoricalAverageCache.secondsFromMidnight(arrivalDeparture.getFreqStartTime(), 2);

				time = FrequencyBasedHistoricalAverageCache.round(time, FrequencyBasedHistoricalAverageCache.getCacheIncrementsForFrequencyService());

				if (trip != null) {
					tripKey = new TripKey(arrivalDeparture.getRouteId(),
									arrivalDeparture.getDirectionId(),
									nearestDay.getTime(),
									time);

					logger.debug("Putting :{} in TripDataHistoryCache cache using key {}.", arrivalDeparture, tripKey);
					IpcArrivalDeparture ipcad = null;
					try {
						ipcad = new IpcArrivalDeparture(arrivalDeparture);
					} catch (Exception e) {
						logger.error("Exception creating IpcArrivalDeparture {}", e, e);
						return tripKey;
					}
					TripEvents empty = new TripEvents(ipcad);

					synchronized (cache) {
						TripEvents element = cache.get(tripKey);
						if (element == null) {
							cache.put(tripKey, empty);
						} else {
							// immutable object for thread safety
							element = element.copyAdd(ipcad);
							cache.put(tripKey, element);
						}
					}
				}
			} else {
				logger.error("Cannot add event to TripDataHistoryCache as it has no freqStartTime set. {}", arrivalDeparture);
			}
		}				
		return tripKey;
	}

	public TripKey putArrivalDepartureInMemory(Map<TripKey, TripEvents> map, ArrivalDeparture arrivalDeparture) {

		/* just put todays time in for last three days to aid development. This means it will kick in in 1 days rather than 3. Perhaps be a good way to start rather than using default transiTime method but I doubt it. */
		Date nearestDay = DateUtils.truncate(new Date(arrivalDeparture.getTime()), Calendar.DAY_OF_MONTH);

		DbConfig dbConfig = Core.getInstance().getDbConfig();
		TripKey tripKey = null;
		Trip trip = dbConfig.getTrip(arrivalDeparture.getTripId());

		// TODO need to set start time based on start of bucket
		if (arrivalDeparture.getFreqStartTime() != null) {
			Integer time = FrequencyBasedHistoricalAverageCache.secondsFromMidnight(arrivalDeparture.getFreqStartTime(), 2);

			time = FrequencyBasedHistoricalAverageCache.round(time, FrequencyBasedHistoricalAverageCache.getCacheIncrementsForFrequencyService());

			if (trip != null) {
				tripKey = new TripKey(arrivalDeparture.getRouteId(),
								arrivalDeparture.getDirectionId(),
								nearestDay.getTime(),
								time);

				logger.debug("Putting :{} in TripDataHistoryCache cache using key {}.", arrivalDeparture, tripKey);
				IpcArrivalDeparture ipcad = null;
				try {
					ipcad = new IpcArrivalDeparture(arrivalDeparture);
				} catch (Exception e) {
					logger.error("Exception creating IpcArrivalDeparture {}", e, e);
					return tripKey;
				}
				TripEvents empty = new TripEvents(ipcad);

				TripEvents element = map.get(tripKey);
				if (element == null) {
					map.put(tripKey, empty);
				} else {
					// immutable object for thread safety
					element = element.copyAdd(ipcad);
					map.put(tripKey, element);
				}
			}
		} else {
			logger.error("Cannot add event to TripDataHistoryCache as it has no freqStartTime set. {}", arrivalDeparture);
		}
		return tripKey;
	}
		/* (non-Javadoc)
	 * @see org.transitclock.core.dataCache.TripDataHistoryCacheInterface#populateCacheFromDb(org.hibernate.Session, java.util.Date, java.util.Date)
	 */
	
	@Override
	public void populateCacheFromDb(List<ArrivalDeparture> results)
	{
		Map<TripKey, TripEvents> map = new HashMap<>(results.size());
		try {
			for (ArrivalDeparture result : results) {
				// TODO this might be better done in the database.
				if (GtfsData.routeNotFiltered(result.getRouteId())) {
					putArrivalDepartureInMemory(map, result);
				}
			}
		} catch (Throwable t) {
			logger.error("Exception in populateCacheFromDb {}", t, t);
		}
		synchronized (cache) {
			cache.putAll(map);
		}
	}
		
	/* (non-Javadoc)
	 * @see org.transitclock.core.dataCache.ehcache.test#findPreviousArrivalEvent(java.util.List, org.transitclock.db.structs.ArrivalDeparture)
	 */
	@Override
	public IpcArrivalDeparture findPreviousArrivalEvent(List<IpcArrivalDeparture> arrivalDepartures,IpcArrivalDeparture current)
	{
		Collections.sort(arrivalDepartures, new IpcArrivalDepartureComparator());
		for (IpcArrivalDeparture tocheck : emptyIfNull(arrivalDepartures)) 
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
	public IpcArrivalDeparture findPreviousDepartureEvent(List<IpcArrivalDeparture> arrivalDepartures,IpcArrivalDeparture current)
	{	
		Collections.sort(arrivalDepartures, new IpcArrivalDepartureComparator());							
		for (IpcArrivalDeparture tocheck : emptyIfNull(arrivalDepartures)) 
		{
			try {
				
				if(tocheck.getStopPathIndex()==(current.getStopPathIndex()-1) 
						&& (current.isArrival() && tocheck.isDeparture())
							&& current.getFreqStartTime().equals(tocheck.getFreqStartTime()))
				{					
					return tocheck;
				}
			} catch (Exception e) {
				logger.error("exception {}", e, e);
			}			
		}		
		return null;		
	}
	
	private static <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
		return iterable == null ? Collections.<T> emptyList() : iterable;
	}

	@Override
	public List<TripKey> getKeys() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
