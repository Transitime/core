<%@ page import="org.transitime.reports.PredAccuracyRangeQuery" %>
<%@ page import="org.transitime.utils.Time" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.text.ParseException" %>

<%
    // Get params from the query string
    String agencyId = request.getParameter("a");

	String beginDate = request.getParameter("beginDate");
    String numDays = request.getParameter("numDays");
    String beginTime = request.getParameter("beginTime");
    String endTime = request.getParameter("endTime");

    String routeIds[] = request.getParameterValues("r");
    // source can be "" (for all), "Transitime", or "Other";
	String source = request.getParameter("source");
	
	String predictionType = request.getParameter("predictionType");
	
    int allowableEarlySec = (int) 1.5 * Time.SEC_PER_MIN; // Default value
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

    if (agencyId == null || beginDate == null || numDays == null ) {
		response.getWriter().write("For predAccuracyRangeData.jsp must "
			+ "specify parameters 'a' (agencyId), 'beginDate', "
			+ "and 'numDays'."); 
		return;
    }
	
	// Make sure not trying to get data for too long of a time span since
	// that could bog down the database.
	if (Integer.parseInt(numDays) > 31) {
		throw new ParseException(
				"Number of days of " + numDays + " spans more than a month", 0);
	}

    try {
		// Perform the query.
		PredAccuracyRangeQuery query = new PredAccuracyRangeQuery(agencyId);

		// Convert results of query to a JSON string
		String jsonString = query
			.getJson(beginDate, numDays, beginTime, endTime,
				routeIds, source, predictionType,
				allowableEarlySec, allowableLateSec);

		// If no data then return error status with an error message
		if (jsonString == null || jsonString.isEmpty()) {
		    String message = "No data for beginDate=" + beginDate 
			    + " numDays=" + numDays 
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
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.getWriter().write(jsonString);
    } catch (java.sql.SQLException e) {
    	response.setStatus(400);
    	response.getWriter().write(e.getMessage());    }
%>