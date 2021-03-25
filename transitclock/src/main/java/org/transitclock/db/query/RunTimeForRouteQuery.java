package org.transitclock.db.query;

import org.transitclock.core.ServiceType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public class RunTimeForRouteQuery {
    private LocalDate beginDate;
    private LocalDate endDate;
    private Integer beginTime;
    private Integer endTime;
    private String routeShortName;
    private ServiceType serviceType;
    private boolean timePointsOnly;
    private boolean scheduledTimesOnly;
    private boolean readOnly;

    private RunTimeForRouteQuery(Builder builder){
        this.beginDate = builder.beginDate;
        this.endDate = builder.endDate;
        this.beginTime = builder.beginTime;
        this.endTime = builder.endTime;
        this.routeShortName = builder.routeShortName;
        this.serviceType = builder.serviceType;
        this.timePointsOnly = builder.timePointsOnly;
        this.scheduledTimesOnly = builder.scheduledTimesOnly;
        this.readOnly = builder.readOnly;
    }

    public LocalDate getBeginDate() {
        return beginDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Integer getBeginTime() {
        return beginTime;
    }

    public Integer getEndTime() {
        return endTime;
    }

    public String getRouteShortName() {
        return routeShortName;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public boolean isTimePointsOnly() {
        return timePointsOnly;
    }

    public boolean isScheduledTimesOnly() {
        return scheduledTimesOnly;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public static class Builder{

        private LocalDate beginDate;
        private LocalDate endDate;
        private Integer beginTime;
        private Integer endTime;
        private String routeShortName;
        private ServiceType serviceType;
        private boolean timePointsOnly = false;
        private boolean scheduledTimesOnly = false;
        private boolean readOnly = false;

        public Builder(){}

        public Builder beginDate(LocalDate beginDate) {
            this.beginDate = beginDate;
            return this;
        }

        public Builder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder beginTime(Integer beginTime) {
            this.beginTime = beginTime;
            return this;
        }

        public Builder endTime(Integer endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder routeShortName(String routeShortName) {
            this.routeShortName = routeShortName;
            return this;
        }

        public Builder serviceType(ServiceType serviceType) {
            this.serviceType = serviceType;
            return this;
        }

        public Builder timePointsOnly(boolean timePointsOnly) {
            this.timePointsOnly = timePointsOnly;
            return this;
        }

        public Builder scheduledTimesOnly(boolean scheduledTimesOnly) {
            this.scheduledTimesOnly = scheduledTimesOnly;
            return this;
        }

        public Builder readOnly(boolean readOnly) {
            this.readOnly = readOnly;
            return this;
        }

        public RunTimeForRouteQuery build(){
            RunTimeForRouteQuery query = new RunTimeForRouteQuery(this);
            return query;
        }
    }
}
