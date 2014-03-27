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
import org.transitime.gtfs.GtfsData;
import org.transitime.gtfs.HttpGetGtfsFile;
import org.transitime.gtfs.TitleFormatter;
import org.transitime.utils.Time;
import org.transitime.utils.Unzip;

/**
 * Reads GTFS files, validates and cleans up the data, stores the
 * data into Java objects, and then stores those objects into the
 * database.
 * 
 * There are a good number of options. Therefore there are addOption
 * methods so don't have a constructor with a large number of
 * parameters.
 *  
 * @author SkiBu Smith
 *
 */
public class GtfsFileProcessor {

	// Optional command line info used within this class
	private final String gtfsUrl;
	private String gtfsZipFileName;
	private final String unzipSubdirectory;
	private String gtfsDirectoryName;
	
	// Optional command line info used for GtfsData class
	private final String supplementDir;
	private final String regexReplaceListFileName;
	private final double pathOffsetDistance;	
	private final double maxStopToPathDistance;	
	private final double maxDistanceForEliminatingVertices;	
	private final int defaultWaitTimeAtStopMsec;
	private final double maxTravelTimeSegmentLength;
	private final boolean shouldCombineShortAndLongNamesForRoutes;
	
	// Logging important in this class
	private static final Logger logger = 
			LoggerFactory.getLogger(GtfsFileProcessor.class);

	/******************* Constructor ***********************/

	/**
	 * Simple constructor. Stores the configurable parameters for this class.
	 * Declared private since only used internally by createGtfsFileProcessor().
	 * 
	 * @param configFile
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
	 * @param shouldCombineShortAndLongNamesForRoutes
	 */
	private GtfsFileProcessor(String configFile,
			String gtfsUrl,
			String gtfsZipFileName,
			String unzipSubdirectory,
			String gtfsDirectoryName,
			String supplementDir,
			String regexReplaceListFileName,
			double pathOffsetDistance,
			double maxStopToPathDistance,
			double maxDistanceForEliminatingVertices,
			int defaultWaitTimeAtStopMsec,
			double maxTravelTimeSegmentLength,
			boolean shouldCombineShortAndLongNamesForRoutes) {
		// Read in config params
		try {
			// Read in the data from config file
			Config.processConfig(configFile);
		} catch (Exception e) {
			logger.error("Error reading in config file \"" + configFile + 
					"\". Exiting program.", e);
			System.exit(-1);
		}
		
		this.gtfsUrl = gtfsUrl;
		this.gtfsZipFileName = gtfsZipFileName;
		this.unzipSubdirectory = unzipSubdirectory;
		this.gtfsDirectoryName = gtfsDirectoryName;
		this.supplementDir = supplementDir;
		this.regexReplaceListFileName = regexReplaceListFileName;
		this.pathOffsetDistance = pathOffsetDistance;
		this.maxStopToPathDistance = maxStopToPathDistance;
		this.maxDistanceForEliminatingVertices = maxDistanceForEliminatingVertices;
		this.defaultWaitTimeAtStopMsec = defaultWaitTimeAtStopMsec;
		this.maxTravelTimeSegmentLength = maxTravelTimeSegmentLength;
		this.shouldCombineShortAndLongNamesForRoutes = shouldCombineShortAndLongNamesForRoutes;
	}
	
		
	/********************** Member Functions **************************/
	
	/**
	 * Gets the GTFS files ready. Reads the zip file from the web if
	 * necessary. Then unpacks the zip file if necessary. This method
	 * will make sure gtfsDirectoryName is set to where the unzipped files can be
	 * found
	 */
	private void obtainGtfsFiles() 
			throws IllegalArgumentException {
		// Make sure params are OK. One and only one of gtfsUrl, gtfsZipFileName,
		// or gtfsDirectoryName should be set.
		if (gtfsUrl == null && gtfsZipFileName == null && gtfsDirectoryName == null) {
			throw new IllegalArgumentException(
					"For GtfsFileProcessor must specify the addOptionGtfsUrl(), " + 
					"the addOptionGtfsZipFileName(), or the addOptionGtfsDirName() " +
					"option.");			
		}
		if (gtfsUrl != null && gtfsZipFileName != null) {
			throw new IllegalArgumentException(
					"For GtfsFileProcessor both the addOptionGtfsUrl() and the " + 
					"addOptionGtfsZipFileName() options were specified but only " + 
					"allowed to specify one or the other");
		}
		if (gtfsDirectoryName != null && 
				(gtfsUrl != null || gtfsZipFileName != null)) {
			throw new IllegalArgumentException(
					"For GtfsFileProcessor both the addOptionGtfsDirName() and " + 
					"the addOptionGtfsUrl() or addOptionGtfsZipFileName() options " + 
					"were specified but only allowed to specify one or the other");
		}
		
		// First need access to the zip file.
		// If URL set then should the file from web and store it
		if (gtfsUrl != null) {
			gtfsZipFileName = HttpGetGtfsFile.getFile(CoreConfig.getProjectId(), gtfsUrl);
		}
		
		// Uncompress the GTFS zip file if need to
		if (gtfsZipFileName != null) {
			gtfsDirectoryName = Unzip.unzip(gtfsZipFileName, unzipSubdirectory);
		}
	}
		
