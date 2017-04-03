package org.transitime.custom.gtt;

import java.io.InputStream;
import java.util.Date;
import java.text.SimpleDateFormat;

import org.json.JSONArray;
import org.json.JSONObject;
import org.transitime.avl.PollUrlAvlModule;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.AvlReport.AssignmentType;
import org.transitime.modules.Module;

public class GTTAvlModule extends PollUrlAvlModule {

	private static String avlURL="http://m.gatech.edu/api/buses/position";
	
	public GTTAvlModule(String agencyId) {
		super(agencyId);		
	}

	@Override
	protected String getUrl() {
		
		return avlURL;
	}

	@Override
	protected void processData(InputStream in) throws Exception {
		
		String json=this.getJsonString(in);
		
		JSONArray array = new JSONArray(json);
				
		for (int i=0; i<array.length(); ++i) {
			JSONObject entry = array.getJSONObject(i);
			
			
			
				if(entry.has("route") && !entry.isNull("route") && entry.getString("route").equals("red"))
				{
					String vehicleId=entry.getString("id");
					Double latitude=entry.getDouble("lat");
					Double longitude=entry.getDouble("lng");
					
					//2016-09-07 17:02:48
					SimpleDateFormat dateformater=new SimpleDateFormat("HH:mm:ss");
					
					Date timestamp=dateformater.parse(entry.getString("ts"));
					
					float heading=Float.NaN;
					
					float speed=Float.NaN;
					
					AvlReport avlReport =
							new AvlReport(vehicleId, timestamp.getTime(), latitude,
									longitude, heading, speed, "GTT");
					
					String assignmentId = "1079682";
					avlReport.setAssignment(assignmentId, AssignmentType.ROUTE_ID);
					
					processAvlReport(avlReport);
				}
			
		}
	}
	/**
	 * Just for debugging
	 */
	public static void main(String[] args) {
		// Create a WexfordCoachAvlModule for testing
		Module.start("org.transitime.custom.gtt.GTTAvlModule");
	}
}
