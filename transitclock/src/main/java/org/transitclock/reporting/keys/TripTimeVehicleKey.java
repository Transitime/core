package org.transitclock.reporting.keys;

import com.google.common.base.Objects;

public class TripTimeVehicleKey {

    private final String tripId;
    private final Long tripStartTime;
    private final String vehicleId;

    public TripTimeVehicleKey(String tripId, Long tripStartTime, String vehicleId){
        this.tripId = tripId;
        this.tripStartTime = tripStartTime;
        this.vehicleId = vehicleId;
    }

    public String getTripId() {
        return tripId;
    }

    public Long getTripStartTime() {
        return tripStartTime;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TripTimeVehicleKey that = (TripTimeVehicleKey) o;
        return Objects.equal(tripId, that.tripId) &&
                Objects.equal(tripStartTime, that.tripStartTime) &&
                Objects.equal(vehicleId, that.vehicleId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tripId, tripStartTime, vehicleId);
    }

    @Override
    public String toString() {
        return "TripTimeVehicleKey{" +
                "tripId='" + tripId + '\'' +
                ", tripStartTime=" + tripStartTime +
                ", vehicleId='" + vehicleId + '\'' +
                '}';
    }
}
