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

package org.transitime.api.rootResources;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.BeanParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.transitime.api.data.ApiActiveBlocks;
import org.transitime.api.data.ApiActiveBlocksRoutes;
import org.transitime.api.data.ApiAgencies;
import org.transitime.api.data.ApiAgency;
import org.transitime.api.data.ApiBlock;
import org.transitime.api.data.ApiBlocks;
import org.transitime.api.data.ApiBlocksTerse;
import org.transitime.api.data.ApiCalendars;
import org.transitime.api.data.ApiDirections;
import org.transitime.api.data.ApiIds;
import org.transitime.api.data.ApiPredictions;
import org.transitime.api.data.ApiRmiServerStatus;
import org.transitime.api.data.ApiRoute;
import org.transitime.api.data.ApiRouteSummaries;
import org.transitime.api.data.ApiSchedulesHorizStops;
import org.transitime.api.data.ApiSchedulesVertStops;
import org.transitime.api.data.ApiServerStatus;
import org.transitime.api.data.ApiTrip;
import org.transitime.api.data.ApiTripPatterns;
import org.transitime.api.data.ApiTripWithTravelTimes;
import org.transitime.api.data.ApiVehicleConfigs;
import org.transitime.api.data.ApiVehicles;
import org.transitime.api.data.ApiVehiclesDetails;
import org.transitime.api.predsByLoc.PredsByLoc;
import org.transitime.api.utils.StandardParameters;
import org.transitime.api.utils.WebUtils;
import org.transitime.db.structs.Agency;
import org.transitime.db.structs.Location;
import org.transitime.ipc.data.IpcActiveBlock;
import org.transitime.ipc.data.IpcBlock;
import org.transitime.ipc.data.IpcCalendar;
import org.transitime.ipc.data.IpcPrediction;
import org.transitime.ipc.data.IpcPredictionsForRouteStopDest;
import org.transitime.ipc.data.IpcRoute;
import org.transitime.ipc.data.IpcRouteSummary;
import org.transitime.ipc.data.IpcSchedule;
import org.transitime.ipc.data.IpcServerStatus;
import org.transitime.ipc.data.IpcDirectionsForRoute;
import org.transitime.ipc.data.IpcTrip;
import org.transitime.ipc.data.IpcTripPattern;
import org.transitime.ipc.data.IpcVehicle;
import org.transitime.ipc.data.IpcVehicleConfig;
import org.transitime.ipc.interfaces.ConfigInterface;
import org.transitime.ipc.interfaces.PredictionsInterface;
import org.transitime.ipc.interfaces.ServerStatusInterface;
import org.transitime.ipc.interfaces.VehiclesInterface;
import org.transitime.ipc.interfaces.PredictionsInterface.RouteStop;

/**
 * Contains the API commands for the Transitime API for getting real-time
 * vehicle and prediction information plus the static configuration information.
 * The intent of this feed is to provide what is needed for creating a user
 * interface application, such as a smartphone application.
 * <p>
 * The data output can be in either JSON or XML. The output format is specified
 * by the accept header or by using the query string parameter "format=json" or
 * "format=xml".
 *
 * @author SkiBu Smith
 *
 */
@Path("/key/{key}/agency/{agency}")
public class TransitimeApi {

