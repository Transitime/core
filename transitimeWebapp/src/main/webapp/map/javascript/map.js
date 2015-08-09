/**
 * This JavaScript file is attempt to separate out map JavaScript into
 * separate file for use with smart phone oriented map.
 */

// Use a FeatureGroup to contain all paths and stops so that can use
// bringToBack() on the whole group at once in  order to make sure that
// the paths & stops are drawn below the vehicles even if the vehicles
// happen to be drawn first.
var routeFeatureGroup = null;

// Set to true if want more detailed info
var verbose = false;

//For keeping track the predictions popup so can update content
var predictionsPopup = null;
var predictionsTimeout = null;

/**
 * Called when prediction read from API. Updates the content of the
 * predictionsPopup with the new prediction info.
 */
 
function predictionCallback(preds, status) {
	// If predictions popup was closed then don't do anything
	if (predictionsPopup == null) 
		return;
	
	// There will be predictions for just a single route/stop
	var routeStopPreds = preds.predictions[0];
	
	// Set timeout to update predictions again in few seconds
	predictionsTimeout = setTimeout(getPredictionsJson, 20000, 
			routeStopPreds.routeShortName, routeStopPreds.stopId);

	// Add route and stop info
	var content = routeStopPreds.stopName + '<br/>';
		
	// For each destination add predictions
	for (var i in routeStopPreds.dest) {
		// If there are several destinations then add a horizontal rule
		// to break predictions up by destination
		if (routeStopPreds.dest.length > 1)
			content += '<hr/>';
		
		// Add the destination/headsign info
		if (routeStopPreds.dest[i].headsign)
			content += 'To ' + routeStopPreds.dest[i].headsign + '<br/>';
		
		// Add each prediction for the current destination
		if (routeStopPreds.dest[i].pred.length > 0) {
			content += '<span class="prediction">';
			
			for (var j in routeStopPreds.dest[i].pred) {
				// Separators between the predictions
				if (j == 1)
					content += ' & ';
					
				// Add the actual prediction
				var pred = routeStopPreds.dest[i].pred[j];
				content += pred.min;
				
				// Added any special indicators for if schedule based,
				// delayed, or not yet departed from terminal
				if (pred.scheduleBased)
					content += '<sup>sched</sup>';
				else {
					if (pred.notYetDeparted)
						content += '<sup>waiting</sup>';
					else
						if (pred.delayed) 
							content += '<sup>delay</sup>';
				}
			}
			content += ' mins';
			
			content += '</span>';
		} else {
			// There are no predictions so let user know
			content += "No predictions";
		}
	}
	
	// Now update popup with the wonderful prediction info
	predictionsPopup.setContent(content);
}

/**
 * Initiates API call to get prediction data.
 */
function getPredictionsJson(routeShortName, stopId) {
	// JSON request of prediction data
	var url = apiUrlPrefix + "/command/predictions?rs=" + routeShortName 
			+ "|" + stopId + "&numPreds=2";
	$.getJSON(url, predictionCallback);	
}

/**
 * Called when user clicks on stop. Request predictions from API and calls
 * predictionCallback() when data received.
 */
function showStopPopup(stopMarker) {
	// JSON request of prediction data
	getPredictionsJson(stopMarker.routeShortName, stopMarker.stop.id);
    
	// Create popup in proper place but content will be added in predictionCallback()
	predictionsPopup = L.popup(stopPopupOptions)
		.setLatLng(stopMarker.getLatLng())
		.openOn(map);
}
/**
 * Reads in route data obtained via AJAX and draws route and stops on map.
 * 
 * Uses global minorStopOptions, stopOptions, minorShapeOptions, shapeOptions, 
 * and map. 
 */
