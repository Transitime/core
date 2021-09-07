<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page import="org.transitclock.web.WebConfigParams" %>
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
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;700&display=swap" rel="stylesheet">

    <script src="//unpkg.com/leaflet@0.7.3/dist/leaflet.js"></script>

    <script src="<%= request.getContextPath() %>/maps/javascript/leafletRotatedMarker.js"></script>
    <script src="<%= request.getContextPath() %>/maps/javascript/mapUiOptions.js"></script>

    <link rel="stylesheet" href="<%= request.getContextPath() %>/maps/css/mapUi.css"/>

    <link href="params/reportParams.css" rel="stylesheet"/>

    <%--        <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>--%>

    <!-- Load in Select2 files so can create fancy route selector -->
    <link href="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/css/select2.min.css" rel="stylesheet"/>
    <script src="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/js/select2.min.js"></script>
</head>
<body class="map-screen real-time-live-map real-time-schedule-adhrence">
<%@include file="/template/header.jsp" %>
<div id="paramsSidebar">
    <div class="header-title">
        Schedule Adherence
    </div>

    <div id="paramsFields">
        <div id="routesParam" class="margintop">
            <div class="paramLabel">Routes</div>
            <%-- For passing agency param to the report --%>
            <input type="hidden" name="a" value="<%= request.getParameter("a")%>">
            <jsp:include page="params/routeMultipleNoLabel.jsp" />
        </div>

        <div id="search" class="margintop">
            <div class="paramLabel">Search</div>
            <div class="param">
                <input type="text" id="vehiclesSearch" placeholder="Vehicles" name="vehiclesSearch">
                <button type="submit" id="vehiclesSubmit"
                        onclick="getAndProcessSchAdhData($('#route').val(), $('#vehiclesSearch').val())">Show Vehicle
                </button>
            </div>
        </div>
    </div>
    <div id="links">
        <div id="liveMapLink">
            <a href="realTimeLiveMap.jsp?a=1">Live Map View >></a>
        </div>
        <div id="dispatcherLink">
            <a href="realTimeDispatcher.jsp?a=1">Dispatcher View >></a>
        </div>
    </div>
</div>

<div id="mainPage" style="width: 79%; height: 100%; display: inline-block;">
    <div id="map"></div>
</div>

<div id="legend">
    <div><b>Schedule Marker Legend</b></div>
    <br/>
    <div class="schedule-legend-container">
        <div class="circular-element onTime" ></div>
        <text> = On Time</text>
    </div>
    <div class="schedule-legend-container">
        <div class="circular-element late" ></div>
        <text> = Late</text>
    </div>
    <div class="schedule-legend-container">
        <div class="circular-element early" ></div>
        <text> = Early</text>
    </div>

</div>

<script>

    $("#route").attr("style", "width: 200px");

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
        var content =
            "<b>Vehicle:</b> " + vehicle.id
            + "<br/><b>Route:</b> " + vehicle.routeName;
        if (vehicle.headsign)
            content += "<br/><b>To:</b> " + vehicle.headsign;
        if (vehicle.schAdhStr)
            content += "<br/><b>SchAhd:</b> " + vehicle.schAdhStr;
        if (vehicle.block)
            content += "<br/><b>Block:</b> " + vehicle.block;
        if (vehicle.tripId)
            content += "<br/><b>Trip:</b> " + vehicle.tripId;
        if (vehicle.driver)
            content += "<br/><b>Driver:</b> " + vehicle.driver;


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

                    if (vehicleId.trim() != "" && vehicle.id == vehicleId) {
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
</html>