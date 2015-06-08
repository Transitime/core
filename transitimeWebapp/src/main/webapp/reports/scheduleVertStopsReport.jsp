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

  .stopColumnClass {
  	font-weight: bold;
  	background-color: #F2F5F7;
  }
  
  </style>
  
  <script type="text/javascript" src="https://www.google.com/jsapi"></script>
  
  <script type="text/javascript">
      // Load in Google charts library
      google.load("visualization", "1", {packages:["table"]});
      google.setOnLoadCallback(getDataAndDrawChart);
      
      function centerTablesKludge() {
          $(".google-visualization-table-table").css("width", "");
          $(".google-visualization-table-table").css("margin-left", "auto");
          $(".google-visualization-table-table").css("margin-right", "auto");
          $(".google-visualization-table").css("width", "");    	  
      }

      // Sets the property for the entire column, except for the header.
      // Need to use this for Table since it ignores column wide properties.
      // Can be used to set propertyName of 'style' or 'className'.
      function setColumnProperty(dataTable, columnIndex, propertyName, propertyValue) {
    	  for (var rowIndex=0; rowIndex<dataTable.getNumberOfRows(); ++rowIndex) {
    		  dataTable.setProperty(rowIndex, columnIndex, propertyName, propertyValue);
    	  }
      }
      
      function dataReadCallback(jsonData) {
	      var tableOptions = {
	    		  showRowNumber: false, 
	    		  allowHtml: true, 
	    		  sort: 'disable'
	      };

	      // Set the title now that have the route name from the API
	      $('#title').html(jsonData.routeName);
	      
	      // Go through all service classes and directions for route
    	  for (var i=0; i<jsonData.schedule.length; ++i) {
    		  var schedule = jsonData.schedule[i];
    		  
    		  // Create title for schedule
    		  $('body').append("<div id='scheduleTitle'>" 
    				  + "Direction " + schedule.directionId 
    				  + ", " + schedule.serviceName
    				  + "</div>");
    		  
    		  // Create data for the schedule table
    		  var data = new google.visualization.DataTable();
    		  
    		  // Create the columns. First column is stop name. And then there
    		  // is one column per trip.
    		  data.addColumn('string', '', 'stopColumn');
    		  for (var j=0; j<schedule.trip.length; ++j) {
    			  var trip = schedule.trip[j];
    			  var tripName = trip.tripShortName;
    			  if (tripName == null)
    				  tripName = trip.tripId;
    			  var tripNameTooLong = tripName.length > 6;
    			  var html = tripNameTooLong ?
    					  "Block<br/>" + trip.blockId : "Trip<br/>" + tripName;
        		  data.addColumn("string", html);    			  
    		  }

    		  // Add data for each row for the schedule. This is a bit complicated
    		  // because the API provides data per trip but want each row in the
    		  // schedule to be for a particular stop for all trips.
    		  for (var stopIdx=0; stopIdx<schedule.timesForStop.length; ++stopIdx) {
        		  var rowArray = [];
        		  // Add stop name to row
        		  rowArray.push(schedule.timesForStop[stopIdx].stopName);
        		  
        		  // Add the times for the stop to the row
        		  var timesForStop = schedule.timesForStop[stopIdx];
    			  for (var tripIdx=0; tripIdx<timesForStop.time.length; ++tripIdx) {    				  
    				  var time = timesForStop.time[tripIdx];
    				  rowArray.push(time.timeStr);
    			  }
        		  
        		  // Add row of data to the data table
        		  data.addRow(rowArray);
    		  }    		  

    		  // Reduce horizontal padding so can fit in more trips per page.
    		  // Tried by setting class for the cells but that didn't work because
    		  // apparently google charts overrides the padding for the class. But
    		  // setting the style works.
    		  for (var tripIdx=0; tripIdx<schedule.timesForStop[0].time.length; ++tripIdx)
    		  	setColumnProperty(data, tripIdx, 'style', 'padding: 2px 1px;');
    		  
    	      // Make stop cells bold. When setting to class
    	      // stopColumnClass also need to set to google-visualization-table-td
    	      // because otherwise the default properties for those cells are erased.
			  setColumnProperty(data, 0, 'className', 'stopColumnClass google-visualization-table-td');
    		  
    		  // Create the div to be used as the data table. Note that
    		  // appendTo() returns JQuery object. Therefore need to use 
    		  // rather cryptic "[0]" to get the HTML DOM object.
    		  var tableDiv = $("<div id='dataTable'>the table</div>").appendTo('body')[0];
    		  
    		  // Actually create the table for the schedule using the appropriate data
    		  var table = new google.visualization.Table(tableDiv);
    	      table.draw(data, tableOptions);
    	  }
	      
	      // Deal with having table only take up minimal space and to be centered
    	  centerTablesKludge();
      }
      
      function getData() {
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
      }
      
      function getDataAndDrawChart() {
    	getData();  
      }
      
  </script>

<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Schedule Report</title>
</head>
<body>
<%@include file="/template/header.jsp" %>
<div id="title"></div>

</body>
