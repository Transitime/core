<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <%@include file="/template/includes.jsp" %>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Run Time Analysis</title>

    <!-- Load in Select2 files so can create fancy route selector -->
    <link href="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/css/select2.min.css" rel="stylesheet" />
    <script src="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/js/select2.min.js"></script>

    <link href="params/reportParams.css" rel="stylesheet"/>
    <style>
        label {
            text-align: left;
            width: auto;
        }

        hr {
            height: 2px;
            background-color: darkgray;
            margin-right: 5px;
        }

        #paramsSidebar {
            width: 29%;
            height: 100vh;
            margin-left: 10px;
            float:left;
            border-right: 1px solid black;
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

    </style>
    <%--        <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>--%>
    <link rel="stylesheet" type="text/css" href="../jquery.datepick.package-5.1.0/css/jquery.datepick.css">
    <script type="text/javascript" src="../jquery.datepick.package-5.1.0/js/jquery.plugin.js"></script>
    <script type="text/javascript" src="../jquery.datepick.package-5.1.0/js/jquery.datepick.js"></script>
</head>
<body>
    <%@include file="/template/header.jsp" %>
    <div id="paramsSidebar">
        <div id="title" style="text-align: left; font-size:xx-large; margin-bottom: 20px;">
            Run Time Analysis
        </div>

        <div id="paramsFields">
            <%-- For passing agency param to the report --%>
            <input type="hidden" name="a" value="<%= request.getParameter("a")%>">

            <jsp:include page="params/routeAllOrSingle.jsp" />

            <div class="param">
                <label for="direction">Direction:</label>
                <select id="direction" name="direction" disabled="true" style="width: 177px"></select>
            </div>

            <div class="param">
                <label for="startStop">Start Stop:</label>
                <select id="startStop" name="startStop" disabled="true" style="width: 177px"></select>
            </div>

            <div class="param">
                <label for="endStop">End Stop:</label>
                <select id="endStop" name="endStop" disabled="true" style="width: 177px"></select>
            </div>

            <script src="../javascript/jquery-timepicker/jquery.timepicker.min.js"></script>
            <link rel="stylesheet" type="text/css" href="../javascript/jquery-timepicker/jquery.timepicker.css"></link>

            <script>
                $(function() {
                    var calendarIconTooltip = "Popup calendar to select date";

                    $( "#datepicker" ).datepick({
                        dateFormat: "yy-mm-dd",
                        showOtherMonths: true,
                        selectOtherMonths: true,
                        // Show button for calendar
                        buttonImage: "img/calendar.gif",
                        buttonImageOnly: true,
                        showOn: "both",
                        // Don't allow going past current date
                        maxDate: 0,
                        // onClose is for restricting end date to be after start date,
                        // though it is potentially confusing to user
                        rangeSelect: true,
                        showTrigger: '<button type="button" class="trigger">' +
                            '<img src="../jquery.datepick.package-5.1.0/img/calendar.gif" alt="Popup"></button>',
                        onClose: function( selectedDate ) {
                            // Strangely need to set the title attribute for the icon again
                            // so that don't revert back to a "..." tooltip
                            // FIXME $(".ui-datepicker-trigger").attr("title", calendarIconTooltip);
                        }
                    });

                    // Use a better tooltip than the default "..." for the calendar icon
                    $(".ui-datepicker-trigger").attr("title", calendarIconTooltip);

                    $("#beginTime, #endTime").timepicker({timeFormat: "H:i"})
                        .on('change', function(evt) {
                            if (evt.originalEvent) { // manual change
                                // validate that this looks like HH:MM
                                if (!evt.target.value.match(/^(([0,1][0-9])|(2[0-3])):[0-5][0-9]$/))
                                    evt.target.value = evt.target.oldval ? evt.target.oldval : "";
                            }
                            evt.target.oldval = evt.target.value;
                        });

                });
            </script>

            <div class="param">
                <label for="datepicker">Date:</label>
                <input type="text" id="datepicker" name="datepicker"
                       title="The range of dates that you want to examine data for.
                               <br><br> Begin date must be before the end date."
                       size="18"
                       value="Date range" />
            </div>

            <div class="param">
                <label for="beginTime">Begin Time:</label>
                <input id="beginTime" name="beginTime"
                       title="Optional begin time of day to limit query to. Useful if
                                want to see result just for rush hour, for example. Leave blank
                                if want data for entire day.
                                <br/><br/>Format: hh:mm, as in '07:00' for 7AM."
                       size="5"
                       value="" /> <span class="note">(hh:mm)</span>
            </div>

            <div class="param">
                <label for="endTime">End Time:</label>
                <input id="endTime" name="endTime"
                       title="Optional end time of day to limit query to. Useful if
                                want to see result just for rush hour, for example. Leave blank
                                if want data for entire day.
                                <br/><br/>Format: hh:mm, as in '09:00' for 9AM.
                                Use '23:59' for midnight."
                       size="5"
                       value="" /> <span class="note">(hh:mm)</span>
            </div>

            <div class="param">
                <select id="serviceDayType" name="serviceDayType">
                    <option value="">Service Day Type</option>
                    <option value="">All</option>
                    <option value="weekday">Weekday</option>
                    <option value="saturday">Saturday</option>
                    <option value="sunday">Sunday</option>
                </select>
            </div>
        </div>

        <input type="button" id="submit" class="submit" value="Submit" style="margin-top: 10px; margin-bottom: 10px;">
    </div>

    <div id="mainPage" style="width: 69%; height: 100%; display: inline-block; margin-left: 20px;">
        <div id="mainResults">
            <div id="paramDetails" class="paramDetails" style="float: left;">
                <p style='font-size: 0.8em;'></p>
            </div>

            <div id="avgRunTime" style="margin-left: 20px; width: 35%; vertical-align: middle;">
                <p style="font-size: 0.9em;display: inline-block;"></p>
                <p style="font-size: 0.9em;display: inline-block; width: 80px; height: 1.5em;"></p>
            </div>

            <div id="runTimeBreakdown" style="margin-left: 20px; width: 70%; vertical-align: middle;">
                <p style="font-size: 0.9em;display: inline-block;"></p>
                <p style="font-size: 0.9em;display: inline-block; width: 80px; height: 1.5em;"></p>
                <p style="font-size: 0.9em;display: inline-block;"></p>
                <p style="font-size: 0.9em;display: inline-block; width: 80px; height: 1.5em;"></p>
                <p style="font-size: 0.9em;display: inline-block;"></p>
                <p style="font-size: 0.9em;display: inline-block; width: 80px; height: 1.5em;"></p>
            </div>

            <input type="button" id="visualizeButton" class="visualizeButton" value="Visualize trips" style="margin-top: 10px; margin-bottom: 10px;" hidden="true">
        </div>

        <div id="comparisonModal" hidden="true">
            <div id="modalContents" style="margin-right: 30px; margin-left: 30px; margin-top: 10px;">
                <div id="modalHeader" style="text-align: left; vertical-align: middle; font-size: medium">
                    Trip Run Time Comparison
                    <button id='closeModal' type='button' style='float:right; margin-right: -10px;'>&times;</button>
                </div>
                <div id="paramDetailsModal" class="paramDetails" style="margin-top: 20px; margin-bottom: 20px;"></div>

                <script src="../javascript/jquery-timepicker/jquery.timepicker.min.js"></script>
                <link rel="stylesheet" type="text/css" href="../javascript/jquery-timepicker/jquery.timepicker.css"></link>

                <script>
                    $(function() {
                        var calendarIconTooltip = "Popup calendar to select date";

                        $("#modalDatepicker").datepick({
                            dateFormat: "yy-mm-dd",
                            showOtherMonths: true,
                            selectOtherMonths: true,
                            // Show button for calendar
                            buttonImage: "img/calendar.gif",
                            buttonImageOnly: true,
                            showOn: "both",
                            // Don't allow going past current date
                            maxDate: 0,
                            // onClose is for restricting end date to be after start date,
                            // though it is potentially confusing to user
                            rangeSelect: true,
                            showTrigger: '<button type="button" class="trigger">' +
                                '<img src="../jquery.datepick.package-5.1.0/img/calendar.gif" alt="Popup"></button>',
                            onClose: function (selectedDate) {
                                // Strangely need to set the title attribute for the icon again
                                // so that don't revert back to a "..." tooltip
                                // FIXME $(".ui-datepicker-trigger").attr("title", calendarIconTooltip);
                            }
                        });

                        // Use a better tooltip than the default "..." for the calendar icon
                        $(".ui-datepicker-trigger").attr("title", calendarIconTooltip);
                    })
                </script>

                <div style='font-size: medium;'>Select Comparison Range:</div>

                <div class="param" style="display: inline-block;">
                    <label for="modalDatepicker">Date:</label>
                    <input type="text" id="modalDatepicker" name="modalDatepicker"
                           title="The range of dates that you want to examine data for.
                               <br><br> Begin date must be before the end date."
                           size="18"
                           value="Date range" />
                </div>

                <div class="param" style="display: inline-block; margin-left: 30px;">
                    <select id="modalServiceDayType" name="modalServiceDayType">
                        <option value="">Service Day Type</option>
                        <option value="">All</option>
                        <option value="weekday">Weekday</option>
                        <option value="saturday">Saturday</option>
                        <option value="sunday">Sunday</option>
                    </select>
                </div>

                <input type="button" id="modalSubmit" class="submit" value="Submit" style="display: block; margin-top: 20px; margin-bottom: 20px;">
            </div>
        </div>

        <div id="comparisonResults" hidden="true"></div>

        <script src="https://cdn.jsdelivr.net/npm/chart.js@2.8.0"></script>

        <div id="runTimeVisualization" hidden="true">
            <canvas id="visualizationCanvas" height="100" style="margin-top: 10px;"></canvas>
        </div>
    </div>

