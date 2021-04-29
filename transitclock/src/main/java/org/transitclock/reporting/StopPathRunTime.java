package org.transitclock.reporting;

import com.google.common.base.Objects;
import org.transitclock.db.structs.RunTimesForStops;

public class StopPathRunTime {

    private String stopPathId;

    private int stopPathIndex;

    private Boolean timePoint;

    private Long runTime;

    public StopPathRunTime(RunTimesForStops runTimesForStops) {
        this.stopPathId = runTimesForStops.getStopPathId();
        this.stopPathIndex = runTimesForStops.getStopPathIndex();
        this.timePoint = runTimesForStops.getTimePoint();
        this.runTime = runTimesForStops.getRunTime();
    }

    public StopPathRunTime(String stopPathId, int stopPathIndex, Boolean timePoint, Long runTime) {
        this.stopPathId = stopPathId;
        this.stopPathIndex = stopPathIndex;
        this.timePoint = timePoint;
        this.runTime = runTime;
    }

    public String getStopPathId() {
        return stopPathId;
    }

    public int getStopPathIndex() {
        return stopPathIndex;
    }

    public Boolean getTimePoint() {
        return timePoint;
    }

    public Long getRunTime() {
        return runTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StopPathRunTime that = (StopPathRunTime) o;
        return stopPathIndex == that.stopPathIndex &&
                Objects.equal(stopPathId, that.stopPathId) &&
                Objects.equal(timePoint, that.timePoint) &&
                Objects.equal(runTime, that.runTime);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(stopPathId, stopPathIndex, timePoint, runTime);
    }

    @Override
    public String toString() {
        return "StopPathRunTime{" +
                "stopPathId='" + stopPathId + '\'' +
                ", stopPathIndex=" + stopPathIndex +
                ", timePoint=" + timePoint +
                ", runTime=" + runTime +
                '}';
    }
}
