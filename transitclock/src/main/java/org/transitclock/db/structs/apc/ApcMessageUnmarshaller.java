package org.transitclock.db.structs.apc;

import com.amazonaws.services.sqs.model.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.db.structs.ApcRecord;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Unmarshal messages into ApcRecord format.
 */
public class ApcMessageUnmarshaller {

  public static StringConfigValue timestampFormat = new StringConfigValue(
          "transitclock.apc.timestampFormat",
          "YYYY-MM-DD'T'HH:mm:ss",
          "SimpleDataFormat specification of timestamp");
  public static StringConfigValue dateFormat = new StringConfigValue(
          "transitclock.apc.dateFormat",
          "YYYYMMDD",
          "SimpleDateFormat specification of date");
  public static IntegerConfigValue expectedMessageType = new IntegerConfigValue(
          "transitclock.apc.supportedMessageType",
          128,
          "Ordinal validating the message type of the APC message");

  private static final Logger logger =
          LoggerFactory.getLogger(ApcMessageUnmarshaller.class);

  public List<ApcRecord> toApcRecord(Message message) {
    List<ApcRecord> records = new ArrayList<>();
    String body = message.getBody();
    return toApcRecord(body);
  }

  public List<ApcRecord> toApcRecord(String body) {
    List<ApcRecord> results = new ArrayList<>();
    JSONArray array = new JSONArray(removeWrapper(body));
    for (int i = 0; i < array.length(); i++ ) {
      ApcRecord apc = toSingleRecord(array.getJSONObject(i));
      if (apc != null)
        results.add(apc);
    }
    return results;
  }

  private ApcRecord toSingleRecord(JSONObject jsonObject) {

    int messageType = safeGetInt(jsonObject, "message_type");
    if (expectedMessageType.getValue() != null
          && expectedMessageType.getValue() != messageType) {
      logger.info("invalid message type {}", messageType);
      return null;
    }

    String messageId = safeGetString(jsonObject, "id");
    long time = safeGetTimestamp(jsonObject, "timestamp");
    long serviceDate = safeGetDate(jsonObject, "service_date");
    String driverId = safeGetString(jsonObject, "driver");
    int odo = safeGetInt(jsonObject, "odo");
    String vehicleId = safeGetString(jsonObject, "vehicle_id");
    int boardings = safeGetInt(jsonObject, "ons");
    int alightings = safeGetInt(jsonObject, "offs");
    int doorOpen = safeGetInt(jsonObject, "door_open");
    int doorClose = safeGetInt(jsonObject, "door_close");
    int arrival = safeGetInt(jsonObject, "arr");
    int departure = safeGetInt(jsonObject, "dep");
    double lat = safeGetDouble(jsonObject, "lat");
    double lon = safeGetDouble(jsonObject, "lon");

    ApcRecord a = new ApcRecord(messageId,
            time,
            serviceDate,
            driverId,
            odo,
            vehicleId,
            boardings,
            alightings,
            doorOpen,
            doorClose,
            arrival,
            departure,
            lat,
            lon);
    return a;
  }

  private double safeGetDouble(JSONObject jsonObject, String id) {
    if (jsonObject.has(id))
      return jsonObject.getDouble(id);
    return -1.0;
  }

  private long safeGetDate(JSONObject jsonObject, String id) {
    if (jsonObject.has(id)) {
      return parseDateToLong(jsonObject.get(id).toString());
    }
    return -1;
  }

  private long parseDateToLong(String dateStamp) {
    return parseDateStampToLong(dateStamp);
  }

  private long safeGetTimestamp(JSONObject jsonObject, String id) {
    if (jsonObject.has(id))
      return parseTimestampToLong(jsonObject.getString(id));
    return -1;
  }

  private long parseDate(String stamp, String format) {
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    Date parse = null;
    try {
      parse = sdf.parse(stamp);
    } catch (ParseException e) {
      logger.debug("invalid date format {} when expecting {}", stamp, format, e);
    }
    if (parse != null)
      return parse.getTime();
    return -1;
  }

  long parseTimestampToLong(String timestamp) {
    return parseDate(timestamp, timestampFormat.getValue());
  }
  long parseDateStampToLong(String timestamp) {
    return parseDate(timestamp, dateFormat.getValue());
  }


  private int safeGetInt(JSONObject jsonObject, String id) {
    if (jsonObject.has(id))
      return jsonObject.getInt(id);
    return -1;
  }

  private String safeGetString(JSONObject jsonObject, String id) {
    if (jsonObject.has(id))
      return jsonObject.get(id).toString();
    return null;
  }

  /**
   * if array is enclosed a object wrapper remove it as its not
   * well-formed JSON
   * @param wrapper
   * @return
   */
  String removeWrapper(String wrapper) {
    if (wrapper == null) return null;
    if (wrapper.startsWith("{")) {
      String body = wrapper.replaceFirst("\\{", "");
      int pos = body.lastIndexOf("}");
      body = body.substring(0, pos);
      return body;
    }
    return wrapper;
  }
}
