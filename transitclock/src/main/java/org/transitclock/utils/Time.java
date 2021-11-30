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
package org.transitclock.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.BooleanConfigValue;
import org.transitclock.core.PredictionGeneratorDefaultImpl;
import org.transitclock.db.structs.Agency;
import org.transitclock.gtfs.DbConfig;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Contains convenience methods for dealing with time issues.
 * <p>
 * Note: To use the proper timezone should set
 * <code> TimeZone.setDefault(TimeZone.getTimeZone(timeZoneStr));</code> before
 * this class is initialized. Otherwise the SimpleDateFormat objects will
 * wrongly use the system default timezone.
 *
 * @author SkiBu Smith
 *
 */
public class Time {
	// Some handy constants for dealing with time.
	// MS_PER_SEC and MS_PER_MIN are declared as integers since they after
	// often used where just interested in a few minutes, which easily
	// fits within an int. By being an int instead of a long don't need
	// to cast to an int when using these values for things like config
	// parameters which are typically a IntegerConfigValue. But for the
	// big values, such as MS_PER_HOUR and longer then risk wrapping
	// around if just using an int. For example a month of 31 days *
	// MS_PER_DAY would wrap if MS_PER_DAY was an integer instead of a long.
	public static final int MS_PER_SEC = 1000;
	public static final int SEC_IN_MSECS = MS_PER_SEC;
	public static final int MS_PER_MIN = 60 * MS_PER_SEC;
	public static final int MIN_IN_MSECS = MS_PER_MIN;
	public static final long MS_PER_HOUR = 60 * MS_PER_MIN;
	public static final long HOUR_IN_MSECS = MS_PER_HOUR;
	public static final long MS_PER_DAY = 24 * MS_PER_HOUR;
	public static final long DAY_IN_MSECS = MS_PER_DAY;
	public static final long MS_PER_WEEK = 7 * MS_PER_DAY;
	public static final long WEEK_IN_MSECS = MS_PER_WEEK;
	public static final long MS_PER_YEAR = 365 * MS_PER_DAY;
	public static final long YEAR_IN_MSECS = MS_PER_YEAR;

	public static final int SEC_PER_MIN = 60;
	public static final int MIN_IN_SECS = SEC_PER_MIN;
	public static final int SEC_PER_HOUR = 60 * SEC_PER_MIN;
	public static final int HOUR_IN_SECS = SEC_PER_HOUR;
	public static final int SEC_PER_DAY = 24 * SEC_PER_HOUR;
	public static final int DAY_IN_SECS = SEC_PER_DAY;

	public static final int MIN_PER_HOUR = 60;
	public static final int HOUR_IN_MINS = MIN_PER_HOUR;

	public static final long NSEC_PER_MSEC = 1000000;
	public static final long MSEC_IN_NSECS = NSEC_PER_MSEC;


	private static final String dateTimePatternMDY = "MM-dd-yyy HH:mm:ss";
	private static final String dateTimePatternYMD = "yyyy-MM-dd HH:mm:ss";

	// Month, Day, Year Format
	private static final DateFormat defaultDateFormatMDY =
			SimpleDateFormat.getDateInstance(DateFormat.SHORT);
	private static final DateFormat dateFormatDashesShortYearMDY =
			new SimpleDateFormat("MM-dd-yy");

	private static final DateFormat readableDateFormatMDY =
			new SimpleDateFormat("MM-dd-yyyy");

	private static final DateFormat readableDateFormat24MDY =
			new SimpleDateFormat("MM-dd-yyyy HH:mm:ss z");

	private static final DateFormat readableDateFormat24NoSecsMDY =
			new SimpleDateFormat("MM-dd-yyyy HH:mm");

	private static final DateFormat readableDateFormat24MsecMDY =
			new SimpleDateFormat("MM-dd-yyyy HH:mm:ss.SSS z");

	private static final DateFormat readableDateFormat24NoTimeZoneMsecMDY =
			new SimpleDateFormat("MM-dd-yyyy HH:mm:ss.SSS");

	private static final DateFormat readableDateFormat24NoTimeZoneNoMsecMDY =
			new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

	// Year, Month, Day Format
	// These two are for reading in dates in various formats
	private static final DateFormat defaultDateFormatYMD =
			new SimpleDateFormat("yyyy-MM-dd");
	private static final DateFormat dateFormatDashesShortYearYMD =
			new SimpleDateFormat("yy-MM-dd");


