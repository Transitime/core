<%@ page import="org.transitime.utils.web.WebUtils" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <!-- So that get proper sized map on iOS mobile device -->
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
  
  <link rel="stylesheet" href="<%= request.getContextPath() %>/maps/css/mapUi.css" />
 
  <!-- Load javascript and css files -->
  <%@include file="/template/includes.jsp" %>
  
  <link rel="stylesheet" href="http://cdn.leafletjs.com/leaflet-0.7.3/leaflet.css" />
  <script src="http://cdn.leafletjs.com/leaflet-0.7.3/leaflet.js"></script>
        
  <script src="<%= request.getContextPath() %>/maps/javascript/leafletRotatedMarker.js"></script>
  <script src="<%= request.getContextPath() %>/maps/javascript/mapUiOptions.js"></script>
  
  <!--  Override the body style from the includes.jsp/general.css files -->
  <style>
    body {
	  margin: 0px;
    }
    
    /* For the AVL points */
    div.avlMarker {
      background-color: #ff7800;
      border-color: black;
      border-radius: 4px;
      border-style: solid;
      border-width: 1px;
      width:7px;
      height:7px;
    }
    
    .popupTable {
    	border-spacing: 0px;
    	line-height: 1.2;
    }
    	
    .popupTableLabel {
    	font-weight: bold;
    	text-align: right;
    	padding-right: 4px;
    }
    
  </style>

  <script>
  /* For drawing the route and stops */
  var routeOptions = {
			color: '#00ee00',
			weight: 5,
			opacity: 0.4,
			lineJoin: 'round',
			clickable: false
		};
				
  var stopOptions = {
		    color: '#006600',
		    opacity: 0.4,
		    radius: 4,
		    weight: 2,
		    fillColor: '#006600',
		    fillOpacity: 0.3,
		};

  var routePolylineOptions = {clickable: false, color: "#00f", opacity: 0.5, weight: 4};

  var avlPopupOptions = {closeButton: false};

  var stopPopupOptions = {closeButton: false};

  </script>
  
  <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
  
  <title>AVL Data Map</title>
  
  <script>
  
  /* Called when user clicks on map. Displays AVL data */
  function showAvlPopup(avlMarker) {
  	var avl = avlMarker.avl;
  	var speed = Math.round(parseFloat(avl.speed) * 10)/10;
	var content = "<table class='popupTable'>" 
		+ "<tr><td class='popupTableLabel'>Vehicle:</td><td>" + avl.vehicleid + "</td></tr>" 
		+ "<tr><td class='popupTableLabel'>GPS&nbsp;Time:</td><td>" + avl.time + "</td></tr>" 
  		+ "<tr><td class='popupTableLabel'>Route:</td><td>" + avl.routeshortname + "</td></tr>"
  		+ "<tr><td class='popupTableLabel'>Block:</td><td>" + avl.blockid + "</td></tr>"
  		+ "<tr><td class='popupTableLabel'>Trip:</td><td>" + avl.tripid + "</td></tr>";
  		
  		// Only output both trip ID and trip short name if they are different from each other
  		if (avl.tripid != avl.tripshortname) {
  			content += 
  				"<tr><td class='popupTableLabel'>Trip&nbsp;Name:</td><td>" + avl.tripshortname + "</td></tr>";
  		}
  		
  		content +=
  		  "<tr><td class='popupTableLabel'>Schedule&nbsp;Adh:</td><td>" + avl.schedadh + "</td></tr>"
 		+ "<tr><td class='popupTableLabel'>Lat/Lon:</td><td>" + avl.lat + ", " + avl.lon + "</td></tr>"
  		+ "<tr><td class='popupTableLabel'>Speed:</td><td>" + speed + " kph</td></tr>"
  		+ "<tr><td class='popupTableLabel'>Heading:</td><td>" + avl.heading + " degrees</td></tr>"
  		+ "<tr><td class='popupTableLabel'>Delayed:</td><td>" + avl.isdelayed + "</td></tr>"
  		+ "<tr><td class='popupTableLabel'>Layover:</td><td>" + avl.islayover + "</td></tr>"
  		+ "<tr><td class='popupTableLabel'>Wait&nbsp;Stop:</td><td>" + avl.iswaitstop + "</td></tr>"
  		+ "<tr><td class='popupTableLabel'>Time&nbsp;Proc:</td><td>" + avl.timeprocessed + "</td></tr>"
  		+ "<tr><td class='popupTableLabel'>AVL Source:</td><td>" + avl.source + "</td></tr>"

  		+ "</table>";
	  		
  	  L.popup(avlPopupOptions)
		.setLatLng(avlMarker.getLatLng())
		.setContent(content)
		.openOn(map);

  }
  
  /* Called when receiving the AVL data via AJAX call */
  function processAvlCallback(jsonData) {  
    // List of all the latLngs
    var latLngs = [];

    // So can draw separate polyline per vehicle
    var previousVehicleId = "";
    var latLngsForVehicle = [];
    
    // For each AVL report...
    for (var i=0; i<jsonData.data.length; ++i) {
    	var avl = jsonData.data[i];
    	var latLng = L.latLng(avl.lat, avl.lon);
    	
    	// If getting data for new vehicle then need to draw out polyline 
    	// for the old vehicle
		if (avl.vehicleid != previousVehicleId) {		    
	    	// Create a line connecting the AVL locations as long as there are 
	    	// at least 2 AVL reports for the vehicle
	    	if (latLngsForVehicle.length >= 2)
	    		L.polyline(latLngsForVehicle, routePolylineOptions).addTo(map); //.bringToBack();
	    	latLngsForVehicle = [];
		}  
		previousVehicleId = avl.vehicleid;
		latLngsForVehicle.push(latLng);
		
    	// Create the marker. Use a divIcon so that can have tooltips
    	var tooltip = avl.time.substring(avl.time.indexOf(' ') + 1);    	
    	var avlMarker = L.marker(latLng, {
            icon: L.divIcon({
                className: 'avlMarker',
                iconSize: [7, 7]
            }),
            title: tooltip
        }).addTo(map);
    	
  		// Store the AVL data with the marker so can popup detailed info
    	avlMarker.avl = avl;
    	
		// When user clicks on AVL marker popup information box
		avlMarker.on('click', function(e) {
			showAvlPopup(this);
		}).addTo(map);

		latLngs.push(latLng);
    }
    
    // Draw polyline for the last vehicle in the AVL data
    if (latLngsForVehicle.length >= 2)
    	L.polyline(latLngsForVehicle, routePolylineOptions).addTo(map); //.bringToBack();
    
    // If actually read AVL data...
    if (latLngs.length > 0) {
    	// To make all AVL reports fit in bounds of map
    	map.fitBounds(latLngs);
  	} else {
		alert("No AVL data for the criteria specified.")
  	}
  }

  /* Called when user clicks on map. Displays AVL data */
  function showStopPopup(stopMarker) {
	  var stop = stopMarker.stop;
	  
	  var content = "<table class='popupTable'>" 
			+ "<tr><td class='popupTableLabel'>Stop ID:</td><td>" + stop.id + "</td></tr>" 
			+ "<tr><td class='popupTableLabel'>Name:</td><td>" + stop.name + "</td></tr>" 
	  		+ "</table>";
	  
	  L.popup(stopPopupOptions)
		.setLatLng(stopMarker.getLatLng())
		.setContent(content)
		.openOn(map);
  }
  
  /**
   * Reads in route data obtained via AJAX and draws route and stops on map.
   */
  function routeConfigCallback(route, status) {
	// Draw the paths for the route
	for (var i=0; i<route.shape.length; ++i) {
		var shape = route.shape[i];
		var latLngs = [];		
		for (var j=0; j<shape.loc.length; ++j) {
			var loc = shape.loc[j];			
			latLngs.push(L.latLng(loc.lat, loc.lon));
		}
		L.polyline(latLngs, routeOptions).addTo(map);
	}
	  
  	// Draw stops for the route. 
  	for (var i=0; i<route.direction.length; ++i) {
  		var direction = route.direction[i];
  		for (var j=0; j<direction.stop.length; ++j) {
  			var stop = direction.stop[j];
  			
  			// Create the stop Marker
  			var stopMarker = L.circleMarker([stop.lat,stop.lon], stopOptions).addTo(map);
  			
  			// Store stop data obtained via AJAX with stopMarker so it can be used in popup
  			stopMarker.stop = stop;
  			
  			// When user clicks on stop popup information box
  			stopMarker.on('click', function(e) {
  				showStopPopup(this);
  			}).addTo(map);
  		}
   	 }
  }

  </script>
