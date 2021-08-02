<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <%@include file="/template/includes.jsp" %>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>On Time Performance</title>

    <!-- Load in Select2 files so can create fancy route selector -->
    <link href="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/css/select2.min.css" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;700&display=swap" rel="stylesheet">
    <script src="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/js/select2.min.js"></script>

    <link rel="stylesheet" type="text/css" href="../jquery.datepick.package-5.1.0/css/jquery.datepick.css">
    <link rel="stylesheet" type="text/css" href="../javascript/jquery-timepicker/jquery.timepicker.css"></link>

    <script type="text/javascript" src="../jquery.datepick.package-5.1.0/js/jquery.plugin.js"></script>
    <script type="text/javascript" src="../jquery.datepick.package-5.1.0/js/jquery.datepick.js"></script>
    <script src="../javascript/jquery-timepicker/jquery.timepicker.min.js"></script>



    <link href="params/reportParams.css" rel="stylesheet"/>

    <script src="https://cdn.jsdelivr.net/npm/chart.js@2.8.0"></script>
    <script src="https://cdn.jsdelivr.net/gh/emn178/chartjs-plugin-labels/src/chartjs-plugin-labels.js"></script>
</head>
<body class="run-time-screen speed-map-page on-time-performance">
<%@include file="/template/header.jsp" %>
<div class="wrapper">

    <div class="paramsWrapper">
        <div id="paramsSidebar">


            <div id="paramsFields">
                <%--<div id="title" class="header-title"><span>Service Delivery Report</span></div>--%>
                <%-- For passing agency param to the report --%>
                <input type="hidden" name="a" value="<%= request.getParameter("a")%>">
                <span class="vert-offset">Route Options</span>
                <jsp:include page="params/routeAllOrSingle.jsp" />
                <%--                <jsp:include page="params/fromDateNumDaysTime.jsp" />--%>

                <%-- For specifying a begin date, number of days, begin time, and end time --%>

                <div class="param">
                    <label for="beginDate">Date:</label>
                    <input type="text" id="beginDate" name="beginDate" class="date-picker-input"
                           title="The range of dates that you want to examine data for.
                               <br><br> Begin date must be before the end date."
                           size="18"
                           value="Date range" />
                </div>

                <div class="param pair">
                    <label for="beginTime">Begin:</label>
                    <input id="beginTime" name="beginTime" class="time-picker-input"
                           title="Optional begin time of day to limit query to. Useful if
                                want to see result just for rush hour, for example. Leave blank
                                if want data for entire day.
                                <br/><br/>Format: hh:mm, as in '07:00' for 7AM."
                           size="6"
                           value=""
                           placeholder="hh:mm"/>
                    <%--<span class="note">(hh:mm)</span>--%>
                    <label for="endTime" id="endTimeLabel">End:</label>
                    <input id="endTime" name="endTime" class="time-picker-input"
                           title="Optional end time of day to limit query to. Useful if
                                want to see result just for rush hour, for example. Leave blank
                                if want data for entire day.
                                <br/><br/>Format: hh:mm, as in '09:00' for 9AM.
                                Use '23:59' for midnight."
                           size="6"
                           value=""
                           placeholder="hh:mm"/>
                    <%--<span class="note">(hh:mm)</span>--%>
                </div>

                <%--<div class="param">--%>
                <%----%>
                <%--</div>--%>

                <div class="param vertical">
                    <%--                    <i id="reportSettings" class="fa fa-caret-right"></i>Report Settings--%>
                    <%--                    <button id="reportSettings" onclick="$('#radioButtons').toggle();"></button>--%>
                    <span>Report Settings</span>

                    <div id="radioButtons">
                        <input type="radio" name="stopType" checked="checked" id="allStops"><label for="allStops">All stops</label>
                        <input type="radio" name="stopType" id="timePointsOnly"><label for="timePointsOnly" id="timePointsOnlyLabel">Timepoints Only</label>

                    </div>
                </div>

                <div class="param vertical">
                    <span>OTP Definition</span>

                    <div class="pair">
                        <label for="early">Min Early:</label>
                        <input type="number" id="early" name="early" min="0" max="1440" step="0.5" value="1">
                    </div>
                    <div class="pair">
                        <label for="late">Min Late:</label>
                        <input type="number" id="late" name="late" min="0" max="1440" step="0.5" value="5">
                    </div>


                </div>
                <%--<div class="param" style="display: inline-block">--%>

                <%--</div>--%>

                <%--<hr>--%>

                <div class="param vertical">
                    <label for="serviceDayType">Service Day Type</label>
                    <select id="serviceDayType" name="serviceDayType">
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

            <div class="submitDiv"><button id="submit" class="submit" >Submit</button></div>


        </div>

    </div>
    <div id="reportResults">
        <div id="overlay"></div>
        <div id="bars1">
            <span></span>
            <span></span>
            <span></span>
            <span></span>
            <span></span>
        </div>
        <h2>On Time Performance By Route</h2>
        <div class="closeIcon">&times;</div>
        <canvas id="chartCanvas" style="margin-top: 10px;"></canvas>
        <div id="chartTotal"></div>

    </div>