<script>

    var stops = [];

    $("#route").attr("style", "width: 200px");

    $("#route").change(function() {
        populateDirection();
    })

    $("#direction").change(function() {
        populateStartStop();
    })

    $("#startStop").change(function() {
        populateEndStop();
    })

    function populateDirection() {
        $("#endStop").empty();
        $("#startStop").empty();
        $("#direction").removeAttr('disabled');
        $("#direction").empty();


        $.ajax({
            url: apiUrlPrefix + "/command/headsigns",
            // Pass in query string parameters to page being requested
            data: {r: $("#route").val()},
            // Needed so that parameters passed properly to page being requested
            traditional: true,
            dataType:"json",
            success: function(response) {
                response.headsigns.forEach(function(headsign) {
                    $("#direction").append("<option value='" + headsign.headsign + "'>" + headsign.label + "</option>");
                })
                populateStartStop();
            },
            error: function(response) {
                alert("Error retrieving directions for route " + response.r);
            }
        })
    }

    function populateStartStop() {
        $("#endStop").empty();
        $("#startStop").removeAttr('disabled');
        $("#startStop").empty();

        stops.length = 0;
        var apiResults = [{id: 1, name: "first stop", start: true, end: ["4"]}, {id: 2, name: "second stop", start: true, end: ["1"]}, {id: 3, name: "third stop", start: false, end: ["2"]}, {id: 4, name: "last stop", start: true, end:["1", "2"]}];
        apiResults.forEach(function(stop) {
            stops.push(stop);
            if (stop.start == true) {
                $("#startStop").append("<option value='" + stop.id + "'>" + stop.name + "</option>");
            }
        })
        populateEndStop();
    }

    function populateEndStop() {
        $("#endStop").removeAttr('disabled');
        $("#endStop").empty();

        stops.forEach(function(stop) {
            if (stop.end.includes($("#startStop").val())) {
                $("#endStop").append("<option value='" + stop.id + "'>" + stop.name + "</option>");
            }
        })
    }

    var canvas = $("#visualizationCanvas");
    var barGraph = new Chart(canvas, {
        type: 'horizontalBar',
        data: {
            datasets: [{
                data: [20, 20, 20, 20],
                backgroundColor: '#36509b',
                label: "Fixed"
            },
            {
                data: [13, 11, 10, 11],
                backgroundColor: '#df7f17',
                label: "Variable"
            },
            {
                data: [8, 7, 9, 12],
                backgroundColor: '#8c8c8c',
                label: "Dwell"
            }],
            labels: ['Trip A', 'Trip B', 'Trip C', 'Trip D']
        },
        options: {
            scales: {
                xAxes: [{
                    stacked: true,
                    scaleLabel: {
                        display: true,
                        labelString: "Min"
                    }
                }],
                yAxes: [{
                    stacked: true,
                }]
            },
            legend: {
                position: 'top',
                onClick: function(l) {
                    l.stopPropagation();
                }

            },
            animation: {
                duration: 1,
                onComplete: function () {
                    var chartInstance = this.chart,
                        ctx = chartInstance.ctx;
                    ctx.font = Chart.helpers.fontString(Chart.defaults.global.defaultFontSize, Chart.defaults.global.defaultFontStyle, Chart.defaults.global.defaultFontFamily);
                    ctx.fillStyle = '#000000';
                    ctx.textAlign = 'center';
                    ctx.textBaseline = 'bottom';

                    this.data.datasets.forEach(function (dataset, i) {
                        var meta = chartInstance.controller.getDatasetMeta(i);
                        meta.data.forEach(function (bar, index) {
                            var data = dataset.data[index];
                            ctx.fillText(data, bar._model.x - ((bar._model.x - bar._model.base) / 2), bar._model.y);
                        });
                    });
                }
            }
        },
    });

    $("#submit").click(function() {
        $("#submit").attr("disabled", "disabled");
        $("#runTimeVisualization").hide();
        $("#modalDatepicker").val("Date range")
        $("#comparisonModal").hide();
        $("#comparisonResults").hide();
        $("#comparisonResults").html(
            '<hr>' +

            '<div id="comparisonParams" class="paramDetails" style="float: left;">' +
                '<p style="font-size: 0.8em;"></p>' +
            '</div>' +

            '<div id="comparisonAvgRunTime" style="margin-left: 20px; width: 35%; vertical-align: middle;">' +
                '<p style="font-size: 0.9em;display: inline-block;"></p>' +
                '<p style="font-size: 0.9em;display: inline-block; width: 80px; height: 1.5em;"></p>' +
            '</div>' +

            '<div id="comparisonRunTimeBreakdown" style="margin-left: 20px; width: 70%; vertical-align: middle;">' +
                '<p style="font-size: 0.9em;display: inline-block;"></p>' +
                '<p style="font-size: 0.9em;display: inline-block; width: 80px; height: 1.5em;"></p>' +
                '<p style="font-size: 0.9em;display: inline-block;"></p>' +
                '<p style="font-size: 0.9em;display: inline-block; width: 80px; height: 1.5em;"></p>' +
                '<p style="font-size: 0.9em;display: inline-block;"></p>' +
                '<p style="font-size: 0.9em;display: inline-block; width: 80px; height: 1.5em;"></p>' +
            '</div>' +

            '<input type="button" id="comparisonVisualizeButton" class="visualizeButton" value="Visualize trips" style="margin-top: 10px; margin-bottom: 10px;">'
        );

        request = {}

        if ($("#datepicker").val() == "Date range") {
            var today = new Date();
            var beginDate = endDate = today.getFullYear() + "-"
                + (today.getMonth() <= 10 ? "0" + (today.getMonth() + 1) : (today.getMonth() + 1))
                + "-" + (today.getDate() < 10 ? "0" + today.getDate() : today.getDate());
        } else {
            var dateRangeStrings = $("#datepicker").val().replace(/\s/g, "").split("-");
            var beginYear = "20" + dateRangeStrings[0];
            var endYear = "20" + dateRangeStrings[3];
            var beginDate = [beginYear, dateRangeStrings[1], dateRangeStrings[2]].join("-");
            var endDate = [endYear, dateRangeStrings[4], dateRangeStrings[5]].join("-");
        }

        var beginTime = $("#beginTime").val() == "" ? "00:00:00" : $("#beginTime").val() + ":00";
        var endTime = $("#endTime").val() == "" ? "23:59:59" : $("#endTime").val() + ":00";

        request.beginDate = beginDate;
        request.endDate = endDate;
        request.beginTime = beginTime;
        request.endTime = endTime;
        request.r = $("#route").val();
        request.headsign = $("#direction").val();
        request.serviceType = $("#serviceDayType").val();
        request.startStop = $("#startStop").val();
        request.endStop = $("#endStop").val();

        $.ajax({
            url: apiUrlPrefix + "/report/speedmap/runTime",
            // Pass in query string parameters to page being requested
            data: request,
            // Needed so that parameters passed properly to page being requested
            traditional: true,
            dataType: "json",
            success: function (response) {
                $("#submit").removeAttr("disabled");
                var beginDateArray = request.beginDate.split("-");
                var endDateArray = request.endDate.split("-");
                [beginDateArray[0], beginDateArray[1], beginDateArray[2]] = [beginDateArray[1], beginDateArray[2], beginDateArray[0]];
                [endDateArray[0], endDateArray[1], endDateArray[2]] = [endDateArray[1], endDateArray[2], endDateArray[0]];
                var beginDateString = beginDateArray.join("/");
                var endDateString = endDateArray.join("/");

                var timeRange = request.beginTime + " to " + request.endTime;

                if (beginTime == "00:00:00" && endTime == "23:59:59") {
                    timeRange = "All times";
                }

                var serviceDayString = request.serviceType;

                if (serviceDayString == "") {
                    serviceDayString = "All days";
                }

                $("#paramDetails").html("<p style='font-size: 0.8em;'>Route " + request.r + " to " + request.headsign + " | All stops | " + beginDateString + " to " + endDateString + " | " + timeRange + " | " + serviceDayString + "<a id='compareLink' style='font-size: 0.8em; margin-bottom: 1em; margin-left: 4em; color: blue; text-decoration: underline; cursor: pointer' onclick='openModal()'>Compare</a></p>");
                $("#paramDetailsModal").html("<p style='font-size: 0.7em;'>Route " + request.r + " to " + request.headsign + " | All stops | " + beginDateString + " to " + endDateString + " | " + timeRange + " | " + serviceDayString + "</p>");

                var runTimes = {"avgRunTime": "10.5", "fixed": "8", "variable": "6", "dwell": "8"};

                $("#avgRunTime").html(
                    "<p style='font-size: 0.9em;display: inline-block; vertical-align: middle;'>Average run time</p>" +
                    "<p style='font-size: 0.9em;display: inline-block; margin-left: 50px; width: 80px; height: 1.5em; background-color: gray; vertical-align: middle;'>" + runTimes.avgRunTime + "</p>"
                );

                $("#runTimeBreakdown").html(
                    "<p style='font-size: 0.9em;display: inline-block;'>Fixed</p>" +
                    "<p style='font-size: 0.9em;display: inline-block; margin-left: 50px; width: 80px; height: 1.5em; background-color: gray; vertical-align: middle;'>" + runTimes.fixed + "</p>" +
                    "<p style='font-size: 0.9em;display: inline-block; margin-left: 40px; vertical-align: middle;'>Variable</p>" +
                    "<p style='font-size: 0.9em;display: inline-block; margin-left: 40px; width: 80px; height: 1.5em; background-color: gray; vertical-align: middle;'>" + runTimes.variable + "</p>" +
                    "<p style='font-size: 0.9em;display: inline-block; margin-left: 40px; vertical-align: middle;'>Dwell</p>" +
                    "<p style='font-size: 0.9em;display: inline-block; margin-left: 40px; width: 80px; height: 1.5em; background-color: gray; vertical-align: middle;'>" + runTimes.dwell + "</p>"
                );

                $("#visualizeButton").show();

                alert("Success");
            },
            error: function () {
                $("#submit").removeAttr("disabled");
                alert("Error processing average trip run time.");
            }
        })
    })

    $("#modalSubmit").click(function() {
        $(".submit").attr("disabled", "disabled");
        $("#runTimeVisualization").hide();

        request = {}

        if ($("#modalDatepicker").val() == "Date range") {
            var today = new Date();
            var beginDate = endDate = today.getFullYear() + "-"
                + (today.getMonth() <= 10 ? "0" + (today.getMonth() + 1) : (today.getMonth() + 1))
                + "-" + (today.getDate() < 10 ? "0" + today.getDate() : today.getDate());
        } else {
            var dateRangeStrings = $("#modalDatepicker").val().replace(/\s/g, "").split("-");
            var beginYear = "20" + dateRangeStrings[0];
            var endYear = "20" + dateRangeStrings[3];
            var beginDate = [beginYear, dateRangeStrings[1], dateRangeStrings[2]].join("-");
            var endDate = [endYear, dateRangeStrings[4], dateRangeStrings[5]].join("-");
        }

        var beginTime = $("#beginTime").val() == "" ? "00:00:00" : $("#beginTime").val() + ":00";
        var endTime = $("#endTime").val() == "" ? "23:59:59" : $("#endTime").val() + ":00";

        request.beginDate = beginDate;
        request.endDate = endDate;
        request.beginTime = beginTime;
        request.endTime = endTime;
        request.r = $("#route").val();
        request.headsign = $("#direction").val();
        request.serviceType = $("#modalServiceDayType").val();
        request.startStop = $("#startStop").val();
        request.endStop = $("#endStop").val();

        $.ajax({
            url: apiUrlPrefix + "/report/speedmap/runTime",
            // Pass in query string parameters to page being requested
            data: request,
            // Needed so that parameters passed properly to page being requested
            traditional: true,
            dataType: "json",
            success: function (response) {
                $(".submit").removeAttr("disabled");
                $("#comparisonModal").hide();

                var beginDateArray = request.beginDate.split("-");
                var endDateArray = request.endDate.split("-");
                [beginDateArray[0], beginDateArray[1], beginDateArray[2]] = [beginDateArray[1], beginDateArray[2], beginDateArray[0]];
                [endDateArray[0], endDateArray[1], endDateArray[2]] = [endDateArray[1], endDateArray[2], endDateArray[0]];
                var beginDateString = beginDateArray.join("/");
                var endDateString = endDateArray.join("/");

                var timeRange = request.beginTime + " to " + request.endTime;

                if (beginTime == "00:00:00" && endTime == "23:59:59") {
                    timeRange = "All times";
                }

                var serviceDayString = request.serviceType;

                if (serviceDayString == "") {
                    serviceDayString = "All days";
                }

                $("#comparisonParams").html("<p style='font-size: 0.8em;'>Route " + request.r + " to " + request.headsign + " | All stops | " + beginDateString + " to " + endDateString + " | " + timeRange + " | " + serviceDayString + "<a id='compareLink' style='font-size: 0.8em; margin-bottom: 1em; margin-left: 4em; color: blue; text-decoration: underline; cursor: pointer' onclick='openModal()'>Compare</a></p>");

                var runTimes = {"avgRunTime": "10.5", "fixed": "8", "variable": "6", "dwell": "8"};

                $("#comparisonAvgRunTime").html(
                    "<p style='font-size: 0.9em;display: inline-block; vertical-align: middle;'>Average run time</p>" +
                    "<p style='font-size: 0.9em;display: inline-block; margin-left: 50px; width: 80px; height: 1.5em; background-color: gray; vertical-align: middle;'>" + runTimes.avgRunTime + "</p>"
                );

                $("#comparisonRunTimeBreakdown").html(
                    "<p style='font-size: 0.9em;display: inline-block;'>Fixed</p>" +
                    "<p style='font-size: 0.9em;display: inline-block; margin-left: 50px; width: 80px; height: 1.5em; background-color: gray; vertical-align: middle;'>" + runTimes.fixed + "</p>" +
                    "<p style='font-size: 0.9em;display: inline-block; margin-left: 40px; vertical-align: middle;'>Variable</p>" +
                    "<p style='font-size: 0.9em;display: inline-block; margin-left: 40px; width: 80px; height: 1.5em; background-color: gray; vertical-align: middle;'>" + runTimes.variable + "</p>" +
                    "<p style='font-size: 0.9em;display: inline-block; margin-left: 40px; vertical-align: middle;'>Dwell</p>" +
                    "<p style='font-size: 0.9em;display: inline-block; margin-left: 40px; width: 80px; height: 1.5em; background-color: gray; vertical-align: middle;'>" + runTimes.dwell + "</p>"
                );

                $("#comparisonResults").show();

                alert("Success");
            },
            error: function () {
                $(".submit").removeAttr("disabled");
                alert("Error processing average trip run time.");
            }
        })
    })

    function openModal() {
        $("#comparisonModal").show();
    }

    $("#closeModal").click(function() {
        $("#comparisonModal").hide();
    })

    $(".visualizeButton").click(function() {
        $("#runTimeVisualization").show();
    })

</script>