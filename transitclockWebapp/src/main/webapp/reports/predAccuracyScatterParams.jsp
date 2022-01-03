<%@ page import="org.transitclock.utils.web.WebUtils" %>

<%@page import="org.transitclock.db.webstructs.WebAgency"%>
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
    String sourceParam = request.getParameter("source");
    String chartTitle = "Prediction Accuracy Bucket Chart for "
            + WebAgency.getCachedWebAgency(agencyId).getAgencyName();
%>
<html>
<head>
    <%@include file="/template/includes.jsp" %>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Specify Parameters</title>

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
<body>
<%@include file="/template/header.jsp" %>
<div class="panel split overflow-x-hidden">
    <div class="left-panel">
        <h4 class="page-title">
            Prediction Accuracy Scatter Chart
        </h4>
        <form class="row" novalidate>

            <input type="hidden" name="date-range-picker" class="isDateRangePicker">

            <div class="row">
                <input type="hidden" name="isAllRoutesDisabled"  class="isAllRoutesDisabled" value="true">
                <input type="hidden" name="a" value="<%= request.getParameter("a")%>">
                <input type="hidden" name="routeById"  class="routeById" value="true">
                <input type="hidden" name="source" value="<%= request.getParameter("source")%>">
                <input type="hidden" name="chartTitle" value="<%= request.getParameter("chartTitle")%>">
                <jsp:include page="params/routeAllOrSingleNew.jsp" />
            </div>

            <jsp:include page="params/fromDateNumDaysTime.jsp" />


            <div class="row">
                <label class="col-sm-12 col-form-label">Provide tooltip info</label>
                <div class="col-sm-12">
                    <select id="tooltips" name="tooltips"
                            class="form-select"
                            title="If set to True then provides detailed
      information on data through tooltip. Can be useful but if processing
      large amounts of data can slow down the query.">
                        <option value="true">True</option>
                        <option value="false">False</option>

                        <span class="select2-selection__arrow"><b role="presentation"></b></span>
                    </select>
                </div>

            </div>

            <div class="row">
                <label class="col-sm-12 col-form-label">Prediction Type</label>
                <div class="col-sm-12">
                    <select id="predictionType" name="predictionType"
                            class="form-select"
                            title="Specifies whether or not to show prediction accuracy for
						predictions that were affected by a layover. Select 'All' to show
						data for predictions, 'Affected by layover' to only see data where
						predictions affected by when a driver is scheduled to leave a layover,
						or 'Not affected by layover' if you only want data for predictions
						that were not affected by layovers.">

                        <option value="">All</option>
                        <option value="AffectedByWaitStop">Affected by layover</option>
                        <option value="NotAffectedByWaitStop">Not affected by layover</option>

                        <span class="select2-selection__arrow"><b role="presentation"></b></span>
                    </select>
                </div>

            </div>








            <div class="list-group">
                <button class="list-group-item list-group-item-action"  id="submit"   type='submit' >Run Report</button>
            </div>
        </form>

    </div>

    <div class="right-panel  position-relative">

        <div class="list-group toggle-chart d-none">
            <button class="list-group-item list-group-item-action" >Prediction Accuracy Scatter Chart</button>
        </div>
        <div class="image-container full-width-image d-flex justify-content-center grey-bg">
            <div class="position-relative d-flex justify-content-centerd-flex justify-content-center align-items-center img-path">
                <img src="<%= request.getContextPath() %>/reports/images/ontime.png" id="on-time-performance"  class="img-fluid grey-img"/>
                <h1 class=" position-absolute view-form-btn"   >Submit Form to View Data</h1>
            </div>
        </div>

        <div id="chart_div" class="row" style="height: 500px;"></div>


    </div>

</div>
<script type="text/javascript" src="https://www.google.com/jsapi"></script>

