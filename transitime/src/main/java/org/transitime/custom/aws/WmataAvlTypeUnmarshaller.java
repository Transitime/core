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
    if (message == null) return null;
    JSONObject jsonObj = new JSONObject(message.getBody());
    String vehicleId = null;
    if (jsonObj.has("vehicleid"))
      vehicleId = String.valueOf(jsonObj.getLong("vehicleid"));
    Double lat = null;
    if (jsonObj.has("latitude"))
      lat = jsonObj.getDouble("latitude");

    Double lon = null;
    if (jsonObj.has("longitude"))
      lon = jsonObj.getDouble("longitude");

    Long time = null;
    if (jsonObj.has("avlDate"))
      time = jsonObj.getLong("avlDate");
    
    Float heading = null;
    if (jsonObj.has("heading")) {  
      heading = (float) jsonObj.getDouble("heading");
    }
    
    Float speed = null;
    if (jsonObj.has("averageSpeed")) {
      speed = (float) jsonObj.getDouble("averageSpeed");
    }
    
    String source = "sqs";
    if (vehicleId != null && lat != null && lon != null && time != null) {
    	AvlReport ar = new AvlReport(vehicleId, time, lat, lon, speed, heading, source);
    	if (jsonObj.has("blockAlpha")) {
    	  String blockAlpha = jsonObj.getString("blockAlpha");
    	  if (blockAlpha != null) {
    	    ar.setAssignment(blockAlpha, AssignmentType.BLOCK_ID);
    	  }
    	}
    	return ar;
    }
    // missing necessary info
    return null;
  }
  
  @Override
  public String toString(Message message) {
    if (message == null) return null;
    return message.getBody();
  }

}
