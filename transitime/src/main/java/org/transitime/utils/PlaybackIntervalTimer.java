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

import java.text.DecimalFormat;

import org.transitime.applications.Core;

/**
 * Very simple class for timing duration of events. Main use is at the beginning
 * of the process to construct an IntervalTimer and then at the end to call
 * <code>elapsedMsec()</code> to determine elapsed time.
 * 
 * @author SkiBu Smith
 * 
 */
public class PlaybackIntervalTimer {

	// The time the interval timer created or reset
	private long initialtime;
	
	private static DecimalFormat decimalFormat = new DecimalFormat("#.###");
	
	/********************** Member Functions **************************/

	/**
	 * Records the current time so that elapsed time can later be determined.
	 */
	public PlaybackIntervalTimer() {
		initialtime = Core.getInstance().getSystemTime();
	}
	
	/**
	 * Resets the timer to the current time.
	 */
	public void resetTimer() {
		initialtime = Core.getInstance().getSystemTime();
	}
	
	/**
	 * Time elapsed in msec since IntervalTimer created or resetTimer() called
	 * 
	 * @return elapsed time in msec
	 */
	public long elapsedMsec() {
		return Math.abs(Core.getInstance().getSystemTime() - initialtime);
	}
			
	/**
	 * For outputting elapsed time in milliseconds 
	 * 
	 * @return String of elapsed time in milliseconds
	 */
	private String elapsedMsecStr() {
		
		return ""+(Core.getInstance().getSystemTime() - initialtime);
	}
	
	/**
	 * toString() is defined so that it works well when debug logging. This way
	 * can pass in reference to the timer object. Only if debug logging is
	 * enabled will the toString() be called causing the string to actually be
	 * generated.
	 */
	@Override
	public String toString() {
		return elapsedMsecStr();
	}
}
