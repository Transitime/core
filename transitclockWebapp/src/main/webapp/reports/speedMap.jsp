<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <%@include file="/template/includes.jsp" %>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Speed Map</title>

    <!-- Load in Select2 files so can create fancy route selector -->
    <link href="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/css/select2.min.css" rel="stylesheet" />
    <script src="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/js/select2.min.js"></script>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;700&display=swap" rel="stylesheet">


    <link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.3/leaflet.css" />
    <script src="//cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.3/leaflet.js"></script>

    <script src="<%= request.getContextPath() %>/maps/javascript/leafletRotatedMarker.js"></script>
    <script src="<%= request.getContextPath() %>/maps/javascript/mapUiOptions.js"></script>

    <link rel="stylesheet" href="<%= request.getContextPath() %>/maps/css/mapUi.css" />

    <link href="params/reportParams.css" rel="stylesheet"/>
    <style>
        hr {
            height: 2px;
            background-color: darkgray;
            margin-right: 5px;
        }

        label {
            text-align: left;
            width: auto;
        }

        .wrapper {
            background: #f1f1f1f1;
            font-family: 'Montserrat', sans-serif;
            height: 100vh;
            width: 100vw;
            position: fixed;
            display: flex;
            flex-flow: row;
            font-family: 'Montserrat', sans-serif;
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

        select {
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

        input[type="range"] {
            -webkit-appearance: slider-horizontal;
            box-shadow: none;
            border: none;
        }

        .paramsWrapper {
            width: 100%;
            height: 100vh;
            transition: width .75s ease-in-out, max-width .75s ease-in-out;
            font-size: 16px;
            background-color: #fff;
            border: #969696 solid 1px;
            box-shadow: 3px 3px 4px rgba(0,0,0,0.3);
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
        .param > * {
            font-size: 16px;
        }
        .param > span {
            font-weight: 500;
            padding-bottom: 12px;
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

        .select2-selection.select2-selection--single {
            background-color: #fff;
            box-shadow: 0px 2px 6px rgba(0, 0, 0, 0.33);
            border: none;
            -webkit-border-radius: 0px;
            -moz-border-radius: 0px;
            border-radius: 0px;
        }

        .select2-dropdown.select2-dropdown--below {
            border: none;
            -webkit-border-radius: 0px;
            -moz-border-radius: 0px;
            border-radius: 0px;
            box-shadow: 0px 4px 4px rgba(0,0,0,0.3);
        }
        .select2-route-container{
            font-family: 'Montserrat', sans-serif;
        }

        #serviceDayType {
            width: 100%;
            height: 36px;
            margin-top: 6px;
            box-shadow: 0px 1px 4px #69696969;
            font-family: 'Montserrat', sans-serif;
        }


        #mainPage {
            visibility: hidden;
            opacity: 0;
            display: none;
            margin-left: 2vw;
            margin-top: 20vh;
            height: 80vh;
            width: 75%;
            max-width: 1250px;
            padding: 10px 0px;
            background-color: #fff;
            border-radius: 4px;
            box-shadow: 0px 4px 8px rgba(0,0,0,0.3);
            transition: visibility .25s .75s ease-in-out, opacity .25s .75s ease-in-out;
        }

        .split #mainPage {
            display: inline-block;
            position: relative;
            visibility: visible;
            opacity: 1;
            margin-top: 2vh;
        }


        #mainPage h2 {
            text-align: center;
            margin-top: 20px;
            padding: 10px;
            background-color: #019932;
            color: #fff;
        }

        .inactive {
            filter: blur(2px) grayscale(100%);
        }





        #runTimesFlyout {
            width: 20%;
            height: 100%;
            position: absolute;
            right: 0px;
            z-index: 1001;
            border-left: 1px solid black;
            background-color: white;
            transition: all .5s ease
        }

        .datepick-popup {
            z-index: 1002 !important;
        }

        .speedLegend {
            display: inline-block;
            width: 14px;
            min-width: 14px;
            height: 14px;
            border: 1px solid black;
            margin-bottom: 5px;
        }

        #speedParams .vertical {
            margin-top: 0px;
        }

        #speedParams input[type="number"]{
            max-width: 48px;
        }

        #speedParams .pair span {
            width: 90%;
            margin: auto;
        }

        .loader {
            position: absolute;
            left: 50%;
            top: 50%;
            border: 16px solid #f3f3f3; /* Light grey */
            border-top: 16px solid #3498db; /* Blue */
            border-radius: 50%;
            width: 120px;
            height: 120px;
            -webkit-animation: spin 2s linear infinite;
            animation: spin 2s linear infinite;
            z-index: 1000;
        }

        .submit {
            margin: 24px;
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


        @-webkit-keyframes spin {
            0% { -webkit-transform: rotate(0deg); }
            100% { -webkit-transform: rotate(360deg); }
        }

        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }

    </style>
    <%--        <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>--%>
    <link rel="stylesheet" type="text/css" href="../jquery.datepick.package-5.1.0/css/jquery.datepick.css">
    <script type="text/javascript" src="../jquery.datepick.package-5.1.0/js/jquery.plugin.js"></script>
    <script type="text/javascript" src="../jquery.datepick.package-5.1.0/js/jquery.datepick.js"></script>

    <script src="https://cdn.jsdelivr.net/npm/chart.js@2.8.0"></script>
    <script src="https://cdn.jsdelivr.net/gh/emn178/chartjs-plugin-labels/src/chartjs-plugin-labels.js"></script>