	private static final DateFormat readableDateFormatYMD =
			new SimpleDateFormat("yyyy-MM-dd");

	private static final DateFormat readableDateFormat24YMD =
			new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

	private static final DateFormat readableDateFormat24NoSecsYMD =
			new SimpleDateFormat("yyyy-MM-dd HH:mm");

	private static final DateFormat readableDateFormat24MsecYMD =
			new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z");

	private static final DateFormat readableDateFormat24NoTimeZoneMsecYMD =
			new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	private static final DateFormat readableDateFormat24NoTimeZoneNoMsecYMD =
			new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	// Time
	private static final DateFormat timeFormat24 =
			new SimpleDateFormat("HH:mm:ss z");

	private static final DateFormat timeFormat24NoTimezone =
			new SimpleDateFormat("HH:mm:ss");

	private static final DateFormat timeFormat24Msec =
			new SimpleDateFormat("HH:mm:ss.SSS z");

	private static final DateTimeFormatter timeFormat24MsecNoTimeZone = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

	// Sun, 06 Nov 1994 08:49:37 GMT  ; RFC 822, updated by RFC 1123
	private static final DateFormat httpFormat =
			new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");

	// Note that this one is not static. It is for when need to include
	// timezone via a Time object.
	private final DateFormat readableDateFormat24MsecForTimeZoneMDY =
			new SimpleDateFormat("MM-dd-yyyy HH:mm:ss.SSS z");
	private final DateFormat readableDateFormatForTimeZoneMDY =
			new SimpleDateFormat("MM-dd-yyyy");

	private final DateFormat readableDateFormat24MsecForTimeZoneYMD =
			new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z");
	private final DateFormat readableDateFormatForTimeZoneYMD =
			new SimpleDateFormat("yyyy-MM-dd");

	private final DateFormat readableTimeFormatForTimeZone =
			new SimpleDateFormat("HH:mm:ss");

	// So can output headings and such with a consistent number of decimal places
	private static final DecimalFormat oneDigitFormat = new DecimalFormat("0.0");

	// Have a shared calendar so don't have to keep creating one
	private Calendar calendar;

	private static final Logger logger = LoggerFactory.getLogger(Time.class);

	private static BooleanConfigValue useMonthDayYearFormat =
			new BooleanConfigValue(
					"transitclock.utils.useMonthDayYearFormat",
					false,
					"Use the month-day-year date format instead of year-month-date.");

	/******************* Methods ******************/

	public Time(DbConfig dbConfig) {

		Agency agency = dbConfig.getFirstAgency();
		this.calendar =
				agency != null ? new GregorianCalendar(agency.getTimeZone())
						: new GregorianCalendar();
	}

	public static String getDateTimePattern(){
		if(useMonthDayYearFormat.getValue()) {
			return dateTimePatternMDY;
		}
		return dateTimePatternYMD;
	}

	private static DateFormat getDefaultDateFormat() {
		if(useMonthDayYearFormat.getValue()) {
			return defaultDateFormatMDY;
		}
		return defaultDateFormatYMD;
	}

	private static DateFormat getDateFormatDashesShortYear() {
		if(useMonthDayYearFormat.getValue()) {
			return dateFormatDashesShortYearMDY;
		}
		return dateFormatDashesShortYearYMD;
	}

	private static DateFormat getReadableDateFormat() {
		if(useMonthDayYearFormat.getValue()) {
			return readableDateFormatMDY;
		}
		return defaultDateFormatYMD;
	}

	private static DateFormat getReadableDateFormat24() {
		if(useMonthDayYearFormat.getValue()) {
			return readableDateFormat24MDY;
		}
		return readableDateFormat24YMD;
	}

	private static DateFormat getReadableDateFormat24NoSecs() {
		if(useMonthDayYearFormat.getValue()) {
			return readableDateFormat24NoSecsMDY;
		}
		return readableDateFormat24NoSecsYMD;
	}

	private static DateFormat getReadableDateFormat24Msec() {
		if(useMonthDayYearFormat.getValue()) {
			return readableDateFormat24MsecMDY;
		}
		return readableDateFormat24MsecYMD;
	}

	private static DateFormat getReadableDateFormat24NoTimeZoneMsec() {
		if(useMonthDayYearFormat.getValue()) {
			return readableDateFormat24NoTimeZoneMsecMDY;
		}
		return readableDateFormat24NoTimeZoneMsecYMD;
	}

