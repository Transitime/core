package org.transitclock.core.predictiongenerator.kalman;

import org.transitclock.config.BooleanConfigValue;

/**
 * The theory behind the Kalman Filter application to link travel times is provided
 * https://scholarcommons.usf.edu/cgi/viewcontent.cgi?article=1342&context=jpt
 *
 * From the above paper we need to understand the following terminology:
 * * "g" (gain) equals the filter gain
 * * "a" (loopGain) is the loop gain
 * * "e" (error/lastPredictionError) represents filter error
 * * "p" (prediction) equals prediction
 * * art(k) (lastVehicleDuration) is actual running time of the previous bus at instant (k)
 * * art1(k+1) (historicalDuration) is actual running time of the previous day at instant (k+1) (we use average here instead to dampen)
 * * VAR[data out] equals the prediction variance
 * * VAR[data in] is the last three days “art3(k+1), art2(k+1) and art1(k+1)” variance
 *
 *  With that, the predict method below implements P(k +1) according to:
 *
 *  gain equation:
 *  g(k+1) = (e(k) + VAR[local]) / (e(k) + 2 * VAR(local))
 *  as code:
 *  gain = (lastPredictionError + variance) / (lastPredictionError + ( 2 * variance ))
 *
 *  loop gain equation:
 *  a(k + 1) = 1 – g(k + 1)
 *  as code:
 *  loopGain = 1 - lastPredictionError
 *
 *  error equation:
 *  e(k + 1) = VAR[datain] * g(k + 1)
 *  as code:
 *  filterError = variance * loopGain
 *
 *  prediction equation:
 *  P(k + 1) = a(k+1) * art(k) + g(k+1) * art1(k + 1)
 *  as code:
 *  prediction = (loopGain * lastVehicleDuration)+(gain * historicalDuration)
 *
 *
 * @author Sean Óg Crudden
 *
 */
public class KalmanPrediction {

  private static final BooleanConfigValue useAverage = new BooleanConfigValue (
          "transitclock.prediction.kalman.useaverage", new Boolean(true),
          "Will use average travel time as opposed to last historical vehicle in Kalman prediction calculation."
  );

  /**
   * @param lastVehicleSegment The last vehicle info for the time taken to cover the same segment
   * @param historicalSegments The last 3 days for info relating to the time taken for the vehicle handling the same service/trip
   * @param lastPredictionError From the previous segments calculation result
   * @return KalmanPredictionResult contains the predicted time and the lastPredictionError to be used in the next prediction calculation
   * @throws Exception
   */
  public KalmanPredictionResult predict(TripSegment lastVehicleSegment,
                                        TripSegment historicalSegments[],
                                        double lastPredictionError) throws Exception {
    double average = historicalAverage(historicalSegments);
    double variance = historicalVariance(historicalSegments, average);
    double gain = gain(average, variance, lastPredictionError );
    double loopGain = 1 - gain;

    return new KalmanPredictionResult(
            prediction(
                    gain,
                    loopGain,
                    historicalSegments,
                    lastVehicleSegment,
                    average),
            filterError(variance, gain));
  }

  private double historicalAverage(TripSegment historicalSegments[]) throws Exception {
    if (historicalSegments.length>0) {
      long total=0;
      for(int i=0;i<historicalSegments.length;i++) {
        long duration = historicalSegments[i].getDestination().getTime() - historicalSegments[i].getOrigin().getTime();
        total=total+duration;
      }
      return (double) (total / historicalSegments.length);
    } else {
      throw new Exception("Cannot average nothing");
    }
  }

  private double historicalVariance(TripSegment historicalSegments[], double average) {
    double total=0;

    for(int i=0;i<historicalSegments.length;i++) {
      long duration = historicalSegments[i].getDestination().getTime() - historicalSegments[i].getOrigin().getTime();

      double diff = duration - average;
      double longDiffSquared=diff*diff;
      total=total+longDiffSquared;
    }
    return total/historicalSegments.length;
  }

  private double filterError(double variance, double loopGain) {
    return variance*loopGain;
  }

  private double gain(double average, double variance, double lastPredictionError ) {
    double gain = (lastPredictionError + variance) / (lastPredictionError + (2 * variance));
    return gain;
  }

  private double prediction(double gain,
                            double loopGain,
                            TripSegment historicalSegments[],
                            TripSegment lastVehicleSegment,
                            double averageDuration) {

    double historicalDuration=averageDuration;

    /* This may be better use the historical average rather than just the vehicle on previous day. This would damping issues with last days value being dramatically different. */
    if(useAverage.getValue()==false) {
      historicalDuration = historicalSegments[historicalSegments.length-1].getDestination().getTime()
              - historicalSegments[historicalSegments.length-1].getOrigin().getTime();
    }
    long lastVehicleDuration=lastVehicleSegment.getDestination().getTime() - lastVehicleSegment.getOrigin().getTime();

    return (loopGain * lastVehicleDuration) + (gain * historicalDuration);
  }

  public static void main(String [ ] args) {
    KalmanPrediction kalmanPrediction = new KalmanPrediction();

    Vehicle vehicle=new Vehicle("RIY 30");

    VehicleStopDetail originDetail=new VehicleStopDetail(null, 0, vehicle);
    VehicleStopDetail destinationDetail_1_k=new VehicleStopDetail(null, 380, vehicle);
    VehicleStopDetail destinationDetail_2_k=new VehicleStopDetail(null, 420, vehicle);
    VehicleStopDetail destinationDetail_3_k=new VehicleStopDetail(null, 400, vehicle);

    VehicleStopDetail destinationDetail_0_k_1=new VehicleStopDetail(null, 300, vehicle);


    TripSegment ts_day_1_k=new TripSegment(originDetail, destinationDetail_1_k);
    TripSegment ts_day_2_k=new TripSegment(originDetail, destinationDetail_2_k);
    TripSegment ts_day_3_k=new TripSegment(originDetail, destinationDetail_3_k);

    TripSegment ts_day_0_k_1=new TripSegment(originDetail, destinationDetail_0_k_1);

    TripSegment historicalSegments_k[]={ts_day_1_k, ts_day_2_k, ts_day_3_k};

    TripSegment lastVehicleSegment=ts_day_0_k_1;

    try {
      KalmanPredictionResult result = kalmanPrediction.predict(lastVehicleSegment, historicalSegments_k,  72.40);

      if(result!=null) {
        if((result.getResult() > 355 && result.getResult() < 356) && (result.getFilterError()>149 && result.getFilterError()<150))
        {
          System.out.println("Successful Kalman Filter Prediction.");
        } else {
          System.out.println("UnSuccessful Kalman Filter Prediction.");
        }
      } else {
        System.out.println("No result.");
      }
    } catch (Exception e) {
      System.out.println("Whoops");
      e.printStackTrace();
    }
  }
}
