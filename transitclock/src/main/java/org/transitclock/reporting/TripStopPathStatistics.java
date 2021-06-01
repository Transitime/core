package org.transitclock.reporting;

import org.transitclock.db.structs.*;
import org.transitclock.gtfs.TitleFormatter;
import org.transitclock.reporting.keys.StopPathRunTimeKey;
import org.transitclock.reporting.keys.TripTimeVehicleKey;

import java.util.*;

public class TripStopPathStatistics {

    private Trip primaryTrip;
    private Integer nextTripTime;
    private int expectedStopPathCount;
    private Map<StopPathRunTimeKey, StopPath> stopPathsGroupedById;

    private Set<StopPathRunTimeKey> matchedStopPathRunTimes = new LinkedHashSet<>();
    private Set<StopPathRunTimeKey> matchedStopPathDwellTimes = new LinkedHashSet<>();
    private Map<StopPathRunTimeKey, StopPathStatistics> stopPathStatistics = new HashMap<>();
    private Map<TripTimeVehicleKey, Long> runTimesForTrips = new HashMap<>();

    public TripStopPathStatistics(Trip primaryTrip, Integer nextTripTime) {
        this.primaryTrip = primaryTrip;
        this.nextTripTime = nextTripTime;
        this.expectedStopPathCount = primaryTrip.getNumberStopPaths() - 1;
        List<StopPath> stopPaths = primaryTrip.getStopPaths();
        stopPathsGroupedById = new LinkedHashMap();
        for(int i=0; i< stopPaths.size(); i++){
            StopPathRunTimeKey key = getKey(stopPaths.get(i).getId(), i);
            stopPathsGroupedById.put(key, stopPaths.get(i));
        }
    }

    private StopPathRunTimeKey getKey(String stopPathId, Integer stopPathIndex){
        return new StopPathRunTimeKey(stopPathId, stopPathIndex);
    }

    /**
     * Method to collect stopPath run-times
     *
     * @param runTimesForStops
     */
    public void addStopPathRunTime(RunTimesForStops runTimesForStops) {
        if (runTimesForStops.getRunTimesForRoutes().getTripId().equals(primaryTrip.getId())
                && stopPathsGroupedById.get(getKey(runTimesForStops.getStopPathId(),runTimesForStops.getStopPathIndex())) != null) {

            addRunTimeForTrip(runTimesForStops.getRunTimesForRoutes());

            Double runTime = getRunTimeForStopPath(runTimesForStops);
            if (runTime != null) {
                StopPathRunTimeKey stopPathkey = new StopPathRunTimeKey(runTimesForStops.getStopPathId(),
                        runTimesForStops.getStopPathIndex());

                StopPathStatistics result = getStatsMapResult(stopPathkey, runTimesForStops);
                result.getRunTimeStats().add(runTime);
                if(!matchedStopPathRunTimes.contains(stopPathkey)){
                    matchedStopPathRunTimes.add(stopPathkey);
                }
            }
        }
    }