function routeConfigCallback(route, status) {
	// If there is an old route then remove it
	if (routeFeatureGroup) {
		map.removeLayer(routeFeatureGroup);
	}
	// Use a FeatureGroup to contain all paths and stops so that can use
	// bringToBack() on the whole group at once in  order to make sure that
	// the paths & stops are drawn below the vehicles even if the vehicles
	// happen to be drawn first.
	routeFeatureGroup = L.featureGroup();

	// Draw stops for the route. Do stops before paths so that when call  
	// bringToBack() the stops will end up being on top.
	var locsToFit = [];
	for (var i=0; i<route.direction.length; ++i) {
		var direction = route.direction[i];
		for (var j=0; j<direction.stop.length; ++j) {
			var stop = direction.stop[j];
			var options = stop.minor ? minorStopOptions : stopOptions;
			
			// Draw first non-minor stop differently to highlight it
			if (stop.id == initialStopId) {
				options = firstStopOptions;
			}
			
			// Keep track of non-minor stop locations so can fit map to show them all
			if (!stop.minor)
				locsToFit.push(L.latLng(stop.lat, stop.lon));
			
			// Create the stop Marker
			var stopMarker = L.circleMarker([stop.lat,stop.lon], options).addTo(map);
			
			routeFeatureGroup.addLayer(stopMarker);
			
			// Store stop data obtained via AJAX with stopMarker so it can be used in popup
			stopMarker.stop = stop;
			
			// Store routeShortName obtained via AJAX with stopMarker so can be 
			// used to get predictions for stop/route
			stopMarker.routeShortName = route.shortName;
			
			// When user clicks on non-minor stop popup information box
			if (!stop.minor) {
				stopMarker.on('click', function(e) {
					showStopPopup(this);
					}).addTo(map);
			}
			
			// If dealing with the selected stop then popup predictions for it.
			// Making sure it is not a minor stop so that only happens for proper direction.
			if (stop.id == initialStopId && !stop.minor)
				showStopPopup(stopMarker);
		}
	}

	// Draw the paths for the route
	for (var i=0; i<route.shape.length; ++i) {
		var shape = route.shape[i];
		var options = shape.minor ? minorShapeOptions : shapeOptions;
		
		var latLngs = [];		
		for (var j=0; j<shape.loc.length; ++j) {
			var loc = shape.loc[j];			
			latLngs.push(L.latLng(loc.lat, loc.lon));
		}
		var polyline = L.polyline(latLngs, options).addTo(map);
		
		routeFeatureGroup.addLayer(polyline);
	}
	
	// Add all of the paths and stops to the map at once via the FeatureGroup
	routeFeatureGroup.addTo(map);

	// If stop was specified for getting route then locationOfNextPredictedVehicle
	// is also returned. Use this vehicle location when fitting bounds of map
	// so that user will always see the next vehicle coming.
	if (route.locationOfNextPredictedVehicle) {
		locsToFit.push(L.latLng(route.locationOfNextPredictedVehicle.lat, 
				route.locationOfNextPredictedVehicle.lon));
	}
	
	// Get map to fit route
	map.fitBounds(locsToFit);
	
	// It can happen that vehicles get drawn before the route paths & stops.
	// In this case need call bringToBack() on the paths and stops so that
	// the vehicles will be drawn on top.
	// Note: bringToBack() must be called after map is first specified 
	// via fitBounds() or other such method.
	routeFeatureGroup.bringToBack();
}

var vehicleMarkers = [];

/**
 * Gets vehicle marker from the array vehicleIcons 
 */
function getVehicleMarker(vehicleId) {
	for (var i=0; i<vehicleMarkers.length; ++i) {
		if (vehicleMarkers[i].vehicleData.id == vehicleId)
			return vehicleMarkers[i];
	}	
	
	// Don't yet have marker for that vehicle
	return null;
}

/**
 * So that can easily output speed in km/hr instead of m/s
 */
function formatSpeed(speedInMetersPerSec) {
	// If not a number then just return blank string
	if (speedInMetersPerSec == "NaN")
		return "";
	
	// Convert m/s to km/hr and truncate to 1 decimal place to make
	// output pretty
	return (parseFloat(speedInMetersPerSec) * 3.6).toFixed(1) + " km/hr";
}

