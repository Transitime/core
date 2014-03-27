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
 * So that can have access to a system time whether in normal
 * mode where the system clock can be used, or playback mode
 * where use the last AVL time.
 * 
 * @author SkiBu Smith
 * 
 */
public interface SystemTime {

	/**
	 * @return Returns current system epoch time in milliseconds. If in playback
	 *         mode or such this might not actually be the clock time for the
	 *         computer.
	 */
	public abstract long get();
}
