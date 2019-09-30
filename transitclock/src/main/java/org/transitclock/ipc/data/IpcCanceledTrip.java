package org.transitclock.ipc.data;

import java.io.Serializable;
import java.util.Objects;

public class IpcCanceledTrip implements Serializable {
    private String tripId;
    private String routeId;
    private String tripStartDate;
    private long timeStamp;



    public IpcCanceledTrip(String tripId, String routeId, String tripStartTime, long timeStamp){
        this.tripId = tripId;
        this.routeId = routeId;
        this.tripStartDate = tripStartTime;
        this.timeStamp = timeStamp;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getTripStartDate() {
        return tripStartDate;
    }

    public void setTripStartDate(String tripStartDate) {
        this.tripStartDate = tripStartDate;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IpcCanceledTrip that = (IpcCanceledTrip) o;
        return tripId.equals(that.tripId) &&
                Objects.equals(routeId, that.routeId) &&
                Objects.equals(tripStartDate, that.tripStartDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tripId, routeId, tripStartDate);
    }

    @Override
    public String toString() {
        return "IpcCanceledTrip{" +
                "tripId='" + tripId + '\'' +
                ", routeId='" + routeId + '\'' +
                ", tripStartDate='" + tripStartDate + '\'' +
                ", timeStamp=" + timeStamp +
                '}';
    }
}
