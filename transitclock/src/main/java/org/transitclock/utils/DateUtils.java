package org.transitclock.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * collection of helpful Date conversions.
 */
public class DateUtils {

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

}
