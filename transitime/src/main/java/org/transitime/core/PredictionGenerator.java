/*
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Transitime.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Transitime.org .  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transitime.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.lang3.time.DateUtils;
import org.transitime.core.dataCache.TripDataHistoryCache;
import org.transitime.core.dataCache.TripKey;
import org.transitime.db.structs.ArrivalDeparture;
import org.transitime.ipc.data.IpcPrediction;

/**
 * Defines the interface for generating predictions. To create predictions using
 * an alternate method simply implement this interface and configure
 * PredictionGeneratorFactory to instantiate the new class when a
 * PredictionGenerator is needed.
 * 
 * @author SkiBu Smith
 * 
 */
public abstract class PredictionGenerator  {
	
	/**
	 * Generates and returns the predictions for the vehicle. 
	 * 
	 * @param vehicleState
	 *            Contains the new match for the vehicle that the predictions
	 *            are to be based on.
	 */
	public abstract List<IpcPrediction> generate(VehicleState vehicleState);
	
	protected VehicleState getClosetVechicle(List<VehicleState> vehiclesOnRoute,
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



			result = cache.getTripHistory(tripKey);

			if (result != null) {

				result = getDepartureArrival(stopPathIndex, result);

				if (result != null && result.size() > 1) {
					ArrivalDeparture arrival = getArrival(result);
										
					ArrivalDeparture departure = getDeparture(result);
					if (arrival != null && departure != null) {

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
	
	protected long timeBetweenStops(ArrivalDeparture ad1, ArrivalDeparture ad2) {
		if (ad2.getStopPathIndex() - ad1.getStopPathIndex() == 1) {
			// This is the movemment between two stops
			
			return (ad2.getTime() - ad1.getTime());
		}
		return -1;
	}

	protected long getTimeTaken(TripDataHistoryCache cache,
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

	protected ArrivalDeparture getArrival(List<ArrivalDeparture> results) {
		for (ArrivalDeparture result : emptyIfNull(results)) {
			if (result.isArrival())
				return result;
		}
		return null;
	}

	protected ArrivalDeparture getDeparture(List<ArrivalDeparture> results) {
		for (ArrivalDeparture result : emptyIfNull(results)) {
			if (result.isDeparture())
				return result;
		}
		return null;
	}

	protected List<ArrivalDeparture> getDepartureArrival(int stopPathIndex,
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

	protected VehicleState getPreviousVehicle(List<VehicleState> vehicles,
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

	protected static <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
		return iterable == null ? Collections.<T> emptyList() : iterable;
	}
	
}
