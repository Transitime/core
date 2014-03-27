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
package org.transitime.worldbank.gtfsRtExample;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.List;

import com.google.transit.realtime.GtfsRealtime.*;

/**
 * For processing GPS data and outputting it into a GTFS-realtime format
 * file. The data can come from a database or can use test data. The
 * data can be written to a file or output to stdout in human readable 
 * format. Data can be obtained and written once or can continuously 
 * poll for data. Writing data once is for when want to create a large
 * data set for improving the accuracy of the GTFS schedule times. The 
 * polling is for when want to provide a live realtime feed of GTFS-realtime
 * data. 
 */
public class GtfsRealtimeExample {
	// Command line arguments
	private static boolean useTestData = false;
	private static boolean outputHumanReadable = false;
	private static int pollingTimeSeconds = 0;
	
	/**
	 * Returns true if running in polling mode.
	 * @return
	 */
	public static boolean usingPolling() {
		return pollingTimeSeconds > 0;
	}
	
	/**
	 * Sets the member variables according to the command line arguments
	 * @param args
	 */
	private static void processCommandLineArguments(String args[]) {
		for (int i=0; i<args.length; ++i) {
			if (args[i].equals("-h") || args[i].equals("-help")) {
				System.out.println("Usage:\n" +
						" -polling <seconds> For when application should " +
						"continue to run and query db and output data every " +
						"specified number of seconds.\n" +
						" -useTestData For when want to use test GPS " +
						"data instead of retrieving it from a database.\n" +
						" -outputHumanReadable For when should also output " +
						"results in human readable format\n" +
						" -help Displays this usage information.");
				System.exit(0);
			}
			if (args[i].equals("-polling")) {
				pollingTimeSeconds = Integer.parseInt(args[i+1]);
			}
			if (args[i].equals("-useTestData")) {
				useTestData = true;
			}
			if (args[i].equals("-outputHumanReadable")) {
				outputHumanReadable = true;
			}
		}
	}
	
	/**
	 * Takes in VehicleData and puts it into a GTFS-realtime 
	 * VehiclePosition object.
	 *  
	 * @param vehicleData
	 * @return the resulting VehiclePosition
	 * @throws ParseException
	 */
	private static VehiclePosition createVehiclePosition(VehicleData vehicleData) 
			throws ParseException {
		// Create the VehicleDescriptor information
		VehicleDescriptor.Builder vehicle = VehicleDescriptor.newBuilder()
				.setId(vehicleData.getVehicleId());
		// License plate information is optional sos only add it if not null
		if (vehicleData.getLicensePlate() != null)
			vehicle.setLicensePlate(vehicleData.getLicensePlate());

		// Add the Position information
		Position.Builder position = Position.newBuilder()
				.setLatitude(vehicleData.getLatitude())
				.setLongitude(vehicleData.getLongitude());
		// Heading and speed are optional so only add them if not null
		if (vehicleData.getHeading() !=null)
			position.setBearing(Float.parseFloat(vehicleData.getHeading()));
		if (vehicleData.getSpeed() != null)
			position.setSpeed(Float.parseFloat(vehicleData.getSpeed()));
		
		// If there is route information then add it via the TripDescriptor
		TripDescriptor.Builder trip = null;
		if (vehicleData.getRouteId() != null && vehicleData.getRouteId().length() > 0) {
			trip = TripDescriptor.newBuilder()
					.setRouteId(vehicleData.getRouteId());
		}
		
		// Convert the GPS timestamp information to an epoch time as
		// number of milliseconds since 1970.
		long gpsTime = vehicleData.getGpsTime().getTime();
		
		// Create the VehiclePosition object using the information from above
		VehiclePosition.Builder vehiclePosition =
		  VehiclePosition.newBuilder()
			.setVehicle(vehicle)
			.setPosition(position)
			.setTimestamp(gpsTime);
		// Trip/route info is optional so only add it if it exists
		if (trip != null)
			vehiclePosition.setTrip(trip);
		
		// Return the results
		return vehiclePosition.build();
	}
	
	/**
	 * Creates a GTFS-realtime message for the list of VehicleData
	 * passed in.
	 * @param vehicleDataList the data to be put into the GTFS-realtime message
	 * @return the GTFS-realtime FeedMessage
	 */
	private static FeedMessage createMessage(List<VehicleData> vehicleDataList) {
		FeedMessage.Builder message = FeedMessage.newBuilder();
		
		FeedHeader.Builder feedheader = FeedHeader.newBuilder()
				.setGtfsRealtimeVersion("1.0")
				.setTimestamp(System.currentTimeMillis());
		message.setHeader(feedheader);
		  
		for (VehicleData vehicleData : vehicleDataList) {
			FeedEntity.Builder vehiclePositionEntity = FeedEntity.newBuilder()
					.setId(vehicleData.getVehicleId());

			VehiclePosition vehiclePosition;
			try {
				vehiclePosition = createVehiclePosition(vehicleData);
			} catch (Exception e) {
				// Output error message
				System.err.println("Error parsing vehicle data. " + 
						e.getMessage() + ".\n" + 
						vehicleData);
				e.printStackTrace();
				
				// This data was bad so continue to the next vehicle.
				continue;
			}
    		vehiclePositionEntity.setVehicle(vehiclePosition);
    		
    		message.addEntity(vehiclePositionEntity);
		}		
		
		return message.build();	
	}
	
