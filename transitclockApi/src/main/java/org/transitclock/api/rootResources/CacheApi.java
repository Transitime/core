/*
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License (GPL) as published by the
 * Free Software Foundation, either version 3 of the License, or any later
 * version.
 * 
 * Transitime.org is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Transitime.org . If not, see <http://www.gnu.org/licenses/>.
 */

package org.transitclock.api.rootResources;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.transitclock.api.data.ApiArrivalDepartures;
import org.transitclock.api.data.ApiCacheDetails;
import org.transitclock.api.data.ApiHistoricalAverage;
import org.transitclock.api.data.ApiHistoricalAverageCacheKeys;
import org.transitclock.api.data.ApiHoldingTime;
import org.transitclock.api.data.ApiHoldingTimeCacheKeys;
import org.transitclock.api.data.ApiKalmanErrorCacheKeys;
import org.transitclock.api.data.ApiPredictionsForStopPath;
import org.transitclock.api.utils.StandardParameters;
import org.transitclock.api.utils.WebUtils;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.ipc.data.IpcHistoricalAverage;
import org.transitclock.ipc.data.IpcHistoricalAverageCacheKey;
import org.transitclock.ipc.data.IpcHoldingTime;
import org.transitclock.ipc.data.IpcHoldingTimeCacheKey;
import org.transitclock.ipc.data.IpcKalmanErrorCacheKey;
import org.transitclock.ipc.data.IpcPredictionForStopPath;
import org.transitclock.ipc.interfaces.CacheQueryInterface;
import org.transitclock.ipc.interfaces.HoldingTimeInterface;
import org.transitclock.ipc.interfaces.PredictionAnalysisInterface;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.servers.Servers;

/**
 * Contains the API commands for the Transitime API for getting info on data that is cached.
 * <p>
 * The data output can be in either JSON or XML. The output format is specified
 * by the accept header or by using the query string parameter "format=json" or
 * "format=xml".
 *
 * @author SkiBu Smith
 *
 */
