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
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.transitime.feed.gtfsRt.GtfsRtTripFeed;
import org.transitime.feed.gtfsRt.GtfsRtVehicleFeed;
import org.transitime.feed.gtfsRt.OctalDecoder;
import org.transitime.ipc.clients.ConfigInterfaceFactory;
import org.transitime.ipc.clients.PredictionsInterfaceFactory;
import org.transitime.ipc.clients.VehiclesInterfaceFactory;
import org.transitime.ipc.data.Prediction;
import org.transitime.ipc.data.Route;
import org.transitime.ipc.data.Vehicle;
import org.transitime.ipc.interfaces.ConfigInterface;
import org.transitime.ipc.interfaces.PredictionsInterface;
import org.transitime.ipc.interfaces.PredictionsInterface.RouteStop;
import org.transitime.ipc.interfaces.VehiclesInterface;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;

/**
 * A command line application that allows user to request data from the
 * server. Such information includes predictions and vehicles.
 *  
 * @author SkiBu Smith
 *
 */
public class RmiQuery {

	private static String projectId;
	private static Command command;
	private static String routeShortNames[];
	private static String stopIds[];
	private static String vehicleIds[];
	
	private static enum Command {NOT_SPECIFIED, GET_PREDICTIONS, GET_VEHICLES, 
		GET_ROUTE_CONFIG, GET_GTFS_RT_VEHICLES, GET_GTFS_RT_TRIPS};

	/********************** Member Functions **************************/

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
		
		options.addOption(OptionBuilder.withArgName("projectId")
                .hasArg()
                .isRequired()
                .withDescription("Project name.")
                .create("p")
                );
		
		options.addOption(OptionBuilder.withArgName("command")
                .hasArg()
                .isRequired()
                .withDescription("Name of command to execute. Can be " +
                		"\"preds\", \"vehicles\", \"routeConfig\", " + 
                		"\"gtfsRtVehiclePositions\", or \"gtfsRtTripUpdates\" .")
                .create("c")
                );

		options.addOption(OptionBuilder.withArgName("routeShortName")
                .hasArg()
                .withDescription("Route short name (not the route_id).")
                .create("r")
                );
		
		options.addOption(OptionBuilder.withArgName("stopId")
                .hasArg()
                .withDescription("Stop ID.")
                .create("s")
                );
		
		options.addOption(OptionBuilder.withArgName("vehicleId")
                .hasArg()
                .withDescription("Vehicle ID.")
                .create("v")
                );
		
