<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page import="org.transitclock.web.WebConfigParams"%>
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

    <link href="params/reportParams.css" rel="stylesheet"/>

    <%--        <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>--%>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">

    <title>TransitClock Map</title>
</head>
<body class="map-screen real-time-live-map">
<%@include file="/template/header.jsp" %>

<div id="paramsSidebar">
    <div class="title">
        Live Map
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
                <input type="text" id="stopsSearch" placeholder="Stops" name="stopsSearch">
                <button type="submit" id="stopsSubmit" onclick="showStopDetails($('#routes').val(), $('#stopsSearch').val())">Show Stop</button>
            </div>

            <div class="param">
                <input type="text" id="vehiclesSearch" placeholder="Vehicles" name="vehiclesSearch">
                <button type="submit" id="vehiclesSubmit" onclick="openVehiclePopup(getVehicleMarker($('#vehiclesSearch').val()))">Show Vehicle</button>
            </div>
        </div>

        <div class="margintop">
            <div class="paramCheckbox">
                <label for="assignedFilter">
                    <span>Assigned Only</span>
                    <input type="checkbox" id="assignedFilter" name="assignedFilter">
                </label>
            </div>
        </div>
    </div>
    <div id="links">
        <div id="dispatcherLink">
            <a href="realTimeDispatcher.jsp?a=1">Dispatcher View >></a>
        </div>
        <div id="schAdhLink">
            <a href="realTimeScheduleAdherence.jsp?a=1">Schedule Adherence View >></a>
        </div>
    </div>
</div>


<div id="mainPage" style="width: 79%; height: 100%; display: inline-block;">
    <div id="map"></div>
</div>

<script type="text/javascript">
    var mapTileUrl = '<%= WebConfigParams.getMapTileUrl() %>';
    var copyRight ='<%= WebConfigParams.getMapTileCopyright() %>';
</script>

<script type="text/javascript"  src="<%= request.getContextPath() %>/javascript/map-helper.js"> </script>

</body>
