
var highestPoints = [];

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
                    if(chart.config.type !== 'bar'){
                        ctx.fillText(data, bar._model.x - ((bar._model.x - bar._model.base) / 2), bar._model.y + 5);
                    }


                }
                ctx.restore(); //<- restore canvas state
            }))
        }));
    }
});

function populateDirection() {

    $("#submit").attr("disabled", true);
    $("#submit").html("Loading...");
    // $("body").addClass("loader");
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
            //  $("body").removeClass("loader");
            $("#submit").html("Submit");
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
                $("#submit").html("Loading...");
                //  $("body").addClass("loader");
            } else {
                $("#tripPattern").removeAttr('disabled');
                $("#submit").removeAttr('disabled');
                $("#submit").html("Submit");
                // $("body").removeClass("loader");

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
            //  $("body").removeClass("loader");
            $("#submit").html("Submit");
        }
    });
}


function datePickerIntialization () {
    var calendarIconTooltip = "Popup calendar to select date";

    $(".date-picker-input").datepick({
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

    $(".time-picker-input").timepicker({timeFormat: "H:i"})
        .on('change', function (evt) {
            if (evt.originalEvent) { // manual change
                // validate that this looks like HH:MM
                if (!evt.target.value.match(/^(([0,1][0-9])|(2[0-3])):[0-5][0-9]$/))
                    evt.target.value = evt.target.oldval ? evt.target.oldval : "";
            }
            evt.target.oldval = evt.target.value;
        });

}

function getDefaultChartOptions(options){

    var canvas = $("#visualizationCanvas");
    var barGraph = new Chart(canvas, {
        type: options && options.type || 'horizontalBar',
        data: {},
        options: {
            showPercentage: options && options.showPercentage,
            scales: {
                xAxes: [
                    {
                        stacked: true,
                        position: 'top',
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: ""
                        },
                        ticks: options && options.xAxis && options.xAxis.ticks ||{}


                    },{
                        stacked: true,
                        type:"linear",
                        display: true,
                        position: 'bottom',
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


function calculateMaxMins(points) {
    var maxMins = Math.round(points[0]) + Math.round(points[1]) + Math.round(points[2]);
    if (Math.round(points[3]) > maxMins) {
        maxMins = Math.round(points[3]);
    }
    if (Math.round(points[4]) > maxMins) {
        maxMins = Math.round(points[4]);
    }

    return Math.ceil(maxMins / 5) * 5;
}


function getFixedVariableDwellDataSet(fixed,variable,dwell) {
   return [
        {
            data: msToMin(fixed),
            backgroundColor: '#36509b',
            label: "Fixed",
            yAxisId: "bars"
        },
        {
            data: msToMin(variable),
            backgroundColor: '#df7f17',
            label: "Variable",
            yAxisId: "bars"
        },
        {
            data: msToMin(dwell),
            backgroundColor: '#8c8c8c',
            label: "Dwell",
            yAxisId: "bars"
        }
    ]
}

function findMinMax(arr) {

    var min = arr[0], max = arr[0];

    for (var i = 1, len=arr.length; i < len; i++) {
        var v = arr[i].y;
        min = (v < min) ? v : min;
        max = (v > max) ? v : max;
    }

    return [min, max];
}

function getHighestData(data){
    var highest = 0;

    for (var i = 0 in data) {
        data[i] = data[i];
        if (data[i] > highest) {
            highest = data[i];
        }
    }

    highestPoints.push(highest);
    return data;
}


function range(start, end, custom) {
    return Array(end - start + 1).fill().map(function(_, idx) {
        if(custom){
            return custom;
        }
        return { "name": (5 *(idx+1) + "%" ), "value": (5 *(idx+1))}
    })
}

function arraysToXAndY(data) {
   var xsAndYs = [];
    for (var i = 0 in data[0]) {
        xsAndYs[i] = {x: data[0][i], y: data[1][i]};
    }

    return xsAndYs;
}

function createCanvasElement(data){
    var defaultHeight = (data.length ) *100;
    var defaultWidth = window.innerWidth;

    if(defaultHeight < (window.innerHeight/2 - 100)) {
        defaultHeight =  window.innerHeight;
    }
    $("#runTimeVisualization").html(' <canvas id="visualizationCanvas" class="custom-canvas"  height="'+defaultHeight+'" width="'+defaultWidth+'"></canvas>');
}

function updateSummaryTable(summary){
    var avgRunTime = typeof (summary.avgRunTime) == 'undefined' ? "N/A" : (summary.avgRunTime / 60000).toFixed(1) + " min";
    var avgFixed = typeof (summary.fixed) == 'undefined' ? "N/A" : (summary.fixed / 60000).toFixed(1) + " min";
    var avgVar = typeof (summary.variable) == 'undefined' ? "N/A" : (summary.variable / 60000).toFixed(1) + " min";
    var avgDwell = typeof (summary.dwell) == 'undefined' ? "N/A" : (summary.dwell / 60000).toFixed(1) + " min";

    var tableTD = "<td>"+avgRunTime+"</td>";
    tableTD += "<td>"+avgFixed+"</td>";
    tableTD += "<td>"+avgVar+"</td>";
    tableTD += "<td>"+avgDwell+"</td>";
    if($(".average-time-details").length > 0)
    {
        $(".average-time-details").html(tableTD);
    }   else {
        $("#avg-run-time").html(avgRunTime);
        $("#fixed-time").html(avgFixed);
        $("#variable-time").html(avgVar);
        $("#dwell-time").html(avgDwell);
    }
}

function updateParamDetails(route, headsign, tripPattern, beginDateString, endDateString, timeRange, serviceDayString, showTrip){
    var tripPatternContent = "";
        if(showTrip){
            tripPatternContent =  (!tripPattern || tripPattern == "" ? "All Trip Patterns" : tripPattern) + " | ";
        }
                
    $(".param-detail-content").html("<p style='font-size: 0.8em;'>" +
        (!route || route == "" ? "All routes" : "Route " + route) + " to " +
        (!headsign || headsign == "" ? "All directions" : headsign) + " | " +
        tripPatternContent +
        beginDateString + " to " + endDateString +  " | " +
        timeRange +
        (!serviceDayString || serviceDayString == "" ? "" : " | " + serviceDayString) +
        "</p>");
}

$("#route").attr("style", "width: 200px");

$("#direction").change(function () {
    populateTripPattern();
})