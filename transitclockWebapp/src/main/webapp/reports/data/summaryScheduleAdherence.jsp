<%@ page import="org.transitclock.reports.ScheduleAdherenceController" %>
<%@ page import="org.transitclock.utils.Time" %>
<%@ page import="java.sql.Timestamp" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>

<%@ page contentType="application/json" %>
<%
// todo this code should be in a struts action
String startDateStr = request.getParameter("beginDate");
String numDaysStr = StringUtils.isBlank(request.getParameter("numDays")) ? "1" : request.getParameter("numDays");
String startTime = request.getParameter("beginTime");
String endTime = request.getParameter("endTime");
String earlyLimitStr = request.getParameter("allowableEarly");
String lateLimitStr = request.getParameter("allowableLate");
Double earlyLimit = -60.0;
Double lateLimit = 60.0;

if (StringUtils.isEmpty(startTime))
	startTime = "00:00:00";
else
	startTime += ":00";

if (StringUtils.isEmpty(endTime))
	endTime = "23:59:59";
else
	endTime += ":00";

if (!StringUtils.isEmpty(earlyLimitStr)) {
	earlyLimit = Double.parseDouble(earlyLimitStr) * 60;
}
if (!StringUtils.isEmpty(lateLimitStr)) {
	lateLimit = Double.parseDouble(lateLimitStr) * 60;
}


String routeIdList = request.getParameter("r");
List<String> routeIds = routeIdList == null ? null : Arrays.asList(routeIdList.split(","));


SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
Date startDate = dateFormat.parse(startDateStr);

List<Integer> results = ScheduleAdherenceController.routeScheduleAdherenceSummary(startDate,
		Integer.parseInt(numDaysStr), startTime, endTime, earlyLimit, lateLimit, routeIds);

JSONArray json = new JSONArray(results);
json.write(out);

%>