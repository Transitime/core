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

package org.transitime.configData;

import org.transitime.config.BooleanConfigValue;
import org.transitime.config.DoubleConfigValue;
import org.transitime.config.FloatConfigValue;
import org.transitime.config.IntegerConfigValue;
import org.transitime.config.StringConfigValue;

/**
 * Handles the AVL configuration data. 
 *
 * @author SkiBu Smith
 *
 */
public class AvlConfig {
	/**
	 * Specifies whether should use JMS to queue AVL data or if should
	 * process directly using single thread.
	 * @return
	 */
	public static boolean shouldUseJms() {
		return shouldUseJms.getValue();
	}
	private static BooleanConfigValue shouldUseJms =
			new BooleanConfigValue("transitime.avl.shouldUseJms", false,
					"Specifies whether should use JMS queue for handling " +
					"AVL reports. Useful for if feed is read on one machine " +
					"but processed on another.");
	
	/**
	 * How frequently an AVL feed should be polled for new data.
	 * @return
	 */
	public static int getSecondsBetweenAvlFeedPolling() {
		return secondsBetweenAvlFeedPolling.getValue();
	}
	private static IntegerConfigValue secondsBetweenAvlFeedPolling =
			new IntegerConfigValue("transitime.avl.feedPollingRateSecs", 5,
					"How frequently an AVL feed should be polled for new data.");
	
	/**
	 * For when polling AVL XML feed.
	 * @return
	 */
	public static int getAvlFeedTimeoutInMSecs() {
		return avlFeedTimeoutInMSecs.getValue();
	}
	private static IntegerConfigValue avlFeedTimeoutInMSecs =
			new IntegerConfigValue("transitime.avl.feedTimeoutInMSecs", 10000,
					"For when polling AVL XML feed. The feed logs error if "
					+ "the timeout value is exceeded when performing the XML "
					+ "request.");
	
	/**
	 * Max speed that an AVL report is allowed to have.
	 * @return max speed in m/s
	 */
	public static double getMaxAvlSpeed() {
		return maxAvlSpeed.getValue();
	}
	private static DoubleConfigValue maxAvlSpeed =
			new DoubleConfigValue("transitime.avl.maxSpeed", 
					31.3, // 31.3m/s = 70mph
					"Max speed between AVL reports for a vehicle. If this " +
					"value is exceeded then the AVL report is ignored.");
	
	/**
	 * If AVL report speed is below this threshold then the heading is not
	 * considered valid.
	 * @return
	 */
	public static double minSpeedForValidHeading() {
		return minSpeedForValidHeading.getValue();
	}
	private static DoubleConfigValue minSpeedForValidHeading =
			new DoubleConfigValue("transitime.avl.minSpeedForValidHeading", 
					1.5, // 1.5m/s = .34mph
					"If AVL report speed is below this threshold then the " +
					"heading is not considered valid.");
	
	/**
	 * For filtering out bad AVL reports. The default values of latitude 15.0 to
	 * 55.0 and longitude of -135.0 to -60.0 are for North America, including
	 * Mexico and Canada. Can see maps of lat/lon at
	 * http://www.mapsofworld.com/lat_long/north-america.html
	 * 
	 * @return
	 */
	public static float getMinAvlLatitude() {
		return minAvlLatitude.getValue();
	}
	public static String getMinAvlLatitudeParamName() {
		return minAvlLatitude.getID();
	}
	private static FloatConfigValue minAvlLatitude =
			new FloatConfigValue("transitime.avl.minLatitude", 15.0f,
					"For filtering out bad AVL reports. The default values " +
					"of latitude 15.0 to 55.0 and longitude of -135.0 to " +
					"-60.0 are for North America, including Mexico and " +
					"Canada. Can see maps of lat/lon at " +
					"http://www.mapsofworld.com/lat_long/north-america.html");
	
