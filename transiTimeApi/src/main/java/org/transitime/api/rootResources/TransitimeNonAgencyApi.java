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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.transitime.api.data.ApiAgencies;
import org.transitime.api.data.ApiAgency;
import org.transitime.api.utils.StandardParameters;
import org.transitime.api.utils.WebUtils;
import org.transitime.db.structs.Agency;
import org.transitime.db.webstructs.WebAgency;
import org.transitime.ipc.clients.ConfigInterfaceFactory;
import org.transitime.ipc.interfaces.ConfigInterface;

/**
 * Contains the API commands for the Transitime API for system wide commands,
 * such as determining all agencies. The intent of this feed is to provide what
 * is needed for creating a user interface application, such as a smartphone
 * application.
 * <p>
 * The data output can be in either JSON or XML. The output format is specified
 * by the accept header or by using the query string parameter "format=json" or
 * "format=xml".
 *
 * @author SkiBu Smith
 *
 */
@Path("/key/{key}")
public class TransitimeNonAgencyApi {

	/********************** Member Functions **************************/

	@Path("/command/agencies")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getAgencies(@BeanParam StandardParameters stdParameters)
			throws WebApplicationException {
		// Make sure request is valid
		stdParameters.validate();

		// For each agency handled by this server create an ApiAgencies
		// and return the list.
		try {
			List<ApiAgency> apiAgencyList = new ArrayList<ApiAgency>();
			Collection<WebAgency> webAgencies = WebAgency.getWebAgencies();
			for (WebAgency webAgency : webAgencies) {
				String agencyId = webAgency.getAgencyId();
				ConfigInterface inter = ConfigInterfaceFactory.get(agencyId);

				// If can't communicate with IPC with that agency then move on
				// to the next one. This is important because some agencies
				// might be declared in the web db but they might not actually
				// be running.
				if (inter == null) {
					// Should really log something here to explain that skipping
					// agency

					continue;
				}

				List<Agency> agencies = inter.getAgencies();
				for (Agency agency : agencies) {
					apiAgencyList.add(new ApiAgency(agencyId, agency));
				}
			}
			ApiAgencies apiAgencies = new ApiAgencies(apiAgencyList);
			return stdParameters.createResponse(apiAgencies);
		} catch (RemoteException e) {
			// If problem getting data then return a Bad Request
			throw WebUtils.badRequestException(e.getMessage());
		}
	}

}
