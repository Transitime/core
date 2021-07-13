package org.transitclock.core.reporting;

import org.apache.commons.csv.CSVRecord;
import org.transitclock.db.structs.Arrival;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Block;
import org.transitclock.db.structs.Departure;

import java.util.Date;

public class TestArrivalDeparture {
    private boolean isArrival;
    private int configRev;
    private String vehicleId;
    private Date time;
    private Date avlTime;
    private Block block;
    private int tripIndex;
    private int pathIndex;
    private Date freqStartTime;
    private String stopPathId;
    private Long dwellTime;


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
        private boolean isWaitStop;

        public Builder(boolean isArrival){
            this.isArrival = isArrival;
        }

        public TestArrivalDeparture.Builder configRev(int configRev) {
            this.configRev = configRev;
            return this;
        }

        public TestArrivalDeparture.Builder vehicleId(String vehicleId) {
            this.vehicleId = vehicleId;
            return this;
        }

        public TestArrivalDeparture.Builder time(Date time) {
            this.time = time;
            return this;
        }

        public TestArrivalDeparture.Builder avlTime(Date avlTime) {
            this.avlTime = avlTime;
            return this;
        }

        public TestArrivalDeparture.Builder block(Block block) {
            this.block = block;
            return this;
        }

        public TestArrivalDeparture.Builder tripIndex(int tripIndex) {
            this.tripIndex = tripIndex;
            return this;
        }

        public TestArrivalDeparture.Builder stopPathIndex(int pathIndex) {
            this.pathIndex = pathIndex;
            return this;
        }

        public TestArrivalDeparture.Builder freqStartTime(Date freqStartTime) {
            this.freqStartTime = freqStartTime;
            return this;
        }

        public TestArrivalDeparture.Builder stopPathId(String stopPathId) {
            this.stopPathId = stopPathId;
            return this;
        }

        public TestArrivalDeparture.Builder dwellTime(Long dwellTime) {
            this.dwellTime = dwellTime;
            return this;
        }

        public TestArrivalDeparture.Builder isWaitStop(boolean isWaitStop) {
            this.isWaitStop = isWaitStop;
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
                    stopPathId,
                    isWaitStop
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
                    stopPathId,
                    isWaitStop
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
