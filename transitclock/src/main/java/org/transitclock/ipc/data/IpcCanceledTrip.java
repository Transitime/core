package org.transitclock.ipc.data;

import java.io.Serializable;
import java.util.Objects;

public class IpcCanceledTrip implements Serializable {

    private static final long serialVersionUID = 1L;

    private String tripId;
    private String routeId;
    private String tripStartDate;
    private long timeStamp;
    private float latitude;
    private float longitude;

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

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IpcCanceledTrip that = (IpcCanceledTrip) o;
        return timeStamp == that.timeStamp &&
                Float.compare(that.latitude, latitude) == 0 &&
                Float.compare(that.longitude, longitude) == 0 &&
                Objects.equals(tripId, that.tripId) &&
                Objects.equals(routeId, that.routeId) &&
                Objects.equals(tripStartDate, that.tripStartDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tripId, routeId, tripStartDate, timeStamp, latitude, longitude);
    }

    @Override
    public String toString() {
        return "IpcCanceledTrip{" +
                "tripId='" + tripId + '\'' +
                ", routeId='" + routeId + '\'' +
                ", tripStartDate='" + tripStartDate + '\'' +
                ", timeStamp=" + timeStamp +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