	/**
	 * Once the GtfsFileProcessor is constructed and the options have been
	 * set then this function is used to actually process the GTFS data
	 * and store it into the database.
	 */
	public void process() throws IllegalArgumentException {
		obtainGtfsFiles();
		
		// Create a title formatter
		TitleFormatter titleFormatter = 
				new TitleFormatter(regexReplaceListFileName, true);

		// Process the GTFS data
		GtfsData gtfsData = 
				new GtfsData(CoreConfig.getProjectId(),
						gtfsDirectoryName, 
						supplementDir, 
						shouldCombineShortAndLongNamesForRoutes,
						pathOffsetDistance, 
						maxStopToPathDistance, 
						maxDistanceForEliminatingVertices, 
						defaultWaitTimeAtStopMsec,
						maxTravelTimeSegmentLength,
						titleFormatter);
		gtfsData.processData();
			
		// Log possibly useful info
		titleFormatter.logRegexesThatDidNotMakeDifference();		
	}
	
	/**
	 * Displays the command line options on stdout
	 * @param options
	 */
	private static void displayCommandLineOptions(Options options) {
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
			double defaultValue,
			CommandLine commandLineArgs) {
		String valueStr = commandLineArgs.getOptionValue(commandLineOption);
		if (valueStr != null) {
			try {
				return Double.parseDouble(valueStr);
			} catch (NumberFormatException e) {
				logger.error("The parameter {} was set to {} which is not a " + 
						"valid number. The default value of {} will be used.",
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
			int defaultValue,
			CommandLine commandLineArgs) {
		String valueStr = commandLineArgs.getOptionValue(commandLineOption);
		if (valueStr != null) {
			try {
				return Integer.parseInt(valueStr);
			} catch (NumberFormatException e) {
				logger.error("The parameter {} was set to {} which is not a " + 
						"valid number. The default value of {} will be used.",
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
	private static GtfsFileProcessor createGtfsFileProcessor(CommandLine commandLineArgs) {
		String configFile = commandLineArgs.getOptionValue("c");
		String gtfsUrl = commandLineArgs.getOptionValue("gtfsUrl");
		String gtfsZipFileName = commandLineArgs.getOptionValue("gtfsZipFileName");
		String unzipSubdirectory = commandLineArgs.getOptionValue("unzipSubdirectory");
		String gtfsDirectoryName = commandLineArgs.getOptionValue("gtfsDirectoryName");
		String supplementDir = commandLineArgs.getOptionValue("supplementDir");
		String regexReplaceFile = commandLineArgs.getOptionValue("regexReplaceFile");

		// Handle the parameters that have a floating point, double or int value
		double pathOffsetDistance = getDoubleCommandLineOption(
				"pathOffsetDistance", 0.0, commandLineArgs);
		double maxStopToPathDistance = getDoubleCommandLineOption(
				"maxStopToPathDistance", 60.0, commandLineArgs);
		double maxDistanceForEliminatingVertices = getDoubleCommandLineOption(
				"maxDistanceForEliminatingVertices", 0.0, commandLineArgs);
		int defaultWaitTimeAtStopMsec = getIntegerCommandLineOption(
				"defaultWaitTimeAtStopMsec", 10*Time.MS_PER_SEC, commandLineArgs);
		double maxTravelTimeSegmentLength = getDoubleCommandLineOption(
				"maxTravelTimeSegmentLength", 200.0, commandLineArgs);

		// Handle boolean command line options
		boolean shouldCombineShortAndLongNamesForRoutes = 
				commandLineArgs.hasOption("combineRouteNames");
		
		// Create the processor and set all the options
		GtfsFileProcessor processor = 
				new GtfsFileProcessor(configFile,
						gtfsUrl,
						gtfsZipFileName,
						unzipSubdirectory,
						gtfsDirectoryName,
						supplementDir,
						regexReplaceFile,
						pathOffsetDistance,
						maxStopToPathDistance,
						maxDistanceForEliminatingVertices,
						defaultWaitTimeAtStopMsec,
						maxTravelTimeSegmentLength,
						shouldCombineShortAndLongNamesForRoutes);		
		
		return processor;
	}
	
	/**
	 * Processes all command line options using Apache CLI.
	 * Further info at http://commons.apache.org/proper/commons-cli/usage.html .
	 * Returns the CommandLine object that provides access to each arg.
	 * Exits if there is a parsing problem.
	 * 
	 * @param args The arguments from the command line
	 * @return CommandLine object that provides access to all the args
	 * @throws ParseException
	 */
	@SuppressWarnings("static-access")  // Needed for using OptionBuilder
	private static CommandLine processCommandLineOptions(String[] args) {
		// Specify the options
		Options options = new Options();
		
		options.addOption("h", false, "Display usage and help info."); 
		
		options.addOption(OptionBuilder.withArgName("configFile")
                .hasArg()
                .withDescription("Specifies configuration file to read in. " +
                		"Needed for specifying how to connect to database.")
                .withLongOpt("config")
                .create("c")
                );
		
		options.addOption(OptionBuilder.hasArg()
				.withArgName("url")
                .withDescription("URL where to get GTFS zip file from. It will " + 
                		"be copied over, unzipped, and processed.")
                .create("gtfsUrl")
                );
		
		options.addOption(OptionBuilder.hasArg()
				.withArgName("zipFileName")
                .withDescription("Local file name where the GTFS zip file is. " + 
                		"It will be unzipped and processed.")
                .create("gtfsZipFileName")
                );

		options.addOption(OptionBuilder.hasArg()
				.withArgName("dirName")
                .withDescription("For when unzipping GTFS files. If set then " + 
                		"the resulting files go into this subdirectory.")
                .create("unzipSubdirectory")
                );

		options.addOption(OptionBuilder.hasArg()
				.withArgName("dirName")
                .withDescription("Directory where unzipped GTFS file are. Can " + 
                		"be used if already have current version of GTFS data " + 
                		"and it is already unzipped.")
                .create("gtfsDirectoryName")
                );

		options.addOption(OptionBuilder.hasArg()
				.withArgName("dirName")
                .withDescription("Directory where supplemental GTFS files can be " + 
                		"found. These files are combined with the regular GTFS " + 
                		"files. Useful for additing additional info such as " + 
                		"routeorder and hidden.")
                .create("supplementDir")
                );

		options.addOption(OptionBuilder.hasArg()
				.withArgName("fileName")
                .withDescription("File that contains pairs or regex and " + 
                		"replacement text. The names in the GTFS files are " + 
                		"processed using these replacements to fix up spelling " + 
                		"mistakes, capitalization, etc.")
                .create("regexReplaceFile")
                );

		options.addOption(OptionBuilder.hasArg()
				.withArgName("meters")
                .withDescription("When set then the shapes from shapes.txt are " +
                		"offset to the right by this distance in meters. Useful " +
                		"for when shapes.txt is street centerline data. By " +
                		"offsetting the shapes then the stopPaths for the two " +
                		"directions won't overlap when zoomed in on the map. " +
                		"Can use a negative distance to adjust stopPaths to the " +
                		"left instead of right, which could be useful for " +
                		"countries where one drives on the left side of the road.")
                .create("pathOffsetDistance")
                );

		options.addOption(OptionBuilder.hasArg()
				.withArgName("meters")
                .withDescription("How far a stop can be away from the stopPaths. " +
                		"If the stop is further away from the distance then " +
                		"a warning message will be output and the path will " +
                		"be modified to include the stop.")
                .create("maxStopToPathDistance")
                );

		options.addOption(OptionBuilder.hasArg()
				.withArgName("meters")
                .withDescription("For consolidating vertices for a path. If " +
                		"have short segments that line up then might as combine " +
                		"them. If a vertex is off the rest of the path by only " +
                		"the distance specified then the vertex will be removed, " +
                		"thereby simplifying the path. Value is in meters. " +
                		"Default is 0.0m, which means that not vertices will be " +
                		"eliminated.")
                .create("maxDistanceForEliminatingVertices")
                );
		
		options.addOption(OptionBuilder.hasArg()
				.withArgName("msec")
                .withDescription("For initial travel times before AVL data " +
                		"used to refine them. Specifies how long vehicle is " +
                		"expected to wait at the stop. " +
                		"Default is 10,000 msec (10 seconds).")
                .create("defaultWaitTimeAtStopMsec")
                );
		
		options.addOption(OptionBuilder.hasArg()
				.withArgName("meters")
                .withDescription("For determining how many travel time " +
                		"segments should have between a pair of stops. " +
                		"Default is 200.0m, which means that many stop stopPaths " +
                		"will have only a single travel time segment between " +
                		"stops.")
                .create("maxTravelTimeSegmentLength")
                );
		
		options.addOption("combineRouteNames", 
				false, 
				"Combines short and long route names to create full name."); 
		
		// Parse the options
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse( options, args);
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
		} catch (IllegalArgumentException e) {
			logger.error(e.getMessage());
		}
	}
}
