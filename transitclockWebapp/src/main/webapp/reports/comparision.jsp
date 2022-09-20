<%@ page import="org.transitclock.utils.web.WebUtils" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page import="org.transitclock.web.WebConfigParams"%>
<%
    String agencyId = request.getParameter("a");
    if (agencyId == null || agencyId.isEmpty()) {
        response.getWriter().write("You must specify agency in query string (e.g. ?a=mbta)");
        return;
    }
%>
<html>
<head>
    <%@include file="/template/includes.jsp" %>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Run Time Analysis</title>

    <link rel="stylesheet" href="//unpkg.com/leaflet@0.7.3/dist/leaflet.css" />
    <script src="//unpkg.com/leaflet@0.7.3/dist/leaflet.js"></script>
    <script src="<%= request.getContextPath() %>/javascript/jquery-dateFormat.min.js"></script>

    <script src="<%= request.getContextPath() %>/maps/javascript/leafletRotatedMarker.js"></script>
    <script src="<%= request.getContextPath() %>/maps/javascript/mapUiOptions.js"></script>

    <!-- Load in Select2 files so can create fancy selectors -->
    <link href="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/css/select2.min.css" rel="stylesheet" />
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;700&display=swap" rel="stylesheet">
    <script src="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/js/select2.min.js"></script>

    <link rel="stylesheet" href="<%= request.getContextPath() %>/maps/css/mapUi.css" />


    <%--        <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>--%>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/page-panels.css">

    <script src="https://cdn.jsdelivr.net/npm/chart.js@2.8.0"></script>
    <script src="https://cdn.jsdelivr.net/gh/emn178/chartjs-plugin-labels/src/chartjs-plugin-labels.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/chart.js@2.9.4"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/chartjs-chart-box-and-violin-plot/2.4.0/Chart.BoxPlot.js"></script>

</head>
<script>
    var request = {<%= WebUtils.getAjaxDataString(request) %>},
        contextPath = "<%= request.getContextPath() %>";
</script>
<body>
<%@include file="/template/header.jsp" %>
<div class="panel split overflow-x-hidden">
    <div class="left-panel">
        <h4 class="page-title">
            Run Time Comparison
        </h4>
        <form class="row no-margin" novalidate>

            <input type="hidden" name="a" value="<%= request.getParameter("a")%>">


            <input type="hidden" name="date-range-picker" value="true" class="isDateRangePicker">

            <jsp:include page="params/routeAllOrSingleNew.jsp" />

            <div class="row">
                <label class="col-sm-12 col-form-label">Direction</label>
                <div class="col-sm-12">
                    <select id="direction" name="direction" disabled="true" class="form-select">

                    </select>
                </div>

            </div>

            <div class="row">
                <label class="col-sm-12 col-form-label">Trip Pattern</label>
                <div class="col-sm-12">
                    <select id="tripPattern" name="tripPattern" disabled="true" class="form-select">

                    </select>
                </div>

            </div>
            <div class="comparision-1 comparision row">
                <div class="row comparision-label-container">
                    <label class="col-sm-12 col-form-label ">Select Comparison Period 1</label>
                </div>

                <jsp:include page="params/fromDateNumDaysTimeSidePanel.jsp" />

                <div class="row">
                    <label class="col-sm-12 col-form-label">Service Day Type</label>
                    <div class="col-sm-12">
                        <select id="serviceDayType" name="serviceDayType" class="form-select">
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
            <div class="comparision-2 comparision row">
                <div class="row comparision-label-container">
                    <label class="col-sm-12 col-form-label">Select Comparison Period 2</label>
                </div>
                <jsp:include page="params/fromDateNumDaysTimeSidePanel.jsp" />

                <div class="row">
                    <label class="col-sm-12 col-form-label">Service Day Type</label>
                    <div class="col-sm-12">
                        <select id="serviceDayType" name="serviceDayType" class="form-select">
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

        </form>
        <div class="list-group">
            <button class="list-group-item list-group-item-action"  id="submit">Submit</button>
        </div>
    </div>
    <div class="right-panel position-relative">

        <div class="list-group toggle-chart ">
            <button class="list-group-item list-group-item-action">Average Trip Run Times</button>
        </div>
        <div class="image-container full-width-image d-flex justify-content-center grey-bg">
            <div class="position-relative d-flex justify-content-centerd-flex justify-content-center align-items-center img-path">
                <img src="<%= request.getContextPath() %>/reports/images/ontime.png" id="on-time-performance"  class="img-fluid grey-img"/>
                <h1 class=" position-absolute view-form-btn">Submit Form to View Data</h1>
            </div>
        </div>
        <div class="row toggle-chart">
                <div class="visualization-container no-margin-all">

                    <div id="runTimeVisualization" class="comparision-runTimeVisualization">

                    </div>
                </div>
        </div>


    </div>
