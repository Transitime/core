/* This file is part of Transitime.org
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

import java.util.ArrayList;
import java.util.List;
import org.transitime.config.BooleanConfigValue;
import org.transitime.config.DoubleConfigValue;
import org.transitime.config.FloatConfigValue;
import org.transitime.config.IntegerConfigValue;
import org.transitime.config.StringConfigValue;
import org.transitime.config.StringListConfigValue;
import org.transitime.utils.Time;

/**
 * Handles the core configuration data file. Allows parameters to be read in
 * from the file at startup and, in the future, while program is running. Goal
 * is for the accessing of the data to be fast so don't want to do any work
 * then. But also need to make sure that it is thread safe since might be
 * reading data while it is being updated.
 * 
 * @author SkiBu Smith
 * 
 */
public class CoreConfig {
	public static String getProjectId() {
		return projectId.getValue();
	}
	private static StringConfigValue projectId = 
			new StringConfigValue("transitime.core.projectId", "sf-muni");
	
	// Database params
	public static String getDbHost() {
		return dbHost.getValue();
	}
	private static StringConfigValue dbHost = 
			new StringConfigValue("transitime.core.dbHost", "localhost");
	
	public static String getDbUserName() {
		return dbUserName.getValue();
	}
	private static StringConfigValue dbUserName = 
			new StringConfigValue("transitime.core.dbUserName", "root");
	
	public static String getDbPassword() {
		return dbPassword.getValue();
	}
	private static StringConfigValue dbPassword = 
			new StringConfigValue("transitime.core.dbPassword", "transitime");
	
	
	/**
	 * So that have flexibility with where the hibernate config file is.
	 * This way can easily access it within Eclipse.
	 * @return
	 */
	public static String getHibernateConfigFileName() {
		return hibernateConfigFileName.getValue();
	}
	private static StringConfigValue hibernateConfigFileName = 
			new StringConfigValue("transitime.hibernate.configFile", 
					"C:/Users/Mike/git/testProject/testProject/src/main/config/hibernate.cfg.xml");
	
	/**
	 * The list of names of all of the modules that should be automatically
	 * started.
	 * @return
	 */
	public static List<String> getOptionalModules() {
		return optionalModules.getValue();
	}
	private static List<String> optionalModulesDefaultList = new ArrayList<String>();
	static {
		// Can add all the modules that should be started as default here
		//optionalModulesDefaultList.add("org.transitime.avl.NextBusAvlModule");
	}	
	private static StringListConfigValue optionalModules = 
			new StringListConfigValue("transitime.modules.optionalModulesList", 
						optionalModulesDefaultList);
	
	/**
	 * General parameters for matching vehicles, making predictions, etc
	 */
	
	/**
	 * How far a location can be from a path segment and still be considered
	 * a match.
	 * @return
	 */
	public static double getMaxDistanceFromSegment() {
		return maxDistanceFromSegment.getValue();
	}
	private static DoubleConfigValue maxDistanceFromSegment =
			new DoubleConfigValue("transitime.core.maxDistanceFromSegment", 60.0);
	
	/**
	 * How far heading in degrees of vehicle can be away from path segment
	 * and still be considered a match. Needs to be pretty lenient because
	 * stopPaths and heading might not be that accurate.
	 * @return
	 */
	public static float getMaxHeadingOffsetFromSegment() {
		return maxHeadingOffsetFromSegment.getValue();
	}
	private static FloatConfigValue maxHeadingOffsetFromSegment =
			new FloatConfigValue("transitime.core.maxHeadingOffsetFromSegment", 135.0f);

	/**
	 * For determining if enough time to deadhead to beginning of a trip. If
	 * vehicles are far away then they are more likely to be able to travel 
	 * faster because they could take a freeway or other fast road. But when
	 * get closer then will be on regular streets and will travel more slowly.
	 * The parameters should be set in a conservative way such that the travel
	 * time is underestimated by using slower speeds than will actually 
	 * encounter. This way the vehicle will arrive after the predicted time
	 * which means that passenger won't miss the bus.
	 * @return
	 */
	public static float getDeadheadingShortVersusLongDistance() {
		return deadheadingShortVersusLongDistance.getValue();
	}
	private static FloatConfigValue deadheadingShortVersusLongDistance =
			new FloatConfigValue("transitime.core.deadheadingShortVersusLongDistance", 1000.0f);

	public static float getShortDistanceDeadheadingSpeed() {
		return shortDistanceDeadheadingSpeed.getValue();
	}
	private static FloatConfigValue shortDistanceDeadheadingSpeed =
			new FloatConfigValue("transitime.core.shortDistanceDeadheadingSpeed", 4.0f); // 4.0m/s is about 8 mph

	public static float getLongDistanceDeadheadingSpeed() {
		return longDistanceDeadheadingSpeed.getValue();
	}
	private static FloatConfigValue longDistanceDeadheadingSpeed =
			new FloatConfigValue("transitime.core.longDistanceDeadheadingSpeed", 10.0f); // 10.0m/s is about 20mph

	/**
	 **************************************************************************
	 * For predictions
	 */
	public static int getMaxPredictionsTimeSecs() {
		return maxPredictionsTimeSecs.getValue();
	}
	public static int getMaxPredictionsTimeMsecs() {
		return getMaxPredictionsTimeSecs() * Time.MS_PER_SEC;
	}
	private static IntegerConfigValue maxPredictionsTimeSecs =
			new IntegerConfigValue("transitime.core.maxPredictionsTimeSecs", 
					60 * Time.SEC_PER_MIN);
	
