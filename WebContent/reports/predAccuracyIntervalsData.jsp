<%@ page import="org.transitime.reports.PredictionAccuracyQuery.IntervalsType" %>
<%@ page import="org.transitime.reports.PredictionAccuracyQuery" %>
<%@ page import="java.util.Arrays" %>

<%
    // Get params from the query string
    String routeIds[] = request.getParameterValues("r");
    String dbName = request.getParameter("a");
    // source can be "" (for all), "Transitime", or "Other";
	String source = request.getParameter("source");
	
    IntervalsType intervalsType = IntervalsType
		    .createIntervalsType(request.getParameter("intervalsType"));
	
    double intervalFraction1 = 0.68; // Default value
    String intervalFraction1Str = request.getParameter("intervalFraction1");
    if (intervalFraction1Str != null)
		intervalFraction1 = Double.parseDouble(intervalFraction1Str);
    
    double intervalFraction2 = Double.NaN; // Default value
    String intervalFraction2Str = request.getParameter("intervalFraction2");
    if (intervalFraction2Str != null)
		intervalFraction2 = Double.parseDouble(intervalFraction2Str);

    String beginDate = request.getParameter("beginDate");
    String beginTime = request.getParameter("beginTime");
    String endDate = request.getParameter("endDate");
    String endTime = request.getParameter("endTime");

    if (dbName == null || beginDate == null || beginTime == null 
	    || endDate == null || endTime == null) {
		response.getWriter().write("For predAccuracyIntervalsData.jsp must "
			+ "specify parameters 'a' (agency dbName), 'beginDate', 'beginTime', "
			+ "'endDate', and 'endTime'."); 
		return;
    }
	
    String beginTimeStr = beginDate + " " + beginTime;
    String endTimeStr = endDate + " " + endTime;

    // FIXME These db params are hardcoded for now but they need
    // to come from database
    String dbType = "postgresql";// "mysql";
    String dbHost = "sfmta.c3zbap9ppyby.us-west-2.rds.amazonaws.com";// "localhost";
    String dbUserName = "transitime";// "root";
    String dbPassword = "transitime";

    try {
		// Perform the query.
		PredictionAccuracyQuery query = new PredictionAccuracyQuery(
			dbType, dbHost, dbName, dbUserName, dbPassword);

		// Convert results of query to a JSON string
		String jsonString = query.getJson(beginTimeStr, endTimeStr, routeIds,
			source, intervalsType, intervalFraction1,
			intervalFraction2);

		// If no data then return error status with an error message
		if (jsonString == null || jsonString.isEmpty()) {
		    String message = "No data for beginTime=" + beginTimeStr
			    + " endTime=" + endTimeStr 
			    + " routeIds=" + Arrays.asList(routeIds)
			    + " source=" + source
			    + " intervalsType=" + intervalsType;
		    response.sendError(416 /* Requested Range Not Satisfiable */, 
			    message);
			return;
		}
		
		// Respond with the JSON string
		response.getWriter().write(jsonString);
    } catch (java.sql.SQLException e) {
		// Respond with error message of exception
		response.sendError(400, e.getMessage());
    }
%>