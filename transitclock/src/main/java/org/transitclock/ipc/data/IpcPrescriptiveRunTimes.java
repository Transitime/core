package org.transitclock.ipc.data;

import java.io.Serializable;
import java.util.List;

public class IpcPrescriptiveRunTimes implements Serializable {
    List<IpcPrescriptiveRunTime> prescriptiveRunTimes;
    private Double currentOtp;
    private Double expectedOtp;

    public IpcPrescriptiveRunTimes(List<IpcPrescriptiveRunTime> prescriptiveRunTimes, Double currentOtp, Double expectedOtp) {
        this.prescriptiveRunTimes = prescriptiveRunTimes;
        this.currentOtp = currentOtp;
        this.expectedOtp = expectedOtp;
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
}
