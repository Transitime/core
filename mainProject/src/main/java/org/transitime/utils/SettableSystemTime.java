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
 * For when need a system time that can be set to a specific value. For playback 
 * or for debugging.
 * 
 * @author SkiBu Smith
 *
 */
public class SettableSystemTime implements SystemTime {
	private long time;

	/********************** Member Functions **************************/
	
	public SettableSystemTime(long time) {
		this.time = time;
	}
	
	public SettableSystemTime() {
		this.time = 0;
	}
	
	/* (non-Javadoc)
	 * @see org.transitime.utils.SystemTime#get()
	 */
	@Override
	public long get() {
		return time;
	}

	/**
	 * So can update the time
	 * 
	 * @param time
	 */
	public void set(long time) {
		this.time = time;
	}
}
