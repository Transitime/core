package org.transitclock.api.data.reporting.chartjs.custom;

import javax.xml.bind.annotation.XmlElement;

/**
 * Class to help serialize PieChart data into the correct format for Chart.js
 * Conforms to data documentation specified for Chart.js 2.x
 *
 * TODO: Should either be its own module or should use other external dependency
 *
 * @author carabalb
 *
 */
public class TripRunTimeMixedChart {

    @XmlElement
    private TripAggregatedRunTimeData data;

    public TripRunTimeMixedChart() {}

    public TripRunTimeMixedChart(TripAggregatedRunTimeData data) {
        this.data = data;
    }

    public TripAggregatedRunTimeData getData() {
        return data;
    }

    public void setData(TripAggregatedRunTimeData data) {
        this.data = data;
    }
}
