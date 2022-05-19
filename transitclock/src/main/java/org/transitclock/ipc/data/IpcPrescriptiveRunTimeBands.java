package org.transitclock.ipc.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Prescriptive RunTime Bands
 */
public class IpcPrescriptiveRunTimeBands implements Serializable {
    private List<IpcStopPath> timePoints;
    private List<IpcPrescriptiveRunTimeBand> timeBands = new ArrayList<>();
    private Double currentOtp;
    private Double expectedOtp;

    public IpcPrescriptiveRunTimeBands(List<IpcStopPath> timePoints) {
        this.timePoints = timePoints;
    }

    public List<IpcStopPath> getTimePoints() {
        return timePoints;
    }

    public void setTimePoints(List<IpcStopPath> timePoints) {
        this.timePoints = timePoints;
    }

    public List<IpcPrescriptiveRunTimeBand> getTimeBands() {
        return timeBands;
    }

    public void setTimeBands(List<IpcPrescriptiveRunTimeBand> timeBands) {
        this.timeBands = timeBands;
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
}
