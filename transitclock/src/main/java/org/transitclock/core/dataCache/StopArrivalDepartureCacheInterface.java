package org.transitclock.core.dataCache;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.ipc.data.IpcArrivalDeparture;

public abstract class StopArrivalDepartureCacheInterface {

	private static final Logger logger = LoggerFactory.getLogger(StopArrivalDepartureCacheInterface.class);

	abstract  public  List<IpcArrivalDeparture> getStopHistory(StopArrivalDepartureCacheKey key);

	abstract  public StopArrivalDepartureCacheKey putArrivalDeparture(ArrivalDeparture arrivalDeparture);

	abstract public void populateCacheFromDb(List<ArrivalDeparture> results);
	public void defaultPopulateCacheFromDb(List<ArrivalDeparture> results) {
		try {
			for (ArrivalDeparture result : results) {
				this.putArrivalDeparture(result);
				//TODO might be better with its own populateCacheFromdb
				DwellTimeModelCacheFactory.getInstance().addSample(result);
			}
		} catch (Throwable t) {
			logger.error("StopArrivalDepartureCacheInterface failed with {}", t, t);
		}
	}

}