<%@ page import="org.transitime.utils.web.WebUtils" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <!-- So that get proper sized map on iOS mobile device -->
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
  
  <link rel="stylesheet" href="<%= request.getContextPath() %>/map/css/mapUi.css" />
  <link rel="stylesheet" href="<%= request.getContextPath() %>/map/css/avlMapUi.css" />
 
  <!-- Load javascript and css files -->
  <%@include file="/template/includes.jsp" %>
  
  <link rel="stylesheet" href="http://cdn.leafletjs.com/leaflet-0.7.3/leaflet.css" />
  <script src="http://cdn.leafletjs.com/leaflet-0.7.3/leaflet.js"></script>
  
  <script src="<%= request.getContextPath() %>/map/javascript/leafletMovingMarker.js"></script>
  <script src="<%= request.getContextPath() %>/map/javascript/leafletRotatedMarker.js"></script>
  <script src="<%= request.getContextPath() %>/map/javascript/mapUiOptions.js"></script>
  
   <!-- Load in Select2 files so can create fancy route selector -->
  <link href="../select2/select2.css" rel="stylesheet"/>
  <script src="../select2/select2.min.js"></script>
  
  <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
  
  <title>AVL Data Map</title>
  

</head>

<body>
  <div id="map"></div>
  <div id="params">
  	<table id="paramsTable"></table>
  	<jsp:include page="params/vehicle.jsp" />
  	<jsp:include page="params/fromToDateTime.jsp" />
    <jsp:include page="params/routeSingle.jsp" /> <br>
    <a href="#" id="exportData">Export</a>
  </div>
  <div id="playback_container">
	  <div id="playback">
	  	<input type="image" src="<%= request.getContextPath() %>/reports/images/playback/media-seek-backward.svg" id="playbackPrev" />
	  	<input type="image" src="<%= request.getContextPath() %>/reports/images/playback/media-skip-backward.svg" id="playbackRew" />
	  	<input type="image" src="<%= request.getContextPath() %>/reports/images/playback/media-playback-start.svg" id="playbackPlay" />
	  	<input type="image" src="<%= request.getContextPath() %>/reports/images/playback/media-skip-forward.svg" id="playbackFF" /> 
	  	<input type="image" src="<%= request.getContextPath() %>/reports/images/playback/media-seek-forward.svg" id="playbackNext" /> <br>
	  	<span id="playbackRate">1X</span> <br>
	  	<span id="playbackTime">00:00:00</span>
	  </div>
  </div>
</body>

<script src="<%= request.getContextPath() %>/map/javascript/avlMap.js"></script>
<script>
var request = {<%= WebUtils.getAjaxDataString(request) %>},
	contextPath = "<%= request.getContextPath() %>";
main(request, contextPath);
</script>

</html>