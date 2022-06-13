package org.transitclock.reporting.service.runTime.prescriptive.timebands.helper;

import org.transitclock.reporting.service.runTime.prescriptive.timebands.model.RunTimeData;

import java.util.*;

public class RunTimeDataHelper {

    //sets the sequence based on the scheduled start time
    public static void assignSequence(List<RunTimeData> runTimeData) {
        Collections.sort(runTimeData, new RunTimeDataScheduledStartTimeComparator());
        for (int i = 0; i < runTimeData.size(); i++) {
            runTimeData.get(i).setSequence(i);
        }
    }

    public static List<RunTimeData> reduce(List<RunTimeData> runTimeData) {
        Map<String, List<RunTimeData>> mappedList = new HashMap<>();
        Map<String, RunTimeData> reducedList = new HashMap<>();
        for (RunTimeData rtd : runTimeData) {
            if (!mappedList.containsKey(rtd.getTripId()))
                mappedList.put(rtd.getTripId(), new ArrayList<>());
            mappedList.get(rtd.getTripId()).add(rtd);
        }
        for (String key : mappedList.keySet()) {
            List<RunTimeData> sorted = mappedList.get(key);
            sorted.sort(Comparator.comparingDouble(RunTimeData::getActualRuntime));
            RunTimeData median = sorted.get(sorted.size()/2);//just always use the lower one
            reducedList.put(key, median);
        }
        return new ArrayList(reducedList.values());
    }

    public static Map<String, List<RunTimeData>> mapRunTimeDataByTripPattern(List<RunTimeData> runTimeData){
        Map<String, List<RunTimeData>> tripPatternMap = new HashMap<>();
        for (RunTimeData rtd : runTimeData) {
            if (!tripPatternMap.containsKey(rtd.getTripPatternId())) {
                tripPatternMap.put(rtd.getTripPatternId(), new ArrayList<>());
            }
            tripPatternMap.get(rtd.getTripPatternId()).add(rtd);
        }
        return tripPatternMap;
    }
}
