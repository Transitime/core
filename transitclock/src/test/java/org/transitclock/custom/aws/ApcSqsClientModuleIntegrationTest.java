package org.transitclock.custom.aws;

import com.amazonaws.services.sqs.model.Message;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.avl.ApcParsedRecord;
import org.transitclock.config.ConfigFileReader;
import org.transitclock.config.StringConfigValue;
import org.transitclock.db.structs.ApcRecordSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * This is an integration test that can verify APC SQS connectivity.
 * Run by passing the following args to the test:
 *
 * -Dtransitclock.apc.sqsKey=<key>
 * -Dtransitclock.apc.sqsSecret=<secret>
 * -Dtransitclock.apc.sqsUrl=<sqs_url>
 * -Dtransitclock.apc.captureTestData=true
 */
public class ApcSqsClientModuleIntegrationTest {

  private static final Logger logger =
          LoggerFactory.getLogger(ApcSqsClientModuleIntegrationTest.class);
  private ApcSqsClientModule sqs;
  private ApcRecordSupport apcRecordSupport = new ApcRecordSupport();
  private static StringConfigValue sqsKey =
          new StringConfigValue("transitclock.apc.sqsKey", null, "The AWS Key with SQS read access");


  @Test
  public void testConnect() throws Exception {
    // load environment configuration
    ConfigFileReader.processConfig();

    if (sqsKey.getValue() == null) {
      logger.error("ApcSqsClientModuleIntegrationTest not configured, exiting");
      return;
    }

    ApcSqsCallback sponge = new ApcSqsCallback();
    sqs = new ApcSqsClientModule("1", sponge);
    sqs.start();
    // grab a minute's worth of data
    final int maxWait = 1 * 60;
    final int maxRecords = 1000;
    int i = 0;
    while (sponge.size() < maxRecords && i < maxWait) {
      i++;
      Thread.sleep(1 * 1000);
      if (i %100 == 0) {
        System.out.println("received " + sponge.size() + " records....");
      }
    }
    System.out.println("loop completed with " + sponge.size() + " records");
    sqs.shutdown();
    List<Message> records = sponge.getRawRecords();
    assertNotNull(records);
    assertTrue(!records.isEmpty());
    if ("true".equals(System.getProperty("transitclock.apc.captureTestData"))) {
      // serialize for test data
      String filename = System.getProperty("java.io.tmpdir")
              + File.separator
              + "apcRecords" + System.currentTimeMillis()
              + ".json";
      apcRecordSupport.writeToFile(filename, records);
    }

  }


  public static class ApcSqsCallback implements SqsCallback {

    private List<ApcParsedRecord> apcRecords = new ArrayList<>();
    private List<Message> rawRecords = new ArrayList<>();

    @Override
    public void receiveRawMessages(List<Message> messages) {
      rawRecords.addAll(messages);
    }

    @Override
    public void receiveApcRecords(List<ApcParsedRecord> records) {
      apcRecords.addAll(records);
    }

    public boolean isEmpty() {
      return apcRecords.isEmpty();
    }

    public List<ApcParsedRecord> getApcRecords() {
      // return a copy as list will grow
      return new ArrayList<>(apcRecords);
    }

    public List<Message> getRawRecords() {
      return new ArrayList<>(rawRecords);
    }

    public int size() {
      return rawRecords.size();
    }
  }
}