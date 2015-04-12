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

import org.transitime.db.structs.TravelTimesForStopPath;
import org.transitime.db.structs.TravelTimesForStopPath.HowSet;
import org.transitime.utils.StringUtils;

/**
 *
 *
 * @author SkiBu Smith
 *
 */
public class ApiTravelTimesForStopPath {

    @XmlAttribute
    private int stopPathIndex;
    
    @XmlAttribute
    private String stopPathId;

    @XmlAttribute
    private String travelTimeSegmentLength;
    
    @XmlAttribute
    private int stopTimeMsec;
    
    @XmlAttribute
    private int totalTravelTime;
    
    @XmlAttribute
    private HowSet howSet;
    
    @XmlElement(name="travelTimesForSegment")
    private List<ApiTravelTimeForSegment> travelTimesForSegments;
    
    /********************** Member Functions **************************/

    /**
     * Constructor
     * 
     * @param travelTimesForStopPath
     */
    public ApiTravelTimesForStopPath(int stopPathIndex,
	    TravelTimesForStopPath travelTimesForStopPath) {
	this.stopPathIndex = stopPathIndex;
	this.stopPathId = travelTimesForStopPath.getStopPathId();
	this.travelTimeSegmentLength = StringUtils
		.twoDigitFormat(travelTimesForStopPath
			.getTravelTimeSegmentLength());
	this.stopTimeMsec = travelTimesForStopPath.getStopTimeMsec();
	this.totalTravelTime = travelTimesForStopPath
		.getStopPathTravelTimeMsec();
	this.howSet = travelTimesForStopPath.getHowSet();
	
	this.travelTimesForSegments = new ArrayList<ApiTravelTimeForSegment>();
	for (int segmentIndex = 0; 
		segmentIndex < travelTimesForStopPath.getNumberTravelTimeSegments(); 
		++segmentIndex) {
	    int travelTimeForSegment = travelTimesForStopPath
		    .getTravelTimeSegmentMsec(segmentIndex);
	    ApiTravelTimeForSegment travelTime = new ApiTravelTimeForSegment(
		    segmentIndex, travelTimeForSegment,
		    travelTimesForStopPath.getTravelTimeSegmentLength());
	    this.travelTimesForSegments.add(travelTime);
	}
    }
    
}
