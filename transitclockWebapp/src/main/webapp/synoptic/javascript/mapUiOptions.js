/**
 * Options that effect how routes, stops, and vehicles are drawn
 */

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
    iconSize: [25, 25],
    iconAnchor: [13, 13],
    popupAnchor: [0, -13],
});

var streetcarIcon = L.icon({
    iconUrl: 'images/rail-light-24.png',
    iconRetinaUrl: 'images/rail-light-24@2x.png',
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
    fillColor: '#1e3f78',
    fillOpacity: 1.0,				
};

var secondaryVehicleMarkerBackgroundOptions = {
    radius: 12,
    weight: 0,
    fillColor: '#1e3f78',
    fillOpacity: 0.80,				
};

var minorVehicleMarkerBackgroundOptions = {
    radius: 12,
    weight: 0,
    fillColor: '#1e3f78',
    fillOpacity: 0.3,				
};

var unassignedVehicleMarkerBackgroundOptions = {
	    radius: 10,
	    weight: 0,
	    fillColor: '#F0FA39',
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

