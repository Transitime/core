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
import java.util.Map;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.lang3.time.DateUtils;
import org.transitime.config.IntegerConfigValue;
import org.transitime.core.dataCache.StopArrivalDepartureCache;
import org.transitime.core.dataCache.StopArrivalDepartureCacheKey;
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
public abstract class PredictionGenerator {

	/**
	 * Generates and returns the predictions for the vehicle.
	 * 
	 * @param vehicleState
	 *            Contains the new match for the vehicle that the predictions
	 *            are to be based on.
	 */
	public abstract List<IpcPrediction> generate(VehicleState vehicleState);

	private static final IntegerConfigValue closestVehicleStopsAhead = new IntegerConfigValue(
			"transitime.prediction.closestvehiclestopsahead", new Integer(2),
			"Num stops ahead a vehicle must be to be considers in the closest vehicle calculation");

	protected long getLastVehicleTravelTime(VehicleState currentVehicleState, Indices indices) {

		StopArrivalDepartureCacheKey nextStopKey = new StopArrivalDepartureCacheKey(
				indices.getStopPath().getStopId(),
				new Date(currentVehicleState.getMatch().getAvlTime()));
										
		/* TODO how do we handle the the first stop path. Where do we get the first stop id. */ 		 
		if(!indices.atBeginningOfTrip())
		{						
			String currentStopId = indices.getPreviousStopPath().getStopId();
			
			StopArrivalDepartureCacheKey currentStopKey = new StopArrivalDepartureCacheKey(currentStopId,
					new Date(currentVehicleState.getMatch().getAvlTime()));
	
			List<ArrivalDeparture> currentStopList = StopArrivalDepartureCache.getInstance().getStopHistory(currentStopKey);
	
			List<ArrivalDeparture> nextStopList = StopArrivalDepartureCache.getInstance().getStopHistory(nextStopKey);
	
			if (currentStopList != null && nextStopList != null) {
				// lists are already sorted when put into cache.
				for (ArrivalDeparture currentArrivalDeparture : currentStopList) {
					
					if(currentArrivalDeparture.isDeparture() && currentArrivalDeparture.getVehicleId() != currentVehicleState.getVehicleId())
					{
						ArrivalDeparture found;
											
						if ((found = findMatchInList(nextStopList, currentArrivalDeparture)) != null) {
							if(found.getTime() - currentArrivalDeparture.getTime()>0)
							{																
								return found.getTime() - currentArrivalDeparture.getTime();
							}else
							{
								// must be going backwards
								return -1;
							}
						}else
						{
							return -1;
						}
					}
				}
			}
		}
		return -1;
	}
	protected Indices getLastVehicleIndices(VehicleState currentVehicleState, Indices indices) {

		StopArrivalDepartureCacheKey nextStopKey = new StopArrivalDepartureCacheKey(
				indices.getStopPath().getStopId(),
				new Date(currentVehicleState.getMatch().getAvlTime()));
										
		/* TODO how do we handle the the first stop path. Where do we get the first stop id. */ 		 
		if(!indices.atBeginningOfTrip())
		{						
			String currentStopId = indices.getPreviousStopPath().getStopId();
			
			StopArrivalDepartureCacheKey currentStopKey = new StopArrivalDepartureCacheKey(currentStopId,
					new Date(currentVehicleState.getMatch().getAvlTime()));
	
			List<ArrivalDeparture> currentStopList = StopArrivalDepartureCache.getInstance().getStopHistory(currentStopKey);
	
			List<ArrivalDeparture> nextStopList = StopArrivalDepartureCache.getInstance().getStopHistory(nextStopKey);
	
			if (currentStopList != null && nextStopList != null) {
				// lists are already sorted when put into cache.
				for (ArrivalDeparture currentArrivalDeparture : currentStopList) {
					
					if(currentArrivalDeparture.isDeparture() && currentArrivalDeparture.getVehicleId() != currentVehicleState.getVehicleId())
					{
						ArrivalDeparture found;
											
						if ((found = findMatchInList(nextStopList, currentArrivalDeparture)) != null) {
							if(found.getTime() - currentArrivalDeparture.getTime()>0)
							{																
								return new Indices(found.getBlock(), found.getTripIndex(), found.getStopPathIndex(), 0);
							}else
							{
								// must be going backwards
								return null;
							}
						}else
						{
							return null;
						}
					}
				}
			}
		}
		return null;
	}
	/* TODO could also make it a requirement that it is on the same route as the one we are generating prediction for */
	protected ArrivalDeparture findMatchInList(List<ArrivalDeparture> nextStopList,
			ArrivalDeparture currentArrivalDeparture) {
		for (ArrivalDeparture nextStopArrivalDeparture : nextStopList) {			
			if (currentArrivalDeparture.getVehicleId() == nextStopArrivalDeparture.getVehicleId()
					&& currentArrivalDeparture.getTripId() == nextStopArrivalDeparture.getTripId()
					&&  currentArrivalDeparture.isDeparture() && nextStopArrivalDeparture.isArrival() ) {
				return nextStopArrivalDeparture;
			}
		}
		return null;
	}

