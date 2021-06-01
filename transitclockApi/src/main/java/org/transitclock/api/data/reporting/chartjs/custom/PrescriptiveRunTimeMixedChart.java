package org.transitclock.api.data.reporting.chartjs.custom;

import org.transitclock.api.data.reporting.PrescriptiveRunTimeData;

import javax.xml.bind.annotation.XmlElement;

public class PrescriptiveRunTimeMixedChart {
    @XmlElement
    private PrescriptiveRunTimeData data;

    public PrescriptiveRunTimeMixedChart() {}

    public PrescriptiveRunTimeMixedChart(PrescriptiveRunTimeData data) {
        this.data = data;
    }

    public PrescriptiveRunTimeData getData() {
        return data;
    }

    public void setData(PrescriptiveRunTimeData data) {
        this.data = data;
    }
}
