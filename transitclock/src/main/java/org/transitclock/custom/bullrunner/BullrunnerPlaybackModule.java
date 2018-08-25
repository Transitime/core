package org.transitclock.custom.bullrunner;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.avl.PollUrlAvlModule;
import org.transitclock.config.StringConfigValue;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.db.structs.AvlReport.AssignmentType;
import org.transitclock.modules.Module;
import org.transitclock.utils.Time;

/**
 * @author scrudden
 * Throw away module. Just used to read in archived Bullrunner data to work on Kalman predictions for frequency based services.
 *
 */
public class BullrunnerPlaybackModule extends PollUrlAvlModule {

	private static StringConfigValue bullrunnerbasePlaybackUrl = 
			new StringConfigValue("transitclock.avl.bullrunnerbasePlaybackUrl", "http://transit.jadorno.com/bullrunner/gtfsrt/vehicle_positions",
					"The URL of the historical json feed to use.");
	
	private static String getPlaybackStartTimeStr() {
		return playbackStartTimeStr.getValue();
	}
	
	private static StringConfigValue playbackStartTimeStr =
			new StringConfigValue("transitclock.avl.bullrunner.playbackStartTime", 
					"08-06-2018 12:00:00",
					"Date and time of when to start the playback.");
	
	private static StringConfigValue playbackEndTimeStr =
			new StringConfigValue("transitclock.avl.bullrunner.playbackEndTime", 
					"08-09-2018 23:00:00",
					"Date and time of when to end the playback.");

	
	
	private static StringConfigValue runnerTestRoute = 
			new StringConfigValue("transitclock.avl.testroute", null,
					"Route to test against.");
	
	private static final Logger logger = LoggerFactory
			.getLogger(BullrunnerPlaybackModule.class);

	public BullrunnerPlaybackModule(String agencyId) {
		super(agencyId);
		
		if(latesttime==null)
			latesttime=parsePlaybackStartTime(playbackStartTimeStr.getValue());
		
	}
	Long latesttime=null;
	
	// If debugging feed and want to not actually process
		// AVL reports to generate predictions and such then
		// set shouldProcessAvl to false;
		private static boolean shouldProcessAvl = true;
	@Override
	protected String getUrl() {
		
		return bullrunnerbasePlaybackUrl.getValue()+"?timestamp="+latesttime;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Module.start("org.transitclock.custom.bullrunner.BullrunnerPlaybackModule");
	}

	@Override
	protected Collection<AvlReport> processData(InputStream in) throws Exception {
		
		String jsonStr = getJsonString(in);
		Collection<AvlReport> avlReportsReadIn = new ArrayList<AvlReport>();
		try {
			// Convert JSON string to a JSON object
			JSONObject jsonObj = new JSONObject(jsonStr);

			JSONArray entities=(JSONArray) jsonObj.get("entity");
			JSONObject header=(JSONObject) jsonObj.get("header");
			
			//JSONArray vehicles = entityObj.getJSONArray("vehicle");
			
			for(int i=0;i<entities.length();i++)
			{
				JSONObject entity=entities.getJSONObject(i);
				JSONObject vehicle=entity.getJSONObject("vehicle");
				JSONObject vehicleIdentity=vehicle.getJSONObject("vehicle");
				JSONObject vehiclePosition=vehicle.getJSONObject("position");
				JSONObject vehicleTrip=vehicle.getJSONObject("trip");
				
				if(vehicleTrip.has("routeId") && (runnerTestRoute.getValue()==null || vehicleTrip.getString("routeId").equals(runnerTestRoute.getValue())))
				{
					//String blockid=vehicle.getString("block_id");
					long timestamp=header.getLong("timestamp");
					
					Core.getInstance().setSystemTime(latesttime);
					
					
					// Create the AvlReport
					AvlReport avlReport =
							new AvlReport(vehicleIdentity.getString("id"), latesttime, vehiclePosition.getDouble("latitude"), vehiclePosition.getDouble("longitude"), Float.NaN,
									(float) vehiclePosition.getInt("bearing"), "BullrunnerArchive");	
					
					// Actually set the assignment
					avlReport.setAssignment(vehicleTrip.getString("routeId"),
							AssignmentType.ROUTE_ID);
		
					logger.debug("From BullrunnerPlaybackModule {}", avlReport);
					System.out.println(avlReport);
					
					if (shouldProcessAvl) {						
						avlReportsReadIn.add(avlReport);
					}
				}
			}					
			
			latesttime=latesttime+(30*Time.MS_PER_SEC);
			// Return all the AVL reports read in
			return avlReportsReadIn;
		} catch (JSONException e) {
			logger.error("Error parsing JSON. {}. {}", e.getMessage(), jsonStr,
					e);
			return new ArrayList<AvlReport>();

		}

	}

	
	private static long parsePlaybackStartTime(String playbackStartTimeStr) {
		try {
			long playbackStartTime = Time.parse(playbackStartTimeStr).getTime();
			
			// If specified time is in the future then reject.
			if (playbackStartTime > System.currentTimeMillis()) {
				logger.error("Playback start time \"{}\" specified by " +
						"transitclock.avl.bullrunner.playbackStartTime parameter is in " +
						"the future and therefore invalid!",
						playbackStartTimeStr);
				System.exit(-1);					
			}
				
			return playbackStartTime;
		} catch (java.text.ParseException e) {
			logger.error("Paramater -t \"{}\" specified by " +
					"transitclock.avl.bullrunner.playbackStartTime parameter could not " +
					"be parsed. Format must be \"MM-dd-yyyy HH:mm:ss\"",
					playbackStartTimeStr);
			System.exit(-1);
			
			// Will never be reached because the above state exits program but
			// needed so compiler doesn't complain.
			return -1;
		}
	}
	private static long parsePlaybackEndTime(String playbackEndTimeStr) {
		try {
			long playbackEndTime = Time.parse(playbackEndTimeStr).getTime();
			
			// If specified time is in the future then reject.
			if (playbackEndTime > System.currentTimeMillis()) {
				logger.error("Playback end time \"{}\" specified by " +
						"transitclock.avl.playbackEndTime parameter is in " +
						"the future and therefore invalid!",
						playbackEndTimeStr);
				System.exit(-1);					
			}
				
			return playbackEndTime;
		} catch (java.text.ParseException e) {
			logger.error("Paramater -t \"{}\" specified by " +
					"transitclock.avl.playbackEndTime parameter could not " +
					"be parsed. Format must be \"MM-dd-yyyy HH:mm:ss\"",
					playbackEndTimeStr);
			System.exit(-1);
			
			// Will never be reached because the above state exits program but
			// needed so compiler doesn't complain.
			return -1;
		}
	}
}
