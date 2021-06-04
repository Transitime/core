package org.transitclock.ipc.data;

import java.io.Serializable;
import java.util.List;

public class IpcPrescriptiveRunTimes implements Serializable {
    List<IpcPrescriptiveRunTime> prescriptiveRunTimes;
    private Double currentOtp;
    private Double expectedOtp;
    private IpcRunTime runTimeSummary;

    public IpcPrescriptiveRunTimes(List<IpcPrescriptiveRunTime> prescriptiveRunTimes, Double currentOtp,
                                   Double expectedOtp, IpcRunTime runTimeSummary) {
        this.prescriptiveRunTimes = prescriptiveRunTimes;
        this.currentOtp = currentOtp;
        this.expectedOtp = expectedOtp;
        this.runTimeSummary = runTimeSummary;
    }

    public List<IpcPrescriptiveRunTime> getPrescriptiveRunTimes() {
        return prescriptiveRunTimes;
    }

    public Double getCurrentOtp() {
        return currentOtp;
    }

    public Double getExpectedOtp() {
        return expectedOtp;
    }

    public IpcRunTime getRunTimeSummary() {
        return runTimeSummary;
    }
}
