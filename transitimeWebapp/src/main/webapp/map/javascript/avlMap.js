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
			animation.stop();
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

$("#playbackPrev").on("click", function() {
	if (animation)
		animation.stop();
	//vehicleGroup.clearLayers();
	animation = makeAnimation(vehicles);
	animation.setup();
	
	// change button back to play
	$("#playbackPlay").attr("src", playButton);
	
	$("#playbackRate").text("1X");
});

$("#playbackNext").on("click", function() {
	// create animation if it doesn't exist, or stop it.
	if (animation)
		animation.stop()
	else {
		animation = makeAnimation(vehicles)
		animation.setup()
	}
	
	// move marker to end
	for (var i = 0; i < vehicles.length; i++) {
		var marker = vehicles[i].animation,
			positions = vehicles[i].data,
			last = positions[positions.length-1];
		
		marker.setLatLng(last);
		
		// change clock time
		$("#playbackTime").text(parseTime(last.timenum))
	}
	
	// change button back to play
	$("#playbackPlay").attr("src", playButton);
})

$("#playbackPlay").on("click", function() {
	if (animation === undefined) {
		//vehicleGroup.clearLayers();
		animation = makeAnimation(vehicles);
		animation.setup();
	}
	
	if (animation.paused()) {
		animation.resume();
		$("#playbackPlay").attr("src", pauseButton);
	}
	else if (animation.started()) {
		animation.pause()
		// change button icon back to play
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

//This is a factory function to return a (closure style) object to animate the clock,
//with start, pause, and a rate getter/setter.
function makeAnimation(vehicles) {
	
	var startTimes = vehicles.map(function(v) { return (v.data[0].timenum) });
	var startTime = Math.min.apply(null, startTimes);
	var endTimes = vehicles.map(function(v) { return (v.data[v.data.length-1].timenum) });
	var endTime = Math.max.apply(null, endTimes);
	
	var clock = $("#playbackTime");
	
	var animation = {},
		rate = 1;
	
	var baseTimes, elapsedTime;
	
	var started = false, paused = false;
	
	animation.setup = function() {
		
		// delete old animation
		animationGroup.clearLayers();
		
		started = false;
		paused = false;
		
		// Base times to use to calculate clock: AVL times. 
		baseTimes = vehicles[0].data.map(function (v) { return v.timenum }),
			elapsedTime = 0;
		
		for (var i = 0; i < vehicles.length; i++) {
			var vehicle = vehicles[i];
			var positions = vehicle.data.map(function(v) { return [v.lat, v.lon] });
			var durations = []
			for (var i = 0; i < vehicle.data.length-1; i++) {
				var d = (vehicle.data[i+1].timenum - vehicle.data[i].timenum)/rate;
				durations.push(d);
			}
			
			vehicle.animation = L.Marker.movingMarker(positions, durations, {
				icon:  L.icon({
				    iconUrl:  contextPath + "/reports/images/bus.png", 
				    iconSize: [25,25]
				}),
				zIndexOffset: 1000
			}).addTo(animationGroup)
							
			vehicle.animation.on("end", function() {
				pause = true;
				// set play button back to play
				$("#playbackPlay").attr("src", playButton);
			})
		}
		
		// Synchronize clock to first vehicle.
		vehicles[0].animation.on("tick", function(tick) {
			var baseTime = baseTimes[tick.currentIndex];
			elapsedTime = tick.elapsedTime;
			var currTime = baseTime + tick.elapsedTime * rate;
			if (!paused)
				clock.text(parseTime(currTime));
		})
		
		clock.text(parseTime(baseTimes[0]))
	}
	
	animation.start = function() {
		started = true, paused = false;
		for (var i = 0; i < vehicles.length; i++)
			vehicles[i].animation.start();
	}
	
	animation.resume = function() {
		paused = false;
		for (var i = 0; i < vehicles.length; i++)
			vehicles[i].animation.resume();
	}
	
	animation.pause = function() {
		paused = true;
		var referenceAnim = vehicles[0].animation;
		baseTimes[referenceAnim._currentIndex] += elapsedTime * rate;
		for (var i = 0; i < vehicles.length; i++)
			vehicles[i].animation.pause();
	}
	
	animation.stop = function() {
		for (var i = 0; i < vehicles.length; i++)
			vehicles[i].animation.stop();
	}
	
	animation.rate = function(_) {
		if (_) {
			var oldrate = rate;
			rate = _;
			animation.pause();
			var delta = oldrate / rate;
			for (var i = 0; i < vehicles.length; i++) {
				var durations = vehicles[i].animation._durations;
		        for (var j = 0; j < durations.length; j++) 
					durations[j] *= delta;
		        vehicles[i].animation._currentDuration *= delta;
			}
			animation.resume();
		}
		else
			return rate;				
	}
	
	animation.paused = function() { return paused; };
	animation.started = function() { return started; };
	
	return animation;
}
				
function parseTime(x) {
	return new Date(x).toTimeString().slice(0, 8);
}
	

