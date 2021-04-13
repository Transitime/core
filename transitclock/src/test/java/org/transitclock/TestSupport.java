package org.transitclock;

import org.junit.Ignore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

@Ignore
/**
 * Helper methods for Unit Tests.
 */
public class TestSupport {

  public static String getStreamAsString(InputStream inputStream) throws Exception {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    int nRead;
    byte[] data = new byte[1024];
    while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
      buffer.write(data, 0, nRead);
    }

    buffer.flush();
    byte[] byteArray = buffer.toByteArray();

    String text = new String(byteArray, StandardCharsets.UTF_8);
    return text;
  }

  public static long toEpoch(String date, String time) throws Exception {
    return toEpoch(date, time, null);
  }

  public static long toEpoch(String date, String time, String tz) throws Exception {
    SimpleDateFormat sdf;
    if (time == null) {
      sdf = new SimpleDateFormat("yyyy-MM-dd");
      if (tz != null) sdf.setTimeZone(TimeZone.getTimeZone(tz));
      return sdf.parse(date).getTime();
    }
    sdf = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
    if (tz != null) sdf.setTimeZone(TimeZone.getTimeZone(tz));
    return sdf.parse(date+time).getTime();

  }


}
