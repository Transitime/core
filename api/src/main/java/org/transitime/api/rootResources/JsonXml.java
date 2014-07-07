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
import java.util.Collection;
import java.util.List;

import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.transitime.api.data.VehiclesData;
import org.transitime.api.data.VehiclesDetailsData;
import org.transitime.api.utils.KeyValidator;
import org.transitime.api.utils.StdParametersBean;
import org.transitime.api.utils.UsageValidator;
import org.transitime.api.utils.WebUtils;
import org.transitime.ipc.clients.VehiclesInterfaceFactory;
import org.transitime.ipc.data.Vehicle;
import org.transitime.ipc.interfaces.VehiclesInterface;

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
	    VehiclesInterface inter = getVehicleInterface(stdParameters.getAgencyId());
	    
	    Collection<Vehicle> vehicles;
	    if (!routeIds.isEmpty()) {
		vehicles = inter.getForRouteUsingRouteId(routeIds);
	    } else if (!routeShortNames.isEmpty()) {
		vehicles = inter.getForRoute(routeShortNames);
	    } else if (!vehicleIds.isEmpty()) {
		vehicles = inter.get(vehicleIds);
	    } else {
		vehicles = inter.get();
	    }

	    // return VehiclesData response
	    return createResponse(new VehiclesData(vehicles), stdParameters);
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
	    VehiclesInterface inter = getVehicleInterface(stdParameters.getAgencyId());
	    
	    Collection<Vehicle> vehicles;
	    if (!routeIds.isEmpty()) {
		vehicles = inter.getForRouteUsingRouteId(routeIds);
	    } else if (!routeShortNames.isEmpty()) {
		vehicles = inter.getForRoute(routeShortNames);
	    } else if (!vehicleIds.isEmpty()) {
		vehicles = inter.get(vehicleIds);
	    } else {
		vehicles = inter.get();
	    }

	    // return VehiclesDetailsData response
	    return createResponse(new VehiclesDetailsData(vehicles), stdParameters);
	} catch (RemoteException e) {
	    // If problem getting data then return a Bad Request
	    throw WebUtils.badRequestException(e.getMessage());
	}
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
    private static VehiclesInterface getVehicleInterface(String agencyId) 
	    throws WebApplicationException {
	VehiclesInterface vehiclesInterface = VehiclesInterfaceFactory.get(agencyId);
	if (vehiclesInterface == null)
	    throw WebUtils.badRequestException("Agency ID " + agencyId + " is not valid");
	
	return vehiclesInterface;
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
//    private static Response createListResponse(Collection<VehicleData> collection,
//	    StdParametersBean stdParameters) {
//	// Must be ArrayList so can use GenericEntity to create Response.
//	ArrayList<VehicleData> arrayList = (ArrayList<VehicleData>) collection;
//	
//	// Create a GenericEntity that can handle list of the appropriate
//	// type.
//	GenericEntity<List<VehicleData>> entity = 
//	            new GenericEntity<List<VehicleData>>(arrayList) {};
//	            
//	// Return the response using the generic entity
//	return createResponse(entity, stdParameters);
//    }

}
