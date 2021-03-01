package org.transitclock.db.query;

import org.transitclock.core.ServiceType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

public class ArrivalDepartureQuery {
    private LocalDate beginDate;
    private LocalDate endDate;
    private LocalTime beginTime;
    private LocalTime endTime;
    private String routeShortName;
    private String headsign;
    private String startStop;
    private String endStop;
    private String tripPatternId;
    private Set<String> tripIds;
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
        this.tripPatternId = builder.tripPatternId;
        this.tripIds = builder.tripIds;
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

    public String getStartStop() {
        return startStop;
    }

    public String getEndStop() {
        return endStop;
    }

    public String getTripPatternId() {
        return tripPatternId;
    }

    public Set<String> getTripIds() {
        return tripIds;
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

    public boolean isDwellTimeOnly() {
        return dwellTimeOnly;
    }

    public boolean isIncludeTrip() {
        return includeTrip;
    }

    public boolean isIncludeStop() {
        return includeStop;
    }

    public boolean isIncludeStopPath() {
        return includeStopPath;
    }

    public boolean isReadOnly() {
        return readOnly;
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
        private String tripPatternId;
        private Set<String> tripIds;
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

        public Builder tripPatternId(String tripPatternId){
            this.tripPatternId = tripPatternId;
            return this;
        }

        public Builder tripIds(Set<String> tripIds){
            this.tripIds = tripIds;
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
