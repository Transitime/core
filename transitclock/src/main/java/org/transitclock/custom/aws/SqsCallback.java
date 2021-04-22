package org.transitclock.custom.aws;

import com.amazonaws.services.sqs.model.Message;
import org.transitclock.avl.ApcParsedRecord;

import java.util.List;

public interface SqsCallback {

  void receiveRawMessages(List<Message> messages);
  void receiveApcRecords(List<ApcParsedRecord> records);

}
