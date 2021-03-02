package org.transitclock.reporting.keys;

import com.google.common.base.Objects;

import java.time.LocalDate;

public class TripDateKey{
    private final String tripId;
    private final LocalDate date;

    public TripDateKey(String tripId, LocalDate date){
        this.tripId = tripId;
        this.date = date;
    }

    public String getTripId() {
        return tripId;
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TripDateKey that = (TripDateKey) o;
        return Objects.equal(tripId, that.tripId) &&
                Objects.equal(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tripId, date);
    }
}
