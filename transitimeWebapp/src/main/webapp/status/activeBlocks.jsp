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
  <%@include file="/template/includes.jsp" %>

<style>
#accordion {
  margin-left: 20px;
  margin-right: 20px;
  line-height: 1.0; /* Reduce vertical height from 1.3 so can fit more on page */
}

.routeLabel, .routeValue {
	float: right; 
	font-size: 18px;
}

.routeLabel {
	color: #999;
	padding-left: 10px;
}

.routeValue {
	padding-left: 3px;
	padding-right: 3px;
	width: 1.2em; /* Trying to make coloring of early/late vehicles look right */
	text-align: right;
}

/* Separate Blocks: & Vehicles: from the schedule adherence labels */
#routeVehicles {
	margin-right: 50px;
}

#blocksDiv {
	background: #ccc;
	line-height: 1.0;
	font-size: large;
	padding: 0.0em 0.1em 0.6em;
}

/* Make all the labels in the detailed block info window similar */
.blockLabel {
	padding-left: 2.0em;
	text-align: right;
	font-size: medium;
	color: #999;
}

.blockValueSmallerFont {
	font-size: 14px;
}

/* Separate each block by a bit of space */
#block {
	padding-top: 0.6em;
}

/* Since using padding to separate each block need to align text in cells to bottom */
#blocksTable td {
	vertical-align: text-bottom;
}

#summary {
	margin-left: auto;
	margin-right: auto;
	margin-top: 16px;
	text-align: center;
}

#percentWithVehiclesLabel, #percentOnTimeLabel, #percentEarlyLabel {
	margin-left: 20px;
}

#percentLateLabel {
	margin-left: 40px;
}

#totalBlocksLabel, #percentWithVehiclesLabel, #percentOnTimeLabel, #percentEarlyLabel, #percentLateLabel {
	margin-right: 4px;
	color: graytext;
}

</style>

<script>
var ALLOWABLE_EARLY_MSEC = 1 * 60*1000; // 1 minute
var ALLOWABLE_LATE_MSEC  = 4 * 60*1000; // 4 minutes

//need to escape special character in jquery as . : are not interpreted correctly
function jq( myid ) {	 
    return myid.replace( /(:|\.|\[|\]|,)/g, "\\\\$1" );
}