	private static DateFormat getReadableDateFormat24NoTimeZoneNoMsec() {
		if(useMonthDayYearFormat.getValue()) {
			return readableDateFormat24NoTimeZoneNoMsecMDY;
		}
		return readableDateFormat24NoTimeZoneNoMsecYMD;
	}

	private DateFormat getReadableDateFormat24MsecForTimeZone() {
		if(useMonthDayYearFormat.getValue()) {
			return readableDateFormat24MsecForTimeZoneMDY;
		}
		return readableDateFormat24MsecForTimeZoneYMD;
	}

	private DateFormat getReadableDateFormatForTimeZone() {
		if(useMonthDayYearFormat.getValue()) {
			return readableDateFormatForTimeZoneMDY;
		}
		return readableDateFormatForTimeZoneYMD;
	}


	/**
	 * Creates a Time object for the specified timezone. Useful for when have to
	 * frequently call members such as getSecondsIntoDay() that need an
	 * expensive calendar object.
	 *
	 * @param timeZoneStr
	 *            Such as "America/Los_Angeles" . List of time zones can be found
	 *            at http://en.wikipedia.org/wiki/List_of_tz_database_time_zones . 
	 *            If null then local timezone is used
	 */
	public Time(String timeZoneStr) {
		// If no time zone string specified then use local timezone
		if (timeZoneStr == null)
			return;

		TimeZone timeZone = TimeZone.getTimeZone(timeZoneStr);
		this.calendar = new GregorianCalendar(timeZone);

		getReadableDateFormat24MsecForTimeZone().setCalendar(this.calendar);
		readableTimeFormatForTimeZone.setCalendar(this.calendar);
		getReadableDateFormatForTimeZone().setCalendar(this.calendar);
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
	 * @param epochDate
	 * @return seconds into the day
	 */
	public int getSecondsIntoDay(Date epochDate) {
		return getSecondsIntoDay(epochDate.getTime());
	}

	/**
	 * Returns day of year. This method is not threadsafe in that it first sets
	 * the time of the calendar and then gets the day of the year without
	 * synchronizing the calendar. But this is a bit faster.
	 *
	 * @param epochDate
	 * @return
	 */
	public int getDayOfYear(Date epochDate) {
		calendar.setTimeInMillis(epochDate.getTime());
		return calendar.get(Calendar.DAY_OF_YEAR);
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
	 * Returns the epoch time of the start of the day for the date and timezone
	 * specified.
	 *
	 * @param date
	 *            the time that the start of the day is needed for
	 * @param tz
	 *            the timezone
	 * @return start of the current day
	 */
	public static long getStartOfDay(Date date, TimeZone tz) {
		Calendar calendar = new GregorianCalendar(tz);
		calendar.setTime(date);

		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		// Get the epoch time
		long epochTime = calendar.getTimeInMillis();
		return epochTime;
	}

	/**
	 * Returns the epoch time of the start of the current day for the default
	 * timezone. The default timezone should be set by the application at
	 * startup using TimeZone.setDefault(TimeZone.getTimeZone(timezoneName)).
	 *
	 * @param date
	 *            the time that the start of the day is needed for
	 * @return start of the current day
	 */
	public static long getStartOfDay(Date date) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);

		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		// Get the epoch time
		long epochTime = calendar.getTimeInMillis();
		return epochTime;
	}

	/**
	 * Converts secondsIntoDay into an epoch time.
	 *
	 * @param secondsIntoDay
	 *            To be converted into epoch time
	 * @param referenceDate
	 *            The approximate epoch time so that can handle times before and
	 *            after midnight.
	 * @return epoch time
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
			// time by plus or minus day. Note: originally used 12 hours
			// instead of 20 hours but that caused problems when trying to 
			// determine if a block is active because it might have started
			// more than 12 hours ago. By using 20 hours we are much more likely
			// to get the correct day because will only correct if really far 
			// off.
			if (epochTime > referenceDate.getTime() + 20 * MS_PER_HOUR) {
				// subtract a day
				epochTime -= MS_PER_DAY;
			} else if (epochTime < referenceDate.getTime() - 20 * MS_PER_HOUR) {
				// add a day
				epochTime += MS_PER_DAY;
			}

			// Get the results
			return epochTime;
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
	 * @return epoch time
	 */
	public long getTripStartDate(int secondsIntoDay, Date referenceDate) {
			// Determine seconds, minutes, and hours
			int minutesIntoDay = secondsIntoDay / 60;
			int hoursIntoDay = minutesIntoDay / 60;

			long hourAdjustment = 4 * MS_PER_HOUR;
			long minuteAdjustment = 5 * MS_PER_MIN;

		long referenceDateTime = referenceDate.getTime();

			// Handles cases where reference date is rolling over past midnight and seconds into day is < 4:00 or >= 24:00

			//	If trip start time is greater than or equal to 20:00 then subtract hourAdjustment hours from referenceDate.
			//	The idea behind this is that if the referenceDate goes past midnight, we can still get the correct
			//	start date by subtracting those hours hours.
			// Start from 20 instead of 24 to handle case where trip leaves past midnight but was scheduled to leave before midnight
			if(hoursIntoDay >=20){
				referenceDateTime -= hourAdjustment;
			}
			// Handles case where reference date is before midnight and seconds into do for future stops is low
			else if(hoursIntoDay < 4){
				referenceDateTime += hourAdjustment;
			}

			// Get the results
			return referenceDateTime;
	}

