package org.transitclock.utils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Simple POJO to represent a date range.
 * If Date is in the format "2020-01-02 12:34:56,2021-01-02 12:34:56"
 * it will be parsed into a list of DateRanges.
 */
public class DateRange {
  private Date start;
  private Date end;

  public DateRange(Date start, Date end) {
    this.start = start;
    this.end = end;
  }

  public static List<DateRange> parseFromCSV(String startStr, String endStr) {
    ArrayList<DateRange> ranges = new ArrayList<>();
    if (startStr == null || startStr.length() == 0)
      return ranges;
    if (endStr == null || endStr.length() == 0)
      return ranges;

    String[] startStrArray = startStr.split(",");
    String[] endStrArray = endStr.split(",");

    if (startStrArray.length != endStrArray.length)
      return ranges;

    for (int i=0; i< startStrArray.length; i++) {
      try {
        ranges.add(new DateRange(
                Time.parse(startStrArray[i]),
                Time.parse(endStrArray[i])
        ));
      } catch (ParseException ex) {
        // bury
      }
    }

    return ranges;
  }

  public Date getStart() {
    return start;
  }

  public void setStart(Date start) {
    this.start = start;
  }

  public Date getEnd() {
    return end;
  }

  public void setEnd(Date end) {
    this.end = end;
  }

  @Override
  public String toString() {
    return "" + start + " -> " + end;
  }
}
