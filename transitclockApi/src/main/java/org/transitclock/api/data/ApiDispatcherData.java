package org.transitclock.api.data;

import javax.xml.bind.annotation.XmlAttribute;

public class ApiDispatcherData {
    @XmlAttribute(name = "vehicle")
    String vehicleId;

    @XmlAttribute(name = "last_report")
    String lastReportTime;

    @XmlAttribute(name = "heading")
    String heading;

    @XmlAttribute(name = "speed")
    String speed;

    @XmlAttribute(name = "route_assignment")
    String route;

    @XmlAttribute(name = "schedule_adherence")
    String scheduleAdherence;

    @XmlAttribute(name = "operator_id")
    String operatorId;

    public ApiDispatcherData() {}

    public ApiDispatcherData(String vehicleId, String lastReportTime, String heading, String speed,
                             String route, String scheduleAdherence, String operatorId){
        this.vehicleId = vehicleId;
        this.lastReportTime = lastReportTime;
        this.heading = heading;
        this.speed = speed;
        this.route = route;
        this. scheduleAdherence = scheduleAdherence;
        this.operatorId = operatorId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getLastReportTime() {
        return lastReportTime;
    }

    public String getHeading() {
        return heading;
    }

    public String getSpeed() {
        return speed;
    }

    public String getRoute() {
        return route;
    }

    public String getScheduleAdherence() {
        return scheduleAdherence;
    }

    public String getOperatorId() {
        return operatorId;
    }
}
