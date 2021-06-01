package org.transitclock.ipc.data;

import java.io.Serializable;

public class IpcPrescriptiveRunTime implements Serializable {
    private final String stopPathId;
    private final String stopName;
    private final Integer stopPathIndex;
    private final double adjustment;
    private final double scheduled;

    public IpcPrescriptiveRunTime(String stopPathId, String stopName, Integer stopPathIndex,
                                  double adjustment, double scheduled){
        this.stopPathId = stopPathId;
        this.stopName = stopName;
        this.stopPathIndex = stopPathIndex;
        this.adjustment = adjustment;
        this.scheduled = scheduled;
    }

    public String getStopPathId() {
        return stopPathId;
    }

    public String getStopName() {
        return stopName;
    }

    public Integer getStopPathIndex() {
        return stopPathIndex;
    }

    public double getAdjustment() {
        return adjustment;
    }

    public double getScheduled() {
        return scheduled;
    }
}
