package org.transitclock.core.dataCache.jcs.frequency;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.DoubleConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.LongConfigValue;
import org.transitclock.core.Indices;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheFactory;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheKey;
import org.transitclock.core.dataCache.StopPathCacheKey;
import org.transitclock.core.dataCache.frequency.FrequencyBasedHistoricalAverageCache;
import org.transitclock.core.predictiongenerator.scheduled.dwell.rls.TransitClockRLS;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Block;
import org.transitclock.db.structs.Headway;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.utils.Time;

public class DwellTimeModelCache implements org.transitclock.core.dataCache.DwellTimeModelCacheInterface {

	final private static String cacheName = "DwellTimeModelCache";

	private static IntegerConfigValue maxDwellTimeAllowedInModel = new IntegerConfigValue("org.transitclock.core.dataCache.jcs.maxDwellTimeAllowedInModel", 2 * Time.MS_PER_MIN, "Max dwell time to be considered in dwell RLS algotithm.");
	private static LongConfigValue maxHeadwayAllowedInModel = new LongConfigValue("org.transitclock.core.dataCache.jcs.maxHeadwayAllowedInModel", 1*Time.MS_PER_HOUR, "Max headway to be considered in dwell RLS algotithm.");
	
	private static DoubleConfigValue lambda = new DoubleConfigValue("org.transitclock.core.dataCache.jcs.lambda", 0.75, "This sets the rate at which the RLS algorithm forgets old values. Value are between 0 and 1. With 0 being the most forgetful.");
	
	private CacheAccess<StopPathCacheKey, TransitClockRLS>  cache = null;
	
	private static final Logger logger = LoggerFactory.getLogger(DwellTimeModelCache.class);
	
	public DwellTimeModelCache() {
		cache = JCS.getInstance(cacheName);			
	}
	@Override
	synchronized public void addSample(ArrivalDeparture event, Headway headway, long dwellTime) {
						
		Integer time=FrequencyBasedHistoricalAverageCache.secondsFromMidnight(event.getFreqStartTime(),2);
		
		time=FrequencyBasedHistoricalAverageCache.round(time, FrequencyBasedHistoricalAverageCache.getCacheIncrementsForFrequencyService());
		
		StopPathCacheKey key=new StopPathCacheKey(event.getTripId(), event.getStopPathIndex(), false, new Long(time));		
						
		TransitClockRLS rls = null;
		if(cache.get(key)!=null)
		{			
			rls=cache.get(key);
			
			double[] x = new double[1];
			x[0]=headway.getHeadway();
			
			double y = Math.log10(dwellTime);
			
								
			double[] arg0 = new double[1];
			arg0[0]=headway.getHeadway();
			if(rls.getRls()!=null)
			{
				double prediction = Math.pow(10,rls.getRls().predict(arg0));
				
				logger.debug("Predicted dwell: "+prediction + " for: "+key + " based on headway: "+TimeUnit.MILLISECONDS.toMinutes((long) headway.getHeadway())+" mins");
				
				logger.debug("Actual dwell: "+ dwellTime + " for: "+key + " based on headway: "+TimeUnit.MILLISECONDS.toMinutes((long) headway.getHeadway())+" mins");
			}
			
			rls.addSample(headway.getHeadway(), Math.log10(dwellTime));			
			if(rls.getRls()!=null)
			{
				double prediction = Math.pow(10,rls.getRls().predict(arg0));
	
				logger.debug("Predicted dwell after: "+ prediction + " for: "+key+ " with samples: "+rls.numSamples());
			}
		}else
		{			
						
			rls=new TransitClockRLS(lambda.getValue());
			rls.addSample(headway.getHeadway(), Math.log10(dwellTime));
		}
		cache.put(key,rls);
	}

