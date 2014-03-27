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
package org.transitime.core;

import org.transitime.utils.Geo;

/**
 * Describes where an AVL report matches to an assignment temporally. It
 * is a SpatialMatch but also include the temporal difference of how
 * far off the expected time the match is.
 * 
 * @author SkiBu Smith
 *
 */
public class TemporalMatch extends SpatialMatch {

	private final TemporalDifference temporalDifference;
	
	/********************** Member Functions **************************/

	public TemporalMatch(SpatialMatch spatialMatch, 
			TemporalDifference temporalDifference) {
		super(spatialMatch);
		this.temporalDifference = temporalDifference;
	}

	@Override
	public String toString() {
		return "TemporalMatch [" 
				+ "temporalDifference=" + temporalDifference
				+ ", vehicleId=" + vehicleId
				+ ", blockId=" + block.getId()
				+ ", tripIndex=" + tripIndex
				+ ", stopPathIndex=" + stopPathIndex
				+ ", segmentIndex=" + segmentIndex
				+ ", atLayover=" + atLayover()
				+ ", distanceToSegment=" + Geo.distanceFormat(distanceToSegment) 
				+ ", distanceAlongSegment=" + Geo.distanceFormat(distanceAlongSegment)
				+ ", trip=" + getTrip().toShortString()
				+ "]";
	}
	
	/************************ Getter Methods ***************************/
	
	public TemporalDifference getTemporalDifference() {
		return temporalDifference;
	}

}
