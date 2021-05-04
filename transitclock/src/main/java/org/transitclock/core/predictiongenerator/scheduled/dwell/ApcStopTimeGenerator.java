package org.transitclock.core.predictiongenerator.scheduled.dwell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.avl.ApcDataProcessor;
import org.transitclock.avl.ApcModule;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.core.Indices;
import org.transitclock.core.VehicleState;
import org.transitclock.core.predictiongenerator.HistoricalPredictionLibrary;
import org.transitclock.core.predictiongenerator.kalman.KalmanPredictionResult;
import org.transitclock.core.predictiongenerator.scheduled.traveltime.kalman.KalmanPredictionGeneratorImpl;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.utils.Time;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *  Kalman Dwell time implementation based on
 *  https://scholarcommons.usf.edu/cgi/viewcontent.cgi?article=1342&context=jpt
 *
 *  From the above paper we define:
 *     prediction = (loopGain*dwellTime)+(gain*historicalDwellTime)
 *     real-time component:
 *     dwellTime = passengerArrivalRate * predictedHeadway * passengerBoardingTime
 *     passengerArrivalRate = yesterday's boardings / actual previous headway
 *     predictedHeadway = typical headway calculation
 *     passengerBoardingTime = heuristic based on vehicle/route characteristics
 *     historical component:
 *     historicalDwellTime = historicalPassengerArrivalRate * previousHeadway * passengerBoardingTIme
 */
public class ApcStopTimeGenerator extends KalmanPredictionGeneratorImpl {

  private static final IntegerConfigValue minKalmanDays = new IntegerConfigValue(
          "transitclock.prediction.data.kalman.mindays", new Integer(3),
          "Min number of days trip data that needs to be available before Kalman prediction is used instead of default transitClock prediction.");

  private static final IntegerConfigValue maxKalmanDays = new IntegerConfigValue(
          "transitclock.prediction.data.kalman.maxdays", new Integer(3),
          "Max number of historical days trips to include in Kalman prediction calculation.");

  private static final IntegerConfigValue maxKalmanDaysToSearch = new IntegerConfigValue(
          "transitclock.prediction.data.kalman.maxdaystosearch", new Integer(21),
          "Max number of days to look back for data. This will also be effected by how old the data in the cache is.");

  private static final Logger logger = LoggerFactory.getLogger(ApcStopTimeGenerator.class);

  @Override
  public long getStopTimeForPath(Indices indices, AvlReport avlReport, VehicleState vehicleState) {
    if (!hasApcData()) {
      logger.info("exiting apc dwell time, no apc data");
      return super.getStopTimeForPath(indices, avlReport, vehicleState);
    }
    Double passengerArrivalRate = getPassengerArrivalRate(indices, vehicleState);
    if (passengerArrivalRate == null) {
      // we didn't have enough information, fall back on default impl
      logger.info("exiting apc dwell time, no passenger arrival rate");
      return super.getStopTimeForPath(indices, avlReport, vehicleState);
    }
    Long currentHeadway = getHeadway(vehicleState, indices);
    if (currentHeadway == null) {
      logger.info("exiting apc dwell time, no headway data");
      return super.getStopTimeForPath(indices, avlReport, vehicleState);
    }
    double passengerBoardingTime = getPassengerBoardingTime(vehicleState);
    long dwellTime = new Double(passengerArrivalRate * currentHeadway * passengerBoardingTime).longValue();

    List<Double> historicalDwellTime = getHistoricalDwellTime(indices, vehicleState, passengerBoardingTime);
    if (historicalDwellTime == null || historicalDwellTime.size() < 3) {
      logger.info("exiting apc dwell time, no historical data");
      return super.getStopTimeForPath(indices, avlReport, vehicleState);
    }

    return predict(dwellTime, historicalDwellTime, getLastPredictionError());
  }

  private double getLastPredictionError() {
    if (true)
      throw new UnsupportedOperationException("impl cache here...");
    return 1.0;
  }

  private boolean hasApcData() {
    return ApcDataProcessor.getInstance().isEnabled();
  }

  private Long getHeadway(VehicleState vehicleState, Indices indices) {
    try {
      return HistoricalPredictionLibrary.getHeadway(vehicleState, indices);
    } catch (Exception e) {
      logger.error("travel time lookup threw exception {}", e, e);
    }
    return null;
  }

