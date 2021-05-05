package org.transitclock.api.data;

import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.annotation.XmlAttribute;

public class ApiDispatcherData {
    @XmlAttribute(name = "vehicle")
    String vehicleId;

    @XmlAttribute(name = "last_report")
    String lastReportTime;

    @XmlAttribute(name = "block_id")
    String blockId;

    @XmlAttribute(name = "speed")
    String speed;

    @XmlAttribute(name = "route_assignment")
    String route;

    @XmlAttribute(name = "schedule_adherence")
    String scheduleAdherence;

    @XmlAttribute(name = "schedule_adherence_time_diff")
    Integer scheduleAdherenceTimeDiff;

    @XmlAttribute(name = "operator_id")
    String operatorId;

    @XmlAttribute(name = "assigned")
    boolean isAssigned;

    @XmlAttribute(name = "headway")
    Double headway;

    public ApiDispatcherData() {}

    public ApiDispatcherData(String vehicleId, String lastReportTime, String blockId, String speed,
                             String route, String scheduleAdherence, Integer scheduleAdherenceTimeDiff, String operatorId,
                             Double headway){
        this.vehicleId = vehicleId;
        this.lastReportTime = lastReportTime;
        this.blockId = blockId;
        this.speed = speed;
        this.route = route;
        this. scheduleAdherence = scheduleAdherence;
        this. scheduleAdherenceTimeDiff = scheduleAdherenceTimeDiff;
        this.operatorId = operatorId;
        if(StringUtils.isNotBlank(blockId)){
            isAssigned = true;
        }
        this.headway = headway;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getLastReportTime() {
        return lastReportTime;
    }

    public String getBlockId() {
        return blockId;
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

    public Integer getScheduleAdherenceTimeDiff() {
        return scheduleAdherenceTimeDiff;
    }

    public boolean isAssigned() {
        return isAssigned;
    }

    public Double getHeadway() {
        return headway;
    }
}
