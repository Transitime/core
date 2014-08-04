<%@page import="org.transitime.utils.StringUtils"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<link rel="stylesheet" href="http://cdn.leafletjs.com/leaflet-0.7.3/leaflet.css" />
<script src="http://cdn.leafletjs.com/leaflet-0.7.3/leaflet.js"></script>
<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
<script src="/api/javascript/jquery-dateFormat.min.js"></script>

<style type="text/css">
#map { height: 660px; }
</style>

<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>apiTestFile</title>
</head>
<body>
<div id="map"></div>
</body>
<script>
function getQueryVariable(variable)
{
       var query = window.location.search.substring(1);
       var vars = query.split("&");
       for (var i=0;i<vars.length;i++) {
               var pair = vars[i].split("=");
               if(pair[0] == variable){return pair[1];}
       }
       return(false);
}

// Class for creating marker with an orientation. Found at
// https://www.mapbox.com/mapbox.js/example/v1.0.0/rotating-controlling-marker/
// Orients the icon to marker.options.angle when setLatLng() is called. 
// MIT-licensed code by Benjamin Becquet
// https://github.com/bbecquet/Leaflet.PolylineDecorator
L.RotatedMarker = L.Marker.extend({
options: { angle: 0 },
_setPos: function(pos) {
 L.Marker.prototype._setPos.call(this, pos);
 if (L.DomUtil.TRANSFORM) {
   // use the CSS transform rule if available
   this._icon.style[L.DomUtil.TRANSFORM] += ' rotate(' + this.options.angle + 'deg)';
 } else if (L.Browser.ie) {
   // fallback for IE6, IE7, IE8
   var rad = this.options.angle * L.LatLng.DEG_TO_RAD,
   costheta = Math.cos(rad),
   sintheta = Math.sin(rad);
   this._icon.style.filter += ' progid:DXImageTransform.Microsoft.Matrix(sizingMethod=\'auto expand\', M11=' +
     costheta + ', M12=' + (-sintheta) + ', M21=' + sintheta + ', M22=' + costheta + ')';
 }
}
});
L.rotatedMarker = function(pos, options) {
 return new L.RotatedMarker(pos, options);
};

var verbose = getQueryVariable("verbose");
var urlPrefix = "http://localhost:8080/api/v1/key/TEST/agency/" + getQueryVariable("a");

// Create the map with a scale and specify which map tiles to use
var map = L.map('map');

L.control.scale({metric: false}).addTo(map);

L.tileLayer('http://{s}.tiles.mapbox.com/v3/examples.map-i86knfo3/{z}/{x}/{y}.png', {
    attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>',
    maxZoom: 19
}).addTo(map);


// Options that effect how routes, stops, and vehicles are drawn
var shapeOptions = {
	color: '#00ee00',
	weight: 8,
	opacity: 0.8,
	lineJoin: 'round'
};
		
var minorShapeOptions = {
	color: '#00ee00',
	weight: 2,
	opacity: 0.4,
};
		
var stopOptions = {
    color: '#006600',
    opacity: 1.0,
    radius: 4,
    weight: 2,
    fillColor: '#006600',
    fillOpacity: 0.6,
};

var firstStopOptions = {
    color: '#006600',
    opacity: 1.0,
    radius: 7,
    weight: 2,
    fillColor: '#ccffcc',
    fillOpacity: 0.9,		
}

var minorStopOptions = {
    color: '#006600',
    opacity: 0.2,
    radius: 3,
    weight: 2,
    fillColor: '#006600',
    fillOpacity: 0.2,
};

var busIcon = L.icon({
    iconUrl: 'images/bus-24.png',
    iconRetinaUrl: 'images/bus-24@2x.png',
    iconSize: [24, 24],
    iconAnchor: [12, 12],
    popupAnchor: [0, -12],
});

var streetcarIcon = L.icon({
    iconUrl: 'images/rail-light-24.png',
    iconRetinaUrl: 'images/rail-light-24@2x.png',
    iconSize: [24, 24],
    iconAnchor: [12, 12],
    popupAnchor: [0, -12],
});

var layoverIcon = L.icon({
    iconUrl: 'images/cafe-24.png',
    iconRetinaUrl: 'images/cafe-24@2x.png',
    iconSize: [24, 24],
    iconAnchor: [12, 12],
    popupAnchor: [0, -12],
	
});

var arrowIcon = L.icon({
	iconUrl: 'images/arrow.png',
	iconSize: [30, 30],
	iconAnchor: [15,15],
});

var vehicleMarkerOptions = {
	opacity: 1.0,
};

var secondaryVehicleMarkerOptions = {
	opacity: 0.7,		
};

var minorVehicleMarkerOptions = {
	opacity: 0.4,		
};

var vehicleMarkerBackgroundOptions = {
    radius: 12,
    weight: 0,
    fillColor: '#ffffff',
    fillOpacity: 1.0,				
};

var secondaryVehicleMarkerBackgroundOptions = {
    radius: 12,
    weight: 0,
    fillColor: '#ffffff',
    fillOpacity: 0.75,				
};

