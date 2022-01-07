package org.transitclock.core.reporting;

import org.transitclock.db.structs.Arrival;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Block;
import org.transitclock.db.structs.Departure;

import java.util.Date;

public class MockArrivalDeparture {
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

        public MockArrivalDeparture.Builder configRev(int configRev) {
            this.configRev = configRev;
            return this;
        }

        public MockArrivalDeparture.Builder vehicleId(String vehicleId) {
            this.vehicleId = vehicleId;
            return this;
        }

        public MockArrivalDeparture.Builder time(Date time) {
            this.time = time;
            return this;
        }

        public MockArrivalDeparture.Builder avlTime(Date avlTime) {
            this.avlTime = avlTime;
            return this;
        }

        public MockArrivalDeparture.Builder block(Block block) {
            this.block = block;
            return this;
        }

        public MockArrivalDeparture.Builder tripIndex(int tripIndex) {
            this.tripIndex = tripIndex;
            return this;
        }

        public MockArrivalDeparture.Builder stopPathIndex(int pathIndex) {
            this.pathIndex = pathIndex;
            return this;
        }

        public MockArrivalDeparture.Builder freqStartTime(Date freqStartTime) {
            this.freqStartTime = freqStartTime;
            return this;
        }

        public MockArrivalDeparture.Builder stopPathId(String stopPathId) {
            this.stopPathId = stopPathId;
            return this;
        }

        public MockArrivalDeparture.Builder dwellTime(Long dwellTime) {
            this.dwellTime = dwellTime;
            return this;
        }

        public MockArrivalDeparture.Builder isWaitStop(boolean isWaitStop) {
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
