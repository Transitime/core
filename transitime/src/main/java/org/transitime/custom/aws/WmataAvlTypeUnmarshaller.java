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
  public AvlReport deserialize(Message message) {
    JSONObject jsonObj = new JSONObject(message.getBody());
    String vehicleId = String.valueOf(jsonObj.getLong("vehicleid"));
    long time = jsonObj.getLong("avlDate");
    double lat = jsonObj.getDouble("latitude");
    double lon = jsonObj.getDouble("longitude");
    float speed = (float) jsonObj.getDouble("averageSpeed");
    float heading = (float) jsonObj.getDouble("heading");
    String source = "sqs";
    AvlReport ar = new AvlReport(vehicleId, time, lat, lon, speed, heading, source);
    ar.setAssignment(jsonObj.getString("blockAlpha"), AssignmentType.BLOCK_ID);
    return ar;
  }

}
