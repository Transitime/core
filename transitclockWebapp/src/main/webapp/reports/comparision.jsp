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

        .comparison-header{
            font-size: 16px;
            margin: 20px 0;
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

    </style>

</head>
<body>
<%@include file="/template/header.jsp" %>


<div class="wrapper">
    <div class="paramsWrapper">
        <div id="paramsSidebar">
            <div id="title">
                Run Time Comparison
            </div>

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

                <div class="comparison-container">

                    <div class="comparison-header">Select Comparison Period 1</div>

                    <div class="param">
                        <label for="datepicker1">Date:</label>
                        <input type="text" id="datepicker1" name="datepicker1"
                               title="The range of dates that you want to examine data for.
                                        <br><br> Begin date must be before the end date."
                               size="18"
                               value="Date range"/>
                    </div>

                    <div class="param">
                        <label for="beginTime1">Begin Time:</label>
                        <input id="beginTime1" name="beginTime1"
                               title="Optional begin time of day to limit query to. Useful if
                                            want to see result just for rush hour, for example. Leave blank
                                            if want data for entire day.
                                            <br/><br/>Format: hh:mm, as in '07:00' for 7AM."
                               placeholder="(hh:mm)"
                               style="width:100%;"
                               value=""/>
                    </div>

                    <div class="param">
                        <label for="endTime1">End Time:</label>
                        <input id="endTime1" name="endTime1"
                               title="Optional end time of day to limit query to. Useful if
                                            want to see result just for rush hour, for example. Leave blank
                                            if want data for entire day.
                                            <br/><br/>Format: hh:mm, as in '09:00' for 9AM.
                                            Use '23:59' for midnight."
                               placeholder="(hh:mm)"
                               style="width:100%;"
                               value=""/>
                    </div>

                    <div class="param">
                        <label for="serviceDayType1">Service Day:</label>
                        <select id="serviceDayType1" name="serviceDayType1">
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


                <div class="comparison-container">

                    <div class="comparison-header">Select Comparison Period 2</div>

                    <div class="param">
                        <label for="datepicker2">Date:</label>
                        <input type="text" id="datepicker2" name="datepicker2"
                               title="The range of dates that you want to examine data for.
                                        <br><br> Begin date must be before the end date."
                               size="18"
                               value="Date range"/>
                    </div>

                    <div class="param">
                        <label for="beginTime2">Begin Time:</label>
                        <input id="beginTime2" name="beginTime2"
                               title="Optional begin time of day to limit query to. Useful if
                                            want to see result just for rush hour, for example. Leave blank
                                            if want data for entire day.
                                            <br/><br/>Format: hh:mm, as in '07:00' for 7AM."
                               placeholder="(hh:mm)"
                               style="width:100%;"
                               value=""/>
                    </div>

                    <div class="param">
                        <label for="endTime2">End Time:</label>
                        <input id="endTime2" name="endTime2"
                               title="Optional end time of day to limit query to. Useful if
                                            want to see result just for rush hour, for example. Leave blank
                                            if want data for entire day.
                                            <br/><br/>Format: hh:mm, as in '09:00' for 9AM.
                                            Use '23:59' for midnight."
                               placeholder="(hh:mm)"
                               style="width:100%;"
                               value=""/>
                    </div>

                    <div class="param">
                        <label for="serviceDayType2">Service Day:</label>
                        <select id="serviceDayType2" name="serviceDayType2">
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

            </div>
            <div class="submitDiv">

                <button id="submit" class="submit" >Submit</button>
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
        <h3 id="visualization-container-header">Average Trip Run Times</h3>


        <div class="visualization-container">

            <div id="runTimeVisualization">

            </div>
        </div>

    </div>
</div>
</body>
</html>

<script>

    var stops = {};

    $("#route").attr("style", "width: 200px");

    $("#route").change(function () {
        if ($("#route").val().trim() != "") {
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
        $(".wrapper").addClass("split");
        $("#mainResults").hide();
        $("#overlay").show();
        $("#mainPage").addClass("inactive-split")
        $("#bars1").show();
        $("#runTimeVisualization").html('');

        var request1 = getParams(true);
        var request2 = getParams(false);
        var responses = {};
        var count = 0;
        var sumResponse = {"avgRunTime":[],"fixed":[],"variable":[],"dwell":[],"labels":[]}
        var callBack = function(key , response){
            count++;

            if (jQuery.isEmptyObject(response)) {
                alert("No run time information available for selected parameters.");
            } else {
                responses[key] = response;
            }


            if(count === 2)
            {
                $("#submit").removeAttr("disabled");
                $("#overlay").hide();
                $("#mainPage").removeClass("inactive-split")
                $("#bars1").hide();

                Object.keys(responses).forEach(function(eachResponse, index){

                    sumResponse["avgRunTime"].push(responses[eachResponse]["avgRunTime"]);
                    sumResponse["fixed"].push(responses[eachResponse]["fixed"]);
                    sumResponse["variable"].push(responses[eachResponse]["variable"]);
                    sumResponse["dwell"].push(responses[eachResponse]["dwell"]);
                    sumResponse["labels"].push("Period  " + eachResponse);

                });

                visualizeData(sumResponse)
            }



        };

        serviceCall(request1, "1",  callBack);
        serviceCall(request2, "2", callBack);



    });

    function visualizeData(response){
        var defaultHeight = (response.avgRunTime.length ) *300;
        var defaultWidth = window.innerWidth;

        $("#runTimeVisualization").html(' <canvas id="visualizationCanvas" class="custom-canvas"  height="'+defaultHeight+'" width="'+defaultWidth+'"></canvas>');
        generateComparisonChart(response);
    }

    function calculateMaxMins(points) {
        maxMins = Math.round(points[0]) + Math.round(points[1]) + Math.round(points[2]);
        if (Math.round(points[3]) > maxMins) {
            maxMins = Math.round(points[3]);
        }
        if (Math.round(points[4]) > maxMins) {
            maxMins = Math.round(points[4]);
        }

        return Math.ceil(maxMins / 5) * 5;
    }

    function generateComparisonChart(response){
        var barGraph = getDefaultChartOptions({
            yAxis:{
                isStacked: true
            }
        });
        barGraph.data = {
            datasets: [
                {
                    data: msToMin(response.fixed),
                    backgroundColor: '#36509b',
                    label: "Fixed",
                    yAxisId: "bars"
                },
                {
                    data: msToMin(response.variable),
                    backgroundColor: '#df7f17',
                    label: "Variable",
                    yAxisId: "bars"
                },
                {
                    data: msToMin(response.dwell),
                    backgroundColor: '#8c8c8c',
                    label: "Dwell",
                    yAxisId: "bars"
                }/*,
                {
                    type: "scatter",
                    data: arraysToXAndY([msToMin(response.data.scheduled), response.data.stopPaths]),
                    backgroundColor: '#70a260',
                    label: "Scheduled",
                    showLine: false,
                    fill: false,
                }*/],
            labels: response.labels
        }

        barGraph.options.scales.xAxes[0].ticks.max = calculateMaxMins(highestPoints);

        barGraph.update();


    }
    Chart.plugins.register({
        afterDatasetsDraw: function(chart) {
            var ctx = chart.ctx;

            ctx.font = Chart.helpers.fontString(Chart.defaults.global.defaultFontSize, Chart.defaults.global.defaultFontStyle, Chart.defaults.global.defaultFontFamily);
            ctx.fillStyle = '#000000';
            ctx.textAlign = 'center';
            ctx.textBaseline = 'bottom';

            Chart.helpers.each(chart.data.datasets.forEach(function(dataset, i) {
                var meta = chart.controller.getDatasetMeta(i);
                Chart.helpers.each(meta.data.forEach(function(bar, index) {
                    ctx.save();
                    var data = dataset.data[index];
                    if(chart.options && chart.options.showPercentage) {
                        data = Math.floor(data);
                        if(data !== 0) {
                            ctx.fillText(data + "%", bar._model.x - ((bar._model.x - bar._model.base) / 2), bar._model.y + 5);
                        }
                    } else {
                        ctx.fillText(data, bar._model.x - ((bar._model.x - bar._model.base) / 2), bar._model.y + 5);
                    }
                    ctx.restore(); //<- restore canvas state
                }))
            }));
        }
    });


    function getDefaultChartOptions(options){

        var canvas = $("#visualizationCanvas");
        var barGraph = new Chart(canvas, {
            type: 'horizontalBar',
            data: {},
            options: {
                showPercentage: options && options.showPercentage,
                scales: {
                    xAxes: [
                        {
                            stacked: true,
                            scaleLabel: {
                                display: true,
                                labelString: "Minutes"
                            },
                            ticks: options && options.xAxis && options.xAxis.ticks ||{}
                        }],
                    yAxes: [
                        {
                            id: "bars",
                            stacked: options && options.yAxis && options.yAxis.isStacked || false,
                            ticks: options && options.yAxis && options.yAxis.ticks || {
                                stepSize: 1
                            }
                        }
                    ]
                },
                legend: {
                    position: 'top',
                    onClick: function (l) {
                        l.stopPropagation();
                    }

                },
                tooltips: {
                    callbacks: {
                        label: function (tooltipItem) {
                            var data = this._data.datasets[tooltipItem.datasetIndex];
                            var value = function () {
                                if(options && options.showPercentage) {
                                    return  Math.floor(data.data[tooltipItem.index]) +"%";
                                } else  if (data.label == "Scheduled" || data.label == "Next trip start") {
                                    return data.data[tooltipItem.index].x;
                                } else {
                                    return data.data[tooltipItem.index];
                                }
                            }
                            return data.label + ": " + value();
                        }
                    }
                },
                animation: false
            }
        });
        visualarGraphChart = barGraph;
        return barGraph;
    }

    function populateDirection() {

        $("#submit").attr("disabled", true);

        $("#tripPattern").empty();
        $("#direction").removeAttr('disabled');
        $("#direction").empty();


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
                $("#submit").attr("disabled", false);
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
                } else {
                    $("#tripPattern").removeAttr('disabled');
                    $("#submit").removeAttr('disabled');

                    $("#tripPattern").append("<option value=''>All</option>")
                    resp.tripPatterns.forEach(function (tripPattern) {
                        $("#tripPattern").append("<option value='" + tripPattern.id + "'>" + tripPattern.firstStopName + ' to ' + tripPattern.lastStopName + "</option>");
                    })

                }

            },
            // When there is an AJAX problem alert the user
            error: function (request, status, error) {
                alert(error + '. ' + request.responseText);
                $("#submit").attr("disabled", false);
            }
        });
    }

    function getParams(flag) {

        var datepicker, serviceTypeSelector, endTime, beginTime;
        if (flag) {
            datepicker = "datepicker1";
            beginTime="beginTime1";
            endTime="endTime1";
            serviceTypeSelector = "serviceDayType1";
        } else {
            datepicker = "datepicker2";
            beginTime="beginTime2";
            endTime="endTime2";
            serviceTypeSelector = "serviceDayType2";
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

        var beginTime = $("#"+beginTime).val() == "" ? "00:00:00" : $("#"+beginTime).val() + ":00";
        var endTime = $("#"+endTime).val() == "" ? "23:59:59" : $("#"+endTime).val() + ":00";

        var routeName = $("#route").val().trim() == "" ? "" : $("#route").val();
        var tripPatternName = $("#tripPattern").val() == null ? "" : $("#tripPattern").val();
        var directionName = $("#direction").val();


        params = {};

        params.beginDate = beginDate;
        params.endDate = endDate;
        params.beginTime = beginTime;
        params.endTime = endTime;
        params.r = routeName
        params.headsign = directionName;
        params.serviceType = $("#" + serviceTypeSelector).val();
        params.tripPattern = tripPatternName;

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

    function datePickerIntialization () {
        var calendarIconTooltip = "Popup calendar to select date";

        $("#datepicker1, #datepicker2").datepick({
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

        $("#beginTime1, #endTime1, #beginTime2, #endTime2").timepicker({timeFormat: "H:i"})
            .on('change', function (evt) {
                if (evt.originalEvent) { // manual change
                    // validate that this looks like HH:MM
                    if (!evt.target.value.match(/^(([0,1][0-9])|(2[0-3])):[0-5][0-9]$/))
                        evt.target.value = evt.target.oldval ? evt.target.oldval : "";
                }
                evt.target.oldval = evt.target.value;
            });

    }

    function serviceCall(request, key, callBack){

        $.ajax({
            url: apiUrlPrefix + "/report/runTime/avgRunTime",
            // Pass in query string parameters to page being requested
            data: request,
            // Needed so that parameters passed properly to page being requested
            traditional: true,
            dataType: "json",
            success: function (response) {


                callBack(key,response);

            },
            error: function () {
                $("#submit").removeAttr("disabled");
                alert("Error processing average trip run time.");
            }
        })

    }

    datePickerIntialization();
</script>