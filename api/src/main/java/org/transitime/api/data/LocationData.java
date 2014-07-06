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
import org.transitime.utils.Geo;

public class LocationData {

    private String lat;
    private String lon;

    protected LocationData() {}

    /**
     * @param lat
     * @param lon
     */
    public LocationData(double lat, double lon) {
	this.lat = Geo.format(lat);
	this.lon = Geo.format(lon);
    }

    /**
     * Made an attribute so that the XML is a bit cleaner.
     * @return
     */
    @XmlAttribute
    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    /**
     * Made an attribute so that the XML is a bit cleaner.
     * @return
     */
    @XmlAttribute
    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    };
    
    
}
