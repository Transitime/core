package org.transitclock.db.reporting;

import org.transitclock.db.structs.ArrivalDeparture;

import java.util.*;

public class TripStatisticsForConfigRev {
    private final int configRev;
    private final String tripId;
    private final int tripIndex;
    private final String blockId;
    private final int expectedStopPathCount;
    private Set<StopPathRunTimeKey> uniqueStopPathRunTimes= new HashSet<>();
    private Set<StopPathRunTimeKey> uniqueStopPathDwellTimes=new HashSet<>();
    private Map<StopPathRunTimeKey, StopPathStatistics> stopPathStatistics = new HashMap<>();

    public TripStatisticsForConfigRev(int configRev, String tripId, int expectedStopPathCount, int tripIndex, String blockId){
        this.configRev = configRev;
        this.tripId = tripId;
        this.expectedStopPathCount = expectedStopPathCount;
        this.tripIndex = tripIndex;
        this.blockId = blockId;
    }

    public Map<StopPathRunTimeKey, StopPathStatistics> getAllStopPathStatistics() {
        return stopPathStatistics;
    }

    public StopPathStatistics getStopPathStatistics(String stopId, String stopPathId, Integer stopPathIndex) {
        return stopPathStatistics.get(new StopPathRunTimeKey(stopId, stopPathId, stopPathIndex));
    }

    public int getConfigRev() {
        return configRev;
    }

    public String getTripId() {
        return tripId;
    }

    public int getTripIndex() {
        return tripIndex;
    }

    public String getBlockId() {
        return blockId;
    }

    public boolean hasAllStopPathsForRunTimes(){
        return uniqueStopPathRunTimes.size() == expectedStopPathCount;
    }

    public boolean hasAllStopPathsForDwellTimes(){
        return uniqueStopPathDwellTimes.size() == expectedStopPathCount;
    }

    public void addStopPathRunTime(StopPathRunTimeKey key, ArrivalDeparture ad, Double runTime){
        StopPathStatistics result = getStatsMapResult(key, ad);
        result.getRunTimeStats().accept(runTime);
        uniqueStopPathRunTimes.add(key);
    }

    public void addStopPathDwellTime(StopPathRunTimeKey key, ArrivalDeparture ad, Double dwellTime){
        StopPathStatistics result = getStatsMapResult(key, ad);
        result.getDwellTimeStats().accept(dwellTime);
        uniqueStopPathDwellTimes.add(key);
    }

    private StopPathStatistics getStatsMapResult(StopPathRunTimeKey key, ArrivalDeparture ad){
        StopPathStatistics result = stopPathStatistics.get(key);
        if(result == null){
            boolean isLastStop = ad.getStopPathIndex() == expectedStopPathCount;
            result = new StopPathStatistics(ad.getTripId(), ad.getStopPathId(), ad.getStopPathIndex(), isLastStop);
            stopPathStatistics.put(key, result);
        }
        return result;
    }

    public Double getTripAverageRunTime(){
        double tripRunTime = 0;
        if(hasAllStopPathsForRunTimes()){
            for(Map.Entry<StopPathRunTimeKey, StopPathStatistics> entry : stopPathStatistics.entrySet()){
                StopPathStatistics sps = entry.getValue();
                Double avgRunTime = sps.getAverageRunTime();
                if(avgRunTime == null){
                    return null;
                }
                tripRunTime += avgRunTime;
            }
            return tripRunTime;
        }
        return null;
    }

    public Double getTripFixedRunTime(){
        double tripFixedRunTime = 0;
        if(hasAllStopPathsForRunTimes()){
            for(Map.Entry<StopPathRunTimeKey, StopPathStatistics> entry : stopPathStatistics.entrySet()){
                StopPathStatistics sps = entry.getValue();
                if(sps.isFirstStop()){
                    continue;
                }
                Double minRunTime = sps.getMinRunTime();
                if(minRunTime == null){
                    return null;
                }
                tripFixedRunTime += minRunTime;
            }
            return tripFixedRunTime;
        }
        return null;
    }

    public Double getAvgDwellTime(){
        double tripDwellTime = 0;
        if(hasAllStopPathsForDwellTimes()){
            for(Map.Entry<StopPathRunTimeKey, StopPathStatistics> entry : stopPathStatistics.entrySet()){
                StopPathStatistics sps = entry.getValue();
                Double avgDwellTime = sps.getAverageDwellTime();
                if(sps.isLastStop()){
                    continue;
                }
                else if(avgDwellTime == 0 || avgDwellTime == null){
                    return null;
                }
                tripDwellTime += avgDwellTime;
            }
            return tripDwellTime;
        }
        return null;
    }

}
