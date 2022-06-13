package org.transitclock.reporting.service.runTime.prescriptive.timebands.kmeans;

public class BasicDistance implements Distance {
    @Override
    public Double calculate(Double v1, Double v2) {
        return Math.abs(v1 - v2);
    }
}
