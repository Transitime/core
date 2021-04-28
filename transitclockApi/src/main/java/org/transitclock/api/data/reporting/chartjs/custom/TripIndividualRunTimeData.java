package org.transitclock.api.data.reporting.chartjs.custom;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

public class TripIndividualRunTimeData {

    @XmlElement(name = "runTimes")
    List<Long> runTimes;

    public TripIndividualRunTimeData() {
    }

    public TripIndividualRunTimeData(List<Long> runTimes) {
        this.runTimes = runTimes;
    }

    public List<Long> getRunTimes() {
        return runTimes;
    }

    public void setRunTimes(List<Long> runTimes) {
        this.runTimes = runTimes;
    }
}
