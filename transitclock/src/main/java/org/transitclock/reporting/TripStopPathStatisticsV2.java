package org.transitclock.reporting;

import org.transitclock.core.travelTimes.DataFetcher;
import org.transitclock.db.structs.*;
import org.transitclock.reporting.keys.StopPathRunTimeKey;
import org.transitclock.reporting.keys.TripTimeVehicleKey;

import java.util.*;
import java.util.stream.Collectors;

public class TripStopPathStatisticsV2 {

    private Trip primaryTrip;
    private Integer nextTripTime;
    private int expectedStopPathCount;
    Map<String, StopPath> stopPathsGroupedById;

    private Set<StopPathRunTimeKey> uniqueStopPathRunTimes = new LinkedHashSet<>();
    private Set<StopPathRunTimeKey> uniqueStopPathDwellTimes = new LinkedHashSet<>();
    private Map<StopPathRunTimeKey, StopPathStatisticsV2> stopPathStatistics = new HashMap<>();
    private Map<TripTimeVehicleKey, Long> runTimesForTrips = new HashMap<>();

    public TripStopPathStatisticsV2(Trip primaryTrip, int nextTripTime) {
        this.primaryTrip = primaryTrip;
        this.nextTripTime = nextTripTime;
        this.expectedStopPathCount = primaryTrip.getNumberStopPaths() - 1;
        stopPathsGroupedById = primaryTrip.getStopPaths().stream()
                .collect(Collectors.toMap(StopPath::getId, stopPath -> stopPath));
    }

    /**
     * Method to collect stopPath run-times
     *
     * @param runTimesForStops
     */
    public void addStopPathRunTime(RunTimesForStops runTimesForStops) {
        if (runTimesForStops.getRunTimesForRoutes().getTripId().equals(primaryTrip.getId())
                && stopPathsGroupedById.get(runTimesForStops.getStopPathId()) != null) {

            addRunTimeForTrip(runTimesForStops.getRunTimesForRoutes());

            Double runTime = getRunTimeForStopPath(runTimesForStops);
            if (runTime != null) {
                StopPathRunTimeKey stopPathkey = new StopPathRunTimeKey(null, runTimesForStops.getStopPathId(),
                        runTimesForStops.getStopPathIndex());

                StopPathStatisticsV2 result = getStatsMapResult(stopPathkey, runTimesForStops);
                result.getRunTimeStats().add(runTime);
                uniqueStopPathRunTimes.add(stopPathkey);
            }
        }
    }

    private void addRunTimeForTrip(RunTimesForRoutes runTimeForRoute){
        TripTimeVehicleKey tripTimeVehicleKey = new TripTimeVehicleKey(runTimeForRoute.getTripId(),
                runTimeForRoute.getStartTime().getTime(), runTimeForRoute.getVehicleId());

        if(!runTimesForTrips.containsKey(tripTimeVehicleKey)){
            runTimesForTrips.put(tripTimeVehicleKey, runTimeForRoute.getRunTime());
        }
    }

    /*private void addStopPathRunTimeForTrip(StopPathRunTimeKey stopPathRunTimeKey,
                                           RunTimesForRoutes runTimeForRoute,
                                           RunTimesForStops runTimesForStop){
        TripTimeVehicleKey tripTimeVehicleKey = new TripTimeVehicleKey(runTimeForRoute.getTripId(),
                runTimeForRoute.getStartTime().getTime(), runTimeForRoute.getVehicleId());

        Map<StopPathRunTimeKey, StopPathRunTime> stopPathRunTimes = stopPathRunTimesPerUniqueTrip.get(tripTimeVehicleKey);
        if(stopPathRunTimes == null){
            stopPathRunTimes = new HashMap<>();
            stopPathRunTimesPerUniqueTrip.put(tripTimeVehicleKey, stopPathRunTimes);
        }

        stopPathRunTimes.put(stopPathRunTimeKey, );
        if(stopPathRunTime == null){

        }
    }

    private StopPathRunTime getStopPathRunTime(RunTimesForStops runTimesForStop){
        return new StopPathRunTime(runTimesForStop);
    }
    */

    private Double getRunTimeForStopPath(RunTimesForStops runTimesForStops) {
            Long runTimeForStopPath = runTimesForStops.getRunTime();
            if(runTimeForStopPath != null){
                return new Double(runTimeForStopPath);
            }
            return null;
    }

