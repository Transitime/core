package org.transitclock.core.reporting;

import org.transitclock.db.structs.Arrival;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Block;
import org.transitclock.db.structs.Departure;

import java.util.Date;

public class TestArrivalDepartureBuilder {
    public static class Builder{
        private boolean isArrival;
        private int configRev;
        private String vehicleId = null;
        private Date time = null;
        private Date avlTime = null;
        private Block block = null;
        private int tripIndex;
        private int pathIndex;
        private Date freqStartTime = null;
        private String stopPathId = null;
        private Long dwellTime = null;

        public Builder(boolean isArrival){
            this.isArrival = isArrival;
        }

        public TestArrivalDepartureBuilder.Builder configRev(int configRev) {
            this.configRev = configRev;
            return this;
        }

        public TestArrivalDepartureBuilder.Builder vehicleId(String vehicleId) {
            this.vehicleId = vehicleId;
            return this;
        }

        public TestArrivalDepartureBuilder.Builder time(Date time) {
            this.time = time;
            return this;
        }

        public TestArrivalDepartureBuilder.Builder avlTime(Date avlTime) {
            this.avlTime = avlTime;
            return this;
        }

        public TestArrivalDepartureBuilder.Builder block(Block block) {
            this.block = block;
            return this;
        }

        public TestArrivalDepartureBuilder.Builder tripIndex(int tripIndex) {
            this.tripIndex = tripIndex;
            return this;
        }

        public TestArrivalDepartureBuilder.Builder stopPathIndex(int pathIndex) {
            this.pathIndex = pathIndex;
            return this;
        }

        public TestArrivalDepartureBuilder.Builder freqStartTime(Date freqStartTime) {
            this.freqStartTime = freqStartTime;
            return this;
        }

        public TestArrivalDepartureBuilder.Builder stopPathId(String stopPathId) {
            this.stopPathId = stopPathId;
            return this;
        }

        public TestArrivalDepartureBuilder.Builder dwellTime(Long dwellTime) {
            this.dwellTime = dwellTime;
            return this;
        }

        protected Arrival buildArrival(){
            return new Arrival(
                    configRev,
                    vehicleId,
                    time,
                    avlTime,
                    block,
                    tripIndex,
                    pathIndex,
                    freqStartTime,
                    stopPathId
            );

        }

        protected Departure buildDeparture(){
            return new Departure(
                    configRev,
                    vehicleId,
                    time,
                    avlTime,
                    block,
                    tripIndex,
                    pathIndex,
                    freqStartTime,
                    dwellTime,
                    stopPathId
            );
        }

        public ArrivalDeparture build(){
            if(isArrival){
                return buildArrival();
            }
            else {
                return buildDeparture();
            }
        }
    }
}
