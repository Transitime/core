<%@ page import="org.transitclock.utils.web.WebUtils" %>
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
    <title>AVL Data Map</title>

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
    <link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/1.10.21/css/jquery.dataTables.css">

    <script type="text/javascript" charset="utf8" src="https://cdn.datatables.net/1.10.21/js/jquery.dataTables.js"></script>


    <link rel="stylesheet" href="<%= request.getContextPath() %>/map/css/avlMapUi.css" />

    <%--        <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>--%>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/page-panels.css">

    <script src="<%= request.getContextPath() %>/maps/javascript/leafletRotatedMarker.js"></script>
    <script src="<%= request.getContextPath() %>/maps/javascript/mapUiOptions.js"></script>
    <script src="<%= request.getContextPath() %>/map/javascript/animation.js"></script>
</head>
<body>
<%@include file="/template/header.jsp" %>
<div class="panel split">
    <div class="left-panel">
        <h4 class="page-title">
            AVL Data Map
        </h4>
        <form class="row" novalidate>
            <input type="hidden" name="isAllRoutesDisabled"  class="isAllRoutesDisabled" value="true">
            <jsp:include page="params/routeAllOrSingleWithShortName.jsp" />

            <br>
            <jsp:include page="params/vehicle.jsp" />
            <jsp:include page="params/fromDateNumDaysTimeSidePanel.jsp" />

            <div class="row">
                <label class="col-sm-7 col-form-label">Allowable late (mins)</label>
                <div class="col-sm-5 pad-left-0">
                    <input type="number" id="late" class="form-control"  name="late" min="0" max="1440" step="0.5" value="5">
                </div>

            </div>
            <div class="row">
                <label class="col-sm-7 col-form-label">Allowable early (mins)</label>
                <div class="col-sm-5 pad-left-0">
                    <input type="number" id="early"  class="form-control" name="early" min="0" max="1440" step="0.5" value="1">
                </div>

            </div>

        </form>
        <div class="list-group">
            <button class="list-group-item list-group-item-action"  id="submit" >Submit</button>
            <a  class="list-group-item list-group-item-action secondary-btn d-none " id="exportData">
                Export
            </a>
        </div>
    </div>
    <div class="right-panel">
        <div id="map"></div>

        <div id="playbackContainer">
            <div id="playback">

        <div class="image-container">
            <img src="<%= request.getContextPath() %>/reports/images/playback/media-seek-forward.svg" id="playbackPrev"  title="Go Back 15 sec " class="img-thumbnail reverse"/>
            <img src="<%= request.getContextPath() %>/reports/images/playback/media-skip-forward.svg" id="playbackRew"  title="Decrease Playback Speed" class="img-thumbnail reverse"/>
            <img src="<%= request.getContextPath() %>/reports/images/playback/media-playback-start.svg" id="playbackPlay"  title="Start / Pause Vehicle Playback" class="img-thumbnail pause-custom"/>
            <img src="<%= request.getContextPath() %>/reports/images/playback/media-skip-forward.svg" id="playbackFF" title="Increase Playback Speed" class="img-thumbnail"/>
            <img src="<%= request.getContextPath() %>/reports/images/playback/media-seek-forward.svg" id="playbackNext" title="Skip Ahead 15 sec"  class="img-thumbnail"/>
        </div>
                <div><span id="playbackRate">1X</span></div>
                <div><span id="playbackTime">00:00:00</span></div>
            </div>
        </div>

        <div class="map-legend-icons leaflet-popup-content ">
            <div class="card">

                <div class="card-body">
<%--                    <b>AVL Marker Legend</b>--%>
                    <ul class="list-group">
                        <li class="list-group-item d-flex align-items-center">
                            <i class="bi bi-triangle-fill green"></i>
                             &nbsp;= On Time
                        </li>
                        <li class="list-group-item d-flex align-items-center">
                            <i class="bi bi-triangle-fill yellow"></i>
                             &nbsp;= Late
                        </li>
                        <li class="list-group-item d-flex  align-items-center">
                            <i class="bi bi-triangle-fill red"></i>
                             &nbsp;= Early
                        </li>
                        <li class="list-group-item d-flex  align-items-center">
                            <i class="bi bi-triangle-fill blue"></i>
                             &nbsp;= No Data
                        </li>
                    </ul>
                </div>

            </div>
        </div>

    </div>
</div>

<div class="modal fade comparision-modal-popup" id="schedule-modal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel"
     aria-hidden="true">
    <div class="modal-dialog dialog-content modal-lg" role="document">
    </div>
</div>
</div>

<script>
    var request = {<%= WebUtils.getAjaxDataString(request) %>},
        contextPath = "<%= request.getContextPath() %>";
</script>
<script src="<%= request.getContextPath() %>/map/javascript/avlMap.js" defer></script>
</body>
</html>