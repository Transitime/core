package org.transitclock.core.dataCache;

import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Headway;

import java.util.List;

public interface DwellTimeModelCacheInterface {
	
	void addSample(ArrivalDeparture event, Headway headway, long dwellTime);
	
	void addSample(ArrivalDeparture departure);
	
	Long predictDwellTime(StopPathCacheKey cacheKey, Headway headway);

  void populateCacheFromDb(List<ArrivalDeparture> results);
}
