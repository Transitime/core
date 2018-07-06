/*
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Transitime.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Transitime.org .  If not, see <http://www.gnu.org/licenses/>.
 */
package org.transitclock.api.rootResources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Date;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.transitclock.api.data.ApiCommandAck;
import org.transitclock.api.utils.StandardParameters;
import org.transitclock.api.utils.WebUtils;
import org.transitclock.db.GenericQuery;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.db.structs.MeasuredArrivalTime;
import org.transitclock.db.structs.AvlReport.AssignmentType;
import org.transitclock.ipc.data.IpcAvl;
import org.transitclock.ipc.data.IpcTrip;
import org.transitclock.ipc.interfaces.CommandsInterface;
import org.transitclock.ipc.interfaces.ConfigInterface;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.servers.Servers;

@Path("/key/{key}/agency/{agency}")
public class CommandsApi {

	private static final String AVL_SOURCE = "API";
	
	/**
	 * Reads in a single AVL report specified by the query string parameters
	 * v=vehicleId
	 * &t=epochTimeInMsec&lat=latitude&lon=longitude&s=speed(optional)
	 * &h=heading(option) . Can also optionally specify assignmentType="1234" and
	 * assignmentType="BLOCK_ID" or ROUTE_ID, TRIP_ID, or TRIP_SHORT_NAME.
	 * 
	 * @param stdParameters
	 * @param vehicleId
	 * @param time
	 * @param lat
	 * @param lon
	 * @param speed
	 *            (optional)
	 * @param heading
	 *            (optional)
	 * @param assignmentId
	 *            (optional)
	 * @param assignmentTypeStr
	 *            (optional)
	 * @return ApiCommandAck response indicating whether successful
	 * @throws WebApplicationException
	 */
	@Path("/command/pushAvl")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Operation(summary="Reads in a single AVL report specified by the query string parameters",
	description="Reads in a single AVL report specified by the query string parameters.",tags= {"operation","vehicle","avl"})
	public Response pushAvlData(
			@BeanParam StandardParameters stdParameters,
			@Parameter(description="VehicleId. Unique identifier of the vehicle.",required=true)@QueryParam(value = "v") String vehicleId,
			@Parameter(description="GPS epoch time in msec.",required=true) @QueryParam(value = "t") long time,
			@Parameter(description="Latitude of AVL reporte. Decimal degrees.",required=true) @QueryParam(value = "lat") double lat,
			@Parameter(description="Longitude of AVL reporte. Decimal degrees.",required=true) @QueryParam(value = "lon") double lon,
			@Parameter(description="Speed of AVL reporte. m/s.",required=false) @QueryParam(value = "s") @DefaultValue("NaN") float speed,
			@Parameter(description="Heading of AVL report. Degrees. 0 degrees=North. Should be set to Float.NaN if speed not available",required=false)@QueryParam(value = "h") @DefaultValue("NaN") float heading,
			@Parameter(description="Indicates the assignmet id of the AVL report according to the assingment tyoe. For example, if assingment type is ROUTE_ID, the assingment ID should be one route_id loaded in the system.", required=false)@QueryParam(value = "assignmentId") String assignmentId,
			@Parameter(description="Indicates the assignmet type of the AV report. This parameter can take the next values: <ul><li>ROUTE_ID</li><li>TRIP_ID</li>TRIP_SHORT_NAME</li> </ul>")@QueryParam(value = "assignmentType") String assignmentTypeStr)
			throws WebApplicationException {
		// Make sure request is valid
		stdParameters.validate();

		if (vehicleId == null || vehicleId.isEmpty())
			throw WebUtils.badRequestException("Must specify vehicle ID using "
					+ "\"v=vehicleId\"");
		if (time == 0)
			throw WebUtils.badRequestException("Must specify GPS epoch time in "
					+ "msec using for example \"t=14212312333\"");
		
		try {
			// Get RMI interface for sending the command
			CommandsInterface inter = stdParameters.getCommandsInterface();
			
			// Create and send an IpcAvl report to the server
			AvlReport avlReport = new AvlReport(vehicleId, time, 
					lat, lon, speed, heading, AVL_SOURCE);
			
			// Deal with assignment info if it is set
			if (assignmentId != null) {
				AssignmentType assignmentType = AssignmentType.BLOCK_ID;
				if (assignmentTypeStr.equals("ROUTE_ID"))
					assignmentType = AssignmentType.ROUTE_ID;
				else if (assignmentTypeStr.equals("TRIP_ID"))
					assignmentType = AssignmentType.TRIP_ID;
				else if (assignmentTypeStr.equals("TRIP_SHORT_NAME"))
					assignmentType = AssignmentType.TRIP_SHORT_NAME;

				avlReport.setAssignment(assignmentId, assignmentType);
			}
			
			IpcAvl ipcAvl = new IpcAvl(avlReport);
			inter.pushAvl(ipcAvl);
			
			// Create the acknowledgment and return it as JSON or XML
			ApiCommandAck ack = new ApiCommandAck(true, "AVL processed");
			return stdParameters.createResponse(ack);
		} catch (Exception e) {
			// If problem getting data then return a Bad Request
			throw WebUtils.badRequestException(e);
		}

	}

