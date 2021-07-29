<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page import="org.transitclock.web.WebConfigParams" %>

<!--
Query String parameters:
a=AGENCY (required)
r=ROUTE (optional, if not specified then a route selector is created)
s=STOP_ID (optional, for specifying which stop interested in)
tripPattern=TRIP_PATTERN (optional, for specifying which stop interested in).
verbose=true (optional, for getting additional info in vehicle popup window)
showUnassignedVehicles=true (optional, for showing unassigned vehicles)
-->
<html>
<head>
    <!-- So that get proper sized map on iOS mobile device -->
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"/>

    <link rel="stylesheet" href="css/mapUi.css"/>

    <!-- Load javascript and css files -->
    <%@include file="/template/includes.jsp" %>
    <link rel="stylesheet" href="//unpkg.com/leaflet@1.0.0/dist/leaflet.css"/>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;700&display=swap" rel="stylesheet">
    <script src="//unpkg.com/leaflet@1.0.0/dist/leaflet.js"></script>
    <script src="javascript/leafletRotatedMarker.js"></script>
    <script src="javascript/mapUiOptions.js"></script>
    <script src="<%= request.getContextPath() %>/javascript/jquery-dateFormat.min.js"></script>

    <link rel="stylesheet" href="<%= request.getContextPath() %>/reports/params/reportParams.css" />

    <%-- MBTA wants some color customization. Load in options file if mbta --%>
    <% if (request.getParameter("a").startsWith("mbta")) { %>
    <link rel="stylesheet" href="css/mbtaMapUi.css"/>
    <script src="javascript/mbtaMapUiOptions.js"></script>
    <% } %>

    <!-- Load in Select2 files so can create fancy selectors -->
    <link href="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/css/select2.min.css" rel="stylesheet"/>
    <script src="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/js/select2.min.js"></script>


    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">

    <title>TransitClock Map</title>
</head>

<body class="map-screen">

<!-- To center successfully in all situations tried to use a div within a div.
     The title text is set in css so that it is easily configurable -->
<%@include file="/template/header.jsp" %>

<!--  Wanted to center the routes selector horizontally but couldn't get
      it to work. Got problems with the map not fitting page properly
      when tried to use fancy position absolute and relative css.
      Also, found that only way to set width of route selector is
      to set the css width here. Yes, strange! -->
<div class="page-container">
    <div class="side-container-view">
        <!--  To center successfully in all situations use div within a div trick -->
        <div class="param-container">
            <div class="title">
                Real Time Vehicle Monitoring
            </div>
            <div id="routesParam" class="margintop">
                <div class="paramLabel">Routes</div>
                <div id="routesDiv" class="param">
                    <select id="route"  multiple="multiple"></select>
                </div>
            </div>

            <div id="search" class="margintop">
                <div class="paramLabel">Search</div>
                <div class="param">
                    <input type="text" id="stopsSearch" placeholder="Stops" name="stopsSearch">
                    <button type="submit" id="stopsSubmit" onclick="showStopDetails($('#routes').val(), $('#stopsSearch').val())">Show Stop</button>
                </div>

                <div class="param">
                    <input type="text" id="vehiclesSearch" placeholder="Vehicles" name="vehiclesSearch">
                    <button type="submit" id="vehiclesSubmit" onclick="openVehiclePopup(getVehicleMarker($('#vehiclesSearch').val()))">Show Vehicle</button>
                </div>
            </div>
        </div>
    </div>
    <div class="map-container-view">
        <div id="map"></div>
    </div>


</body>
<script type="text/javascript">
    var mapTileUrl = '<%= WebConfigParams.getMapTileUrl() %>';
    var copyRight ='<%= WebConfigParams.getMapTileCopyright() %>';
</script>
<script type="text/javascript"  src="<%= request.getContextPath() %>/javascript/map-helper.js"> </script>
