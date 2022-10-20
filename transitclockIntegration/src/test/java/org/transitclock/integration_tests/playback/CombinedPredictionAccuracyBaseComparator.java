package org.transitclock.integration_tests.playback;

import java.util.Comparator;

/**
 * Base class Comparator.
 */
public abstract class CombinedPredictionAccuracyBaseComparator implements Comparator<CombinedPredictionAccuracy> {
    protected int compare(long a1, long a2) {
        return new Long(a1).compareTo(a2);
    }

    protected int compare(int a1, int a2) {
        return new Integer(a1).compareTo(a2);
    }

    @Override
    public abstract int compare(CombinedPredictionAccuracy o1, CombinedPredictionAccuracy o2);

}
