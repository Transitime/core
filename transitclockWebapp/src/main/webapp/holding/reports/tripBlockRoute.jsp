<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="org.transitclock.utils.web.WebUtils" %>
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
<title>Trip Block By Route Report</title>

<style>

</style>

<script>

/* Programatically create contents of table */
function dataReadCallback(jsonData) {
	var table = document.getElementById("dataTable");
	
	for (var i=0; i<jsonData.data.length; ++i) {
		var tripInfo = jsonData.data[i];

		// Insert row (after the header)
		var row = table.insertRow(i+1);
        row.insertCell(0).innerHTML = tripInfo.routeShortName;
        row.insertCell(1).innerHTML = tripInfo.blockId;
        row.insertCell(2).innerHTML = tripInfo.tripShortName;
        row.insertCell(3).innerHTML = convertSecInDayToString(tripInfo.startTime);
        row.insertCell(4).innerHTML = convertSecInDayToString(tripInfo.endTime);

	}
}

// Initiate AJAX call to get data to put into table
$( document ).ready(function() {
  $.ajax({
   	// The page being requested
  	url: "/web/reports/tripBlockRouteData.jsp",
   	// Pass in query string parameters to page being requested
   	data: {<%= WebUtils.getAjaxDataString(request) %>},
  	// Needed so that parameters passed properly to page being requested
   	traditional: true,
    dataType:"json",
	success: dataReadCallback
  });
});
</script>
</head>
<body>
<%@include file="/template/header.jsp" %>
<div id="title">Trip Block By Route</div>
<table id="dataTable">
  <tr><th>Route</th><th>Block</th><th>Trip</th><th>Start Time</th><th>End Time</th></tr>
  </table>
</body>
</html>