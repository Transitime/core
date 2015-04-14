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
<div id="title">Historical Reports</div>
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
  <li><a href="scheduleParams.jsp?a=<%= agencyId %>"
    title="Displays in a table the schedule for a specified route.">
      Schedule for Route</a></li>
</ul>
</div>
</body>
</html>