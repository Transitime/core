<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
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
  <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script> 

  <!-- Load in JQuery UI javascript and css to set general look and feel -->
  <script src="/api/jquery-ui/jquery-ui.js"></script>
  <link rel="stylesheet" href="/api/jquery-ui/jquery-ui.css">
  
  <!-- So can fit long names into div -->
  <script src="/api/javascript/jquery.quickfit.js"></script>
<style>
#accordion {
  margin-left: 20px;
  margin-right: 20px;
  line-height: 1.0; /* Reduce vertical height from 1.3 so can fit more on page */
}

/* For display number of vehicles for route */
#vehicles {
	float: right; 
	padding-left: 6px;
	margin-right: 50px;
	width: 2em; 
	font-size: 18px;
	/* background-color: #EDABC6; */
}

/* For display number of vehicles for route */
#vehiclesLabel {
	float: right; 
	text-align: right; 
	width: 100px; 
	font-size: 18px;
}

/* For displaying number of blocks for route */
#blocks {
	float: right; 
	padding-left: 6px;
	margin-right: 50px;
	width: 2em; 
	font-size: 18px;
}

/* For displaying number of blocks for route */
#blocksLabel {
	float: right; 
	text-align: right; 
	width: 70px; 
	font-size: 18px;
}

#blocksInfo {
	background: #ccc;
	line-height: 1.0;
	font-size: large;
	padding: 0.2em 2.2em;
}

/* Put spacing between block listings */	
.blockInfo:not(:first-child) {
	margin-top:12px;
}

/* So columns after block & trip can line up */
#block, #trip {
	display: inline-block;
	width: 70px;
	/* background: yellow; */
}

/* For all the labels in the block display */
/*
.blockLabel {
	display: inline-block;
	width: 60px;
	text-align: right;
	padding-right: 0.2em;
	font-size: medium;
	color: graytext;
}
*/

/* Want the first column to line up */
#blockLabel, #tripLabel, #vehiclesForBlockLabel {
	display: inline-block;
	width: 60px;
	text-align: right;
	padding-left: 0em;
}

/* Make all the labels similar */
.blockLabel {
	padding-left: 1.5em;
	padding-right: 0.2em;
	font-size: medium;
	color: graytext;
	/* background: cyan; */
}

#blockServiceLabel {
	width: 75px;
}
</style>

<script>
function removeUnneededBlockAndRouteElements(routes) {
	// First get rid of route elements that are not needed anymore because they
	// are not in the ajax data.
	var routesElements = $("[id|='routeId']");
	for (var i=0; i<routesElements.length; ++i) {
		var routeElementId = routesElements[i].id;
		var routeInAjaxData = false;
		// Go through ajax route data
		for (var j=0; j<routes.route.length; ++j) {
			if (routeElementId == routes.route[j].id) {
				routeInAjaxData = true;
				break;
			}
		}

		// If route element not in the ajax data then remove it
		if (!routeInAjaxData)
			routesElements[i].remove();
	}
	
	// Get rid of block elements that are not needed anymore
	var blockElements = $("[id|='blockId']");
	for (var i=0; i<blockElements.length; ++i) {
		var blockElementId = blockElements[i].id;
		var blockInAjaxData = false;
		// Go through block ajax data
		for (var j=0; j<routes.route.length; ++j) {
			var routeData = routes.route[j];
			for (var k=0; k<routeData.block.length; ++k) {
				if (blockElementId == routeData.block[k].id) {
					blockInAjaxData = true;
					break;
				}
			}
			if (blockInAjaxData)
				break;
		}

		// If block element not in the ajax data then remove it
		if (!blockInAjaxData)
			blockElements[i].remove();
	}
}

