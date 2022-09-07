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
            Prediction Accuracy Bucket Chart
        </h4>
        <form class="row" novalidate>

            <input type="hidden" name="date-range-picker" class="isDateRangePicker">

        <div class="row">
            <label class="col-sm-12 col-form-label">Routes</label>
            <input type="hidden" name="isAllRoutesDisabled"  class="isAllRoutesDisabled" value="true">
            <input type="hidden" name="a" value="<%= request.getParameter("a")%>">
            <input type="hidden" name="source" value="<%= request.getParameter("source")%>">
            <input type="hidden" name="chartTitle" value="<%= request.getParameter("chartTitle")%>">
            <jsp:include page="params/routeMultipleNoLabel.jsp" />
        </div>

        <jsp:include page="params/fromDateNumDaysTimeSidePanel.jsp" />

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
        <div class="row">
            <label class="col-sm-12 col-form-label">0-3 Minute Bucket</label>
        </div>
        <div class="row">
            <label class="col-sm-7 col-form-label font-weight-normal">Allowable early (mins)</label>
            <div class="col-sm-5 pad-left-0">
                <input  class="form-control"  id="allowableEarly1" name="allowableEarly1"
                        title="How early a vehicle can arrive compared to the prediction
            and still be acceptable. Must be a positive number to indicate
            early."
                        value="1.0"
                        step="0.5"
                        placeholder="minute"
                        type="number">
            </div>

        </div>
        <div class="row">
            <label class="col-sm-7 col-form-label font-weight-normal">Allowable late (mins)</label>
            <div class="col-sm-5 pad-left-0">
                <input   class="form-control"  id="allowableLate1" name="allowableLate1"
                         title="How late a vehicle can arrive compared to the prediction and still be acceptable. Must be a positive number to indicate late."
                         value="1.0"
                         step="0.5"
                         placeholder="minute"
                         type="number">
            </div>

        </div>



        <div class="row">
            <label class="col-sm-12 col-form-label">3-6 Minute Bucket</label>
        </div>
        <div class="row">
            <label class="col-sm-7 col-form-label font-weight-normal">Allowable early (mins)</label>
            <div class="col-sm-5 pad-left-0">
                <input  class="form-control"  id="allowableEarly2" name="allowableEarly2"
                        title="How early a vehicle can arrive compared to the prediction
            and still be acceptable. Must be a positive number to indicate
            early."
                        value="1.5"
                        step="0.5"
                        placeholder="minute"
                        type="number">
            </div>

        </div>
        <div class="row">
            <label class="col-sm-7 col-form-label font-weight-normal">Allowable late (mins)</label>
            <div class="col-sm-5 pad-left-0">
                <input   class="form-control"  id="allowableLate2" name="allowableLate2"
                         title="How late a vehicle can arrive compared to the prediction and still be acceptable. Must be a positive number to indicate late."
                         value="2.0"
                         step="0.5"
                         type="number"
                         placeholder="minute">
            </div>

        </div>


        <div class="row">
            <label class="col-sm-12 col-form-label">6-12 Minute Bucket</label>
        </div>
        <div class="row">
            <label class="col-sm-7 col-form-label font-weight-normal">Allowable early (mins)</label>
            <div class="col-sm-5 pad-left-0">
                <input  class="form-control"  id="allowableEarly3" name="allowableEarly3"
                        title="How early a vehicle can arrive compared to the prediction
            and still be acceptable. Must be a positive number to indicate
            early."
                        value="2.5"
                        step="0.5"
                        placeholder="minute"
                        type="number">
            </div>

        </div>
        <div class="row">
            <label class="col-sm-7 col-form-label font-weight-normal">Allowable late (mins)</label>
            <div class="col-sm-5 pad-left-0">
                <input   class="form-control"  id="allowableLate3" name="allowableLate3"
                         title="How late a vehicle can arrive compared to the prediction and still be acceptable. Must be a positive number to indicate late."
                         value="3.5"
                         step="0.5"
                         type="number"
                         placeholder="minute">
            </div>

        </div>

        <div class="row">
            <label class="col-sm-12 col-form-label">12 - 20 Minute Bucket</label>
        </div>
        <div class="row">
            <label class="col-sm-7 col-form-label font-weight-normal">Allowable early (mins)</label>
            <div class="col-sm-5 pad-left-0">
                <input  class="form-control"  id="allowableEarly4" name="allowableEarly4"
                        title="How early a vehicle can arrive compared to the prediction
            and still be acceptable. Must be a positive number to indicate
            early."
                        value="4.0"
                        step="0.5"
                        type="number"
                        placeholder="minute">
            </div>

        </div>
        <div class="row">
            <label class="col-sm-7 col-form-label font-weight-normal">Allowable late (mins)</label>
            <div class="col-sm-5 pad-left-0">
                <input   class="form-control" id="allowableLate4" name="allowableLate4"
                         title="How late a vehicle can arrive compared to the prediction and still be acceptable. Must be a positive number to indicate late."
                         value="6.0"
                         step="0.5"
                         type="number"
                         placeholder="minute">
            </div>

        </div>

        <div class="row">
            <label class="col-sm-12 col-form-label">20 - 30 Minute Bucket</label>
        </div>
        <div class="row">
            <label class="col-sm-7 col-form-label font-weight-normal">Allowable early (mins)</label>
            <div class="col-sm-5 pad-left-0">
                <input  class="form-control"  id="allowableEarly5" name="allowableEarly5"
                        title="How early a vehicle can arrive compared to the prediction
            and still be acceptable. Must be a positive number to indicate
            early."
                        value="4.0"
                        step="0.5"
                        type="number"
                        placeholder="minute">
            </div>

        </div>
        <div class="row">
            <label class="col-sm-7 col-form-label font-weight-normal">Allowable late (mins)</label>
            <div class="col-sm-5 pad-left-0">
                <input   class="form-control"  id="allowableLate5" name="allowableLate5"
                         title="How late a vehicle can arrive compared to the prediction and still be acceptable. Must be a positive number to indicate late."
                         value="6.0"
                         step="0.5"
                         type="number"
                         placeholder="minute"
                >
            </div>

        </div>
        <div class="list-group">
            <button class="list-group-item list-group-item-action"  id="submit"   type='submit' >Run Report</button>
        </div>
        </form>

    </div>

    <div class="right-panel  position-relative">

        <div class="list-group toggle-chart d-none">
            <button class="list-group-item list-group-item-action" >Prediction Accuracy Bucket Chart</button>
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

    function drawChart() {
        var chartOptions = {
            //title: 'chart title',
            // titleTextStyle: {fontSize: 28},
            //tooltip: {isHtml: true},
            isStacked: true,
            series: [{'color': '#E84D5F'}, {'color': '#6FD656'}, {'color': '#F0DB56'}],
            legend: 'bottom',
            chartArea: {
                // Use most of available area. But need to not use 100% or else
                // labels won't appear
                width:'90%',
                height:'70%',
                // Make chart a bit graay so that it stands out
                backgroundColor: '#f2f2f2'},
            hAxis: {
                title: 'Actual Time Until Stop Arrival (minutes)',
                // So that last column is labeled
                maxValue: 5,
                // Want a gridline for every minute, not just the default of 5 gridlines
                gridlines: {count: 20},
                // Nice to show a faint line for every 30 seconds as well
            },
            vAxis: {
                title: '% of Predictions Within Range',
                maxValue: 100,
                // Specify ticks so that when column adds up to just over 100% the horizontal
                // part of chart not increased to 120% to accomodate it.
                ticks: [
                    {v:0, f:'0'},
                    {v:20, f:'20'},
                    {v:40, f:'40'},
                    {v:60, f:'60'},
                    {v:80, f:'80'},
                    {v:100, f:'100'}]
            },

        };

        var chart = new google.visualization.ColumnChart(document.getElementById('chart_div'));

        chart.draw(globalDataTable, chartOptions);
        $(".image-container ").addClass("d-none");
        $(".toggle-chart").removeClass("d-none");
        $("#submit").removeAttr("disabled")
        $("#submit").html("Submit");
    }

    $( "#submit" ).click(function(e) {
        e.preventDefault();
        $("#submit").attr("disabled","disabled");
        $("#submit").html("Loading...");

        var beginTime = $("#beginTime").val() == "" ? "00:00" : $("#beginTime").val() ;
        var endTime = $("#endTime").val() == "" ? "23:59" : $("#endTime").val() ;

        request = {};
        request.beginDate = $("#beginDate").val();
        request.beginTime = beginTime;
        request.endTime = endTime;
        request.r = $("#route").val();

        request.predictionType = $("#predictionType").val();

        request.allowableEarly4= $("#allowableEarly4").val();
        request.allowableEarly5 = $("#allowableEarly5").val();
        request.allowableEarly2 = $("#allowableEarly2").val();
        request.allowableEarly3= $("#allowableEarly3").val();
        request.allowableEarly1= $("#allowableEarly1").val();

        request.allowableLate5= $("#allowableLate5").val();
        request.allowableLate4= $("#allowableLate4").val();
        request.allowableLate3= $("#allowableLate3").val();
        request.allowableLate2= $("#allowableLate2").val();
        request.allowableLate1 = $("#allowableLate1").val();

        request.a = $("input[name=a]").val()
        request.source = $("input[name=souce]").val() || 'TransitClock';
        request.s = '';
        request.numDays = 1;
        var chartTitle = $("input[name=chartTitle]").val()

        $.ajax({
            url:  "/web/reports/predAccuracyBucketData.jsp",
            // Pass in query string parameters to page being requested
            data: request,
            // Needed so that parameters passed properly to page being requested
            traditional: true,
            dataType:"json",
            success: function (data){
                globalDataTable = new google.visualization.DataTable(data);
                drawChart();
            }
        })

/*        $.ajax({
            url:  "data/summaryScheduleAdherence.jsp",
            // Pass in query string parameters to page being requested
            data: request,
            // Needed so that parameters passed properly to page being requested
            traditional: true,
            dataType:"json",
            success: function (data){
                console.log(data);
            }
        })
*/



    });

    // Start visualization after the body created so that the
    // page loading image will be displayed right away
    google.load("visualization", "1", {packages:["corechart"]});
</script>
</body>
</html>