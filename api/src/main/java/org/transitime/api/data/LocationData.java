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

import org.transitime.ipc.data.Vehicle;
import org.transitime.utils.Geo;
import org.transitime.utils.StringUtils;

public class LocationData {

    @XmlAttribute
    private String lat;
    
    @XmlAttribute
    private String lon;
    
    @XmlAttribute(name="t")
    private long time;
    
    @XmlAttribute(name="spd")
    private String speed;
    
    @XmlAttribute(name="hd")
    private String heading;
    
    @XmlAttribute(name="pathHd")
    private String pathHeading;

    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really 
     * obtuse "MessageBodyWriter not found for media type=application/json"
     * exception.
     */
    protected LocationData() {}

    /**
     * @param lat
     * @param lon
     */
    public LocationData(Vehicle vehicle) {
	this.lat = Geo.format(vehicle.getLatitude());
	this.lon = Geo.format(vehicle.getLongitude());
	this.time = vehicle.getGpsTime();
	this.speed = StringUtils.oneDigitFormat(vehicle.getSpeed());
	this.heading = StringUtils.oneDigitFormat(vehicle.getHeading());
	this.pathHeading = StringUtils.oneDigitFormat(vehicle.getPathHeading());

    }
    
}
