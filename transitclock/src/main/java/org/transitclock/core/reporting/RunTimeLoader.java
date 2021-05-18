package org.transitclock.core.reporting;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.core.RunTimeServiceUtils;
import org.transitclock.core.travelTimes.DataFetcher;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.RunTimesForRoutes;
import org.transitclock.db.structs.Trip;
import org.transitclock.ipc.data.IpcArrivalDeparture;

import java.util.*;


/**
 * Bulk loader of RunTimes, called from UpdateRunTimes.
 */
public class RunTimeLoader {

  private static final Logger logger =
          LoggerFactory.getLogger(RunTimeLoader.class);

  private Double clampingSpeed;
  public RunTimeCache cache;
  public RunTimeWriter writer;
  private RunTimeServiceUtils serviceUtils;

  public RunTimeLoader(RunTimeWriter writer, RunTimeCache cache, Double clampingSpeed, RunTimeServiceUtils serviceUtils) {
    this.writer = writer;
    this.cache = cache;
    this.clampingSpeed = clampingSpeed;
    this.serviceUtils = serviceUtils;
  }

  public void run(String agencyId, Map<DataFetcher.DbDataMapKey, List<ArrivalDeparture>> arrivalsDeparturesMap) {
    Session session = null;
    List<RunTimesForRoutes> results = new ArrayList<>();
    RunTimeProcessor processor = new RunTimeProcessor();
    try {
      if(agencyId != null) {
        session = HibernateUtils.getSession(agencyId);
      }
      for (Map.Entry<DataFetcher.DbDataMapKey, List<ArrivalDeparture>> arrivalDepartures : arrivalsDeparturesMap.entrySet()) {
        List<ArrivalDeparture> arrivalDeparturesList = arrivalDepartures.getValue();
        reverseArrivalsDepartures(arrivalDeparturesList);

        if (arrivalDeparturesList.size() > 0) {
          ArrivalDeparture firstArrivalDeparture = arrivalDeparturesList.get(0);
          Trip trip = Trip.getTrip(session, firstArrivalDeparture.getConfigRev(), firstArrivalDeparture.getTripId());
          String vehicleId = firstArrivalDeparture.getVehicleId();

          RunTimeProcessorResult runTimeProcessorResult = processor.processRunTimesForTrip(
                  this.cache,
                  vehicleId,
                  trip,
                  toIpcArrivalDepartures(arrivalDeparturesList),
                  null,
                  null,
                  getLastStopIndex(arrivalDeparturesList),
                  clampingSpeed,
                  serviceUtils,
                  false);
          if (runTimeProcessorResult.success()) {
            cache.update(runTimeProcessorResult, arrivalDeparturesList);

            logger.info("added {} run times stops to run time for route number {} with a/d size of {} for trip {} one {}",
                    runTimeProcessorResult.getRunTimesForRoutes().getRunTimesForStops().size(),
                    results.size(),
                    arrivalDeparturesList.size(),
                    trip.getId(),
                    arrivalDepartures.getKey());
            results.add(runTimeProcessorResult.getRunTimesForRoutes());
          } else {
            logger.info("no runTimesForStops for trip {} on {}", trip.getId(), arrivalDepartures.getKey());
          }
        }
      }
    }catch(Exception e){
      e.printStackTrace();
    }finally {
      if(session != null)
        session.close();
    }

    logger.info("complete, writing to database");

    writer.writeToDatabase(agencyId, cache);

  }

  private void reverseArrivalsDepartures(List<ArrivalDeparture> arrivalDepartures) {
    arrivalDepartures.sort(Comparator.comparing(ArrivalDeparture::getTime)
            .thenComparing(ArrivalDeparture::getStopPathIndex)
            .thenComparing(ArrivalDeparture::isDeparture).reversed());
  }

  private Integer getLastStopIndex(List<ArrivalDeparture> tripArrivalDepartures) {
    if (tripArrivalDepartures == null || tripArrivalDepartures.isEmpty()) return null;
    return tripArrivalDepartures.get(0).getStopPathIndex();
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

}
