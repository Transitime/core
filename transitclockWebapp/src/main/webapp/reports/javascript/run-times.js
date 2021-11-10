
    var stops = {};
    var visualarGraphChart;

    $(".individual-route-only").hide();
    $( "#run-time-tabs" ).tabs();
    $(".route-settings").hide();
    $("#timePointsOnly").trigger("click");

    $("#route").change(function () {
        if ($("#route").val().trim() != "") {
            $(".individual-route-only").show();
            $(".route-settings").show();
            populateDirection();
        } else {
            $(".individual-route-only").hide();
            $(".route-settings").hide();
            $("#direction").empty();
            $("#tripPattern").empty();
            $("#direction").attr("disabled", true);
            $("#tripPattern").attr("disabled", true);
        }
    })

    datePickerIntialization();

    function percentileCalculation(data, param){

        var sortedData = data.sort(function(a, b) {
            return a - b;
        });
        var sortedDataLength = sortedData.length;
        var percentageValue = param;
        var valueOfPercentile = sortedDataLength * percentageValue / 100;
        var pickedValue = 0;
        var flooredValue = Math.floor(valueOfPercentile) === valueOfPercentile;
        var sortedIndexValues = [];
        var nonSortedIndexValues = [];
        var valuesPert = [];

        if(flooredValue){
            var u = valueOfPercentile- 1;
            var a = valueOfPercentile;
            sortedIndexValues.push(u);
            sortedIndexValues.push(a);


            pickedValue = (data[valueOfPercentile- 1] + data[valueOfPercentile]) / 2;
        } else {
            var ceilValue = Math.ceil(valueOfPercentile);
            sortedIndexValues.push(ceilValue-1);
            pickedValue = sortedData[ceilValue - 1];
        }
        data.forEach(function(eachValue,index){
            if(sortedIndexValues.length == 2 && (sortedData[sortedIndexValues[0]] === eachValue || sortedData[sortedIndexValues[1]] === eachValue)){
                nonSortedIndexValues.push(index);
            }else if(sortedIndexValues.length == 1 && (sortedData[sortedIndexValues[0]] === eachValue)){
                nonSortedIndexValues.push(index);
            }

        });


        return nonSortedIndexValues;
    }

    function convertToMins(scheduledData){
        var data = [];
        var sumOfData = 0;
        var average;
        for (var i = 0 in scheduledData) {

            data.push(parseFloat(scheduledData[i]));
            sumOfData += parseFloat(scheduledData[i] );

        }

        average = Math.round(sumOfData/scheduledData.length);
        var minMax = findMinMax(scheduledData);

        return {
            minsData : data,
            average: average,
            min: minMax[0],
            max: minMax[1]

        }
    }


    function generatePercentileTable(tripsDisplayData, formattedScheduled, formattedRunTimeTrips){

        var tableTD = "<tr><th>Trip</th><th>Schedule</th><th>Run Time</th></tr>";

        var sumOfData = 0;
        tripsDisplayData.tripName.forEach(function (eachTrip, i) {

            var eachData = {
                trip: eachTrip,
                schedule: formattedScheduled[i]+" min",
                percentile: formattedRunTimeTrips[i] +" min"
            };
            sumOfData += parseFloat(formattedRunTimeTrips[i]);
            tableTD += "<tr>";
            tableTD += "<td>"+eachData.trip+"</td>";
            tableTD += "<td>"+eachData.schedule+"</td>";
            tableTD += "<td>"+eachData.percentile+"</td>";
            tableTD += "</tr>";
        });

        var average = Math.round(sumOfData/tripsDisplayData.tripVal.length);
        var percentileSummaryData = average +" min";

        $("#percentile-summary-content").html(percentileSummaryData);

        return tableTD;
    }

    function generateRunTimes(tripRunTimes, percentile){
        var filteredRunTimeTrips = [];
        tripRunTimes.forEach(function (eachTrip, i) {
            if(!eachTrip.formattedRunTimes){
                eachTrip.formattedRunTimes = msToMin(eachTrip.runTimes);
            }
            var nonSortedIndex =   percentileCalculation(eachTrip.formattedRunTimes, percentile);
            filteredRunTimeTrips.push(eachTrip.formattedRunTimes[nonSortedIndex[0]] || 0);
        });

        return filteredRunTimeTrips;
    }
    
    function distributionTabDetails(response){

        $("#distributionVisualization").html('');

        var distributedData = [];
        var tripRunTimes = response.data.tripRunTimes;
        tripRunTimes.forEach(function(eachTrip){
            var eachdata = msToMin(JSON.parse(JSON.stringify(eachTrip.runTimes)));
            distributedData.push(eachdata);
        });

        var tripsDisplayData = getTripsDisplayData(response.data.trips);

        var defaultHeight = (tripsDisplayData.tripName.length ) *80;
        var defaultWidth = window.innerWidth;

        if(defaultHeight < (window.innerHeight/2 - 100)) {
            defaultHeight =  window.innerHeight;
        }

        $("#distributionVisualization").html(' <canvas id="distributionCanvas" class="custom-canvas"  height="'+defaultHeight+'" width="'+defaultWidth+'"></canvas>');
        var color = Chart.helpers.color;
        var rgbRED = "rgb(255,0,0)";
        var boxplotData = {
            labels: tripsDisplayData.tripName,
            datasets: [{
                label: 'Trip Run Times',
                backgroundColor: color(rgbRED).alpha(0.5).rgbString(),
                borderColor: rgbRED,
                borderWidth: 1,
                data: distributedData,
                padding: 10,
                itemRadius: 2,
                itemStyle: 'circle',
                itemBackgroundColor: 'rgba(143,143,143,0.5)'
            }]

        };

        var canvas = $("#distributionCanvas");
        var barGraph = new Chart(canvas, {
            type: 'horizontalBoxplot',
            data: boxplotData,
            options: {
                responsive: true,
                legend: {
                    position: 'top',
                },
                scales: {
                    xAxes: [
                        {
                            scaleLabel: {
                                display: true,
                                labelString: "Minutes"
                            }
                        }
                    ]
                }
            }
        });


    }

    function percentageTabDetails(response){

        $("#percentile-select-container").html("");
        $("#percentile-summary-content").html("");
        $(".percentile-summary-details").html("");

        var percentileSelectOptions = range(1, 20);

        var tripRunTimes =  response.data.tripRunTimes;

        var formattedScheduled = convertToMins(response.data.scheduled);

        var formattedRunTimeTrips = generateRunTimes(tripRunTimes, 50);
        var tripsDisplayData = getTripsDisplayData(response.data.trips);

        var tableTD = generatePercentileTable(tripsDisplayData, formattedScheduled.minsData, formattedRunTimeTrips);

        var percentileSelect = $('<select id="percentile-select-box" name="percentileSelect" class="form-select"></select>');
        percentileSelectOptions[percentileSelectOptions.length-1] = {
            value: "99",
            name: "99%"
        };
        percentileSelectOptions.forEach(function (eachTrip, i) {
            var option = $('<option></option>');
            option.attr('value', eachTrip.value);
            option.text(eachTrip.name);
            percentileSelect.append(option);
        });

        percentileSelect.append( '<span class="select2-selection__arrow"><b role="presentation"></b></span>');
        var percentileSelectContainer = $('<div class="percentileSelect"></div>');

        $("#percentile-select-container").append('<label for="percentileSelect" class="percentileLabel">Percentile : </label>');
        $(percentileSelectContainer).append(percentileSelect);
        $("#percentile-select-container").append(percentileSelectContainer);
        $("#percentile-select-container").append('<label for="percentileSelect2" class="percentileLabel2">Average Run Time for <span id="n-th-percentile">50th</span> percentile</label>');

        $("#percentile-select-box").val("50");
        $("#percentile-select-box").change(function () {
            var tableTD ;

            if ($("#percentile-select-box").val().trim() !== "") {

                var valuePercentage = $("#percentile-select-box").val().trim();

                $("#n-th-percentile").html(valuePercentage+"th");
                var formattedRunTimeTrips = generateRunTimes(tripRunTimes, valuePercentage );

                tableTD = generatePercentileTable(tripsDisplayData, formattedScheduled.minsData, formattedRunTimeTrips);
                console.log($("#percentile-select-box").val().trim());
            } else {
                tableTD = generatePercentileTable(tripsDisplayData, formattedScheduled.minsData, 0);
                console.log($("#percentile-select-box").val().trim());
            }

            $(".percentile-summary-details").html(tableTD);

        });


        $(".percentile-summary-details").html(tableTD);

    }



    function getParams() {
        var datepicker, serviceTypeSelector;

        datepicker = "beginDate";
        serviceTypeSelector = "serviceDayType";

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

        var tripPatternName = $("#tripPattern").val() == null ? "" : $("#tripPattern").val();

        var directionName = $("#direction").val();

        params = {};

        params.beginDate = beginDate;
        params.endDate = endDate;
        params.beginTime = beginTime;
        params.endTime = endTime;
        params.r = routeName
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

    function getTripsDisplayData(trips){
        var tripsData = {};
        tripsData.tripName = [];
        tripsData.tripVal = []
        trips.forEach(function (eachTrip, i) {
            var values = eachTrip.split("-");
            var optionValue = values[1].trim();
            if(tripsData.tripVal.indexOf(optionValue) < 0){
                tripsData.tripName.push(eachTrip);
                tripsData.tripVal.push(optionValue);
            }

        });
        return tripsData;
    }

    function showStopView(){

        highestPoints = [];
        request = getParams();

        request.timePointsOnly = $("#timePointsOnly")[0].checked;
        request.tripId = $("#trips-select-box").val();
        /*Orginal URL*/
        var stopDataURL = apiUrlPrefix +  "/report/runTime/avgStopPathRunTimes";

        $.ajax({
            url: stopDataURL,
            // Pass in query string parameters to page being requested
            data: request,
            // Needed so that parameters passed properly to page being requested
            traditional: true,
            dataType: "json",
            success: function (response) {
                if(response.data.stopPaths && response.data.stopPaths.length ){

                    createCanvasElement(response.data.stopPaths);
                    generateIndividualChart(response, false);
                } else {
                    alert("Error retrieving stop-by-stop summary.");
                }
            },
            error: function (e) {
                alert("Error retrieving stop-by-stop summary.");

            }
        });

    }


    $("#submit").click(function () {
        $("#submit").attr("disabled", "disabled");
        $("#submit").html("Loading...");
        $("#overlay").show();
        $("#bars1").show();
        $("#mainPage").addClass("inactive-split")
        $(".wrapper").addClass("split");
        $("#mainResults").hide();
        $("#runTimeVisualization").hide();
        $("#modalDatepicker").val("Date range")

        request = getParams();

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

        var visualDataURL = apiUrlPrefix +  "/report/runTime/avgTripRunTimes";
        var isAllRoutes = false;

        if(!request.r){
            delete request.r;
            delete request.tripPattern;
            delete request.serviceType
            delete  request.headsign;
            isAllRoutes = true;
            request.minEarlySec="60";
            request.minLateSec="300";
            visualDataURL =  apiUrlPrefix + "/report/runTime/routeRunTimes";
        }

        $.ajax({
            url: visualDataURL,
            // Pass in query string parameters to page being requested
            data: request,
            // Needed so that parameters passed properly to page being requested
            traditional: true,
            dataType: "json",
            success: function (response) {
                if (jQuery.isEmptyObject(response)) {
                    $("#component").hide();
                    // add 
                    resetDisable();
                    alert("No run time information available for selected parameters.");
                }else if(response.data && (response.data.summary || response.data.routes)) {
                    /*updateParamDetails(request.r, request.headsign, request.tripPattern, beginDateString, endDateString,
                        timeRange, serviceDayString, true);*/
                    var selectedRoute = "All routes";
                    if(request.r){
                        for( var i = 0; i < route.options.length; i++){
                            var eachOption = route.options[i];
                            if(request.r === eachOption.value){
                                selectedRoute = eachOption.text;
                            }
                        }
                    }

                    var selectedDate = beginDateString + " - " + endDateString;
                    var contentTripHeader = "<div class='route-time-analysis-header-param'>Analysis Details :  "+selectedRoute+"</div>";
                    contentTripHeader += "<div class='route-time-analysis-header-param'>Date : "+selectedDate+"</div>";
                    contentTripHeader += "<div class='route-time-analysis-header-param'>Time : "+timeRange+"</div>";

                    $(".route-time-analysis-header").html(contentTripHeader);

                    if(response.data.summary){
                        $(".all-routes").hide();
                        $(".individual-route").show();
                        updateSummaryTable(response.data.summary);
                    } else{
                        $(".individual-route").hide();
                    }
                    visualizeData(response, visualDataURL, isAllRoutes);
                    $("#component").show();

                    $("#mainResults").show();
                    resetDisable(true);
                }
                else {
                    $("#component").hide();
                    resetDisable()
                    alert("Unable to find any valid results. Please try a different search.");
                }

            },
            error: function () {
                resetDisable()
                alert("Error processing average trip run time.");
            }
        })
    })

    function resetDisable(addSplit){

        $("#submit").removeAttr("disabled");
        $("#submit").html("Submit");
        $("#overlay").hide();
        $("#bars1").hide();
        if(!addSplit){
            $("#mainPage").removeClass("inactive-split")
        }
    
    }

 
    
    function visualizeData(response, visualDataURL, isAllRoutes) {
        highestPoints = [];

        if(visualarGraphChart && visualarGraphChart.destroy){
            visualarGraphChart.destroy();
        }

        if(isAllRoutes){
            $("#run-time-tabs" ).tabs().tabs('destroy');
            $(".only-individual-route").addClass("hide-routes");
            $(".all-routes").show();
        } else{
            $(".only-individual-route").removeClass("hide-routes");
            $(".all-routes").hide();
            $("#run-time-tabs" ).tabs({ active: 0 });
        }

        if(response.data && ((response.data.trips && response.data.trips.length > 0) ||
            (response.data.routes && response.data.routes.length > 0))){

            var cloneResponse =  JSON.parse(JSON.stringify(response));

            if(response.data.trips && response.data.trips.length ){

                var tripSelectBox = $('<select id="trips-select-box" class="form-select col-sm-8" name="tripBoxType"><option value="">All Trips</option></select>');
                var tripsDisplayData = getTripsDisplayData(response.data.trips);
                tripsDisplayData.tripVal.forEach(function (eachTrip, i) {
                    var option = $('<option></option>');
                    option.attr('value', tripsDisplayData.tripVal[i]);
                    option.text(tripsDisplayData.tripName[i]);
                    tripSelectBox.append(option);
                });

                tripSelectBox.append( '<span class="select2-selection__arrow"><b role="presentation"></b></span>');
                var tripSelectBoxContainer = $('<div class="trip-select-box-container"><h3>Component Visualization</h3></div>');

                var tripSelectBoxContainerChild = $('<div class="row flex-nowrap align-items-center"></div>');
                var tripSelectBoxContainerChild2 = $('<div class="col-sm-2"></div>');

                tripSelectBoxContainerChild2.append(tripSelectBox);
                tripSelectBoxContainerChild.append(tripSelectBoxContainerChild2)
                tripSelectBoxContainerChild.append('<label for="tripBoxType" id="visualization-container-header" class="col-sm-4 d-none">Trip Run Times</label>');
                tripSelectBoxContainer.append(tripSelectBoxContainerChild);
                $("#trips-container").html("");

                $("#trips-container").append(tripSelectBoxContainer);

               //  $("#trips-container").append('<label for="tripBoxType" id="visualization-container-header" class="col-sm-4">Trip Run Times</label>');

                $("#trips-select-box").change(function () {

                    if ($("#trips-select-box").val().trim() != "") {
                        showStopView();
                        $("#visualization-container-header").html("Stop Run Times");
                    } else {
                        $.ajax({
                            url: visualDataURL,
                            // Pass in query string parameters to page being requested
                            data: request,
                            // Needed so that parameters passed properly to page being requested
                            traditional: true,
                            dataType: "json",
                            success: function (response) {
                                visualizeData(response, visualDataURL, false);
                            },
                            error: function () {
                                $("#submit").removeAttr("disabled");
                                $("#submit").html("Submit");
                                $("body").removeClass("loader");
                                alert("Error retreiving trip run times.");
                            }
                        });
                        $("#visualization-container-header").html(" Trip Run Times");
                    }
                });
                     
                createCanvasElement(response.data.trips);
            } else{
                 createCanvasElement(response.data.routes);
            }

            if(isAllRoutes){
                generateAllRouteChart(response);
            } else{
                generateIndividualChart(response, true);
                percentageTabDetails(response);
                distributionTabDetails(cloneResponse);
            }

            $("#comparisonResults").hide();
            $("#runTimeVisualization").show();

        } else{
            alert("No trip breakdown available for selected run time data.");

        }
    }
    
    function generateIndividualChart(response, isRoute){
        var barGraph = getDefaultChartOptions();
        var datasets = getFixedVariableDwellDataSet(response.data.fixed,response.data.variable,response.data.dwell);
        var labels = response.data.stopNames;
        var labelValue= "Timepoint Run Times For Trip";
        if(isRoute){
            datasets = [].concat(datasets,[{
                type: "scatter",
                data: arraysToXAndY([msToMin(response.data.scheduled), response.data.trips]),
                backgroundColor: '#70a260',
                label: "Scheduled",
                showLine: false,
                // fill: false,
                // yAxisId: "icons"
            },
            {
                type: "scatter",
                data: arraysToXAndY([msToMin(response.data.nextTripStart), response.data.trips]),
                backgroundColor: '#dfbf2c',
                label: "Next trip start",
                showLine: false,
                // fill: false,
                // yAxisId: "icons"
            }]);
            labels = response.data.trips;
            labelValue= "Aggregate Run Times For All Trips";

        }
        barGraph.data = {
            datasets: datasets,
            labels: labels
        }

        barGraph.options.scales.xAxes[0].ticks.max = calculateMaxMins(highestPoints);
        barGraph.options.scales.xAxes[1].ticks.max = calculateMaxMins(highestPoints);
    $("#heading-canvas").html(labelValue);
        barGraph.update();
    }



    function percentageFormatter(data){

        var convertedPercentile = {
            early:[],
            onTime:[],
            late:[]
        };

        for(var i=0; i < data.early.length; i++){

            var totalSum  = data.early[i]+data.onTime[i]+data.late[i];

            var individualShare = ((100)/totalSum);
            if(!isFinite(individualShare)){
                individualShare = 0
            }
            convertedPercentile.early.push(individualShare*data.early[i]);
            convertedPercentile.onTime.push(individualShare*data.onTime[i]);
            convertedPercentile.late.push(individualShare*data.late[i]);

        }

        return convertedPercentile;
    }


    function generateAllRouteChart(nonPercentileResponse){

        var barGraph = getDefaultChartOptions({
            xAxis:{
                ticks: {
                    max: 100,
                }
            },
            yAxis:{
                isStacked: true
            },
            showPercentage : true

        });
        var response = percentageFormatter(nonPercentileResponse.data);


        barGraph.data = {
            datasets: [
                {
                    data: getHighestData(response.early),
                    backgroundColor: '#4778de',
                    label: "Ahead Of Schedule",
                    yAxisId: "bars"
                },
                {
                    data: getHighestData(response.onTime),
                    backgroundColor: '#4bd56b',
                    label: "On Schedule",
                    yAxisId: "bars"
                },
                {
                    data: getHighestData(response.late),
                    backgroundColor: '#e33b3b',
                    label: "Behind Schedule",
                    yAxisId: "bars"
                },

            ],
            labels: nonPercentileResponse.data.routes
        }

        barGraph.update();
    }

