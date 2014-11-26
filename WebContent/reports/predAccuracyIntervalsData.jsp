<%@ page import="org.transitime.reports.PredictionAccuracyQuery.IntervalsType" %>
<%@ page import="org.transitime.reports.PredAccuracyIntervalQuery" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.text.ParseException" %>

<%
    // Get params from the query string
    String dbName = request.getParameter("a");

	String beginDate = request.getParameter("beginDate");
    String endDate = request.getParameter("endDate");
    String beginTime = request.getParameter("beginTime");
    String endTime = request.getParameter("endTime");

    String routeIds[] = request.getParameterValues("r");
    // source can be "" (for all), "Transitime", or "Other";
	String source = request.getParameter("source");
	
	String predictionType = request.getParameter("predictionType");
	
    IntervalsType intervalsType = IntervalsType
		    .createIntervalsType(request.getParameter("intervalsType"));
	
    double intervalPercentage1 = 0.68; // Default value
	String intervalPercentage1Str = request.getParameter("intervalPercentage1");
    try {
    	if (intervalPercentage1Str != null && !intervalPercentage1Str.isEmpty())
			intervalPercentage1 = Double.parseDouble(intervalPercentage1Str);
    } catch (NumberFormatException e) {
	    response.sendError(416 /* Requested Range Not Satisfiable */, 
		    "Could not parse Interval Percentage 1 of " + intervalPercentage1Str);
	    return;	    
    }

    double intervalPercentage2 = Double.NaN; // Default value
	String intervalPercentage2Str = request.getParameter("intervalPercentage2");
    try {
    	if (intervalPercentage2Str != null && !intervalPercentage2Str.isEmpty())
			intervalPercentage2 = Double.parseDouble(intervalPercentage2Str);
    } catch (NumberFormatException e) {
	    response.sendError(416 /* Requested Range Not Satisfiable */, 
		    "Could not parse Interval Percentage 2 of " + intervalPercentage2Str);
	    return;	    
    }

    if (dbName == null || beginDate == null || endDate == null ) {
		response.getWriter().write("For predAccuracyIntervalsData.jsp must "
			+ "specify parameters 'a' (agency dbName), 'beginDate', "
			+ "and 'endDate'."); 
		return;
    }
	
    // FIXME These db params are hardcoded for now but they need
    // to come from database
    String dbType = "postgresql";// "mysql";
    String dbHost = "sfmta.c3zbap9ppyby.us-west-2.rds.amazonaws.com";// "localhost";
    String dbUserName = "transitime";// "root";
    String dbPassword = "transitime";

    try {
		// Perform the query.
		PredAccuracyIntervalQuery query = new PredAccuracyIntervalQuery(
			dbType, dbHost, dbName, dbUserName, dbPassword);

		// Convert results of query to a JSON string
		String jsonString = query
			.getJson(beginDate, endDate, beginTime, endTime,
				routeIds, source, predictionType,
				intervalsType, intervalPercentage1,
				intervalPercentage2);

		// If no data then return error status with an error message
		if (jsonString == null || jsonString.isEmpty()) {
		    String message = "No data for beginDate=" + beginDate 
			    + " endDate=" + endDate 
			    + " beginTime=" + beginTime
			    + " endTime=" + endTime 
			    + " routeIds=" + Arrays.asList(routeIds) 
			    + " source=" + source
			    + " predictionType=" + predictionType
			    + " intervalsType=" + intervalsType;
		    response.sendError(
			    416 /* Requested Range Not Satisfiable */, message);
		    return;
		}

		// Respond with the JSON string
		response.getWriter().write(jsonString);
    } catch (java.sql.SQLException e) {
		// Respond with error message of exception
		response.sendError(400, e.getMessage());
    }
%>