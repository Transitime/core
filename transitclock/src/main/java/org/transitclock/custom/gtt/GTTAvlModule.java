package org.transitclock.custom.gtt;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.transitclock.avl.PollUrlAvlModule;
import org.transitclock.config.StringListConfigValue;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.db.structs.AvlReport.AssignmentType;
import org.transitclock.modules.Module;

public class GTTAvlModule extends PollUrlAvlModule {

	private static String avlURL="http://m.gatech.edu/api/buses/position";
	HashMap<String, Date> avlreports = new HashMap<String, Date>();
	
	protected static StringListConfigValue vehiclesonredroute = new StringListConfigValue("transitclock.gtt.vehiclesonredroute",null, "List of vehicles on read route for HoldingTime trial.");

	public GTTAvlModule(String agencyId) {
		super(agencyId);		
	}

	@Override
	protected String getUrl() {
		
		return avlURL;
	}

	@Override
	protected Collection<AvlReport> processData(InputStream in) throws Exception {
		
		String json=this.getJsonString(in);
		// The return value for the method
		Collection<AvlReport> avlReportsReadIn = new ArrayList<AvlReport>();
		
		JSONArray array = new JSONArray(json);
				
		for (int i=0; i<array.length(); ++i) {
			JSONObject entry = array.getJSONObject(i);
			
			
				String vehicleId=entry.getString("id");
				Double latitude=entry.getDouble("lat");
				Double longitude=entry.getDouble("lng");
			
				if((entry.has("route") && !entry.isNull("route") && entry.getString("route").equals("red"))|| inVehicleList(vehicleId))
				{				
					
					//2016-09-07 17:02:48
					SimpleDateFormat dateformater=new SimpleDateFormat("yyyyMMdd HH:mm:ss");
					
					LocalDate localDate = LocalDate.now();
					
				    String date=DateTimeFormatter.ofPattern("yyyyMMdd").format(localDate);

					
					Date timestamp=dateformater.parse(date +" "+entry.getString("ts"));
					
					float heading=Float.NaN;
					
					float speed=Float.NaN;
					AvlReport avlReport=null;
					if(avlreports.containsKey(vehicleId))
					{
						if(!avlreports.get(vehicleId).equals(timestamp))
						{
							avlReport =
									new AvlReport(vehicleId, timestamp.getTime(), latitude,
											longitude, heading, speed, "GTT");
						}					
					}else
					{
						avlreports.put(vehicleId, timestamp);
						avlReport =
								new AvlReport(vehicleId, timestamp.getTime(), latitude,
										longitude, heading, speed, "GTT");
					}
					
					
					
					/* TODO This does not work for freq services. It breaks autoassigner. 
					
					String assignmentId = "1079682";
					avlReport.setAssignment(assignmentId, AssignmentType.ROUTE_ID);
					
					*/			
					
					if(avlReport!=null)
					{
						avlReportsReadIn.add(avlReport);
					}
				}							
		}
		return avlReportsReadIn;
	}
	/**
	 * Just for debugging
	 */
	public static void main(String[] args) {
		// Create a WexfordCoachAvlModule for testing
		Module.start("org.transitclock.custom.gtt.GTTAvlModule");
	}
	private boolean inVehicleList(String vehicleId)
	{
		if(vehiclesonredroute!=null && vehiclesonredroute.getValue()!=null)
		{
			for(String vehicle:vehiclesonredroute.getValue())
			{
				if(vehicle.equals(vehicleId))
				{
					return true;
				}
			}
		}
		return false;
	}
}