/**
 * Takes in vehicleData info from API and determines the content
 * to be displayed for the vehicles popup.
 */
function getVehiclePopupContent(vehicleData) {
	var currentTime = (new Date).getTime()/1000;
	var ageInSecs = Math.round(currentTime - vehicleData.loc.time);
	return "Last GPS:<br/>" + ageInSecs + " sec";
}

/**
 * Determines options for drawing the vehicle marker icon based on uiType
 */
function getVehicleMarkerOptions(vehicleData) {
	if (!vehicleData.uiType || vehicleData.uiType == "normal")
		return vehicleMarkerOptions;
	else if (vehicleData.uiType == "secondary")
		return secondaryVehicleMarkerOptions;
	else
		return minorVehicleMarkerOptions;
}

/**
 * Determines options for drawing the vehicle background circle based on uiType
 */
function getVehicleMarkerBackgroundOptions(vehicleData) {
	// Handle unassigned vehicles
	if (!vehicleData.block)
		return unassignedVehicleMarkerBackgroundOptions;
	else if (!vehicleData.uiType || vehicleData.uiType == "normal")
		return vehicleMarkerBackgroundOptions;
	else if (vehicleData.uiType == "secondary")
		return secondaryVehicleMarkerBackgroundOptions;
	else
		return minorVehicleMarkerBackgroundOptions;
}

/**
 * Determines which icon to use to represent vehicle, such as a streetcar icon,
 * a bus icon, or an icon to represent a layover.
 */
 function getIconForVehicle(vehicleData) {
	// Determine which icon to use. Vehicles types defined per route
	// as indicated in the GTFS spec at https://developers.google.com/transit/gtfs/reference#routes_fields
	var vehicleIcon = busIcon;
	if (vehicleData.vehicleType == "0")
		vehicleIcon = streetcarIcon;
	else if (vehicleData.vehicleType == "2")
		vehicleIcon = railIcon;
	else if (vehicleData.vehicleType == "4")
		vehicleIcon = ferryIcon;

	// Indicate layovers specially
	if (vehicleData.layover)
		vehicleIcon = layoverIcon;
	
	// Return the result
	return vehicleIcon;
}

/**
 * Removes the specified marker from the map
 */
function removeVehicleMarker(vehicleMarker) {
	// Close stop predictions popup if there is one
	if (vehicleMarker.popup)
		map.closePopup(vehicleMarker.popup);
	
	map.removeLayer(vehicleMarker.background);
	map.removeLayer(vehicleMarker.headingArrow);
	map.removeLayer(vehicleMarker);
}

/**
 * Removes all vehicle markers from map
 */
function removeAllVehicles() {
	// Remove each vehicleMarker
	for (var i in vehicleMarkers) {
		var vehicleMarker = vehicleMarkers[i];
		removeVehicleMarker(vehicleMarker);
	}
	
	// Clear out the vehicleMarkers array
	vehicleMarkers.length = 0;
}

/*
 * For determining if vehicles and other markers are stale.
 * This can happen if laptop or tablet with map already running
 * is turned on again. 
 */
var lastVehiclesUpdateTime = new Date();

function hideThingsIfStale() {
	// If no vehicle update for 30 seconds then...
	if (new Date() - lastVehiclesUpdateTime > 30000) {
		// Remove all the vehicle icons. Should also remove
		// predictions as well but that would be more work
		// to implement.
		removeAllVehicles();
		
		console.log("Removing all vehicle because no update in a while.");
		
		// Update lastVehiclesUpdateTime so that don't keep
		// calling removeAlVehicles().
		lastVehiclesUpdateTime = new Date();
	}	
}

/**
 * Creates a new marker for the vehicle. The marker actually consists of
 * the main icon marker, a background circle, and an arrow marker to indicate
 * heading. 
 */
