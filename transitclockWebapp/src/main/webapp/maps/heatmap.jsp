<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%-- This heapmap page was just an experiment and wasn't completed and doesn't work.
     It is kept here for now only in case want to look at creating another heatmap
     in the future. --%>    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page import="org.transitclock.web.WebConfigParams"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Schedule Adherence</title>

  <!-- So that get proper sized map on iOS mobile device -->
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />

  <%@include file="/template/includes.jsp" %>
  
  <link rel="stylesheet" href="http://cdn.leafletjs.com/leaflet-0.7.3/leaflet.css" />
  <script src="http://cdn.leafletjs.com/leaflet-0.7.3/leaflet.js"></script>

  <script src="heatmap/heatmap.js"></script>
  <script src="heatmap/leaflet-heatmap.js"></script>

<style>
html, body, #map { 
	height: 100%; width: 100%; padding: 0px; margin: 0px;
}
</style>

<script>
/**
 * Create the leaflet map with a scale and specify which map tiles to use.
 * Creates the global map variable.
 */
var map;
var heatmapLayerForEarlyVehicles;
var heatmapLayerForLateVehicles;

function createHeatmapLayer() {
	var cfg = {
			// If want to scale the radius then radius is in lat/lon units
			//"radius": 0.005,
			//"scaleRadius": true, 
			
			// If don't scale radius then radius is in pixels
			"radius": 25,
			"scaleRadius": false, 
			
			"useLocalExtrema": false,
			
			"maxOpacity": .8,
			"valueField": "value",
/*			
			"gradient": {
			    // enter n keys between 0 and 1 here
			    // for gradient color customization
			    '.5': 'red',
			    '.8': 'blue',
			    '.95': 'white'
			  }
	*/
	}
	
	var heatmapLayer = new HeatmapOverlay(cfg);
	
	return heatmapLayer;
}

/**
 * Create map and zoom to extent of agency
 */
function createMap(mapTileUrl, mapTileCopyright) {
	heatmapLayerForEarlyVehicles = createHeatmapLayer();
	//heatmapLayerForLateVehicles = createHeatmapLayer();
	heatmapLayerForLateVehicles = new HeatmapOverlay({
		// If want to scale the radius then radius is in lat/lon units
		//"radius": 0.005,
		//"scaleRadius": true, 
		
		// If don't scale radius then radius is in pixels
		"radius": 25,
		"scaleRadius": false, 
		
		"useLocalExtrema": false,
		
		"maxOpacity": .8,
		"valueField": "value"});
	
	var baseTiles = L.tileLayer(mapTileUrl,
			  // Specifying a shorter version of attribution. Original really too long.
			  //attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>',
			  {attribution: '&copy; <a href="http://openstreetmap.org">OpenStreetMap</a> &amp; <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © ' + mapTileCopyright,
			   maxZoom: 19
			   });

	// Create map. 
	map = L.map('map', 
			{layers: [baseTiles /*, heatmapLayerForEarlyVehicles, heatmapLayerForLateVehicles */]});
	L.control.scale({metric: false}).addTo(map);

	map.addLayer(heatmapLayerForLateVehicles);
	map.addLayer(heatmapLayerForEarlyVehicles);
	
	// Set the CLIP_PADDING to a higher value so that when user pans on map
	// the route path doesn't need to be redrawn. Note: leaflet documentation
	// says that this could decrease drawing performance. But hey, it looks
	// better.
	L.Path.CLIP_PADDING = 0.8;
	
	
	// Set map bounds to the agency extent
	$.getJSON(apiUrlPrefix + "/command/agencyGroup", 
			function(agencies) {
		        // Fit the map initially to the agency
				var e = agencies.agency[0].extent;
				map.fitBounds([[e.minLat, e.minLon], [e.maxLat, e.maxLon]]);
				
				// Note: setting of data must be done after bounds is adjusted
				// for map. Otherwise fitBounds() will try to access heatmap
				// before it has been properly initialized. This is 
				// apparently a bug with heatmap.js .
				
				// NOTES: min can't be negative. If negative the range is
				// max-min but the min will be 0.
				// Max ends up being increased to maximum value of data values.
				// Therefore need to clamp the data values to the max.
				var earlyTestData = {
						min: 0,
						max: 10,
				        data: [{lat: 37.775000, lng: -122.439307, value: 0},
				               {lat: 37.786064, lng: -122.399261, value: 5},
				               {lat: 37.786064, lng: -122.390261, value: 5},
				               ]
				};
				heatmapLayerForEarlyVehicles.setData(earlyTestData);

				var lateTestData = {
						min: 0,
						max: 10,
				        data: [{lat: 37.777000, lng: -122.439307, value: 0},
				               {lat: 37.788064, lng: -122.399261, value: 5},
				               {lat: 37.788064, lng: -122.390261, value: 5},
				               ]
				};
				heatmapLayerForLateVehicles.setData(lateTestData);

			});	
}

function getAndProcessData() {
	$.getJSON(apiUrlPrefix + "/command/vehiclesDetails", 
			function(jsonData) {
				var len = jsonData.vehicle.length;
				var data = {};
				data.min = -360000; // Negative means vehicle is late
				data.max = 120000; 
				
				var dataArray = [];
				while (len--) {
					var vehicle = jsonData.vehicle[len];
					
					// If no schedule adherence info for vehicle skip it
					if (!vehicle.schAdh)
						continue;
					
					var dataObj = {lat: vehicle.loc.lat, lng: vehicle.loc.lon, value: vehicle.schAdh};
					dataArray.push(dataObj);
				}
				data.data = dataArray;
				heatmapLayer.setData(data);
			});
}
/**
 * When page finishes loading then create map
 */
$( document ).ready(function() {
	createMap('<%= WebConfigParams.getMapTileUrl() %>', 
			'<%= WebConfigParams.getMapTileCopyright() %>');
	
	// FIXME getAndProcessData();
});

</script>
	
</head>
<body>
<div id="map"></div>
</body>
</html>