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

// Start dynamic AVL polling rate at 2 sec and give it a max of 20 sec
var avlPollingRate = 2000;
var maxAvlPollingRate = 20000;

//Globals are set via showRoute()
var apiUrlPrefixAllAgencies;
var apiUrlPrefix;
var initialStopId; // So can popup predictions window at startup
var initialRouteId;
var initialDirectionId; // Needed cause some agencies use single stop for both directions
var updateVehiclesTimer;
var hideStaleThingsTimer;

/**
 * Called when prediction read from API. Updates the content of the
 * predictionsPopup with the new prediction info.
 */
 
function predictionCallback(preds, status) {
	// If predictions popup was closed then don't do anything,
	// and don't set timer to reread predictions again.
	if (predictionsPopup == null) 
		return;
	
	// There will be predictions for just a single route/stop
	var routeStopPreds = preds.predictions[0];
	
	// Set timeout to update predictions again in few seconds
	predictionsTimeout = setTimeout(getAndShowPredictionsForStop, 20000, 
			routeStopPreds.routeShortName, routeStopPreds.stopId);

	// Add route and stop info
	var content = routeStopPreds.stopName + '<br/>';
		
	// For each destination add predictions
	for (var i in routeStopPreds.dest) {
		// If direction was specified then only show predictions for
		// that direction. This is needed for agencies who define a single
		// stop for both directions. So if prediction for wrong directionId
		// then skip it.
		if (initialDirectionId && initialDirectionId != routeStopPreds.dest[i].dir)
			continue;
		
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
 * Initiates AJAX call to get prediction data.
 */
function getAndShowPredictionsForStop(routeShortName, stopId) {
	// Clear any existing prediction timeout so don't get wrong prediction 
	// displayed
	if (predictionsTimeout)
		clearTimeout(predictionsTimeout);
	predictionsTimeout = null;

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
	getAndShowPredictionsForStop(stopMarker.routeShortName, stopMarker.stop.id);
    
	// Create popup in proper place but content will be added in predictionCallback()
	predictionsPopup = L.popup(stopPopupOptions)
		.setLatLng(stopMarker.getLatLng())
		.openOn(map);
}

// So can zoom to user and initialStop
var userLatLng = null;
var nextVehicleLatLng = null;
var initialStop = null;
var secondStop = null;

/**
 * Zooms in further to the user location, the first stop, and the location
 * of the next vehicle for the stop.
 */
function zoomIn() {
	// To fit in stop the user selected
	var bounds = [];
	bounds.push(L.latLng(initialStop.lat, initialStop.lon));

	// Nice to show at least a bit of the route for context, so also make
	// sure the second stop is shown
	if (secondStop)
		bounds.push(L.latLng(secondStop.lat, secondStop.lon));
	
	// Use user's location if it has been determined. If not known
	// then will zoom in to the first stop alone.
	if (userLatLng)
		bounds.push(userLatLng);
	
	// As experiment also fit in next predicted vehicle
	if (nextVehicleLatLng) {
		bounds.push(nextVehicleLatLng);
	
		// If would have to zoom out too much to fit in the vehicle then
		// don't do so.
		var zoomLevelToIncludeVehicle = map.getBoundsZoom(bounds);
		if (zoomLevelToIncludeVehicle < 15) {
			bounds.pop();
			//console.log("zoomLevelToIncludeVehicle=" + zoomLevelToIncludeVehicle);
		}
	}
	
	// Actually zoom in. Animation is definitely cool looking.
	// Limit maxZoom in case user is close to stop. Allow to zoom
	// in only up to 4 levels because beyond that won't use nice
	// animation for the transition. And limit zoom to level 18
	// because greater than that is too far.
	var zoomLimit = map.getZoom() + 4;
	if (zoomLimit > 18)
		zoomLimit = 18;
	map.fitBounds(bounds, {animate: true, maxZoom: zoomLimit, padding: [10, 10]});
}

/**
 * Reads in route data obtained via AJAX and draws route and stops on map.
 * Also fits the map to show the important part of the route along with
 * the predicted for vehicles.
 * 
 * Uses global minorStopOptions, stopOptions, minorShapeOptions, shapeOptions, 
 * and map. 
 */
function routeConfigCallback(routesData, status) {
	// If there is an old route then remove it
	if (routeFeatureGroup) {
		map.removeLayer(routeFeatureGroup);
	}
	
	// If there is an old popup then remove it to make sure that when showing
	// new route it doesn't try to also show the popup.
	if (predictionsPopup) {
		map.closePopup();
		predictionsPopup = null;
	}

	// Use a FeatureGroup to contain all paths and stops so that can use
	// bringToBack() on the whole group at once in  order to make sure that
	// the paths & stops are drawn below the vehicles even if the vehicles
	// happen to be drawn first.
	routeFeatureGroup = L.featureGroup();

	// Only working with single route at a time for now
	var route = routesData.routes[0];
	
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
				
				// Remember the first stop so can zoom to it
				initialStop = stop;
				
				// Also remember the second major stop if there is one
				if (j+1 < direction.stop.length)
					secondStop = direction.stop[j+1];
				else
					secondStop = null;
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
		nextVehicleLatLng = L.latLng(route.locationOfNextPredictedVehicle.lat, 
				route.locationOfNextPredictedVehicle.lon);
		locsToFit.push(nextVehicleLatLng);
	}
	
	// Get map to fit route. Need to set animate to false. Otherwise if called
	// a second time fitBounds() doesn't work and the map pans to a strange
	// place or completely stops working.
	map.fitBounds(locsToFit, {animate: false});
	
	// It can happen that vehicles get drawn before the route paths & stops.
	// In this case need call bringToBack() on the paths and stops so that
	// the vehicles will be drawn on top.
	// Note: bringToBack() must be called after map is first specified 
	// via fitBounds() or other such method.
	routeFeatureGroup.bringToBack();
	
	// Now that have the stop location can show walking directions if also
	// already have the user location. If don't yet have user location
	// then showWalkingDirections() will be called when it is found.
	if (userLatLng) {
		showWalkingDirections(userLatLng.lat, userLatLng.lng, 
				initialStop.lat, initialStop.lon);
	}

	// After a bit of a delay, once user can see context, zoom in further
	setTimeout(zoomIn, 1200);
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
	else if (vehicleData.vehicleType == "1")
		vehicleIcon = subwayIcon;
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
	if (vehicleData.uiType != "minor") {
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
	}
	
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

	// Keep track if got updated AVL data. If did then can keep the AVL
	// polling rate the same.
	var gotUpdatedAvlData = false;
	
	// Go through vehicle data read in for route...
	for (var i = 0; i < vehicles.vehicles.length; ++i) {
		var vehicleData = vehicles.vehicles[i];
				
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
			
			// Definitely got updated data
			gotUpdatedAvlData = true;
		} else {			
			// If got new AVL report then remember such
			var oldVehicleData = vehicleMarker.vehicleData;
			if (vehicleData.loc.time != oldVehicleData.loc.time)
				gotUpdatedAvlData = true;
				
			// Vehicle icon already exists, so update it
			updateVehicleMarker(vehicleMarker, vehicleData);
		}

		// Store vehicle data obtained via AJAX with vehicle so it can be used 
		// in popup
		vehicleMarker.vehicleData = vehicleData;
	}
	
	// If didn't get any updated AVL data then back off on the polling rate
	if (!gotUpdatedAvlData) {
		avlPollingRate = 2 * avlPollingRate;
		if (avlPollingRate > maxAvlPollingRate)
			avlPollingRate = maxAvlPollingRate;
		console.log("Didn't get new AVL data so increasing polling rate to " 
				+ avlPollingRate + " msec.");
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
	
	// Call this function again at the appropriate time. This can't be done
	// in vehicleLocationsCallback() because it won't be called if there is
	// an error.
	updateVehiclesTimer = 
		setTimeout(updateVehiclesUsingApiData, avlPollingRate);
}

/**
 * Clears any timers that might be running. Intended for when leaving the
 * map page.
 */
function clearTimers() {
	if (updateVehiclesTimer)
		clearTimeout(updateVehiclesTimer);
	if (hideStaleThingsTimer)
		clearInterval(hideStaleThingsTimer);
}

/**
 * Start the timers for hiding stale vehicles
 */
function startTimers() {
	// Setup timer to determine if haven't updated vehicles in a while.
	// This happens when open up a laptop or tablet that was already
	// displaying the map. For this situation should get rid of the
	// old predictions and vehicles so that they don't scoot around
	// wildly once actually do a vehicle update. This should happen
	// pretty frequently (every 300ms) so that stale vehicles and
	// such are removed as quickly as possible.
	hideStaleThingsTimer = setInterval(hideThingsIfStale, 300);
}

/**
 * To be called when the map is first displayed. Shows the specified route
 * and then the vehicles and user location.
 * 
 * @param agencyId
 * @param routeId
 * @param stopId
 * @param apiKey
 */
function showRoute(agencyId, routeId, directionId, stopId, apiKey) {
	// Set globals
	apiUrlPrefixAllAgencies = "/api/v1/key/" + apiKey;
	apiUrlPrefix = apiUrlPrefixAllAgencies + "/agency/" + agencyId;
	initialRouteId = routeId;
	initialDirectionId = directionId;
	initialStopId = stopId;
	
	// Get the route config data
	var url = apiUrlPrefix + "/command/routesDetails?r=" + routeId;
	if (directionId)
		url += "&d=" + directionId;
	if (stopId)
		url += "&s=" + stopId;
	$.getJSON(url, routeConfigCallback);	
	
	// Deal with the timers that show vehicles and such. First 
	// clear any old ones. Do this before updateVehiclesUsingApiData()
	// since that creates a new timer.
	clearTimers();
	startTimers();
	
	// Show the vehicles for the route
	updateVehiclesUsingApiData();
}

/**
 * Displays walking directions on map
 * @param data results from API
 */
function walkingDirectionCallback(data) {
	// Just draw the first direction. The MapBox walking instructions
	// often return a couple of choices that differ only in how
	// one crosses a street. Looks bad to show two choices that
	// almost overlap.
	for (var i=0; i<1 /*data.routes.length*/; ++i) {
		var route = data.routes[i]
		
		// If person is right be the stop, within 20m, then no
		// point drawing directions.
		if (route.distance < 20)
			break;
		
		var coordinates = route.geometry.coordinates;

		// Create label for the path
		var duration = Math.round(route.duration/60) + 1;
		var theHtml = "<image src='images/pitch-12.png'></image>" + duration + "min";
		var coordIdx = Math.round(coordinates.length/3);
		var iconLat = coordinates[coordIdx][1];
		var iconLon = coordinates[coordIdx][0];
		iconOptions = {
				className: 'walkingDirDiv', 
				html: theHtml,
				iconAnchor: [-3,0],
				iconSize: null}; // So size isn't auto set to 12px
		var myIcon = L.divIcon(iconOptions);		
		L.marker([iconLat, iconLon], {icon: myIcon, clickable: false}).addTo(map);

		// Draw the path
		var latLngs = [];		
		for (var j=0; j<coordinates.length; ++j) {
			var loc = coordinates[j];
			// Note: for Mapbox API lat & lng are reversed
			latLngs.push(L.latLng(loc[1], loc[0]));
		}		
		var polyline = L.polyline(latLngs, walkingOptions).addTo(map);

	}
}

/**
 * Gets walking directions from Mapbox API and displays them on the map
 */
function showWalkingDirections(lat1, lon1, lat2, lon2) {
	// Note that order of longitudes and latitudes is different for Mapbox than usual
	var url = "https://api.mapbox.com/v4/directions/mapbox.walking/" 
		+ lon1 + "," + lat1 + ";" 
		+ lon2 + "," + lat2 +
		".json?" 
		+ "access_token=pk.eyJ1IjoidHJhbnNpdGltZSIsImEiOiJiYnNWMnBvIn0.5qdbXMUT1-d90cv1PAIWOQ";
	$.getJSON(url, walkingDirectionCallback);
}


var userMarker = null;
var userAccuracyMarker = null;

/**
 * Creates the marker to indicate the user's location
 * @param latlng
 */
function createUserLocationMarker(latlng, accuracyRadius) {
	// Create the user marker
	userMarker = L.circleMarker(latlng, userMarkerOptions).addTo(map);
	
	// Create the user location accuracy marker
	if (accuracyRadius)
		userAccuracyMarker = L.circle(latlng, accuracyRadius, 
				userAccuracyMarkerOptions)
				.addTo(map);
}

/**
 * Moves the users current location marker to specified location
 */
function setUserLocationMarkerLocation(latlng, accuracyRadius) {
	if (userMarker)
		userMarker.setLatLng(latlng);	
	if (userAccuracyMarker) {
		userAccuracyMarker.setLatLng(latlng);
		if (accuracyRadius)
			userAccuracyMarker.setRadius(accuracyRadius);
	}
}

/**
 * Called when got user's location
 * @param locationEvent
 */
function locationFound(locationEvent) {
	// If user location just now being set then show walking instructions
	if (!userLatLng && initialStop) {
		showWalkingDirections(locationEvent.latlng.lat, locationEvent.latlng.lng, 
				initialStop.lat, initialStop.lon);
	}
	
	// Remember user location so can zoom to it when route selected
	userLatLng = locationEvent.latlng;
	
	if (userMarker) {
		// userMarker already created so move it to proper location
		setUserLocationMarkerLocation(locationEvent.latlng, locationEvent.accuracy);
	} else {
		// userMarker not yet created so create it now
		createUserLocationMarker(locationEvent.latlng, locationEvent.accuracy);
	}

}

/**
 * Called when error locating user. Nothing to do here but log error.
 * @param errorEvent
 */
function locationError(errorEvent) {
	console.log("locationError() called. Error=" + errorEvent.message);
}

/**
 * Create the leaflet map with a scale and specify which map tiles to use.
 * Creates the global map variable.
 */
var map;

function createMap(mapTileUrl, mapTileCopyright) {
	// Create map. Don't use default zoom control so can set it's position
	map = L.map('map', {zoomControl: false});
	L.control.scale({metric: false}).addTo(map);
	
	// Add zoom control where it won't interfere with a back button in 
	// upper left hand corner.
	L.control.zoom({position: 'bottomleft'}).addTo(map);
	
	L.tileLayer(mapTileUrl,
	  // Specifying a shorter version of attribution. Original really too long.
	  //attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>',
	  {attribution: '&copy; <a href="http://openstreetmap.org">OpenStreetMap</a> &amp; <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © ' + mapTileCopyright,
	   maxZoom: 19
	   }).addTo(map);

	// Set the CLIP_PADDING to a higher value so that when user pans on map
	// the route path doesn't need to be redrawn. Note: leaflet documentation
	// says that this could decrease drawing performance. But hey, it looks
	// better.
	L.Path.CLIP_PADDING = 0.8;

	// Initiate event handler to be called when a popup is closed. Sets 
	// predictionsPopup to null to indicate that don't need to update predictions 
	// anymore since stop popup not displayed anymore.
	map.on('popupclose', function(e) {
		predictionsPopup = null;
		clearTimeout(predictionsTimeout);
		predictionsTimeout = null;
		
		if (e.popup.parent)
			e.popup.parent.popup = null;
	});
	
	// See if user location specified in query string. If so, use it.
	// This way can test out map, including walking directions, for
	// particular locations.
	if (getQueryVariable('userLat') && getQueryVariable('userLon')) {
		var userLat = parseFloat(getQueryVariable('userLat'));
		var userLon = parseFloat(getQueryVariable('userLon'));
		userLatLng = L.latLng(userLat, userLon);
		createUserLocationMarker(userLatLng);
	} else {
		// User location not specified in query string so start 
		// continuous tracking of user location
		map.locate({watch: true});
		map.on("locationfound", locationFound);
		map.on("locationerror", locationError);
	}	
}

function getQueryVariable(paramName) {
	var query = window.location.search.substring(1);
    var vars = query.split("&");
    for (var i=0;i<vars.length;i++) {
    	var pair = vars[i].split("=");
        if (pair[0] == paramName){
        	return pair[1];
        }
    }
    
    // Didn't find the specified param so return false
    return false;
}