function createVehicleMarker(vehicleData) {
	var vehicleLoc = L.latLng(vehicleData.loc.lat, vehicleData.loc.lon);
	
	// Create new icon. First create the background marker as
	// a simple colored circle.
	var vehicleBackground = L.circleMarker(vehicleLoc,
			getVehicleMarkerBackgroundOptions(vehicleData)).addTo(
			map);

	// Add a arrow to indicate the heading of the vehicle
	var headingArrow = L.rotatedMarker(vehicleLoc,
			getVehicleMarkerOptions(vehicleData))
			.setIcon(arrowIcon).addTo(map);
	headingArrow.options.angle = vehicleData.loc.heading;
	headingArrow.setLatLng(vehicleLoc);
	// If heading is NaN then don't show arrow at all
	if (isNaN(parseFloat(vehicleData.loc.heading))) {
		headingArrow.setOpacity(0.0);
	}

	// Create the actual vehicle marker. This needs to be created
	// last and on top since the popup callback for the vehicle is
	// attached to this marker. Otherwise won't get click events.
	var vehicleMarker = L.marker(vehicleLoc,
			getVehicleMarkerOptions(vehicleData))
			.setIcon(getIconForVehicle(vehicleData))
			.addTo(map);

	// Add the background and the heading arrow markers to 
	// vehicleIcon so they can all be updated when the vehicle moves.
	vehicleMarker.background = vehicleBackground;
	vehicleMarker.headingArrow = headingArrow;

	// When user clicks on vehicle popup shows additional info
	vehicleMarker.on('click', function(e) {
		var content = getVehiclePopupContent(this.vehicleData);
		var latlng = L.latLng(this.vehicleData.loc.lat,
				this.vehicleData.loc.lon);
		// Create popup and associate it with the vehicleMarker
		// so can later update the content.
		this.popup = L.popup(vehiclePopupOptions, this)
			.setLatLng(latlng)
			.setContent(content).openOn(map);
	});	
	
	// Return the new marker
	return vehicleMarker;
}

/**
 * Updates the vehicle markers plus the popup based on the vehicleData read
 * in from the API.
 */
function updateVehicleMarker(vehicleMarker, vehicleData) {
	// If changing its minor status then need to redraw it with new options.
	if (vehicleMarker.vehicleData.uiType != vehicleData.uiType) {
		// Change from minor to non-minor, or visa versa so update icon
		vehicleMarker
				.setOpacity(getVehicleMarkerOptions(vehicleData).opacity);
		vehicleMarker.background
				.setStyle(getVehicleMarkerBackgroundOptions(vehicleData));
	}

	// Set to proper icon, which changes depending not just on vehicle
	// type but also on whether currently on layover.
	vehicleMarker.setIcon(getIconForVehicle(vehicleData));
	
	// Update orientation of the arrow for when setLatLng() is called
	vehicleMarker.headingArrow.options.angle = vehicleData.loc.heading;

	// Set opacity of arrow icon depending on state and on whether
	// the heading is valid
	if (isNaN(parseFloat(vehicleData.loc.heading))) {
		vehicleMarker.headingArrow.setOpacity(0.0);
	} else {
		vehicleMarker.headingArrow
				.setOpacity(getVehicleMarkerOptions(vehicleData).opacity);
	}

	// Update content in vehicle popup, if it has changed, in case it 
	// is actually popped up
	if (vehicleMarker.popup) {
		var content = getVehiclePopupContent(vehicleData);
		// If the content has actually changed then update the popup
		if (content != vehicleMarker.popup.getContent())
			vehicleMarker.popup.setContent(content).update();
	}

	// Update markers location on the map if vehicle has actually moved.
	if (vehicleMarker.vehicleData.loc.lat != vehicleData.loc.lat
			|| vehicleMarker.vehicleData.loc.lon != vehicleData.loc.lon) {
		animateVehicle(vehicleMarker, 
				vehicleMarker.vehicleData.loc.lat, 
				vehicleMarker.vehicleData.loc.lon, 
				vehicleData.loc.lat, vehicleData.loc.lon);
	}
}

