package org.transitclock.core.dataCache.jcs;

import java.util.List;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.core.HeadwayDetails;
import org.transitclock.core.Indices;
import org.transitclock.core.TravelTimes;
import org.transitclock.core.dataCache.DwellTimeCacheKey;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheFactory;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheKey;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Headway;

import smile.regression.RLS;

public class DwellTimeModelCache implements org.transitclock.core.dataCache.DwellTimeModelCacheInterface {

	final private static String cacheName = "DwellTimeModelCache";

	private static IntegerConfigValue maxDwellTimeAllowedInModel = new IntegerConfigValue("maxDwellTimeAllowedInModel", 120000, "Max dwell time to be considered in dwell RLS algotithm."); 
	
	private CacheAccess<DwellTimeCacheKey, RLS>  cache = null;
	
	public DwellTimeModelCache() {
		cache = JCS.getInstance(cacheName);	
		
	}
	@Override
	synchronized public void addSample(Indices indices, Headway headway, long dwellTime) {
		
		DwellTimeCacheKey key=new DwellTimeCacheKey(indices);
		
		RLS rls = null;
		if(cache.get(key)!=null)
		{			
			rls=cache.get(key);
			
			double[] x = new double[1];
			x[0]=headway.getHeadway();
			
			double y = dwellTime;
			rls.learn(x, y);			
		}else
		{			
			
			double samplex[][]=new double[2][1];
			double sampley[]=new double[2];
			
			/* TODO We will use the sample and the (scheduled headway,scheduled dwell time) to start with instead of (0, 0) and sample..*/
			samplex[0][0]=0;
			sampley[0]=0;						
						
			samplex[1][0]=headway.getHeadway();
			sampley[1]=dwellTime;						
									
			rls=new RLS(samplex, sampley);
		}
		cache.put(key,rls);
	}

	@Override
	public void addSample(ArrivalDeparture departure) {
		if(departure!=null && !departure.isArrival())
		{
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
						
						if(dwelltime<maxDwellTimeAllowedInModel.getValue())
							addSample(indices, headway,dwelltime);
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
						if(event.getTime()<arrival.getTime())
							return event;
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
				if(event.getVehicleId().equals(departure.getVehicleId()))
				{
					if(event.getTripId().equals(departure.getTripId()))
					{
						return event;
					}
				}
			}
		}
		return null;
	}
	@Override
	public Long predictDwellTime(Indices indices, HeadwayDetails headway) {
		
		DwellTimeCacheKey key=new DwellTimeCacheKey(indices);
		RLS rls=cache.get(key);
		if(rls!=null)
		{
			double[] arg0 = new double[1];
			arg0[0]=headway.getHeadway();
			rls.predict(arg0);
			return new Long((long) rls.predict(arg0));
		}else
		{
			return null;
		}
	}

}
