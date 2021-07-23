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
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" type="text/css"  href="../javascript/jquery-timepicker/jquery.timepicker.css" />
    <link rel="stylesheet" type="text/css" href="../jquery.datepick.package-5.1.0/css/jquery.datepick.css" />
    <link href="params/reportParams.css" rel="stylesheet"/>



</head>
<body class="run-time-screen">
<%@include file="/template/header.jsp" %>


<div class="wrapper">
    <div class="paramsWrapper">
        <div id="paramsSidebar">
            <div class="header-title">
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
                        <input type="text" id="datepicker1" name="datepicker1" class="date-picker-input"
                               title="The range of dates that you want to examine data for.
                                        <br><br> Begin date must be before the end date."
                               size="18"
                               value="Date range"/>
                    </div>

                    <div class="param">
                        <label for="beginTime1">Begin Time:</label>
                        <input id="beginTime1" name="beginTime1"  class="time-picker-input"
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
                        <input id="endTime1" name="endTime1"  class="time-picker-input"
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
                        <input type="text" id="datepicker2" name="datepicker2" class="date-picker-input"
                               title="The range of dates that you want to examine data for.
                                        <br><br> Begin date must be before the end date."
                               size="18"
                               value="Date range"/>
                    </div>

                    <div class="param">
                        <label for="beginTime2">Begin Time:</label>
                        <input id="beginTime2" name="beginTime2"   class="time-picker-input"
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
                        <input id="endTime2" name="endTime2"   class="time-picker-input"
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

<script src="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/js/select2.min.js"></script>
<script type="text/javascript" src="../jquery.datepick.package-5.1.0/js/jquery.plugin.js"></script>
<script type="text/javascript" src="../jquery.datepick.package-5.1.0/js/jquery.datepick.js"></script>
<script src="../javascript/jquery-timepicker/jquery.timepicker.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/chart.js@2.9.4"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/chartjs-chart-box-and-violin-plot/2.4.0/Chart.BoxPlot.js"></script>
<script type="text/javascript" src="javascript/run-time-helper.js"> </script>
</body>
</html>

<script>

    var stops = {};

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


    function generateComparisonChart(response){
        var barGraph = getDefaultChartOptions({
            yAxis:{
                isStacked: true
            }
        });
        var barDataSets = getFixedVariableDwellDataSet(response.fixed,response.variable,response.dwell)
        barGraph.data = {
            datasets: barDataSets,
            labels: response.labels
        }

        barGraph.options.scales.xAxes[0].ticks.max = calculateMaxMins(highestPoints);
        barGraph.update();
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