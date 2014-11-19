<%@ page import="org.transitime.reports.JsonDataFeed" %>
<%
// Parameters from request
String dbName = request.getParameter("a");
String routeId =  request.getParameter("r");
String beginDate = request.getParameter("beginDate");
String beginTime = request.getParameter("beginTime");
String endDate = request.getParameter("endDate");
String endTime = request.getParameter("endTime");
String source = request.getParameter("source");

boolean showTooltips = true;
String showTooltipsStr = request.getParameter("tooltips");
if (showTooltipsStr != null && showTooltipsStr.toLowerCase().equals("false"))
    showTooltips = false;
    
if (dbName == null || beginDate == null || beginTime == null 
	|| endDate == null || endTime == null) {
		response.getWriter().write("For predAccuracyScatterData.jsp must "
			+ "specify parameters 'a' (agency dbName), " 
			+ "'beginDate', 'beginTime', 'endDate', and 'endTime'."); 
		return;
}

String beginTimeStr = beginDate + " " + beginTime;
String endTimeStr = endDate + " " + endTime;

// Hardcoded parameters for database
String dbType = "postgresql";// "mysql";
String dbHost = "sfmta.c3zbap9ppyby.us-west-2.rds.amazonaws.com";// "localhost";
String dbUserName = "transitime";// "root";
String dbPassword = "transitime";

// Determine the source portion of the SQL. Default is to provide
// predictions for all sources
String sourceSql = "";
if (source != null && !source.isEmpty()) {
    if (source.equals("Transitime")) {
		// Only "Transitime" predictions
		sourceSql = " AND predictionSource='Transitime'";
    } else {
		// Anything but "Transitime"
		sourceSql = " AND predictionSource<>'Transitime'";
    }
}

String tooltipsSql = "";
if (showTooltips)
    tooltipsSql = 	
    	", format(E'predAccuracy= %s prediction=%s\\\\n"
		+         "stop=%s routeId=%s\\\\n"
		+         "tripId=%s\\\\n"
		+         "arrDepTime=%s\\\\n"
		+         "predTime=%s predReadTime=%s\\\\n"
		+         "vehicleId=%s source=%s', "
		+ "   CAST(predictionAccuracyMsecs || ' msec' AS INTERVAL), predictedTime-predictionReadTime,"
		+ "   stopId, routeId, tripId, "
		+ "   to_char(arrivalDepartureTime, 'HH:MI:SS.MS MM/DD/YYYY'),"
		+ "   to_char(predictedTime, 'HH:MI:SS.MS'),"
		+ "   to_char(predictionReadTime, 'HH:MI:SS.MS'),"
		+ "   vehicleId,"
		+ "   predictionSource) AS tooltip ";
    
String sql = "SELECT "
	+ "     to_char(predictedTime-predictionReadTime, 'SSSS')::integer as predLength, "
	+ "     predictionAccuracyMsecs/1000 as predAccuracy "
	+ tooltipsSql
	+ " FROM predictionaccuracy "
	+ "WHERE arrivaldeparturetime BETWEEN '" + beginTimeStr + "' AND '" + endTimeStr + "' "
	+ "  AND predictedTime-predictionReadTime < '00:15:00' "
	+ ((routeId!=null && !routeId.isEmpty())? "  AND routeId='" + routeId + "' " : "")
	+ sourceSql
	// Filter out MBTA_seconds source since it is isn't significantly different from MBTA_epoch. 
	// TODO should clean this up by not having MBTA_seconds source at all
	// in the prediction accuracy module for MBTA.
	+ "  AND predictionSource <> 'MBTA_seconds' ";

// Determine the json data by running the query
String jsonString = JsonDataFeed.getJsonData(sql, dbType, dbHost, dbName,
	    dbUserName, dbPassword);

// If no data then return error status with an error message
if (jsonString == null || jsonString.isEmpty()) {
    String message = "No data for beginTime=" + beginTimeStr
	    + " endTime=" + endTimeStr 
	    + " routeId=" + routeId
	    + " source=" + source;
    response.sendError(416 /* Requested Range Not Satisfiable */, 
	    message);
	return;
}

// Return the JSON data
response.getWriter().write(jsonString);
%>