/**
 * Reads in vehicle data obtained via AJAX. Called for each vehicle in API
 * whether vehicle changed or not.
 */
function vehicleLocationsCallback(vehicles, status) {
	// If no data from the API for vehicle where have created an icon
	// then remove that icon. Count down in for loop since might
	// be deleting some elements.
	for (var i = vehicleMarkers.length - 1; i >= 0; --i) {
		var haveDataForVehicle = false;
		var vehicleMarker = vehicleMarkers[i];
		var markerVehicleId = vehicleMarker.vehicleData.id;
		for (var j = 0; j < vehicles.vehicle.length; ++j) {
			var dataVehicleId = vehicles.vehicle[j].id;
			if (markerVehicleId == dataVehicleId) {
				haveDataForVehicle = true;
				break;
			}
		}

		// If no data from the API for the vehicle associated with the icon...
		if (!haveDataForVehicle) {
			// Delete the marker from the map
			removeVehicleMarker(vehicleMarker);

			// Remove the vehicleIcon from the vehiclesIcon array
			vehicleMarkers.splice(i, 1);
		}
	}

	// Go through vehicle data read in for route...
	for (var i = 0; i < vehicles.vehicle.length; ++i) {
		var vehicleData = vehicles.vehicle[i];
		
		// Don't display schedule based vehicles since they are not real and
		// would only serve to confuse people.
		if (vehicleData.scheduleBased)
			continue;
		
		var vehicleLoc = L.latLng(vehicleData.loc.lat, vehicleData.loc.lon);

		// If vehicle icon wasn't already created then create it now
		var vehicleMarker = getVehicleMarker(vehicleData.id);
		if (vehicleMarker == null) {
			// Create the new marker
			vehicleMarker = createVehicleMarker(vehicleData);
			
			// Keep track of vehicle marker so it can be updated
			vehicleMarkers.push(vehicleMarker);
		} else {
			// Vehicle icon already exists, so update it
			updateVehicleMarker(vehicleMarker, vehicleData);
		}

		// Store vehicle data obtained via AJAX with vehicle so it can be used in popup
		vehicleMarker.vehicleData = vehicleData;
	}
	
	// Update when vehicles last updated so can determine if update
	// hasn't happened in a long time
	lastVehiclesUpdateTime = new Date();
}

/**
 * Moves the vehicle an increment between its original and new locations.
 * Calls setTimeout() to call this function again in order to continue
 * the animation until finished.
 */
function interpolateVehicle(vehicleMarker, cnt, interpolationSteps, origLat, origLon, newLat, newLon) {
	// Determine the interpolated location
	var interpolatedLat = parseFloat(origLat) + (newLat - origLat) * cnt
			/ interpolationSteps;
	var interpolatedLon = parseFloat(origLon) + (newLon - origLon) * cnt
			/ interpolationSteps;
	var interpolatedLoc = [ interpolatedLat, interpolatedLon ];

//	console.log("interpolating vehicleId=" + vehicleMarker.vehicleData.id + " cnt=" + cnt + 
//			" interpolatedLat=" + interpolatedLat + " interpolatedLon=" + interpolatedLon);

	// Update all markers sto have interpolated location
	vehicleMarker.setLatLng(interpolatedLoc);
	vehicleMarker.background.setLatLng(interpolatedLoc);
	vehicleMarker.headingArrow.setLatLng(interpolatedLoc);

	// If there is a popup for the vehicle then need to move it too
	if (vehicleMarker.popup)
		vehicleMarker.popup.setLatLng(interpolatedLoc);

	if (++cnt <= interpolationSteps) {
		setTimeout(interpolateVehicle, 60, 
				vehicleMarker, cnt, interpolationSteps, 
				origLat, origLon, newLat, newLon);
	}
}

