package org.transitclock.ipc.data;

import org.apache.commons.collections.ListUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class IpcPrescriptiveRunTimesForTimeBands implements Serializable {
    private double currentOnTime = 0;
    private double expectedOnTime = 0;
    private double totalRunTimes = 0;
    private List<IpcPrescriptiveRunTimesForTimeBand> runTimesForTimeBands = new ArrayList<>();

    public IpcPrescriptiveRunTimesForTimeBands() {}

    public double getCurrentOnTime() {
        return currentOnTime;
    }

    public double getExpectedOnTime() {
        return expectedOnTime;
    }

    public double getTotalRunTimes() {
        return totalRunTimes;
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

    public List<IpcPrescriptiveRunTimesForTimeBand> getRunTimesForTimeBands() {
        return runTimesForTimeBands;
    }

    public void addRunTimesForTimeBands(IpcPrescriptiveRunTimesForTimeBand runTimesForTimeBand) {
        runTimesForTimeBands.add(runTimesForTimeBand);
    }
}
