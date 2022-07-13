package org.transitclock.db.structs;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.DynamicUpdate;
import org.transitclock.core.dwell.DwellTimeUtil;
import org.transitclock.db.hibernate.HibernateUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
@DynamicUpdate
@Table(name="RunTimesForStops")
public class RunTimesForStops implements Serializable {

    @Id
    private int stopPathIndex;

    @Id
    @Temporal(TemporalType.TIMESTAMP)
    private Date time;

    @Column(length=2*HibernateUtils.DEFAULT_ID_SIZE)
    private String stopPathId;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date prevStopDepartureTime;

    @Column
    private Integer scheduledTime;

    @Column
    private Integer scheduledPrevStopArrivalTime;

    @Column
    private Long dwellTime;

    @Column
    private Double speed;

    @Column
    private Boolean lastStop;

    @Column
    private Boolean timePoint;

    @Transient
    private boolean isScheduled;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.MERGE})
    @JoinColumns({
        @JoinColumn(insertable=false, updatable=false,name="vehicleId", referencedColumnName="vehicleId"),
        @JoinColumn(insertable=false, updatable=false,name="tripId", referencedColumnName="tripId"),
        @JoinColumn(insertable=false, updatable=false,name="startTime", referencedColumnName="startTime"),
        @JoinColumn(insertable=false, updatable=false,name="configRev", referencedColumnName="configRev")
    })
    private RunTimesForRoutes runTimesForRoutes;

    @Transient
    boolean firstStopDwellSet = false;

    @Transient
    private String vehicleId;

    @Transient
    private String tripId;

    @Transient
    private Date startTime;

    @Transient
    private Integer configRev;



    public RunTimesForStops() {}


    public RunTimesForStops(String stopPathId,
                            int stopPathIndex,
                            Date time,
                            Date prevStopDepartureTime,
                            Integer scheduledTime,
                            Integer scheduledPrevStopArrivalTime,
                            Long dwellTime,
                            Double speed,
                            Boolean lastStop,
                            Boolean timePoint) {
        this.stopPathId = stopPathId;
        this.stopPathIndex = stopPathIndex;
        this.time = time;
        this.prevStopDepartureTime = prevStopDepartureTime;
        this.scheduledTime = scheduledTime;
        this.scheduledPrevStopArrivalTime = scheduledPrevStopArrivalTime;
        this.dwellTime = dwellTime;
        this.speed = speed;
        this.lastStop = lastStop;
        this.timePoint = timePoint;
    }

    public RunTimesForStops(String stopPathId,
                            int stopPathIndex,
                            Date time,
                            Date prevStopDepartureTime,
                            Integer scheduledTime,
                            Integer scheduledPrevStopArrivalTime,
                            Long dwellTime,
                            Double speed,
                            Boolean lastStop,
                            Boolean timePoint,
                            String vehicleId,
                            String tripId,
                            Date startTime,
                            Integer configRev) {
        this(stopPathId, stopPathIndex, time, prevStopDepartureTime, scheduledTime, scheduledPrevStopArrivalTime, dwellTime,
                speed, lastStop, timePoint);
        this.vehicleId = vehicleId;
        this.tripId = tripId;
        this.startTime = startTime;
        this.configRev = configRev;
    }


    public String getStopPathId() {
        return stopPathId;
    }

    public void setStopPathId(String stopPathId) {
        this.stopPathId = stopPathId;
    }



    public Date getPrevStopDepartureTime() {
        return prevStopDepartureTime;
    }

    public void setPrevStopDepartureTime(Date prevStopDepartureTime) {
        this.prevStopDepartureTime = prevStopDepartureTime;
    }

    public Integer getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(Integer scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public Integer getScheduledPrevStopArrivalTime() {
        return scheduledPrevStopArrivalTime;
    }

    public void setScheduledPrevStopArrivalTime(Integer scheduledPrevStopArrivalTime) {
        this.scheduledPrevStopArrivalTime = scheduledPrevStopArrivalTime;
    }

    public Long getDwellTime() {
        if(!firstStopDwellSet && stopPathIndex == 0 && (dwellTime == null || dwellTime > 1000)
                && scheduledTime != null && time != null){
            firstStopDwellSet = true;
            dwellTime = DwellTimeUtil.calculateFirstStopDwellTime(scheduledTime, time.getTime());
        }
        return dwellTime;
    }

    public void setDwellTime(Long dwellTime) {
        this.dwellTime = dwellTime;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Boolean getLastStop() {
        return lastStop;
    }

    public void setLastStop(Boolean lastStop) {
        this.lastStop = lastStop;
    }

    public boolean getFirstStop(){
        return stopPathIndex == 0;
    }

    public Boolean getTimePoint() {
        return timePoint;
    }

    public void setTimePoint(Boolean timePoint) {
        this.timePoint = timePoint;
    }

    public RunTimesForRoutes getRunTimesForRoutes() {
        return runTimesForRoutes;
    }

    public int getStopPathIndex() {
        return stopPathIndex;
    }

    public void setStopPathIndex(int stopPathIndex) {
        this.stopPathIndex = stopPathIndex;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }


    public void setRunTimesForRoutes(RunTimesForRoutes runTimesForRoutes) {
        this.runTimesForRoutes = runTimesForRoutes;
    }

    public Long getRunTime() {
        if(stopPathIndex == 0){
            return 0l;
        }
        else if (prevStopDepartureTime != null && time != null && dwellTime != null) {
            return (time.getTime() - dwellTime) - prevStopDepartureTime.getTime();
        }
        return null;
    }

    public Integer getScheduledRunTime() {
        if(stopPathIndex == 0){
            return 0;
        }
        else if (scheduledPrevStopArrivalTime != null && scheduledTime != null) {
            return (scheduledTime - scheduledPrevStopArrivalTime);
        }
        return null;
    }

    public boolean isScheduled() {
        return isScheduled;
    }

    public void setScheduled(boolean scheduled) {
        isScheduled = scheduled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RunTimesForStops that = (RunTimesForStops) o;
        return stopPathIndex == that.stopPathIndex
                && isScheduled == that.isScheduled
                && firstStopDwellSet == that.firstStopDwellSet
                && time.equals(that.time)
                && stopPathId.equals(that.stopPathId)
                && Objects.equals(prevStopDepartureTime, that.prevStopDepartureTime)
                && scheduledTime.equals(that.scheduledTime)
                && Objects.equals(scheduledPrevStopArrivalTime, that.scheduledPrevStopArrivalTime)
                && Objects.equals(dwellTime, that.dwellTime)
                && Objects.equals(speed, that.speed)
                && lastStop.equals(that.lastStop)
                && timePoint.equals(that.timePoint)
                && runTimesForRoutes.equals(that.runTimesForRoutes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stopPathIndex, time, stopPathId, prevStopDepartureTime, scheduledTime,
                scheduledPrevStopArrivalTime, dwellTime, speed, lastStop, timePoint, isScheduled,
                runTimesForRoutes, firstStopDwellSet);
    }

    @Override
    public String toString() {
        return "RunTimesForStops{" +
                "stopPathIndex=" + stopPathIndex +
                ", time=" + time +
                ", stopPathId='" + stopPathId + '\'' +
                ", prevStopDepartureTime=" + prevStopDepartureTime +
                ", scheduledTime=" + scheduledTime +
                ", scheduledPrevStopArrivalTime=" + scheduledPrevStopArrivalTime +
                ", dwellTime=" + dwellTime +
                ", speed=" + speed +
                ", lastStop=" + lastStop +
                ", timePoint=" + timePoint +
                ", firstStopDwellSet=" + firstStopDwellSet +
                ", vehicleId=" + runTimesForRoutes.getVehicleId() +
                ", tripId=" + runTimesForRoutes.getTripId() +
                ", startTime=" + runTimesForRoutes.getStartTime() +
                ", route=" + runTimesForRoutes.getRouteShortName() +
                ", runTime=" + getRunTime() +
                '}';
    }
}
