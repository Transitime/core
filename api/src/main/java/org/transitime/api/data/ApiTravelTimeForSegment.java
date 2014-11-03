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

import javax.xml.bind.annotation.XmlAttribute;

import org.transitime.utils.Geo;
import org.transitime.utils.StringUtils;
import org.transitime.utils.Time;

/**
 * For representing travel time for a single segment.
 *
 * @author SkiBu Smith
 *
 */
public class ApiTravelTimeForSegment {

    @XmlAttribute
    private int segmentIndex;
    
    @XmlAttribute
    private int segmentTimeMsec;
    
    // Use String so can output speed with desired digits past decimal point
    @XmlAttribute 
    private String speedInMph;
    
    // Use String so can output speed with desired digits past decimal point
    @XmlAttribute
    private String speedInKph;
    
    // Use String so can output speed with desired digits past decimal point
    @XmlAttribute
    private String speedInMetersPerSec;
    
    /********************** Member Functions **************************/

    /**
     * Constructor
     * 
     * @param segmentIndex
     * @param segmentTimeMsec
     * @param segmentLength
     */
    public ApiTravelTimeForSegment(int segmentIndex, int segmentTimeMsec,
	    double segmentLength) {
	this.segmentIndex = segmentIndex;
	this.segmentTimeMsec = segmentTimeMsec;
	
	double speedInMetersPerSec = segmentLength * Time.MS_PER_SEC / 
		segmentTimeMsec;
	this.speedInMph = StringUtils.oneDigitFormat(speedInMetersPerSec / Geo.MPH_TO_MPS);
	this.speedInKph = StringUtils.oneDigitFormat(speedInMetersPerSec / Geo.KPH_TO_MPS);
	this.speedInMetersPerSec = StringUtils.oneDigitFormat(speedInMetersPerSec);
    }
}
