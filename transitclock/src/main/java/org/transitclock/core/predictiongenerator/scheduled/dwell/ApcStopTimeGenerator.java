package org.transitclock.core.predictiongenerator.scheduled.dwell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.avl.ApcDataProcessor;
import org.transitclock.avl.ApcModule;
import org.transitclock.config.DoubleConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.core.Indices;
import org.transitclock.core.VehicleState;
import org.transitclock.core.dataCache.KalmanError;
import org.transitclock.core.predictiongenerator.HistoricalPredictionLibrary;
import org.transitclock.core.predictiongenerator.kalman.KalmanPredictionResult;
import org.transitclock.core.predictiongenerator.scheduled.traveltime.kalman.KalmanPredictionGeneratorImpl;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.db.structs.ScheduleTime;
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
          "transitclock.prediction.data.kalman.apc.mindays", new Integer(3),
          "Min number of days trip data that needs to be available before Kalman prediction is used instead of default transitClock prediction.");

  private static final IntegerConfigValue maxKalmanDays = new IntegerConfigValue(
          "transitclock.prediction.data.kalman.apc.maxdays", new Integer(5),
          "Max number of historical days trips to include in Kalman prediction calculation.");

  private static final IntegerConfigValue maxKalmanDaysToSearch = new IntegerConfigValue(
          "transitclock.prediction.data.kalman.apc.maxdaystosearch", new Integer(21),
          "Max number of days to look back for data. This will also be effected by how old the data in the cache is.");

  private static final DoubleConfigValue initialErrorValue = new DoubleConfigValue(
          "transitclock.prediction.data.kalman.apc.initialerrorvalue", new Double(50),
          "Initial Kalman error value to use to start filter.");

  private static final Logger logger = LoggerFactory.getLogger(ApcStopTimeGenerator.class);

  @Override
  public long getStopTimeForPath(Indices indices, AvlReport avlReport, VehicleState vehicleState) {
    if (!hasApcData()) {
      logger.debug("exiting apc dwell time, no apc data");
      logMiss();
      return super.getStopTimeForPath(indices, avlReport, vehicleState);
    }
    Double passengerArrivalRateInSeconds = getArrivalsPerSecond(indices, vehicleState);
    if (passengerArrivalRateInSeconds == null) {
      // we didn't have enough information, fall back on default impl
      logger.debug("exiting apc dwell time, no passenger arrival rate");
      getMonitoring().rateMetric("PredictionDwellApcHit", false);
      logMiss();
      return super.getStopTimeForPath(indices, avlReport, vehicleState);
    } else {
      getMonitoring().rateMetric("PredictionDwellApcHit", true);
    }
    Long currentHeadwayInSeconds = getHeadwayInSeconds(vehicleState, indices);
    if (currentHeadwayInSeconds == null) {
      logMiss();
      getMonitoring().rateMetric("PredictionDwellHeadwayHit", false);
      return super.getStopTimeForPath(indices, avlReport, vehicleState);
    } else {
      getMonitoring().rateMetric("PredictionDwellHeadwayHit", true);
    }
    double passengerBoardingTime = getPassengerBoardingTime(vehicleState);
    long dwellTime = new Double(passengerArrivalRateInSeconds * currentHeadwayInSeconds * passengerBoardingTime).longValue();
    logger.debug("dwellTime={} = passengerArrivalRateInSeconds={} * currentHeadway={} * passengerBoardingTime={}",
            dwellTime, passengerArrivalRateInSeconds, currentHeadwayInSeconds, passengerBoardingTime);

    List<Double> historicalDwellTime = getHistoricalDwellTime(indices, vehicleState, passengerBoardingTime);
    if (historicalDwellTime == null || historicalDwellTime.size() < 3) {
      logger.debug("exiting apc dwell time, no historical data");
      getMonitoring().rateMetric("PredictionDwellHistoryHit", false);
      logMiss();
      return super.getStopTimeForPath(indices, avlReport, vehicleState);
    } else {
      getMonitoring().rateMetric("PredictionDwellHistoryHit", true);
    }

    KalmanPredictionResult result = predict(dwellTime, historicalDwellTime, getLastPredictionError(indices));
    getKalmanErrorCache().putDwellErrorValue(indices, result.getFilterError());
    logHit();
    return new Double(result.getResult()).longValue();
  }

  private void logHit() {
    getMonitoring().rateMetric("PredictionDwellHit", true);
  }

  private void logMiss() {
    getMonitoring().rateMetric("PredictionDwellHit", false);
  }

  private double getLastPredictionError(Indices indices) {
    KalmanError result;
    try {
      result = getKalmanErrorCache().getDwellErrorValue(indices);
    } catch (Exception e) {
      result = null;
    }
    if (result == null) {
      return initialErrorValue.getValue();
    }
    return result.getError();
  }

  private boolean hasApcData() {
    return ApcDataProcessor.getInstance().isEnabled();
  }

  private Long getHeadwayInSeconds(VehicleState vehicleState, Indices indices) {
    try {
      Long headway = HistoricalPredictionLibrary.getHeadway(vehicleState, indices);
      if (headway != null) return headway / Time.MS_PER_SEC;
    } catch (Exception e) {
      logger.error("travel time lookup threw exception {}", e, e);
    }
    return null;
  }

  private KalmanPredictionResult predict(long dwellTime, List<Double> historicalDwellTimes,
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

    return result;
  }

  private double prediction(double gain, double loopGain, List<Double> historicalDwellTimes, long dwellTime, double average) {
    double averageHistoricalDuration = historicalAverage(historicalDwellTimes);
    double prediction = ((loopGain * dwellTime) + (gain * averageHistoricalDuration));
    logger.debug("(loopGain={} * dwellTime={}) + (gain={} * averageHistoricalDuration={}{}) = ({}) + ({}) = {}",
            loopGain, dwellTime, gain, averageHistoricalDuration, historicalDwellTimes,
            loopGain*dwellTime, gain*averageHistoricalDuration,
            prediction);
    return prediction;
  }

  private double filterError(double variance, double loopGain) {
    return variance*loopGain;
  }

  private double gain(double average, double variance, double lastPredictionError) {
    double gain = (lastPredictionError + variance) / (lastPredictionError + (2 * variance));
    logger.debug("(lastPredictionError={} + variance={}) / (lastPredictionError + (2 * variance) = {}/{} = {} ",
            lastPredictionError, variance,
            lastPredictionError + variance, lastPredictionError + 2*variance, gain);
    return gain;
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
      total = total + d;
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
    while (daysBack < maxKalmanDaysToSearch.getValue()) {
      daysBack++;
      currentArrivalTime = currentArrivalTime - Time.MS_PER_DAY;if (isSameCalendarType(arrivalTime, currentArrivalTime)) {
        Double arrivalRate = ApcModule.getInstance()
                .getBoardingsPerSecond(stopId,
                        new Date(currentArrivalTime));
        if (arrivalRate == null) {
          if (historicalDwells.size() < minKalmanDays.getValue()) {
            logger.debug("no historical boardings found for {} on stop {}", new Date(currentArrivalTime), stopId);
          }
          continue;
        }
        Long headway = getPreviousHeadway(stopId, currentArrivalTime, routeId);
        if (headway == null) {
          if (historicalDwells.size() < minKalmanDays.getValue()) {
            logger.debug("no historical headway found for {} on stop {}", new Date(currentArrivalTime), stopId);
          }
          continue;
        }
        double historicalDwellTime =
                arrivalRate * (headway / Time.MS_PER_SEC) * passengerBoardingTime;
        logger.debug("historicalDwellTime {} = arrivalRate={} * headway={} * passengerBoardingTime={} ",
                historicalDwellTime, arrivalRate, headway/Time.MS_PER_SEC, passengerBoardingTime);
        historicalDwells.add(historicalDwellTime);
        if (historicalDwells.size() > maxKalmanDays.getValue()) {
          return historicalDwells;
        }
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
  private Double getArrivalsPerSecond(Indices indices, VehicleState vehicleState) {
    String stopId = indices.getStopPath().getStopId();

    Long arrivalTime = getScheduledArrivalTime(indices, vehicleState);
    if (arrivalTime == null) return null;
    Double arrivalRate = ApcModule.getInstance().getBoardingsPerSecond(stopId, new Date(arrivalTime));

    if (arrivalRate != null)
      return arrivalRate;
    // if we don't have any data assume no boarding during this period
    return 0.0;
  }

  private Long getScheduledArrivalTime(Indices indices, VehicleState vehicleState) {
    ScheduleTime st = indices.getScheduleTime();
    if (st == null) return null;
    if (vehicleState == null) return null;
    return Core.getInstance().getTime()
            .getEpochTime(indices.getScheduleTime().getTime(), vehicleState.getAvlReport().getTime());
  }

  private double getPassengerBoardingTime(VehicleState vehicleState) {
    // TODO - this will be heuristic derived from vehicleType
    return 2.5 * Time.MS_PER_SEC; // seconds per passenger
  }


}
