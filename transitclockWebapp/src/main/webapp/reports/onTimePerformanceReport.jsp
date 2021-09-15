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

    <script src="https://cdn.jsdelivr.net/npm/chart.js@2.8.0"></script>
    <script src="https://cdn.jsdelivr.net/gh/emn178/chartjs-plugin-labels/src/chartjs-plugin-labels.js"></script>

</head>
<script>
    var request = {<%= WebUtils.getAjaxDataString(request) %>},
        contextPath = "<%= request.getContextPath() %>";
</script>
<body class="avl-page">
<%@include file="/template/header.jsp" %>
<div class="panel split overflow-x-hidden">
    <div class="left-panel">
        <h4 class="page-title">
            On Time Performance
        </h4>
        <form class="row" novalidate>

            <input type="hidden" name="a" value="<%= request.getParameter("a")%>">
            <input type="hidden" name="date-range-picker" value="true" class="isDateRangePicker">

            <jsp:include page="params/routeAllOrSingleNew.jsp" />
            <jsp:include page="params/fromDateNumDaysTime.jsp" />

            <div class="row">
                <label class="col-sm-12 col-form-label">Report Settings</label>
                <div class="btn-group" role="group" aria-label="Basic radio toggle button group">



                    <input type="radio" class="btn-check" value="timePointsOnly" name="stopType" id="timePointsOnly" autocomplete="off">
                    <label class="btn btn-outline-primary" for="timePointsOnly">Timepoints Only</label>

                    <input type="radio" class="btn-check" value="allStops" name="stopType" id="allStops" autocomplete="off" >
                    <label class="btn btn-outline-primary" for="allStops">All stops</label>

                </div>
            </div>


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

            <div class="row">
                <label class="col-sm-12 col-form-label">Service Day Type</label>
                <div class="col-sm-12">
                    <select id="serviceDayType" name="serviceDayType" class="form-select">
                        <option value="">All</option>
                        <option value="weekday">Weekday</option>
                        <option value="saturday">Saturday</option>
                        <option value="sunday">Sunday</option>
                        <span class="select2-selection__arrow">
											<b role="presentation"></b>
										</span>
                    </select>
                </div>

            </div>


        </form>
        <div class="list-group">
            <button class="list-group-item list-group-item-action"  id="submit" >Submit</button>
        </div>
    </div>
    <div class="right-panel  position-relative">

        <div class="list-group toggle-chart ">
            <button class="list-group-item list-group-item-action"   >On Time Performance By Route</button>
        </div>
        <div class="image-container full-width-image d-flex justify-content-center grey-bg">
            <div class="position-relative d-flex justify-content-centerd-flex justify-content-center align-items-center img-path">
              <img src="<%= request.getContextPath() %>/reports/images/ontime.png" id="on-time-performance"  class="img-fluid grey-img"/>
                <h1 class=" position-absolute view-form-btn"   >Submit Form to View Data</h1>
            </div>
        </div>
        <div class="row toggle-chart d-flex justify-content-center ">
            <div class=" chart-container">
                <canvas id="chartCanvas" ></canvas>
            </div>
        </div>
        <div class="row toggle-chart">
            <div class="col-sm-12 d-flex justify-content-center align-items-center">
                <div id="chartTotal"></div>
            </div>
        </div>

    </div>
</div>
</div>


<script src="<%= request.getContextPath() %>/javascript/onTimePerformanceReport.js"></script>

</body>
</html>