    /**
     * Method to collect stopPath dwell-times
     * @param runTimesForStops
     */
    public void addStopPathDwellTime(RunTimesForStops runTimesForStops) {
        if (runTimesForStops.getRunTimesForRoutes().getTripId().equals(primaryTrip.getId())
                && runTimesForStops.getStopPathIndex() < expectedStopPathCount
                && stopPathsGroupedById.get(runTimesForStops.getStopPathId()) != null) {

            Double dwellTime = getDwellTimeForStopPath(runTimesForStops);
            if (dwellTime != null) {
                StopPathRunTimeKey stopPathkey = new StopPathRunTimeKey(null, runTimesForStops.getStopPathId(),
                        runTimesForStops.getStopPathIndex());
                StopPathStatisticsV2 result = getStatsMapResult(stopPathkey, runTimesForStops);
                result.getDwellTimeStats().add(dwellTime);
                uniqueStopPathDwellTimes.add(stopPathkey);
            }
        }
    }

    private Double getDwellTimeForStopPath(RunTimesForStops runTimesForStops) {
        if (runTimesForStops.getDwellTime() != null) {
            return new Double(runTimesForStops.getDwellTime());
        }
        return null;
    }

    public Map<StopPathRunTimeKey, StopPathStatisticsV2> getAllStopPathStatistics() {
        return stopPathStatistics;
    }

    public StopPathStatisticsV2 getStopPathStatistics(String stopId, String stopPathId, Integer stopPathIndex) {
        return stopPathStatistics.get(new StopPathRunTimeKey(stopId, stopPathId, stopPathIndex));
    }

    public Trip getTrip() {
        return primaryTrip;
    }


    public Double getTripAverageRunTime() {
        double tripRunTime = 0;
        if (hasAllStopPathsForRunTimes()) {
            for (Map.Entry<StopPathRunTimeKey, StopPathStatisticsV2> entry : stopPathStatistics.entrySet()) {
                StopPathStatisticsV2 sps = entry.getValue();
                Double avgRunTime = sps.getAverageRunTime();
                if (avgRunTime == null) {
                    return null;
                }
                tripRunTime += avgRunTime;
            }
            return tripRunTime;
        }
        return null;
    }

    public Double getTripMedianRunTime() {
        double tripRunTime = 0;
        if (hasAllStopPathsForRunTimes()) {
            for (Map.Entry<StopPathRunTimeKey, StopPathStatisticsV2> entry : stopPathStatistics.entrySet()) {
                StopPathStatisticsV2 sps = entry.getValue();
                if(sps.isFirstStop()){
                    continue;
                }
                Double medianRunTime = sps.getMedianRunTime();
                if (medianRunTime == null) {
                    return null;
                }
                tripRunTime += medianRunTime;
            }
            return tripRunTime;
        }
        return null;
    }

    public Double getTripFixedRunTime() {
        double tripFixedRunTime = 0;
        if (hasAllStopPathsForRunTimes()) {
            for (Map.Entry<StopPathRunTimeKey, StopPathStatisticsV2> entry : stopPathStatistics.entrySet()) {
                StopPathStatisticsV2 sps = entry.getValue();
                if (sps.isFirstStop()) {
                    continue;
                }
                Double minRunTime = sps.getMinRunTime();
                if (minRunTime == null) {
                    return null;
                }
                tripFixedRunTime += minRunTime;
            }
            return tripFixedRunTime;
        }
        return null;
    }

    public Double getTripAvgDwellTime() {
        double tripDwellTime = 0;
        if (hasAllStopPathsForDwellTimes()) {
            for (Map.Entry<StopPathRunTimeKey, StopPathStatisticsV2> entry : stopPathStatistics.entrySet()) {
                StopPathStatisticsV2 sps = entry.getValue();
                if (sps.isLastStop()) {
                    continue;
                }
                Double avgDwellTime = sps.getAverageDwellTime();
                if (avgDwellTime == null) {
                    return null;
                }
                tripDwellTime += avgDwellTime;
            }
            return tripDwellTime;
        }
        return null;
    }

