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

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.transitime.ipc.data.Vehicle;
import org.transitime.utils.StringUtils;

/**
 * 
 * Note: @XmlType(propOrder=""...) is used to get the elements to be output in
 * desired order instead of the default of alphabetical. This makes the resulting
 * JSON/XML more readable.
 * 
 * @author SkiBu Smith
 * 
 */
@XmlRootElement(name="vehicle")
@XmlType(propOrder = { "id", "gpsTime", "loc", "speed", "heading",
	"pathHeading", "routeId", "routeShortName" })
public class VehicleData {
    
    private String id;
    private long gpsTime;
    private LocationData loc;
    private String speed;
    private String heading;
    private String pathHeading;
    private String routeId;
    private String routeShortName;
    
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
	gpsTime = vehicle.getGpsTime();
	loc = new LocationData(vehicle.getLatitude(), vehicle.getLongitude());
	speed = StringUtils.oneDigitFormat(vehicle.getSpeed());
	heading = StringUtils.oneDigitFormat(vehicle.getHeading());
	pathHeading = StringUtils.oneDigitFormat(vehicle.getPathHeading());
	routeId = vehicle.getRouteId();
	routeShortName = vehicle.getRouteShortName();
    }

    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public long getGpsTime() {
	return gpsTime;
    }
    
    public void setGpsTime(long gpsTime) {
	this.gpsTime = gpsTime;
    }
    
    public LocationData getLoc() {
        return loc;
    }

    public void setLoc(LocationData loc) {
        this.loc = loc;
    }

    public String getSpeed() {
        return speed;
    }
    
    public void setSpeed(String speed) {
        this.speed = speed;
    }
    
    public String getHeading() {
        return heading;
    }
    
    public void setHeading(String heading) {
        this.heading = heading;
    }
    
    public String getPathHeading() {
	return pathHeading;
    }
    
    public void setPathHeading(String pathHeading) {
	this.pathHeading = pathHeading;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getRouteShortName() {
        return routeShortName;
    }

    public void setRouteShortName(String routeShortName) {
        this.routeShortName = routeShortName;
    }
    
}
