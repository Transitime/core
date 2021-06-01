package org.transitclock.core.dwell;

import org.transitclock.applications.Core;
import org.transitclock.config.LongConfigValue;
import org.transitclock.db.structs.Block;
import org.transitclock.db.structs.Trip;
import org.transitclock.utils.Time;

import java.util.Date;

public class DwellTimeUtil {

    /**
     * Specify minimum allowable time in msec when calculating dwell time for departures.
     * @return
     */
    private static long getMinAllowableDwellTime() {
        return minAllowableDwellTime.getValue();
    }
    private static LongConfigValue minAllowableDwellTime =
            new LongConfigValue(
                    "transitclock.arrivalsDepartures.minAllowableDwellTimeMsec",
                    1l * Time.MS_PER_SEC,
                    "Specify minimum allowable time in msec when calculating dwell time for departures.");

    /**
     * Specifying the Max allowable time when calculating dwell time for departures.
     * @return
     */
    private static long getMaxAllowableDwellTime() {
        return maxAllowableDwellTime.getValue();
    }
    private static LongConfigValue maxAllowableDwellTime =
            new LongConfigValue(
                    "transitclock.arrivalsDepartures.maxAllowableDwellTimeMsec",
                    60l * Time.MS_PER_MIN,
                    "Specify maximum allowable time in msec when calculating dwell time for departures.");


    public static Long getDwellTime(Long arrivalTime, Long departureTime,Block block,int tripIndex,
                              int departureStopPathIndex, Integer arrivalStopPathIndex){

        if (departureStopPathIndex == 0 && departureTime != null) {
            Trip trip = block.getTrip(tripIndex);
            if (trip != null) {
                return calculateFirstStopDwellTime(trip.getStartTime(), departureTime);
            }
        } else if (departureStopPathIndex != block.numStopPaths(tripIndex) - 1) {
            if (arrivalTime != null && departureTime != null &&
                    arrivalStopPathIndex != null && departureStopPathIndex == arrivalStopPathIndex) {
                return recalculateDwellTimeUsingThresholds(departureTime - arrivalTime);
            }
        } else {
            return 0l;
        }

        return null;
    }

    public static Long calculateFirstStopDwellTime(Integer scheduledStartTime, Long departureTime){
            long tripStartTimeMsecs = scheduledStartTime * 1000;
            long msecsIntoDay =
                    Core.getInstance().getTime().getMsecsIntoDay(new Date(departureTime), tripStartTimeMsecs);
            if (msecsIntoDay < tripStartTimeMsecs) {
                return 0l;
            } else {
                return recalculateDwellTimeUsingThresholds(msecsIntoDay - tripStartTimeMsecs);
            }
    }

    private static Long recalculateDwellTimeUsingThresholds(Long dwellTime){
        if(dwellTime != null){
            if(dwellTime < getMinAllowableDwellTime()){
                return getMinAllowableDwellTime();
            }
            else if(dwellTime <= getMaxAllowableDwellTime()){
                return dwellTime;
            }
        }
        return null;
    }
}
