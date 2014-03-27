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
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.jms.JMSException;
import javax.naming.NamingException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;

import org.transitime.avl.AvlJmsClient;
import org.transitime.avl.BatchGtfsRealtimeModule;
import org.transitime.avl.PlaybackModule;
import org.transitime.config.Config;
import org.transitime.configData.AvlConfig;
import org.transitime.configData.CoreConfig;
import org.transitime.core.Service;
import org.transitime.core.dataCache.PredictionDataCache;
import org.transitime.core.dataCache.VehicleDataCache;
import org.transitime.db.hibernate.DataDbLogger;
import org.transitime.gtfs.DbConfig;
import org.transitime.ipc.servers.ConfigServer;
import org.transitime.ipc.servers.PredictionsServer;
import org.transitime.ipc.servers.VehiclesServer;
import org.transitime.modules.Module;
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

	private final Service service;
	private final Time time;

	// So that can access the current time, even when in playback mode
	private SystemTime systemTime = new SystemCurrentTime();
	
	// Set by command line option. Specifies which config file to read in. 
	private static String configFile = null;
	
	// Set by command line option. If not null then in 
	// playback mode and should get AVL data
	// from the database instead of from AVL feed.
	private static Date playbackStartTime = null;
	
	// Set by command line option. If in playback mode 
	// and playbackVehicle is set then will 
	// playback for only for specified vehicle.
	private static String playbackVehicle = null;
	
	// Set by command line option. Indicates if instead 
	// of using realtime AVL feed should instead
	// get AVL data from a batch GTFS-realtime file.
	private static boolean batchGtfsRealtimeMode = false;
	
	private static final Logger logger = 
			LoggerFactory.getLogger(Core.class);
	
	/********************** Member Functions **************************/

	/**
	 * Construct the Core object and read in the config data. This is private
	 * so that the createCore() factory method must be used.
	 * 
	 * @param projectId
	 */
	private Core(String projectId) {
		// Create the DataDBLogger so that generated data can be stored
		// to database via a robust queue. But don't actually log data
		// if in playback mode since then would be writing data again 
		// that was first written when predictor was run in real time.
		dataDbLogger = DataDbLogger.getDataDbLogger(projectId, inPlaybackMode());
		
		// Read in all config data
		configData = new DbConfig(projectId);
		// FIXME Use rev 0 for now but should be using current rev
		int configRev = 0;
		configData.read(configRev);
		
		// Set the timezone so that when dates are logged the time
		// is correct. Valid timezone format is at 
		// http://en.wikipedia.org/wiki/List_of_tz_zones
		String timezoneStr = configData.getFirstAgency().getTimeZoneStr();
		TimeZone.setDefault(TimeZone.getTimeZone(timezoneStr));

		service = new Service(configData);
		time = new Time(configData);
	}
	
	/**
	 * Creates the Core object for the application. There can only be one Core
	 * object per application.
	 * 
	 * @param projectId
	 * @return
	 */
	public static Core createCore(String projectId) {
		// Make sure only can have a single Core object
		if (Core.singleton != null) {
			logger.error("Core singleton already created. Cannot create another one.");
			return null;
		}
		
		Core core = new Core(projectId);
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
	 * Returns the Service object that can be reused for efficiency.
	 * @return
	 */
	public Service getService() {
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
	    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
	    StatusPrinter.print(lc);
		
	}
	
	/**
	 * Returns true if in playback mode, indicating that shouldn't be
	 * running AVL feeds nor logging data to db.
	 * @return
	 */
	public static boolean inPlaybackMode() {
		return playbackStartTime != null;
	}
	
	/**
	 * Returns true if in batch GTFS-realtime mode. This means that
	 * shouldn't be running realtime AVL feeds. Instead, the AVL data will
	 * come from a single batch file. Useful for the World Bank project
	 * where want to determine departure times from historic GPS data
	 * and update the schedule times in the GTFS stop_times.txt file to
	 * make them more accurate.
	 * @return
	 */
	public static boolean inBatchGtfsRealtimeMode() {
		return batchGtfsRealtimeMode;
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
		
		options.addOption(OptionBuilder.withArgName("\"MM-dd-yyyy HH:mm:ss z\"")
                .hasArg()
                .withDescription("For playback. Specifies what time play should start. " + 
                		"Format is \"MM-dd-yyyy HH:mm:ss.SSS z\", " +
                		"such as \"9-20-2014 17:00:00.021 EST\".")
                .withLongOpt("time")
                .create("t")
                );

		options.addOption(OptionBuilder.withArgName("vehicleId")
                .hasArg()
                .withDescription("For playback. Specifies which vehicle to playback.")
                .withLongOpt("vehicle")
                .create("v")
                );
		
		options.addOption("b", "batchGtfsRealtimeMode", false, 
				"Specifies that instead of regular AVL feed should use a " +
				"batch GTFS-realtime file.");
		
		// Parse the options
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse( options, args);
		
		// Handle config file option
		if (cmd.hasOption("c")) {
			configFile = cmd.getOptionValue("c");
		}
		
		// Handle date/time option for playback
		if (cmd.hasOption("t")) {
			String datetimeStr = cmd.getOptionValue("t");
			try {
				playbackStartTime = Time.parse(datetimeStr);
				
				// If specified time is in the future then reject.
				if (playbackStartTime.getTime() > System.currentTimeMillis()) {
					System.err.println("Paramater -t \"" + datetimeStr + 
							"\" is in the future and therefore invalid!");
					System.exit(-1);					
				}
					
			} catch (java.text.ParseException e) {
				System.err.println("Paramater -t \"" + datetimeStr + 
						"\" could not be parsed. Format must be \"MM-dd-yyyy HH:mm:ss\"");
				System.exit(-1);
			}
		}
		
		// Handle vehicle option for playback
		if (cmd.hasOption("v")) {
			playbackVehicle = cmd.getOptionValue("v");
		}
		
		// Handle batch GTFS-realtime data mode
		if (cmd.hasOption("batchGtfsRealtimeMode")) {
			batchGtfsRealtimeMode = true;
		}
		
		// Handle help option
		if (cmd.hasOption("h")) {
			// Display help
			final String commandLineSyntax = "java TransitimeCore.jar";
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
	 * @param projectId
	 */
	private static void startRmiServers(String projectId) {
		// Start up all of the RMI servers
		PredictionsServer.start(projectId, PredictionDataCache.getInstance());
		VehiclesServer.start(projectId, VehicleDataCache.getInstance());
		ConfigServer.start(projectId);
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
			
			String projectId = CoreConfig.getProjectId();
			Core core = createCore(projectId);
			
			if (inPlaybackMode()) {
				// Get AVL data from database
				PlaybackModule playbackModule = 
						new PlaybackModule(projectId, playbackStartTime, 
								playbackVehicle);
				core.systemTime = playbackModule.getSystemTime();
				playbackModule.start();
			} else if (inBatchGtfsRealtimeMode()) {
				// Get AVL data from batch GTFS-realtime file
				(new BatchGtfsRealtimeModule(projectId)).start();				
			} else {
				// Not in playback or batch mode so need to get AVL data through JMS feed.
				// Start the AVL Client threads which read in the AVL data from JMS
				// and processes it to generate predictions and all other data.
				try {
					AvlJmsClient.start(projectId, AvlConfig.getAvlQueueSize(), 
							AvlConfig.getNumAvlThreads());
				} catch (JMSException e) {
					logger.error("Exception when starting AvlJmsClient", e);
				} catch (NamingException e) {
					logger.error("Exception when starting AvlJmsClient", e);
				}
			}
			
			// Start any optional modules. 
			// For how CoreConfig default modules includes the NextBus AVL feed
			// module and the default projectId is sf-muni. So will automatically
			// start reading data for sf-muni.
			List<String> optionalModuleNames = CoreConfig.getOptionalModules();
			if (optionalModuleNames.size() > 0)
				logger.info("Starting up optional modules specified via " + 
						"transitest.modules.optionalModulesList param:");
			else
				logger.info("No optional modules to start up.");
			for (String moduleName : optionalModuleNames) {
				logger.info("Starting up optional module " + moduleName);
				Module.start(moduleName);
			}	
			
			// Start the RMI Servers so that clients can obtain data
			// on predictions, vehicles locations, etc.
			startRmiServers(projectId);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}
