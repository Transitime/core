<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page import="org.transitclock.web.WebConfigParams"%>
<%
    String agencyId = request.getParameter("a");
    if (agencyId == null || agencyId.isEmpty()) {
        response.getWriter().write("You must specify agency in query string (e.g. ?a=mbta)");
        return;
    }
%>
<html>
<head>
    <%@include file="/template/includes.jsp" %>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Real-time Operations</title>

    <link rel="stylesheet" href="//unpkg.com/leaflet@0.7.3/dist/leaflet.css" />
    <script src="//unpkg.com/leaflet@0.7.3/dist/leaflet.js"></script>
    <script src="<%= request.getContextPath() %>/javascript/jquery-dateFormat.min.js"></script>

    <script src="<%= request.getContextPath() %>/maps/javascript/leafletRotatedMarker.js"></script>
    <script src="<%= request.getContextPath() %>/maps/javascript/mapUiOptions.js"></script>

    <!-- Load in Select2 files so can create fancy selectors -->
    <link href="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/css/select2.min.css" rel="stylesheet" />
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;700&display=swap" rel="stylesheet">
    <script src="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/js/select2.min.js"></script>

    <link rel="stylesheet" href="<%= request.getContextPath() %>/maps/css/mapUi.css" />


    <%--        <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>--%>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/page-panels.css">
    <title>TransitClock Map</title>
</head>
<body class="real-time-live-map">
<%@include file="/template/header.jsp" %>
<div class="panel split">
    <div class="left-panel">
        <h4 class="page-title">
            Schedule Adherence
        </h4>
        <form class="row" novalidate>


            <div class="row mb-0">
                <label class="col-sm-12 col-form-label">Search</label>
            </div>

            <div class="row">
                <div class="col-sm-9">
                    <input type="text" class="form-control" id="search-realpage" placeholder="Vehicle">
                </div>
                <div class="col-sm-3 pad-left-0">
                    <button class="btn btn-primary submit-button "  type="button" value="show" onclick="showVehicle()">Show</button>
                </div>
            </div>

            <div class="row">
                <label class="col-sm-12 col-form-label">Routes</label>
                <input type="hidden" name="isAllRoutesDisabled"  class="isAllRoutesDisabled" value="true">
                <input type="hidden" name="a" value="<%= request.getParameter("a")%>">
                <jsp:include page="params/routeMultipleNoLabel.jsp" />
            </div>

        </form>
        <div class="list-group">
            <a  class="list-group-item list-group-item-action secondary-btn"
                href="realTimeLiveMap.jsp?a=<%= agencyId %>" >
                Live Map View
            </a>
            <button  class="list-group-item list-group-item-action">

                Schedule Adherence View
            </button>

            <a  class="list-group-item list-group-item-action secondary-btn"
                href="realTimeDispatcher.jsp?a=<%= agencyId %>" >
                Dispatcher View
            </a>
        </div>
    </div>
    <div class="right-panel">
        <div id="map"></div>

        <div class="map-legend-icons leaflet-popup-content ">
            <div class="card">
<%--                <div class="card-header header-theme">
                    <b>Schedule Marker Legend</b>
                </div>--%>

                <div class="card-body">
                    <ul class="list-group">
                        <li class="list-group-item d-flex align-items-center">
                            <span class="badge bg-green rounded-pill"> &nbsp;</span>
                            = &nbsp; On Time
                        </li>
                        <li class="list-group-item d-flex align-items-center">
                            <span class="badge bg-yellow rounded-pill">&nbsp;</span>
                            = &nbsp; Late
                        </li>
                        <li class="list-group-item d-flex  align-items-center">
                            <span class="badge bg-red rounded-pill">&nbsp;</span>
                            = &nbsp; Early
                        </li>
                    </ul>
                </div>

            </div>
        </div>
    </div>
</div>
</div>

