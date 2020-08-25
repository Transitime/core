package org.transitclock.api.data.reporting.chartjs.custom;

import javax.ws.rs.core.GenericType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

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
    private TripRunTimeData data;

    public TripRunTimeMixedChart() {}

    public TripRunTimeMixedChart(TripRunTimeData data) {
        this.data = data;
    }

    public TripRunTimeData getData() {
        return data;
    }

    public void setData(TripRunTimeData data) {
        this.data = data;
    }
}
