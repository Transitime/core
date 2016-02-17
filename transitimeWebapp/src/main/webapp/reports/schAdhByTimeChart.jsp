<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="org.transitime.utils.web.WebUtils" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
  <head>
    <%@include file="/template/includes.jsp" %>

  <style>
    #chart_div {
      width: 98%; 
      height: 600px;
      margin-top: 10px;
      margin-left: 10px;
    }
    
    #loading {
        position: fixed;
		left: 0px;
		top: 0px;
		width: 100%;
		height: 100%;
		z-index: 9999;
		background: url('images/page-loader.gif') 50% 50% no-repeat rgb(249,249,249);
      }
      
    #errorMessage {
	  display: none;
      position: fixed;
	  left: 0px;
	  top: 0px;
	  width: 100%;
	  height: 100%;
	  padding-top: 100px;
	  text-align: center;
	  font-family: sans-serif;
	  font-size: large;
	  z-index: 9999;
	  background: white;
	}
  </style>

    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Schedule Adherence</title>
  
    <script type="text/javascript" src="https://www.google.com/jsapi"></script>

  </head>
  <body>
    <%@include file="/template/header.jsp" %>
  
  <%
  String allowableEarly = request.getParameter("allowableEarly");;
  String allowableLate = request.getParameter("allowableLate");;
  String chartSubtitle = allowableEarly + " min early to " 
    + allowableLate + " min late</br>" 
	+ request.getParameter("dateRange");
  
  String beginTime = request.getParameter("beginTime");
  String endTime = request.getParameter("endTime");
  if ((beginTime != null && !beginTime.isEmpty()) 
		  || (endTime != null && !endTime.isEmpty())) {
	  if (beginTime.isEmpty())
		  beginTime = "00:00"; // default value
	  if (endTime.isEmpty())
		  endTime = "24:00";   // default value
		  chartSubtitle += ", " + beginTime + " to " + endTime;
  }  
%>
    <div id="title"></div>
    <div id="subtitle"><%= chartSubtitle %></div>
    <div id="chart_div"></div>
    <div id="loading"></div>
    <div id="errorMessage"></div>
  </body>
  
  <script type="text/javascript">
  

var MAX_LATE_BUCKET = -1800;
var MAX_EARLY_BUCKET = 1200;
	
var globalDataTable;
var globalChartOptions;

function drawChart() {
    // Actually create the chart
    var chart = new google.visualization.ColumnChart(document.getElementById('chart_div'));
    chart.draw(globalDataTable, globalChartOptions);    
}

/**
 * Reads in jsonData, creates globalDataTable and globalChartOptions, and then
 * draws the chart using drawChart().
 */
