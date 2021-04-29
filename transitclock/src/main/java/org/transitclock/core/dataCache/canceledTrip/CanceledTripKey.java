package org.transitclock.core.dataCache.canceledTrip;

import java.io.Serializable;
import java.util.Objects;

public class CanceledTripKey implements Serializable {

    private final String vehicleId;
    private final String tripId;


    // Since hashCode() can be called a lot might as well cache the value
    // since this object is immutable.
    private int cachedHashCode;

    public CanceledTripKey(String vehicleId, String tripId) {
        this.vehicleId = vehicleId;
        this.tripId = tripId;
        this.cachedHashCode = createHashCode();
    }

    public String getVehicleId(){
       return vehicleId;
    }

    public String getTripId(){
        return tripId;
    }

    @Override
    public String toString() {
        return "CanceledTripKey [" + "vehicleId=" + vehicleId
                + ", tripId=" + tripId + "]";
    }


    /**
     * Using cached hash code for speed. Therefore this method is used by
     * constructors to initialize the cached hash code.
     *
     * @return hash code to be cached
     */
    private int createHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((vehicleId == null) ? 0 : vehicleId.hashCode());
        result = prime * result + ((tripId == null) ? 0 : tripId.hashCode());
        return result;
    }

    /**
     * @return the cached hash code
     */
    @Override
    public int hashCode() {
        return cachedHashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CanceledTripKey that = (CanceledTripKey) o;
        return Objects.equals(vehicleId, that.vehicleId) &&
                Objects.equals(tripId, that.tripId);
    }
}