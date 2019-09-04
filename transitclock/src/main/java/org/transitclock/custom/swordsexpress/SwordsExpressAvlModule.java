package org.transitclock.custom.swordsexpress;

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

public class SwordsExpressAvlModule extends PollUrlAvlModule {

	private static String avlURL="http://www.swordsexpress.com/latlong.php?id=100774022844716920000";
	
	public SwordsExpressAvlModule(String agencyId) {
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
		Collection<AvlReport> avlReportsReadIn = new ArrayList<AvlReport>();
		JSONArray array = new JSONArray(json);
		SimpleDateFormat dateformater=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for (int i=0; i<array.length(); ++i) {
			JSONArray entryarray = array.getJSONArray(i);
			
			if(entryarray.length()>2)
			{
				//["07G2339","53.348110","-6.248768","2016-09-07 22:35:53","1","30 kmph","w"]
				String vehicleId = entryarray.getString(0);
				Double latitude=new Double(entryarray.getString(1));
				Double longitude=new Double(entryarray.getString(2));
				
				Date timestamp=dateformater.parse(entryarray.getString(3));
				float heading=Float.NaN;
				
				float speed=Float.NaN;
				
				AvlReport avlReport =
						new AvlReport(vehicleId, timestamp.getTime(), latitude,
								longitude, heading, speed, "Swords Express");
				
				
				avlReportsReadIn.add(avlReport);
			}		
		}
		return avlReportsReadIn;
	}
	/**
	 * Just for debugging
	 */
	public static void main(String[] args) {
		// Create a WexfordCoachAvlModule for testing
		Module.start("org.transitclock.custom.swordsexpress.SwordsExpressAvlModule");
	}
}
