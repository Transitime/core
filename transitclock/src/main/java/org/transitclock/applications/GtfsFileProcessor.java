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
package org.transitclock.applications;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.ConfigFileReader;
import org.transitclock.configData.AgencyConfig;
import org.transitclock.gtfs.GtfsData;
import org.transitclock.gtfs.HttpGetGtfsFile;
import org.transitclock.gtfs.TitleFormatter;
import org.transitclock.gtfs.gtfsStructs.GtfsAgency;
import org.transitclock.gtfs.readers.GtfsAgencyReader;
import org.transitclock.utils.Time;
import org.transitclock.utils.Zip;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Reads GTFS files, validates and cleans up the data, stores the data into Java
 * objects, and then stores those objects into the database.
 * 
 * There are a good number of options. Therefore there are addOption methods so
 * don't have a constructor with a large number of parameters.
 * 
 * @author SkiBu Smith
 *
 */
public class GtfsFileProcessor {

	// Optional command line info used within this class
	private final String gtfsUrl;
	private String gtfsZipFileName;
	// Last modified time of GTFS zip file. Null if zip file not used.
	private Date zipFileLastModifiedTime;
	private final String unzipSubdirectory;
	private String gtfsDirectoryName;
	private String feedVersion;

	// Optional command line info used for GtfsData class
	private final String supplementDir;
	private final String regexReplaceListFileName;
	private final double pathOffsetDistance;
	private final double maxStopToPathDistance;
	private final double maxDistanceForEliminatingVertices;
	private final int defaultWaitTimeAtStopMsec;
	private final double maxSpeedKph;
	private final double maxTravelTimeSegmentLength;
	private final int configRev;
	private final boolean shouldStoreNewRevs;
	private final boolean shouldDeleteRevs;
	private final String notes;
	private final boolean trimPathBeforeFirstStopOfTrip;

	// Read in configuration files. This should be done statically before
	// the logback LoggerFactory.getLogger() is called so that logback can
	// also be configured using a transitime config file.
	static {
		ConfigFileReader.processConfig();
	}

	// Logging important in this class
	private static final Logger logger = LoggerFactory
			.getLogger(GtfsFileProcessor.class);

	/******************* Constructor ***********************/

	/**
	 * Simple constructor. Stores the configurable parameters for this class.
	 * In main project, only used by createGtfsFileProcessor().
	 * 
	 * @param configFile
	 * @param notes
	 * @param gtfsUrl
	 * @param gtfsZipFileName
	 * @param unzipSubdirectory
	 * @param gtfsDirectoryName
	 * @param supplementDir
	 * @param regexReplaceListFileName
	 * @param pathOffsetDistance
	 * @param maxStopToPathDistance
	 * @param maxDistanceForEliminatingVertices
	 * @param defaultWaitTimeAtStopMsec
	 * @param maxTravelTimeSegmentLength
	 * @param configRev
	 *            If not -1 then will use this config rev instead of
	 *            incrementing from the current config rev from the db
	 * @param shouldStoreNewRevs
	 *            If true then will store the new config and travel times revs
	 *            into ActiveRevisions table in db
	 */
	public GtfsFileProcessor(String configFile, String notes, String gtfsUrl,
			String gtfsZipFileName, String unzipSubdirectory,
			String gtfsDirectoryName, String supplementDir,
			String regexReplaceListFileName, double pathOffsetDistance,
			double maxStopToPathDistance,
			double maxDistanceForEliminatingVertices,
			int defaultWaitTimeAtStopMsec, double maxSpeedKph,
			double maxTravelTimeSegmentLength,
			int configRev,
			boolean shouldStoreNewRevs, boolean shouldDeleteRevs, boolean trimPathBeforeFirstStopOfTrip) {
		// Read in config params if command line option specified
		if (configFile != null) {
			try {
				// Read in the data from config file
				ConfigFileReader.processConfig(configFile);
			} catch (Exception e) {
				logger.error("Error reading in config file \"" + configFile
						+ "\". Exiting program.", e);
				System.exit(-1);
			}
		}

		this.gtfsUrl = gtfsUrl;
		this.gtfsZipFileName = gtfsZipFileName;
		this.unzipSubdirectory = unzipSubdirectory;
		this.gtfsDirectoryName = gtfsDirectoryName;
		this.supplementDir = supplementDir;
		this.regexReplaceListFileName = regexReplaceListFileName;
		this.pathOffsetDistance = pathOffsetDistance;
		this.maxStopToPathDistance = maxStopToPathDistance;
		this.maxDistanceForEliminatingVertices =
				maxDistanceForEliminatingVertices;
		this.defaultWaitTimeAtStopMsec = defaultWaitTimeAtStopMsec;
		this.maxSpeedKph = maxSpeedKph;
		this.maxTravelTimeSegmentLength = maxTravelTimeSegmentLength;
		this.configRev = configRev;
		this.notes = notes;
		this.shouldStoreNewRevs = shouldStoreNewRevs;
		this.shouldDeleteRevs = shouldDeleteRevs;
		this.trimPathBeforeFirstStopOfTrip = trimPathBeforeFirstStopOfTrip;
	}

