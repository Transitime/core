package org.transitclock.db.structs.apc;

import org.junit.Before;
import org.junit.Test;
import org.transitclock.TestSupport;
import org.transitclock.db.structs.ApcRecord;
import org.transitclock.utils.Time;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.transitclock.TestSupport.toEpoch;

public class ApcMessageUnmarshallerTest {

  private String validRecords;
  private String invalidRecord;
  private SimpleApcMessageUnmarshaller unmarshaller;



  @Before
  public void setup() throws Exception {
    // load in resource file from classloader
    InputStream is1 = this.getClass().getResourceAsStream("apcMessage1.json");
    assertNotNull(is1); // make sure file is found
    validRecords = getStreamAsString(is1);

    InputStream is2 = this.getClass().getResourceAsStream("apcMessage2.json");
    assertNotNull(is2);
    invalidRecord = getStreamAsString(is2);
    unmarshaller = new SimpleApcMessageUnmarshaller();

  }

  @Test
  public void testToApcRecord() throws Exception {
    List<ApcRecord> apcRecords = unmarshaller.toApcRecord(validRecords, "CST", "UTC");
    assertNotNull(apcRecords);
    assertEquals(10, apcRecords.size());
    ApcRecord a = apcRecords.get(0);
    assertNotNull(a);
    assertEquals("2139984326", a.getMessageId());
    assertEquals(toEpoch("2021-02-18", null,"CST"), a.getServiceDate());
    // NOTE:  service date needs to be relative to time zone for follow on calculations to work
    assertEquals(new Date(toEpoch("2021-02-18", "00:00:00","CST")), new Date(a.getServiceDate()));

    assertEquals(new Date(TestSupport.toEpoch("2021-02-18", "21:07:18", "UTC")), new Date(a.getTime()));

    assertEquals("79497", a.getDriverId());
    // TODO fom: Figure Of Merit; quality of GPS lock
    assertEquals("2081", a.getVehicleId());
    assertEquals(54422, a.getDoorOpen());
    // 907min = 15:07 CST
    assertEquals("907.0 min", Time.elapsedTimeStr(a.getDoorOpen() * Time.SEC_IN_MSECS));

    assertEquals(new Date(TestSupport.toEpoch("2021-02-18", "15:07:02", "CST")), new Date(a.getDoorOpenEpoch()));
    assertEquals(new Date(TestSupport.toEpoch("2021-02-18", "21:07:02", "UTC")), new Date(a.getDoorOpenEpoch()));
    assertEquals(TestSupport.toEpoch("2021-02-18", "21:07:02", "UTC"), a.getDoorOpenEpoch());
    assertEquals(54429, a.getDoorClose());
    assertEquals(new Date(TestSupport.toEpoch("2021-02-18", "21:07:09", "UTC")), new Date(a.getDoorCloseEpoch()));
    assertEquals(TestSupport.toEpoch("2021-02-18", "21:07:09", "UTC"), a.getDoorCloseEpoch());
    assertEquals(54442, a.getDeparture());
    assertEquals(new Date(TestSupport.toEpoch("2021-02-18", "21:07:22", "UTC")), new Date(a.getDepartureEpoch()));
    assertEquals(54409, a.getArrival());
    assertEquals(new Date(TestSupport.toEpoch("2021-02-18", "21:06:49", "UTC")), new Date(a.getArrivalEpoch()));

    assertEquals(1, a.getBoardings());
    assertEquals(0, a.getAlightings());
    assertEquals(44.9465819, a.getLat(), 0.00001);
    assertEquals(-93.1493102, a.getLon(), 0.00001);
  }


  @Test
  public void testInvalidMessageType() {
    List<ApcRecord> apcRecords = unmarshaller.toApcRecord(invalidRecord, "CST", "UTC");
    assertNotNull(apcRecords);
    assertEquals(0, apcRecords.size());
  }

  @Test
  public void testRemoveWrapper() {
    String wrapper = "{[]}";
    assertEquals("[]", unmarshaller.removeWrapper(wrapper));
    wrapper = "{[]}\n";
    assertEquals("[]", unmarshaller.removeWrapper(wrapper));
  }

  @Test
  public void testParseTimeStampToLong() throws Exception {
    String timestamp = "2021-02-18T21:07:18";
    assertEquals(toEpoch("2021-02-18", "21:07:18", "UTC"), unmarshaller.parseTimestampToLong(timestamp, "UTC"));
  }

  @Test
  public void testParseDateStampToLong() throws Exception{
    String stamp = "20210218";
    // service date needs to be time zone aware as it sets the base time for secondsIntoDay
    assertEquals(new Date(toEpoch("2021-02-18", null,"CST")), new Date(unmarshaller.parseDateStampToLong(stamp, "CST")));
    assertEquals(toEpoch("2021-02-18", null,"CST"), unmarshaller.parseDateStampToLong(stamp, "CST"));
  }

  private String getStreamAsString(InputStream inputStream) throws Exception {
    return TestSupport.getStreamAsString(inputStream);
  }



}