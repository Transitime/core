package org.transitclock.api.data.reporting.chartjs.custom;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class StopPathRunTimeData {
    @XmlElement(name = "stopPaths")
    private List<String> stopPathsList = new ArrayList<>();

    @XmlElement(name = "stopNames")
    private List<String> stopNames = new ArrayList<>();

    @XmlElement(name = "fixed")
    private List<Long> fixedList = new ArrayList<>();

    @XmlElement(name = "variable")
    private List<Long> variableList = new ArrayList<>();

    @XmlElement(name = "dwell")
    private List<Long> dwellList = new ArrayList<>();

    @XmlElement(name = "scheduled")
    private List<Long> scheduledList = new ArrayList<>();

    public List<String> getStopPathsList() {
        return stopPathsList;
    }

    public List<String> getStopNames() {
        return stopNames;
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

}
