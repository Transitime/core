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

import org.transitime.db.structs.Location;
import org.transitime.ipc.data.IpcShape;

/**
 * A portion of a shape that defines a trip pattern. A List of ApiLocation
 * objects.
 *
 * @author SkiBu Smith
 *
 */
public class ApiShape {

    @XmlAttribute(name="tripPattern")
    private String tripPatternId;
    
    @XmlAttribute
    private String headsign;
    
    @XmlAttribute(name="forRemainingTrip")
    private Boolean isUiShape;

    @XmlElement(name="loc")
    private List<ApiLocation> locations;
    
    /********************** Member Functions **************************/

    protected ApiShape() {}
    
    public ApiShape(IpcShape shape) {
	this.tripPatternId = shape.getTripPatternId();
	this.headsign = shape.getHeadsign();
	
	// If false then set to null so that this attribute won't then be
	// output as XML/JSON, therefore making output a bit more compact.
	this.isUiShape = shape.isUiShape() ? true : null;

	this.locations = new ArrayList<ApiLocation>(); 
	for (Location loc : shape.getLocations()) {
	    this.locations.add(new ApiLocation(loc.getLat(), loc.getLon()));
	}
    }
    
}
