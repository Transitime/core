<%@ page import="org.transitime.reports.ScheduleAdherenceController" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="org.json.JSONArray" %>
<%@ page contentType="application/json" %>
<%
// todo this code should be in a struts action
String startDateStr = request.getParameter("beginDate");
String endDateStr = request.getParameter("endDate");
String startTime = request.getParameter("startTime");
String endTime = request.getParameter("endTime");
String earlyLimitStr = request.getParameter("allowableEarly");
String lateLimitStr = request.getParameter("allowableLate");
Double earlyLimit = -60.0;
Double lateLimit = 60.0;

if (startTime == null || startTime == "")
	startTime = "00:00:00";
else
	startTime += ":00";

if (endTime == null || endTime == "")
	endTime = "23:59:59";
else
	endTime += ":00";

if (earlyLimitStr != null && earlyLimitStr != "") {
	earlyLimit = Double.parseDouble(earlyLimitStr) * 60;
}
if (lateLimitStr != null && lateLimitStr != "") {
	lateLimit = Double.parseDouble(lateLimitStr) * 60;
}


String routeIdList = request.getParameter("routeIds");
List<String> routeIds = routeIdList == null ? null : Arrays.asList(routeIdList.split(","));


SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
Date startDate = dateFormat.parse(startDateStr);
Date endDate = dateFormat.parse(endDateStr);

List<Integer> results = ScheduleAdherenceController.routeScheduleAdherenceSummary(startDate,
		endDate, startTime, endTime, earlyLimit, lateLimit, routeIds);

JSONArray json = new JSONArray(results);
json.write(out);

%>