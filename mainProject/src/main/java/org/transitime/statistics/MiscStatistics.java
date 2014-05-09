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

package org.transitime.statistics;

import java.util.ArrayList;
import java.util.List;

/**
 * Some miscellaneous statistics functions
 *
 * @author SkiBu Smith
 *
 */
public class MiscStatistics {

	/********************** Member Functions **************************/

	/**
	 * Gets the average values of the values passed in, without doing any
	 * filtering.
	 * 
	 * @param values
	 *            The values to be averaged
	 * @return The average
	 */
	public static int average(List<Integer> values) {
		// If only a single value then return it
		if (values.size() == 1) 
			return values.get(0);

		// There are multiple values so determine average
		int sum = 0;
		for (int value : values) 
			sum += value;
		int average = sum / values.size();
		
		// Return result
		return average;
	}
	
	/**
	 * Returns a list of values that was passed in but with the worst outlier
	 * filtered out. If there were no outliers then returns the original list.
	 * If there are only 2 elements in the values list then can't really have
	 * outliers so it simply returns the original list.
	 * 
	 * @param values
	 *            The original list of values to filter
	 * @param target
	 *            Used in conjunction with fractionalLimit to determine if a
	 *            value should be filtered out.
	 * @param fractionalLimit
	 *            If value is further away than fractionalLimit of average it is
	 *            filtered out if there are more than 2 data points. Expressed
	 *            as a fractional value between 0.0 and 1.0 such that a value of
	 *            1.0 means 100%. Therefore if one uses a value of 0.5 than
	 *            values below 50% or above 200% of the average are filtered
	 *            out.
	 * @return List of values but with the worst outlier filtered out
	 */
	private static List<Integer> filteredList(List<Integer> values,
			int target, double fractionalLimit) {
		// If the size of the list is just 2 items or less then can't
		// effectively filter out outliers.
		if (values.size() <= 2)
			return values;
		
		// Find the worst value that is beyond the fractionalLimit
		// of the target.
		double worstOffenderFraction = 1.0;
		int worstOffenderIndex = -1;
		for (int index=0; index<values.size(); ++index) {
			int value = values.get(index);
			double fraction = value/target;
			
			// Use fraction value between 0.0 and 1.0. So if value greater
			// than 1.0 use the reciprocal.
			if (fraction > 1.0)
				fraction = 1.0/fraction;
			
			// If the value is beyond the acceptable limit and it is the worst
			// one then remember it so it can be filtered out.
			if (fraction < fractionalLimit && fraction < worstOffenderFraction) {
				worstOffenderFraction = fraction;
				worstOffenderIndex = index;
			}
		}
	
		if (worstOffenderIndex == -1) {
			// There were no values that needed to be filtered to simply
			// return the original list.
			return values;
		} else {
			// Need to filter out problem value so create new list
			// and return it.
			List<Integer> result = new ArrayList<Integer>(values.size());
			for (int index=0; index<values.size(); ++index) {
				if (index != worstOffenderIndex) {
					int value = values.get(index);
					result.add(value);
				}
			}
			return result;
		}
	}
	
	/**
	 * Gets the average value of the values passed in. Filters outliers that are
	 * less than minPercentage or greater than maxPercentage of the average of
	 * all values.
	 * 
	 * @param values
	 *            The values to be averaged
	 * @param fractionalLimit
	 *            If value is further away than fractionalLimit of average it is
	 *            filtered out if there are more than 2 data points. Expressed
	 *            as a fractional value between 0.0 and 1.0 such that a value of
	 *            1.0 means 100%. Therefore if one uses a value of 0.5 than
	 *            values below 50% or above 200% of the average are filtered
	 *            out.
	 * @return The average value, after outliers have been filtered
	 */
	public static int filteredAverage(List<Integer> values,
			double fractionalLimit) {
		// First determine average without any filtering
		int average = average(values);
		
		// Filter out outliers in case there were more than 2 data points 
		while (true) {
			List<Integer> filteredList = 
					filteredList(values, average, fractionalLimit);
			if (filteredList == values) {
				return average;
			} else {
				values = filteredList;
				average = average(values);
			}
		}		
	}
	
}
