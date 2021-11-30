
/* For drawing the route and stops */
var routeOptions = {
	color: '#00ee00',
	weight: 4,
	opacity: 1,
	lineJoin: 'round',
	clickable: false
};
				
 var stopOptions = {
    color: '#006600',
    opacity: 1,
    radius: 4,
    weight: 2,
    fillColor: '#006600',
    fillOpacity: 1,
};

var routePolylineOptions = {clickable: false, color: "#00f", opacity: 0.5, weight: 4};

var stopPopupOptions = {closeButton: false};

function drawAvlMarker(avl) {
	var latLng = L.latLng(avl.lat, avl.lon);

	// Set avl keys and values
	var labels = ["Vehicle", "GPS Time", "Time Proc", "Lat/Lon", "Speed", "Heading", "Assignment ID", "Route", "Headsign", "Schedule Adherence", "OTP", "Headway Adherence"],
		keys = ["vehicleId", "time", "timeProcessed", "latlon", "niceSpeed", "heading", "assignmentId", "routeShortName", "headsign", "schedAdh", "otp", "headway"];

	// populate missing keys
	avl.latlon = avl.lat + ", " + avl.lon
	avl.niceSpeed =  Math.round(parseFloat(avl.speed) * 10)/10 + " kph";

	if (typeof avl['otp'] == 'undefined') {
		avl['otp'] = '';
	}

	// Create the marker. Use a divIcon so that can have tooltips. Set color according to OTP
  	var tooltip = avl.time.substring(avl.time.indexOf(' ') + 1);  
  	var avlMarker = L.rotatedMarker(avl, {
          icon: L.divIcon({
        	  className: 'avlMarker_',
        	  html: "<div class='avlTriangle" + avl['otp'] + "' />",
        	  iconSize: [7,7]
          }),
          angle: avl.heading,
          title: tooltip
      }).addTo(vehicleGroup); 
	
  	// Create popup with detailed info

	var content = $("<div />").attr("class","card");
	content.append('<div class="card-header header-theme">Vehicle</div>')
	var table = $("<div />").attr("class", "card-body");
	
	for (var i = 0; i < labels.length; i++) {
		var label = $("<b />").text(labels[i] + ": ");
		var value = $("<div />").attr("class", "vehicle-value").text('N/A');
		if(avl[keys[i]]){
			value = $("<div />").attr("class", "vehicle-value").text(avl[keys[i]]);
		}
		table.append( $("<div />").attr("class", "vehicle-item").append(label, value) )
	}

	// Links to schedule and google maps for vehicle
	var links = $("<div />")

	var mapsLink = 'http://google.com/maps?q=loc:' + avl.lat + ',' + avl.lon

	links.append( $("<a data-toggle='modal' href='#schedule-modal' class='list-group-item list-group-item-action secondary-btn' onclick='scheduleAjax(" + avl['tripId'] + "); return false;'>Schedule</a>"))
	// links.append( $("<div style='border-left:2px solid black;height:20px;display:inline-block;vertical-align:middle'></div>"))
	links.append( $("<a href=" + mapsLink + " target='_blank' class='list-group-item list-group-item-action' >View Location in Google Maps</a>"))

	content.append(table)
	content.append(links)
		
	avlMarker.bindPopup(content[0]);
  	
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
    var vehicles = [];
    
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
	    		L.polyline(latLngsForVehicle, routePolylineOptions).addTo(vehicleGroup); //.bringToBack();
	    	latLngsForVehicle = [];
	    	vehicle = {id: avl.vehicleId, data: []}
	    	vehicles.push(vehicle);
		} 
		
    	// parse date string -> number
    	avl.timestamp = Date.parse(avl.time.replace(/-/g, '/').slice(0,-2))
    	
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
  	} else {
		alert("No AVL data for the criteria specified.")
  	}
	return vehicles;
}

function routeConfigCallback(data, status) {
	// Draw the paths for the route
	if (map._loaded) {
		routeConfigCallback2(data, status);
	} else{
		map.on("load",  routeConfigCallback2(data, status));
	}
}

/**
 * Reads in route data obtained via AJAX and draws route and stops on map.
 */

