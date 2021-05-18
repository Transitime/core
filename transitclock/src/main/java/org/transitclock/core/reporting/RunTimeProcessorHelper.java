package org.transitclock.core.reporting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.structs.Block;
import org.transitclock.db.structs.ScheduleTime;
import org.transitclock.db.structs.StopPath;
import org.transitclock.db.structs.Trip;
import org.transitclock.ipc.data.IpcArrivalDeparture;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.transitclock.core.reporting.RunTimeGenerator.getUseScheduledArrivalDepartureValues;

/**
 * Convenience methods/Utility class for RunTimeProcessor.
 */
public class RunTimeProcessorHelper {

  private static final Logger logger =
          LoggerFactory.getLogger(RunTimeProcessorHelper.class);

  public float getStopPathLength(StopPath stopPath){
    if(stopPath != null){
      return (float) stopPath.getLength();
    }
    return Float.NaN;
  }

  public Boolean isTimePoint(StopPath stopPath){
    if(stopPath != null) {
      return stopPath.isScheduleAdherenceStop();
    }
    return null;
  }

  public Date getPrevStopDepartureTime(IpcArrivalDeparture prevDeparture){
    if(prevDeparture != null){
      return prevDeparture.getTime();
    }
    return null;
  }

  public Integer getScheduledTime(ScheduleTime scheduleTime) {
    if(scheduleTime != null){
      return scheduleTime.getTime();
    }
    return null;
  }

  public IpcArrivalDeparture getPreviousDeparture(int arrivalDepartureIndex,
                                           int currentStopPathIndex,
                                           List<IpcArrivalDeparture> arrivalDepartures){
    try {
      if (currentStopPathIndex > 0 && arrivalDepartureIndex < arrivalDepartures.size() - 1) {

        int prevStopPathIndex = currentStopPathIndex - 1;
        int currentArrivalDepartureIndex = arrivalDepartureIndex + 1;

        IpcArrivalDeparture currentPrevArrivalDeparture = arrivalDepartures.get(currentArrivalDepartureIndex);

        boolean arrivalDepartureIndexInBounds = currentArrivalDepartureIndex < arrivalDepartures.size() - 1;

        while (arrivalDepartureIndexInBounds && currentPrevArrivalDeparture.getStopPathIndex() >= prevStopPathIndex) {

          if (prevStopPathIndex == currentPrevArrivalDeparture.getStopPathIndex() && currentPrevArrivalDeparture.isDeparture()) {
            return currentPrevArrivalDeparture;
          }
          ++currentArrivalDepartureIndex;

          // Update while loop conditions
          arrivalDepartureIndexInBounds = currentArrivalDepartureIndex < arrivalDepartures.size();

          if(arrivalDepartureIndexInBounds){
            currentPrevArrivalDeparture = arrivalDepartures.get(currentArrivalDepartureIndex);
          }

        }
      }
    } catch(Exception e){
      logger.error("Unable to retrieve Previous Departure", e);
    }
    return null;
  }

  public String getStopPathId(StopPath stopPath){
    if(stopPath != null){
      return stopPath.getId();
    }
    return null;
  }


  public boolean isFirstStopForTrip(Integer currentStopPathIndex){
    return currentStopPathIndex == 0;
  }

  public Integer getNextTripStartTime(Trip trip, Block block){
    if (trip == null) return null;
    if (block == null) return null; // trip may not have a block
    int currentTripIndex = block.getTripIndex(trip);
    Trip nextTrip = block.getTrip(currentTripIndex + 1);
    if (nextTrip != null) {
      return nextTrip.getStartTime();
    }
    return null;
  }

  public ScheduleTime getScheduledTime(Trip trip, int stopPathIndex){
    if (stopPathIndex >= 0 && !trip.isNoSchedule()) {
      return trip.getScheduleTime(stopPathIndex);
    }
    return null;
  }

  public Long getDwellTime(IpcArrivalDeparture arrivalDeparture, ScheduleTime scheduleTime){
    if(arrivalDeparture.getDwellTime() != null){
      return arrivalDeparture.getDwellTime();
    } else if(getUseScheduledArrivalDepartureValues() && scheduleTime != null &&
            scheduleTime.getArrivalTime() != null && scheduleTime.getDepartureTime() != null){
      return TimeUnit.SECONDS.toMillis(Long.valueOf((scheduleTime.getDepartureTime() - scheduleTime.getArrivalTime())));
    }
    return null;
  }

  public Boolean isLastStopOnTrip(IpcArrivalDeparture arrivalDeparture, Integer finalStopPathIndex) {
    return arrivalDeparture.getStopPathIndex() == finalStopPathIndex;
  }
}
