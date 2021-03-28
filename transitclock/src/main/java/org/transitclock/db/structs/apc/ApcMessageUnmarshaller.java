package org.transitclock.db.structs.apc;

import com.amazonaws.services.sqs.model.Message;
import org.json.JSONObject;
import org.transitclock.db.structs.ApcRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Unmarshal messages into ApcRecord format.
 */
public class ApcMessageUnmarshaller {

  public List<ApcRecord> toApcRecord(Message message) {
    List<ApcRecord> records = new ArrayList<>();
    String body = message.getBody();
    return toApcRecord(body);
  }

  public List<ApcRecord> toApcRecord(String body) {
    JSONObject wrapper = new JSONObject(body);
    return null;
  }
}
