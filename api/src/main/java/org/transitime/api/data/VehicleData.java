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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.transitime.ipc.data.Vehicle;

/**
 * Contains the data for a single vehicle. 
 * <p>
 * Note: @XmlType(propOrder=""...) is used to get the elements to be output in
 * desired order instead of the default of alphabetical. This makes the resulting
 * JSON/XML more readable.
 * 
 * @author SkiBu Smith
 * 
 */
@XmlRootElement
@XmlType(propOrder = { "id", "routeId", "routeShortName", "loc" })
public class VehicleData {
    
    @XmlAttribute
    protected String id;
    
    @XmlElement
    protected LocationData loc;
    
    @XmlAttribute
    protected String routeId;
    
    @XmlAttribute(name="routeShrtNm")
    protected String routeShortName;
    
    /**
     * Need a no-arg constructor for Jersey. Otherwise get really 
     * obtuse "MessageBodyWriter not found for media type=application/json"
     * exception.
     */
    protected VehicleData() {}
        
    /**
     * Takes a Vehicle object for client/server communication and constructs a
     * VehicleData object for the API.
     * 
     * @param vehicle
     */
    public VehicleData(Vehicle vehicle) {
	id = vehicle.getId();
	loc = new LocationData(vehicle);
	routeId = vehicle.getRouteId();
	routeShortName = vehicle.getRouteShortName();
    }

}
