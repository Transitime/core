package org.transitclock.db.query;

import java.time.LocalTime;
import java.util.Set;

public class TripQuery {

    private final String routeShortName;
    private final String headsign;
    private final String direction;
    private final Integer firstStartTime;
    private final Integer lastStartTime;
    private final Set<Integer> configRevs;
    private final boolean readOnly;

    private TripQuery(Builder builder){
        this.routeShortName = builder.routeShortName;
        this.headsign = builder.headsign;
        this.direction = builder.direction;
        this.configRevs = builder.configRevs;
        this.firstStartTime = builder.firstStartTime;
        this.lastStartTime = builder.lastStartTime;
        this.readOnly = builder.readOnly;
    }

    public String getRouteShortName() {
        return routeShortName;
    }

    public String getHeadsign() {
        return headsign;
    }

    public String getDirection() {
        return direction;
    }

    public Set<Integer> getConfigRevs() {
        return configRevs;
    }

    public Integer getFirstStartTime() {
        return firstStartTime;
    }

    public Integer getLastStartTime() {
        return lastStartTime;
    }

    public boolean isReadOnly() {
        return readOnly;
    }


    public static class Builder{

        private String routeShortName;
        private String headsign;
        private String direction;
        private Integer firstStartTime;
        private Integer lastStartTime;
        private Set<Integer> configRevs;
        private boolean readOnly = false;

        public Builder(String routeShortName, Set<Integer> configRevs){
            this.routeShortName = routeShortName;
            this.configRevs = configRevs;
        }

        public Builder headsign(String headsign) {
            this.headsign = headsign;
            return this;
        }

        public Builder direction(String direction){
            this.direction = direction;
            return this;
        }

        public Builder firstStartTime(LocalTime firstStartTime){
            if(firstStartTime != null){
                this.firstStartTime = firstStartTime.toSecondOfDay();
            }
            return this;
        }

        public Builder lastStartTime(LocalTime lastStartTime){
            if(lastStartTime != null){
                this.lastStartTime = lastStartTime.toSecondOfDay();
            }
            return this;
        }

        public Builder readOnly(boolean readOnly) {
            this.readOnly = readOnly;
            return this;
        }

        public TripQuery build(){
            TripQuery query = new TripQuery(this);
            return query;
        }
    }
}
