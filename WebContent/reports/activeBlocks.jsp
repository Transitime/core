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

#blocksDiv {
	background: #ccc;
	line-height: 1.0;
	font-size: large;
	padding: 0.0em 0.1em 0.6em;
}

/* Make all the labels similar */
.blockLabel {
	padding-left: 2.0em;
	text-align: right;
	font-size: medium;
	color: graytext;
}

/* Separate each block by a bit of space */
#block {
	padding-top: 0.6em;
}

/* Since using padding to separate each block need to align text in cells to bottom */
#blocksTable td {
	vertical-align: text-bottom;
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
			if (routeElementId == "routeId-" + routes.route[j].id) {
				routeInAjaxData = true;
				break;
			}
		}

		// If route element not in the ajax data then remove it
		if (!routeInAjaxData)
			routesElements[i].remove();
	}
	
	// Get rid of block elements that are not needed anymore
	var blockElements = $("[id|='blockId']"); // Get all elements having id starting with blockId
	for (var i=0; i<blockElements.length; ++i) {
		var blockElementId = blockElements[i].id;
		var blockInAjaxData = false;
		// Go through block ajax data
		for (var j=0; j<routes.route.length; ++j) {
			var routeData = routes.route[j];
			for (var k=0; k<routeData.block.length; ++k) {
				if (blockElementId == "blockId=" + routeData.block[k].id) {
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
			// Note: the outer div with class='group' is needed so user can reorder the routes
 			$("#accordion").append(
 					"<div class='group' id='" + routeElementId + "'>" +
 					 " <h3>" + routeData.name + 
 				     "  <span id='vehicles'></span><span id='vehiclesLabel'>Vehicles:</span>" +
 				     "  <span id='blocks'></span><span id='blocksLabel'>Blocks:</span>" +
					 " </h3>" +
 					 " <div id='blocksDiv'><table id='blocksTable'></table></div>" +
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
		var blocksTable = $("#" + routeElementId + " #blocksTable");
		for (var i=0; i<routeData.block.length; ++i) {
			// If block element doesn't yet exist then create it
			var blockData = routeData.block[i];
			var blockElementId = "blockId-" + blockData.id;
			var blockElement = $("#" + routeElementId + " #" + blockElementId);
			if (blockElement.length == 0) {
				blocksTable.append(
						"<tr id='" + blockElementId + "'>" +
						" <td class='blockLabel'>Block:</td><td id='block'></td>" +
						" <td class='blockLabel'>Start:</td><td id='blockStart'></td>" + 
						" <td class='blockLabel'>End:</td><td id='blockEnd'></td>" + 
						" <td class='blockLabel'>Service:</td><td id='blockService'></td>" +
						"</tr>" +
						"<tr id='" + blockElementId + "'>" +
						" <td class='blockLabel'>Trip:</td><td id='trip'></td>" + 
						" <td class='blockLabel'>Start:</td><td id='tripStart'></td>" + 
						" <td class='blockLabel'>End:</td><td id='tripEnd'></td>" + 
						"</tr>" +
						"<tr id='" + blockElementId + "'>" +
						" <td class='blockLabel'>Vehicle:</td><td id='vehiclesForBlock'></td>" +
						" <td class='blockLabel'>Adh:</td><td id='vehicleSchedAdh'></td>" +
						"</tr>");
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
			
			var tripStartValueElement = $("#" + routeElementId + " #" + blockElementId + " #tripStart");
			tripStartValueElement.text(blockData.trip.startTime);

			var tripEndValueElement = $("#" + routeElementId + " #" + blockElementId + " #tripEnd");
			tripEndValueElement.text(blockData.trip.endTime);
			
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
			var vehiclesValueElement = $("#" + routeElementId + " #" + blockElementId + " #vehiclesForBlock");
			vehiclesValueElement.text(vehiclesValue);
			
			var schAdhStr = "-";
			if (blockData.vehicle.length > 0) {
				schAdhStr = blockData.vehicle[0].schAdhStr;
			}
			var vehiclesSchedAdhElement = $("#" + routeElementId + " #" + blockElementId + " #vehicleSchedAdh");
			vehiclesSchedAdhElement.text(schAdhStr);
			
		}
	}
	
	// Since route widgets might have changed need to call refresh
	$( "#accordion" ).accordion("refresh");	
}

/*
 * Get active block data via AJAX
 */
function getAndProcessData() {
	var urlPrefix = "/api/v1/key/TEST/agency/<%= request.getParameter("a") %>";
	$.getJSON(urlPrefix + "/command/activeBlocksByRoute", handleAjaxData)
		.fail(function() {
	 		console.log( "Could not access /command/activeBlocksByRoute" );
	 	});	
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
	
	// Start getting the active blocks data and processing it.
	// Update every 30 seconds.
	getAndProcessData();
	setInterval(getAndProcessData, 30000);
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