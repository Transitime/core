<%@ page import="org.transitclock.reports.PredAccuracyFiveBucketQuery" %>
<%@ page import="org.transitclock.utils.Time" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.text.ParseException" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>

<%
    // Get params from the query string
    String agencyId = request.getParameter("a");

	String beginDate = request.getParameter("beginDate");
    String numDays = StringUtils.isBlank(request.getParameter("numDays")) ? "1" : request.getParameter("numDays");
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

	String horizonMinStr1 = request.getParameter("horizonMinStr1");
	String horizonMinStr2 = request.getParameter("horizonMinStr2");
	String horizonMinStr3 = request.getParameter("horizonMinStr3");
	String horizonMinStr4 = request.getParameter("horizonMinStr4");
	String horizonMinStr5 = request.getParameter("horizonMinStr5");

	String horizonMaxStr1 = request.getParameter("horizonMaxStr1");
	String horizonMaxStr2 = request.getParameter("horizonMaxStr2");
	String horizonMaxStr3 = request.getParameter("horizonMaxStr3");
	String horizonMaxStr4 = request.getParameter("horizonMaxStr4");
	String horizonMaxStr5 = request.getParameter("horizonMaxStr5");

	String[] allowableEarlyStr = new String[] { allowableEarly1, allowableEarly2, allowableEarly3, allowableEarly4, allowableEarly5 };
	String[] allowableLateStr = new String[] { allowableLate1, allowableLate2, allowableLate3, allowableLate4, allowableLate5 };
	String[] horizonMinStr = new String[] { horizonMinStr1, horizonMinStr2, horizonMinStr3, horizonMinStr4, horizonMinStr5 };
	String[] horizonMaxStr = new String[] { horizonMaxStr1, horizonMaxStr2, horizonMaxStr3, horizonMaxStr4, horizonMaxStr5 };

	Double[] allowableEarlySec = new Double[5];
	Double[] allowableLateSec = new Double[5];
	Integer[] horizonMinSec = new Integer[5];
	Integer[] horizonMaxSec = new Integer[5];

	Map<Integer, Double> defaultEarlySec = new HashMap<>();
	defaultEarlySec.put(0, 1 * 60.0);
	defaultEarlySec.put(1, 1.5 * 60.0);
	defaultEarlySec.put(2, 2.5 * 60.0);
	defaultEarlySec.put(3, 4 * 60.0);
	defaultEarlySec.put(4, 4 * 60.0);

	Map<Integer, Double> defaultLateSec = new HashMap<>();
	defaultLateSec.put(0, 1 * 60.0);
	defaultLateSec.put(1, 2 * 60.0);
	defaultLateSec.put(2, 3.5 * 60.0);
	defaultLateSec.put(3, 6 * 60.0);
	defaultLateSec.put(4, 6 * 60.0);

	Map<Integer, Integer> defaultHorizonMin = new HashMap<>();
	defaultHorizonMin.put(0, 0);
	defaultHorizonMin.put(1,180);
	defaultHorizonMin.put(2, 360);
	defaultHorizonMin.put(3, 720);
	defaultHorizonMin.put(4, 1200);

	Map<Integer, Integer> defaultHorizonMax = new HashMap<>();
	defaultHorizonMax.put(0,180);
	defaultHorizonMax.put(1, 360);
	defaultHorizonMax.put(2, 720);
	defaultHorizonMax.put(3, 1200);
	defaultHorizonMax.put(4, 1800);

	String predictionType = request.getParameter("predictionType");

	String routeIds[] = request.getParameterValues("r");
	String stopIds[];
	String stopIdParam = request.getParameter("s");
    // source can be "" (for all), "Transitime", or "Other";
	String source = StringUtils.isBlank(request.getParameter("source")) ? "TransitClock" : request.getParameter("source");

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
				allowableEarlySec[i] =  Double.parseDouble(allowableEarlyStr[i]) * Time.SEC_PER_MIN;
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
				allowableLateSec[i] = Double.parseDouble(allowableLateStr[i]) * Time.SEC_PER_MIN;
			} catch (NumberFormatException e) {
				response.sendError(416 /* Requested Range Not Satisfiable */,
						"Could not parse Allowable Late value of " + allowableLateStr + "for bucket " + (i > 0 ? 5 * i : 1));
				return;
			}
		} else {
			allowableLateSec[i] = defaultLateSec.get(i);
		}

		if(horizonMinStr[i] != null && !horizonMinStr[i].isEmpty() ) {
			try {
				horizonMinSec[i] = Integer.parseInt(horizonMinStr[i]);
			} catch (NumberFormatException e) {
				response.sendError(416 /* Requested Range Not Satisfiable */,
						"Could not parse Horizon Min value of " + horizonMinStr + "for bucket " + (i > 0 ? 5 * i : 1));
				return;
			}
		} else {
			horizonMinSec[i] = defaultHorizonMin.get(i);
		}

		if(horizonMaxStr[i] != null && !horizonMaxStr[i].isEmpty() ) {
			try {
				horizonMaxSec[i] = Integer.parseInt(horizonMaxStr[i]);
			} catch (NumberFormatException e) {
				response.sendError(416 /* Requested Range Not Satisfiable */,
						"Could not parse Horizon Max value of " + horizonMaxStr + "for bucket " + (i > 0 ? 5 * i : 1));
				return;
			}
		} else {
			horizonMaxSec[i] = defaultHorizonMax.get(i);
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
				allowableEarlySec, allowableLateSec, horizonMinSec, horizonMaxSec);

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