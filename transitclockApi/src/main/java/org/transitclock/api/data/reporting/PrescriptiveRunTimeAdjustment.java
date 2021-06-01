package org.transitclock.api.data.reporting;

import javax.xml.bind.annotation.XmlElement;


public class PrescriptiveRunTimeAdjustment {
    @XmlElement(name = "stop")
    private String stopName;

    @XmlElement(name = "schedule")
    private Long schedule;

    @XmlElement(name = "adjustment")
    private Long adjustment;

    public PrescriptiveRunTimeAdjustment() { }

    public PrescriptiveRunTimeAdjustment(String stopName, Long schedule, Long adjustment) {
        this.stopName = stopName;
        this.schedule = schedule;
        this.adjustment = adjustment;
    }

    public String getStopName() {
        return stopName;
    }

    public Long getSchedule() {
        return schedule;
    }

    public Long getAdjustment() {
        return adjustment;
    }
}
