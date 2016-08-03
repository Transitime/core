package org.transitime.core.predictiongenerator.kalman;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.DoubleConfigValue;
import org.transitime.config.IntegerConfigValue;
import org.transitime.core.Indices;
import org.transitime.core.PredictionGeneratorDefaultImpl;
import org.transitime.core.TravelTimes;
import org.transitime.core.VehicleState;
import org.transitime.core.dataCache.KalmanErrorCache;
import org.transitime.core.dataCache.TripDataHistoryCache;
import org.transitime.core.dataCache.TripKey;
import org.transitime.core.dataCache.VehicleDataCache;
import org.transitime.core.dataCache.VehicleStateManager;
import org.transitime.core.predictiongenerator.PredictionComponentElementsGenerator;
import org.transitime.core.predictiongenerator.average.HistoricalAveragePredictionGeneratorImpl;
import org.transitime.db.structs.ArrivalDeparture;
import org.transitime.db.structs.AvlReport;
import org.transitime.ipc.data.IpcPrediction;
import org.transitime.ipc.data.IpcVehicleComplete;

/**
 * @author Sean Og Crudden This is a prediction generator that uses a Kalman
 *         filter to provide predictions. It uses historical average while waiting on enough data to support a Kalman filter.
 * 
 *         TODO I intend using the error value from the last transiTime
 *         prediction as the starting value.
 */
public class KalmanPredictionGeneratorImpl extends HistoricalAveragePredictionGeneratorImpl
		implements PredictionComponentElementsGenerator {

	/*
	 * TODO I think this needs to be a minimum of two and if just one will use
	 * historical value. 
	 */
	private static final IntegerConfigValue minKalmanDays = new IntegerConfigValue(
			"transitime.prediction.data.kalman.mindays", new Integer(2),
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

		logger.debug("Calling Kalman prediction algorithm.");
		
		TripDataHistoryCache tripCache = TripDataHistoryCache.getInstance();

		KalmanErrorCache kalmanErrorCache = KalmanErrorCache.getInstance();
		
		VehicleStateManager vehicleStateManager = VehicleStateManager.getInstance();

		VehicleState currentVehicleState = vehicleStateManager.getVehicleState(avlReport.getVehicleId());		

		

		long time = 0;

		// time = getTimeTaken(tripCache, previousVehicleOnRouteState, indices);

		time = this.getLastVehicleTravelTime(currentVehicleState, indices);
		/*
		 * The first vehicle of the day should use schedule or historic data to
		 * make prediction. Cannot use Kalman as yesterdays vehicle will have
		 * little to say about todays.
		 */
		if (time > -1) {

			Date nearestDay = DateUtils.truncate(Calendar.getInstance().getTime(), Calendar.DAY_OF_MONTH);

			List<Integer> lastDaysTimes = lastDaysTimes(tripCache, currentVehicleState.getTrip().getId(),
					indices.getStopPathIndex(), nearestDay, currentVehicleState.getTrip().getStartTime(),
					maxKalmanDaysToSearch.getValue(), minKalmanDays.getValue());
			/*
			 * if we have enough data start using Kalman filter otherwise revert
			 * to default. This does not mean that this method of prediction is
			 * yet better than the default.
			 */

			if (lastDaysTimes != null && lastDaysTimes.size() >= minKalmanDays.getValue().intValue()) {

				logger.debug("Generating Kalman prediction.");
				
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

					Double last_prediction_error = lastPredictionError(kalmanErrorCache, indices,
							avlReport.getVehicleId());

					kalmanPredictionResult = kalmanPrediction.predict(last_vehicle_segment, historical_segments_k,
							last_prediction_error);

					long predictionTime = (long) kalmanPredictionResult.getResult();

					kalmanErrorCache.putErrorValue(indices, avlReport.getVehicleId(),
							kalmanPredictionResult.getFilterError());

					logger.debug("Using Kalman prediction: " + predictionTime + " Transitime prediction: "
							+ super.getTravelTimeForPath(indices, avlReport));
					logger.debug("Setting Kalman error value: " + kalmanPredictionResult.getFilterError() + " Vechicle Id: "
							+ avlReport.getVehicleId());

					return predictionTime;

				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}

		}
		//logger.debug("Generating default prediction.");
		return super.getTravelTimeForPath(indices, avlReport);

	}

	private Double lastPredictionError(KalmanErrorCache cache, Indices indices, String vechicleId) {
		Indices lastErrorIndices = new Indices(indices.getBlock(), indices.getTripIndex(),
				indices.decrementStopPath().getStopPathIndex(), indices.getSegmentIndex());
		Double result = cache.getErrorValue(lastErrorIndices, vechicleId);
		if (result == null)
			return initialErrorValue.getValue();
		return result;
	}

	@Override
	public IpcPrediction generatePredictionForStop(AvlReport avlReport, Indices indices, long predictionTime,
			boolean useArrivalTimes, boolean affectedByWaitStop, boolean isDelayed, boolean lateSoMarkAsUncertain) {

		return super.generatePredictionForStop(avlReport, indices, predictionTime, useArrivalTimes, affectedByWaitStop,
				isDelayed, lateSoMarkAsUncertain);
	}
}