	/********************** Member Functions **************************/

	/**
	 * Gets the GTFS files ready. Reads the zip file from the web if necessary.
	 * Then unpacks the zip file if necessary. This method will make sure
	 * gtfsDirectoryName is set to where the unzipped files can be found.
	 * <p>
	 * Doesn't need to do anything if GTFS files are already unzipped and
	 * available locally.
	 */
	private void obtainGtfsFiles() throws IllegalArgumentException {
		// Make sure params are OK. One and only one of gtfsUrl,
		// gtfsZipFileName,
		// or gtfsDirectoryName should be set.
		if (gtfsUrl == null && gtfsZipFileName == null
				&& gtfsDirectoryName == null) {
			throw new IllegalArgumentException(
					"For GtfsFileProcessor must specify the addOptionGtfsUrl(), "
							+ "the addOptionGtfsZipFileName(), or the addOptionGtfsDirName() "
							+ "option.");
		}
		if (gtfsUrl != null && gtfsZipFileName != null) {
			throw new IllegalArgumentException(
					"For GtfsFileProcessor both the addOptionGtfsUrl() and the "
							+ "addOptionGtfsZipFileName() options were specified but only "
							+ "allowed to specify one or the other");
		}
		if (gtfsDirectoryName != null
				&& (gtfsUrl != null || gtfsZipFileName != null)) {
			throw new IllegalArgumentException(
					"For GtfsFileProcessor both the addOptionGtfsDirName() and "
							+ "the addOptionGtfsUrl() or addOptionGtfsZipFileName() options "
							+ "were specified but only allowed to specify one or the other");
		}

		// First need access to the zip file.
		// If URL set then should get the file from web and store it
		if (gtfsUrl != null) {
			gtfsZipFileName =
					HttpGetGtfsFile.getFile(AgencyConfig.getAgencyId(),
							gtfsUrl, unzipSubdirectory);
		}

		// Uncompress the GTFS zip file if need to
		if (gtfsZipFileName != null) {
			gtfsDirectoryName = Zip.unzip(gtfsZipFileName, unzipSubdirectory);

			zipFileLastModifiedTime =
					new Date(new File(gtfsZipFileName).lastModified());
		}
	}

	/**
	 * Cleans up GTFS files. Specifically, if unzipped a GTFS zip file then
	 * removes the .txt files from gtfsDirectoryName since they can be quite
	 * large and don't need them around anymore.
	 */
	private void cleanupGtfsFiles() {
		// Only need to cleanup if unzipped a zip file
		if (gtfsZipFileName == null)
			return;

		try {
			File f = new File(gtfsDirectoryName);
			String fileNames[] = f.list();
			for (String fileName : fileNames) {
				if (fileName.endsWith(".txt")) {
					Files.delete(Paths.get(gtfsDirectoryName, fileName));
				}
			}
		} catch (Exception e) {
			logger.error("Exception when cleaning up GTFS files", e);
		}
	}

