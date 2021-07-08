package org.transitclock.core.dataCache.jcs.frequency;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.core.dataCache.ArrivalDepartureComparator;
import org.transitclock.core.dataCache.IpcArrivalDepartureComparator;
import org.transitclock.core.dataCache.TripDataHistoryCacheFactory;
import org.transitclock.core.dataCache.TripDataHistoryCacheInterface;
import org.transitclock.core.dataCache.TripKey;
import org.transitclock.core.dataCache.frequency.FrequencyBasedHistoricalAverageCache;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Trip;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.gtfs.GtfsData;
import org.transitclock.ipc.data.IpcArrivalDeparture;



public class TripDataHistoryCache implements TripDataHistoryCacheInterface {
	final private static String cacheName = "FrequencyTripDataHistoryCache";
	
	private static final Logger logger = LoggerFactory
			.getLogger(TripDataHistoryCache.class);
	
	private CacheAccess<TripKey, List<IpcArrivalDeparture>>  cache = null;
	
	
	public List<TripKey> getKeys() {
		ArrayList<TripKey> fulllist=new ArrayList<TripKey>();
		Set<String> names = JCS.getGroupCacheInstance(cacheName).getGroupNames();
		
		for(String name:names)
		{
			Set<Object> keys = JCS.getGroupCacheInstance(cacheName).getGroupKeys(name);
			
			for(Object key:keys)
			{
				fulllist.add((TripKey)key);
			}
		}
		return fulllist;
	}

	public TripDataHistoryCache() {
		cache = JCS.getInstance(cacheName);		
	}

	
	public void logCache(Logger logger) {
		
		logger.debug("Cache content log. Not implemented.");		
	}

	@Override
	public List<IpcArrivalDeparture> getTripHistory(TripKey tripKey) {	
				
		/* this is what gets the trip from the buckets */
		int time = FrequencyBasedHistoricalAverageCache.round(tripKey.getStartTime(), FrequencyBasedHistoricalAverageCache.getCacheIncrementsForFrequencyService());
		
		tripKey.setStartTime(time);
				
		return cache.get(tripKey);
	}

	@Override
	synchronized public TripKey putArrivalDeparture(ArrivalDeparture arrivalDeparture) {
		logger.debug("Putting :"+arrivalDeparture.toString() + " in TripDataHistoryCache cache.");
		/* just put todays time in for last three days to aid development. This means it will kick in in 1 days rather than 3. Perhaps be a good way to start rather than using default transiTime method but I doubt it. */
		int days_back=1;
		
		TripKey tripKey=null;
		
		for(int i=0;i < days_back;i++)
		{
			Date nearestDay = DateUtils.truncate(new Date(arrivalDeparture.getTime()), Calendar.DAY_OF_MONTH);
									 
			nearestDay=DateUtils.addDays(nearestDay, i*-1);
			
			DbConfig dbConfig = Core.getInstance().getDbConfig();
			
			Trip trip=dbConfig.getTrip(arrivalDeparture.getTripId());
			
			if(trip!=null)
			{				
				Integer time=FrequencyBasedHistoricalAverageCache.secondsFromMidnight(arrivalDeparture.getDate(),2);
				
				/* this is what gets the trip from the buckets */
				time=FrequencyBasedHistoricalAverageCache.round(time, FrequencyBasedHistoricalAverageCache.getCacheIncrementsForFrequencyService());
				
				tripKey = new TripKey(arrivalDeparture.getTripId(),
						nearestDay,
						time);
										
				List<IpcArrivalDeparture> list  = cache.get(tripKey);
		
				if(list==null)				
					list = new ArrayList<IpcArrivalDeparture>();				
				
				try {
					list.add(new IpcArrivalDeparture(arrivalDeparture));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				cache.put(tripKey, Collections.synchronizedList(list));							
			}														
		}				
		return tripKey;
		
	}

	/* (non-Javadoc)
	 * @see org.transitclock.core.dataCache.TripDataHistoryCacheInterface#populateCacheFromDb(org.hibernate.Session, java.util.Date, java.util.Date)
	 */
	
	@Override
	public void populateCacheFromDb(List<ArrivalDeparture> results)
	{

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
	public IpcArrivalDeparture findPreviousDepartureEvent(List<IpcArrivalDeparture> arrivalDepartures, IpcArrivalDeparture current)
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}		
		return null;		
	}
	private static <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
		return iterable == null ? Collections.<T> emptyList() : iterable;
	}
}
