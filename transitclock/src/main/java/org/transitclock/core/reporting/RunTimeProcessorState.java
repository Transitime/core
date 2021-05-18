package org.transitclock.core.reporting;

import org.transitclock.core.ServiceType;
import org.transitclock.db.structs.*;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.reporting.SpeedCalculator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Internal state of RunTimeProcessor, and methods to mutate it.
 */
public class RunTimeProcessorState {

  private int dwellTimeCount;
  private Long totalDwellTime = null;
  private Date firstStopDepartureTime = null;
  private Date finalStopArrivalTime = null;
  private Integer firstStopPathIndex = null;
  private Integer finalStopPathIndex = null;
  private Trip trip;
  private Block block;
  private List<IpcArrivalDeparture> arrivalDeparturesForStop;
  RunTimesForRoutes runTimesForRoutes = null;
  List<RunTimesForStops> runTimesForStops = new ArrayList<>();
  private RunTimeProcessorHelper helper = new RunTimeProcessorHelper();
  private RunTimeCache cache;

  public RunTimeProcessorState(RunTimeCache cache,
                               Trip trip,
                               Block block,
                               List<IpcArrivalDeparture> arrivalDeparturesForStop) {
    this.cache = cache;
    this.trip = trip;
    this.block = block;
    this.arrivalDeparturesForStop = arrivalDeparturesForStop;
  }

  public int getDwellTimeCount() {
    return dwellTimeCount;
  }
  public void setDwellTimeCount(int dwellTimeCount) {
    this.dwellTimeCount = dwellTimeCount;
  }
  public void incrementDwellTimeCount() {
    dwellTimeCount++;
  }
  public void decrementDwellTimeCount() {
    dwellTimeCount--;
  }

  public Long getTotalDwellTime() {
    return totalDwellTime;
  }
  public void setTotalDwellTime(long l) {
    this.totalDwellTime = l;
  }
  public void resetTotalDwellTime() {
    this.totalDwellTime = null;
  }
  public void addTotalDwellTime(long add) {
    this.totalDwellTime += add;
  }

  public Date getFirstStopDepartureTime() {
    return firstStopDepartureTime;
  }
  public void setFirstStopDepartureTime(Date time) {
    this.firstStopDepartureTime = time;
  }
  public Date getFinalStopArrivalTime() {
    return finalStopArrivalTime;
  }
  public void setFinalStopArrivalTime(Date time) {
    this.finalStopArrivalTime = time;
  }
  public Integer getFirstStopPathIndex() {
    return firstStopPathIndex;
  }
  public void setFirstStopPathIndex(Integer i) {
    this.firstStopPathIndex = i;
  }
  public Integer getFinalStopPathIndex() {
    return finalStopPathIndex;
  }
  public void setFinalStopPathIndex(Integer i) {
    this.finalStopPathIndex = i;
  }
  public List<RunTimesForStops> getRunTimesForStops() {
    return runTimesForStops;
  }


  public RunTimesForStops addDeparture(IpcArrivalDeparture arrivalDeparture, int i) {

    IpcArrivalDeparture prevDeparture =
            helper.getPreviousDeparture(i, arrivalDeparture.getStopPathIndex(), arrivalDeparturesForStop);

    RunTimesForStops runTimesForStop = createRunTimesForStop(arrivalDeparture,
            prevDeparture);

    setFirstStopDepartureTime(arrivalDeparture.getTime());

    return runTimesForStop;
  }

  public RunTimesForStops addArrival(IpcArrivalDeparture arrivalDeparture, int i) {

    setFinalStopArrivalTime(arrivalDeparture.getTime());
    setFinalStopPathIndex(arrivalDeparture.getStopPathIndex());
    setTotalDwellTime(0l);
    decrementDwellTimeCount();

    IpcArrivalDeparture prevDeparture =
            helper.getPreviousDeparture(i, arrivalDeparture.getStopPathIndex(), arrivalDeparturesForStop);

    RunTimesForStops runTimesForStop = createRunTimesForStop(arrivalDeparture,
            prevDeparture);
    return runTimesForStop;
  }