var minorVehicleMarkerBackgroundOptions = {
    radius: 12,
    weight: 0,
    fillColor: '#ffffff',
    fillOpacity: 0.5,				
};

var vehiclePopupOptions = {
	offset: L.point(0,-2), 
	closeButton: false
};


/**
 * Reads in route data obtained via AJAX and draws route and stops on map.
 */
function routeConfigCallback(route, status) {
	// Use a FeatureGroup to contain all paths and stops so that can use
	// bringToBack() on the whole group at once in  order to make sure that
	// the paths & stops are drawn below the vehicles even if the vehicles
	// happen to be drawn first.
	var featureGroup = L.featureGroup();

	// Draw stops for the route. Do stops before paths so that when call  
	// bringToBack() the stops will end up being on top.
	var locsToFit = [];
	var firstNonMinorStop = true;
	for (var i=0; i<route.stop.length; ++i) {
		var stop = route.stop[i];
		var options = stop.minor ? minorStopOptions : stopOptions;
		// Draw first non-minor stop differently to highlight it
		if (!stop.minor && firstNonMinorStop) {
			options = firstStopOptions;
			firstNonMinorStop = false;
		}
		
		// Keep track of non-minor stop locations so can fit map to show them all
		if (!stop.minor)
			locsToFit.push(L.latLng(stop.lat, stop.lon));
		
		// Create the stop Marker
		var stopMarker = L.circleMarker([stop.lat,stop.lon], options).addTo(map);
		
		featureGroup.addLayer(stopMarker);
		
		// Store stop data obtained via AJAX with stopMarker so it can be used in popup
		stopMarker.stop = stop;
		
		// When user clicks on stop popup information box
		stopMarker.on('click', function(e) {
			var content = this.stop.name + '<br/><b>Stop Id:</b> ' + this.stop.id;
			L.popup().setLatLng(e.latlng).setContent(content).openOn(map);}
					 ).addTo(map);
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
		var polyline = L.polyline(latLngs, options);//.addTo(map);
		
		featureGroup.addLayer(polyline);
		
		// Store shape data obtained via AJAX with polyline so it can be used in popup
		polyline.shapeData = shape;
		
		polyline.on('click', function(e) {
			var content = "TripPattern=" + this.shape.tripPattern + "<br/>Headsign=" + this.shape.headsign;
			L.popup().setLatLng(e.latlng).setContent(content).openOn(map);}
					 ).addTo(map);

	}

	
	// Add all of the paths and stops to the map at once via the FeatureGroup
	featureGroup.addTo(map);

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
	featureGroup.bringToBack();
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
 * Takes in vehicleData info from API and determines the content
 * to be displayed for the vehicles popup.
 */
function getVehiclePopupContent(vehicleData) {
    var layoverStr = verbose && vehicleData.layover ? 
			 ("<br/><b>Layover:</b> " + vehicleData.layover) : "";
    var layoverDepartureStr = vehicleData.layover ? 
    		 ("<br/><b>Departure:</b> " + 
    				 $.format.date(new Date(parseInt(vehicleData.layoverDepartureTime)), 'HH:mm:ss')) : "";
    var nextStopIdStr = vehicleData.nextStopId ? 
    		 ("<br/><b>Next Stop:</b> " + vehicleData.nextStopId) : "";
    var latLonHeadingStr = verbose ? "<br/><b>Lat:</b> " + vehicleData.loc.lat
    			+ "<br/><b>Lon:</b> " + vehicleData.loc.lon 
    			+ "<br/><b>Heading:</b> " + vehicleData.loc.heading : "";
	var gpsTimeStr = $.format.date(new Date(vehicleData.loc.time), 'HH:mm:ss');
    var directionStr = verbose ? "<br/><b>Direction:</b> " + vehicleData.direction : ""; 
    var tripPatternStr = verbose ? "<br/><b>Trip Pattern:</b> " + vehicleData.tripPattern : "";
    
    var content = "<b>Vehicle:</b> " + vehicleData.id 
		+ latLonHeadingStr
		+ "<br/><b>GPS Time:</b> " + gpsTimeStr
		+ "<br/><b>Headsign:</b> " + vehicleData.headsign
		+ directionStr 
		+ "<br/><b>SchAhd:</b> " + vehicleData.schAdhStr 
		+ "<br/><b>Block:</b> " + vehicleData.block
		+ "<br/><b>Trip:</b> " + vehicleData.trip
		+ tripPatternStr
		+ layoverStr
		+ layoverDepartureStr
		+ nextStopIdStr;
	
	return content;
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
	if (!vehicleData.uiType || vehicleData.uiType == "normal")
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
	// Determine which icon to use
	var vehicleIcon = busIcon;
	if (vehicleData.vehicleType == "0")
		vehicleIcon = streetcarIcon;
	if (vehicleData.layover)
		vehicleIcon = layoverIcon;
	return vehicleIcon;
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
				// Delete the icon
				map.removeLayer(vehicleMarker.background);
				map.removeLayer(vehicleMarker);

				// Remove the vehicleIcon from the vehiclesIcon array
				vehicleMarkers.splice(i, 1);
			}
		}

		// Go through vehicle data read in for route...
		for (var i = 0; i < vehicles.vehicle.length; ++i) {
			var vehicleData = vehicles.vehicle[i];
			var vehicleLoc = L.latLng(vehicleData.loc.lat, vehicleData.loc.lon);

			// If vehicle icon wasn't already created then create it now
			var vehicleMarker = getVehicleMarker(vehicleData.id);
			if (vehicleMarker == null) {
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
				vehicleMarker = L.marker(vehicleLoc,
						getVehicleMarkerOptions(vehicleData))
						.setIcon(getIconForVehicle(vehicleData))
						.addTo(map);

				// Add the background and the heading arrow markers to 
				// vehicleIcon so they can all be updated when the vehicle moves.
				vehicleMarker.background = vehicleBackground;
				vehicleMarker.headingArrow = headingArrow;

				// Keep track of all the vehicle icons so they can be moved
				vehicleMarkers.push(vehicleMarker);

				// When user clicks on vehicle popup shows additional info
				vehicleMarker.on('click', function(e) {
					var content = getVehiclePopupContent(this.vehicleData);
					var latlng = L.latLng(this.vehicleData.loc.lat,
							this.vehicleData.loc.lon);
					this.popup = L.popup(vehiclePopupOptions, this)
						.setLatLng(latlng)
						.setContent(content).openOn(map);
				});
			} else {
				// Vehicle icon already exists. If changing its minor status then need to
				// redraw it with new options.
				if (vehicleMarker.vehicleData.uiType != vehicleData.Type) {
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

				// Update content, if it has changed, in vehicle popup in case it 
				// is popped up
				if (vehicleMarker.popup) {
					var content = getVehiclePopupContent(vehicleData);
					// If the content has actually changed then update the popup
					if (content != vehicleIcon.popup.getContent())
						vehicleIcon.popup.setContent(content).update();
				}

				// Update it's location on the map if it is actually changing.
				if (vehicleMarker.vehicleData.loc.lat != vehicleLoc.lat
						|| vehicleMarker.vehicleData.loc.lon != vehicleLoc.lng) {
					animateVehicle(vehicleMarker, 1,
							vehicleMarker.vehicleData.loc.lat,
							vehicleMarker.vehicleData.loc.lon, 
							vehicleLoc.lat,	vehicleLoc.lng);
				}
			}

			// Store vehicle data obtained via AJAX with vehicle so it can be used in popup
			vehicleMarker.vehicleData = vehicleData;
		}
	}

	var ANIMATION_STEPS = 15;

	/**
	 * Moves the vehicle an increment between its original and new locations.
	 * Calls setTimeout() to call this function again in order to continue
	 * the animation until finished.
	 */
	function animateVehicle(vehicleMarker, cnt, origLat, origLon, newLat, newLon) {
		var interpolatedLat = parseFloat(origLat) + (newLat - origLat) * cnt
				/ ANIMATION_STEPS;
		var interpolatedLon = parseFloat(origLon) + (newLon - origLon) * cnt
				/ ANIMATION_STEPS;
		var interpolatedLoc = [ interpolatedLat, interpolatedLon ];

		//console.log("animating vehicleId=" + vehicleIcon.vehicleData.id + " cnt=" + cnt + 
		//		" interpolatedLat=" + interpolatedLat + " interpolatedLoc=" + interpolatedLoc);

		vehicleMarker.setLatLng(interpolatedLoc);
		vehicleMarker.background.setLatLng(interpolatedLoc);
		vehicleMarker.headingArrow.setLatLng(interpolatedLoc);

		// If there is a popup for the vehicle then need to move it too
		if (vehicleMarker.popup)
			vehicleMarker.popup.setLatLng(interpolatedLoc);

		if (++cnt <= ANIMATION_STEPS) {
			setTimeout(animateVehicle, 50, vehicleMarker, cnt, origLat, origLon,
					newLat, newLon);
		}
	}

	// Should actually read in new vehicle positions and adjust
	// all vehicle icons.
	function updateVehicles() {
		var url = urlPrefix + "/command/vehiclesDetails?rShortName="
				+ getQueryVariable("rShortName");
		// If stop specified as query str param to this page pass it to the 
		// vehicles request such that all but the next 2 predicted vehicles
		// will be labled as minor ones and can therefore be drawn in UI to not
		// attract as much attention.
		if (getQueryVariable("s"))
			url += "&s=" + getQueryVariable("s") + "&numPreds=2";
		$.getJSON(url, vehicleLocationsCallback);
	}

	//Read in route info and draw it on map
	var url = urlPrefix + "/command/route?rShortName="
			+ getQueryVariable('rShortName');
	if (getQueryVariable("s"))
		url += "&s=" + getQueryVariable("s");
	if (getQueryVariable("tripPattern"))
		url += "&tripPattern=" + getQueryVariable("tripPattern");
	$.getJSON(url, routeConfigCallback);

	// Read in vehicle locations
	updateVehicles();
	setInterval(updateVehicles, 10000);
</script>
</html>