package org.transitclock.db.structs;

import com.amazonaws.services.sqs.model.Message;
import org.transitclock.SingletonSupport;
import org.transitclock.avl.ApcParsedRecord;
import org.transitclock.avl.SimpleApcMessageUnmarshaller;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

import static org.transitclock.SingletonSupport.getStreamAsString;

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
    SingletonSupport.writeToFile(filename, sb.toString());
  }

  public List<ApcParsedRecord> loadApcRecords(String s) throws Exception {
    InputStream is1;
    if (s.endsWith(".zip")) {
      ZipInputStream zin = new ZipInputStream(this.getClass().getResourceAsStream(s));
      zin.getNextEntry();
      is1 = zin;
    } else {
      is1 = this.getClass().getResourceAsStream(s);
    }
    if (is1 == null)
      throw new FileNotFoundException("File " + s + " not found!");
    // apc data in UTC TZ
    String fileContents = getStreamAsString(is1);
    if (fileContents.split("\\]\\[").length > 1) {
      fileContents = fileContents.substring(1, fileContents.length()-1);
      List<ApcParsedRecord> allRecords = new ArrayList<>();
      int i = 0;
      for (String s0 : fileContents.split("\\]\\[")) {
        s0 = "[" + s0 + "]";
        allRecords.addAll(unmarshaller.toApcRecord(s0,"CST","UTC"));
        i++;
      }
      return allRecords;
    }
    return unmarshaller.toApcRecord(fileContents, "CST","UTC");
  }

}
