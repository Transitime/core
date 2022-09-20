package org.transitclock.integration_tests.playback;

import java.util.Objects;

/**
 * Key for prediction comparison in integration tests.
 */
public class PredictionKey {
    private String tripId;
    private Integer stopSequence;
    private CombinedPredictionAccuracy.ArrivalOrDeparture arrivalOrDeparture;
    private Long avlTime;

    public PredictionKey(String tripId, Integer stopSequence,
                         CombinedPredictionAccuracy.ArrivalOrDeparture arrivalOrDeparture,
                         Long avlTime) {
        this.tripId = tripId;
        this.stopSequence = stopSequence;
        this.arrivalOrDeparture = arrivalOrDeparture;
        this.avlTime = avlTime;
    }

    public String getTripId() {
        return tripId;
    }

    public Integer getStopSequence() {
        return stopSequence;
    }

    public CombinedPredictionAccuracy.ArrivalOrDeparture getArrivalOrDeparture() {
        return arrivalOrDeparture;
    }

    public Long getAvlTime() {
        return avlTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tripId, stopSequence, arrivalOrDeparture, avlTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PredictionKey that = (PredictionKey) o;
        return Objects.equals(tripId, that.tripId) &&
                Objects.equals(stopSequence, that.stopSequence) &&
                Objects.equals(arrivalOrDeparture, that.arrivalOrDeparture) &&
                Objects.equals(avlTime, that.avlTime);
    }
}
