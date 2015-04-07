<%@ page import="org.transitime.reports.PredAccuracyRangeQuery" %>
<%@ page import="org.transitime.utils.Time" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.text.ParseException" %>

<%
    // Get params from the query string
    String agencyId = request.getParameter("a");

	String beginDate = request.getParameter("beginDate");
    String endDate = request.getParameter("endDate");
    String beginTime = request.getParameter("beginTime");
    String endTime = request.getParameter("endTime");

    String routeIds[] = request.getParameterValues("r");
    // source can be "" (for all), "Transitime", or "Other";
	String source = request.getParameter("source");
	
	String predictionType = request.getParameter("predictionType");
	
    int allowableEarlySec = (int) -1.5 * Time.SEC_PER_MIN; // Default value
	String allowableEarlyStr = request.getParameter("allowableEarly");
    try {
    	if (allowableEarlyStr != null && !allowableEarlyStr.isEmpty())
    	allowableEarlySec = 
    		(int) Double.parseDouble(allowableEarlyStr) * Time.SEC_PER_MIN;
    } catch (NumberFormatException e) {
	    response.sendError(416 /* Requested Range Not Satisfiable */, 
		    "Could not parse Allowable Early value of " + allowableEarlyStr);
	    return;	    
    }

    int allowableLateSec = (int) 4.0 * Time.SEC_PER_MIN; // Default value
	String allowableLateStr = request.getParameter("allowableLate");
    try {
    	if (allowableLateStr != null && !allowableLateStr.isEmpty())
    	allowableLateSec = 
    		(int) Double.parseDouble(allowableLateStr) * Time.SEC_PER_MIN;
    } catch (NumberFormatException e) {
	    response.sendError(416 /* Requested Range Not Satisfiable */, 
		    "Could not parse Allowable Late value of " + allowableLateStr);
	    return;	    
    }

    if (agencyId == null || beginDate == null || endDate == null ) {
		response.getWriter().write("For predAccuracyRangeData.jsp must "
			+ "specify parameters 'a' (agencyId), 'beginDate', "
			+ "and 'endDate'."); 
		return;
    }
	
    try {
		// Perform the query.
		PredAccuracyRangeQuery query = new PredAccuracyRangeQuery(agencyId);

		// Convert results of query to a JSON string
		String jsonString = query
			.getJson(beginDate, endDate, beginTime, endTime,
				routeIds, source, predictionType,
				allowableEarlySec, allowableLateSec);

		// If no data then return error status with an error message
		if (jsonString == null || jsonString.isEmpty()) {
		    String message = "No data for beginDate=" + beginDate 
			    + " endDate=" + endDate 
			    + " beginTime=" + beginTime
			    + " endTime=" + endTime 
			    + " routeIds=" + Arrays.asList(routeIds) 
			    + " source=" + source
			    + " allowableEarlyMsec=" + allowableEarlySec
			    + " allowableLateMsec=" + allowableLateSec;
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