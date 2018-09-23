package org.transitclock.core.dataCache.jcs.scheduled;

import java.util.Calendar;
import java.util.Date;
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
import org.transitclock.core.predictiongenerator.scheduled.dwell.rls.TransitClockRLS;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Block;
import org.transitclock.db.structs.Headway;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.utils.Time;

public class DwellTimeModelCache implements org.transitclock.core.dataCache.DwellTimeModelCacheInterface {

	final private static String cacheName = "DwellTimeModelCache";


	private static LongConfigValue maxDwellTimeAllowedInModel = new LongConfigValue("transitclock.prediction.rls.maxDwellTimeAllowedInModel", (long) (2 * Time.MS_PER_MIN), "Max dwell time to be considered in dwell RLS algotithm.");
	private static LongConfigValue minDwellTimeAllowedInModel = new LongConfigValue("transitclock.prediction.rls.minDwellTimeAllowedInModel", (long) 1000, "Min dwell time to be considered in dwell RLS algotithm.");
	private static LongConfigValue maxHeadwayAllowedInModel = new LongConfigValue("transitclock.prediction.rls.maxHeadwayAllowedInModel", 1*Time.MS_PER_HOUR, "Max headway to be considered in dwell RLS algotithm.");
	private static LongConfigValue minHeadwayAllowedInModel = new LongConfigValue("transitclock.prediction.rls.minHeadwayAllowedInModel", (long) 1000, "Min headway to be considered in dwell RLS algotithm.");


	private static DoubleConfigValue lambda = new DoubleConfigValue("transitclock.prediction.rls.lambda", 0.75, "This sets the rate at which the RLS algorithm forgets old values. Value are between 0 and 1. With 0 being the most forgetful.");

	private CacheAccess<StopPathCacheKey, TransitClockRLS>  cache = null;

	private static final Logger logger = LoggerFactory.getLogger(DwellTimeModelCache.class);

	public DwellTimeModelCache() {
		cache = JCS.getInstance(cacheName);
	}
	@Override
	synchronized public void addSample(ArrivalDeparture event, Headway headway, long dwellTime) {

		StopPathCacheKey key=new StopPathCacheKey(headway.getTripId(), event.getStopPathIndex());


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
						/* TODO Should abstract this behind an anomaly detention interface/Factory */
						if(dwelltime<maxDwellTimeAllowedInModel.getValue() &&
								dwelltime >  minDwellTimeAllowedInModel.getValue() &&
									headway.getHeadway() < maxHeadwayAllowedInModel.getValue()
									&& headway.getHeadway() > minHeadwayAllowedInModel.getValue())

						{
							addSample(departure,headway,dwelltime);
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
							if(event.getTime()<arrival.getTime()&&(sameDay(event.getTime(), arrival.getTime())|| Math.abs(event.getTime()-arrival.getTime()) < maxHeadwayAllowedInModel.getValue()))
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
	public Long predictDwellTime(StopPathCacheKey cacheKey, Headway headway) {

		TransitClockRLS rls=cache.get(cacheKey);
		if(rls!=null&&rls.getRls()!=null)
		{
			double[] arg0 = new double[1];
			arg0[0]=headway.getHeadway();
			rls.getRls().predict(arg0);
			return (long) Math.pow(10, rls.getRls().predict(arg0));
		}else
		{
			return null;
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
