<%@ page import="org.transitclock.reports.PredAccuracyFiveBucketQuery" %>
<%@ page import="org.transitclock.utils.Time" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.text.ParseException" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>

<%
    // Get params from the query string
    String agencyId = request.getParameter("a");

	String beginDate = request.getParameter("beginDate");
    String numDays = request.getParameter("numDays");
    if (numDays == null) numDays = "1";
    String beginTime = request.getParameter("beginTime");
    String endTime = request.getParameter("endTime");

    String allowableEarly1 = request.getParameter("allowableEarly1");
	String allowableEarly2 = request.getParameter("allowableEarly2");
	String allowableEarly3 = request.getParameter("allowableEarly3");
	String allowableEarly4 = request.getParameter("allowableEarly4");
	String allowableEarly5 = request.getParameter("allowableEarly5");

	String allowableLate1 = request.getParameter("allowableLate1");
	String allowableLate2 = request.getParameter("allowableLate2");
	String allowableLate3 = request.getParameter("allowableLate3");
	String allowableLate4 = request.getParameter("allowableLate4");
	String allowableLate5 = request.getParameter("allowableLate5");

	String[] allowableEarlyStr = new String[] { allowableEarly1, allowableEarly2, allowableEarly3, allowableEarly4, allowableEarly5 };
	String[] allowableLateStr = new String[] { allowableLate1, allowableLate2, allowableLate3, allowableLate4, allowableLate5 };

	Integer[] allowableEarlySec = new Integer[5];
	Integer[] allowableLateSec = new Integer[5];

	Map<Integer, Integer> defaultEarlySec = new HashMap<>();
	defaultEarlySec.put(0,0);
	defaultEarlySec.put(1, 1*60);
	defaultEarlySec.put(2, 2*60);
	defaultEarlySec.put(3, 2*60);
	defaultEarlySec.put(4, 2*60);

	Map<Integer, Integer> defaultLateSec = new HashMap<>();
	defaultLateSec.put(0, 2*60);
	defaultLateSec.put(1, 2*60);
	defaultLateSec.put(2, 3*60);
	defaultLateSec.put(3, 3*60);
	defaultLateSec.put(4, 4*60);

	String predictionType = request.getParameter("predictionType");

	String routeIds[] = request.getParameterValues("r");
	String stopIds[];
	String stopIdParam = request.getParameter("s");
    // source can be "" (for all), "Transitime", or "Other";
	String source = request.getParameter("source");

	if(stopIdParam != null){
		stopIds = stopIdParam.trim().split("\\s*,\\s*");
		if(stopIds.length > 0){
			stopIds[0] = stopIds[0].trim();
		}
	} else {
		stopIds = new String[0];
	}

	for (int i=0; i < allowableEarlyStr.length; i++) {
		if(allowableEarlyStr[i] != null && !allowableEarlyStr[i].isEmpty() ) {
			try {
				allowableEarlySec[i] = (int) Double.parseDouble(allowableEarlyStr[i]) * Time.SEC_PER_MIN;
			} catch (NumberFormatException e) {
				response.sendError(416 /* Requested Range Not Satisfiable */,
						"Could not parse Allowable Early value of " + allowableEarlyStr + "for bucket " + (i > 0 ? 5 * i : 1));
				return;
			}
		} else {
			allowableEarlySec[i] = defaultEarlySec.get(i);
		}

		if(allowableLateStr[i] != null && !allowableLateStr[i].isEmpty() ) {
			try {
				allowableLateSec[i] = (int) Double.parseDouble(allowableLateStr[i]) * Time.SEC_PER_MIN;
			} catch (NumberFormatException e) {
				response.sendError(416 /* Requested Range Not Satisfiable */,
						"Could not parse Allowable Late value of " + allowableLateStr + "for bucket " + (i > 0 ? 5 * i : 1));
				return;
			}
		} else {
			allowableLateSec[i] = defaultLateSec.get(i);
		}
	}

	if (agencyId == null || beginDate == null || numDays == null ) {
		response.getWriter().write("For predAccuracyBucketData.jsp must "
				+ "specify parameters 'a' (agencyId=" +  agencyId + "), 'beginDate' (" + beginDate + ") , "
				+ "and 'numDays' (" + numDays + ") + .");
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
		PredAccuracyFiveBucketQuery query = new PredAccuracyFiveBucketQuery(agencyId);

		// Convert results of query to a JSON string
		String jsonString = query
			.getJson(beginDate, numDays, beginTime, endTime,
				routeIds, stopIds, source, predictionType,
				allowableEarlySec, allowableLateSec);

		// If no data then return error status with an error message
		if (jsonString == null || jsonString.isEmpty()) {
		    String message = "No data for beginDate=" + beginDate 
			    + " numDays=" + numDays 
			    + " beginTime=" + beginTime
			    + " endTime=" + endTime
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