    public Double getTripMedianDwellTime() {
        double tripRunTime = 0;
        if (hasAllStopPathsForDwellTimes()) {
            for (Map.Entry<StopPathRunTimeKey, StopPathStatisticsV2> entry : stopPathStatistics.entrySet()) {
                StopPathStatisticsV2 sps = entry.getValue();
                if (sps.isLastStop()) {
                    continue;
                }
                Double medianRunTime = sps.getMedianDwellTime();
                if (medianRunTime == null) {
                    return null;
                }
                tripRunTime += medianRunTime;
            }
            return tripRunTime;
        }
        return null;
    }

    public Map<StopPathRunTimeKey, Double> getStopPathAverageRunTime() {
        if (hasAllStopPathsForRunTimes()) {
            Map<StopPathRunTimeKey, Double> stopPathAverageRunTime = new LinkedHashMap<>();
            for (Map.Entry<StopPathRunTimeKey, StopPathStatisticsV2> entry : stopPathStatistics.entrySet()) {
                StopPathStatisticsV2 sps = entry.getValue();
                Double avgRunTime = sps.getAverageRunTime();
                if (avgRunTime == null) {
                    return null;
                }
                stopPathAverageRunTime.put(entry.getKey(), avgRunTime);
            }
            return stopPathAverageRunTime;
        }
        return null;
    }

    public Map<StopPathRunTimeKey, Double> getStopPathAvgDwellTime() {
        if (hasAllStopPathsForDwellTimes()) {
            Map<StopPathRunTimeKey, Double> stopPathAvgDwellTime = new LinkedHashMap<>();
            for (Map.Entry<StopPathRunTimeKey, StopPathStatisticsV2> entry : stopPathStatistics.entrySet()) {
                StopPathStatisticsV2 sps = entry.getValue();
                Double avgDwellTime = sps.getAverageDwellTime();
                if (sps.isLastStop()) {
                    continue;
                } else if (avgDwellTime == null) {
                    return null;
                }
                stopPathAvgDwellTime.put(entry.getKey(), avgDwellTime);
            }
            return stopPathAvgDwellTime;
        }
        return null;
    }

    public Map<StopPathRunTimeKey, Double> getStopPathFixedRunTime() {
        if (hasAllStopPathsForRunTimes()) {
            Map<StopPathRunTimeKey, Double> stopPathFixedRunTime = new LinkedHashMap<>();
            for (Map.Entry<StopPathRunTimeKey, StopPathStatisticsV2> entry : stopPathStatistics.entrySet()) {
                StopPathStatisticsV2 sps = entry.getValue();
                if (sps.isFirstStop()) {
                    continue;
                }
                Double minRunTime = sps.getMinRunTime();
                if (minRunTime == null) {
                    return null;
                }
                stopPathFixedRunTime.put(entry.getKey(), minRunTime);
            }
            return stopPathFixedRunTime;
        }
        return null;
    }

    public long getTotalTrips() {
        if (hasAllStopPathsForRunTimes()) {
            long count = 0;
            for (Map.Entry<StopPathRunTimeKey, StopPathStatisticsV2> entry : stopPathStatistics.entrySet()) {
                StopPathStatisticsV2 sps = entry.getValue();
                if (sps.getCount() > count) {
                    count = sps.getCount();
                }
            }
            return count;
        }
        return 0;
    }


    public String getTripId() {
        return primaryTrip.getId();
    }

    public int getExpectedStopPathCount() {
        return expectedStopPathCount;
    }

    public Integer getNextTripTime() {
        return nextTripTime;
    }

    public boolean hasAllStopPathsForRunTimes() {
        return uniqueStopPathRunTimes.size() == expectedStopPathCount + 1;
    }

    public boolean hasAllStopPathsForDwellTimes() {
        return uniqueStopPathDwellTimes.size() == expectedStopPathCount;
    }


    private StopPathStatisticsV2 getStatsMapResult(StopPathRunTimeKey key, RunTimesForStops runTimesForStops) {
        StopPathStatisticsV2 result = stopPathStatistics.get(key);
        if (result == null) {
            result = new StopPathStatisticsV2(runTimesForStops.getRunTimesForRoutes().getTripId(),
                    runTimesForStops.getStopPathId(), runTimesForStops.getStopPathIndex(), runTimesForStops.getLastStop());
            stopPathStatistics.put(key, result);
        }
        return result;
    }
}