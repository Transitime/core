<%@ page import="org.transitime.utils.Time" %>
<%@ page import="org.transitime.db.GenericCsvQuery" %>
<%@ page import="java.text.ParseException" %>
<%-- This file is for outputting prediction accuracy data in CSV format. 
  --%>
<%
// In order to make at least Windows based systems treat the file as
// a CSV file need to change the file name suffix to csv and also
// set the content type. This way the file can be loaded into Excel
// directory from the web browser.
response.setHeader("Content-Disposition", "filename=predAccuracy.csv");
response.setContentType("application/csv");

// Parameters from request
String agencyId = request.getParameter("a");
String beginDate = request.getParameter("beginDate");
String numDays = request.getParameter("numDays");
String beginTime = request.getParameter("beginTime");
String endTime = request.getParameter("endTime");
String routeId =  request.getParameter("r");

if (agencyId == null || beginDate == null || numDays == null) {
	response.getWriter().write("For predAccuracyCsv.jsp must "
		+ "specify parameters 'a' (agencyId), " 
		+ "'beginDate', and 'numDays'."); 
	return;
}

//Determine the time portion of the SQL
String timeSql = "";
if ((beginTime != null && !beginTime.isEmpty()) 
		|| (endTime != null && !endTime.isEmpty())) {
	// If only begin or only end time set then use default value
	if (beginTime == null || beginTime.isEmpty())
		beginTime = "00:00:00";
	if (endTime == null || endTime.isEmpty())
		endTime = "23:59:59";
    timeSql = " AND arrivalDepartureTime::time BETWEEN '" 
		+ beginTime + "' AND '" + endTime + "' ";
}

//Determine route portion of SQL. Default is to provide info for
//all routes.
String routeSql = "";
if (routeId!=null && !routeId.trim().isEmpty()) {
 routeSql = "  AND routeShortName='" + routeId + "' ";
}

// NOTE: this query only works on postgreSQL. For mySQL would need to change
// from using to_char(), not using casting like "::interger", don't put
// single quotes around "'1 day'", etc.
String sql = "SELECT "
		+ "     to_char(predictedTime-predictionReadTime, 'SSSS')::integer as pred_length_secs, "
		+ "     predictionAccuracyMsecs/1000 as accuracy_secs, "
		+ "     predictedTime AS predicted_time,"
		+ "     arrivalDepartureTime AS actual_time,"
		+ "     predictionreadtime AS prediction_read_time,"
		+ "     predictionSource AS source, routeId AS route, " 
		+ "     directionId AS direction, tripId AS trip, "
		+ "     stopId AS stop, vehicleId AS vehicle, " 
		+ "     affectedByWaitStop AS affected_by_wait_stop"
		+ " FROM predictionAccuracy "
		+ "WHERE arrivalDepartureTime BETWEEN cast(? as timestamp) "
		+     "AND cast(? as timestamp) + INTERVAL '" + numDays + " day' "
		+ timeSql
		+ "  AND predictedTime-predictionReadTime < '00:15:00' \n"
		+ routeSql + "\n"
		// Filter out MBTA_seconds source since it is isn't significantly different from MBTA_epoch. 
		// TODO should clean this up by not having MBTA_seconds source at all
		// in the prediction accuracy module for MBTA.
		+ "  AND predictionSource <> 'MBTA_seconds' ";
		
		System.out.println("\nFor prediction accuracy by route query sql=\n" + sql);				
// Do the actual query	
String csvStr = GenericCsvQuery.getCsvString(agencyId, sql, Time.parseDate(beginDate), Time.parseDate(beginDate));
response.getWriter().write(csvStr);
%>