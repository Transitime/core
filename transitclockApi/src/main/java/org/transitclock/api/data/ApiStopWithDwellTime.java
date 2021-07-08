package org.transitclock.api.data;

import org.transitclock.ipc.data.IpcStopWithDwellTime;

import javax.xml.bind.annotation.XmlAttribute;

public class ApiStopWithDwellTime extends ApiTransientLocation{
    @XmlAttribute(name = "stopId")
    private String stopId;

    @XmlAttribute(name = "stopName")
    private String stopName;

    @XmlAttribute(name = "stopCode")
    private Integer stopCode;

    @XmlAttribute(name = "avgDwellTime")
    private Double avgDwellTime;

    public ApiStopWithDwellTime(){}

    public ApiStopWithDwellTime(IpcStopWithDwellTime stopWithDwellTime, Double avgDwellTime){
        super(stopWithDwellTime.getLoc().getLat(), stopWithDwellTime.getLoc().getLon());
        this.stopId = stopWithDwellTime.getId();
        this.stopName = stopWithDwellTime.getName();
        this.stopCode = stopWithDwellTime.getCode();
        if(avgDwellTime != null){
            this.avgDwellTime = Math.rint(avgDwellTime/1000);
        } else {
            this.avgDwellTime = null;
        }
    }
}
