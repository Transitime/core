/**
 * 
 */
package org.transitclock.core.dataCache.ehcache.scheduled;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.core.dataCache.*;
import org.transitclock.core.dataCache.ehcache.CacheManagerFactory;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Trip;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.gtfs.GtfsData;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.utils.DateUtils;
import org.transitclock.utils.Time;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Sean Og Crudden 
 * 		   This is a Cache to hold historical arrival departure data for schedule based trips. It
 *         is intended to look up a trips historical data when a trip starts and
 *         place in cache for use in generating predictions based on a Kalman
 *         filter. 
 *         
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
	

	/* (non-Javadoc)
	 * @see org.transitclock.core.dataCache.TripDataHistoryCacheInterface#getTripHistory(org.transitclock.core.dataCache.TripKey)
	 */
	@Override
	public List<IpcArrivalDeparture> getTripHistory(TripKey tripKey) {
		TripEvents result = null;
		synchronized (cache) {
			// we need to protect the deserializer from modifications
			result = cache.get(tripKey);
		}

		if (result != null) {
			// TripEvent is immutable so this call is now threadsafe
			return result.getEvents();
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.transitclock.core.dataCache.TripDataHistoryCacheInterface#putArrivalDeparture(org.transitclock.db.structs.ArrivalDeparture)
	 */
	@Override	
	public TripKey putArrivalDeparture(ArrivalDeparture arrivalDeparture) {
		
		logger.trace("Putting :"+arrivalDeparture.toString() + " in TripDataHistoryCache cache.");
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
			Integer tripStartTime = null;

			if (trip != null) {
				tripStartTime = trip.getStartTime();
			}

			tripKey = new TripKey(arrivalDeparture.getRouteId(),
					arrivalDeparture.getDirectionId(),
					nearestDay.getTime(),
					tripStartTime);

			IpcArrivalDeparture ipcad = null;
			try {
				ipcad = new IpcArrivalDeparture(arrivalDeparture);
			} catch (Exception e) {
				logger.error("Error adding "+arrivalDeparture.toString()+" event to TripDataHistoryCache.", e);
			}
			TripEvents empty = new TripEvents(ipcad);

			synchronized (cache) {
				TripEvents result = cache.get(tripKey);
				if (result == null) {
					cache.put(tripKey, empty);
				} else {
					// update TripEvents in a threadsafe way
					// object needs to be immutable for ehcache to store
					result = result.copyAdd(ipcad);
					cache.put(tripKey, result);
				}
			}
		}
		return tripKey;
	}

	public TripKey putArrivalDeparture(Map<TripKey, TripEvents> map, ArrivalDeparture arrivalDeparture,
																		 DbConfig dbConfig, Date nearestDay)
		throws Exception {

		Trip trip = dbConfig.getTripFromCurrentOrPreviousConfigRev(arrivalDeparture.getTripId());
		Integer tripStartTime = null;

		if (trip != null) {
			tripStartTime = trip.getStartTime();
		} else {
			logger.info("missing trip {}, invalid A/D", arrivalDeparture.getTripId());
		}

		TripKey tripKey = new TripKey(arrivalDeparture.getRouteId(),
						arrivalDeparture.getDirectionId(),
						nearestDay.getTime(),
						tripStartTime);

		IpcArrivalDeparture ipcad = new IpcArrivalDeparture(arrivalDeparture);

		TripEvents result = map.get(tripKey);
		if (result == null) {
			map.put(tripKey, new TripEvents(ipcad));
		} else {
			// update TripEvents in a threadsafe way
			// object needs to be immutable for ehcache to store
			//map.put(tripKey, result.copyAdd(ipcad));
			map.put(tripKey, result.addUnsafe(ipcad));
		}
		return tripKey;
	}

	/* (non-Javadoc)
	 * @see org.transitclock.core.dataCache.TripDataHistoryCacheInterface#populateCacheFromDb(org.hibernate.Session, java.util.Date, java.util.Date)
	 */
	
	@Override
	public void populateCacheFromDb(List<ArrivalDeparture> results)
	{
		if (results == null || results.isEmpty()) {
			logger.info("in populateCacheFromDb with null");
			return;
		}
		logger.info("in populateCacheFromDb with results entries" + results.size());

		Map<TripKey, TripEvents> map = new HashMap<>(results.size());
		Date nearestDay = DateUtils.truncate(new Date(results.get(0).getTime()), Calendar.DAY_OF_MONTH);
		Date furthestDay = DateUtils.truncate(new Date(results.get(results.size()-1).getTime()), Calendar.DAY_OF_MONTH);
		boolean cacheNearestDay = (nearestDay.equals(furthestDay));
		DbConfig dbConfig = Core.getInstance().getDbConfig();

		try {
			int counter = 0;
			for (ArrivalDeparture result : results) {
				if (counter % 1000 == 0) {
					logger.info("{} out of {} scheduled Trip Data History Records ({}%)", counter, results.size(), (int) ((counter * 100.0f) / results.size()));
				}
				// TODO this might be better done in the database.
				if (GtfsData.routeNotFiltered(result.getRouteId())) {
					if (cacheNearestDay) {
						putArrivalDeparture(map, result, dbConfig, nearestDay);
					} else {
						putArrivalDeparture(map, result, dbConfig,
										DateUtils.truncate(new Date(result.getTime()), Calendar.DAY_OF_MONTH));
					}
				} else {
					logger.info("filtered route " + result.getRouteId());
				}
				counter++;
			}
		} catch (Throwable t) {
			logger.error("Exception in populateCacheFromDb {}", t, t);
		}

		logger.info("sorting Trip Data History Records of {}", map.size());
		for (TripEvents value : map.values()) {
			value.sort();
		}
		logger.info("sorted Trip Data History Records of {}", map.size());

		synchronized (cache) {
			logger.info("adding " + map.size() + " to cache");
			cache.putAll(map);
		}
	}

	/* (non-Javadoc)
	 * @see org.transitclock.core.dataCache.ehcache.test#findPreviousArrivalEvent(java.util.List, org.transitclock.db.structs.ArrivalDeparture)
	 */
	@Override
	public IpcArrivalDeparture findPreviousArrivalEvent(List<IpcArrivalDeparture> arrivalDepartures,IpcArrivalDeparture current)
	{
		if (arrivalDepartures == null) return null;
		Collections.sort(arrivalDepartures, new IpcArrivalDepartureComparator());
		for (IpcArrivalDeparture tocheck : emptyIfNull(arrivalDepartures)) {
			if (tocheck.getStopId().equals(current.getStopId()) && (current.isDeparture() && tocheck.isArrival())) {
				return tocheck;
			}
		}
		return null;
	}
	/* (non-Javadoc)
	 * @see org.transitclock.core.dataCache.ehcache.test#findPreviousDepartureEvent(java.util.List, org.transitclock.db.structs.ArrivalDeparture)
	 */
	@Override
	public IpcArrivalDeparture findPreviousDepartureEvent(List<IpcArrivalDeparture> arrivalDeparturesUnsafe,IpcArrivalDeparture current)
	{
		if (arrivalDeparturesUnsafe == null) return null;
		List<IpcArrivalDeparture> arrivalDepartures = new ArrayList<>(arrivalDeparturesUnsafe);
		Collections.sort(arrivalDepartures, new IpcArrivalDepartureComparator());
		for (IpcArrivalDeparture tocheck : emptyIfNull(arrivalDepartures)) {
			try {
				if (tocheck.getStopPathIndex() == (current.getStopPathIndex() - 1) && (current.isArrival() && tocheck.isDeparture())) {
					return tocheck;
				}
			} catch (Exception e) {
				logger.error("Exception searching {}", e, e);
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
