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
                width: 25%;
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

                <br>

                <div id="avgRunTime" style="display: inline-block; margin-left: 20px; width: 90%; vertical-align: middle;">
                    <p style="font-size: 0.8em;display: inline-block;"></p>
                    <p style="font-size: 0.8em;display: inline-block; width: 60px; height: 1.5em;"></p>
                    <p style="font-size: 0.8em;display: inline-block;"></p>
                    <p style="font-size: 0.8em;display: inline-block; width: 60px; height: 1.5em;"></p>
                    <p style="font-size: 0.8em;display: inline-block;"></p>
                    <p style="font-size: 0.8em;display: inline-block; width: 60px; height: 1.5em;"></p>
                    <p style="font-size: 0.8em;display: inline-block;"></p>
                    <p style="font-size: 0.8em;display: inline-block; width: 60px; height: 1.5em;"></p>
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
    </body>
</html>

<script>

    var stops = {};

    $("#route").attr("style", "width: 200px");

    $("#route").change(function() {
        if ($("#route").val().trim() != "") {
            populateDirection();
        }
        else {
            $("#direction").empty();
            $("#endStop").empty();
            $("#startStop").empty();
            $("#direction").attr("disabled", true);
            $("#endStop").attr("disabled", true);
            $("#startStop").attr("disabled", true);
        }
    })

    $("#direction").change(function() {
        populateStartStop();
    })

    $("#startStop").change(function() {
        populateEndStop(stops);
    })

    function populateDirection() {

        $("#submit").attr("disabled", true);

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
                $("#submit").attr("disabled", false);
            }
        })
    }

    function populateStartStop() {
        $("#endStop").empty();
        $("#startStop").empty();

        stops = {};
        var request = {};

        request.a = 1;
        request.r = $("#route").val();
        request.headsign = $("#direction").val();

        $.ajax({
            // The page being requested
            url: "stopsJsonData.jsp",
            // Pass in query string parameters to page being requested
            data: request,
            // Needed so that parameters passed properly to page being requested
            traditional: true,
            dataType: "json",
            async: true,
            // When successful process JSON data
            success: function (resp) {
                if (resp.data.length == 0) {
                    alert("No stop data for selected route and headsign.");
                    $("#submit").attr("disabled", false);
                }
                else {
                    $("#startStop").removeAttr('disabled');

                    var tripId;
                    var first;
                    var last;

                    resp.data.forEach(function (stop) {
                        if (stop.tripPatternId == tripId) {
                            last = stop;
                        } else {
                            if (typeof (last) == 'undefined') {
                                first = stop;
                            } else {
                                if (typeof (stops[first.id]) == 'undefined') {
                                    stops[first.id] = {name: first.name, endStops: [{id: last.id, name: last.name}]};
                                } else {
                                    stops[first.id].endStops.push({id: last.id, name: last.name});
                                }
                                first = stop;
                            }
                            tripId = stop.tripPatternId;
                        }
                    })

                    if (typeof (stops[first.id]) == 'undefined') {
                        stops[first.id] = {name: first.name, endStops: [{id: last.id, name: last.name}]};
                    } else {
                        stops[first.name].endStops.push({id: last.id, name: last.name});
                    }

                    Object.entries(stops).forEach(function (stop) {
                        $("#startStop").append("<option value='" + stop[0] + "'>" + stop[1].name + "</option>");
                    })

                    populateEndStop(stops);
                }

            },
            // When there is an AJAX problem alert the user
            error: function (request, status, error) {
                alert(error + '. ' + request.responseText);
                $("#submit").attr("disabled", false);
            }
        });
    }

    function populateEndStop(stops) {
        $("#endStop").removeAttr('disabled');
        $("#endStop").empty();

        stops[$("#startStop").val()].endStops.forEach(function(endStop) {
            $("#endStop").append("<option value='" + endStop.id + "'>" + endStop.name + "</option>");
        })
        $("#submit").attr("disabled", false);
    }

    function getParams(modal) {
        var datepicker, serviceTypeSelector;
        if (modal) {
            datepicker = "modalDatepicker";
            serviceTypeSelector = "modalServiceDayType";
        }
        else {
            datepicker = "datepicker";
            serviceTypeSelector = "serviceDayType";
        }

        if ($("#" + datepicker).val() == "Date range") {
            var today = new Date();
            var beginDate = endDate = today.getFullYear() + "-"
                + (today.getMonth() <= 10 ? "0" + (today.getMonth() + 1) : (today.getMonth() + 1))
                + "-" + (today.getDate() < 10 ? "0" + today.getDate() : today.getDate());
        } else {
            var dateRangeStrings = $("#" + datepicker).val().replace(/\s/g, "").split("-");
            var beginYear = "20" + dateRangeStrings[0];
            var endYear = "20" + dateRangeStrings[3];
            var beginDate = [beginYear, dateRangeStrings[1], dateRangeStrings[2]].join("-");
            var endDate = [endYear, dateRangeStrings[4], dateRangeStrings[5]].join("-");
        }

        var beginTime = $("#beginTime").val() == "" ? "00:00:00" : $("#beginTime").val() + ":00";
        var endTime = $("#endTime").val() == "" ? "23:59:59" : $("#endTime").val() + ":00";

        var routeName = $("#route").val().trim() == "" ? "" : $("#route").val();
        var directionName = $("#direction").val() == null ? "" : $("#direction").val();
        var startStopName = $("#startStop").val() == null ? "" : $("#startStop").val();
        var endStopName = $("#endStop").val() == null ? "" : $("#endStop").val();

        params = {};

        params.beginDate = beginDate;
        params.endDate = endDate;
        params.beginTime = beginTime;
        params.endTime = endTime;
        params.r = routeName
        params.headsign = directionName;
        params.serviceType = $("#" + serviceTypeSelector).val();
        params.startStop = startStopName;
        params.endStop = endStopName;

        return params;
    }

    function msToMin(data) {
        var highest = 0;

        for (var i = 0 in data) {
            data[i] = (data[i] / 60000).toFixed(1);
            if (data[i] > highest) {
                highest = data[i];
            }
        }

        highestPoints.push(highest);
        return data;
    }

    function arraysToXAndY(data) {
        xsAndYs = [];
        for (var i = 0 in data[0]) {
            xsAndYs[i] = {x: data[0][i], y: data[1][i]};
        }

        return xsAndYs;
    }

    function calculateMaxMins(points) {
        maxMins = Math.round(points[0]) + Math.round(points[1]) + Math.round(points[2]);
        if (Math.round(points[3]) > maxMins) {
            maxMins = Math.round(points[3]);
        }
        if (Math.round(points[4]) > maxMins) {
            maxMins = Math.round(points[4]);
        }

        return Math.ceil(maxMins/5)*5;
    }

    var canvas = $("#visualizationCanvas");
    var barGraph = new Chart(canvas, {
        type: 'horizontalBar',
        data: {},
        options: {
            scales: {
                xAxes: [
                    {
                        stacked: true,
                        scaleLabel: {
                            display: true,
                            labelString: "Min"
                        },
                        ticks: {
                            max: 50
                        }
                    }],
                yAxes: [
                    {
                        id: "bars",
                        stacked: false
                    },
                    // {
                    //     id: "icons",
                    //     stacked: false,
                    //     display: true,
                    //     ticks: {
                    //         beginAtZero: true,
                    //         min: 0,
                    //         max: 8,
                    //         stepSize: 1
                    //     }
                    // }
                ]
            },
            legend: {
                position: 'top',
                onClick: function(l) {
                    l.stopPropagation();
                }

            },
            tooltips: {
                callbacks: {
                    label: function(tooltipItem) {
                        var data = this._data.datasets[tooltipItem.datasetIndex];
                        var value = function() {
                            if (data.label == "Scheduled" || data.label == "Next trip start") {
                                return data.data[tooltipItem.index].x;
                            }
                            else {
                                return data.data[tooltipItem.index];
                            }
                        }
                        return data.label + ": " + value();
                    }
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
                            ctx.fillText(data, bar._model.x - ((bar._model.x - bar._model.base) / 2), bar._model.y + 5);
                        });
                    });
                }
            }
        },
    });

    var highestPoints = [];

    function visualizeData() {
        $("#submit").attr("disabled", true);
        $("#visualizeButton").attr("disabled", true);

        highestPoints = [];
        request = getParams(false)

        $.ajax({
            url: apiUrlPrefix + "/report/runTime/avgTripRunTimes",
            // Pass in query string parameters to page being requested
            data: request,
            // Needed so that parameters passed properly to page being requested
            traditional: true,
            dataType: "json",
            success: function (response) {
                if (response.data.trips.length == 0) {
                    alert("No trip breakdown available for selected run time data.");
                    $("#visualizeButton").attr("disabled", false);
                    $("#submit").attr("disabled",  false);
                }
                else {
                    barGraph.data = {
                        datasets: [
                            {
                                data: msToMin(response.data.fixed),
                                backgroundColor: '#36509b',
                                label: "Fixed",
                                yAxisId: "bars"
                            },
                            {
                                data: msToMin(response.data.variable),
                                backgroundColor: '#df7f17',
                                label: "Variable",
                                yAxisId: "bars"
                            },
                            {
                                data: msToMin(response.data.dwell),
                                backgroundColor: '#8c8c8c',
                                label: "Dwell",
                                yAxisId: "bars"
                            },
                            {
                                type: "scatter",
                                data: arraysToXAndY([msToMin(response.data.scheduled), response.data.trips]),
                                backgroundColor: '#70a260',
                                label: "Scheduled",
                                showLine: false,
                                fill: false,
                                // yAxisId: "icons"
                            },
                            {
                                type: "scatter",
                                data: arraysToXAndY([msToMin(response.data.nextTripStart), response.data.trips]),
                                backgroundColor: '#dfbf2c',
                                label: "Next trip start",
                                showLine: false,
                                fill: false,
                                // yAxisId: "icons"
                            }],
                        labels: response.data.trips
                    }

                    barGraph.options.scales.xAxes[0].ticks.max = calculateMaxMins(highestPoints);

                    barGraph.update();

                    $("#comparisonResults").hide();
                    $("#runTimeVisualization").show();
                    $("#visualizeButton").attr("disabled", false);
                    $("#submit").attr("disabled",  false);
                }

            },
            error: function(e) {
                alert("Error retrieving trip-by-trip summary.");
                $("#visualizeButton").attr("disabled", false);
                $("#submit").attr("disabled",  false);
            }
        })
    }


    $("#submit").click(function() {
        $("#submit").attr("disabled", "disabled");
        $("#mainResults").hide();
        $("#runTimeVisualization").hide();
        $("#modalDatepicker").val("Date range")
        $("#comparisonModal").hide();
        $("#comparisonResults").hide();
        $("#comparisonResults").html(
            '<hr>' +

            '<div id="comparisonParams" class="paramDetails" style="float: left;">' +
                '<p style="font-size: 0.8em;"></p>' +
            '</div>' +

            '<div id="comparisonAvgRunTime" style="display: inline-block; margin-left: 20px; width: 90%; vertical-align: middle;">' +
                '<p style="font-size: 0.8em;display: inline-block;"></p>' +
                '<p style="font-size: 0.8em;display: inline-block; width: 60px; height: 1.5em;"></p>' +
                '<p style="font-size: 0.8em;display: inline-block;"></p>' +
                '<p style="font-size: 0.8em;display: inline-block; width: 60px; height: 1.5em;"></p>' +
                '<p style="font-size: 0.8em;display: inline-block;"></p>' +
                '<p style="font-size: 0.8em;display: inline-block; width: 60px; height: 1.5em;"></p>' +
                '<p style="font-size: 0.8em;display: inline-block;"></p>' +
                '<p style="font-size: 0.8em;display: inline-block; width: 60px; height: 1.5em;"></p>' +
            '</div>'
        );

        request = getParams(false);

        $.ajax({
            url: apiUrlPrefix + "/report/runTime/avgRunTime",
            // Pass in query string parameters to page being requested
            data: request,
            // Needed so that parameters passed properly to page being requested
            traditional: true,
            dataType: "json",
            success: function (response) {
                if (jQuery.isEmptyObject(response)) {
                    $("#submit").removeAttr("disabled");
                    alert("No run time information available for selected parameters.");
                }
                else {
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

                    $("#paramDetails").html("<p style='font-size: 0.8em;'>" + (request.r == "" ? "All routes" : "Route " + request.r)  + " to " + (request.headsign == "" ? "All directions" : request.headsign) + " | " + (request.startStop == "" && request.endStop == "" ? "All stops" : request.startStop + " to " + request.endStop) + " | " + beginDateString + " to " + endDateString + " | " + timeRange + " | " + serviceDayString + "<a id='compareLink' style='font-size: 0.8em; margin-bottom: 1em; margin-left: 4em; color: blue; text-decoration: underline; cursor: pointer' onclick='openModal()'>Compare</a></p>");
                    $("#paramDetailsModal").html("<p style='font-size: 0.7em;'>" + (request.r == "" ? "All routes" : "Route " + request.r)  + " to " + (request.headsign == "" ? "All directions" : request.headsign) + " | " + (request.startStop == "" && request.endStop == "" ? "All stops" : request.startStop + " to " + request.endStop) + " | " + beginDateString + " to " + endDateString + " | " + timeRange + " | " + serviceDayString + "</p>");

                    var avgRunTime = typeof (response.avgRunTime) == 'undefined' ? "N/A" : (response.avgRunTime / 60000).toFixed(1);
                    var avgFixed = typeof (response.fixed) == 'undefined' ? "N/A" : (response.fixed / 60000).toFixed(1);
                    var avgVar = typeof (response.variable) == 'undefined' ? "N/A" : (response.variable / 60000).toFixed(1);
                    var avgDwell = typeof (response.dwell) == 'undefined' ? "N/A" : (response.dwell / 60000).toFixed(1);


                    $("#avgRunTime").html(
                        "<p style='font-size: 0.8em;display: inline-block; vertical-align: middle;'>Average run time</p>" +
                        "<p style='font-size: 0.8em;display: inline-block; margin-left: 50px; width: 60px; height: 1.5em; background-color: lightgray; vertical-align: middle;'>" + avgRunTime + "</p>" +
                        "<p style='font-size: 0.8em;display: inline-block; margin-left: 50px;'>Fixed</p>" +
                        "<p style='font-size: 0.8em;display: inline-block; margin-left: 50px; width: 60px; height: 1.5em; background-color: lightgray; vertical-align: middle;'>" + avgFixed + "</p>" +
                        "<p style='font-size: 0.8em;display: inline-block; margin-left: 40px; vertical-align: middle;'>Variable</p>" +
                        "<p style='font-size: 0.8em;display: inline-block; margin-left: 40px; width: 60px; height: 1.5em; background-color: lightgray; vertical-align: middle;'>" + avgVar + "</p>" +
                        "<p style='font-size: 0.8em;display: inline-block; margin-left: 40px; vertical-align: middle;'>Dwell</p>" +
                        "<p style='font-size: 0.8em;display: inline-block; margin-left: 40px; width: 60px; height: 1.5em; background-color: lightgray; vertical-align: middle;'>" + avgDwell + "</p>"
                    );

                    $("#mainResults").show();
                    $("#visualizeButton").show();
                }

            },
            error: function () {
                $("#submit").removeAttr("disabled");
                alert("Error processing average trip run time.");
            }
        })
    })

    $("#modalSubmit").click(function() {
        $(".submit").attr("disabled", "disabled");
        $("#comparisonResults").hide();
        $("#runTimeVisualization").hide();

        request = getParams(true);

        $.ajax({
            url: apiUrlPrefix + "/report/runTime/avgRunTime",
            // Pass in query string parameters to page being requested
            data: request,
            // Needed so that parameters passed properly to page being requested
            traditional: true,
            dataType: "json",
            success: function (response) {
                if (jQuery.isEmptyObject(response)) {
                    $(".submit").removeAttr("disabled");
                    alert("No run time information available for selected parameters.");
                }
                else {
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

                    $("#comparisonParams").html("<p style='font-size: 0.8em;'>" + (request.r == "" ? "All routes" : "Route " + request.r)  + " to " + (request.headsign == "" ? "All directions" : request.headsign) + " | " + (request.startStop == "" && request.endStop == "" ? "All stops" : request.startStop + " to " + request.endStop) + " | " + beginDateString + " to " + endDateString + " | " + timeRange + " | " + serviceDayString + "<a id='clearLink' style='font-size: 0.8em; margin-bottom: 1em; margin-left: 4em; color: blue; text-decoration: underline; cursor: pointer' onclick='clearComparison()'>Clear</a></p>");

                    var avgRunTime = typeof (response.avgRunTime) == 'undefined' ? "N/A" : (response.avgRunTime / 60000).toFixed(1);
                    var avgFixed = typeof (response.fixed) == 'undefined' ? "N/A" : (response.fixed / 60000).toFixed(1);
                    var avgVar = typeof (response.variable) == 'undefined' ? "N/A" : (response.variable / 60000).toFixed(1);
                    var avgDwell = typeof (response.dwell) == 'undefined' ? "N/A" : (response.dwell / 60000).toFixed(1);

                    $("#comparisonAvgRunTime").html(
                        "<p style='font-size: 0.8em;display: inline-block; vertical-align: middle;'>Average run time</p>" +
                        "<p style='font-size: 0.8em;display: inline-block; margin-left: 50px; width: 60px; height: 1.5em; background-color: lightgray; vertical-align: middle;'>" + avgRunTime + "</p>" +
                        "<p style='font-size: 0.8em;display: inline-block; margin-left: 50px;'>Fixed</p>" +
                        "<p style='font-size: 0.8em;display: inline-block; margin-left: 50px; width: 60px; height: 1.5em; background-color: lightgray; vertical-align: middle;'>" + avgFixed + "</p>" +
                        "<p style='font-size: 0.8em;display: inline-block; margin-left: 40px; vertical-align: middle;'>Variable</p>" +
                        "<p style='font-size: 0.8em;display: inline-block; margin-left: 40px; width: 60px; height: 1.5em; background-color: lightgray; vertical-align: middle;'>" + avgVar + "</p>" +
                        "<p style='font-size: 0.8em;display: inline-block; margin-left: 40px; vertical-align: middle;'>Dwell</p>" +
                        "<p style='font-size: 0.8em;display: inline-block; margin-left: 40px; width: 60px; height: 1.5em; background-color: lightgray; vertical-align: middle;'>" + avgDwell + "</p>"
                    );

                    $("#comparisonResults").show();
                }
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

    function clearComparison() {
        $("#comparisonResults").hide();
    }

    function removeSpinner() {
        $(".spinner").hide();
    }

    $("#closeModal").click(function() {
        $("#comparisonModal").hide();
    })

    $(".visualizeButton").click(function() {
        if (!($("#runTimeVisualization").is(":visible"))) {
            visualizeData();
        }
    })

</script>