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

package org.transitime.api.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.transitime.db.structs.Agency;
import org.transitime.ipc.data.IpcRoute;
import org.transitime.ipc.data.IpcRouteSummary;

/**
 * An ordered list of routes.
 *
 * @author SkiBu Smith
 *
 */
@XmlRootElement
public class ApiRoutes {
	// So can easily get agency name when getting routes. Useful for db reports 
	// and such.
	@XmlElement(name = "agency")
	private String agencyName;
	
	// List of route info
	@XmlElement(name = "routes")
	private List<ApiRoute> routesData;

	/********************** Member Functions **************************/

	/**
	 * Need a no-arg constructor for Jersey. Otherwise get really obtuse
	 * "MessageBodyWriter not found for media type=application/json" exception.
	 */
	protected ApiRoutes() {
	}

	/**
	 * Constructs an ApiRouteSummaries using a collection of IpcRouteSummary
	 * objects.
	 * 
	 * @param routes
	 * @param agency so can get agency name
	 */
	public ApiRoutes(Collection<IpcRouteSummary> routes, Agency agency) {
		routesData = new ArrayList<ApiRoute>();
		for (IpcRouteSummary route : routes) {
			ApiRoute routeSummary = new ApiRoute(route);
			routesData.add(routeSummary);
		}
		
		// Also set agency name
		agencyName = agency.getName();
	}
	
	/**
	 * Constructs an ApiRouteSummaries using a collection of IpcRoute objects.
	 * 
	 * @param routes
	 * @param agency
	 *            so can get agency name
	 */
	public ApiRoutes(List<IpcRoute> routes, Agency agency) {
		routesData = new ArrayList<ApiRoute>();
		for (IpcRouteSummary route : routes) {
			ApiRoute routeSummary = new ApiRoute(route);
			routesData.add(routeSummary);
		}
		
		// Also set agency name
		agencyName = agency.getName();
	}
}
