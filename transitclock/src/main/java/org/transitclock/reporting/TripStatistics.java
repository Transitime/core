package org.transitclock.reporting;

import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.StopPath;
import org.transitclock.db.structs.Trip;
import org.transitclock.reporting.keys.StopPathRunTimeKey;

import java.util.*;
import java.util.stream.Collectors;

public class TripStatistics {

    private Trip trip;
    private int tripIndex;
    private String blockId;
    private int expectedStopPathCount;
    Map<String, StopPath> stopPathsGroupedById = new HashMap<>();

    private Set<StopPathRunTimeKey> uniqueStopPathRunTimes= new LinkedHashSet<>();
    private Set<StopPathRunTimeKey> uniqueStopPathDwellTimes=new LinkedHashSet<>();
    private Map<StopPathRunTimeKey, StopPathStatistics> stopPathStatistics = new HashMap<>();

    public TripStatistics(Trip trip, int tripIndex) {
        this.trip = trip;
        this.tripIndex = tripIndex;
        this.expectedStopPathCount = trip.getNumberStopPaths() - 1;
        stopPathsGroupedById = trip.getStopPaths().stream()
                                                  .collect(Collectors.toMap(StopPath::getId, stopPath -> stopPath));
    }

    /**
     * Method to collect stopPath run-times
     * @param ad
     * @param runTime
     */
    public void addStopPathRunTime(ArrivalDeparture ad, Double runTime){
       /* if(ad.getTripId().equals("6190176")) {
            System.out.println(ad.getTripId() + " -- " + ad.getStopPathIndex() + "(" + ad.getStopId() + ")" + " - " + runTime);
        }*/

        if(ad.getTripId().equals(trip.getId())
                && stopPathsGroupedById.get(ad.getStopPathId()) != null){
            StopPathRunTimeKey stopPathkey = new StopPathRunTimeKey(ad.getStopId(), ad.getStopPathId(), ad.getStopPathIndex());
            addStopPathRunTime(stopPathkey, ad, runTime);
        }
    }

    /**
     * Method to collect stopPath dwell-times
     * @param ad
     * @param dwellTime
     */
    public void addStopPathDwellTime(ArrivalDeparture ad, Double dwellTime){
        if(ad.getTripId().equals(trip.getId())
                && ad.getStopPathIndex() < expectedStopPathCount
                && stopPathsGroupedById.get(ad.getStopPathId()) != null){
            StopPathRunTimeKey stopPathkey = new StopPathRunTimeKey(ad.getStopId(), ad.getStopPathId(), ad.getStopPathIndex());
            addStopPathDwellTime(stopPathkey, ad, dwellTime);
        }
    }


    public Map<StopPathRunTimeKey, StopPathStatistics> getAllStopPathStatistics() {
        return stopPathStatistics;
    }

    public StopPathStatistics getStopPathStatistics(String stopId, String stopPathId, Integer stopPathIndex) {
        return stopPathStatistics.get(new StopPathRunTimeKey(stopId, stopPathId, stopPathIndex));
    }

    public Trip getTrip() {
        return trip;
    }

    public int getTripIndex() {
        return tripIndex;
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

    public Double getTripAvgDwellTime(){
        double tripDwellTime = 0;
        if(hasAllStopPathsForDwellTimes()){
            for(Map.Entry<StopPathRunTimeKey, StopPathStatistics> entry : stopPathStatistics.entrySet()){
                StopPathStatistics sps = entry.getValue();
                Double avgDwellTime = sps.getAverageDwellTime();
                if(sps.isLastStop()){
                    continue;
                }
                else if(avgDwellTime == null){
                    return null;
                }
                tripDwellTime += avgDwellTime;
            }
            return tripDwellTime;
        }
        return null;
    }

    public Map<StopPathRunTimeKey, Double> getStopPathAverageRunTime(){
        if(hasAllStopPathsForRunTimes()){
            Map<StopPathRunTimeKey, Double> stopPathAverageRunTime = new LinkedHashMap<>();
            for(Map.Entry<StopPathRunTimeKey, StopPathStatistics> entry : stopPathStatistics.entrySet()){
                StopPathStatistics sps = entry.getValue();
                Double avgRunTime = sps.getAverageRunTime();
                if(avgRunTime == null){
                    return null;
                }
                stopPathAverageRunTime.put(entry.getKey(), avgRunTime);
            }
            return stopPathAverageRunTime;
        }
        return null;
    }

    public Map<StopPathRunTimeKey, Double> getStopPathAvgDwellTime(){
        if(hasAllStopPathsForDwellTimes()){
            Map<StopPathRunTimeKey, Double> stopPathAvgDwellTime = new LinkedHashMap<>();
            for(Map.Entry<StopPathRunTimeKey, StopPathStatistics> entry : stopPathStatistics.entrySet()){
                StopPathStatistics sps = entry.getValue();
                Double avgDwellTime = sps.getAverageDwellTime();
                if(sps.isLastStop()){
                    continue;
                }
                else if(avgDwellTime == null){
                    return null;
                }
                stopPathAvgDwellTime.put(entry.getKey(), avgDwellTime);
            }
            return stopPathAvgDwellTime;
        }
        return null;
    }

    public Map<StopPathRunTimeKey, Double> getStopPathFixedRunTime(){
        if(hasAllStopPathsForRunTimes()){
            Map<StopPathRunTimeKey, Double> stopPathFixedRunTime = new LinkedHashMap<>();
            for(Map.Entry<StopPathRunTimeKey, StopPathStatistics> entry : stopPathStatistics.entrySet()){
                StopPathStatistics sps = entry.getValue();
                if(sps.isFirstStop()){
                    continue;
                }
                Double minRunTime = sps.getMinRunTime();
                if(minRunTime == null){
                    return null;
                }
                stopPathFixedRunTime.put(entry.getKey(),minRunTime);
            }
            return stopPathFixedRunTime;
        }
        return null;
    }

    public long getTotalTrips(){
        if(hasAllStopPathsForRunTimes()){
            long count = 0;
            for(Map.Entry<StopPathRunTimeKey, StopPathStatistics> entry : stopPathStatistics.entrySet()){
                StopPathStatistics sps = entry.getValue();
                if(sps.getCount() > count){
                    count = sps.getCount();
                }
            }
            return count;
        }
        return 0;
    }


    public String getTripId() {
        return trip.getId();
    }

    public String getBlockId() {
        return trip.getBlockId();
    }

    public int getExpectedStopPathCount() {
        return expectedStopPathCount;
    }

    public boolean hasAllStopPathsForRunTimes(){
        return uniqueStopPathRunTimes.size() == expectedStopPathCount;
    }

    public boolean hasAllStopPathsForDwellTimes(){
        return uniqueStopPathDwellTimes.size() == expectedStopPathCount;
    }

    private void addStopPathRunTime(StopPathRunTimeKey key, ArrivalDeparture ad, Double runTime){
        StopPathStatistics result = getStatsMapResult(key, ad);
        result.getRunTimeStats().accept(runTime);
        uniqueStopPathRunTimes.add(key);
    }

    private void addStopPathDwellTime(StopPathRunTimeKey key, ArrivalDeparture ad, Double dwellTime){
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


}