	/**
	 * Converts secondsIntoDay into an epoch time.
	 *
	 * @param secondsIntoDay
	 *            To be converted into epoch time
	 * @param referenceTime
	 *            The approximate epoch time so that can handle times before and
	 *            after midnight.
	 * @return epoch time
	 */
	public long getEpochTime(int secondsIntoDay, long referenceTime) {
		return getEpochTime(secondsIntoDay, new Date(referenceTime));
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
	 * Parses the dateStr into a Date using the timezone for this Time object.
	 * @param dateStr
	 * @return
	 * @throws ParseException
	 */
	public Date parseUsingTimezone(String dateStr) throws ParseException {
		return getReadableDateFormatForTimeZone().parse(dateStr);
	}

	/**
	 * Parses the datetimeStr and returns a Date object. Format is
	 * "MM-dd-yyyy HH:mm:ss z". Tries multiple formats including with
	 * milliseconds and with and without time zones.
	 *
	 * @param datetimeStr
	 * @return
	 * @throws ParseException
	 */
	public static Date parse(String datetimeStr) throws ParseException {
		// First try with timezone and msec, the most complete form
		try {
			Date date = getReadableDateFormat24Msec().parse(datetimeStr);
			return date;
		} catch (ParseException e) {}

		// Got exception so try without timezone but still try msec
		try {
			Date date = getReadableDateFormat24NoTimeZoneMsec().parse(datetimeStr);
			return date;
		} catch (ParseException e) {}

		// Still not working so try without seconds but with timezone
		try {
			Date date = getReadableDateFormat24().parse(datetimeStr);
			return date;
		} catch (ParseException e) {}

		// Still not working so try without msecs and without timezone
		try {
			Date date = getReadableDateFormat24NoTimeZoneNoMsec().parse(datetimeStr);
			return date;
		} catch (ParseException e) {}

		// Still not working so try without seconds and without timezone
		try {
			Date date = getReadableDateFormat24NoSecs().parse(datetimeStr);
			return date;
		} catch (ParseException e) {}

		// Still not working so try date alone. This will ignore any time
		// specification so this attempt needs to be done after trying all
		// the other formats.
		try {
			Date date = getReadableDateFormat().parse(datetimeStr);
			return date;
		} catch (ParseException e) {}

		// As last resort try the default syntax. Will throw a ParseException
		// if can't parse.
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
			return getDefaultDateFormat().parse(dateStr);
		} catch (ParseException e) {}

		// Try using "-" instead of "/" as separator. Having the date formatter
		// specify only two digits for the year means it also works when 4
		// digits are used, making it pretty versatile.
		return getDateFormatDashesShortYear().parse(dateStr);
	}

