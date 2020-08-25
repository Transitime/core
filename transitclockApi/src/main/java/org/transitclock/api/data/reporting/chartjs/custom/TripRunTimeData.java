package org.transitclock.api.data.reporting.chartjs.custom;

import javax.xml.bind.annotation.XmlElement;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TripRunTimeData {
    @XmlElement(name = "trips")
    private List<String> tripsList = new ArrayList<>();

    @XmlElement(name = "fixed")
    private List<BigDecimal> fixedList = new ArrayList<>();

    @XmlElement(name = "variable")
    private List<BigDecimal> variableList = new ArrayList<>();

    @XmlElement(name = "dwell")
    private List<BigDecimal> dwellList = new ArrayList<>();

    @XmlElement(name = "scheduled")
    private List<BigDecimal> scheduledList = new ArrayList<>();

    @XmlElement(name = "nextTripStart")
    private List<BigDecimal> nextTripStartList = new ArrayList<>();

    public List<String> getTripsList() {
        return tripsList;
    }

    public List<BigDecimal> getFixedList() {
        return fixedList;
    }

    public List<BigDecimal> getVariableList() {
        return variableList;
    }

    public List<BigDecimal> getDwellList() {
        return dwellList;
    }

    public List<BigDecimal> getScheduledList() {
        return scheduledList;
    }

    public List<BigDecimal> getNextTripStartList() {
        return nextTripStartList;
    }

}
