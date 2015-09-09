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

import javax.xml.bind.annotation.XmlAttribute;

import org.transitime.utils.Geo;
import org.transitime.utils.MathUtils;
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

	@XmlAttribute
	private double speedInMph;

	@XmlAttribute
	private double speedInKph;

	@XmlAttribute
	private double speedInMetersPerSec;

	/********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse
     * "MessageBodyWriter not found for media type=application/json" exception.
     */
	protected ApiTravelTimeForSegment() {}
	
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

		double speedInMetersPerSec =
				segmentLength * Time.MS_PER_SEC / segmentTimeMsec;
		this.speedInMph = MathUtils.round(speedInMetersPerSec / Geo.MPH_TO_MPS, 1);
		this.speedInKph = MathUtils.round(speedInMetersPerSec / Geo.KPH_TO_MPS, 1);
		this.speedInMetersPerSec = MathUtils.round(speedInMetersPerSec, 1);
	}
}
