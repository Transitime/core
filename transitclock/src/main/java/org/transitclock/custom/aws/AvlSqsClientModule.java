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
import org.transitclock.avl.AvlClient;
import org.transitclock.config.ClassConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.core.AvlProcessor;
import org.transitclock.modules.Module;
import org.transitclock.monitoring.MonitoringService;
import org.transitclock.utils.threading.BoundedExecutor;
import org.transitclock.utils.threading.NamedThreadFactory;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Reads AVL data from AWS SQS topic, deserializes it, and process it
 * following the patter established by AvlJmsClientModule. 
 *
 */
public class AvlSqsClientModule extends Module {
  private static final Logger logger = 
      LoggerFactory.getLogger(AvlSqsClientModule.class);
  
  private final BoundedExecutor _avlClientExecutor;
  private AWSCredentials _sqsCredentials;
  private AmazonSQS _sqs;
  private AmazonSNSClient _sns = null;
  private String _url = null;
  private SqsMessageUnmarshaller _messageUnmarshaller;
  private int _messageCount = 0;
  private long _messageStart = System.currentTimeMillis();
  private ArrayBlockingQueue<Message> _receiveQueue;
  private ArrayBlockingQueue<Message> _deserializeQueue;
  private ArrayBlockingQueue<Message> _acknowledgeQueue;
  private ArrayBlockingQueue<Message> _archiveQueue;
  private MonitoringService monitoring;
  
  private final static int MAX_THREADS = 100;

  private static final int DEFAULT_MESSAGE_LOG_FREQUENCY = 10000;

  
  private static IntegerConfigValue avlQueueSize = 
      new IntegerConfigValue("transitclock.avl.jmsQueueSize", 350,
          "How many items to go into the blocking AVL queue "
          + "before need to wait for queue to have space. "
          + "Only for when JMS is used.");

  private static IntegerConfigValue numAvlThreads = 
      new IntegerConfigValue("transitclock.avl.jmsNumThreads", 1,
          "How many threads to be used for processing the AVL " +
          "data. For most applications just using a single thread " +
          "is probably sufficient and it makes the logging simpler " +
          "since the messages will not be interleaved. But for " +
          "large systems with lots of vehicles then should use " +
          "multiple threads, such as 3-5 so that more of the cores " +
          "are used. Only for when JMS is used.");

  private static IntegerConfigValue jmsPauseTimeInSeconds =
          new IntegerConfigValue("transitclock.avl.jmsPauseTimeInSeconds", 20,
                  "How long to block for the next message.");


  private static IntegerConfigValue messageLogFrequency =
      new IntegerConfigValue("transitclock.avl.messageLogFrequency", 
          DEFAULT_MESSAGE_LOG_FREQUENCY, 
          "How often (in count of message) a log message is output " +
          "confirming messages have been received");
  
  private static StringConfigValue avlUrl =
      new StringConfigValue("transitclock.avl.sqsUrl", null, "The SQS URL from AWS");

  private static StringConfigValue sqsKey =
      new StringConfigValue("transitclock.avl.sqsKey", null, "The AWS Key with SQS read access");

  private static StringConfigValue sqsSecret =
      new StringConfigValue("transitclock.avl.sqsSecret", null, "The AWS Secret with SQS read access");

  
  private static StringConfigValue snsKey =
      new StringConfigValue("transitclock.avl.snsKey", null, "The AWS Key with SNS write access");

  private static StringConfigValue snsSecret =
      new StringConfigValue("transitclock.avl.snsSecret", null, "The AWS Secret with SNS write access");
  
  private static StringConfigValue snsArn =
      new StringConfigValue("transitclock.avl.snsArn", null, "The AWS SNS ARN to write to");

  private static ClassConfigValue unmarshallerConfig =
      new ClassConfigValue("transitclock.avl.unmarshaller", WmataAvlTypeUnmarshaller.class, 
          "Implementation of SqsMessageUnmarshaller to perform " + 
      "the deserialization of SQS Message objects into AVLReport objects");

