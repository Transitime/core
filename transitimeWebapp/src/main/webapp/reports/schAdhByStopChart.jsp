<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="org.transitime.utils.web.WebUtils" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <style>
    .chart_div {
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
  
  <div id="title"></div>
  <div id="subtitle"></div>
  <div class="chart_div" id="chart_direction_div_0"></div>
  <div class="chart_div" id="chart_direction_div_1"></div>
  <div id="loading"></div>
  <div id="errorMessage"></div>
</body>

<script type="text/javascript">


  function drawChart(directionId, divId, dataTable, numberOfStops) {
      // Need to set height of the chart div 
	  var vertSpaceForTitleAndHAxis = 100;
	  var vertSpaceForChart = (numberOfStops + 1) * 22;
	  var chartDivHeight = vertSpaceForTitleAndHAxis + vertSpaceForChart;
      $("#" + divId).height(chartDivHeight);
      
        var options = {
          title: 'Direction: ' + directionId, 
          titleTextStyle: {
        	 fontSize: 22,
        	 bold: false
          },
          animation: {
        	 startup: true,
        	 duration: 500, 
        	 easing: 'out'
          },
          bars: 'horizontal',
          isStacked: 'percent',
          
          // Don't want lots of space between bars
          bar: { groupWidth: '98%' },
          
          // Make chart area a fixed offset so won't have default behavior of
          // making offset really big if the chart is really tall.
          // Use small height than default to make sure units show for hAxis.
          // Use smaller width than default so that have room for route
          // names and the legend on the right
          chartArea: {top: 60, height: vertSpaceForChart, width: '60%'},
          
          // Need to set font size for things since default is to use font 
          // size proportional to height of chart and since could show many
          // routes can end up with silly large fonts.
          annotations: {textStyle: {fontSize: 12}},
          tooltip: {textStyle: {fontSize: 16}},
          vAxis: {textStyle: {fontSize: 14}},
          hAxis: {textStyle: {fontSize: 14}},
          legend: {textStyle: {fontSize: 16}, position: 'right'},
          
          // Use proper colors for late, ontime, and early
          series: [{'color': '#F0DB56'}, {'color': '#6FD656'}, {'color': '#E84D5F'}],
        };

        // Use old style charts instead of the Material ones because animation
        // only seems to work with old style ones.
        var chart = new google.visualization.BarChart(document.getElementById(divId));
        chart.draw(dataTable, options);        
  }
  
  // Each chartInfo has numberOfStops and dataTable members
  var globalChartInfos = [];
  
  function drawCharts() {
	  for (var i=0; i<globalChartInfos.length; ++i) {
		  var chartInfo = globalChartInfos[i];
		  drawChart(chartInfo.directionId, chartInfo.divId, chartInfo.dataTable, chartInfo.numberOfStops)
	  }
  }
  
  function createDataTableAndDrawChartForDirection(stopsData) {
	  // Initialize dataArray with the column info
	  var dataArray = [[
	                  'Stop', 
	                  'Late',    {role: 'style'}, { role: 'tooltip'}, { role: 'annotation'},
	                  'On Time', {role: 'style'}, { role: 'tooltip'}, { role: 'annotation'},
	                  'Early',   {role: 'style'}, { role: 'tooltip'}, { role: 'annotation'}
	                  ]];
	  
	  // Add data for each route to the dataArray
	  var totalEarly = 0;
	  var totalOntime = 0;
	  var totalLate = 0;
	  for (var i=0; i<stopsData.length; ++i) {
		  var stop = stopsData[i];
		  var earlyPercent = (100.0 * stop.early / stop.total); 
		  var ontimePercent = (100.0 * stop.ontime / stop.total); 
		  var latePercent = (100.0 * stop.late / stop.total); 
		  var dataArrayForStop = [
		             stop.stop_name + ' ', 
		             latePercent, 
		             	'opacity: 1.0', 
		             	'Late: ' + stop.late + ' out of ' + stop.total + ' times', 
		             	stop.late > 0 ? latePercent.toFixed(1) + '%' : '',
		             ontimePercent, 
		             	'opacity: 1.0', 
		             	'On time: ' + stop.ontime + ' out of ' + stop.total + ' times', 
		             	stop.ontime > 0 ? ontimePercent.toFixed(1) + '%' : '',
			         earlyPercent, 
		             	'opacity: 1.0', 
		             	'Early: ' + stop.early + ' out of ' + stop.total + ' times', 
		             	(stop.early > 0) ? earlyPercent.toFixed(1) + '%' : ''
		             ];
		  dataArray.push(dataArrayForStop);
		  
		  totalEarly += stop.early;
		  totalOntime += stop.ontime;
		  totalLate += stop.late;
	  }
	  
	  // Add totals row to output
	  var totalTotal = totalEarly + totalOntime + totalLate;
	  var earlyPercent = (100.0 * totalEarly / totalTotal); 
	  var ontimePercent = (100.0 * totalOntime / totalTotal); 
	  var latePercent = (100.0 * totalLate / totalTotal); 
	  var dataArrayForStop = [
	     		 'Total: ',
	     		 latePercent, 
	     		 	'opacity: 1.0', 
	     		 	'Late: ' + totalLate + ' out of ' + totalTotal + ' times', 
	     		 	latePercent.toFixed(1) + '%',
		     	 ontimePercent, 
		     	 	'opacity: 1.0', 
		     	 	'On time: ' + totalOntime + ' out of ' + totalTotal + ' times', 
		     	 	ontimePercent.toFixed(1) + '%',
	     		 earlyPercent, 
	     		 	'opacity: 1.0', 
	     		 	'Early: ' + totalEarly + ' out of ' + totalTotal + ' times', 
	     		 	earlyPercent.toFixed(1) + '%'
	     		 ];
	  dataArray.push(dataArrayForStop);		  
	  
	  // Need to store info for each chart so that they can be redrawn when resized
	  var chartInfo = {
	      numberOfStops: stopsData.length,
	      dataTable: google.visualization.arrayToDataTable(dataArray),
	      directionId: stopsData[0].direction_id,
	      divId: "chart_direction_div_" + stopsData[0].direction_id
	  };	  
	  globalChartInfos.push(chartInfo)
  }

  function createDataTablesAndDrawCharts(jsonData) {
	  $("#loading").fadeOut("fast");	  

	  // Determine data for each direction 
	  var directionsData = [];
	  var previousDirection = "";
	  var directionData = [];
	  for (var i=0; i<jsonData.data.length; ++i) {
		  var stop = jsonData.data[i];

		  if (stop.direction_id != previousDirection) {
			  directionData = [];
			  directionsData.push(directionData);
			  previousDirection = stop.direction_id;
		  }
		  
		  directionData.push(stop);
	  }
	  
	  for (var i=0; i<directionsData.length; ++i) {
		  createDataTableAndDrawChartForDirection(directionsData[i]);
	  }
	  
	  drawCharts();
  }
  
  /**
   * Determines and sets title using data from routes AJAX API scall 
   */
  function determineChartTitle(routeData) {
	var agencyName = routeData.agency;
	var routeName = routeData.routes[0].name;
		
	  <%
	  String allowableEarly = request.getParameter("allowableEarly");;
	  String allowableLate = request.getParameter("allowableLate");;
	  String chartParams = 
	    allowableEarly + " min early to " + allowableLate + " min late</br>" 
		+ request.getParameter("dateRange");
	  
	  String beginTime = request.getParameter("beginTime");
	  String endTime = request.getParameter("endTime");
	  if (!beginTime.isEmpty() || !endTime.isEmpty()) {
		  if (beginTime.isEmpty())
			  beginTime = "00:00"; // default value
		  if (endTime.isEmpty())
			  endTime = "24:00";   // default value
		  chartParams += ", " + beginTime + " to " + endTime;
	  }
	%>
	  
	$("#title").html('Schedule Adherence for ' + routeName);
	$("#subtitle").html('<%= chartParams %>');
  }
  
  function getDataAndDrawCharts() {
	  // Get agency and route name for titles
	  $.getJSON(apiUrlPrefix + "/command/routes?r=<%= request.getParameter("r") %>", 
			  determineChartTitle);	
	  
	  // Get the data for the report
	  $.ajax({
	  	// The page being requested
	    url: "schAdhByStopData.jsp",
		// Pass in query string parameters to page being requested
		data: {<%= WebUtils.getAjaxDataString(request) %>},
	 	// Needed so that parameters passed properly to page being requested
	 	traditional: true,
	    // When successful read in data into the JSON table used by the chart
	    success: createDataTablesAndDrawCharts,
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
  google.setOnLoadCallback(getDataAndDrawCharts);

  // Updates chart when page is resized. But only does so at most
  // every 300 msec so that don't bog system down trying to repeatedly
  // update the chart.
  var globalTimer;
  window.onresize = function () {
            clearTimeout(globalTimer);
            globalTimer = setTimeout(drawCharts, 300)
          };

</script>

</html>