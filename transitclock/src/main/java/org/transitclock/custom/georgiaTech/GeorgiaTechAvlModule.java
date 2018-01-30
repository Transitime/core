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

package org.transitclock.custom.georgiaTech;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.avl.AvlModule;
import org.transitclock.avl.NmeaGpsLocation;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.db.structs.AvlReport.AssignmentType;
import org.transitclock.modules.Module;
import org.transitclock.utils.Time;

/**
 * Gets GPS data from the Georgia Tech AVL feed. This feed is different from
 * other AvlModules in that it reads data from a socket instead of polling from
 * a URL (such as an XML feed).
 *
 * @author SkiBu Smith
 *
 */
public class GeorgiaTechAvlModule extends AvlModule {

	private static StringConfigValue georgiaTechFeedDomainName = 
			new StringConfigValue("transitime.avl.georgiaTechFeedDomainName", 
					"The domain name for the socket connection for the "
					+ "Georgia Tech AVLfeed.");

	private static IntegerConfigValue georgiaTechFeedPort =
			new IntegerConfigValue("transitime.avl.georgiaTechFeedPort", 
					"The port number for the socket connection for the "
							+ "Georgia Tech AVLfeed.");
	
	// If debugging feed and want to not actually process
	// AVL reports to generate predictions and such then
	// set shouldProcessAvl to false;
	private static boolean shouldProcessAvl = true;

	private static final Logger logger = 
			LoggerFactory.getLogger(GeorgiaTechAvlModule.class);

	/********************** Member Functions **************************/

	/**
	 * Constructor
	 * 
	 * @param agencyId
	 */
	public GeorgiaTechAvlModule(String agencyId) {
		super(agencyId);
	}

	/**
	 * So that can debug log every read.
	 * 
	 * @param reader
	 * @return Line read from reader
	 * @throws IOException
	 */
	private static String readLine(BufferedReader reader) throws IOException {
		String input = reader.readLine();

		logger.debug("GeorgiaTech AVL Feed={}", input);

		return input;
	}

	/**
	 * Process AVL report for a vehicle by reading data from the socket. The
	 * data is in NMEA format and also includes special commands for specifying
	 * the vehicleId and the assignment. Calls superclass processAvlReport() for
	 * each AVL report in order to actually process the report and generate
	 * predictions.
	 * 
	 * @param reader
	 * @throws IOException
	 */
	private void readVehicleDataFromSocket(BufferedReader reader)
			throws IOException {
		// Read in GPS location as NMEA data
		String input = readLine(reader);
		NmeaGpsLocation gpsLoc = NmeaGpsLocation.parseIgnoringChecksum(input);

		// Read in other special data until empty line indicates end of data 
		// for vehicle
		String vehicleId = null;
		String assignmentId = null;
		while (!(input = readLine(reader)).isEmpty()) {
			// Determine the type of parameter
			int equals = input.indexOf("=");
			String paramterName = input.substring(0, equals);
			String value = input.substring(equals + 1);
			
			// If it is vehicle ID then keep track of it 
			if (paramterName.equals("VID"))
				vehicleId = value;
			
			// If assignment ("job"?) then keep track of it
			if (paramterName.equals("J"))
				assignmentId = value;
		}
		
		// For testing only
		//writeToFile(vehicleId, gpsLoc);

		// Create AvlReport that corresponds to data read in
		AvlReport avlReport =
				new AvlReport(vehicleId, gpsLoc.getTime(),
						gpsLoc.getLocation(), gpsLoc.getSpeed(),
						gpsLoc.getHeading(), "GeorgiaT");
		avlReport.setAssignment(assignmentId, AssignmentType.BLOCK_ID);
		
		logger.debug("{}", avlReport);
		
		// Process the AVL report to generate predictions and such
		if (shouldProcessAvl) 
			processAvlReport(avlReport);
	}

	/**
	 * Appends data for a vehicle to a CSV file. Just for testing. Can be useful
	 * for sketching out vehicle locations using Google Map Engine.
	 * 
	 * @param vehicleId
	 * @param gpsLoc
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	private static void writeToFile(String vehicleId, NmeaGpsLocation gpsLoc)
			throws IOException {
		String fileName = "C:/Users/Mike/tmp/georgiaTech_" + vehicleId + ".csv";
		String line = vehicleId + "," + gpsLoc.getLat() + "," + gpsLoc.getLon()
				+ "," + gpsLoc.getHeading() + "," + new Date(gpsLoc.getTime());
		List<String> lines = Arrays.asList(line);
		Files.write(Paths.get(fileName), lines, StandardCharsets.UTF_8,
				StandardOpenOption.CREATE, StandardOpenOption.APPEND);
	}

	/**
	 * Reads data from socket forever and processes it. Catches exceptions so
	 * really will run forever.
	 */
	private void readLoop() {
		// Loop forever. Exceptions should all be caught so will
		// continue looping.
		while (true) {
			Socket soc = null;
			DataInputStream din = null;
			BufferedReader reader = null;
			try {
				// Open up the socket connection
				soc = new Socket(georgiaTechFeedDomainName.getValue(),
						georgiaTechFeedPort.getValue());
				din = new DataInputStream(soc.getInputStream());
				reader = new BufferedReader(new InputStreamReader(din));
				
				// Read data forever from the socket...
				while (true) {
					readVehicleDataFromSocket(reader);
				}
			} catch (Exception e) {
				logger.error("Exception occurred in readLoop(). Will try again "
								+ "in a few seconds", e);

				// Sleep for a few seconds so don't get into tight loop if
				// there is a problem
				Time.sleep(5 * Time.MS_PER_SEC);
			} finally {
				// Close input stream
				try {
					if (reader != null)
						reader.close();
					if (din != null)
						din.close();
					if (soc != null)
						soc.close();
				} catch (IOException e1) {
					// Can ignore problem with closing streams since will just
					// open them up again.
					logger.error("Exception when closing files.", e1);
				}

			}
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// Log that module successfully started
		logger.info("Started module {} for agencyId={}", 
				getClass().getName(), getAgencyId());
		
		// Actually process all the data
		readLoop();
	}

	/**
	 * For testing
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// For debugging turn off the actual processing of the AVL data.
		// This way the AVL data is logged, but that is all.
		shouldProcessAvl = false;

		// Create a NextBusAvlModue for testing
		Module.start("org.transitclock.custom.georgiaTech.GeorgiaTechAvlModule");
	}

}