/**
 * Initiates animation of moving vehicle from old location to new location.
 * Determines number of interpolations to use for the animation. Doesn't
 * use animation (though still updates marker position) if vehicle not on
 * visible part of map or moves less than a pixel. Interpolation count is
 * determined such that will move at least a pixel towards the new loc.
 * Trying to avoid situation where moves less than a pixel towards the
 * new loc but then moves a pixel in the other horiz/vert direction due
 * to rounding.
 */
function animateVehicle(vehicleMarker, origLat, origLon, newLat, newLon) {
	//console.log("animating vehicleId=" + vehicleMarker.vehicleData.id + 
	//		" origLat=" + origLat + " origLon=" + origLon +
	//		" newLat=" + newLat + " newLon=" + newLon);
	
	// Use default interpolationSteps of 1 so that the marker location
	// will be updated no matter what. This is important because even
	// if vehicle only moves slightly or is off the map, still need
	// to update vehicle position.
	var interpolationSteps = 1;
	
	// Determine if vehicle is visile since no need to animate vehicles
	// that aren't visible
	var bounds = map.getBounds();
	var origLatLng = L.latLng(origLat, origLon);
	var newLatLng = L.latLng(newLat, newLon);
	if (bounds.contains(origLatLng) && bounds.contains(newLatLng)) {
		// Vehicle is visible. Determine number of pixels moving vehicle.
		// Don't want to look at sqrt(x^2 + y^2) since trying to avoid 
		// making the animation jagged. If use the sqrt distance traveled
		// then would sometimes have the marker move sideways instead of
		// forward since would be moving less than 1 pixel at a time
		// horizontally or vertically.
		var origPoint = map.latLngToLayerPoint(origLatLng) ;
		var newPoint = map.latLngToLayerPoint(newLatLng) ;
		var deltaX = origPoint.x - newPoint.x;
		var deltaY = origPoint.y - newPoint.y;
		var pixelsToMove = Math.max(Math.abs(deltaX), Math.abs(deltaY));

		//console.log("origPoint=" + origPoint + " newPoint=" + newPoint);
		//console.log("pixelsToMove=" + pixelsToMove);
		
		// Set interpolationSteps to number of pixels that need to move. This
		// provides smoothest possible animation. But limit interpolationSteps
		// to be at least 1 and at most 10.
		interpolationSteps = Math.max(pixelsToMove, 1);
		interpolationSteps = Math.min(interpolationSteps, 10);
	}
		
	// Start the interpolation process to update marker position
	interpolateVehicle(vehicleMarker, 1, interpolationSteps, origLat, origLon, 
			newLat, newLon);
}

/**
 * Should actually read in new vehicle positions and adjust all vehicle icons.
 */
function updateVehiclesUsingApiData() {
	var url = apiUrlPrefix + "/command/vehiclesDetails?r=" + initialRouteId 
		+ "&s=" + initialStopId + "&numPreds=2";

	// Use ajax() instead of getJSON() so that can set timeout since
	// will be polling vehicle info every 10 seconds and don't want there
	// to be many simultaneous requests.
	$.ajax(url, {
		  dataType: 'json',
		  success: vehicleLocationsCallback,
		  timeout: 6000 // 6 second timeout
		});
}

// Globals are set via showRoute()
var apiUrlPrefixAllAgencies;
var apiUrlPrefix;
var initialStopId; // So can popup predictions window at startup
var initialRouteId;

function showRoute(agencyId, routeId, stopId, apiKey) {
	// Set globals
	apiUrlPrefixAllAgencies = "/api/v1/key/" + apiKey;
	apiUrlPrefix = apiUrlPrefixAllAgencies + "/agency/" + agencyId;
	initialRouteId = routeId;
	initialStopId = stopId;
	
	// Get the route config data
	var url = apiUrlPrefix + "/command/route?r=" + routeId;
	if (stopId)
		url += "&s=" + stopId;
	$.getJSON(url, routeConfigCallback);	
	
	// Show the vehicles for the route
	updateVehiclesUsingApiData();
}



