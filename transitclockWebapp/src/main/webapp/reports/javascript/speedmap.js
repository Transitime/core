
var mapTileUrl ='http://tile.openstreetmap.org/{z}/{x}/{y}.png'
var map = L.map('map', {
    minZoom: 12,
    maxZoom: 16
});
L.control.scale({metric: false}).addTo(map);
L.tileLayer(mapTileUrl, {
    attribution: '&copy; <a href="http://openstreetmap.org">OpenStreetMap</a> &amp; <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery ? <a href="http://mapbox.com">Mapbox</a>',
    maxZoom: 19
}).addTo(map);
// Calculate the offset
//var offset = map.getSize().x*0.15;
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
    // fillColor: '#ffffff',
    fillColor: '#D3D3D3',
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


        var content = $("<div />").attr("class","card");
        content.append('<div class="card-header header-theme">Speed</div>');


        var table = $("<div />").attr("class", "card-body");

        var label = $("<b />").text( "Speed: ");
        var value = $("<div />").attr("class", "vehicle-value").text(stopPath.speed + " mph");
        table.append( $("<div />").attr("class", "vehicle-item").append(label, value) )
        content.append(table)
        speedSegment.bindPopup(content[0], {className: 'vehicle-pop-up'});
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
        var content = $("<div />").attr("class","card");
        content.append('<div class="card-header header-theme">'+stop["stopName"]+'</div>')
        var table = $("<div />").attr("class", "card-body");

        //var content = $("<table />").attr("class", "popupTable");
        var labels = ["ID", "Avg Dwell Time"], keys = ["stopId",  "avgDwellTime"];
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
            var label = $("<b />").text(labels[i] + ": ");
            var value = $("<div />").attr("class", "vehicle-value").text(stop[keys[i]]);
            table.append( $("<div />").attr("class", "vehicle-item").append(label, value) )
        }
        content.append(table)
        stopMarker.bindPopup(content[0], {className: 'vehicle-pop-up'});
    })

    resetDetail();
}

$("#mainSubmit").click(function() {
    $("#mainSubmit").attr("disabled", "disabled");
    $("#mainSubmit").attr("value","loading...");
    $("#dateRangePicker").val("Date range")
    $("#avgRunTimeComparison").html("");
    $("#runTimesFlyout").hide();
    $(".loader").show();
    $(".wrapper").addClass("split");
    $("#mainPage").addClass("inactive-split");

    request = {}

    getParams("#beginDate");

    routeGroup.clearLayers();
    if (request.r != "") {
        var url = apiUrlPrefix + "/command/routesDetails?r=" + request.r;
        $.getJSON(url, routeConfigCallback);
    }

    setTimeout(function(){ map.invalidateSize(), map.setZoom(14)}, 500);

    var lowSpeed = parseFloat($("#lowSpeedManual").val());
    var midSpeed = parseFloat($("#midSpeedManual").val())

    $("#low-speed-label").html("Low Speed (0 - "+lowSpeed+" mph)")
    $("#mid-speed-label").html("Mid Speed ("+lowSpeed+" - "+midSpeed+" mph)")
    $("#high-speed-label").html("High Speed (> "+midSpeed+" mph)")

    $.ajax({
        url: apiUrlPrefix + "/report/speedmap/stopPathsSpeed",
        // Pass in query string parameters to page being requested
        data: request,
        // Needed so that parameters passed properly to page being requested
        traditional: true,
        dataType: "json",
        success: function(response) {
            speedsCallback(response);
            $(".comparsion-button-list").removeClass("d-none");
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

                    var timeRange = request.beginTime + " <span>to</span> " + request.endTime;

                    if (beginTime == "00:00:00" && endTime == "23:59:59") {
                        timeRange = "All times";
                    }

                    var serviceDayString = request.serviceType;

                    if (serviceDayString == "") {
                        serviceDayString = "All days";
                    }

                    $(".paramDetails").each(function() {
                        $(this).html("<p >Route " + request.r + " <span>to</span> " + request.headsign + " | " + beginDateString + " <span>to</span> " + endDateString + " | " + timeRange + " | " + serviceDayString + "</p>");
                    })

                    stopsCallback(response);
                },
                error: function (e) {
                    resetDetail("Error processing stop details.");
                }
            })
        },
        error: function (e) {

            resetDetail("Error processing speed details for stops.");
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
            var compareLink = "<a id='compareLink' data-bs-toggle='modal' class='link white' data-bs-target='#runTimesFlyout' onclick='openFlyout()'>Compare</a>";

            if (response.numberOfTrips == 0) {
                $("#avgRunTimeTop").html("<div class='paramDetails'></div><p>No average run time data.</p>" + compareLink);
            }
            else {
                runTimesChart.data.datasets[0].data[0] = response.averageRunTime;
                runTimesChart.update();

                $("#avgRunTimeTop").html("<div class='paramDetails'></div><p>Average Trip Run Time: " + msToMinsSecs(response.averageRunTime) + "</p>" + compareLink);
            }
        },
        error: function () {
            resetDetail("Error processing average trip run time.");
        }
    })
})

function openFlyout() {
    $("#avgRunTimeFlyout").html($("#avgRunTimeTop > p").html());
    $("#runTimesFlyout").show();
}

$("#runTimeSubmit").click(function() {
    $(".submit").attr("disabled", "disabled");
    $(".loader").show();

    getParams("#dateRangePicker");

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

            resetDetail();
        },
        error: function () {
            resetDetail("Error processing average trip run time.");
        }
    })
})

$("#closeFlyout").click(function() {
    $("#runTimesFlyout").hide();
})
function resetDetail(alertContent, isRemoveClass){

    $(".loader").hide();
    $("#mainSubmit").removeAttr("disabled");
    $("#mainSubmit").attr("value","Submit");

    if(!isRemoveClass){
        $("#mainPage").removeClass("inactive-split");
    }

    if(alertContent){
        alert(alertContent);
    }

}

if(datePickerIntialization){
    datePickerIntialization();
}