function routeConfigCallback2(data, status) {
	// Draw the paths for the route
	var route = data.routes[0];
	var locsToFit = [];


	for (var i=0; i<route.shape.length; ++i) {
		var shape = route.shape[i];

		var routeOptions2 = JSON.parse(JSON.stringify(routeOptions));
		routeOptions2.color = '#'+route.color;
		routeOptions2.fillColor = '#'+route.textColor;

		L.polyline(shape.loc, routeOptions2).addTo(routeGroup);
	}

	// Draw stops for the route.
	for (var i=0; i<route.direction.length; ++i) {
		var direction = route.direction[i];
		for (var j=0; j<direction.stop.length; ++j) {
			var stop = direction.stop[j];

			var stopOptions2 = JSON.parse(JSON.stringify(stopOptions));

			stopOptions2.radius= 4;
			stopOptions2.color = '#'+route.color;
			stopOptions2.fillColor = '#'+route.textColor;

			locsToFit.push(L.latLng(stop.lat, stop.lon));
			// Create the stop Marker
			var stopMarker = L.circleMarker([stop.lat,stop.lon], stopOptions2).addTo(routeGroup);

			// Create popup for stop marker

			var content = $("<div />").attr("class", "card");
			var labels = ["Stop ID"], keys = ["id"];
			content.append('<div class="card-header header-theme">'+ stop["name"]+'</div>')
			var content2 = $("<div />").attr("class", "card-body");
			for (var k = 0; k < labels.length; k++) {
				var label = $("<b />").text(labels[k] + ": ")
				var value = $("<div />").attr("class","vehicle-value").text(stop[keys[k]]);
				content2.append( $("<div />").attr("class", "vehicle-item").append(label, value) );
			}
			content.append(content2);
			stopMarker.bindPopup(content[0]);
		}
	}

	if (locsToFit.length > 0) {
		map.fitBounds(locsToFit);
	}
}

// Data in vehicles will be available as CSV when you click the `export' link.
// CSV should be the AVL CSV format used elsewhere in Transitime.
// org.transitclock.avl.AvlCsvWriter writes the following header:
// vehicleId,time,justTime,latitude,longitude,speed,heading,assignmentId,assignmentType
// org.transitclock.avl.AvlCsvRecord has required keys vehicleId, time, latitude, longitude
// all others optional
function createExport(vehicles) {
	
	var data = vehicles[0].data
  	
  	// set keys
	var keys = ["vehicleId", "time", "latitude", "longitude", "speed", "heading", "assignmentId, routeShortName"]
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
var mapTileUrl ='http://tile.openstreetmap.org/{z}/{x}/{y}.png'
var map = L.map('map');
L.control.scale({metric: false}).addTo(map);
L.tileLayer(mapTileUrl, {
 attribution: '&copy; <a href="http://openstreetmap.org">OpenStreetMap</a> &amp; <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>',
 maxZoom: 19
}).addTo(map);

//fit map to agency boundaries.
$.getJSON(apiUrlPrefix + "/command/agencyGroup", function(agencies) {
	var e = agencies.agency[0].extent;
	map.fitBounds([[e.minLat, e.minLon], [e.maxLat, e.maxLon]]);
})
.fail(function(request, status, error) {
  alert(error + '. ' + request.responseText);
});


var vehicleGroup = L.layerGroup().addTo(map);
var routeGroup = L.layerGroup().addTo(map);
var animationGroup = L.layerGroup().addTo(map);

//Set the CLIP_PADDING to a higher value so that when user pans on map
//the route path doesn't need to be redrawn. Note: leaflet documentation
//says that this could decrease drawing performance. But hey, it looks
//better.
L.Path.CLIP_PADDING = 0.8;0

if (request.v || request.r) {
	// Request exists; set all the controls to match the values in the request.
	$("#vehicle").val(request.v).trigger("change");
	$("#beginDate").val(request.beginDate).trigger("change");
	$("#numDays").val(parseInt(request.numDays)).trigger("change");
	$("#beginTime").val(request.beginTime).trigger("change");
	$("#endTime").val(request.endTime).trigger("change");
	$("#route").val(request.r).trigger("change");
	
	//draw vehicles if there is already request information
	if (request.v) {
		drawAvlData();
	}
	if (request.r && request.r != "")
		drawRoute(request.r);
	
}
else {
	// no request
	// set beginDate and endDate to defaults
	request.beginDate = $("#beginDate").val()
	request.numDays = $("#numDays").val()
	if($("#route").val())
	{
		drawRoute($("#route").val());
	}
}

// draw route data when dropdown is selected
$("#route").change(function(evt) { drawRoute(evt.target.value) });

// draw AVL data when submit button is clicked
$("#submit").on("click", function() {

	if ($("#early").val() > 1440) {
		$("#early").val(1440);
	}
	if ($("#early").val() == "" || $("#early").val() < 0) {
		$("#early").val(1.5)
	}
	if ($("#late").val() > 1440) {
		$("#late").val(1440);
	}
	if ($("#late").val() == "" || $("#late").val() < 0) {
		$("#late").val(2.5)
	}
    /* Set request object to match new values */
    request.v = $("#vehicle").val();
    request.beginDate = $("#beginDate").val();
    request.beginTime = $("#beginTime").val();
    request.endTime = $("#endTime").val();
    request.early = $("#early").val() * 60000;
    request.late = $("#late").val() * -60000;
    request.r = $("#route").val();
    request.includeHeadway = "true";

    var askConfirm = allVehiclesRequested() && (request.r == "All Routes" || request.r ==" " );
    var confirmYes = false;
    if (askConfirm) {
        confirmYes = confirm('Are you sure you want All Vehicles and All Routes?');
    }

    if(!request.r){
    	$("#route-error-display").removeClass("d-none")
    	return true;
	}
	$("#route-error-display").addClass("d-none")
    //go ahead if no confirm needed or if the confirm was a yes
    if (!askConfirm || confirmYes) {
		// Clear existing layer and draw new objects on map.
		drawAvlData();
	}
});

function allVehiclesRequested() {
	return request.v == "All Vehicles" || request.v == "";
}


//Get the AVL data via AJAX and call processAvlCallback to draw it
function drawAvlData() {

    $("#submit").attr("disabled","disabled");
	$("#exportData").addClass("d-none")
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
	    success: function(resp) {
            $("#submit").removeAttr("disabled");
	    	vehicleGroup.clearLayers();
	    	var vehicles = processAvlCallback(resp);
	    	if(vehicles.length){
				// connect export to link to csv creation.
				createExport(vehicles);

				$("#exportData").removeClass("d-none")
			}


	    	for (i in animations) {
	    		animations[i].removeIcon();

			}
	    	animations = {};
			$("img.leaflet-clickable").remove()
	    	if (!allVehiclesRequested() && vehicles.length)
	    		prepareAnimations(vehicles); // only animate first vehicle returned.

			if (allVehiclesRequested())
				prepareAnimations(vehicles);

			else $("#playback").show();
	    },
	    // When there is an AJAX problem alert the user
	    error: function(request, status, error) {
            $("#submit").removeAttr("disabled");
	      alert(error + '. ' + request.responseText);
	    },
	});
}

