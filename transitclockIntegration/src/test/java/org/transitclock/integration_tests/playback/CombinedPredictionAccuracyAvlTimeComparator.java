package org.transitclock.integration_tests.playback;

/**
 * Sort CombinedPredictionAccuracy Objects by avlTime.
 */
public class CombinedPredictionAccuracyAvlTimeComparator extends CombinedPredictionAccuracyBaseComparator {
    @Override
    public int compare(CombinedPredictionAccuracy o1, CombinedPredictionAccuracy o2) {
        return compare(o1.avlTime, o2.avlTime);
    }
}