	/**
	 * Sets timezone for the application so that times and dates will be written
	 * correctly to the database. This is especially important for calendar
	 * dates. Needs to be called before the db is first accessed in order to
	 * have an effect with postgres (with mysql can do so afterwards, which is
	 * strange). So needs to be done before GtfsData() object is constructed.
	 * <p>
	 * The timezone string is obtained from the agency.txt GTFS file.
	 * 
	 * @param gtfsDirectoryName Where to find the GTFS files
	 */
	private void setTimezone(String gtfsDirectoryName) {
		// Read in the agency.txt GTFS data from file
		GtfsAgencyReader agencyReader = new GtfsAgencyReader(gtfsDirectoryName);
		List<GtfsAgency> gtfsAgencies = agencyReader.get();
		if (gtfsAgencies.isEmpty()) {
			logger.error("Could not read in {}/agency.txt file, which is "
					+ "needed for createDateFormatter()", gtfsDirectoryName);
			System.exit(-1);
		}
		String timezoneName = gtfsAgencies.get(0).getAgencyTimezone();

		// Set system timezone so that dates and times will be written to db
		// properly
		TimeZone.setDefault(TimeZone.getTimeZone(timezoneName));
		logger.info("Set at beginning default timezone to {}", timezoneName);
	}

	/**
	 * Once the GtfsFileProcessor is constructed and the options have been set
	 * then this function is used to actually process the GTFS data and store it
	 * into the database.
	 */
	public void process() throws IllegalArgumentException {
		// Gets the GTFS files from URL or from a zip file if need be.
		// This also sets gtfsDirectoryName member
		obtainGtfsFiles();

		// Set the timezone of the application so that times and dates will be
		// written correctly to the database. This is especially important for
		// calendar dates. This has to be done after obtainGtfsFiles() so that
		// gtfsDirectoryName member is set. Needs to be done before the db is
		// first accessed in order to have an effect with postgres (with mysql
		// can do so afterwards, which is strange). So needs to be done before
		// GtfsData() object is constructed.
		setTimezone(gtfsDirectoryName);

		// Create a title formatter
		TitleFormatter titleFormatter =
				new TitleFormatter(regexReplaceListFileName, true);

		// Process the GTFS data
		GtfsData gtfsData =
				new GtfsData(configRev, notes, zipFileLastModifiedTime,
						shouldStoreNewRevs, shouldDeleteRevs,
						AgencyConfig.getAgencyId(),
						gtfsDirectoryName, supplementDir,
						pathOffsetDistance, maxStopToPathDistance,
						maxDistanceForEliminatingVertices,
						defaultWaitTimeAtStopMsec, maxSpeedKph,
						maxTravelTimeSegmentLength,
						trimPathBeforeFirstStopOfTrip, titleFormatter);
		
		gtfsData.processData();

		// Log possibly useful info
		titleFormatter.logRegexesThatDidNotMakeDifference();

		// Do any necessary cleanup
		cleanupGtfsFiles();
	}

	/**
	 * Displays the command line options on stdout
	 * 
	 * @param options
	 */
	private static void displayCommandLineOptions(Options options) {
		// Display help
		final String commandLineSyntax = "java transitclock.jar";
		final PrintWriter writer = new PrintWriter(System.out);
		final HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.printHelp(writer, 80, // printedRowWidth
				commandLineSyntax, "args:", // header
				options, 2, // spacesBeforeOption
				2, // spacesBeforeOptionDescription
				null, // footer
				true); // displayUsage
		writer.close();
	}