function createDataTableAndDrawChart(jsonData) {
    // Initialize the data array with the header that describes the columns
	var dataArray = [[
		'Early/Late', 
		'Stops', 
		{ role: 'style' }, 
		{ role: 'annotation' }, 
		{ role: 'tooltip' }
	]];
	
    var minBucket = 0;
    var maxBucket = 0;
    
    for (var i in jsonData.data) {
    	var bucket = jsonData.data[i];
    	
    	// Ignore buckets that are really early or really late
    	if (bucket.time_period < MAX_LATE_BUCKET || bucket.time_period > MAX_EARLY_BUCKET)
    		continue;
    	
    	// Keep track of min and max bucket so can create tick marks just
    	// for where there is data.
    	if (bucket.time_period < minBucket)
    		minBucket = bucket.time_period;
    	if (bucket.time_period > maxBucket)
    		maxBucket = bucket.time_period;
    	
    	// Create row of data for chart using the current time bucket
    	var timeFloor = bucket.time_period / 60.0;
    	
    	var counts = bucket.counts_per_time_period;
    	
    	var color;
    	if (bucket.time_period / 60.0 < -<%= allowableLate %>)
    		color = '#F0DB56'; // The late color
    	else if (bucket.time_period / 60.0 < <%= allowableEarly %>)
    		color = '#6FD656'; // The ontime color
    	else
    		color = '#E84D5F'; // The early color
    	var style = 'color: ' + color + '; stroke-color: #888; stroke-width: 1';
    	
    	var annotation = bucket.counts_per_time_period;
    	
    	var tooltip = bucket.counts_per_time_period + ' stops for vehicles that are ';
    	if (bucket.time_period < 0) {
    		// vehicle late
    		tooltip += -(bucket.time_period+30) + 's to ' + (-bucket.time_period) + 's late. ';
    	} else {
    		// vehicle early
    		tooltip += bucket.time_period + 's to ' + (bucket.time_period+30) + 's early. ';
    	}
    	
    	var row = [timeFloor, counts, style, annotation, tooltip];
    	dataArray.push(row);
    }
	globalDataTable = google.visualization.arrayToDataTable(dataArray);
	

	// By putting the tick marks at partial intervals, such as 0.75,
	// but then labeling them with the integer label we cleverly
	// get the labels to be in exactly the right place. Also
	// want to only show tick marks for range where there is actually data
	// so that horizontal axis is drawn as wide as possible. This is
	// important becauses if tick marks are configured then they are
	// drawn even if there is no corresponding data.
	var ticks = []; 
	// Find minimum tick that is divisible by 2
	var minTick = Math.ceil(minBucket/60/2) * 2;
	var maxTick = Math.floor(maxBucket/60/2) * 2;
	for (var tick = minTick; tick <= maxTick; tick += 2) {
		ticks.push({v:tick-0.25	, f:''+tick});
	}

	// The options for the chart
    globalChartOptions = {
          animation: {
          	 startup: true,
          	 duration: 500, 
          	 easing: 'out'
          },
          chartArea: {top:10, width: '86%', height: '90%'},
          vAxis: {
        	  minValue: 0,
        	  title: "Number of stops per time interval",
        	  textStyle: {fontSize: 12},
        	  },
          hAxis: { 
        	  ticks: ticks,
        	  title: "Minutes vehicle late (negative) or early (positive)",
        	  // The chart always draws the baseline at value 0 over the chart.
        	  // Since the baseline isn't true zero, since using bars and 
        	  // putting tick marks at the edges of the bars, don't want this
        	  // the line to be so visible. Only thing that we can do is to
        	  // make it the same color as the bar, which which will be
        	  // the ontime color, or the early color if allowable early is
        	  // set to zero.
        	  baselineColor: <%= allowableEarly %> > 0 ? '#6FD656' : '#E84D5F',
        	  },
          bar: {groupWidth: "100%"},
          legend: { position: 'none' },
          annotations: {
        	    textStyle: {fontSize: 10}},
    };

    // Get rid of the loading icon
    $("#loading").fadeOut("fast");

	drawChart();
}

function determineChartTitle(routeData) {
	var agencyName = routeData.agency;
	var routeName = routeData.routes[0].name;
	$("#title").html('Schedule Adherence for ' + routeName);
}

function getDataAndDrawChart() {
    // Get agency and route name for titles
	$.getJSON(apiUrlPrefix + "/command/routes?r=<%= request.getParameter("r") %>", 
			  determineChartTitle);	

	$.ajax({
	  	// The page being requested
	    url: "schAdhByTimeData.jsp",
		// Pass in query string parameters to page being requested
		data: {<%= WebUtils.getAjaxDataString(request) %>},
	 	// Needed so that parameters passed properly to page being requested
	 	traditional: true,
	    // When successful read in data into the JSON table used by the chart
	    success: createDataTableAndDrawChart,
	    // When there is an AJAX problem alert the user
	    error: function(request, status, error) {
	       //alert(error + '. ' + request.responseText);
	     	$("#errorMessage").html(request.responseText +
	     			"<br/><br/>Hit back button to try other parameters.");
	        $("#errorMessage").fadeIn("fast");
	        $("#loading").fadeOut("slow");
	       },
	    });
}

// Start visualization after the body created so that the
// page loading image will be displayed right away
google.load("visualization", "1.1", {packages:["corechart"]});
//google.load("visualization", "1.1", {packages:["bar"]});
google.setOnLoadCallback(getDataAndDrawChart);

// Updates chart when page is resized. But only does so at most
// every 300 msec so that don't bog system down trying to repeatedly
// update the chart.
var globalTimer;
window.onresize = function () {
          clearTimeout(globalTimer);
          globalTimer = setTimeout(drawChart, 300)
        };

</script>

</html>