</div>
</body>
</html>

<script src="<%= request.getContextPath() %>/javascript/date-picker.js"></script>
<script>
    // $("#route").attr("style", "width: 200px");

    var canvas = $("#chartCanvas");
    var pieChart = new Chart(canvas, {
        type: 'pie',
        data: {
            datasets: [{
                data: [],
                backgroundColor: ['#fffe00', '#ff2000', '#0ca900']
            }],
            labels: ['Early', 'Late', 'On time']
        },
        options: {
            legend: {
                position: 'bottom'
            },
            plugins: {
                labels: {
                    render: function(args) {
                        return args.value + "\n(" + args.percentage + "%)";
                    },
                    fontSize: 24,
                    fontColor: '#000000',
                    position: 'border',
                    precision: 1
                }
            }
        }
    });

    function showSplit(){
        $(".wrapper").addClass("split");
    }

    function closeSplit(){
        $(".wrapper").removeClass("split");
    }

    function removeSpinner() {
        $(".spinner").hide();
    }



    $(document).on('click', ".closeIcon", function(){closeSplit();});

    $("#submit").click(function() {
        $("#submit").attr("disabled","disabled");
        $("#submit").html("Loading...");
        // $("body").addClass("loader");
        $("#overlay").show();
        $("#bars1").show();
        $("#reportResults").addClass("inactive-split");


        if ($("#beginDate").val() == "Date range") {
            var today = new Date();
            var beginDate = endDate = today.getFullYear() + "-"
                + (today.getMonth() <= 10 ? "0" + (today.getMonth() + 1) : (today.getMonth() + 1))
                + "-" + (today.getDate() < 10 ? "0" + today.getDate() : today.getDate());
        }
        else {
            var dateRangeStrings = $("#beginDate").val().replace(/\s/g, "").split("-");
            var beginYear = "20" + dateRangeStrings[0];
            var endYear = "20" + dateRangeStrings[3];
            var beginDate = [beginYear,  dateRangeStrings[1],  dateRangeStrings[2]].join("-");
            var endDate = [endYear, dateRangeStrings[4], dateRangeStrings[5]].join("-");
        }

        var beginTime = $("#beginTime").val() == "" ? "00:00:00" : $("#beginTime").val() + ":00";
        var endTime = $("#endTime").val() == "" ? "23:59:59" : $("#endTime").val() + ":00";

        request = {};
        request.beginDate = beginDate;
        request.beginTime = beginTime;
        request.endDate = endDate;
        request.endTime = endTime;
        request.r = $("#route").val();
        request.minEarlyMSec = $("#early").val() * 60000;
        request.minLateMSec = $("#late").val() * 60000;
        request.serviceType = $("#serviceDayType").val();
        request.timePointsOnly = $("#timePointsOnly")[0].checked;

        $.ajax({
            url: apiUrlPrefix + "/report/chartjs/onTimePerformanceByRoute",
            // Pass in query string parameters to page being requested
            data: request,
            // Needed so that parameters passed properly to page being requested
            traditional: true,
            dataType:"json",
            success: drawChart
        })
    })

    function drawChart(response) {
        $("#submit").removeAttr("disabled")
        $("#submit").html("Submit");
        // $("body").removeClass("loader");
        $("#overlay").hide();
        $("#bars1").hide();
        $("#reportResults").removeClass("inactive-split");
        var values = response.data.datasets[0].data
        pieChart.data.datasets[0].data = values;
        pieChart.update();
        $("#chartTotal").html("Total count: " + values.reduce(function(total, num) {return total + num}));

        showSplit();

    }
    datePickerIntialization();
</script>