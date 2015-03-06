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

package org.transitime.avl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.db.structs.Location;

/**
 * For parsing a NMEA command into a GPS location.
 * <p>
 * Based on code at https://github.com/ktuukkan/marine-api
 * 
 * @author SkiBu Smith
 *
 */
public class NmeaGpsLocation {

	private final double lat;
	private final double lon;
	private final float speed;
	private final float heading;
	private final long time;
	
	// Corresponds to 230394 = 23rd of March 1994
	private static final DateFormat dateFormatter = 
			new SimpleDateFormat("ddMMyy");
	private static final TimeZone gmtTimeZone = TimeZone.getTimeZone("GMT");
	
	private static final int TIME_OF_DAY = 1;
	private static final int LATITUDE = 3;
	private static final int LATITUDE_HEMISPHERE = 4;
	private static final int LONGITUDE = 5;
	private static final int LONGITUDE_HEMISPHERE = 6;
	private static final int SPEED = 7;
	private static final int ANGLE = 8;
	private static final int DATE = 9;
	
	private static final char CHECKSUM_DELIMITER = '*';
	
	private static final float KNOTS_TO_M_PER_SEC = (float) 0.51444;
	
	private static final Logger logger = LoggerFactory
			.getLogger(NmeaGpsLocation.class);

	/********************** Member Functions **************************/

	/**
	 * Constructor. Private since to use parse() to construct object.
	 * 
	 * @param lat
	 * @param lon
	 * @param speed
	 * @param heading
	 * @param time
	 */
	private NmeaGpsLocation(double lat, double lon, float speed,
			float heading, long time) {
		super();
		this.lat = lat;
		this.lon = lon;
		this.speed = speed;
		this.heading = heading;
		this.time = time;
	}
	
	/**
	 * Calculates XOR checksum of given NMEA string from between the '$' and the
	 * checksum. Resulting hex value is returned as a String in two digit
	 * format, padded with a leading zero if necessary.
	 * 
	 * @param nmeaString
	 * @return The checksum for the NMEA string
	 */
	private static String calculateChecksum(String nmeaString) {
		// Get part of string between '$' and the '*' checksum
		// character, exclusive
		String partialNmeaStr;
		if (nmeaString.indexOf(CHECKSUM_DELIMITER) > 0)
			partialNmeaStr = nmeaString.substring(1,
					nmeaString.indexOf(CHECKSUM_DELIMITER));
		else
			partialNmeaStr = nmeaString.substring(1);
		
		int sum = 0;
		for (int i=0; i < partialNmeaStr.length(); ++i) {
			sum ^= (byte) partialNmeaStr.charAt(i);
		}
		return String.format("%02X", sum);
	}
	
	/**
	 * Checks if checksum ok
	 * 
	 * @param nmeaString
	 * @return True if checksum ok
	 */
	private static boolean checksumOk(String nmeaString) {
		int i = nmeaString.indexOf(CHECKSUM_DELIMITER);
		if (i > 0) {
			String checksumFromString = nmeaString.substring(i + 1, i + 3);
			String calculatedChecksum = calculateChecksum(nmeaString);
			boolean checksumOk = checksumFromString.equals(calculatedChecksum);
			if (!checksumOk) {
				logger.debug("NMEA string checksum is no valid. String is: {}", 
						nmeaString);
			}
			return checksumOk;
		} else
			return true;
	}
	
	/**
	 * Makes sure the NMEA string is valid. Only can handle $GPRMC commands.
	 * Checksum must be OK or not included.
	 * 
	 * @param nmeaString
	 * @param ignoreChecksum So can handle NMEA strings with invalid checksum
	 * @return True if NMEA string valid
	 */
	private static boolean isValid(String nmeaString, boolean ignoreChecksum) {
		// If not $GPRMC command then can't handle it
		if (!nmeaString.startsWith("$GPRMC"))
			return false;
		
		return ignoreChecksum || checksumOk(nmeaString);
	}
	
	/**
	 * Gets the latitude from the NMEA string
	 * 
	 * @param nmeaComponents
	 * @return latitude
	 */
	private static double getLatitude(String nmeaComponents[]) {
		String field = nmeaComponents[LATITUDE];
		double full = Double.parseDouble(field);
		int deg = (int) (full/100);
		double minutes = full - deg*100;
		double latitude = deg + (minutes/60);
		
		String hemisphereLat = nmeaComponents[LATITUDE_HEMISPHERE];
		if (hemisphereLat.equals("S"))
			latitude = -latitude;
		
		return latitude;		
	}
	
	/**
	 * Gets the longitude from the NMEA string
	 * 
	 * @param nmeaComponents
	 * @return longitude
	 */
	private static double getLongitude(String nmeaComponents[]) {
		String field = nmeaComponents[LONGITUDE];
		double full = Double.parseDouble(field);
		int deg = (int) (full/100);
		double minutes = full - deg*100;
		double longitude = deg + (minutes/60);
		
		String hemisphereLon = nmeaComponents[LONGITUDE_HEMISPHERE];
		if (hemisphereLon.equals("W"))
			longitude = -longitude;
		
		return longitude;			
	}
	