		// Parse the options
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse( options, args);
		} catch (Exception e) {
			// There was a parse problem so display the command line options 
			// so user knows what is needed, and exit since can't continue.
			displayCommandLineOptionsAndExit(options);
		}
		
		// Handle help option
		if (cmd.hasOption("h")) {
			displayCommandLineOptionsAndExit(options);
		}

		projectId = cmd.getOptionValue("p");
		
		// Process the command
		String commandStr = cmd.getOptionValue("c");
		if ("preds".equals(commandStr))
			command = Command.GET_PREDICTIONS;
		else if ("vehicles".equals(commandStr))
			command = Command.GET_VEHICLES;
		else if ("routeConfig".equals(commandStr))
			command = Command.GET_ROUTE_CONFIG;
		else if ("gtfsRtVehiclePositions".equals(commandStr))
			command = Command.GET_GTFS_RT_VEHICLES;
		else if ("gtfsRtTripUpdates".equals(commandStr))
			command = Command.GET_GTFS_RT_TRIPS;
		else {
			System.out.println("Command \"" + commandStr + "\" is not valid.\n");
			displayCommandLineOptionsAndExit(options);
		}
		
		routeShortNames = cmd.getOptionValues("r");
		
		stopIds = cmd.getOptionValues("s");
		
		vehicleIds = cmd.getOptionValues("v");
		
		// Return the CommandLine so that arguments can be accessed
		return cmd;
	}
	
	/**
	 * Displays the command line options on stdout
	 * 
	 * @param options Command line options to be displayed
	 */
	private static void displayCommandLineOptionsAndExit(Options options) {
		// Display help
		final String commandLineSyntax = "java TransitimeCore.jar";
		final PrintWriter writer = new PrintWriter(System.out);
		writer.append(
				"A command line application that allows user to request data\n" +
				"from the server. Such information includes predictions and\n" +
				"vehicles.\n\n");
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

	/**
	 * For the "preds" command. Gets predictions for specified route/stop.
	 * @throws RemoteException 
	 */
	private static void getPredictions() throws RemoteException {
		// FIXME just for debugging
		System.err.println("Getting predictions for projectId=" + projectId + 
				" routeShortNames=" + routeShortNames + " stopIds=" + stopIds);

		// Make sure stops specified
		if (stopIds == null) {
			System.err.println("Error: must specify stop(s) to get predictions.");
			return;
		}
		
		PredictionsInterface predsInterface = 
				PredictionsInterfaceFactory.get(projectId);

		// If getting prediction for single stop...
		if (stopIds.length == 1) {
			String stopId = stopIds[0];
			String routeId = routeShortNames == null ? null : routeShortNames[0];
			
			List<Prediction> predictionList = 
					predsInterface.get(routeId, stopId, 3);
			
			System.out.println("PredictionsInterface for projectId=" + 
					projectId +	" routeId=" + routeId + " stopId=" + 
					stopId + " are " + predictionList);
		} else {
			// Getting predictions for multiple stops
			List<RouteStop> routeStops = new ArrayList<RouteStop>();
			for (int i=0; i<stopIds.length; ++i) {
				String routeId = routeShortNames == null ? null : routeShortNames[i];
				String stopId = stopIds[i];
				RouteStop routeStop = new RouteStop(routeId, stopId);
				routeStops.add(routeStop);
			}
			
			List<List<Prediction>> predictionListList = 
					predsInterface.get(routeStops, 3);
			
			System.out.println("PredictionsInterface for projectId=" + 
					projectId +	" routeStops=" + routeStops + "\nare " + 
					predictionListList);
		}
	}
	
	private static void getVehicles() throws RemoteException {
		// FIXME just for debugging
		System.err.println("Getting vehicles for projectId=" + projectId + 
				" routeShortNames=" + routeShortNames + " vehicleIds=" + vehicleIds);
		
		VehiclesInterface vehiclesInterface = 
				VehiclesInterfaceFactory.get(projectId);
		
		Collection<Vehicle> vehicles = null;
		if (vehicleIds != null && vehicleIds.length == 1) {
			// Get single vehicle
			Vehicle vehicle = vehiclesInterface.get(vehicleIds[0]);
			vehicles = new ArrayList<Vehicle>();
			vehicles.add(vehicle);
		} else if (vehicleIds != null && vehicleIds.length > 1) {
			// Get multiple vehicles
			vehicles = vehiclesInterface.get(vehicleIds);
		} else if (routeShortNames != null && routeShortNames.length == 1) {
			// Get vehicles for specified route
			vehicles = vehiclesInterface.getForRoute(routeShortNames[0]);
		} else {
			// Get all vehicles
			vehicles = vehiclesInterface.get();
		}
		
		if (vehicles.size() > 0) {
			System.out.println("Vehicles are:");
			for (Vehicle vehicle : vehicles) {
				System.out.println("  " + vehicle);
			}
		} else {
			System.out.println("No vehicles found");
		}
	}
	
	private static void getRouteConfig() throws RemoteException {
		ConfigInterface configInterface = 
				ConfigInterfaceFactory.get(projectId);
		Collection<Route> routes = configInterface.getRoutes();
		System.out.println("Routes are:");
		for (Route route : routes) {
			System.out.println(route);
		}
	}
	
	/**
	 * Outputs in human readable format current snapshot of vehicle positions.
	 */
	private static void getGtfsRtVehiclesPositions() {
		GtfsRtVehicleFeed feed = new GtfsRtVehicleFeed(projectId);
		FeedMessage message = feed.createMessage();
		
		// Output data in human readable format. First, convert
		// the octal escaped message to regular UTF encoding.
		String decodedMessage = 
				OctalDecoder.convertOctalEscapedString(message.toString());

		// Write message out to stdout
		System.out.println(decodedMessage);
	}
	
	/**
	 * Outputs in human readable format current snapshot of trip updates,
	 * which contains all the prediction information.
	 */
	private static void getGtfsRtTripUpdates() {
		GtfsRtTripFeed feed = new GtfsRtTripFeed(projectId);
		FeedMessage message = feed.createMessage();
		
		// Output data in human readable format. First, convert
		// the octal escaped message to regular UTF encoding.
		String decodedMessage = 
				OctalDecoder.convertOctalEscapedString(message.toString());

		// Write message out to stdout
		System.out.println(decodedMessage);

	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		processCommandLineOptions(args);

		try {
			if (command == Command.GET_PREDICTIONS) {
				getPredictions();
			} else if (command == Command.GET_VEHICLES) {
				getVehicles();
			} else if (command == Command.GET_ROUTE_CONFIG) {
				getRouteConfig();
			} else if (command == Command.GET_GTFS_RT_VEHICLES) {
				getGtfsRtVehiclesPositions();
			} else if (command == Command.GET_GTFS_RT_TRIPS) {
				getGtfsRtTripUpdates();
			}
		} catch (RemoteException e) {
			// Output stack trace as error message
			e.printStackTrace();
		}
	}

}