</head>
<body>
    <%@include file="/template/header.jsp" %>
    <div class="wrapper">

    <div class="paramsWrapper">

        <div id="paramsSidebar">
            <div id="title">
                Speed Map
            </div>

            <div id="paramsFields">
                <%-- For passing agency param to the report --%>
                <input type="hidden" name="a" value="<%= request.getParameter("a")%>">

                <jsp:include page="params/routeAllOrSingle.jsp" />

                <div class="param">
                    <label for="direction">Direction:</label>
                    <select id="direction" name="direction" disabled="true">

                    </select>
                </div>

                <script src="../javascript/jquery-timepicker/jquery.timepicker.min.js"></script>
                <link rel="stylesheet" type="text/css" href="../javascript/jquery-timepicker/jquery.timepicker.css"></link>

                <script>
                    $(function() {
                        var calendarIconTooltip = "Popup calendar to select date";

                        $( "#mainDatepicker" ).datepick({
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
                    <label for="mainDatepicker">Date:</label>
                    <input type="text" id="mainDatepicker" name="mainDatepicker"
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
                           placeholder="(hh:mm)"
                           value="" />
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
                           placeholder="(hh:mm)"
                           value="" />
                </div>

                <div class="param vertical">
                    <label for="serviceDayType">Service Day Type</label>
                    <select id="serviceDayType" name="serviceDayType">
                        <option value="">All</option>
                        <option value="weekday">Weekday</option>
                        <option value="saturday">Saturday</option>
                        <option value="sunday">Sunday</option>
                    </select>
                </div>

                <hr>

                <div id="speedParams">
                    <div class = "param vertical">
                        <div class="pair">
                            <div class="speedLegend" style="background-color: red;"></div>
                            <span>Low Speed (mph max)</span>
                        </div>
                        <div class="pair">
                            <input id='lowSpeedSlider' name='lowSpeedSlider' type="range" min="0" max="98" step="0.1" value="0" oninput="lowSpeedSlider(this.value)">
                            <input type="number" id="lowSpeedManual" name="lowSpeedManual" min="0" max="98" step="0.1" value="0" oninput="lowSpeedManual(this.value)">
                        </div>
                    </div>
                    <div class = "param vertical">
                        <div class="pair">
                            <div class="speedLegend" style="background-color: yellow;"></div>
                            <span>Mid Speed (mph max)</span>
                        </div>
                        <div class="pair">
                            <input id='midSpeedSlider' name='midSpeedSlider' type="range" min="1" max="99" step="0.1" value="1" oninput="midSpeedSlider(this.value)">
                            <input type="number" id="midSpeedManual" name="midSpeedManual" min="1" max="99" step="0.1" value="1" oninput="midSpeedManual(this.value)">

                        </div>
                    </div>
                    <div class = "param vertical">
                        <div class="pair">
                            <div class="speedLegend" style="background-color: green;"></div>
                            <span>High Speed (mph max)</span>
                        </div>
                    </div>
                </div>
            </div>

            <input type="button" id="mainSubmit" class="submit" value="Submit">

        </div>

    </div>

    <div id="mainPage">
        <div id="paramDetailsTop" class="paramDetails" style="height: 3%; float: left; margin-left: 20px; width: 60%;">
            <p style='font-size: 0.8em;'></p>
        </div>
        <div id="avgRunTimeTop" class="avgRunTime" style="display: inline-block; float: right; margin-right: 20px; margin-bottom: 20px; width: 30%; text-align: right"></div>
        <div id="runTimesFlyout" hidden="true">
            <div id="flyoutContents" style="margin-right: 10px; margin-left: 20px; margin-top: 10px;">
                <div id="runTimesHeader" style="text-align: left; vertical-align: middle; font-size: medium">
                    Trip Run Time Comparison
                    <button id='closeFlyout' type='button' style='float:right;'>&times;</button>
                </div>
                <div id="paramDetailsFlyout" class="paramDetails" style="margin-top: 20px; margin-bottom: 20px;"></div>
                <div id="avgRunTimeFlyout" class="avgRunTime" style="margin-top: 20px; margin-bottom: 20px;"></div>

                <script src="../javascript/jquery-timepicker/jquery.timepicker.min.js"></script>
                <link rel="stylesheet" type="text/css" href="../javascript/jquery-timepicker/jquery.timepicker.css"></link>

                <script>
                    $(function() {
                            var calendarIconTooltip = "Popup calendar to select date";

                            $("#flyoutDatepicker").datepick({
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

                <div class="param">
                    <label for="flyoutDatepicker">Date:</label>
                    <input type="text" id="flyoutDatepicker" name="flyoutDatepicker"
                           title="The range of dates that you want to examine data for.
                               <br><br> Begin date must be before the end date."
                           size="18"
                           value="Date range" />
                </div>

                <input type="button" id="runTimeSubmit" class="submit" value="Submit" style="margin-top: 20px; margin-bottom: 20px;">

                <canvas id="comparisonChart" height="400" style="margin-top: 40px;"></canvas>
            </div>
        </div>
        <div id="map" style="height: 90%; width: 90%; margin: auto;">
            <div class="loader" hidden="true"></div>
        </div>
    </div>

    </div>

<script>
    var mapTileUrl ='http://tile.openstreetmap.org/{z}/{x}/{y}.png'
    var map = L.map('map');
    L.control.scale({metric: false}).addTo(map);
    L.tileLayer(mapTileUrl, {
        attribution: '&copy; <a href="http://openstreetmap.org">OpenStreetMap</a> &amp; <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery � <a href="http://mapbox.com">Mapbox</a>',
        maxZoom: 19
    }).addTo(map);
    // Calculate the offset
    var offset = map.getSize().x*0.15;
    // // Then move the map
    // map.panBy(new L.Point(-offset, 0), {animate: false});

    //fit map to agency boundaries.
    $.getJSON(apiUrlPrefix + "/command/agencyGroup", function(agencies) {
        var e = agencies.agency[0].extent;
        map.fitBounds([[e.minLat, e.minLon], [e.maxLat, e.maxLon]]);
    })
        .fail(function(request, status, error) {
            alert(error + '. ' + request.responseText);
        });

    var routeGroup = L.layerGroup().addTo(map);
    //Set the CLIP_PADDING to a higher value so that when user pans on map
    //the route path doesn't need to be redrawn. Note: leaflet documentation
    //says that this could decrease drawing performance. But hey, it looks
    //better.
    L.Path.CLIP_PADDING = 0.8;0

    /* For drawing the route and stops */
    var routeOptions = {
        color: '#36aaff',
        weight: 4,
        opacity: 0.4,
        lineJoin: 'round',
        clickable: false
    };

    var speedOptions = {
        weight: 3,
        opacity: 0.8,
        lineJoin: 'round',
        clickable: true
    };

    var stopOptions = {
        color: '#000000',
        opacity: 1,
        radius: 4,
        weight: 2,
        fillColor: '#ffffff',
        fillOpacity: 1,
    };

    var routePolylineOptions = {clickable: false, color: "#00f", opacity: 0.5, weight: 4};

    var stopPopupOptions = {closeButton: false};

    var runTimesChart = new Chart(comparisonChart, {
        type: 'bar',
        data: {
            labels: ["Initial Query", "Comparison"],
            datasets: [{
                label: "Average Run Time",
                backgroundColor: 'rgb(37,137,197)',
                data: []
            }]
        },
        options: {
            title: {
                display: true,
                text: 'Run Time Comparison'
            },
            scales: {
                yAxes: [{
                    ticks: {
                        suggestedMax: 3600000,
                        min: 0,
                        stepSize: 300000,
                        callback: function(value, index, values) {
                            return msToMinsSecs(value);
                        }
                    }
                }]
            },
            tooltips: {
                callbacks: {
                    label: function (tooltipItem, data) {
                        return msToMinsSecs(data.datasets[0].data[tooltipItem.index]);
                    }
                }
            },
            plugins: {
                labels: {
                    render: function(args) {
                        return msToMinsSecs(args.value);
                    }
                }
            }
        }
    });

    function msToMinsSecs(milliseconds) {
        var minutes = parseInt(milliseconds / 60000).toString();
        var seconds = parseInt(milliseconds % 60000 / 1000).toString();
        if (seconds.length == 1) {
            seconds = "0" + seconds;
        }

        return minutes + ":" + seconds;
    }

    $("#route").attr("style", "width: 200px");

    $("#route").change(function() {
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
            }
        })
    })

    function lowSpeedSlider(value) {
        $("#lowSpeedManual").val(value);
        lowSpeedManual(value);
    }

    function lowSpeedManual(value) {
        if (parseFloat(value) < 0 || isNaN(parseFloat(value))) {
            value = 0;
            $("#lowSpeedManual").val(value);
        }
        else if (parseFloat(value) > 98) {
            value = 98;
            $("#lowSpeedManual").val(value);
        }

        $("#lowSpeedSlider").val(value);

        $("#midSpeedManual").attr("min", (parseFloat(value) + 1));
        $("#midSpeedSlider").attr("min", (parseFloat(value) + 1));

        if (value < 0) {

        }

        if ($("#midSpeedManual").val() <= (parseFloat(value) + 1)) {
            midSpeedSlider((parseFloat(value) + 1));
        }
    }

    function midSpeedSlider(value) {
        $("#midSpeedManual").val(value);
    }

    function midSpeedManual(value) {
        if (parseFloat(value) < 1 || isNaN(parseFloat(value))) {
            value = 1;
            $("#midSpeedManual").val(value);
        }
        else if (parseFloat(value) > 99) {
            value = 99;
            $("#midSpeedManual").val(value);
        }

        $("#midSpeedSlider").val(value);
        if (value <= (parseFloat($("#lowSpeedManual").val()) + 1)) {
            lowSpeedSlider(value - 1);
        }
    }

    function getParams(dateParam) {
        if ($(dateParam).val() == "Date range") {
            var today = new Date();
            var beginDate = endDate = today.getFullYear() + "-"
                + (today.getMonth() <= 10 ? "0" + (today.getMonth() + 1) : (today.getMonth() + 1))
                + "-" + (today.getDate() < 10 ? "0" + today.getDate() : today.getDate());
        } else {
            var dateRangeStrings = $(dateParam).val().replace(/\s/g, "").split("-");
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
    }

    /**
     * Reads in route data obtained via AJAX and draws route and stops on map.
     */
    function routeConfigCallback(data, status) {
        // Draw the paths for the route

        var route = data.routes[0];

        for (var i=0; i<route.shape.length; ++i) {
            var shape = route.shape[i];
            var routePoly = L.polyline(shape.loc, routeOptions).addTo(routeGroup);
            map.fitBounds(routePoly.getLatLngs());
        }
    }

    function speedsCallback(data) {
        if (data.stopPaths.length == 0) {
            alert("No speed data available for selected parameters.")
        }
        data.stopPaths.forEach(function(stopPath) {
            var options = speedOptions;

            if (stopPath.speed < parseFloat($("#lowSpeedManual").val())) {
                options.color = '#FF0000';
            }
            else if (stopPath.speed < parseFloat($("#midSpeedManual").val())) {
                options.color = '#FFFF00';
            }
            else {
                options.color = '#008000';
            }

            for (i = 0; i < stopPath.latlngs.length; i++) {
                stopPath.latlngs[i] = {lat: JSON.parse(stopPath.latlngs[i])[0], lon: JSON.parse(stopPath.latlngs[i])[1]};
            }

            var speedSegment = L.polyline(stopPath.latlngs, options).addTo(routeGroup);

            var content = $("<table />").attr("class", "popupTable");
            content.append( $("<tr />").append("<td>" + stopPath.speed + " mph </td>") );

            speedSegment.bindPopup(content[0]);
        })
    }

    function stopsCallback(data) {
        if (data.stops.length == 0) {
            alert("No stops data available for selected parameters.");
        }

        data.stops.forEach(function(stop) {
            // Create the stop Marker
            var stopMarker = L.circleMarker([stop.lat,stop.lon], stopOptions).addTo(routeGroup);
            stopMarker.bringToFront();

            // Create popup for stop marker

            var content = $("<table />").attr("class", "popupTable");
            var labels = ["Stop ID", "Name", "Avg Dwell Time"], keys = ["stopId", "stopName", "avgDwellTime"];
            if (typeof stop['avgDwellTime'] == 'undefined') {
                stop['avgDwellTime'] = '';
            }
            else {
                var dwellTimeMinutes = parseInt(stop['avgDwellTime'] / 60).toString();
                var dwellTimeSeconds = parseInt(stop['avgDwellTime'] % 60).toString();
                if (dwellTimeSeconds.length == 1) {
                    dwellTimeSeconds = "0" + dwellTimeSeconds;
                }
                stop['avgDwellTime'] = dwellTimeMinutes + ":" + dwellTimeSeconds;
            }
            for (var i = 0; i < labels.length; i++) {
                var label = $("<td />").attr("class", "popupTableLabel").text(labels[i] + ": ");
                var value = $("<td />").text(stop[keys[i]]);
                content.append( $("<tr />").append(label, value) );
            }
            stopMarker.bindPopup(content[0]);
        })

        $(".loader").hide();
        $("#mainSubmit").removeAttr("disabled");
        $("#mainSubmit").attr("value","Submit");
        $("#mainPage").removeClass("inactive");
    }

    $("#mainSubmit").click(function() {
        $("#mainSubmit").attr("disabled", "disabled");
        $("#mainSubmit").attr("value","loading...");
        $("#flyoutDatepicker").val("Date range")
        $("#avgRunTimeComparison").html("");
        $("#runTimesFlyout").hide();
        $(".loader").show();
        $(".wrapper").addClass("split");
        $("#mainPage").addClass("inactive");

        request = {}

        getParams("#mainDatepicker");

        routeGroup.clearLayers();
        if (request.r != "") {
            var url = apiUrlPrefix + "/command/routesDetails?r=" + request.r;
            $.getJSON(url, routeConfigCallback);
        }

        $.ajax({
            url: apiUrlPrefix + "/report/speedmap/stopPathsSpeed",
            // Pass in query string parameters to page being requested
            data: request,
            // Needed so that parameters passed properly to page being requested
            traditional: true,
            dataType: "json",
            success: function(response) {
                speedsCallback(response);

                $.ajax({
                    url: apiUrlPrefix + "/report/speedmap/stops",
                    // Pass in query string parameters to page being requested
                    data: request,
                    // Needed so that parameters passed properly to page being requested
                    traditional: true,
                    dataType: "json",
                    success: function (response) {
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

                        $(".paramDetails").each(function() {
                            $(this).html("<p style='font-size: 0.8em;'>Route " + request.r + " to " + request.headsign + " | " + beginDateString + " to " + endDateString + " | " + timeRange + " | " + serviceDayString + "</p>");
                        })

                        stopsCallback(response);
                    },
                    error: function (e) {
                        $(".loader").hide();
                        $("#mainSubmit").removeAttr("disabled");
                        $("#mainSubmit").attr("value","Submit");
                        $("#mainPage").removeClass("inactive");
                        alert("Error processing stop details.");
                    }
                })
            },
            error: function (e) {
                $(".loader").hide();
                $("#mainSubmit").removeAttr("disabled");
                $("#mainSubmit").attr("value","Submit");
                $("#mainPage").removeClass("inactive");
                alert("Error processing speed details for stops.");
            }
        })

        runTimesChart.data.datasets[0].data[0] = null;
        runTimesChart.data.datasets[0].data[1] = null;
        runTimesChart.update();

        $.ajax({
            url: apiUrlPrefix + "/report/speedmap/runTime",
            // Pass in query string parameters to page being requested
            data: request,
            // Needed so that parameters passed properly to page being requested
            traditional: true,
            dataType: "json",
            success: function (response) {
                var compareLink = "<a id='compareLink' style='font-size: 0.8em; margin-bottom: 1em; color: blue; text-decoration: underline; cursor: pointer' onclick='openFlyout()'>Compare</a>";

                if (response.numberOfTrips == 0) {
                    $("#avgRunTimeTop").html("<p style='font-size: 0.8em; margin-bottom: 0em;'>No average run time data.</p>" + compareLink);
                }
                else {
                    runTimesChart.data.datasets[0].data[0] = response.averageRunTime;
                    runTimesChart.update();

                    $("#avgRunTimeTop").html("<p style='font-size: 0.8em; margin-bottom: 0em;'>Average Trip Run Time: " + msToMinsSecs(response.averageRunTime) + "</p>" + compareLink);
                }
            },
            error: function () {
                $(".loader").hide();
                $("#mainSubmit").removeAttr("disabled");
                $("#mainSubmit").attr("value","Submit");
                $("#mainPage").removeClass("inactive");
                alert("Error processing average trip run time.");
            }
        })
    })

    function openFlyout() {
        $("#avgRunTimeFlyout").html($("#avgRunTimeTop > p").html()).css("font-size", "0.8em");
        $("#runTimesFlyout").show();
    }

    $("#runTimeSubmit").click(function() {
        $(".submit").attr("disabled", "disabled");
        $(".loader").show();

        getParams("#flyoutDatepicker");

        $.ajax({
            url: apiUrlPrefix + "/report/speedmap/runTime",
            // Pass in query string parameters to page being requested
            data: request,
            // Needed so that parameters passed properly to page being requested
            traditional: true,
            dataType: "json",
            success: function (response) {
                if (response.numberOfTrips == 0) {
                    runTimesChart.data.datasets[0].data[1] = null;
                    runTimesChart.update();
                    alert("No average run time data for comparison dates.");
                }
                else {
                    runTimesChart.data.datasets[0].data[1] = response.averageRunTime;
                    runTimesChart.update();
                }
                $(".loader").hide();
                $(".submit").removeAttr("disabled");
            },
            error: function () {
                $(".loader").hide();
                $(".submit").removeAttr("disabled");
                alert("Error processing average trip run time.");
            }
        })
    })

    $("#closeFlyout").click(function() {
        $("#runTimesFlyout").hide();
    })

</script>