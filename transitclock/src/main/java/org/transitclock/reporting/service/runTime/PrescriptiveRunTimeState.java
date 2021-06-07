package org.transitclock.reporting.service.runTime;

import org.transitclock.config.BooleanConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.db.structs.ScheduleTime;
import org.transitclock.ipc.data.IpcPrescriptiveRunTime;
import org.transitclock.ipc.data.IpcRunTime;
import org.transitclock.reporting.TimePointStatistics;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.transitclock.reporting.service.runTime.PrescriptiveRunTimeHelper.*;

public class PrescriptiveRunTimeState {

    Map<Integer, ScheduleTime> scheduleTimesByStopPathIndexMap;
    Map<Integer, IpcPrescriptiveRunTime> ipcPrescriptiveRunTimesByStopPathIndex = new LinkedHashMap<>();

    // Avg RunTime Values
    private double avgFixed;
    private double avgVariable;
    private double avgDwell;

    // OtpStates
    private double currentOnTime;
    private double expectedOnTime;
    private double totalRunTimes;

    private TimePointStatistics timePointStatistics;

    private Double currentTimePointFixedTime = 0d;
    private Double currentTimePointDwellTime = 0d;
    private Double currentTimePointVariableTime = 0d;
    private Double currentTimePointRemainder = 0d;
    private Integer previousTimePointStopPathIndex = 0;

    private int currentTimePointIndex = 1;

    public PrescriptiveRunTimeState(Map<Integer, ScheduleTime> scheduleTimesByStopPathIndexMap) {
        this.scheduleTimesByStopPathIndexMap = scheduleTimesByStopPathIndexMap;
    }

    private void resetForNewTimePoint() {
        currentTimePointFixedTime = 0d;
        currentTimePointDwellTime = 0d;
        currentTimePointVariableTime = 0d;
        if (!timePointStatistics.isFirstStop()) {
            currentTimePointIndex++;
            previousTimePointStopPathIndex = timePointStatistics.getStopPathIndex();
        } else {
            previousTimePointStopPathIndex = 0;
        }
    }

    public int getCurrentTimePointIndex() {
        return currentTimePointIndex;
    }

    public void updateScheduleAdjustments(TimePointStatistics timePointStatistics, boolean includeFirstStopDwell) {
        this.timePointStatistics = timePointStatistics;

        boolean isFirstStop = timePointStatistics.isFirstStop();
        boolean isLastStop = timePointStatistics.isLastStop();

        if(!isFirstStop) {
            List<Double> allDwellTimes = timePointStatistics.getAllDwellTimes();
            List<Double> allRunTimes = timePointStatistics.getAllRunTimes();
            Double fixedTime = timePointStatistics.getMinRunTime();

            avgFixed += fixedTime;
            avgVariable += timePointStatistics.getAverageRunTime() - fixedTime;
            avgDwell += timePointStatistics.getAverageDwellTime();

            addFixedTime(fixedTime);
            addDwellTime(getDwellPercentileValue(allDwellTimes, isLastStop));
            addVariableTime(getVariablePercentileValue(fixedTime, allRunTimes, getCurrentTimePointIndex(), isLastStop));
            addRemainder(getRemainderPercentileValue(fixedTime, allRunTimes, getCurrentTimePointIndex(), isLastStop));
        }else if(includeFirstStopDwell) {
            avgDwell += timePointStatistics.getAverageDwellTime();
        }

    }

    public void addFixedTime(Double fixedTime) {
        currentTimePointFixedTime += fixedTime;
    }

    public void addDwellTime(Double dwellTime) {
        currentTimePointDwellTime += dwellTime;
    }

    public void addVariableTime(Double variableTime) {
        currentTimePointVariableTime += variableTime;
    }

    public void addRemainder(Double remainderTime) {
        currentTimePointRemainder += remainderTime;
    }


    public void createPrescriptiveRunTimeForTimePoint() {
        Double scheduleRunTime = getScheduledRunTime();
        Double runTimeAdjustment = getRunTimeAdjustment(scheduleRunTime);
        updateOnTimeCounts(scheduleRunTime, runTimeAdjustment);
        IpcPrescriptiveRunTime ipcPrescriptiveRunTime = new IpcPrescriptiveRunTime(timePointStatistics.getStopPathId(),
                                                                                timePointStatistics.getStopName(),
                                                                                timePointStatistics.getStopPathIndex(),
                                                                                runTimeAdjustment,
                                                                                scheduleRunTime);

        ipcPrescriptiveRunTimesByStopPathIndex.put(timePointStatistics.getStopPathIndex(), ipcPrescriptiveRunTime);
        resetForNewTimePoint();
    }

    private void updateOnTimeCounts(Double scheduleRunTime, Double adjustment){
        for(Double runTime : timePointStatistics.getTotalRunTimes()){
            if(Math.abs(runTime - scheduleRunTime) <= TimeUnit.MINUTES.toMillis(1)){
                currentOnTime++;
            }
            if(Math.abs(runTime - (scheduleRunTime + adjustment)) <= TimeUnit.MINUTES.toMillis(1)){
                expectedOnTime++;
            }
            totalRunTimes++;
        }
    }


    private Double getRunTimeAdjustment(Double scheduleRunTime) {
        if (timePointStatistics.isFirstStop()) {
            return 0d;
        }
        return (getCurrentTimePointFixedTime() + getCurrentTimePointVariableTime() + getCurrentTimePointDwellTime()) - scheduleRunTime;
    }

    private Double getScheduledRunTime() {
        if (timePointStatistics.isFirstStop()) {
            return 0d;
        }
        Integer prevScheduledTime = scheduleTimesByStopPathIndexMap.get(previousTimePointStopPathIndex).getDepartureTime();
        Integer currentScheduledTime = scheduleTimesByStopPathIndexMap.get(timePointStatistics.getStopPathIndex()).getTime();
        if (prevScheduledTime == null || currentScheduledTime == null) {
            return 0d;
        }
        return Double.valueOf((currentScheduledTime - prevScheduledTime) * 1000);
    }

    private Double getCurrentTimePointFixedTime() {
        return currentTimePointFixedTime;
    }

    private Double getCurrentTimePointVariableTime() {
        if (timePointStatistics.isLastStop()) {
            return currentTimePointVariableTime += currentTimePointRemainder;
        }
        return currentTimePointVariableTime;
    }

    private Double getCurrentTimePointDwellTime() {
        return currentTimePointDwellTime;
    }

    public List<IpcPrescriptiveRunTime> getPrescriptiveRunTimes() {
        return new ArrayList<>(ipcPrescriptiveRunTimesByStopPathIndex.values());
    }

    public Map<Integer, IpcPrescriptiveRunTime> getPrescriptiveRunTimesByStopPathIndex() {
        return ipcPrescriptiveRunTimesByStopPathIndex;
    }

    public double getCurrentOnTimeFraction() {
        return currentOnTime/totalRunTimes;
    }

    public double getExpectedOnTimeFraction() {
        return expectedOnTime/totalRunTimes;
    }

    public IpcRunTime getAvgRunTimes(){
        return new IpcRunTime(avgFixed, avgVariable, avgDwell);
    }

    public String getHash(String... values){
        String hash = null;
        for(String value : values){
            if(hash != null){
                hash += "_";
            }
            hash += "value";
        }
        return hash;
    }

}