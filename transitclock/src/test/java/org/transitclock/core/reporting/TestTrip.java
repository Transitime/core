package org.transitclock.core.reporting;

import org.apache.commons.csv.CSVRecord;
import org.transitclock.utils.csv.CsvBase;

public class TestTrip extends CsvBase {

    private int configRev;
    private String tripId;
    private String serviceId;
    private String directionId;
    private String routeShortName;
    private String tripPatternId;
    private String headSign;
    private int startTime;
    private int endTime;

    public TestTrip(TestTrip.Builder builder){
        this.configRev = builder.configRev;
        this.tripId = builder.tripId;
        this.serviceId = builder.serviceId;
        this.directionId = builder.directionId;
        this.routeShortName = builder.routeShortName;
        this.tripPatternId = builder.tripPatternId;
        this.headSign = builder.headSign;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
    }

    public int getConfigRev() {
        return configRev;
    }

    public String getTripId() {
        return tripId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getDirectionId() {
        return directionId;
    }

    public String getRouteShortName() {
        return routeShortName;
    }

    public String getTripPatternId() {
        return tripPatternId;
    }

    public String getHeadSign() {
        return headSign;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public static class Builder{

        private int configRev;
        private String tripId;
        private String serviceId;
        private String directionId;
        private String routeShortName;
        private String tripPatternId;
        private String headSign;
        private int startTime;
        private int endTime;

        public Builder(){ }

        public TestTrip.Builder configRev(int configRev) {
            this.configRev = configRev;
            return this;
        }

        public TestTrip.Builder tripId(String tripId){
            this.tripId = tripId;
            return this;
        }

        public TestTrip.Builder serviceId(String serviceId){
            this.serviceId = serviceId;
            return this;
        }

        public TestTrip.Builder directionId(String directionId){
            this.directionId = directionId;
            return this;
        }

        public TestTrip.Builder routeShortName(String routeShortName){
            this.routeShortName = routeShortName;
            return this;
        }

        public TestTrip.Builder tripPatternId(String tripPatternId){
            this.tripPatternId = tripPatternId;
            return this;
        }

        public TestTrip.Builder headSign(String headSign){
            this.headSign = headSign;
            return this;
        }

        public TestTrip.Builder startTime(int startTime){
            this.startTime = startTime;
            return this;
        }

        public TestTrip.Builder endTime(int endTime){
            this.endTime = endTime;
            return this;
        }

        public TestTrip build(){
            TestTrip trip = new TestTrip(this);
            return trip;
        }
    }
}
