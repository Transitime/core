package org.transitclock.core.reporting;

import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.RunTimesForRoutes;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class TestRunTimeCacheImpl implements RunTimeCache{
    @Override
    public List<RunTimesForRoutes> getAll() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public void update(RunTimeProcessorResult runTimeProcessorResult, List<ArrivalDeparture> arrivalDepartures) {

    }

    @Override
    public boolean containsDuplicateStops(RunTimesForRoutes rt) {
        return false;
    }

    @Override
    public RunTimesForRoutes deduplicate(RunTimesForRoutes rt) {
        return null;
    }

    @Override
    public RunTimesForRoutes getOrCreate(int configRev, String id, Date startTime, String vehicleId) {
        return null;
    }

    @Override
    public boolean isValid(RunTimesForRoutes rt) {
        return false;
    }
}
