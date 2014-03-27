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
package org.transitime.db.structs;

import org.transitime.utils.Geo;

/**
 * Inherits from Vector but automatically calculates the heading. Useful 
 * for if heading is frequently used.
 * 
 * @author SkiBu Smith
 *
 */
public class VectorWithHeading extends Vector {

	// Heading in degrees clockwise from due North. Note that this is
	// not the same as the "angle" which is degrees clockwise from the
	// equator.
	private final float headingInDegrees;
	
	/********************** Member Functions **************************/

	/**
	 * Construct a vector and determine its heading.
	 * @param l1
	 * @param l2
	 */
	public VectorWithHeading(Location l1, Location l2) {
		super(l1, l2);
		headingInDegrees = (float) heading();
	}

	/**
	 * @return Heading in degrees clockwise from due North. Note that this is
	 *         not the same as the "angle" which is degrees clockwise from the
	 *         equator. The value is calculated in the constructor so that
	 *         this method is efficient if the heading is retrieved multiple
	 *         times for a vector.
	 */
	public float getHeading() {
		return headingInDegrees;
	}
	
	/**
	 * Returns true if heading is within allowableDelta of segment.
	 * 
	 * @param vehicleHeading Heading of vehicle. If Float.NaN then
	 * method will return true
	 * @param allowableDelta
	 * @return Whether heading is within allowableDelta of segment
	 */
	public boolean headingOK(float vehicleHeading, float allowableDelta) {
		return Geo.headingOK(vehicleHeading, headingInDegrees, allowableDelta); 
	}
}
