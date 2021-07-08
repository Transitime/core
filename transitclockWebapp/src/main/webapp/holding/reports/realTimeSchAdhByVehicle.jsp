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
<title>Real Time Schedule Adherence By Vehicle Report</title>

<style>

</style>

<script>

/* Programatically create contents of table */
function dataReadCallback(jsonData) {
	var table = document.getElementById("dataTable");
	
	for (var i=0; i<jsonData.data.length; ++i) {
		var schAdhInfo = jsonData.data[i];

		// Insert row (after the header)
		var row = table.insertRow(i+1);
        row.insertCell(0).innerHTML = schAdhInfo.vehicleId;
        row.insertCell(1).innerHTML = schAdhInfo.blockId;
        row.insertCell(2).innerHTML = schAdhInfo.routeShortName;
        row.insertCell(3).innerHTML = schAdhInfo.avlTime;
        row.insertCell(4).innerHTML = schAdhInfo.schedAdh + ' min';

	}
}

// Initiate AJAX call to get data to put into table
$( document ).ready(function() {
  $.ajax({
   	// The page being requested
  	url: "/web/reports/realTimeSchAdhByVehicleData.jsp",
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
<div id="title">Real Time Schedule Adherence By Vehicle</div>
<table id="dataTable">
  <tr><th>Vehicle</th><th>Block</th><th>Route</th><th>Last Updated On</th><th>Schedule Adherence</th></tr>
  </table>
</body>
</html>