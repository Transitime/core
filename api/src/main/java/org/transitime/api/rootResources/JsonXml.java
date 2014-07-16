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

import java.io.IOException;
import java.io.OutputStream;
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
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import org.transitime.api.data.ApiDirections;
import org.transitime.api.data.ApiPredictions;
import org.transitime.api.data.ApiRoute;
import org.transitime.api.data.ApiRouteSummaries;
import org.transitime.api.data.ApiVehicles;
import org.transitime.api.data.ApiVehiclesDetails;
import org.transitime.api.utils.KeyValidator;
import org.transitime.api.utils.StdParametersBean;
import org.transitime.api.utils.UsageValidator;
import org.transitime.api.utils.WebUtils;
import org.transitime.feed.gtfsRt.GtfsRtTripFeed;
import org.transitime.feed.gtfsRt.GtfsRtVehicleFeed;
import org.transitime.feed.gtfsRt.OctalDecoder;
import org.transitime.ipc.clients.ConfigInterfaceFactory;
import org.transitime.ipc.clients.PredictionsInterfaceFactory;
import org.transitime.ipc.clients.VehiclesInterfaceFactory;
import org.transitime.ipc.data.IpcDirection;
import org.transitime.ipc.data.IpcPredictionsForRouteStopDest;
import org.transitime.ipc.data.IpcRoute;
import org.transitime.ipc.data.IpcRouteSummary;
import org.transitime.ipc.data.IpcStopsForRoute;
import org.transitime.ipc.data.IpcVehicle;
import org.transitime.ipc.interfaces.ConfigInterface;
import org.transitime.ipc.interfaces.PredictionsInterface;
import org.transitime.ipc.interfaces.VehiclesInterface;
import org.transitime.ipc.interfaces.PredictionsInterface.RouteStop;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;

/**
 *
 *
 * @author SkiBu Smith
 *
 */
