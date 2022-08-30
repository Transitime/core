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

package org.transitclock.core.predictiongenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.core.Indices;
import org.transitclock.core.TravelTimeDetails;
import org.transitclock.core.VehicleState;
import org.transitclock.core.dataCache.*;
import org.transitclock.core.predictiongenerator.datafilter.TravelTimeDataFilter;
import org.transitclock.core.predictiongenerator.datafilter.TravelTimeFilterFactory;
import org.transitclock.db.structs.ArrivalDeparture;
import org.transitclock.db.structs.Block;
import org.transitclock.db.structs.PredictionEvent;
import org.transitclock.db.structs.Trip;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.monitoring.MonitoringService;
import org.transitclock.utils.DateUtils;

import java.util.*;

/**
 * Commonly-used methods for PredictionGenerators that use historical cached data.
 */
public class HistoricalPredictionLibrary {

	private static MonitoringService monitoring = null;

	private static final IntegerConfigValue closestVehicleStopsAhead = new IntegerConfigValue(
			"transitclock.prediction.closestvehiclestopsahead", new Integer(2),
			"Num stops ahead a vehicle must be to be considers in the closest vehicle calculation");

	private static final IntegerConfigValue maxHeadwayMinutes = new IntegerConfigValue(
					"transitclock.prediction.dwell.maxHeadwayMinutes",
					180,
					"Max time between trips to be considered a valid headway");

	private static final Logger logger = LoggerFactory.getLogger(HistoricalPredictionLibrary.class);

	public static TravelTimeDetails getLastVehicleTravelTime(VehicleState currentVehicleState, Indices indices) throws Exception {

		// NOTE: direction is relative to index, not vehicleState!
		// We may be on a future trip in a reverse direction!
		String currentDirection = indices.getTrip().getDirectionId();
		StopArrivalDepartureCacheKey nextStopKey = new StopArrivalDepartureCacheKey(
				indices.getStopPath().getStopId(),
				new Date(currentVehicleState.getMatch().getAvlTime()));

		/* TODO how do we handle the the first stop path. Where do we get the first stop id. */
		if(!indices.atBeginningOfTrip())
		{
			String currentStopId = indices.getPreviousStopPath().getStopId();

			StopArrivalDepartureCacheKey currentStopKey = new StopArrivalDepartureCacheKey(currentStopId,
					new Date(currentVehicleState.getMatch().getAvlTime()));

			List<IpcArrivalDeparture> currentStopList = StopArrivalDepartureCacheFactory.getInstance().getStopHistory(currentStopKey);
			getMonitoring().rateMetric("PredictionStopADCurrentHit", (currentStopList==null||currentStopList.isEmpty()?false:true));

			List<IpcArrivalDeparture> nextStopList = StopArrivalDepartureCacheFactory.getInstance().getStopHistory(nextStopKey);
			getMonitoring().rateMetric("PredictionStopADNextHit", (nextStopList==null||nextStopList.isEmpty()?false:true));

			if (currentStopList != null && nextStopList != null) {
				// lists are already sorted when put into cache.
				for (IpcArrivalDeparture currentArrivalDeparture : currentStopList) {

					if(currentArrivalDeparture.isDeparture()
							&& !currentArrivalDeparture.getVehicleId().equals(currentVehicleState.getVehicleId())
							&& (currentDirection==null || currentDirection.equals(currentArrivalDeparture.getDirectionId())))
					{
						// this appears bound by percentage of service filled
						getMonitoring().rateMetric("PredictionStopADVehicleHit", true);
						IpcArrivalDeparture found;
						// for this departure find the next arrival
						if ((found = findMatchInList(nextStopList, currentArrivalDeparture)) != null) {
							getMonitoring().rateMetric("PredictionStopADTTHit", true);
							// NOTE: this constructor is departure, arrival!!!!
							TravelTimeDetails travelTimeDetails=new TravelTimeDetails(currentArrivalDeparture, found);
							if(travelTimeDetails.getTravelTime()>0)
							{
								getMonitoring().rateMetric("PredictionStopADHit", true);
								getMonitoring().rateMetric("PredictionStopADValidTTHit", true);
								return travelTimeDetails;

							}else
							{
								getMonitoring().rateMetric("PredictionStopADValidTTHit", false);
								String description=found + " : " + currentArrivalDeparture;
								PredictionEvent.create(currentVehicleState.getAvlReport(), currentVehicleState.getMatch(), PredictionEvent.TRAVELTIME_EXCEPTION,
										description,
										travelTimeDetails.getArrival().getStopId(),
										travelTimeDetails.getDeparture().getStopId(),
										travelTimeDetails.getArrival().getVehicleId(),
										travelTimeDetails.getArrival().getTime(),
										travelTimeDetails.getDeparture().getTime()
								);
								getMonitoring().rateMetric("PredictionStopADHit", false);
								return null;
							}
						}else
						{
							// match not found
							getMonitoring().rateMetric("PredictionStopADHit", false);
							getMonitoring().rateMetric("PredictionStopADTTHit", false);
							return null;
						}
					} else {
						logger.debug("vehicle/direction did not match {} != {}",
										currentArrivalDeparture.getVehicleId(), currentVehicleState.getVehicleId());
					}
				}
				getMonitoring().rateMetric("PredictionStopADVehicleHit", false);
				logger.debug("fall through");
			} else {
				logger.debug("stop lists not populated");
			}
		}
		getMonitoring().rateMetric("PredictionStopADHit", false);
		return null;
	}