function drawRoute(route) {
	routeGroup.clearLayers();
	if (route != "") {
		var url = apiUrlPrefix + "/command/routesDetails?r=" + route;
		$.getJSON(url, routeConfigCallback);
	}
}

/* Animation controls */

var busIcon = L.icon({
	iconUrl: '/web/maps/images/bus-24.png',
	iconRetinaUrl: '/web/maps/images/bus-24.png',
	iconSize: [25, 25],
	iconAnchor: [13, 13],
	popupAnchor: [0, -13],
});

var busBullet = L.icon({
	iconUrl: '/web/maps/images/bus-bullet.png',
	iconRetinaUrl: '/web/maps/images/bus-bullet.png',
	iconSize: [25, 25],
	iconAnchor: [13, 13],
	popupAnchor: [0, -13],
});

var animation = avlAnimation(animationGroup, busIcon, $("#playbackTime")[0]);
var animations = {};

var playButton = contextPath + "/reports/images/playback/media-playback-start.svg",
	pauseButton = contextPath + "/reports/images/playback/media-playback-pause.svg";

animation.onEnd(function() {
	$("#playbackPlay").attr("src", playButton);
})

// Given a list of AVL positions, initialize the animation object.
function prepareAnimations(avlsData) {

	// Make sure animation controls are in their initial state.
	$("#playbackPlay").attr("src", playButton);
	$("#playbackRate").text("1X");

	for (i in avlsData) {
		if (i == 0) {
			animation(avlsData[i].data);
			animations[avlsData[i].id] = animation;
		}
		else {
			newAnimation = avlAnimation(animationGroup, busIcon, $("#playbackTime")[0]);
			newAnimation(avlsData[i].data)
			animations[avlsData[i].id] = newAnimation;
		}
	}
}

function playAnimations() {

	if (!animation.paused()) {
		for (i in animations) {
			animations[i].pause();
		}

		$(".play-back-popup-btn").html("Hide Other Vehicles");
		$("#playbackPlay").attr("src", playButton);
	}
	else { // need to start it
		for (i in animations) {
			animations[i].start();
		}

			$(".play-back-popup-btn").html("Show Other Vehicles");

		$("#playbackPlay").attr("src", pauseButton);
	}
}

function playAnimation(vehicleId) {
	for (i in animations) {
		if (!animation.paused()) {
			// animations[i].addIcon();
			animations[i].setOpacityIcon(1);
		} else{
			if (i != vehicleId) {
				// animations[i].removeIcon();
				animations[i].setOpacityIcon(0.3);
			}
		}
	}

	playAnimations(vehicleId);
}

$("#playbackNext").on("click", function() {
	for (i in animations) {
		animations[i].next();
	}
});

