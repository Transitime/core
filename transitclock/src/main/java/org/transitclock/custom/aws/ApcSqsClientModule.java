package org.transitclock.custom.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.avl.ApcParsedRecord;
import org.transitclock.config.ClassConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.avl.ApcMessageUnmarshaller;
import org.transitclock.avl.SimpleApcMessageUnmarshaller;
import org.transitclock.modules.Module;
import org.transitclock.monitoring.MonitoringService;

import java.util.List;

/**
 * Reads APC data from AWS SQS topic, deserialized it, and processes it.
 */
public class ApcSqsClientModule extends Module {
  private static final Logger logger =
          LoggerFactory.getLogger(ApcSqsClientModule.class);

  private static final int DEFAULT_MESSAGE_LOG_FREQUENCY = 100;
  private AWSCredentials _sqsCredentials;
  private AmazonSQS _sqs;
  private AmazonSNSClient _sns = null;
  private String _url = null;
  private ApcMessageUnmarshaller _messageUnmarshaller;
  private MonitoringService monitoring;


  private static IntegerConfigValue messageLogFrequency =
          new IntegerConfigValue("transitclock.apc.messageLogFrequency",
                  DEFAULT_MESSAGE_LOG_FREQUENCY,
                  "How often (in count of message) a log message is output " +
                          "confirming messages have been received");

  private static StringConfigValue apcUrl =
          new StringConfigValue("transitclock.apc.sqsUrl", null, "The SQS URL from AWS");

  private static IntegerConfigValue apcPauseTimeInSeconds =
          new IntegerConfigValue("transitclock.apc.pauseTime",
                  20,
                  "time to block between updates.  Longer means less AWS costs " +
                          "at the risk of latency due to a failed network connection.  AWS allows 0-20.");
  private static StringConfigValue sqsKey =
          new StringConfigValue("transitclock.apc.sqsKey", null, "The AWS Key with SQS read access");

  private static StringConfigValue sqsSecret =
          new StringConfigValue("transitclock.apc.sqsSecret", null, "The AWS Secret with SQS read access");

  private static StringConfigValue sqsRegion =
          new StringConfigValue("transitclock.apc.sqsRegion", "us-east-1", "The AWS region hosting the SQS queue");
  private static StringConfigValue snsKey =
          new StringConfigValue("transitclock.apc.snsKey", null, "The AWS Key with SNS write access");

  private static StringConfigValue snsSecret =
          new StringConfigValue("transitclock.apc.snsSecret", null, "The AWS Secret with SNS write access");

  private static StringConfigValue snsArn =
          new StringConfigValue("transitclock.apc.snsArn", null, "The AWS SNS ARN to write to");

  private static ClassConfigValue unmarshallerConfig =
          new ClassConfigValue("transitclock.apc.unmarshaller", SimpleApcMessageUnmarshaller.class,
                  "Implementation of ApcMessageUnmarshaller to perform " +
                          "the deserialization of SQS Message objects into AVLReport objects");

  private static StringConfigValue apcServiceDateTimeZone =
          new StringConfigValue("transitclock.apc.serviceDateTz",
                  "CST",
                  "TimeZone of incoming APC messages");

  private static StringConfigValue apcTimeStampTimeZone =
          new StringConfigValue("transitclock.apc.timestampTz",
                  "UTC",
                  "TimeZone of incoming APC messages");

  private boolean shutdown = false;
  // listener for new messages -- to audit/log, etc.
  private SqsCallback callback = null;
  private int messageCount = 0;
  private long messageStartTime = System.currentTimeMillis();

  /**
   * Constructor. Subclasses must implement a constructor that takes
   * in agencyId and calls this constructor via super(agencyId).
   *
   * @param agencyId
   */
  public ApcSqsClientModule(String agencyId) throws Exception {
  this(agencyId, null);
  }

  // testing constructor using optional callback
  public ApcSqsClientModule(String agencyId, SqsCallback callback) throws Exception {
    super(agencyId);
    this.callback = callback;
    monitoring = MonitoringService.getInstance();
    // if we've been configured via optionalModulesList then the following properties need to be set!
    // deliberately halt the application launch otherwise to make it obvious
    if (sqsKey.getValue() == null || sqsSecret.getValue() == null || apcUrl.getValue() == null) {
      throw new IllegalStateException("ApcSqsClientModule invalid configuration, expecting sqsKey (" +
              sqsKey.getValue() +
              "), sqsSecret (" +
              sqsSecret.getValue() + "), " +
              "and apcUrl (" +
              apcUrl.getValue() + ") to be populated");
    }
    _sqsCredentials = new BasicAWSCredentials(sqsKey.getValue(), sqsSecret.getValue());
    connect();

    _url = apcUrl.getValue();

    _messageUnmarshaller = (ApcMessageUnmarshaller) unmarshallerConfig.getValue().newInstance();
  }

