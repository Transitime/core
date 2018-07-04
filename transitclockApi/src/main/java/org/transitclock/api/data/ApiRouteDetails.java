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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.transitclock.db.structs.Location;
import org.transitclock.ipc.data.IpcDirection;
import org.transitclock.ipc.data.IpcDirectionsForRoute;
import org.transitclock.ipc.data.IpcRoute;
import org.transitclock.ipc.data.IpcShape;

/**
 * Provides detailed information for a route include stops and shape info.
 *
 * @author SkiBu Smith
 *
 */
@XmlRootElement(name = "route")
public class ApiRouteDetails {

	@XmlAttribute
	private String id;

	@XmlAttribute
	private String name;

	@XmlAttribute
	private String shortName;

	@XmlAttribute
	private String longName;
	
	@XmlAttribute
	private String color;

	@XmlAttribute
	private String textColor;

	@XmlAttribute
	private String type;

	@XmlElement(name = "direction")
	private List<ApiDirection> directions;

	@XmlElement(name = "shape")
	private List<ApiShape> shapes;

	@XmlElement
	private ApiExtent extent;

	@XmlElement
	private ApiLocation locationOfNextPredictedVehicle;

	/********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse
     * "MessageBodyWriter not found for media type=application/json" exception.
     */
	protected ApiRouteDetails() {
	}

	public ApiRouteDetails(IpcRoute ipcRoute) {
		this.id = ipcRoute.getId();
		this.name = ipcRoute.getName();
		this.shortName = ipcRoute.getShortName();
		this.longName = ipcRoute.getLongName();
		this.color = ipcRoute.getColor();
		this.textColor = ipcRoute.getTextColor();
		this.type = ipcRoute.getType();

		IpcDirectionsForRoute stops = ipcRoute.getStops();
		this.directions = new ArrayList<ApiDirection>();
		for (IpcDirection ipcDirection : stops.getDirections()) {
			this.directions.add(new ApiDirection(ipcDirection));
		}

		this.shapes = new ArrayList<ApiShape>();
		for (IpcShape shape : ipcRoute.getShapes()) {
			this.shapes.add(new ApiShape(shape));
		}

		this.extent = new ApiExtent(ipcRoute.getExtent());

		Location vehicleLoc = ipcRoute.getLocationOfNextPredictedVehicle();
		if (vehicleLoc == null)
			this.locationOfNextPredictedVehicle = null;
		else
			this.locationOfNextPredictedVehicle = new ApiLocation(vehicleLoc);
	}

}