$("#playbackPrev").on("click", function() {
	for (i in animations) {
		animations[i].prev();
	}
});

$("#playbackPlay").on("click", function() {
	for (i in animations) {
		animations[i].addIcon();
	}
	playAnimations();
});

$("#playbackFF").on("click", function() {
	var rate = animation.rate()*2;
	for (i in animations) {
		animations[i].rate(rate);
	}
	$("#playbackRate").text(rate + "X");
});

$("#playbackRew").on("click", function() {
	var rate = animation.rate()/2;
	for (i in animations) {
		animations[i].rate(rate);
	}
	$("#playbackRate").text(rate + "X");
});

function scheduleAjax(tripId) {

	if ($("#schedule-modal").length == 0) {
		var containerHeight = $(".leaflet-popup-content").height();
		var scheduleModal =
			$("<div class='modal' id='schedule-modal' style='display: none;z-index: -1;width: 200%;height: " + containerHeight + "px;overflow: auto;background-color: rgb(255,255,255);'>"
				+ "<div class='modal-content' style='z-index: -1;'>"
					+ "<div class='modal-header'>"
						+ "<button type='button' class='close-modal' style='float:right;'>&times;</button>"
					+ "</div>"
					+ "<div class='modal-body'>"
					+ "</div>"
					+ "<div class='modal-footer' style='text-align: center;'>"
						+ "<button type='button' class='btn btn-default close-modal' style='margin: 10px auto;'>Close</button>"
					+ "</div>"
				+ "</div>"
			+ "</div>");

		$(".leaflet-popup-content-wrapper").append(scheduleModal);
	}

	$.ajax({
			// The page being requested
			url: apiUrlPrefix + "/command/scheduleTripVertStops",
			// Pass in query string parameters to page being requested
			data: {t: tripId, a: 1},
			// Needed so that parameters passed properly to page being requested
			traditional: true,
			dataType:"json",
			success: dataReadCallback
	})
}

function dataReadCallback(jsonData) {
	// Set the title now that have the trip name from the API
	if ($('.modal-title').length > 0) {
		$('.modal-title').html("<h4 class='modal-title'>Schedule for trip " + jsonData.schedule[0].trip[0].tripShortName + "</h4>");
	} else {
		($('.modal-header').append("<h4 class='modal-title'>Schedule for trip " + jsonData.schedule[0].trip[0].tripShortName + "</h4>"));
	}

	// Only one schedule
	var schedule = jsonData.schedule[0];

	// Create title for schedule
	$('.modal-body').html("<div id='scheduleTitle'>"
		+ "Direction: " + schedule.directionId
		+ ", Service: " + schedule.serviceName
		+ "</div>");

	if ($('#dataTable').length > 0) {
		var table = $('.modal-body')[0].innerHTML($("<table id='dataTable'></table>"));
	} else {
		var table = $("<table id='dataTable'></table>").appendTo('.modal-body')[0];
	}

	// Create the columns. First column is stop name. And then there
	// is one column per trip.
	var headerRow = table.insertRow(0);
	headerRow.insertCell(0).id = 'headerCell';

	var trip = schedule.trip[0];
	var tripName = trip.tripShortName;
	if (tripName == null)
		tripName = trip.tripId;
	var tripNameTooLong = tripName.length > 6;
	var html = tripNameTooLong ?
		"Block<br/>" + trip.blockId : "Trip<br/>" + tripName;

	var headerCell = headerRow.insertCell(1);
	headerCell.id = 'headerCell';
	headerCell.innerHTML = html;

	// Add data for each row for the schedule. This is a bit complicated
	// because the API provides data per trip but want each row in the
	// schedule to be for a particular stop for all trips.
	for (var stopIdx=0; stopIdx<schedule.timesForStop.length; ++stopIdx) {
		var row = table.insertRow(stopIdx+1);

		var timesForStop = schedule.timesForStop[stopIdx];

		// Add stop name to row
		var headerCell = row.insertCell(0);
		headerCell.id = 'stopCell';
		headerCell.innerHTML = timesForStop.stopName;

		// Add the times for the stop to the row
		for (var tripIdx=0; tripIdx<timesForStop.time.length; ++tripIdx) {
			var time = timesForStop.time[tripIdx];
			row.insertCell(tripIdx+1).innerHTML = time.timeStr ? time.timeStr : '';
		}
	}

	$("#schedule-modal")[0].style.display = "block";

	$(".close-modal").click(function() {
		$("#schedule-modal")[0].style.display = "none";
	})

	$("#schedule-modal").hover(function() {
			map.scrollWheelZoom.disable();
		}, function() {
			map.scrollWheelZoom.enable();
		}
	)
}


