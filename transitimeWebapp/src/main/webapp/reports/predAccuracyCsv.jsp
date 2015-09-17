<%@ page import="org.transitime.db.GenericCsvQuery" %>
<%@ page import="org.transitime.db.webstructs.WebAgency" %>
<%@ page import="org.transitime.utils.Time" %>
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
String endDate = request.getParameter("endDate");
String beginTime = request.getParameter("beginTime");
String endTime = request.getParameter("endTime");
String routeId =  request.getParameter("r");

if (agencyId == null || beginDate == null || endDate == null) {
	response.getWriter().write("For predAccuracyCsv.jsp must "
		+ "specify parameters 'a' (agencyId), " 
		+ "'beginDate', and 'endDate'."); 
	return;
}

//Make sure not trying to get data for too long of a time span since
//that could bog down the database.
long timespan = Time.parseDate(endDate).getTime() - 
	Time.parseDate(beginDate).getTime() + 1*Time.MS_PER_DAY;
if (timespan > 31*Time.MS_PER_DAY) {
	throw new ParseException("Begin date to end date spans more than a month", 0);
}

//Determine the time portion of the SQL
String timeSql = "";
if (beginTime != null && !beginTime.isEmpty() 
		&& endTime != null && !endTime.isEmpty()) {
 timeSql = " AND arrivalDepartureTime::time BETWEEN '" 
		+ beginTime + "' AND '" + endTime + "' ";
}

//Determine route portion of SQL. Default is to provide info for
//all routes.
String routeSql = "";
if (routeId!=null && !routeId.isEmpty()) {
 routeSql = "  AND routeId='" + routeId + "' ";
}

// NOTE: this query only works on postgreSQL. For mySQL would need to change
// from using to_char(), not using casting like "::interger", don't put
// single quotes around "'1 day'", etc.

    WebAgency agency = WebAgency.getCachedWebAgency(agencyId);
    String dbtype = agency.getDbType();
    String sql = null;
    if(dbtype.equals("mysql")){
        sql = "SELECT "
                + "     predictedTime-predictionReadTime as pred_length_secs, "
                + "     predictionAccuracyMsecs/1000 as accuracy_secs, "
                + "     predictedTime AS predicted_time,"
                + "     arrivalDepartureTime AS actual_time,"
                + "     predictionreadtime AS prediction_read_time,"
                + "     predictionSource AS source, routeId AS route, "
                + "     directionId AS direction, tripId AS trip, "
                + "     stopId AS stop, vehicleId AS vehicle, "
                + "     affectedByWaitStop AS affected_by_wait_stop"
                + " FROM predictionAccuracy "
                + "WHERE arrivalDepartureTime BETWEEN STR_TO_DATE('" + beginDate + "', '%Y-%m-%d') and DATE_ADD(STR_TO_DATE('" + endDate + "', '%Y-%m-%d'),INTERVAL 1 DAY) "
                + "  AND predictedTime-predictionReadTime < (15 * 60) "
                + routeSql
                // Filter out MBTA_seconds source since it is isn't significantly different from MBTA_epoch.
                // TODO should clean this up by not having MBTA_seconds source at all
                // in the prediction accuracy module for MBTA.
                + "  AND predictionSource <> 'MBTA_seconds' ";
    }else{
        sql = "SELECT "
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
                + "WHERE arrivalDepartureTime BETWEEN '" + beginDate
                +     "' AND TIMESTAMP '" + endDate + "' + INTERVAL '1 day' "
                + timeSql
                + "  AND predictedTime-predictionReadTime < '00:15:00' "
                + routeSql
                // Filter out MBTA_seconds source since it is isn't significantly different from MBTA_epoch.
                // TODO should clean this up by not having MBTA_seconds source at all
                // in the prediction accuracy module for MBTA.
                + "  AND predictionSource <> 'MBTA_seconds' ";
    }

// Do the actual query	
String csvStr = GenericCsvQuery.getCsvString(agencyId, sql);
response.getWriter().write(csvStr);
%>