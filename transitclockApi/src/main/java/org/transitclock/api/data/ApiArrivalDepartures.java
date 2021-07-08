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

import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.ipc.data.IpcRouteSummary;

/**
 * An ordered list of routes.
 *
 * @author SkiBu Smith
 *
 */
@XmlRootElement(name = "arrivalDepartures")
public class ApiArrivalDepartures {

	@XmlElement(name = "arrivalDeparture")
	private List<ApiArrivalDeparture> arrivalDeparturesData;

	/********************** Member Functions **************************/

	/**
	 * Need a no-arg constructor for Jersey. Otherwise get really obtuse
	 * "MessageBodyWriter not found for media type=application/json" exception.
	 */
	protected ApiArrivalDepartures() {
	}

	/**
	 * Constructs an ApiRouteSummaries using a collection of IpcRouteSummary
	 * objects.
	 * 
	 * @param arrivalDepartures
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 */
	public ApiArrivalDepartures(Collection<IpcArrivalDeparture> arrivalDepartures) throws IllegalAccessException, InvocationTargetException {
		arrivalDeparturesData = new ArrayList<ApiArrivalDeparture>();
		for (IpcArrivalDeparture arrivalDeparture : arrivalDepartures) {
			ApiArrivalDeparture apiArrivalDeparture = new ApiArrivalDeparture(arrivalDeparture);
			arrivalDeparturesData.add(apiArrivalDeparture);
		}
	}
}
