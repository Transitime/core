/*
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License (GPL) as published by the
 * Free Software Foundation, either version 3 of the License, or any later
 * version.
 * 
 * Transitime.org is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Transitime.org . If not, see <http://www.gnu.org/licenses/>.
 */

package org.transitclock.utils;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Simple string utilities such as for formating double to desired number of
 * decimal places.
 * 
 * @author SkiBu Smith
 * 
 */
public class StringUtils {

	private static final DecimalFormat oneDigitFormat =
			new DecimalFormat("0.0");
	private static final DecimalFormat twoDigitFormat = new DecimalFormat(
			"0.00");
	private static final DecimalFormat threeDigitFormat = new DecimalFormat(
			"0.000");
	private static final DecimalFormat sixDigitFormat = new DecimalFormat(
			"0.000000");
	private static final DecimalFormat commasFormat = new DecimalFormat(
			"###,###");

	/**
	 * Returns formatted string indicating number of bytes. Uses highest
	 * possible units, up to 4 digits. will get a result such as 4,231MB
	 * 
	 * @param arg
	 * @return
	 */
	public static String memoryFormat(long arg) {
		String units = " bytes";
		if (arg >= 10000) {
			arg /= 1024;
			units = "KB";
			if (arg >= 10000) {
				arg /= 1024;
				units = "MB";
				if (arg >= 10000) {
					arg /= 1024;
					units = "GB";
					if (arg >= 10000) {
						arg /= 1024;
						units = "TB";
					}
				}
			}
		}

		return commasFormat.format(arg) + units;
	}

	/**
	 * Outputs distance string along with units. If value below 3,000 then uses
	 * meters as in 392.2m . If greater than 3,000 then uses km as in 3.1km .
	 * 
	 * @param distance
	 * @return The distance as a string
	 */
	public static String distanceFormat(double distance) {
		String units = "m";
		if (distance > 3000.0) {
			distance /= 1000.0;
			units = "km";
		}
		return oneDigitFormat(distance) + units;
	}

	/**
	 * For formatting double to 1 decimal place.
	 * 
	 * @param arg
	 * @return the value as a string
	 */
	public static String oneDigitFormat(double arg) {
		// Handle NaN specially
		if (Double.isNaN(arg))
			return "NaN";

		return oneDigitFormat.format(arg);
	}

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
	 * For formatting double to 3 decimal places.
	 * 
	 * @param arg
	 * @return the value as a string
	 */
	public static String threeDigitFormat(double arg) {
		// Handle NaN specially
		if (Double.isNaN(arg))
			return "NaN";

		return threeDigitFormat.format(arg);
	}

	/**
	 * For formatting double to 6 decimal places.
	 * 
	 * @param arg
	 * @return the value as a string
	 */
	public static String sixDigitFormat(double arg) {
		// Handle NaN specially
		if (Double.isNaN(arg))
			return "NaN";

		return sixDigitFormat.format(arg);
	}

	/**
	 * Returns original string padded with spaces so that it is
	 * desiredCharacters long. If the original string is already at least as
	 * long as desired then it is returned.
	 * 
	 * @param original
	 * @param desiredCharacters
	 * @return
	 */
	public static String padWithBlanks(String original, int desiredCharacters) {
		if (original == null)
			return null;

		if (original.length() >= desiredCharacters)
			return original;

		String paddedStr = original;
		for (int i = 0; i < desiredCharacters - original.length(); ++i) {
			paddedStr = " " + paddedStr;
		}
		return paddedStr;
	}

	/**
	 * Returns the name passed in, but the first number in the name is padded
	 * with zeros so that the numbers have the same number of digits. This way
	 * can sort the names properly even if have names such as Y2 and Y101 (Y2
	 * should be before Y101 even though if used regular string comparison Y101
	 * would be before Y2).
	 * 
	 * @param name
	 *            The alpha numeric name to be padded.
	 * @return a modified version of the name that can be used for proper
	 *         ordering of a collection of names. for ordering
	 */
	public static String paddedName(String name) {
		final int NUMBER_DIGITS_WHEN_PADDED = 8;

		// Find the first digit
		int startOfNumber = 0;
		while (startOfNumber < name.length()
				&& !Character.isDigit(name.charAt(startOfNumber))) {
			++startOfNumber;
		}

		// If no digits then simply return the route's short name
		if (startOfNumber >= name.length())
			return name;

		// Determine how many digits there are
		int numberOfDigits = 0;
		while (startOfNumber + numberOfDigits < name.length()
				&& Character.isDigit(name
						.charAt(startOfNumber + numberOfDigits))) {
			++numberOfDigits;
		}

		// Create the result. First add the leading non digits
		StringBuilder builder = new StringBuilder();
		builder.append(name.substring(0, startOfNumber));
		// Add necessary 0 padding to make numbers the same length
		for (int i = 0; i < NUMBER_DIGITS_WHEN_PADDED - numberOfDigits; ++i)
			builder.append('0');
		// Add the number and any remaining part of the string
		builder.append(name.substring(startOfNumber));

		// Return the padded result
		return builder.toString();
	}

	/**
	 * Sorts the list of IDs. Pads the first numeric part of the identifiers
	 * when doing the sorting so that numbers will end up in the proper order.
	 * This is 10 times slower than sortIds(), but the ordering is much nicer.
	 * 
	 * @param ids
	 */
	public static void sortIdsNumerically(List<String> ids) {
		Collections.sort(ids, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				String paddedStr1 = StringUtils.paddedName(o1);
				String paddedStr2 = StringUtils.paddedName(o2);

				return paddedStr1.compareTo(paddedStr2);
			}
		});
	}

	/**
	 * Sorts the list of IDs. Uses a regular string sort. Much faster than
	 * sorting IDs numerically because don't need to pad the IDs in order for
	 * them to be ordered properly. But then number won't be in the proper
	 * order. For example, "10" will end up before "9".
	 * 
	 * @param ids
	 *            to be sorted
	 */
	public static void sortIds(List<String> ids) {
		Collections.sort(ids);
	}

}
