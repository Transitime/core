package org.transitclock.avl;

import org.transitclock.config.IntegerConfigValue;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.modules.Module;
import org.transitclock.utils.Time;

import java.util.Date;
import java.util.List;

public class ApcModule extends Module {

  public static IntegerConfigValue arrivalRateWindow
          = new IntegerConfigValue("transitclock.apc.arrivalRateWindowInMinutes",
          7,
          "Minutes to consider in arrival rate calculation");
  private static ApcModule instance;
  private ApcDataProcessor processor;
  /**
   * Constructor. Subclasses must implement a constructor that takes
   * in agencyId and calls this constructor via super(agencyId).
   *
   * @param agencyId
   */
  public ApcModule(String agencyId) {
    super(agencyId);
    instance = this;
  }

  public static ApcModule getInstance() {
    return instance;
  }

  public ApcDataProcessor getProcessor() {
    return processor;
  }
  @Override
  public void run() {
    init();
    // TODO call Core.populateFromDB
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
    Integer totalArrivals = 0;
    int window = 0;
    for (int i = -arrivalRateWindow.getValue(); i <= arrivalRateWindow.getValue(); i++){
      Integer arrivals = processor.getBoardingsPerMinute(stopId, addTime(arrivalTime, i * Time.MS_PER_MIN));
      window++;
      if (arrivals != null) {
        if (totalArrivals == null) totalArrivals = 0;
        totalArrivals = totalArrivals + arrivals;
      }
    }
    if (totalArrivals != null && totalArrivals != 0) {
      double boardingPerSecond = new Double(totalArrivals) / window / Time.SEC_PER_MIN;
      logger.info("boardingsPerMinute={} = totalArrivals={} / arrivalRateWindow={}",
              boardingPerSecond * Time.SEC_PER_MIN, totalArrivals, window);
      return boardingPerSecond;
    }

    return null;
  }

  public void populateFromDb(List<ArrivalDeparture> arrivalDepartures) {
    if (processor != null && processor.isEnabled() && arrivalDepartures != null) {
      processor.populateFromDb(arrivalDepartures);
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