	/**
	 * Parses a time such as HH:MM:SS or HH:MM into seconds into the day.
	 * Instead of using SimpleDateFormat or such this function does the
	 * conversion directly and simply in order to be quicker. This is useful for
	 * reading in large volumes of GTFS data and such.
	 *
	 * @return Seconds into the day
	 */
	public static int parseTimeOfDay(String timeStr) {
		// At some point GTFS will handle negative values
		// to indicate a time early in the morning before midnight.
		// Therefore might as well handle negative values now.
		boolean negative = timeStr.charAt(0) == '-';
		String positiveTimeStr = negative ? timeStr.substring(1) : timeStr;

		int firstColon = positiveTimeStr.indexOf(":");
		int hours = Integer.parseInt(positiveTimeStr.substring(0, firstColon));

		// If there is a second colon then also process seconds
		int secondColon = positiveTimeStr.lastIndexOf(":");
		int minutes, seconds;
		if (firstColon != secondColon) {
			// Second colon, so handle minutes and seconds
			minutes = Integer.parseInt(positiveTimeStr.substring(firstColon+1, secondColon));
			seconds = Integer.parseInt(positiveTimeStr.substring(secondColon+1));
		} else {
			// No second colon so just handle minutes
			minutes = Integer.parseInt(positiveTimeStr.substring(firstColon+1));
			seconds = 0;
		}

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
	 * @return Can be null
	 */
	public static String timeOfDayStr(Integer secInDay) {
		if (secInDay == null)
			return null;
		return timeOfDayStr(secInDay.intValue());
	}

	/**
	 * Converts seconds in day to a string HH:MM.
	 * Note: secInDay can be negative.
	 *
	 * @param secInDay
	 * @return
	 */
	public static String timeOfDayShortStr(long secInDay) {
		String timeStr = "";
		if (secInDay < 0) {
			timeStr="-";
			secInDay = -secInDay;
		}
		long hours = secInDay / (60*60);
		long minutes = (secInDay % (60*60)) / 60;

		// Use StringBuilder instead of just concatenating strings since it
		// indeed is faster. Actually measured it and when writing out
		// GTFS stop_times file it was about 10% faster when using
		// StringBuilder.
		StringBuilder b = new StringBuilder(8);
		b.append(timeStr);
		if (hours<10) b.append("0");
		b.append(hours).append(":");
		if (minutes < 10) b.append("0");
		b.append(minutes);
		return b.toString();
	}

	/**
	 * Converts seconds in day to a string HH:MM.
	 * If secInDay null then returns null.
	 * Note: secInDay can be negative.
	 *
	 * @param secInDay
	 * @return Can be null
	 */
	public static String timeOfDayShortStr(Integer secInDay) {
		if (secInDay == null)
			return null;
		return timeOfDayShortStr(secInDay.intValue());
	}

	/**
	 * Converts seconds in day to a string HH:MM AM/PM.
	 * Note: secInDay can be negative.
	 *
	 * @param secInDay
	 * @return
	 */
	public static String timeOfDayAmPmStr(long secInDay) {
		String timeStr = "";
		if (secInDay < 0) {
			timeStr="-";
			secInDay = -secInDay;
		}

		// Handle if time is into next day
		if (secInDay > 24*60*60)
			secInDay -= 24*60*60;

		// Handle if PM instead of AM
		boolean pm = false;
		if (secInDay > 12*60*60) {
			pm = true;
			secInDay -= 12*60*60;
		}

		long hours = secInDay / (60*60);
		long minutes = (secInDay % (60*60)) / 60;

		// Use StringBuilder instead of just concatenating strings since it
		// indeed is faster. Actually measured it and when writing out
		// GTFS stop_times file it was about 10% faster when using
		// StringBuilder.
		StringBuilder b = new StringBuilder(8);
		b.append(timeStr);
		if (hours<10) b.append("0");
		b.append(hours).append(":");
		if (minutes < 10) b.append("0");
		b.append(minutes);
		if (pm)
			b.append("PM");
		else
			b.append("AM");
		return b.toString();
	}

	/**
	 * Converts seconds in day to a string HH:MM AM/PM.
	 * Note: secInDay can be negative.
	 *
	 * @param secInDay Can be null
	 * @return
	 */
	public static String timeOfDayAmPmStr(Integer secInDay) {
		if (secInDay == null)
			return null;
		return timeOfDayAmPmStr(secInDay.intValue());
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
		return getReadableDateFormat().format(epochTime);
	}

	/**
	 * Returns date in format "MM-dd-yyyy"
	 * @param epochTime
	 * @return
	 */
	public static String dateStr(Date epochTime) {
		return getReadableDateFormat().format(epochTime);
	}

	/**
	 * Returns epochTime as a string in the format MM-dd-yyyy HH:mm:ss z
	 * @param epochTime
	 * @return
	 */
	public static String dateTimeStr(long epochTime) {
		return getReadableDateFormat24().format(epochTime);
	}

	/**
	 * Returns epochTime as a string in the format MM-dd-yyyy HH:mm:ss z
	 *
	 * @param epochTime
	 * @return
	 */
	public static String dateTimeStr(Date epochTime) {
		return getReadableDateFormat24().format(epochTime.getTime());
	}

	/**
	 * Returns epochTime as a string in the format MM-dd-yyyy HH:mm:ss.SSS z
	 *
	 * @param epochTime
	 * @return
	 */
	public static String dateTimeStrMsec(long epochTime) {
		return getReadableDateFormat24Msec().format(epochTime);
	}

	/**
	 * Returns epochTime as a string in the format MM-dd-yyyy HH:mm:ss.SSS z
	 * but does so for the Timezone specified by this Time object.
	 *
	 * @param epochTime
	 * @return
	 */
	public String dateTimeStrMsecForTimezone(long epochTime) {
		return getReadableDateFormat24MsecForTimeZone().format(epochTime);
	}

	public String timeStrForTimezone(long epochTime) {
		return readableTimeFormatForTimeZone.format(epochTime);
	}

	/**
	 * Returns epochTime as a string, including msec, in the 
	 * format MM-dd-yyyy HH:mm:ss.SSS z
	 *
	 * @param epochTime
	 * @return
	 */
	public static String dateTimeStrMsec(Date epochTime) {
		return getReadableDateFormat24Msec().format(epochTime.getTime());
	}

	/**
	 * Returns just the time string in format "HH:mm:ss z"
	 *
	 * @param epochTime
	 * @return
	 */
	public static String timeStr(long epochTime) {
		return timeFormat24.format(epochTime);
	}

	/**
	 * Returns just the time string in format "HH:mm:ss z"
	 *
	 * @param epochTime
	 * @return
	 */
	public static String timeStr(Date epochTime) {
		return timeStr(epochTime.getTime());
	}

	/**
	 * Returns just the time string in format "HH:mm:ss"
	 *
	 * @param epochTime
	 * @return
	 */
	public static String timeStrNoTimeZone(long epochTime) {
		return timeFormat24NoTimezone.format(epochTime);
	}

	/**
	 * Returns just the time string in format "HH:mm:ss"
	 *
	 * @param epochTime
	 * @return
	 */
	public static String timeStrNoTimeZone(Date epochTime) {
		return timeStrNoTimeZone(epochTime.getTime());
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
	 * Returns just the time string. Includes msec but no timezone.
	 * e.g. "HH:mm:ss.SSS"
	 *
	 * @param epochTime
	 * @return
	 */
	public static String timeStrMsecNoTimeZone(long epochTime) {
		try {
			return Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).format(timeFormat24MsecNoTimeZone);
		} catch(Exception e){
			logger.error("Unable to convert epoch time {} to formatted time", epochTime, e);
			return null;
		}
	}

	/**
	 * Returns just the time string. Includes msec but no timezone.
	 * e.g. "HH:mm:ss.SSS"
	 *
	 * @param epochTime
	 * @return
	 */
	public static String timeStrMsecNoTimeZone(Date epochTime) {
		try {
			return LocalDateTime.ofInstant(epochTime.toInstant(), ZoneId.systemDefault()).format(timeFormat24MsecNoTimeZone);
		} catch(Exception e){
			logger.error("Unable to convert epoch time {} to formatted time", epochTime, e);
			return null;
		}
	}

	/**
	 * For when sending date as part of http request.
	 * Sun, 06 Nov 1994 08:49:37 GMT  ; RFC 822, updated by RFC 1123
	 *
	 * @param epochTime
	 * @return
	 */
	public static String httpDate(long epochTime) {
		httpFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return httpFormat.format(epochTime);
	}

	/**
	 * Returns the absolute value of the difference between the two times. If
	 * the difference is greater than 12 hours then 24hours-difference is
	 * returned. This is useful for when the times wrap around midnight. For
	 * example, if the times are 11:50pm and 12:05am then the difference will be
	 * 15 minutes instead of 23 hours and 45 minutes.
	 *
	 * @param time1SecsIntoDay
	 * @param time2SecsIntoDay
	 * @return The absolute value of the difference between the two times
	 */
	public static int getTimeDifference(int time1SecsIntoDay,
										int time2SecsIntoDay) {
		int timeDiffSecs = Math.abs(time1SecsIntoDay - time2SecsIntoDay);
		if (timeDiffSecs > 12 * Time.SEC_PER_HOUR)
			return Time.SEC_PER_DAY - timeDiffSecs;
		else
			return timeDiffSecs;
	}

	public static Long getTimeDifference(Date minuendDate, Date subtrahendDate) {
		if(minuendDate != null && subtrahendDate !=null){
			return Math.abs(minuendDate.getTime() - subtrahendDate.getTime());
		}
		return null;
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