  private RunTimesForStops createRunTimesForStop(IpcArrivalDeparture arrivalDeparture,
                                                 IpcArrivalDeparture prevDeparture) {

    boolean isFirstStop = helper.isFirstStopForTrip(arrivalDeparture.getStopPathIndex());
    // Get scheduled time info for stopPath
    ScheduleTime prevStopScheduledTime = null;
    if (!isFirstStop) {
      prevStopScheduledTime = helper.getScheduledTime(trip, arrivalDeparture.getStopPathIndex() - 1);
    }
    ScheduleTime currentStopScheduledTime = helper.getScheduledTime(trip, arrivalDeparture.getStopPathIndex());

    // Lookup stopPath for trip using ArrivalDeparture stopPathIndex
    StopPath stopPath = trip.getStopPath(arrivalDeparture.getStopPathIndex());

    // Get scheduled time info for stopPath
    Integer scheduledPrevStopDepartureTime = null;
    if (prevStopScheduledTime != null) {
      scheduledPrevStopDepartureTime = helper.getScheduledTime(prevStopScheduledTime);
    }
    Integer scheduledCurrentStopDepartureTime = helper.getScheduledTime(currentStopScheduledTime);

    Long dwellTime = 0l;
    if (arrivalDeparture.isDeparture()) {
      // calculate for addDeaprture, finalDeparture but not arrival
      dwellTime = helper.getDwellTime(arrivalDeparture, currentStopScheduledTime);
    }
    Date prevStopDepartureTime = null;
    if (prevDeparture != null) {
      prevStopDepartureTime = helper.getPrevStopDepartureTime(prevDeparture);
    }
    Double speed = null;
    // Setting first stop properties
    if(isFirstStop || prevDeparture == null) {
      speed = 0.0;
      scheduledPrevStopDepartureTime = 0;
    } else {
      speed = SpeedCalculator.getSpeedDataBetweenTwoArrivalDepartures(prevDeparture, arrivalDeparture);
    }

    // Setting dwell time and stopPathLength to help calculate speed since
    // we need to subtract dwell time to get arrival time and use stopPathLength
    // to get distance travelled
    arrivalDeparture.setDwellTime(dwellTime);
    arrivalDeparture.setStopPathLength(helper.getStopPathLength(stopPath));

    RunTimesForStops runTimesForStop = new RunTimesForStops(
            helper.getStopPathId(stopPath),
            arrivalDeparture.getStopPathIndex(),
            arrivalDeparture.getTime(),
            prevStopDepartureTime,
            scheduledCurrentStopDepartureTime,
            scheduledPrevStopDepartureTime,
            dwellTime,
            speed,
            helper.isLastStopOnTrip(arrivalDeparture, finalStopPathIndex),
            helper.isTimePoint(stopPath)
    );
    runTimesForStops.add(runTimesForStop);

    return runTimesForStop;
  }

  public RunTimesForRoutes populateRuntimesForRoutes(String vehicleId,
                                                     ServiceType serviceType,
                                                     Integer lastStopIndex) {
    if (runTimesForRoutes == null) {
        runTimesForRoutes = cache.getOrCreate(trip.getConfigRev(), trip.getId(), getFirstStopDepartureTime(), vehicleId);
    }
    runTimesForRoutes.setServiceId(trip.getServiceId());
    runTimesForRoutes.setDirectionId(trip.getDirectionId());
    runTimesForRoutes.setRouteShortName(trip.getRouteShortName());
    runTimesForRoutes.setTripPatternId(trip.getTripPattern().getId());
    runTimesForRoutes.setHeadsign(trip.getHeadsign());
    runTimesForRoutes.setEndTime(getFinalStopArrivalTime());
    runTimesForRoutes.setScheduledStartTime(trip.getStartTime());
    runTimesForRoutes.setScheduledEndTime(trip.getEndTime());
    runTimesForRoutes.setNextTripStartTime(helper.getNextTripStartTime(trip, block));
    runTimesForRoutes.setServiceType(serviceType);
    if (runTimesForRoutes.getDwellTime() == null) {
      runTimesForRoutes.setDwellTime(getTotalDwellTime());
    } else {
      if (getTotalDwellTime() != null) {
        runTimesForRoutes.setDwellTime(getTotalDwellTime() + runTimesForRoutes.getDwellTime());
      }
    }
    runTimesForRoutes.setStartStopPathIndex(getFirstStopPathIndex());
    runTimesForRoutes.setActualLastStopPathIndex(getFinalStopPathIndex());
    runTimesForRoutes.setExpectedLastStopPathIndex(lastStopIndex);
    if (runTimesForRoutes.getRunTimesForStops() != null) {
      runTimesForRoutes.getRunTimesForStops().addAll(runTimesForStops);
    } else {
      runTimesForRoutes.setRunTimesForStops(runTimesForStops);
    }

    for(RunTimesForStops runTimesForStop: runTimesForStops){
      runTimesForStop.setRunTimesForRoutes(runTimesForRoutes);
    }

    return runTimesForRoutes;
  }

  public RunTimesForRoutes getRunTimesForRoutes() {
    return runTimesForRoutes;
  }
}
