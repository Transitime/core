package org.transitclock.db.structs.apc;

import org.junit.Before;
import org.transitclock.db.structs.ApcRecord;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.*;

public class ApcMessageUnmarshallerTest {

  private String input1;
  private ApcMessageUnmarshaller unmarshaller;

  @Before
  public void setup() throws Exception {
    // load in resource file from classloader
    InputStream is1 = this.getClass().getResourceAsStream("apcMessage1.json");
    input1 = getSteamAsString(is1);
    unmarshaller = new ApcMessageUnmarshaller();

  }

  public void testToApcRecord() {
    List<ApcRecord> apcRecords = unmarshaller.toApcRecord(input1);
    assertNotNull(apcRecords);
    assertEquals(5, apcRecords.size());
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

}