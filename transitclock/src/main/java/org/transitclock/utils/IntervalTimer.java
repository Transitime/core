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

import java.text.DecimalFormat;

/**
 * Very simple class for timing duration of events. Main use is at the beginning
 * of the process to construct an IntervalTimer and then at the end to call
 * <code>elapsedMsec()</code> to determine elapsed time.
 * 
 * @author SkiBu Smith
 * 
 */
public class IntervalTimer {

	// The time the interval timer created or reset
	private long initialNanotime;

	private final String name;
	
	private static DecimalFormat decimalFormat = new DecimalFormat("#.###");
	
	/********************** Member Functions **************************/

	/**
	 * Records the current time so that elapsed time can later be determined.
	 */
	public IntervalTimer() {
		this.name = "timer";
		initialNanotime = System.nanoTime();
	}

	public IntervalTimer(String name){
		this.name = name;
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
	public long elapsedNanoSec() {
		return System.nanoTime() - initialNanotime;
	}
	
	/**
	 * For outputting elapsed time in milliseconds with 3 digits after the
	 * decimal point, as in 123.456 msec.
	 * 
	 * @return String of elapsed time in milliseconds
	 */
	public String elapsedMsecStr() {
		long microSecs = (System.nanoTime() - initialNanotime)/1000;
		return decimalFormat.format((double) microSecs / 1000);
	}

	public void printTime(){
		System.out.println(name + " - " + elapsedMsecStr());
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
