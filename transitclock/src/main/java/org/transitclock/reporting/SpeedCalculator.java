package org.transitclock.reporting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.core.TemporalDifference;
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
        return  prevDeparture != null && currentDeparture != null &&
                isScheduleAdherenceValid(prevDeparture, currentDeparture) &&
                !isSameStop(prevDeparture, currentDeparture) &&
                isPrevAndCurrentStopDeparture(prevDeparture, currentDeparture);
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
        return schedAdh == null || schedAdh.isWithinBounds(getMaxSchedAdh(), getMaxSchedAdh());
    }

    /**
     * Check if arrival and departure is for the same stop
     * @param prevDeparture
     * @param currentDeparture
     * @return
     */
    private static boolean isSameStop(ArrivalDepartureSpeed prevDeparture, ArrivalDepartureSpeed currentDeparture){
        return prevDeparture.getStopPathIndex() == currentDeparture.getStopPathIndex();
    }

    /**
     * If looking at departure from one stop to the arrival time at the
     * very next stop then can determine the travel times between the stops
     * @param prevDeparture
     * @param currentDeparture
     * @return
     */
    private static boolean isPrevAndCurrentStopDeparture(ArrivalDepartureSpeed prevDeparture, ArrivalDepartureSpeed currentDeparture){
        return currentDeparture.getStopPathIndex() - prevDeparture.getStopPathIndex() == 1 && prevDeparture.isDeparture();
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
            if(currentDeparture.getDwellTime() == null){
                return null;
            }
            long prevDepartureTime = getDepartureTime(prevDeparture);
            long currentArrivalTime = currentDeparture.getDate().getTime() - currentDeparture.getDwellTime();

            double travelTimeBetweenStopsMsec = (currentArrivalTime - prevDepartureTime) / Time.MS_PER_SEC;
            double speedMps = (currentDeparture.getStopPathLength() / travelTimeBetweenStopsMsec);

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
