package org.transitclock.reporting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.core.TemporalDifference;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.ipc.interfaces.ArrivalDepartureSpeed;
import org.transitclock.utils.Geo;
import org.transitclock.utils.Time;

import static org.transitclock.configData.ReportingConfig.*;
import static org.transitclock.configData.ReportingConfig.getMinStopPathSpeedMps;

public class SpeedCalculator {

    private static final Logger logger = LoggerFactory.getLogger(SpeedCalculator.class);

    public static Double getSpeedDataBetweenTwoArrivalDepartures(ArrivalDepartureSpeed prevDeparture,
                                                                 ArrivalDepartureSpeed currentDeparture) {
        Double travelSpeedForStopPath = null;

        if(isValidArrivalDeparturePair(prevDeparture, currentDeparture)){
            // Determine the travel times and add them to the map
            travelSpeedForStopPath = determineTravelSpeedForStopPath(prevDeparture,currentDeparture);
        }

        return travelSpeedForStopPath;
    }

    private static boolean isValidArrivalDeparturePair(ArrivalDepartureSpeed prevDeparture, ArrivalDepartureSpeed currentDeparture){
        if(prevDeparture == null || currentDeparture == null){
            logger.warn("prevDeparture {} or currentDeparture {} is null", prevDeparture, currentDeparture);
            return false;
        }
        return  isScheduleAdherenceValid(prevDeparture, currentDeparture) &&
                !isSameStop(prevDeparture, currentDeparture) &&
                isPrevAndCurrentStopDeparture(prevDeparture, currentDeparture) &&
                isValidCurrentDeparture(currentDeparture);
    }

    /**
     * If schedule adherence is really far off then ignore the data point because it would skew the results.
     * @param prevDeparture
     * @param currentDeparture
     * @return
     */
    private static boolean isScheduleAdherenceValid(ArrivalDepartureSpeed prevDeparture, ArrivalDepartureSpeed currentDeparture){
        TemporalDifference schedAdh = prevDeparture.getScheduleAdherence();
        if (schedAdh == null) {
            schedAdh = currentDeparture.getScheduleAdherence();
        }
        boolean isScheduleAdherenceValid = schedAdh == null || schedAdh.isWithinBounds(getMaxSchedAdh(), getMaxSchedAdh());
        if(!isScheduleAdherenceValid){
            logger.warn("schedule adherence {} is not valid for prevDeparture {} and currentDeparture {} is null",
                    schedAdh, prevDeparture, currentDeparture);
        }
        return isScheduleAdherenceValid;
    }

    /**
     * Check if arrival and departure is for the same stop
     * @param prevDeparture
     * @param currentDeparture
     * @return
     */
    private static boolean isSameStop(ArrivalDepartureSpeed prevDeparture, ArrivalDepartureSpeed currentDeparture){
        boolean isSameStop = prevDeparture.getStopPathIndex() == currentDeparture.getStopPathIndex();
        if(isSameStop){
            logger.warn("Found same stop when determining speed for prevDeparture {} and currentDeparture {}",
                    prevDeparture, currentDeparture);
        }
        return isSameStop;
    }

    /**
     * If looking at departure from one stop to the arrival time at the
     * very next stop then can determine the travel times between the stops
     * @param prevDeparture
     * @param currentDeparture
     * @return
     */
    private static boolean isPrevAndCurrentStopDeparture(ArrivalDepartureSpeed prevDeparture, ArrivalDepartureSpeed currentDeparture){
        boolean isSequentialDeparture = currentDeparture.getStopPathIndex() - prevDeparture.getStopPathIndex() == 1 &&
                prevDeparture.isDeparture();
        if(!isSequentialDeparture){
            logger.warn("prevDeparture {} and currentDeparture {} are not sequential", prevDeparture, currentDeparture);
        }
        return isSequentialDeparture;
    }

    private static boolean isValidCurrentDeparture(ArrivalDepartureSpeed currentDeparture){
        if(currentDeparture.getDwellTime() == null){
            logger.warn("currentDeparture {} does not have dwellTime, can't caluculate speed", currentDeparture);
            return false;
        }
        if(Float.isNaN(currentDeparture.getStopPathLength())){
            logger.warn("currentDeparture {} does not have stopPathLength, can't caluculate speed", currentDeparture);
            return false;
        }
        return true;
    }

    /**
     * Determine and return the travel time between the stop departures
     * @param prevDeparture
     * @param currentDeparture
     * @return
     */
    public static Double determineTravelSpeedForStopPath(ArrivalDepartureSpeed prevDeparture,
                                                         ArrivalDepartureSpeed currentDeparture) {
        try {
            long prevDepartureTime = getDepartureTime(prevDeparture);
            long currentArrivalTime = currentDeparture.getDate().getTime() - currentDeparture.getDwellTime();

            double travelTimeBetweenStopsMsec = (currentArrivalTime - prevDepartureTime) / Time.MS_PER_SEC;
            Double speedMps = (currentDeparture.getStopPathLength() / travelTimeBetweenStopsMsec);

            if(Double.isNaN(speedMps)){
                logger.warn("speed is NaN for prevDeparture {} and currentDeparture {} with travelTimeBetweenStops {} " +
                                "ms and currentDeparture stopLength {}", prevDeparture, currentDeparture,
                        travelTimeBetweenStopsMsec, currentDeparture.getStopPathLength());
                return null;
            }

            if(getMaxStopPathSpeedMps() < speedMps){
                logger.warn("For stopPath {} the speed of {} is above the max stoppath speed limit of {} m/s. " +
                                "The speed is getting reset to max stoppath speed. arrival/departure {} and {}.",
                        currentDeparture.getStopPathId(), speedMps, getMaxStopPathSpeedMps(),currentDeparture, prevDeparture);
                speedMps = getMaxStopPathSpeedMps();
            } else if(speedMps < getMinStopPathSpeedMps()){
                logger.warn("For stopPath {} the speed of {} is above the below stoppath speed limit of {} m/s. " +
                                "The speed is getting reset to min stoppath speed. arrival/departure {} and {}.",
                        currentDeparture.getStopPathId(), speedMps, getMinStopPathSpeedMps(),currentDeparture, prevDeparture);
                speedMps = getMinStopPathSpeedMps();
            }
            double speedMph = speedMps * Geo.MPS_TO_MPH;
            return speedMph;
        } catch (Exception e){
            logger.error("Unable to calculate speed between arrival/departure {} and {}.", currentDeparture, prevDeparture, e);
            return null;
        }

    }

    /**
     * Determine departure time. If shouldn't use departures times
     * for terminal departure that are earlier then schedule time
     * then use the scheduled departure time. This prevents creating
     * bad predictions due to incorrectly determined travel times.
     * @param arrivalDeparture
     * @return
     */
    private static long getDepartureTime(ArrivalDepartureSpeed arrivalDeparture){
        long departureTime = arrivalDeparture.getDate().getTime();
        if (arrivalDeparture.getStopPathIndex() == 0
                && arrivalDeparture.getDate().getTime() < arrivalDeparture.getScheduledDate().getTime()) {
            departureTime = arrivalDeparture.getScheduledDate().getTime();
        }
        return departureTime;
    }
}
