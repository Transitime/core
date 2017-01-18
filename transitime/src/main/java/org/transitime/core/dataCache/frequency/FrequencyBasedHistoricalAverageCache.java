package org.transitime.core.dataCache.frequency;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.config.IntegerConfigValue;
import org.transitime.db.structs.ArrivalDeparture;
import org.transitime.db.structs.Trip;
import org.transitime.gtfs.DbConfig;
import org.transitime.core.VehicleState;
import org.transitime.core.dataCache.*;
/**
 * @author Sean Óg Crudden
 * This class is to hold the historical average for frequency based services. It puts them in buckets that represent increments of time. The start time of the trip is used to decide which 
 * bucket to apply the data to or which average to retrieve.  
 */
public class FrequencyBasedHistoricalAverageCache {	
	
	private static FrequencyBasedHistoricalAverageCache singleton = new FrequencyBasedHistoricalAverageCache();
	
	private static final Logger logger = LoggerFactory
			.getLogger(FrequencyBasedHistoricalAverageCache.class);
			
	private static int getCacheIncrementsForFrequencyService() {
		return cacheIncrementsForFrequencyService.getValue();
	}
	private static IntegerConfigValue cacheIncrementsForFrequencyService = 
			new IntegerConfigValue(	
					"transitime.core.frequency.cacheIncrementsForFrequencyService",
					30,
					"This is the intervals size of the day that the average is applied to. ");
	
	private HashMap<StopPathKey, TreeMap<Long, HistoricalAverage>> m = new HashMap<StopPathKey,TreeMap<Long, HistoricalAverage>>();
		
	/**
	 * Gets the singleton instance of this class.
	 * 
	 * @return
	 */
	public static FrequencyBasedHistoricalAverageCache getInstance() {
		return singleton;
	}
	
