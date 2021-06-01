package org.transitclock.ipc.data;

import com.google.common.base.Objects;

public class IpcRunTimeForStopPath extends IpcRunTime {
    private String stopPathId;
    private String stopName;
    private Integer stopPathIndex;
    private Double prevStopSchDepartureTime;
    private Double currentStopSchDepartureTime;
    private  Double scheduledCompletionTime;
    private boolean isTimePoint;

    public IpcRunTimeForStopPath(String stopPathId,
                                 String stopName,
                                 Integer stopPathIndex,
                                 Double prevStopSchDepartureTime,
                                 Double currentStopSchDepartureTime,
                                 boolean isTimePoint,
                                 Double avgRunTime,
                                 Double fixed,
                                 Double variable,
                                 Double dwell){
        super(avgRunTime, fixed, variable, dwell);
        this.stopPathId = stopPathId;
        this.stopName = stopName;
        this.stopPathIndex = stopPathIndex;
        this.prevStopSchDepartureTime = prevStopSchDepartureTime;
        this.currentStopSchDepartureTime = currentStopSchDepartureTime;
        this.isTimePoint = isTimePoint;

        if(prevStopSchDepartureTime != null && currentStopSchDepartureTime != null) {
            double schTimeLengthMsec = (currentStopSchDepartureTime - prevStopSchDepartureTime) * 1000;
            this.scheduledCompletionTime = schTimeLengthMsec;
        }

    }

    public String getStopPathId() {
        return stopPathId;
    }

    public String getStopName() {
        return stopName;
    }

    public Integer getStopPathIndex() {
        return stopPathIndex;
    }

    public Double getPrevStopSchDepartureTime() {
        return prevStopSchDepartureTime;
    }

    public Double getCurrentStopSchDepartureTime() {
        return currentStopSchDepartureTime;
    }

    public Double getScheduledCompletionTime() {
        return scheduledCompletionTime;
    }

    public boolean isTimePoint() {
        return isTimePoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        IpcRunTimeForStopPath that = (IpcRunTimeForStopPath) o;
        return Objects.equal(stopPathId, that.stopPathId) &&
                Objects.equal(prevStopSchDepartureTime, that.prevStopSchDepartureTime) &&
                Objects.equal(currentStopSchDepartureTime, that.currentStopSchDepartureTime) &&
                Objects.equal(scheduledCompletionTime, that.scheduledCompletionTime);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), stopPathId, prevStopSchDepartureTime, currentStopSchDepartureTime, scheduledCompletionTime);
    }

    @Override
    public String toString() {
        return "IpcRunTimeForStopPath{" +
                "stopPathId='" + stopPathId + '\'' +
                ", prevStopSchDepartureTime=" + prevStopSchDepartureTime +
                ", currentStopSchDepartureTime=" + currentStopSchDepartureTime +
                ", scheduledCompletionTime=" + scheduledCompletionTime +
                ", avgRunTime=" + getAvgRunTime() +
                ", fixed=" + getFixed() +
                ", variable=" + getVariable() +
                ", dwell=" + getDwell() +
                '}';
    }
}
