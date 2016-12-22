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
					"This is the intervals size of the day that the avaerge is applied to. ");
	
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
			
	synchronized public HistoricalAverage getAverage(StopPathCacheKey key) {
		
		TreeMap<Long, HistoricalAverage> result = m.get(new StopPathKey(key));
		if(result!=null)
		{
			if(key.getStartTime()!=null)
			{
				SortedMap<Long, HistoricalAverage> subresult = result.subMap(key.getStartTime(), key.getStartTime()+getCacheIncrementsForFrequencyService());
				
				if(subresult.size()==1)
				{
					return subresult.get(subresult.lastKey());
				}
			}
		}
		return null;
	}
	synchronized private void putAverage(StopPathCacheKey key, HistoricalAverage average) {
			
		
		TreeMap<Long, HistoricalAverage> result = m.get(new StopPathKey(key));
		
		logger.debug("Putting: "+new StopPathKey(key).toString()+" in FrequencyBasedHistoricalAverageCache with values : "+average);
		
		if(result==null)
		{			
			m.put(new StopPathKey(key), new TreeMap<Long, HistoricalAverage> ());			
		}
		m.get(new StopPathKey(key)).put(key.getStartTime(), average);													
	}
	synchronized public void putArrivalDeparture(ArrivalDeparture arrivalDeparture) 
	{
		logger.debug("Putting :"+arrivalDeparture.toString() + " in FrequencyBasedHistoricalAverageCache cache.");
						
		DbConfig dbConfig = Core.getInstance().getDbConfig();
				
		Trip trip=dbConfig.getTrip(arrivalDeparture.getTripId());
						
		VehicleState vehicleState = VehicleStateManager.getInstance().getVehicleState(arrivalDeparture.getVehicleId());
						
		if(vehicleState!=null && vehicleState.getTripStartEvent()!=null)
		{
			Integer time=secondsFromMidnight(vehicleState.getTripStartEvent().getDate());
			
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
	private class StopPathKey
	{
		@Override
		public String toString() {
			return "StopPathKey [tripId=" + tripId + ", stopPathIndex=" + stopPathIndex + "]";
		}

		String tripId;
		Integer stopPathIndex;
		
		
		public StopPathKey(StopPathCacheKey stopPathCacheKey)
		{
			this.tripId= stopPathCacheKey.getTripId();
			this.stopPathIndex = stopPathCacheKey.getStopPathIndex();
		}

		private FrequencyBasedHistoricalAverageCache getOuterType() {
			return FrequencyBasedHistoricalAverageCache.this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((stopPathIndex == null) ? 0 : stopPathIndex.hashCode());
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
			if (tripId == null) {
				if (other.tripId != null)
					return false;
			} else if (!tripId.equals(other.tripId))
				return false;
			return true;
		}
		
	}
}
