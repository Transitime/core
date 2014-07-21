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

import org.transitime.api.data.siri.SiriStopMonitoring;
import org.transitime.api.data.siri.SiriVehiclesMonitoring;
import org.transitime.api.utils.StandardParameters;
import org.transitime.api.utils.WebUtils;
import org.transitime.ipc.data.IpcExtVehicle;
import org.transitime.ipc.data.IpcPrediction;
import org.transitime.ipc.data.IpcPredictionsForRouteStopDest;
import org.transitime.ipc.interfaces.PredictionsInterface;
import org.transitime.ipc.interfaces.VehiclesInterface;

/**
 *
 *
 * @author SkiBu Smith
 *
 */
@Path("/key/{key}/agency/{agency}")
public class SiriApi {

    /********************** Member Functions **************************/

    @Path("/command/siri/vehicleMonitoring")
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
	    VehiclesInterface inter = stdParameters.getVehiclesInterface();
	    
	    Collection<IpcExtVehicle> vehicles;
	    if (!routeIds.isEmpty()) {
		vehicles = inter.getExtForRouteUsingRouteId(routeIds);
	    } else if (!routeShortNames.isEmpty()) {
		vehicles = inter.getExtForRoute(routeShortNames);
	    } else if (!vehicleIds.isEmpty()) {
		vehicles = inter.getExt(vehicleIds);
	    } else {
		vehicles = inter.getExt();
	    }

	    // Determine and return SiriStopMonitoring response
	    SiriVehiclesMonitoring siriVehicles = 
		    new SiriVehiclesMonitoring(vehicles, stdParameters.getAgencyId());
	    return stdParameters.createResponse(siriVehicles);
	} catch (RemoteException e) {
	    // If problem getting data then return a Bad Request
	    throw WebUtils.badRequestException(e.getMessage());
	}
    }

    @Path("/command/siri/stopMonitoring")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getVehicles(@BeanParam StandardParameters stdParameters,
	    @QueryParam(value = "r") String routeId,
	    @QueryParam(value = "rShortName") String routeShortName,
	    @QueryParam(value = "s") String stopId,
	    @QueryParam(value="numPreds") @DefaultValue("3") int numberPredictions) 
		    throws WebApplicationException {
	// Make sure request is valid
	stdParameters.validate();
	
	try {
	    // Get prediction data from server
	    PredictionsInterface inter = stdParameters.getPredictionsInterface();
	    
	    List<IpcPredictionsForRouteStopDest> preds;
	    if (routeId != null)
		preds = inter.getUsingRouteId(routeId, stopId, numberPredictions);
	    else
		preds = inter.get(routeShortName, stopId, numberPredictions);

	    // For each prediction also need corresponding vehicle so can create
	    // the absurdly large MonitoredVehicleJourney element.
	    List<String> vehicleIds = new ArrayList<String>();
	    for (IpcPredictionsForRouteStopDest predsForDest : preds) {
		for (IpcPrediction individualPred : predsForDest.getPredictionsForRouteStop()) {
		    vehicleIds.add(individualPred.getVehicleId());
		}
	    }
	    VehiclesInterface vehicleInter = stdParameters.getVehiclesInterface();
	    Collection<IpcExtVehicle> vehicles = vehicleInter.getExt(vehicleIds);
	    
	    // Determine SiriStopMonitoring response
	    SiriStopMonitoring siriStopMonitoring = 
		    new SiriStopMonitoring(preds, vehicles, stdParameters.getAgencyId());
	    
	    // Return SiriStopMonitoring response
	    return stdParameters.createResponse(siriStopMonitoring);
	} catch (RemoteException e) {
	    // If problem getting data then return a Bad Request
	    throw WebUtils.badRequestException(e.getMessage());
	}
    }

}
