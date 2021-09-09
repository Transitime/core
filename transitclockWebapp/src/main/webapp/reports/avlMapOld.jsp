<%@ page import="org.transitclock.utils.web.WebUtils" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <!-- So that get proper sized map on iOS mobile device -->
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />

    <link rel="stylesheet" href="<%= request.getContextPath() %>/maps/css/mapUi.css" />
    <link rel="stylesheet" href="<%= request.getContextPath() %>/map/css/avlMapUi.css" />

    <!-- Load javascript and css files -->
    <%@include file="/template/includes.jsp" %>

    <link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.3/leaflet.css" />
    <script src="//cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.3/leaflet.js"></script>

    <script src="<%= request.getContextPath() %>/maps/javascript/leafletRotatedMarker.js"></script>
    <script src="<%= request.getContextPath() %>/maps/javascript/mapUiOptions.js"></script>
    <script src="<%= request.getContextPath() %>/map/javascript/animation.js"></script>

    <!-- Load in Select2 files so can create fancy route selector -->
    <link href="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/css/select2.min.css" rel="stylesheet" />
    <script src="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/js/select2.min.js"></script>

    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">

    <title>AVL Data Map</title>


</head>

<body>
<div id="map"></div>
<div id="params">
    <table id="paramsTable"></table>
    <jsp:include page="params/vehicle.jsp" />
    <jsp:include page="params/fromDateNumDaysTime.jsp" />
    <div class="param">
        <label for="late">Allowable late (mins):</label>
        <input type="number" id="late" name="late" min="0" max="1440" step="0.5" value="5">
    </div>
    <div class="param">
        <label for="early">Allowable early (mins):</label>
        <input type="number" id="early" name="early" min="0" max="1440" step="0.5" value="1">
    </div>
    <jsp:include page="params/routeAllOrSingleWithShortName.jsp" /> <br>
    <input type="button" id="submit" value="Submit">
    <a href="#" id="exportData">Export</a>
</div>
<div id="playbackContainer">
    <div id="playback">
        <div>
            <input type="image" src="<%= request.getContextPath() %>/reports/images/playback/media-seek-backward.svg" id="playbackPrev" />
            <input type="image" src="<%= request.getContextPath() %>/reports/images/playback/media-skip-backward.svg" id="playbackRew" />
            <input type="image" src="<%= request.getContextPath() %>/reports/images/playback/media-playback-start.svg" id="playbackPlay" />
            <input type="image" src="<%= request.getContextPath() %>/reports/images/playback/media-skip-forward.svg" id="playbackFF" />
            <input type="image" src="<%= request.getContextPath() %>/reports/images/playback/media-seek-forward.svg" id="playbackNext" />
        </div>
        <div><span id="playbackRate">1X</span></div>
        <div><span id="playbackTime">00:00:00</span></div>
    </div>
</div>
<div id="legend">
    <p  class="avl-legend-elements-p">AVL Marker Legend</p>
    <div class="avl-legend-elements">
        <div class="avlTriangleon-time" ></div>
        <text >= On Time</text>
    </div>
    <div class="avl-legend-elements">
        <div class="avlTrianglelate" ></div>
        <text >= Late</text>
    </div>
    <div class="avl-legend-elements">
        <div class="avlTriangleearly" ></div>
        <text >= Early</text>
    </div>
    <div class="avl-legend-elements">
        <div class="avlTriangle" ></div>
        <text >= No Data</text>
    </div>
</div>
</body>

<script>
    var request = {<%= WebUtils.getAjaxDataString(request) %>},
        contextPath = "<%= request.getContextPath() %>";
</script>
<script src="<%= request.getContextPath() %>/map/javascript/avlMap.js"></script>
</html>

