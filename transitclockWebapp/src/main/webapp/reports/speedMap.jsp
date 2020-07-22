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

        #paramsSidebar {
            width: 20%;
            height: 100%;
            margin-left: 10px;
            float:left;
            border-right: 1px solid black;
        }

        #runTimesFlyout {
            width: 17%;
            height: 100%;
            position: absolute;
            right: 0px;
            z-index: 9999;
            border-left: 1px solid black;
            background-color: white;
            transition: all .5s ease
        }

        .speedLegend {
            display: inline-block;
            width: 20px;
            height: 20px;
            border: 1px solid black;
            margin-right: 10px;
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
        <div id="title" style="text-align: left; font-size:x-large">
            Speed Map
        </div>

        <div id="paramsFields">
            <%-- For passing agency param to the report --%>
            <input type="hidden" name="a" value="<%= request.getParameter("a")%>">

            <jsp:include page="params/routeAllOrSingle.jsp" />

            <div class="param">
                <label for="direction">Direction:</label>
                <select id="direction" name="direction" disabled="true" style="width: 177px">

                </select>
            </div>

            <script src="../javascript/jquery-timepicker/jquery.timepicker.min.js"></script>
            <link rel="stylesheet" type="text/css" href="../javascript/jquery-timepicker/jquery.timepicker.css"></link>

            <script>
                $(function() {
                    var calendarIconTooltip = "Popup calendar to select date";

                    $( "#beginDate" ).datepick({
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
                <label for="beginDate">Date:</label>
                <input type="text" id="beginDate" name="beginDate"
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

            <hr>

            <div id="speedParams">
                <div class = "param">
                    <div class="speedLegend" style="background-color: green;"></div>
                    <input id='lowSpeed' name='lowSpeed' type="range" min="0" max="98" step="0.1" value="0" oninput="lowSpeedSlider(this.value)">
                    <output id="lowSpeedOutput">mph max</output>
                </div>
                <div class="param">
                    <div class="speedLegend" style="background-color: yellow;"></div>
                    <input id='midSpeed' name='midSpeed' type="range" min="1" max="99" step="0.1" value="1" oninput="midSpeedSlider(this.value)">
                    <output id="midSpeedOutput">mph max</output>
                </div>
                <div class="param">
                    <div class="speedLegend" style="background-color: red;"></div>
                    <input id='highSpeed' name='highSpeed' type="range" min="2" max="100" step="0.1" value="2" oninput="highSpeedSlider(this.value)">
                    <output id="highSpeedOutput">mph max</output>
                </div>
            </div>
        </div>

        <input type="button" id="submit" value="Submit" style="margin-top: 10px; margin-bottom: 10px;">

    </div>

    <div id="mainPage" style="width: 79%; height: 100%; display: inline-block;">
        <div id="paramDetails" style="height: 3%; float: left; margin-left: 20px; width: 60%;">
            <p style='font-size: 0.8em;'></p>
        </div>
        <div id="avgRunTime" style="display: inline-block; float: right; margin-right: 20px; margin-bottom: 20px; width: 30%; text-align: right"></div>
        <div id="runTimesFlyout" hidden="true">
            <div id="flyoutContents" style="margin-right: 10px; margin-left: 20px; margin-top: 10px;">
                <div id="runTimesHeader" style="text-align: left; vertical-align: middle; font-size: medium">
                    Trip Run Time Comparison
                    <button id='closeFlyout' type='button' style='float:right;'>&times;</button>
                </div>

                <div id="runTimeParams">

                </div>
            </div>
        </div>
        <div id="map" style="height: 90%; width: 90%; margin: auto;">

        </div>
    </div>

<script>
    var map = L.map('map');
    L.control.scale({metric: false}).addTo(map);
    L.tileLayer('http://api.tiles.mapbox.com/v4/transitime.j1g5bb0j/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoidHJhbnNpdGltZSIsImEiOiJiYnNWMnBvIn0.5qdbXMUT1-d90cv1PAIWOQ', {
        attribution: '&copy; <a href="http://openstreetmap.org">OpenStreetMap</a> &amp; <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>',
        maxZoom: 19
    }).addTo(map);

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
        color: '#00ee00',
        weight: 4,
        opacity: 0.4,
        lineJoin: 'round',
        clickable: false
    };

    var stopOptions = {
        color: '#006600',
        opacity: 0.4,
        radius: 4,
        weight: 2,
        fillColor: '#006600',
        fillOpacity: 0.3,
    };

    var routePolylineOptions = {clickable: false, color: "#00f", opacity: 0.5, weight: 4};

    var stopPopupOptions = {closeButton: false};

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
        $("#lowSpeedOutput").val(value + " mph max");

        $("#midSpeed").attr("min", (parseFloat(value) + 1));
        $("#highSpeed").attr("min", (parseFloat(value) + 2));

        if ($("#midSpeed").val() <= (parseFloat(value) + 1)) {
            midSpeedSlider((parseFloat(value) + 1));
        }
    }

    function midSpeedSlider(value) {
        $("#midSpeedOutput").val(value + " mph max");

        $("#highSpeed").attr("min", (parseFloat(value) + 1));

        if ($("#highSpeed").val() <= (parseFloat(value) + 1)) {
            highSpeedSlider((parseFloat(value) + 1));
        }
    }

    function highSpeedSlider(value) {
        $("#highSpeedOutput").val(value + " mph max");

        $("#highSpeed").attr("min", (parseFloat($("#midSpeed").val()) + 1));
    }

    /**
     * Reads in route data obtained via AJAX and draws route and stops on map.
     */
    function routeConfigCallback(data, status) {
        // Draw the paths for the route

        var route = data.routes[0];

        for (var i=0; i<route.shape.length; ++i) {
            var shape = route.shape[i];
            L.polyline(shape.loc, routeOptions).addTo(routeGroup);
        }
    }

    function stopsCallback(data) {
        data.stops.forEach(function(stop) {
            // Create the stop Marker
            var stopMarker = L.circleMarker([stop.lat,stop.lon], stopOptions).addTo(routeGroup);

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
    }

    function flyout() {
       $("#runTimesFlyout").show();
    }

    $("#submit").click(function() {
        $("#submit").attr("disabled", "disabled");
        $("#runTimesFlyout").hide();

        if ($("#beginDate").val() == "Date range") {
            var today = new Date();
            var beginDate = endDate = today.getFullYear() + "-"
                + (today.getMonth() <= 10 ? "0" + (today.getMonth() + 1) : (today.getMonth() + 1))
                + "-" + (today.getDate() < 10 ? "0" + today.getDate() : today.getDate());
        } else {
            var dateRangeStrings = $("#beginDate").val().replace(/\s/g, "").split("-");
            var beginYear = "20" + dateRangeStrings[0];
            var endYear = "20" + dateRangeStrings[3];
            var beginDate = [beginYear, dateRangeStrings[1], dateRangeStrings[2]].join("-");
            var endDate = [endYear, dateRangeStrings[4], dateRangeStrings[5]].join("-");
        }

        var beginTime = $("#beginTime").val() == "" ? "00:00:00" : $("#beginTime").val() + ":00";
        var endTime = $("#endTime").val() == "" ? "23:59:59" : $("#endTime").val() + ":00";

        request = {}

        request.beginDate = beginDate;
        request.endDate = endDate;
        request.beginTime = beginTime;
        request.endTime = endTime;
        request.r = $("#route").val();
        request.headsign = $("#direction").val();
        request.serviceType = $("#serviceDayType").val();

        routeGroup.clearLayers();
        if (request.r != "") {
            var url = apiUrlPrefix + "/command/routesDetails?r=" + request.r;
            $.getJSON(url, routeConfigCallback);
        }

        $.ajax({
            url: apiUrlPrefix + "/report/speedmap/stops",
            // Pass in query string parameters to page being requested
            data: request,
            // Needed so that parameters passed properly to page being requested
            traditional: true,
            dataType: "json",
            success: function (response) {
                $("#submit").removeAttr("disabled");
                var beginDateArray = beginDate.split("-");
                var endDateArray = endDate.split("-");
                [beginDateArray[0], beginDateArray[1], beginDateArray[2]] = [beginDateArray[1], beginDateArray[2], beginDateArray[0]];
                [endDateArray[0], endDateArray[1], endDateArray[2]] = [endDateArray[1], endDateArray[2], endDateArray[0]];
                var beginDateString = beginDateArray.join("/");
                var endDateString = endDateArray.join("/");

                var timeRange = beginTime + " to " + endTime;

                if (beginTime == "00:00:00" && endTime == "23:59:59") {
                    timeRange = "All times";
                }

                var serviceDayString = $("#serviceDayType").val();

                if (serviceDayString == "") {
                    serviceDayString = "All days";
                }

                $("#paramDetails").html("<p style='font-size: 0.8em;'>Route " + $("#route").val() + " to " + $("#direction").val() + " | " + beginDateString + " to " + endDateString + " | " + timeRange + " | " + serviceDayString + "</p>");

                stopsCallback(response);
            },
            error: function () {
                $("#submit").removeAttr("disabled");
                alert("Error processing stop details.");
            }
        })

        $.ajax({
            url: apiUrlPrefix + "/report/speedmap/runTime",
            // Pass in query string parameters to page being requested
            data: request,
            // Needed so that parameters passed properly to page being requested
            traditional: true,
            dataType: "json",
            success: function (response) {
                var compareLink = "<a id='compareLink' style='font-size: 0.8em; margin-bottom: 1em; color: blue; text-decoration: underline; cursor: pointer' onclick='flyout()'>Compare</a>";

                if (response.numberOfTrips == 0) {
                    $("#avgRunTime").html("<p style='font-size: 0.8em; margin-bottom: 0em;'>No average run time data.</p>"
                        + compareLink);
                }
                else {
                    var runTimeMinutes = parseInt(response.averageRunTime / 60000).toString();
                    var runTimeSeconds = parseInt(response.averageRunTime % 60000 / 1000).toString();
                    if (runTimeSeconds.length == 1) {
                        runTimeSeconds = "0" + runTimeSeconds;
                    }
                    $("#avgRunTime").html("<p style='font-size: 0.8em; margin-bottom: 0em;'>Average Trip Run Time: " + runTimeMinutes + ":" + runTimeSeconds + "</p>"
                        + compareLink);
                }
            },
            error: function () {
                alert("Error processing average trip run time.");
            }
        })
    })

    $("#closeFlyout").click(function() {
        $("#runTimesFlyout").hide();
    })

</script>