<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Arrival Measurer</title>

<!-- So that get proper sized map on iOS mobile device -->
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
<%@include file="/template/includes.jsp" %>

<style type="text/css">
  body {
  	margin-left: 5px; 
  	font-size: medium;
  }
  
  .route { margin-top: 14px; }
  
  .stop { font-size: xx-small;} 
</style>

<script>

/**
 * Writes the arrival data to website which stores it in db
 */
function sendVehicleArrivedEventToWebsite(agencyId, routeId, routeShortName, routeName, stopId, stopName, directionId, headsign) {
	var url = apiUrlPrefixAllAgencies 
				+ "/agency/" + agencyId + "/command/pushMeasuredArrivalTime?" 
				+ "r=" + routeId 
				+ "&rShortName=" + routeShortName 
				+ "&s=" + stopId 
				+ "&d=" + directionId 
				+ "&headsign=" + headsign; 
	$.getJSON(url);	
	$('#' + routeId + stopId).css('background-color', '#f99');
	setTimeout(function() {$('#' + routeId + stopId).css('background-color', 'white')}, 1000);
}

/**
 * Needed for variable closure so that the variables have the proper value
 * when sendVehicleArrivedEventToWebsite() is called.
 */
function sendVehicleArrivedEventToWebsiteHandler(agencyId, routeId, routeShortName, 
		routeName, stopId, stopName, directionId, headsign) {
	return function() {
		sendVehicleArrivedEventToWebsite(agencyId, routeId, routeShortName, 
				routeName, stopId, stopName, directionId, headsign)
	}
}

/**
 * Called when preds read from API. Creates Arrival button for each route nearby
 */
function predictionsReadCallback(predictionData) {
	var agencyId = predictionData.agencies[0].agencyId;
	var preds = predictionData.agencies[0].predictions;
	for (var i=0; i<preds.length; ++i) {
		// Get the params for the route/direction
		var routeName = preds[i].routeName;
		var routeId = preds[i].routeId;
		var routeShortName = preds[i].routeShortName;
		var stopName = preds[i].stopName;
		var stopId = preds[i].stopId;
		
		var dest = preds[i].dest[0];
		var headsign = preds[i].dest[0].headsign;
		var directionId = preds[i].dest[0].dir;
		var prediction = preds[i].dest[0].pred[0];
		var sec = prediction.sec;
		
		// Create the labels and button for sending arrived event
		jQuery('<div/>', {
			"class": 'route',
			id: routeId + stopId,
			text: routeName
		}).appendTo('body');
		jQuery('<div/>', {
			"class": 'headsign',
			text: 'To ' + headsign
		}).appendTo('body');
		jQuery('<div/>', {
			"class": 'stop',
			text: stopName
		}).appendTo('body');
		jQuery('<button/>', {
			text: 'Arrived',
			click: sendVehicleArrivedEventToWebsiteHandler(agencyId, routeId, routeShortName, 
						routeName, stopId, stopName, directionId, headsign)
		}).appendTo('body');
	}
}

/**
 * Called when user location determined. Gets nearby stops from API.
 */
function positionCallback(pos) {
	var url = apiUrlPrefixAllAgencies 
				+ "/command/predictionsByLoc?lat=" 
				+ pos.coords.latitude + "&lon=" + pos.coords.longitude 
				+ "&maxDistance=200.0&numPreds=1";
    $.getJSON(url, predictionsReadCallback);	
}

// Start things off by getting user position
navigator.geolocation.getCurrentPosition(positionCallback);
</script>
</head>
<body>

</body>
</html>