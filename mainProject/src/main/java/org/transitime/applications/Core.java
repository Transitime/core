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
package org.transitime.applications;

import java.io.PrintWriter;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.Config;
import org.transitime.configData.CoreConfig;
import org.transitime.core.ServiceUtils;
import org.transitime.core.dataCache.PredictionDataCache;
import org.transitime.core.dataCache.VehicleDataCache;
import org.transitime.db.hibernate.DataDbLogger;
import org.transitime.gtfs.DbConfig;
import org.transitime.ipc.servers.ConfigServer;
import org.transitime.ipc.servers.PredictionsServer;
import org.transitime.ipc.servers.ServerStatusServer;
import org.transitime.ipc.servers.VehiclesServer;
import org.transitime.modules.Module;
import org.transitime.utils.SettableSystemTime;
import org.transitime.utils.SystemTime;
import org.transitime.utils.SystemCurrentTime;
import org.transitime.utils.Time;

/**
 * The main class for running a Transitime Core real-time data processing
 * system. Handles command line arguments and then initiates AVL feed.
 * 
 * @author SkiBu Smith
 * 
 */
public class Core {
	
	private static Core singleton = null;
	
	// Contains the configuration data read from database
	private final DbConfig configData;
	
	// For logging data such as AVL reports and arrival times to database
	private final DataDbLogger dataDbLogger;

	private final ServiceUtils service;
	private final Time time;

	// So that can access the current time, even when in playback mode
	private SystemTime systemTime = new SystemCurrentTime();
	
	// Set by command line option. Specifies which config file to read in. 
	private static String configFile = null;
		
	private static final Logger logger = 
			LoggerFactory.getLogger(Core.class);
	
	/********************** Member Functions **************************/

	/**
	 * Construct the Core object and read in the config data. This is private
	 * so that the createCore() factory method must be used.
	 * 
	 * @param agencyId
	 */
	private Core(String agencyId) {
		// Read in all GTFS based config data from the database
		configData = new DbConfig(agencyId);
		// FIXME Use rev 0 for now but should be using current rev
		int configRev = 0;
		configData.read(configRev);
		
		// Set the timezone so that when dates are logged the time
		// is correct. Valid timezone format is at 
		// http://en.wikipedia.org/wiki/List_of_tz_zones
		String timezoneStr = configData.getFirstAgency().getTimeZoneStr();
		TimeZone.setDefault(TimeZone.getTimeZone(timezoneStr));

		// Create the DataDBLogger so that generated data can be stored
		// to database via a robust queue. But don't actually log data
		// if in playback mode since then would be writing data again 
		// that was first written when predictor was run in real time.
		// Note: DataDbLogger needs to be started after the timezone is set.
		// Otherwise when running for a different timezone than what the
		// computer is setup for then can log data using the wrong time!
		// This is strange since setting TimeZone.setDefault() is supposed
		// to work across all threads it appears that sometimes it wouldn't
		// work if Db logger started first.
		dataDbLogger = DataDbLogger.getDataDbLogger(agencyId,
				CoreConfig.storeDataInDatabase(),
				CoreConfig.pauseIfDbQueueFilling());
		
		service = new ServiceUtils(configData);
		time = new Time(configData);
	}
	
	/**
	 * Creates the Core object for the application. There can only be one Core
	 * object per application.
	 * 
	 * @param agencyId
	 * @return
	 */
	public static Core createCore(String agencyId) {
		// Make sure only can have a single Core object
		if (Core.singleton != null) {
			logger.error("Core singleton already created. Cannot create another one.");
			return null;
		}
		
		Core core = new Core(agencyId);
		Core.singleton = core;
		return core;
	}
	
	/**
	 * @returns the Core singleton object for this application.
	 */ 
	public static Core getInstance() {
		return singleton;
	}
	
	/**
	 * Makes the config data available to all
	 * @return
	 */
	public DbConfig getDbConfig() {
		return configData;
	}
	
	/**
	 * Returns the ServiceUtils object that can be reused for efficiency.
	 * @return
	 */
	public ServiceUtils getServiceUtils() {
		return service;
	}
	
	/**
	 * For when want to use methods in Time. This is important when need
	 * methods that access a Calendar a lot. By putting the Calendar in
	 * Time it can be shared.
	 * @return
	 */
	public Time getTime() {
		return time;
	}
	
