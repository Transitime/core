package org.transitclock.api.data.reporting.chartjs.pie;

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
public class PieChart {

    public PieChart() {}

    @XmlElement
    private PieChartData data;

    public PieChartData getData() {
        return data;
    }

    public void setData(PieChartData data) {
        this.data = data;
    }
}
