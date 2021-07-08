package org.transitclock.api.data.reporting.chartjs.custom;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class RouteRunTimeData {
    @XmlElement(name = "routes")
    private List<String> routesList = new ArrayList<>();

    @XmlElement(name = "early")
    private List<Long> earlyList = new ArrayList<>();

    @XmlElement(name = "onTime")
    private List<Long> onTimeList = new ArrayList<>();

    @XmlElement(name = "late")
    private List<Long> lateList = new ArrayList<>();

    public List<String> getRoutesList() {
        return routesList;
    }

    public List<Long> getEarlyList() {
        return earlyList;
    }

    public List<Long> getOnTimeList() {
        return onTimeList;
    }

    public List<Long> getLateList() {
        return lateList;
    }
}
