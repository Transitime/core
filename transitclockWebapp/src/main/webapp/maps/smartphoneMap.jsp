<%@page import="org.transitclock.web.WebConfigParams"%>
<!--  NOTE: this file is obsolete. Should only be using smartphoneMap.jsp. 
      But this smartphoneMap.html needs to be kept around because is being 
      used for VTA smartphone app. -->
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Smartphone Map</title>
  <!-- So that get proper sized map on iOS mobile device -->
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
  
  <link rel="stylesheet" href="css/mapUi.css" />
 
  <!-- Load javascript and css files -->
  <link rel="stylesheet" href="//unpkg.com/leaflet@0.7.3/dist/leaflet.css" />
  <script src="//unpkg.com/leaflet@0.7.3/dist/leaflet.js"></script>
  <!-- New version of map. CLIP_PADDING doesn't seem to work and then see 
       route paths be redrawn in ugly way when panning
  <link rel="stylesheet" href="leaflet/leaflet.css" />
  <script src="leaflet/leaflet.js"></script>
  -->
  <script src="javascript/leafletRotatedMarker.js"></script>
  <script src="javascript/mapUiOptions.js"></script>
  <script src="javascript/map.js"></script>
  
  <!-- Load in JQuery -->
  <script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>

  <!--  Override the body style from the includes.jsp/general.css files -->
  <style>
    body {
	  margin: 0px;
    }
  </style>
  
  <style>
  /* Make prediction popup small */
  .leaflet-popup-content {
    line-height: 1.2;
    margin: 3px 3px;
    text-align: center;
    font-size: x-small;
  }
  
  .prediction {font-size: large; font-weight: bold;}
  
  /* For a back button. Currently not implemented */
  #button {
  	position: absolute; 
  	z-index: 9999;
  	top: 10px;
  	left: 10px;
  	padding-left: 20px;
  	padding-right: 20px;
  	font-size: x-large;
  }
  
  /* For labeling walking directions with distance and time */
  .walkingDirDiv {
  	background: white;
  	padding-left: 0px;
  	padding-right: 1px;
  	height: 14px;
  	white-space: nowrap; /* So all in one line */
  	opacity: 0.9;
  	font-size: x-small;
  	border-radius: 3px;
  	vertical-align: top;
  	border-style: solid;
  	border-width: 1px;
  	border-color: gray;
  }
    
  </style>
  
</head>

<body>
  <!--  Create map that takes up entire view -->
  <div id="map">
    <!--  <div id='button'>&lt; Back</div>  --> 
  </div>
</body>
  
  <script>

  // Customize some map options to get desired look. These override the usual
  // options from mapUIOptions.js
  var stopPopupOptions = {
			offset: L.point(0, -0),
			closeButton: true,
			// If true then popup closes when pan or zoom, which is underiable.
			// But if false, then popup can only be closed if use the close button
			// which is a nuisance since the popup can hide info on walking to stop.
			closeOnClick: false,
			// Don't want map to auto pan every time prediction updated. Tried
			// setting autoPan to false but didn't work. 
			autoPan: false
		}
  
  var shapeOptions = {
			color: '#0080FF',
			weight: 7,
			opacity: 0.75,
			lineJoin: 'round',
			clickable: false
		};
				
  var minorShapeOptions = {
			color: '#0080FF',
			weight: 1,
			opacity: 0.4,
			clickable: false
		};
  
  var stopOptions = {
		    color: '#092F87',
		    opacity: 1.0,
		    radius: 3,
		    weight: 2,
		    fillColor: '#092F87',
		    fillOpacity: 0.6,
		};

  var firstStopOptions = {
		    color: '#092F87',
		    opacity: 1.0,
		    radius: 4,
		    weight: 2,
		    fillColor: '#0080FF',
		    fillOpacity: 0.8,		
		}

  var minorStopOptions = {
		    color: '#092F87',
		    opacity: 0.25,
		    radius: 2,
		    weight: 1,
		    fillColor: '#092F87',
		    fillOpacity: 0.25,
		    clickable: false
		};

  var vehicleMarkerOptions = {
			opacity: 1.0,
		};

  var secondaryVehicleMarkerOptions = {
			opacity: 0.65,		
		};

  var minorVehicleMarkerOptions = {
			opacity: 0.3,
			clickable: false
		};


  var busIcon = L.icon({
		    iconUrl: 'images/bus-24.png',
		    iconRetinaUrl: 'images/bus-24@2x.png',
		    iconSize: [25, 25],
		    iconAnchor: [13, 13],
		    popupAnchor: [0, -13],
		});

  //Specifies how the blue dot indicating user position looks. Dark blue circle.
  var userMarkerOptions = {
  	    color: '#ffffff',
  	    opacity: 1.0,
  	    radius: 7,
  	    weight: 1,
  	    fillColor: '#2363FA',
  	    fillOpacity: 1.0,
  	    clickable: false
  	};
  
  // For indicating accuracy of user location. Faint blue circle.
  var userAccuracyMarkerOptions = {
	  color: '#ff000',
	  fillColor: '#2363FA',
	  fillOpacity: 0.13,
	  clickable: false
  };

  // For showing walking directions
  var walkingOptions = {
		color: '#008800',
		opacity: 0.6,
		weight: 4,
		dashArray: "3,9",
		lineCap: "square",
		clickable: false
  };

  // Create the leaflet map
  createMap('<%= WebConfigParams.getMapTileUrl() %>', '<%= WebConfigParams.getMapTileCopyright() %>');
  
  // Get the parameters from query string and display the route
  var agencyId = getQueryVariable("a");
  var routeId = getQueryVariable("r");
  var directionId = getQueryVariable("d");
  var stopId = getQueryVariable("s");
  var apiKey = "5ec0de94";
  showRoute(agencyId, routeId, directionId, stopId, apiKey);
  
  </script>

</html>