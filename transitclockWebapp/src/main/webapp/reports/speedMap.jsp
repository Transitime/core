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
    <title>Speed Map</title>

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

    <script src="https://cdn.jsdelivr.net/npm/chart.js@2.8.0"></script>
    <script src="https://cdn.jsdelivr.net/gh/emn178/chartjs-plugin-labels/src/chartjs-plugin-labels.js"></script>

</head>
<script>
    var request = {<%= WebUtils.getAjaxDataString(request) %>},
        contextPath = "<%= request.getContextPath() %>";
</script>
<body>
<%@include file="/template/header.jsp" %>
<div class="panel split overflow-x-hidden">
    <div class="left-panel">
        <h4 class="page-title">
            Speed Map
        </h4>
        <form class="row" novalidate>

            <input type="hidden" name="a" value="<%= request.getParameter("a")%>">

            <input type="hidden" name="allRoutesDisabled" value="no-auto-trigger-default" class="isAllRoutesDisabled">
            <input type="hidden" name="date-range-picker" value="true" class="isDateRangePicker">

            <jsp:include page="params/routeAllOrSingleNew.jsp" />

            <div class="row">
                <label class="col-sm-12 col-form-label">Direction</label>
                <div class="col-sm-12">
                    <select id="direction" name="direction" disabled="true" class="form-select">

                    </select>
                </div>

            </div>

            <jsp:include page="params/fromDateNumDaysTimeSidePanel.jsp" />

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
            <div class="row">
                <label class="col-sm-12 col-form-label">Speed Settings</label>
            </div>
            <div class="row">
                <label class="col-sm-8 col-form-label"><i   class="bi bi-square-fill red pad-rt-10"></i>Low Speed (mph max)</label>
                <div class="col-sm-4 pad-left-0">
                    <input type="number"  class="form-control"  id="lowSpeedManual" name="lowSpeedManual" min="0" max="98" step="0.5" value="15" oninput="lowSpeedManual(this.value)" >
                </div>

            </div>
            <div class="row">
                <label class="col-sm-8 col-form-label"><i   class="bi bi-square-fill yellow pad-rt-10"></i>Mid Speed (mph max)</label>
                <div class="col-sm-4 pad-left-0">
                    <input type="number"  class="form-control"  id="midSpeedManual" name="midSpeedManual" min="1" max="99" step="0.5" value="25" oninput="midSpeedManual(this.value)">
                </div>

            </div>
            <div class="row">
                <label class="col-sm-12 col-form-label"><i   class="bi bi-square-fill dark-green pad-rt-10"></i>High Speed (mph max)</label>

            </div>

        </form>
        <div class="list-group">
            <button class="list-group-item list-group-item-action"  id="mainSubmit">Submit</button>
        </div>
    </div>
    <div class="right-panel position-relative">
        <div class="list-group position-absolute comparsion-button-list d-none">
            <div id="avgRunTimeTop" class="d-flex list-group-item list-group-item-action justify-content-between">No Routes</div>
        </div>
        <div class="map-legend-icons leaflet-popup-content absolute-bottom-right ">
            <div class="card">
                <%--                <div class="card-header header-theme">
                                    <b>Schedule Marker Legend</b>
                                </div>--%>

                <div class="card-body">
                    <ul class="list-group">
                        <li class="list-group-item d-flex align-items-center">
                            <i   class="bi bi-dash-lg red pad-rt-10"></i>  <span class="legend-label" id="low-speed-label"> Low Speed (0 - 15 mph)</span>

                        </li>
                        <li class="list-group-item d-flex align-items-center">
                            <i   class="bi bi-dash-lg yellow pad-rt-10"></i>   <span class="legend-label" id="mid-speed-label"> Mid Speed (15 - 25 mph)</span>
                        </li>
                        <li class="list-group-item d-flex  align-items-center">
                            <i   class="bi bi-dash-lg dark-green pad-rt-10"></i>  <span class="legend-label" id="high-speed-label"> High Speed (> 25 mph)</span>
                        </li>
                        <li class="list-group-item d-flex  align-items-center">
                            <img src="images/click_icon.png" alt="hand-pointer" class="hand-pointer-img">
<%--                            <i   class="bi bi-hand-index-thumb white"></i> --%>
                                <span class="legend-label">Click segment to view speed</span>
                        </li>
                    </ul>
                </div>

            </div>
        </div>

        <div id="map"></div>

        <div id="runTimesFlyout" class="modal fade comparision-modal-popup" >
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header align-items-center">
                        <h5 class="modal-title" id="exampleModalLabel">Trip Run Time Comparison </h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" id='closeFlyout' aria-label="Close"></button>
                    </div>
                    <div id="flyoutContents" class="modal-body">


                        <div class="row">
                            <div id="paramDetailsFlyout" ></div>
                        </div>
                        <div class="row">
                            <div id="avgRunTimeFlyout"  ></div>
                        </div>

                        <jsp:include page="params/dateRangePicker.jsp" />
                        <div class="list-group ">
                            <button class="list-group-item list-group-item-action"   id="runTimeSubmit"  >Submit</button>
                        </div>
                        <div class="row">

                            <canvas id="comparisonChart" height="400" ></canvas>

                        </div>
                    </div>
                </div>
            </div>
        </div>

    </div>
</div>
</div>


<script src="<%= request.getContextPath() %>/reports/javascript/speedmap.js"></script>

</body>
</html>