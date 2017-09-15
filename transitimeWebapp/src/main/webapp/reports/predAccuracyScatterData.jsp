<%@ page import="org.transitime.reports.ChartGenericJsonQuery" %>
<%@ page import="org.transitime.utils.Time" %>
<%@ page import="java.text.ParseException" %>
<%
	// Parameters from request
String agencyId = request.getParameter("a");
String beginDate = request.getParameter("beginDate");
String numDays = request.getParameter("numDays");
String beginTime = request.getParameter("beginTime");
String endTime = request.getParameter("endTime");
String routeId =  request.getParameter("r");
String source = request.getParameter("source");
String predictionType = request.getParameter("predictionType");

boolean showTooltips = true;
String showTooltipsStr = request.getParameter("tooltips");
if (showTooltipsStr != null && showTooltipsStr.toLowerCase().equals("false"))
    showTooltips = false;
    
if (agencyId == null || beginDate == null || numDays == null) {
		response.getWriter().write("For predAccuracyScatterData.jsp must "
	+ "specify parameters 'a' (agencyId), " 
	+ "'beginDate', and 'numDays'."); 
		return;
}

// Make sure not trying to get data for too long of a time span since
// that could bog down the database.
if (Integer.parseInt(numDays) > 31) {
	throw new ParseException(
			"Number of days of " + numDays + " spans more than a month", 0);
}

// Determine the time portion of the SQL
String timeSql = "";
if ((beginTime != null && !beginTime.isEmpty())
		|| (endTime != null && !endTime.isEmpty())) {
	// If only begin or only end time set then use default value
	if (beginTime == null || beginTime.isEmpty())
		beginTime = "00:00:00";
	else {
		// beginTimeStr set so make sure it is valid, and prevent 
		// possible SQL injection
		if (!beginTime.matches("\\d+:\\d+"))
			throw new ParseException("begin time \"" + beginTime 
					+ "\" is not valid.", 0);
	}
	if (endTime == null || endTime.isEmpty())
		endTime = "23:59:59";
	else {
		// endTimeStr set so make sure it is valid, and prevent 
		// possible SQL injection
		if (!endTime.matches("\\d+:\\d+"))
			throw new ParseException("end time \"" + endTime 
					+ "\" is not valid.", 0);
	}

    timeSql = " AND arrivalDepartureTime::time BETWEEN '" 
		+ beginTime + "' AND '" + endTime + "' ";
}

// Determine route portion of SQL. Default is to provide info for
// all routes.
String routeSql = "";
if (routeId != null && !routeId.trim().isEmpty()) {
    routeSql = "  AND (routeId='" + routeId + "' OR routeShortName='" + routeId + "') ";
}

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

// Determine SQL for prediction type ()
String predTypeSql = "";
if (predictionType != null && !predictionType.isEmpty()) {
    if (source.equals("AffectedByWaitStop")) {
		// Only "AffectedByLayover" predictions
		predTypeSql = " AND affectedByWaitStop = true ";
    } else {
		// Only "NotAffectedByLayover" predictions
		predTypeSql = " AND affectedByWaitStop = false ";
    }
}

String tooltipsSql = "";
if (showTooltips)
    tooltipsSql = 	
    	", format(E'predAccuracy= %s\\n"
    	+         "prediction=%s\\n"
		+         "stopId=%s\\n"
		+         "routeId=%s\\n"
		+         "tripId=%s\\n"
		+         "arrDepTime=%s\\n"
		+         "predTime=%s\\n"
		+         "predReadTime=%s\\n"
		+         "vehicleId=%s\\n"
		+         "source=%s\\n"
		+         "affectedByLayover=%s', "
		+ "   CAST(predictionAccuracyMsecs || ' msec' AS INTERVAL), predictedTime-predictionReadTime,"
		+ "   stopId, routeId, tripId, "
		+ "   to_char(arrivalDepartureTime, 'HH24:MI:SS.MS MM/DD/YYYY'),"
		+ "   to_char(predictedTime, 'HH24:MI:SS.MS'),"
		+ "   to_char(predictionReadTime, 'HH24:MI:SS.MS'),"
		+ "   vehicleId,"
		+ "   predictionSource," 
		+ "   CASE WHEN affectedbywaitstop THEN 'True' ELSE 'False' END) AS tooltip ";
    
String sql = "SELECT "
	+ "     to_char(predictedTime-predictionReadTime, 'SSSS')::integer as predLength, "
	+ "     predictionAccuracyMsecs/1000 as predAccuracy "
	+ tooltipsSql
	+ " FROM predictionAccuracy "

	+ "WHERE arrivalDepartureTime BETWEEN cast(? as timestamp) " 
	+     " AND cast(? as timestamp)" + " + INTERVAL '1 day' "
	+ timeSql
	+ "  AND predictedTime-predictionReadTime < '00:15:00' "
	+ routeSql
	+ sourceSql
	+ predTypeSql
	// Filter out MBTA_seconds source since it is isn't significantly different from MBTA_epoch. 
	// TODO should clean this up by not having MBTA_seconds source at all
	// in the prediction accuracy module for MBTA.
	+ "  AND predictionSource <> 'MBTA_seconds' ";

			
			
// Determine the json data by running the query
String jsonString = ChartGenericJsonQuery.getJsonString(agencyId, sql, Time.parseDate(beginDate), Time.parseDate(beginDate));

// If no data then return error status with an error message
if (jsonString == null || jsonString.isEmpty()) {
    String message = "No data for beginDate=" + beginDate
	    + " numDays=" + numDays 
	    + " beginTime=" + beginTime
	    + " endTime=" + endTime 
	    + " routeId=" + routeId
	    + " source=" + source
	    + " predictionType=" + predictionType;
	response.setStatus(400);
	response.getWriter().write(message);
	return;
}

// Return the JSON data
response.setHeader("Access-Control-Allow-Origin", "*");
response.getWriter().write(jsonString);
%>