	/**
	 * This method is from http://mindprod.com/jgloss/utf.html Copyright: (c)
	 * 2009-2014 Roedy Green, Canadian Mind Products, http://mindprod.com
	 * 
	 * decode a String from UTF-8 bytes. We handle only 16-bit chars.
	 * <p/>
	 * UTF-8 is normally decoded simply with new String( byte[], "UTF-8" ) or
	 * with an InputStreamReader but this is roughly what goes on under the
	 * hood, if you ever need to write your own decoder for some non-Java
	 * platform, or you are just curious how it works.
	 * <p/>
	 * This works for 16-bit characters only. It does not handle 32-bit
	 * characters encoded with the contortionist use of the low (0xdc00..0xdfff)
	 * and high(0xd800..0xdbff) bands of surrogate characters.
	 * 
	 * @param input
	 *            bytes encoded with UTF-8.
	 * 
	 * @return decoded string
	 */
	private static String decode(byte[] input) {
		char[] output = new char[input.length];
		// index input[]
		int i = 0;
		// index output[]
		int j = 0;
		while (i < input.length && input[i] != 0) {
			// get next byte unsigned
			int b = input[i++] & 0xff;
			// classify based on the high order 3 bits
			switch (b >>> 5) {
			default:
				// one byte encoding
				// 0xxxxxxx
				// use just low order 7 bits
				// 00000000 0xxxxxxx
				output[j++] = (char) (b & 0x7f);
				break;
			case 6:
				// two byte encoding
				// 110yyyyy 10xxxxxx
				// use low order 6 bits
				int y = b & 0x1f;
				// use low order 6 bits of the next byte
				// It should have high order bits 10, which we don't check.
				int x = input[i++] & 0x3f;
				// 00000yyy yyxxxxxx
				output[j++] = (char) (y << 6 | x);
				break;
			case 7:
				// three byte encoding
				// 1110zzzz 10yyyyyy 10xxxxxx
				assert (b & 0x10) == 0 : "UTF8Decoder does not handle 32-bit characters";
				// use low order 4 bits
				int z = b & 0x0f;
				// use low order 6 bits of the next byte
				// It should have high order bits 10, which we don't check.
				y = input[i++] & 0x3f;
				// use low order 6 bits of the next byte
				// It should have high order bits 10, which we don't check.
				x = input[i++] & 0x3f;
				// zzzzyyyy yyxxxxxx
				int asint = (z << 12 | y << 6 | x);
				output[j++] = (char) asint;
				break;
			}// end switch
		}// end while
		return new String(output, 0/* offset */, j/* count */);
	}

	/**
	 * 
	 * @param octalEscapedString
	 * @return
	 */
	private static String convertOctalEscapedStringToUtf(
			String octalEscapedString) {
		byte[] originalBytes= octalEscapedString.getBytes();
		byte[] convertedBytes = new byte[originalBytes.length];
		
		// Goes through each byte of the original octal escaped string.
		// If escaped octal sequence found then the for "\xxx" bytes
		// are converted to a regular byte.
		for (int i=0, j=0; i<originalBytes.length; ++i,++j) {
			byte originalByte = originalBytes[i];
			// If escaped octal character handle it specially
			if (originalByte == '\\') {
				// Start of octal sequence
				int octalDigit1 = originalBytes[i+1]-'0';
				int octalDigit2 = originalBytes[i+2]-'0';
				int octalDigit3 = originalBytes[i+3]-'0';
				originalByte = (byte) ((octalDigit1 << 6) + (octalDigit2 << 3) + 
						octalDigit3);
				// Handle 3 extra characters so increment the index accordingly
				i = i+3;
			} 
			convertedBytes[j] = originalByte;
			
		}
		// Now that have proper byte stream, convert it to a string
		// and change to proper UTF-8 string.
		String decodedMessage = decode(convertedBytes);
		return decodedMessage;
	}
	
