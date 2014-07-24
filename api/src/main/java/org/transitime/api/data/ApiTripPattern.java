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

import org.transitime.ipc.data.IpcStopPath;
import org.transitime.ipc.data.IpcTripPattern;

/**
 * A single trip pattern
 *
 * @author SkiBu Smith
 *
 */
public class ApiTripPattern {

    @XmlAttribute
    private final int configRev;
    
    @XmlAttribute
    private final String id;
    
    @XmlAttribute
    private final String headsign;
    
    @XmlAttribute
    private final String directionId;
    
    @XmlAttribute
    private final String routeId;
    
    @XmlAttribute
    private final String routeShortName;
    
    @XmlElement
    private final ApiExtent extent;
    
    @XmlAttribute
    private final String shapeId;
    
    @XmlElement(name="stopPath")
    private final List<ApiStopPath> stopPaths;
    
    /********************** Member Functions **************************/

    /**
     * 
     * @param ipcTripPattern
     * @param includeStopPaths
     *            Stop paths are only included in output if this param set to
     *            true.
     */
    public ApiTripPattern(IpcTripPattern ipcTripPattern, 
	    boolean includeStopPaths) {
	configRev = ipcTripPattern.getConfigRev();
	id = ipcTripPattern.getId();
	headsign = ipcTripPattern.getHeadsign();
	directionId = ipcTripPattern.getDirectionId();
	routeId = ipcTripPattern.getRouteId();
	routeShortName = ipcTripPattern.getRouteShortName();
	extent = new ApiExtent(ipcTripPattern.getExtent());
	shapeId = ipcTripPattern.getShapeId();

	// Only include stop paths if actually want them. This
	// can greatly reduce volume of the output.
	if (includeStopPaths) {
	    stopPaths = new ArrayList<ApiStopPath>();
	    for (IpcStopPath ipcStopPath : ipcTripPattern.getStopPaths()) {
		stopPaths.add(new ApiStopPath(ipcStopPath));
	    }
	} else {
	    stopPaths = null;
	}
    }
}
