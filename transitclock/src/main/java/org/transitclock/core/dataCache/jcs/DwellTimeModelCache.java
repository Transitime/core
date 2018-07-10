package org.transitclock.core.dataCache.jcs;

import java.util.List;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.transitclock.core.HeadwayDetails;
import org.transitclock.core.Indices;
import org.transitclock.core.dataCache.DwellTimeCacheKey;
import org.transitclock.core.dataCache.KalmanErrorCacheKey;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheFactory;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheKey;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Headway;

import net.sf.ehcache.Cache;
import smile.regression.RLS;

public class DwellTimeModelCache implements org.transitclock.core.dataCache.DwellTimeModelCacheInterface {

	final private static String cacheName = "DwellTimeModelCache";
	
	private CacheAccess<DwellTimeCacheKey, RLS>  cache = null;
	
	public DwellTimeModelCache() {
		cache = JCS.getInstance(cacheName);	
		
	}
	@Override
	public void addSample(Indices indices, Headway headway, long dwellTime) {
		// TODO Auto-generated method stub
		DwellTimeCacheKey key=new DwellTimeCacheKey(indices);
		
		RLS rls = null;
		if(cache.get(key)==null)
		{
			
			rls=cache.get(key);
		}else
		{
			double samplex[][]=new double[1][1];
			double sampley[]=new double[1];
			
			samplex[0][0]=headway.getHeadway();
			sampley[0]=dwellTime;
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
			ArrivalDeparture arrival=findArrival(stopData, departure);
			ArrivalDeparture previousArrival=findPreviousArrival(stopData, arrival);
			if(arrival!=null&&previousArrival!=null)
			{			
				Headway headway=new Headway();
				headway.setHeadway(arrival.getTime()-previousArrival.getTime());
				long dwelltime=departure.getTime()-arrival.getTime();
				addSample(indices, headway,dwelltime);
			}								
		}		
	}

	private ArrivalDeparture findPreviousArrival(List<ArrivalDeparture> stopData, ArrivalDeparture arrival) {
		// TODO Auto-generated method stub
		return null;
	}
	private ArrivalDeparture findArrival(List<ArrivalDeparture> stopData, ArrivalDeparture departure) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public long predictDwellTime(Indices indices, HeadwayDetails headway) {
		// TODO Auto-generated method stub
		DwellTimeCacheKey key=new DwellTimeCacheKey(indices);
		RLS rls=cache.get(key);
		double[] arg0 = new double[1];
		arg0[0]=headway.getHeadway();
		rls.predict(arg0);
		return (long) rls.predict(arg0);
	}

}
