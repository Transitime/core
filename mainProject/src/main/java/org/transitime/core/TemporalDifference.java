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
package org.transitime.core;

import java.io.Serializable;

import org.transitime.configData.CoreConfig;
import org.transitime.utils.Time;

/**
 * For keeping track of how far off a vehicle is from the expected time. For
 * schedule adherence and for determining temporal matches. Positive means ahead
 * of and traveling faster than expected.
 * <p>
 * Class made serializable so that schedule adherence data can be made available
 * through RMI calls.
 * 
 * @author SkiBu Smith
 * 
 */
public class TemporalDifference implements Serializable {

	// Positive means ahead of and traveling faster than expected. Negative 
	// means behind and traveling slower than expected.
	private final int temporalDifferenceMsec;

	private static final long serialVersionUID = 3306638473792296250L;

	/********************** Member Functions **************************/

	/**
	 * @param temporalDifferenceMsec
	 *            Positive means vehicle is ahead of where expected, negative
	 *            means behind.
	 */
	public TemporalDifference(int temporalDifferenceMsec) {
		this.temporalDifferenceMsec = temporalDifferenceMsec;
	}

	/**
	 * Often working with longs for time so it is convenient to have a
	 * constructor that will accept a long as the temporal difference.
	 * 
	 * @param temporalDifferenceMsec
	 *            Positive means vehicle is ahead of where expected, negative
	 *            means behind.
	 */
	public TemporalDifference(long temporalDifferenceMsec) {
		this.temporalDifferenceMsec = (int) temporalDifferenceMsec;
	}

	/**
	 * Returns true if the temporal difference is within the bounds specified by
	 * CoreConfig.getAllowableEarlySeconds() and
	 * CoreConfig.getAllowableLateSeconds().
	 * 
	 * @return true if within bounds
	 */
	public boolean isWithinBounds() {
		return temporalDifferenceMsec < 
					CoreConfig.getAllowableEarlySeconds() * 1000 
				&& -temporalDifferenceMsec < 
					CoreConfig.getAllowableLateSeconds() * 1000;
	}

	/**
	 * Returns true if the temporal difference is within the bounds specified by
	 * CoreConfig.getAllowableEarlySecondsForInitialMatching() and
	 * CoreConfig.getAllowableLateSecondsForInitialMatching(). For use when
	 * initially matching a vehicle. Need to be more restrictive than for other
	 * matching since the initial matching is more difficult. Need to only match
	 * if it is a reasonable match since vehicles do really peculiar things.
	 * 
	 * @return true if within bounds
	 */
	public boolean isWithinBoundsForInitialMatching() {
		return temporalDifferenceMsec < 
					CoreConfig.getAllowableEarlySecondsForInitialMatching() * 1000 
				&& -temporalDifferenceMsec < 
					CoreConfig.getAllowableLateSecondsForInitialMatching() * 1000;
	}

	/**
	 * Returns whether the temporal difference is within the specified bounds.
	 * 
	 * @param allowableEarlySeconds
	 * @param allowableLateSeconds
	 * @return
	 */
	public boolean isWithinBounds(int allowableEarlySeconds,
			int allowableLateSeconds) {
		return temporalDifferenceMsec < allowableEarlySeconds
				&& -temporalDifferenceMsec < allowableLateSeconds;
	}
	
	/**
	 * Returns an easily comparable positive value for the temporal difference
	 * that takes into account that being early is far worse and less likely
	 * than being late.
	 * 
	 * @return
	 */
	private int temporalDifferenceAbsoluteValue() {
		if (temporalDifferenceMsec > 0)
			return (int) Math.round(temporalDifferenceMsec * 
					CoreConfig.getEarlyToLateRatio());
		else 
			return -temporalDifferenceMsec;
	}
	
	/**
	 * Returns if the temporal difference for this object is better than that
	 * for the TemporalDifference parameter other.
	 * 
	 * @param other
	 *            TemporalDifference to be compared to
	 * @return
	 */
	public boolean betterThan(TemporalDifference other) {
		// If other parameter not specified then temporal difference for
		// this object should be considered true.
		if (other == null)
			return true;
		
		// Compare using absolute values that are adjusted by the
		// getEarlyToLateRatio.
		return this.temporalDifferenceAbsoluteValue() < 
				other.temporalDifferenceAbsoluteValue();
	}
	
	/**
	 * Returns if the temporal difference for this object is better than or
	 * equal to that for the TemporalDifference parameter other.
	 * 
	 * @param other
	 *            TemporalDifference to be compared to
	 * @return
	 */
	public boolean betterThanOrEqualTo(TemporalDifference other) {
		// If other parameter not specified then temporal difference for
		// this object should be considered true.
		if (other == null)
			return true;
		
		// Compare using absolute values that are adjusted by the
		// getEarlyToLateRatio.
		return this.temporalDifferenceAbsoluteValue() <= 
				other.temporalDifferenceAbsoluteValue();
	}
	
	/**
	 * Returns the temporalDifferenceMsec in msec. Positive means vehicle
	 * is early and negative means that vehicle is late.
	 * @return
	 */
	public int getTemporalDifference() {
		return temporalDifferenceMsec;
	}

	@Override
	public String toString() {
		String str = Time.elapsedTimeStr(temporalDifferenceMsec);
		
		// Add early/ontime/late info
		if (temporalDifferenceMsec > 0)
			str += " (early)";
		else if (temporalDifferenceMsec == 0)
			str += " (ontime)";
		else
			str += " (late)";
		
		// Return the results
		return str;
	}
	
}
