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

import org.transitime.api.rootResources.TransitimeApi.UiMode;
import org.transitime.ipc.data.IpcVehicle;

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
@XmlType(propOrder = { "id", "routeId", "routeShortName", "headsign",
	"directionId", "vehicleType", "uiType", "loc" })
public class ApiVehicle {
    
    @XmlAttribute
    protected String id;
    
    @XmlElement
    protected ApiGpsLocation loc;
    
    @XmlAttribute
    protected String routeId;
    
    @XmlAttribute(name="routeShrtNm")
    protected String routeShortName;
    
    @XmlAttribute
    protected String headsign;
    
    @XmlAttribute(name="direction")
    protected String directionId;
    
    @XmlAttribute
    protected String vehicleType;
    
    // Whether NORMAL, SECONDARY, or MINOR. Specifies how vehicle should
    // be drawn in the UI
    @XmlAttribute
    protected String uiType;
    
    /**
     * Need a no-arg constructor for Jersey. Otherwise get really 
     * obtuse "MessageBodyWriter not found for media type=application/json"
     * exception.
     */
    protected ApiVehicle() {}
        
    /**
     * Takes a Vehicle object for client/server communication and constructs a
     * ApiVehicle object for the API.
     * 
     * @param vehicle
     * @param uiType
     *            If should be labeled as "minor" in output for UI.
     */
    public ApiVehicle(IpcVehicle vehicle, UiMode uiType) {
	id = vehicle.getId();
	loc = new ApiGpsLocation(vehicle);
	routeId = vehicle.getRouteId();
	routeShortName = vehicle.getRouteShortName();
	headsign = vehicle.getHeadsign();
	directionId = vehicle.getDirectionId();

	// Set GTFS vehicle type. If it was not set in the config then use
	// default value of "3" which is for buses.
	vehicleType = vehicle.getVehicleType();
	if (vehicleType == null)
	    vehicleType = "3";
	
	// Determine UI type. Usually will be displaying vehicles
	// as NORMAL. To simplify API use null for this case. 
	this.uiType = null;
	if (uiType == UiMode.SECONDARY)
	    this.uiType = "secondary";
	else if (uiType == UiMode.MINOR)
	    this.uiType = "minor";
    }

}