</head>

<body>
  <div id="map"></div>
</body>

<script>
var map = L.map('map');
L.control.scale({metric: false}).addTo(map);
L.tileLayer('http://api.tiles.mapbox.com/v4/transitime.34a63309/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoidHJhbnNpdGltZSIsImEiOiJiYnNWMnBvIn0.5qdbXMUT1-d90cv1PAIWOQ', {
    attribution: '&copy; <a href="http://openstreetmap.org">OpenStreetMap</a> &amp; <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>',
    maxZoom: 19
}).addTo(map);

// Set the CLIP_PADDING to a higher value so that when user pans on map
// the route path doesn't need to be redrawn. Note: leaflet documentation
// says that this could decrease drawing performance. But hey, it looks
// better.
L.Path.CLIP_PADDING = 0.8;

// Get the AVL data via AJAX and call processAvlCallback to draw it
$.ajax({
  	// The page being requested
    url: "/web/reports/avlJsonData.jsp",
	// Pass in query string parameters to page being requested
	data: {<%= WebUtils.getAjaxDataString(request) %>},
	// Needed so that parameters passed properly to page being requested
	traditional: true,
    dataType:"json",
    async: true,
    // When successful process JSON data
    success: processAvlCallback,
    // When there is an AJAX problem alert the user
    error: function(request, status, error) {
      alert(error + '. ' + request.responseText);
    },
});

// If route specified then display it
var route = "<%= request.getParameter("r") %>";
if (route != "") {
	var url = apiUrlPrefix + "/command/routes?r=" + route;
	$.getJSON(url, routeConfigCallback);		

}

</script>

</html>