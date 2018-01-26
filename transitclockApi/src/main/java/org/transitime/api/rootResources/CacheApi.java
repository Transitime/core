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

package org.transitime.api.rootResources;

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
import org.transitime.api.data.ApiArrivalDepartures;
import org.transitime.api.data.ApiCacheDetails;
import org.transitime.api.data.ApiHistoricalAverage;
import org.transitime.api.data.ApiHistoricalAverageCacheKeys;
import org.transitime.api.data.ApiHoldingTime;
import org.transitime.api.data.ApiHoldingTimeCacheKeys;
import org.transitime.api.data.ApiKalmanErrorCacheKeys;
import org.transitime.api.data.ApiPredictionsForStopPath;
import org.transitime.api.utils.StandardParameters;
import org.transitime.api.utils.WebUtils;

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
	public Response getCacheInfo(@BeanParam StandardParameters stdParameters,
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
	public Response getStopArrivalDepartureCacheData(@BeanParam StandardParameters stdParameters,
			@QueryParam(value = "stopid") String stopid, @QueryParam(value = "date") Date date)
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
	public Response getTripArrivalDepartureCacheData(@BeanParam StandardParameters stdParameters,
			@QueryParam(value = "tripId") String tripid, @QueryParam(value = "date") DateParam date,
			@QueryParam(value = "starttime") Integer starttime) throws WebApplicationException {
		try {

			CacheQueryInterface cachequeryInterface = stdParameters.getCacheQueryInterface();
			LocalDate queryDate = null;
			if (date != null)
				queryDate = date.getDate();
			List<IpcArrivalDeparture> result = cachequeryInterface.getTripArrivalDepartures(tripid, queryDate,
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
	public Response getHistoricalAverageCacheData(@BeanParam StandardParameters stdParameters,
			@QueryParam(value = "tripId") String tripId, @QueryParam(value = "stopPathIndex") Integer stopPathIndex) {
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
	public Response getKalmanErrorValue(@BeanParam StandardParameters stdParameters,
			@QueryParam(value = "tripId") String tripId, @QueryParam(value = "stopPathIndex") Integer stopPathIndex) {
		try {

			CacheQueryInterface cachequeryInterface = stdParameters.getCacheQueryInterface();

			Double result = cachequeryInterface.getKalmanErrorValue(tripId, stopPathIndex);

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
	public Response getStopPathPredictions(@BeanParam StandardParameters stdParameters,
			@QueryParam(value = "algorithm") String algorithm,
			@QueryParam(value = "tripId") String tripId, @QueryParam(value = "stopPathIndex" ) Integer stopPathIndex, @QueryParam(value = "date") DateParam date) 
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
	public Response getHoldingTime(@BeanParam StandardParameters stdParameters,			
			@QueryParam(value = "stopId") String stopId, @QueryParam(value = "vehicleId" ) String vehicleId) 
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
