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
package org.transitime.utils;

import net.jcip.annotations.Immutable;

/**
 * This class is for having a key for a map that is made up of two or three
 * Strings. Often for such keys a shortcut is taken where one simply appends the
 * strings together along with a separator character so one gets a key such as
 * "string1|string2". But that is really clunky. Best to do it right and use
 * this class as the key for a map.
 * 
 * @author SkiBu Smith
 * 
 */
@Immutable
public class MapKey {

	// The key is made up of these three strings. Null values are OK. If only
	// two strings are needed then s3 will be null.
	private final String s1;
	private final String s2;
	private final String s3;

	// Since hashCode() can be called a lot might as well cache the value
	// since this object is immutable.
	private int cachedHashCode;

	/********************** Member Functions **************************/

	/**
	 * For when need a key consisting of two strings.
	 * 
	 * @param s1
	 *            String to be part of the key
	 * @param s2
	 *            String to be part of the key
	 */
	public MapKey(String s1, String s2) {
		this.s1 = s1;
		this.s2 = s2;
		this.s3 = null;
		this.cachedHashCode = createHashCode();
	}

	/**
	 * For when need a key consisting of three strings.
	 * 
	 * @param s1
	 *            String to be part of the key
	 * @param s2
	 *            String to be part of the key
	 * @param s3
	 *            String to be part of the key
	 */
	public MapKey(String s1, String s2, String s3) {
		this.s1 = s1;
		this.s2 = s2;
		this.s3 = s3;
		this.cachedHashCode = createHashCode();
	}

	/**
	 * A different way of creating a MapKey by using a static method instead of
	 * using new MapKey(). For when key consists of two strings.
	 * 
	 * @param s1
	 *            String to be part of the key
	 * @param s2
	 *            String to be part of the key
	 * @return the MapKey for the specified strings
	 */
	public static MapKey create(String s1, String s2) {
		return new MapKey(s1, s2);
	}

	/**
	 * A different way of creating a MapKey by using a static method instead of
	 * using new MapKey(). For when key consists of three strings.
	 * 
	 * @param s1
	 *            String to be part of the key
	 * @param s2
	 *            String to be part of the key
	 * @param s3
	 *            String to be part of the key
	 * @return the MapKey for the specified strings
	 */
	public static MapKey create(String s1, String s2, String s3) {
		return new MapKey(s1, s2, s3);
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
		result = prime * result + ((s1 == null) ? 0 : s1.hashCode());
		result = prime * result + ((s2 == null) ? 0 : s2.hashCode());
		result = prime * result + ((s3 == null) ? 0 : s3.hashCode());
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
		if (s1 == null) {
			if (other.s1 != null)
				return false;
		} else if (!s1.equals(other.s1))
			return false;
		if (s2 == null) {
			if (other.s2 != null)
				return false;
		} else if (!s2.equals(other.s2))
			return false;
		if (s3 == null) {
			if (other.s3 != null)
				return false;
		} else if (!s3.equals(other.s3))
			return false;
		return true;
	}

}
