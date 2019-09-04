/**
 * Options that effect how routes, stops, and vehicles are drawn
 */

var shapeOptions = {
	color: '#0080FF',
	weight: 7,
	opacity: 0.75,
	lineJoin: 'round'
};
		
var minorShapeOptions = {
	color: '#0080FF',
	weight: 4,
	opacity: 0.5,
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
    radius: 6,
    weight: 2,
    fillColor: '#0080FF',
    fillOpacity: 0.8,		
}

var minorStopOptions = {
    color: '#092F87',
    opacity: 0.2,
    radius: 2,
    weight: 1,
    fillColor: '#092F87',
    fillOpacity: 0.2,
    clickable: false
};

var busIcon = L.icon({
    iconUrl: 'images/bus-24.png',
    iconRetinaUrl: 'images/bus-24@2x.png',
    iconSize: [24, 24],
    iconAnchor: [13, 12],
    popupAnchor: [0, -12],
});

var streetcarIcon = L.icon({
    iconUrl: 'images/rail-light-24.png',
    iconRetinaUrl: 'images/rail-light-24@2x.png',
    iconSize: [24, 24],
    iconAnchor: [12, 12],
    popupAnchor: [0, -12],
});

var subwayIcon = L.icon({
    iconUrl: 'images/rail-metro-24.png',
    iconRetinaUrl: 'images/rail-metro-24@2x.png',
    iconSize: [24, 24],
    iconAnchor: [12, 12],
    popupAnchor: [0, -12],
});

var railIcon = L.icon({
    iconUrl: 'images/rail-24.png',
    iconRetinaUrl: 'images/rail-24@2x.png',
    iconSize: [24, 24],
    iconAnchor: [13, 12],
    popupAnchor: [0, -12],
});

var ferryIcon = L.icon({
    iconUrl: 'images/ferry-24.png',
    iconRetinaUrl: 'images/ferry-24@2x.png',
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
	opacity: 0.8,		
};

var minorVehicleMarkerOptions = {
	opacity: 0.3,		
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
    fillOpacity: 0.80,				
};

var minorVehicleMarkerBackgroundOptions = {
    radius: 12,
    weight: 0,
    fillColor: '#ffffff',
    fillOpacity: 0.3,				
};

var unassignedVehicleMarkerBackgroundOptions = {
	    radius: 10,
	    weight: 0,
	    fillColor: '#ffffff',
	    fillOpacity: 0.6,				
};

var vehiclePopupOptions = {
	offset: L.point(0,-2), 
	closeButton: false
};

var stopPopupOptions = {
	offset: L.point(0, -0),
	closeButton: false
}

var tripPatternPopupOptions = {
	closeButton: false
}

