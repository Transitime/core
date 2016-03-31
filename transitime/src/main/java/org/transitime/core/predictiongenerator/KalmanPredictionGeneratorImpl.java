package org.transitime.core.predictiongenerator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.transitime.db.structs.ArrivalDeparture;
import org.transitime.db.structs.AvlReport;
import org.transitime.ipc.data.IpcVehicleComplete;

/**
 * @author Sean Og Crudden This is a prediction generator that uses a Kalman
 *         filter to provide predictions. It uses the default prediction
 *         method of transiTime while it generates enough data to support
 *         this method of prediction.
 * 
 *         TODO I intend using the error value from the last transiTime
 *         prediciton as the starting value.
 */
public class KalmanPredictionGeneratorImpl extends
		PredictionGeneratorDefaultImpl  implements PredictionGenerator {

	/*
	 * TODO I think this needs to be a minimum of two and if just one will use
	 * historical value. This needs to be added to transiTime config file
	 */
	private static final IntegerConfigValue minKalmanDays = new IntegerConfigValue(
			"transitime.prediction.data.kalman.mindays",
			new Integer(2),
			"Min number of days trip data that needs to be available before Kalman prediciton is used instead of default transiTime prediction.");

	private static final IntegerConfigValue maxKalmanDays = new IntegerConfigValue(
			"transitime.prediction.data.kalman.maxdays",
			new Integer(3),
			"Max number of historical days trips to include in Kalman prediction calculation.");

	private static final IntegerConfigValue maxKalmanDaysToSearch = new IntegerConfigValue(
			"transitime.prediction.data.kalman.maxdaystoseach",
			new Integer(21),
			"Max number of days to look back for data. This will also be effected by how old the data in the cache is.");

	/* TODO This needs to be added to the transitTime config file */
	private static Double initialErrorValue = (double) 100;

	private static final Logger logger = LoggerFactory
			.getLogger(KalmanPredictionGeneratorImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.transitime.core.PredictionGeneratorDefaultImpl#getTravelTimeForPath
	 * (org.transitime.core.Indices, org.transitime.db.structs.AvlReport)
	 */
	@Override
	public long getTravelTimeForPath(Indices indices, AvlReport avlReport) {

		VehicleDataCache vehicleCache = VehicleDataCache.getInstance();

		TripDataHistoryCache tripCache = TripDataHistoryCache.getInstance();

		KalmanErrorCache kalmanErrorCache = KalmanErrorCache.getInstance();

		List<VehicleState> vehiclesOnRoute = new ArrayList<VehicleState>();

		VehicleStateManager vehicleStateManager = VehicleStateManager
				.getInstance();

		VehicleState currentVehicleState = vehicleStateManager
				.getVehicleState(avlReport.getVehicleId());

		for (IpcVehicleComplete vehicle : emptyIfNull(vehicleCache
				.getVehiclesForRoute(currentVehicleState.getRouteId()))) {
			VehicleState vehicleOnRouteState = vehicleStateManager
					.getVehicleState(vehicle.getId());
			vehiclesOnRoute.add(vehicleOnRouteState);
		}

		VehicleState previousVehicleOnRouteState = getClosetVechicle(
				vehiclesOnRoute, indices);
		/*
		 * The first vehicle of the day should use schedule or historic data to
		 * make prediction. Cannot use Kalman as yesterdays vehicle will have
		 * little to say about todays.
		 */
		if (previousVehicleOnRouteState != null) {

			long time = 0;

			time = getTimeTaken(tripCache, previousVehicleOnRouteState, indices);

			if (time > 0) {

				Date nearestDay = DateUtils.truncate(Calendar.getInstance()
						.getTime(), Calendar.DAY_OF_MONTH);

				List<Integer> lastDaysTimes = lastDaysTimes(tripCache,
						currentVehicleState.getTrip().getId(),
						indices.getStopPathIndex(), nearestDay,
						currentVehicleState.getTrip().getStartTime(),
						maxKalmanDaysToSearch.getValue(),
						minKalmanDays.getValue());
				/*
				 * if we have enough data start using Kalman filter otherwise
				 * revert to default. This does not mean that this method of
				 * prediction is yet better than the default.
				 */

				if (lastDaysTimes != null
						&& lastDaysTimes.size() >= minKalmanDays.getValue()
								.intValue()) {

					logger.debug("Generating Kalman prediction.");

					TravelTimes.getInstance().expectedStopTimeForStopPath(
							indices);
					try {

						KalmanPrediction kalmanPrediction = new KalmanPrediction();

						KalmanPredictionResult kalmanPredictionResult;

						Vehicle vehicle = new Vehicle(avlReport.getVehicleId());

						VehicleStopDetail originDetail = new VehicleStopDetail(
								null, 0, vehicle);
						TripSegment[] historical_segments_k = new TripSegment[lastDaysTimes
								.size()];
						for (int i = 0; i < lastDaysTimes.size()
								&& i < maxKalmanDays.getValue(); i++) {
							VehicleStopDetail destinationDetail = new VehicleStopDetail(
									null, lastDaysTimes.get(i), vehicle);
							historical_segments_k[i] = new TripSegment(
									originDetail, destinationDetail);
						}

						VehicleStopDetail destinationDetail_0_k_1 = new VehicleStopDetail(
								null, time, vehicle);

						TripSegment ts_day_0_k_1 = new TripSegment(
								originDetail, destinationDetail_0_k_1);

						TripSegment last_vehicle_segment = ts_day_0_k_1;

						Double last_prediction_error = lastPredictionError(
								kalmanErrorCache, indices,
								avlReport.getVehicleId());

						kalmanPredictionResult = kalmanPrediction.predict(
								last_vehicle_segment, historical_segments_k,
								last_prediction_error);

						long predictionTime = (long) kalmanPredictionResult
								.getResult();

						kalmanErrorCache.putErrorValue(indices,
								avlReport.getVehicleId(),
								kalmanPredictionResult.getFilterError());
						
						logger.debug("Kalman prediction: "+predictionTime+" Historical average prediction: "+super.getTravelTimeForPath(indices, avlReport));
						logger.debug("Kalman error value: "+kalmanPredictionResult.getFilterError()+" Vechicle Id: "+avlReport.getVehicleId());
						
						return predictionTime;

					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		}
		/* logger.debug("Generating default prediction."); */
		return super.getTravelTimeForPath(indices, avlReport);

	}

	private VehicleState getClosetVechicle(List<VehicleState> vehiclesOnRoute,
			Indices indices) {
		int index_diff = 100;
		VehicleState result = null;
		for (VehicleState vehicle : vehiclesOnRoute) {
			if (vehicle.getMatch() != null) {
				if (vehicle.getMatch().getStopPathIndex() > indices
						.getStopPathIndex()) {
					int diff = vehicle.getMatch().getStopPathIndex()
							- indices.getStopPathIndex();
					if (diff < index_diff) {
						index_diff = diff;
						result = vehicle;
					}
				}
			}
		}
		return result;
	}

	private Double lastPredictionError(KalmanErrorCache cache, Indices indices,
			String vechicleId) {
		Indices lastErrorIndices = new Indices(indices.getBlock(),
				indices.getTripIndex(), indices.getStopPathIndex() - 1,
				indices.getSegmentIndex());
		Double result = cache.getErrorValue(lastErrorIndices, vechicleId);
		if (result == null)
			return initialErrorValue;
		return result;
	}

	protected List<Integer> lastDaysTimes(TripDataHistoryCache cache,
			String tripId, int stopPathIndex, Date startDate,
			Integer startTime, int num_days_look_back, int num_days) {

		List<Integer> times = new ArrayList<Integer>();
		List<ArrivalDeparture> result = null;
		int num_found = 0;
		/*
		 * TODO This could be smarter about the dates it looks at by looking at
		 * which services use this trip and only l.111ook on day srvice is running
		 */

		for (int i = 0; i < num_days_look_back && num_found < num_days; i++) {

			Date nearestDay = DateUtils.truncate(
					DateUtils.addDays(startDate, (i + 1) * -1),
					Calendar.DAY_OF_MONTH);

			TripKey tripKey = new TripKey(tripId, nearestDay, startTime);

			logger.debug("Looking for: " + tripKey.toString());

			result = cache.getTripHistory(tripKey);

			if (result != null) {
				logger.debug("Found: "+result);
				result = getDepartureArrival(stopPathIndex, result);

				if (result != null && result.size() > 1) {
					ArrivalDeparture arrival = getArrival(result);
										
					ArrivalDeparture departure = getDeparture(result);
					if (arrival != null && departure != null) {
						logger.debug("Arrival: "+arrival);
						logger.debug("Departure: "+departure);
						times.add(new Integer((int) (timeBetweenStops(
								departure, arrival))));
						num_found++;
						
						
					}
				}
			}
		}
		if (times.size() == num_days) {
			return times;
		} else {
			return null;
		}
	}

	private long timeBetweenStops(ArrivalDeparture ad1, ArrivalDeparture ad2) {
		if (ad2.getStopPathIndex() - ad1.getStopPathIndex() == 1) {
			// This is the movemment between two stops
			logger.debug("Time between " + ad1.getStopId() + " and "
					+ ad2.getStopId() + " was "
					+ (ad2.getTime() - ad1.getTime()) + ".");
			return (ad2.getTime() - ad1.getTime());
		}
		return -1;
	}

	private long getTimeTaken(TripDataHistoryCache cache,
			VehicleState previousVehicleOnRouteState,
			Indices currentVehicleIndices) {

		int currentIndex = currentVehicleIndices.getStopPathIndex();

		Date nearestDay = DateUtils.truncate(Calendar.getInstance().getTime(),
				Calendar.DAY_OF_MONTH);

		TripKey tripKey = new TripKey(previousVehicleOnRouteState.getTrip()
				.getId(), nearestDay, previousVehicleOnRouteState.getTrip()
				.getStartTime());

		List<ArrivalDeparture> results = cache.getTripHistory(tripKey);

		if (results != null) {
			results = getDepartureArrival(currentIndex, results);

			if (results != null && results.size() > 1) {
				ArrivalDeparture arrival = getArrival(results);
				ArrivalDeparture departure = getDeparture(results);
				if (arrival != null && departure != null) {
					return timeBetweenStops(departure, arrival);
				}
			}
		}
		return 0;
	}

	private ArrivalDeparture getArrival(List<ArrivalDeparture> results) {
		for (ArrivalDeparture result : emptyIfNull(results)) {
			if (result.isArrival())
				return result;
		}
		return null;
	}

	private ArrivalDeparture getDeparture(List<ArrivalDeparture> results) {
		for (ArrivalDeparture result : emptyIfNull(results)) {
			if (result.isDeparture())
				return result;
		}
		return null;
	}

	private List<ArrivalDeparture> getDepartureArrival(int stopPathIndex,
			List<ArrivalDeparture> results) {
		BeanComparator<ArrivalDeparture> compartor = new BeanComparator<ArrivalDeparture>(
				"stopPathIndex");

		Collections.sort(results, compartor);

		ArrayList<ArrivalDeparture> stopPathEnds = new ArrayList<ArrivalDeparture>();
		for (ArrivalDeparture result : emptyIfNull(results)) {
			if ((result.getStopPathIndex() == (stopPathIndex - 1) && result
					.isDeparture())
					|| (result.getStopPathIndex() == stopPathIndex && result
							.isArrival())) {
				stopPathEnds.add(result);
			}
		}
		return stopPathEnds;

	}

	@SuppressWarnings("unused")
	private VehicleState getPreviousVehicle(List<VehicleState> vehicles,
			VehicleState vehicle) {
		double closestDistance = 1000000;

		VehicleState vehicleState = null;
		String direction = vehicle.getMatch().getTrip().getDirectionId();
		for (VehicleState currentVehicle : vehicles) {

			String currentDirection = currentVehicle.getMatch().getTrip()
					.getDirectionId();

			if (currentDirection.equals(direction)) {
				double distance = vehicle.getMatch().distanceBetweenMatches(
						currentVehicle.getMatch());
				/*
				 * must check which is closest that has actually passed the stop
				 * the current vehicle is moving towards
				 */
				if (distance > 0
						&& distance < closestDistance
						&& currentVehicle.getMatch().getStopPath()
								.getGtfsStopSeq() > vehicle.getMatch()
								.getStopPath().getGtfsStopSeq()) {
					closestDistance = distance;
					vehicleState = currentVehicle;
				}
			}
		}
		return vehicleState;
	}

	public static <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
		return iterable == null ? Collections.<T> emptyList() : iterable;
	}
}
