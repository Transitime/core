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
package org.transitime.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.transitime.gtfs.DbConfig;

/**
 * Contains convenience methods for dealing with time issues.
 * 
 * @author SkiBu Smith
 *
 */
public class Time {
	// Some handy constants for dealing with time
	public static final int MS_PER_SEC = 1000;
	public static final int MS_PER_MIN = 60 * MS_PER_SEC;
	public static final int MS_PER_HOUR = 60 * MS_PER_MIN;
	public static final int MS_PER_DAY = 24 * MS_PER_HOUR;
	public static final int MS_PER_WEEK = 7 * MS_PER_DAY;
	public static final int MS_PER_YEAR = 365 * MS_PER_DAY;
	
	public static final int SEC_PER_MIN = 60;
	public static final int SEC_PER_HOUR = 60 * SEC_PER_MIN;
	public static final int SEC_PER_DAY = 24 * SEC_PER_HOUR;
	
	public static final long NSEC_PER_MSEC = 1000000;
	
	// These two are for reading in dates in various formats
	private static final DateFormat defaultDateFormat =
			SimpleDateFormat.getDateInstance(DateFormat.SHORT);
	private static final DateFormat dateFormatDashesShortYear =
			new SimpleDateFormat("MM-dd-yy");

	
	private static final DateFormat readableDateFormat =
			new SimpleDateFormat("MM-dd-yyyy");
	
	private static final DateFormat readableDateFormat24 = 
			new SimpleDateFormat("MM-dd-yyyy HH:mm:ss z");
	
	private static final DateFormat readableDateFormat24Msec = 
			new SimpleDateFormat("MM-dd-yyyy HH:mm:ss.SSS z");
	
	private static final DateFormat readableDateFormat24NoTimeZoneMsec = 
			new SimpleDateFormat("MM-dd-yyyy HH:mm:ss.SSS");
	
	private static final DateFormat readableDateFormat24NoTimeZoneNoMsec = 
			new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

	private static final DateFormat timeFormat24 =
			new SimpleDateFormat("HH:mm:ss z");

	private static final DateFormat timeFormat24Msec =
			new SimpleDateFormat("HH:mm:ss.SSS z");

	// So can output headings and such with a consistent number of decimal places
	private static final DecimalFormat oneDigitFormat = new DecimalFormat("0.0");

	// Have a shared calendar so don't have to keep creating one
	private Calendar calendar;
	
	/******************* Methods ******************/
	
	public Time(DbConfig dbConfig) {
		this.calendar = new GregorianCalendar(dbConfig.getFirstAgency().getTimeZone());
	}
	
	/**
	 * Creates a Time object for the specified timezone. Useful for when have to
	 * frequently call members such as getSecondsIntoDay() that need an
	 * expensive calendar object.
	 * 
	 * @param timezoneStr Such as "America/Los_Angeles"
	 */
	public Time(String timezoneStr) {
		TimeZone timeZone = TimeZone.getTimeZone(timezoneStr);
		this.calendar = new GregorianCalendar(timeZone);
	}
	
	/**
	 * Converts the epoch time into number of seconds into the day.
	 * 
	 * @param epochTime
	 * @return seconds into the day
	 */
	public int getSecondsIntoDay(long epochTime) {
		// Since setting and then getting time and this method might be called
		// by multiple threads need to synchronize.
		synchronized (calendar) {
			// Get seconds into day
			calendar.setTimeInMillis(epochTime);
			return calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60 +
					calendar.get(Calendar.MINUTE) * 60          +
					calendar.get(Calendar.SECOND);
		}
	}
	
	/**
	 * Converts the epoch time into number of seconds into the day.
	 * 
	 * @param epochTime
	 * @return seconds into the day
	 */
	public int getSecondsIntoDay(Date epochTime) {
		return getSecondsIntoDay(epochTime.getTime());
	}
	
