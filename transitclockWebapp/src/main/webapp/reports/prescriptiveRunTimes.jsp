<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <%@include file="/template/includes.jsp" %>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Run Time Analysis</title>

    <!-- Load in Select2 files so can create fancy route selector -->
    <link href="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/css/select2.min.css" rel="stylesheet"/>
    <script src="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/js/select2.min.js"></script>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;700&display=swap" rel="stylesheet">

    <link rel="stylesheet" type="text/css" href="../jquery.datepick.package-5.1.0/css/jquery.datepick.css">

    <script type="text/javascript" src="../jquery.datepick.package-5.1.0/js/jquery.plugin.js"></script>
    <script type="text/javascript" src="../jquery.datepick.package-5.1.0/js/jquery.datepick.js"></script>
    <script src="../javascript/jquery-timepicker/jquery.timepicker.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/chart.js@2.9.4"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/chartjs-chart-box-and-violin-plot/2.4.0/Chart.BoxPlot.js"></script>

    <link href="params/reportParams.css" rel="stylesheet"/>
    <link rel="stylesheet" type="text/css"  href="../javascript/jquery-timepicker/jquery.timepicker.css"></link>
    <style>

        .wrapper {
            background: #f1f1f1f1;
            font-family: 'Montserrat', sans-serif !important;
            height: 100vh;
            width: 100vw;
            position: fixed;
            display: flex;
            flex-flow: row;
        }

        .wrapper.split {
            flex-flow: row;
        }

        #title {
            margin-top: 40px;
            margin-bottom: 2px;
            font-weight: normal;
            text-align: center;
            background: #019932;
            color: white;
            padding: 8px;
            font-size: 24px;
            width: -webkit-fill-available;
            display: inline-block !important;
        }

        #routesDiv {
            font-family: 'Montserrat', sans-serif !important;
        }

        input {
            -webkit-appearance: none;
            width: -webkit-fill-available;
            border: 1px solid #c1c1c1c1;
            background-color: #fff;
            line-height: 1.5;
            box-shadow: 0px 1px 4px rgba(0, 0, 0, 0.33);
            color: #444;
            padding: 0px 6px;
            font-family: 'Montserrat', sans-serif;
            font-size: 16px;
        }

        input::placeholder {
            color: #44444469;
        }

        select:not(.datepick-month-year):not(.datepick-month-year) lect {
            width: -webkit-fill-available;
            border: 1px solid #c1c1c1c1;
            background-color: #fff;
            line-height: 1.5;
            box-shadow: 0px 1px 4px rgba(0, 0, 0, 0.33);
            color: #444;
            padding: 0px 6px;
            font-family: 'Montserrat', sans-serif;
            font-size: 16px;
        }

        label {
            text-align: left;
            width: auto;
        }

        hr {
            height: 2px;
            background-color: darkgray;
            margin-right: 5px;
        }

        .paramsWrapper {
            width: 100%;
            height: 100vh;
            transition: width .75s ease-in-out, max-width .75s ease-in-out;
            font-size: 16px;
            background-color: #fff;
            border: #969696 solid 1px;
            box-shadow: 3px 3px 4px rgba(0, 0, 0, 0.3);
            /* align-self: center; */
            position: relative;
            z-index: 8;
        }

        .split .paramsWrapper {
            width: 22%;
        }

        #paramsSidebar {
            height: 100vh;
            max-width: 420px;
            width: 100%;
            margin: auto;
            display: flex;
            align-items: center;
            flex-flow: column;
            background-color: #fff;
            z-index: 2;
        }

        .split #paramsSidebar {
        }

        #paramsSidebar > * {
            display: flex;
        }

        #paramsFields {
            flex-flow: column;
            width: 90%;
            max-width: 30vw;
        }

        .param {
            display: flex;
            flex-flow: row;
            justify-content: space-between;
            margin-top: 6%;
        }

        .param-modal {
            margin: 10px 0px;
        }

        .param, .param-modal > * {
            font-size: 14px;
        }

        .param > label, .param-modal > label {
            width: 130px;
        }


        .param > span {
            font-weight: 500;
            padding-bottom: 12px;
        }

        .param > input, .param > select {
            height: 30px;
        }

        .param-modal > input, .param-modal > select {
            height: 30px;
            width: 200px;
        }

        .pair {
            display: flex;
            flex-flow: row;
            justify-content: space-between;
            margin-bottom: 6px;
        }

        .vertical {
            flex-flow: column;
            margin-top: 8%;
            /* background-color: #f1f1f1f1; */
            padding: 10px 0px;
        }


        /*#paramsSidebar {*/
        /*width: 25%;*/
        /*height: 100vh;*/
        /*margin-left: 10px;*/
        /*float:left;*/
        /*border-right: 1px solid black;*/
        /*}*/

        #mainPage {
            visibility: hidden;
            opacity: 0;
            display: none;
            margin-left: 2vw;
            margin-top: 20vh;
            height: 100vh;
            width: 90%;
            max-width: 1250px;
            padding: 10px 30px;
            background-color: #fff;
            border-radius: 4px;
            box-shadow: 0px 4px 8px rgba(0, 0, 0, 0.3);
            transition: visibility .25s .75s ease-in-out, opacity .25s .75s ease-in-out;
        }

        .split #mainPage {
            display: inline-block;
            position: relative;
            visibility: visible;
            opacity: 1;
            margin-top: 2vh;
        }

        .inactive {
            filter: blur(2px) grayscale(100%);
        }

        #comparisonModal {
            width: 40%;
            height: 40%;
            z-index: 999;
            border: 1px solid black;
            background-color: white;
            transition: all .5s ease;
            position: absolute;
            left: 44%;
        }

        #serviceDayType {
            width: 100%;
            height: 30px;
            margin-top: 0px;
            box-shadow: 0px 1px 4px #69696969;
            font-family: 'Montserrat', sans-serif;

        }

        #beginTime, #endTime {
            width: 50%;
        }


        .submit {
            margin: 40px 24px;
            background-color: #029932;
            cursor: pointer;
            width: 210px;
            padding: 5px 70px;
            color: #fff;
            font-family: 'Montserrat', sans-serif;
            box-shadow: 0 4px rgba(127, 127, 127, 0.8);
        }

        .submit:hover {
            background-color: #02772c;
        }

        .submit:active {
            box-shadow: 0 1px rgba(127, 127, 127, 0.33);
            transform: translateY(3px);
            outline: none;
        }
        .late{
            background-color:#E6D83E;
        }
        .early{
            background-color: #E34B71;
        }
        .ontime{
            background-color:#37E627;
        }
        .perceptive-table-flex{
            display: flex;
            flex-direction: row;
            width: 100%;
            margin-top: 20px;
            align-items: flex-start;
            justify-content: space-between;
        }

        .color-legend-block {
            padding: 10px;
            text-align: center;
            width: 50px;
            margin: 2.5px;
        }

        .legend-container {
            display: flex;
            margin: 2px;
            width: 100%;
            align-items: center;
            justify-content: space-between;
        }

        .otp-content {
        //  background-color: #e5e5e5;
            padding: 5px;
        // border: 1px solid #e5e5e5;
            margin: 10px 0;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }
        .otp-primary {
            background-color: #e5e5e5;
            border: 1px solid #e5e5e5;
        }
        .gtfs-submit {
            margin: 40px 24px;
            background-color: #029932;
            cursor: pointer;
            width: 210px;
            padding: 10px 20px;
            color: #fff;
            font-family: 'Montserrat', sans-serif;
            box-shadow: 0 4px rgb(127 127 127 / 80%);
        }

    </style>

