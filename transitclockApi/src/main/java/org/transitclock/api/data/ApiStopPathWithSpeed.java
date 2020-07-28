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

import org.transitclock.db.structs.Location;
import org.transitclock.ipc.data.IpcStopPath;
import org.transitclock.ipc.data.IpcStopPathWithSpeed;
import org.transitclock.utils.Geo;
import org.transitclock.utils.MathUtils;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Represents a path from one stop to another.
 *
 * @author SkiBu Smith
 *
 */
public class ApiStopPathWithSpeed {

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

	@XmlElement(name = "latlngs")
	private List locations;

	@XmlAttribute
	private Double pathLength;

	@XmlAttribute
	private Double speed;

	/********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse
     * "MessageBodyWriter not found for media type=application/json" exception.
     */
	protected ApiStopPathWithSpeed() {
	}

	public ApiStopPathWithSpeed(IpcStopPathWithSpeed ipcStopPathWithSpeed) {
		stopPathId = ipcStopPathWithSpeed.getStopPathId();
		stopId = ipcStopPathWithSpeed.getStopId();
		stopName = ipcStopPathWithSpeed.getStopName();
		gtfsStopSeq = ipcStopPathWithSpeed.getGtfsStopSeq();
		layoverStop = ipcStopPathWithSpeed.isLayoverStop() ? true : null;
		waitStop = ipcStopPathWithSpeed.isWaitStop() ? true : null;
		scheduleAdherenceStop =
				ipcStopPathWithSpeed.isScheduleAdherenceStop() ? true : null;
		breakTime =
				ipcStopPathWithSpeed.getBreakTime() != 0 ? ipcStopPathWithSpeed.getBreakTime()
						: null;

		locations = ipcStopPathWithSpeed.getLocations();

		//locations = new ArrayList<>();

		/*for(int i = 0; i < locationsList.size(); i++){
			Double lat = MathUtils.round(locationsList.get(i).getLat(),1);
			Double lon = MathUtils.round(locationsList.get(i).getLon(),1);
			locations.add(Arrays.asList(lat, lon));
		}*/

		pathLength = MathUtils.round(ipcStopPathWithSpeed.getPathLength(), 1);
		speed = ipcStopPathWithSpeed.getSpeed() != null ? MathUtils.round(ipcStopPathWithSpeed.getSpeed(), 1) : null;
	}
}