@Path("/key/{key}/agency/{agency}")
public class JsonXml {

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
    public Response getVehicles(@BeanParam StdParametersBean stdParameters,
	    @QueryParam(value = "v") List<String> vehicleIds,
	    @QueryParam(value = "r") List<String> routeIds,
	    @QueryParam(value = "rShortName") List<String> routeShortNames) 
		    throws WebApplicationException {
	// Make sure request is valid
	validate(stdParameters);
	
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
	    return createResponse(new ApiVehicles(vehicles), stdParameters);
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
    public Response getVehiclesDetails(@BeanParam StdParametersBean stdParameters,
	    @QueryParam(value = "v") List<String> vehicleIds,
	    @QueryParam(value = "r") List<String> routeIds,
	    @QueryParam(value = "rShortName") List<String> routeShortNames) 
		    throws WebApplicationException {
	// Make sure request is valid
	validate(stdParameters);
	
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
	    return createResponse(new ApiVehiclesDetails(vehicles),
		    stdParameters);
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
     *            Maximum number of predictions to return
     * @return
     * @throws WebApplicationException
     */
    @Path("/command/predictions")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getPredictions(@BeanParam StdParametersBean stdParameters,
	    @QueryParam(value="rs") List<String> routeStopStrs,
	    @QueryParam(value="numPreds") @DefaultValue("3") int numberPredictions) 
		    throws WebApplicationException {
	// Make sure request is valid
	validate(stdParameters);
	
	try {
	    // Get Prediction data from server
	    PredictionsInterface inter = 
		    getPredictionsInterface(stdParameters.getAgencyId());
	    
	    List<RouteStop> routeStopsList = new ArrayList<RouteStop>();
	    for (String routeStopStr : routeStopStrs) {
		// Each route/stop is specified as a single string using "\"
		// as a divider (e.g. "routeId|stopId")
		String routeStopParams[] = routeStopStr.split("\\|");
		RouteStop routeStop = new RouteStop(routeStopParams[0], routeStopParams[1]);
		routeStopsList.add(routeStop);
	    }
	    List<IpcPredictionsForRouteStopDest> predsForRouteStopDestinations = 
		    inter.get(routeStopsList, numberPredictions);

	    // return ApiPredictions response
	    ApiPredictions predictionsData = new ApiPredictions(predsForRouteStopDestinations);
	    return createResponse(predictionsData, stdParameters);
	} catch (RemoteException e) {
	    // If problem getting data then return a Bad Request
	    throw WebUtils.badRequestException(e.getMessage());
	}
    }

    @Path("/command/routes")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getRoutes(@BeanParam StdParametersBean stdParameters) 
		    throws WebApplicationException {
	// Make sure request is valid
	validate(stdParameters);
	
	try {
	    // Get Vehicle data from server
	    ConfigInterface inter = 
		    getConfigInterface(stdParameters.getAgencyId());	    
	    Collection<IpcRouteSummary> routes = inter.getRoutes();

	    // Create and return @QueryParam(value="s") String stopId response
	    ApiRouteSummaries routesData = new ApiRouteSummaries(routes);
	    return createResponse(routesData, stdParameters);
	} catch (RemoteException e) {
	    // If problem getting data then return a Bad Request
	    throw WebUtils.badRequestException(e.getMessage());
	}
    }

    @Path("/command/route")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getRoute(@BeanParam StdParametersBean stdParameters,
	    @QueryParam(value="r") String routeShrtNm,
	    @QueryParam(value="s") String stopId,
	    @QueryParam(value="tripPattern") String destinationName) 
		    throws WebApplicationException {
	// Make sure request is valid
	validate(stdParameters);
	
	try {
	    // Get Vehicle data from server
	    ConfigInterface inter = 
		    getConfigInterface(stdParameters.getAgencyId());	    
	    IpcRoute route = inter.getRoute(routeShrtNm, stopId, destinationName);

	    // Create and return ApiRoute response
	    ApiRoute routeData = new ApiRoute(route);
	    return createResponse(routeData, stdParameters);
	} catch (RemoteException e) {
	    // If problem getting data then return a Bad Request
	    throw WebUtils.badRequestException(e.getMessage());
	}
    }
    
    @Path("/command/stops")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getStops(@BeanParam StdParametersBean stdParameters,
	    @QueryParam(value="r") String routeShrtNm) 
		    throws WebApplicationException {

	// Make sure request is valid
	validate(stdParameters);
	
	try {
	    // Get Vehicle data from server
	    ConfigInterface inter = 
		    getConfigInterface(stdParameters.getAgencyId());	    
	    IpcStopsForRoute stopsForRoute = inter.getStops(routeShrtNm);

	    // Create and return ApiRoute response
	    ApiDirections directionsData = new ApiDirections(stopsForRoute);
	    return createResponse(directionsData, stdParameters);
	} catch (RemoteException e) {
	    // If problem getting data then return a Bad Request
	    throw WebUtils.badRequestException(e.getMessage());
	}

    }
    
    private final int MAX_GTFS_RT_CACHE_SECS = 15;

    /**
     * For getting GTFS-realtime Vehicle Positions data for all vehicles.
     * 
     * @param stdParameters
     * @param format
     *            if set to "human" then will output GTFS-rt data in human
     *            readable format. Otherwise will output data in binary format.
     * @return
     * @throws WebApplicationException
     */
    @Path("/command/gtfs-rt/vehiclePositions")
    @GET
    @Produces({MediaType.TEXT_PLAIN})
    public Response getGtfsRealtimeVehiclePositionsFeed(
	    final @BeanParam StdParametersBean stdParameters,
	    @QueryParam(value="format") String format) 
	    throws WebApplicationException {
	
	// Make sure request is valid
	validate(stdParameters);
	
	// Determine if output should be in human readable format or in 
	// standard binary GTFS-realtime format.
	final boolean humanFormatOutput = "human".equals(format);
	
	// Determine the appropriate output format. For plain text best to use
	// MediaType.TEXT_PLAIN so that output is formatted properly in web 
	// browser instead of newlines being removed. For binary output should
	// use MediaType.APPLICATION_OCTET_STREAM.
	String mediaType = humanFormatOutput ? 
		MediaType.TEXT_PLAIN : MediaType.APPLICATION_OCTET_STREAM;
	
	// Prepare a StreamingOutput object so can write using it
	StreamingOutput stream = new StreamingOutput() {
	    public void write(OutputStream outputStream) 
		    throws IOException, WebApplicationException {
		try {
		    FeedMessage message = 
			    GtfsRtVehicleFeed.getPossiblyCachedMessage(
				    stdParameters.getAgencyId(),
				    MAX_GTFS_RT_CACHE_SECS);
		    
		    // Output in human readable format or in standard binary format
		    if (humanFormatOutput) {
			// Output data in human readable format. First, convert
			// the octal escaped message to regular UTF encoding.
			String decodedMessage = OctalDecoder
				.convertOctalEscapedString(message.toString());
			outputStream.write(decodedMessage.getBytes());
		    } else {
			// Standard binary output
			message.writeTo(outputStream);
		    }
		} catch (Exception e) {
		    throw new WebApplicationException(e);
		}
	    }
	};

	// Write out the data using the output stream 
	return Response.ok(stream).type(mediaType).build();
    }

    /**
     * For getting GTFS-realtime Vehicle Positions data for all vehicles.
     * 
     * @param stdParameters
     * @param format
     *            if set to "human" then will output GTFS-rt data in human
     *            readable format. Otherwise will output data in binary format.
     * @return
     * @throws WebApplicationException
     */
    @Path("/command/gtfs-rt/tripUpdates")
    @GET
    @Produces({MediaType.TEXT_PLAIN})
    public Response getGtfsRealtimeTripFeed(
	    final @BeanParam StdParametersBean stdParameters,
	    @QueryParam(value="format") String format) 
	    throws WebApplicationException {
	
	// Make sure request is valid
	validate(stdParameters);
	
	// Determine if output should be in human readable format or in 
	// standard binary GTFS-realtime format.
	final boolean humanFormatOutput = "human".equals(format);
	
	// Determine the appropriate output format. For plain text best to use
	// MediaType.TEXT_PLAIN so that output is formatted properly in web 
	// browser instead of newlines being removed. For binary output should
	// use MediaType.APPLICATION_OCTET_STREAM.
	String mediaType = humanFormatOutput ? 
		MediaType.TEXT_PLAIN : MediaType.APPLICATION_OCTET_STREAM;
	
	// Prepare a StreamingOutput object so can write using it
	StreamingOutput stream = new StreamingOutput() {
	    public void write(OutputStream outputStream) 
		    throws IOException, WebApplicationException {
		try {
		    FeedMessage message = 
			    GtfsRtTripFeed.getPossiblyCachedMessage(
				    stdParameters.getAgencyId(),
				    MAX_GTFS_RT_CACHE_SECS);
		    
		    // Output in human readable format or in standard binary format
		    if (humanFormatOutput) {
			// Output data in human readable format. First, convert
			// the octal escaped message to regular UTF encoding.
			String decodedMessage = OctalDecoder
				.convertOctalEscapedString(message.toString());
			outputStream.write(decodedMessage.getBytes());
		    } else {
			// Standard binary output
			message.writeTo(outputStream);
		    }
		} catch (Exception e) {
		    throw new WebApplicationException(e);
		}
	    }
	};

	// Write out the data using the output stream 
	return Response.ok(stream).type(mediaType).build();
    }

    /**
     * Makes sure not access feed too much and that the key is valid.
     * 
     * @param stdParameters
     * @throws WebApplicationException
     */
    private static void validate(StdParametersBean stdParameters) 
    		throws WebApplicationException {
	// Make sure not accessing feed too much. This needs to be done
	// early in the handling of the request so can stop processing
	// bad requests before too much effort is expended. Throw exception 
	// if usage limits exceeded.
	UsageValidator.getInstance().validateUsage(stdParameters);
	
	// Make sure the application key is valid
	KeyValidator.getInstance().validateKey(stdParameters);
    }
    
    /**
     * For creating a Response of a single object of the appropriate media type.
     * For List<> of object need to use createListResponse() instead.
     * 
     * @param object
     *            Object to be returned in XML or JSON
     * @param parameters
     *            For specifying media type.
     * @return The created response in the proper media type.
     */
    private static Response createResponse(Object object,
	    StdParametersBean stdParameters) {
	// Start building the response
	ResponseBuilder responseBuilder = Response.ok(object); 
	
	// Since this is a truly open API intended to be used by
	// other web pages allow cross-origin requests.
	responseBuilder.header("Access-Control-Allow-Origin", "*");
	
	// Specify media type of XML or JSON
	responseBuilder.type(stdParameters.getMediaType());
	
	// Return the response
	return responseBuilder.build();
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