	/**
	 * Handles command line option for doubles. If the option is not set or
	 * cannot be parsed then the default value is used.
	 * 
	 * @param commandLineOption
	 * @param defaultValue
	 * @param commandLineArgs
	 * @return
	 */
	private static double getDoubleCommandLineOption(String commandLineOption,
			double defaultValue, CommandLine commandLineArgs) {
		String valueStr = commandLineArgs.getOptionValue(commandLineOption);
		if (valueStr != null) {
			try {
				return Double.parseDouble(valueStr);
			} catch (NumberFormatException e) {
				logger.error(
						"The parameter {} was set to {} which is not a "
								+ "valid number. The default value of {} will be used.",
						"-" + commandLineOption, valueStr, defaultValue);
			}
		}

		return defaultValue;
	}

	/**
	 * Handles command line option for an int. If the option is not set or
	 * cannot be parsed then the default value is used.
	 * 
	 * @param commandLineOption
	 * @param defaultValue
	 * @param commandLineArgs
	 * @return
	 */
	private static int getIntegerCommandLineOption(String commandLineOption,
			int defaultValue, CommandLine commandLineArgs) {
		String valueStr = commandLineArgs.getOptionValue(commandLineOption);
		if (valueStr != null) {
			try {
				return Integer.parseInt(valueStr);
			} catch (NumberFormatException e) {
				logger.error(
						"The parameter {} was set to {} which is not a "
								+ "valid number. The default value of {} will be used.",
						"-" + commandLineOption, valueStr, defaultValue);
			}
		}

		return defaultValue;
	}

	/**
	 * Uses the command line args to fully configure a GtfsFileProcessor object.
	 * This is where defaults go for command line options.
	 * 
	 * @param commandLineArgs
	 * @return Fully configured GtfsFileProcessor object
	 */
	private static GtfsFileProcessor createGtfsFileProcessor(
			CommandLine commandLineArgs) {
		String configFile = commandLineArgs.getOptionValue("c");
		String notes = commandLineArgs.getOptionValue("n");
		String gtfsUrl = commandLineArgs.getOptionValue("gtfsUrl");
		String gtfsZipFileName =
				commandLineArgs.getOptionValue("gtfsZipFileName");
		String unzipSubdirectory =
				commandLineArgs.getOptionValue("unzipSubdirectory");
		String gtfsDirectoryName =
				commandLineArgs.getOptionValue("gtfsDirectoryName");
		String supplementDir = commandLineArgs.getOptionValue("supplementDir");
		String regexReplaceFile =
				commandLineArgs.getOptionValue("regexReplaceFile");

		// Handle the parameters that have a floating point, double or int value
		double pathOffsetDistance =
				getDoubleCommandLineOption("pathOffsetDistance", 0.0,
						commandLineArgs);
		double maxStopToPathDistance =
				getDoubleCommandLineOption("maxStopToPathDistance", 60.0,
						commandLineArgs);
		double maxDistanceForEliminatingVertices =
				getDoubleCommandLineOption("maxDistanceForEliminatingVertices",
						3.0, commandLineArgs);
		int defaultWaitTimeAtStopMsec =
				getIntegerCommandLineOption("defaultWaitTimeAtStopMsec",
						10 * Time.MS_PER_SEC, commandLineArgs);
		double maxSpeedKph =
				getDoubleCommandLineOption("maxSpeedKph", 97.0, commandLineArgs);
		double maxTravelTimeSegmentLength =
				getDoubleCommandLineOption("maxTravelTimeSegmentLength", 200.0,
						commandLineArgs);
		int configRev =
				getIntegerCommandLineOption("configRev", -1, commandLineArgs);

		// Handle boolean command line options
		boolean shouldStoreNewRevs = commandLineArgs.hasOption("storeNewRevs");
		boolean shouldDeleteRevs = !commandLineArgs.hasOption("skipDeleteRevs");
		boolean trimPathBeforeFirstStopOfTrip =
				commandLineArgs.hasOption("trimPathBeforeFirstStopOfTrip");

		// Create the processor and set all the options
		GtfsFileProcessor processor =
				new GtfsFileProcessor(configFile, notes, gtfsUrl,
						gtfsZipFileName, unzipSubdirectory, gtfsDirectoryName,
						supplementDir, regexReplaceFile, pathOffsetDistance,
						maxStopToPathDistance,
						maxDistanceForEliminatingVertices,
						defaultWaitTimeAtStopMsec, maxSpeedKph,
						maxTravelTimeSegmentLength,
						configRev,
						shouldStoreNewRevs, shouldDeleteRevs, 
						trimPathBeforeFirstStopOfTrip);

		return processor;
	}

