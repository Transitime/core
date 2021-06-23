package org.transitclock.db.structs;

import com.google.common.base.Objects;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.DynamicUpdate;
import org.transitclock.core.dwell.DwellTimeUtil;
import org.transitclock.db.hibernate.HibernateUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

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


    public String getStopPathId() {
        return stopPathId;
    }

    public void setStopPathId(String stopPathId) {
        this.stopPathId = stopPathId;
    }



    public Date getPrevStopDepartureTime() {
        return prevStopDepartureTime;
    }

    public void setPrevStopDepartureTime(Date prevStopArrivalTime) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RunTimesForStops that = (RunTimesForStops) o;
        return stopPathIndex == that.stopPathIndex &&
                Objects.equal(time, that.time) &&
                Objects.equal(stopPathId, that.stopPathId) &&
                Objects.equal(prevStopDepartureTime, that.prevStopDepartureTime) &&
                Objects.equal(scheduledTime, that.scheduledTime) &&
                Objects.equal(scheduledPrevStopArrivalTime, that.scheduledPrevStopArrivalTime) &&
                Objects.equal(dwellTime, that.dwellTime) &&
                Objects.equal(speed, that.speed) &&
                Objects.equal(lastStop, that.lastStop) &&
                Objects.equal(timePoint, that.timePoint);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(stopPathIndex, time, stopPathId, prevStopDepartureTime, scheduledTime,
                scheduledPrevStopArrivalTime, dwellTime, speed, lastStop, timePoint);
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
                '}';
    }
}