	@Override
	public void addSample(ArrivalDeparture departure) {
		try {
			if(departure!=null && !departure.isArrival())
			{
				Block block=null;
				if(departure.getBlock()==null)
				{
					DbConfig dbConfig = Core.getInstance().getDbConfig();
					block=dbConfig.getBlock(departure.getServiceId(), departure.getBlockId());								
				}else
				{
					block=departure.getBlock();
				}
				
				Indices indices = new Indices(departure);
				StopArrivalDepartureCacheKey key= new StopArrivalDepartureCacheKey(departure.getStopId(), departure.getDate());
				List<IpcArrivalDeparture> stopData = StopArrivalDepartureCacheFactory.getInstance().getStopHistory(key);
				
				if(stopData!=null && stopData.size()>1)
				{
					IpcArrivalDeparture arrival=findArrival(stopData, new IpcArrivalDeparture(departure));
					if(arrival!=null)
					{
						IpcArrivalDeparture previousArrival=findPreviousArrival(stopData, arrival);
						if(arrival!=null&&previousArrival!=null)
						{			
							Headway headway=new Headway();
							headway.setHeadway(arrival.getTime().getTime()-previousArrival.getTime().getTime());
							long dwelltime=departure.getTime()-arrival.getTime().getTime();
							headway.setTripId(arrival.getTripId());
							
							// Negative dwell times are errors in data so do not include.
							// TODO not sure if it should ignore zero values.
							if(dwelltime>=0)
							{						
								/* Leave out silly values as they are most likely errors or unusual circumstance. */ 
								if(dwelltime<maxDwellTimeAllowedInModel.getValue() && 
										headway.getHeadway() < maxHeadwayAllowedInModel.getValue())
								{
									addSample(departure, headway,dwelltime);
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	private IpcArrivalDeparture findPreviousArrival(List<IpcArrivalDeparture> stopData, IpcArrivalDeparture arrival) {		
		for(IpcArrivalDeparture event:stopData)
		{
			if(event.isArrival())
			{
				if(!event.getVehicleId().equals(arrival.getVehicleId()))
				{
					if(!event.getTripId().equals(arrival.getTripId()))							
					{
						if(event.getStopId().equals(arrival.getStopId()))
						{
							if(event.getTime().getTime()<arrival.getTime().getTime())
								return event;
						}
					}
					// If trip id the same check that not the same trip by checking the freqStartTime 
					if(event.getTripId().equals(arrival.getTripId()))
					{
						if(event.getFreqStartTime()!=null && arrival.getFreqStartTime()!=null)
						{
							if(!event.getFreqStartTime().equals(arrival.getFreqStartTime()))
							{								
								if(event.getStopId().equals(arrival.getStopId()))
								{
									if(event.getTime().getTime()<arrival.getTime().getTime())
										return event;
								}							
							}
						}												
					}
					
				}
			}
		}

		return null;
	}
	private IpcArrivalDeparture findArrival(List<IpcArrivalDeparture> stopData, IpcArrivalDeparture departure) {

		for(IpcArrivalDeparture event:stopData)
		{
			if(event.isArrival())
			{
				if(event.getStopId().equals(departure.getStopId()))
				{
					if(event.getVehicleId().equals(departure.getVehicleId()))
					{
						if(event.getTripId().equals(departure.getTripId()))
						{
							return event;
						}
					}
				}
			}
		}
		return null;
	}
	@Override
	public Long predictDwellTime(StopPathCacheKey cacheKey, Headway headway) {		
		
		TransitClockRLS rls=cache.get(cacheKey);
		if(rls!=null&&rls.getRls()!=null)
		{
			double[] arg0 = new double[1];
			arg0[0]=headway.getHeadway();
			rls.getRls().predict(arg0);
			long prediction = (long) Math.pow(10, rls.getRls().predict(arg0));
			
			// If silly values returned then need to reset model and allow it use the super prediction.
			if(prediction>maxDwellTimeAllowedInModel.getValue())
			{				
				cache.remove(cacheKey);
				return null;
			}	
			return prediction; 
		}else
		{
			return null;
		}
	}

	@Override
	public void populateCacheFromDb(List<ArrivalDeparture> results) {
		synchronized (cache) {
			for (ArrivalDeparture result : results) {
				addSample(result);
			}
		}
	}

	public static void main(String[] args)
	{
		 double startvalue=1000;
		 double result1 = Math.log10(startvalue);
		 double result2 = Math.pow(10, result1);
		 if(startvalue==result2)
			 System.out.println("As expected they are the same.");		 		 		
	}

}
