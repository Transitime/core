package org.transitclock.ipc.data;

import com.google.common.base.Objects;

public class IpcRunTimeForTrip extends IpcRunTime {
    private final String tripId;
    private Integer scheduledTripStartTime;
    private  Double scheduledTripCompletionTime;
    private  Double nextScheduledTripStartTime;

    public IpcRunTimeForTrip(String tripId, Integer tripSchStartTime, Integer tripSchEndTime, Integer nextTripSchStartTime,
                             Double avgRunTime, Double fixed, Double variable, Double dwell){
        super(avgRunTime, fixed, variable, dwell);
        this.tripId = tripId;
        this.scheduledTripStartTime = tripSchEndTime;

        if(tripSchStartTime != null && tripSchEndTime != null) {
            double tripSchTimeLengthMsec = (tripSchEndTime - tripSchStartTime) * 1000;
            this.scheduledTripCompletionTime = tripSchTimeLengthMsec;
        }

        if(tripSchStartTime != null && nextTripSchStartTime != null) {
            double nextTripSchTripLengthMsec = (nextTripSchStartTime - tripSchStartTime) * 1000;
            this.nextScheduledTripStartTime = nextTripSchTripLengthMsec;
        }
    }

    public String getTripId() {
        return tripId;
    }

    public Integer getScheduledTripStartTime() {
        return scheduledTripStartTime;
    }

    public double getScheduledTripCompletionTime() {
        return scheduledTripCompletionTime;
    }

    public double getNextScheduledTripStartTime() {
        return nextScheduledTripStartTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        IpcRunTimeForTrip that = (IpcRunTimeForTrip) o;
        return Objects.equal(tripId, that.tripId) &&
                Objects.equal(scheduledTripStartTime, that.scheduledTripStartTime) &&
                Objects.equal(scheduledTripCompletionTime, that.scheduledTripCompletionTime) &&
                Objects.equal(nextScheduledTripStartTime, that.nextScheduledTripStartTime);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), tripId, scheduledTripStartTime, scheduledTripCompletionTime, nextScheduledTripStartTime);
    }

    @Override
    public String toString() {
        return "IpcRunTimeForTrip{" +
                "tripId=" + tripId +
                ", scheduledTripStartTime=" + scheduledTripStartTime +
                ", scheduledTripCompletionTime=" + scheduledTripCompletionTime +
                ", nextScheduledTripStartTime=" + nextScheduledTripStartTime +
                ", avgRunTime=" + getAvgRunTime() +
                ", fixed=" + getFixed() +
                ", variable=" + getVariable() +
                ", dwell=" + getDwell() +
                '}';
    }
}
