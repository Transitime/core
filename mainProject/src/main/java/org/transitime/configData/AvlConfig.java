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

import org.transitime.config.DoubleConfigValue;
import org.transitime.config.FloatConfigValue;
import org.transitime.config.IntegerConfigValue;
import org.transitime.config.StringConfigValue;
import org.transitime.utils.Time;

/**
 * Handles the AVL configuration data. 
 *
 * @author SkiBu Smith
 *
 */
public class AvlConfig {
	/**
	 * How frequently an AVL feed should be polled for new data.
	 * @return
	 */
	public static int getSecondsBetweenAvlFeedPolling() {
		return secondsBetweenAvlFeedPolling.getValue();
	}
	private static IntegerConfigValue secondsBetweenAvlFeedPolling =
			new IntegerConfigValue("transitime.avl.feedPollingRateSecs", 5);
	
	/**
	 * For when polling AVL XML feed.
	 * @return
	 */
	public static int getAvlFeedTimeoutInMSecs() {
		return avlFeedTimeoutInMSecs.getValue();
	}
	private static IntegerConfigValue avlFeedTimeoutInMSecs =
			new IntegerConfigValue("transitime.avl.feedTimeoutInMSecs", 10000);
	
	/**
	 * How many items to go into the blocking AVL queue before need to
	 * wait for queue to have space.
	 * @return
	 */
	public static int getAvlQueueSize() {
		return avlQueueSize.getValue();
	}
	private static IntegerConfigValue avlQueueSize = 
			new IntegerConfigValue("transitime.avl.queueSize", 1000);

	/**
	 * How many threads to be used for processing the AVL data. For most
	 * applications just using a singe thread is probably sufficient and it
	 * makes the logging simpler since the messages will not be interleaved.
	 * But for large systems with lots of vehicles then should use multiple 
	 * threads, such as 3-5 so that more of the cores are used.
	 * @return
	 */
	public static int getNumAvlThreads() {
		return numAvlThreads.getValue();
	}
	private static IntegerConfigValue numAvlThreads = 
			new IntegerConfigValue("transitime.avl.numThreads", 1);
	
	/**
	 * Max speed that an AVL report is allowed to have.
	 * @return
	 */
	public static double getMaxAvlSpeed() {
		return maxAvlSpeed.getValue();
	}
	private static DoubleConfigValue maxAvlSpeed =
			new DoubleConfigValue("transitime.avl.maxSpeed", 45.0); // 45m/s = 100mph
	
	public static double minSpeedForValidHeading() {
		return minSpeedForValidHeading.getValue();
	}
	private static DoubleConfigValue minSpeedForValidHeading =
			new DoubleConfigValue("transitime.avl.minSpeedForValidHeading", 1.5); // 1.5m/s = .34mph
	
	/**
	 * For filtering out bad AVL reports. The default values of latitude 15.0 to 55.0 and
	 * longitude of -135.0 to -60.0 are for North America, including Mexico and Canada.
	 * Can see maps of lat/lon at http://www.mapsofworld.com/lat_long/north-america.html
	 * @return
	 */
	public static float getMinAvlLatitude() {
		return minAvlLatitude.getValue();
	}
	public static String getMinAvlLatitudeParamName() {
		return minAvlLatitude.getID();
	}
	private static FloatConfigValue minAvlLatitude =
			new FloatConfigValue("transitime.avl.minLatitude", 15.0f);
	
	public static float getMaxAvlLatitude() {
		return maxAvlLatitude.getValue();
	}
	public static String getMaxAvlLatitudeParamName() {
		return maxAvlLatitude.getID();
	}
	private static FloatConfigValue maxAvlLatitude =
			new FloatConfigValue("transitime.avl.maxLatitude", 55.0f);
	
	public static float getMinAvlLongitude() {
		return minAvlLongitude.getValue();
	}
	public static String getMinAvlLongitudeParamName() {
		return minAvlLongitude.getID();
	}
	private static FloatConfigValue minAvlLongitude =
			new FloatConfigValue("transitime.avl.minLongitude", -135.0f);
	
	public static float getMaxAvlLongitude() {
		return maxAvlLongitude.getValue();
	}
	public static String getMaxAvlLongitudeParamName() {
		return maxAvlLongitude.getID();
	}
	private static FloatConfigValue maxAvlLongitude =
			new FloatConfigValue("transitime.avl.maxLongitude", -60.0f);

	/**
	 * So can filter out unpredictable assignments such as for training coaches,
	 * service vehicles, or simply vehicles that are not in service and should
	 * not be attempted to be made predictable.
	 */
	public static String getUnpredictableAssignmentsRegEx() {
		return unpredictableAssignmentsRegEx.getValue();
	}
	private static StringConfigValue unpredictableAssignmentsRegEx =
			new StringConfigValue("transitime.avl.unpredictableAssignmentsRegEx", null);
	
	/**
	 * For AVL timeouts. If don't get an AVL report for the vehicle in this
	 * amount of time then the vehicle will be made non-predictable.
	 * @return
	 */
	public static int getAvlTimeoutSecs() {
		return timeoutSecs.getValue();
	}
	private static IntegerConfigValue timeoutSecs =
			new IntegerConfigValue("transitime.avl.timeoutSecs", 6*Time.SEC_PER_MIN);

}
