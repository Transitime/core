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
    <title>Run Time Analysis</title>

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
            Run Time Analysis
        </h4>
        <form class="row" novalidate>

            <input type="hidden" name="a" value="<%= request.getParameter("a")%>">

            <input type="hidden" name="allRoutesDisabled" value="no-auto-trigger-default" >
            <input type="hidden" name="date-range-picker" value="true" class="isDateRangePicker">

            <jsp:include page="params/routeAllOrSingleNew.jsp" />

            <div class="row">
                <label class="col-sm-12 col-form-label">Direction</label>
                <div class="col-sm-12">
                    <select id="direction" name="direction" disabled="true" class="form-select">

                    </select>
                </div>

            </div>

            <div class="row">
                <label class="col-sm-12 col-form-label">Trip Pattern</label>
                <div class="col-sm-12">
                    <select id="tripPattern" name="tripPattern" disabled="true" class="form-select">

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
                <label class="col-sm-12 col-form-label">Route Settings</label>
                <div class="btn-group" role="group" aria-label="Basic radio toggle button group">

                    <input type="radio" class="btn-check" value="Time Points"  name="stopType" id="timePointsOnly" autocomplete="off" >
                    <label class="btn btn-outline-primary" for="timePointsOnly">Time Points</label>

                    <input type="radio" class="btn-check" value="All Stops" name="stopType" id="allStops" autocomplete="off">
                    <label class="btn btn-outline-primary" for="allStops">All Stops</label>

                </div>
            </div>



    </form>
    <div class="list-group">
        <button class="list-group-item list-group-item-action"  id="submit">Submit</button>
    </div>
</div>
<div class="right-panel position-relative">
    <div class="list-group comparsion-button-list m-bt-0 row">
        <div class="col-xs-12">
            <div id="paramDetails" class=" route-time-analysis-header bg-65 d-flex list-group-item list-group-item-action justify-content-between">No Routes</div>
        </div>
    </div>


    <div class="row">
        <div class="col-xs-12">
            <div id="run-time-tabs">
            <ul class="only-individual-route">
                <li><a href="#component">Component</a></li>

                <li><a href="#percentage">Percentile</a></li>

                <li><a href="#distribution">Distribution</a></li>
            </ul>

            <div id="component">
                <div id="mainResults" class="m-bt-20">
                    <div class="individual-route">
                        <h3>Summary Statistics</h3>
                        <div class="row flex-nowrap m-bt-10 run-time-vairables">
                            <div >Average Run time :</div>
                            <div id="avg-run-time"></div>
                        </div>
                        <div class="row flex-nowrap run-time-vairables m-bt-10">
                            <div class="d-flex flex-wrap">
                                <label >Fixed <i class="bi bi-info-circle" data-bs-toggle="tool-tip" title="Fixed Content" type="button"></i> :</label>
                                <div id="fixed-time" ></div>
                            </div>
                            <div class="d-flex flex-wrap">
                                <label>Variable <i class="bi bi-info-circle" data-bs-toggle="tool-tip" title="Variable Content" type="button"></i> :</label>
                                <div id="variable-time" ></div>
                            </div>
                            <div class="d-flex flex-wrap">
                                <label >Dwell <i class="bi bi-info-circle" data-bs-toggle="tool-tip" title="Dwell Content" type="button"></i> :</label>
                                <div id="dwell-time" ></div>
                            </div>
                        </div>

                    </div>
                </div>
                <div class="individual-route trip-block " >
                    <div class="row align-items-center" id="trips-container"></div>
                </div>
                <div class="all-routes" >
                    <h3 id="visualization-container-header">Route Run Time Performance</h3>
                </div>
                <div class="visualization-container m-tp-0">
                    <label class="d-flex align-items-center justify-content-center" id="heading-canvas"> Aggregate Run Times for all trips</label>
                    <div id="runTimeVisualization" ></div>
                </div>
                <br>
                <br>
            </div>

            <div id="percentage" class="only-individual-route">
                <div class="percentile-select-container row align-items-center m-bt-20" id="percentile-select-container"></div>


                <!--<h3>Trip Run Time For Percentile</h3> -->
                <div class="row m-bt-10">
                    <div class="col-sm-6">
                    <label>All trips average : <span id="percentile-summary-content"></span></label>
                </div>
                </div>
                <div class="row">
                    <div class="col-sm-6">
                        <table class="table table-bordered percentile-summary-details">

                        </table>
                    </div>

                </div>
            </div>

            <div id="distribution" class="only-individual-route">
                <div id="distributionVisualization">

                </div>

            </div>

        </div>
        </div>
    </div>
</div>
</div>
</div>


<script src="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/js/select2.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/chart.js@2.9.4"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/chartjs-chart-box-and-violin-plot/4.0.0/Chart.BoxPlot.js"></script>
<script type="text/javascript" src="../jquery.datepick.package-5.1.0/js/jquery.plugin.js"></script>
<script type="text/javascript" src="../jquery.datepick.package-5.1.0/js/jquery.datepick.js"></script>
<script src="../javascript/jquery-timepicker/jquery.timepicker.min.js"></script>

<script type="text/javascript" src="javascript/run-time-helper.js"> </script>
<script type="text/javascript" src="javascript/run-times.js" defer> </script>


</body>
</html>