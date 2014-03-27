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
 * Very simple class for timing duration of events.
 * 
 * @author SkiBu Smith
 *
 */
public class IntervalTimer {

	long initialNanotime;
	
	/********************** Member Functions **************************/

	/**
	 * Records the current time so that elapsed time can later be determined.
	 */
	public IntervalTimer() {
		initialNanotime = System.nanoTime();
	}
	
	/**
	 * Resets the timer to the current time.
	 */
	public void resetTimer() {
		initialNanotime = System.nanoTime();
	}
	
	/**
	 * Time elapsed in msec since IntervalTimer created or resetTimer() called
	 * 
	 * @return elapsed time in msec
	 */
	public long elapsedMsec() {
		return (System.nanoTime() - initialNanotime) / Time.NSEC_PER_MSEC;
	}
	
	/**
	 * Time elapsed in nanoseconds since IntervalTimer created or resetTimer() called
	 * 
	 * @return elapsed time in nanoseconds
	 */
	public long elapseNanoSec() {
		return System.nanoTime() - initialNanotime;
	}
}
