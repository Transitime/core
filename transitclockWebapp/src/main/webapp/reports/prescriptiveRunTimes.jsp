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
    <title>Prescriptive Run Times</title>

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

    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/page-panels.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/reports/prescriptive-runtime.css" />
    <title>Prescriptive Run Times</title>
</head>
<body class="real-time-live-map">
    <%@include file="/template/header.jsp" %>

    <div class="panel split">
        <div class="left-panel">
            <h4 class="page-title">
                Prescriptive Run Times
            </h4>
            <input type="hidden" name="isAllRoutesDisabled"  class="isAllRoutesDisabled" value="true">
            <form class="row" novalidate>
                <jsp:include page="params/routeAllOrSingleNew.jsp" />

                <div class="row">
                    <label class="col-sm-12 col-form-label">Service Day Type</label>
                    <div class="col-sm-12">
                        <select id="serviceDayType" name="serviceDayType" class="form-select">
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
                    <label class="col-sm-12 col-form-label">Service Period</label>
                    <div class="col-sm-12">
                        <select id="gtfsDateRange" name="gtfsDateRange" class="form-select">
                        </select>
                    </div>
                </div>

            </form>
            <div class="list-group">
                <button class="list-group-item list-group-item-action"  id="submit" >Submit</button>
            </div>
        </div>
        <div class="right-panel position-relative  no-borders ">
            <!-- default placeholder image here -->
            <div class="image-container full-width-image d-flex justify-content-center grey-bg">
                <div class="position-relative d-flex justify-content-centerd-flex justify-content-center align-items-center img-path">
                    <img src="<%= request.getContextPath() %>/reports/images/ontime.png" id="on-time-performance"  class="img-fluid grey-img"/>
                    <h1 class=" position-absolute view-form-btn">Submit Form to View Data</h1>
                </div>
            </div>
            <!-- top header box -->
            <div class="d-flex pl-10 flex-column box-shadow">
                <div class="list-group comparsion-button-list m-bt-0 row toggle-chart ">
                <div class="col-xs-12">
                    <div id="paramDetails"  class="param-detail-content  route-time-analysis-header
                    bg-65 d-flex list-group-item list-group-item-action justify-content-between">No Routes</div>
                </div>
            </div>
            <!-- main content -->
            <div class="row mx-2 pt-3">
                <!-- OTP Info -->
                <div class="row toggle-chart mb-1">
                    <div class="col-sm-4">
                        <label class="col-sm-6 col-form-label m-bt-20">(Old) Schedule Adherence: <span  class="fw-light" id="current_otp"></span></label>
                        <label class=" col-sm-6 col-form-label bg-secondary-light m-bt-20">(New) Minimum Schedule Adherence: <span  class="fw-light" id="expected_otp"></span></label>
                    </div>
                    <div class="col-sm-4"></div>
                    <div class="col-sm-4">
                        <ul class="prescriptive-legend float-end">
                            <li><span class="prescriptive-legend-block bg-orange"></span><span class="align-top prescriptive-legend-label">Total RunTime</span></li>
                            <li><span class="prescriptive-legend-block bg-lime-green"></span><span class="align-top prescriptive-legend-label">New RunTime</span></li>
                            <li><span class="prescriptive-legend-block bg-light-yellow"></span><span class="align-top prescriptive-legend-label">Old RunTime</span></li>
                        </ul>
                    </div>
                </div>
                <!-- Run Time Tables -->
                <div class="row">
                    <!-- tables header label -->
                    <div class="row toggle-chart align-items-center">
                        <div class="col-sm-2"><h5>Run Time Tables</h5></div>
                        <div class="col-sm-8"></div>
                        <div class="col-sm-2">
                            <button class="float-end list-group-item list-group-item-action gtfs-submit">Export</button>
                        </div>
                    </div>
                    <!-- table content -->
                    <div class="row toggle-chart">
                        <div class="col-sm-12 pl-1 adjustment-details inner-flex justify-content-center">
                        </div>
                    </div>
                    <!-- export button -->
                    <div class="row toggle-chart py-2">
                        <div class="d-flex justify-content-center">
                            <div class="col-sm-2">
                                <input type="hidden" id="resultObject">
                                <input type="hidden" id="routeName">
                                <button class="list-group-item list-group-item-action gtfs-submit">Export</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        </div>
    </div>


    <script src="https://cdn.jsdelivr.net/npm/chart.js@2.9.4"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/chartjs-chart-box-and-violin-plot/2.4.0/Chart.BoxPlot.js"></script>
    <script type="text/javascript" src="javascript/run-time-helper.js"> </script>
    <script type="text/javascript"  src="javascript/prescriptiveRunTimes.js"> </script>

</body>