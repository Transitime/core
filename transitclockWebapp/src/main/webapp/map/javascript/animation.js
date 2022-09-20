// D3-style object to create and control an animation of the AVL
// data for a particular vehicle.
// map : map or group where animation will be added.
// clock: DOM object where current time should be updated.
// icon: Leaflet icon which will be animated.
function avlAnimation(map, icon, clock) {
	
	var startTime, endTime, rate, currentIndex, elapsedTime,
		lastTime, lineDone, paused, durations, sprite, positions, end;
	
	var ready = false;


	// create icon for animation and initialize values
	// positions is an array of position values: { lat, lon, timestamp }
	function popupData(data){
		var avl = data;
		var labels = ["Vehicle", "GPS Time", "Time Proc", "Lat/Lon", "Speed", "Heading", "Assignment ID", "Route", "Headsign", "Schedule Adherence", "OTP", "Headway Adherence"],
			keys = ["vehicleId", "time", "timeProcessed", "latlon", "niceSpeed", "heading", "assignmentId", "routeShortName", "headsign", "schedAdh", "otp", "headway"];

		var content = $("<div />").attr("class","card");
		content.append('<div class="card-header header-theme">Vehicle</div>')
		var table = $("<div />").attr("class", "card-body");

		for (var i = 0; i < labels.length; i++) {
			var label = $("<b />").text(labels[i] + ": ");
			var value = $("<div />").attr("class", "vehicle-value").text('N/A');

			var text = avl[keys[i]];
			if(text){
				var key = keys[i];
				if(key == 'schedAdh'){
					text += ' Minutes';
				}
				value = $("<div />").attr("class", "vehicle-value").text(text);
			}


			table.append( $("<div />").attr("class", "vehicle-item").append(label, value) )
		}

		// Links to schedule and google maps for vehicle
		var links = $("<div />")

		var mapsLink = 'http://google.com/maps?q=loc:' + avl.lat + ',' + avl.lon

		links.append( $("<a data-toggle='modal' href='#schedule-modal' class='list-group-item list-group-item-action secondary-btn' onclick='scheduleAjax(" + avl['tripId'] + "); return false;'>Schedule</a>"))
		// links.append( $("<div style='border-left:2px solid black;height:20px;display:inline-block;vertical-align:middle'></div>"))
		links.append( $("<a href=" + mapsLink + " target='_blank' class='list-group-item list-group-item-action secondary-btn' >View Location in Google Maps</a>"))

		var links2 = $("<div />")

		links2.append( $("<a href='#'  id='" + avl.vehicleId + "' onclick='playAnimation(" + avl.vehicleId + ")' class='list-group-item list-group-item-action play-back-popup-btn' >Hide Other Vehicles</a>"))

		content.append(table)
		content.append(links)
		content.append(links2)
		return content;
	}

	function animation(data) {
		
		positions = data
		
		ready = true;
		startTime = positions[0].timestamp;
		endTime = positions[positions.length-1].timestamp;
		rate = 1;
	
		currentIndex = 0; // this means we're going to 1
	
		elapsedTime = positions[0].timestamp,
			lastTime = 0,
			lineDone = 0;
	
		paused = true;
	
		durations = []
		for (var i = 0; i < positions.length - 1; i++)
			durations.push(positions[i+1].timestamp - positions[i].timestamp);

		/*var content = $("<div />").attr("class","card");
		content.append('<div class="card-header header-theme">Vehicle</div>')




		var label = $("<b />").text("Id: ");
		var value = $("<div />").attr("class", "vehicle-value").text(data[0].vehicleId);

		var table = $("<div />").attr("class", "card-body");
		table.append( $("<div />").attr("class", "vehicle-item").append(label, value) )

		var links = $("<div />")

		links.append( $("<a href='#'  id='" + data[0].vehicleId + "' onclick='playAnimation(" + data[0].vehicleId + ")' class='list-group-item list-group-item-action play-back-popup-btn' >Hide Other Vehicles</a>"))


		content.append(table)
		content.append(links)
*/
		var content = popupData(data[0]);
		/* var popupContent = $("<div />");
		var popupTable = $("<table />").attr("class", "popupTable");

		var vehicleIdLabel = $("<td />").attr("class", "popupTableLabel").text("Vehicle ID: ");
		var vehicleIdValue = $("<td />").text(data[0].vehicleId);
		var playbackLink = $("<td><a href='#' onclick='playAnimation(" + data[0].vehicleId + ")'>Playback</a></td>");

		popupTable.append( $("<tr />").append(vehicleIdLabel, vehicleIdValue) );
		popupTable.append( $("<tr />").append(playbackLink));
		popupContent.append(popupTable); */

		// Add a arrow to indicate the heading of the vehicle
		var headingArrow = L.rotatedMarker(positions[0])
			.setIcon(arrowIcon).addTo(map);

		headingArrow.options.angle = data[0].heading;
		headingArrow.setLatLng(positions[0]);
		// If heading is NaN then don't show arrow at all
		if (isNaN(parseFloat(data[0].heading))) {
			headingArrow.setOpacity(0.0);
		}

    var vehicleBackground = L.circleMarker(positions[0],
			getVehicleMarkerBackgroundOptions(data[0])).addTo(
			map);
		//console.log('positions[currentIndex].heading', positions[0].heading)

		sprite = L.marker(positions[0], {icon: icon}).bindPopup(content[0]).addTo(map);
		sprite.headingArrow = headingArrow;
		sprite.background = vehicleBackground;
		sprite.setZIndexOffset(400);
		clock.textContent = parseTime(elapsedTime);



	}

	/**
	 * Determines options for drawing the vehicle background circle based on uiType
	 */
	function getVehicleMarkerBackgroundOptions(vehicleData) {
		// Handle unassigned vehicles
		var vehicleMarkerBackgroundOptions = {
			radius: 15,
			weight: 0,
			fillColor: '#1E3F78',
			fillOpacity: 1,
			pane: "markerPane"
		};

		if (vehicleData.otp === "on-time")
			vehicleMarkerBackgroundOptions.fillColor = '#37E627';
		else if (vehicleData.otp === "early")
			vehicleMarkerBackgroundOptions.fillColor = '#E34B71';
		else if (vehicleData.otp === "late")
			vehicleMarkerBackgroundOptions.fillColor = '#E6D83E'

		return vehicleMarkerBackgroundOptions;
	}

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
			
			if (currentIndex == positions.length - 1) {
				if (end)
					end()
				currentIndex = 0;
				paused = true;
				return;
			}
			//console.log('positions[currentIndex].heading', positions[currentIndex].heading)
			sprite.setLatLng(positions[currentIndex])
			sprite.headingArrow.options.angle = positions[currentIndex].heading;
			sprite.headingArrow.setLatLng(positions[currentIndex]);
			sprite.headingArrow.update();

			sprite._popup.setContent(popupData(positions[currentIndex])[0]);
			sprite.background.setLatLng(positions[currentIndex])
			sprite.background.options.fillColor= getVehicleMarkerBackgroundOptions(positions[currentIndex]).fillColor;
			sprite.setZIndexOffset(400);
			sprite.background._updateStyle();


			sprite.update()
			elapsedTime = positions[currentIndex].timestamp
		}
		else {
			var pos = interpolatePosition(positions[currentIndex], positions[currentIndex+1], durations[currentIndex], lineDone)
			sprite.setLatLng(pos)
			//console.log('positions[currentIndex].heading', positions[currentIndex].heading)
			sprite.headingArrow.options.angle = positions[currentIndex].heading;
			sprite.headingArrow.setLatLng(pos);
			sprite._popup.setContent(popupData(positions[currentIndex])[0]);
			sprite.headingArrow.update();

			sprite.background.setLatLng(pos);
			sprite.background.options.fillColor= getVehicleMarkerBackgroundOptions(positions[currentIndex]).fillColor;
			sprite.background._updateStyle()
			sprite.update()
			
		}
		clock.textContent = parseTime(elapsedTime);
		
		if (!paused)
			requestAnimationFrame(tick)
	}
	
	animation.ready = function() {
		return ready;
	}
	
	animation.start = function() { 
		lastTime = Date.now();
		paused = false;
		tick();
	}
	
	animation.pause = function() {
		paused = true;
	}
	
	animation.paused = function() {
		return paused;
	}
	
	animation.onEnd = function (_) {
		end = _;
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
		// In most cases, we don't actually want to go *back* an index, just
		// restart this one. Exception: if we are less than 500ms (in realtime)
		// into this avl.
		
		var delta = elapsedTime - positions[currentIndex].timestamp;
		if (delta/rate < 500)
			updateToIndex(currentIndex-1);
		else
			updateToIndex(currentIndex);
	}
	
	// find next AVL that has a different lat/lng
	animation.advance = function() {
		var pos = positions[currentIndex]
		var nextIndex = currentIndex + 1; 
		while(nextIndex < positions.length) {
			var next = positions[currentIndex];
			if (pos.lat != next.lat || pos.lon != next.lon)
				break;
			nextIndex++;
		}
		updateToIndex(nextIndex);
		//console.log(nextIndex)
	}


	// clean up icon
    animation.removeIcon = function() {
        // remove old sprite.
        if (sprite){
			map.removeLayer(sprite.headingArrow);
			map.removeLayer(sprite);
		}

    }

	// clean up icon
	animation.setOpacityIcon = function(value) {
		// remove old sprite.
		if (sprite){
			sprite.setOpacity(value);
			sprite.headingArrow.setOpacity(value);
		}
		if(value === 1){
			// sprite.setZIndexOffset(400)
			sprite._zIndex = 400
		} else{
			// sprite.setZIndexOffset(200)
			sprite._zIndex = 200
		}

		sprite.update();

	}

    animation.addIcon = function() {
		map.addLayer(sprite);
		map.addLayer(sprite.headingArrow);
	}
		
	function updateToIndex(i) {
		if (i > positions.length - 1)
			i = positions.length - 1;
		if (i < 0)
			i = 0;
		
		currentIndex = i; //+= 1;
		lineDone = 0;
		var avl = positions[currentIndex];
		elapsedTime = avl.timestamp;
		
		// update GUI if tick won't.
		// if (paused) {
			sprite.setLatLng(avl);

			sprite.headingArrow.options.angle = positions[currentIndex].heading;
			//console.log('positions[currentIndex].heading', positions[currentIndex].heading)
			sprite.headingArrow.setLatLng(avl);
			sprite.headingArrow.update();

			sprite.background.setLatLng(positions[currentIndex])
			sprite.background.options.fillColor= getVehicleMarkerBackgroundOptions(positions[currentIndex]).fillColor;
			sprite.background._updateStyle();

			sprite.setZIndexOffset(400);

			sprite.update();
			console.log('elapsedTime .....',parseTime(elapsedTime));
			clock.textContent = parseTime(elapsedTime);
		// }
	}
	
	function parseTime(x) {
		return new Date(x).toTimeString().slice(0, 8);
	}
	
	// taken from leafletMovingMarker.js
	var interpolatePosition = function(p1, p2, duration, t) {
	    var k = t/duration;
	    k = (k>0) ? k : 0;
	    k = (k>1) ? 1 : k;
	    return L.latLng(p1.lat + k*(p2.lat-p1.lat), p1.lon + k*(p2.lon-p1.lon));
	};
	

	return animation;
}
				
	