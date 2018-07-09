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

package org.transitclock.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.lang3.time.DateUtils;
import org.transitclock.applications.Core;
import org.transitclock.config.BooleanConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.core.dataCache.PredictionComparator;
import org.transitclock.core.dataCache.PredictionDataCache;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheFactory;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheKey;
import org.transitclock.core.dataCache.TripDataHistoryCacheFactory;
import org.transitclock.core.dataCache.TripDataHistoryCacheInterface;
import org.transitclock.core.dataCache.TripKey;
import org.transitclock.core.dataCache.ehcache.StopArrivalDepartureCache;
import org.transitclock.core.dataCache.ehcache.TripDataHistoryCache;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.db.structs.Block;
import org.transitclock.db.structs.Trip;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.ipc.data.IpcPrediction;
import org.transitclock.ipc.data.IpcPredictionsForRouteStopDest;

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
			"transitclock.prediction.closestvehiclestopsahead", new Integer(2),
			"Num stops ahead a vehicle must be to be considers in the closest vehicle calculation");

	protected static BooleanConfigValue storeTravelTimeStopPathPredictions = new BooleanConfigValue("transitclock.core.storeTravelTimeStopPathPredictions",
			false,
			"This is set to true to record all travelTime  predictions for individual stopPaths generated. Useful for comparing performance of differant algorithms. (MAPE comparison). Not for normal use as will generate massive amounts of data.");

	protected static BooleanConfigValue storeDwellTimeStopPathPredictions = new BooleanConfigValue("transitclock.core.storeDwellTimeStopPathPredictions",
			false,
			"This is set to true to record all travelTime  predictions for individual dwell times generated. Useful for comparing performance of differant algorithms. (MAPE comparison). Not for normal use as will generate massive amounts of data.");

	protected TravelTimeDetails getLastVehicleTravelTime(VehicleState currentVehicleState, Indices indices) {

		StopArrivalDepartureCacheKey nextStopKey = new StopArrivalDepartureCacheKey(
				indices.getStopPath().getStopId(),
				new Date(currentVehicleState.getMatch().getAvlTime()));

		/* TODO how do we handle the the first stop path. Where do we get the first stop id. */
		if(!indices.atBeginningOfTrip())
		{
			String currentStopId = indices.getPreviousStopPath().getStopId();

			StopArrivalDepartureCacheKey currentStopKey = new StopArrivalDepartureCacheKey(currentStopId,
					new Date(currentVehicleState.getMatch().getAvlTime()));

			List<ArrivalDeparture> currentStopList = StopArrivalDepartureCacheFactory.getInstance().getStopHistory(currentStopKey);

			List<ArrivalDeparture> nextStopList = StopArrivalDepartureCacheFactory.getInstance().getStopHistory(nextStopKey);

			if (currentStopList != null && nextStopList != null) {
				// lists are already sorted when put into cache.
				for (ArrivalDeparture currentArrivalDeparture : currentStopList) {

					if(currentArrivalDeparture.isDeparture()
							&& currentArrivalDeparture.getVehicleId() != currentVehicleState.getVehicleId()
							&& (currentVehicleState.getTrip().getDirectionId()==null || currentVehicleState.getTrip().getDirectionId().equals(currentArrivalDeparture.getDirectionId())))
					{
						ArrivalDeparture found;

						if ((found = findMatchInList(nextStopList, currentArrivalDeparture)) != null) {
							TravelTimeDetails travelTimeDetails=new TravelTimeDetails(currentArrivalDeparture, found);
							if(travelTimeDetails.getTravelTime()>0)
							{
								return travelTimeDetails;

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

			List<ArrivalDeparture> currentStopList = StopArrivalDepartureCacheFactory.getInstance().getStopHistory(currentStopKey);

			List<ArrivalDeparture> nextStopList = StopArrivalDepartureCacheFactory.getInstance().getStopHistory(nextStopKey);

			if (currentStopList != null && nextStopList != null) {
				// lists are already sorted when put into cache.
				for (ArrivalDeparture currentArrivalDeparture : currentStopList) {

					if(currentArrivalDeparture.isDeparture() && currentArrivalDeparture.getVehicleId() != currentVehicleState.getVehicleId()
							&& (currentVehicleState.getTrip().getDirectionId()==null || currentVehicleState.getTrip().getDirectionId().equals(currentArrivalDeparture.getDirectionId())))
					{
						ArrivalDeparture found;

						if ((found = findMatchInList(nextStopList, currentArrivalDeparture)) != null) {
							if(found.getTime() - currentArrivalDeparture.getTime()>0)
							{
								Block currentBlock=null;
								/* block is transient in arrival departure so when read from database need to get from dbconfig. */
								if(currentArrivalDeparture.getBlock()==null&&currentArrivalDeparture.getServiceId()!=null && currentArrivalDeparture.getBlockId()!=null)
								{
									DbConfig dbConfig = Core.getInstance().getDbConfig();

									currentBlock=dbConfig.getBlock(currentArrivalDeparture.getServiceId(), currentArrivalDeparture.getBlockId());
								}else
								{
									currentBlock=currentArrivalDeparture.getBlock();
								}
								if(currentBlock!=null)
									return new Indices(currentBlock, currentArrivalDeparture.getTripIndex(), found.getStopPathIndex(), 0);
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

	protected List<TravelTimeDetails> lastDaysTimes(TripDataHistoryCacheInterface cache, String tripId,String direction, int stopPathIndex, Date startDate,
			Integer startTime, int num_days_look_back, int num_days) {

		List<TravelTimeDetails> times = new ArrayList<TravelTimeDetails>();
		List<ArrivalDeparture> results = null;
		int num_found = 0;
		/*
		 * TODO This could be smarter about the dates it looks at by looking at
		 * which services use this trip and only 1ook on day service is
		 * running
		 */


		for (int i = 0; i < num_days_look_back && num_found < num_days; i++) {

			Date nearestDay = DateUtils.truncate(DateUtils.addDays(startDate, (i + 1) * -1), Calendar.DAY_OF_MONTH);

			TripKey tripKey = new TripKey(tripId, nearestDay, startTime);

			results = cache.getTripHistory(tripKey);

			if (results != null) {

				ArrivalDeparture arrival = getArrival(stopPathIndex, results);

				if(arrival!=null)
				{
					ArrivalDeparture departure = TripDataHistoryCacheFactory.getInstance().findPreviousDepartureEvent(results, arrival);

					if (arrival != null && departure != null) {

						TravelTimeDetails travelTimeDetails=new TravelTimeDetails(departure, arrival);

						times.add(travelTimeDetails);

						num_found++;
					}
				}
			}
		}
		return times;
	}
	ArrivalDeparture getArrival(int stopPathIndex, List<ArrivalDeparture> results)
	{
		for(ArrivalDeparture result:results)
		{
			if(result.isArrival()&&result.getStopPathIndex()==stopPathIndex)
			{
				return result;
			}
		}
		return null;
	}
	protected long timeBetweenStops(ArrivalDeparture ad1, ArrivalDeparture ad2) {

		return Math.abs(ad2.getTime() - ad1.getTime());
	}

	protected static <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
		return iterable == null ? Collections.<T> emptyList() : iterable;
	}

	public long getHeadway(Indices indices, AvlReport avlReport, VehicleState vehicleState) throws Exception {
		
		// This is a WIP to get a predicted headway at the stop.
		List<IpcPrediction> masterList=new ArrayList<IpcPrediction>();

		List<IpcPredictionsForRouteStopDest> predicitonsForRouteStopDest = PredictionDataCache.getInstance().getPredictions(vehicleState.getRouteId(), vehicleState.getTrip().getDirectionId(), indices.getTrip().getStopPath(indices.getStopPathIndex()).getStopId(), 5);

		for(IpcPredictionsForRouteStopDest predictions:predicitonsForRouteStopDest)
		{
			for( IpcPrediction prediction:predictions.getPredictionsForRouteStop())
			{
				masterList.add(prediction);
			}
		}

		// No such thing as headway if only one vehicle.
		if(masterList.size()>1)
		{
			Collections.sort(masterList, new PredictionComparator());
			int index=0;
			boolean found=false;
			for(IpcPrediction prediction:masterList)
			{
				/* find this vehicles prediction for this stop and the last ones prediction. */
				if(prediction.getVehicleId().equals(vehicleState.getVehicleId()))
				{
					found=true;
					break;
				}
				index++;
			}
			if(found&&index>0)
			{
				IpcPrediction currentVehiclePrediction = masterList.get(index);
				IpcPrediction lastVehiclePrediction = masterList.get(index-1);
				/* now the difference between these will give the predicted headway. */
				long headway=currentVehiclePrediction.getPredictionTime()-lastVehiclePrediction.getPredictionTime();
				if(headway>0)
				{
					return new HeadwayDetails(currentVehiclePrediction, lastVehiclePrediction).getHeadway();
				}
			}
		}
		return -1;			
	}
}
