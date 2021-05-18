package org.transitclock.core.reporting;

import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.RunTimesForRoutes;

import java.util.Date;
import java.util.List;

public interface RunTimeCache {
    List<RunTimesForRoutes> getAll();

    void update(RunTimeProcessorResult runTimeProcessorResult, List<ArrivalDeparture> arrivalDepartures);

    boolean containsDuplicateStops(RunTimesForRoutes rt);

    RunTimesForRoutes deduplicate(RunTimesForRoutes rt);

    RunTimesForRoutes getOrCreate(int configRev, String id, Date startTime, String vehicleId);

    boolean isValid(RunTimesForRoutes rt);
}