</head>
<body>
<%@include file="/template/header.jsp" %>


<div class="wrapper">
    <div class="paramsWrapper">
        <div id="paramsSidebar">
            <div id="title">
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
        <div id="perceptive-header-container"> </div>
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

    $("#route").attr("style", "width: 200px");

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

    $("#direction").change(function () {
        populateTripPattern();
    })

    var highestPoints = [];

    function msToMin(data) {
        var highest = 0;

        for (var i = 0 in data) {
            data[i] = parseFloat((data[i] / 60000).toFixed(1));
            if (data[i] > highest) {
                highest = data[i];
            }
        }

        highestPoints.push(highest);
        return data;
    }
    $("#submit").click(function () {

        $("#submit").attr("disabled", "disabled");
        $("#submit").html("Loading...");
        // $("body").addClass("loader");
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
                $("#submit").attr("disabled", false);
                $("#submit").html("Analyze");
              //  $("body").removeClass("loader");
                $("#overlay").hide();
                $("#mainPage").removeClass("inactive-split")
                $("#bars1").hide();
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
                $("#submit").attr("disabled", false);
                $("#submit").html("Analyze");
                $("#overlay").hide();
                $("#bars1").hide()
                $("#mainPage").removeClass("inactive-split")
                // $("body").removeClass("loader");
                alert("No Prescriptive RunTimes available for selected criteria.");
            }
        })
    });

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

        updateParamDetails(request.r, request.headsign, request.tripPattern, beginDateString, endDateString,
            request.beginTime, request.endTime, serviceDayString);

        var avgRunTime = typeof (response.summary.avgRunTime) == 'undefined' ? "N/A" : (response.summary.avgRunTime / 60000).toFixed(1) + " min";
        var avgFixed = typeof (response.summary.fixed) == 'undefined' ? "N/A" : (response.summary.fixed / 60000).toFixed(1) + " min";
        var avgVar = typeof (response.summary.variable) == 'undefined' ? "N/A" : (response.summary.variable / 60000).toFixed(1) + " min";
        var avgDwell = typeof (response.summary.dwell) == 'undefined' ? "N/A" : (response.summary.dwell / 60000).toFixed(1) + " min";

        var tableTD = "<td>"+avgRunTime+"</td>";
        tableTD += "<td>"+avgFixed+"</td>";
        tableTD += "<td>"+avgVar+"</td>";
        tableTD += "<td>"+avgDwell+"</td>";

        $(".average-time-details").html(tableTD);
    }

    function updateParamDetails(route, headsign, tripPattern, beginDateString, endDateString, beginTime,
                                endTime, serviceDayString){
        $("#perceptive-header-container").html("<p style='font-size: 0.8em;'>" +
            (!route || route == "" ? "All routes" : "Route " + route) + " to " +
            (!headsign || headsign == "" ? "All directions" : headsign) + " | " +
            //(!tripPattern || tripPattern == "" ? "All Trip Patterns" : tripPattern) + " | " +
            beginDateString + " to " + endDateString +  " | " +
            beginTime + " - " + endTime +
            (!serviceDayString || serviceDayString == "" ? "" : " | " + serviceDayString) +
            "</p>");
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

    function populateDirection() {

        $("#direction").removeAttr('disabled');
        $("#direction").empty();
        $("#tripPattern").empty();


        $.ajax({
            url: apiUrlPrefix + "/command/headsigns",
            // Pass in query string parameters to page being requested
            data: {
                r: $("#route").val(),
                formatLabel: false
            },
            // Needed so that parameters passed properly to page being requested
            traditional: true,
            dataType: "json",
            success: function (response) {
                response.headsigns.forEach(function (headsign) {
                    var headsignDirection = new Object();
                    headsignDirection.headsign = headsign.headsign;
                    headsignDirection.directionId = headsign.directionId;
                    var headSignDirectionVal = JSON.stringify(headsignDirection);

                    $("#direction").append('<option value=\'' + headSignDirectionVal + '\'>' + headsign.label + '</option>');
                })
                populateTripPattern();
            },
            error: function (response) {
                alert("Error retrieving directions for route " + response.r);
            }
        })
    }


    function populateTripPattern() {
        $("#tripPattern").empty();

        var direction = JSON.parse($("#direction").val());

        var request = {};

        request.a = 1;
        request.r = $("#route").val();
        request.headsign = direction.headsign;
        request.directionId = direction.directionId;
        request.includeStopPaths = 'false';

        $.ajax({
            // The page being requested
            url: apiUrlPrefix + "/command/tripPatterns",
            // Pass in query string parameters to page being requested
            data: request,
            // Needed so that parameters passed properly to page being requested
            traditional: true,
            dataType: "json",
            async: true,
            // When successful process JSON data
            success: function (resp) {
                if (resp.tripPatterns.length == 0) {
                    alert("No trip pattern data for selected route and headsign.");
                    $("#submit").attr("disabled", true);
                    $("#submit").html("Loading");
                    // $("body").addClass("loader");

                } else {
                    $("#tripPattern").removeAttr('disabled');
                    $("#submit").removeAttr('disabled');
                    $("#submit").html("Analyze");
                    // $("body").removeClass("loader");
                    resp.tripPatterns.forEach(function (tripPattern) {
                        $("#tripPattern").append("<option value='" + tripPattern.id + "'>" + tripPattern.firstStopName + ' to ' + tripPattern.lastStopName + "</option>");
                    })

                }

            },
            // When there is an AJAX problem alert the user
            error: function (request, status, error) {
                alert(error + '. ' + request.responseText);
                $("#submit").attr("disabled", false);
                $("#submit").html("Analyze");
               // $("body").removeClass("loader");
            }
        });
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