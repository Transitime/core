package org.transitclock.api.data.reporting.prescriptive;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class PrescriptiveRunTimeDataAll {

    @XmlElement(name = "data")
    private List<PrescriptiveRunTimeDataForPattern> dataForPatterns = new ArrayList<>();

    @XmlElement
    private String routeShortName;

    @XmlElement
    private String currentOnTime;

    @XmlElement
    private String expectedOnTime;

    public PrescriptiveRunTimeDataAll() {
    }

    public List<PrescriptiveRunTimeDataForPattern> getDataForPatterns() {
        return dataForPatterns;
    }

    public String getRouteShortName() {
        return routeShortName;
    }

    public void setRouteShortName(String routeShortName) {
        this.routeShortName = routeShortName;
    }

    public String getCurrentOnTime() {
        return currentOnTime;
    }

    public void setCurrentOnTime(String currentOnTime) {
        this.currentOnTime = currentOnTime;
    }

    public String getExpectedOnTime() {
        return expectedOnTime;
    }

    public void setExpectedOnTime(String expectedOnTime) {
        this.expectedOnTime = expectedOnTime;
    }
}
