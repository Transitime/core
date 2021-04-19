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

package org.transitclock.api.data;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.transitclock.ipc.data.IpcActiveBlock;
import org.transitclock.ipc.data.IpcTrip;

/**
 * A list of routes for when outputting active blocks
 *
 * @author SkiBu Smith
 *
 */
@XmlRootElement(name = "routes")
public class ApiActiveBlocksRoutes {

	@XmlElement(name = "routes")
	private List<ApiActiveBlocksRoute> routeData;

	/********************** Member Functions **************************/
	
	/**
	 * Need a no-arg constructor for Jersey. Otherwise get really obtuse
	 * "MessageBodyWriter not found for media type=application/json" exception.
	 */
	protected ApiActiveBlocksRoutes() {
	}

	/**
	 * Constructs an ApiRouteSummaries using a collection of IpcActiveBlock
	 * objects.
	 * 
	 * @param activeBlocks Already ordered list of active blocks
	 * @param agencyId
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 */
	public ApiActiveBlocksRoutes(Collection<IpcActiveBlock> activeBlocks,
			String agencyId, SpeedFormat speedFormat) throws IllegalAccessException, InvocationTargetException {
		routeData = new ArrayList<ApiActiveBlocksRoute>();
		
		ApiActiveBlocksRoute apiRoute = null;
		for (IpcActiveBlock activeBlock : activeBlocks) {			
			IpcTrip trip = activeBlock.getBlock().getTrips()
					.get(activeBlock.getActiveTripIndex());
			
			// If first block for the current route then create a new 
			// ApiActiveBlocksRoute object to hold the info
			if (apiRoute == null || !apiRoute.getName().equals(trip.getRouteName())) {
				apiRoute = new ApiActiveBlocksRoute(
						trip.getRouteId(), trip.getRouteShortName(),
						trip.getRouteName());
				
				routeData.add(apiRoute);
			}
			
			// Add the block info to the ApiActiveBlocksRoute object
			apiRoute.add(activeBlock, agencyId, speedFormat);
		}		
	}

}
