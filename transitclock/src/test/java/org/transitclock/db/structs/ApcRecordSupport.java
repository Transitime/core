package org.transitclock.db.structs;

import com.amazonaws.services.sqs.model.Message;
import org.transitclock.TestSupport;
import org.transitclock.avl.ApcParsedRecord;
import org.transitclock.avl.SimpleApcMessageUnmarshaller;

import java.io.InputStream;
import java.util.List;

import static org.transitclock.TestSupport.getStreamAsString;

/**
 * convenience methods for unit testing APCRecords.
 */
public class ApcRecordSupport {

  private SimpleApcMessageUnmarshaller unmarshaller = new SimpleApcMessageUnmarshaller();

  public void writeToFile(String filename, List<Message> records) throws Exception {
    StringBuffer sb = new StringBuffer();
    for (Message message : records) {
      sb.append(unmarshaller.toString(message));
    }
    TestSupport.writeToFile(filename, sb.toString());
  }

  public List<ApcParsedRecord> loadApcRecords(String s) throws Exception {
    InputStream is1 = this.getClass().getResourceAsStream(s);
    // apc data in UTC TZ
    return unmarshaller.toApcRecord(getStreamAsString(is1), "CST","UTC");
  }

}
