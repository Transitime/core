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

package org.transitime.api.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.transitime.db.structs.Location;
import org.transitime.ipc.data.IpcStopPath;
import org.transitime.utils.MathUtils;

/**
 *
 *
 * @author SkiBu Smith
 *
 */
public class ApiStopPath {

	@XmlAttribute
	private int configRev;

	@XmlAttribute
	private String stopPathId;

	@XmlAttribute
	private String stopId;

	@XmlAttribute
	private String stopName;

	@XmlAttribute
	private int gtfsStopSeq;

	@XmlAttribute
	private Boolean layoverStop;

	@XmlAttribute
	private Boolean waitStop;

	@XmlAttribute
	private Boolean scheduleAdherenceStop;

	@XmlAttribute
	private Integer breakTime;

	@XmlElement
	private List<ApiLocation> locations;

	@XmlAttribute
	private Double pathLength;

	/********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse
     * "MessageBodyWriter not found for media type=application/json" exception.
     */
	protected ApiStopPath() {
	}

	public ApiStopPath(IpcStopPath ipcStopPath) {
		configRev = ipcStopPath.getConfigRev();
		stopPathId = ipcStopPath.getStopPathId();
		stopId = ipcStopPath.getStopId();
		stopName = ipcStopPath.getStopName();
		gtfsStopSeq = ipcStopPath.getGtfsStopSeq();
		layoverStop = ipcStopPath.isLayoverStop() ? true : null;
		waitStop = ipcStopPath.isWaitStop() ? true : null;
		scheduleAdherenceStop =
				ipcStopPath.isScheduleAdherenceStop() ? true : null;
		breakTime =
				ipcStopPath.getBreakTime() != 0 ? ipcStopPath.getBreakTime()
						: null;

		locations = new ArrayList<ApiLocation>();
		for (Location loc : ipcStopPath.getLocations()) {
			locations.add(new ApiLocation(loc.getLat(), loc.getLon()));
		}

		pathLength = MathUtils.round(ipcStopPath.getPathLength(), 1);
	}
}
