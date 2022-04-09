package org.transitclock.utils;

import org.transitclock.config.StringConfigValue;
import org.transitclock.db.structs.Trip;

import java.util.Calendar;
import java.util.Date;

/**
 * collection of helpful Date conversions.
 */
public class DateUtils {

  public static StringConfigValue HOLIDAY_REGEX
          = new StringConfigValue("transitclock.apc.holidayRegex",
          "Holiday",
          "tripid regex to determine if holiday");

  // similar ot ServiceType, but needs to consider holidays
  public enum CalendarType {
    WEEKDAY, SATURDAY, SUNDAY, HOLIDAY;

    public boolean isWeekend() {
      return this.equals(CalendarType.SATURDAY)
              || this.equals(CalendarType.SUNDAY);
    }
  }

  /**
   * Round the given date down to a given bucket precision.  useful for
   * indexing off lower precisions dates for cache lookups.  Example:
   *
   * dateBinning(toDate("2021-05-25 12:03:01"), Calendar.MINUTE, 15);
   * -> returns "2021-05-25 12:00.xx"
   * dateBinning(toDate("2021-05-25 12:26:09"), Calendar.MINUTE, 15);
   * -> returns "2021-05-25 12:15.xx"
   * dateBinning(toDate("2021-05-25 12:37:09"), Calendar.MINUTE, 15);
   * -> returns "2021-05-25 12:30.xx"
   * dateBinning(toDate("2021-05-25 12:59:59"), Calendar.MINUTE, 15);
   * -> returns "2021-05-25 12:45.xx"
   * @param input
   * @param field
   * @param binWidth
   * @return
   */
  public static Date dateBinning(Date input, int field, int binWidth) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(input);
    int oldValue = cal.get(field);
    int newValue = oldValue / binWidth;
    cal.set(field, newValue*binWidth);

    // now zero out remaining fields
    for (int i = field + 1; i <= Calendar.MILLISECOND; i++) {
      cal.set(i, 0);
    }

    return cal.getTime();
  }

  public static Date truncate(Date input, int field) {
    return org.apache.commons.lang3.time.DateUtils.truncate(input, field);
  }

  public static long dateBinning(long time, int binWidthInMillis) {
    return new Double(Math.floor(new Double(time).doubleValue()/binWidthInMillis) * binWidthInMillis).longValue();
  }

  public static Date addDays(Date time, int days) {
    return org.apache.commons.lang3.time.DateUtils.addDays(time, days);
  }

  public static Date getPreviousDayForArrivalTime(Date arrivalTime, boolean isHoliday) {
    CalendarType currentType = getTypeForDate(arrivalTime, isHoliday);

    Date previousDay = addDays(arrivalTime, -1);
    while (!getTypeForDate(previousDay).equals(currentType)) {
      previousDay = org.apache.commons.lang3.time.DateUtils.addDays(previousDay, -1);
    }
    return previousDay;
  }

  public static boolean isHoliday(Trip trip) {
    return trip.getId().contains(HOLIDAY_REGEX.getValue());
  }

  public static CalendarType getTypeForDate(Date instanceTime) {
    Calendar instance = Calendar.getInstance();
    instance.setTime(instanceTime);
    if (Calendar.SUNDAY == (instance.get(Calendar.DAY_OF_WEEK))) {
      return CalendarType.SUNDAY;
    }
    if (Calendar.SATURDAY == (instance.get(Calendar.DAY_OF_WEEK))) {
      return CalendarType.SATURDAY;
    }
    return CalendarType.WEEKDAY;
  }

  // special case of get getType matching holidays to sundays
  public static CalendarType getTypeForDate(Date instanceTime, boolean isHoliday) {
    Calendar instance = Calendar.getInstance();
    instance.setTime(instanceTime);
    if (Calendar.SUNDAY == (instance.get(Calendar.DAY_OF_WEEK)) || isHoliday) {
      return CalendarType.SUNDAY;
    }
    if (Calendar.SATURDAY == (instance.get(Calendar.DAY_OF_WEEK))) {
      return CalendarType.SATURDAY;
    }
    return CalendarType.WEEKDAY;
  }

}
