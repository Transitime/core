package org.transitclock.reporting.service.runTime.prescriptive.model;

import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.ScheduleTime;
import org.transitclock.ipc.data.IpcPrescriptiveRunTime;
import org.transitclock.ipc.data.IpcRunTime;
import org.transitclock.reporting.TimePointStatistics;
import org.transitclock.statistics.StatisticsV2;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.transitclock.reporting.service.runTime.prescriptive.timebands.helper.PrescriptiveRunTimeHelper.*;

public class PrescriptiveRunTimeState {

    Map<Integer, ScheduleTime> scheduleTimesByStopPathIndexMap;
    Map<Integer, IpcPrescriptiveRunTime> ipcPrescriptiveRunTimesByStopPathIndex = new LinkedHashMap<>();
    Map<String, List<ArrivalDeparture>> arrivalDeparturesByStopPath;

    // Avg RunTime Values
    private double avgFixed;
    private double avgVariable;
    private double avgDwell;

    // OtpStates
    private double currentOnTime;
    private double currentExpectedOnTime;
    private double totalCurrentOnTime;
    private double totalExpectedOnTime;
    private double totalRunTimes;

    private double plusExpectedOnTime;
    private double minusExpectedOnTime;

    private TimePointStatistics timePointStatistics;

    private Double currentTimePointFixedTime = 0d;
    private Double currentTimePointDwellTime = 0d;
    private Double currentTimePointVariableTime = 0d;
    private Double currentTimePointRemainder = 0d;

    private Double currentTimePointAvgRunTime = 0d;

    private Integer previousTimePointStopPathIndex = 0;
    private final double adjustedTimePositiveError;
    private final double adjustedTimeNegativeError;


    private int currentTimePointIndex = 1;

    public PrescriptiveRunTimeState(Map<Integer, ScheduleTime> scheduleTimesByStopPathIndexMap,
                                    Map<String, List<ArrivalDeparture>> arrivalDeparturesByStopPath,
                                    double adjustedTimePositiveError,
                                    double adjustedTimeNegativeError) {
        this.scheduleTimesByStopPathIndexMap = scheduleTimesByStopPathIndexMap;
        this.arrivalDeparturesByStopPath = arrivalDeparturesByStopPath;
        this.adjustedTimePositiveError = adjustedTimePositiveError;
        this.adjustedTimeNegativeError = adjustedTimeNegativeError;
    }

    private void resetForNewTimePoint() {
        currentTimePointFixedTime = 0d;
        currentTimePointDwellTime = 0d;
        currentTimePointVariableTime = 0d;
        currentTimePointAvgRunTime = 0d;
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

        if(!isFirstStop) {

            // Update Fixed, Variable and DwellTime, and Remainder Values
            updateComponentValues(timePointStatistics);

            Double dwellTime = getAdjustedDwellTime(timePointStatistics);

            addAvgRunTime(timePointStatistics.getAverageRunTime() + dwellTime);

        }else if(includeFirstStopDwell) {
            avgDwell += timePointStatistics.getAverageDwellTime();
        }

    }

    private void updateComponentValues(TimePointStatistics timePointStatistics){
        //double runTimeStdDev = StatisticsV2.getStdDev(allRunTimes, timePointStatistics.getAverageRunTime());
        List<Double> allDwellTimes = timePointStatistics.getAllDwellTimes();
        List<Double> allRunTimes = timePointStatistics.getAllRunTimes();
        Double fixedTime = timePointStatistics.getMinRunTime();

        boolean isLastStop = timePointStatistics.isLastStop();
        avgFixed += fixedTime;
        avgVariable += timePointStatistics.getAverageRunTime() - fixedTime;
        avgDwell += timePointStatistics.getAverageDwellTime();

        addFixedTime(fixedTime);
        addDwellTime(getDwellPercentileValue(allDwellTimes));
        addVariableTime(getVariablePercentileValue(fixedTime, allRunTimes, getCurrentTimePointIndex(), isLastStop));
        addRemainder(getRemainderPercentileValue(fixedTime, allRunTimes, getCurrentTimePointIndex(), isLastStop));
    }

