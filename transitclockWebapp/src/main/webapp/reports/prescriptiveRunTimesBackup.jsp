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


    <%--        <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>--%>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/page-panels.css">
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
                <label class="col-sm-12 col-form-label">Time Band</label>
                <div class="col-sm-12">
                    <select id="timeband" name="timeband" class="form-select">
                    </select>
                </div>

            </div>




        </form>
        <div class="list-group">
            <button class="list-group-item list-group-item-action"  id="submit" >Submit</button>
        </div>
    </div>
    <div class="right-panel position-relative  no-borders ">
        <div class="image-container full-width-image d-flex justify-content-center grey-bg">
            <div class="position-relative d-flex justify-content-centerd-flex justify-content-center align-items-center img-path">
                <img src="<%= request.getContextPath() %>/reports/images/ontime.png" id="on-time-performance"  class="img-fluid grey-img"/>
                <h1 class=" position-absolute view-form-btn"   >Submit Form to View Data</h1>
            </div>
        </div>
        <div class="d-flex pl-10 flex-column box-shadow">
            <div class="list-group comparsion-button-list m-bt-0 row toggle-chart ">
            <div class="col-xs-12">
                <div id="paramDetails"  class="param-detail-content  route-time-analysis-header bg-65 d-flex list-group-item list-group-item-action justify-content-between">No Routes</div>
            </div>
        </div>

            <div class="row toggle-chart">
                <div class="col-sm-6">
                    <table class="table table-bordered">
                        <tbody><tr>
                            <th>Average</th>
                            <th>Fixed</th>
                            <th>Variable</th>
                            <th>Dwell</th>
                        </tr>
                        <tr class="average-time-details"></tr>
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="row toggle-chart">
                <h5>Trip Run Time Summary</h5>
            </div>
            <div class="row toggle-chart">
                <div class="col-sm-6 adjustment-details inner-flex">


                </div>
                <div class="col-sm-3">

                    <label class="col-sm-12 col-form-label m-bt-20">OTP (current) <span  class="fw-light" id="current_otp"></span></label>
                    <label class="col-sm-12 col-form-label bg-secondary-light m-bt-20 ">OTP (predicted) <span  class="fw-light" id="expected_otp"></span></label>
                    <h6 class="col-sm-12">Actual Performance</h6>
                    <div class="col-sm-12 legend-container d-flex flex-row">
                        <div class="color-legend-block bg-red">Short</div>
                        <div class="color-legend-block bg-green">Good</div>
                        <div class="color-legend-block bg-yellow">Long</div>
                    </div>
                </div>
                <div class="col-sm-2">
                    <div class="list-group">
                        <button class="list-group-item list-group-item-action gtfs-submit " >Export to GTFS</button>
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
<script>

    var stops = {};

    function generateTimeBands(){

        var timebandOptions = [{
            name: "Early AM (00:00 - 06:30)",
            value: "00:00 - 06:30"

        },{
            name: "AM Rush (06:30 - 09:00)",
            value:"06:30 - 09:00"
        },{
            name: "AM Midday (09:00 - 12:00)",
            value:"09:00 - 12:00"
        },{
            name: "PM Midday (12:00 - 15:30)",
            value:"12:00 - 15:30"
        },{
            name:  "PM Rush (15:30 - 18:30)",
            value:"15:30 - 18:30"
        },{
            name: "Late PM (18:30 - 11:59)",
            value:"18:30 - "
        }];


        timebandOptions.forEach(function (eachTime) {
            $("#timeband").append("<option value='" + eachTime.value + "'>" + eachTime.name + "</option>");
        })

        $("#timeband").append(' <span class="select2-selection__arrow"><b role="presentation"></b> </span>');


    }

    function getScheduledType(timeReference){
        if (timeReference <= -6000) {
            return "late";
        } else if (timeReference >= 6000) {
            return "early";
        }
        return "ontime";
    }


    $("#route").change(function () {
        if ($("#route").val() && $("#route").val().trim() != "") {
            $(".individual-route-only").show();

            populateDirection();
        } else {
            $(".individual-route-only").hide();
            $("#direction").empty();
            $("#tripPattern").empty();
            $("#direction").attr("disabled", true);
            $("#tripPattern").attr("disabled", true);
        }
    })

    var highestPoints = [];
    $(".toggle-chart").addClass("d-none");
    $("#submit").click(function () {

        $("#submit").attr("disabled", "disabled");
        $("#submit").html("Loading...");
        $("#overlay").show();
        $("#bars1").show();
        $("#mainPage").addClass("inactive-split")
        $(".wrapper").addClass("split");
        $("#mainResults").hide();

        var request = getParams();
        var dataUrl = apiUrlPrefix +  "/report/runTime/prescriptiveRunTimes";

        $.ajax({
            url: dataUrl,
            // Pass in query string parameters to page being requested
            data: request,
            // Needed so that parameters passed properly to page being requested
            traditional: true,
            dataType: "json",
            success: function (response) {
                resetDisable();
                if (jQuery.isEmptyObject(response)) {
                    alert("No run time information available for selected parameters.");
                } else {
                    var adjustmentsSuccess = response.adjustments && response.adjustments.length > 0;
                    var summarySuccess = response.summary && response.summary.avgRunTime;

                    $(".toggle-chart").removeClass("d-none");
                    $(".image-container").addClass("d-none");
                    if(adjustmentsSuccess && summarySuccess){
                        generateAverageRunTimesTable(request, response);
                        generatePrescriptiveRunTimesTable(response);
                    }
                    else if(!adjustmentsSuccess) {
                        alert("No Prescriptive RunTimes available for selected criteria.");
                    } else if(!summarySuccess) {
                        alert("Unable to retreive Average RunTime information for selected criteria.");
                    }
                }
            },
            error: function (e) {
                resetDisable();
                // $("body").removeClass("loader");
                alert("No Prescriptive RunTimes available for selected criteria.");
            }
        })
    });

    function resetDisable(){
        $("#submit").attr("disabled", false);
        $("#submit").html("Analyze");
        $("#overlay").hide();
        $("#mainPage").removeClass("inactive-split")
        $("#bars1").hide();
    }
    function generateAverageRunTimesTable(request, response){
        var beginDateArray = request.beginDate.split("-");
        var endDateArray = request.endDate.split("-");

        [beginDateArray[0], beginDateArray[1], beginDateArray[2]] = [beginDateArray[1], beginDateArray[2], beginDateArray[0]];
        [endDateArray[0], endDateArray[1], endDateArray[2]] = [endDateArray[1], endDateArray[2], endDateArray[0]];

        var beginDateString = beginDateArray.join("/");
        var endDateString = endDateArray.join("/");

        var serviceDayString = request.serviceType;

        if (serviceDayString == "") {
            serviceDayString = "All days";
        }
        var selectedRoute = "All routes";
        if(request.r){
            for( var i = 0; i < route.options.length; i++){
                var eachOption = route.options[i];
                if(request.r === eachOption.value){
                    selectedRoute = eachOption.text;
                }
            }
        }

        var timeRange = request.beginTime + " to " + request.endTime;
        var selectedDate = beginDateString + " - " + endDateString;
        var contentTripHeader = "<div class='route-time-analysis-header-param'>Analysis Details :  "+selectedRoute+"</div>";
        contentTripHeader += "<div class='route-time-analysis-header-param'>Date : "+selectedDate+"</div>";
        contentTripHeader += "<div class='route-time-analysis-header-param'>Time : "+timeRange+"</div>";
        contentTripHeader += "<div class='route-time-analysis-header-param'>Service Day : "+serviceDayString.toUpperCase()+"</div>";

        $(".route-time-analysis-header").html(contentTripHeader);

        /*updateParamDetails(request.r, request.headsign, request.tripPattern, beginDateString, endDateString,
            timeRange, serviceDayString, false);*/
        updateSummaryTable(response.summary)
    }

    function generatePrescriptiveRunTimesTable(response){
        var currentTable = '<table class="table table-bordered">';
        currentTable += '<tbody><tr><th>Stop</th><th>Scheduled</th><th>Adjustment</th></tr>';

        var totalScheduleMin = 0;
        var totalAdjustmentMin = 0;

        response.adjustments.forEach(function(eachAdjustment){

            var scheduleMin  = parseFloat((eachAdjustment.schedule / 60000).toFixed(1));
            var adjustment  = parseFloat((eachAdjustment.adjustment / 60000).toFixed(1));
            var sheduledClassName = getScheduledType(eachAdjustment.adjustment);

            totalScheduleMin += scheduleMin;
            totalAdjustmentMin += adjustment;

            if(adjustment > 0){
                var adjustment = "+" + adjustment;
            }

            currentTable += "<tr><td>"+eachAdjustment.stop+"</td>";
            currentTable += '<td class="'+sheduledClassName+'">'+scheduleMin+' min</td>';
            currentTable += "<td>"+adjustment+" min</td>";
            currentTable += "</tr>";

        });

        totalAdjustmentMin = totalAdjustmentMin.toFixed(1);
        if(totalAdjustmentMin > 0){
            var totalAdjustmentMin = "+" + totalAdjustmentMin;
        }

        currentTable += "<tr><td>Total For All Stops</td>";
        currentTable += "<td>"+totalScheduleMin+" min</td>";
        currentTable += "<td>"+totalAdjustmentMin+" min</td>";
        currentTable += "</tr>";

        currentTable += '</tbody></table>';
        $("#current_otp").html(response.current_otp);
        $("#expected_otp").html(response.expected_otp);
        $(".adjustment-details").html(currentTable);
    }



    function getParams() {


        var routeName = $("#route").val().trim() == "" ? "" : $("#route").val();
        var directionName = $("#direction").val();

        params = {};


        var timeBand = $("#timeband").val() == null ? "00:00-06:30": $("#timeband").val();


        var firstDay = new Date();
        firstDay.setDate(firstDay.getDate() - 30);

        var lastDay = new Date();

        params.beginDate =  firstDay.getFullYear() + "-"
            + (firstDay.getMonth() < 9 ? "0" + (firstDay.getMonth() + 1) : (firstDay.getMonth() + 1))
            + "-" + (firstDay.getDate() < 10 ? "0" + firstDay.getDate() : firstDay.getDate());

        params.endDate =  lastDay.getFullYear() + "-"
            + (lastDay.getMonth() < 9 ? "0" + (lastDay.getMonth() + 1) : (lastDay.getMonth() + 1))
            + "-" + (lastDay.getDate() < 10 ? "0" + lastDay.getDate() : lastDay.getDate());


        params.beginTime = (timeBand.split("-")[0]).trim()+":00";
        params.endTime = timeBand.split("-")[1] == " " ? "": (timeBand.split("-")[1]).trim()+":00";
        params.r = routeName
        params.headsign = directionName;
        params.serviceType = $("#serviceDayType").val();
        params.tripPattern =  $("#tripPattern").val() == null ? "" : $("#tripPattern").val();

        if(directionName == null){
            params.headsign = "";
            params.directionId= "";
        } else {
            var directionJson = JSON.parse($("#direction").val());
            params.headsign = directionJson.headsign;
            params.directionId = directionJson.directionId;
        }

        return params;
    }

    generateTimeBands();

    $(".gtfs-submit").click(function () {
        $(".gtfs-submit").attr("disabled", "disabled");
        var request = getParams();
        var type = "text/csv";
        var filename = "stop_times.txt";
        var dataUrl = apiUrlPrefix +  "/report/runTime/prescriptiveRunTimesSchedule";

        $.ajax({
            url: dataUrl,
            accepts:{text:type},
            // Pass in query string parameters to page being requested
            data: request,
            // Needed so that parameters passed properly to page being requested
            traditional: true,
            success: function (data) {
                $(".gtfs-submit").attr("disabled", false);
                var blob = new Blob([data], { type: type });
                saveFile(filename, type, blob);
            },
            error: function (e) {
                console.log(e);
                $(".gtfs-submit").attr("disabled", false);
                alert("Unable to export Prescriptive RunTimes GTFS Schedule.");
            }
        })

    });

    function saveFile (name, type, data) {
        if (data !== null){
            if(window.navigator && window.navigator.msSaveOrOpenBlob){
                return navigator.msSaveBlob(new Blob([data], { type: type }), name);
            }
            else{
                var a = $("<a style='display: none;'/>");
                var url = window.URL.createObjectURL(new Blob([data], {type: type}));
                a.attr("href", url);
                a.attr("download", name);
                $("body").append(a);
                a[0].click();
                window.URL.revokeObjectURL(url);
                a.remove();
            }
        } else {
            alert("Unable to export Prescriptive RunTimes GTFS Schedule.");
        }
    }

</script>

</body>