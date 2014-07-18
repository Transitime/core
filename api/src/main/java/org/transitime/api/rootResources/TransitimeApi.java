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
import java.util.List;

import javax.ws.rs.BeanParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.transitime.api.data.ApiDirections;
import org.transitime.api.data.ApiPredictions;
import org.transitime.api.data.ApiRoute;
import org.transitime.api.data.ApiRouteSummaries;
import org.transitime.api.data.ApiVehicles;
import org.transitime.api.data.ApiVehiclesDetails;
import org.transitime.api.utils.StandardParameters;
import org.transitime.api.utils.WebUtils;
import org.transitime.db.structs.Location;
import org.transitime.ipc.clients.ConfigInterfaceFactory;
import org.transitime.ipc.clients.PredictionsInterfaceFactory;
import org.transitime.ipc.clients.VehiclesInterfaceFactory;
import org.transitime.ipc.data.IpcPredictionsForRouteStopDest;
import org.transitime.ipc.data.IpcRoute;
import org.transitime.ipc.data.IpcRouteSummary;
import org.transitime.ipc.data.IpcStopsForRoute;
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
     * @return The Response object already configured for the specified media
     *         type.
     */
    @Path("/command/vehicles")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getVehicles(@BeanParam StandardParameters stdParameters,
	    @QueryParam(value = "v") List<String> vehicleIds,
	    @QueryParam(value = "r") List<String> routeIds,
	    @QueryParam(value = "rShortName") List<String> routeShortNames) 
		    throws WebApplicationException {
	// Make sure request is valid
	stdParameters.validate();
	
	try {
	    // Get Vehicle data from server
	    VehiclesInterface inter = 
		    getVehiclesInterface(stdParameters.getAgencyId());
	    
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

	    // return ApiVehicles response
	    return stdParameters.createResponse(new ApiVehicles(vehicles));
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
     * @return The Response object already configured for the specified media
     *         type.
     */
    @Path("/command/vehiclesDetails")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getVehiclesDetails(@BeanParam StandardParameters stdParameters,
	    @QueryParam(value = "v") List<String> vehicleIds,
	    @QueryParam(value = "r") List<String> routeIds,
	    @QueryParam(value = "rShortName") List<String> routeShortNames) 
		    throws WebApplicationException {
	// Make sure request is valid
	stdParameters.validate();
	
	try {
	    // Get Vehicle data from server
	    VehiclesInterface inter = 
		    getVehiclesInterface(stdParameters.getAgencyId());
	    
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

	    // return ApiVehiclesDetails response
	    ApiVehiclesDetails apiVehiclesDetails = 
		    new ApiVehiclesDetails(vehicles);
	    return stdParameters.createResponse(apiVehiclesDetails);
	} catch (RemoteException e) {
	    // If problem getting data then return a Bad Request
	    throw WebUtils.badRequestException(e.getMessage());
	}
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
	    PredictionsInterface inter = 
		    getPredictionsInterface(stdParameters.getAgencyId());
	    
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
		    getPredictionsInterface(stdParameters.getAgencyId());
	    
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
    
    @Path("/command/routes")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getRoutes(@BeanParam StandardParameters stdParameters) 
		    throws WebApplicationException {
	// Make sure request is valid
	stdParameters.validate();
	
	try {
	    // Get Vehicle data from server
	    ConfigInterface inter = 
		    getConfigInterface(stdParameters.getAgencyId());	    
	    Collection<IpcRouteSummary> routes = inter.getRoutes();

	    // Create and return @QueryParam(value="s") String stopId response
	    ApiRouteSummaries routesData = new ApiRouteSummaries(routes);
	    return stdParameters.createResponse(routesData);
	} catch (RemoteException e) {
	    // If problem getting data then return a Bad Request
	    throw WebUtils.badRequestException(e.getMessage());
	}
    }

    @Path("/command/route")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getRoute(@BeanParam StandardParameters stdParameters,
	    @QueryParam(value="r") String routeShrtNm,
	    @QueryParam(value="s") String stopId,
	    @QueryParam(value="tripPattern") String destinationName) 
		    throws WebApplicationException {
	// Make sure request is valid
	stdParameters.validate();
	
	try {
	    // Get Vehicle data from server
	    ConfigInterface inter = 
		    getConfigInterface(stdParameters.getAgencyId());	    
	    IpcRoute route = inter.getRoute(routeShrtNm, stopId, destinationName);

	    // Create and return ApiRoute response
	    ApiRoute routeData = new ApiRoute(route);
	    return stdParameters.createResponse(routeData);
	} catch (RemoteException e) {
	    // If problem getting data then return a Bad Request
	    throw WebUtils.badRequestException(e.getMessage());
	}
    }
    
    @Path("/command/stops")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getStops(@BeanParam StandardParameters stdParameters,
	    @QueryParam(value="r") String routeShrtNm) 
		    throws WebApplicationException {

	// Make sure request is valid
	stdParameters.validate();
	
	try {
	    // Get Vehicle data from server
	    ConfigInterface inter = 
		    getConfigInterface(stdParameters.getAgencyId());	    
	    IpcStopsForRoute stopsForRoute = inter.getStops(routeShrtNm);

	    // Create and return ApiRoute response
	    ApiDirections directionsData = new ApiDirections(stopsForRoute);
	    return stdParameters.createResponse(directionsData);
	} catch (RemoteException e) {
	    // If problem getting data then return a Bad Request
	    throw WebUtils.badRequestException(e.getMessage());
	}

    }
    
     /**
     * Gets the VehiclesInterface for the specified agencyId. If not valid then
     * throws WebApplicationException.
     * 
     * @param agencyId
     * @return The VehiclesInterface
     */
    private static VehiclesInterface getVehiclesInterface(String agencyId) 
	    throws WebApplicationException {
	VehiclesInterface vehiclesInterface = VehiclesInterfaceFactory.get(agencyId);
	if (vehiclesInterface == null)
	    throw WebUtils.badRequestException("Agency ID " + agencyId + " is not valid");
	
	return vehiclesInterface;
    }

    /**
     * Gets the PredictionsInterface for the specified agencyId. If not valid then
     * throws WebApplicationException.
     * 
     * @param agencyId
     * @return The VehiclesInterface
     */
    private static PredictionsInterface getPredictionsInterface(String agencyId) 
	    throws WebApplicationException {
	PredictionsInterface predictionsInterface = PredictionsInterfaceFactory.get(agencyId);
	if (predictionsInterface == null)
	    throw WebUtils.badRequestException("Agency ID " + agencyId + " is not valid");
	
	return predictionsInterface;
    }

    /**
     * Gets the ConfigInterface for the specified agencyId. If not valid then
     * throws WebApplicationException.
     * 
     * @param agencyId
     * @return The VehiclesInterface
     */
    private static ConfigInterface getConfigInterface(String agencyId) 
	    throws WebApplicationException {
	ConfigInterface configInterface = ConfigInterfaceFactory.get(agencyId);
	if (configInterface == null)
	    throw WebUtils.badRequestException("Agency ID " + agencyId + " is not valid");
	
	return configInterface;
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
