package org.transitclock.core.dataCache.frequency;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

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
import org.transitclock.applications.Core;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.core.Indices;
import org.transitclock.core.VehicleState;
import org.transitclock.core.dataCache.*;
import org.transitclock.core.dataCache.ehcache.TripDataHistoryCache;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Trip;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.gtfs.GtfsData;
/**
 * @author Sean Óg Crudden
 * This class is to hold the historical average for frequency based services. It puts them in buckets that represent increments of time. The start time of the trip is used to decide which 
 * bucket to apply the data to or which average to retrieve.  
 */
public class FrequencyBasedHistoricalAverageCache {	
	
	private static FrequencyBasedHistoricalAverageCache singleton = new FrequencyBasedHistoricalAverageCache();
	
	private static final Logger logger = LoggerFactory
			.getLogger(FrequencyBasedHistoricalAverageCache.class);
			
	
	
	private static IntegerConfigValue minTravelTimeFilterValue= new IntegerConfigValue(	
			"transitclock.core.frequency.minTravelTimeFilterValue",
			0,
			"The value to be included in average calculation for Travel times must exceed this value.");
	
	private static IntegerConfigValue maxTravelTimeFilterValue = new IntegerConfigValue(	
			"transitclock.core.frequency.maxTravelTimeFilterValue",
			600000,
			"The value to be included in average calculation for Travel times must be less than this value.");
	
	private static IntegerConfigValue minDwellTimeFilterValue = new IntegerConfigValue(	
			"transitclock.core.frequency.minDwellTimeFilterValue",
			0,
			"The value to be included in average calculation for dwell time must exceed this value.");
			
	private static IntegerConfigValue maxDwellTimeFilterValue = new IntegerConfigValue(	
			"transitclock.core.frequency.maxDwellTimeFilterValue",
			600000,
			"The value to be included in average calculation for dwell time must be less this value.");
	
	private static IntegerConfigValue cacheIncrementsForFrequencyService = 
			new IntegerConfigValue(	
					"transitclock.core.frequency.cacheIncrementsForFrequencyService",
					180*60,
					"This is the intervals size of the day that the average is applied to. ");
	
	public static int getCacheIncrementsForFrequencyService() {
		return cacheIncrementsForFrequencyService.getValue();
	}
	
	
	private final ConcurrentHashMap<StopPathKey, TreeMap<Long, HistoricalAverage>> m = new ConcurrentHashMap<StopPathKey,TreeMap<Long, HistoricalAverage>>();
		
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
		
		String totalsString=new String();
		for(StopPathKey key:m.keySet())
		{
			TreeMap<Long, HistoricalAverage> values=new TreeMap<Long, HistoricalAverage>();
			
			TreeMap<Long, HistoricalAverage> map = m.get(key);
			Set<Long> times = map.keySet();
			for(Long time:times)
			{
				values.put(time, map.get(time));
				
				totalsString=totalsString+"\n"+key.tripId+","+key.stopPathIndex+","+key.travelTime+","+time+","+map.get(time).getCount()+","+map.get(time).getAverage();								
			}	
		
		}
		
