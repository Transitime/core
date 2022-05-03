package org.transitclock.reporting.service.runTime.prescriptive.timebands.model;

import com.google.common.base.Objects;
import org.transitclock.core.ServiceType;
import org.transitclock.db.structs.RunTimesForRoutes;

import java.util.Date;

public class RunTimeData {

    private Date startTime;

    private String tripId;

    private String headsign;

    private String tripPatternId;

    private Integer scheduledStartTime;

    private ServiceType serviceType;

    private Double expectedRuntime;

    private Double actualRuntime;

    private String routeShortName;

    private Integer configRev;

    private Integer sequence;//calculated

    public RunTimeData(){}

    public RunTimeData(RunTimesForRoutes runTimesForRoutes){
        if(runTimesForRoutes.hasAllScheduledAndActualTimes()){
            this.setActualRuntime(runTimesForRoutes.getRunTime().doubleValue());
            this.setExpectedRuntime(runTimesForRoutes.getScheduledRunTime().doubleValue());
        }
        this.setConfigRev(runTimesForRoutes.getConfigRev());
        this.setHeadsign(runTimesForRoutes.getHeadsign());
        this.setRouteShortName(runTimesForRoutes.getRouteShortName());
        this.setScheduledStartTime(runTimesForRoutes.getScheduledStartTime());
        this.setServiceType(runTimesForRoutes.getServiceType());
        this.setStartTime(runTimesForRoutes.getStartTime());
        this.setTripId(runTimesForRoutes.getTripId());
        this.setTripPatternId(runTimesForRoutes.getTripPatternId());
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getHeadsign() {
        return headsign;
    }

    public void setHeadsign(String headsign) {
        this.headsign = headsign;
    }

    public String getTripPatternId() {
        return tripPatternId;
    }

    public void setTripPatternId(String tripPatternId) {
        this.tripPatternId = tripPatternId;
    }

    public Integer getScheduledStartTime() {
        return scheduledStartTime;
    }

    public void setScheduledStartTime(Integer scheduledStartTime) {
        this.scheduledStartTime = scheduledStartTime;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public Double getExpectedRuntime() {
        return expectedRuntime;
    }

    public void setExpectedRuntime(Double expectedRuntime) {
        this.expectedRuntime = expectedRuntime;
    }

    public Double getActualRuntime() {
        return actualRuntime;
    }

    public void setActualRuntime(Double actualRuntime) {
        this.actualRuntime = actualRuntime;
    }

    public Integer getSequence() { return sequence; }

    public void setSequence(Integer sequence) { this.sequence = sequence; }

    public String getRouteShortName() {
        return routeShortName;
    }

    public void setRouteShortName(String routeShortName) {
        this.routeShortName = routeShortName;
    }

    public Integer getConfigRev() {
        return configRev;
    }

    public void setConfigRev(Integer configRev) {
        this.configRev = configRev;
    }


    @Override
    public String toString() {
        return "RunTimeData{" +
                "startTime=" + startTime +
                ", tripId='" + tripId + '\'' +
                ", headsign='" + headsign + '\'' +
                ", tripPatternId='" + tripPatternId + '\'' +
                ", scheduledStartTime=" + scheduledStartTime +
                ", serviceType='" + serviceType + '\'' +
                ", expectedRuntime=" + expectedRuntime +
                ", actualRuntime=" + actualRuntime +
                ", routeShortName='" + routeShortName + '\'' +
                ", configRev=" + configRev +
                ", sequence=" + sequence +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RunTimeData that = (RunTimeData) o;
        return Objects.equal(startTime, that.startTime) &&
                Objects.equal(tripId, that.tripId) &&
                Objects.equal(headsign, that.headsign) &&
                Objects.equal(tripPatternId, that.tripPatternId) &&
                Objects.equal(scheduledStartTime, that.scheduledStartTime) &&
                Objects.equal(serviceType, that.serviceType) &&
                Objects.equal(expectedRuntime, that.expectedRuntime) &&
                Objects.equal(actualRuntime, that.actualRuntime) &&
                Objects.equal(routeShortName, that.routeShortName) &&
                Objects.equal(configRev, that.configRev) &&
                Objects.equal(sequence, that.sequence);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(startTime, tripId, headsign, tripPatternId, scheduledStartTime, serviceType,
                expectedRuntime, actualRuntime, routeShortName, configRev, sequence);
    }
}
