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
	public static String getAgencyId() {
		return projectId.getValue();
	}
	private static StringConfigValue projectId = 
			new StringConfigValue("transitime.core.agencyId", 
					null,
					"Specifies the ID of the agency. Used for the database " +
					"name and in the logback configuration to specify the " +
					"directory where to put the log files.");
	
	// Database params
	public static String getDbHost() {
		return dbHost.getValue();
	}
	private static StringConfigValue dbHost = 
			new StringConfigValue("transitime.core.dbHost", 
					null, // Null as default so can get from hibernate config
					"Specifies the name of the machine the database for the " +
					"project resides on. Use null value to use values from " +
					"hibernate config file. Set to \"localhost\" if database " +
					"running on same machine as the core application.");
	
	public static String getDbUserName() {
		return dbUserName.getValue();
	}
	private static StringConfigValue dbUserName = 
			new StringConfigValue("transitime.core.dbUserName", 
					null,
					"Specifies login for the project database. Use null " +
					"value to use values from hibernate config file.");
	
	public static String getDbPassword() {
		return dbPassword.getValue();
	}
	private static StringConfigValue dbPassword = 
			new StringConfigValue("transitime.core.dbPassword", 
					null,
					"Specifies password for the project database. Use null " +
					"value to use values from hibernate config file.",
					false); // Don't log password in configParams log file
	
	/**
	 * When in playback mode or some other situations don't want to store
	 * generated data such as arrivals/departures, events, and such to the
	 * database because only debugging.
	 * 
	 * @return
	 */
	public static boolean storeDataInDatabase() {
		return storeDataInDatabase.getValue();
	}
	private static BooleanConfigValue storeDataInDatabase =
			new BooleanConfigValue("transitime.core.storeDataInDatabase",
					true,
					"When in playback mode or some other situations don't " +
					"want to store generated data such as arrivals/" +
					"departures, events, and such to the database because " +
					"only debugging.");
	
	/**
	 * When batching large amount of AVL data through system to generate
	 * improved schedule time (as has been done for Zhengzhou) it takes huge
	 * amount of time to process everything. To speed things up you can set
	 * -Dtransitime.core.onlyNeedArrivalDepartures=true such that the system
	 * will be sped up by not generating nor logging predictions, not logging
	 * AVL data nor storing it in db, and not logging nor storing match data in
	 * db.
	 * 
	 * @return
	 */
	public static boolean onlyNeedArrivalDepartures() {
		return onlyNeedArrivalDepartures.getValue();
	}
	private static BooleanConfigValue onlyNeedArrivalDepartures =
			new BooleanConfigValue("transitime.core.onlyNeedArrivalDepartures",
					false,
					"When batching large amount of AVL data through system " +
					"to generate improved schedule time (as has been done " +
					"for Zhengzhou) it takes huge amount of time to process " +
					"everything. To speed things up you can set " +
					"-Dtransitime.core.onlyNeedArrivalDepartures=true such " +
					"that the system will be sped up by not generating nor " +
					"logging predictions, not logging AVL data nor storing " +
					"it in db, and not logging nor storing match data in db.");

	/**
	 * When in batch mode can flood db with lots of objects. If 
	 * transitime.core.pauseIfDbQueueFilling is set to true then when objects
	 * are put into the DataDbLogger queue the calling thread will be
	 * temporarily suspended so that the separate thread can run to write
	 * to the db and thereby empty out the queue.
	 * @return
	 */
	public static boolean pauseIfDbQueueFilling() {
		return pauseIfDbQueueFilling.getValue();
	}
	private static BooleanConfigValue pauseIfDbQueueFilling =
			new BooleanConfigValue("transitime.core.pauseIfDbQueueFilling", 
					false,
					"When in batch mode can flood db with lots of objects. If" +
					"transitime.core.pauseIfDbQueueFilling is set to true " +
					"then when objects are put into the DataDbLogger queue " +
					"the calling thread will be temporarily suspended so " +
					"that the separate thread can run to write to the db and " +
					"thereby empty out the queue.");
	
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
					"/REPOSITORY/PROJECT/src/main/config/hibernate.cfg.xml",
					"So that have flexibility with where the hibernate " +
					"config file is. This way can easily access it within " +
					"Eclipse.");
	
	/**
	 * The semicolon separated list of names of all of the modules that should
	 * be automatically started.
	 * 
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
						optionalModulesDefaultList,
						"The semicolon separated list of names of all of the " +
						"modules that should be automatically started.");
	
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
			new DoubleConfigValue("transitime.core.maxDistanceFromSegment", 
					60.0,
					"How far a location can be from a path segment and still " +
					"be considered a match.");
	
	/**
	 * How many bad spatial/temporal matches a predictable vehicle can have in a
	 * row before the vehicle is made unpredictable.
	 * 
	 * @return
	 */
	public static int getAllowableNumberOfBadMatches() {
		return allowableNumberOfBadMatches.getValue();
	}
	private static IntegerConfigValue allowableNumberOfBadMatches =
			new IntegerConfigValue("transitime.core.allowableNumberOfBadMatches", 
					2,
					"How many bad spatial/temporal matches a predictable " +
					"vehicle can have in a row before the vehicle is made " +
					"unpredictable.");
			
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
			new FloatConfigValue("transitime.core.maxHeadingOffsetFromSegment", 
					135.0f,
					"How far heading in degrees of vehicle can be away from " +
					"path segment and still be considered a match. Needs to " +
					"be pretty lenient because stopPaths and heading might " +
					"not be that accurate.");

	/**
	 * For initial matching of vehicle to block assignment. If vehicle is closer
	 * than this distance from the end of the block then the spatial match will
	 * not be used. This is to prevent a vehicle that has already completed its
	 * block from wrongly being assigned to that block again.
	 * 
	 * @return
	 */
	public static double getDistanceFromEndOfBlockForInitialMatching() {
		return distanceFromEndOfBlockForInitialMatching.getValue();
	}
	private static DoubleConfigValue distanceFromEndOfBlockForInitialMatching = 
			new DoubleConfigValue(
					"transitime.core.distanceFromEndOfBlockForInitialMatching", 
					250.0,
					"For initial matching of vehicle to block assignment. If " +
					"vehicle is closer than this distance from the end of " +
					"the block then the spatial match will not be used. This " +
					"is to prevent a vehicle that has already completed its " +
					"block from wrongly being assigned to that block again.");

	/**
	 * How close vehicle needs to be from the last stop of the block such that
	 * the next AVL report should possibly be considered to match to the end of
	 * the block. This is important for determining the arrival time at the last
	 * stop of the block even if don't get an AVL report near that stop.
	 * 
	 * @return
	 */
	public static double getDistanceFromLastStopForEndMatching() {
		return distanceFromLastStopForEndMatching.getValue();
	}
	private static DoubleConfigValue distanceFromLastStopForEndMatching = 
			new DoubleConfigValue(
					"transitime.core.distanceFromLastStopForEndMatching", 
					250.0,
					"How close vehicle needs to be from the last stop of the " +
					"block such that the next AVL report should possibly be " +
					"considered to match to the end of the block. This is " +
					"important for determining the arrival time at the last" +
					"stop of the block even if don't get an AVL report near " +
					"that stop.");
	
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
			new FloatConfigValue("transitime.core.deadheadingShortVersusLongDistance", 
					1000.0f,
					"For determining if enough time to deadhead to beginning " +
					"of a trip. If vehicles are far away then they are more " +
					"likely to be able to travel faster because they could " +
					"take a freeway or other fast road. But when get closer " +
					"then will be on regular streets and will travel more " +
					"slowly. The parameters should be set in a conservative " +
					"way such that the travel time is underestimated by " +
					"using slower speeds than will actually encounter. This " +
					"way the vehicle will arrive after the predicted time " +
					"which means that passenger won't miss the bus.");

	public static float getShortDistanceDeadheadingSpeed() {
		return shortDistanceDeadheadingSpeed.getValue();
	}
	private static FloatConfigValue shortDistanceDeadheadingSpeed =
			new FloatConfigValue("transitime.core.shortDistanceDeadheadingSpeed", 
					4.0f, // 4.0m/s is about 8 mph
					"Part of determining if enough time to deadhead to layover.");
	
	public static float getLongDistanceDeadheadingSpeed() {
		return longDistanceDeadheadingSpeed.getValue();
	}
	private static FloatConfigValue longDistanceDeadheadingSpeed =
			new FloatConfigValue("transitime.core.longDistanceDeadheadingSpeed", 
					10.0f, // 10.0m/s is about 20mph
					"Part of determining if enough time to deadhead to layover.");

	/**
	 **************************************************************************
	 * For predictions
	 */
	
	/**
	 * How far forward into the future should generate predictions for.
	 * 
	 * @return
	 */
	public static int getMaxPredictionsTimeSecs() {
		return maxPredictionsTimeSecs.getValue();
	}
	public static int getMaxPredictionsTimeMsecs() {
		return getMaxPredictionsTimeSecs() * Time.MS_PER_SEC;
	}
	private static IntegerConfigValue maxPredictionsTimeSecs =
			new IntegerConfigValue("transitime.core.maxPredictionsTimeSecs", 
					45 * Time.SEC_PER_MIN,
					"How far forward into the future should generate " +
					"predictions for.");
	
	/**
	 * For specifying whether to use arrival predictions or departure
	 * predictions for normal, non-wait time, stops.
	 * 
	 * @return
	 */
	public static boolean getUseArrivalPredictionsForNormalStops() {
		return useArrivalPredictionsForNormalStops.getValue();
	}
	private static BooleanConfigValue useArrivalPredictionsForNormalStops =
			new BooleanConfigValue("transitime.core.useArrivalPredictionsForNormalStops", 
					true,
					"For specifying whether to use arrival predictions or " +
					"departure predictions for normal, non-wait time, stops.");
	
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
					0*Time.SEC_PER_MIN,
					"For determining if prediction should be stored in db. " +
					"Set to 0 to never store predictions. A very large " +
					"number of predictions is created so be careful with " +
					"this value so that the db doesn't get filled up too " +
					"quickly.");
	
	/**
	 * How early a vehicle can be and still be matched to a layover. Needs to 
	 * be pretty large because sometimes vehicles will be assigned to a layover
	 * quite early, and want to be able to make the vehicle predictable and 
	 * generate predictions far in advance. Don't want it to be too large, like
	 * 90 minutes, though because then could match incorrectly if vehicle
	 * simply stays at terminal.
	 * @return
	 */
	public static int getAllowableEarlyForLayoverSeconds() {
		return allowableEarlyForLayoverSeconds.getValue();
	}
	private static IntegerConfigValue allowableEarlyForLayoverSeconds = 
			new IntegerConfigValue("transitime.core.allowableEarlyForLayoverSeconds", 
					60 * Time.SEC_PER_MIN,
					"How early a vehicle can be and still be matched to a " +
					"layover. Needs to be pretty large because sometimes " +
					"vehicles will be assigned to a layover quite early, " +
					"and want to be able to make the vehicle predictable and " +
					"generate predictions far in advance. Don't want it to " +
					"be too large, like 90 minutes, though because then " +
					"could match incorrectly if vehicle simply stays at " +
					"terminal.");

	/**
	 * How early a vehicle can be and still be matched to its block assignment.
	 * If when a new AVL report is received for a predictable vehicle and it is
	 * found with respect to the real-time schedule adherence to be earlier than
	 * this value the vehicle will be made unpredictable.
	 * 
	 * @return
	 */
	public static int getAllowableEarlySeconds() {
		return allowableEarlySeconds.getValue();
	}
	private static IntegerConfigValue allowableEarlySeconds = 
			new IntegerConfigValue("transitime.core.allowableEarlySeconds", 
					15 * Time.SEC_PER_MIN,
					"How early a vehicle can be and still be matched to its " +
					"block assignment. If when a new AVL report is received " +
					"for a predictable vehicle and it is found with respect " +
					"to the real-time schedule adherence to be earlier than " +
					"this value the vehicle will be made unpredictable.");

	/**
	 * How late a vehicle can be and still be matched to its block assignment.
	 * If when a new AVL report is received for a predictable vehicle and it is
	 * found with respect to the real-time schedule adherence to be later than
	 * this value the vehicle will be made unpredictable.
	 * 
	 * @return
	 */
	public static int getAllowableLateSeconds() {
		return allowableLateSeconds.getValue();
	}
	private static IntegerConfigValue allowableLateSeconds = 
			new IntegerConfigValue("transitime.core.allowableLateSeconds", 
					90 * Time.SEC_PER_MIN,
					"How late a vehicle can be and still be matched to its " +
					"block assignment. If when a new AVL report is received " +
					"for a predictable vehicle and it is found with respect " +
					"to the real-time schedule adherence to be later than " +
					"this value the vehicle will be made unpredictable.");

	/**
	 * How early a vehicle can be and still be matched to its block assignment.
	 * 
	 * @return time in seconds
	 */
	public static int getAllowableEarlySecondsForInitialMatching() {
		return allowableEarlySecondsForInitialMatching.getValue();
	}
	private static IntegerConfigValue allowableEarlySecondsForInitialMatching = 
			new IntegerConfigValue("transitime.core.allowableEarlySecondsForInitialMatching", 
					10 * Time.SEC_PER_MIN,
					"How early a vehicle can be in seconds and still be " +
					"matched to its block assignment.");

	/**
	 * How late a vehicle can be and still be matched to its block assignment.
	 * 
	 * @return time in seconds
	 */
	public static int getAllowableLateSecondsForInitialMatching() {
		return allowableLateSecondsForInitialMatching.getValue();
	}
	private static IntegerConfigValue allowableLateSecondsForInitialMatching = 
			new IntegerConfigValue("transitime.core.allowableLateSecondsForInitialMatching", 
					20 * Time.SEC_PER_MIN,
					"How late a vehicle can be in seconds and still be " +
					"matched to its block assignment.");

	/**
	 * For initial matching. If the spatial match is for a layover then the
	 * temporal match is biased by this amount in order to make match to
	 * non-layover more likely. This prevents wrongly matching vehicles to 
	 * layovers when they are running late.
	 * 
	 * @return layover bias in seconds
	 */
	public static int getLayoverBiasForInitialMatching() {
		return layoverBiasSecondsForInitialMatching.getValue();
	}
	private static IntegerConfigValue layoverBiasSecondsForInitialMatching = 
			new IntegerConfigValue("transitime.core.layoverBiasSecondsForInitialMatching", 
					20 * Time.SEC_PER_MIN,
					"For initial matching. If the spatial match is for a " +
					"layover then the temporal match is biased by this " +
					"amount in order to make match to non-layover more " +
					"likely. This prevents wrongly matching vehicles to " +
					"layovers when they are running late.");
	
	/**
	 * For initial matching vehicle to assignment when there isn't any heading
	 * information. In that case also want to match to previous AVL report.
	 * This parameter specifies how far, as the crow flies, the previous AVL
	 * report to be used from the VehicleState AvlReport history is from the
	 * current AvlReport.
	 * @return
	 */
	public static double getDistanceBetweenAvlsForInitialMatchingWithoutHeading() {
		return distanceBetweenAvlsForInitialMatchingWithoutHeading.getValue();
	}
	private static DoubleConfigValue distanceBetweenAvlsForInitialMatchingWithoutHeading =
			new DoubleConfigValue("transitime.core.distanceBetweenAvlsForInitialMatchingWithoutHeading",
					100.0,
					"For initial matching vehicle to assignment when there " +
					"isn't any heading information. In that case also want " +
					"to match to previous AVL report. This parameter " +
					"specifies how far, as the crow flies, the previous AVL " +
					"report to be used from the VehicleState AvlReport " +
					"history is from the current AvlReport.");
					
	/**
	 * How far along path past a layover a vehicle can spatially match but still
	 * be considered to be at that layover. Important for determining
	 * predictions and such.
	 * 
	 * @return
	 */
	public static double getDistanceAtWhichStillAtLayover() {
		return distanceAtWhichStillAtLayover.getValue();
	}
	private static DoubleConfigValue distanceAtWhichStillAtLayover =
			new DoubleConfigValue("transitime.core.distanceAtWhichStillAtLayover", 
					100.0,
					"How far along path past a layover a vehicle can " +
					"spatially match but still be considered to be at that " +
					"layover. Important for determining predictions and such.");
	
	/**
	 * How far a vehicle can be ahead of a stop in meters and be considered to
	 * have arrived.
	 * 
	 * @return
	 */
	public static double getBeforeStopDistance() {
		return beforeStopDistance.getValue();
	}
	private static DoubleConfigValue beforeStopDistance =
			new DoubleConfigValue("transitime.core.beforeStopDistance", 
					50.0,
					"How far a vehicle can be ahead of a stop in meters and " +
					"be considered to have arrived.");
	
	/**
	 * How far a vehicle can be past a stop in meters and still be considered at
	 * the stop.
	 * 
	 * @return
	 */
	public static double getAfterStopDistance() {
		return afterStopDistance.getValue();
	}
	private static DoubleConfigValue afterStopDistance =
			new DoubleConfigValue("transitime.core.afterStopDistance", 
					50.0,
					"How far a vehicle can be past a stop in meters and " +
					"still be considered at the stop.");
	
	/**
	 * How far a vehicle can be past a stop in meters and still be considered at
	 * the stop.
	 * 
	 * @return
	 */
	public static int getDefaultBreakTimeSec() {
		return defaultBreakTimeSec.getValue();
	}
	private static IntegerConfigValue defaultBreakTimeSec =
			new IntegerConfigValue("transitime.core.defaultBreakTimeSec", 
					0,
					"How far a vehicle can be past a stop in meters and " +
					"still be considered at the stop.");
	
	/**
	 * How much worse it is for a vehicle to be early as opposed to late when
	 * determining schedule adherence.
	 * 
	 * @return
	 */
	public static double getEarlyToLateRatio() {
		return earlyToLateRatio.getValue();
	}
	private static DoubleConfigValue earlyToLateRatio =
			new DoubleConfigValue("transitime.core.earlyToLateRatio", 
					3.0,
					"How much worse it is for a vehicle to be early as " +
					"opposed to late when determining schedule adherence.");

}
