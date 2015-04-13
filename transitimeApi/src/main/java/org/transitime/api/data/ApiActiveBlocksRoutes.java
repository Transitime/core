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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.transitime.ipc.data.IpcActiveBlock;
import org.transitime.ipc.data.IpcTrip;

/**
 * A list of routes for when outputting active blocks
 *
 * @author SkiBu Smith
 *
 */
@XmlRootElement(name = "routes")
public class ApiActiveBlocksRoutes {

	@XmlElement(name = "route")
	private List<ApiActiveBlocksRoute> routeData;

	/********************** Member Functions **************************/
	/**
	 * Need a no-arg constructor for Jersey. Otherwise get really obtuse
	 * "MessageBodyWriter not found for media type=application/json" exception.
	 */
	protected ApiActiveBlocksRoutes() {
	}

	/**
	 * Constructs an ApiRouteSummaries using a collection of IpcRouteSummary
	 * objects.
	 * 
	 * @param routes
	 */
	public ApiActiveBlocksRoutes(Collection<IpcActiveBlock> activeBlocks,
			String agencyId) {
		HashMap<String, ApiActiveBlocksRoute> routesMap = 
				new HashMap<String, ApiActiveBlocksRoute>();
		
		for (IpcActiveBlock activeBlock : activeBlocks) {
			IpcTrip trip = activeBlock.getBlock().getTrips()
					.get(activeBlock.getActiveTripIndex());
			
			// Get the ApiActiveBlocksRoute object. Create it if haven't yet
			String routeId = trip.getRouteId();
			ApiActiveBlocksRoute apiRoute = routesMap.get(routeId);
			if (apiRoute == null) {
				apiRoute = new ApiActiveBlocksRoute(
						trip.getRouteId(), trip.getRouteShortName(),
						trip.getRouteName());
				routesMap.put(routeId, apiRoute);
			}
			
			// Add the new activeBlock to the ApiActiveBlocksRoute
			apiRoute.add(activeBlock, agencyId);
		}

		// Put sorted results into routeData member
		routeData = new ArrayList<ApiActiveBlocksRoute>(routesMap.values());
		Collections.sort(routeData, comparator);
	}

    /**
     * For sorting the active blocks by route and then block ID
     */
	private static final Comparator<ApiActiveBlocksRoute> comparator = 
			new Comparator<ApiActiveBlocksRoute>() {
		@Override
		public int compare(ApiActiveBlocksRoute o1, ApiActiveBlocksRoute o2) {
			return o1.getName().compareTo(o2.getName());
		}
	};

}
