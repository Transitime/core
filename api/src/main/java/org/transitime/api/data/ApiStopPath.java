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
import org.transitime.ipc.data.IpcStopPath;
import org.transitime.utils.StringUtils;

/**
 *
 *
 * @author SkiBu Smith
 *
 */
public class ApiStopPath {

    @XmlAttribute
    private final int configRev;

    @XmlAttribute
    private final String stopPathId;

    @XmlAttribute
    private final String stopId;

    @XmlAttribute
    private final int gtfsStopSeq;

    @XmlAttribute
    private final String layoverStop;

    @XmlAttribute
    private final String waitStop;

    @XmlAttribute
    private final String scheduleAdherenceStop;

    @XmlAttribute
    private final String breakTime;

    @XmlElement(name="location")
    private List<ApiLocation> locations;

    @XmlAttribute
    private String pathLength;

    /********************** Member Functions **************************/

    public ApiStopPath(IpcStopPath ipcStopPath) {
	configRev = ipcStopPath.getConfigRev();
	stopPathId = ipcStopPath.getStopPathId();
	stopId = ipcStopPath.getStopId();
	gtfsStopSeq = ipcStopPath.getGtfsStopSeq();
	layoverStop = ipcStopPath.isLayoverStop() ? "true" : null;
	waitStop = ipcStopPath.isWaitStop() ? "true" : null;
	scheduleAdherenceStop = ipcStopPath.isScheduleAdherenceStop() ? 
		"true" : null;
	breakTime = ipcStopPath.getBreakTime() != 0 ? 
		ipcStopPath.getBreakTime().toString() : null;
	
	locations = new ArrayList<ApiLocation>();
	for (Location loc : ipcStopPath.getLocations()) {
	    locations.add(new ApiLocation(loc.getLat(), loc.getLon()));
	}
	
	pathLength = StringUtils.oneDigitFormat(ipcStopPath.getPathLength());
    }
}
