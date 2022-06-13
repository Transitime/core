package org.transitclock.reporting.service.runTime.prescriptive.model;

import org.transitclock.db.structs.ScheduleTime;

public class AvgScheduleTime extends ScheduleTime {
    private Integer totalArrivalTime;
    private Integer totalDepartureTime;
    private int arrivalCount = 0;
    private int departureCount = 0;

    public AvgScheduleTime(ScheduleTime scheduleTime){
        add(scheduleTime);
    }

    public void add(ScheduleTime scheduleTime){
        if(scheduleTime.getArrivalTime() != null){
            if(totalArrivalTime == null){
                totalArrivalTime = 0;
            }
            totalArrivalTime += scheduleTime.getArrivalTime();
            arrivalCount++;
        }
        if(scheduleTime.getDepartureTime() != null){
            if(totalDepartureTime == null){
                totalDepartureTime = 0;
            }
            totalDepartureTime += scheduleTime.getDepartureTime();
            departureCount++;
        }
    }

    public ScheduleTime getAverageScheduleTime(){
        Integer avgArrivalTime = totalArrivalTime != null ? totalArrivalTime/arrivalCount : null;
        Integer avgDepartureTime = totalDepartureTime != null ? totalDepartureTime/departureCount : null;
        return new ScheduleTime(avgArrivalTime, avgDepartureTime);
    }
}
