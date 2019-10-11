<%@ page import="org.transitclock.db.webstructs.WebAgency" %>
<%@ page import="org.transitclock.reports.ChartGenericJsonQuery" %>
<%@ page import="org.transitclock.utils.Time" %>
<%@ page import="java.text.ParseException" %>
<%@ page import="org.transitclock.reports.SqlUtils" %>
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

WebAgency agency = WebAgency.getCachedWebAgency(agencyId);
String dbtype = agency.getDbType();
boolean isMysql = "mysql".equals(dbtype);

boolean showTooltips = false;
String showTooltipsStr = request.getParameter("tooltips");
if (showTooltipsStr != null && showTooltipsStr.toLowerCase().equals("true"))
    showTooltips = true;
    
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
            if (endTime == null || endTime.isEmpty())
                endTime = "23:59:59";

			timeSql = SqlUtils.timeRangeClause(request, "arrivalDepatureTime", Integer.parseInt(numDays));
        }

// Determine route portion of SQL. Default is to provide info for
// all routes.
        String routeSql = "";
        if (routeId!=null && !routeId.trim().isEmpty()) {
            routeSql = String.format(" AND (routeId='%1$s' OR routeShortName='%1$s')", routeId);
        }

// Determine the source portion of the SQL. Default is to provide
// predictions for all sources
        String sourceSql = "";
        if (source != null && !source.isEmpty()) {
            if (source.equals("TransitClock")) {
                // Only "Transitime" predictions
                sourceSql = " AND predictionSource='TransitClock'";
            } else {
                // Anything but "Transitime"
                sourceSql = " AND predictionSource<>'TransitClock'";
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

        String predLengthSql = "     to_char(predictedTime-predictionReadTime, 'SSSS')::integer";
        String predAccuracySql = "     predictionAccuracyMsecs/1000 ";
        if (isMysql) {
          predLengthSql = "TIMESTAMPDIFF(SECOND,predictionReadTime,predictedTime)";
          predAccuracySql = "CAST(predictionAccuracyMsecs/1000 AS DECIMAL)";
        }

    String tooltipsSql = "";
    if (showTooltips) {
        if (isMysql) {
            tooltipsSql = ", CONCAT("
                    + "\'time=\',"
                    + "predictionReadTime"
                    + ",\'\\n\'"
                    + ",\'stopId=\',"
                    + "stopId"
                    + ",\'\\n\'"
                    + ",\'routeId=\',"
                    + "routeId"
                    + ",\'\\n\'"
                    + ",\'vehicleId=\',"
                    + "vehicleId"
                    + ") as tooltip";

        } else {
            tooltipsSql =
                    ", format(E'predAccuracy=%s\\n"
                            + "prediction=%s\\n"
                            + "stopId=%s\\n"
                            + "routeId=%s\\n"
                            + "tripId=%s\\n"
                            + "arrDepTime=%s\\n"
                            + "predTime=%s\\n"
                            + "predReadTime=%s\\n"
                            + "vehicleId=%s\\n"
                            + "source=%s\\n"
                            + "affectedByLayover=%s', "
                            + "   CAST(predictionAccuracyMsecs || ' msec' AS INTERVAL), predictedTime-predictionReadTime,"
                            + "   stopId, routeId, tripId, "
                            + "   to_char(arrivalDepartureTime, 'HH24:MI:SS.MS MM/DD/YYYY'),"
                            + "   to_char(predictedTime, 'HH24:MI:SS.MS'),"
                            + "   to_char(predictionReadTime, 'HH24:MI:SS.MS'),"
                            + "   vehicleId,"
                            + "   predictionSource,"
                            + "   CASE WHEN affectedbywaitstop THEN 'True' ELSE 'False' END) AS tooltip ";
        }
    }
        
       String sql = "SELECT "
                + predLengthSql + " as predLength, "
                + predAccuracySql + " as predAccuracy "
                + tooltipsSql
                + " FROM PredictionAccuracy "
                + "WHERE "
                + "1=1 "
				+ SqlUtils.timeRangeClause(request, "arrivalDepartureTime", 30)
                + "  AND "+predLengthSql+" <= 1200 "
                + routeSql
                + sourceSql
                + predTypeSql
                // Filter out MBTA_seconds source since it is isn't significantly different from MBTA_epoch.
                // TODO should clean this up by not having MBTA_seconds source at all
                // in the prediction accuracy module for MBTA.
                + "  AND predictionSource <> 'MBTA_seconds' ";



    // Determine the json data by running the query
    String jsonString = ChartGenericJsonQuery.getJsonString(agencyId, sql);


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
