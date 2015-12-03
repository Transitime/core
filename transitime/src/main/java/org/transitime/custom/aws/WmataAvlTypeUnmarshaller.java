package org.transitime.custom.aws;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
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
  public List<AvlReportWrapper> toAvlReports(Message message) {
    List<AvlReportWrapper> reports = new ArrayList<AvlReportWrapper>();
    String body = message.getBody();
    if (body.indexOf('\\') != -1) {
      // we have a bug in the serialization
      body = body.replace("\\", "");
    }

    if (body.startsWith("[")) {
      // we have an array
      JSONArray jsonArray = new JSONArray(body);
      for (int i=0; i<jsonArray.length(); i++) {
        reports.add(toAvlReport((JSONObject)jsonArray.get(i)));
      }
    } else {
      // not an array, try to deserialize as is
      reports.add(toAvlReport(message));
    }
    return reports;
  }

  
  private AvlReportWrapper toAvlReport(JSONObject jsonObj) {
    JSONObject msgObj;
    try{
        msgObj = new JSONObject(jsonObj.getString("Message"));
    }catch(JSONException e){
        msgObj = jsonObj;  //un
    }

    String vehicleId = null;
    if (msgObj.has("vehicleid"))
        vehicleId = String.valueOf(msgObj.getLong("vehicleid"));
    Double lat = null;
    if (msgObj.has("latitude"))
        lat = msgObj.getDouble("latitude");

    Double lon = null;
    if (msgObj.has("longitude"))
        lon = msgObj.getDouble("longitude");

    Long time = null;
    if (msgObj.has("avlDate"))
        time = msgObj.getLong("avlDate");

    Float heading = null;
    if (msgObj.has("heading")) {
        heading = (float) msgObj.getDouble("heading");
    }

    Float speed = null;
    if (msgObj.has("averageSpeed")) {
        speed = (float) msgObj.getDouble("averageSpeed") * 0.44704f; // convert to m/s
    }

    Long proxied = null;
    if (msgObj.has("proxied")) {
      proxied = msgObj.getLong("proxied");
    }
    
    Long sqsLatency = null;
    Long avlLatency = null;
    Long totalLatency = null;
    if (proxied != null) {
      Long now = System.currentTimeMillis();
      sqsLatency = now - proxied;
      if (time != null) {
        totalLatency = now - time;
        avlLatency = proxied - time;
      }
      
    }
    
    String source = "sqs";
    if (vehicleId != null && lat != null && lon != null && time != null) {
        AvlReport ar = new AvlReport(vehicleId, time, lat, lon, speed, heading, source);
        if (msgObj.has("blockAlpha")) {
            String blockAlpha = msgObj.getString("blockAlpha");
            if (blockAlpha != null) {
                ar.setAssignment(blockAlpha, AssignmentType.BLOCK_ID);
            }
        }
        return new AvlReportWrapper(ar, avlLatency, sqsLatency, totalLatency);
    }
    // missing necessary info
    return null;
    
  }
  
    @Override
    public AvlReportWrapper toAvlReport(Message message) {
        if (message == null || message.getBody() == null) return null;
        JSONObject jsonObj = new JSONObject(message.getBody());
        return toAvlReport(jsonObj);
    }

    @Override
    public String toString(Message message) {
        if (message == null) return null;
        return message.getBody().replace("\\\"", "'"); // we have a bug in the serialization
    }

}
