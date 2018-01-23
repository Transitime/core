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
import org.transitime.ipc.data.IpcVehicleComplete;
import org.transitime.ipc.data.IpcPrediction;
import org.transitime.ipc.data.IpcPredictionsForRouteStopDest;
import org.transitime.ipc.interfaces.PredictionsInterface;
import org.transitime.ipc.interfaces.VehiclesInterface;

/**
 * The Siri API
 *
 * @author SkiBu Smith
 *
 */
@Path("/key/{key}/agency/{agency}")
public class SiriApi {

	/********************** Member Functions **************************/

	/**
	 * Returns vehicleMonitoring vehicle information in SIRI format. Can specify
	 * vehicleIds, routeIds, or routeShortNames to get subset of data. If not
	 * specified then vehicle information for entire agency is returned.
	 * 
	 * @param stdParameters
	 * @param vehicleIds
	 *            List of vehicle IDs
	 * @param routesIdOrShortNames
	 *            List of routes
	 * @return The response
	 * @throws WebApplicationException
	 */
	@Path("/command/siri/vehicleMonitoring")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getVehicles(@BeanParam StandardParameters stdParameters,
			@QueryParam(value = "v") List<String> vehicleIds,
			@QueryParam(value = "r") List<String> routesIdOrShortNames)
			throws WebApplicationException {
		// Make sure request is valid
		stdParameters.validate();

		try {
			// Get Vehicle data from server
			VehiclesInterface inter = stdParameters.getVehiclesInterface();

			Collection<IpcVehicleComplete> vehicles;
			if (!routesIdOrShortNames.isEmpty()) {
				vehicles = inter.getCompleteForRoute(routesIdOrShortNames);
			} else if (!vehicleIds.isEmpty()) {
				vehicles = inter.getComplete(vehicleIds);
			} else {
				vehicles = inter.getComplete();
			}

			// Determine and return SiriStopMonitoring response
			SiriVehiclesMonitoring siriVehicles =
					new SiriVehiclesMonitoring(vehicles,
							stdParameters.getAgencyId());
			return stdParameters.createResponse(siriVehicles);
		} catch (Exception e) {
			// If problem getting data then return a Bad Request
			throw WebUtils.badRequestException(e);
		}
	}

	/**
	 * Returns stopMonitoring vehicle information in SIRI format. Can specify
	 * routeId or routeShortName. Need to also specify stopId. Can optionally
	 * specify how many max number of predictions per stop to return.
	 * 
	 * @param stdParameters
	 * @param routeIdOrShortName
	 * @param stopId
	 * @param numberPredictions
	 * @return
	 * @throws WebApplicationException
	 */
	@Path("/command/siri/stopMonitoring")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public
			Response
			getVehicles(
					@BeanParam StandardParameters stdParameters,
					@QueryParam(value = "r") String routeIdOrShortName,
					@QueryParam(value = "s") String stopId,
					@QueryParam(value = "numPreds") @DefaultValue("3") int numberPredictions)
					throws WebApplicationException {
		// Make sure request is valid
		stdParameters.validate();

		try {
			// Get prediction data from server
			PredictionsInterface inter =
					stdParameters.getPredictionsInterface();

			List<IpcPredictionsForRouteStopDest> preds =
					inter.get(routeIdOrShortName, stopId, numberPredictions);

			// For each prediction also need corresponding vehicle so can create
			// the absurdly large MonitoredVehicleJourney element.
			List<String> vehicleIds = new ArrayList<String>();
			for (IpcPredictionsForRouteStopDest predsForDest : preds) {
				for (IpcPrediction individualPred : predsForDest
						.getPredictionsForRouteStop()) {
					vehicleIds.add(individualPred.getVehicleId());
				}
			}
			VehiclesInterface vehicleInter =
					stdParameters.getVehiclesInterface();
			Collection<IpcVehicleComplete> vehicles =
					vehicleInter.getComplete(vehicleIds);

			// Determine SiriStopMonitoring response
			SiriStopMonitoring siriStopMonitoring =
					new SiriStopMonitoring(preds, vehicles,
							stdParameters.getAgencyId());

			// Return SiriStopMonitoring response
			return stdParameters.createResponse(siriStopMonitoring);
		} catch (Exception e) {
			// If problem getting data then return a Bad Request
			throw WebUtils.badRequestException(e);
		}
	}

}
