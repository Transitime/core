<%@ page import="org.transitclock.reports.ScheduleAdherenceController" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="org.json.JSONArray" %>
<%@ page contentType="application/json" %>
<%

String startDateStr = request.getParameter("beginDate");
String numDaysStr = request.getParameter("numDays");
String startTime = request.getParameter("beginTime");
String endTime = request.getParameter("endTime");
boolean byStop = new Boolean(request.getParameter("byGroup"));
String datatype = request.getParameter("datatype");

if (startTime == null || startTime == "")
	startTime = "00:00:00";
else
	startTime += ":00";

if (endTime == null || endTime == "")
	endTime = "23:59:59";
else
	endTime += ":00";

String stopIdList = request.getParameter("stopIds");
List<String> stopIds = stopIdList == null ? null : Arrays.asList(stopIdList.split(","));


SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
Date startDate = dateFormat.parse(startDateStr);
int numDays  = Integer.parseInt(numDaysStr);

List<Object> results = ScheduleAdherenceController.stopScheduleAdherence(startDate,
		numDays, startTime, endTime, stopIds, byStop, datatype);

JSONArray json = new JSONArray(results);
json.write(out);

%>