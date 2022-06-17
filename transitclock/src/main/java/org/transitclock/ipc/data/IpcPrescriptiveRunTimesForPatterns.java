package org.transitclock.ipc.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Prescriptive RunTime Bands
 */
public class IpcPrescriptiveRunTimesForPatterns implements Serializable {
    private List<IpcPrescriptiveRunTimesForPattern> runTimesForPatterns = new ArrayList<>();
    private double currentOnTime = 0;
    private double expectedOnTime = 0;
    private double totalRunTimes = 0;

    public IpcPrescriptiveRunTimesForPatterns() {}

    public void addRunTimesForPatterns(IpcPrescriptiveRunTimesForPattern runTimesForPattern) {
        runTimesForPatterns.add(runTimesForPattern);
    }

    public void addCurrentOnTime(double currentOnTime) {
        this.currentOnTime += currentOnTime;
    }

    public void addExpectedOnTime(double expectedOnTime) {
        this.expectedOnTime += expectedOnTime;
    }

    public void addTotalRunTime(double totalRunTimes) {
        this.totalRunTimes += totalRunTimes;
    }

    public List<IpcPrescriptiveRunTimesForPattern> getRunTimesForPatterns() {
        return runTimesForPatterns;
    }

    public double getCurrentOnTime() {
        return currentOnTime;
    }

    public double getExpectedOnTime() {
        return expectedOnTime;
    }

    public double getTotalRunTimes() {
        return totalRunTimes;
    }
}
