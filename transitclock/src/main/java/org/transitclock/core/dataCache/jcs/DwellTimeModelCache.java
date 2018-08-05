package org.transitclock.core.dataCache.jcs;

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
import org.transitclock.config.StringConfigValue;
import org.transitclock.core.HeadwayDetails;
import org.transitclock.core.Indices;
import org.transitclock.core.TravelTimes;
import org.transitclock.core.dataCache.DwellTimeCacheKey;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheFactory;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheKey;
import org.transitclock.core.predictiongenerator.rls.dwell.DwellTimePredictionGeneratorImpl;
import org.transitclock.core.predictiongenerator.rls.dwell.TransitClockRLS;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Block;
import org.transitclock.db.structs.Headway;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.utils.Time;

import smile.regression.RLS;

public class DwellTimeModelCache implements org.transitclock.core.dataCache.DwellTimeModelCacheInterface {

	final private static String cacheName = "DwellTimeModelCache";

	private static IntegerConfigValue maxDwellTimeAllowedInModel = new IntegerConfigValue("org.transitclock.core.dataCache.jcs.maxDwellTimeAllowedInModel", 120000, "Max dwell time to be considered in dwell RLS algotithm.");
	private static LongConfigValue maxHeadwayAllowedInModel = new LongConfigValue("org.transitclock.core.dataCache.jcs.maxHeadwayAllowedInModel", 1*Time.MS_PER_HOUR, "Max dwell time to be considered in dwell RLS algotithm.");
	
	private static DoubleConfigValue lambda = new DoubleConfigValue("org.transitclock.core.dataCache.jcs.lambda", 0.5, "This sets the rate at which the RLS algorithm forgets old values. Value are between 0 and 1. With 0 being the most forgetful.");
	
	private CacheAccess<DwellTimeCacheKey, TransitClockRLS>  cache = null;
	
	private static final Logger logger = LoggerFactory.getLogger(DwellTimeModelCache.class);
	
	public DwellTimeModelCache() {
		cache = JCS.getInstance(cacheName);			
	}
	@Override
	synchronized public void addSample(Indices indices, Headway headway, long dwellTime) {
		
		DwellTimeCacheKey key=new DwellTimeCacheKey(headway.getTripId(), indices.getStopPathIndex());
		
		
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
				double prediction = rls.getRls().predict(arg0);
				logger.debug("Predicted dwell: "+prediction + " for: "+key + " based on headway: "+TimeUnit.MILLISECONDS.toMinutes((long) headway.getHeadway())+" mins");
				
				logger.debug("Actual dwell: "+ dwellTime + " for: "+key + " based on headway: "+TimeUnit.MILLISECONDS.toMinutes((long) headway.getHeadway())+" mins");
			}
			
			rls.addSample(headway.getHeadway(), Math.log10(dwellTime));			
			if(rls.getRls()!=null)
			{
				double prediction = rls.getRls().predict(arg0);
	
				logger.debug("Predicted dwell after: "+ prediction + " for: "+key+ " with samples: "+rls.numSamples());
			}
		}else
		{			
						
			rls=new TransitClockRLS(lambda.getValue());
			rls.addSample(headway.getHeadway(), dwellTime);
		}
		cache.put(key,rls);
	}

	@Override
	public void addSample(ArrivalDeparture departure) {
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
			List<ArrivalDeparture> stopData = StopArrivalDepartureCacheFactory.getInstance().getStopHistory(key);
			
			if(stopData!=null && stopData.size()>1)
			{
				ArrivalDeparture arrival=findArrival(stopData, departure);
				if(arrival!=null)
				{
					ArrivalDeparture previousArrival=findPreviousArrival(stopData, arrival);
					if(arrival!=null&&previousArrival!=null)
					{			
						Headway headway=new Headway();
						headway.setHeadway(arrival.getTime()-previousArrival.getTime());
						long dwelltime=departure.getTime()-arrival.getTime();
						headway.setTripId(arrival.getTripId());
						
						/* Leave out silly values as they are most likely errors or unusual circumstance. */ 
						if(dwelltime<maxDwellTimeAllowedInModel.getValue() && 
								headway.getHeadway() < maxHeadwayAllowedInModel.getValue())
						{
							addSample(indices, headway,dwelltime);
						}
					}
				}
			}
		}		
	}

	private ArrivalDeparture findPreviousArrival(List<ArrivalDeparture> stopData, ArrivalDeparture arrival) {		
		for(ArrivalDeparture event:stopData)
		{
			if(event.isArrival())
			{
				if(!event.getVehicleId().equals(arrival.getVehicleId()))
				{
					if(!event.getTripId().equals(arrival.getTripId()))
					{
						if(event.getStopId().equals(arrival.getStopId()))
						{
							if(event.getTime()<arrival.getTime())
								return event;
						}
					}
				}
			}
		}

		return null;
	}
	private ArrivalDeparture findArrival(List<ArrivalDeparture> stopData, ArrivalDeparture departure) {

		for(ArrivalDeparture event:stopData)
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
	public Long predictDwellTime(Indices indices, Headway headway) {
		
		DwellTimeCacheKey key=new DwellTimeCacheKey(indices);
		TransitClockRLS rls=cache.get(key);
		if(rls!=null&&rls.getRls()!=null)
		{
			double[] arg0 = new double[1];
			arg0[0]=headway.getHeadway();
			rls.getRls().predict(arg0);
			return (long) Math.pow(10, (int) rls.getRls().predict(arg0));
		}else
		{
			return null;
		}
	}

}
