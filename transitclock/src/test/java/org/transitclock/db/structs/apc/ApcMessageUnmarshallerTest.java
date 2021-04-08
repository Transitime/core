package org.transitclock.db.structs.apc;

import org.junit.Before;
import org.junit.Test;
import org.transitclock.db.structs.ApcRecord;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;

import static org.junit.Assert.*;

public class ApcMessageUnmarshallerTest {

  private String validRecords;
  private String invalidRecord;
  private ApcMessageUnmarshaller unmarshaller;

  @Before
  public void setup() throws Exception {
    // load in resource file from classloader
    InputStream is1 = this.getClass().getResourceAsStream("apcMessage1.json");
    assertNotNull(is1); // make sure file is found
    validRecords = getSteamAsString(is1);

    InputStream is2 = this.getClass().getResourceAsStream("apcMessage2.json");
    assertNotNull(is2);
    invalidRecord = getSteamAsString(is2);
    unmarshaller = new ApcMessageUnmarshaller();

  }

  @Test
  public void testToApcRecord() throws Exception {
    List<ApcRecord> apcRecords = unmarshaller.toApcRecord(validRecords);
    assertNotNull(apcRecords);
    assertEquals(10, apcRecords.size());
    ApcRecord a = apcRecords.get(0);
    assertNotNull(a);
    assertEquals("2139984326", a.getMessageId());
    assertEquals(toDate("2021-02-18", null), a.getServiceDate());
    assertEquals("79497", a.getDriverId());
    // TODO fom? what is this?
    assertEquals("2081", a.getVehicleId());
    assertEquals(54422, a.getDoorOpen());
    assertEquals(54429, a.getDoorClose());
    assertEquals(54442, a.getDeparture());
    assertEquals(54409, a.getArrival());
    assertEquals(toDate("2021-02-18", "21:07:18"), a.getTime());
    assertEquals(1, a.getBoardings());
    assertEquals(0, a.getAlightings());
    assertEquals(44.9465819, a.getLat(), 0.00001);
    assertEquals(-93.1493102, a.getLon(), 0.00001);
  }


  @Test
  public void testInvalidMessageType() {
    List<ApcRecord> apcRecords = unmarshaller.toApcRecord(invalidRecord);
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
    assertEquals(toDate("2021-02-18", "21:07:18"), unmarshaller.parseTimestampToLong(timestamp));
  }

  @Test
  public void testParseDateStampToLong() throws Exception{
    String stamp = "20210218";
    assertEquals(toDate("2021-02-18", null), unmarshaller.parseDateStampToLong(stamp));
  }

  private String getSteamAsString(InputStream inputStream) throws Exception {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    int nRead;
    byte[] data = new byte[1024];
    while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
      buffer.write(data, 0, nRead);
    }

    buffer.flush();
    byte[] byteArray = buffer.toByteArray();

    String text = new String(byteArray, StandardCharsets.UTF_8);
    return text;
  }

  private long toDate(String date, String time) throws Exception {
    SimpleDateFormat sdf;
    if (time == null) {
      sdf = new SimpleDateFormat("YYYY-MM-DD");
      return sdf.parse(date).getTime();
    }
    sdf = new SimpleDateFormat("YYYY-MM-DDHH:mm:ss");
    return sdf.parse(date+time).getTime();

  }


}