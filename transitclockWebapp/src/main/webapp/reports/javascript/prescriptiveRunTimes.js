var stops = {};

// this must be for sizing the screen?
var highestPoints = [];

// this must be to show hide results?
$(".toggle-chart").addClass("d-none");

// ROUTE - Change Event
$("#route").change(function() {
    if ($("#route").val() && $("#route").val().trim() != "") {
        $(".individual-route-only").show();
    } else {
        $(".individual-route-only").hide();
    }
})

// GTFS VERSIONS - Populate GTFS Versions (On Load)
function populateGtfsVersions() {
    $("#gtfsDateRange").attr("disabled", true);

    $.ajax({
        url: apiUrlPrefix + "/report/runTime/datedGtfs",
        // Pass in query string parameters to page being requested
        // Needed so that parameters passed properly to page being requested
        traditional: true,
        dataType: "json",
        success: function (response) {
            response.datedGtfs.forEach(function (gtfs) {
                var datedGtfs = new Object();
                datedGtfs.startDate = gtfs.startDate;
                datedGtfs.endDate = gtfs.endDate;
                datedGtfs.version = gtfs.version;
                datedGtfs.configRev = gtfs.configRev;
                var datedGtfsVal = JSON.stringify(datedGtfs);

                $("#gtfsDateRange").append('<option value=\'' + datedGtfsVal + '\'>' + gtfs.label + '</option>');
            })
            $("#gtfsDateRange").attr("disabled", false);
        },
        error: function (response) {
            alert("Error retrieving directions for route " + response.r);
            $("#gtfsDateRange").attr("disabled", false);
        }
    })
}

// Execulte Populate Dated Gtfs
populateGtfsVersions();


// SUBMIT EVENT **********************************

// SUBMIT - Click
$("#submit").click(function() {

    // Handle loading
    $("#submit").attr("disabled", "disabled");
    $("#submit").html("Loading...");
    $("#overlay").show();
    $("#bars1").show();
    $("#mainPage").addClass("inactive-split")
    $(".wrapper").addClass("split");
    $("#mainResults").hide();

    var requestParams = getParams();
    var dataUrl = apiUrlPrefix + "/report/runTime/prescriptiveRunTimes";

    $.ajax({
        url: dataUrl,
        // Pass in query string parameters to page being requested
        data: requestParams,
        // Needed so that parameters passed properly to page being requested
        traditional: true,
        dataType: "json",
        success: function(response) {
            resetDisable();
            if (jQuery.isEmptyObject(response)) {
                alert("No run time information available for selected parameters.");
            } else {
                var adjustmentsSuccess = response.data &&  response.data.length > 0;

                $(".toggle-chart").removeClass("d-none");
                $(".image-container").addClass("d-none");
                if (adjustmentsSuccess) {
                    updateParamHeader(requestParams);
                    generatePrescriptiveRunTimesTable(response.data, response.routeShortName);
                } else {
                    alert("No Prescriptive RunTimes available for selected criteria.");
                }
            }
        },
        error: function(e) {
            resetDisable();
            // $("body").removeClass("loader");
            alert("No Prescriptive RunTimes available for selected criteria.");
        }
    })
});

function updateParamHeader(request){
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
        for( var i = 0; i < $("#route")[0].options.length; i++){
            var eachOption = $("#route")[0].options[i];
            if(request.r === eachOption.value){
                selectedRoute = eachOption.text;
            }
        }
    }
    var selectedDate = beginDateString + " - " + endDateString;
    var contentTripHeader = "<div class='route-time-analysis-header-param'>Route :  "+selectedRoute+"</div>";
    contentTripHeader += "<div class='route-time-analysis-header-param'>Service Date Range : "+selectedDate+"</div>";
    contentTripHeader += "<div class='route-time-analysis-header-param'>Service Day : "+serviceDayString.toUpperCase()+"</div>";

    $(".route-time-analysis-header").html(contentTripHeader);
}

// SUBMIT - Reset Disable
function resetDisable() {
    $("#submit").attr("disabled", false);
    $("#submit").html("Analyze");
    $("#overlay").hide();
    $("#mainPage").removeClass("inactive-split")
    $("#bars1").hide();
}

// SUBMIT - Parse params from input fields
function getParams() {

    var routeName = $("#route").val().trim() == "" ? "" : $("#route").val();
    var serviceTypeVal = $("#serviceDayType").val();
    var gtfsDateRangeVal = $("#gtfsDateRange").val();

    params = {};

    params.r = routeName
    params.serviceType = serviceTypeVal;

    if (gtfsDateRangeVal == null) {
        params.beginDate = "";
        params.endDate = "";
        params.configRev = "";
    } else {
        var gtfsDateRangeJSON = JSON.parse(gtfsDateRangeVal);
        params.beginDate = gtfsDateRangeJSON.startDate;
        params.endDate = gtfsDateRangeJSON.endDate;
        params.configRev = gtfsDateRangeJSON.configRev;
    }

    return params;
}

