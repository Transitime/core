package org.transitclock.reporting.service.runTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.structs.ScheduleTime;
import org.transitclock.db.structs.StopPath;
import org.transitclock.db.structs.Trip;
import org.transitclock.ipc.data.IpcStopTime;
import org.transitclock.utils.Time;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PrescriptiveScheduleProcessor {
    private static final Logger logger = LoggerFactory.getLogger(PrescriptiveScheduleProcessor.class);
    private Map<Integer, Double> scheduleAdjustmentMap;
    private final List<IpcStopTime> stopTimes = new ArrayList<>();
    //private final List<StopPath> stopPaths;
    private final Map<String, Trip> tripPatternTripsByTripId;

    // Keep track of the overall schedule adjustment on the block.
    // Mainly used to know how much to increase schedule times of trips
    // if previous trips run longer
    private int currentBlockScheduleAdjustmentDelta = 0;

    public PrescriptiveScheduleProcessor(Map<String, Trip> tripPatternTripsByTripId,
                                         Map<Integer, Double> scheduleAdjustmentMap,
                                         List<StopPath> stopPaths) {
        this.tripPatternTripsByTripId = tripPatternTripsByTripId;
        this.scheduleAdjustmentMap = scheduleAdjustmentMap;
        //this.stopPaths = stopPaths;
    }

    /**
     * Loop through trips in each block to adjust the schedule times for tripPattern trips and
     * any other trips that may be impacted indirectly by trips that run longer than originally scheduled
     * @param tripsByBlockId
     */
    public void processTripsForBlocks(Map<String, Map<String,Trip>> tripsByBlockId){

        for(Map.Entry<String, Map<String,Trip>> tripsByBlockIdEntry : tripsByBlockId.entrySet()){

            // All the trips for the current block
            Map<String, Trip> blockTripsById = tripsByBlockIdEntry.getValue();

            // Loop through all the trips on the block
            for(Map.Entry<String, Trip> tripEntry : blockTripsById.entrySet()){
                Trip trip = tripEntry.getValue();
                processTrip(trip);
            }

            resetCurrentBlockAdjustmentDelta();
        }
    }

    /**
     * Process trip if it contains a schedule adjustment
     * @param trip
     */
    public void processTrip(Trip trip){
        if(trip != null){
            List<ScheduleTime> scheduleTimes = trip.getScheduleTimes();
            Trip tripPatternTrip = tripPatternTripsByTripId.get(trip.getId());

            Integer prevScheduledArrivalTime = null;
            Integer prevScheduledDepartureTime = null;
            Integer prevAdjustedArrivalTime = null;
            Integer prevAdjustedDepartureTime = null;

            if(tripPatternTrip != null || currentBlockScheduleAdjustmentDelta > 0) {
                List<StopPath> currentTripStopPaths = trip.getStopPaths();
                // Go through all the stop paths and adjust arrival and departure times.
                // Build stopTime from stop paths and adjusted schedule times
                for (int i = 0; i < currentTripStopPaths.size(); i++) {
                    StopPath stopPath = currentTripStopPaths.get(i);
                    ScheduleTime st = scheduleTimes.get(i);

                    // Default to 1 for schedule adjustment fraction. This is for trips that don't actually
                    // have adjustments, but the block schedule adjustment is positive so have to modify the
                    // schedule anyway
                    double stopAdjustmentFraction = 1d;
                    if(tripPatternTrip != null){
                        stopAdjustmentFraction = scheduleAdjustmentMap.get(i);
                    }

                    // Get arrival and departure times as seconds. If one or the other is not available defaults to null.
                    Integer arrivalTimeSec = getAdjustedScheduleTimeSec(st.getArrivalTime(),
                            prevScheduledArrivalTime,
                            prevAdjustedArrivalTime,
                            stopAdjustmentFraction,
                            currentBlockScheduleAdjustmentDelta);


                    Integer departureTimeSec = getAdjustedScheduleTimeSec(st.getDepartureTime(),
                                                                          prevScheduledDepartureTime,
                                                                          prevAdjustedDepartureTime,
                                                                          stopAdjustmentFraction,
                                                                          currentBlockScheduleAdjustmentDelta);

                    // Convert arrival and departure times to an actual time to include in stop times
                    String arrivalTime = getAdjustedScheduleTimeStr(arrivalTimeSec);
                    String departureTime = getAdjustedScheduleTimeStr(departureTimeSec);


                    // Update previous adjusted arrivalTimes
                    if(tripPatternTrip != null){
                        prevScheduledArrivalTime = st.getArrivalOrDepartureTime();
                        prevScheduledDepartureTime = st.getTime();
                        prevAdjustedArrivalTime = arrivalTimeSec != null ? arrivalTimeSec : departureTimeSec;
                        prevAdjustedDepartureTime = departureTimeSec != null ? departureTimeSec : arrivalTimeSec;
                    }

                    IpcStopTime stopTime = new IpcStopTime(trip.getId(), arrivalTime, departureTime, stopPath.getStopId(),
                            stopPath.getGtfsStopSeq(), stopPath.isScheduleAdherenceStop());
                    stopTimes.add(stopTime);

                    // If end of trip is reached, try to update the block schedule adjustment delta
                    if(stopPath.isLastStopInTrip()){
                        if(!updateBlockScheduleAdjustmentDelta(st, arrivalTimeSec, departureTimeSec)){
                            logger.warn("Unable to update block schedule adjustment for block {}, trip {}" +
                                    "and stopPath {}", trip.getBlockId(), trip.getId(), stopPath.getId());
                        };
                    }
                }
            }
        }
    }

    private boolean updateBlockScheduleAdjustmentDelta(ScheduleTime scheduleTime,
                                                       Integer arrivalTimeSec,
                                                       Integer departureTimeSec){
        if(arrivalTimeSec != null){
            int currentTripScheduleAdjustment = arrivalTimeSec - scheduleTime.getTime();
            currentBlockScheduleAdjustmentDelta += currentTripScheduleAdjustment;
            return true;
        } else if(departureTimeSec != null){
            int currentTripScheduleAdjustment = departureTimeSec - scheduleTime.getTime();
            currentBlockScheduleAdjustmentDelta += currentTripScheduleAdjustment;
            return true;
        }
        return false;
    }

    private Integer getAdjustedScheduleTimeSec(Integer scheduleTime,
                                               Integer prevScheduledTime,
                                               Integer prevAdjustedTime,
                                               double stopAdjustmentFraction,
                                               int blockAdjustment){
        if(scheduleTime != null){
            int adjustedScheduleTime = scheduleTime;

            if(prevScheduledTime != null){
                int scheduleTimeDelta = scheduleTime - prevScheduledTime;
                if(prevAdjustedTime!= null){
                    adjustedScheduleTime = (int)(scheduleTimeDelta * stopAdjustmentFraction) + prevAdjustedTime;
                }
                else if(stopAdjustmentFraction != 1d){
                    adjustedScheduleTime = (int)(scheduleTimeDelta * stopAdjustmentFraction) + prevScheduledTime;
                }
            }

            if(blockAdjustment > 0){
                adjustedScheduleTime += blockAdjustment;
            }
            return adjustedScheduleTime;
        }
        return null;
    }

    private String getAdjustedScheduleTimeStr(Integer adjustedScheduleTime){
        if(adjustedScheduleTime != null){
            return Time.timeOfDayStr(adjustedScheduleTime);
        }
        return null;
    }

    private void resetCurrentBlockAdjustmentDelta() {
        currentBlockScheduleAdjustmentDelta = 0;
    }

    public List<IpcStopTime> getStopTimes() {
        return stopTimes;
    }
}
