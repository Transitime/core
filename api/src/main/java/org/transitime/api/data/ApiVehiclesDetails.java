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
import java.util.Set;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.transitime.ipc.data.IpcVehicle;

/**
 * For when have list of VehicleDetails. By using this class can control
 * the element name when data is output.
 *
 * @author SkiBu Smith
 *
 */
@XmlRootElement(name="vehicles")
public class ApiVehiclesDetails {

    // Need to use @XmlElementRef so that the element name used for each
    // ApiVehicle object will be what is specified in the ApiVehicle class.
    @XmlElementRef
    private List<ApiVehicleDetails> vehiclesData;
    
    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really 
     * obtuse "MessageBodyWriter not found for media type=application/json"
     * exception.
     */
    protected ApiVehiclesDetails() {}

    /**
     * For constructing a ApiVehiclesDetails object from a Collection of Vehicle
     * objects.
     * 
     * @param vehicles
     * @param vehiclesGeneratingPreds
     *            So can determine which ones are to be labeled as minor. Set to
     *            null if this feature is not being used.
     */
    public ApiVehiclesDetails(Collection<IpcVehicle> vehicles, 
	    Set<String> vehiclesGeneratingPreds) {
	vehiclesData = new ArrayList<ApiVehicleDetails>();
	for (IpcVehicle vehicle : vehicles) {
	    boolean minorVehicle = vehiclesGeneratingPreds != null
		    && !vehiclesGeneratingPreds.contains(vehicle.getId());
	    vehiclesData.add(new ApiVehicleDetails(vehicle, minorVehicle));
	}
    }

}
