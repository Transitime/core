package org.transitclock;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Helper methods for working with Structs in Unit Tests.
 */
public class StructSupport {
  protected long dateToLong(String[] split, int index, String tz) {
    SimpleDateFormat sdf = new SimpleDateFormat(patternForLength(split[index].length()));
    if (tz != null) sdf.setTimeZone(TimeZone.getTimeZone(tz));
    Date parse = null;
    try {
      parse = sdf.parse(split[index]);
    } catch (ParseException e) {
      // bury
      return -1;
    }
    return parse.getTime();
  }

  protected String patternForLength(int length) {
    switch (length) {
      case 21:
        return "yyyy-MM-dd HH:mm:ss.S";
      case 19:
      default:
        return "yyyy-MM-dd HH:mm:ss";
    }
  }

  protected float toFloat(String[] split, int index) {
    return Float.parseFloat(split[index]);
  }

  protected Long toLong(String[] split, int index) {
    if (split[index] == null || "NULL".equals(split[index])) return -1l;
    return Long.parseLong(split[index]);
  }

  protected boolean toBoolean(String[] split, int index) {
    return "1".equals(split[index]);
  }

  protected Double toDouble(String [] split, int index) { return Double.parseDouble(split[index]); }

  protected int toInt(String[] split, int index) {
    return Integer.parseInt(split[index]);
  }

}
