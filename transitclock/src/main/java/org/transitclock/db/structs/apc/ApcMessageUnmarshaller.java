package org.transitclock.db.structs.apc;

import com.amazonaws.services.sqs.model.Message;
import org.transitclock.db.structs.ApcRecord;

import java.util.List;

public interface ApcMessageUnmarshaller {
  List<ApcRecord> toApcRecord(Message message, String serviceDateTz, String timestampTz);
  List<ApcRecord> toApcRecord(String body, String serviceDateTz, String timestampTz);
  String toString(Message message);
}
