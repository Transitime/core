<%-- Displays the schedule for a route--%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="org.transitime.utils.web.WebUtils" %>
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
    
  <style>
  #scheduleTitle {
  	font-size: x-large;
  	margin-top: 30px;
  	margin-bottom: 6px;
  	text-align: center;
  }

  #dataTable {
	font-size: 8pt;
    text-align: center;
  }
    
  #headerCell, #stopCell {
  	font-weight: bold;
  	background-color: #F2F5F7;
  }
  
  #stopCell {
  	white-space: nowrap;
  	text-align: left;
  } 
  
  </style>
  
  <script type="text/javascript" src="https://www.google.com/jsapi"></script>
  
  <script type="text/javascript">
      function dataReadCallback(jsonData) {
	      // Set the title now that have the route name from the API
	      $('#title').html('Schedule for ' + jsonData.routeName);
	      
	      // Go through all service classes and directions for route
    	  for (var i=0; i<jsonData.schedule.length; ++i) {
    		  var schedule = jsonData.schedule[i];
    		  
    		  // Create title for schedule
    		  $('body').append("<div id='scheduleTitle'>" 
    				  + "Direction: " + schedule.directionId 
    				  + ", Service: " + schedule.serviceName
    				  + "</div>");
    		  
    		  var table = $("<table id='dataTable'></table>").appendTo('body')[0];

    		  // Create the columns. First column is stop name. And then there
    		  // is one column per trip.
    		  var headerRow = table.insertRow(0);
    		  headerRow.insertCell(0).id = 'headerCell';
    		  for (var j=0; j<schedule.trip.length; ++j) {
    			  var trip = schedule.trip[j];
    			  var tripName = trip.tripShortName;
    			  if (tripName == null)
    				  tripName = trip.tripId;
    			  var tripNameTooLong = tripName.length > 6;
    			  var html = tripNameTooLong ?
    					  "Block<br/>" + trip.blockId : "Trip<br/>" + tripName;
    					  
    	    	  var headerCell = headerRow.insertCell(j+1);
    	    	  headerCell.id = 'headerCell';
    	    	  headerCell.innerHTML = html;
    		  }

    		  // Add data for each row for the schedule. This is a bit complicated
    		  // because the API provides data per trip but want each row in the
    		  // schedule to be for a particular stop for all trips.
    		  for (var stopIdx=0; stopIdx<schedule.timesForStop.length; ++stopIdx) {
        		  var row = table.insertRow(stopIdx+1);

        		  var timesForStop = schedule.timesForStop[stopIdx];

        		  // Add stop name to row
        		  var headerCell = row.insertCell(0);
        		  headerCell.id = 'stopCell';
        		  headerCell.innerHTML = timesForStop.stopName;
        		  
        		  // Add the times for the stop to the row
    			  for (var tripIdx=0; tripIdx<timesForStop.time.length; ++tripIdx) {    				  
    				  var time = timesForStop.time[tripIdx];
    				  row.insertCell(tripIdx+1).innerHTML = time.timeStr ? time.timeStr : '';
    			  }
    		  }    		  
    	  }
      }
      
      $( document ).ready(function() {
    	  $.ajax({
    	      	// The page being requested
    		  	url: apiUrlPrefix + "/command/scheduleVertStops",
    	      	// Pass in query string parameters to page being requested
    	      	data: {<%= WebUtils.getAjaxDataString(request) %>},
    	    	// Needed so that parameters passed properly to page being requested
    	    	traditional: true,
    	        dataType:"json",
				success: dataReadCallback
    	  });
      });
      
  </script>

<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Schedule Report</title>
</head>
<body>
<%@include file="/template/header.jsp" %>
<div id="title"></div>

</body>