    private Double getAdjustedDwellTime(TimePointStatistics timePointStatistics) {
        List<Double> allDwellTimes = timePointStatistics.getAllDwellTimes();
        Double averageDwellTime = timePointStatistics.getAverageDwellTime();

        double dwellTimeStdDev = StatisticsV2.getStdDev(allDwellTimes, averageDwellTime);

        Double dwellPercentile = getDwellPercentileValue(allDwellTimes);

        if(dwellPercentile > averageDwellTime - (dwellTimeStdDev * 1f) &&
                dwellPercentile < averageDwellTime +  (dwellTimeStdDev * 1f)){
            return dwellPercentile;
        }

        return averageDwellTime;

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

    public void addAvgRunTime(Double avgRunTime){
        currentTimePointAvgRunTime +=avgRunTime;
    }


    public void createPrescriptiveRunTimeForTimePoint(boolean isScheduledOnly) {
        Double scheduleRunTime = getScheduledRunTime();
        Double scheduleDwellTime = getScheduledDwellTime();
        Double runTimeAdjustment = getRunTimeAdjustment(scheduleRunTime, isScheduledOnly);

        updateCurrentOnTimeCounts(scheduleRunTime, scheduleDwellTime, runTimeAdjustment);

        boolean isCurrentExpectedOtpBetter = currentExpectedOnTime >= currentOnTime;

        // Compares Performance of Adjusted vs Existing
        // If the existing schedule is better than don't adjust the time
        updateTotalOnTimeCounts(isCurrentExpectedOtpBetter);

        Double updatedRunTimeAdjustment = getUpdatedRunTimeAdjustmentForCurrentCounts(isCurrentExpectedOtpBetter, runTimeAdjustment);

        IpcPrescriptiveRunTime ipcPrescriptiveRunTime = new IpcPrescriptiveRunTime(timePointStatistics.getStopPathId(),
                                                                                    timePointStatistics.getStopName(),
                                                                                    timePointStatistics.getStopPathIndex(),
                                                                                    updatedRunTimeAdjustment,
                                                                                    scheduleRunTime);

        ipcPrescriptiveRunTimesByStopPathIndex.put(timePointStatistics.getStopPathIndex(), ipcPrescriptiveRunTime);
        resetForNewTimePoint();
    }

    private Double getUpdatedRunTimeAdjustmentForCurrentCounts(boolean isCurrentExpectedOtpBetter,
                                                               Double runTimeAdjustment) {
        if(isCurrentExpectedOtpBetter){
            return runTimeAdjustment;
        }
        return 0d;
    }

    private void updateTotalOnTimeCounts(boolean isCurrentExpectedOtpBetter) {
        totalCurrentOnTime += currentOnTime;
        if(isCurrentExpectedOtpBetter){
            totalExpectedOnTime += currentExpectedOnTime;
        } else {
            totalExpectedOnTime += currentOnTime;
        }
    }

    private void updateCurrentOnTimeCounts(Double scheduleRunTime, Double scheduledDwellTime, Double adjustment){
        Long maxEarlyTime = TimeUnit.MINUTES.toMillis(1);
        Long maxLateTime = TimeUnit.MINUTES.toMillis(-5);

        // reset current ontime counts
        currentOnTime = 0;
        currentExpectedOnTime = 0;

        double positiveErrorBuffer = adjustment + adjustedTimePositiveError;

        List<ArrivalDeparture> arrivalDepartures = arrivalDeparturesByStopPath.get(timePointStatistics.getStopPathId());
        if(arrivalDepartures != null && scheduleRunTime > 0){
            for(ArrivalDeparture ad: arrivalDepartures){
                if(ad.isArrival()){
                    boolean currentIsOnTime = ad.getScheduleAdherence().getTemporalDifference() > maxLateTime;
                    boolean expectedIsNotLate = (ad.getScheduledTime() + adjustment) - ad.getTime() > maxLateTime
                            || (ad.getScheduledTime() + positiveErrorBuffer) - ad.getTime() > maxLateTime;
                    boolean expectedIsOnTime = adjustment == 0 ? currentIsOnTime : expectedIsNotLate;

                    if(currentIsOnTime){
                        currentOnTime++;
                    }
                    if(expectedIsOnTime){
                        currentExpectedOnTime++;
                    }
                } else if(ad.isDeparture()) {
                    boolean currentIsOnTime = ad.getScheduleAdherence().getTemporalDifference() < maxEarlyTime;
                    if(currentIsOnTime){
                        currentOnTime++;
                        currentExpectedOnTime++;
                    }
                }

                totalRunTimes++;
            }
        }
    }


    private Double getRunTimeAdjustment(Double scheduleRunTime, boolean isScheduledOnly) {
        if (isScheduledOnly || timePointStatistics.isFirstStop()) {
            return 0d;
        }
        Double currentFixedTime = getCurrentTimePointFixedTime();
        Double currentTimePointVariableTime = getCurrentTimePointVariableTime();
        Double currentTimePointDwellTime = getCurrentTimePointDwellTime();
        //Double newRunTime = currentFixedTime + currentTimePointVariableTime + currentTimePointDwellTime;

        Double newRunTime = getCurrentTimePointAvgRunTime();
        Double adjustment = newRunTime - scheduleRunTime;

        return adjustment;
    }

    private Double getScheduledRunTime() {
        if (timePointStatistics.isFirstStop()) {
            return 0d;
        }
        Integer prevScheduledTime = scheduleTimesByStopPathIndexMap.get(previousTimePointStopPathIndex).getTime();
        Integer currentScheduledTime = scheduleTimesByStopPathIndexMap.get(timePointStatistics.getStopPathIndex()).getTime();
        if (prevScheduledTime == null || currentScheduledTime == null) {
            return 0d;
        }
        return Double.valueOf((currentScheduledTime - prevScheduledTime) * 1000);
    }

    private Double getScheduledDwellTime() {
        if (timePointStatistics.isLastStop()) {
            return 0d;
        }
        Integer arrivalScheduledTime = scheduleTimesByStopPathIndexMap.get(timePointStatistics.getStopPathIndex()).getArrivalOrDepartureTime();
        Integer departureScheduledTime = scheduleTimesByStopPathIndexMap.get(timePointStatistics.getStopPathIndex()).getTime();
        if (arrivalScheduledTime == null || departureScheduledTime == null) {
            return 0d;
        }
        return Double.valueOf((departureScheduledTime - arrivalScheduledTime) * 1000);
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

    private Double getCurrentTimePointAvgRunTime() {
        return currentTimePointAvgRunTime;
    }

    public List<IpcPrescriptiveRunTime> getPrescriptiveRunTimes() {
        return new ArrayList<>(ipcPrescriptiveRunTimesByStopPathIndex.values());
    }

    public Map<Integer, IpcPrescriptiveRunTime> getPrescriptiveRunTimesByStopPathIndex() {
        return ipcPrescriptiveRunTimesByStopPathIndex;
    }

    public double getCurrentOnTimeFraction() {
        return totalCurrentOnTime/totalRunTimes;
    }

    public double getExpectedOnTimeFraction() {
        return totalExpectedOnTime/totalRunTimes;
    }

    public double getCurrentOnTime() {
        return totalCurrentOnTime;
    }

    public double getExpectedOnTime() {
        return totalExpectedOnTime;
    }

    public double getTotalRunTimes() {
        return totalRunTimes;
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