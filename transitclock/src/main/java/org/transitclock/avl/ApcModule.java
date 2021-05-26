package org.transitclock.avl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.modules.Module;
import org.transitclock.utils.Time;

import java.util.Date;
import java.util.List;

public class ApcModule {

  private static final Logger logger = LoggerFactory.getLogger(ApcModule.class);

  public static IntegerConfigValue arrivalRateWindow
          = new IntegerConfigValue("transitclock.apc.arrivalRateWindowInMinutes",
          60,
          "Minutes to consider in arrival rate calculation");
  private static ApcModule instance = null;
  private ApcDataProcessor processor;

  private ApcModule() {
  }

  public static ApcModule getInstance() {
    if (instance == null) {
      synchronized (arrivalRateWindow) {
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
   * retrieve boardings (counts) over +/- arrivalRateWindow to smooth
   * the average and then return as a rate per second.
   *
   * @param stopId
   * @param arrivalTime
   * @return
   */
  public Double getBoardingsPerSecond(String stopId, Date arrivalTime) {
      Double boardingsPerMinute = processor.getBoardingsPerMinute(stopId, arrivalTime);
    if (boardingsPerMinute != null && boardingsPerMinute != 0) {
      double boardingPerSecond = new Double(boardingsPerMinute) / Time.SEC_PER_MIN;
      logger.debug("boardingsPerMinute={} = totalArrivals={}",
              boardingPerSecond * Time.SEC_PER_MIN, boardingsPerMinute);
      return boardingPerSecond;
    }

    return null;
  }

  public void populateFromDb(List<ArrivalDeparture> arrivalDepartures) {
    logger.info("calling apc populateFromDb");
    if (processor != null && processor.isEnabled() && arrivalDepartures != null) {
      processor.populateFromDb(arrivalDepartures);
    } else {
     logger.info("populateFromDb called with nothing to do");
    }
  }

  private Date addTime(Date arrivalTime, long millis) {
    return new Date(arrivalTime.getTime() + millis);
  }

  private void init() {
    processor = ApcDataProcessor.getInstance();
    processor.enable();
  }
}
