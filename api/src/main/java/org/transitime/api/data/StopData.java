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

import org.transitime.ipc.data.IpcStop;
import org.transitime.utils.ChinaGpsOffset;
import org.transitime.utils.Geo;

/**
 *
 *
 * @author SkiBu Smith
 *
 */
public class StopData {

    @XmlAttribute
    private String id;
       
    @XmlAttribute
    private String name;

    @XmlAttribute
    private String lat;
    
    @XmlAttribute
    private String lon;
    
    @XmlAttribute
    private Integer code;
    
     /********************** Member Functions **************************/

    protected StopData() {}
    
    public StopData(IpcStop stop) {
	this.id = stop.getId();
	this.name = stop.getName();

	ChinaGpsOffset.LatLon latLon = ChinaGpsOffset.transform(
		stop.getLoc().getLat(), stop.getLoc().getLon());	
	this.lat = Geo.format(latLon.getLat());
	this.lon = Geo.format(latLon.getLon());

	this.code = stop.getCode();
    }

}
