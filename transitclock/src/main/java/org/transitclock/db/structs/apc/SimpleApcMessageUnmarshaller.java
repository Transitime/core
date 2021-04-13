package org.transitclock.db.structs.apc;

import com.amazonaws.services.sqs.model.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.db.structs.ApcRecord;
import org.transitclock.utils.Time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Unmarshal messages into ApcRecord format.
 */
public class SimpleApcMessageUnmarshaller implements ApcMessageUnmarshaller {

  public static StringConfigValue timestampFormat = new StringConfigValue(
          "transitclock.apc.timestampFormat",
          "yyyy-MM-dd'T'HH:mm:ss",
          "SimpleDataFormat specification of timestamp");
  public static StringConfigValue dateFormat = new StringConfigValue(
          "transitclock.apc.dateFormat",
          "yyyyMMdd",
          "SimpleDateFormat specification of date");
  public static IntegerConfigValue expectedMessageType = new IntegerConfigValue(
          "transitclock.apc.supportedMessageType",
          128,
          "Ordinal validating the message type of the APC message");

  private static final Logger logger =
          LoggerFactory.getLogger(SimpleApcMessageUnmarshaller.class);

  private Time time;

  @Override
  public List<ApcRecord> toApcRecord(Message message, String serviceDateTz, String timestampTz) {
    String body = message.getBody();
    return toApcRecord(body,serviceDateTz, timestampTz);
  }

  @Override
  public List<ApcRecord> toApcRecord(String body, String serviceDateTz, String timestampTz) {
    List<ApcRecord> results = new ArrayList<>();
    JSONArray array = new JSONArray(removeWrapper(body));
    for (int i = 0; i < array.length(); i++ ) {
      ApcRecord apc = toSingleRecord(array.getJSONObject(i), serviceDateTz, timestampTz);
      if (apc != null)
        results.add(apc);
    }
    return results;
  }

  @Override
  public String toString(Message message) {
    if (message == null) return null;
    return message.getBody();
  }

  private ApcRecord toSingleRecord(JSONObject jsonObject, String serviceDateTz, String timestampTz) {

    int messageType = safeGetInt(jsonObject, "message_type");
    if (expectedMessageType.getValue() != null
          && expectedMessageType.getValue() != messageType) {
      logger.info("invalid message type {}", messageType);
      return null;
    }

    String messageId = safeGetString(jsonObject, "id");
    long time = safeGetTimestamp(jsonObject, "timestamp", timestampTz);
    long serviceDate = safeGetDate(jsonObject, "service_date", serviceDateTz);
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

  private long safeGetDate(JSONObject jsonObject, String id, String tz) {
    if (jsonObject.has(id)) {
      return parseDateToLong(jsonObject.get(id).toString(), tz);
    }
    return -1;
  }

  private long parseDateToLong(String dateStamp, String tz) {
    return parseDateStampToLong(dateStamp, tz);
  }

  private long safeGetTimestamp(JSONObject jsonObject, String id, String tz) {
    if (jsonObject.has(id))
      return parseTimestampToLong(jsonObject.getString(id), tz);
    return -1;
  }

  private long parseDate(String stamp, String format, String tz) {
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    if (tz != null) sdf.setTimeZone(TimeZone.getTimeZone(tz));
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

  long parseTimestampToLong(String timestamp, String tz) {
    return parseDate(timestamp, timestampFormat.getValue(), tz);
  }
  long parseDateStampToLong(String timestamp, String tz) {
    return parseDate(timestamp, dateFormat.getValue(), tz);
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