  private long predict(long dwellTime, List<Double> historicalDwellTimes,
                       double lastPredictionError) {

    double average = historicalAverage(historicalDwellTimes);
    double variance = historicalVariance(historicalDwellTimes, average);
    double gain = gain(average, variance, lastPredictionError);
    double loopGain = 1 - gain;

    KalmanPredictionResult result = new KalmanPredictionResult(
            prediction(gain,
                    loopGain,
                    historicalDwellTimes,
                    dwellTime,
                    average),

            filterError(variance, gain));

    return new Double(result.getResult()).longValue();
  }

  private double prediction(double gain, double loopGain, List<Double> historicalDwellTimes, long dwellTime, double average) {
    double averageHistoricalDuration = historicalAverage(historicalDwellTimes);
    return (loopGain * dwellTime) + (gain * averageHistoricalDuration);
  }

  private double filterError(double variance, double loopGain) {
    return variance*loopGain;
  }

  private double gain(double average, double variance, double lastPredictionError) {
    return (lastPredictionError + variance) / (lastPredictionError + (2 * variance));
  }

  private double historicalVariance(List<Double> historicalDwellTimes, double average) {
    double total = 0.0;
    for (double d : historicalDwellTimes) {
      double diff = d - average;
      double longDiffSquared = diff * diff;
      total = total + longDiffSquared;
    }
    return total/historicalDwellTimes.size();
  }

  private double historicalAverage(List<Double> historicalDwellTimes) {
    double total = 0.0;
    for (double d : historicalDwellTimes) {
      total = total + 1;
    }
    return total / historicalDwellTimes.size();
  }

  private List<Double> getHistoricalDwellTime(Indices indices, VehicleState vehicleState, double passengerBoardingTime) {
    ArrayList<Double> historicalDwells = new ArrayList<>();
    int daysBack = 0;
    long arrivalTime = getScheduledArrivalTime(indices, vehicleState);
    long currentArrivalTime = arrivalTime;
    String stopId = indices.getStopPath().getStopId();
    String routeId = indices.getTrip().getRouteId();
    while (historicalDwells.size() < maxKalmanDays.getValue() && daysBack < maxKalmanDaysToSearch.getValue()) {
      daysBack++;
      currentArrivalTime = currentArrivalTime - Time.MS_PER_DAY;
      if (isSameCalendarType(arrivalTime, currentArrivalTime)) {
        Double arrivalRate = ApcModule.getInstance()
                .getBoardingsPerSecond(stopId,
                        new Date(currentArrivalTime));
        if (arrivalRate == null) {
          if (historicalDwells.size() < minKalmanDays.getValue()) {
            logger.info("no historical boardings found for {} on stop {}", new Date(currentArrivalTime), stopId);
          }
          continue;
        }
        Long headway = getPreviousHeadway(stopId, currentArrivalTime, routeId);
        if (headway == null) {
          if (historicalDwells.size() < minKalmanDays.getValue()) {
            logger.info("no historical headway found for {} on stop {}", new Date(currentArrivalTime), stopId);
          }
          continue;
        }
        double historicalDwellTime =
                arrivalRate * headway * passengerBoardingTime;
        historicalDwells.add(historicalDwellTime);
      }

    }
    return historicalDwells;
  }

  private Long getPreviousHeadway(String stopId, long currentArrivalTime, String routeIdFilter) {
    return HistoricalPredictionLibrary.getHeadway(stopId, currentArrivalTime, routeIdFilter);
  }

  private boolean isSameCalendarType(long arrivalTime, long currentArrivalTime) {
    // TODO
    return true;
  }

  /**
   * passengerArrivalRate = yesterday's boardings / actual previous headway
   * @param indices
   * @return
   */
  private Double getPassengerArrivalRate(Indices indices, VehicleState vehicleState) {
    String stopId = indices.getStopPath().getStopId();

    // TODO calculate expected arrival time for this stopId
    Long arrivalTime = getScheduledArrivalTime(indices, vehicleState);
    if (arrivalTime == null) return null;
    Double arrivalRate = ApcModule.getInstance().getBoardingsPerSecond(stopId, new Date(arrivalTime));

    if (arrivalRate != null)
      return arrivalRate;
    // if we don't have any data assume no boarding during this period
    return 0.0;
  }

  private Long getScheduledArrivalTime(Indices indices, VehicleState vehicleState) {
    Long tripStartTime = vehicleState.getTripStartTime(indices.getTripIndex());
    if (tripStartTime == null) return null;
    long serviceDay = Time.getStartOfDay(new Date(tripStartTime));
    return serviceDay + indices.getScheduleTime().getTime() * Time.MS_PER_SEC;
  }

  private double getPassengerBoardingTime(VehicleState vehicleState) {
    // TODO
    return 2.5; // seconds per passenger
  }


}
