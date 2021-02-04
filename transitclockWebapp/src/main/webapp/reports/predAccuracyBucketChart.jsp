<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="org.transitclock.utils.web.WebUtils" %>
<%@page import="org.transitclock.db.webstructs.WebAgency"%>
<%     
// Determine all the parameters from the query string

// Determine agency using "a" param
String agencyId = request.getParameter("a");

// Determine list of routes for title using "r" param.
// Note that can specify multiple routes.
String stopIds[] = request.getParameterValues("s");
String routeIds[] = request.getParameterValues("r");
String titleRoutes = "";
if (routeIds != null && !routeIds[0].isEmpty()) {
    titleRoutes += ", route ";
    if (routeIds.length > 1) 
        titleRoutes += "s";
    titleRoutes += routeIds[0];
    for (int i=1; i<routeIds.length; ++i) {
		String routeId = routeIds[i];
	    titleRoutes += " & " + routeId;
    }
}

String sourceParam = request.getParameter("source");
String source = (sourceParam != null && !sourceParam.isEmpty()) ? 
	", " + sourceParam + " predictions" : ""; 
String beginDate = request.getParameter("beginDate");
String numDays = request.getParameter("numDays");
if (numDays == null) numDays = "1";
String beginTime = request.getParameter("beginTime");
String endTime = request.getParameter("endTime");

String chartTitle = "Prediction Accuracy Bucket Chart for "
    + WebAgency.getCachedWebAgency(agencyId).getAgencyName()   
	+ titleRoutes 
	+ source 
	+ ", " + beginDate + " for " + numDays + " day" + (Integer.parseInt(numDays) > 1 ? "s" : "");
	
if ((beginTime != null && !beginTime.isEmpty()) || (endTime != null && !endTime.isEmpty())) {
	chartTitle += ", " + beginTime + " to " + endTime;
}

%>  
	  
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <%@include file="/template/includes.jsp" %>
  <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
  <title>Prediction Accuracy</title>

    <style>
      .google-visualization-tooltip {
        font-family: arial, sans-serif;
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

      #summary {
      	font-family: arial, sans-serif;
      	width: 100%;
      	text-align: center;
      	margin-top: 1%;
      }
      
      #errorMessage {
		  display: none;
          position: fixed;
	      top: 30px;
	      margin-left: 20%;
	      margin-right: 20%;
	      height: 100%;
	      text-align: center;
	      font-family: sans-serif;
	      font-size: large;
	      z-index: 9999;
		}
    </style>
</head>

<body>
  <%@include file="/template/header.jsp" %>
  
  <div id="chart_div" style="width: 100%; height: 600px;"></div>
  <div id="loading"></div>
  <div id="errorMessage"></div>
  <div id="summary"><small>Schedule Adherence loading....</small></div>
</body>

<script type="text/javascript" src="https://www.google.com/jsapi"></script>

<script type="text/javascript">

// Updates chart when page is resized. But only does so at most
// every 200 msec so that don't bog system down trying to repeatedly
// update the chart.
var globalTimer;
window.onresize = function () {
             clearTimeout(globalTimer);
             globalTimer = setTimeout(drawChart, 100)
           };
           
var globalDataTable = null;

function getDataTable() {
  var jsonTextData = $.ajax({
  	// The page being requested
    url: "predAccuracyBucketData.jsp",
	// Pass in query string parameters to page being requested
	data: {<%= WebUtils.getAjaxDataString(request) %>},
 	// Needed so that parameters passed properly to page being requested
 	traditional: true,
     dataType:"json",
     async: false,
     // When successful read in data into the JSON table used by the chart
     success: function(jsonData) {
        console.log(jsonData);
       globalDataTable = new google.visualization.DataTable(jsonData);
       },
     // When there is an AJAX problem alert the user
     error: function(request, status, error) {
        console.log(request.responseText)
        var msg = $("<p>").html("<br>No data for requested parameters. Hit back button to try other parameters.")
     	$("#errorMessage").append(msg);
        $("#errorMessage").fadeIn("slow");
       },
     }).responseJSON;
 }

function drawChart() {
	var chartOptions = {
	         title: '<%= chartTitle %>',
	         titleTextStyle: {fontSize: 28},
	         //tooltip: {isHtml: true},
	         isStacked: true,
	         series: [{'color': '#E84D5F'}, {'color': '#6FD656'}, {'color': '#F0DB56'}],
	         legend: 'bottom',
	         chartArea: {
	                // Use most of available area. But need to not use 100% or else 
	                // labels won't appear
	            	width:'90%', 
	            	height:'70%', 
	            	// Make chart a bit graay so that it stands out
	            	backgroundColor: '#f2f2f2'},
	         hAxis: {
	            	title: 'Actual Time Until Stop Arrival (minutes)',
	            	// So that last column is labeled
	            	maxValue: 5,
	            	// Want a gridline for every minute, not just the default of 5 gridlines
	       	        gridlines: {count: 20},
	       	        // Nice to show a faint line for every 30 seconds as well
	       	    },
	         vAxis: {
	        	 title: '% of Predictions Within Range',
	        	 maxValue: 100,
	        	 // Specify ticks so that when column adds up to just over 100% the horizontal
	        	 // part of chart not increased to 120% to accomodate it.
	        	 ticks: [
	        	          {v:0, f:'0'},
	        	          {v:20, f:'20'},
	        	          {v:40, f:'40'},
	        	          {v:60, f:'60'},
	        	          {v:80, f:'80'},
	        	          {v:100, f:'100'}]
	             },

	};

    var chart = new google.visualization.ColumnChart(document.getElementById('chart_div'));

    chart.draw(globalDataTable, chartOptions);	
}

function parseSummary(data) {
	var results = [];
	data.forEach(function(d) {
		results.push(d);
	});
	document.getElementById('summary').innerHTML = "Schedule Adherence over " + results[0] + " arrival and departures<br/>"
	+ "Early: <b>" + results[1]
	+ "</b>% OnTime: <b>" + results[2] 
	+ "</b>% Late: <b>" + results[3] + "</b>%";
	
}

function formatQueryParams() {
  return "<%= WebUtils.getQueryParamsString(request) %>";
}

function showSummary() {
	$("#summary").show();
	$.get("data/summaryScheduleAdherence.jsp?"+formatQueryParams(), parseSummary)
	.fail(function() {
		document.getElementById('summary').innerHTML ="Error!";
	});
}

function getDataAndDrawChart() {
    getDataTable();
    if (globalDataTable != null) {
		drawChart();
		showSummary();
    }
	
    // Now that chart has been drawn faceout the loading image
    $("#loading").fadeOut("slow");
}

// Start visualization after the body created so that the
// page loading image will be displayed right away
google.load("visualization", "1", {packages:["corechart"]});
google.setOnLoadCallback(getDataAndDrawChart);
</script>
</html>
