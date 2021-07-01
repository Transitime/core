package org.transitclock.core.reporting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.core.RunTimeServiceUtils;
import org.transitclock.core.ServiceType;
import org.transitclock.core.ServiceUtilsImpl;
import org.transitclock.core.TemporalMatch;
import org.transitclock.db.structs.Block;
import org.transitclock.db.structs.RunTimesForRoutes;
import org.transitclock.db.structs.RunTimesForStops;
import org.transitclock.db.structs.Trip;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.utils.IntervalTimer;

import java.util.ArrayList;
import java.util.List;

/**
 * Process RunTimes based on underlying ArrivalDepartures.
 */
public class RunTimeProcessor {

  private static final Logger logger = LoggerFactory.getLogger(RunTimeProcessor.class);


  /**
   * Process RunTimes for Trip
   * Used by RunTimes Generator
   * @param vehicleId
   * @param trip
   * @param block
   * @param arrivalDeparturesForStop
   * @param lastStopIndex
   * @param serviceUtils
   * @return
   */
  public boolean processRunTimesForTrip(String vehicleId,
                                        Trip trip,
                                        Block block,
                                        List<IpcArrivalDeparture> arrivalDeparturesForStop,
                                        Integer lastStopIndex,
                                        ServiceUtilsImpl serviceUtils) {
    RunTimeCache cache = new RunTimeCacheImpl();
    return processRunTimesForTrip(
            cache,
            vehicleId,
            trip,
            block,
            arrivalDeparturesForStop,
            lastStopIndex,
            null,
            serviceUtils, true).success();
  }

  /**
   * Process RunTimes for Trip
   * Used by RunTimes Loader
   * @param cache
   * @param vehicleId
   * @param trip
   * @param block
   * @param arrivalDeparturesForStop
   * @param lastStopIndex
   * @param clampingSpeed
   * @param serviceUtils
   * @param writeToDb
   * @return
   */
  public RunTimeProcessorResult processRunTimesForTrip(RunTimeCache cache,
                                                       String vehicleId,
                                                       Trip trip,
                                                       Block block,
                                                       List<IpcArrivalDeparture> arrivalDeparturesForStop,
                                                       Integer lastStopIndex,
                                                       Double clampingSpeed,
                                                       RunTimeServiceUtils serviceUtils,
                                                       boolean writeToDb){

    RunTimeProcessorState state = new RunTimeProcessorState(cache, trip, block, arrivalDeparturesForStop);
    state.setDwellTimeCount(lastStopIndex);

    IntervalTimer timer = new IntervalTimer();
    for(int i=0;i<arrivalDeparturesForStop.size(); i++)
    {
      IpcArrivalDeparture arrivalDeparture = arrivalDeparturesForStop.get(i);

      if(isSpatialMatchAndArrivalDepartureMatch(arrivalDeparture, trip.getId(), vehicleId))
      {
        // list of arrival / departures is reversed
        // this is first entry / last arrival
        // must find this before processing other arrival / departures
        if(state.getFinalStopArrivalTime() == null){
          if(arrivalDeparture.isArrival()){
            state.addArrival(arrivalDeparture, i);
          }
          else {
            continue;
          }
        }
        // this is everything else (all departures)
        else if (state.getFinalStopArrivalTime() != null) {

          if (arrivalDeparture.isDeparture()) {
            RunTimesForStops runTimesForStop = state.addDeparture(arrivalDeparture, i);

            if(state.getTotalDwellTime() != null
                    && runTimesForStop.getDwellTime() != null
                    && state.getDwellTimeCount() == arrivalDeparture.getStopPathIndex()) {
              state.addTotalDwellTime(arrivalDeparture.getDwellTime());
              state.decrementDwellTimeCount();
            } else {
              state.resetTotalDwellTime();
            }
          }
          // if we are the first stop on the trip, close off the routes
         if (i == arrivalDeparturesForStop.size()-1) {
           state.setFirstStopPathIndex(arrivalDeparture.getStopPathIndex());
           // Confirm totalDwellTime count is valid
           if (state.getDwellTimeCount() >= 0 && state.getDwellTimeCount() != arrivalDeparture.getStopPathIndex()) {
             state.resetTotalDwellTime();
           }

           return finalizeRoute(vehicleId,
                   state,
                   trip,
                   arrivalDeparture,
                   lastStopIndex,
                   timer,
                   clampingSpeed,
                   serviceUtils,
                   writeToDb);
         }
        }
      }
    }

    // we didn't detect our final case
    logger.error("Exited via fall-through for trip {} which took {} msec",
            trip.getId(),
            timer.elapsedMsecStr());
    // close off route even though we didn't find the last stop
    return finalizeRoute(vehicleId,
            state,
            trip,
            arrivalDeparturesForStop.get(0),
            lastStopIndex,
            timer,
            clampingSpeed,
            serviceUtils,
            writeToDb);
  }

  private RunTimeProcessorResult finalizeRoute(String vehicleId,
                                               RunTimeProcessorState state,
                                               Trip trip,
                                               IpcArrivalDeparture arrivalDeparture,
                                               Integer lastStopIndex,
                                               IntervalTimer timer,
                                               Double clampingSpeed,
                                               RunTimeServiceUtils serviceUtils,
                                               boolean writeToDb) {
    // Process Run Times for Route
    ServiceType serviceType = serviceUtils.getServiceTypeForTrip(arrivalDeparture.getTime(), trip);

    RunTimesForRoutes runTimesForRoutes = state.populateRuntimesForRoutes(vehicleId, serviceType, lastStopIndex);

    RunTimeProcessorResult result = new RunTimeProcessorResult(validateSpeed(state.getRunTimesForRoutes(), clampingSpeed));

    if(result.success()) {
      logger.debug("{} with {} stops", runTimesForRoutes.toString(), state.getRunTimesForStops().size());

      if (writeToDb) {
        Core.getInstance().getDbLogger().add(runTimesForRoutes);
      }
    }
    logger.info("Processing Run Times for Route took {} msec", timer.elapsedMsecStr());
    return result;

  }

  private RunTimesForRoutes validateSpeed(RunTimesForRoutes routes, Double clampingSpeed) {
    if(routes != null) {
      List<RunTimesForStops> results = new ArrayList<>(routes.getRunTimesForStops().size());
      for (RunTimesForStops rt : routes.getRunTimesForStops()) {
        if (rt.getSpeed() != null && Double.isNaN(rt.getSpeed())) {
          if (clampingSpeed != null) {
            // correct the speed
            rt.setSpeed(clampingSpeed);
            results.add(rt);
          } else {
            logger.error("rejecting speed for rt {}", rt);
          }
        } else {
          results.add(rt);
        }
      }
      routes.setRunTimesForStops(results);
    }
    return routes;
  }

  boolean isSpatialMatchAndArrivalDepartureMatch(IpcArrivalDeparture arrivalDeparture,
                                                 String tripId,
                                                 String vehicleId){
    return arrivalDeparture.getTripId().equals(tripId) &&
            arrivalDeparture.getVehicleId().equals(vehicleId);
  }

}
