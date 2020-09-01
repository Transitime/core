package org.transitclock.api.data.reporting.chartjs.custom;

import javax.xml.bind.annotation.XmlElement;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TripRunTimeData {
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

}
