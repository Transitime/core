package org.transitclock.avl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Trip;
import org.transitclock.utils.DateUtils;
import org.transitclock.utils.Time;

import java.util.Date;
import java.util.List;

public class ApcModule {

  private static final Logger logger = LoggerFactory.getLogger(ApcModule.class);

  private static ApcModule instance = null;
  private ApcDataProcessor processor;

  private ApcModule() {
  }

  public static ApcModule getInstance() {
    if (instance == null) {
      synchronized (logger) {
        if (instance == null) {
          instance = new ApcModule();
          instance.init();
        }
      }
    }
    return instance;
  }

  public ApcDataProcessor getProcessor() {
    return processor;
  }

  /**
   * passenger arrival rate (PAR) in boardings per second.
   *
   * @param stopId
   * @param arrivalTime
   * @return
   */
  public Double getPassengerArrivalRate(Trip trip, String stopId, Date arrivalTime) {
    String routeId = trip.getRouteId();
    boolean isHoliday = DateUtils.isHoliday(trip);
    Date previousDayArrivalTime = DateUtils.getPreviousDayForArrivalTime(arrivalTime, isHoliday);
    Double boardingsPerMinute = processor.getBoardingsPerMinute(routeId, stopId, previousDayArrivalTime);
    if (boardingsPerMinute == null || boardingsPerMinute == 0.0) return boardingsPerMinute;
    double boardingPerSecond = new Double(boardingsPerMinute) / Time.SEC_PER_MIN;
    logger.debug("boardingsPerMinute={} = boardingsPerMinute={}",
            boardingPerSecond * Time.SEC_PER_MIN, boardingsPerMinute);
    return boardingPerSecond;
  }

  public void populateFromDb(List<ArrivalDeparture> arrivalDepartures) {
    logger.info("calling apc populateFromDb");
    if (processor != null && processor.isEnabled() && arrivalDepartures != null) {
      processor.populateFromDb(arrivalDepartures);
    } else {
     logger.info("populateFromDb called with nothing to do");
    }
  }

  private void init() {
    processor = ApcDataProcessor.getInstance();
    processor.enable();
  }

  public Long getDwellTime(String routeId, String stopId, Date arrivalTime) {
    return processor.getDwellTime(routeId, stopId, arrivalTime);
  }
}