@Path("/key/{key}/agency/{agency}")
public class CacheApi {

	
	@Path("/command/kalmanerrorcachekeys")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Operation(summary="Gets the list of Kalman Cache error.",
	description="Gets the list of Kalman Cache error.",
	tags= {"kalman","cache"})
	public Response getKalmanErrorCacheKeys(@BeanParam StandardParameters stdParameters)
			throws WebApplicationException {
		try {
			CacheQueryInterface cachequeryInterface = stdParameters.getCacheQueryInterface();

			List<IpcKalmanErrorCacheKey> result = cachequeryInterface.getKalmanErrorCacheKeys();

			ApiKalmanErrorCacheKeys keys = new ApiKalmanErrorCacheKeys(result);

			Response response = stdParameters.createResponse(keys);

			return response;

		} catch (Exception e) {
			// If problem getting result then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}
	@Path("/command/scheduledbasedhistoricalaveragecachekeys")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Operation(summary="Gets a list of the keys that have values in the historical average cache for schedules based services.",
	description="Gets a list of the keys that have values in the historical average cache for schedules based services.",
	tags= {"cache"})
	public Response getSchedulesBasedHistoricalAverageCacheKeys(@BeanParam StandardParameters stdParameters)
			throws WebApplicationException {
		try {
			CacheQueryInterface cachequeryInterface = stdParameters.getCacheQueryInterface();

			List<IpcHistoricalAverageCacheKey> result = cachequeryInterface.getScheduledBasedHistoricalAverageCacheKeys();

			ApiHistoricalAverageCacheKeys keys = new ApiHistoricalAverageCacheKeys(result);

			Response response = stdParameters.createResponse(keys);

			return response;

		} catch (Exception e) {
			// If problem getting result then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}
	// TODO This is not completed and should not be used.
	@Path("/command/frequencybasedhistoricalaveragecachekeys")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Operation(summary="Gets a list of the keys that have values in the historical average cache for frequency based services.",
	description="Gets a list of the keys that have values in the historical average cache for frequency based services."
				+ "<font color=\"#FF0000\">This is not completed and should not be used.<font>",
	tags= {"cache"})
	public Response getFrequencyBasedHistoricalAverageCacheKeys(@BeanParam StandardParameters stdParameters)
			throws WebApplicationException {
		try {
			CacheQueryInterface cachequeryInterface = stdParameters.getCacheQueryInterface();

			List<IpcHistoricalAverageCacheKey> result = cachequeryInterface.getFrequencyBasedHistoricalAverageCacheKeys();

			ApiHistoricalAverageCacheKeys keys = new ApiHistoricalAverageCacheKeys(result);

			Response response = stdParameters.createResponse(keys);

			return response;

		} catch (Exception e) {
			// If problem getting result then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}
	
	@Path("/command/holdingtimecachekeys")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Operation(summary="Gets a list of the keys for the holding times in the cache.",
	description="Gets a list of the keys for the holding times in the cache.",
	tags= {"cache"})
	public Response getHoldingTimeCacheKeys(@BeanParam StandardParameters stdParameters)
			throws WebApplicationException {
		try {
			CacheQueryInterface cachequeryInterface = stdParameters.getCacheQueryInterface();

			List<IpcHoldingTimeCacheKey> result = cachequeryInterface.getHoldingTimeCacheKeys();

			ApiHoldingTimeCacheKeys keys = new ApiHoldingTimeCacheKeys(result);

			Response response = stdParameters.createResponse(keys);

			return response;

		} catch (Exception e) {
			// If problem getting result then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}

	/**
	 * Returns info about a cache.
	 * 
	 * @param stdParameters
	 * @param cachename
	 *            this is the name of the cache to get the size of.
	 * @return
	 * @throws WebApplicationException
	 */
	@Path("/command/cacheinfo")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Operation(summary="Returns the number of entries in the cacheName cache.",
	description="Returns the number of entries in the cacheName cache. The name is passed throug the cachename parameter.",
	tags= {"cache"})
	public Response getCacheInfo(@BeanParam StandardParameters stdParameters,
			@Parameter(description="Name of the cache",required=true)
			@QueryParam(value = "cachename") String cachename) throws WebApplicationException {
		try {

			CacheQueryInterface cachequeryInterface = stdParameters.getCacheQueryInterface();

			Integer size = cachequeryInterface.entriesInCache(cachename);

			if (size != null)
				return stdParameters.createResponse(new ApiCacheDetails(cachename, size));
			else
				throw new Exception("No cache named:" + cachename);

		} catch (Exception e) {
			// If problem getting result then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}

	}

	@Path("/command/stoparrivaldeparturecachedata")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Operation(summary="Returns a list of current arrival or departure events for a specified stop that are in the cache.",
	description="Returns a list of current arrival or departure events for a specified stop that are in the cache.",
	tags= {"cache"})
	public Response getStopArrivalDepartureCacheData(@BeanParam StandardParameters stdParameters,
			@Parameter(description="Stop Id.",required=true)@QueryParam(value = "stopid") String stopid, @QueryParam(value = "date") Date date)
			throws WebApplicationException {
		try {

			CacheQueryInterface cachequeryInterface = stdParameters.getCacheQueryInterface();

			List<IpcArrivalDeparture> result = cachequeryInterface.getStopArrivalDepartures(stopid);

			ApiArrivalDepartures apiResult = new ApiArrivalDepartures(result);
			Response response = stdParameters.createResponse(apiResult);
			return response;

		} catch (Exception e) {
			// If problem getting result then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}

	@Path("/command/triparrivaldeparturecachedata")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Operation(summary="Returns the arrivals and departures for a trip on a specific day and start time.",
	description="Returns a list  of the arrivals and departures for a trip on a specific day and start time."
			+ "Either tripId or date must be specified.",
	tags= {"cache"})
	public Response getTripArrivalDepartureCacheData(@BeanParam StandardParameters stdParameters,
			@Parameter(description="if specified, returns the list for that tripId.",required=false) 
			@QueryParam(value = "routeId") String routeId,
			@Parameter(description="if specified, returns the list for that direction.",required=false)
			@QueryParam(value = "directionId") String directionId,
			@Parameter(description="if specified, returns the list for that date.",required=false)
			@QueryParam(value = "date") DateParam date,
			@Parameter(description="if specified, returns the list for that starttime.",required=false)
			@QueryParam(value = "starttime") Integer starttime) throws WebApplicationException {
		try {

			CacheQueryInterface cachequeryInterface = stdParameters.getCacheQueryInterface();
			LocalDate queryDate = null;
			if (date != null)
				queryDate = date.getDate();
			List<IpcArrivalDeparture> result = cachequeryInterface.getTripArrivalDepartures(routeId, directionId, queryDate,
					starttime);

			ApiArrivalDepartures apiResult = new ApiArrivalDepartures(result);
			Response response = stdParameters.createResponse(apiResult);
			return response;

		} catch (Exception e) {
			// If problem getting result then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}

	/*
	 * This will give the historical cache value for an individual stop path
	 * index of a trip private String tripId; private Integer stopPathIndex;
	 */
	@Path("/command/historicalaveragecachedata")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Operation(summary="Returns the historical cache value for an individual stop path index of a trip.",
	description="Returns the historical cache value for an individual stop path index of a trip.",
	tags= {"cache"})
	public Response getHistoricalAverageCacheData(@BeanParam StandardParameters stdParameters,
			@Parameter(description="Trip Id",required=true)
			@QueryParam(value = "tripId") String tripId, 
			@Parameter(description="Stop path index",required=true)
			@QueryParam(value = "stopPathIndex") Integer stopPathIndex) {
		try {

			CacheQueryInterface cachequeryInterface = stdParameters.getCacheQueryInterface();

			IpcHistoricalAverage result = cachequeryInterface.getHistoricalAverage(tripId, stopPathIndex);

			Response response = stdParameters.createResponse(new ApiHistoricalAverage(result));

			return response;

		} catch (Exception e) {
			// If problem getting result then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}

	@Path("/command/getkalmanerrorvalue")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Operation(summary="Returns the latest Kalman error value for a the stop path of a trip.",
	description="Returns the latest Kalman error value for a the stop path of a trip.",
	tags= {"kalman","cache"})
	public Response getKalmanErrorValue(@BeanParam StandardParameters stdParameters,
			@Parameter(description="Trip Id",required=true)
			@QueryParam(value = "routeId") String routeId,
			@QueryParam(value = "directionId") String directionId,
			@Parameter(description="Trip Start time",required=true)
			@QueryParam(value = "startTimeSecondsIntoDay") Integer startTimeSecondsIntoDay,
			@Parameter(description="Origin Stop Id",required=true)
			@QueryParam(value = "originStopId") String originStopId,
			@Parameter(description="Destination Stop Id",required=true)
			@QueryParam(value = "destinationStopId") String destinationStopId) {
		try {

			CacheQueryInterface cachequeryInterface = stdParameters.getCacheQueryInterface();

			Double result = cachequeryInterface.getKalmanErrorValue(routeId, directionId,
							startTimeSecondsIntoDay, originStopId, destinationStopId);

			Response response = stdParameters.createResponse(result);

			return response;

		} catch (Exception e) {
			// If problem getting result then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}
	@Path("/command/getstoppathpredictions")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Operation(summary="Returns a list of predictions for a the stop path of a trip.",
	description="Returns a list of predictions for a the stop path of a trip.",
	tags= {"cache"})
	//TODO: (vsperez) I believe date is not used at all 
	public Response getStopPathPredictions(@BeanParam StandardParameters stdParameters,
			@Parameter(description="Algorith used for calculating the perdiction",required=false)
			@QueryParam(value = "algorithm") String algorithm,
			@Parameter(description="Trip Id",required=true)
			@QueryParam(value = "tripId") String tripId, 
			@Parameter(description="Stop path index",required=true)
			@QueryParam(value = "stopPathIndex" ) Integer stopPathIndex,
			@Parameter(description="Specified the date.",required=true)
			@QueryParam(value = "date") DateParam date) 
	{
		try {						
			LocalTime midnight = LocalTime.MIDNIGHT;
			Date end_date=null;
			Date start_date=null;
			if(date!=null)
			{
				LocalDate now = date.getDate();
							
				LocalDateTime todayMidnight = LocalDateTime.of(now, midnight);
				LocalDateTime yesterdatMidnight = todayMidnight.plusDays(-1);
										
				end_date = Date.from(todayMidnight.atZone(ZoneId.systemDefault()).toInstant());
				start_date = Date.from(yesterdatMidnight.atZone(ZoneId.systemDefault()).toInstant());
			}
											
			PredictionAnalysisInterface predictionAnalysisInterface = stdParameters.getPredictionAnalysisInterface();

			List<IpcPredictionForStopPath> result = predictionAnalysisInterface.getCachedTravelTimePredictions(tripId, stopPathIndex, start_date, end_date, algorithm);
			
			Response response = stdParameters.createResponse(new ApiPredictionsForStopPath(result));

			return response;

		} catch (Exception e) {
			// If problem getting result then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}
	@Path("/command/getholdingtime")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Operation(summary="Returns the IpcHoldingTime for a specific stop Id and vehicle Id.",
	description="Returns the IpcHoldingTime for a specific stop Id and vehicle Id.",
	tags= {"cache"})
	public Response getHoldingTime(@BeanParam StandardParameters stdParameters,			
			@Parameter(description="Stop id",required=true)
			@QueryParam(value = "stopId") String stopId, 
			@Parameter(description="Vehicle id",required=true)
			@QueryParam(value = "vehicleId" ) String vehicleId) 
	{
		try {						
			 HoldingTimeInterface holdingtimeInterface = stdParameters.getHoldingTimeInterface();
			 IpcHoldingTime result = holdingtimeInterface.getHoldTime(stopId, vehicleId);
			 			 
			 Response response = stdParameters.createResponse(new ApiHoldingTime(result));

			 return response;		
		} catch (Exception e) {
			// If problem getting result then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}
	
}
