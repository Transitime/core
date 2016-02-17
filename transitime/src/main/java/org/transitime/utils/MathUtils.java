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

import java.util.Collection;

/**
 * Simple math utilities. Note that didn't call class "Math" because
 * that would cause confusion with the usual Java Math class.
 * 
 * @author SkiBu Smith
 *
 */
public class MathUtils {

	private static long TENS[] = new long[19]; static {
	    TENS[0] = 1;
	    for (int i = 1; i < TENS.length; i++) TENS[i] = 10 * TENS[i - 1];
	}

	/**
	 * Fast way of truncating a double to a certain number of digits. Certainly
	 * useful for latitudes and longitudes.
	 * 
	 * @param v
	 * @param precision
	 * @return the value v rounded such that there are specified precision
	 *         number of digits after the decimal point. If v is NaN then NaN is
	 *         returned.
	 */
	public static double round(double v, int precision) {
		if (Double.isNaN(v))
			return v;
		
	    assert precision >= 0 && precision < TENS.length;
	    double unscaled = v * TENS[precision];
	    if(unscaled < Long.MIN_VALUE || unscaled > Long.MAX_VALUE) 
	       return v;
	    long unscaledLong = (long) (unscaled + (v < 0 ? -0.5 : 0.5));
	    return (double) unscaledLong / TENS[precision];
	}

    public static double average(Collection<Double> doubles) {
        Double avg = 0d;
        int count = 0;
        for(Double doubleVal : doubles){
            count++;
            avg += doubleVal;
        }
        return avg / count;
    }

    public static double sum(Collection<Double> doubles) {
        Double sum = 0d;
        for(Double doubleVal : doubles){
            sum += doubleVal;
        }
        return sum;
    }

    public static double min(Collection<Double> doubles) {
        if(doubles.size() < 1)
            throw new IllegalArgumentException("No items in list");
        Double min = Double.MAX_VALUE;
        for(Double doubleVal : doubles){
            if(doubleVal < min)
                min = doubleVal;
        }
        return min;
    }

    public static double max(Collection<Double> doubles) {
        if(doubles.size() < 1)
            throw new IllegalArgumentException("No items in list");
        Double max = 0d;
        for(Double doubleVal : doubles){
            if(doubleVal > max)
                max = doubleVal;
        }
        return max;
    }
}
