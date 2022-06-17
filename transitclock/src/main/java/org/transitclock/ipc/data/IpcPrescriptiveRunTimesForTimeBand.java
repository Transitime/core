package org.transitclock.ipc.data;

import org.apache.commons.collections.ListUtils;

import java.io.Serializable;
import java.util.List;

public class IpcPrescriptiveRunTimesForTimeBand implements Serializable {
    private final String startTime;
    private final String endTime;
    private final double currentOtp;
    private final double expectedOtp;
    private final List<IpcPrescriptiveRunTime> runTimeByStopPathId;

    public IpcPrescriptiveRunTimesForTimeBand(String startTime,
                                              String endTime,
                                              double currentOtp,
                                              double expectedOtp,
                                              List<IpcPrescriptiveRunTime> runTimeByStopPathId) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.currentOtp = currentOtp;
        this.expectedOtp = expectedOtp;
        this.runTimeByStopPathId = ListUtils.unmodifiableList(runTimeByStopPathId);
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public double getCurrentOtp() {
        return currentOtp;
    }

    public double getExpectedOtp() {
        return expectedOtp;
    }

    public List<IpcPrescriptiveRunTime> getRunTimeByStopPathId() {
        return runTimeByStopPathId;
    }

}
