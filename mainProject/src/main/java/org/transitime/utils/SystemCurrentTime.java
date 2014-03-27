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

/**
 * Implements SystemTime and returns the system current time when get() 
 * is called.
 * 
 * @author SkiBu Smith
 *
 */
public class SystemCurrentTime implements SystemTime {
	
	/**
	 * @return System epoch time in milliseconds
	 */
	@Override
	public long get() {
		return System.currentTimeMillis();
	}
}