    private void addRunTimeForTrip(RunTimesForRoutes runTimeForRoute){
        TripTimeVehicleKey tripTimeVehicleKey = new TripTimeVehicleKey(runTimeForRoute.getTripId(),
                runTimeForRoute.getStartTime().getTime(), runTimeForRoute.getVehicleId());

        if(!runTimesForTrips.containsKey(tripTimeVehicleKey) && runTimeForRoute.hasCompleteRunTime()){
            runTimesForTrips.put(tripTimeVehicleKey, runTimeForRoute.getRunTime());
        }
    }

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
                && stopPathsGroupedById.get(getKey(runTimesForStops.getStopPathId(),runTimesForStops.getStopPathIndex())) != null) {

            Double dwellTime = getDwellTimeForStopPath(runTimesForStops);
            if (dwellTime != null) {
                StopPathRunTimeKey stopPathkey = new StopPathRunTimeKey(runTimesForStops.getStopPathId(),
                        runTimesForStops.getStopPathIndex());
                StopPathStatistics result = getStatsMapResult(stopPathkey, runTimesForStops);
                result.getDwellTimeStats().add(dwellTime);
                if(!matchedStopPathDwellTimes.contains(stopPathkey)){
                    matchedStopPathDwellTimes.add(stopPathkey);
                }
            }
        }
    }

    private Double getDwellTimeForStopPath(RunTimesForStops runTimesForStops) {
        if (runTimesForStops.getDwellTime() != null) {
            return new Double(runTimesForStops.getDwellTime());
        }
        return null;
    }

    public Map<StopPathRunTimeKey, StopPathStatistics> getAllStopPathStatistics() {
        return stopPathStatistics;
    }

    public StopPathStatistics getStopPathStatistics(String stopId, String stopPathId, Integer stopPathIndex) {
        return stopPathStatistics.get(new StopPathRunTimeKey(stopPathId, stopPathIndex));
    }

    public Trip getTrip() {
        return primaryTrip;
    }


    public Double getTripAverageRunTime() {
        double tripRunTime = 0;
        if (hasAllStopPathsForRunTimes()) {
            for (Map.Entry<StopPathRunTimeKey, StopPathStatistics> entry : stopPathStatistics.entrySet()) {
                StopPathStatistics sps = entry.getValue();
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
            for (Map.Entry<StopPathRunTimeKey, StopPathStatistics> entry : stopPathStatistics.entrySet()) {
                StopPathStatistics sps = entry.getValue();
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
            for (Map.Entry<StopPathRunTimeKey, StopPathStatistics> entry : stopPathStatistics.entrySet()) {
                StopPathStatistics sps = entry.getValue();
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
            for (Map.Entry<StopPathRunTimeKey, StopPathStatistics> entry : stopPathStatistics.entrySet()) {
                StopPathStatistics sps = entry.getValue();
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
            for (Map.Entry<StopPathRunTimeKey, StopPathStatistics> entry : stopPathStatistics.entrySet()) {
                StopPathStatistics sps = entry.getValue();
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
            for (Map.Entry<StopPathRunTimeKey, StopPathStatistics> entry : stopPathStatistics.entrySet()) {
                StopPathStatistics sps = entry.getValue();
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
            for (Map.Entry<StopPathRunTimeKey, StopPathStatistics> entry : stopPathStatistics.entrySet()) {
                StopPathStatistics sps = entry.getValue();
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
            for (Map.Entry<StopPathRunTimeKey, StopPathStatistics> entry : stopPathStatistics.entrySet()) {
                StopPathStatistics sps = entry.getValue();
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
        return matchedStopPathRunTimes.size() == expectedStopPathCount + 1;
    }

    public boolean hasAllStopPathsForDwellTimes() {
        return matchedStopPathDwellTimes.size() == expectedStopPathCount;
    }


    private StopPathStatistics getStatsMapResult(StopPathRunTimeKey key, RunTimesForStops runTimesForStops) {
        StopPathStatistics result = stopPathStatistics.get(key);

        if (result == null) {
            result = new StopPathStatistics(runTimesForStops.getRunTimesForRoutes().getTripId(),
                                              runTimesForStops.getStopPathId(),
                                              runTimesForStops.getStopPathIndex(),
                                              getStopNameForStopPath(runTimesForStops.getStopPathId(),
                                                      runTimesForStops.getStopPathIndex()),
                                              isTimePoint(runTimesForStops.getStopPathId(), runTimesForStops.getStopPathIndex()),
                                              runTimesForStops.getLastStop());
            stopPathStatistics.put(key, result);
        }
        return result;
    }

    private boolean isTimePoint(String stopPathId, Integer stopPathIndex){
        StopPath stopPath = stopPathsGroupedById.get(getKey(stopPathId,stopPathIndex));
        if(stopPath != null && stopPath.isScheduleAdherenceStop()){
            return true;
        }
        return false;
    }

    private String getStopNameForStopPath(String stopPathId, int stopPathIndex){
        StopPath stopPath = stopPathsGroupedById.get(getKey(stopPathId,stopPathIndex));
        if(stopPath != null && stopPath.getStopName() != null){
            return getStopNamePrefix(stopPathIndex) + TitleFormatter.capitalize(stopPath.getStopName());
        }
        return null;
    }

    private String getStopNamePrefix(int stopPathIndex){
        if(stopPathIndex == 0){
            return "";
        }
        return "To ";
    }
}