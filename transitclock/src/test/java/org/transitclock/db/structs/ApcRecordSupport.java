package org.transitclock.db.structs;

import org.transitclock.db.structs.apc.SimpleApcMessageUnmarshaller;

import java.io.InputStream;
import java.util.List;

import static org.transitclock.TestSupport.getStreamAsString;

/**
 * convenience methods for unit testing APCRecords.
 */
public class ApcRecordSupport {

  private SimpleApcMessageUnmarshaller unmarshaller = new SimpleApcMessageUnmarshaller();

  public List<ApcRecord> loadApcRecords(String s) throws Exception {
    InputStream is1 = this.getClass().getResourceAsStream(s);
    // apc data in UTC TZ
    return unmarshaller.toApcRecord(getStreamAsString(is1), "CST","UTC");
  }

}