// SUBMIT - Show Prescriptive RunTimes
function generatePrescriptiveRunTimesTable(data, routeName) {

    $(".adjustment-details").html('');

    var resultObject = {};
    resultObject.routeName = 'ROUTE ' + routeName;
    resultObject.runTimeTables = [];

    data.forEach(function(tripPattern){
        var runTimeTable = {};
        runTimeTable.rowHeader = ['From Time','To Time'];
        runTimeTable.rowValues = [];

        var currentTable = '<table class="table table-bordered small"><tbody>';

        // Header row
        currentTable += '<tr>';
        currentTable += '<th>From Time</th><th>To Time</th><th>Total Time</th>';
        tripPattern.stop_names.forEach(function(stopName){
            currentTable += '<th>' + stopName + '</th>';
            runTimeTable.rowHeader.push(stopName);
        });
        currentTable += '</tr>';

        // Time band info
        tripPattern.adjustments.forEach(function(adjustment){
            var rowValue = [];

            currentTable += '<tr>';
            currentTable += '<td>' + adjustment.fromTime + '</td>';
            currentTable += '<td>' + adjustment.toTime + '</td>';

            rowValue.push(adjustment.fromTime);
            rowValue.push(adjustment.toTime);


            if(adjustment.adjusted_times !== 'undefined') {
                var size  = adjustment.adjusted_times.length;
                var rounded_adjusted_times = [];
                var rounded_scheduled_times = [];
                var rounded_total_adjusted_time = 0;
                var rounded_total_scheduled_time = 0;

                for (let idx = 0; idx < size; idx++) {
                    var adjusted_time = adjustment.adjusted_times[idx];
                    var schedule_time = adjustment.original_times[idx];

                    var rounded_adjustment_time = getRoundedTime(adjusted_time);
                    var rounded_scheduled_time = getRoundedTime(schedule_time);

                    rounded_adjusted_times.push(rounded_adjustment_time);
                    rounded_scheduled_times.push(rounded_scheduled_time);
                    rounded_total_adjusted_time += rounded_adjustment_time;
                    rounded_total_scheduled_time += rounded_scheduled_time;
                }

                currentTable += '<td><table class="prescriptive-table-cell"><tr>';
                currentTable += '<td class="totalAdjustment">' + rounded_total_adjusted_time + '</td>';
                currentTable += '<td class="totalScheduled">' + rounded_total_scheduled_time + '</td>';
                currentTable += '</tr></table></td>';

                for (let idx = 0; idx < size; idx++) {
                    var adjustment_output = rounded_adjusted_times[idx];
                    var scheduled_output = rounded_scheduled_times[idx];

                    if(adjustment_output == 0){
                        currentTable += '<td><table class="prescriptive-table-cell"><tr>';
                        currentTable += '<td>' + '-' + '</td>'
                        currentTable += '</tr></table></td>';
                    }
                    else {
                        currentTable += '<td><table class="prescriptive-table-cell"><tr>';
                        currentTable += '<td class="adjustment">' + adjustment_output + '</td>'
                        currentTable += '<td class="schedule">' + scheduled_output + '</td>'
                        currentTable += '</tr></table></td>';
                    }

                    rowValue.push(adjustment_output);
                }
            }

            runTimeTable.rowValues.push(rowValue);

            currentTable += '</tr>';

        });

        currentTable += '</tbody></table>';

        resultObject.runTimeTables.push(runTimeTable);

        //$("#current_otp").html(response.current_otp);
        //$("#expected_otp").html(response.expected_otp);
        $(".adjustment-details").append(currentTable);
    });

    $("#routeName").val(routeName);
    $("#resultObject").val(encodeURIComponent(JSON.stringify(resultObject)));


}


function getScheduledType(timeReference) {
    if (timeReference <= -6000) {
        return "late";
    } else if (timeReference >= 6000) {
        return "early";
    }
    return "ontime";
}

// DOWNLOAD SCHEDULE *******************************

// DOWNLOAD - Click
$(".gtfs-submit").click(function() {
    $(".gtfs-submit").attr("disabled", "disabled");
    var type = "text/csv";
    var errorMsg = "Unable to export Prescriptive RunTimes Schedule.";
    var dataUrl = apiUrlPrefix + "/report/runTime/prescriptiveRunTimesExport";
    var data = decodeURIComponent($("#resultObject").val());
    var routeName = $("#routeName").val();
    var filename = 'ROUTE ' + routeName + '.csv';


    $.ajax({
        url: dataUrl,
        // Pass in query string parameters to page being requested
        data: data,
        method: 'POST',
        // Needed so that parameters passed properly to page being requested
        success: function(result) {
            $(".gtfs-submit").attr("disabled", false);
            var blob = new Blob([result], {type: 'application/csv'});
            saveFile(filename, type, blob, errorMsg);
        },
        error: function(e) {
            console.log(e);
            $(".gtfs-submit").attr("disabled", false);
            alert(errorMsg);
        }
    })

});

// DOWNLOAD - Save File Function
function saveFile(name, type, data, errorMsg) {
    if (data !== null) {
        if (window.navigator && window.navigator.msSaveOrOpenBlob) {
            return navigator.msSaveBlob(new Blob([data], {
                type: type
            }), name);
        } else {
            var a = $("<a style='display: none;'/>");
            var url = window.URL.createObjectURL(new Blob([data], {
                type: type
            }));
            a.attr("href", url);
            a.attr("download", name);
            $("body").append(a);
            a[0].click();
            window.URL.revokeObjectURL(url);
            a.remove();
        }
    } else {
        alert(errorMsg);
    }
}

function getRoundedTime(timeInMillisec){
    var result = parseFloat((timeInMillisec / 60000).toFixed(0));
    if(timeInMillisec > 0 && result <= 1){
        return 2;
    }
    return result;
}