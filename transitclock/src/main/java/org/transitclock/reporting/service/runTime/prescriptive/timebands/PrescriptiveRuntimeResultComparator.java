package org.transitclock.reporting.service.runTime.prescriptive.timebands;

import org.transitclock.reporting.service.runTime.prescriptive.timebands.model.PrescriptiveRuntimeResult;

import java.util.Comparator;

public class PrescriptiveRuntimeResultComparator implements Comparator<PrescriptiveRuntimeResult> {

    @Override
    public int compare(PrescriptiveRuntimeResult o1, PrescriptiveRuntimeResult o2) {
        if (o1 == null || o2 == null) throw new IllegalArgumentException("arguments cannot be null");
        return o1.getSortHash().compareTo(o2.getSortHash());
    }
}