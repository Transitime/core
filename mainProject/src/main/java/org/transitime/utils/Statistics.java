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

import java.util.Arrays;
import java.util.List;

/**
 * Simple statistical functions for determining mean and standard deviation.
 * Could use Apache Commons math3 library instead but this calculations are
 * so simple that seems unnecessary.
 * 
 * @author SkiBu Smith
 *
 */
public class Statistics {

	/********************** Member Functions **************************/

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
	 * Returns the mean of the array of doubles
	 * 
	 * @param values
	 * @return the mean
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
