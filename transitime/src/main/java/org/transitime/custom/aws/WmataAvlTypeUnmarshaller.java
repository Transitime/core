package org.transitime.custom.aws;

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
    public AvlReportWrapper toAvlReport(Message message) {
        if (message == null || message.getBody() == null) return null;

        JSONObject jsonObj = new JSONObject(message.getBody());
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
            speed = (float) msgObj.getDouble("averageSpeed");
        }

        Long proxied = null;
        if (msgObj.has("proxied")) {
          proxied = msgObj.getLong("proxied");
        }
        
        Long queueLatency = null;
        if (proxied != null) {
          queueLatency = System.currentTimeMillis() - proxied;
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
            return new AvlReportWrapper(ar, queueLatency);
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
