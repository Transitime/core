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

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;

import org.transitime.db.hibernate.HibernateUtils;


/**
 * This class is the base part of a TripPattern. It can be
 * used as the key such for identifying TripPatterns in a 
 * Map. Any additional members not needed for hashCode()
 * or equals() should go in the TripPattern class.
 *
 * @author SkiBu Smith
 */
@MappedSuperclass
public class TripPatternBase {

	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	final protected String shapeId;
	
	// For the List of Paths want to use FetchType.EAGER
	// because otherwise need to keep the session open till the Paths
	// are accessed with the default LAZY loading. And use CascadeType.ALL
	// so that when the TripPattern is stored the Paths are
	// automatically stored.
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	@JoinTable(name="TripPattern_to_Path_joinTable")
	@OrderColumn( name="listIndex")
	final protected List<StopPath> stopPaths;
	
	/********************** Member Functions **************************/
	
	/**
	 * Construct TripPatternBase. Can then be used as key to map to
	 * see of the TripPattern already exists.
	 * 
	 * @param shapeId from the trip.txt GTFS file
	 * @param stopPaths Specifies the stops for the trip pattern. Must
	 * not be null.
	 */
	public TripPatternBase(String shapeId, List<StopPath> paths) {
		if (paths == null)
			throw new RuntimeException("stopPaths param must be " + 
					"non-null for TripPatternBase() constructor");
		
		this.shapeId = shapeId;
		this.stopPaths = paths;
	}

	/**
	 * Construct TripPatternBase by using an existing one. This
	 * is used by TripPattern when need to construct a TripPattern
	 * but can use existing TripPatternBase so don't need to create
	 * stop list and such.
	 * 
	 * @param alreadyExistingBase
	 */
	protected TripPatternBase(TripPatternBase alreadyExistingBase) {
		shapeId = alreadyExistingBase.shapeId;
		stopPaths = alreadyExistingBase.stopPaths;
	}
	
	/**
	 * Hibernate requires a no-arg constructor
	 */
	protected TripPatternBase() {
		shapeId = null;
		stopPaths = null;
	}
	
	/**
	 * Needed so can have Set/Map of TripPatterns and looking
	 * up elements will be quick and proper. Note that this method does
	 * not do a full hashcode on stopPaths. This is because the StopPath elements
	 * are not finalized when building up trip patterns. The tripPatternId
	 * still hasn't been set. So don't want to compare the entire StopPath
	 * objects. Instead, only use StopPath.basicHashCode(). This difference
	 * is very important so using an automatically generated method 
	 * here would not suffice.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((shapeId == null) ? 0 : shapeId.hashCode());
		int pathHashCode = 1;
		for (StopPath path : stopPaths) {
			pathHashCode = 31 * pathHashCode + path.basicHashCode();
		}
		result += pathHashCode;
		return result;
	}

	/**
	 * Needed so can have Set/Map of TripPatterns and looking
	 * up elements will be quick and proper. Note that this method does
	 * not do a full equals() on stopPaths. This is because the StopPath elements
	 * are not finalized when building up trip patterns. The tripPatternId
	 * still hasn't been set. So don't want to compare the entire StopPath
	 * objects. Instead, only use StopPath.basicEquals(). This difference
	 * is very important so using an automatically generated method 
	 * here would not suffice.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TripPatternBase))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TripPatternBase other = (TripPatternBase) obj;
		if (shapeId == null) {
			if (other.shapeId != null)
				return false;
		} else if (!shapeId.equals(other.shapeId))
			return false;
		if (stopPaths == null) {
			if (other.stopPaths != null)
				return false;
		} else {
			if (stopPaths.size() != other.stopPaths.size())
				return false;
			for (int i=0; i<stopPaths.size(); ++i) {
				StopPath path = stopPaths.get(i);
				StopPath otherPath = other.stopPaths.get(i);
				if (!path.basicEquals(otherPath))
					return false;
			}
		}
		return true;
	}

}
