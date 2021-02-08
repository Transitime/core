package org.transitclock.db.query;

import org.transitclock.core.ServiceType;

import java.time.LocalDate;
import java.time.LocalTime;

public class ArrivalDepartureQuery {
    private LocalDate beginDate;
    private LocalDate endDate;
    private LocalTime beginTime;
    private LocalTime endTime;
    private String routeShortName;
    private String headsign;
    private String startStop;
    private String endStop;
    private ServiceType serviceType;
    private boolean timePointsOnly;
    private boolean scheduledTimesOnly;
    private boolean dwellTimeOnly;
    private boolean includeTrip;
    private boolean includeStop;
    private boolean includeStopPath;
    private boolean readOnly;

    private ArrivalDepartureQuery(Builder builder){
        this.beginDate = builder.beginDate;
        this.endDate = builder.endDate;
        this.beginTime = builder.beginTime;
        this.endTime = builder.endTime;
        this.routeShortName = builder.routeShortName;
        this.headsign = builder.headsign;
        this.startStop = builder.startStop;
        this.endStop = builder.endStop;
        this.serviceType = builder.serviceType;
        this.timePointsOnly = builder.timePointsOnly;
        this.scheduledTimesOnly = builder.scheduledTimesOnly;
        this.dwellTimeOnly = builder.dwellTimeOnly;
        this.includeTrip = builder.includeTrip;
        this.includeStop = builder.includeStop;
        this.includeStopPath = builder.includeStopPath;
        this.readOnly = builder.readOnly;
    }

    public LocalDate getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalTime getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(LocalTime beginTime) {
        this.beginTime = beginTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getRouteShortName() {
        return routeShortName;
    }

    public void setRouteShortName(String routeShortName) {
        this.routeShortName = routeShortName;
    }

    public String getHeadsign() {
        return headsign;
    }

    public void setHeadsign(String headsign) {
        this.headsign = headsign;
    }

    public String getStartStop() {
        return startStop;
    }

    public void setStartStop(String startStop) {
        this.startStop = startStop;
    }

    public String getEndStop() {
        return endStop;
    }

    public void setEndStop(String endStop) {
        this.endStop = endStop;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public boolean isTimePointsOnly() {
        return timePointsOnly;
    }

    public void setTimePointsOnly(boolean timePointsOnly) {
        this.timePointsOnly = timePointsOnly;
    }

    public boolean isScheduledTimesOnly() {
        return scheduledTimesOnly;
    }

    public void setScheduledTimesOnly(boolean scheduledTimesOnly) {
        this.scheduledTimesOnly = scheduledTimesOnly;
    }

    public boolean isDwellTimeOnly() {
        return dwellTimeOnly;
    }

    public void setDwellTimeOnly(boolean dwellTimeOnly) {
        this.dwellTimeOnly = dwellTimeOnly;
    }

    public boolean isIncludeTrip() {
        return includeTrip;
    }

    public void setIncludeTrip(boolean includeTrip) {
        this.includeTrip = includeTrip;
    }

    public boolean isIncludeStop() {
        return includeStop;
    }

    public void setIncludeStop(boolean includeStop) {
        this.includeStop = includeStop;
    }

    public boolean isIncludeStopPath() {
        return includeStopPath;
    }

    public void setIncludeStopPath(boolean includeStopPath) {
        this.includeStopPath = includeStopPath;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public static class Builder{

        private LocalDate beginDate;
        private LocalDate endDate;
        private LocalTime beginTime;
        private LocalTime endTime;
        private String routeShortName;
        private String headsign;
        private String startStop;
        private String endStop;
        private ServiceType serviceType;
        private boolean timePointsOnly = false;
        private boolean scheduledTimesOnly = false;
        private boolean dwellTimeOnly = false;
        private boolean includeTrip = false;
        private boolean includeStop = false;
        private boolean includeStopPath = false;
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

        public Builder beginTime(LocalTime beginTime) {
            this.beginTime = beginTime;
            return this;
        }

        public Builder endTime(LocalTime endTime) {
            this.endTime = endTime;
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

        public Builder startStop(String startStop) {
            this.startStop = startStop;
            return this;
        }

        public Builder endStop(String endStop) {
            this.endStop = endStop;
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

        public Builder dwellTimeOnly(boolean dwellTimeOnly) {
            this.dwellTimeOnly = dwellTimeOnly;
            return this;
        }

        public Builder includeTrip(boolean includeTrip) {
            this.includeTrip = includeTrip;
            return this;
        }

        public Builder includeStop(boolean includeStop) {
            this.includeStop = includeStop;
            return this;
        }

        public Builder includeStopPath(boolean includeStopPath) {
            this.includeStopPath = includeStopPath;
            return this;
        }

        public Builder readOnly(boolean readOnly) {
            this.readOnly = readOnly;
            return this;
        }

        public ArrivalDepartureQuery build(){
            ArrivalDepartureQuery query = new ArrivalDepartureQuery(this);
            return query;
        }
    }
}
