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
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.statistics.ScheduleStatistics;
import org.transitime.utils.Time;

/**
 * For generating more accurate schedule times for GTFS trips.txt file by
 * using departure data obtained via GPS.
 *  
 * @author SkiBu Smith
 *
 */
public class ScheduleGenerator {

	// Command line args
	private static String projectId;
	private static String gtfsDirectoryName;
	private static Date beginTime;
	private static Date endTime;
	private static Time timeForUsingCalendar;
	private static double desiredFractionEarly;
	private static int allowableDifferenceFromMeanSecs;
	private static int allowableDifferenceFromOriginalTimeSecs;
	private static boolean doNotUpdateFirstStopOfTrip;
	private static int allowableEarlySecs;
	private static int allowableLateSecs;
	
	private static final Logger logger = 
			LoggerFactory.getLogger(ScheduleGenerator.class);


	/********************** Member Functions **************************/

	/**
	 * Displays the command line options on stdout
	 * 
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
		
		options.addOption(
				OptionBuilder.hasArg()
				.withArgName("projectId")
                .isRequired()
                .withDescription("Specifies which projectId processing configuration for.")
                .create("p")
                );

		options.addOption(
				OptionBuilder.hasArg()
				.withArgName("dirName")
                .isRequired()
                .withDescription("Directory where unzipped GTFS file are. Can " + 
                		"be used if already have current version of GTFS data " + 
                		"and it is already unzipped.")
                .create("gtfsDirectoryName")
                );
		
		options.addOption(
				OptionBuilder.hasArg()
				.withArgName("MM-dd-yyyy")                
                .isRequired()
                .withDescription("Begin date for reading arrival/departure " +
                		"times from database. Format is MM-dd-yyyy, " +
                		"such as 9-20-2014.")
                .create("b")
                );

		options.addOption(
				OptionBuilder.hasArg()                
				.withArgName("MM-dd-yyyy")
                .withDescription("Optional end date for reading arrival/departure " +
                		"times from database. Format is MM-dd-yyyy, " +
                		"such as 9-20-2014. Will read up to current " +
                		"time if this option is not set.")
                .create("e")
                );

		options.addOption(
				OptionBuilder.hasArg()
				.withArgName("timeZone")
                .isRequired()
                .withDescription("Timezone for agency, such has \"America/Los_Angeles\"")
                .create("tz")
                );

		options.addOption(
				OptionBuilder.hasArg()
				.withArgName("fraction")
                .isRequired()
                .withDescription("Specifies fraction of times that should. " +
                		"Good value is probably 0.2")
                .create("f")
                );
		
		options.addOption(
				OptionBuilder.hasArg()
				.withArgName("secs")
				.withDescription("How many seconds arrival/departure must be " +
						"within mean for the trip/stop to not be filtered out.")
				.create("allowableFromMean")
				);
		
		options.addOption(
				OptionBuilder.hasArg()
				.withArgName("secs")
				.withDescription("How many seconds arrival/departure must be " +
						"within original schedule time for the trip/stop to " +
						"not be filtered out.")
				.create("allowableFromOriginal")
				);
		
		options.addOption(
				OptionBuilder.hasArg()
				.withArgName("minutes")
				.withDescription("For providing schedule adherence information. " +
						"Specifies how many minutes a vehicle can be ahead of " +
						"the schedule and still be considered on time. Default " +
						"is 1 minute.")
				.create("allowableEarly")
				);
		
		options.addOption(
				OptionBuilder.hasArg()
				.withArgName("minutes")
				.withDescription("For providing schedule adherence information. " +
						"Specifies how many minutes a vehicle can be behind " +
						"schedule and still be considered on time. Default is " +
						"5 minutes.")
				.create("allowableLate")
				);
		
		options.addOption("updateFirstStopOfTrip", false, 
				"Set if should modify time even for first stops of trips."); 

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
			displayCommandLineOptions(options);
			System.exit(0);
		}
		
		// Handle help option
		if (cmd.hasOption("h")) {
			displayCommandLineOptions(options);
			System.exit(0);
		}

		// Determine if should update even the first stops of trips.
		// Default is to no update the times for those stops.
		doNotUpdateFirstStopOfTrip = !cmd.hasOption("updateFirstStopOfTrip");
		
		// Set variables based on the command line args
		gtfsDirectoryName = cmd.getOptionValue("gtfsDirectoryName");
		projectId = cmd.getOptionValue("p");
		String timeZoneStr = cmd.getOptionValue("tz");
		timeForUsingCalendar = new Time(timeZoneStr);
		
		// Get the fraction early "e" command line option
		String fractionEarlyStr = cmd.getOptionValue("f");
		try {
			desiredFractionEarly = Double.parseDouble(fractionEarlyStr);
		} catch (NumberFormatException e1) {
			System.err.println("Paramater -f \"" + desiredFractionEarly + 
					"\" could not be parsed.");
			System.exit(-1);
		}
		if (desiredFractionEarly < 0.0 || desiredFractionEarly > 0.5) {
			System.err.println("Paramater -f \"" + desiredFractionEarly + 
					"\" must be between 0.0 and 0.5");
			System.exit(-1);

		}
		
		// Get the beginTime "b" command line option
		String beginDateStr = cmd.getOptionValue("b");
		try {
			beginTime = Time.parseDate(beginDateStr);
			
			// If specified time is in the future then reject
			if (beginTime.getTime() > System.currentTimeMillis()) {
				System.err.println("Paramater -b \"" + beginDateStr + 
						"\" is in the future and therefore invalid!");
				System.exit(-1);					
			}				
		} catch (java.text.ParseException e) {
			System.err.println("Paramater -b \"" + beginDateStr + 
					"\" could not be parsed. Format must be \"MM-dd-yyyy\"");
			System.exit(-1);
		}

		// Get the optional endTime "e" command line option
		if (cmd.hasOption("e")) {
			String endDateStr = cmd.getOptionValue("e");
			try {
				// Get the end date specified and add 24 hours since want to 
				// load data up to the end of the date
				endTime = new Date(Time.parseDate(endDateStr).getTime() + 
						24*Time.MS_PER_HOUR);
				
			} catch (java.text.ParseException e) {
				System.err.println("Paramater -e \"" + endDateStr + 
						"\" could not be parsed. Format must be \"MM-dd-yyyy\"");
				System.exit(-1);
			}
		} else {
			// End time not specified so simply uses current time
			endTime = new Date();
		}
		
		// Get the optional "allowableFromMean" command line option.
		// Default is 15 minutes.
		allowableDifferenceFromMeanSecs = 15 * Time.SEC_PER_MIN;  
		if (cmd.hasOption("allowableFromMean")) {
			String param = cmd.getOptionValue("allowableFromMean");
			try {
				allowableDifferenceFromMeanSecs = 
						Integer.parseInt(param);
			} catch (NumberFormatException e) {
				System.err.println("Option -allowableFromMean value \"" + 
						param +	"\" could not be parsed into an integer.");
				System.exit(-1);
			}
		}
		
		// Get the optional "allowableFromOriginal" command line option
		// Default is 30 minutes.
		allowableDifferenceFromOriginalTimeSecs = 30 * Time.SEC_PER_MIN;
		if (cmd.hasOption("allowableFromOriginal")) {
			String param = cmd.getOptionValue("allowableFromOriginal");
			try {
				allowableDifferenceFromOriginalTimeSecs = 
						Integer.parseInt(param);
			} catch (NumberFormatException e) {
				System.err.println("Option -allowableFromOriginal value \"" + 
						param + "\" could not be parsed into an integer.");
				System.exit(-1);
			}
		}
		
		// Get the optional "allowableEarly" and "allowableLate" command
		// line options. Default is 1 minute early and 5 minutes late.
		allowableEarlySecs = 1 * Time.SEC_PER_MIN;
		if (cmd.hasOption("allowableEarly")) {
			String param = cmd.getOptionValue("allowableEarly");
			try {
				allowableEarlySecs = (int) (Double.parseDouble(param) * Time.SEC_PER_MIN);				
			} catch (NumberFormatException e) {
				System.err.println("Option -allowableEarly value \"" + 
						param + "\" could not be parsed.");
				System.exit(-1);
			}
		}
		allowableLateSecs = 5 * Time.SEC_PER_MIN;
		if (cmd.hasOption("allowableLate")) {
			String param = cmd.getOptionValue("allowableLate");
			try {
				allowableLateSecs = (int) (Double.parseDouble(param) * Time.SEC_PER_MIN);				
			} catch (NumberFormatException e) {
				System.err.println("Option -allowableLate value \"" + 
						param + "\" could not be parsed.");
				System.exit(-1);
			}
		}
		
		// Return the CommandLine so that arguments can be further accessed
		return cmd;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		processCommandLineOptions(args);

		// Use ScheduleStatistics class to actually process all the data
		ScheduleStatistics stats = 
				new ScheduleStatistics(projectId, gtfsDirectoryName, 
						beginTime, endTime, timeForUsingCalendar,
						desiredFractionEarly,
						allowableDifferenceFromMeanSecs,
						allowableDifferenceFromOriginalTimeSecs,
						doNotUpdateFirstStopOfTrip,
						allowableEarlySecs,
						allowableLateSecs);
		stats.process();	
	}
	
}