<script>

    var globalTimer;
    window.onresize = function () {
        clearTimeout(globalTimer);
        globalTimer = setTimeout(drawChart, 100)
    };

    var globalDataTable = null;


    /* Actualy draws the chart */
    function drawChart(request) {

        var sourceColor = (request && (request.source==null || request.source != "Other") ) ? "blue" : "red" ;
        var chartOptions = {
            title: '<%= chartTitle %>',
            titleTextStyle: {fontSize: 28},
            // Could use html tooltips so can format them but for now using regular ones
            // FIXME tooltip: {isHtml: false},
            hAxis: {
                title: 'Prediction Time (minutes)',
                minValue: 0,
                maxValue: 1200,
                ticks: [
                    {v:60, f:'1'},
                    {v:120, f:'2'},
                    {v:180, f:'3'},
                    {v:240, f:'4'},
                    {v:300, f:'5'},
                    {v:360, f:'6'},
                    {v:420, f:'7'},
                    {v:480, f:'8'},
                    {v:540, f:'9'},
                    {v:600, f:'10'},
                    {v:660, f:'11'},
                    {v:720, f:'12'},
                    {v:780, f:'13'},
                    {v:840, f:'14'},
                    {v:900, f:'15'},
                    {v:960, f:'16'},
                    {v:1020, f:'17'},
                    {v:1080, f:'18'},
                    {v:1140, f:'19'},
                    {v:1200, f:'20'}]
            },
            vAxis: {title: 'Prediction Accuracy (secs) (postive means vehicle later than predicted)',
                // Try to show accuracy on a consistent vertical axis and
                // divide into minutes. This unfortunately won't work well
                // if values are greater than 360 because then chart will
                // autoscale but will still be using 13 gridlines
                minValue: -360,
                maxValue: 360,
                gridlines: {count: 13},
                // Nice to show a faint line for every 30 seconds as well
                minorGridlines: {count: 1}
            },
            // Usually will first be displaying Transitime predictions and
            // those will get the first color. If both Transitime and Tther
            // predictions shown then the Other ones will get the second color.
            // But want color for the Other predictions to be consistent
            // whether only Other predictions or both Other and Transitime ones
            // are shown. Therefore do something fancy here for consistency.
            series: [{'color':  sourceColor},{'color': 'red'}],
            legend: 'none',
            // Use small points since have lots of them
            pointSize: 1,
            dataOpacity: 0.4,

            // Draw a trendline for data series 0
            trendlines: {
                0: {
                    color: 'red',
                    lineWidth: 3,
                    visibleInLegend: true,
                    opacity: 1,
                }
            },
            // Need to not use 100% or else labels won't appear
            chartArea: {width:'90%', height:'80%', backgroundColor: '#f2f2f2'},
            // Allow zooming
            //explorer: { actions: ['dragToZoom', 'rightClickToReset'] }
        };

        var chart = new google.visualization.ScatterChart(document.getElementById('chart_div'));

        chart.draw(globalDataTable, chartOptions);


        $(".image-container ").addClass("d-none");
        $(".toggle-chart").removeClass("d-none")
    }


    $( "#submit" ).click(function(e) {
        e.preventDefault();

        var beginTime = $("#beginTime").val() == "" ? "00:00" : $("#beginTime").val() ;
        var endTime = $("#endTime").val() == "" ? "23:59" : $("#endTime").val() ;

        request = {};
        request.beginDate = $("#beginDate").val();
        request.beginTime = beginTime;
        request.endTime = endTime;
        request.r = $("#route").val();

        request.predictionType = $("#predictionType").val();


       request.tooltips=$("#tooltips").val();

        request.a = $("input[name=a]").val()
        request.source = $("input[name=souce]").val() || 'TransitClock';
        request.s = '';
        request.numDays = 1;
        var chartTitle = $("input[name=chartTitle]").val()

        $.ajax({
            url:  "/web/reports/predAccuracyScatterData.jsp",
            // Pass in query string parameters to page being requested
            data: request,
            // Needed so that parameters passed properly to page being requested
            traditional: true,
            dataType:"json",
            success: function (data){
                globalDataTable = new google.visualization.DataTable(data);
                drawChart(request);
            }
        })






    });

    // Start visualization after the body created so that the
    // page loading image will be displayed right away
    google.load("visualization", "1", {packages:["corechart"]});
</script>
</body>
</html>