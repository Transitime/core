package org.transitclock.reporting.service.runTime.prescriptive.timebands.model;

import java.time.LocalTime;
import java.util.List;

import static org.transitclock.utils.Time.formatSecondsIntoDay;

public class TimebandTime {
    LocalTime startTime;
    LocalTime endTime;

    public TimebandTime(){}

    public TimebandTime(LocalTime startTime, LocalTime endTime){
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public TimebandTime(List<RunTimeData> runTimeData){
        this.startTime = LocalTime.ofSecondOfDay(runTimeData.stream().findFirst().get().getScheduledStartTime());
        this.endTime = LocalTime.ofSecondOfDay(runTimeData.stream().reduce((first, second) -> second).get().getScheduledStartTime());
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
}

