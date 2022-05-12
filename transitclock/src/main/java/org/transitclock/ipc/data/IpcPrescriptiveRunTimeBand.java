package org.transitclock.ipc.data;

import org.apache.commons.collections.ListUtils;

import java.io.Serializable;
import java.util.List;

public class IpcPrescriptiveRunTimeBand implements Serializable {
    private final String startTime;
    private final String endTime;
    private final List<IpcPrescriptiveRunTime> runTimeByStopPathId;

    public IpcPrescriptiveRunTimeBand(String startTime,
                                      String endTime,
                                      List<IpcPrescriptiveRunTime> runTimeByStopPathId) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.runTimeByStopPathId = ListUtils.unmodifiableList(runTimeByStopPathId);
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public List<IpcPrescriptiveRunTime> getRunTimeByStopPathId() {
        return runTimeByStopPathId;
    }
}