	public static Indices getLastVehicleIndices(VehicleState currentVehicleState, Indices indices) {

		StopArrivalDepartureCacheKey nextStopKey = new StopArrivalDepartureCacheKey(
				indices.getStopPath().getStopId(),
				new Date(currentVehicleState.getMatch().getAvlTime()));

		/* TODO how do we handle the the first stop path. Where do we get the first stop id. */
		if(!indices.atBeginningOfTrip())
		{
			String currentStopId = indices.getPreviousStopPath().getStopId();

			StopArrivalDepartureCacheKey currentStopKey = new StopArrivalDepartureCacheKey(currentStopId,
					new Date(currentVehicleState.getMatch().getAvlTime()));

			List<IpcArrivalDeparture> currentStopList = StopArrivalDepartureCacheFactory.getInstance().getStopHistory(currentStopKey);

			List<IpcArrivalDeparture> nextStopList = StopArrivalDepartureCacheFactory.getInstance().getStopHistory(nextStopKey);

			if (currentStopList != null && nextStopList != null) {
				// lists are already sorted when put into cache.
				for (IpcArrivalDeparture currentArrivalDeparture : currentStopList) {

					if(currentArrivalDeparture.isDeparture() && !currentArrivalDeparture.getVehicleId().equals(currentVehicleState.getVehicleId())
							&& (currentVehicleState.getTrip().getDirectionId()==null || currentVehicleState.getTrip().getDirectionId().equals(currentArrivalDeparture.getDirectionId())))
					{
						IpcArrivalDeparture found;

						if ((found = findMatchInList(nextStopList, currentArrivalDeparture)) != null) {
							if(found.getTime().getTime() - currentArrivalDeparture.getTime().getTime()>0)
							{
								Block currentBlock=null;
								/* block is transient in arrival departure so when read from database need to get from dbconfig. */

								DbConfig dbConfig = Core.getInstance().getDbConfig();

								currentBlock=dbConfig.getBlock(currentArrivalDeparture.getServiceId(), currentArrivalDeparture.getBlockId());

								if(currentBlock!=null)
									return new Indices(currentBlock, currentArrivalDeparture.getTripIndex(), found.getStopPathIndex(), 0);
							}else
							{
								// must be going backwards
								return null;
							}
						}else
						{
							return null; // not matched in list
						}
					}
				}
			}
		}
		return null;
	}
	/* TODO could also make it a requirement that it is on the same route as the one we are generating prediction for */
	private static IpcArrivalDeparture findMatchInList(List<IpcArrivalDeparture> nextStopList,
													   IpcArrivalDeparture currentArrivalDeparture) {
		for (IpcArrivalDeparture nextStopArrivalDeparture : nextStopList) {
			if (currentArrivalDeparture.getVehicleId().equals(nextStopArrivalDeparture.getVehicleId())
					&& currentArrivalDeparture.getTripId().equals(nextStopArrivalDeparture.getTripId())
					&&  currentArrivalDeparture.isDeparture() && nextStopArrivalDeparture.isArrival() ) {
				return nextStopArrivalDeparture;
			}
		}
		return null;
	}

