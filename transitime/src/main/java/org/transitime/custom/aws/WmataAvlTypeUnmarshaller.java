package org.transitime.custom.aws;

import org.json.JSONObject;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.AvlReport.AssignmentType;

import com.amazonaws.services.sqs.model.Message;

/**
 * Implementation of SqsMessageUnmarshaller for WMATA data. 
 *
 */
public class WmataAvlTypeUnmarshaller implements SqsMessageUnmarshaller {

  @Override
  public AvlReport toAvlReport(Message message) {
    JSONObject jsonObj = new JSONObject(message.getBody());
    // mandatory
    String vehicleId = String.valueOf(jsonObj.getLong("vehicleid"));
    // mandatory
    double lat = jsonObj.getDouble("latitude");
    // mandatory
    double lon = jsonObj.getDouble("longitude");
    // mandatory
    long time = jsonObj.getLong("avlDate");
    Float heading = null;
    Float speed = null;
    // optional
    if (jsonObj.has("heading")) {  
      heading = (float) jsonObj.getDouble("heading");
    }
    // optional
    if (jsonObj.has("averageSpeed")) {
      speed = (float) jsonObj.getDouble("averageSpeed");
    }
    
    String source = "sqs";
    AvlReport ar = new AvlReport(vehicleId, time, lat, lon, speed, heading, source);
    ar.setAssignment(jsonObj.getString("blockAlpha"), AssignmentType.BLOCK_ID);
    return ar;
  }
  
  @Override
  public String toString(Message message) {
    if (message == null) return null;
    return message.getBody();
  }

}
