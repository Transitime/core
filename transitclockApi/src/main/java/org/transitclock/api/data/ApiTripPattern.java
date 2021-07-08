/*
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License (GPL) as published by the
 * Free Software Foundation, either version 3 of the License, or any later
 * version.
 * 
 * Transitime.org is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Transitime.org . If not, see <http://www.gnu.org/licenses/>.
 */

package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.transitclock.ipc.data.IpcStopPath;
import org.transitclock.ipc.data.IpcTripPattern;

/**
 * A single trip pattern
 *
 * @author SkiBu Smith
 *
 */
public class ApiTripPattern {

	@XmlAttribute
	private int configRev;

	@XmlAttribute
	private String id;

	@XmlAttribute
	private String headsign;

	@XmlAttribute
	private String directionId;

	@XmlAttribute
	private String routeId;

	@XmlAttribute
	private String routeShortName;

	@XmlElement
	private ApiExtent extent;

	@XmlAttribute
	private String shapeId;

	@XmlAttribute
	private String firstStopName;

	@XmlAttribute
	private String lastStopName;

	@XmlElement
	private List<ApiStopPath> stopPaths;

	/********************** Member Functions **************************/

	/**
	 * Need a no-arg constructor for Jersey. Otherwise get really obtuse
	 * "MessageBodyWriter not found for media type=application/json" exception.
	 */
	protected ApiTripPattern() {	
	}
	
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
		firstStopName = ipcTripPattern.getFirstStopName();
		lastStopName = ipcTripPattern.getLastStopName();

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
