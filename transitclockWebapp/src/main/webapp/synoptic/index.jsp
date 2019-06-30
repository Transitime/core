<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<!-- 
 Query String parameters:
   a=AGENCY (required)
   r=ROUTE (optional, if not specified then a route selector is created)
   s=STOP_ID (optional, for specifying which stop interested in)
   tripPattern=TRIP_PATTERN (optional, for specifying which stop interested in).
   verbose=true (optional, for getting additional info in vehicle popup window)
   showUnassignedVehicles=true (optional, for showing unassigned vehicles)
   updateRate=MSEC (optional, for specifying update rate for vehicle locations.
-->
<html>
<head>
  <!-- So that get proper sized map on iOS mobile device -->
   <%@include file="/template/includes.jsp" %>
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
 <link rel="stylesheet" href="http://cdn.leafletjs.com/leaflet-0.7.3/leaflet.css" />
  <script src="http://cdn.leafletjs.com/leaflet-0.7.3/leaflet.js"></script>
  <script src="javascript/leafletRotatedMarker.js"></script>
  <script src="javascript/mapUiOptions.js"></script>
  
  <script src="<%= request.getContextPath() %>/javascript/jquery-dateFormat.min.js"></script>
<script src="<%= request.getContextPath() %>/synoptic/javascript/synoptic.js"></script>

  <link rel="stylesheet" href="css/mapUi.css" />
  <link rel="stylesheet" href="<%= request.getContextPath() %>/synoptic/css/synoptic.css">	
  
  <!-- Load in Select2 files so can create fancy selectors -->
  <link href="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/css/select2.min.css" rel="stylesheet" />
  <script src="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/js/select2.min.js"></script>
 

  <!--  Override the body style from the includes.jsp/general.css files -->
  <style>
    body {
	  margin: 0px;
    }
  </style>
  
  <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
  
  <title>TheTransitClock Synoptic</title>
</head>

<body>
  
  <div id="routesContainer">
    <div id="routesDiv">
      <select id="routes" style="width:380px"></select>	
      <input type="hidden" id="routes" style="width:380px" />
    </div>
  </div>
  <!-- For testing purpose 
  <input type="button" onclick="myFunction()" value="test"/> 
  -->
  <div id="synoptic"
		style="width: 100%; height: 480px; left: 0px; top: 50px; position: absolute;' "></div>
</body>
	<script type="text/javascript">

	var agencyTimezoneOffset;
	function dateFormat(time) {
		var localTimezoneOffset = (new Date()).getTimezoneOffset();
		var timezoneDiffMinutes = localTimezoneOffset - agencyTimezoneOffset;
		
		var offsetDate = new Date(parseInt(time)*1000 + timezoneDiffMinutes*60*1000);
		// Use jquery-dateFormat javascript library
		return $.format.date(offsetDate, 'HH:mm:ss');
	}


	$.getJSON(apiUrlPrefix + "/command/agencyGroup", 
			function(agencies) {
				// If agency not defined, such as when just testing AVL feed,
				// then set map to United States
				if (agencies.agency.length == 0) {
					return;
				}
				
		        agencyTimezoneOffset = agencies.agency[0].timezoneOffsetMinutes;
				
		        // Fit the map initially to the agency, but only if route not
		        // specified in query string. If route specified in query string
		        // then the map will be fit to that route once it is loaded in.
		      
		});	

	var shortNameParam;
	function getShortNameParam() {
		return routeQueryStrParam;
	}

	function setShortNameParam(param) {
		shortNameParam = param;
	}

	
	var routeQueryStrParam;

	function getRouteQueryStrParam() {
		return routeQueryStrParam;
	}

	function setRouteQueryStrParam(param) {
		routeQueryStrParam = param;
	}

	var shapeMapLength;

	function getShapeLength(tripPatternId) {
		return shapeMapLength[tripPatternId];
	}

	function setShapeMapLenght(param) {
		shapeMapLength = param;
	}
	var imgVehicleOnSechedule=new Image();
	var imgVehicleNotOnSechedule=new Image();
	this.imgVehicleOnSechedule.src="mybus.png";
	this.imgVehicleOnSechedule.width=30;
	this.imgVehicleOnSechedule.height=30;
	this.imgVehicleNotOnSechedule.src="mybusRed.png";
	this.imgVehicleNotOnSechedule.width=30;
	this.imgVehicleNotOnSechedule.height=30;
	
	/** 
	* Returns different image according to some conditions
	*
	**/
	function vehicleAlternColorCallBack(vehicle)
	{
		if(Math.abs(vehicle.schAdh)<120000)//ON SCHEDULE
			return imgVehicleOnSechedule;
		else
			return imgVehicleNotOnSechedule;
	}
	
function testFunc()
{}


function predictionCallback(preds, status) {
	// If predictions popup was closed then don't do anything
	
	
	// There will be predictions for just a single route/stop
	var routeStopPreds = preds.predictions[0];
	
	// Set timeout to update predictions again in few seconds
	//predictionsTimeout = setTimeout(getPredictionsJson, 20000, routeStopPreds.routeShortName, routeStopPreds.stopId);

	// Add route and stop info
	var stopName = routeStopPreds.stopName;
	if (routeStopPreds.stopCode)
		stopName += " (" + routeStopPreds.stopCode + ")";
	var content = '<table class="tooltipTable">' 
		+ '<tr><td><b>Stop:</b> ' + stopName + '</td></tr>';
	//if (verbose)
	//	content += '<b>Stop Id:</b> ' + routeStopPreds.stopId + '<br/>';
		
	// For each destination add predictions
	for (var i in routeStopPreds.dest) {
		// If there are several destinations then add a horizontal rule
		// to break predictions up by destination
		if (routeStopPreds.dest.length > 1)
			content += '<tr><td>############</td></tr>';
		
		// Add the destination/headsign info
		if (routeStopPreds.dest[i].headsign)
			content += '<tr><td><b>Destination:</b> ' + routeStopPreds.dest[i].headsign + '</td></tr>';
		content +='<tr><td>';
		// Add each prediction for the current destination
		
		if (routeStopPreds.dest[i].pred.length > 0) {
			content += '<table  class="tooltipTable" style="border-spacing: 0px;border-collapse: collapse;">';
			content += '<tr class="tooltipTable" style="font-weight:bold;"><td style="border: 1px solid">trip</td>';
			content += '<td style="border: 1px solid">Vehicle</td>';
			content += '<td style="border: 1px solid">Minutes</td>';
			for (var j in routeStopPreds.dest[i].pred) {
				// Separators between the predictions
				
					
				// Add the actual prediction
				var pred = routeStopPreds.dest[i].pred[j];
				content+= '<tr ><td style="border: 1px solid">'+  pred.trip + "</td>";
				var ident=synoptic.getVehicleIdentifier(pred.vehicle );
				if(ident==null)
					ident=pred.vehicle;
				content+= '<td style="border: 1px solid">'+ ident + "</td>";
				content+= '<td style="border: 1px solid">'+  pred.min + "</td></tr>";
				
				
				// Added any special indipredcators for if schedule based,
				// delayed, or not yet departed from terminal
				/*
				if (pred.scheduleBased)
					content += '<sup>sched</sup>';
				else {
					if (pred.notYetDeparted)
						content += '<sup>not yet left</sup>';
					else
						if (pred.delayed) 
							content += '<sup>delayed</sup>';
				}
				*/
				// If in verbose mode add vehicle info
				//if (verbose)
			}
			
			content += '</table>';
			
		} else {
			// There are no predictions so let user know
			content += "No predictions";
		}
		content +='</td></tr>';
	}
	
	// Now update popup with the wonderful prediction info
	synoptic.setPredictionsContent(content);
}

function getPredictionsJson( stopId) {
	
	// JSON request of predicton data
	var url = apiUrlPrefix + "/command/predictions?rs=" + getShortNameParam()
			+ encodeURIComponent("|") + stopId;
	$.getJSON(url, predictionCallback);	
}

function vehicleUpdate(vehicleDetail, status)
{
	var buses=[];
	for(var i in  vehicleDetail.vehicles)
	{
		console.log("i: "+i);
		vehicle=vehicleDetail.vehicles[i];
		console.log(vehicle);

		//if(vehicle.direction==undefined)
		//	vehicle.direction=="0";
		
		var directionVehicle=(vehicle.direction=="0" || vehicle.direction==undefined)?0:1;
		var _identifier=(vehicle.licensePlate==undefined)?vehicle.id:vehicle.licensePlate;
		var gpsTimeStr = dateFormat(vehicle.loc.time);
		buses.push({id:vehicle.id, projection:vehicle.distanceAlongTrip/getShapeLength(vehicle.tripPattern),identifier:_identifier,direction:directionVehicle,gpsTimeStr:gpsTimeStr,nextStopName:vehicle.nextStopName,schAdhStr:vehicle.schAdhStr,trip:vehicle.trip,schAdh:vehicle.schAdh,headway:vehicle.headway});

	}
	synoptic.setBuses(buses);
	synoptic.steps=100;
	synoptic.counter=0;
	//window.requestAnimationFrame(test.animateBus);
	synoptic.animateBus(20);
	console.log(vehicleDetail);
}
function startTimerUpdate()
{
	console.log("startTimerUpdate");
	var url = apiUrlPrefix + "/command/vehiclesDetails?r=" + getRouteQueryStrParam();
		$.getJSON(url, vehicleUpdate);
	setTimeout(startTimerUpdate, 30000);
}

function routeConfigCallback(routeDetail, status)
{
	console.log(routeDetail);
	console.log(status);
	//FOR EACH DIRECTION
	var stops=[];
	var buses=[];
	var showReturn=false;
	var shapeMap = {};
	setShortNameParam(routeDetail.routes[0].shortName);
	for(var i  in routeDetail.routes[0].direction)
	{
		//console.log(routeDetail.routes[0].direction[i]);
		console.log("Direction "+routeDetail.routes[0].direction[i].id);
		console.log(routeDetail.routes[0].direction[i]);
		if(routeDetail.routes[0].direction[i]==undefined)
			routeDetail.routes[0].direction[i]="0";
		var distanceOverPath=0.0;

		var routeLenght=-1;
		console.log(routeDetail.routes[0].shape );
		for(	var k in routeDetail.routes[0].shape)
		{
			var shape=routeDetail.routes[0].shape[k];
			console.log("K "+k );
			if(routeDetail.routes[0].direction[i].id!=shape.directionId)
				continue;
			
			shapeMap[shape.tripPattern]=shape.length;
			if(shape.length>routeLenght)
				routeLenght=shape.length;
		}
		
		for(var j in routeDetail.routes[0].direction[i].stop)
		{
			
			var stop=routeDetail.routes[0].direction[i].stop[j];
			distanceOverPath+=stop.pathLength;
			var projectionStop=distanceOverPath/routeLenght;
			if(stop.direction==undefined)
				stop.direction="0";
			var directionStop=(stop.direction==routeDetail.routes[0].direction[i].id=="0")?0:1;
			stops.push({id: stop.id, identifier: stop.name,projection:projectionStop,direction:directionStop,distance:distanceOverPath});
		}
		if(i>0)
			showReturn=true;
		
		console.log(stops);
		
	}
	setShapeMapLenght(shapeMap);
	synoptic=null;
	
	var canvas = document.getElementById("synoptic");
	while (canvas.firstChild) {
		canvas.removeChild(canvas.firstChild);
	}
	var params={container:canvas,
			onVehiClick:testFunc,
			infoStop:function(data) {console.log(data.identifier);return "<table class=\"table\"><th >"+data.identifier+"</th><tr><td>distance: "+parseFloat(data.distance).toFixed(2) +" m. </td></tr></table>"},
			//var startTimeStr = vehicleData.isScheduledService ? "" : "<br/><b>Start Time:</b> "+dateFormat(vehicleData.freqStartTime/1000)
			infoVehicle:function(data) {console.log(data.identifier);return "<table class=\"table\"><th >"+data.identifier+"</th><tr><td>GPSTime: "+data.gpsTimeStr +" </td></tr><tr><td>NextStop: "+data.nextStopName+"</td></tr><tr><td>schAdh: "+data.schAdhStr+"</td></tr><tr><td>trip: "+data.trip+((data.isScheduledService!=undefined && data.isScheduledService==false)?("</td></tr><tr><td>Start Time: "+dateFormat(data.freqStartTime/1000)):"")+"</td></tr><tr><td>headway: "+((data.headway==-1)?"-":((data.headway/60000).toFixed(2))+ " min.") +"</td><tr></table>"},
			routeName:routeDetail.routes[0].name,
			patternType:routeDetail.routes[0].shape[0].patternType,
			drawReturnUpside:false,
			showReturn:showReturn,
			predictionFunction:getPredictionsJson,
			vehicleAlternColorCallBack:vehicleAlternColorCallBack
	}
	
	synoptic=new Sinoptico(params);
	synoptic.setStops(stops);
	synoptic.setBuses(buses);
	synoptic.init();
	synoptic.paintBus();
	console.log(routeDetail.routes[0].id);
	startTimerUpdate();
}
	
	console.log("INIT "+apiUrlPrefix);
	$.getJSON(apiUrlPrefix + "/command/routes", 
	 		function(routes) {
				console.log("JSON "+routes);
				console.log(routes);
		        // Generate list of routes for the selector
		 		//var selectorData = [];
		 		var selectorData = [{id: '', text: 'Select Route'}];
		 		for (var i in routes.routes) {
		 			console.log("VAR i"+i);
		 			var route = routes.routes[i];
		 			selectorData.push({id: route.id, text: route.name})
		 		}
		 		console.log(selectorData);
		 		// Configure the selector to be a select2 one that has
		 		// search capability
		 		$("#routes").select2({
 				placeholder: "Select Route", 				
 				data : selectorData})
 				.on("select2:select", function(e) {
 	 				
 					// First remove all old vehicles so that they don't
 					// get moved around when zooming to new route
 					//removeAllVehicles();
 					// Configure map for new route	
 					console.log("SELECTED "+e.params.data.id);
 					var selectedRouteId = e.params.data.id;
 					var url = apiUrlPrefix + "/command/routesDetails?r=" + selectedRouteId;
 					$.getJSON(url, routeConfigCallback);
 					setRouteQueryStrParam(selectedRouteId)
 					// Reset the polling rate back down to minimum value since selecting new route
 					//avlPollingRate = MIN_AVL_POLLING_RATE;
 					//if (avlTimer)
 					//	clearTimeout(avlTimer);
 					
 					// Read in vehicle locations now
 					//setRouteQueryStrParam("r=" + selectedRouteId); 					
 					//updateVehiclesUsingApiData();
 					
 		 			// Disable tooltips. For some reason get an unwanted 
 		 			// tooltip consisting of the current select once a selection
 		 			// has been made. It is really distracting. So have to do
 		 			// this convoluted thing after every selection in order to
 		 			// make sure this annoying tooltip doesn't popup.
 		 			$( "#select2-routes-container" ).tooltip({ content: 'foo' });
 		 			$( "#select2-routes-container" ).tooltip("option", "disabled", true);
				});
 				
	 			
	 		
	 	});	 
	
	</script>
