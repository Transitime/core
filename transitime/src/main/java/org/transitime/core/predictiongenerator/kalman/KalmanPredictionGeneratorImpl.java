package org.transitime.core.predictiongenerator.kalman;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.config.DoubleConfigValue;
import org.transitime.config.IntegerConfigValue;
import org.transitime.core.Indices;
import org.transitime.core.PredictionGeneratorDefaultImpl;
import org.transitime.core.VehicleState;
import org.transitime.core.dataCache.KalmanErrorCache;
import org.transitime.core.dataCache.KalmanErrorCacheKey;
import org.transitime.core.dataCache.TripDataHistoryCache;
import org.transitime.core.dataCache.VehicleStateManager;
import org.transitime.core.predictiongenerator.HistoricalPredictionLibrary;
import org.transitime.core.predictiongenerator.PredictionComponentElementsGenerator;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.PredictionForStopPath;

/**
 * @author Sean Og Crudden This is a prediction generator that uses a Kalman
 *         filter to provide predictions. It uses historical average while waiting on enough data to support a Kalman filter.
 */
public class KalmanPredictionGeneratorImpl extends PredictionGeneratorDefaultImpl
		implements PredictionComponentElementsGenerator {
	
	private String alternative="HistoricalAveragePredictionGeneratorImpl";
	/*
	 * TODO I think this needs to be a minimum of three and if just two will use
	 * historical value. 
	 */
	private static final IntegerConfigValue minKalmanDays = new IntegerConfigValue(
			"transitime.prediction.data.kalman.mindays", new Integer(3),
			"Min number of days trip data that needs to be available before Kalman prediciton is used instead of default transiTime prediction.");

	private static final IntegerConfigValue maxKalmanDays = new IntegerConfigValue(
			"transitime.prediction.data.kalman.maxdays", new Integer(3),
			"Max number of historical days trips to include in Kalman prediction calculation.");

	private static final IntegerConfigValue maxKalmanDaysToSearch = new IntegerConfigValue(
			"transitime.prediction.data.kalman.maxdaystoseach", new Integer(21),
			"Max number of days to look back for data. This will also be effected by how old the data in the cache is.");
	
	private static final DoubleConfigValue initialErrorValue = new DoubleConfigValue(
			"transitime.prediction.data.kalman.initialerrorvalue", new Double(100),
			"Initial Kalman error value to use to start filter.");

	private static final Logger logger = LoggerFactory.getLogger(KalmanPredictionGeneratorImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.transitime.core.PredictionGeneratorDefaultImpl#getTravelTimeForPath
	 * (org.transitime.core.Indices, org.transitime.db.structs.AvlReport)
	 */
	@Override
	public long getTravelTimeForPath(Indices indices, AvlReport avlReport) {

		logger.debug("Calling Kalman prediction algorithm for : "+indices.toString());
		
		TripDataHistoryCache tripCache = TripDataHistoryCache.getInstance();

		KalmanErrorCache kalmanErrorCache = KalmanErrorCache.getInstance();
		
		VehicleStateManager vehicleStateManager = VehicleStateManager.getInstance();

		VehicleState currentVehicleState = vehicleStateManager.getVehicleState(avlReport.getVehicleId());		
	
		long time = 0;	

		time = HistoricalPredictionLibrary.getLastVehicleTravelTime(currentVehicleState, indices);
		/*
		 * The first vehicle of the day should use schedule or historic data to
		 * make prediction. Cannot use Kalman as yesterdays vehicle will have
		 * little to say about todays.
		 */
		if (time > -1) {

			logger.debug("Kalman has last vehicle info for : " +indices.toString());
			
			
			
			Date nearestDay = DateUtils.truncate(Calendar.getInstance().getTime(), Calendar.DAY_OF_MONTH);

			List<Integer> lastDaysTimes = HistoricalPredictionLibrary.lastDaysTimes(tripCache, currentVehicleState.getTrip().getId(),
					indices.getStopPathIndex(), nearestDay, currentVehicleState.getTrip().getStartTime(),
					maxKalmanDaysToSearch.getValue(), minKalmanDays.getValue());
												
			if(lastDaysTimes!=null)
			{												
				logger.debug("Kalman has " +lastDaysTimes.size()+ " historical values for : " +indices.toString());
			}
			/*
			 * if we have enough data start using Kalman filter otherwise revert
			 * to extended class for prediction. 
			 */	
			if (lastDaysTimes != null && lastDaysTimes.size() >= minKalmanDays.getValue().intValue()) {

				logger.debug("Generating Kalman prediction for : "+indices.toString());
				
				try {

					KalmanPrediction kalmanPrediction = new KalmanPrediction();

					KalmanPredictionResult kalmanPredictionResult;

					Vehicle vehicle = new Vehicle(avlReport.getVehicleId());

					VehicleStopDetail originDetail = new VehicleStopDetail(null, 0, vehicle);
					TripSegment[] historical_segments_k = new TripSegment[lastDaysTimes.size()];
					for (int i = 0; i < lastDaysTimes.size() && i < maxKalmanDays.getValue(); i++) {
						VehicleStopDetail destinationDetail = new VehicleStopDetail(null, lastDaysTimes.get(i),
								vehicle);
						historical_segments_k[i] = new TripSegment(originDetail, destinationDetail);
					}

					VehicleStopDetail destinationDetail_0_k_1 = new VehicleStopDetail(null, time, vehicle);

					TripSegment ts_day_0_k_1 = new TripSegment(originDetail, destinationDetail_0_k_1);

					TripSegment last_vehicle_segment = ts_day_0_k_1;
								
					Indices previousVehicleIndices = HistoricalPredictionLibrary.getLastVehicleIndices(currentVehicleState, indices);
					
					Double last_prediction_error = lastVehiclePredictionError(kalmanErrorCache, previousVehicleIndices);
					
					for(int i=0;i<historical_segments_k.length;i++)
					{
						logger.debug("Using historical value: " + historical_segments_k[i].getDuration() + " for : "+new KalmanErrorCacheKey(indices).toString());	
					}
					
					logger.debug("Using error value: " + last_prediction_error + " from: "+new KalmanErrorCacheKey(previousVehicleIndices).toString());
					
					//TODO this should also display the detail of which vehicle it choose as the last one.
					logger.debug("Using last vehicle value: " + time + " for : "+ indices.toString());
					
					kalmanPredictionResult = kalmanPrediction.predict(last_vehicle_segment, historical_segments_k,
							last_prediction_error);

					long predictionTime = (long) kalmanPredictionResult.getResult();

					logger.debug("Setting Kalman error value: " + kalmanPredictionResult.getFilterError() + " for : "+ new KalmanErrorCacheKey(indices).toString());
					
					kalmanErrorCache.putErrorValue(indices, kalmanPredictionResult.getFilterError());

					logger.debug("Using Kalman prediction: " + predictionTime + " instead of "+alternative+" prediction: "
							+ super.getTravelTimeForPath(indices, avlReport) +" for : " + indices.toString());
					
					if(storeTravelTimeStopPathPredictions.getValue())
					{
						PredictionForStopPath predictionForStopPath=new PredictionForStopPath(Calendar.getInstance().getTime(), new Double(new Long(predictionTime).intValue()), indices.getTrip().getId(), indices.getStopPathIndex(), "KALMAN");					
						Core.getInstance().getDbLogger().add(predictionForStopPath);
					}													
					return predictionTime;
					
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}	
		return super.getTravelTimeForPath(indices, avlReport);
	}

	private Double lastVehiclePredictionError(KalmanErrorCache cache, Indices indices) {		
		Double result = cache.getErrorValue(indices);
		if(result!=null)
		{
			logger.debug("Kalman Error value : "+result +" for key: "+new KalmanErrorCacheKey(indices).toString());
		}
		else
		{
			logger.debug("Kalman Error value set to default: "+initialErrorValue.getValue() +" for key: "+new KalmanErrorCacheKey(indices).toString());
			return initialErrorValue.getValue();
		}
		return result;
	}

	@Override
	public long getStopTimeForPath(Indices indices, AvlReport avlReport) {
		// TODO Auto-generated method stub
		return super.getStopTimeForPath(indices, avlReport);
	}
	
	@Override
	public boolean hasDataForPath(Indices indices, AvlReport avlReport) {

		TripDataHistoryCache tripCache = TripDataHistoryCache.getInstance();		
		VehicleStateManager vehicleStateManager = VehicleStateManager.getInstance();
		VehicleState currentVehicleState = vehicleStateManager.getVehicleState(avlReport.getVehicleId());		
	
		long time = HistoricalPredictionLibrary.getLastVehicleTravelTime(currentVehicleState, indices);
		/*
		 * The first vehicle of the day should use schedule or historic data to
		 * make prediction. Cannot use Kalman as yesterdays vehicle will have
		 * little to say about todays.
		 */
		if (time > -1) {
			Date nearestDay = DateUtils.truncate(Calendar.getInstance().getTime(), Calendar.DAY_OF_MONTH);
			List<Integer> lastDaysTimes = HistoricalPredictionLibrary.lastDaysTimes(tripCache, currentVehicleState.getTrip().getId(),
					indices.getStopPathIndex(), nearestDay, currentVehicleState.getTrip().getStartTime(),
					maxKalmanDaysToSearch.getValue(), minKalmanDays.getValue());
			
			return (lastDaysTimes != null && lastDaysTimes.size() >= minKalmanDays.getValue().intValue());
		}
		
		return false;
	}
}
