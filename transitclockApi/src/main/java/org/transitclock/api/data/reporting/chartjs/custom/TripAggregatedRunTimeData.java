package org.transitclock.api.data.reporting.chartjs.custom;

import org.transitclock.api.data.ApiRunTimeSummary;
import org.transitclock.ipc.data.IpcRunTime;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class TripAggregatedRunTimeData {

    public TripAggregatedRunTimeData() { }

    @XmlElement(name = "trips")
    private List<String> tripsList = new ArrayList<>();

    @XmlElement(name = "fixed")
    private List<Long> fixedList = new ArrayList<>();

    @XmlElement(name = "variable")
    private List<Long> variableList = new ArrayList<>();

    @XmlElement(name = "dwell")
    private List<Long> dwellList = new ArrayList<>();

    @XmlElement(name = "scheduled")
    private List<Long> scheduledList = new ArrayList<>();

    @XmlElement(name = "nextTripStart")
    private List<Long> nextTripStartList = new ArrayList<>();

    @XmlElement(name = "tripRunTimes")
    private List<TripIndividualRunTimeData> tripRunTimes = new ArrayList<>();

    @XmlElement(name = "summary")
    private ApiRunTimeSummary summary;

    public List<String> getTripsList() {
        return tripsList;
    }

    public List<Long> getFixedList() {
        return fixedList;
    }

    public List<Long> getVariableList() {
        return variableList;
    }

    public List<Long> getDwellList() {
        return dwellList;
    }

    public List<Long> getScheduledList() {
        return scheduledList;
    }

    public List<Long> getNextTripStartList() {
        return nextTripStartList;
    }

    public List<TripIndividualRunTimeData> getTripRunTimes() {
        return tripRunTimes;
    }

    public ApiRunTimeSummary getSummary() {
        return summary;
    }

    public void setSummary(IpcRunTime summary) {
        this.summary = new ApiRunTimeSummary(summary);
    }
}