	/**
	 * For when need system time but might be in playback mode
	 * @return
	 */
	public SystemTime getSystemTime() {
		return systemTime;
	}
	
	/**
	 * For setting the system time when in playback or batch mode.
	 * 
	 * @param systemTime
	 */
	public void setSystemTime(long systemEpochTime) {
		this.systemTime = new SettableSystemTime(systemEpochTime);
	}
	
	/**
	 * Returns the Core logger so that each class doesn't need to create
	 * its own and have it be configured properly.
	 * @return
	 */
	public static final Logger getLogger() {
		return logger;
	}
	
	/**
	 * This method logs status of the logger system to the console. Could 
	 * be useful for seeing if there are problems with the logger config file.
	 */
	private static void outputLoggerStatus() {
		// For debugging output current state of logger
// Commented out for now because not truly useful
//	    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
//	    StatusPrinter.print(lc);		
	}
	
	/**
	 * Returns the DataDbLogger for logging data to db.
	 * @return
	 */
	public DataDbLogger getDbLogger() {
		return dataDbLogger;
	}
	
	/**
	 * Processes all command line options using Apache CLI.
	 * Further info at http://commons.apache.org/proper/commons-cli/usage.html
	 */
	@SuppressWarnings("static-access")  // Needed for using OptionBuilder
	private static void processCommandLineOptions(String[] args) throws ParseException {
		// Specify the options
		Options options = new Options();
		options.addOption("h", "help", false, "Display usage and help info."); 
		
		options.addOption(OptionBuilder.withArgName("configFile")
                .hasArg()
                .withDescription("Specifies configuration file to read in.")
                .withLongOpt("config")
                .create("c")
                );
		
		// Parse the options
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse( options, args);
		
		// Handle config file option
		if (cmd.hasOption("c")) {
			configFile = cmd.getOptionValue("c");
		}
								
		// Handle help option
		if (cmd.hasOption("h")) {
			// Display help
			final String commandLineSyntax = "java transitime.jar";
			final PrintWriter writer = new PrintWriter(System.out);
			final HelpFormatter helpFormatter = new HelpFormatter();
			helpFormatter.printHelp(writer,
									80, // printedRowWidth
									commandLineSyntax,
									"args:", // header
									options,
									2,             // spacesBeforeOption
									2,             // spacesBeforeOptionDescription
									null,          // footer
									true);         // displayUsage
			writer.close();
			System.exit(0);
		}
	}
	
	/*
	 * Start the RMI Servers so that clients can obtain data
	 * on predictions, vehicles locations, etc.
	 *  
	 * @param agencyId
	 */
	private static void startRmiServers(String agencyId) {
		// Start up all of the RMI servers
		PredictionsServer.start(agencyId, PredictionDataCache.getInstance());
		VehiclesServer.start(agencyId, VehicleDataCache.getInstance());
		ConfigServer.start(agencyId);
		ServerStatusServer.start(agencyId);
	}
	
	/**
	 * The main program that runs the entire Transitime application.!
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			try {
				processCommandLineOptions(args);
			} catch (ParseException e1) {
				e1.printStackTrace();
				System.exit(-1);
			}
			
			// For making sure logger configured properly
			outputLoggerStatus();
			
			// Read in config params
			try {
				// Read in the data from config file
				Config.processConfig(configFile);
			} catch (Exception e) {
				logger.error("Error reading in config file \"" + configFile + 
						"\". Exiting program.", e);
				System.exit(-1);
			}
			
			String agencyId = CoreConfig.getAgencyId();
			createCore(agencyId);
			
			// Start any optional modules. 
			// For how CoreConfig default modules includes the NextBus AVL feed
			// module and the default agencyId is sfmta. So will automatically
			// start reading data for sfmta.
			List<String> optionalModuleNames = CoreConfig.getOptionalModules();
			if (optionalModuleNames.size() > 0)
				logger.info("Starting up optional modules specified via " + 
						"transitime.modules.optionalModulesList param:");
			else
				logger.info("No optional modules to start up.");
			for (String moduleName : optionalModuleNames) {
				logger.info("Starting up optional module " + moduleName);
				Module.start(moduleName);
			}	
			
			// Start the RMI Servers so that clients can obtain data
			// on predictions, vehicles locations, etc.
			startRmiServers(agencyId);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}