	/**
	 * Processes all command line options using Apache CLI. Further info at
	 * http://commons.apache.org/proper/commons-cli/usage.html . Returns the
	 * CommandLine object that provides access to each arg. Exits if there is a
	 * parsing problem.
	 * 
	 * @param args
	 *            The arguments from the command line
	 * @return CommandLine object that provides access to all the args
	 * @throws ParseException
	 */
	@SuppressWarnings("static-access")
	// Needed for using OptionBuilder
			private static
			CommandLine processCommandLineOptions(String[] args) {
		// Specify the options
		Options options = new Options();

		options.addOption("h", false, "Display usage and help info.");

		options.addOption(OptionBuilder.withArgName("notes").hasArg()
				.withDescription("Description of why processing the GTFS data")
				.withLongOpt("notes").create("n"));

		options.addOption(OptionBuilder
				.withArgName("configFile")
				.hasArg()
				.withDescription(
						"Specifies configuration file to read in. "
								+ "Needed for specifying how to connect to database.")
				.withLongOpt("config").create("c"));

		options.addOption(OptionBuilder
				.hasArg()
				.withArgName("url")
				.withDescription(
						"URL where to get GTFS zip file from. It will "
								+ "be copied over, unzipped, and processed.")
				.create("gtfsUrl"));

		options.addOption(OptionBuilder
				.hasArg()
				.withArgName("zipFileName")
				.withDescription(
						"Local file name where the GTFS zip file is. "
								+ "It will be unzipped and processed.")
				.create("gtfsZipFileName"));

		options.addOption(OptionBuilder
				.hasArg()
				.withArgName("dirName")
				.withDescription(
						"For when unzipping GTFS files. If set then "
								+ "the resulting files go into this subdirectory.")
				.create("unzipSubdirectory"));

		options.addOption(OptionBuilder
				.hasArg()
				.withArgName("dirName")
				.withDescription(
						"Directory where unzipped GTFS file are. Can "
								+ "be used if already have current version of GTFS data "
								+ "and it is already unzipped.")
				.create("gtfsDirectoryName"));

		options.addOption(OptionBuilder
				.hasArg()
				.withArgName("dirName")
				.withDescription(
						"Directory where supplemental GTFS files can be "
								+ "found. These files are combined with the regular GTFS "
								+ "files. Useful for additing additional info such as "
								+ "routeorder and hidden.")
				.create("supplementDir"));

		options.addOption(OptionBuilder
				.hasArg()
				.withArgName("fileName")
				.withDescription(
						"File that contains pairs or regex and "
								+ "replacement text. The names in the GTFS files are "
								+ "processed using these replacements to fix up spelling "
								+ "mistakes, capitalization, etc.")
				.create("regexReplaceFile"));

		options.addOption(OptionBuilder
				.hasArg()
				.withArgName("meters")
				.withDescription(
						"When set then the shapes from shapes.txt are "
								+ "offset to the right by this distance in meters. Useful "
								+ "for when shapes.txt is street centerline data. By "
								+ "offsetting the shapes then the stopPaths for the two "
								+ "directions won't overlap when zoomed in on the map. "
								+ "Can use a negative distance to adjust stopPaths to the "
								+ "left instead of right, which could be useful for "
								+ "countries where one drives on the left side of the road.")
				.create("pathOffsetDistance"));

		options.addOption(OptionBuilder
				.hasArg()
				.withArgName("meters")
				.withDescription(
						"How far a stop can be away from the stopPaths. "
								+ "If the stop is further away from the distance then "
								+ "a warning message will be output and the path will "
								+ "be modified to include the stop.")
				.create("maxStopToPathDistance"));

		options.addOption(OptionBuilder
				.hasArg()
				.withArgName("meters")
				.withDescription(
						"For consolidating vertices for a path. If "
								+ "have short segments that line up then might as combine "
								+ "them. If a vertex is off the rest of the path by only "
								+ "the distance specified then the vertex will be removed, "
								+ "thereby simplifying the path. Value is in meters. "
								+ "Default is 0.0m, which means that no vertices will be "
								+ "eliminated.")
				.create("maxDistanceForEliminatingVertices"));

		options.addOption(OptionBuilder
				.hasArg()
				.withArgName("msec")
				.withDescription(
						"For initial travel times before AVL data "
								+ "used to refine them. Specifies how long vehicle is "
								+ "expected to wait at the stop. "
								+ "Default is 10,000 msec (10 seconds).")
				.create("defaultWaitTimeAtStopMsec"));

		options.addOption(OptionBuilder
				.hasArg()
				.withArgName("kph")
				.withDescription(
						"For initial travel times before AVL data "
								+ "used to refine them. Specifies maximum speed "
								+ "a vehicle can go between stops when "
								+ "determining schedule based travel times. "
								+ "Default is 97kph (60mph).")
				.create("maxSpeedKph"));

		options.addOption(OptionBuilder
				.hasArg()
				.withArgName("meters")
				.withDescription(
						"For determining how many travel time "
								+ "segments should have between a pair of stops. "
								+ "Default is 200.0m, which means that many stop stopPaths "
								+ "will have only a single travel time segment between "
								+ "stops.")
				.create("maxTravelTimeSegmentLength"));

		options.addOption("storeNewRevs", false,
				"Stores the config and travel time revs into ActiveRevisions "
						+ "in database.");

		options.addOption("skipDeleteRevs", false,
				"Delete the rev to be created first just in case.");
		
		options.addOption(
				"trimPathBeforeFirstStopOfTrip",
				false,
				"For trimming off path from shapes.txt for before the first "
						+ "stops of trips. Useful for when the shapes have problems "
						+ "at the beginning, which is suprisingly common.");

        options.addOption(
                "integrationTest",
                false,
                "Flag to indicate whether import is being run as part of integration test");

		// Parse the options
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (Exception e) {
			// There was a parse problem so log the problem,
			// display the command line options so user knows
			// what is needed, and exit since can't continue.
			logger.error(e.getMessage());
			System.err.println(e.getMessage());
			displayCommandLineOptions(options);
			System.exit(0);
		}

		// Handle help option
		if (cmd.hasOption("h")) {
			displayCommandLineOptions(options);
			System.exit(0);
		}

		// Return the CommandLine so that arguments can be accessed
		return cmd;
	}

	/**
	 * For actually invoking the GTFS processor. Note that command line options
	 * are used to set the necessary parameters.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// Process command line args
		CommandLine commandLineArgs = processCommandLineOptions(args);

		// Create GTFS processor according to the command line args
		GtfsFileProcessor processor = createGtfsFileProcessor(commandLineArgs);

		// Process the data
		try {
			processor.process();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		// Found that when running on AWS that program never terminates,
		// probably because still have db threads running. Therefore
		// using exit() to definitely end the process.
        String integrationTest = System.getProperty("transitclock.core.integrationTest");
        if(integrationTest != null){
            logger.info("GTFS import complete for integration test");
            System.setProperty("transitclock.core.gtfsImported","true");
        }else{
            System.exit(0);
        }

	}
}
