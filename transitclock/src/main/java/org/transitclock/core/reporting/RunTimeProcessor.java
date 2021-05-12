package org.transitclock.core.reporting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.core.ServiceType;
import org.transitclock.core.ServiceUtils;
import org.transitclock.core.TemporalMatch;
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

  private static final Logger logger =
          LoggerFactory.getLogger(RunTimeProcessor.class);


  public boolean processRunTimesForTrip(String vehicleId,
                                        Trip trip,
                                        List<IpcArrivalDeparture> arrivalDeparturesForStop,
                                        TemporalMatch matchAtPreviousStop,
                                        TemporalMatch matchAtCurrentStop,
                                        Integer lastStopIndex) {
    RunTimeCache cache = new RunTimeCache();
    return processRunTimesForTrip(cache,
            vehicleId,
            trip,
            arrivalDeparturesForStop,
            matchAtPreviousStop,
            matchAtCurrentStop,
            lastStopIndex,
            null,
            true).success();
  }

  public RunTimeProcessorResult processRunTimesForTrip(RunTimeCache cache,
                                        String vehicleId,
                                        Trip trip,
                                        List<IpcArrivalDeparture> arrivalDeparturesForStop,
                                        TemporalMatch matchAtPreviousStop,
                                        TemporalMatch matchAtCurrentStop,
                                        Integer lastStopIndex,
                                        Double clampingSpeed,
                                        boolean writeToDb){

    RunTimeProcessorState state = new RunTimeProcessorState(cache, trip,
            arrivalDeparturesForStop);
    state.setDwellTimeCount(lastStopIndex);

    IntervalTimer timer = new IntervalTimer();
    for(int i=0;i<arrivalDeparturesForStop.size(); i++)
    {
      IpcArrivalDeparture arrivalDeparture = arrivalDeparturesForStop.get(i);

      if(isSpatialMatchAndArrivalDepartureMatch(arrivalDeparture, trip.getId(), vehicleId))
      {
        // this is first entry / last arrival
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
          // if we are the last stop on the trip, close off the routes
         if (i == arrivalDeparturesForStop.size()-1) {
           // Confirm totalDwellTime count is valid
           if (state.getDwellTimeCount() >= 0 && state.getDwellTimeCount() != arrivalDeparture.getStopPathIndex()) {
             state.resetTotalDwellTime();
           }

           return finalizeRoute(vehicleId,
                   state,
                   trip,
                   arrivalDeparture,
                   matchAtPreviousStop,
                   matchAtCurrentStop,
                   lastStopIndex,
                   timer,
                   clampingSpeed,
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
            matchAtPreviousStop,
            matchAtCurrentStop,
            lastStopIndex,
            timer,
            clampingSpeed,
            writeToDb);
  }

  private RunTimeProcessorResult finalizeRoute(String vehicleId,
                                               RunTimeProcessorState state,
                                               Trip trip,
                                               IpcArrivalDeparture arrivalDeparture,
                                               TemporalMatch matchAtPreviousStop,
                                               TemporalMatch matchAtCurrentStop,
                                               Integer lastStopIndex,
                                               IntervalTimer timer,
                                               Double clampingSpeed,
                                               boolean writeToDb) {
    // Process Run Times for Route
    ServiceUtils serviceUtils = Core.getInstance().getServiceUtils();
    ServiceType serviceType = serviceUtils.getServiceTypeForTrip(arrivalDeparture.getTime(),
            trip.getStartTime(), trip.getServiceId());

    RunTimesForRoutes runTimesForRoutes =
            state.populateRuntimesForRoutes(vehicleId,
                    serviceType, lastStopIndex);

    if (matchAtPreviousStop != null) {
      logger.debug("Previous Match {}", matchAtPreviousStop.toString());
    }
    if (matchAtPreviousStop != null) {
      logger.debug("Current Match {}", matchAtCurrentStop.toString());
    }

    logger.debug("{} with {} stops", runTimesForRoutes.toString(), state.getRunTimesForStops().size());

    if (writeToDb) {
      Core.getInstance().getDbLogger().add(runTimesForRoutes);
    }
    logger.info("Processing Run Times for Route took {} msec", timer.elapsedMsecStr());
    return new RunTimeProcessorResult(validate(state.getRunTimesForRoutes(), clampingSpeed));

  }

  private RunTimesForRoutes validate(RunTimesForRoutes routes, Double clampingSpeed) {
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
    return routes;
  }

  boolean isSpatialMatchAndArrivalDepartureMatch(IpcArrivalDeparture arrivalDeparture,
                                                 String tripId,
                                                 String vehicleId){
    return arrivalDeparture.getTripId().equals(tripId) &&
            arrivalDeparture.getVehicleId().equals(vehicleId);
  }

}
