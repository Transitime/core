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

import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.utils.Time;

/**
 * For parsing Trimble TAIP format GPS commands into a Java object. It only
 * deals with simple RPC commands.
 * <p>
 * It should be noted that TAIP only provides seconds into the day. The epoch
 * time is determined by using the current system time to determine the day.
 * This means that if processing old data for a different day that could bet an
 * epoch time for the wrong day.
 * 
 * @author SkiBu Smith
 * 
 */
public class TaipGpsLocation {

	private final long fixEpochTime;
	private final double latitude;
	private final double longitude;
	private final float heading;
	private final float speedMetersPerSecond;
	private final String gpsSourceStr;
	private final String ageStr;
	
	private static TimeZone gmtTimeZone = TimeZone.getTimeZone("GMT");

	private static final Logger logger = 
			LoggerFactory.getLogger(TaipGpsLocation.class);
 
	/********************** Member Functions **************************/

	/**
	 * Simple constructor. Declared private because should use get() to create a
	 * TaipGpsLocation object from a TAIP string.
	 * 
	 * @param fixEpochTime
	 * @param latitude
	 * @param longitude
	 * @param heading
	 * @param speedMetersPerSecond
	 * @param gpsSourceStr
	 * @param ageStr
	 */
	private TaipGpsLocation(long fixEpochTime, double latitude,
			double longitude, float heading, float speedMetersPerSecond,
			String gpsSourceStr, String ageStr) {
		this.fixEpochTime = fixEpochTime;
		this.latitude = latitude;
		this.longitude = longitude;
		this.heading = heading;
		this.speedMetersPerSecond = speedMetersPerSecond;
		this.gpsSourceStr = gpsSourceStr;
		this.ageStr = ageStr;
	}

	/**
	 * Makes sure the passed in TAIP string is valid.
	 * 
	 * @param s The TAIP string to examine
	 * @return True if valid
	 */
	private static boolean validate(String s) {
		if (s == null) {
			logger.warn("TAIP string is null");
			return false;
		}
		
		if (s.isEmpty()) {
			logger.warn("TAIP string is empty");
			return false;
		}
		
		if (!s.startsWith(">")) {
			logger.warn("TAIP string \"{}\" doesn't start with '>'", s);
			return false;
		}
		
		if (!s.endsWith("<")) {
			logger.warn("TAIP string \"{}\" doesn't start with '>'", s);
			return false;	
		}
		
		if (!s.startsWith(">RPV")) {
			logger.warn("TAIP string \"{}\" isn't for RPV command", s);
			return false;				
		}
		
		if (s.length() < 35) {
			logger.warn("TAIP string \"{}\" isn't for RPV command since " +
					"less than 35 characters long", s);
			return false;	
		}
		
		// Everything OK
		return true;
	}
	
	/**
	 * Processes the TAIP string and returns corresponding TaipGpsLocation
	 * object
	 * 
	 * @param s
	 *            The TAIP string
	 * @return A TaipGpsLocation object containing all the processed info from
	 *         the TAIP string, or null if there is a problem
	 */
	public static TaipGpsLocation get(String s) {
		// If the string is not valid give up
		if (!validate(s))
			return null;
		
		// Trim out the '>' and the '<'
		String trimmed = s.substring(1, s.length()-1);
		
		// Format of TAIP RPC command is:
		// R = [ 0, 1] Response Query
		// PV = [ 1, 3] Position/Velocity
		// 15714 = [ 3, 8] GPS time-of-day
		// +3739438 = [ 8,16] Latitude
		// -14203846 = [16,25] Longitude
		// 015 = [25,28] Speed (mph)
		// 126 = [28,31] Heading (degrees)
		// 1 = [31,32] GPS source [0=2D-GPS, 1=3D-GPS, 2=2D-DGPS, 3=3D-DGPS, 6=DR, 8=Degraded-DR, 9=Unknown]
		// 2 = [32,33] Age [0=n/a, 1=old, 2=fresh]
		
		String secondsIntoDayStr = trimmed.substring(3,8);
		int secondsIntoDay = Integer.parseInt(secondsIntoDayStr);
		
		// Convert seconds into day in GMT to an epoch fix time and make sure 
		// that handling midnight properly. If fix epoch time ends up being 
		// far in the future (more than 30 minutes) then subtract a day to
		// get it right.
		long fixEpochTime = Time.getStartOfDay(gmtTimeZone) + secondsIntoDay*Time.MS_PER_SEC;
		long currentEpochTime = System.currentTimeMillis();
		if (fixEpochTime - currentEpochTime > 30 * Time.MS_PER_MIN)
			fixEpochTime -= Time.MS_PER_DAY;
		
		String latStr = trimmed.substring(8,16);
		double latitude = Double.parseDouble(latStr) / 100000.0;
		if (latitude == 0.0) {
			logger.error("TAIP string has bad latitude of 0.0. {}", s);
			return null;
		}

		String lonStr = trimmed.substring(16,25);
		double longitude = Double.parseDouble(lonStr) / 100000.0;
		if (longitude == 0.0) {
			logger.error("TAIP string has bad longitude of 0.0. {}", s);
			return null;
		}
		
		String speedMphStr = trimmed.substring(25,28);
		float speedMetersPerSecond = Float.parseFloat(speedMphStr) * 0.44704f;
		
		String headingStr = trimmed.substring(28,31);
		float heading = Float.parseFloat(headingStr);
		
		String gpsSourceStr = trimmed.substring(31,32);
		String ageStr = trimmed.substring(32,33);
		
		return new TaipGpsLocation(fixEpochTime, latitude, longitude, heading,
				speedMetersPerSecond, gpsSourceStr, ageStr);
	}
	
	@Override
	public String toString() {
		return "TaipGpsLocation [" 
				+ "fixTime=" + Time.dateTimeStrMsec(fixEpochTime) 
				+ ", fixEpochTime=" + fixEpochTime 
				+ ", latitude=" + latitude 
				+ ", longitude=" + longitude 
				+ ", heading=" + heading 
				+ ", speedMetersPerSecond=" + speedMetersPerSecond
				+ ", gpsSourceStr=" + gpsSourceStr 
				+ ", ageStr=" + ageStr 
				+ "]";
	}

	public long getFixEpochTime() {
		return fixEpochTime;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public float getHeading() {
		return heading;
	}

	public float getSpeedMetersPerSecond() {
		return speedMetersPerSecond;
	}

	public String getGpsSourceStr() {
		return gpsSourceStr;
	}

	public String getAgeStr() {
		return ageStr;
	}

	/**
	 * Just for debugging
	 * @param args
	 */
	public static void main(String[] args) {
		TaipGpsLocation loc;
		
		loc = get(">RPV64100+4233322-0710597600017412<");
		System.out.println(loc);

		loc = get(">RPV64100+4198118-0706901200012312<");
		System.out.println(loc);
		
		loc = get(">RPV64101+4224070-0711295400017912<");
		System.out.println(loc);
		
	}

}
