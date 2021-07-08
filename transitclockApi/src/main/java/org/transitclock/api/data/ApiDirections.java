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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.transitclock.ipc.data.IpcDirection;
import org.transitclock.ipc.data.IpcDirectionsForRoute;

/**
 * A list of directions.
 *
 * @author SkiBu Smith
 *
 */
@XmlRootElement(name = "directions")
public class ApiDirections {

	@XmlElement(name = "direction")
	private List<ApiDirection> directionsData;

	/********************** Member Functions **************************/

	/**
	 * Need a no-arg constructor for Jersey. Otherwise get really obtuse
	 * "MessageBodyWriter not found for media type=application/json" exception.
	 */
	protected ApiDirections() {
	}

	public ApiDirections(IpcDirectionsForRoute stopsForRoute) {
		Collection<IpcDirection> directions = stopsForRoute.getDirections();
		directionsData = new ArrayList<>(directions.size());
		for (IpcDirection direction : directions) {
			directionsData.add(new ApiDirection(direction));
		}
	}
}
