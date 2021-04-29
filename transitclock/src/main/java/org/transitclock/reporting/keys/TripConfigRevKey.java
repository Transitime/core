package org.transitclock.reporting.keys;

import com.google.common.base.Objects;

public class TripConfigRevKey {
    private final String tripId;
    private final Integer configRev;

    public TripConfigRevKey(String tripId, Integer configRev){
        this.tripId = tripId;
        this.configRev = configRev;
    }

    public String getTripId() {
        return tripId;
    }

    public Integer getConfigRev() {
        return configRev;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TripConfigRevKey that = (TripConfigRevKey) o;
        return Objects.equal(tripId, that.tripId) &&
                Objects.equal(configRev, that.configRev);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tripId, configRev);
    }
}