  private static StringConfigValue applicableList =
      new StringConfigValue("transitclock.avl.filterVehicleIdList", null, "List of vehicle Ids to filter on");
  
    public AvlSqsClientModule(String agencyId) throws Exception {
      super(agencyId);
      monitoring = MonitoringService.getInstance();
      logger.info("loading AWS SQS credentials from environment");
      _sqsCredentials = new BasicAWSCredentials(sqsKey.getValue(), sqsSecret.getValue());
      connect();

      int maxAVLQueueSize = avlQueueSize.getValue();
      int numberThreads = numAvlThreads.getValue();
      _url = avlUrl.getValue();
      
      logger.info("Starting AvlClient for agencyId={} with "
          + "maxAVLQueueSize={}, numberThreads={} and url={}", agencyId,
          maxAVLQueueSize, numberThreads, _url);

      // Make sure that numberThreads is reasonable
      if (numberThreads < 1) {
        logger.error("Number of threads must be at least 1 but {} was "
            + "specified. Therefore using 1 thread.", numberThreads);
        numberThreads = 1;
      }
      if (numberThreads > MAX_THREADS) {
        logger.error("Number of threads must be no greater than {} but "
            + "{} was specified. Therefore using {} threads.",
            MAX_THREADS, numberThreads, MAX_THREADS);
        numberThreads = MAX_THREADS;
      }

      
      // Create the executor that actually processes the AVL data
      NamedThreadFactory avlClientThreadFactory = new NamedThreadFactory(
          "avlClient");
      Executor executor = Executors.newFixedThreadPool(numberThreads,
          avlClientThreadFactory);
      _avlClientExecutor = new BoundedExecutor(executor, maxAVLQueueSize);
      _receiveQueue = new ArrayBlockingQueue<Message>(maxAVLQueueSize);
      _deserializeQueue = new ArrayBlockingQueue<Message>(maxAVLQueueSize*10);
      _acknowledgeQueue = new ArrayBlockingQueue<Message>(maxAVLQueueSize*10);
      _archiveQueue = new ArrayBlockingQueue<Message>(maxAVLQueueSize*1000);
      
      logger.info("starting {} threads for queues.", numberThreads);
      for (int i = 0; i <= numberThreads; i++) {
        // todo this really should be executors
        new Thread(new ReceiveTask()).start();
        new Thread(new DeserailzeTask()).start();
        new Thread(new AcknowledgeTask()).start();
        new Thread(new ArchiveTask()).start();
      }
      
      new Thread(new StatusTask()).start();
      
      // create an instance of the SQS message unmarshaller
      _messageUnmarshaller = (SqsMessageUnmarshaller) unmarshallerConfig.getValue().newInstance();
      
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
    
    
    
    private synchronized void connect() {
      _sqs = new AmazonSQSClient(_sqsCredentials);
      Region usEast1 = Region.getRegion(Regions.US_EAST_1);
      _sqs.setRegion(usEast1);
    }

    @Override
    public void run() {
      while (!Thread.interrupted()) {
        try {
          processAVLDataFromSQS();
        } catch (Exception e) {
          logger.error("issue processing data:", e);
        }
      }
    }

    private void processAVLDataFromSQS() {
      int logFrequency = messageLogFrequency.getValue();
      logger.info("logFrequency={}", logFrequency);
      
      while (!Thread.interrupted()) {
        try {
          
          Message message = _receiveQueue.poll(250, TimeUnit.MILLISECONDS);
          if (message == null) continue;

          try {
            _messageCount++;
            _deserializeQueue.add(message);
            _archiveQueue.add(message);
          } catch (IllegalStateException ise) {
            logger.error("dropping message {} as queue is full.  deseralize size={}, archive size={}:  ", message, ise, _deserializeQueue.size(), _archiveQueue.size());
          } catch (Exception any) {
            logger.error("exception deserializing mesage={}: ", message, any);
          }
          
        } catch (Exception e) {
          logger.error("issue receiving request", e);
        }
        
        try {
        // put out a log message to show progress every so often
        if (_messageCount % logFrequency == 0) {
          long delta = (System.currentTimeMillis() - _messageStart)/1000;
          long rate = 0;
          if (delta != 0) {
            rate = _messageCount / delta;
          }
          logger.info("received " + _messageCount + " messages in " +
              delta + " seconds (" + rate + "/s) receive size=" + _deserializeQueue.size() +
              ", archive size=" + _archiveQueue.size() + ", ack size=" 
              + _acknowledgeQueue.size());
          _messageStart = System.currentTimeMillis();
          _messageCount = 0;
        }
        } catch (Exception e) {
          logger.error("status message exception: ", e);
        }
      }
    }


  private void archive(Message message) {
    if (message == null || _sns == null) return;
    // currently AWS does not support batch publishing to SNS
    try {
      PublishRequest request = new PublishRequest();
      request.setTopicArn(snsArn.getValue());
      String content = _messageUnmarshaller.toString(message);
      logger.debug("archiving content {}", content);
      request.setMessage(content);
      _sns.publish(request);
    } catch (Exception any) {
      logger.error("issue archving message {}: ", message, any);
    }
  }


    private void acknowledge(Message message) {
      // let SQS know we processed the messages
      if (message != null) {
        // only acknowledge receipt of the transmission, not of each message
        String messageReceiptHandle = message.getReceiptHandle();
        try {
          logger.trace("ack message");
          _sqs.deleteMessage(new DeleteMessageRequest(_url, messageReceiptHandle));
        } catch (Exception e) {
          logger.error("unable to mark message as received: ", e);
        }
      }
    }

    private class ReceiveTask implements Runnable {
      
      @Override
      public void run() {
        try {
        while (!Thread.interrupted()) {
          try {
            ReceiveMessageRequest request = new ReceiveMessageRequest(_url);
            request.setWaitTimeSeconds(jmsPauseTimeInSeconds.getValue());
            List<Message> messages = _sqs.receiveMessage(request).getMessages();
            try {
              _receiveQueue.addAll(messages);
            } catch (IllegalStateException ise) {
              logger.error("dropping receive {} as queue is full: ",  messages, ise);
            }
            
            if (!messages.isEmpty()) {           
              try {
                  // we only need to ack receipt of the request, not each message
                _acknowledgeQueue.add(messages.get(0));
              } catch (IllegalStateException ise) {
                logger.error("dropping ack {} as queue is full: ",  messages, ise);
              }
            }
          } catch (Exception any) {
            logger.error("exception receiving: ", any);
          }
        }
      } finally {
        logger.error("ReceiveTask exiting!");
      }
      }
    }

    private class DeserailzeTask implements Runnable {
      
      @Override
      public void run() {
        int logFrequency = messageLogFrequency.getValue();
        int recordCount = 0;
        long recordStart = System.currentTimeMillis();
        try {
        while (!Thread.interrupted()) {
          try {
            Message message = _deserializeQueue.poll(250, TimeUnit.MILLISECONDS);
            if (message == null) continue;
            List<AvlReportWrapper> avlReports = null;
            try {
              avlReports = _messageUnmarshaller.toAvlReports(message);
              for (AvlReportWrapper avlReport : avlReports) {
                recordCount++;
                if (avlReport != null) {
                  if (avlReport.getTotalLatency() != null) {
                    monitoring.averageMetric("PredictionTotalQueueLatencyInMillis", new Double(avlReport.getTotalLatency()));
                  }
                  if (avlReport.getSqsLatency() != null) {
                    monitoring.averageMetric("PredictionSQSQueueLatencyInMillis", new Double(avlReport.getSqsLatency()));
                  }
                  if (avlReport.getAvlLatency() != null) {
                    monitoring.averageMetric("PredictionAvlQueueLatencyInMillis", new Double(avlReport.getAvlLatency()));
                  }
                  if (avlReport.getForwarderProcessingLatency() != null) {
                    monitoring.averageMetric("PredictionForwarderProcessingLatencyInMillis", new Double(avlReport.getForwarderProcessingLatency()));
                  }
                  if (avlReport.getForwarderSendLatency() != null) {
                    monitoring.averageMetric("PredictionForwarderSendLatencyInMillis", new Double(avlReport.getForwarderSendLatency()));
                  }
                }
                  
                if (avlReport != null) {
                  if (applicableList.getValue() == null || isApplicable(avlReport)) {
                    Runnable avlClient = new AvlClient(avlReport.getReport());
                    _avlClientExecutor.execute(avlClient);
                  }
                }
               if (recordCount % logFrequency == 0) {
                 long delta = (System.currentTimeMillis() - recordStart)/1000;
                 long rate = 0;
                 if (delta != 0) {
                   rate = recordCount / delta;
                 }
                 logger.info("deserialized " + recordCount + " messages in " +
                     delta + " seconds (" + rate + "/s) receive size=" + _deserializeQueue.size() +
                     ", archive size=" + _archiveQueue.size() + ", ack size=" 
                     + _acknowledgeQueue.size());
                 recordStart = System.currentTimeMillis();
                 recordCount = 0;
               }
              }
            } catch (Exception any) {
              logger.error("exception deserializing message {}", message, any);
            }
          } catch (Exception any) {
            logger.error("unexpected exception: ", any);
          }
        }
        
        } finally {
          logger.error("DeserializeTask exiting!");
        }
      }

      private boolean isApplicable(AvlReportWrapper avlReport) {
        for (String s : applicableList.getValue().split(",")) {
          if (avlReport.getReport().getVehicleId().equals(s.trim())) {
            return true;
          }
        }
        return false;
      }
    }
    
    private class AcknowledgeTask implements Runnable {
      
      @Override
      public void run() {
        try {
        while (!Thread.interrupted()) {
          try {
            Message message = _acknowledgeQueue.poll(250, TimeUnit.MILLISECONDS);
            if (message == null) continue;
            acknowledge(message);
          } catch (Exception any) {
            logger.error("exception acking: ", any);
          }
        }
        } finally {
        logger.error("AcknowledgeTask exiting!");
        }
      }
    }
    
    private class ArchiveTask implements Runnable {
      
      @Override
      public void run() {
        try {
        while (!Thread.interrupted()) {
          try {
            Message message = _archiveQueue.poll(250, TimeUnit.MILLISECONDS);
            if (message == null) continue;
            archive(message);
          } catch (Exception any) {
            logger.error("exception archiving: ", any);
          }
        }
      } finally {
        logger.error("ArchiveTask exiting!");
      }
      }
    }

    private class StatusTask implements Runnable {
      private static final int STATUS_FREQUENCY_SECONDS = 60;

      @Override
      public void run() {
        while (!Thread.interrupted()) {
          try {
            long lastAvlReportTime = (System.currentTimeMillis() - AvlProcessor.getInstance().lastAvlReportTime())/1000;
            logger.info("Queue Size Report:  AVL last report {}s, recieve={}, deserialize={}, ack={}, archive={}",
                lastAvlReportTime,
                _receiveQueue.size(),
                _deserializeQueue.size(),
                _acknowledgeQueue.size(),
                _archiveQueue.size());
            // lastAvlReportTime is already reported as LatestAvlReportAgeInSeconds
            monitoring.averageMetric("PredictionReceiveQueueSize", new Double(_receiveQueue.size()));
            monitoring.averageMetric("PredictionDeserializeQueueSize", new Double(_deserializeQueue.size()));
            monitoring.averageMetric("PredictionAckQueueSize", new Double(_acknowledgeQueue.size()));
            monitoring.averageMetric("PredictionArchiveQueueSize", new Double(_archiveQueue.size()));
            Thread.sleep(STATUS_FREQUENCY_SECONDS * 1000);
          } catch (Exception any) {
            logger.error("exception with status: ", any);
          }
        }
      }
    }
    
}
