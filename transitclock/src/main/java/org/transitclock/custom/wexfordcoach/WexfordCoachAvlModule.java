package org.transitclock.custom.wexfordcoach;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.text.SimpleDateFormat;

import org.json.JSONArray;
import org.json.JSONObject;
import org.transitclock.avl.PollUrlAvlModule;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.modules.Module;

public class WexfordCoachAvlModule extends PollUrlAvlModule {

	private static String avlURL="http://www.textmemybus.com/live_bus_positions/bus_positions.json";
	
	public WexfordCoachAvlModule(String agencyId) {
		super(agencyId);		
	}

	@Override
	protected String getUrl() {
		
		return avlURL;
	}

	@Override
	protected Collection<AvlReport> processData(InputStream in) throws Exception {
		
		Collection<AvlReport> reports = new ArrayList<AvlReport>();
		
		String json=this.getJsonString(in);
		// The return value for the method
		Collection<AvlReport> avlReportsReadIn = new ArrayList<AvlReport>();
		JSONArray array = new JSONArray(json);
				
		for (int i=0; i<array.length(); ++i) {
			JSONObject entry = array.getJSONObject(i);
			String vehicleId=entry.getString("Name");
			Double latitude=entry.getDouble("Latitude");
			Double longitude=entry.getDouble("Longitude");
			
			//2016-09-07 17:02:48
			SimpleDateFormat dateformater=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			Date timestamp=dateformater.parse(entry.getString("Time"));
			
			float heading=Float.NaN;
			
			float speed=Float.NaN;
			
			AvlReport avlReport =
					new AvlReport(vehicleId, timestamp.getTime(), latitude,
							longitude, heading, speed, "Wexford Coach");
			
			
			avlReportsReadIn.add(avlReport);
		}
		return avlReportsReadIn;
	}
	/**
	 * Just for debugging
	 */
	public static void main(String[] args) {
		// Create a WexfordCoachAvlModule for testing
		Module.start("org.transitclock.custom.wexfordcoach.WexfordCoachAvlModule");
	}
}
