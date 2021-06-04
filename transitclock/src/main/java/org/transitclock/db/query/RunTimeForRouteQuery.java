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
    private String headsign;
    private String tripPatternId;
    private String routeShortName;
    private String tripId;
    private String directionId;
    private ServiceType serviceType;
    private boolean timePointsOnly;
    private boolean scheduledTimesOnly;
    private boolean readOnly;
    private boolean includeRunTimesForStops;

    private RunTimeForRouteQuery(Builder builder){
        this.beginDate = builder.beginDate;
        this.endDate = builder.endDate;
        this.beginTime = builder.beginTime;
        this.endTime = builder.endTime;
        this.routeShortName = builder.routeShortName;
        this.headsign = builder.headsign;
        this.tripId = builder.tripId;
        this.tripPatternId = builder.tripPatternId;
        this.directionId = builder.directionId;
        this.serviceType = builder.serviceType;
        this.timePointsOnly = builder.timePointsOnly;
        this.scheduledTimesOnly = builder.scheduledTimesOnly;
        this.readOnly = builder.readOnly;
        this.includeRunTimesForStops = builder.includeRunTimesForStops;
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

    public String getHeadsign() {
        return headsign;
    }

    public String getTripPatternId() {
        return tripPatternId;
    }

    public String getTripId() {
        return tripId;
    }

    public String getDirectionId() {
        return directionId;
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

    public boolean includeRunTimesForStops() {
        return includeRunTimesForStops;
    }

    public static class Builder{

        private LocalDate beginDate;
        private LocalDate endDate;
        private Integer beginTime;
        private Integer endTime;
        private String routeShortName;
        private String headsign;
        private String tripId;
        private String tripPatternId;
        private String directionId;
        private ServiceType serviceType;
        private boolean timePointsOnly = false;
        private boolean scheduledTimesOnly = false;
        private boolean readOnly = false;
        private boolean includeRunTimesForStops = false;

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
            this.beginTime = getTimeAsSecondOfDay(beginTime);
            return this;
        }

        public Builder endTime(LocalTime endTime) {
            this.endTime = getTimeAsSecondOfDay(endTime);
            return this;
        }

        public Builder routeShortName(String routeShortName) {
            this.routeShortName = routeShortName;
            return this;
        }

        public Builder headsign(String headsign) {
            this.headsign = headsign;
            return this;
        }

        public Builder tripPatternId(String tripPatternId) {
            this.tripPatternId = tripPatternId;
            return this;
        }

        public Builder tripId(String tripId) {
            this.tripId = tripId;
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

        public Builder includeRunTimesForStops(boolean includeRunTimesForStops){
            this.includeRunTimesForStops = includeRunTimesForStops;
            return this;
        }

        public RunTimeForRouteQuery build(){
            RunTimeForRouteQuery query = new RunTimeForRouteQuery(this);
            return query;
        }

        private Integer getTimeAsSecondOfDay(LocalTime time){
            return time != null ? time.toSecondOfDay() : null;
        }
    }
}