<script>

    var routeOptions = {
        color: '#0080FF',
        weight: 1,
        opacity: 1.0,
        clickable: false
    };

    var vehiclePopupOptions = {
        offset: L.point(0, -2),
        closeButton: false
    };

    var map;

    // Global so can keep track and delete all vehicles at once
    var vehicleLayer;

    function formatRoute (route) {
        if (!route.id || route.id == " ") {
            return route.text;
        }
        return route.id;
    }

    function showVehicle(){
        getAndProcessSchAdhData($('#route').val(), $('#search-realpage').val())
    }
    function selectUnSelectCallBack(e){

        var configuredTitle = $( "#route" ).attr("title");

        $( "#select2-route-container" ).tooltip({ content: configuredTitle,
            position: { my: "left+10 center", at: "right center" } });

        var selectedDataList = $("#route").select2("data");
        var selectedRouteId = "";
        var selectedRouteValues = "";
        $(selectedDataList).each(function(index, eachList){
            selectedRouteId += "r=" + eachList.id + ($(selectedDataList).length-1 === index ? "": "&");
            selectedRouteValues += eachList.id + ($(selectedDataList).length-1 === index ? "": "&");
        });

        if (selectedRouteId.trim() != "") {
            var url = apiUrlPrefix + "/command/routesDetails?" + selectedRouteId;
            $.getJSON(url, routeConfigCallback);
        }   else  if(routeFeatureGroup && map){
            map.removeLayer(routeFeatureGroup);
        }
        getAndProcessSchAdhData(selectedRouteValues, $("#vehiclesSearch").val())
    }


    $.getJSON(apiUrlPrefix + "/command/routes?keepDuplicates=true",
        function (routes) {
            // Generate list of routes for the selector
            var selectorData = [{id: '', text: 'Select Route'}];
            for (var i in routes.routes) {
                var route = routes.routes[i];
                selectorData.push({id: route.shortName, text: route.name})
            }

            $("#route").select2({
                data : selectorData,
                placeholder: "Select Routes",
                templateSelection: formatRoute
            })
                // Need to reset tooltip after selector is used. Sheesh!
                .on("select2:select", selectUnSelectCallBack)
                .on("select2:unselect", selectUnSelectCallBack);


            /*  $("#route").select2({
                 data: selectorData
             }).on("select2:select", function (e) {
                 $.getJSON(apiUrlPrefix + "/command/routesDetails", routeConfigCallback);
                 getAndProcessSchAdhData($("#route").val(), $("#vehiclesSearch").val())
             }); */
        }
    );

    /**
     * Returns content for vehicle popup window. For showing vehicle ID and
     * schedule adherence.
     */
    function getVehiclePopupContent(vehicle) {
        var content = '<div class="card"><div class="card-header header-theme">';
        content +=  "<b>Vehicle:</b> " + vehicle.id +"</div>";

        content +=  "<div class='card-body'><div class='vehicle-item'><b >Route:</b> <div class='vehicle-value'> " + vehicle.routeName+"</div></div>";

            content += "<div class='vehicle-item'><b>To:</b> <div class='vehicle-value'>" + (vehicle.headsign || 'N/A' )+"</div></div>";

            content += "<div class='vehicle-item'><b>SchAhd:</b> <div class='vehicle-value'>" + (vehicle.schAdhStr  || 'N/A' )+"</div></div>";

            content += "<div class='vehicle-item'><b>Block:</b> <div class='vehicle-value'>" + ( vehicle.block  || 'N/A' )+"</div></div>";

            content += "<div class='vehicle-item'><b>Trip:</b> <div class='vehicle-value'>" + (vehicle.tripId  || 'N/A' )+"</div></div>";

            content += "<div class='vehicle-item'><b>Driver:</b> <div class='vehicle-value'>" + (vehicle.driver  || 'N/A')+"</div></div>";

        content += "</div></div></div>";

        return content;
    }

    /**
     * Reads in and processes schedule adherence data
     */
    function getAndProcessSchAdhData(routeId, vehicleId) {

        var selectedDataList = [];

        if( $("#route").val() != null ) {
            selectedDataList =  $("#route").select2("data");
        }


        var selectedRouteId = "";
        // var selectedRouteValues = "";
        $(selectedDataList).each(function(index, eachList){
            selectedRouteId += "&r=" + eachList.id + ($(selectedDataList).length-1 === index ? "": "&");
        });

        // Do API call to get schedule adherence data
        $.getJSON(apiUrlPrefix + "/command/vehiclesDetails?onlyAssigned=true"+ selectedRouteId,
            // Process data
            function (jsonData) {
                var newVehicleLayer = L.featureGroup();

                // Add new vehicles
                var len = jsonData.vehicles.length;
                while (len--) {
                    var vehicle = jsonData.vehicles[len];

                    // If no schedule adherence info for vehicle skip it
                    if (!vehicle.schAdh)
                        continue;

                    // Use different color for rim of circle depending on
                    // direction. This way can kind of see which direction
                    // vehicle is going in without needing an obtrusive
                    // arrow or such.
                    var directionColor = '#0a0';
                    if (vehicle.direction == "1")
                        directionColor = '#00f';

                    // For determining gradiation of how early/late a vehicle is
                    var earlyTime = 30000;  // 30 seconds
                    var maxEarly = 180000;  // 3 minutes
                    var lateTime = -120000; // 2 minutes
                    var maxLate = -960000;  // 16 minutes
                    var msecPerRadiusPixels = 33000;

                    // Determine radius and color for vehicle depending on how early/late it is
                    var radius;
                    var fillColor;
                    var fillOpacity;
                    if (vehicle.schAdh < lateTime) {
                        // Vehicle is late
                        radius = 5 - (Math.max(maxLate, vehicle.schAdh) - lateTime) / msecPerRadiusPixels;
                        fillColor = '#E6D83E';
                        fillOpacity = 0.5;
                    } else if (vehicle.schAdh > earlyTime) {
                        // Vehicle is early. Since early is worse make radius
                        // of larger by using msecPerRadiusPixels/2
                        radius = 5 + (Math.min(maxEarly, vehicle.schAdh) - earlyTime) / (msecPerRadiusPixels / 2);
                        fillColor = '#E34B71';
                        fillOpacity = 0.5;
                    } else {
                        // Vehicle is ontime
                        radius = 5;
                        fillColor = '#37E627';
                        fillOpacity = 0.5;
                    }

                    // Specify options on how circle is be drawn. Depends
                    // on how early/late a vehicle is
                    var vehicleMarkerOptions = {
                        // Specify look of the rim of the circle
                        color: directionColor,
                        weight: 1,
                        opacity: 1.0,

                        // Set size and color of circle depending on how
                        // early or late the vehicle is
                        radius: radius,
                        fillColor: fillColor,
                        fillOpacity: fillOpacity,
                    };

                    // Create circle for vehicle showing schedule adherence
                    var vehicleMarker =
                        L.circleMarker([vehicle.loc.lat, vehicle.loc.lon],
                            vehicleMarkerOptions);
                    newVehicleLayer.addLayer(vehicleMarker);

                    // Store vehicle data obtained via AJAX with stopMarker so it can be used in popup
                    vehicleMarker.vehicle = vehicle;

                    // Create popup window for vehicle when clicked on
                    vehicleMarker.on('click', function (e) {
                        openVehiclePopup(this);
                    });

                    if (vehicleId && vehicleId.trim() != "" && vehicle.id == vehicleId) {
                        openVehiclePopup(vehicleMarker);
                    }

                } // End of while loop

                // Remove old vehicles
                if (vehicleLayer)
                    map.removeLayer(vehicleLayer);

                // Add all the vehicles at once
                newVehicleLayer.addTo(map);

                // Remember the vehicle layer so can remove all the old
                // vehicles next time updating the map
                vehicleLayer = newVehicleLayer;
            });
    }

    /**
     * Reads in route data obtained via AJAX and draws route on map.
     */
    var routeFeatureGroup = null;
    function routeConfigCallback(routeData, status) {
        // So can make sure routes are drawn below the stops

        if(routeFeatureGroup && map){
            map.removeLayer(routeFeatureGroup);
        }
        routeFeatureGroup = L.featureGroup();
        var locsToFit = [];

        var routeOptions2 = JSON.parse(JSON.stringify((routeOptions)));
        routeOptions2.weight = 6;
        routeOptions2.color = "#1887fc ";
        routeOptions2.fillOpacity = 0.6;


        // For each route
        for (var r = 0; r < routeData.routes.length; ++r) {
            // Draw the paths for the route
            var route = routeData.routes[r];
            for (var i = 0; i < route.shape.length; ++i) {
                var shape = route.shape[i];
                var latLngs = [];
                for (var j = 0; j < shape.loc.length; ++j) {
                    var loc = shape.loc[j];
                    latLngs.push(L.latLng(loc.lat, loc.lon));
                    locsToFit.push(L.latLng(loc.lat, loc.lon));
                }
                var polyline = L.polyline(latLngs, routeOptions2);
                routeFeatureGroup.addLayer(polyline);
            }
        }

        // Add all of the paths and stops to the map at once via the FeatureGroup
        routeFeatureGroup.addTo(map);

        if (locsToFit.length > 0) {
            map.fitBounds(locsToFit);
        }

        // It can happen that vehicles get drawn before the route paths.
        // In this case need call bringToBack() on the paths so that
        // the vehicles will be drawn on top.
        // Note: bringToBack() must be called after map is first specified
        // via fitBounds() or other such method.
        routeFeatureGroup.bringToBack();
    }

    /**
     * Create map and zoom to extent of agency
     */
    function createMap(mapTileUrl, mapTileCopyright) {
        // Create map.
        map = L.map('map');
        L.control.scale({metric: false}).addTo(map);

        L.tileLayer(mapTileUrl, {
            // Specifying a shorter version of attribution. Original really too long.
            //attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery ? <a href="http://mapbox.com">Mapbox</a>',
            attribution: '&copy; <a href="http://openstreetmap.org">OpenStreetMap</a> &amp; <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery ?<%= WebConfigParams.getMapTileCopyright() %>',
            maxZoom: 19
        }).addTo(map);

        // Set the CLIP_PADDING to a higher value so that when user pans on map
        // the route path doesn't need to be redrawn. Note: leaflet documentation
        // says that this could decrease drawing performance. But hey, it looks
        // better.
        L.Path.CLIP_PADDING = 0.8;

        // Set map bounds to the agency extent
        $.getJSON(apiUrlPrefix + "/command/agencyGroup",
            function (agencies) {
                // Fit the map initially to the agency
                var e = agencies.agency[0].extent;
                map.fitBounds([[e.minLat, e.minLon], [e.maxLat, e.maxLon]]);
            });

        // Get route config data and draw all routes
        // $.getJSON(apiUrlPrefix + "/command/routesDetails", routeConfigCallback);

        // Start showing schedule adherence data and update every 10 seconds.
        // Updating every is more than is truly useful since won't get significant
        // change every 10 seconds, but it shows that the map is live and is
        // really cool.
        getAndProcessSchAdhData($("#route").val(), $("#vehiclesSearch").val());
        setInterval(function () {
            getAndProcessSchAdhData($("#route").val(), $("#vehiclesSearch").val())
        }, 10000);
    }

    function openVehiclePopup(vehicleMarker) {
        var content = getVehiclePopupContent(vehicleMarker.vehicle);
        var latlng = L.latLng(vehicleMarker.vehicle.loc.lat,
            vehicleMarker.vehicle.loc.lon);
        // Create popup and associate it with the vehicleMarker
        // so can later update the content.
        vehicleMarker.popup = L.popup(vehiclePopupOptions, vehicleMarker)
            .setLatLng(latlng)
            .setContent(content).openOn(map);
    }

    /**
     * When page finishes loading then create map
     */
    $(document).ready(function () {
        createMap('<%= WebConfigParams.getMapTileUrl() %>',
            '<%= WebConfigParams.getMapTileCopyright() %>');
    });

</script>


</body>