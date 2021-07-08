package org.transitclock.api.data.reporting.chartjs.custom;

import javax.xml.bind.annotation.XmlElement;

public class RouteRunTimeMixedChart {
    @XmlElement
    private RouteRunTimeData data;

    public RouteRunTimeMixedChart() {}

    public RouteRunTimeMixedChart(RouteRunTimeData data) {
        this.data = data;
    }

    public RouteRunTimeData getData() {
        return data;
    }

    public void setData(RouteRunTimeData data) {
        this.data = data;
    }
}
