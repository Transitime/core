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

import org.transitime.api.data.ApiBlock;
import org.transitime.api.data.ApiDirections;
import org.transitime.api.data.ApiPredictions;
import org.transitime.api.data.ApiRoute;
import org.transitime.api.data.ApiRouteSummaries;
import org.transitime.api.data.ApiTrip;
import org.transitime.api.data.ApiTripPatterns;
import org.transitime.api.data.ApiVehicles;
import org.transitime.api.data.ApiVehiclesDetails;
import org.transitime.api.utils.StandardParameters;
import org.transitime.api.utils.WebUtils;
import org.transitime.db.structs.Location;
import org.transitime.ipc.data.IpcBlock;
import org.transitime.ipc.data.IpcPrediction;
import org.transitime.ipc.data.IpcPredictionsForRouteStopDest;
import org.transitime.ipc.data.IpcRoute;
import org.transitime.ipc.data.IpcRouteSummary;
import org.transitime.ipc.data.IpcStopsForRoute;
import org.transitime.ipc.data.IpcTrip;
import org.transitime.ipc.data.IpcTripPattern;
import org.transitime.ipc.data.IpcVehicle;
import org.transitime.ipc.interfaces.ConfigInterface;
import org.transitime.ipc.interfaces.PredictionsInterface;
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
     * @param routeIds
     *            Optional way of specifying which routes to get data for
     * @param routeShortNames
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
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getVehicles(@BeanParam StandardParameters stdParameters,
	    @QueryParam(value = "v") List<String> vehicleIds,
	    @QueryParam(value = "r") List<String> routeIds,
	    @QueryParam(value = "rShortName") List<String> routeShortNames,
	    @QueryParam(value = "s") String stopId,
	    @QueryParam(value="numPreds") @DefaultValue("2") int numberPredictions) 
		    throws WebApplicationException {
	// Make sure request is valid
	stdParameters.validate();
	
	try {
	    // Get Vehicle data from server
	    VehiclesInterface inter = stdParameters.getVehiclesInterface();
	    
	    Collection<IpcVehicle> vehicles;
	    if (!routeIds.isEmpty()) {
		vehicles = inter.getForRouteUsingRouteId(routeIds);
	    } else if (!routeShortNames.isEmpty()) {
		vehicles = inter.getForRoute(routeShortNames);
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

	    // To determine how vehicles should be drawn in UI. If stop specified
	    // when getting vehicle info then only the vehicles being predicted
	    // for, should be highlighted. The others should be dimmed.
	    Map<String, UiMode> uiTypesForVehicles = determineUiModesForVehicles(
		    vehicles, stdParameters, routeShortNames, stopId, numberPredictions);

	    ApiVehicles apiVehicles = 
		    new ApiVehicles(vehicles, uiTypesForVehicles);
	    
	    // return ApiVehicles response
	    return stdParameters.createResponse(apiVehicles);
	} catch (RemoteException e) {
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
     * @param routeIds
     *            Optional way of specifying which routes to get data for
     * @param routeShortNames
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
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getVehiclesDetails(@BeanParam StandardParameters stdParameters,
	    @QueryParam(value = "v") List<String> vehicleIds,
	    @QueryParam(value = "r") List<String> routeIds,
	    @QueryParam(value = "rShortName") List<String> routeShortNames,
	    @QueryParam(value = "s") String stopId,
	    @QueryParam(value="numPreds") @DefaultValue("3") int numberPredictions) 
		    throws WebApplicationException {
	// Make sure request is valid
	stdParameters.validate();
	
	try {
	    // Get Vehicle data from server
	    VehiclesInterface inter = stdParameters.getVehiclesInterface();
	    
	    Collection<IpcVehicle> vehicles;
	    if (!routeIds.isEmpty()) {
		vehicles = inter.getForRouteUsingRouteId(routeIds);
	    } else if (!routeShortNames.isEmpty()) {
		vehicles = inter.getForRoute(routeShortNames);
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
	    
	    // To determine how vehicles should be drawn in UI. If stop specified
	    // when getting vehicle info then only the vehicles being predicted
	    // for, should be highlighted. The others should be dimmed.
	    Map<String, UiMode> uiTypesForVehicles = determineUiModesForVehicles(
		    vehicles, stdParameters, routeShortNames, stopId, numberPredictions);
	    
	    // Convert IpcVehiclesDetails to ApiVehiclesDetails
	    ApiVehiclesDetails apiVehiclesDetails = 
		    new ApiVehiclesDetails(vehicles, uiTypesForVehicles);

	    // return ApiVehiclesDetails response
	    return stdParameters.createResponse(apiVehiclesDetails);
	} catch (RemoteException e) {
	    // If problem getting data then return a Bad Request
	    throw WebUtils.badRequestException(e.getMessage());
	}
    }

    // For specifying how vehicles should be drawn in the UI. 
    public enum UiMode {NORMAL, SECONDARY, MINOR};   

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
     * @param routeShortNames
     * @param stopId
     * @param numberPredictions
     * @return
     * @throws RemoteException
     */
    private static Map<String, UiMode> determineUiModesForVehicles(
	    Collection<IpcVehicle> vehicles, StandardParameters stdParameters,
	    List<String> routeShortNames, String stopId, int numberPredictions)
	    throws RemoteException {
	// Create map and initialize all vehicles to NORMAL UI mode
	Map<String, UiMode> modeMap = new HashMap<String, UiMode>();

	if (routeShortNames.isEmpty() || stopId == null) {
	    // Stop not specified so simply return NORMAL type for all vehicles
	    for (IpcVehicle ipcVehicle : vehicles) {
		modeMap.put(ipcVehicle.getId(), UiMode.NORMAL);
	    }
	} else {
	    // Stop specified so get predictions and set UI type accordingly
	    List<String> vehiclesGeneratingPreds = determineVehiclesGeneratingPreds(
		    stdParameters, routeShortNames, stopId, numberPredictions);
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
     * etc. If routeShortNames or stopId not specified then will return empty array.
     * 
     * @param stdParameters
     * @param routeShortNames
     * @param stopId
     * @param numberPredictions
     * @return List of vehicle IDs
     * @throws RemoteException
     */
    private static List<String> determineVehiclesGeneratingPreds(
	    StandardParameters stdParameters, List<String> routeShortNames,
	    String stopId, int numberPredictions) throws RemoteException {
	// The array of vehicle IDs to be returned
	List<String> vehiclesGeneratingPreds = new ArrayList<String>();

	// If stop specified then also get predictions for the stop to
	// determine which vehicles are generating the predictions.
	// If vehicle is not one of the ones generating a prediction
	// then it is labeled as a minor vehicle for the UI.
	if (!routeShortNames.isEmpty() && stopId != null) {
	    PredictionsInterface predsInter = stdParameters
		    .getPredictionsInterface();
	    List<IpcPredictionsForRouteStopDest> predictions = predsInter.get(
		    routeShortNames.get(0), stopId, numberPredictions);

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
     *            List of route/stops. The route specifier is the route short
     *            name for consistency across configuration changes (route ID is
     *            not consistent for many agencies). Each route/stop is
     *            separated by the "|" character so for example the query string
     *            could have "rs=43|2029&rs=43|3029"
     * @param numberPredictions
     *            Maximum number of predictions to return. Default value is 3.
     * @return
     * @throws WebApplicationException
     */
    @Path("/command/predictions")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getPredictions(@BeanParam StandardParameters stdParameters,
	    @QueryParam(value="rs") List<String> routeStopStrs,
	    @QueryParam(value="numPreds") @DefaultValue("3") int numberPredictions) 
		    throws WebApplicationException {
	// Make sure request is valid
	stdParameters.validate();
	
	try {
	    // Get Prediction data from server
	    PredictionsInterface inter = stdParameters.getPredictionsInterface();
	    
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
	} catch (RemoteException e) {
	    // If problem getting data then return a Bad Request
	    throw WebUtils.badRequestException(e.getMessage());
	}
    }

    // The maximum allowable maxDistance for getting predictions by location
    private final static double MAX_MAX_DISTANCE = 2000.0;
    
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
     * @param lat
     * @param lon
     * @param maxDistance
     *            How far away a stop can be from the lat/lon. Default is 2,000 m.
     * @param numberPredictions
     *            Maximum number of predictions to return. Default value is 3.
     * @return
     * @throws WebApplicationException
     */
    @Path("/command/predictionsByLoc")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getPredictions(@BeanParam StandardParameters stdParameters,
	    @QueryParam(value="lat") Double lat,
	    @QueryParam(value="lon") Double lon,
	    @QueryParam(value="maxDistance") @DefaultValue("1500.0") double maxDistance,
	    @QueryParam(value="numPreds") @DefaultValue("3") int numberPredictions) 
		    throws WebApplicationException {
	// Make sure request is valid
	stdParameters.validate();
	
	if (maxDistance > MAX_MAX_DISTANCE)
	    throw WebUtils.badRequestException("Maximum maxDistance parameter "
	    	+ "is " + MAX_MAX_DISTANCE + "m but " + maxDistance 
	    	+ "m was specified in the request.");
	
	try {
	    // Get Prediction data from server
	    PredictionsInterface inter = 
		    stdParameters.getPredictionsInterface();
	    
		// Get predictions by location
	    List<IpcPredictionsForRouteStopDest> predictions = inter.get(
		    new Location(lat, lon), maxDistance, numberPredictions);
	    
	    // return ApiPredictions response
	    ApiPredictions predictionsData = new ApiPredictions(predictions);
	    return stdParameters.createResponse(predictionsData);
	} catch (RemoteException e) {
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
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
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
	} catch (RemoteException e) {
	    // If problem getting data then return a Bad Request
	    throw WebUtils.badRequestException(e.getMessage());
	}
    }

    /**
     * Handles the "route" command. Provides detailed information for a route
     * includes all stops and paths such that it can be drawn in a map.
     * 
     * @param stdParameters
     * @param routeId
     * @param routeShortName
     * @param stopId
     * @param tripPatternId
     * @return
     * @throws WebApplicationException
     */
    @Path("/command/route")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getRoute(@BeanParam StandardParameters stdParameters,
	    @QueryParam(value="r") String routeId,
	    @QueryParam(value="rShortName") String routeShortName,
	    @QueryParam(value="s") String stopId,
	    @QueryParam(value="tripPattern") String tripPatternId) 
		    throws WebApplicationException {
	// Make sure request is valid
	stdParameters.validate();
	
	try {
	    // Get Vehicle data from server
	    ConfigInterface inter = stdParameters.getConfigInterface();	    
	    IpcRoute route;
	    
	    if (routeId != null) {
		route = inter.getRoute(routeShortName, stopId, tripPatternId);
		// If the route doesn't exist then throw exception such that
		// Bad Request with an appropriate message is returned.
		if (route == null)
		    throw WebUtils.badRequestException("Route for "
			    + "routeShortName=" + routeShortName
			    + " does not exist.");
	    } else {
		route = inter.getRouteUsingRouteId(routeId, stopId,
			tripPatternId);
		// If the route doesn't exist then throw exception such that
		// Bad Request with an appropriate message is returned.
		if (route == null)
		    throw WebUtils.badRequestException("Route for routeId="
			    + routeId + " does not exist.");
	    }				    
	    
	    // Create and return ApiRoute response
	    ApiRoute routeData = new ApiRoute(route);
	    return stdParameters.createResponse(routeData);
	} catch (RemoteException e) {
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
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getStops(@BeanParam StandardParameters stdParameters,
	    @QueryParam(value="rShortName") String routeShortName) 
		    throws WebApplicationException {

	// Make sure request is valid
	stdParameters.validate();
	
	try {
	    // Get stops data from server
	    ConfigInterface inter = stdParameters.getConfigInterface();	    
	    IpcStopsForRoute stopsForRoute = inter.getStops(routeShortName);

	    // If the route doesn't exist then throw exception such that
	    // Bad Request with an appropriate message is returned.
	    if (stopsForRoute == null)
		throw WebUtils.badRequestException("routeShortName=" + routeShortName 
			+ " does not exist.");
	    
	    // Create and return ApiDirections response
	    ApiDirections directionsData = new ApiDirections(stopsForRoute);
	    return stdParameters.createResponse(directionsData);
	} catch (RemoteException e) {
	    // If problem getting data then return a Bad Request
	    throw WebUtils.badRequestException(e.getMessage());
	}
    }
    
    /**
     * Handles the "block" command which outputs configuration data for
     * the specified block. Includes all sub-data such as trips
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
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getBlock(@BeanParam StandardParameters stdParameters,
	    @QueryParam(value="blockId") String blockId,
	    @QueryParam(value="serviceId") String serviceId) 
		    throws WebApplicationException {

	// Make sure request is valid
	stdParameters.validate();
	
	try {
	    // Get block data from server
	    ConfigInterface inter = stdParameters.getConfigInterface();	    
	    IpcBlock ipcBlock = inter.getBlock(blockId, serviceId);

	    // If the block doesn't exist then throw exception such that
	    // Bad Request with an appropriate message is returned.
	    if (ipcBlock == null)
		throw WebUtils.badRequestException("BlockId=" + blockId 
			+ " for serviceId=" + serviceId + " does not exist.");
	    
	    // Create and return ApiBlock response
	    ApiBlock apiBlock = new ApiBlock(ipcBlock);
	    return stdParameters.createResponse(apiBlock);
	} catch (RemoteException e) {
	    // If problem getting data then return a Bad Request
	    throw WebUtils.badRequestException(e.getMessage());
	}
    }

    /**
     * Handles the "trip" command which outputs configuration data for
     * the specified trip. Includes all sub-data such as trip patterns. 
     * 
     * @param stdParameters
     * @param tripId
     * @return
     * @throws WebApplicationException
     */
    @Path("/command/trip")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getTrip(@BeanParam StandardParameters stdParameters,
	    @QueryParam(value="tripId") String tripId) 
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
	} catch (RemoteException e) {
	    // If problem getting data then return a Bad Request
	    throw WebUtils.badRequestException(e.getMessage());
	}
    }

    /**
     * Handles the "tripPattern" command which outputs trip pattern configuration data for
     * the specified route.
     * 
     * @param stdParameters
     * @param routeShortName
     * @return
     * @throws WebApplicationException
     */
    @Path("/command/trippatterns")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getTripPatterns(@BeanParam StandardParameters stdParameters,
	    @QueryParam(value="rShortName") String routeShortName) 
		    throws WebApplicationException {

	// Make sure request is valid
	stdParameters.validate();
	
	try {
	    // Get block data from server
	    ConfigInterface inter = stdParameters.getConfigInterface();	    
	    List<IpcTripPattern> ipcTripPatterns = 
		    inter.getTripPatterns(routeShortName);

	    // If the trip doesn't exist then throw exception such that
	    // Bad Request with an appropriate message is returned.
	    if (ipcTripPatterns == null)
		throw WebUtils.badRequestException("routeShortName=" + routeShortName 
			+ " does not exist.");
	    
	    // Create and return ApiBlock response
	    ApiTripPatterns apiTripPatterns = 
		    new ApiTripPatterns(ipcTripPatterns);
	    return stdParameters.createResponse(apiTripPatterns);
	} catch (RemoteException e) {
	    // If problem getting data then return a Bad Request
	    throw WebUtils.badRequestException(e.getMessage());
	}
    }

    
    //    /**
//     * For creating response of list of vehicles. Would like to make this a
//     * generic type but due to type erasure cannot do so since GenericEntity
//     * somehow works differently with generic types.
//     * <p>
//     * Deprecated because found that much better off using a special
//     * container class for lists of items since that way can control the
//     * name of the list element.
//     * 
//     * @param collection
//     *            Collection of Vehicle objects to be returned in XML or JSON.
//     *            Must be ArrayList so can use GenericEntity to create Response.
//     * @param stdParameters
//     *            For specifying media type.
//     * @return The created response in the proper media type.
//     */
//    private static Response createListResponse(Collection<ApiVehicle> collection,
//	    StdParametersBean stdParameters) {
//	// Must be ArrayList so can use GenericEntity to create Response.
//	ArrayList<ApiVehicle> arrayList = (ArrayList<ApiVehicle>) collection;
//	
//	// Create a GenericEntity that can handle list of the appropriate
//	// type.
//	GenericEntity<List<ApiVehicle>> entity = 
//	            new GenericEntity<List<ApiVehicle>>(arrayList) {};
//	            
//	// Return the response using the generic entity
//	return createResponse(entity, stdParameters);
//    }

}
