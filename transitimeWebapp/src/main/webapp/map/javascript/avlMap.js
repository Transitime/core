//Edit route input width.
$("#route").attr("style", "width: 200px");

// Put the param menu into a table.
$(".param label").wrap("<td>")
$(".param").each(function(i, div) {
	$(div).find(":not(:first-child)").wrapAll("<td>")
	$(div).replaceWith("<tr class='param'>" + div.innerHTML + "</tr>")
})
	
/* For drawing the route and stops */
var routeOptions = {
	color: '#00ee00',
	weight: 4,
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
  
// vehicles is a list of {id: vehicle_id, data: [avl1, avl2, ...]}
var vehicles = [];
  
/* Called when user clicks on map. Displays AVL data */
function showAvlPopup(avlMarker) {
	var avl = avlMarker.avl;
  	var speed = Math.round(parseFloat(avl.speed) * 10)/10;
  	
	var content = "<table class='popupTable'>" 
		+ "<tr><td class='popupTableLabel'>Vehicle:</td><td>" + avl.vehicleId + "</td></tr>" 
		+ "<tr><td class='popupTableLabel'>GPS Time:</td><td>" + avl.time + "</td></tr>" 
  		+ "<tr><td class='popupTableLabel'>Time Proc:</td><td>" + avl.timeProcessed + "</td></tr>"
 		+ "<tr><td class='popupTableLabel'>Lat/Lon:</td><td>" + avl.lat + ", " + avl.lon + "</td></tr>"
  		+ "<tr><td class='popupTableLabel'>Speed:</td><td>" + speed + " kph</td></tr>"
  		+ "<tr><td class='popupTableLabel'>Heading:</td><td>" + avl.heading + "</td></tr>"
  		+ "</table>";
	  		
  	  L.popup(avlPopupOptions)
		.setLatLng(avlMarker.getLatLng())
		.setContent(content)
		.openOn(map);
}


function drawAvlMarker(avl) {
	var latLng = L.latLng(avl.lat, avl.lon);
	  
	// Create the marker. Use a divIcon so that can have tooltips
  	var tooltip = avl.time.substring(avl.time.indexOf(' ') + 1);  
  	var avlMarker = L.rotatedMarker(latLng, {
          icon: L.divIcon({
        	  className: 'avlMarker_',
        	  html: "<div class='avlTriangle icon_" + avl.vehicleId + "' />",
        	  iconSize: [7,7]
          }),
          angle: avl.heading,
          title: tooltip
      }).addTo(vehicleGroup); 
	
  	// Store the AVL data with the marker so can popup detailed info
	avlMarker.avl = avl;
	
  	// When user clicks on AVL marker popup information box
	avlMarker.on('click', function(e) {
		showAvlPopup(this);
	});
  	
	return avlMarker;
}
  
/* Called when receiving the AVL data via AJAX call */
function processAvlCallback(jsonData) {
	 
	/* Save avl data */ 
	
    // List of all the latLngs
    var latLngs = [];

    // So can draw separate polyline per vehicle
    var previousVehicleId = "";
    var latLngsForVehicle = [];
    
    // reset list of vehicles
    vehicles = [];
    
    // For each AVL report...
    var vehicle;
    for (var i=0; i<jsonData.data.length; ++i) {
    	var avl = jsonData.data[i];
    	
    	// If getting data for new vehicle then need to draw out polyline 
    	// for the old vehicle
		if (avl.vehicleId != previousVehicleId) {		    
	    	// Create a line connecting the AVL locations as long as there are 
	    	// at least 2 AVL reports for the vehicle
	    	if (latLngsForVehicle.length >= 2)
	    		L.polyline(latLngsForVehicle, routePolylineOptions).addTo(map); //.bringToBack();
	    	latLngsForVehicle = [];
	    	vehicle = {id: avl.vehicleId, data: []}
	    	vehicles.push(vehicle);
		} 
		
    	// parse date string -> number
    	avl.timenum = Date.parse(avl.time.replace(/-/g, '/').slice(0,-2))
    	
		vehicle.data.push(avl)
		previousVehicleId = avl.vehicleId;
		
		var latLng = drawAvlMarker(avl).getLatLng();

		latLngsForVehicle.push(latLng);
		latLngs.push(latLng);
    }
    
    // Draw polyline for the last vehicle in the AVL data
    if (latLngsForVehicle.length >= 2)
    	L.polyline(latLngsForVehicle, routePolylineOptions).addTo(vehicleGroup); //.bringToBack();
    
    // If actually read AVL data...
    if (latLngs.length > 0) {
    	// To make all AVL reports fit in bounds of map.
    	map.fitBounds(latLngs);
    	// connect export to link to csv creation.
    	createExport(vehicles);
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
	
	var latLngs = [];
	
	for (var i=0; i<route.shape.length; ++i) {
		var shape = route.shape[i];
		for (var j=0; j<shape.loc.length; ++j) {
			var loc = shape.loc[j];			
			latLngs.push(L.latLng(loc.lat, loc.lon));
		}
		L.polyline(latLngs, routeOptions).addTo(routeGroup);
	}
	  
  	// Draw stops for the route. 
  	for (var i=0; i<route.direction.length; ++i) {
  		var direction = route.direction[i];
  		for (var j=0; j<direction.stop.length; ++j) {
  			var stop = direction.stop[j];
  			
  			// Create the stop Marker
  			var stopMarker = L.circleMarker([stop.lat,stop.lon], stopOptions).addTo(routeGroup);
  			
  			// Store stop data obtained via AJAX with stopMarker so it can be used in popup
  			stopMarker.stop = stop;
  			
  			// When user clicks on stop popup information box
  			stopMarker.on('click', function(e) {
  				showStopPopup(this);
  			}).addTo(routeGroup);
  		}
   	 }
}
  
// Data in vehicles will be available as CSV when you click the `export' link.
// CSV should be the AVL CSV format used elsewhere in Transitime.
// org.transitime.avl.AvlCsvWriter writes the following header:
// vehicleId,time,justTime,latitude,longitude,speed,heading,assignmentId,assignmentType
// org.transitime.avl.AvlCsvRecord has required keys vehicleId, time, latitude, longitude
// all others optional
function createExport(vehicles) {
	
	var data = vehicles[0].data
  	
  	// set keys
	var keys = ["vehicleId", "time", "latitude", "longitude", "speed", "heading", "assignmentId"]
	// CSV key => JS object key
	function mapKey(k) {
		var o = {"latitude": "lat", "longitude": "lon"}
		return o[k] || k;
	}
	
  	// write header
  	var text = keys[0];
  	for (var i = 1; i < keys.length; i++)
  		text += "," + keys[i]
  	text += '\n'
  	
  	// write rows
  	for (var i = 0; i < data.length; i++) {
  		text += data[i][keys[0]]
  		for (var j = 1; j < keys.length; j++) {
  			var k = mapKey(keys[j])
  			text += "," + data[i][k]
  		}
  		text += "\n";
  	}
  	
  	var blob = new Blob([text], { type: 'text/plain' }); // change to text/csv for download prompt
  	$("#exportData")[0].href = window.URL.createObjectURL(blob);
 }
 
//Add a new layer for only route/bus markers, so that it can be refreshed
//when selections change without having to redraw tiles.
var map = L.map('map');
L.control.scale({metric: false}).addTo(map);
L.tileLayer('http://api.tiles.mapbox.com/v4/transitime.j1g5bb0j/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoidHJhbnNpdGltZSIsImEiOiJiYnNWMnBvIn0.5qdbXMUT1-d90cv1PAIWOQ', {
 attribution: '&copy; <a href="http://openstreetmap.org">OpenStreetMap</a> &amp; <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>',
 maxZoom: 19
}).addTo(map);

var vehicleGroup = L.layerGroup().addTo(map);
var routeGroup = L.layerGroup().addTo(map);
var animationGroup = L.layerGroup().addTo(map);

//Set the CLIP_PADDING to a higher value so that when user pans on map
//the route path doesn't need to be redrawn. Note: leaflet documentation
//says that this could decrease drawing performance. But hey, it looks
//better.
L.Path.CLIP_PADDING = 0.8;

var contextPath, playButton, pauseButton;

function main(request, path) {
	
	contextPath = path;
	
	playButton = contextPath + "/reports/images/playback/media-playback-start.svg",
		pauseButton = contextPath + "/reports/images/playback/media-playback-pause.svg";
		
	if (request.v || request.r) {
		// Request exists; set all the controls to match the values in the request.
		$("#vehicle").val(request.v).trigger("change");
		$("#beginDate").val(request.beginDate).trigger("change");
		$("#endDate").val(request.endDate).trigger("change");
		$("#beginTime").val(request.beginTime).trigger("change");
		$("#endTime").val(request.endTime).trigger("change");
		$("#route").val(request.r).trigger("change");
	}
	else {
		// no request
		// set beginDate and endDate to defaults
		request.beginDate = $("#beginDate").val()
		request.endDate = $("#endDate").val()
		
		// fit map to agency boundaries.
		$.ajax({
		  	
		    url: apiUrlPrefix + "/command/agencyGroup",
			
		    success: function(agencies) {
		    	var e = agencies.agency[0].extent;
				map.fitBounds([[e.minLat, e.minLon], [e.maxLat, e.maxLon]]);
		    },
		    
		    // When there is an AJAX problem alert the user
		    error: function(request, status, error) {
		      alert(error + '. ' + request.responseText);
		    },
		});
	}
	
	$(".param input").on("change", function() {
		
		/* Set request object to match new values */
		request.v = $("#vehicle").val();
		request.beginDate = $("#beginDate").val();
		request.endDate = $("#endDate").val();
		request.beginTime = $("#beginTime").val();
		request.endTime = $("#endTime").val();
		request.r = $("#route").val();
	
		
		// Clear existing layer and draw new objects on map.
		routeGroup.clearLayers();
		vehicleGroup.clearLayers();
		drawAvlData();
		
		// Reset animation object and associated UI elements.
		if (animation) {
			animation.destroy();
			$("#playbackPlay").attr("src", playButton);	
			$("#playbackRate").text("1X");
			$("#playbackTime").text("00:00:00");
			animation = undefined;
		}
	});
	
	//Get the AVL data via AJAX and call processAvlCallback to draw it
	function drawAvlData() {
		$.ajax({
		  	// The page being requested
		    url: contextPath + "/reports/avlJsonData.jsp",
			// Pass in query string parameters to page being requested
			data: request,
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
		var route = request.r;
		if (route != "") {
			var url = apiUrlPrefix + "/command/route?r=" + route;
			$.getJSON(url, routeConfigCallback);
		}
	
	}
	
	if (request.v) // draw vehicles if there is request information
		drawAvlData();
}

/* Playback functionality */

var animation;


$("#playbackNext").on("click", function() {
	if (animation)
		animation.next()
});

$("#playbackPrev").on("click", function() {
	if (animation)
		animation.prev()
})

$("#playbackPlay").on("click", function() {
	if (animation === undefined) {
		animation = makeAnimation(vehicles[0]);
	}
	
	if (!animation.paused()) {
		animation.pause();
		$("#playbackPlay").attr("src", playButton);
	}
	else { // need to start it
		animation.start();
		$("#playbackPlay").attr("src", pauseButton);
	}
	
});

$("#playbackFF").on("click", function() {
	if (!animation)
		return;
	var rate = animation.rate()*2;
	animation.rate(rate);
	$("#playbackRate").text(rate + "X");
});

$("#playbackRew").on("click", function() {
	if (!animation)
		return;
	var rate = animation.rate()/2;
	animation.rate(rate);
	$("#playbackRate").text(rate + "X");
});


// taken from leafletMovingMarker.js
var interpolatePosition = function(p1, p2, duration, t) {
    var k = t/duration;
    k = (k>0) ? k : 0;
    k = (k>1) ? 1 : k;
    return L.latLng(p1.lat + k*(p2.lat-p1.lat), p1.lon + k*(p2.lon-p1.lon));
};

function makeAnimation(vehicle) {
	
	var startTime = vehicle.data[0].timenum,
		endTime = vehicle.data[vehicle.data.length-1].timenum;
	
	var clock = $("#playbackTime");
	
	var animation = {},
		rate = 1;
	
	var currentIndex = 0; // this means we're going to 1
	
	var elapsedTime = vehicle.data[0].timenum,
		lastTime = 0,
		lineDone = 0;
	
	var paused = true;
	
	var durations = []
	for (var i = 0; i < vehicle.data.length - 1; i++)
		durations.push(vehicle.data[i+1].timenum - vehicle.data[i].timenum);
		
	var icon = L.marker(vehicle.data[0],
			{icon:  L.icon({
			    iconUrl:  contextPath + "/reports/images/bus.png", 
			    iconSize: [25,25]
			})}).addTo(animationGroup);
	
	function tick() {
		var now = Date.now(),
			delta = now - lastTime;
		
		lastTime = now;
		
		elapsedTime += delta * rate;
		
		lineDone += delta * rate;
		
		if (lineDone > durations[currentIndex]) {
			// advance index and icon
			currentIndex += 1
			lineDone = 0;
			
			if (currentIndex == vehicle.data.length - 1)
				return;
			
			icon.setLatLng(vehicle.data[currentIndex])
			icon.update()
			elapsedTime = vehicle.data[currentIndex].timenum
		}
		else {
			var pos = interpolatePosition(vehicle.data[currentIndex], vehicle.data[currentIndex+1], durations[currentIndex], lineDone)
			icon.setLatLng(pos)
			icon.update()
			
		}
		clock.text(parseTime(elapsedTime));
		
		if (!paused)
			requestAnimationFrame(tick)
	}
	
	animation.start = function() { 
		lastTime = Date.now();
		paused = false;
		tick();
	}
	
	animation.pause = function() {
		paused = true;
	}
	
	animation.destroy = function() {
		paused = true;
		animationGroup.removeLayer(icon);
	}
	
	animation.paused = function() {
		return paused;
	}
	
	animation.rate = function(_) {
		if(_)
			rate = _;
		else
			return rate;
	}
	
	// skip to next AVL
	animation.next = function() {
		updateToIndex(currentIndex+1);
	}
	
	// previous AVL
	animation.prev = function() {
		// don't actually want to go *back* an index, just restart this one.
		updateToIndex(currentIndex);
	}
		
	function updateToIndex(i) {
		if (i > vehicle.data.length - 1)
			i = vehicle.data.length - 1;
		if (i < 0)
			i = 0;
		
		currentIndex = i; //+= 1;
		lineDone = 0;
		var avl = vehicle.data[currentIndex];
		elapsedTime = avl.timenum;
		
		// update GUI if tick won't.
		if (paused) {
			icon.setLatLng(avl);
			icon.update();
			clock.text(parseTime(elapsedTime));
		}
	}
	
	
	return animation;
}
				
function parseTime(x) {
	return new Date(x).toTimeString().slice(0, 8);
}
	