function handleAjaxData(routes) {
	// Remove blocks and routes that are not in the ajax data
	removeUnneededBlockAndRouteElements(routes);

	// Now add a route element if it is in ajax data but element doesn't exist yet
	for (var j=0; j<routes.route.length; ++j) {
		var routeData = routes.route[j];
				
		// If route element doesn't yet exist for this route then create it
		var routeElementId = "routeId-" + routeData.id;
		var routeElement = $("#" + routeElementId);
		if (routeElement.length == 0) {
 			$("#accordion").append(
 					"<div class='group' id='" + routeElementId + "'>" +
 					 "<h3>" + routeData.name + 
 				     "<span id='vehicles'></span><span id='vehiclesLabel'>Vehicles:</span>" +
 				     "<span id='blocks'></span><span id='blocksLabel'>Blocks:</span>" +
					 "</h3>" +
 					 "<div id='blocksInfo'></div>" +
 					 "</div>");	
 		}
		
		// Update the route info by setting number of blocks
		var blocksValueElement = $("#" + routeElementId + " #blocks");
		blocksValueElement.text(routeData.block.length);

		// Update the route info by setting number of vehicles
		var vehiclesValueElement = $("#" + routeElementId + " #vehicles");
		var numberOfVehicles = 0;
		for (var k=0; k<routeData.block.length; ++k) {
			var blockData = routeData.block[k];
			numberOfVehicles += blockData.vehicle.length;
		}
		vehiclesValueElement.text(numberOfVehicles);

		// Update all the block information for this route
		var blocksInfoDiv = $("#" + routeElementId + " #blocksInfo");
		for (var i=0; i<routeData.block.length; ++i) {
			// If block element doesn't yet exist then create it
			var blockData = routeData.block[i];
			var blockElementId = "blockId-" + blockData.id;
			var blockElement = $("#" + routeElementId + " #" + blockElementId);
			if (blockElement.length == 0) {
				blocksInfoDiv.append(
						"<div id='" + blockElementId + "' class='blockInfo'>" + 
						"<span id='blockLabel' class='blockLabel'>Block:</span><span id='block'></span>" + 
						"<span id='blockStartLabel' class='blockLabel'>Start:</span><span id='blockStart'></span>" + 
						"<span id='blockEndLabel' class='blockLabel'>End:</span><span id='blockEnd'></span>" + 
						"<span id='blockServiceLabel' class='blockLabel'>Service:</span><span id='blockService'></span>" +
						"<br/>" +
						"<span id='tripLabel' class='blockLabel'>Trip:</span><span id='trip'></span>" + 
						"<span id='tripStartLabel' class='blockLabel'>Start:</span><span id='tripStart'></span>" + 
						"<span id='tripEndLabel' class='blockLabel'>End:</span><span id='tripEnd'></span>" + 
						"<br/>" +
						"<span id='vehiclesForBlockLabel' class='blockLabel'>Vehicle:</span><span id='vehiclesForBlock'></span>" +
						"<span id='vehiclesSchedAdhLabel' class='blockLabel'>Adh:</span><span id='vehicleSchedAdh'></span>" +
						"</div>");
			}
			
			// Update the information for the block 
			var blockValueElement = $("#" + routeElementId + " #" + blockElementId + " #block");
			blockValueElement.text(blockData.id);
			
			var blockStartValueElement = $("#" + routeElementId + " #" + blockElementId + " #blockStart");
			blockStartValueElement.text(blockData.startTime);
			
			var blockEndValueElement = $("#" + routeElementId + " #" + blockElementId + " #blockEnd");
			blockEndValueElement.text(blockData.endTime);
			
			var blockServiceValueElement = $("#" + routeElementId + " #" + blockElementId + " #blockService");
			blockServiceValueElement.text(blockData.serviceId);
			
			var tripValueElement = $("#" + routeElementId + " #" + blockElementId + " #trip");
			var tripId = blockData.trip.shortName != null ? 
					blockData.trip.shortName : blockData.trip.id;
			tripValueElement.text(tripId);
	
			// Sometimes trip names are long. For this situation need to not
			// have the values in first column overlap with the next labels.
			// For this situation need to make the elements wider.
			if (tripId.length > 10) {
				tripValueElement.css("width", "140px");
				blockValueElement.css("width", "140px");
				
				// And reduce trip ID font so that it fits.
				// Max size of 18px seems to correspond to "large" font size.
				tripValueElement.quickfit({max: 18, min: 10});
			}
		
			var tripStartValueElement = $("#" + routeElementId + " #" + blockElementId + " #tripStart");
			tripStartValueElement.text(blockData.trip.startTime);

			var tripEndValueElement = $("#" + routeElementId + " #" + blockElementId + " #tripEnd");
			tripEndValueElement.text(blockData.trip.endTime);
			
			var vehiclesValueElement = $("#" + routeElementId + " #" + blockElementId + " #vehiclesForBlock");
			var vehiclesValue = "none";
			if (blockData.vehicle.length != 0) {
				vehiclesValue = "";
				for (var v=0; v<blockData.vehicle.length; ++v) {
					var vehicleData = blockData.vehicle[v];
					if (v > 0)
						vehiclesValue += ", ";
					vehiclesValue += vehicleData.id;
					
				}
			}
			vehiclesValueElement.text(vehiclesValue);
		}
	}
	
	// Since route widgets might have changed need to call refresh
	$( "#accordion" ).accordion("refresh");	
}

// Called when page is ready
$(function() {
	// Make the data a JQuery UI accordion that is sortable
	$( "#accordion" ).accordion({
			collapsible: true,     // So can hide details for all routes
			active: false,         // Don't have any panels open at startup
			animate: 200,
			heightStyle: "content", // So each blocks info element can be different size 
			header: "> div > h3"}) // So can be sortable
		.sortable({
			axis: "y",
			handle: "h3",
			stop: function( event, ui ) {
			// IE doesn't register the blur when sorting
			// so trigger focusout handlers to remove .ui-state-focus
			ui.item.children( "h3" ).triggerHandler( "focusout" );
			// Refresh accordion to handle new order
			$( this ).accordion( "refresh" );
			}
		});
	
	// Get active block data via AJAX
	var urlPrefix = "/api/v1/key/TEST/agency/<%= request.getParameter("a") %>";
	$.getJSON(urlPrefix + "/command/activeBlocksByRoute", handleAjaxData)
		.fail(function() {
	 		console.log( "Could not access /command/activeBlocksByRoute" );
	 	});

});


</script>

  <link rel="stylesheet" href="/api/css/general.css">
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Active Blocks</title>
</head>
<body>
<%@include file="/template/header.jsp" %>

<div id="title">Active Blocks</div>
<div id="accordion"></div>

</body>
</html>