  private void connect() {
    _sqs = new AmazonSQSClient(_sqsCredentials);
    Region defaultRegion = Region.getRegion(Regions.fromName(sqsRegion.getValue()));
    _sqs.setRegion(defaultRegion);

    // now setup _sns if credentials present
    if (!StringUtils.isEmpty(snsKey.getValue())
            && !StringUtils.isEmpty(snsSecret.getValue())
            && !StringUtils.isEmpty(snsArn.getValue())) {
      try {
        logger.info("creating sns connection for archiving to ARN {}", snsArn.getValue());
        _sns = new AmazonSNSClient(new BasicAWSCredentials(snsKey.getValue(), snsSecret.getValue()));
      } catch (Exception any) {
        // SNS topic failure is non-fatal
        logger.error("failed to create sns client: {}", any);
        _sns = null;
      }
    } else {
      logger.info("sns configuration not set, skipping.");
    }

  }

  public void shutdown() {
    shutdown = true;
  }

  @Override
  public void run() {

    while (!Thread.interrupted() && !shutdown) {
      try {
        processAPCDataFromSQS();
      } catch (Throwable e) {
        logger.error("Exception processing apc data {}:", e, e);
      }
    }

  }

  private void processAPCDataFromSQS() {
    // we do this work on the module thread as this is expected to be low volume

    ReceiveMessageRequest request = new ReceiveMessageRequest(_url);
    request.setWaitTimeSeconds(apcPauseTimeInSeconds.getValue());
    List<Message> messages = _sqs.receiveMessage(request).getMessages();
    messageCount = messageCount + messages.size();
    monitoring.averageMetric("PredictionApcInputRecords", messages.size());
    replicateMessages(messages);

    List<ApcParsedRecord> apcRecords = null;
    Message firstMessage = null;
    for (Message message : messages) {
      if (firstMessage == null) firstMessage = message;
      apcRecords = _messageUnmarshaller.toApcRecord(message,
              apcServiceDateTimeZone.getValue(),
              apcTimeStampTimeZone.getValue());
    }
    if (callback != null) {
      callback.receiveRawMessages(messages);
      callback.receiveApcRecords(apcRecords);
    }
    acknowledge(firstMessage);
    archiveMessages(apcRecords);


    logStatus();
  }

  private void archiveMessages(List<ApcParsedRecord> records) {
    if (records == null) return;
    for (ApcParsedRecord record : records) {
      Core.getInstance().getDbLogger().add(record.toApcReport());
    }
    monitoring.averageMetric("PredictionApcParsedRecords", records.size());
  }

  private void logStatus() {
    if (messageCount % messageLogFrequency.getValue() == 0) {
      long delta = (System.currentTimeMillis() - messageStartTime)/1000;
      long rate = 0;
      if (delta != 0) {
        rate = messageCount / delta;
      }
      logger.info("received {} messages in {} delta seconds ({}/s}",
              messageCount,
              delta,
              rate);
      messageStartTime = System.currentTimeMillis();
      messageCount = 0;
    }
  }

  private void acknowledge(Message message) {
    if (message != null) {
      String messageReceiptHandle = message.getReceiptHandle();
      try {
        _sqs.deleteMessage(new DeleteMessageRequest(_url, messageReceiptHandle));
      } catch (Exception any) {
        logger.error("unable to mark message as received", any);
      }
    }
  }

  /**
   * post to SNS topic so messages can be replicated.
   * @param messages
   */
  private void replicateMessages(List<Message> messages) {
    if (messages == null
            || messages.isEmpty()
            || _sns == null) return;
    for (Message message : messages) {
      try {
        PublishRequest request = new PublishRequest();
        request.setTopicArn(snsArn.getValue());
        request.setMessage(_messageUnmarshaller.toString(message));
        _sns.publish(request);
      } catch (Exception any) {
        logger.error("issue archiving message {}: ", message, any);
      }
    }
  }
}
