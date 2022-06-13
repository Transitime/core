package org.transitclock.core.predictiongenerator.scheduled.traveltime.kalman;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.BooleanConfigValue;
import org.transitclock.config.DoubleConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.core.*;
import org.transitclock.core.dataCache.*;
import org.transitclock.core.predictiongenerator.HistoricalPredictionLibrary;
import org.transitclock.core.predictiongenerator.PredictionComponentElementsGenerator;
import org.transitclock.core.predictiongenerator.kalman.*;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.db.structs.PredictionEvent;
import org.transitclock.db.structs.PredictionForStopPath;
import org.transitclock.db.structs.TrafficSensorData;
import org.transitclock.utils.DateUtils;
import org.transitclock.utils.IntervalTimer;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * This is a prediction generator that uses a Kalman filter to provide predictions.
 *  It uses historical average in combination with the a headway vehicle when
 *  enough data is present to support a Kalman filter.
 *
 * @see KalmanPrediction for the research paper and description of the terms
 *
 *
 * @author Sean Óg Crudden
 * @author sheldonabrown
 */
public class KalmanPredictionGeneratorImpl extends PredictionGeneratorDefaultImpl
        implements PredictionComponentElementsGenerator {

  private String alternative="PredictionGeneratorDefaultImpl";

  private static final IntegerConfigValue minKalmanDays = new IntegerConfigValue(
          "transitclock.prediction.data.kalman.mindays", new Integer(3),
          "Min number of days trip data that needs to be available before Kalman prediction is used instead of default transitClock prediction.");

  private static final IntegerConfigValue maxKalmanDays = new IntegerConfigValue(
          "transitclock.prediction.data.kalman.maxdays", new Integer(3),
          "Max number of historical days trips to include in Kalman prediction calculation.");

  private static final IntegerConfigValue maxKalmanDaysToSearch = new IntegerConfigValue(
          "transitclock.prediction.data.kalman.maxdaystosearch", new Integer(21),
          "Max number of days to look back for data. This will also be effected by how old the data in the cache is.");

  private static final DoubleConfigValue initialErrorValue = new DoubleConfigValue(
          "transitclock.prediction.data.kalman.initialerrorvalue", new Double(100),
          "Initial Kalman error value to use to start filter.");

  /* May be better to use the default implementation as it splits things down into segments. */
  private static final BooleanConfigValue useKalmanForPartialStopPaths = new BooleanConfigValue (
          "transitclock.prediction.data.kalman.usekalmanforpartialstoppaths", new Boolean(true),
          "Will use Kalman prediction to get to first stop of prediction."
  );

  private static final IntegerConfigValue percentagePredictionMethodDifferenceEventLog =new IntegerConfigValue(
          "transitclock.prediction.data.kalman.percentagePredictionMethodDifference", new Integer(50),
          "If the difference in prediction method estimates is greater than this percentage log a Vehicle Event");

  private static final IntegerConfigValue tresholdForDifferenceEventLog=new IntegerConfigValue(
          "transitclock.prediction.data.kalman.tresholdForDifferenceEventLog", new Integer(60000),
          "This is the threshold in milliseconds that the difference has to be over before it will consider the percentage difference.");

  private static final Logger logger = LoggerFactory.getLogger(KalmanPredictionGeneratorImpl.class);

  /*
   * return a prediction for the travel of the given vehicle.  If enough data is present this
   * will be a kalman weighted prediction, otherwise it will default to the basic prediction
   * algorithm.
   *
   * @see
   * org.transitclock.core.PredictionGeneratorDefaultImpl#getTravelTimeForPath
   * (org.transitclock.core.Indices, org.transitclock.db.structs.AvlReport)
   */
  @Override
  public PredictionResult getTravelTimeForPath(Indices indices, AvlReport avlReport, VehicleState currentVehicleState) {

    IntervalTimer kalmanTimer = new IntervalTimer();
    logger.debug("Calling Kalman prediction algorithm for : "+indices.toString());
    PredictionResult alternatePrediction = super.getTravelTimeForPath(indices, avlReport, currentVehicleState);

    try {
      // travel times of vehicle one (or more) headways in front of us on this segment
      TravelTimeDetails headwayTravelTimes = getLastVehicleTravelTime(currentVehicleState, indices);

      /*
       * The first vehicle of the day should use schedule or historic data to
       * make prediction. Cannot use Kalman as yesterdays vehicle will have
       * little to say about today's.
       */
      if (headwayTravelTimes!=null) {
        getMonitoring().rateMetric("PredictionKalmanHeadwayHit", true);
        logger.debug("Kalman has last vehicle info for : " +indices.toString()+ " : "+headwayTravelTimes);

        // lookup historical travel times for this trip independent of vehicle
        List<TravelTimeDetails> historicalTravelTimes = getHistoricalTravelTimes(avlReport, indices, currentVehicleState);
        /*
         * if we have enough data start using Kalman filter otherwise revert
         * to base class for prediction.
         */
        if (historicalTravelTimes != null && historicalTravelTimes.size() >= minKalmanDays.getValue().intValue()) {
          getMonitoring().rateMetric("PredictionKalmanHistoryHit", true);
          getMonitoring().averageMetric("PredictionKalmanHistorySize", historicalTravelTimes.size());
          logger.debug("Generating Kalman prediction for : "+indices.toString());

          try {
            KalmanPrediction kalmanPrediction = new KalmanPrediction();
            LinkTravelTimes linkTravelTimes = generateLinkTravelTimes(avlReport, historicalTravelTimes, headwayTravelTimes, indices);
            Indices headwayVehicleIndices = new Indices(headwayTravelTimes.getArrival());
            KalmanError headwayError = getKalmanErrorForIndices(getKalmanErrorCache(), headwayVehicleIndices);

            // perform the adjustment based on the history retrieved and the headway as the realtime input
            KalmanPredictionResult kalmanPredictionResult = kalmanPrediction.predict(linkTravelTimes.getLastVehicleSegment(),
                    linkTravelTimes.getHistoricalSegments(),
                    headwayError.getError());

            long predictionTime = (long) kalmanPredictionResult.getResult();

            getKalmanErrorCache().putErrorValue(indices, kalmanPredictionResult.getFilterError());
            logPredictionEvent(avlReport, headwayTravelTimes, currentVehicleState, predictionTime, alternatePrediction.getPrediction());

            logger.debug("Using Kalman prediction: " + predictionTime + " instead of "+alternative+" prediction: "
                    + alternatePrediction +" for : " + indices.toString());

            storePrediction(currentVehicleState, indices, predictionTime);

            getMonitoring().rateMetric("PredictionKalmanHit", true);
            getMonitoring().sumMetric("PredictionGenerationKalman");
            getMonitoring().averageMetric("PredictionKalmanProcessingTime", kalmanTimer.elapsedMsec());
            return new PredictionResult(predictionTime, Algorithm.KALMAN);

          } catch (Exception e) {
            logger.error("Exception {}",  e.toString(), e);
          }
        } else {
          getMonitoring().rateMetric("PredictionKalmanHistoryHit", false);
          if (historicalTravelTimes == null)
            getMonitoring().averageMetric("PredictionKalmanHistorySize", 0.0);
          else
            getMonitoring().averageMetric("PredictionKalmanHistorySize", historicalTravelTimes.size());
        }
      } else {
        // no travel time
        logger.debug("no travel times for trip {}", currentVehicleState.getTrip().getId());
        getMonitoring().rateMetric("PredictionKalmanHeadwayHit", false);
      }
    } catch (Exception e) {
			logger.error("getTravelTimeForPath exception {}", e, e);
    }
    // instrument kalman miss
    getMonitoring().rateMetric("PredictionKalmanHit", false);
    return alternatePrediction;
  }

  @Override
  public long expectedTravelTimeFromMatchToEndOfStopPath(AvlReport avlReport, SpatialMatch match) {

    if(useKalmanForPartialStopPaths.getValue().booleanValue())
    {

      VehicleState currentVehicleState = getVehicleStateManager().getVehicleState(avlReport.getVehicleId());

      PredictionResult fulltime = this.getTravelTimeForPath(match.getIndices(), avlReport, currentVehicleState);

      double distanceAlongStopPath = match.getDistanceAlongStopPath();

      double stopPathLength =
              match.getStopPath().getLength();

      long remainingtime = (long) (fulltime.getPrediction() * ((stopPathLength-distanceAlongStopPath)/stopPathLength));

      logger.debug("Using Kalman for first stop path {} with value {} instead of {}.", match.getIndices(), remainingtime, super.expectedTravelTimeFromMatchToEndOfStopPath(avlReport, match));

      return remainingtime;
    }else
    {
      return super.expectedTravelTimeFromMatchToEndOfStopPath(avlReport, match);
    }
  }

  protected List<TravelTimeDetails> getHistoricalTravelTimes(AvlReport avlReport, Indices indices, VehicleState currentVehicleState) {
    Date nearestDay = DateUtils.truncate(avlReport.getDate(), Calendar.DAY_OF_MONTH);
    List<TravelTimeDetails> historicalTravelTimes =
            HistoricalPredictionLibrary.getHistoricalTravelTimes(
                    getTripCache(),
                    currentVehicleState.getTrip().getRouteId(),
                    currentVehicleState.getTrip().getDirectionId(),
                    indices.getStopPathIndex(),
                    nearestDay,
                    currentVehicleState.getTrip().getStartTime(),
                    maxKalmanDaysToSearch.getValue(),
                    maxKalmanDays.getValue());
    if(historicalTravelTimes!=null) {
      logger.debug("Kalman has " +historicalTravelTimes.size()+ " historical values for : " +indices.toString());
    }

    return historicalTravelTimes;
  }

  private void storePrediction(VehicleState vehicleState, Indices indices, long predictionTime) {
    if(storeTravelTimeStopPathPredictions.getValue())
    {
      PredictionForStopPath predictionForStopPath=new PredictionForStopPath(
              vehicleState.getVehicleId(),
              new Date(Core.getInstance().getSystemTime()),
              new Double(new Long(predictionTime).intValue()),
              indices.getTrip().getId(),
              indices.getStopPathIndex(),
              "KALMAN",
              true,
              null);
      Core.getInstance().getDbLogger().add(predictionForStopPath);
      StopPathPredictionCacheFactory.getInstance().putPrediction(predictionForStopPath);
    }

  }

  private void logPredictionEvent(AvlReport avlReport, TravelTimeDetails travelTimeDetails, VehicleState vehicleState, long predictionTime, long alternatePrediction) {

    double percentageDifferecence = Math.abs(100 * ((predictionTime - alternatePrediction) / (double)alternatePrediction));

    if (!Double.isInfinite(percentageDifferecence))
      getMonitoring().averageMetric("PredictionKalmanAverageDifference", Math.abs(percentageDifferecence));

    if(((percentageDifferecence *  alternatePrediction)/100) > tresholdForDifferenceEventLog.getValue())
    {
      if(percentageDifferecence > percentagePredictionMethodDifferenceEventLog.getValue())
      {
        String description="Kalman predicts : "+predictionTime+" Super predicts : "+alternatePrediction;

        logger.warn(description);

        PredictionEvent.create(avlReport, vehicleState.getMatch(), PredictionEvent.PREDICTION_VARIATION, description,
                travelTimeDetails.getArrival().getStopId(),
                travelTimeDetails.getDeparture().getStopId(),
                travelTimeDetails.getArrival().getVehicleId(),
                travelTimeDetails.getArrival().getTime(),
                travelTimeDetails.getDeparture().getTime());
      }
    }

  }

  private LinkTravelTimes generateLinkTravelTimes(AvlReport avlReport,
                                                  List<TravelTimeDetails> lastDaysTimes,
                                                  TravelTimeDetails travelTimeDetails,
                                                  Indices indices) {
    Vehicle vehicle = new Vehicle(avlReport.getVehicleId());

    Long trafficTravelTime = getTrafficForIndices(indices);
    if (logger.isInfoEnabled() && trafficTravelTime != null) {
      TrafficSensorData sensorData = TrafficManager.getInstance().getTrafficSensorDataForStopPath(indices.getStopPath());
      double length = indices.getStopPath().getLength();
      long busTravelTime = lastDaysTimes.get(0).getTravelTime();
      Double trafficSpeedInMPH = sensorData.getSpeed() * 2.237  /* m/s to mph */;
      Double busSpeedInMPH = length / busTravelTime * 1000 * 2.237  /* m/s to mph */;

      if (trafficTravelTime != null && trafficTravelTime > 0) {
        logger.info("traffic adjusted speed {}mph, bus speed {}mph, {}% diff",
                trafficSpeedInMPH,
                busSpeedInMPH,
                (trafficSpeedInMPH - busSpeedInMPH)/busSpeedInMPH);
      }
    }

    VehicleStopDetail originDetail = new VehicleStopDetail(null, 0, 0l, vehicle);
    TripSegment[] historical_segments_k = new TripSegment[lastDaysTimes.size()];
    for (int i = 0; i < lastDaysTimes.size() && i < maxKalmanDays.getValue(); i++) {
      // We don't have historical AVL times so guess at it based on now
      Long historicalAvlTime = DateUtils.addDays(new Date(avlReport.getTime()), -1 * i).getTime();
      logger.debug("Kalman is using historical value : "+lastDaysTimes.get(i) +" for : " + indices.toString());
      VehicleStopDetail destinationDetail = new VehicleStopDetail(null, lastDaysTimes.get(i).getTravelTime(),
              getTrafficHistory(indices, historicalAvlTime),
              vehicle);
      // TODO: why do we insert into array in reverse order?
      historical_segments_k[lastDaysTimes.size()-i-1] = new TripSegment(originDetail, destinationDetail);
    }
    VehicleStopDetail destinationDetail_0_k_1 = new VehicleStopDetail(null, travelTimeDetails.getTravelTime(), trafficTravelTime, vehicle);
    TripSegment ts_day_0_k_1 = new TripSegment(originDetail, destinationDetail_0_k_1);
    TripSegment last_vehicle_segment = ts_day_0_k_1;
    return new LinkTravelTimes(last_vehicle_segment, historical_segments_k);
  }

  private Long getTrafficHistory(Indices indices, Long historicalTime) {
    if (!isTrafficDataEnabled() || historicalTime == null) return null;
    if (TrafficManager.getInstance().hasTrafficData(indices.getStopPath())) {
      return TrafficManager.getInstance().getHistoricalTravelTime(indices.getStopPath(), historicalTime);
    }
    return null;
  }

  private boolean isTrafficDataEnabled() {
    return TrafficManager.trafficDataEnabled.getValue();
  }

  private Long getTrafficForIndices(Indices indices) {
    if (!isTrafficDataEnabled()) return null;
    // find stopPath, see if there is a traffic path for it
    // if so, retrieve the traffic travel time for that segment
    if (TrafficManager.getInstance().hasTrafficData(indices.getStopPath())) {
      return TrafficManager.getInstance().getTravelTime(indices.getStopPath());
    }
    return null;
  }

  private KalmanError getKalmanErrorForIndices(ErrorCache cache, Indices indices) {

    KalmanError result;
    try {
      result = cache.getErrorValue(indices);
      if(result==null)
      {
				logger.debug("Kalman Error value set to default: "+initialErrorValue.getValue() +" for indices: " + indices.toString());
        result=new KalmanError(initialErrorValue.getValue());
      }
      return result;
    } catch (Exception e) {
			logger.error("Exception {} retrieving from cache with indices {}",
							e, indices, e);
    }
    return new KalmanError(initialErrorValue.getValue());
  }




  /**
   * protected for unit test overrides.
   * @return
   */
  protected VehicleStateManager getVehicleStateManager() {
    return VehicleStateManager.getInstance();
  }

  /**
   * protected for unit test overrides.
   * @return
   */
  protected TripDataHistoryCacheInterface getTripCache() {
    return TripDataHistoryCacheFactory.getInstance();
  }

  /**
   * protected for unit test overrides.
   * @return
   */
  protected ErrorCache getKalmanErrorCache() {
    return ErrorCacheFactory.getInstance();
  }

  /**
   * protected for unit test overrides.
   * @return
   */
  protected TravelTimeDetails getLastVehicleTravelTime(VehicleState currentVehicleState, Indices indices)
          throws Exception {
    return HistoricalPredictionLibrary.getLastVehicleTravelTime(currentVehicleState, indices);
  }

  /**
   * Current and historical link travel times.
   */
  private static class LinkTravelTimes {
    private TripSegment lastVehicleSegment;
    private TripSegment[] historicalSegments;
    public LinkTravelTimes(TripSegment last_vehicle_segment, TripSegment[] historical_segments_k) {
      this.lastVehicleSegment = last_vehicle_segment;
      this.historicalSegments = historical_segments_k;
    }
    public TripSegment getLastVehicleSegment() { return lastVehicleSegment; }
    public TripSegment[] getHistoricalSegments() { return historicalSegments; }
  }
}