</div>
</div>


<script type="text/javascript" src="javascript/run-time-helper.js"> </script>
<script>

    var stops = {};
    $(".toggle-chart").addClass("d-none");
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

        $(".toggle-chart").removeClass("d-none");
        $(".image-container").addClass("d-none");

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
                    if(eachResponse === "1"){
                        sumResponse["labels"].push(moment(request1.beginDate).format("MMM DD, YYYY") +" to "+ moment(request1.endDate).format("MMM DD, YYYY")
                        );
                    }
                    if(eachResponse === "2"){
                        sumResponse["labels"].push(moment(request2.beginDate).format("MMM DD, YYYY") +" to "+ moment(request2.endDate).format("MMM DD, YYYY")
                        );
                    }



                });

                visualizeData(sumResponse)
            }
        };

        serviceCall(request1, "1",  callBack);
        serviceCall(request2, "2", callBack);



    });

    function visualizeData(response){
        var defaultHeight = (response.avgRunTime.length ) * (window.innerHeight- 200)/2;
        var defaultWidth = window.innerWidth;

        $("#runTimeVisualization").html(' <canvas id="visualizationCanvas" class="custom-canvas"  height="'+defaultHeight+'" width="'+defaultWidth+'"></canvas>');
        generateComparisonChart(response);
    }


    function generateComparisonChart(response){
        var barGraph = getDefaultChartOptions({
            //  type: 'bar',
            yAxis:{
                isStacked: true
            }

        });
        var barDataSets = getFixedVariableDwellDataSet(response.fixed,response.variable,response.dwell)
        barGraph.data = {
            datasets: barDataSets,
            labels: response.labels
        }
        barGraph.options.scales.yAxes[0].ticks.max = calculateMaxMins(highestPoints);
        debugger;

        barGraph.options.scales.xAxes.splice(1,1)
        barGraph.options.scales.xAxes[0].position="bottom";

        barGraph.options.scales.xAxes[0].scaleLabel.labelString = "Minutes"
        barGraph.update();
    }

    function getParams(flag) {

        var datepicker, serviceTypeSelector, endTime, beginTime;
        if (flag) {
            datepicker =  ".comparision-1 #beginDate";
            beginTime = ".comparision-1 #beginTime";
            endTime = ".comparision-1 #endTime";
            serviceTypeSelector = ".comparision-1 #serviceDayType";
        } else {
            datepicker =  ".comparision-2 #beginDate";
            beginTime = ".comparision-2 #beginTime";
            endTime = ".comparision-2 #endTime";
            serviceTypeSelector = ".comparision-2 #serviceDayType";
        }

        if ($(datepicker).val() == "Date range") {
            var today = new Date();
            var beginDate = endDate = today.getFullYear() + "-"
                + (today.getMonth() <= 10 ? "0" + (today.getMonth() + 1) : (today.getMonth() + 1))
                + "-" + (today.getDate() < 10 ? "0" + today.getDate() : today.getDate());
        } else {
            var dateRangeStrings = $(datepicker).val().replace(/\s/g, "").split("-");
            var beginYear = "20" + dateRangeStrings[0];
            var endYear = "20" + dateRangeStrings[3];
            var beginDate = [beginYear, dateRangeStrings[1], dateRangeStrings[2]].join("-");
            var endDate = [endYear, dateRangeStrings[4], dateRangeStrings[5]].join("-");
        }

        var beginTime = $(beginTime).val() == "" ? "00:00:00" : $(beginTime).val() + ":00";
        var endTime = $(endTime).val() == "" ? "23:59:59" : $(endTime).val() + ":00";

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
        params.serviceType = $(serviceTypeSelector).val();
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

    // datePickerIntialization();
</script>
</body>
</html>