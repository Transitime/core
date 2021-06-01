package org.transitclock.reporting.service.runTime;

import org.transitclock.db.structs.ScheduleTime;
import org.transitclock.ipc.data.IpcPrescriptiveRunTime;
import org.transitclock.ipc.data.IpcPrescriptiveRunTimes;
import org.transitclock.reporting.TimePointStatistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrescriptiveRunTimeState {
    private List<IpcPrescriptiveRunTime> ipcPrescriptiveRunTimes = new ArrayList<>();
    Map<Integer, ScheduleTime> scheduleTimesByStopPathIndexMap = new HashMap<>();

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

    public void updateTimePointStats(TimePointStatistics timePointStatistics) {
        this.timePointStatistics = timePointStatistics;
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


    public void createRunTimeForTimePoint() {
        Double scheduleRunTime = getScheduledRunTime();
        Double runTimeAdjustment = getRunTimeAdjustment(scheduleRunTime);
        ipcPrescriptiveRunTimes.add(
                new IpcPrescriptiveRunTime(
                        timePointStatistics.getStopPathId(),
                        timePointStatistics.getStopName(),
                        timePointStatistics.getStopPathIndex(),
                        runTimeAdjustment,
                        scheduleRunTime
                )
        );
        resetForNewTimePoint();
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
        return ipcPrescriptiveRunTimes;
    }
}