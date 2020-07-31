<%@page import="org.transitclock.db.webstructs.WebAgency"%>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>    
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
    
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Historical Reports</title>
</head>
<body>
<%@include file="/template/header.jsp" %>
<div id="mainDiv">
<div id="title">Historical Reports for <%= WebAgency.getCachedWebAgency(agencyId).getAgencyName() %></div>

<div id="subtitle">Prediction Accuracy<br/><span style="font-size: small">(only for agencies where prediction accuracy stored to database)</span></div>
<ul class="choicesList">
  <li><a href="predAccuracyRangeParams.jsp?a=<%= agencyId %>"
    title="Shows percentage of predictions that were accurate
    to within the specified limits.">
      Prediction Accuracy Range Chart</a></li>
  <li><a href="predAccuracyIntervalsParams.jsp?a=<%= agencyId %>"
    title="Shows average prediction accuracy for each prediction length. Also 
hows upper and lower bounds. Allows one to see for a specified percentage 
what the prediction accuracy is for predictions that lie between the 
specified accuracy range.">
      Prediction Accuracy Interval Chart</a></li>
  <li><a href="predAccuracyScatterParams.jsp?a=<%= agencyId %>" 
    title="Shows each individual datapoint for prediction accuracy. Useful for 
finding specific issues with predictions.">
      Prediction Accuracy Scatter Plot</a></li>
  <li><a href="predAccuracyCsvParams.jsp?a=<%= agencyId %>"
    title="For downloading prediction accuracy data in CSV format.">
      Prediction Accuracy CSV Download</a></li>
<%--  <li><a href="routePerformanceTable.jsp?a=<%= agencyId %>"
    title="Shows route performance, where performance is defined as the 
    number of ontime predictions over the total number of predicitons for a
    given route.">
      Route Performance Table</a></li>--%>
</ul>

<div id="subtitle">AVL Reports</div>
<ul class="choicesList">
  <li><a href="avlMap.jsp?a=<%= agencyId %>"
    title="Displays historic AVL data for a vehicle in a map.">
      AVL Data in Map</a></li>
  <li><a href="avlMapParams.jsp?a=<%= agencyId %>"
    title="Displays historic AVL data for a vehicle in a map.">
      AVL Data in Map (parameters page)</a></li>
  <li><a href="lastAvlReport.jsp?a=<%= agencyId %>"
    title="Displays the last time each vehicle reported its GPS position over the last 24 hours.">
      Last GPS Report by Vehicle</a></li>
</ul>

<%--<div id="subtitle">Event Reports</div>
	<ul class="choicesList">
	  <li><a href="vehicleEventParams.jsp?a=<%= agencyId %>"
	    title="Check that all Events for vehicle.">
	      Event for vehicle</a></li>	  
	</ul>
</div>--%>

<div id="subtitle">Schedule Adherence Reports</div>
<ul class="choicesList">
  <li><a href="schAdhByRouteParams.jsp?a=<%= agencyId %>"
    title="Displays historic schedule adherence data by route in a bar chart. 
    Can compare schedule adherence for multiple routes.">
      Schedule Adherence by Route</a></li>
  <li><a href="schAdhByStopParams.jsp?a=<%= agencyId %>"
    title="Displays historic schedule adherence data for each stop for a 
    route in a bar chart. ">
      Schedule Adherence by Stop</a></li>
  <li><a href="schAdhByTimeParams.jsp?a=<%= agencyId %>"
    title="Displays historic schedule adherence data for a route grouped by 
    how early/late. The resulting bell curve shows the distribution of 
    early/late times. ">
      Schedule Adherence by how Early/Late</a></li>
  <li><a href="onTimePerformanceReport.jsp?a=<%=agencyId%>"
     title="Displays historic on time performance data in a chart for a given route.">
      On Time Performance</a></li>
</ul>


<div id="subtitle">Miscellaneous Reports</div>
<ul class="choicesList">
  <li><a href="scheduleHorizStopsParams.jsp?a=<%= agencyId %>"
    title="Displays in a table the schedule for a specified route.">
      Schedule for Route</a></li>
  <li><a href="scheduleVertStopsParams.jsp?a=<%= agencyId %>"
    title="Displays in a table the schedule for a specified route. Stops listed 
    vertically which is useful for when there are not that many trips per day.">
      Schedule for Route (vertical stops)</a></li>
    <%--<li><a href="tripBlockRouteParams.jsp?a=<%= agencyId %>"
           title="Show all blocks assigned to a route for the selected day with trip times">
        Trip Blocks for Route</a></li>--%>
   <%-- <li><a href="realTimeSchAdhByVehicleParams.jsp?a=<%= agencyId %>"
        title="Real time schedule adherence by vehicle">Real Time Schedule Adherence By Vehicle</a></li>--%>
</ul>

<div id="subtitle">Status Reports</div>
<ul class="choicesList">
  <li><a href="../status/activeBlocks.jsp?a=<%= agencyId %>"
    title="Shows how many block assignments are currently active and if they have assigned vehicles">
      Active Blocks</a></li>
  <li><a href="../status/serverStatus.jsp?a=<%= agencyId %>"
    title="Shows how well system is running, including the AVL feed">
      Server Status</a></li>
</ul>

<div id="subtitle">Speed Reports</div>
<ul class="choicesList">
    <li><a href="speedMap.jsp?a=<%=agencyId%>" title="Displays historic average speed data on a map for a given route and headsign.">
        Speed Map</a></li>
</ul>

<div id="subtitle">Run Time Reports</div>
<ul class="choicesList">
    <li><a href="runTime.jsp?a=<%=agencyId%>" title="Displays historic run time data for a given route and headsign.">
        Run Times</a></li>
</ul>

</div>
</body>
</html>