	private FrequencyBasedHistoricalAverageCache() {													
	}
	public String toString()
	{
		return m.toString();
	}
	synchronized public HistoricalAverage getAverage(StopPathCacheKey key) {
		
		logger.debug("Looking for average for : {} in FrequencyBasedHistoricalAverageCache cache.", key);
		TreeMap<Long, HistoricalAverage> result = m.get(new StopPathKey(key));
		if(result!=null)
		{
			if(key.getStartTime()!=null)
			{
				SortedMap<Long, HistoricalAverage> subresult = result.subMap(key.getStartTime(), key.getStartTime()+getCacheIncrementsForFrequencyService());
				
				if(subresult.size()==1)
				{
					logger.debug("Found average for : {} in FrequencyBasedHistoricalAverageCache cache with a value : {}", subresult.get(subresult.lastKey()));
					return subresult.get(subresult.lastKey());					
				}
			}
		}
		logger.debug("No average found in FrequencyBasedHistoricalAverageCache cache for : {}",key);
		return null;
	}
	synchronized private void putAverage(StopPathCacheKey key, HistoricalAverage average) {
			
		
		TreeMap<Long, HistoricalAverage> result = m.get(new StopPathKey(key));
						
		if(result==null)
		{			
			m.put(new StopPathKey(key), new TreeMap<Long, HistoricalAverage> ());			
		}
		
		logger.debug("Putting: {} for start time: {} in FrequencyBasedHistoricalAverageCache with value : {}",new StopPathKey(key), new Date(key.getStartTime()), average);
		m.get(new StopPathKey(key)).put(key.getStartTime(), average);													
	}
	synchronized public void putArrivalDeparture(ArrivalDeparture arrivalDeparture) 
	{		
		DbConfig dbConfig = Core.getInstance().getDbConfig();
				
		Trip trip=dbConfig.getTrip(arrivalDeparture.getTripId());
		
		if(arrivalDeparture.getFreqStartTime()!=null && trip.isNoSchedule())		
		{			
			logger.debug("Putting : {} in FrequencyBasedHistoricalAverageCache cache.", arrivalDeparture);
						
			Integer time=secondsFromMidnight(arrivalDeparture.getFreqStartTime());
			
			/* this is what puts the trip into the buckets (time slots) */
			time=round(time, getCacheIncrementsForFrequencyService());
							
			double pathDuration=getLastPathDuration(arrivalDeparture, trip);
							
			if(pathDuration>0)
			{		
				if(trip.isNoSchedule())
				{							
					StopPathCacheKey historicalAverageCacheKey=new StopPathCacheKey(trip.getId(), arrivalDeparture.getStopPathIndex(), true,new Long(time));
					
					HistoricalAverage average = FrequencyBasedHistoricalAverageCache.getInstance().getAverage(historicalAverageCacheKey);
					
					if(average==null)				
						average=new HistoricalAverage();
					
					average.update(pathDuration);
					
					FrequencyBasedHistoricalAverageCache.getInstance().putAverage(historicalAverageCacheKey, average);
				}
			}				
			double stopDuration=getLastStopDuration(arrivalDeparture, trip);
			if(stopDuration>0)
			{
				StopPathCacheKey historicalAverageCacheKey=new StopPathCacheKey(trip.getId(), arrivalDeparture.getStopPathIndex(), false, new Long(time));
				
				HistoricalAverage average = FrequencyBasedHistoricalAverageCache.getInstance().getAverage(historicalAverageCacheKey);
				
				if(average==null)				
					average=new HistoricalAverage();
				
				average.update(stopDuration);
			
				FrequencyBasedHistoricalAverageCache.getInstance().putAverage(historicalAverageCacheKey, average);
			}	
		}else
		{
			logger.debug("Cannot add to FrequencyBasedHistoricalAverageCache as no start time set : {}", arrivalDeparture);
		}
	}
	public ArrivalDeparture findPreviousDepartureEvent(List<ArrivalDeparture> arrivalDepartures,ArrivalDeparture current)
	{													
		for (ArrivalDeparture tocheck : emptyIfNull(arrivalDepartures)) 
		{
			if(current.getFreqStartTime()!=null && tocheck.getFreqStartTime()!=null && current.getFreqStartTime().equals(tocheck.getFreqStartTime()))
			{
				if(tocheck.getStopPathIndex()==(current.getStopPathIndex()-1) && (current.isArrival() && tocheck.isDeparture()))
				{
					return tocheck;
				}
			}
		}		
		return null;		
	}
	public ArrivalDeparture findPreviousArrivalEvent(List<ArrivalDeparture> arrivalDepartures,ArrivalDeparture current)
	{
		for (ArrivalDeparture tocheck : emptyIfNull(arrivalDepartures)) 
		{
			if(current.getFreqStartTime()!=null && tocheck.getFreqStartTime()!=null && current.getFreqStartTime().equals(tocheck.getFreqStartTime()))
			{
				if(tocheck.getStopPathIndex()==(current.getStopPathIndex()-1) && (current.isDeparture() && tocheck.isArrival()))
				{
					return tocheck;
				}
			}
		}
		return null;
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
			ArrivalDeparture previousEvent = findPreviousDepartureEvent(arrivalDepartures, arrivalDeparture);
			
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
			ArrivalDeparture previousEvent = findPreviousArrivalEvent(arrivalDepartures, arrivalDeparture);
			
			if(previousEvent!=null && arrivalDeparture!=null && previousEvent.isArrival())
					return Math.abs(previousEvent.getTime()-arrivalDeparture.getTime());
		}
		return -1;
	}
	public void populateCacheFromDb(Session session, Date startDate, Date endDate) 
	{
		Criteria criteria =session.createCriteria(ArrivalDeparture.class);				
		
		@SuppressWarnings("unchecked")
		List<ArrivalDeparture> results=criteria.add(Restrictions.between("time", startDate, endDate)).list();
		Collections.sort(results, new ArrivalDepartureComparator());
		for(ArrivalDeparture result : results)
		{
			FrequencyBasedHistoricalAverageCache.getInstance().putArrivalDeparture(result);			
		}		
	}
	private int round(double i, int v){
	    return (int) (Math.round(i/v) * v);
	}
	private int secondsFromMidnight(Date date)
	{
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(date.getTime());		
		long now = c.getTimeInMillis();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		long passed = now - c.getTimeInMillis();
		long secondsPassed = passed / 1000;
		
		return (int)secondsPassed;
	}
	private static <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
		return iterable == null ? Collections.<T> emptyList() : iterable;
	}
	private class StopPathKey
	{
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((stopPathIndex == null) ? 0 : stopPathIndex.hashCode());
			result = prime * result + (travelTime ? 1231 : 1237);
			result = prime * result + ((tripId == null) ? 0 : tripId.hashCode());
			return result;
		}



		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			StopPathKey other = (StopPathKey) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (stopPathIndex == null) {
				if (other.stopPathIndex != null)
					return false;
			} else if (!stopPathIndex.equals(other.stopPathIndex))
				return false;
			if (travelTime != other.travelTime)
				return false;
			if (tripId == null) {
				if (other.tripId != null)
					return false;
			} else if (!tripId.equals(other.tripId))
				return false;
			return true;
		}



		@Override
		public String toString() {
			return "StopPathKey [tripId=" + tripId + ", stopPathIndex=" + stopPathIndex + ", travelTime=" + travelTime
					+ "]";
		}

		String tripId;
		Integer stopPathIndex;
		
		private boolean travelTime=true;
		
		public boolean isTravelTime() {
			return travelTime;
		}
				
		public StopPathKey(StopPathCacheKey stopPathCacheKey)
		{
			this.tripId= stopPathCacheKey.getTripId();
			this.stopPathIndex = stopPathCacheKey.getStopPathIndex();
			this.travelTime = stopPathCacheKey.isTravelTime();
		}

		private FrequencyBasedHistoricalAverageCache getOuterType() {
			return FrequencyBasedHistoricalAverageCache.this;
		}

		
	}
}