	/**
	 * Converts the epoch time into number of msec into the day.
	 * 
	 * @param epochTime
	 * @return msec into the day
	 */
	public int getMsecsIntoDay(Date epochTime) {
		// Since setting and then getting time and this method might be called
		// by multiple threads need to synchronize.
		synchronized (calendar) {
			// Get seconds into day
			calendar.setTime(epochTime);
			return calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000 +
					calendar.get(Calendar.MINUTE) * 60 * 1000          +
					calendar.get(Calendar.SECOND) * 1000               +
					calendar.get(Calendar.MILLISECOND);
		}
	}
	
	/**
	 * Converts secondsIntoDay into an epoch time.
	 * 
	 * @param secondsIntoDay
	 *            To be converted into epoch time
	 * @param referenceDate
	 *            The approximate epoch time so that can handle times before and
	 *            after midnight.
	 * @return
	 */
	public long getEpochTime(int secondsIntoDay, Date referenceDate) {
		// Need to sync the calendar since reusing it.
		synchronized (calendar) {
			// Determine seconds, minutes, and hours
			int seconds = secondsIntoDay % 60;
			int minutesIntoDay = secondsIntoDay / 60;
			int minutes = minutesIntoDay % 60;
			int hoursIntoDay = minutesIntoDay / 60;
			int hours = hoursIntoDay % 24;
			
			// Set the calendar to use the reference time so that get the
			// proper date.
			calendar.setTime(referenceDate);
			
			// Set the seconds, minutes, and hours so that the calendar has
			// the proper time. Need to also set milliseconds because otherwise
			// would use milliseconds from the referenceDate.
			calendar.set(Calendar.MILLISECOND, 0);
			calendar.set(Calendar.SECOND, seconds);
			calendar.set(Calendar.MINUTE, minutes);
			calendar.set(Calendar.HOUR_OF_DAY, hours);
			
			// Get the epoch time
			long epochTime = calendar.getTimeInMillis();
			
			// Need to make sure that didn't have a problem around midnight. 
			// For example, a vehicle is supposed to depart a layover at 
			// 00:05:00 right after midnight but the AVL time might be for
			// 23:57:13, which is actually for the previous day. If would
			// simply set the hours, minutes and seconds then would wrongly
			// get an epoch time for the previous day. Could have the same
			// problem if the AVL time is right after midnight but the 
			// secondsIntoDay is just before midnight. Therefore if the 
			// resulting epoch time is too far away then adjust the epoch
			// time by plus or minus day.
			if (epochTime > referenceDate.getTime() + 12 * MS_PER_HOUR) {
				// subtract a day
				epochTime -= MS_PER_DAY;
			} else if (epochTime < referenceDate.getTime() - 12 * MS_PER_HOUR) {
				// add a day
				epochTime += MS_PER_DAY;
			}
			
			// Get the results
			return epochTime;
		}
	}
	
	/**
	 * Returns time of day in msecs. But uses reference time to determine
	 * if interested in a time before midnight (a negative value) or a
	 * time past midnight (greater than 24 hours). This way when looking
	 * at schedule adherence and such it is much easier to deal with
	 * situations where have blocks that span midnight. Only need to
	 * get the time and do schedule adherence comparison once.
	 * 
	 * @param epochTime
	 * @param referenceTimeIntoDayMsecs
	 * @return
	 */
	public long getMsecsIntoDay(Date epochTime, long referenceTimeIntoDayMsecs) {
		int timeIntoDay = getMsecsIntoDay(epochTime);
		long delta = Math.abs(referenceTimeIntoDayMsecs - timeIntoDay);
		
		long deltaForBeforeMidnight = 
				Math.abs(referenceTimeIntoDayMsecs - (timeIntoDay - Time.MS_PER_DAY));
		if (deltaForBeforeMidnight < delta)
			return timeIntoDay - (int)Time.MS_PER_DAY;
		
		long deltaForAfterMidnight = 
				Math.abs(referenceTimeIntoDayMsecs - (timeIntoDay + Time.MS_PER_DAY));
		if (deltaForAfterMidnight < delta)
			return timeIntoDay + (int)Time.MS_PER_DAY;
		
		return timeIntoDay;
	}
	
