package org.transitclock.integration_tests.playback;

/**
 * Sort CombinedPredictionAccuracy objects by tripId.
 */
public class CombinedPredictionAccuracyTripIdComparator extends CombinedPredictionAccuracyBaseComparator {
    @Override
    public int compare(CombinedPredictionAccuracy o1, CombinedPredictionAccuracy o2) {
        return o1.tripId.compareTo(o2.tripId);
    }
}
