package org.transitclock.avl;

import org.apache.commons.csv.CSVRecord;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Block;
import org.transitclock.utils.Time;
import org.transitclock.utils.csv.CsvBaseReader;

import java.text.ParseException;
import java.util.Date;

/**
 * Support reading in ArrivalDepartures from CSV for historical records.
 */
public class ArrivalDepartureCsvReader extends CsvBaseReader<ArrivalDeparture> {

    private int configRev;
    private boolean includeTrips;
    public ArrivalDepartureCsvReader(String fileName, int configRev, boolean includeTrips) {
        super(fileName);
        this.configRev = configRev;
        this.includeTrips = includeTrips;
    }

    @Override
    protected ArrivalDeparture handleRecord(CSVRecord r, boolean supplemental) throws ParseException, NumberFormatException {
        try {
            // not used
            // String dType = r.get("DTYPE");
            // vehicleId
            String vehicleId = nullSafeGet(r, "vehicleId");
            // time
            long time = Time.parse(r.get("time")).getTime();
            // avlTime
            long avlTime = Time.parse(r.get("avlTime")).getTime();
            Block block = null;
            // directionId
            String directionId = nullSafeGet(r, "directionId");
            // tripIndex
            int tripIndex = Integer.parseInt(r.get("tripIndex"));
            // stopPathIndex
            int stopPathIndex = Integer.parseInt(r.get("stopPathIndex"));
            // isArrival
            boolean isArrival = Integer.parseInt(r.get("isArrival")) > 0;
            // configRev -- param
            // scheduleTime
            long scheduledTime = -1;
            String scheduleTimeStr = nullSafeGet(r, "scheduledTime");
            if (scheduleTimeStr != null && !"NULL".equals(scheduleTimeStr)) {
                try {
                    scheduledTime = Integer.parseInt(r.get("scheduledTime"));
                } catch (NumberFormatException nfe) {
                    //strangely schema has scheduledTime as a DateTime but we only want
                    // seconds since midnight
                    long scheduledTimeSinceEpoch = Time.parse(r.get("scheduledTime")).getTime();
                    // now subtract service day
                    long startOfDay = Time.getStartOfDay(new Date(time));
                    scheduledTime = Math.toIntExact(scheduledTimeSinceEpoch - startOfDay);
                }
            }
            // block
            String blockId = r.get("blockId");
            // tripId
            String tripId = nullSafeGet(r, "tripId");
            // stopId
            String stopId = nullSafeGet(r, "stopId");
            // stopSeq
            int gtfsStopSeq = Integer.parseInt(r.get("gtfsStopSeq"));
            // stopPathLength
            float stopPathLength = Float.parseFloat(r.get("stopPathLength"));
            // routeId
            String routeId = nullSafeGet(r, "routeId");
            // routeShortName
            String routeShortName = nullSafeGet(r, "routeShortName");
            // serviceId
            String serviceId = null;
            // feqStartTime
            Long freqStartTime = null;
            // dwellTime
            Long dwellTime = null;
            try {
                String dwellTimeStr = nullSafeGet(r, "dwellTime");
                if (dwellTimeStr != null && !"NULL".equals(dwellTimeStr))
                    dwellTime = Long.parseLong(r.get("dwellTime"));
            } catch (NumberFormatException nfe) {
                dwellTime = null;
            }
            // tripPatternId
            String tripPatternId = nullSafeGet(r, "tripPatternId");
            // stopPathId
            String stopPathId = nullSafeGet(r, "stopPathId");
            // scheduledAdherenceStop
            boolean scheduleAdherenceStop = false;
            try {
                scheduleAdherenceStop = Integer.parseInt(r.get("scheduleAdherenceStop")) > 0;
            } catch (NumberFormatException nfe) {
                scheduleAdherenceStop = false;
            }

            if (!includeTrips) {
                tripPatternId = null;
            }

            return new ArrivalDeparture.Builder(vehicleId, time, avlTime, block, directionId, tripIndex,
                        stopPathIndex, null, isArrival, configRev, scheduledTime, blockId,
                        tripId, stopId, gtfsStopSeq, stopPathLength, routeId, routeShortName, serviceId,
                        freqStartTime, dwellTime, tripPatternId, stopPathId, scheduleAdherenceStop).create();
        } catch (ParseException ex) {
            logger.error(ex.getMessage());
            return null;
        } catch (Throwable t) {
            logger.error("unexpected data caused {}", t, t);
            return null;
        }

    }

    private String nullSafeGet(CSVRecord r, String key) {
        String str = r.get(key);
        if ("NULL".equals(str))
            return null;
        return str;
    }
}