	public static boolean getUseArrivalPredictionsForNormalStops() {
		return useArrivalPredictionsForNormalStops.getValue();
	}
	private static BooleanConfigValue useArrivalPredictionsForNormalStops =
			new BooleanConfigValue("transitime.core.useArrivalPredictionsForNormalStops", 
					true);
	
	/**
	 * For determining if prediction should be stored in db. Set to 0 to never
	 * store predictions. A very large number of predictions is created so
	 * be careful with this value so that the db doesn't get filled up too
	 * quickly.
	 * 
	 * @return
	 */
	public static int getMaxPredictionsTimeForDbSecs() {
		return maxPredictionsTimeForDbSecs.getValue();
	}
	private static IntegerConfigValue maxPredictionsTimeForDbSecs =
			new IntegerConfigValue("transitime.core.maxPredictionTimeForDbSecs", 
					1*Time.SEC_PER_MIN );
	
	/**
	 * How early a vehicle can be and still be matched to a layover. Needs to 
	 * be pretty large because sometimes vehicles will be assigned to a layover
	 * quite early, and want to be able to make the vehicle predictable and 
	 * generate predictions far in advance.
	 * @return
	 */
	public static int getAllowableEarlyForLayoverSeconds() {
		return allowableEarlyForLayoverSeconds.getValue();
	}
	private static IntegerConfigValue allowableEarlyForLayoverSeconds = 
			new IntegerConfigValue("transitime.core.allowableEarlyForLayoverSeconds", 
					90 * Time.SEC_PER_MIN);

	/**
	 * How early a vehicle can be and still be matched to its block assignment
	 * @return
	 */
	public static int getAllowableEarlySeconds() {
		return allowableEarlySeconds.getValue();
	}
	private static IntegerConfigValue allowableEarlySeconds = 
			new IntegerConfigValue("transitime.core.allowableEarlySeconds", 
					15 * Time.SEC_PER_MIN);

	/**
	 * How late a vehicle can be and still be matched to its block assignment
	 * @return
	 */
	public static int getAllowableLateSeconds() {
		return allowableLateSeconds.getValue();
	}
	private static IntegerConfigValue allowableLateSeconds = 
			new IntegerConfigValue("transitime.core.allowableLateSeconds", 
					90 * Time.SEC_PER_MIN);

	/**
	 * How far along path past a layover a vehicle can spatially match but 
	 * still be considered to be at that layover. Important for determining 
	 * predictions and such.
	 * @return
	 */
	public static double getDistanceAtWhichStillAtLayover() {
		return distanceAtWhichStillAtLayover.getValue();
	}
	private static DoubleConfigValue distanceAtWhichStillAtLayover =
			new DoubleConfigValue("transitime.core.distanceAtWhichStillAtLayover", 
					100.0);
	
	/**
	 * How far a vehicle can be ahead of a stop and be considered to have 
	 * arrived.
	 * @return
	 */
	public static double getBeforeStopDistance() {
		return beforeStopDistance.getValue();
	}
	private static DoubleConfigValue beforeStopDistance =
			new DoubleConfigValue("transitime.core.beforeStopDistance", 
					100.0);
	
	/**
	 * How far a vehicle can be past a stop and still be considered at the stop.
	 * @return
	 */
	public static double getAfterStopDistance() {
		return afterStopDistance.getValue();
	}
	private static DoubleConfigValue afterStopDistance =
			new DoubleConfigValue("transitime.core.afterStopDistance", 
					50.0);
	
	/**
	 * How far a vehicle can be past a stop and still be considered at the stop.
	 * @return
	 */
	public static int getDefaultBreakTimeSec() {
		return defaultBreakTimeSec.getValue();
	}
	private static IntegerConfigValue defaultBreakTimeSec =
			new IntegerConfigValue("transitime.core.defaultBreakTimeSec", 
					0);
	
	/**
	 * How much worse it is for a vehicle to be early as opposed to late when
	 * determining schedule adherence.
	 * @return
	 */
	public static double getEarlyToLateRatio() {
		return earlyToLateRatio.getValue();
	}
	private static DoubleConfigValue earlyToLateRatio =
			new DoubleConfigValue("transitime.core.earlyToLateRatio", 
					3.0);

	/**
	 * debugMode parameter
	 * @return
	 */
	public static boolean isDebugMode() {
		// Also make sure that debugMode is not null. This could
		// happen if isDebugMode() is called before the CoreConfig
		// file has been read in. This of course is a special case.
		if (debugMode.getValue() != null) {
			return debugMode.getValue();
		} else {
			return Boolean.getBoolean("transitime.debugMode");
		}
	}
	private static BooleanConfigValue debugMode = 
			new BooleanConfigValue("transitime.debugMode", false);	
	
	
	////////////  Following are test parameters and will be removed /////////////
	
	// A test param
	public static float getFloatTest() {
		return floatTest.getValue();
	}
	private static FloatConfigValue floatTest = 
			new FloatConfigValue("transitime.predictor.floatTest", 3.14f);	
	
	// A test param
	public static int getIntTest() {
		return intTest.getValue();
	}
	private static IntegerConfigValue intTest = 
			new IntegerConfigValue("transitime.predictor.intTest", 72);

	// A test param
	public static String getStringTest() {
		return strTest.getValue();
	}
	private static StringConfigValue strTest = 
			new StringConfigValue("transitime.predictor.strTest", "default str");

	// A test param
	public static List<String> getStringListTest() {
		return strListTest.getValue();
	}
	private static List<String> strListDefaultList = new ArrayList<String>();
	{
		strListDefaultList.add("item1");
		strListDefaultList.add("item2");
	}	
	private static StringListConfigValue strListTest = 
			new StringListConfigValue("transitime.predictor.strListTest", strListDefaultList);

}
