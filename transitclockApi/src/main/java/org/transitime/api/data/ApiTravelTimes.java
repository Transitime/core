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

import org.transitclock.db.structs.TravelTimesForStopPath;
import org.transitclock.db.structs.TravelTimesForTrip;

/**
 *
 *
 * @author SkiBu Smith
 *
 */
public class ApiTravelTimes {

	@XmlAttribute
	private int configRev;

	@XmlAttribute
	private int travelTimeRev;

	@XmlAttribute
	private String tripPatternId;

	@XmlAttribute
	private String tripCreatedForId;

	@XmlElement(name = "travelTimesForStopPath")
	private List<ApiTravelTimesForStopPath> travelTimesForStopPaths;

	/********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse
     * "MessageBodyWriter not found for media type=application/json" exception.
     */
	protected ApiTravelTimes() {}
	
	/**
	 * Constructor
	 * 
	 * @param travelTimes
	 */
	public ApiTravelTimes(TravelTimesForTrip travelTimes) {
		this.configRev = travelTimes.getConfigRev();
		this.travelTimeRev = travelTimes.getTravelTimeRev();
		this.tripPatternId = travelTimes.getTripPatternId();
		this.tripCreatedForId = travelTimes.getTripCreatedForId();

		this.travelTimesForStopPaths =
				new ArrayList<ApiTravelTimesForStopPath>();

		for (int stopPathIndex = 0; stopPathIndex < travelTimes
				.getTravelTimesForStopPaths().size(); ++stopPathIndex) {
			TravelTimesForStopPath travelTimesForStopPath =
					travelTimes.getTravelTimesForStopPath(stopPathIndex);
			this.travelTimesForStopPaths.add(new ApiTravelTimesForStopPath(
					stopPathIndex, travelTimesForStopPath));
		}
	}
}