	/**
	 * Converts the request body input stream into a JSON object
	 * 
	 * @param requestBody
	 * @return the corresponding JSON object
	 * @throws IOException
	 * @throws JSONException
	 */
	private static JSONObject getJsonObject(InputStream requestBody)
			throws IOException, JSONException {
		// Read in the request body to a string
		BufferedReader reader =
				new BufferedReader(new InputStreamReader(requestBody));
		StringBuilder strBuilder = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			strBuilder.append(line);
		}
		reader.close();

		// Convert string to JSON object
		return new JSONObject(strBuilder.toString());
	}
	
	/**
	 * Processes a POST http request contain AVL data in the message body
	 * in JSON format. The data format is:
	 * <p>
	 * {avl: [{v: "vehicleId1", t: epochTimeMsec, lat: latitude, lon: longitude, 
	 *         s:speed(optional), h:heading(optional)},
	 *        {v: "vehicleId2", t: epochTimeMsec, lat: latitude, lon: longitude, 
	 *         s: speed(optional), h: heading(optional)},
	 *        {etc...}
	 *        ]
	 * }
	 * <p>
	 * Note: can also specify assignment info using 
	 * "assignmentId: 4321, assignmentType: TRIP_ID"
	 * where assignmentType can be BLOCK_ID, ROUTE_ID, TRIP_ID, or 
	 * TRIP_SHORT_NAME.
	 * 
	 * @param stdParameters
	 * @param requestBody
	 * @return ApiCommandAck response indicating whether successful
	 * @throws WebApplicationException
	 */
	@Path("/command/pushAvl")
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Operation(summary="Reads in a single AVL report in the message body.",
	description="Reads in a single AVL report specified by the query string parameters. <p>{avl: [{v: \"vehicleId1\", t: epochTimeMsec, lat: latitude, lon: longitude, s:speed(optional), h:heading(optional)},\r\n" + 
			"  {v: \"vehicleId2\", t: epochTimeMsec, lat: latitude, lon: longitude, " + 
			" s: speed(optional), h: heading(optional)}, "+
			" {etc...}]</p>. Can also specify assignment info using "
			+ " \"assignmentId: 4321, assignmentType: TRIP_ID\" " + 
			" where assignmentType can be BLOCK_ID, ROUTE_ID, TRIP_ID, or " + 
			" TRIP_SHORT_NAME.",tags= {"operation","vehicle","avl"})
	public Response pushAvlData(@BeanParam StandardParameters stdParameters,
			@Parameter(description="Json of avl report.",required=true)InputStream requestBody) throws WebApplicationException {
		// Make sure request is valid
		stdParameters.validate();

		Collection<IpcAvl> avlData = new ArrayList<IpcAvl>();
		try {
			// Process the AVL report data from the JSON object
			JSONObject jsonObj = getJsonObject(requestBody);
			JSONArray jsonArray = jsonObj.getJSONArray("avl");
			for (int i = 0; i < jsonArray.length(); ++i) {
				JSONObject avlJsonObj = jsonArray.getJSONObject(i);
				String vehicleId = avlJsonObj.getString("v");
				long time = avlJsonObj.getLong("t");
				double lat = avlJsonObj.getDouble("lat");
				double lon = avlJsonObj.getDouble("lon");
				float speed = avlJsonObj.has("s") ? 
						(float) avlJsonObj.getDouble("s") : Float.NaN;
				float heading = avlJsonObj.has("h") ? 
						(float) avlJsonObj.getDouble("h") : Float.NaN;
						
				// Convert the AVL info into a IpcAvl object to sent to server
				AvlReport avlReport =
						new AvlReport(vehicleId, time, lat, lon, speed,
								heading, AVL_SOURCE);
				
				// Handle assignment info if there is any
				if (avlJsonObj.has("assignmentId")) {
					String assignmentId = avlJsonObj.getString("assignmentId");
					AssignmentType assignmentType = AssignmentType.BLOCK_ID;
					if (avlJsonObj.has("assignmentType")) {
						String assignmentTypeStr =
								avlJsonObj.getString("assignmentType");
						if (assignmentTypeStr.equals("ROUTE_ID"))
							assignmentType = AssignmentType.ROUTE_ID;
						else if (assignmentTypeStr.equals("TRIP_ID"))
							assignmentType = AssignmentType.TRIP_ID;
						else if (assignmentTypeStr.equals("TRIP_SHORT_NAME"))
							assignmentType = AssignmentType.TRIP_SHORT_NAME;
					}
					avlReport.setAssignment(assignmentId, assignmentType);
				}
				
				// Add new IpcAvl report to array of AVL reports to be handled
				avlData.add(new IpcAvl(avlReport));
			}

			// Get RMI interface and send the AVL data to server
			CommandsInterface inter = stdParameters.getCommandsInterface();
			inter.pushAvl(avlData);
		} catch (JSONException | IOException e) {
			// If problem getting data then return a Bad Request
			throw WebUtils.badRequestException(e);
		}

		// Create the acknowledgment and return it as JSON or XML
		ApiCommandAck ack = new ApiCommandAck(true, "AVL processed");
		return stdParameters.createResponse(ack);
	}
	@Path("/command/resetVehicle")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Operation(summary="Reset a vehicle",
	description="This is to give the means of manually setting a vehicle unpredictable and unassigned so it will be reassigned quickly.",
	tags= {"command","vehicle"})
	public Response getVehicles(@BeanParam StandardParameters stdParameters,
			@Parameter(description="List of vechilesId.")@QueryParam(value = "v") List<String> vehicleIds) throws WebApplicationException {
		// Make sure request is valid
		stdParameters.validate();
		
		try {
			CommandsInterface inter = stdParameters.getCommandsInterface();
			
			for(String vehicleId: vehicleIds)
			{
				inter.setVehicleUnpredictable(vehicleId);
			}
		} catch (RemoteException e) {			
			throw WebUtils.badRequestException(e.getMessage());
		}
				
		ApiCommandAck ack = new ApiCommandAck(true, "Vehicle reset");
		
		return stdParameters.createResponse(ack);		
	}

	/**
	 * Reads in information from request and stores arrival information into db.
	 * 
	 * @param stdParameters
	 * @param routeId
	 * @param stopId
	 * @return
	 * @throws WebApplicationException
	 */
	@Path("/command/pushMeasuredArrivalTime")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Operation(summary="Reads in information from request and stores arrival information into db",
	description="For storing a measured arrival time so that can see if measured arrival time via GPS is accurate.",tags= {"command"})
	public Response pushAvlData(
			@BeanParam StandardParameters stdParameters,
			@Parameter(description="Route id",required=true) @QueryParam(value = "r") String routeId,
			@Parameter(description="Route short name.",required=true) @QueryParam(value = "rShortName") String routeShortName,
			@Parameter(description="Route stop id.",required=true) @QueryParam(value = "s") String stopId,
			@Parameter(description="Direcction id.",required=true) @QueryParam(value = "d") String directionId,
			@Parameter(description="headsign.",required=true) @QueryParam(value = "headsign") String headsign)
			throws WebApplicationException {
		// Make sure request is valid
		stdParameters.validate();

		try {
			// Store the arrival time in the db
			String agencyId = stdParameters.getAgencyId();
			
			MeasuredArrivalTime time =
					new MeasuredArrivalTime(new Date(), stopId, routeId,
							routeShortName, directionId, headsign);
			String sql = time.getUpdateSql();
			GenericQuery query = new GenericQuery(agencyId);
			query.doUpdate(sql);
			
			// Create the acknowledgment and return it as JSON or XML
			ApiCommandAck ack =
					new ApiCommandAck(true, "MeasuredArrivalTime processed");
			return stdParameters.createResponse(ack);
		} catch (Exception e) {
			// If problem getting data then return a Bad Request
			throw WebUtils.badRequestException(e);
		}
	}
	@Path("/command/cancelTrip/{tripId}")
	@GET //SHOULD BE POST,IT IS AN UPDATE
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Operation(summary="Cancel a trip in order to be shown in GTFS realtime.",
	description="<font color=\"#FF0000\">Experimental. It will work olny with the correct version.</font> It cancel a trip that has no vechilce assigned."
	,tags= {"command","trip"})
	
	public Response cancelTrip(@BeanParam StandardParameters stdParameters,
			@Parameter(description="tripId to be marked as canceled.",required=true)@PathParam("tripId") String tripId)
	{
		stdParameters.validate();
		String agencyId = stdParameters.getAgencyId();
		System.out.println(agencyId);
		String result=null;
		try
		{
			CommandsInterface inter = stdParameters.getCommandsInterface();
			//We need to get the block id in order to get the vehicle
			ConfigInterface cofingInterface = stdParameters.getConfigInterface();
			IpcTrip ipcTrip = cofingInterface.getTrip(tripId);
			if(ipcTrip==null)
				throw WebUtils.badRequestException("TripId=" + tripId + " does not exist.");
			String blockId=ipcTrip.getBlockId();
			result=inter.cancelTrip(blockId);
			System.out.println(result);
		}
		catch (RemoteException e) {
			e.printStackTrace();
			throw WebUtils.badRequestException("Could not send request to Core server. "+e.getMessage());
		}
		if(result==null)
			return stdParameters.createResponse(new ApiCommandAck(true,"Processed"));
		else
			return stdParameters.createResponse(new ApiCommandAck(true,result));
	}
}