function removeUnneededBlockAndRouteElements(routes) {
	// First get rid of route elements that are not needed anymore because they
	// are not in the ajax data.
	var routesElements = $("[id|='routeId']");
	for (var i=0; i<routesElements.length; ++i) {
		var routeElementId = routesElements[i].id;
		var routeInAjaxData = false;
		// Go through ajax route data
		for (var j=0; j<routes.routes.length; ++j) {
			if (routeElementId == "routeId-" + routes.routes[j].id) {
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
		for (var j=0; j<routes.routes.length; ++j) {
			var routeData = routes.routes[j];
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

/**
 * When doing $(id) using jquery the id cannot contain "." or ":" characters
 * since those are used to signifify the DOM heirarchy. So convert
 * such characters to something acceptable.
 */
function idForQuery(id) {
	return id.replace(".", "_").replace(":", "_");
}

function handleAjaxData(routes) {
	// Remove blocks and routes that are not in the ajax data
	removeUnneededBlockAndRouteElements(routes);

	var totalNumberBlocks = 0;
	var totalVehicles = 0;
	var totalLate = 0;
	var totalOnTime = 0;
	var totalEarly = 0;
	
	// Now add a route element if it is in ajax data but element doesn't exist yet
	for (var j=0; j<routes.routes.length; ++j) {
		var routeData = routes.routes[j];
				
		// If route element doesn't yet exist for this route then create it
		var routeElementId = "routeId-" + idForQuery(routeData.id);
		var routeElement = $("#" + routeElementId);
		if (routeElement.length == 0) {
			// Note: the outer div with class='group' is needed so user can 
			// reorder the routes
 			$("#accordion").append(
 					"<div class='group' id='" + routeElementId + "'>" +
 					 " <h3>" + routeData.name + 
 					 // Need to use span instead of div since accordion requires 
 					 // using an h3 element and h3 can't have a div in it.
 					 // And the spans need to be created in reverse order since
 					 // using css float: right to get spans displayed on the right.
 				     "  <span class='routeValue' id='routeEarlyVehicles' title='Number of vehicles that are more than 1 minute early'></span>" + 
 				     "  <span class='routeLabel' id='routeEarlyVehiclesLabel' title='Number of vehicles that are more than 1 minute early'>Early:</span>" +
 				     "  <span class='routeValue' id='routeOnTimeVehicles' title='Number of vehicles that are on time'></span>" + 
 				     "  <span class='routeLabel' id='routeOnTimeVehiclesLabel' title='Number of vehicles that are on time'>On Time:</span>" +
 				     "  <span class='routeValue' id='routeLateVehicles' title='Number of vehicles more that 4 minutes late'></span>" + 
 				     "  <span class='routeLabel' id='routeLateVehiclesLabel' title='Number of vehicles more that 4 minutes late'>Late:</span>" +
 				     "  <span class='routeValue' id='routeVehicles' title='Number of vehicles assigned to blocks and predictable for the route'></span>" + 
 				     "  <span class='routeLabel' id='routeVehiclesLabel' title='Number of vehicles assigned to blocks and predictable for the route'>Assigned:</span>" +
 				     "  <span class='routeValue' id='routeBlocks' title='Number of blocks currently active for the route'></span>" + 
 				     "  <span class='routeLabel' id='routeBlocksLabel' title='Number of blocks currently active for the route'>Blocks:</span>" +
					 " </h3>" +
 					 " <div id='blocksDiv'><table id='blocksTable'></table></div>" +
 					 "</div>");	
 		}
		
		// Update the route info by setting number of blocks
		var blocksValueElement = $("#" + routeElementId + " #routeBlocks");
		var numberOfActiveBlocks = routeData.block.length;
		blocksValueElement.text(numberOfActiveBlocks);

		// Update the route info by setting number of vehicles and how many
		// are late, on time, or early. Ignore schedule based vehicles.
		var vehiclesLate = 0;
		var vehiclesOnTime = 0;
		var vehiclesEarly = 0;
		for (var k=0; k<routeData.block.length; ++k) {
			var blockData = routeData.block[k];
			for (var l=0; l<blockData.vehicle.length; ++l) {
				// Only count schedule based vehicles
				if (!blockData.vehicle[l].scheduleBased) {
					var schAdh = parseInt(blockData.vehicle[l].schAdh);
					if (schAdh < -ALLOWABLE_LATE_MSEC) {
						++vehiclesLate;
					} else {
						if (schAdh > ALLOWABLE_EARLY_MSEC) {
							++vehiclesEarly;
						} else {
							++vehiclesOnTime;
						}
					}
				}
			}
		}
		var numberOfVehicles = vehiclesLate + vehiclesEarly + vehiclesOnTime;

		var vehiclesValueElement = $("#" + routeElementId + " #routeVehicles");
		vehiclesValueElement.text(numberOfVehicles);
		if (numberOfVehicles < numberOfActiveBlocks)
			vehiclesValueElement.addClass("problemColor");
		else
			vehiclesValueElement.removeClass("problemColor");
		
		var vehiclesLateValueElement = $("#" + routeElementId + " #routeLateVehicles");
		vehiclesLateValueElement.text(vehiclesLate);
		if (vehiclesLate > 0)
			vehiclesLateValueElement.addClass("lateColor");
		else
			vehiclesLateValueElement.removeClass("lateColor");
		
		var vehiclesOnTimeValueElement = $("#" + routeElementId + " #routeOnTimeVehicles");
		vehiclesOnTimeValueElement.text(vehiclesOnTime);

		var vehiclesEarlyValueElement = $("#" + routeElementId + " #routeEarlyVehicles");
		vehiclesEarlyValueElement.text(vehiclesEarly);
		if (vehiclesEarly > 0)
			vehiclesEarlyValueElement.addClass("earlyColor");
		else
			vehiclesEarlyValueElement.removeClass("earlyColor");

		// Update all the block information for this route
		var blocksTable = $("#" + routeElementId + " #blocksTable");
		for (var i=0; i<routeData.block.length; ++i) {
			// Update total for summary
			++totalNumberBlocks;
			
			// If block element doesn't yet exist then create it
			var blockData = routeData.block[i];
			var blockElementId = "blockId-" + idForQuery(blockData.id);
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
						" <td class='blockLabel'>Headsign:</td><td id='tripHeadsign'></td>" + 
						"</tr>" +
						"<tr id='" + blockElementId + "'>" +
						" <td class='blockLabel'>Vehicle:</td><td id='vehiclesForBlock'></td>" +
						" <td class='blockLabel'>Adh:</td><td id='vehicleSchedAdh'></td>" +
						"</tr>");
			}
			/* this is to escape . and :  characters */			
			routeElementId=jq(routeElementId);
			blockElementId=jq(blockElementId);
			
			// Update the information for the block 
			var blockValueElement = $("#" + routeElementId + " #" + blockElementId + " #block");
			blockValueElement.text(blockData.id);
			
			var blockStartValueElement = $("#" + routeElementId + " #" + blockElementId + " #blockStart");
			blockStartValueElement.text(blockData.startTime);
			
			var blockEndValueElement = $("#" + routeElementId + " #" + blockElementId + " #blockEnd");
			blockEndValueElement.text(blockData.endTime);
			
			var blockServiceValueElement = $("#" + routeElementId + " #" + blockElementId + " #blockService");
			blockServiceValueElement.text(blockData.serviceId);
			if (blockData.serviceId.length > 10) {
				blockServiceValueElement.addClass("blockValueSmallerFont");
			} else {
				blockServiceValueElement.removeClass("blockValueSmallerFont");
			}
			
			var tripValueElement = $("#" + routeElementId + " #" + blockElementId + " #trip");
			var tripId = blockData.trip.shortName != null ? 
					blockData.trip.shortName : blockData.trip.id;
			tripValueElement.text(tripId);
			
			var tripStartValueElement = $("#" + routeElementId + " #" + blockElementId + " #tripStart");
			tripStartValueElement.text(blockData.trip.startTime);

			var tripEndValueElement = $("#" + routeElementId + " #" + blockElementId + " #tripEnd");
			tripEndValueElement.text(blockData.trip.endTime);
			
			var tripHeadsignValueElement = $("#" + routeElementId + " #" + blockElementId + " #tripHeadsign");
			tripHeadsignValueElement.text(blockData.trip.headsign);
			
			var vehiclesValueElement = $("#" + routeElementId + " #" + blockElementId + " #vehiclesForBlock");
			var vehiclesValue = "none";
			var vehicleAssigned = false;
			var scheduleBasedVehicle = false;
			if (blockData.vehicle.length != 0) {
				vehiclesValue = "";
				for (var v=0; v<blockData.vehicle.length; ++v) {
					++totalVehicles;
					
					var vehicleData = blockData.vehicle[v];
					if (v > 0)
						vehiclesValue += ", ";
					vehiclesValue += vehicleData.id;
					
					vehicleAssigned = true;
					if (vehicleData.scheduleBased)
						scheduleBasedVehicle = true;
				}
			}		
			vehiclesValueElement.text(vehiclesValue);
			
			if (vehiclesValue.length > 10) {
				vehiclesValueElement.addClass("blockValueSmallerFont");
			} else {
				vehiclesValueElement.removeClass("blockValueSmallerFont");
			}
			if (vehicleAssigned && !scheduleBasedVehicle) {
				vehiclesValueElement.removeClass("problemColor");
			} else {
				vehiclesValueElement.addClass("problemColor");
			}

			var vehiclesSchedAdhElement = $("#" + routeElementId + " #" + blockElementId + " #vehicleSchedAdh");
			var schAdhStr = "-";
			if (blockData.vehicle.length > 0) {
				schAdhStr = blockData.vehicle[0].schAdhStr;
				if (blockData.vehicle[0].schAdh < -ALLOWABLE_LATE_MSEC) {
					++totalLate;
					vehiclesSchedAdhElement.addClass("lateColor");
				} else if (blockData.vehicle[0].schAdh > ALLOWABLE_EARLY_MSEC) {
					++totalEarly;
					vehiclesSchedAdhElement.addClass("earlyColor");
				} else {
					++totalOnTime;
					vehiclesSchedAdhElement.removeClass("lateColor earlyColor");
				}
			}
			vehiclesSchedAdhElement.text(schAdhStr);
		} // Done with each block for the route
	} // Done with each route
	
	// Update the summary at bottom of page
	$("#totalBlocksValue").text(totalNumberBlocks);
	
	var percentageVehicles = 100.0 * totalVehicles / totalNumberBlocks
	$("#percentWithVehiclesValue").text(percentageVehicles.toFixed(0) + "%");
	if (percentageVehicles < 90.0) {
		$("#percentWithVehiclesValue").addClass("problemColor");
	} else {
		$("#percentWithVehiclesValue").removeClass("problemColor");		
	}
	
	var percentageLate = 100.0 * totalLate / totalNumberBlocks;
	$("#percentLateValue").text(percentageLate.toFixed(0) + "%");
	if (percentageLate > 10.0) {
		$("#percentLateValue").addClass("lateColor");
	} else {
		$("#percentLateValue").removeClass("lateColor");		
	}
	
	var percentageOnTime = 100.0 * totalOnTime / totalNumberBlocks;
	$("#percentOnTimeValue").text(percentageOnTime.toFixed(0) + "%");
	
	var percentageEarly = 100.0 * totalEarly / totalNumberBlocks;
	$("#percentEarlyValue").text(percentageEarly.toFixed(0) + "%");
	if (percentageEarly > 10.0) {
		$("#percentLateValue").addClass("earlyColor");
	} else {
		$("#percentLateValue").removeClass("earlyColor");		
	}

	// Since route widgets might have changed need to call refresh
	$( "#accordion" ).accordion("refresh");	
}

/*
 * Get active block data via AJAX
 */
function getAndProcessData() {
	$.getJSON(apiUrlPrefix + "/command/activeBlocksByRoute", handleAjaxData)
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

<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Active Blocks</title>
</head>
<body>
<%@include file="/template/header.jsp" %>

<div id="title">Active Blocks</div>
<div id="accordion"></div>
<div id="summary">
  <span id="totalBlocksLabel" title="Total number of blocks">Blocks:</span>
  <span id="totalBlocksValue" title="Total number of blocks"></span>
  <span id="percentWithVehiclesLabel" title="Percentage of blocks that have an assigned and predictable vehicle">Assigned:</span>
  <span id="percentWithVehiclesValue" title="Percentage of blocks that have an assigned and predictable vehicle"></span>
  <span id="percentLateLabel" title="Percentage of blocks where vehicle is more than 4 minutes late">Late:</span>
  <span id="percentLateValue" title="Percentage of blocks where vehicle is more than 4 minutes late"></span>
  <span id="percentOnTimeLabel" title="Percentage of blocks where vehicle is on time">OnTime:</span>
  <span id="percentOnTimeValue" title="Percentage of blocks where vehicle is on time"></span>
  <span id="percentEarlyLabel" title="Percentage of blocks where vehicle is more than 1 minute early">Early:</span>
  <span id="percentEarlyValue" title="Percentage of blocks where vehicle is more than 1 minute early"></span>
</div>
</body>
</html>