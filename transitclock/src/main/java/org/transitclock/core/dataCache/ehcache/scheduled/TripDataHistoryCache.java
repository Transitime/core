/**
 * 
 */
package org.transitclock.core.dataCache.ehcache.scheduled;

import java.util.Collections;
import java.util.Date;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.ehcache.Cache;
import org.ehcache.CacheManager;

import org.ehcache.Status;
import org.ehcache.config.builders.CacheManagerBuilder;

import org.ehcache.xml.XmlConfiguration;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.core.dataCache.IpcArrivalDepartureComparator;
import org.transitclock.core.dataCache.TripDataHistoryCacheFactory;
import org.transitclock.core.dataCache.TripDataHistoryCacheInterface;
import org.transitclock.core.dataCache.TripEvents;
import org.transitclock.core.dataCache.TripKey;
import org.transitclock.core.dataCache.ehcache.CacheManagerFactory;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Trip;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.gtfs.GtfsData;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.utils.Time;

/**
 * @author Sean Og Crudden 
 * 		   This is a Cache to hold historical arrival departure data for frequency based trips. It
 *         is intended to look up a trips historical data when a trip starts and
 *         place in cache for use in generating predictions based on a Kalman
 *         filter. 
 *         
 */
public class TripDataHistoryCache implements TripDataHistoryCacheInterface{
	private static TripDataHistoryCacheInterface singleton = new TripDataHistoryCache();
	
	private static boolean debug = false;

	final private static String cacheByTrip = "arrivalDeparturesByTrip";
	
	final URL xmlConfigUrl = getClass().getResource("/ehcache.xml");

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

		TripEvents result = (TripEvents) cache.get(tripKey);

		if(result!=null)
		{						
			return result.getEvents();
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
	synchronized public TripKey putArrivalDeparture(ArrivalDeparture arrivalDeparture) {
		
		logger.debug("Putting :"+arrivalDeparture.toString() + " in TripDataHistoryCache cache.");
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
			
			if(trip!=null)
			{
				
				tripKey = new TripKey(arrivalDeparture.getTripId(),
						nearestDay,
						trip.getStartTime());
		
				TripEvents result = (TripEvents) cache.get(tripKey);
		
				if (result == null ) {													
					result=new TripEvents();					
				}
				
				try {										
					result.addEvent(new IpcArrivalDeparture(arrivalDeparture));
					
				} catch (Exception e) {
					logger.error("Error adding "+arrivalDeparture.toString()+" event to TripDataHistoryCache.", e);				

				}									
							
				cache.put(tripKey, result);
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
				if(tocheck.getStopPathIndex()==(current.getStopPathIndex()-1) && (current.isArrival() && tocheck.isDeparture()))
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

	@Override
	public List<TripKey> getKeys() {
		// TODO Auto-generated method stub
		return null;
	}

}