	public static float getMaxAvlLatitude() {
		return maxAvlLatitude.getValue();
	}
	public static String getMaxAvlLatitudeParamName() {
		return maxAvlLatitude.getID();
	}
	private static FloatConfigValue maxAvlLatitude =
			new FloatConfigValue("transitime.avl.maxLatitude", 55.0f,
					"For filtering out bad AVL reports. The default values " +
					"of latitude 15.0 to 55.0 and longitude of -135.0 to " +
					"-60.0 are for North America, including Mexico and " +
					"Canada. Can see maps of lat/lon at " +
					"http://www.mapsofworld.com/lat_long/north-america.html");
	
	public static float getMinAvlLongitude() {
		return minAvlLongitude.getValue();
	}
	public static String getMinAvlLongitudeParamName() {
		return minAvlLongitude.getID();
	}
	private static FloatConfigValue minAvlLongitude =
			new FloatConfigValue("transitime.avl.minLongitude", -135.0f,
					"For filtering out bad AVL reports. The default values " +
					"of latitude 15.0 to 55.0 and longitude of -135.0 to " +
					"-60.0 are for North America, including Mexico and " +
					"Canada. Can see maps of lat/lon at " +
					"http://www.mapsofworld.com/lat_long/north-america.html");
	
	public static float getMaxAvlLongitude() {
		return maxAvlLongitude.getValue();
	}
	public static String getMaxAvlLongitudeParamName() {
		return maxAvlLongitude.getID();
	}
	private static FloatConfigValue maxAvlLongitude =
			new FloatConfigValue("transitime.avl.maxLongitude", -60.0f,
					"For filtering out bad AVL reports. The default values " +
					"of latitude 15.0 to 55.0 and longitude of -135.0 to " +
					"-60.0 are for North America, including Mexico and " +
					"Canada. Can see maps of lat/lon at " +
					"http://www.mapsofworld.com/lat_long/north-america.html");

	/**
	 * So can filter out unpredictable assignments such as for training coaches,
	 * service vehicles, or simply vehicles that are not in service and should
	 * not be attempted to be made predictable. Returns empty string, the
	 * default value if transitime.avl.unpredictableAssignmentsRegEx is not set.
	 */
	public static String getUnpredictableAssignmentsRegEx() {
		return unpredictableAssignmentsRegEx.getValue();
	}
	private static StringConfigValue unpredictableAssignmentsRegEx =
			new StringConfigValue("transitime.avl.unpredictableAssignmentsRegEx", 
					"", // default value
					"So can filter out unpredictable assignments such as for " +
					"training coaches, service vehicles, or simply vehicles " +
					"that are not in service and should not be attempted to " +
					"be made predictable. Returns empty string, the default " +
					"value if transitime.avl.unpredictableAssignmentsRegEx " +
					"is not set.");
	
	/**
	 * Minimum allowable time in seconds between AVL reports for a vehicle. If
	 * get a report closer than this number of seconds to the previous one then
	 * the new report is filtered out and not processed. Important for when
	 * reporting rate is really high, such as every few seconds.
	 */
	public static int getMinTimeBetweenAvlReportsSecs() {
		return minTimeBetweenAvlReportsSecs.getValue();
	}
	private static IntegerConfigValue minTimeBetweenAvlReportsSecs =
			new IntegerConfigValue(
					"transitime.avl.minTimeBetweenAvlReportsSecs", 
					5,
					"Minimum allowable time in seconds between AVL reports for "
					+ "a vehicle. If get a report closer than this number of "
					+ "seconds to the previous one then the new report is "
					+ "filtered out and not processed. Important for when "
					+ "reporting rate is really high, such as every few "
					+ "seconds.");
	
	/**
	 * For debugging. Logs each AVL report to stdout if set to true.
	 * Default is false.
	 * 
	 * @return
	 */
	public static boolean shouldLogToStdOut() {
		return shouldLogToStdOut.getValue();
	}
	private static BooleanConfigValue shouldLogToStdOut =
			new BooleanConfigValue("transitime.avl.shouldLogToStdOut", false,
					"For debugging. Logs each AVL report to stdout if set "
					+ "to true. Default is false.");
}