	/**
	 * Parses the datetimeStr and returns a Date object.
	 * Format is "MM-dd-yyyy HH:mm:ss z"
	 * 
	 * @param datetimeStr
	 * @return
	 * @throws ParseException
	 */
	public static Date parse(String datetimeStr) throws ParseException {
		// First try with timezone and msec, the most complete form
		try {
			Date date = readableDateFormat24Msec.parse(datetimeStr);
			return date;
		} catch (ParseException e) {}

		// Got exception so try without timezone but still try msec
		try {
			Date date = readableDateFormat24NoTimeZoneMsec.parse(datetimeStr);
			return date;
		} catch (ParseException e) {}
		
		// Still not working so try without seconds but with timezone
		try {
			Date date = readableDateFormat24.parse(datetimeStr);
			return date;
		} catch (ParseException e) {}
		
		// Still not working so try without seconds and without timezone
		try {
			Date date = readableDateFormat24NoTimeZoneNoMsec.parse(datetimeStr);
			return date;
		} catch (ParseException e) {}
		
		// As last resort try the default syntax
		return new SimpleDateFormat().parse(datetimeStr);
	}
	
	/**
	 * Parses the dateStr and returns a Date object. Format of 
	 * date is "MM-dd-yyyy".
	 * 
	 * @param dateStr
	 * @return
	 * @throws ParseException
	 */
	public static Date parseDate(String dateStr) throws ParseException {
		try {
			return defaultDateFormat.parse(dateStr);
		} catch (ParseException e) {}

		// Try using "-" instead of "/" as separator. Having the date formatter
		// specify only two digits for the year means it also works when 4
		// digits are used, making it pretty versatile.
		return dateFormatDashesShortYear.parse(dateStr);		
	}
	
	/**
	 * Parses a time such as HH:MM:SS into seconds into the day.
	 * Instead of using SimipleDateFormat or such this function 
	 * does the conversion directly and simply in order to be quicker.
	 * This is useful for reading in large volumes of GTFS data and
	 * such. 
	 * @return
	 */
	public static int parseTimeOfDay(String timeStr) {
		// At some point GTFS will handle negative values
		// to indicate a time early in the morning before midnight.
		// Therefore might as well handle negative values now.
		boolean negative = timeStr.charAt(0) == '-';
		String positiveTimeStr = negative ? timeStr.substring(1) : timeStr;

		int firstColon = positiveTimeStr.indexOf(":");
		int secondColon = positiveTimeStr.lastIndexOf(":");
		int hours = Integer.parseInt(positiveTimeStr.substring(0, firstColon));  
		int minutes = Integer.parseInt(positiveTimeStr.substring(firstColon+1, secondColon));
		int seconds = Integer.parseInt(positiveTimeStr.substring(secondColon+1));
		
		int result = hours * 60 * 60 + minutes*60 + seconds;
		if (negative)
			return -result;
		else
			return result;
	}
	
	/**
	 * Converts seconds in day to a string HH:MM:SS.
	 * Note: secInDay can be negative.
	 * 
	 * @param secInDay
	 * @return
	 */
	public static String timeOfDayStr(long secInDay) {
		String timeStr = "";
		if (secInDay < 0) {
			timeStr="-";
			secInDay = -secInDay;
		}
		long hours = secInDay / (60*60);
		long minutes = (secInDay % (60*60)) / 60;
		long seconds = secInDay % 60;
		
		// Use StringBuilder instead of just concatenating strings since it
		// indeed is faster. Actually measured it and when writing out
		// GTFS stop_times file it was about 10% faster when using
		// StringBuilder.
		StringBuilder b = new StringBuilder(8);
		b.append(timeStr);
		if (hours<10) b.append("0");
		b.append(hours).append(":");
		if (minutes < 10) b.append("0");
		b.append(minutes).append(":");
		if (seconds<10) b.append("0");
		b.append(seconds);
		return b.toString();
	}
	
	/**
	 * Converts seconds in day to a string HH:MM:SS.
	 * If secInDay null then returns null.
	 * Note: secInDay can be negative.
	 * 
	 * @param secInDay
	 * @return
	 */
	public static String timeOfDayStr(Integer secInDay) {
		if (secInDay == null)
			return null;
		return timeOfDayStr(secInDay.intValue());
	}
	
