package org.transitclock.utils;

import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.*;
import static org.transitclock.SingletonSupport.toDate;

public class DateUtilsTest {

  @Test
  public void dateBinning() throws Exception {
    // BUCKET 1
    assertEquals(
            toDate("2021-05-25", "12:00:00", "EST"),
            DateUtils.dateBinning(toDate("2021-05-25", "12:00:00", "EST"),
                    Calendar.MINUTE, 15));

    assertEquals(
            toDate("2021-05-25", "12:00:00", "EST"),
            DateUtils.dateBinning(toDate("2021-05-25", "12:00:01", "EST"),
                    Calendar.MINUTE, 15));

    assertEquals(
            toDate("2021-05-25", "12:00:00", "EST"),
            DateUtils.dateBinning(toDate("2021-05-25", "12:07:36", "EST"),
                    Calendar.MINUTE, 15));

    assertEquals(
            toDate("2021-05-25", "12:00:00", "EST"),
            DateUtils.dateBinning(toDate("2021-05-25", "12:14:59", "EST"),
                    Calendar.MINUTE, 15));

    // BUCKET 2
    assertEquals(
            toDate("2021-05-25", "12:15:00", "EST"),
            DateUtils.dateBinning(toDate("2021-05-25", "12:15:00", "EST"),
                    Calendar.MINUTE, 15));

    assertEquals(
            toDate("2021-05-25", "12:15:00", "EST"),
            DateUtils.dateBinning(toDate("2021-05-25", "12:20:39", "EST"),
                    Calendar.MINUTE, 15));

    assertEquals(
            toDate("2021-05-25", "12:15:00", "EST"),
            DateUtils.dateBinning(toDate("2021-05-25", "12:29:59", "EST"),
                    Calendar.MINUTE, 15));

    // BUCKET 3
    assertEquals(
            toDate("2021-05-25", "12:30:00", "EST"),
            DateUtils.dateBinning(toDate("2021-05-25", "12:30:00", "EST"),
                    Calendar.MINUTE, 15));

    assertEquals(
            toDate("2021-05-25", "12:30:00", "EST"),
            DateUtils.dateBinning(toDate("2021-05-25", "12:35:39", "EST"),
                    Calendar.MINUTE, 15));

    assertEquals(
            toDate("2021-05-25", "12:30:00", "EST"),
            DateUtils.dateBinning(toDate("2021-05-25", "12:44:59", "EST"),
                    Calendar.MINUTE, 15));

    // BUCKET 4
    assertEquals(
            toDate("2021-05-25", "12:45:00", "EST"),
            DateUtils.dateBinning(toDate("2021-05-25", "12:45:00", "EST"),
                    Calendar.MINUTE, 15));

    assertEquals(
            toDate("2021-05-25", "12:45:00", "EST"),
            DateUtils.dateBinning(toDate("2021-05-25", "12:55:39", "EST"),
                    Calendar.MINUTE, 15));

    assertEquals(
            toDate("2021-05-25", "12:45:00", "EST"),
            DateUtils.dateBinning(toDate("2021-05-25", "12:59:59", "EST"),
                    Calendar.MINUTE, 15));

  }


}