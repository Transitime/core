package org.transitclock.core.dataCache.ehcache.scheduled;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.LongConfigValue;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheFactory;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheKey;
import org.transitclock.core.dataCache.StopPathCacheKey;
import org.transitclock.core.dataCache.ehcache.CacheManagerFactory;
import org.transitclock.core.predictiongenerator.scheduled.dwell.DwellModel;
import org.transitclock.core.predictiongenerator.scheduled.dwell.DwellTimeModelFactory;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Headway;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.utils.Time;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
/**
 *
 * @author scrudden
 * This stores DwellModel instances in the cache. TODO We should abstract the anomaly detection as per TODO in code below.
 */
public class DwellTimeModelCache implements org.transitclock.core.dataCache.DwellTimeModelCacheInterface {

	private static LongConfigValue maxDwellTimeAllowedInModel = new LongConfigValue("transitclock.prediction.dwell.maxDwellTimeAllowedInModel", (long) (2 * Time.MS_PER_MIN), "Max dwell time to be considered in dwell RLS algotithm.");
	private static LongConfigValue minDwellTimeAllowedInModel = new LongConfigValue("transitclock.prediction.dwell.minDwellTimeAllowedInModel", (long) 1000, "Min dwell time to be considered in dwell RLS algotithm.");
	private static LongConfigValue maxHeadwayAllowedInModel = new LongConfigValue("transitclock.prediction.dwell.maxHeadwayAllowedInModel", 1*Time.MS_PER_HOUR, "Max headway to be considered in dwell RLS algotithm.");
	private static LongConfigValue minHeadwayAllowedInModel = new LongConfigValue("transitclock.prediction.dwell.minHeadwayAllowedInModel", (long) 1000, "Min headway to be considered in dwell RLS algotithm.");
	private static IntegerConfigValue minSceheduleAdherence = new IntegerConfigValue("transitclock.prediction.dwell.minSceheduleAdherence", (int)  (10 * Time.SEC_PER_MIN), "If schedule adherence of vehicle is outside this then not considerd in dwell RLS algorithm.");
	private static IntegerConfigValue maxSceheduleAdherence = new IntegerConfigValue("transitclock.prediction.dwell.maxSceheduleAdherence", (int)  (10 * Time.SEC_PER_MIN), "If schedule adherence of vehicle is outside this then not considerd in dwell RLS algorithm.");


	final private static String cacheName= "dwellTimeModelCache";

	private static final Logger logger = LoggerFactory.getLogger(DwellTimeModelCache.class);


	private Cache<StopPathCacheKey, DwellModel> cache = null;

	public DwellTimeModelCache() throws IOException {

		CacheManager cm = CacheManagerFactory.getInstance();

		cache = cm.getCache(cacheName, StopPathCacheKey.class, DwellModel.class);
	}
	@Override
	public void addSample(ArrivalDeparture event, Headway headway, long dwellTime) {

		StopPathCacheKey key=new StopPathCacheKey(headway.getTripId(), event.getStopPathIndex(), false);
		DwellModel empty = DwellTimeModelFactory.getInstance();
		empty.putSample((int) dwellTime, (int) headway.getHeadway(), null);

		synchronized (cache) {
			DwellModel model = cache.get(key);
			if (model == null) {
				cache.put(key, empty);
			} else {
				model.putSample((int) dwellTime, (int) headway.getHeadway(), null);
				cache.put(key, model);
			}
		}
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
							/* TODO Should abstract this behind an anomaly detention interface/Factory */

							if(departure.getScheduleAdherence()!=null && departure.getScheduleAdherence().isWithinBounds(minSceheduleAdherence.getValue(),maxSceheduleAdherence.getValue()))
							{

								if((departure.getStop().isWaitStop()==null||!departure.getStop().isWaitStop())
										&&(departure.getStop().isLayoverStop()==null || !departure.getStop().isLayoverStop()))
								{
									// Arrival schedule adherence appears not to be set much. So only stop if set and outside range.
									if(previousArrival.getScheduledAdherence()==null || previousArrival.getScheduledAdherence().isWithinBounds(minSceheduleAdherence.getValue(),maxSceheduleAdherence.getValue()))
									{
										if(dwelltime<maxDwellTimeAllowedInModel.getValue() &&
												dwelltime >  minDwellTimeAllowedInModel.getValue())
										{
											if(headway.getHeadway() < maxHeadwayAllowedInModel.getValue()
													&& headway.getHeadway() > minHeadwayAllowedInModel.getValue())
											{
												addSample(departure,headway,dwelltime);
											}else
											{
												logger.warn("Headway outside allowable range . {}", headway);
											}
										}else
										{
											logger.warn("Dwell time {} outside allowable range for {}.", dwelltime, departure);
										}
									}else
									{
										logger.warn("Schedule adherence outside allowable range. "+previousArrival.getScheduledAdherence());
									}
								}else
								{
									logger.warn("This is a wait stop or layover so not being included in model as dwell time is affected by if vehicle is early or late to the stop.");
								}
							}else
							{
								logger.warn("Schedule adherence outside allowable range. "+departure.getScheduleAdherence());
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

		DwellModel model=(DwellModel) cache.get(cacheKey);
		if(model==null||headway==null)
			return null;

		if(model.predict((int)headway.getHeadway(), null)!=null)
			return new Long(model.predict((int)headway.getHeadway(), null));

		return null;
	}

	@Override
	public void populateCacheFromDb(List<ArrivalDeparture> results) {
		logger.info("running populateCacheFromDb...");
		synchronized (cache) {
			for (ArrivalDeparture result : results) {
				try {
					addSample(result);
				} catch (Exception e) {
					logger.error("Exception caching {}", e, e);
				}
			}
		}
		logger.info("finished populateCacheFromDb...");
	}


}
