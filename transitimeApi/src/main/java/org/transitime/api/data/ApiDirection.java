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

import org.transitime.ipc.data.IpcDirection;
import org.transitime.ipc.data.IpcStop;

/**
 * A single direction, containing stops
 *
 * @author SkiBu Smith
 *
 */
public class ApiDirection {
    
    @XmlAttribute
    private String id;

    @XmlAttribute
    private String title;
    
    @XmlElement(name="stop")
    private List<ApiStop> stops;
    
    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse
     * "MessageBodyWriter not found for media type=application/json" exception.
     */
    protected ApiDirection() {}
    
    /**
     * Constructs a ApiDirection using an IpcDirection
     * 
     * @param direction
     */
    public ApiDirection(IpcDirection direction) {
	this.id = direction.getDirectionId();
	this.title = direction.getDirectionTitle();
	
	this.stops = new ArrayList<ApiStop>(direction.getStops().size());
	for (IpcStop stop : direction.getStops()) {
	    this.stops.add(new ApiStop(stop));
	}
    }
}
