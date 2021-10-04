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
    <title>Real Time Vehicle Monitoring</title>

    <link rel="stylesheet" href="//unpkg.com/leaflet@0.7.3/dist/leaflet.css" />
    <script src="//unpkg.com/leaflet@0.7.3/dist/leaflet.js"></script>
    <script src="<%= request.getContextPath() %>/javascript/jquery-dateFormat.min.js"></script>

    <script src="<%= request.getContextPath() %>/maps/javascript/leafletRotatedMarker.js"></script>
    <script src="<%= request.getContextPath() %>/maps/javascript/mapUiOptions.js"></script>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;700&display=swap" rel="stylesheet">

    <!-- Load in Select2 files so can create fancy selectors -->
    <link href="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/css/select2.min.css" rel="stylesheet" />

    <script src="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/js/select2.min.js"></script>

    <%--    <link href="https://cdn.jsdelivr.net/npm/select2@4.1.0-rc.0/dist/css/select2.min.css" rel="stylesheet" />
        <script src="https://cdn.jsdelivr.net/npm/select2@4.1.0-rc.0/dist/js/select2.min.js"></script>--%>

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
            Real Time Vehicle Monitoring
        </h4>
        <form class="row" novalidate>

            <div class="row">
                <label class="col-sm-12 col-form-label">Search</label>
                <div class="btn-group" role="group" aria-label="Basic radio toggle button group">

                    <input type="radio" class="btn-check" value="Stop" name="liveMapRadio" id="stopRadioBtn" autocomplete="off" >
                    <label class="btn btn-outline-primary" for="stopRadioBtn">Stop</label>

                    <input type="radio" class="btn-check" value="Vehicle" name="liveMapRadio" id="vehicleRadioBtn" autocomplete="off">
                    <label class="btn btn-outline-primary" for="vehicleRadioBtn">Vehicle</label>

                </div>
            </div>

            <div class="row">
                <div class="col-sm-9">
                    <input type="text" class="form-control" id="search-realpage" placeholder="Stop">
                </div>
                <div class="col-sm-3 pad-left-0">
                    <button class="btn btn-primary submit-button "  type="button" value="show" onclick="toggleShow()">Show</button>
                </div>
            </div>

            <div class="row">
                <label class="col-sm-12 col-form-label">Routes</label>
                <input type="hidden" name="isAllRoutesDisabled"  class="isAllRoutesDisabled" value="true">
                <input type="hidden" name="a" value="<%= request.getParameter("a")%>">
                <jsp:include page="../reports/params/routeMultipleNoLabel.jsp" />
            </div>



        </form>

    </div>
    <div class="right-panel">
        <div id="map"></div>
    </div>
</div>
<script type="text/javascript">
    var mapTileUrl = '<%= WebConfigParams.getMapTileUrl() %>';
    var copyRight ='<%= WebConfigParams.getMapTileCopyright() %>';
</script>

<script type="text/javascript"  src="<%= request.getContextPath() %>/javascript/map-helper.js"> </script>

</body>