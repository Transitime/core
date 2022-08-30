package org.transitclock.integration_tests.playback;

import java.util.Comparator;

/**
 * Sort CombinedPredictionAccuracy objects by GTFS stop sequence.
 */
public class CombinedPredictionAccuracyStopSequenceComparator extends CombinedPredictionAccuracyBaseComparator {
    @Override
    public int compare(CombinedPredictionAccuracy o1, CombinedPredictionAccuracy o2) {
        return compare(o1.stopSeq, o2.stopSeq);
    }
}