	/**
	 * Returns speed in meters per second.
	 * 
	 * @param nmeaComponents
	 * @return Speed in m/s
	 */
	private static float getSpeed(String nmeaComponents[]) {
		float speedInKnots = Float.parseFloat(nmeaComponents[SPEED]);
		return speedInKnots * KNOTS_TO_M_PER_SEC;
	}
	
	/**
	 * Returns heading in degrees measured clockwise from north.
	 * 
	 * @param nmeaComponents
	 * @return Heading in degrees
	 */
	private static float getHeading(String nmeaComponents[]) {
		if (nmeaComponents[ANGLE].isEmpty())
			return Float.NaN;
		
		float heading = Float.parseFloat(nmeaComponents[ANGLE]);
		return heading;
	}
	
	/**
	 * Determines epoch time of GPS report from the NMEA string
	 * 
	 * @param nmeaComponents
	 * @return epoch time
	 * @throws ParseException
	 */
	private static long getTime(String nmeaComponents[]) 
			throws ParseException {
		// Determine time of day from the time component of the NMEA string
		String timeStr = nmeaComponents[TIME_OF_DAY];
		int hours = Integer.parseInt(timeStr.substring(0, 2));
		int minutes = Integer.parseInt(timeStr.substring(2,4));
		int seconds = Integer.parseInt(timeStr.substring(4,6));
		int msec = 0;
		int decimal = timeStr.indexOf('.');
		if (decimal > 0)
			msec = Integer.parseInt(timeStr.substring(decimal + 1));
		long timeOfDayMsec = ((hours * 60 + minutes) * 60 + seconds) * 1000 + msec; 
		
		// Determine epoch time for beginning of date from the date component
		// of the NMEA string
		String dateStr = nmeaComponents[DATE];
		dateFormatter.setTimeZone(gmtTimeZone);
		Date date = dateFormatter.parse(dateStr);
		
		return date.getTime() + timeOfDayMsec;
	}
	
	/**
	 * Returns location from NMEA string
	 * 
	 * @param nmeaString
	 * @param ignoreChecksum
	 *            So can disable checksum checking if it is not valid
	 * @return The location read in if valid NMEA string, otherwise null.
	 */
	private static NmeaGpsLocation parse(String nmeaString,  boolean ignoreChecksum) {
		// Make sure really is a valid $GPRMC command
		if (!isValid(nmeaString, ignoreChecksum))
			return null;
		
		String nmeaComponents[] = nmeaString.split(",");
		try {
			double lat = getLatitude(nmeaComponents);
			double lon = getLongitude(nmeaComponents);
			float speed = getSpeed(nmeaComponents);
			float heading = getHeading(nmeaComponents);
			long time = getTime(nmeaComponents);
			return new NmeaGpsLocation(lat, lon, speed, heading, time);
		} catch (Exception e) {
			logger.debug("Could not parse NMEA string. {} \"{}\"", 
					e.getMessage(), nmeaString, e);
			return null;
		}
	}
	
	/**
	 * Returns location from NMEA string. Makes sure checksum is OK.
	 * 
	 * @param nmeaString
	 * @return The location read in if valid NMEA string, otherwise null.
	 */
	public static NmeaGpsLocation parse(String nmeaString) {
		return parse(nmeaString, false);
	}
	
	/**
	 * Returns location from NMEA string. Doesn't check the checksum.
	 * 
	 * @param nmeaString
	 * @return The location read in if valid NMEA string, otherwise null.
	 */
	public static NmeaGpsLocation parseIgnoringChecksum(String nmeaString) {
		return parse(nmeaString, true);
	}

	@Override
	public String toString() {
		return "NemaGpsLocation [" 
				+ "lat=" + lat 
				+ ", lon=" + lon 
				+ ", speed=" + speed 
				+ ", heading=" + heading 
				+ ", time=" + new Date(time) 
				+ "]";
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	public Location getLocation() {
		return new Location(lat, lon);
	}
	
	public float getSpeed() {
		return speed;
	}

	public float getHeading() {
		return heading;
	}

	public long getTime() {
		return time;
	}

	public static void main(String args[]) {
		NmeaGpsLocation loc;
		loc = parseIgnoringChecksum("$GPRMC,221813.000,A,3346.89942,N,8423.75712,W,8.15,265.24,030315,,*0C,TABLET");
		System.out.println("loc=" + loc);
		loc = parse("$GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W*6A");
		System.out.println("loc=" + loc);
		loc = parseIgnoringChecksum("$GPRMC,061605.000,A,3347.24214,N,8424.34422,W,0.01,,040315,,*0C,TABLET");
		System.out.println("loc=" + loc);
	}
}