	private static VehicleState getClosetVechicle(List<VehicleState> vehiclesOnRoute, Indices indices,
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

	private static boolean isAfter(List<String> stops, String stop1, String stop2) {
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

	private static Integer numAfter(List<String> stops, String stop1, String stop2) {
		if (stops != null && stop1 != null && stop2 != null)
			if (stops.contains(stop1) && stops.contains(stop2))
				return stops.indexOf(stop1) - stops.indexOf(stop2);

		return null;
	}

    public static List<TravelTimeDetails> getHistoricalTravelTimes(TripDataHistoryCacheInterface cache,
																 String routeId,
																 String directionId,
																 int stopPathIndex,
																 Date startDate,
																 Integer startTime,
																 int num_days_look_back,
																 int num_days) {

		List<TravelTimeDetails> times = new ArrayList<TravelTimeDetails>();
		List<IpcArrivalDeparture> results = null;
		int num_found = 0;

		for (int i = 0; i < num_days_look_back && num_found < num_days; i++) {

			Date nearestDay = DateUtils.truncate(DateUtils.addDays(startDate, (i + 1) * -1), Calendar.DAY_OF_MONTH);
			boolean isHoliday = false;
			if (!calendarMatches(startDate, nearestDay, isHoliday)) {
				continue;
			}

			TripKey tripKey = new TripKey(routeId, directionId, nearestDay.getTime(), startTime);

			results = cache.getTripHistory(tripKey);

			if (results != null) {

				IpcArrivalDeparture arrival = getArrival(stopPathIndex, results);

				if(arrival!=null)
				{
					IpcArrivalDeparture departure = TripDataHistoryCacheFactory.getInstance().findPreviousDepartureEvent(results, arrival);

					if (arrival != null && departure != null) {

						TravelTimeDetails travelTimeDetails=new TravelTimeDetails(departure, arrival);

						if(travelTimeDetails.getTravelTime()!=-1)
						{
							TravelTimeDataFilter travelTimefilter = TravelTimeFilterFactory.getInstance();
							if(!travelTimefilter.filter(travelTimeDetails.getDeparture(),travelTimeDetails.getArrival()))
							{
								times.add(travelTimeDetails);
								num_found++;
							}
						} else {
							// invalid travel time
							logger.debug("invalid travel time {}", travelTimeDetails.getTravelTime());
						}
					} else {
						//departure is null
						logger.debug("departure is null for arrival {} ", arrival);
					}
				} else {
					// arrival null
					logger.debug("arrival is null for trip {}", tripKey);
				}
			} else {
				// tripHistory null
				logger.debug("history is empty for trip {}" + tripKey);
			}
		}
		return times;
    }

	private static boolean calendarMatches(Date startDate, Date nearestDay, boolean isHoliday) {
		return DateUtils.getTypeForDate(startDate).equals(DateUtils.getTypeForDate(nearestDay, isHoliday));
	}

	public static Long getLastHeadway(String referenceStopId, String routeId, Date referenceTime) {
		StopArrivalDepartureCacheKey currentStopKey = new StopArrivalDepartureCacheKey(referenceStopId,
						referenceTime);
		List<IpcArrivalDeparture> currentStopList = StopArrivalDepartureCacheFactory.getInstance().getStopHistory(currentStopKey);
		if (currentStopList == null || currentStopList.isEmpty()) {
			logger.debug("no real-time headway stopList for {}", referenceStopId);
			return null;
		}

		// list is sorted in newest order.  First hit AFTER our reference time is the answer
		for (int i = 0; i < currentStopList.size(); i++) {
			IpcArrivalDeparture ipcArrivalDeparture = currentStopList.get(i);
			Trip headwayTrip = Core.getInstance().getDbConfig().getTrip(ipcArrivalDeparture.getTripId());
			if (headwayTrip == null || headwayTrip.getRouteId().equals(routeId)) {
				// don't let the headway be negative -- as can happen during playback (or bad data)
				if (referenceTime.getTime() > ipcArrivalDeparture.getTime().getTime()) {
					return referenceTime.getTime() - ipcArrivalDeparture.getTime().getTime();
				}
			}
		}
		return null;
	}


	private static IpcArrivalDeparture getArrival(int stopPathIndex, List<IpcArrivalDeparture> results)
	{
		for(IpcArrivalDeparture result:results)
		{
			if(result.isArrival()&&result.getStopPathIndex()==stopPathIndex)
			{
				return result;
			}
		}
		return null;
	}

	private static long timeBetweenStops(ArrivalDeparture ad1, ArrivalDeparture ad2) {
		
		return Math.abs(ad2.getTime() - ad1.getTime());		
	}

	public static <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
		return iterable == null ? Collections.<T> emptyList() : iterable;
	}

	/**
	 * lazy load Cloudwatch Monitoring service.
	 * @return
	 */
	protected static MonitoringService getMonitoring() {
		if (monitoring == null)
			monitoring = MonitoringService.getInstance();
		return monitoring;
	}


}
