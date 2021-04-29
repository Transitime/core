package org.transitclock.ipc.data;

import com.google.common.base.Objects;

import java.io.Serializable;

public class IpcCanceledTrip implements Serializable {

    private static final long serialVersionUID = 1L;

    private String tripId;
    private String routeId;
    private String tripStartDate;
    private String vehicleId;
    private Long timeStamp;

    public IpcCanceledTrip(String tripId, String routeId, String vehicleId, String tripStartDate, Long timeStamp){
        this.tripId = tripId;
        this.routeId = routeId;
        this.vehicleId = vehicleId;
        this.tripStartDate = tripStartDate;
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

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IpcCanceledTrip that = (IpcCanceledTrip) o;
        return Objects.equal(tripId, that.tripId) &&
                Objects.equal(routeId, that.routeId) &&
                Objects.equal(tripStartDate, that.tripStartDate) &&
                Objects.equal(vehicleId, that.vehicleId) &&
                Objects.equal(timeStamp, that.timeStamp);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tripId, routeId, tripStartDate, vehicleId, timeStamp);
    }

    @Override
    public String toString() {
        return "IpcCanceledTrip{" +
                "tripId='" + tripId + '\'' +
                ", routeId='" + routeId + '\'' +
                ", tripStartDate='" + tripStartDate + '\'' +
                ", vehicleId='" + vehicleId + '\'' +
                ", timeStamp=" + timeStamp +
                '}';
    }
}
