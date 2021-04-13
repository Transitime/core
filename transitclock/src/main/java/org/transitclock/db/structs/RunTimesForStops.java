package org.transitclock.db.structs;

import com.google.common.base.Objects;
import org.hibernate.annotations.DynamicUpdate;
import org.transitclock.db.hibernate.HibernateUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@DynamicUpdate
@Table(name="RunTimesForStops")

public class RunTimesForStops implements Serializable {

    @Id
    @Column
    private int configRev;

    @Id
    @Column(length=2*HibernateUtils.DEFAULT_ID_SIZE)
    private String stopPathId;

    @Column
    private int stopPathIndex;

    @Id
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date time;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date prevStopArrivalTime;

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

    @ManyToOne(fetch = FetchType.LAZY)
    private RunTimesForRoutes runTimesForRoutes;


    public RunTimesForStops() { }

    public RunTimesForStops(int configRev,
                            String stopPathId,
                            int stopPathIndex,
                            Date time,
                            Date prevStopArrivalTime,
                            Integer scheduledTime,
                            Integer scheduledPrevStopArrivalTime,
                            Long dwellTime,
                            Double speed,
                            Boolean lastStop,
                            Boolean timePoint) {
        this.configRev = configRev;
        this.stopPathId = stopPathId;
        this.stopPathIndex = stopPathIndex;
        this.time = time;
        this.prevStopArrivalTime = prevStopArrivalTime;
        this.scheduledTime = scheduledTime;
        this.scheduledPrevStopArrivalTime = scheduledPrevStopArrivalTime;
        this.dwellTime = dwellTime;
        this.speed = speed;
        this.lastStop = lastStop;
        this.timePoint = timePoint;
    }

    public int getConfigRev() {
        return configRev;
    }

    public void setConfigRev(int configRev) {
        this.configRev = configRev;
    }

    public String getStopPathId() {
        return stopPathId;
    }

    public void setStopPathId(String stopPathId) {
        this.stopPathId = stopPathId;
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

    public Date getPrevStopArrivalTime() {
        return prevStopArrivalTime;
    }

    public void setPrevStopArrivalTime(Date prevStopArrivalTime) {
        this.prevStopArrivalTime = prevStopArrivalTime;
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

    public Boolean getTimePoint() {
        return timePoint;
    }

    public void setTimePoint(Boolean timePoint) {
        this.timePoint = timePoint;
    }

    public RunTimesForRoutes getRunTimesForRoutes() {
        return runTimesForRoutes;
    }

    public void setRunTimesForRoutes(RunTimesForRoutes runTimesForRoutes) {
        this.runTimesForRoutes = runTimesForRoutes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RunTimesForStops that = (RunTimesForStops) o;
        return configRev == that.configRev &&
                stopPathIndex == that.stopPathIndex &&
                Objects.equal(stopPathId, that.stopPathId) &&
                Objects.equal(time, that.time) &&
                Objects.equal(prevStopArrivalTime, that.prevStopArrivalTime) &&
                Objects.equal(scheduledTime, that.scheduledTime) &&
                Objects.equal(scheduledPrevStopArrivalTime, that.scheduledPrevStopArrivalTime) &&
                Objects.equal(dwellTime, that.dwellTime) &&
                Objects.equal(speed, that.speed) &&
                Objects.equal(lastStop, that.lastStop) &&
                Objects.equal(timePoint, that.timePoint) &&
                Objects.equal(runTimesForRoutes, that.runTimesForRoutes);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(configRev, stopPathId, stopPathIndex, time, prevStopArrivalTime,
                scheduledTime, scheduledPrevStopArrivalTime, dwellTime, speed, lastStop, timePoint,
                runTimesForRoutes);
    }
}
