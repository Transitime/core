<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="org.transitclock.reports.ScheduleAdherenceController" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%
String agencyId = request.getParameter("a");
if (agencyId == null || agencyId.isEmpty()) {
    response.getWriter().write("You must specify agency in query string (e.g. ?a=mbta)");
    return;
}
%>

<html>
<head>
  <%@include file="/template/includes.jsp" %>

  <link href="<%= request.getContextPath() %>/css/active-blocks.css" rel="stylesheet"/>
<script type="text/javascript">
  var ALLOWABLE_EARLY_MSEC = <%= ScheduleAdherenceController.getScheduleEarlySeconds() %> * -1000;
  var ALLOWABLE_LATE_MSEC  = <%= ScheduleAdherenceController.getScheduleLateSeconds() %> * 1000;
  var LICENSE_FILTER_LIST  = '<%= ScheduleAdherenceController.getLicenseFilterList() %>'.split(",");
  var LICENSE_FILTER_SET  = new Set(LICENSE_FILTER_LIST);

</script>
  <script src="<%= request.getContextPath() %>/javascript/active-blocks.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Active Blocks</title>
</head>
<body>
<%@include file="/template/header.jsp" %>

<div id="title">Active Blocks</div>
<div id="menu">
	<button id="loadAllData">Load all data</button>
</div>
<div id="accordion"></div>
<div id="summary">
  <span id="totalBlocksLabel" title="Total number of blocks">Blocks:</span>
  <span id="totalBlocksValue" title="Total number of blocks"></span>
  <span id="percentWithVehiclesLabel" title="Percentage of blocks that have an assigned and predictable vehicle">Assigned:</span>
  <span id="percentWithVehiclesValue" title="Percentage of blocks that have an assigned and predictable vehicle"></span>
  <span id="percentLateLabel" title="Percentage of blocks where vehicle is more than <%= ScheduleAdherenceController.getScheduleLateSeconds()/60 %> minutes late">Late:</span>
  <span id="percentLateValue" title="Percentage of blocks where vehicle is more than <%= ScheduleAdherenceController.getScheduleLateSeconds()/60 %> minutes late"></span>
  <span id="percentOnTimeLabel" title="Percentage of blocks where vehicle is on time">OnTime:</span>
  <span id="percentOnTimeValue" title="Percentage of blocks where vehicle is on time"></span>
  <span id="percentEarlyLabel" title="Percentage of blocks where vehicle is more than <%= ScheduleAdherenceController.getScheduleEarlySeconds()/-60 %> minute(s) early">Early:</span>
  <span id="percentEarlyValue" title="Percentage of blocks where vehicle is more than <%= ScheduleAdherenceController.getScheduleEarlySeconds()/-60 %> minute(s) early"></span>
  <span id="asOfLabel" title="Time that summary information was last updated">As of:</span>
  <span id="asOfValue" title="Time that summary information was last updated"></span>
</div>
</body>
</html>
