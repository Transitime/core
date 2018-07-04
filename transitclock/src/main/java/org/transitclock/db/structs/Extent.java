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
package org.transitclock.db.structs;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.transitclock.utils.Geo;

import net.jcip.annotations.Immutable;


/**
 * A rectangle specified by min and max latitudes and longitudes.
 * 
 * @author SkiBu Smith
 *
 */
@Immutable
@Embeddable
public class Extent implements Serializable {

	@Column
	private double minLat = Double.POSITIVE_INFINITY;

	@Column
	private double maxLat = Double.NEGATIVE_INFINITY;

	@Column
	private double minLon = Double.POSITIVE_INFINITY;

	@Column
	private double maxLon = Double.NEGATIVE_INFINITY;
	
	// This value is actually dependent on latitude a bit since the earth
	// is not a perfect sphere. But it doesn't vary that much. So using
	// a hard coded value for latitude of 38 degrees, which is approximately
	// San Francisco. For Mexico City at latitude 19 degrees the difference
	// is a bit less than 0.3%, so pretty small for when doing quick 
	// calculations.
	private static final double METERS_PER_DEGREE = 110996.45;
	
	private static final long serialVersionUID = 6173873318480438032L;

	/********************** Member Functions **************************/

	/**
	 * Hibernate requires a no-arg constructor so might as well be 
	 * explicit about it.
	 */
	public Extent() {		
	}
	
	/**
	 * Once an Extent has been constructed need to simply add
	 * associated Locations (or Extents). Once all locations have 
	 * been added the Extent will be the rectangle spanning all 
	 * of those Locations and Extents.
	 * @param l 
	 */
	public void add(Location l) {
		if (l.getLat() < minLat)
			minLat = l.getLat();
		if (l.getLat() > maxLat)
			maxLat = l.getLat();
		if (l.getLon() < minLon)
			minLon = l.getLon();
		if (l.getLon() > maxLon)
			maxLon = l.getLon();
	}
	
	/**
	 * Once an Extent has been constructed need to simply add
	 * associated Extents (or Locations). Once all have 
	 * been added the Extent will be the rectangle spanning all 
	 * of those Locations and Extents.
	 * @param l 
	 */
	public void add(Extent e) {
		if (e.minLat < minLat)
			minLat = e.minLat;
		if (e.maxLat > maxLat)
			maxLat = e.maxLat;
		if (e.minLon < minLon)
			minLon = e.minLon;
		if (e.maxLon > maxLon)
			maxLon = e.maxLon;
	}
	
	
	/**
	 * If don't have hashCode() and equals() then the objects that include this
	 * object will generate a warning when these methods are implemented.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(maxLat);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(maxLon);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(minLat);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(minLon);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/**
	 * If don't have hashCode() and equals() then the objects that include this
	 * object will generate a warning when these methods are implemented.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Extent other = (Extent) obj;
		if (Double.doubleToLongBits(maxLat) != Double
				.doubleToLongBits(other.maxLat))
			return false;
		if (Double.doubleToLongBits(maxLon) != Double
				.doubleToLongBits(other.maxLon))
			return false;
		if (Double.doubleToLongBits(minLat) != Double
				.doubleToLongBits(other.minLat))
			return false;
		if (Double.doubleToLongBits(minLon) != Double
				.doubleToLongBits(other.minLon))
			return false;
		return true;
	}

	public String toString() {
		return "[" 
				+ "minLat=" + Geo.format(minLat)
				+ ", maxLat=" + Geo.format(maxLat)
				+ ", minLon=" + Geo.format(minLon)
				+ ", maxLon=" + Geo.format(maxLon)
				+ "]";					
	}

	/**
	 * Returns true if the location is with the specified distance of this
	 * extent. This is not a perfectly accurate calculation due to
	 * METERS_PER_DEGREE being a constant and not taking into account changes in
	 * diameter of the earth depending on latitude. Also, looks at latitude and
	 * longitude separately. So there is a corner case where latitude and
	 * longitude might be OK individually, so the method returns true, but
	 * together the distance would be actually be further away then the
	 * specified distance.
	 * 
	 * @param loc
	 * @param distance
	 * @return
	 */
	public boolean isWithinDistance(Location loc, double distance) {
		// First do quick check on latitude
		double distanceInDegreesLatitude = distance / METERS_PER_DEGREE;
		if (minLat > loc.getLat() + distanceInDegreesLatitude
				|| maxLat < loc.getLat() - distanceInDegreesLatitude)
			return false;
		
		// Latitude was OK so check longitude
		double distanceInDegreesLongitude =
				distance
						/ (METERS_PER_DEGREE * Math.cos(Math
								.toRadians((minLat + maxLat) / 2)));
		if (minLon > loc.getLon() + distanceInDegreesLongitude 
				|| maxLon < loc.getLon() - distanceInDegreesLongitude)
			return false;
		
		// Latitude and longitude are OK so return true;
		return true;
	}
	
	public double getMinLat() {
	    return minLat;
	}

	public double getMaxLat() {
	    return maxLat;
	}

	public double getMinLon() {
	    return minLon;
	}

	public double getMaxLon() {
	    return maxLon;
	}

}
