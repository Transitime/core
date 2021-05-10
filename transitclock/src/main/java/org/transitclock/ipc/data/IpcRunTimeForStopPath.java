package org.transitclock.ipc.data;

import com.google.common.base.Objects;

public class IpcRunTimeForStopPath extends IpcRunTime {
    private String stopPathId;
    private String stopName;
    private Integer stopPathIndex;
    private Integer prevStopSchDepartureTime;
    private Integer currentStopSchDepartureTime;
    private  Double scheduledCompletionTime;

    public IpcRunTimeForStopPath(String stopPathId,
                                 String stopName,
                                 Integer stopPathIndex,
                                 Integer prevStopSchDepartureTime,
                                 Integer currentStopSchDepartureTime,
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

    public Integer getPrevStopSchDepartureTime() {
        return prevStopSchDepartureTime;
    }

    public Integer getCurrentStopSchDepartureTime() {
        return currentStopSchDepartureTime;
    }

    public Double getScheduledCompletionTime() {
        return scheduledCompletionTime;
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
