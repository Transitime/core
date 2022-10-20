package org.transitclock.integration_tests.playback;

import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Prediction;

import java.util.Date;

/**
 * CombinedPredictionAccuracy: keep track of stop, old prediction, new prediction.
 * Arrival/Departure Key: key by gtfsStopSeq & whether arrival or departure
 */
public class CombinedPredictionAccuracy {
    public void setOldPrediction(Prediction p) {
        oldPrediction = p;
        if (p!= null && p.getPredictionTime() != null) {
            oldPredTime = p.getPredictionTime().getTime();
            validatePrediction(p);
        } else
            oldPredTime = -1;
    }

    public void setNewPrediction(Prediction p) {
        newPrediction = p;
        if (p!= null && p.getPredictionTime() != null) {
            newPredTime = p.getPredictionTime().getTime();
            validatePrediction(p);
        }
        else
            newPredTime = -1;
    }

    private void validatePrediction(Prediction p) {
        if (p.getGtfsStopSeq() != this.stopSeq)
            throw new IllegalStateException("unmatched stopSeq " + p.getGtfsStopSeq() + ", " + this.stopSeq);
        if (!p.getTripId().equals(this.tripId))
            throw new IllegalStateException("unmatched tripId " + p.getTripId() + ", " + this.tripId);
        if (p.getAvlTime().getTime() != this.avlTime)
            throw new IllegalStateException("unmatched avlTime " + p.getAvlTime() + ", " + new Date(this.avlTime));
        if (p.getPredictionTime() == null)
            throw new IllegalStateException("empty prediction " + p);
    }

    public enum ArrivalOrDeparture {ARRIVAL, DEPARTURE};

    public int stopSeq;
    public ArrivalOrDeparture which;
    public long avlTime;

    public long actualADTime = -1;
    public long predLength = -1; // actualADTime - avlTime

    public long oldPredTime = -1;
    public Prediction oldPrediction = null;
    public long newPredTime = -1;
    public Prediction newPrediction = null;

    public String tripId = null;

    @Override
    public String toString() {
        return "tripId=" + tripId + ", "
                + "stopSeq=" + stopSeq + ", "
                + "isArrival=" + (which==ArrivalOrDeparture.ARRIVAL?"true":"false") + ", "
                + "avlTime=" + new Date(avlTime) + ", "
                + "oldPred=" + oldPredTime + ", "
                + "newPred=" + newPredTime + ", "
                + "acutal=" + actualADTime;
    }

    public CombinedPredictionAccuracy(String tripId, int stopSeq, ArrivalOrDeparture which, long avlTime) {
        this.tripId = tripId;
        this.stopSeq = stopSeq;
        this.which = which;
        this.avlTime = avlTime;
    }

    public CombinedPredictionAccuracy(ArrivalDeparture ad) {
        this(ad.getTripId(),
                ad.getGtfsStopSequence(),
                ad.isArrival() ? ArrivalOrDeparture.ARRIVAL : ArrivalOrDeparture.DEPARTURE,
                ad.getAvlTime().getTime());
        this.actualADTime = ad.getTime();
        this.predLength = actualADTime - avlTime;
    }

}