		return totalsString+"\nDetails\n"+m.toString();
	}
	synchronized public HistoricalAverage getAverage(StopPathCacheKey key) {
		
		logger.debug("Looking for average for : {} in FrequencyBasedHistoricalAverageCache cache.", key);
		TreeMap<Long, HistoricalAverage> result = m.get(new StopPathKey(key));
		if(result!=null)
		{
			logger.debug("Found average buckets for {}. ", key);
			if(key.getStartTime()!=null)
			{
				SortedMap<Long, HistoricalAverage> subresult = result.subMap(key.getStartTime(), key.getStartTime()+getCacheIncrementsForFrequencyService());
				
				if(subresult.size()==1)
				{
					logger.debug("Found average for : {} in FrequencyBasedHistoricalAverageCache cache with a value : {}", key, subresult.get(subresult.lastKey()));
					return subresult.get(subresult.lastKey());					
				}else
				{
					logger.debug("No historical data within time range ({} to {}) for this trip {} in FrequencyBasedHistoricalAverageCache cache.",key.getStartTime(), key.getStartTime()+getCacheIncrementsForFrequencyService(), key);
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
		//logger.debug("Putting: {} for start time: {} in FrequencyBasedHistoricalAverageCache with value : {}",new StopPathKey(key), key.getStartTime(), average);
		m.get(new StopPathKey(key)).put(key.getStartTime(), average);													
	}
	synchronized public void putArrivalDeparture(ArrivalDeparture arrivalDeparture) 
	{		
		DbConfig dbConfig = Core.getInstance().getDbConfig();
				
		Trip trip=dbConfig.getTrip(arrivalDeparture.getTripId());
					
		if(trip!=null && trip.isNoSchedule())		
		{												
			Integer time=secondsFromMidnight(arrivalDeparture.getDate(), 2);
			
			/* this is what puts the trip into the buckets (time slots) */
			time=round(time, getCacheIncrementsForFrequencyService());
							
			TravelTimeResult pathDuration=getLastPathDuration(arrivalDeparture, trip);
							
			if(pathDuration!=null && pathDuration.getDuration() > minTravelTimeFilterValue.getValue() && pathDuration.getDuration() < maxTravelTimeFilterValue.getValue())
			{		
				if(trip.isNoSchedule())
				{							
					StopPathCacheKey historicalAverageCacheKey=new StopPathCacheKey(trip.getId(), pathDuration.getArrival().getStopPathIndex(), true,new Long(time));
					
					HistoricalAverage average = FrequencyBasedHistoricalAverageCache.getInstance().getAverage(historicalAverageCacheKey);
					
					if(average==null)				
						average=new HistoricalAverage();
					
					average.update(pathDuration.getDuration());				
					
					logger.debug("Putting : {} in FrequencyBasedHistoricalAverageCache cache for key : {} which results in : {}.", pathDuration, historicalAverageCacheKey,average);
					
					FrequencyBasedHistoricalAverageCache.getInstance().putAverage(historicalAverageCacheKey, average);
				}
			}				
			DwellTimeResult stopDuration=getLastStopDuration(arrivalDeparture, trip);
			if(stopDuration!=null && stopDuration.getDuration() > minDwellTimeFilterValue.getValue() && stopDuration.getDuration() < maxDwellTimeFilterValue.getValue())
			{
				StopPathCacheKey historicalAverageCacheKey=new StopPathCacheKey(trip.getId(), stopDuration.getDeparture().getStopPathIndex(), false, new Long(time));
				
				HistoricalAverage average = FrequencyBasedHistoricalAverageCache.getInstance().getAverage(historicalAverageCacheKey);
				
				if(average==null)				
					average=new HistoricalAverage();
				
				average.update(stopDuration.getDuration());
				
				logger.debug("Putting : {} in FrequencyBasedHistoricalAverageCache cache for key : {} which results in : {}.", stopDuration, historicalAverageCacheKey, average);
				
				FrequencyBasedHistoricalAverageCache.getInstance().putAverage(historicalAverageCacheKey, average);
			}	
			if(stopDuration==null && pathDuration==null)
			{
				logger.debug("Cannot add to FrequencyBasedHistoricalAverageCache as cannot calculate stopDuration or pathDuration. : {}", arrivalDeparture);
			}
			if(pathDuration!=null && (pathDuration.getDuration() < minTravelTimeFilterValue.getValue() || pathDuration.getDuration() > maxTravelTimeFilterValue.getValue()))
			{
				logger.debug("Cannot add to FrequencyBasedHistoricalAverageCache as pathDuration: {} is outside parameters. : {}",pathDuration, arrivalDeparture);
			}
			if(stopDuration!=null && (stopDuration.getDuration() < minDwellTimeFilterValue.getValue() || stopDuration.getDuration() > maxDwellTimeFilterValue.getValue()))
			{
				logger.debug("Cannot add to FrequencyBasedHistoricalAverageCache as stopDuration: {} is outside parameters. : {}",stopDuration, arrivalDeparture);
			}
		}else
		{
			logger.debug("Cannot add to FrequencyBasedHistoricalAverageCache as no start time set : {}", arrivalDeparture);
		}
	}
	public ArrivalDeparture findPreviousDepartureEvent(List<ArrivalDeparture> arrivalDepartures,ArrivalDeparture current)
	{		
		Collections.sort(arrivalDepartures, new ArrivalDepartureComparator());
		for (ArrivalDeparture tocheck : emptyIfNull(arrivalDepartures)) 
		{
			if(current.getFreqStartTime()!=null && tocheck.getFreqStartTime()!=null&&tocheck.getFreqStartTime().equals(current.getFreqStartTime()))
			{								
				if(tocheck.getStopPathIndex()==(current.getStopPathIndex()-1) && (current.isArrival() && tocheck.isDeparture()) && current.getVehicleId().equals(tocheck.getVehicleId()))
				{
					return tocheck;					
				}
			}
		}		
		return null;		
	}
	public ArrivalDeparture findPreviousArrivalEvent(List<ArrivalDeparture> arrivalDepartures,ArrivalDeparture current)
	{
		Collections.sort(arrivalDepartures, new ArrivalDepartureComparator());
		for (ArrivalDeparture tocheck : emptyIfNull(arrivalDepartures)) 
		{
			if(current.getFreqStartTime()!=null && tocheck.getFreqStartTime()!=null&&tocheck.getFreqStartTime().equals(current.getFreqStartTime()))
			{
				if(tocheck.getStopId().equals(current.getStopId()) && (current.isDeparture() && tocheck.isArrival()) && current.getVehicleId().equals(tocheck.getVehicleId()))
				{
					return tocheck;
				}
			}
		}
		
		return null;
	}
	private TravelTimeResult getLastPathDuration(ArrivalDeparture arrivalDeparture, Trip trip)
	{
		Date nearestDay = DateUtils.truncate(new Date(arrivalDeparture.getTime()), Calendar.DAY_OF_MONTH);
		TripKey tripKey = new TripKey(arrivalDeparture.getTripId(),
				nearestDay,
				trip.getStartTime());
						
		List<ArrivalDeparture> arrivalDepartures=(List<ArrivalDeparture>) TripDataHistoryCacheFactory.getInstance().getTripHistory(tripKey);
		
		if(arrivalDepartures!=null && arrivalDepartures.size()>0 && arrivalDeparture.isArrival())
		{			
			ArrivalDeparture previousEvent = findPreviousDepartureEvent(arrivalDepartures, arrivalDeparture);
			
			if(previousEvent!=null && arrivalDeparture!=null && previousEvent.isDeparture())
			{
				TravelTimeResult travelTimeResult=new TravelTimeResult(previousEvent,arrivalDeparture );
				return travelTimeResult;
			}
				
		}
					
		return null;
	}
	private DwellTimeResult getLastStopDuration(ArrivalDeparture arrivalDeparture, Trip trip)
	{
		Date nearestDay = DateUtils.truncate(new Date(arrivalDeparture.getTime()), Calendar.DAY_OF_MONTH);
		TripKey tripKey = new TripKey(arrivalDeparture.getTripId(), 
				nearestDay,
				trip.getStartTime());
						
		List<ArrivalDeparture> arrivalDepartures=(List<ArrivalDeparture>) TripDataHistoryCacheFactory.getInstance().getTripHistory(tripKey);
		
		if(arrivalDepartures!=null && arrivalDepartures.size()>0 && arrivalDeparture.isDeparture())
		{			
			ArrivalDeparture previousEvent = findPreviousArrivalEvent(arrivalDepartures, arrivalDeparture);
			
			if(previousEvent!=null && arrivalDeparture!=null && previousEvent.isArrival())
			{
					DwellTimeResult dwellTimeResult=new DwellTimeResult(previousEvent, arrivalDeparture);
					return dwellTimeResult;
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
			// TODO this might be better done in the database.
			if(GtfsData.routeNotFiltered(result.getRouteId()))
			{
				FrequencyBasedHistoricalAverageCache.getInstance().putArrivalDeparture(result);
			}
		}		
	}
	public static int round(double i, int v){
	    return (int) (Math.floor(i/v) * v);
	}
	public static int secondsFromMidnight(Date date, int startHour)
	{
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(date.getTime());		
		long now = c.getTimeInMillis();
		c.set(Calendar.HOUR_OF_DAY, startHour);
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
	private class TravelTimeResult {
		@Override
		public String toString() {
			return "TravelTimeResult [departure=" + departure + ", arrival=" + arrival + ", duration="+getDuration()+"]";
		}

		public TravelTimeResult(ArrivalDeparture departure, ArrivalDeparture arrival) {
			super();
			this.arrival = arrival;
			this.departure = departure;
		}

		public ArrivalDeparture getArrival() {
			return arrival;
		}
		
		public ArrivalDeparture getDeparture() {
			return departure;
		}
		public double getDuration()
		{
			return Math.abs(arrival.getTime()-departure.getTime());
		}
		private ArrivalDeparture arrival=null;
		private ArrivalDeparture departure=null;
				
	}
	private class DwellTimeResult {		
		@Override
		public String toString() {
			return "DwellTimeResult [arrival=" + arrival + ", departure=" + departure + ", duration="+getDuration()+"]";
		}

		public DwellTimeResult(ArrivalDeparture arrival, ArrivalDeparture departure) {
			super();
			this.arrival = arrival;
			this.departure = departure;
		}

		public ArrivalDeparture getArrival() {
			return arrival;
		}
		
		public ArrivalDeparture getDeparture() {
			return departure;
		}
		public double getDuration()
		{
			return Math.abs(departure.getTime()-arrival.getTime());
		}
		private ArrivalDeparture arrival=null;
		private ArrivalDeparture departure=null;
	}
}
