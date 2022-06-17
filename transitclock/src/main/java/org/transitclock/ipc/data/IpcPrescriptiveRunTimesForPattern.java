package org.transitclock.ipc.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Prescriptive RunTime Bands
 */
public class IpcPrescriptiveRunTimesForPattern implements Serializable {
    private String routeShortName;
    private List<IpcStopPath> timePoints;
    private List<IpcPrescriptiveRunTimesForTimeBand> runTimesForTimeBands;

    public IpcPrescriptiveRunTimesForPattern(List<IpcStopPath> timePoints,
                                             String routeShortName,
                                             IpcPrescriptiveRunTimesForTimeBands runTimesForTimeBands) {
        this.timePoints = timePoints;
        this.routeShortName = routeShortName;
        this.runTimesForTimeBands = runTimesForTimeBands.getRunTimesForTimeBands();
    }

    public List<IpcStopPath> getTimePoints() {
        return timePoints;
    }


    public List<IpcPrescriptiveRunTimesForTimeBand> getRunTimesForTimeBands() {
        return runTimesForTimeBands;
    }

    public String getRouteShortName() {
        return routeShortName;
    }

    public void setRouteShortName(String routeShortName) {
        this.routeShortName = routeShortName;
    }
}
