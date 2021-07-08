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
package org.transitclock.utils;

import net.jcip.annotations.Immutable;

/**
 * This class is for having a key for a map that is made up of two or three
 * objects. Often for such keys a shortcut is taken where one simply appends
 * strings together along with a separator character so one gets a key such as
 * "string1|string2". But that is really clunky. Best to do it right and use
 * this class as the key for a map.
 * <p>
 * It is probably best to subclass this class for each type of map key so that
 * you can make sure that the proper type of key is used for the map. Otherwise
 * it could be confusing if one has multiple different MapKeys. An example of
 * such a subclass is: 
 * <p>
 * <code>
 * 	public static class TripStopKey extends MapKey {
 *		private TripStopKey(String tripId, String stopId) {
 *			super(tripId, stopId);
 *		}
 * 
 *      @Override 
 *      public String toString() { 
 *      	return "TripStopKey [" + "tripId=" + o1
 *           + ", stopId=" + o2 + "]"; 
 *      } 
 *  } 
 * </code>
 * 
 * @author SkiBu Smith
 * 
 */
@Immutable
public class MapKey {

	// The key is made up of these four strings. Null values are OK. If fewer
	// than four objects are needed then the remaining ones will be null.
	protected final Object o1;
	protected final Object o2;
	protected final Object o3;
	protected final Object o4;

	// Since hashCode() can be called a lot might as well cache the value
	// since this object is immutable.
	private int cachedHashCode;

	/********************** Member Functions **************************/

	/**
	 * For when need a key consisting of two objects.
	 * 
	 * @param o1
	 *            Object to be part of the key
	 * @param o2
	 *            Object to be part of the key
	 */
	public MapKey(Object o1, Object o2) {
		this.o1 = o1;
		this.o2 = o2;
		this.o3 = null;
		this.o4 = null;
		this.cachedHashCode = createHashCode();
	}

	/**
	 * For when need a key consisting of three objects.
	 * 
	 * @param o1
	 *            Object to be part of the key
	 * @param o2
	 *            Object to be part of the key
	 * @param o3
	 *            Object to be part of the key
	 */
	public MapKey(Object o1, Object o2, Object o3) {
		this.o1 = o1;
		this.o2 = o2;
		this.o3 = o3;
		this.o4 = null;
		this.cachedHashCode = createHashCode();
	}

	/**
	 * For when need a key consisting of four objects.
	 * 
	 * @param o1
	 *            Object to be part of the key
	 * @param o2
	 *            Object to be part of the key
	 * @param o3
	 *            Object to be part of the key
	 * @param o4
	 *            Object to be part of the key
	 */
	public MapKey(Object o1, Object o2, Object o3, Object o4) {
		this.o1 = o1;
		this.o2 = o2;
		this.o3 = o3;
		this.o4 = o4;
		this.cachedHashCode = createHashCode();
	}

	/**
	 * A different way of creating a MapKey by using a static method instead of
	 * using new MapKey(). For when key consists of two strings.
	 * 
	 * @param o1
	 *            Object to be part of the key
	 * @param o2
	 *            Object to be part of the key
	 * @return the MapKey for the specified objects
	 */
	public static MapKey create(Object o1, Object o2) {
		return new MapKey(o1, o2);
	}

	/**
	 * A different way of creating a MapKey by using a static method instead of
	 * using new MapKey(). For when key consists of three strings.
	 * 
	 * @param o1
	 *            Object to be part of the key
	 * @param o2
	 *            Object to be part of the key
	 * @param o3
	 *            Object to be part of the key
	 * @return the MapKey for the specified objects
	 */
	public static MapKey create(Object o1, Object o2, Object o3) {
		return new MapKey(o1, o2, o3);
	}

	/**
	 * A different way of creating a MapKey by using a static method instead of
	 * using new MapKey(). For when key consists of four objects.
	 * 
	 * @param o1
	 *            Object to be part of the key
	 * @param o2
	 *            Object to be part of the key
	 * @param o3
	 *            Object to be part of the key
	 * @param o4
	 *            Object to be part of the key
	 * @return the MapKey for the specified strings
	 */
	public static MapKey create(Object o1, Object o2, Object o3, Object o4) {
		return new MapKey(o1, o2, o3, o4);
	}

	/**
	 * Using cached hash code for speed. Therefore this method is used by
	 * constructors to initialize the cached hash code.
	 * 
	 * @return hash code to be cached
	 */
	private int createHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((o1 == null) ? 0 : o1.hashCode());
		result = prime * result + ((o2 == null) ? 0 : o2.hashCode());
		result = prime * result + ((o3 == null) ? 0 : o3.hashCode());
		result = prime * result + ((o4 == null) ? 0 : o4.hashCode());
		return result;
	}

	/**
	 * @return the cached hash code
	 */
	@Override
	public int hashCode() {
		return cachedHashCode;
	}

	/**
	 * Standard equals() override.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MapKey other = (MapKey) obj;
		if (o1 == null) {
			if (other.o1 != null)
				return false;
		} else if (!o1.equals(other.o1))
			return false;
		if (o2 == null) {
			if (other.o2 != null)
				return false;
		} else if (!o2.equals(other.o2))
			return false;
		if (o3 == null) {
			if (other.o3 != null)
				return false;
		} else if (!o3.equals(other.o3))
			return false;
		if (o4 == null) {
			if (other.o4 != null)
				return false;
		} else if (!o4.equals(other.o4))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MapKey [" 
				+ "o1=" + o1 + ", o2=" + o2 + ", o3=" + o3 + ", o4=" + o4
				+ "]";
	}

}
