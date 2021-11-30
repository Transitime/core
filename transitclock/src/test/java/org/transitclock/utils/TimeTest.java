package org.transitclock.utils;

import org.junit.Assert;
import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeTest {
    @Test
    public void testTimeStrMsecNoTimeZoneEpoch(){
        long epochTime = 1632206980l;
        DateFormat timeFormat24MsecNoTimeZone = new SimpleDateFormat("HH:mm:ss.SSS");
        String expectedTime = timeFormat24MsecNoTimeZone.format(epochTime);
        String actualTime = Time.timeStrMsecNoTimeZone(epochTime);
        Assert.assertEquals(expectedTime, actualTime);
    }

    @Test
    public void testTimeStrMsecNoTimeZoneDate(){
        Date date = new Date(1632206980 );
        DateFormat timeFormat24MsecNoTimeZone = new SimpleDateFormat("HH:mm:ss.SSS");
        String expectedTime = timeFormat24MsecNoTimeZone.format(date);
        String actualTime = Time.timeStrMsecNoTimeZone(date);
        Assert.assertEquals(expectedTime, actualTime);
    }

    @Test
    public void testLateStartAfterMidnight() throws ParseException {
        Time time = new Time(ZoneId.systemDefault().getId());
        String string = "January 6, 2021 00:15:59";
        DateFormat format = new SimpleDateFormat("MMMM d, yyyy HH:mm:ss", Locale.ENGLISH);
        Date date = format.parse(string);

        // Seconds into day 25:05:00
        long epochTime = time.getEpochTime(90300, date);
        LocalDate expectedLocalDate = LocalDate.of(2021,01,06);
        LocalDate actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);


        // Seconds into day 24:00:00
        epochTime = time.getEpochTime(86400, date);

        expectedLocalDate = LocalDate.of(2021,01,06);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();

        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Seconds into day 23:59:59
        epochTime = time.getEpochTime(86399, date);

        expectedLocalDate = LocalDate.of(2021,01,05);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();

        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Seconds into day 19:33:19
        epochTime = time.getTripStartDate(70399, date);
        expectedLocalDate = LocalDate.of(2021,01,06);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Seconds into day 19:33:19
        epochTime = time.getTripStartDate(20399, date);
        expectedLocalDate = LocalDate.of(2021,01,06);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);


        // Seconds into day 06:40:00
        epochTime = time.getTripStartDate(24000, date);
        expectedLocalDate = LocalDate.of(2021,01,06);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Seconds into day is 03:20:00
        epochTime = time.getTripStartDate(12000, date);
        expectedLocalDate = LocalDate.of(2021,01,06);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);
    }

    @Test
    public void testGetEpochTimeBeforeMidnight() throws ParseException {
        Time time = new Time(ZoneId.systemDefault().getId());
        String string = "January 5, 2021 23:59:59";
        DateFormat format = new SimpleDateFormat("MMMM d, yyyy HH:mm:ss", Locale.ENGLISH);
        Date date = format.parse(string);

        // Seconds into day 25:05:00
        long epochTime = time.getEpochTime(90300, date);
        LocalDate expectedLocalDate = LocalDate.of(2021,01,06);
        LocalDate actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);


        // Seconds into day 24:00:00
        epochTime = time.getEpochTime(86400, date);

        expectedLocalDate = LocalDate.of(2021,01,06);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();

        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Seconds into day 23:59:59
        epochTime = time.getEpochTime(86399, date);

        expectedLocalDate = LocalDate.of(2021,01,05);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();

        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Seconds into day 19:33:19
        epochTime = time.getTripStartDate(70399, date);
        expectedLocalDate = LocalDate.of(2021,01,05);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Seconds into day 19:33:19
        epochTime = time.getTripStartDate(20399, date);
        expectedLocalDate = LocalDate.of(2021,01,05);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);


        // Seconds into day 06:40:00
        epochTime = time.getTripStartDate(24000, date);
        expectedLocalDate = LocalDate.of(2021,01,05);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Seconds into day is 03:20:00
        epochTime = time.getTripStartDate(12000, date);
        expectedLocalDate = LocalDate.of(2021,01,06);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);
    }

    @Test
    public void testGetEpochTimeMidnight() throws ParseException {
        Time time = new Time(ZoneId.systemDefault().getId());
        String string = "January 6, 2021 00:00:00";
        DateFormat format = new SimpleDateFormat("MMMM d, yyyy HH:mm:ss", Locale.ENGLISH);
        Date date = format.parse(string);

        LocalDate expectedLocalDate;
        LocalDate actualLocalDate;
        long epochTime;

        LocalDateTime expectedLocalDateTime;
        LocalDateTime actualLocalDateTime;

        // Seconds into day 25:05:00
        epochTime = time.getEpochTime(90300, date);
        expectedLocalDate = LocalDate.of(2021,01,06);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        expectedLocalDateTime = LocalDateTime.of(2021,01,06,01,05,00);
        actualLocalDateTime =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDateTime();
        Assert.assertEquals(expectedLocalDateTime, actualLocalDateTime);


        // Seconds into day 24:00:00
        epochTime = time.getEpochTime(86400, date);

        expectedLocalDate = LocalDate.of(2021,01,06);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();

        expectedLocalDateTime = LocalDateTime.of(2021,01,06,0,0,0);
        actualLocalDateTime =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDateTime();
        Assert.assertEquals(expectedLocalDateTime, actualLocalDateTime);

        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Seconds into day 23:59:59
        epochTime = time.getEpochTime(86399, date);

        expectedLocalDate = LocalDate.of(2021,01,05);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();

        Assert.assertEquals(expectedLocalDate, actualLocalDate);
    }

    /*
    trip_id,trip_start_date,trip_start_time,scheduled_start_time
    6893006,2021-09-27,2021-09-28 00:13:59,24:09:00
    6902601,2021-09-27,2021-09-28 00:14:37,24:00:00
    6928676,2021-09-28,2021-09-28 00:12:24,23:15:30
     */

    /**
     * CURRENT TIME Jan 5, 2021 23:15:59 ***
     *
     * Test Trip Start Date when current time is right before midnight
     * Especially useful to see how it handles start date right before
     * transitioning to a new date
     *
     * @throws ParseException
     */
    @Test
    public void testTripStartDateLateStartAfterMidnight() throws ParseException {
        Time time = new Time(ZoneId.systemDefault().getId());

        String string = "January 6, 2021 00:15:59";
        DateFormat format = new SimpleDateFormat("MMMM d, yyyy HH:mm:ss", Locale.ENGLISH);
        Date date = format.parse(string);

        // Current Time is Jan 5, 2021 23:59:59
        // Trip Start Time is 25:05:00
        // Expected Start Date is Jan 5, 2021
        long epochTime = time.getTripStartDate(90300, date);
        LocalDate expectedLocalDate = LocalDate.of(2021,01,05);
        LocalDate actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Current Time is Jan 5, 2021 23:59:59
        // Trip Start Time is 24:00:00
        // Expected Start Date is Jan 5, 2021
        epochTime = time.getTripStartDate(86400, date);
        expectedLocalDate = LocalDate.of(2021,01,05);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Current Time is Jan 5, 2021 23:59:59
        // Trip Start Time is 23:59:59
        // Expected Start Date is Jan 5, 2021
        epochTime = time.getTripStartDate(86399, date);
        expectedLocalDate = LocalDate.of(2021,01,05);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);


        // Current Time is Jan 5, 2021 23:59:59
        // Trip Start Time is 19:33:19
        // Expected Start Date is Jan 6, 2021
        // This is a tricky one since we have to decide whether this start time is actually in the past or in the future
        // In this case in this case the start time is so far ahead we assume its actually in the past
        epochTime = time.getTripStartDate(70399, date);
        expectedLocalDate = LocalDate.of(2021,01,06);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Current Time is Jan 5, 2021 23:59:59
        // Trip Start Time is 00:21:40
        // Expected Start Date is Jan 6, 2021
        epochTime = time.getTripStartDate(1300, date);
        expectedLocalDate = LocalDate.of(2021,01,06);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Current Time is Jan 5, 2021 23:59:59
        // Trip Start Time is 03:20:00
        // Expected Start Date is Jan 6, 2021
        // In this case the start time is close enough that we assume that its actually in the future
        epochTime = time.getTripStartDate(12000, date);
        expectedLocalDate = LocalDate.of(2021,01,06);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Current Time is Jan 5, 2021 23:59:59
        // Trip Start Time is 06:40:00
        // Expected Start Date is Jan 5, 2021
        // In this case the start time is still far enough in the future that we assume that its actually in the past
        epochTime = time.getTripStartDate(24000, date);
        expectedLocalDate = LocalDate.of(2021,01,06);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

    }

    /**
     * CURRENT TIME Jan 5, 2021 23:59:59 ***
     *
     * Test Trip Start Date when current time is right before midnight
     * Especially useful to see how it handles start date right before
     * transitioning to a new date
     *
     * @throws ParseException
     */
    @Test
    public void testTripStartDateRightBeforeMidnight() throws ParseException {
        Time time = new Time(ZoneId.systemDefault().getId());

        String string = "January 5, 2021 23:59:59";
        DateFormat format = new SimpleDateFormat("MMMM d, yyyy HH:mm:ss", Locale.ENGLISH);
        Date date = format.parse(string);

        // Current Time is Jan 5, 2021 23:59:59
        // Trip Start Time is 25:05:00
        // Expected Start Date is Jan 5, 2021
        long epochTime = time.getTripStartDate(90300, date);
        LocalDate expectedLocalDate = LocalDate.of(2021,01,05);
        LocalDate actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Current Time is Jan 5, 2021 23:59:59
        // Trip Start Time is 24:00:00
        // Expected Start Date is Jan 5, 2021
        epochTime = time.getTripStartDate(86400, date);
        expectedLocalDate = LocalDate.of(2021,01,05);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Current Time is Jan 5, 2021 23:59:59
        // Trip Start Time is 23:59:59
        // Expected Start Date is Jan 5, 2021
        epochTime = time.getTripStartDate(86399, date);
        expectedLocalDate = LocalDate.of(2021,01,05);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);


        // Current Time is Jan 5, 2021 23:59:59
        // Trip Start Time is 19:33:19
        // Expected Start Date is Jan 6, 2021
        // This is a tricky one since we have to decide whether this start time is actually in the past or in the future
        // In this case in this case the start time is so far ahead we assume its actually in the past
        epochTime = time.getTripStartDate(70399, date);
        expectedLocalDate = LocalDate.of(2021,01,05);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Current Time is Jan 5, 2021 23:59:59
        // Trip Start Time is 00:21:40
        // Expected Start Date is Jan 6, 2021
        epochTime = time.getTripStartDate(1300, date);
        expectedLocalDate = LocalDate.of(2021,01,06);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Current Time is Jan 5, 2021 23:59:59
        // Trip Start Time is 06:40:00
        // Expected Start Date is Jan 5, 2021
        // In this case the start time is still far enough in the future that we assume that its actually in the past
        epochTime = time.getTripStartDate(24000, date);
        expectedLocalDate = LocalDate.of(2021,01,05);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Current Time is Jan 5, 2021 23:59:59
        // Trip Start Time is 03:20:00
        // Expected Start Date is Jan 6, 2021
        // In this case the start time is close enough that we assume that its actually in the future
        epochTime = time.getTripStartDate(12000, date);
        expectedLocalDate = LocalDate.of(2021,01,06);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

    }

    /**
     * CURRENT TIME January 6, 2021 00:00:00 ***
     *
     * Test Trip Start Date when current time is right at midnight
     * Especially useful to see how it handles start date right after
     * transitioning to a new date
     *
     * @throws ParseException
     */
    @Test
    public void testTripStartDateMidnight() throws ParseException {
        Time time = new Time(ZoneId.systemDefault().getId());

        String string = "January 6, 2021 00:00:00";
        DateFormat format = new SimpleDateFormat("MMMM d, yyyy HH:mm:ss", Locale.ENGLISH);
        Date date = format.parse(string);

        // Current Time is Jan 6, 2021 00:00:00
        // Trip Start Time is 25:05:00
        // Expected Start Date is Jan 5, 2021
        long epochTime = time.getTripStartDate(90300, date);
        LocalDate expectedLocalDate = LocalDate.of(2021,01,05);
        LocalDate actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Current Time is Jan 6, 2021 00:00:00
        // Trip Start Time is 24:00:00
        // Expected Start Date is Jan 5, 2021
        epochTime = time.getTripStartDate(86400, date);
        expectedLocalDate = LocalDate.of(2021,01,05);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Current Time is Jan 6, 2021 00:00:00
        // Trip Start Time is 23:59:59
        // Expected Start Date is Jan 5, 2021
        epochTime = time.getTripStartDate(86399, date);
        expectedLocalDate = LocalDate.of(2021,01,05);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Current Time is Jan 6, 2021 00:05:01
        // Trip Start Time is 23:59:59
        // Expected Start Date is Jan 5, 2021
        epochTime = time.getTripStartDate(86399, date);
        expectedLocalDate = LocalDate.of(2021,01,05);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime + 301000).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Current Time is Jan 6, 2021 00:00:00
        // Trip Start Time is 19:33:19
        // Expected Start Date is Jan 6, 2021
        epochTime = time.getTripStartDate(70399, date);
        expectedLocalDate = LocalDate.of(2021,01,06);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Current Time is Jan 6, 2021 00:00:00
        // Trip Start Time is 00:21:40
        // Expected Start Date is Jan 6, 2021
        epochTime = time.getTripStartDate(1300, date);
        expectedLocalDate = LocalDate.of(2021,01,06);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Current Time is Jan 6, 2021 00:00:00
        // Trip Start Time is 06:40:00
        // Expected Start Date is Jan 6, 2021
        epochTime = time.getTripStartDate(24000, date);
        expectedLocalDate = LocalDate.of(2021,01,06);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

    }

    /**
     * CURRENT TIME January 6, 2021 03:59:59 ***
     *
     * Test Trip Start Date when current time is < 4 hours past midnight
     * Especially useful to test edge cases where how it handles start date right before
     * transitioning to a new date
     *
     * @throws ParseException
     */
    @Test
    public void testTripStartDateEarlyMorning() throws ParseException {
        Time time = new Time(TimeZone.getDefault().getID());

        String string = "January 6, 2021 03:59:59";
        DateFormat format = new SimpleDateFormat("MMMM d, yyyy HH:mm:ss", Locale.ENGLISH);
        Date date = format.parse(string);

        // Current Time is Jan 6, 2021 03:59:59
        // Trip Start Time is 25:05:00
        // Expected Start Date is Jan 5, 2021
        long epochTime = time.getTripStartDate(90300, date);
        LocalDate expectedLocalDate = LocalDate.of(2021,01,05);
        LocalDate actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Current Time is Jan 6, 2021 03:59:59
        // Trip Start Time is 24:00:00
        // Expected Start Date is Jan 6, 2021
        epochTime = time.getTripStartDate(86400, date);
        expectedLocalDate = LocalDate.of(2021,01,05);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // PAST TRIP START TIME
        // Current Time is Jan 6, 2021 03:59:59
        // Trip Start Time is 23:59:59
        // Expected Start Date is Jan 5, 2021
        epochTime = time.getTripStartDate(86399, date);
        expectedLocalDate = LocalDate.of(2021,01,05);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Current Time is Jan 6, 2021 03:59:59
        // Trip Start Time is 19:33:19
        // Expected Start Date is Jan 6, 2021
        epochTime = time.getTripStartDate(70399, date);
        expectedLocalDate = LocalDate.of(2021,01,06);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Current Time is Jan 6, 2021 03:59:59
        // Trip Start Time is 00:21:40
        // Expected Start Date is Jan 6, 2021
        epochTime = time.getTripStartDate(1300, date);
        expectedLocalDate = LocalDate.of(2021,01,06);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Current Time is Jan 6, 2021 03:59:59
        // Trip Start Time is 06:40:00
        // Expected Start Date is Jan 6, 2021
        epochTime = time.getTripStartDate(24000, date);
        expectedLocalDate = LocalDate.of(2021,01,06);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);
    }

    /**
     * CURRENT TIME January 6, 2021 04:00:00 ***
     *
     * Test Trip Start Date when current time is 4+ hours past midnight
     * Especially useful to test edge cases where how it handles start date right before
     * transitioning to a new date
     *
     * @throws ParseException
     */
    @Test
    public void testTripStartDateLateMorning() throws ParseException {
        Time time = new Time(TimeZone.getDefault().getID());

        String string = "January 6, 2021 04:00:00";
        DateFormat format = new SimpleDateFormat("MMMM d, yyyy HH:mm:ss", Locale.ENGLISH);
        Date date = format.parse(string);

        // Current Time is Jan 6, 2021 04:00:00
        // Trip Start Time is 25:05:20
        // Expected Start Date is Jan 6, 2021
        long epochTime = time.getTripStartDate(90300, date);
        LocalDate expectedLocalDate = LocalDate.of(2021,01,06);
        LocalDate actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Current Time is Jan 6, 2021 04:00:00
        // Trip Start Time is 24:00:00
        // Expected Start Date is Jan 6, 2021
        epochTime = time.getTripStartDate(86400, date);
        expectedLocalDate = LocalDate.of(2021,01,06);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Current Time is Jan 6, 2021 04:00:00
        // Trip Start Time is 23:59:59
        // Expected Start Date is Jan 5, 2021
        epochTime = time.getTripStartDate(86399, date);
        expectedLocalDate = LocalDate.of(2021,01,06);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Current Time is Jan 6, 2021 04:00:00
        // Trip Start Time is 19:33:19
        // Expected Start Date is Jan 6, 2021
        epochTime = time.getTripStartDate(70399, date);
        expectedLocalDate = LocalDate.of(2021,01,06);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Current Time is Jan 6, 2021 04:00:00
        // Trip Start Time is Jan 6, 2021 00:21:40
        // Expected Start Date is Jan 6, 2021
        epochTime = time.getTripStartDate(1300, date);
        expectedLocalDate = LocalDate.of(2021,01,06);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);

        // Current Time is Jan 6, 2021 04:00:00
        // Trip Start Time is 06:40:00
        // Expected Start Date is Jan 6, 2021
        epochTime = time.getTripStartDate(24000, date);
        expectedLocalDate = LocalDate.of(2021,01,06);
        actualLocalDate =
                Instant.ofEpochMilli(epochTime).atZone(ZoneId.systemDefault()).toLocalDate();
        Assert.assertEquals(expectedLocalDate, actualLocalDate);
    }
}
