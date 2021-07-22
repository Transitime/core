<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Run Time Analysis</title>

    <!-- Load in Select2 files so can create fancy route selector -->
    <link href="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/css/select2.min.css" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;700&display=swap" rel="stylesheet" />
    <link rel="stylesheet" type="text/css" href="../jquery.datepick.package-5.1.0/css/jquery.datepick.css" />
    <link rel="stylesheet" type="text/css"  href="../javascript/jquery-timepicker/jquery.timepicker.css"/>
    <link href="params/reportParams.css" rel="stylesheet"/>
</head>

<body class="run-time-screen">
<%@include file="/template/header.jsp" %>
<div class="wrapper">
    <div class="paramsWrapper">
        <div id="paramsSidebar">
            <div class="header-title">
                Prescriptive Run Times
            </div>
            <input type="hidden" name="isAllRoutesDisabled"  class="isAllRoutesDisabled" value="true">
            <div id="paramsFields">
                <%-- For passing agency param to the report --%>
                <input type="hidden" name="a" value="<%= request.getParameter("a")%>">

                <jsp:include page="params/routeAllOrSingle.jsp"/>

                <div class="param individual-route-only">
                    <label for="direction">Direction:</label>
                    <select id="direction" name="direction" disabled="true"></select>
                </div>
                <div class="param individual-route-only">
                    <label for="tripPattern">Trip Pattern:</label>
                    <select id="tripPattern" name="tripPattern" disabled="true"></select>
                </div>

                <div class="param">
                    <label for="serviceDayType">Service Day:</label>
                    <select id="serviceDayType" name="serviceDayType">
                        <option value="weekday">Weekday</option>
                        <option value="saturday">Saturday</option>
                        <option value="sunday">Sunday</option>
                        <span class="select2-selection__arrow">
                                <b role="presentation"></b>
                        </span>
                    </select>
                </div>

                <div class="param">
                    <label for="timeband">Time Band:</label>
                    <select id="timeband" name="timeband"></select>
                </div>
            </div>
            <div class="submitDiv">
                <button id="submit" class="submit">Analyze</button>
            </div>


        </div>
    </div>


    <div id="mainPage" class="scrollable-element inner-spacing">
        <div id="overlay"></div>
        <div id="bars1">
            <span></span>
            <span></span>
            <span></span>
            <span></span>
            <span></span>
        </div>
        <div id="perceptive-header-container" class="param-detail-content"> </div>
        <div class="perceptive-summary-container">
            <div class="individual-route">
                <h3>Trip Run Time Summary</h3>
                <table class="border-table">
                    <tbody><tr>
                        <th>Average</th>
                        <th>Fixed</th>
                        <th>Variable</th>
                        <th>Dwell</th>
                    </tr>
                    <tr class="average-time-details"></tr>
                    </tbody></table>
            </div>

        </div>
        <div class="perceptive-table-container">
            <div class="perceptive-table-flex">

                <div class="adjustment-details inner-flex">

                </div>

                <div class=" inner-flex button-adjustment-details">
                    <div class="otp-containers">
                        <div class="otp-content otp-secondary">
                            <span>OTP (current)</span>
                            <span id="current_otp"></span>
                        </div>
                        <div class="otp-content otp-primary">
                            <span>OTP (predicted)</span>
                            <span id="expected_otp"></span>
                        </div>
                    </div>
                    <div class="table-description-container">
                        <h6>Actual Performance</h6>
                        <div class="legend-container">
                            <div class="color-legend-block early">Short</div>
                            <div class="color-legend-block ontime">Good</div>
                            <div class="color-legend-block late">Long</div>
                        </div>
                    </div>
                </div>
                <div class=" inner-flex export-container">
                    <button class="gtfs-submit ">Export to GTFS</button>
                </div>

            </div>
        </div>
    </div>
</div>

<script src="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/js/select2.min.js"></script>
<script type="text/javascript" src="../jquery.datepick.package-5.1.0/js/jquery.plugin.js"></script>
<script type="text/javascript" src="../jquery.datepick.package-5.1.0/js/jquery.datepick.js"></script>
<script src="../javascript/jquery-timepicker/jquery.timepicker.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/chart.js@2.9.4"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/chartjs-chart-box-and-violin-plot/2.4.0/Chart.BoxPlot.js"></script>
<script type="text/javascript" src="javascript/run-time-helper.js"> </script>
</body>
</html>

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
        var timeRange = request.beginTime + " to " + request.endTime;

        updateParamDetails(request.r, request.headsign, request.tripPattern, beginDateString, endDateString,
            timeRange, serviceDayString, false);
        updateSummaryTable(response.summary)
    }

    function generatePrescriptiveRunTimesTable(response){
        var currentTable = '<table class="border-table">';
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
            + (firstDay.getMonth() <= 10 ? "0" + (firstDay.getMonth() + 1) : (firstDay.getMonth() + 1))
            + "-" + (firstDay.getDate() < 10 ? "0" + firstDay.getDate() : firstDay.getDate());

        params.endDate =  lastDay.getFullYear() + "-"
            + (lastDay.getMonth() <= 10 ? "0" + (lastDay.getMonth() + 1) : (lastDay.getMonth() + 1))
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