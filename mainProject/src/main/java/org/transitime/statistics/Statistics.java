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
import java.util.Arrays;
import java.util.List;

/**
 * Some miscellaneous statistics functions such as determining average values
 * from a list and for determining average after filtering out of outliers from
 * a list. Also contains simple statistical functions for determining mean and
 * standard deviation. Could use Apache Commons math3 library instead but this
 * calculations are so simple that seems unnecessary.
 * 
 *
 * @author SkiBu Smith
 *
 */
public class Statistics {

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
			double fraction = (double) value/target;
			
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
	
	/**
	 * Converts the List<Integer> to a array of ints. This can be useful
	 * when amount of memory used is critical.
	 *  
	 * @param list
	 * @return
	 */
	public static int[] toArray(List<Integer> list) {
		if (list == null)
			return new int[0];
		
		int result[] = new int[list.size()];
		for (int i=0; i<list.size(); ++i)
			result[i] = list.get(i);
		return result;
	}
	
	/**
	 * For converting array of ints into array of doubles so that it can
	 * be used in the statistics functions.
	 * 
	 * @param array
	 * @return
	 */
	public static double[] toDoubleArray(int array[]) {
		double result[] = new double[array.length];
		for (int i=0; i<array.length; ++i)
			result[i] = array[i];
		return result;
	}

	/**
	 * For converting List of Integers into array of doubles so that it can
	 * be used in the statistics functions.
	 * 
	 * @param list
	 * @return
	 */
	public static double[] toDoubleArray(List<Integer> list) {
		if (list == null)
			return new double[0];
		
		double result[] = new double[list.size()];
		for (int i=0; i<list.size(); ++i)
			result[i] = list.get(i);
		return result;	    
	}
	
	/**
	 * Returns the mean of the array of doubles
	 * 
	 * @param values
	 * @return the mean, or NaN if there is no data
	 */
	public static double getMean(double[] values) {
		double sum = 0.0;
		for (int i=0; i<values.length; ++i) 
			sum += values[i];
		return sum/values.length;
	}
	
	/**
	 * Returns the mean of the array of ints
	 * 
	 * @param values
	 * @return
	 */
	public static double getMean(int[] values) {
		int sum = 0;
		for (int i=0; i<values.length; ++i) 
			sum += values[i];
		return ((double) sum)/values.length;
	}
	
	/**
	 * Returns the sample standard deviation for the values passed in.
	 * The "sample" standard deviation is used because only looking at
	 * a subset of data and the sample size is expected to be relatively
	 * small. To determine the "sample" standard deviation the variance
	 * is determined by dividing by N-1 instead of N.
	 * Since most clients will also need the mean it is passed in to
	 * this method so that it is not calculated twice.
	 * 
	 * @param values
	 * @param mean
	 * @return the sample standard deviation. Returns NaN if sample size is 1.
	 */
	public static double getSampleStandardDeviation(double[] values, double mean) {
		double sumSquaredDifferences = 0.0;
		for (int i=0; i<values.length; ++i) {
			double differenceFromMean = mean - values[i];
			sumSquaredDifferences += differenceFromMean * differenceFromMean;
		}
		
		// Determine the variance, which is the sum of the squared differences
		// from the mean divided by the sample size N. But note that actually
		// dividing by N-1 in order to reduce the bias due to expected 
		// relatively small sample size.
		double unbiasedVariance = sumSquaredDifferences / (values.length - 1);
		
		// Determine standard deviation
		double standardDeviation = Math.sqrt(unbiasedVariance);
		
		// Return results
		return standardDeviation;
	}
	
	/**
	 * Just for testing
	 */
	public static void main(String args[]) {
		Integer array[] = {2,4, 3};//, 4, 4, 4, 5, 5, 7, 9};
		List<Integer> values = Arrays.asList(array); 
		
		double[] doubleValues = toDoubleArray(toArray(values));
		double mean = getMean(doubleValues);
		double stdDev = getSampleStandardDeviation(doubleValues, mean);
		System.out.println("mean=" + mean + " stdDev=" + stdDev);
	}
}
