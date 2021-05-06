package org.transitclock.core.dataCache.memcached.scheduled;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.LongConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheFactory;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheKey;
import org.transitclock.core.dataCache.StopPathCacheKey;
import org.transitclock.core.predictiongenerator.scheduled.dwell.DwellTimeModelFactory;
import org.transitclock.core.predictiongenerator.datafilter.DwellTimeDataFilter;
import org.transitclock.core.predictiongenerator.datafilter.DwellTimeFilterFactory;
import org.transitclock.core.predictiongenerator.scheduled.dwell.DwellModel;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Headway;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.utils.Time;

import net.spy.memcached.MemcachedClient;
/**
 * 
 * @author scrudden
 * This stores DwellModel instances in the cache. TODO We should abstract the anomaly detection as per TODO in code below.
 */
public class DwellTimeModelCache implements org.transitclock.core.dataCache.DwellTimeModelCacheInterface {

	private static LongConfigValue maxHeadwayAllowedInModel = new LongConfigValue("transitclock.prediction.dwell.maxHeadwayAllowedInModel", 1*Time.MS_PER_HOUR, "Max headway to be considered in dwell RLS algotithm.");
	private static LongConfigValue minHeadwayAllowedInModel = new LongConfigValue("transitclock.prediction.dwell.minHeadwayAllowedInModel", (long) 1000, "Min headway to be considered in dwell RLS algotithm.");
	

	private static StringConfigValue memcachedHost = new StringConfigValue("transitclock.cache.memcached.host", "127.0.0.1",
			"Specifies the host machine that memcache is running on.");

	private static IntegerConfigValue memcachedPort = new IntegerConfigValue("transitclock.cache.memcached.port", 11211,
			"Specifies the port that memcache is running on.");


	private static final Logger logger = LoggerFactory.getLogger(DwellTimeModelCache.class);
	MemcachedClient memcachedClient = null;
	private static String keystub = "DWELLTIMEMODEL_";
	Integer expiryDuration=Time.SEC_PER_DAY*7;
	public DwellTimeModelCache() throws IOException {
		memcachedClient = new MemcachedClient(
				new InetSocketAddress(memcachedHost.getValue(), memcachedPort.getValue().intValue()));
	}
	@Override
	synchronized public void addSample(ArrivalDeparture event, Headway headway, long dwellTime) {

		StopPathCacheKey key=new StopPathCacheKey(headway.getTripId(), event.getStopPathIndex());

		DwellModel model = null;
		
		if(memcachedClient.get(createKey(key))!=null)
		{
			model=(DwellModel) memcachedClient.get(createKey(key));
			
			model.putSample((int)dwellTime, (int)headway.getHeadway(),null);		
		}else
		{
			model=DwellTimeModelFactory.getInstance();		
		}
		model.putSample((int)dwellTime, (int)headway.getHeadway(),null);
		memcachedClient.set(createKey(key),expiryDuration, model);
	}

	@Override
	public void addSample(ArrivalDeparture departure) {
		try {
			if(departure!=null && !departure.isArrival())
			{				
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

							/* Leave out silly values as they are most likely errors or unusual circumstance. */																				
							DwellTimeDataFilter datafilter = DwellTimeFilterFactory.getInstance();
							
							if(!datafilter.filter(arrival, new IpcArrivalDeparture(departure)))
							{						
								
								/* TODO Should also abstract behind an anomaly detention interface/Factory */	
								if(headway.getHeadway() < maxHeadwayAllowedInModel.getValue()
										&& headway.getHeadway() > minHeadwayAllowedInModel.getValue())
								{
									addSample(departure,headway,dwelltime);
								}else
								{
									logger.warn("Headway outside allowable range . {}", headway);
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
							if(event.getTime().getTime()<arrival.getTime().getTime()&&(sameDay(event.getTime().getTime(), arrival.getTime().getTime())|| Math.abs(event.getTime().getTime()-arrival.getTime().getTime()) < maxHeadwayAllowedInModel.getValue()))
								return event;
						}
					}
				}
			}
		}

		return null;
	}
	private boolean sameDay(Long date1, Long date2)
	{
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.setTime(new Date(date1));
		cal2.setTime(new Date(date2));
		boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
	                  cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);

		return sameDay;
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

		DwellModel model=(DwellModel) memcachedClient.get(createKey(cacheKey));
		if(model==null||headway==null)
			return null;
		
		if(model.predict((int)headway.getHeadway(), null)!=null)
			return new Long(model.predict((int)headway.getHeadway(), null));
		
		return null;
	}

	@Override
	public void populateCacheFromDb(List<ArrivalDeparture> results) {
		for (ArrivalDeparture result: results) {
			addSample(result);
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
	private String createKey(StopPathCacheKey key)
	{
		return keystub + key.getTripId() + "_" + key.getStopPathIndex();
	}

}