	protected VehicleState getClosetVechicle(List<VehicleState> vehiclesOnRoute, Indices indices,
			VehicleState currentVehicleState) {

		Map<String, List<String>> stopsByDirection = currentVehicleState.getTrip().getRoute()
				.getOrderedStopsByDirection();

		List<String> routeStops = stopsByDirection.get(currentVehicleState.getTrip().getDirectionId());

		Integer closest = 100;

		VehicleState result = null;

		for (VehicleState vehicle : vehiclesOnRoute) {

			Integer numAfter = numAfter(routeStops, vehicle.getMatch().getStopPath().getStopId(),
					currentVehicleState.getMatch().getStopPath().getStopId());
			if (numAfter != null && numAfter > closestVehicleStopsAhead.getValue() && numAfter < closest) {
				closest = numAfter;
				result = vehicle;
			}
		}
		return result;
	}

	boolean isAfter(List<String> stops, String stop1, String stop2) {
		if (stops != null && stop1 != null && stop2 != null) {
			if (stops.contains(stop1) && stops.contains(stop2)) {
				if (stops.indexOf(stop1) > stops.indexOf(stop2))
					return true;
				else
					return false;
			}
		}
		return false;
	}

	Integer numAfter(List<String> stops, String stop1, String stop2) {
		if (stops != null && stop1 != null && stop2 != null)
			if (stops.contains(stop1) && stops.contains(stop2))
				return stops.indexOf(stop1) - stops.indexOf(stop2);

		return null;
	}

	protected List<Integer> lastDaysTimes(TripDataHistoryCache cache, String tripId, int stopPathIndex, Date startDate,
			Integer startTime, int num_days_look_back, int num_days) {

		List<Integer> times = new ArrayList<Integer>();
		List<ArrivalDeparture> result = null;
		int num_found = 0;
		/*
		 * TODO This could be smarter about the dates it looks at by looking at
		 * which services use this trip and only 1ook on day service is
		 * running
		 */

		for (int i = 0; i < num_days_look_back && num_found < num_days; i++) {

			Date nearestDay = DateUtils.truncate(DateUtils.addDays(startDate, (i + 1) * -1), Calendar.DAY_OF_MONTH);

			TripKey tripKey = new TripKey(tripId, nearestDay, startTime);

			result = cache.getTripHistory(tripKey);

			if (result != null) {

				result = getDepartureArrival(stopPathIndex, result);

				if (result != null && result.size() > 1) {
					ArrivalDeparture arrival = getArrival(result);

					ArrivalDeparture departure = getDeparture(result);
					if (arrival != null && departure != null) {

						times.add(new Integer((int) (timeBetweenStops(departure, arrival))));
						num_found++;
					}
				}
			}
		}
		return times;		
	}

	protected long timeBetweenStops(ArrivalDeparture ad1, ArrivalDeparture ad2) {
		if (ad2.getStopPathIndex() - ad1.getStopPathIndex() == 1) {
			// This is the movement between two stops
			return (ad2.getTime() - ad1.getTime());
		}
		return -1;
	}

	protected long getTimeTaken(TripDataHistoryCache cache, VehicleState previousVehicleOnRouteState,
			Indices currentVehicleIndices) {

		int currentIndex = currentVehicleIndices.getStopPathIndex();

		Date nearestDay = DateUtils.truncate(Calendar.getInstance().getTime(), Calendar.DAY_OF_MONTH);

		TripKey tripKey = new TripKey(previousVehicleOnRouteState.getTrip().getId(), nearestDay,
				previousVehicleOnRouteState.getTrip().getStartTime());

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

	protected List<ArrivalDeparture> getDepartureArrival(int stopPathIndex, List<ArrivalDeparture> results) {
		BeanComparator<ArrivalDeparture> compartor = new BeanComparator<ArrivalDeparture>("stopPathIndex");

		Collections.sort(results, compartor);

		ArrayList<ArrivalDeparture> stopPathEnds = new ArrayList<ArrivalDeparture>();
		for (ArrivalDeparture result : emptyIfNull(results)) {
			if ((result.getStopPathIndex() == (stopPathIndex - 1) && result.isDeparture())
					|| (result.getStopPathIndex() == stopPathIndex && result.isArrival())) {
				stopPathEnds.add(result);
			}
		}
		return stopPathEnds;

	}

	protected VehicleState getPreviousVehicle(List<VehicleState> vehicles, VehicleState vehicle) {
		double closestDistance = 1000000;

		VehicleState vehicleState = null;
		String direction = vehicle.getMatch().getTrip().getDirectionId();
		for (VehicleState currentVehicle : vehicles) {

			String currentDirection = currentVehicle.getMatch().getTrip().getDirectionId();

			if (currentDirection.equals(direction)) {
				double distance = vehicle.getMatch().distanceBetweenMatches(currentVehicle.getMatch());
				/*
				 * must check which is closest that has actually passed the stop
				 * the current vehicle is moving towards
				 */
				if (distance > 0 && distance < closestDistance && currentVehicle.getMatch().getStopPath()
						.getGtfsStopSeq() > vehicle.getMatch().getStopPath().getGtfsStopSeq()) {
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
