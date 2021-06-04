package org.transitclock.reporting.service.runTime;

import org.transitclock.db.structs.RunTimesForStops;
import org.transitclock.db.structs.ScheduleTime;
import org.transitclock.db.structs.StopPath;
import org.transitclock.reporting.TimePointStatistics;
import org.transitclock.reporting.keys.StopPathRunTimeKey;

import java.util.*;
import java.util.stream.Collectors;

public class TimePointRunTimeProcessor {
    Map<StopPathRunTimeKey, StopPath> stopPathsMap = new LinkedHashMap<>();
    Map<StopPathRunTimeKey, StopPath> timePointStopPathsMap = new LinkedHashMap<>();
    Map<StopPathRunTimeKey, TimePointStatistics> timePointStatsMap = new HashMap<>();
    Map<Integer, ScheduleTime> scheduleTimesByStopPathIndexMap;

    public TimePointRunTimeProcessor(List<StopPath> stopPaths) {
        createStopPathsGroupedById(stopPaths);
    }

    private void createStopPathsGroupedById(List<StopPath> stopPaths){
        for(int i=0; i< stopPaths.size(); i++){
            StopPath stopPath = stopPaths.get(i);
            StopPathRunTimeKey key = new StopPathRunTimeKey(stopPath.getId(), i);
            stopPathsMap.put(key, stopPath);
            if(stopPath.isScheduleAdherenceStop()){
                timePointStopPathsMap.put(key, stopPath);
            }
        }
    }

    /**
     * Converts RunTimesForStops and groups them into stop points
     * If a stop for a specific timepoint grouping is missing then the entire
     * timepoint is dropped
     * @param runTimesForStops
     */
    public void addRunTimesForStops(List<RunTimesForStops> runTimesForStops){
        Map<StopPathRunTimeKey, RunTimesForStops> runTimesStopPathsMap = getRunTimesStopPathsMap(runTimesForStops);

        boolean validTimePoint = true;
        Double currentTimePointRunTime = 0d;
        Double currentTimePointDwellTime = 0d;

        for(Map.Entry<StopPathRunTimeKey, StopPath> stopPathsForTripPattern: stopPathsMap.entrySet()) {
            StopPathRunTimeKey key = stopPathsForTripPattern.getKey();
            StopPath stopPath = stopPathsForTripPattern.getValue();
            RunTimesForStops runTimeForStops = runTimesStopPathsMap.get(key);

            if(isValid(runTimeForStops)){
                currentTimePointRunTime += runTimeForStops.getRunTime() == null ? 0 : runTimeForStops.getRunTime();
                currentTimePointDwellTime += runTimeForStops.getDwellTime() == null ? 0 : runTimeForStops.getDwellTime();
            }else{
                validTimePoint = false;
            }

            if(stopPath.isScheduleAdherenceStop()){
                if(validTimePoint) {
                    if(key.getStopPathIndex() == 0){
                        currentTimePointRunTime = 0d;
                    }
                    createRunTimeForTimePoint(key, stopPath, currentTimePointRunTime, currentTimePointDwellTime);
                }
                currentTimePointRunTime = 0d;
                currentTimePointDwellTime = 0d;
                validTimePoint = true;
            }
        }
    }

    private Map<StopPathRunTimeKey, RunTimesForStops> getRunTimesStopPathsMap(List<RunTimesForStops> runTimesForStops){
        Map<StopPathRunTimeKey, RunTimesForStops> runTimesStopPathsMap = new LinkedHashMap<>();
        for(RunTimesForStops runTimeForStop : runTimesForStops){
            StopPathRunTimeKey key = new StopPathRunTimeKey(runTimeForStop.getStopPathId(), runTimeForStop.getStopPathIndex());
            runTimesStopPathsMap.put(key, runTimeForStop);
        }
        return runTimesStopPathsMap;
    }

    private boolean isValid(RunTimesForStops runTimeForStops) {
        if(runTimeForStops == null){
            return false;
        }
        if(runTimeForStops.getFirstStop()){
            return runTimeForStops.getDwellTime() != null;
        }
        else if(runTimeForStops.getLastStop()){
            return runTimeForStops.getRunTime() != null;
        }
        return runTimeForStops.getDwellTime() != null && runTimeForStops.getRunTime() != null;
    }


    public void createRunTimeForTimePoint(StopPathRunTimeKey key,
                                          StopPath stopPath,
                                         Double currentTimePointRunTime,
                                         Double currentTimePointDwellTime){
        TimePointStatistics timePointStatistics = getOrCreateTimePointStatistics(key, stopPath);
        timePointStatistics.getRunTimeStats().add(currentTimePointRunTime);
        timePointStatistics.getDwellTimeStats().add(currentTimePointDwellTime);
        timePointStatistics.getTotalRunTimes().add(currentTimePointRunTime + currentTimePointDwellTime);
    }

    private TimePointStatistics getOrCreateTimePointStatistics(StopPathRunTimeKey key, StopPath stopPath){
        TimePointStatistics timePointStatistics = timePointStatsMap.get(key);
        if(timePointStatistics == null){
            timePointStatistics = new TimePointStatistics(key.getStopPathId(), key.getStopPathIndex(),
                    stopPath.getStopName(), stopPath.isLastStopInTrip());
            timePointStatsMap.put(key, timePointStatistics);
        }
        return timePointStatistics;

    }

    public Map<StopPathRunTimeKey, TimePointStatistics> getTimePointsStatistics(){
        return timePointStatsMap;
    }

    public Map<StopPathRunTimeKey, TimePointStatistics> getSortedTimePointsStatistics(){
        return timePointStatsMap
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    public Map<StopPathRunTimeKey, StopPath> getTimePointStopPaths(){
        return timePointStopPathsMap;
    }

    public Map<Integer, ScheduleTime> getScheduleTimesByStopPathIndexMap() {
        return scheduleTimesByStopPathIndexMap;
    }
}
