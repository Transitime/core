package org.transitclock.core.dataCache.ehcache.dummy;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.core.dataCache.DwellTimeModelCacheInterface;
import org.transitclock.core.dataCache.StopPathCacheKey;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Headway;

import java.util.List;

public class DummyDwellTimeModelCache implements DwellTimeModelCacheInterface {

    private static final Logger logger = LoggerFactory.getLogger(DummyDwellTimeModelCache.class);


    @Override
    public void addSample(ArrivalDeparture event, Headway headway, long dwellTime) {
        return;
    }

    @Override
    public void addSample(ArrivalDeparture departure) {
        return;
    }

    @Override
    public Long predictDwellTime(StopPathCacheKey cacheKey, Headway headway) {
        return null;
    }

    @Override
    public void populateCacheFromDb(List<ArrivalDeparture> results) {

    }
}
