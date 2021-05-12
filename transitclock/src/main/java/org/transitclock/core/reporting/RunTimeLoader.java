package org.transitclock.core.reporting;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.RunTimesForRoutes;
import org.transitclock.db.structs.Trip;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.utils.Time;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.time.DateUtils.addDays;
import static org.transitclock.core.dataCache.StopArrivalDepartureCacheInterface.createArrivalDeparturesReverseCriteria;

/**
 * Bulk loader of RunTimes, called from UpdateRunTimes.
 */
public class RunTimeLoader {

  private static final Logger logger =
          LoggerFactory.getLogger(RunTimeLoader.class);


  private String agencyId;
  private Date beginTime;
  private Date endTime;
  private Double clampingSpeed;
  public RunTimeCache cache;
  public RunTimeWriter writer;

  public RunTimeLoader(String agencyId, Date beginTime, Date endTime,
                       Double clampingSpeed) {
    this.agencyId = agencyId;
    this.beginTime = new Date(Time.getStartOfDay(beginTime));
    this.endTime = addDays(new Date(Time.getStartOfDay(endTime)), 1);
    this.clampingSpeed = clampingSpeed;
    this.writer = new RunTimeWriter();
    this.cache = new RunTimeCache();
  }

  public void run(Session session) {
    List<RunTimesForRoutes> results = new ArrayList<>();
    RunTimeProcessor processor = new RunTimeProcessor();

    Date currentStartDate = beginTime;
    Date currentEndDate = addDays(currentStartDate, 1);

    while (currentEndDate.getTime() < endTime.getTime()) {
      logger.info("running for {} to {} with endTime {}", currentStartDate, currentEndDate, endTime);
      Criteria criteria = session.createCriteria(ArrivalDeparture.class);
      List<ArrivalDeparture> allArrivalDepartures = createArrivalDeparturesReverseCriteria(criteria, currentStartDate, currentEndDate);
      logger.info("retrieved {} A/Ds for {}", allArrivalDepartures.size(), currentStartDate);
      Map<String, List<ArrivalDeparture>> arrivalDeparturesByVehicle = groupByVehicles(allArrivalDepartures);
      int arrivalDeparturesByVehicleCount = 0;

      for (String vehicleId : arrivalDeparturesByVehicle.keySet()) {
        arrivalDeparturesByVehicleCount++;
        List<ArrivalDeparture> vehicleArrivalDepartures = arrivalDeparturesByVehicle.get(vehicleId);
        Map<Trip, List<ArrivalDeparture>> vehicleArrivalDeparturesByTrip = groupByTrip(vehicleArrivalDepartures);

        for (Trip trip : vehicleArrivalDeparturesByTrip.keySet()) {
          List<ArrivalDeparture> tripArrivalDepartures = vehicleArrivalDeparturesByTrip.get(trip);

          RunTimeProcessorResult runTimeProcessorResult = processor.processRunTimesForTrip(this.cache,
                  vehicleId,
                  trip,
                  toIpcArrivalDepartures(tripArrivalDepartures),
                  null,
                  null,
                  getLastStopIndex(tripArrivalDepartures),
                  clampingSpeed,
                  false);
          if (runTimeProcessorResult.success()) {
            cache.update(runTimeProcessorResult);

            logger.info("added {} to {} of {}/{} runTimesForStops for trip {} on {}",
                    runTimeProcessorResult.getRunTimesForRoutes().getRunTimesForStops().size(),
                    results.size(),
                    arrivalDeparturesByVehicleCount,
                    arrivalDeparturesByVehicle.size(),
                    trip.getId(),
                    currentStartDate);
            results.add(runTimeProcessorResult.getRunTimesForRoutes());
          } else {
            logger.info("no runTimesForStops for {} for trip {}", currentStartDate, trip.getId());
          }
        }
      }

      currentStartDate = addDays(currentStartDate, 1);
      currentEndDate = addDays(currentEndDate, 1);
    }

    logger.info("complete, writing to database");

    writer.writeToDatabase(session, cache);

  }

  private Integer getLastStopIndex(List<ArrivalDeparture> tripArrivalDepartures) {
    if (tripArrivalDepartures == null || tripArrivalDepartures.isEmpty()) return null;
    int lastIndex = tripArrivalDepartures.size()-1;
    return tripArrivalDepartures.get(lastIndex).getStopPathIndex();
  }

  private List<IpcArrivalDeparture> toIpcArrivalDepartures(List<ArrivalDeparture> arrivalDepartures) {
    List<IpcArrivalDeparture> ipcs = new ArrayList<>();
    if (arrivalDepartures == null) return ipcs;
    for (ArrivalDeparture ad : arrivalDepartures) {
      try {
        ipcs.add(new IpcArrivalDeparture(ad));
      } catch (Exception e) {
        logger.error("exception converting A/D {} to ipc: {}", ad, e, e);
      }
    }
    return ipcs;
  }

  private Map<Trip, List<ArrivalDeparture>> groupByTrip(List<ArrivalDeparture> vehicleArrivalDepartures) {
    Map<Trip, List<ArrivalDeparture>> tripMap = new HashMap<>();
    for (ArrivalDeparture ad : vehicleArrivalDepartures) {
      Trip trip = ad.getTripFromDb();
      if (tripMap.containsKey(trip)) {
        tripMap.get(trip).add(ad);
      } else {
        List<ArrivalDeparture> tmp = new ArrayList<>();
        tmp.add(ad);
        tripMap.put(trip, tmp);
      }
    }
    return tripMap;
  }

  private Map<String, List<ArrivalDeparture>> groupByVehicles(List<ArrivalDeparture> results) {
    Map<String, List<ArrivalDeparture>> vehicleMap = new HashMap<>();
    for (ArrivalDeparture ad: results) {
      String vehicleId = ad.getVehicleId();
      if (vehicleMap.containsKey(vehicleId)) {
        vehicleMap.get(vehicleId).add(ad);
      } else {
        List<ArrivalDeparture> tmp = new ArrayList<>();
        tmp.add(ad);
        vehicleMap.put(vehicleId, tmp);
      }
    }
    return vehicleMap;
  }

}
