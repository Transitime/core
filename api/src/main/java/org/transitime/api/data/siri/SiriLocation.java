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

package org.transitime.api.data.siri;

import javax.xml.bind.annotation.XmlElement;

import org.transitime.utils.Geo;

/**
 * Location object for SIRI
 *
 * @author SkiBu Smith
 *
 */
public class SiriLocation {

    @XmlElement(name="Longitude")
    private String longitude;
    
    @XmlElement(name="Latitude")
    private String latitude;
    
    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey for JSON. Otherwise get really
     * obtuse "MessageBodyWriter not found for media type=application/json"
     * exception.
     */
    protected SiriLocation() {}
    
    public SiriLocation(double latitude, double longitude) {
	this.longitude = Geo.format(longitude);
	this.latitude = Geo.format(latitude);
    }
}
