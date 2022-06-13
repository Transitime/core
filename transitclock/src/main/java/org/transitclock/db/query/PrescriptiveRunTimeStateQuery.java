package org.transitclock.db.query;

import org.transitclock.core.ServiceType;

import java.time.LocalDate;
import java.time.LocalTime;


public class PrescriptiveRunTimeStateQuery {
    private LocalDate beginDate;
    private LocalDate endDate;
    private LocalTime beginTime;
    private LocalTime endTime;
    private String headsign;
    private String tripPatternId;
    private String routeShortName;
    private String directionId;
    private ServiceType serviceType;
    private Integer configRev;
    private boolean readOnly;

    private PrescriptiveRunTimeStateQuery(PrescriptiveRunTimeStateQuery.Builder builder){
        this.beginDate = builder.beginDate;
        this.endDate = builder.endDate;
        this.beginTime = builder.beginTime;
        this.endTime = builder.endTime;
        this.headsign = builder.headsign;
        this.routeShortName = builder.routeShortName;
        this.tripPatternId = builder.tripPatternId;
        this.directionId = builder.directionId;
        this.serviceType = builder.serviceType;
        this.readOnly = builder.readOnly;
        this.configRev = builder.configRev;
    }

    public LocalDate getBeginDate() {
        return beginDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LocalTime getBeginTime() {
        return beginTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getRouteShortName() {
        return routeShortName;
    }

    public String getHeadsign() {
        return headsign;
    }

    public String getTripPatternId() {
        return tripPatternId;
    }

    public String getDirectionId() {
        return directionId;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public Integer getConfigRev() { return configRev; }

    public boolean isReadOnly() {
        return readOnly;
    }


    public static class Builder{

        private LocalDate beginDate;
        private LocalDate endDate;
        private LocalTime beginTime;
        private LocalTime endTime;
        private String headsign;
        private String routeShortName;
        private String tripPatternId;
        private String directionId;
        private ServiceType serviceType;
        private boolean readOnly = false;
        private Integer configRev;

        public Builder(){}

        public Builder beginDate(LocalDate beginDate) {
            this.beginDate = beginDate;
            return this;
        }

        public Builder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder beginTime(LocalTime beginTime) {
            this.beginTime = beginTime;
            return this;
        }

        public Builder endTime(LocalTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder headsign(String headsign) {
            this.headsign = headsign;
            return this;
        }

        public Builder routeShortName(String routeShortName) {
            this.routeShortName = routeShortName;
            return this;
        }

        public Builder tripPatternId(String tripPatternId) {
            this.tripPatternId = tripPatternId;
            return this;
        }

        public Builder directionId(String directionId) {
            this.directionId = directionId;
            return this;
        }

        public Builder serviceType(ServiceType serviceType) {
            this.serviceType = serviceType;
            return this;
        }

        public Builder configRev(Integer configRev) {
            this.configRev = configRev;
            return this;
        }

        public Builder readOnly(boolean readOnly) {
            this.readOnly = readOnly;
            return this;
        }

        public PrescriptiveRunTimeStateQuery build(){
            PrescriptiveRunTimeStateQuery query = new PrescriptiveRunTimeStateQuery(this);
            return query;
        }
    }
}
