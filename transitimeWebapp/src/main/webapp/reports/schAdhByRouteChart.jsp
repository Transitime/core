<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="org.transitime.utils.web.WebUtils" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <style>
    #chart_div {
      width: 98%; 
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
  
  <%@include file="/template/includes.jsp" %>
  
  <!--  Needed for google charts -->
  <script type="text/javascript" src="https://www.google.com/jsapi"></script>
  
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Schedule Adherence</title>
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
  if (!beginTime.isEmpty() || !endTime.isEmpty()) {
	  if (beginTime.isEmpty())
		  beginTime = "00:00"; // default value
	  if (endTime.isEmpty())
		  endTime = "24:00";   // default value
      chartSubtitle += ", " + beginTime + " to " + endTime;
  }  
%>

  <div id="title">Schedule Adherence by Route</div>
  <div id="subtitle"><%= chartSubtitle %></div>
  <div id="chart_div"></div>
  <div id="loading"></div>
  <div id="errorMessage"></div>
</body>

<script type="text/javascript">

var globalDataTable;
var globalNumberOfRoutes;

  function drawChart() {      
      var vertSpaceForTitleAndHAxis = 150;
	  var vertSpaceForChart = (globalNumberOfRoutes + 1) * 36;
	  var chartDivHeight = vertSpaceForTitleAndHAxis + vertSpaceForChart;
	  
      // Need to set height of the chart div if more than 5 elements
      // so that they fit properly
      if (globalNumberOfRoutes > 0) {
      	$("#chart_div").height(chartDivHeight);
      }
      
        var options = {
          animation: {
        	 startup: true,
        	 duration: 500, 
        	 easing: 'out'
          },
          bars: 'horizontal',
          isStacked: 'percent',
          
          // Don't want lots of space between bars
          bar: { groupWidth: '95%' },
          
          // Make chart area a fixed offset so won't have default behavior of
          // making offset really big if the chart is really tall.
          // Use small height than default to make sure units show for hAxis.
          // Use smaller width than default so that have room for route
          // names and the legend on the right
          chartArea: {top: 10, height: vertSpaceForChart, width: '70%'},
          
          // Need to set font size for things since default is to use font 
          // size proportional to height of chart and since could show many
          // routes can end up with silly large fonts.
          annotations: {textStyle: {fontSize: 12}},
          tooltip: {textStyle: {fontSize: 16}},
          vAxis: {textStyle: {fontSize: 16}},
          hAxis: {textStyle: {fontSize: 16}},
          legend: {textStyle: {fontSize: 16}, position: 'right'},
          
          // Use proper colors for late, ontime, and early
          series: [{'color': '#F0DB56'}, {'color': '#6FD656'}, {'color': '#E84D5F'}],
        };

        // Use old style charts instead of the Material ones because animation
        // only seems to work with old style ones.
        var chart = new google.visualization.BarChart(document.getElementById('chart_div'));
        chart.draw(globalDataTable, options);
        
        // Following is for Google "Material" chart but found that many things 
        // didn't work, like animation.
        //var chart = new google.charts.Bar(document.getElementById('chart_div'));
        //chart.draw(globalDataTable, google.charts.Bar.convertOptions(options));
  }
  
  function createDataTableAndDrawChart(jsonData) {
	  // Initialize dataArray with the column info
	  var dataArray = [[
	                  'Route', 
	                  'Late',    {role: 'style'}, { role: 'tooltip'}, { role: 'annotation'},
	                  'On Time', {role: 'style'}, { role: 'tooltip'}, { role: 'annotation'},
	                  'Early',   {role: 'style'}, { role: 'tooltip'}, { role: 'annotation'}
	                  ]];
	  
	  // Add data for each route to the dataArray
	  var totalEarly = 0;
	  var totalOntime = 0;
	  var totalLate = 0;
	  for (var i=0; i<jsonData.data.length; ++i) {
		  var route = jsonData.data[i];
		  var earlyPercent = (100.0 * route.early / route.total); 
		  var ontimePercent = (100.0 * route.ontime / route.total); 
		  var latePercent = (100.0 * route.late / route.total); 
		  var dataArrayForRoute = [
		             route.name, 
		             latePercent, 
		             	'opacity: 1.0', 
		             	'Late: ' + route.late + ' out of ' + route.total + ' stops', 
		             	route.late > 0 ? latePercent.toFixed(1) + '%' : '',
		             ontimePercent, 
		             	'opacity: 1.0', 
		             	'On time: ' + route.ontime + ' out of ' + route.total + ' stops', 
	             	 	route.ontime > 0 ? ontimePercent.toFixed(1) + '%' : '',
			         earlyPercent, 
		             	'opacity: 1.0', 
		             	'Early: ' + route.early + ' out of ' + route.total + ' stops', 
		             	(route.early > 0) ? earlyPercent.toFixed(1) + '%' : ''
		             ];
		  dataArray.push(dataArrayForRoute);
		  
		  totalEarly += route.early;
		  totalOntime += route.ontime;
		  totalLate += route.late;
	  }
	  
	  // Add totals row to output if showing data for more than a single route
	  if (jsonData.data.length > 1) {
		  var totalTotal = totalEarly + totalOntime + totalLate;
		  var earlyPercent = (100.0 * totalEarly / totalTotal); 
		  var ontimePercent = (100.0 * totalOntime / totalTotal); 
		  var latePercent = (100.0 * totalLate / totalTotal); 
		  var dataArrayForRoute = [
		     		 'Combined',
		     		 latePercent, 
		     		 	'opacity: 1.0', 
		     		 	'Late: ' + totalLate + ' out of ' + totalTotal + ' stops', 
		     		 	latePercent.toFixed(1) + '%',
			     	 ontimePercent, 
			     	 	'opacity: 1.0', 
			     	 	'On time: ' + totalOntime + ' out of ' + totalTotal + ' stops', 
			     	 	ontimePercent.toFixed(1) + '%',
		     		 earlyPercent, 
		     		 	'opacity: 1.0', 
		     		 	'Early: ' + totalEarly + ' out of ' + totalTotal + ' stops', 
		     		 	earlyPercent.toFixed(1) + '%'
		     		 ];
		  dataArray.push(dataArrayForRoute);		  
	  }
	  
	  // Remember how many rows so can set height of chart when it is created
	  globalNumberOfRoutes = jsonData.data.length;
	  
	  globalDataTable = 
		  google.visualization.arrayToDataTable(dataArray);
	  
      $("#loading").fadeOut("fast");

	  drawChart();
  }
  
  function getDataAndDrawChart() {
	  $.ajax({
	  	// The page being requested
	    url: "schAdhByRouteData.jsp",
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