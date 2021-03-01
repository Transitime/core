package org.transitclock.api.data.reporting.chartjs.custom;

import javax.xml.bind.annotation.XmlElement;

public class StopPathRunTimeMixedChart {
    @XmlElement
    private StopPathRunTimeData data;

    public StopPathRunTimeMixedChart() {}

    public StopPathRunTimeMixedChart(StopPathRunTimeData data) {
        this.data = data;
    }

    public StopPathRunTimeData getData() {
        return data;
    }

    public void setData(StopPathRunTimeData data) {
        this.data = data;
    }
}
