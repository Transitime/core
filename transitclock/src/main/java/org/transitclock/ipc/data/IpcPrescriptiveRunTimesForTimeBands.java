package org.transitclock.ipc.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Prescriptive RunTime Bands
 */
public class IpcPrescriptiveRunTimesForTimeBands implements Serializable {
    private String routeShortName;
    private List<IpcStopPath> timePoints;
    private List<IpcPrescriptiveRunTimesForTimeBand> runTimesForTimeBands = new ArrayList<>();
    private Double currentOtp;
    private Double expectedOtp;

    public IpcPrescriptiveRunTimesForTimeBands(List<IpcStopPath> timePoints) {
        this.timePoints = timePoints;
    }

    public List<IpcStopPath> getTimePoints() {
        return timePoints;
    }

    public void setTimePoints(List<IpcStopPath> timePoints) {
        this.timePoints = timePoints;
    }

    public List<IpcPrescriptiveRunTimesForTimeBand> getRunTimesForTimeBands() {
        return runTimesForTimeBands;
    }

    public void setRunTimesForTimeBands(List<IpcPrescriptiveRunTimesForTimeBand> runTimesForTimeBands) {
        this.runTimesForTimeBands = runTimesForTimeBands;
    }

    public Double getCurrentOtp() {
        return currentOtp;
    }

    public void setCurrentOtp(Double currentOtp) {
        this.currentOtp = currentOtp;
    }

    public Double getExpectedOtp() {
        return expectedOtp;
    }

    public void setExpectedOtp(Double expectedOtp) {
        this.expectedOtp = expectedOtp;
    }

    public String getRouteShortName() {
        return routeShortName;
    }

    public void setRouteShortName(String routeShortName) {
        this.routeShortName = routeShortName;
    }
}
