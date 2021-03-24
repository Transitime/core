package org.transitclock.db.structs.apc;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import org.transitclock.config.ClassConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.db.structs.ApcRecord;
import org.transitclock.modules.Module;
import org.transitclock.monitoring.MonitoringService;

import java.util.ArrayList;
import java.util.List;

/**
 * Integrate with Automated Passenger Count data, parse, archive, and
 * feed into dwell time calculations.
 */
public class ApcModule extends Module {
  private static StringConfigValue apcUrl =
          new StringConfigValue("transitclock.apc.sqsUrl", null, "The SQS URL from AWS for APC");

  private static StringConfigValue sqsKey =
          new StringConfigValue("transitclock.apc.sqsKey", null, "The AWS Key with SQS read access");

  private static StringConfigValue sqsSecret =
          new StringConfigValue("transitclock.apc.sqsSecret", null, "The AWS Secret with SQS read access");

  private static ClassConfigValue unmarshallerConfig =
          new ClassConfigValue("transitclock.apc.unmarshaller", ApcMessageUnmarshaller.class,
                  "Implementation of SqsMessageUnmarshaller to perform " +
                          "the deserialization of SQS Message objects into AVLReport objects");
  private static IntegerConfigValue apcPauseTimeInSeconds =
          new IntegerConfigValue("transitclock.apc.sleep",
                  10,
                  "time to wait on a message");


  private AWSCredentials _sqsCredentials;
  private AmazonSQS _sqs;
  private String _url = null;
  private MonitoringService monitoring;
  private ApcMessageUnmarshaller _messageUnmarshaller;
  /**
   * Constructor. Subclasses must implement a constructor that takes
   * in agencyId and calls this constructor via super(agencyId).
   *
   * @param agencyId
   */
  protected ApcModule(String agencyId) throws Exception {
    super(agencyId);
    monitoring = MonitoringService.getInstance();
    _sqsCredentials = new BasicAWSCredentials(sqsKey.getValue(), sqsSecret.getValue());
    _url = apcUrl.getValue();
    connect();
    _messageUnmarshaller = (ApcMessageUnmarshaller)  unmarshallerConfig.getValue().newInstance();
  }

  private void connect() {
    _sqs = new AmazonSQSClient(_sqsCredentials);
    Region usEast1 = Region.getRegion(Regions.US_EAST_1);
    _sqs.setRegion(usEast1);
  }

  @Override
  public void run() {
    while (!Thread.interrupted()) {
      try {
        List<Message> messages = receiveBlocking();
        List<ApcRecord> apcRecords = parseMessages(messages);
        processRecords(apcRecords);
      } catch (Exception e) {
        logger.error("issue with data:", e);
      }
    }

  }

  private void processRecords(List<ApcRecord> apcRecords) {
    //TODO -- the hard work
  }

  private List<ApcRecord> parseMessages(List<Message> messages) {
    List<ApcRecord> records = new ArrayList<>();
    if (messages == null) return records;
    for (Message message : messages) {
      records.addAll(_messageUnmarshaller.toApcRecord(message));
    }
    return records;
  }

  private List<Message> receiveBlocking() {
    ReceiveMessageRequest request = new ReceiveMessageRequest(_url);
    request.setWaitTimeSeconds(apcPauseTimeInSeconds.getValue());
    List<Message> messages = _sqs.receiveMessage(request).getMessages();
    if (messages != null && !messages.isEmpty()) acknowledge(messages);
    return messages;
  }

  private void acknowledge(List<Message> messages) {
    Message firstMessage = messages.get(0);
    String messageReceiptHandle = firstMessage.getReceiptHandle();
    try {
      _sqs.deleteMessage(new DeleteMessageRequest(_url, messageReceiptHandle));
    } catch (Exception any) {
      logger.error("unable to mark message as received: ", any);
    }

  }


}