	/**
	 * Outputs time in minutes with a single digit past the decimal point
	 * @param msec
	 * @return
	 */
	public static String minutesStr(long msec) {
		float minutes = (float) msec / Time.MS_PER_MIN;
		return oneDigitFormat.format(minutes);
	}
	
	/**
	 * Outputs time in seconds with a single digit past the decimal point
	 * @param msec
	 * @return
	 */
	public static String secondsStr(long msec) {
		float seconds = (float) msec / Time.MS_PER_SEC;
		return oneDigitFormat.format(seconds);
	}

	/**
	 * Returns the elapsed time in msec as a string. If the time is below
	 * 1 minute then it is displayed in seconds. If greater than 2 minutes
	 * then it is displayed in minutes. For both, 1 digit after the 
	 * decimal point is displayed. The units, either " sec" or " msec"
	 * are appended.
	 * 
	 * @param msec
	 * @return
	 */
	public static String elapsedTimeStr(long msec) {
		if (Math.abs(msec) < 2*Time.MS_PER_MIN) {
			return Time.secondsStr(msec) + " sec";
		} else {
			return Time.minutesStr(msec) + " min";
		}
	}
	
	/**
	 * Returns date in format "MM-dd-yyyy"
	 * @param epochTime
	 * @return
	 */
	public static String dateStr(long epochTime) {
		return readableDateFormat.format(epochTime);
	}
	
	/**
	 * Returns date in format "MM-dd-yyyy"
	 * @param epochTime
	 * @return
	 */
	public static String dateStr(Date epochTime) {
		return readableDateFormat.format(epochTime);
	}
	
	/**
	 * Returns epochTime as a string in the format MM-dd-yyyy HH:mm:ss z
	 * @param epochTime
	 * @return
	 */
	public static String dateTimeStr(long epochTime) {
		return readableDateFormat24.format(epochTime);
	}
	
	/**
	 * Returns epochTime as a string in the format MM-dd-yyyy HH:mm:ss z
	 * @param epochTime
	 * @return
	 */
	public static String dateTimeStr(Date epochTime) {
		return readableDateFormat24.format(epochTime.getTime());
	}	
	
	/**
	 * Returns epochTime as a string in the format MM-dd-yyyy HH:mm:ss z
	 * @param epochTime
	 * @return
	 */
	public static String dateTimeStrMsec(long epochTime) {
		return readableDateFormat24Msec.format(epochTime);
	}
	
	/**
	 * Returns epochTime as a string, including msec, in the 
	 * format MM-dd-yyyy HH:mm:ss.SSS z
	 * @param epochTime
	 * @return
	 */
	public static String dateTimeStrMsec(Date epochTime) {
		return readableDateFormat24Msec.format(epochTime.getTime());
	}	
	
	/**
	 * Returns just the time string
	 * @param epochTime
	 * @return
	 */
	public static String timeStr(long epochTime) {
		return timeFormat24.format(epochTime);
	}

	/**
	 * Returns just the time string. Includes msec.
	 * 
	 * @param epochTime
	 * @return
	 */
	public static String timeStrMsec(Date epochTime) {
		return timeFormat24Msec.format(epochTime.getTime());
	}
	
	/**
	 * Returns just the time string. Includes msec.
	 * e.g. "HH:mm:ss.SSS z"
	 * 
	 * @param epochTime
	 * @return
	 */
	public static String timeStrMsec(long epochTime) {
		return timeFormat24Msec.format(epochTime);
	}

	/**
	 * Simply calls Thread.sleep() but catches the InterruptedException
	 * so that the calling function doesn't need to.
	 * @param msec
	 */
	public static void sleep(long msec) {
		try {
			Thread.sleep(msec);
		} catch (InterruptedException e) {
			// ignore
		}
	}
	
	public static void main(String args[]) {
		try {
			// TODO make this a unit test
			Time time = new Time("America/Los_Angeles");
			Date referenceDate = parse("11-23-2013 23:55:00");
			int secondsIntoDay = 24 * SEC_PER_HOUR - 60;
			long epochTime = time.getEpochTime(secondsIntoDay, referenceDate);
			System.out.println(new Date(epochTime));
		} catch (ParseException e) {}
		
	}
}
