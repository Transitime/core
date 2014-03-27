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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import net.jcip.annotations.Immutable;

import org.transitime.utils.Geo;


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
	double minLat = Double.POSITIVE_INFINITY;

	@Column
	double maxLat = Double.NEGATIVE_INFINITY;

	@Column
	double minLon = Double.POSITIVE_INFINITY;

	@Column
	double maxLon = Double.NEGATIVE_INFINITY;
	
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

}