	/**
	 * Handles the "vehicles" command. Returns data for all vehicles or for the
	 * vehicles specified via the query string.
	 * <p>
	 * A Response object is returned instead of a regular object so that can
	 * have one method for the both XML and JSON yet always return the proper
	 * media type even if it is configured via the query string "format"
	 * parameter as opposed to the accept header.
	 * 
	 * @param stdParameters
	 *            StdParametersBean that gets the standard parameters from the
	 *            URI, query string, and headers.
	 * @param vehicleIds
	 *            Optional way of specifying which vehicles to get data for
	 * @param routesIdOrShortNames
	 *            Optional way of specifying which routes to get data for
	 * @param stopId
	 *            Optional way of specifying a stop so can get predictions for
	 *            routes and determine which vehicles are the ones generating
	 *            the predictions. The other vehicles are labeled as minor so
	 *            they can be drawn specially in the UI.
	 * @param numberPredictions
	 *            For when determining which vehicles are generating the
	 *            predictions so can label minor vehicles
	 * @return The Response object already configured for the specified media
	 *         type.
	 */
	@Path("/command/vehicles")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getVehicles(
			@BeanParam StandardParameters stdParameters,
			@QueryParam(value = "v") List<String> vehicleIds,
			@QueryParam(value = "r") List<String> routesIdOrShortNames,
			@QueryParam(value = "s") String stopId,
			@QueryParam(value = "numPreds") @DefaultValue("2") int numberPredictions)
			throws WebApplicationException {
		// Make sure request is valid
		stdParameters.validate();

		try {
			// Get Vehicle data from server
			VehiclesInterface inter = stdParameters.getVehiclesInterface();

			Collection<IpcVehicle> vehicles;
			if (!routesIdOrShortNames.isEmpty()) {
				vehicles = inter.getForRoute(routesIdOrShortNames);
			} else if (!vehicleIds.isEmpty()) {
				vehicles = inter.get(vehicleIds);
			} else {
				vehicles = inter.get();
			}

			// If the vehicles doesn't exist then throw exception such that
			// Bad Request with an appropriate message is returned.
			if (vehicles == null)
				throw WebUtils.badRequestException("Invalid specifier for "
						+ "vehicles");

			// To determine how vehicles should be drawn in UI. If stop
			// specified
			// when getting vehicle info then only the vehicles being predicted
			// for, should be highlighted. The others should be dimmed.
			Map<String, UiMode> uiTypesForVehicles = determineUiModesForVehicles(
					vehicles, stdParameters, routesIdOrShortNames, stopId,
					numberPredictions);

			ApiVehicles apiVehicles = new ApiVehicles(vehicles,
					uiTypesForVehicles);

			// return ApiVehicles response
			return stdParameters.createResponse(apiVehicles);
		} catch (Exception e) {
			// If problem getting data then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}

	/**
	 * Handles the vehicleIds command. Returns list of vehicle IDs.
	 * 
	 * @param stdParameters
	 * @return
	 * @throws WebApplicationException
	 */
	@Path("/command/vehicleIds")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getVehicleIds(
			@BeanParam StandardParameters stdParameters)
			throws WebApplicationException {
		// Make sure request is valid
		stdParameters.validate();

		try {
			// Get Vehicle data from server
			ConfigInterface inter = stdParameters.getConfigInterface();
			List<String> ids = inter.getVehicleIds();
			
			ApiIds apiIds = new ApiIds(ids);
			return stdParameters.createResponse(apiIds);
		} catch (Exception e) {
			// If problem getting data then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}

	/**
	 * Handles the "vehiclesDetails" command. Returns detailed data for all
	 * vehicles or for the vehicles specified via the query string. This data
	 * includes things not necessarily intended for the public, such as schedule
	 * adherence and driver IDs.
	 * <p>
	 * A Response object is returned instead of a regular object so that can
	 * have one method for the both XML and JSON yet always return the proper
	 * media type even if it is configured via the query string "format"
	 * parameter as opposed to the accept header.
	 * 
	 * @param stdParameters
	 *            StdParametersBean that gets the standard parameters from the
	 *            URI, query string, and headers.
	 * @param vehicleIds
	 *            Optional way of specifying which vehicles to get data for
	 * @param routesIdOrShortNames
	 *            Optional way of specifying which routes to get data for
	 * @param stopId
	 *            Optional way of specifying a stop so can get predictions for
	 *            routes and determine which vehicles are the ones generating
	 *            the predictions. The other vehicles are labeled as minor so
	 *            they can be drawn specially in the UI.
	 * @param numberPredictions
	 *            For when determining which vehicles are generating the
	 *            predictions so can label minor vehicles
	 * @return The Response object already configured for the specified media
	 *         type.
	 */
	@Path("/command/vehiclesDetails")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getVehiclesDetails(
			@BeanParam StandardParameters stdParameters,
			@QueryParam(value = "v") List<String> vehicleIds,
			@QueryParam(value = "r") List<String> routesIdOrShortNames,
			@QueryParam(value = "s") String stopId,
			@QueryParam(value = "numPreds") @DefaultValue("3") int numberPredictions)
			throws WebApplicationException {
		// Make sure request is valid
		stdParameters.validate();

		try {
			// Get Vehicle data from server
			VehiclesInterface inter = stdParameters.getVehiclesInterface();

			Collection<IpcVehicle> vehicles;
			if (!routesIdOrShortNames.isEmpty()) {
				vehicles = inter.getForRoute(routesIdOrShortNames);
			} else if (!vehicleIds.isEmpty()) {
				vehicles = inter.get(vehicleIds);
			} else {
				vehicles = inter.get();
			}

			// If the vehicles doesn't exist then throw exception such that
			// Bad Request with an appropriate message is returned.
			if (vehicles == null)
				throw WebUtils.badRequestException("Invalid specifier for "
						+ "vehicles");

			// To determine how vehicles should be drawn in UI. If stop
			// specified
			// when getting vehicle info then only the vehicles being predicted
			// for, should be highlighted. The others should be dimmed.
			Map<String, UiMode> uiTypesForVehicles = determineUiModesForVehicles(
					vehicles, stdParameters, routesIdOrShortNames, stopId,
					numberPredictions);

			// Convert IpcVehiclesDetails to ApiVehiclesDetails
			ApiVehiclesDetails apiVehiclesDetails = new ApiVehiclesDetails(
					vehicles, stdParameters.getAgencyId(), uiTypesForVehicles);

			// return ApiVehiclesDetails response
			return stdParameters.createResponse(apiVehiclesDetails);
		} catch (Exception e) {
			// If problem getting data then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}

	// For specifying how vehicles should be drawn in the UI.
	public enum UiMode {
		NORMAL, SECONDARY, MINOR
	};

	/**
	 * Gets information including vehicle IDs for all vehicles that have been
	 * configured. Useful for creating a vehicle selector.
	 * 
	 * @param stdParameters
	 * @return
	 * @throws WebApplicationException
	 */
	@Path("/command/vehicleConfigs")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getVehicleConfigs(
			@BeanParam StandardParameters stdParameters)
			throws WebApplicationException {
		// Make sure request is valid
		stdParameters.validate();

		try {
			// Get Vehicle data from server
			VehiclesInterface inter = stdParameters.getVehiclesInterface();
			Collection<IpcVehicleConfig> ipcVehicleConfigs = 
					inter.getVehicleConfigs();
			ApiVehicleConfigs apiVehicleConfigs = 
					new ApiVehicleConfigs(ipcVehicleConfigs);
			
			// return ApiVehiclesDetails response
			return stdParameters.createResponse(apiVehicleConfigs);
		} catch (Exception e) {
			// If problem getting data then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}
	
	/**
	 * Determines Map of UiTypes for vehicles so that the vehicles can be drawn
	 * correctly in the UI. If when getting vehicles no specific route and stop
	 * were specified then want to highlight all vehicles. Therefore for this
	 * situation all vehicle IDs will be mapped to UiType.NORMAL.
	 * <p>
	 * But if route and stop were specified then the first vehicle predicted for
	 * at the specified stop should be UiType.NORMAL, the subsequent ones are
	 * set to UiType.SECONDARY, and the remaining vehicles are set to
	 * UiType.MINOR.
	 * 
	 * @param vehicles
	 * @param stdParameters
	 * @param routesIdOrShortNames
	 * @param stopId
	 * @param numberPredictions
	 * @return
	 * @throws RemoteException
	 */
	private static Map<String, UiMode> determineUiModesForVehicles(
			Collection<IpcVehicle> vehicles, StandardParameters stdParameters,
			List<String> routesIdOrShortNames, String stopId,
			int numberPredictions) throws RemoteException {
		// Create map and initialize all vehicles to NORMAL UI mode
		Map<String, UiMode> modeMap = new HashMap<String, UiMode>();

		if (routesIdOrShortNames.isEmpty() || stopId == null) {
			// Stop not specified so simply return NORMAL type for all vehicles
			for (IpcVehicle ipcVehicle : vehicles) {
				modeMap.put(ipcVehicle.getId(), UiMode.NORMAL);
			}
		} else {
			// Stop specified so get predictions and set UI type accordingly
			List<String> vehiclesGeneratingPreds = determineVehiclesGeneratingPreds(
					stdParameters, routesIdOrShortNames, stopId,
					numberPredictions);
			for (IpcVehicle ipcVehicle : vehicles) {
				UiMode uiType = UiMode.MINOR;
				if (!vehiclesGeneratingPreds.isEmpty()
						&& ipcVehicle.getId().equals(
								vehiclesGeneratingPreds.get(0)))
					uiType = UiMode.NORMAL;
				else if (vehiclesGeneratingPreds.contains(ipcVehicle.getId()))
					uiType = UiMode.SECONDARY;

				modeMap.put(ipcVehicle.getId(), uiType);
			}

		}

		// Return results
		return modeMap;
	}

	/**
	 * Provides just a list of vehicle IDs of the vehicles generating
	 * predictions for the specified stop. The list of vehicle IDs will be in
	 * time order such that the first one will be the next predicted vehicle
	 * etc. If routeShortNames or stopId not specified then will return empty
	 * array.
	 * 
	 * @param stdParameters
	 * @param routesIdOrShortNames
	 * @param stopId
	 * @param numberPredictions
	 * @return List of vehicle IDs
	 * @throws RemoteException
	 */
	private static List<String> determineVehiclesGeneratingPreds(
			StandardParameters stdParameters,
			List<String> routesIdOrShortNames, String stopId,
			int numberPredictions) throws RemoteException {
		// The array of vehicle IDs to be returned
		List<String> vehiclesGeneratingPreds = new ArrayList<String>();

		// If stop specified then also get predictions for the stop to
		// determine which vehicles are generating the predictions.
		// If vehicle is not one of the ones generating a prediction
		// then it is labeled as a minor vehicle for the UI.
		if (!routesIdOrShortNames.isEmpty() && stopId != null) {
			PredictionsInterface predsInter = stdParameters
					.getPredictionsInterface();
			List<IpcPredictionsForRouteStopDest> predictions = predsInter.get(
					routesIdOrShortNames.get(0), stopId, numberPredictions);

			// Determine set of which vehicles predictions generated for
			for (IpcPredictionsForRouteStopDest predsForRouteStop : predictions) {
				for (IpcPrediction ipcPrediction : predsForRouteStop
						.getPredictionsForRouteStop()) {
					vehiclesGeneratingPreds.add(ipcPrediction.getVehicleId());
				}
			}
		}

		return vehiclesGeneratingPreds;
	}

	/**
	 * Handles "predictions" command. Gets predictions from server and returns
	 * the corresponding response.
	 * <p>
	 * A Response object is returned instead of a regular object so that can
	 * have one method for the both XML and JSON yet always return the proper
	 * media type even if it is configured via the query string "format"
	 * parameter as opposed to the accept header.
	 * 
	 * @param stdParameters
	 *            StdParametersBean that gets the standard parameters from the
	 *            URI, query string, and headers.
	 * @param routeStopStrs
	 *            List of route/stops. The route specifier is the route id or
	 *            the route short name. It is often best to use route short name
	 *            for consistency across configuration changes (route ID is not
	 *            consistent for many agencies). Each route/stop is separated by
	 *            the "|" character so for example the query string could have
	 *            "rs=43|2029&rs=43|3029"
	 * @param numberPredictions
	 *            Maximum number of predictions to return. Default value is 3.
	 * @return
	 * @throws WebApplicationException
	 */
	@Path("/command/predictions")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getPredictions(
			@BeanParam StandardParameters stdParameters,
			@QueryParam(value = "rs") List<String> routeStopStrs,
			@QueryParam(value = "numPreds") @DefaultValue("3") int numberPredictions)
			throws WebApplicationException {
		// Make sure request is valid
		stdParameters.validate();

		try {
			// Get Prediction data from server
			PredictionsInterface inter = stdParameters
					.getPredictionsInterface();

			// Get predictions by route/stops
			List<RouteStop> routeStopsList = new ArrayList<RouteStop>();
			for (String routeStopStr : routeStopStrs) {
				// Each route/stop is specified as a single string using "\"
				// as a divider (e.g. "routeId|stopId")
				String routeStopParams[] = routeStopStr.split("\\|");
				RouteStop routeStop = new RouteStop(routeStopParams[0],
						routeStopParams[1]);
				routeStopsList.add(routeStop);
			}
			List<IpcPredictionsForRouteStopDest> predictions = inter.get(
					routeStopsList, numberPredictions);

			// return ApiPredictions response
			ApiPredictions predictionsData = new ApiPredictions(predictions);
			return stdParameters.createResponse(predictionsData);
		} catch (Exception e) {
			// If problem getting data then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}

	/**
	 * Handles "predictionsByLoc" command. Gets predictions from server and
	 * returns the corresponding response.
	 * <p>
	 * A Response object is returned instead of a regular object so that can
	 * have one method for the both XML and JSON yet always return the proper
	 * media type even if it is configured via the query string "format"
	 * parameter as opposed to the accept header.
	 * 
	 * @param stdParameters
	 *            StdParametersBean that gets the standard parameters from the
	 *            URI, query string, and headers.
	 * @param lat latitude in decimal degrees
	 * @param lon longitude in decimal degrees
	 * @param maxDistance
	 *            How far away a stop can be from the lat/lon. Default is 1,500
	 *            m.
	 * @param numberPredictions
	 *            Maximum number of predictions to return. Default value is 3.
	 * @return
	 * @throws WebApplicationException
	 */
	@Path("/command/predictionsByLoc")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getPredictions(
			@BeanParam StandardParameters stdParameters,
			@QueryParam(value = "lat") Double lat,
			@QueryParam(value = "lon") Double lon,
			@QueryParam(value = "maxDistance") @DefaultValue("1500.0") double maxDistance,
			@QueryParam(value = "numPreds") @DefaultValue("3") int numberPredictions)
			throws WebApplicationException {
		// Make sure request is valid
		stdParameters.validate();

		if (maxDistance > PredsByLoc.MAX_MAX_DISTANCE)
			throw WebUtils.badRequestException("Maximum maxDistance parameter "
					+ "is " + PredsByLoc.MAX_MAX_DISTANCE + "m but " + maxDistance
					+ "m was specified in the request.");

		try {
			// Get Prediction data from server
			PredictionsInterface inter = stdParameters
					.getPredictionsInterface();

			// Get predictions by location
			List<IpcPredictionsForRouteStopDest> predictions = inter.get(
					new Location(lat, lon), maxDistance, numberPredictions);

			// return ApiPredictions response
			ApiPredictions predictionsData = new ApiPredictions(predictions);
			return stdParameters.createResponse(predictionsData);
		} catch (Exception e) {
			// If problem getting data then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}

	/**
	 * Handles the "routes" command. Returns data describing all of the routes.
	 * Useful for creating a route selector as part of a UI.
	 * 
	 * @param stdParameters
	 * @return
	 * @throws WebApplicationException
	 */
	@Path("/command/routes")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getRoutes(@BeanParam StandardParameters stdParameters)
			throws WebApplicationException {
		// Make sure request is valid
		stdParameters.validate();

		try {
			// Get Vehicle data from server
			ConfigInterface inter = stdParameters.getConfigInterface();
			Collection<IpcRouteSummary> routes = inter.getRoutes();

			// Create and return @QueryParam(value="s") String stopId response
			ApiRouteSummaries routesData = new ApiRouteSummaries(routes);
			return stdParameters.createResponse(routesData);
		} catch (Exception e) {
			// If problem getting data then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}

	/**
	 * Handles the "route" command. Provides detailed information for a route
	 * includes all stops and paths such that it can be drawn in a map.
	 * 
	 * @param stdParameters
	 * @param routeIdOrShortName
	 * @param stopId
	 *            optional. If set then only this stop and the remaining ones on
	 *            the trip pattern are marked as being for the UI and can be
	 *            highlighted. Useful for when want to emphasize in the UI only
	 *            the stops that are of interest to the user.
	 * @param direction
	 *            optional. If set then only the shape for specified direction
	 *            is marked as being for the UI. Needed for situations where a
	 *            single stop is used for both directions of a route and want to
	 *            highlight in the UI only the stops and the shapes that the
	 *            user is actually interested in.
	 * @param tripPatternId
	 *            optional. If set then only the specified trip pattern is
	 *            marked as being for the UI.
	 * @return
	 * @throws WebApplicationException
	 */
	@Path("/command/route")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getRoute(@BeanParam StandardParameters stdParameters,
			@QueryParam(value = "r") String routeIdOrShortName,
			@QueryParam(value = "d") String directionId,
			@QueryParam(value = "s") String stopId,
			@QueryParam(value = "tripPattern") String tripPatternId)
			throws WebApplicationException {
		// Make sure request is valid
		stdParameters.validate();

		try {
			// Get Vehicle data from server
			ConfigInterface inter = stdParameters.getConfigInterface();
			IpcRoute route =
					inter.getRoute(routeIdOrShortName, directionId, stopId,
							tripPatternId);

			// If the route doesn't exist then throw exception such that
			// Bad Request with an appropriate message is returned.
			if (route == null)
				throw WebUtils.badRequestException("Route for route="
						+ routeIdOrShortName + " does not exist.");

			// Create and return ApiRoute response
			ApiRoute routeData = new ApiRoute(route);
			return stdParameters.createResponse(routeData);
		} catch (Exception e) {
			// If problem getting data then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}

	/**
	 * Handles the "stops" command. Returns all stops associated with a route,
	 * grouped by direction. Useful for creating a UI where user needs to select
	 * a stop from a list.
	 * 
	 * @param stdParameters
	 * @param routeShortName
	 * @return
	 * @throws WebApplicationException
	 */
	@Path("/command/stops")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getStops(@BeanParam StandardParameters stdParameters,
			@QueryParam(value = "r") String routesIdOrShortNames)
			throws WebApplicationException {

		// Make sure request is valid
		stdParameters.validate();

		try {
			// Get stops data from server
			ConfigInterface inter = stdParameters.getConfigInterface();
			IpcDirectionsForRoute stopsForRoute = inter
					.getStops(routesIdOrShortNames);

			// If the route doesn't exist then throw exception such that
			// Bad Request with an appropriate message is returned.
			if (stopsForRoute == null)
				throw WebUtils.badRequestException("route="
						+ routesIdOrShortNames + " does not exist.");

			// Create and return ApiDirections response
			ApiDirections directionsData = new ApiDirections(stopsForRoute);
			return stdParameters.createResponse(directionsData);
		} catch (Exception e) {
			// If problem getting data then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}

	/**
	 * Handles the "block" command which outputs configuration data for the
	 * specified block ID and service ID. Includes all sub-data such as trips
	 * and trip patterns.
	 * 
	 * @param stdParameters
	 * @param blockId
	 * @param serviceId
	 * @return
	 * @throws WebApplicationException
	 */
	@Path("/command/block")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getBlock(@BeanParam StandardParameters stdParameters,
			@QueryParam(value = "blockId") String blockId,
			@QueryParam(value = "serviceId") String serviceId)
			throws WebApplicationException {

		// Make sure request is valid
		stdParameters.validate();
		if (serviceId == null)
			throw WebUtils.badRequestException("Must specify serviceId");
		
		try {
			// Get block data from server
			ConfigInterface inter = stdParameters.getConfigInterface();
			IpcBlock ipcBlock = inter.getBlock(blockId, serviceId);

			// If the block doesn't exist then throw exception such that
			// Bad Request with an appropriate message is returned.
			if (ipcBlock == null)
				throw WebUtils.badRequestException("The blockId=" + blockId
						+ " for serviceId=" + serviceId + " does not exist.");

			// Create and return ApiBlock response
			ApiBlock apiBlock = new ApiBlock(ipcBlock);
			return stdParameters.createResponse(apiBlock);
		} catch (Exception e) {
			// If problem getting data then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}

	/**
	 * Handles the "blocksTerse" command which outputs configuration data for the
	 * specified block ID. Does not include trip pattern and schedule data for trips.
	 * 
	 * @param stdParameters
	 * @param blockId
	 * @return
	 * @throws WebApplicationException
	 */
	@Path("/command/blocksTerse")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getBlocksTerse(@BeanParam StandardParameters stdParameters,
			@QueryParam(value = "blockId") String blockId)
			throws WebApplicationException {

		// Make sure request is valid
		stdParameters.validate();
		
		try {
			// Get block data from server
			ConfigInterface inter = stdParameters.getConfigInterface();
			Collection<IpcBlock> ipcBlocks = inter.getBlocks(blockId);

			// If the block doesn't exist then throw exception such that
			// Bad Request with an appropriate message is returned.
			if (ipcBlocks.isEmpty())
				throw WebUtils.badRequestException("The blockId=" + blockId
						+ " does not exist.");

			// Create and return ApiBlock response
			ApiBlocksTerse apiBlocks = new ApiBlocksTerse(ipcBlocks);
			return stdParameters.createResponse(apiBlocks);
		} catch (Exception e) {
			// If problem getting data then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}

	/**
	 * Handles the "blocks" command which outputs configuration data for the
	 * specified block ID. Includes all sub-data such as trips and trip
	 * patterns.
	 * 
	 * @param stdParameters
	 * @param blockId
	 * @return
	 * @throws WebApplicationException
	 */
	@Path("/command/blocks")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getBlocks(@BeanParam StandardParameters stdParameters,
			@QueryParam(value = "blockId") String blockId)
			throws WebApplicationException {

		// Make sure request is valid
		stdParameters.validate();
		
		try {
			// Get block data from server
			ConfigInterface inter = stdParameters.getConfigInterface();
			Collection<IpcBlock> ipcBlocks = inter.getBlocks(blockId);

			// If the block doesn't exist then throw exception such that
			// Bad Request with an appropriate message is returned.
			if (ipcBlocks.isEmpty())
				throw WebUtils.badRequestException("The blockId=" + blockId
						+ " does not exist.");

			// Create and return ApiBlock response
			ApiBlocks apiBlocks = new ApiBlocks(ipcBlocks);
			return stdParameters.createResponse(apiBlocks);
		} catch (Exception e) {
			// If problem getting data then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}

	/**
	 * Handles the "blockIds" command. Returns list of block IDs.
	 * 
	 * @param stdParameters
	 * @return
	 * @throws WebApplicationException
	 */
	@Path("/command/blockIds")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getBlockIds(
			@BeanParam StandardParameters stdParameters,
			@QueryParam(value = "serviceId") String serviceId)
			throws WebApplicationException {
		// Make sure request is valid
		stdParameters.validate();

		try {
			// Get Vehicle data from server
			ConfigInterface inter = stdParameters.getConfigInterface();
			List<String> ids = inter.getBlockIds(serviceId);
			
			ApiIds apiIds = new ApiIds(ids);
			return stdParameters.createResponse(apiIds);
		} catch (Exception e) {
			// If problem getting data then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}

	/**
	 * Gets which blocks are active. Can optionally specify list of routes and
	 * how much before a block is supposed to start is it considered active.
	 * 
	 * @param stdParameters
	 *            StdParametersBean that gets the standard parameters from the
	 *            URI, query string, and headers.
	 * @param routesIdOrShortNames
	 *            Optional parameter for specifying which routes want data for.
	 * @param allowableBeforeTimeSecs
	 *            Optional parameter. A block will be active if the time is
	 *            between the block start time minus allowableBeforeTimeSecs and
	 *            the block end time. Default value for allowableBeforeTimeSecs
	 *            is 0.
	 * @return
	 * @throws WebApplicationException
	 */
	@Path("/command/activeBlocks")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getActiveBlocks(@BeanParam StandardParameters stdParameters,
			@QueryParam(value = "r") List<String> routesIdOrShortNames,
			@QueryParam(value = "t") @DefaultValue("0") int allowableBeforeTimeSecs)
			throws WebApplicationException {

		// Make sure request is valid
		stdParameters.validate();

		try {
			// Get active block data from server
			VehiclesInterface vehiclesInterface = 
					stdParameters.getVehiclesInterface();
			Collection<IpcActiveBlock> activeBlocks = vehiclesInterface
					.getActiveBlocks(routesIdOrShortNames,
							allowableBeforeTimeSecs);

			// Create and return ApiBlock response
			ApiActiveBlocks apiActiveBlocks = 
					new ApiActiveBlocks(activeBlocks, stdParameters.getAgencyId());
			return stdParameters.createResponse(apiActiveBlocks);
		} catch (Exception e) {
			// If problem getting data then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}

	@Path("/command/activeBlocksByRoute")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getActiveBlocksByRoute(
			@BeanParam StandardParameters stdParameters,
			@QueryParam(value = "r") List<String> routesIdOrShortNames,
			@QueryParam(value = "t") @DefaultValue("0") int allowableBeforeTimeSecs)
			throws WebApplicationException {

		// Make sure request is valid
		stdParameters.validate();

		try {
			// Get active block data from server
			VehiclesInterface vehiclesInterface = 
					stdParameters.getVehiclesInterface();
			Collection<IpcActiveBlock> activeBlocks = vehiclesInterface
					.getActiveBlocks(routesIdOrShortNames,
							allowableBeforeTimeSecs);

			// Create and return ApiBlock response
			ApiActiveBlocksRoutes apiActiveBlocksRoutes = new ApiActiveBlocksRoutes(
					activeBlocks, stdParameters.getAgencyId());
			return stdParameters.createResponse(apiActiveBlocksRoutes);
		} catch (Exception e) {
			// If problem getting data then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}
	/**
	 * Handles the "trip" command which outputs configuration data for the
	 * specified trip. Includes all sub-data such as trip patterns.
	 * 
	 * @param stdParameters
	 * @param tripId
	 * @return
	 * @throws WebApplicationException
	 */
	@Path("/command/trip")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getTrip(@BeanParam StandardParameters stdParameters,
			@QueryParam(value = "tripId") String tripId)
			throws WebApplicationException {

		// Make sure request is valid
		stdParameters.validate();

		try {
			// Get block data from server
			ConfigInterface inter = stdParameters.getConfigInterface();
			IpcTrip ipcTrip = inter.getTrip(tripId);

			// If the trip doesn't exist then throw exception such that
			// Bad Request with an appropriate message is returned.
			if (ipcTrip == null)
				throw WebUtils.badRequestException("TripId=" + tripId
						+ " does not exist.");

			// Create and return ApiBlock response.
			// Include stop path info since just outputting single trip.
			ApiTrip apiTrip = new ApiTrip(ipcTrip, true);
			return stdParameters.createResponse(apiTrip);
		} catch (Exception e) {
			// If problem getting data then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}

	/**
	 * Handles the "tripWithTravelTimes" command which outputs configuration
	 * data for the specified trip. Includes all sub-data such as trip patterns.
	 * 
	 * @param stdParameters
	 * @param tripId
	 * @return
	 * @throws WebApplicationException
	 */
	@Path("/command/tripWithTravelTimes")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getTripWithTravelTimes(
			@BeanParam StandardParameters stdParameters,
			@QueryParam(value = "tripId") String tripId)
			throws WebApplicationException {

		// Make sure request is valid
		stdParameters.validate();

		try {
			// Get block data from server
			ConfigInterface inter = stdParameters.getConfigInterface();
			IpcTrip ipcTrip = inter.getTrip(tripId);

			// If the trip doesn't exist then throw exception such that
			// Bad Request with an appropriate message is returned.
			if (ipcTrip == null)
				throw WebUtils.badRequestException("TripId=" + tripId
						+ " does not exist.");

			// Create and return ApiBlock response.
			// Include stop path info since just outputting single trip.
			ApiTripWithTravelTimes apiTrip = new ApiTripWithTravelTimes(
					ipcTrip, true);
			return stdParameters.createResponse(apiTrip);
		} catch (Exception e) {
			// If problem getting data then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}

	/**
	 * Handles the tripIds command. Returns list of trip IDs.
	 * 
	 * @param stdParameters
	 * @return
	 * @throws WebApplicationException
	 */
	@Path("/command/tripIds")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getTripIds(
			@BeanParam StandardParameters stdParameters)
			throws WebApplicationException {
		// Make sure request is valid
		stdParameters.validate();

		try {
			// Get Vehicle data from server
			ConfigInterface inter = stdParameters.getConfigInterface();
			List<String> ids = inter.getTripIds();
			
			ApiIds apiIds = new ApiIds(ids);
			return stdParameters.createResponse(apiIds);
		} catch (Exception e) {
			// If problem getting data then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}

	/**
	 * Handles the "tripPattern" command which outputs trip pattern
	 * configuration data for the specified route.
	 * 
	 * @param stdParameters
	 * @param routesIdOrShortNames
	 * @return
	 * @throws WebApplicationException
	 */
	@Path("/command/tripPatterns")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getTripPatterns(
			@BeanParam StandardParameters stdParameters,
			@QueryParam(value = "r") String routesIdOrShortNames)
			throws WebApplicationException {

		// Make sure request is valid
		stdParameters.validate();

		try {
			// Get block data from server
			ConfigInterface inter = stdParameters.getConfigInterface();
			List<IpcTripPattern> ipcTripPatterns = inter
					.getTripPatterns(routesIdOrShortNames);

			// If the trip doesn't exist then throw exception such that
			// Bad Request with an appropriate message is returned.
			if (ipcTripPatterns == null)
				throw WebUtils.badRequestException("route="
						+ routesIdOrShortNames + " does not exist.");

			// Create and return ApiTripPatterns response
			ApiTripPatterns apiTripPatterns = new ApiTripPatterns(
					ipcTripPatterns);
			return stdParameters.createResponse(apiTripPatterns);
		} catch (Exception e) {
			// If problem getting data then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}

	/**
	 * Handles the "scheduleVertStops" command which outputs schedule for the
	 * specified route. The data is output such that the stops are listed
	 * vertically (and the trips are horizontal). For when there are a good
	 * number of stops but not as many trips, such as for commuter rail.
	 * 
	 * @param stdParameters
	 * @param routesIdOrShortNames
	 * @return
	 * @throws WebApplicationException
	 */
	@Path("/command/scheduleVertStops")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getScheduleVertStops(
			@BeanParam StandardParameters stdParameters,
			@QueryParam(value = "r") String routesIdOrShortNames)
			throws WebApplicationException {

		// Make sure request is valid
		stdParameters.validate();

		try {
			// Get block data from server
			ConfigInterface inter = stdParameters.getConfigInterface();
			List<IpcSchedule> ipcSchedules = inter
					.getSchedules(routesIdOrShortNames);

			// If the trip doesn't exist then throw exception such that
			// Bad Request with an appropriate message is returned.
			if (ipcSchedules == null)
				throw WebUtils.badRequestException("route="
						+ routesIdOrShortNames + " does not exist.");

			// Create and return ApiSchedules response
			ApiSchedulesVertStops apiSchedules = new ApiSchedulesVertStops(ipcSchedules);
			return stdParameters.createResponse(apiSchedules);
		} catch (Exception e) {
			// If problem getting data then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}

	/**
	 * Handles the "scheduleHorizStops" command which outputs schedule for the
	 * specified route. The data is output such that the stops are listed
	 * horizontally (and the trips are vertical). For when there are many more
	 * trips than stops, which is typical for bus routes.
	 * 
	 * @param stdParameters
	 * @param routesIdOrShortNames
	 * @return
	 * @throws WebApplicationException
	 */
	@Path("/command/scheduleHorizStops")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getScheduleHorizStops(
			@BeanParam StandardParameters stdParameters,
			@QueryParam(value = "r") String routesIdOrShortNames)
			throws WebApplicationException {

		// Make sure request is valid
		stdParameters.validate();

		try {
			// Get block data from server
			ConfigInterface inter = stdParameters.getConfigInterface();
			List<IpcSchedule> ipcSchedules = inter
					.getSchedules(routesIdOrShortNames);

			// If the trip doesn't exist then throw exception such that
			// Bad Request with an appropriate message is returned.
			if (ipcSchedules == null)
				throw WebUtils.badRequestException("route="
						+ routesIdOrShortNames + " does not exist.");

			// Create and return ApiSchedules response
			ApiSchedulesHorizStops apiSchedules = new ApiSchedulesHorizStops(ipcSchedules);
			return stdParameters.createResponse(apiSchedules);
		} catch (Exception e) {
			// If problem getting data then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}

	/**
	 * For getting Agency data for a specific agencyId.
	 * 
	 * @param stdParameters
	 * @return
	 * @throws WebApplicationException
	 */
	@Path("/command/agencyGroup")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getAgencyGroup(@BeanParam StandardParameters stdParameters)
			throws WebApplicationException {

		// Make sure request is valid
		stdParameters.validate();

		try {
			// Get block data from server
			ConfigInterface inter = stdParameters.getConfigInterface();
			List<Agency> agencies = inter.getAgencies();

			// Create and return ApiAgencies response
			List<ApiAgency> apiAgencyList = new ArrayList<ApiAgency>();
			for (Agency agency : agencies) {
				apiAgencyList.add(new ApiAgency(stdParameters.getAgencyId(),
						agency));
			}
			ApiAgencies apiAgencies = new ApiAgencies(apiAgencyList);
			return stdParameters.createResponse(apiAgencies);
		} catch (Exception e) {
			// If problem getting data then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}

	/**
	 * For getting calendars that are currently active.
	 * 
	 * @param stdParameters
	 * @return
	 * @throws WebApplicationException
	 */
	@Path("/command/currentCalendars")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getCurrentCalendars(@BeanParam StandardParameters stdParameters)
			throws WebApplicationException {

		// Make sure request is valid
		stdParameters.validate();

		try {
			// Get block data from server
			ConfigInterface inter = stdParameters.getConfigInterface();
			List<IpcCalendar> ipcCalendars = inter.getCurrentCalendars();

			// Create and return ApiAgencies response
			ApiCalendars apiCalendars = new ApiCalendars(ipcCalendars);
			return stdParameters.createResponse(apiCalendars);
		} catch (Exception e) {
			// If problem getting data then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}
	
	/**
	 * For getting all calendars.
	 * 
	 * @param stdParameters
	 * @return
	 * @throws WebApplicationException
	 */
	@Path("/command/allCalendars")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getAllCalendars(@BeanParam StandardParameters stdParameters)
			throws WebApplicationException {

		// Make sure request is valid
		stdParameters.validate();

		try {
			// Get block data from server
			ConfigInterface inter = stdParameters.getConfigInterface();
			List<IpcCalendar> ipcCalendars = inter.getAllCalendars();

			// Create and return ApiAgencies response
			ApiCalendars apiCalendars = new ApiCalendars(ipcCalendars);
			return stdParameters.createResponse(apiCalendars);
		} catch (Exception e) {
			// If problem getting data then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}
	
	/**
	 * Handles the "serviceIds" command. Returns list of all service IDs.
	 * 
	 * @param stdParameters
	 * @return
	 * @throws WebApplicationException
	 */
	@Path("/command/serviceIds")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getServiceIds(
			@BeanParam StandardParameters stdParameters)
			throws WebApplicationException {
		// Make sure request is valid
		stdParameters.validate();

		try {
			// Get Vehicle data from server
			ConfigInterface inter = stdParameters.getConfigInterface();
			List<String> ids = inter.getServiceIds();
			
			ApiIds apiIds = new ApiIds(ids);
			return stdParameters.createResponse(apiIds);
		} catch (Exception e) {
			// If problem getting data then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}

	/**
	 * Handles the currentServiceIds command. Returns list of service IDs that are currently active.
	 * 
	 * @param stdParameters
	 * @return
	 * @throws WebApplicationException
	 */
	@Path("/command/currentServiceIds")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getCurrentServiceIds(
			@BeanParam StandardParameters stdParameters)
			throws WebApplicationException {
		// Make sure request is valid
		stdParameters.validate();

		try {
			// Get Vehicle data from server
			ConfigInterface inter = stdParameters.getConfigInterface();
			List<String> ids = inter.getCurrentServiceIds();
			
			ApiIds apiIds = new ApiIds(ids);
			return stdParameters.createResponse(apiIds);
		} catch (Exception e) {
			// If problem getting data then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}

	/**
	 * Returns status about the specified agency server. Currently provides info
	 * on the DbLogger queue.
	 * 
	 * @param stdParameters
	 * @return
	 * @throws WebApplicationException
	 */
	@Path("/command/serverStatus")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getServerStatus(@BeanParam StandardParameters stdParameters)
			throws WebApplicationException {

		// Make sure request is valid
		stdParameters.validate();

		try {
			// Get status information from server
			ServerStatusInterface inter = stdParameters
					.getServerStatusInterface();
			IpcServerStatus ipcServerStatus = inter.get();

			// Create and return ApiServerStatus response
			ApiServerStatus apiServerStatus = new ApiServerStatus(
					stdParameters.getAgencyId(), ipcServerStatus);
			return stdParameters.createResponse(apiServerStatus);
		} catch (Exception e) {
			// If problem getting data then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}

	/**
	 * Returns info for this particular web server for each agency on how many
	 * outstanding RMI calls there are.
	 * 
	 * @param stdParameters
	 * @return
	 * @throws WebApplicationException
	 */
	@Path("/command/rmiStatus")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getRmiStatus(@BeanParam StandardParameters stdParameters)
			throws WebApplicationException {

		// Make sure request is valid
		stdParameters.validate();

		ApiRmiServerStatus apiRmiServerStatus = new ApiRmiServerStatus();
		return stdParameters.createResponse(apiRmiServerStatus);
	}

	// /**
	// * For creating response of list of vehicles. Would like to make this a
	// * generic type but due to type erasure cannot do so since GenericEntity
	// * somehow works differently with generic types.
	// * <p>
	// * Deprecated because found that much better off using a special
	// * container class for lists of items since that way can control the
	// * name of the list element.
	// *
	// * @param collection
	// * Collection of Vehicle objects to be returned in XML or JSON.
	// * Must be ArrayList so can use GenericEntity to create Response.
	// * @param stdParameters
	// * For specifying media type.
	// * @return The created response in the proper media type.
	// */
	// private static Response createListResponse(Collection<ApiVehicle>
	// collection,
	// StdParametersBean stdParameters) {
	// // Must be ArrayList so can use GenericEntity to create Response.
	// ArrayList<ApiVehicle> arrayList = (ArrayList<ApiVehicle>) collection;
	//
	// // Create a GenericEntity that can handle list of the appropriate
	// // type.
	// GenericEntity<List<ApiVehicle>> entity =
	// new GenericEntity<List<ApiVehicle>>(arrayList) {};
	//
	// // Return the response using the generic entity
	// return createResponse(entity, stdParameters);
	// }

}
