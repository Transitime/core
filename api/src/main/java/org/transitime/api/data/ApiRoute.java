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
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.transitime.db.structs.Location;
import org.transitime.ipc.data.IpcRoute;
import org.transitime.ipc.data.IpcShape;
import org.transitime.ipc.data.IpcStop;

/**
 * Provides detailed information for a route include stops and shape info.
 *
 * @author SkiBu Smith
 *
 */
@XmlRootElement(name="route")
public class ApiRoute {

    @XmlAttribute
    private String id;
    
    @XmlAttribute(name="routeShrtNm")
    private String shortName;
    
    @XmlAttribute
    private String name;

    @XmlAttribute
    private String color;

    @XmlAttribute
    private String textColor;

    @XmlAttribute
    private String type;

    @XmlElement(name="stop")
    private List<ApiStop> stops;
    
    @XmlElement(name="shape")
    private List<ApiShape> shapes;
    
    @XmlElement
    private ApiExtent extent;
    
    @XmlElement
    private ApiLocation locationOfNextPredictedVehicle;
    
    /********************** Member Functions **************************/

    protected ApiRoute() {}
    
    public ApiRoute(IpcRoute route) {
	this.id = route.getId();
	this.shortName = route.getShortName();
	this.name = route.getName();
	this.color = route.getColor();
	this.textColor = route.getTextColor();
	this.type = route.getType();
	
	this.stops = new ArrayList<ApiStop>();
	for (IpcStop stop : route.getStops()) {
	    this.stops.add(new ApiStop(stop));
	}
	
	this.shapes = new ArrayList<ApiShape>();
	for (IpcShape shape : route.getShapes()) {
	    this.shapes.add(new ApiShape(shape));
	}
	
	this.extent = new ApiExtent(route.getExtent());	
	
	Location vehicleLoc = route.getLocationOfNextPredictedVehicle();
	if (vehicleLoc == null)
	    this.locationOfNextPredictedVehicle = null;
	else
	    this.locationOfNextPredictedVehicle = new ApiLocation(vehicleLoc);
    }

}
