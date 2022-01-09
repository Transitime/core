package org.transitclock.avl;

import com.amazonaws.services.sqs.model.Message;

import java.util.List;

public interface ApcMessageUnmarshaller {
  List<ApcParsedRecord> toApcRecord(Message message, String serviceDateTz, String timestampTz);
  List<ApcParsedRecord> toApcRecord(String body, String serviceDateTz, String timestampTz);
  String toString(Message message);
}
