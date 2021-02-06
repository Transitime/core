package org.transitclock.core.predictiongenerator.frequency.traveltime.kalman;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.BooleanConfigValue;
import org.transitclock.config.DoubleConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.core.Indices;
import org.transitclock.core.SpatialMatch;
import org.transitclock.core.TravelTimeDetails;
import org.transitclock.core.VehicleState;
import org.transitclock.core.dataCache.*;
import org.transitclock.core.dataCache.frequency.FrequencyBasedHistoricalAverageCache;
import org.transitclock.core.predictiongenerator.HistoricalPredictionLibrary;
import org.transitclock.core.predictiongenerator.PredictionComponentElementsGenerator;
import org.transitclock.core.predictiongenerator.frequency.traveltime.average.HistoricalAveragePredictionGeneratorImpl;
import org.transitclock.core.predictiongenerator.kalman.*;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.db.structs.PredictionEvent;
import org.transitclock.db.structs.PredictionForStopPath;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * This is a prediction generator that uses a Kalman
 *  filter to provide predictions for a frequency based service.
 *
 *  @see KalmanPrediction for the research paper and description of the terms
 * @author Sean Óg Crudden
 * @author sheldonabrown
 */
public class KalmanPredictionGeneratorImpl extends HistoricalAveragePredictionGeneratorImpl
        implements PredictionComponentElementsGenerator {


  private String alternative="LastVehiclePredictionGeneratorImpl";

  private static final IntegerConfigValue minKalmanDays = new IntegerConfigValue(
          "transitclock.prediction.data.kalman.mindays", new Integer(3),
          "Min number of days trip data that needs to be available before Kalman prediciton is used instead of default transiTime prediction.");

  private static final IntegerConfigValue maxKalmanDays = new IntegerConfigValue(
          "transitclock.prediction.data.kalman.maxdays", new Integer(3),
          "Max number of historical days trips to include in Kalman prediction calculation.");

  private static final IntegerConfigValue maxKalmanDaysToSearch = new IntegerConfigValue(
          "transitclock.prediction.data.kalman.maxdaystosearch", new Integer(30),
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
  public long getTravelTimeForPath(Indices indices, AvlReport avlReport, VehicleState currentVehicleState) {

    logger.debug("Calling frequency based Kalman prediction algorithm for : "+indices.toString());
    long alternatePrediction = super.getTravelTimeForPath(indices, avlReport, currentVehicleState);

    Integer time = FrequencyBasedHistoricalAverageCache.secondsFromMidnight(avlReport.getDate(),2);

    time = FrequencyBasedHistoricalAverageCache.round(
            time,
            FrequencyBasedHistoricalAverageCache.getCacheIncrementsForFrequencyService());


    try {
      TravelTimeDetails headwayTravelTimes = getLastVehicleTravelTime(currentVehicleState, indices);

      /*
       * The first vehicle of the day should use schedule or historic data to
       * make prediction. Cannot use Kalman as yesterdays vehicle will have
       * little to say about todays.
       */
      if (headwayTravelTimes!=null) {
        getMonitoring().rateMetric("PredictionKalmanHeadwayHit", true);
        logger.debug("Kalman has last vehicle info for : " +indices.toString()+ " : "+headwayTravelTimes);

        List<TravelTimeDetails> historicalTravelTimes = getHistoricalTravelTimes(avlReport, indices, currentVehicleState, time);
        /*
         * if we have enough data start using Kalman filter otherwise revert
         * to extended class for prediction.
         */
        if (historicalTravelTimes != null && historicalTravelTimes.size() >= minKalmanDays.getValue().intValue()) {
          getMonitoring().rateMetric("PredictionKalmanHistoryHit", true);
          getMonitoring().averageMetric("PredictionKalmanHistorySize", historicalTravelTimes.size());
          logger.debug("Generating Kalman prediction for : "+indices.toString());

          try {
            KalmanPrediction kalmanPrediction = new KalmanPrediction();
            LinkTravelTimes linkTravelTimes = generateLinkTravelTimes(avlReport, historicalTravelTimes, headwayTravelTimes, indices);
            Indices headwayVehicleIndices = new Indices(headwayTravelTimes.getArrival());
            KalmanError headwayError = lastVehiclePredictionError(getKalmanErrorCache(), headwayVehicleIndices);
            logger.debug("Using error value: " + headwayError +" found with vehicle id "+headwayTravelTimes.getArrival().getVehicleId()+ " from: "+new KalmanErrorCacheKey(headwayVehicleIndices).toString());

            KalmanPredictionResult kalmanPredictionResult = kalmanPrediction.predict(linkTravelTimes.getLastVehicleSegment(),
                    linkTravelTimes.getHistoricalSegments(),
                    headwayError.getError());

            long predictionTime = (long) kalmanPredictionResult.getResult();
            logger.debug("Setting Kalman error value: " + kalmanPredictionResult.getFilterError() + " for : "+ new KalmanErrorCacheKey(indices).toString());
            getKalmanErrorCache().putErrorValue(indices, kalmanPredictionResult.getFilterError());
            logPredictionEvent(avlReport, headwayTravelTimes, currentVehicleState, predictionTime, alternatePrediction);

            logger.debug("Using Kalman prediction: " + predictionTime + " instead of "+alternative+" prediction: "
                    + alternatePrediction +" for : " + indices.toString());

            double percentageDifferecence = 100 * ((predictionTime - alternatePrediction) / (double)alternatePrediction);

            storePrediction(currentVehicleState, indices, predictionTime);

            // instrument kalman hit
            getMonitoring().rateMetric("PredictionKalmanHit", true);
            getMonitoring().sumMetric("PredictionGenerationKalman");
            return predictionTime;

          } catch (Exception e) {
            logger.error(e.getMessage(), e);
          }
        } else {
          getMonitoring().rateMetric("PredictionKalmanHistoryHit", false);
          if (historicalTravelTimes == null)
            getMonitoring().averageMetric("PredictionKalmanHistorySize", 0.0);
          else
            getMonitoring().averageMetric("PredictionKalmanHistorySize", historicalTravelTimes.size());
        }
      } else {
        getMonitoring().rateMetric("PredictionKalmanHeadwayHit", false);
      }
    } catch (Exception e) {
      logger.error("kalman prediction error", e);
    }
    // instrument kalman miss
    getMonitoring().rateMetric("PredictionKalmanHit", false);
    return alternatePrediction;
  }

  @Override
  public long expectedTravelTimeFromMatchToEndOfStopPath(AvlReport avlReport, SpatialMatch match) {

    if(useKalmanForPartialStopPaths.getValue().booleanValue())
    {
      VehicleStateManager vehicleStateManager = VehicleStateManager.getInstance();

      VehicleState currentVehicleState = vehicleStateManager.getVehicleState(avlReport.getVehicleId());

      long fulltime = this.getTravelTimeForPath(match.getIndices(), avlReport, currentVehicleState);

      double distanceAlongStopPath = match.getDistanceAlongStopPath();

      double stopPathLength =
              match.getStopPath().getLength();

      long remainingtime = (long) (fulltime * ((stopPathLength-distanceAlongStopPath)/stopPathLength));

      logger.debug("Using Kalman for first stop path {} with value {} instead of {}.", match.getIndices(), remainingtime, super.expectedTravelTimeFromMatchToEndOfStopPath(avlReport, match));

      return remainingtime;
    }else
    {
      return super.expectedTravelTimeFromMatchToEndOfStopPath(avlReport, match);
    }
  }



  private KalmanError lastVehiclePredictionError(ErrorCache cache, Indices indices) {

    KalmanError result = cache.getErrorValue(indices);
    if(result==null)
    {
      logger.debug("Kalman Error value set to default: "+initialErrorValue.getValue() +" for key: "+new KalmanErrorCacheKey(indices).toString());
      result=new KalmanError(initialErrorValue.getValue());
    }
    return result;
  }

  @Override
  public long getStopTimeForPath(Indices indices, AvlReport avlReport, VehicleState vehicleState) {
    long result=super.getStopTimeForPath(indices, avlReport, vehicleState);

    return result;

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

  protected List<TravelTimeDetails> getHistoricalTravelTimes(AvlReport avlReport,
                                                             Indices indices,
                                                             VehicleState currentVehicleState,
                                                             Integer time) {
    Date nearestDay = DateUtils.truncate(avlReport.getDate(), Calendar.DAY_OF_MONTH);
    List<TravelTimeDetails> historicalTravelTimes =
            HistoricalPredictionLibrary.getHistoricalTravelTimes(
                    getTripCache(),
                    currentVehicleState.getTrip().getId(),
                    currentVehicleState.getTrip().getDirectionId(),
                    indices.getStopPathIndex(),
                    nearestDay,
                    time,
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

  private LinkTravelTimes generateLinkTravelTimes(AvlReport avlReport,
                                                  List<TravelTimeDetails> lastDaysTimes,
                                                  TravelTimeDetails travelTimeDetails,
                                                  Indices indices) {

    Vehicle vehicle = new Vehicle(avlReport.getVehicleId());
    VehicleStopDetail originDetail = new VehicleStopDetail(null, 0, vehicle);
    TripSegment[] historical_segments_k = new TripSegment[lastDaysTimes.size()];
    for (int i = 0; i < lastDaysTimes.size() && i < maxKalmanDays.getValue(); i++) {
      logger.debug("Kalman is using historical value : "+lastDaysTimes.get(i) +" for : " + indices.toString());
      VehicleStopDetail destinationDetail = new VehicleStopDetail(null, lastDaysTimes.get(i).getTravelTime(),
              vehicle);
      // NOTE: schedule version inserts into array in reverse order
      historical_segments_k[i] = new TripSegment(originDetail, destinationDetail);
    }
    VehicleStopDetail destinationDetail_0_k_1 = new VehicleStopDetail(null, travelTimeDetails.getTravelTime(), vehicle);
    TripSegment ts_day_0_k_1 = new TripSegment(originDetail, destinationDetail_0_k_1);
    TripSegment last_vehicle_segment = ts_day_0_k_1;
    return new LinkTravelTimes(last_vehicle_segment, historical_segments_k);
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