	/**
	 * Writes the GTFS-realtime message to the specified file.
	 * 
	 * @param message the GTFS-realtime message
	 * @param fileName where to write it
	 */
	private static void writeMessageToFile(FeedMessage message, String fileName) {
		try {
			FileOutputStream output = new FileOutputStream(fileName);
			
			if (outputHumanReadable) {
				// Output data in human readable format. First, convert
				// the octal escaped message to regular UTF encoding.
				String decodedMessage = 
						convertOctalEscapedStringToUtf(message.toString());

				// Open up file so that can try writing UTF-8 characters
				Writer utf8Output = new BufferedWriter(new OutputStreamWriter(output, "UTF-8"));								
				utf8Output.write(decodedMessage); 
				utf8Output.close();
				
				// Write it out to stdout as well
				System.out.println(decodedMessage);				
			} else {
				// Write file using regular binary GTFS-RT format
				message.writeTo(output);	
			}
			output.close();
		} catch (Exception e) {
			System.err.println("Error with writing file " + e.getMessage());
		}	
	}
	
	/**
	 * Writes the GTFS-realtime data to a temporary file and then moves 
	 * it to the proper place. This way the file will always be complete.
	 * 
	 * @param message the GTFS-realtime message
	 * @param fileName fileName where to write it
	 */
	private static void overwriteMessageToFile(FeedMessage message, String fileName) {
		String tempFileName = fileName + "tmp";
		try {
			// Write the data to a temporary file
			FileOutputStream output = new FileOutputStream(tempFileName);
			message.writeTo(output);
			output.close();
			
			// Move the file to the desired name
			if (ConfigurableParameters.MICROSOFT_WINDOWS) {
				// Using Microsoft Windows so use special rename command.
				// Yes, MS Windows is really, really silly. The last parameter
				// can only be the filename. It cannot include the path.
				// First, make sure that the path uses '\' instead of '/' 
				// characters.
				tempFileName = tempFileName.replace('/', '\\');
				fileName = fileName.replace('/', '\\');
				
				// Erase the old file
				String deleteCommand = "cmd.exe /c del  " + fileName;
				Runtime.getRuntime().exec(deleteCommand);			

				// Rename the temporary file to the desired file name
				if (fileName.contains("\\"))
					fileName = fileName.substring(fileName.lastIndexOf("\\")+1);
				String renameCommand = "cmd.exe /c rename " + tempFileName + " " + fileName;
				Runtime.getRuntime().exec(renameCommand);
			} else {
				// Using Linux so handle with regular move file command
				String renameCommand = "mv " + tempFileName + " " + fileName;
				Runtime.getRuntime().exec(renameCommand);
			}
		} catch (Exception e) {
			System.err.println("Error with writing file " + e.getMessage());
		}	
	}
	
	/**
	 * This main() method does all the work of retrieving the data and
	 * writing it to a GTFS-realtime file. Also does the polling if 
	 * so specified by the command line argument -polling.
	 * @param args
	 */
	public static void main(String args[]) {
		// Get parameters
		processCommandLineArguments(args);
		
		// If polling then loop through getting and writing data. But
		// if not polling then just do this loop once.
		do {
			long loopStartTime = System.currentTimeMillis();
			
			// Get the vehicle data from test data or from the database
			List<VehicleData> vehicleDataList;
			if (useTestData) {
				System.out.println("Generating test GPS data...");
				vehicleDataList = DataFetcher.createTestData();
			} else if (usingPolling()){
				System.out.println("Reading unique GPS data from database...");
				vehicleDataList = DataFetcher.queryDatabaseFilteringDuplicates();
			} else {
				System.out.println("Reading GPS data from database...");
				vehicleDataList = DataFetcher.queryDatabase();				
			}
			System.out.println("Read in " + vehicleDataList.size() +
					" records of GPS data.");
			
			// Create the GTFS-realtime message that is to be output
			System.out.println("Creating the GTFS-realtime message...");
			FeedMessage message = createMessage(vehicleDataList);
			
			// If debugging write out the data in human readable format
			if (outputHumanReadable) {
				writeMessageToFile(message, ConfigurableParameters.OUTPUT_FILE_NAME);
			} else {
				// Write the data to the file. If polling then simply
				// write the file. But if not polling then need
				// to overwrite any previous file.
				System.out.println("Writing GTFS-realtime messsage to file \"" + 
						ConfigurableParameters.OUTPUT_FILE_NAME + "\" ...");
				if (usingPolling())
					overwriteMessageToFile(message, ConfigurableParameters.OUTPUT_FILE_NAME);
				else
					writeMessageToFile(message, ConfigurableParameters.OUTPUT_FILE_NAME);
				System.out.println("Done writing GTFS-realtime message to file.");
			}
			
			// If polling then wait the appropriate time until should poll again
			long waitTime = pollingTimeSeconds*1000 - 
					(System.currentTimeMillis() - loopStartTime);
			if (waitTime > 0) {
				try {
					Thread.sleep(waitTime);
				} catch (InterruptedException e) {}
			}
		} while(pollingTimeSeconds != 0);
	}
}
