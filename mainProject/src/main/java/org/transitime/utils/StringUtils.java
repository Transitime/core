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

/**
 * Simple string utilities such as for formating double to desired number of
 * decimal places.
 * 
 * @author SkiBu Smith
 * 
 */
public class StringUtils {
	
	private static final DecimalFormat twoDigitFormat = new DecimalFormat("0.00");

	/**
	 * For formatting double to 2 decimal places.
	 * 
	 * @param arg
	 * @return the value as a string
	 */
	public static String twoDigitFormat(double arg) {
		// Handle NaN specially
		if (Double.isNaN(arg))
			return "NaN";
		
		return twoDigitFormat.format(arg);
	}

	/**
	 * Returns the route shortName, but the first number in the name
	 * is padded with zeros so that the numbers have the same number
	 * of digits. This way can sort the names properly even if have
	 * names such as Y2 and Y101 (Y2 should be before Y101 even though
	 * if used regular string comparison Y101 would be before Y2).
	 * 
	 * @return
	 */
	public static String paddedName(String name) {
		final int NUMBER_DIGITS_WHEN_PADDED = 8;

		// Find the first digit
		int startOfNumber = 0;
		while (startOfNumber<name.length() &&
				!Character.isDigit(name.charAt(startOfNumber))) {
			++startOfNumber;
		}
		
		// If no digits then simply return the route's short name
		if (startOfNumber >= name.length())
			return name;

		// Determine how many digits there are
		int numberOfDigits=0;
		while (startOfNumber+numberOfDigits < name.length() &&
				Character.isDigit(name.charAt(startOfNumber+numberOfDigits))) {
			++numberOfDigits;
		}	
		
		// Create the result. First add the leading non digits
		StringBuilder builder = new StringBuilder();
		builder.append(name.substring(0, startOfNumber));
		// Add necessary 0 padding to make numbers the same length
		for (int i=0; i<NUMBER_DIGITS_WHEN_PADDED-numberOfDigits; ++i)
			builder.append('0');
		// Add the number and any remaining part of the string
		builder.append(name.substring(startOfNumber));
		
		// Return the padded result
		return builder.toString();
	}
	

}
