<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <%@include file="/template/includes.jsp" %>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <title>On Time Performance</title>

        <!-- Load in Select2 files so can create fancy route selector -->
        <link href="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/css/select2.min.css" rel="stylesheet" />
        <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;700&display=swap" rel="stylesheet">
        <script src="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/js/select2.min.js"></script>

        <link href="params/reportParams.css" rel="stylesheet"/>
        <style>
            .wrapper {
                background: #f1f1f1f1;
                font-family: 'Montserrat', sans-serif;
                height: 100vh;
            }

            .spinner {
                display: inline-block;
            }

            input {
                -webkit-appearance: none;
                border: 1px solid #c1c1c1c1;
                background-color: #fff;
                line-height: 1.5;
                box-shadow: 0px 1px 4px rgba(0, 0, 0, 0.33);
                color: #444;
                padding: 0px 6px;
                font-family: 'Montserrat', sans-serif;
            }

            input::placeholder {
                color: #44444469;
            }

            hr {
                height: 2px;
                background-color: darkgray;
                margin-right: 5px;
            }

            label {
                text-align: left;
                width: auto;
                font-family: 'Montserrat', sans-serif;
                cursor: pointer;
            }

            input::-webkit-input-placeholder, input::-moz-placeholder, input:-ms-input-placeholder,
            select::-webkit-input-placeholder, select::-moz-placeholder, select:-ms-input-placeholder {
                font-family: 'Montserrat', sans-serif;
            }

            #title {
                margin-top: 40px;
                margin-bottom: 2px;
                font-weight: normal;
                text-align: center;
                background: #4c6eaf;
                color: white;
                padding: 8px;
                font-size: 21px;
            }

            #route {
                visibility: hidden;
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

            .datepick-trigger {
                height: 29px;
                -webkit-appearance: none;
                vertical-align: top;
                box-shadow: 0px 1px 4px rgba(0, 0, 0, 0.33);
            }

            .datepick-nav, .datepick-ctrl {
                background-color: #4c6eae;
            }

            .paramsWrapper {
                width: 100%;
                margin: auto;
                display: inline-block;
                position: relative;
                z-index: 2;
                transition: width .75s ease-in-out;
            }
            .split .paramsWrapper {
                width: 33%;
                z-index: 2;
                margin-left: 5vw;
            }

            .split .paramsSidebar {
                margin-top: 12px;
                margin-left: 18px;
            }

            #paramsSidebar {
                max-width: 375px;
                height: 75vh;
                margin: 12px auto;
                display: flex;
                align-items: center;
                flex-flow: column;
                background-color: #fff;
                border-radius: 8px;
                box-shadow: 3px 3px 4px rgba(0,0,0,0.3);
                z-index: 2;
                border: #969696 solid 1px;
            }
            #paramsSidebar > * {
                display: flex;
            }
            #paramsFields {
                flex-flow: column;
            }

            .pair {
                display: flex;
                flex-flow: row;
                justify-content: space-between;
            }

            #endTimeLabel {
                margin-left: 12px;
            }

            #radioButtons {
                display: flex;
                overflow: hidden;
            }

            #radioButtons input {
                position: absolute !important;
                clip: rect(0, 0, 0, 0);
                height: 1px;
                width: 1px;
                border: 0;
                overflow: hidden;
            }

            #radioButtons label {
                background-color: #f1f1f1f1;
                font-size: 16px;
                padding: 8px 20px;
                margin-right: -1px;
                border: 1px solid rgba(0, 0, 0, 0.2);
                box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.3), 0 1px rgba(255, 255, 255, 0.1);
                transition: all 0.25s ease-in-out;
            }

            #radioButtons input:checked + label {
                background-color: #4ddb4c9c;
                box-shadow: none;
            }

            #serviceDayType {
                width: 100%;
                height: 36px;
                margin-top: 6px;
                box-shadow: 0px 1px 4px #69696969;
                font-family: 'Montserrat', sans-serif;
            }



            #reportResults {
                visibility: hidden;
                opacity: 0;
                margin-left: -8vw;
                margin-top: 20vh;
                height: 72vh;
                width: 66%;
                max-width: 900px;
                background-color: #fff;
                border-radius: 8px;
                box-shadow: 1px 2px 4px rgba(0,0,0,0.3);
                transition: visibility .25s .75s ease-in-out, opacity .25s .75s ease-in-out;
            }

            .split #reportResults {
                display: inline-block;
                position: relative;
                visibility: visible;
                opacity: 1;
                margin-top: 0vh;
            }


            #reportResults h2 {
                text-align: center;
                margin-top: 20px;
                padding: 10px;
                background-color: #019932;
                color: #fff;
            }
            .closeIcon {
                padding: 6px 12px;
                margin-right: 12px;
                float: right;
                cursor: pointer;
                border: 0px solid #afadad;
                border-radius: 8px;
                box-shadow: 0px 0px 6px #696969;
            }
            #submit {
                background-color: #029932;
                cursor: pointer;
                width: 210px;
                padding: 5px 70px;
                color: #fff;
                font-family: 'Montserrat', sans-serif;
                box-shadow: 0 4px rgba(127, 127, 127, 0.8);
            }

            #submit:hover {
                background-color: #02772c;
            }

            #submit:active {
                box-shadow: 0 1px rgba(127, 127, 127, 0.33);
                transform: translateY(3px);
                outline: none;
            }

            #chartTotal {
                text-align: center;
                width: 100%;
                float: right;
            }



        </style>
<%--        <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>--%>
        <link rel="stylesheet" type="text/css" href="../jquery.datepick.package-5.1.0/css/jquery.datepick.css">
        <script type="text/javascript" src="../jquery.datepick.package-5.1.0/js/jquery.plugin.js"></script>
        <script type="text/javascript" src="../jquery.datepick.package-5.1.0/js/jquery.datepick.js"></script>
    </head>
    <body>
        <%@include file="/template/header.jsp" %>
        <div class="wrapper">

            <div class="paramsWrapper">
            <div id="paramsSidebar">


                <div id="paramsFields">
                    <div id="title"><span>Service Delivery Report</span></div>
                    <%-- For passing agency param to the report --%>
                    <input type="hidden" name="a" value="<%= request.getParameter("a")%>">


                    <jsp:include page="params/routeAllOrSingle.jsp" /><span class="spinner">loading...</span>
    <%--                <jsp:include page="params/fromDateNumDaysTime.jsp" />--%>

                    <%-- For specifying a begin date, number of days, begin time, and end time --%>

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

                    <div class="param pair">
                        <label for="beginTime">Begin:</label>
                        <input id="beginTime" name="beginTime"
                               title="Optional begin time of day to limit query to. Useful if
                                want to see result just for rush hour, for example. Leave blank
                                if want data for entire day.
                                <br/><br/>Format: hh:mm, as in '07:00' for 7AM."
                               size="6"
                               value=""
                                placeholder="hh:mm"/>
                        <%--<span class="note">(hh:mm)</span>--%>
                        <label for="endTime" id="endTimeLabel">End:</label>
                        <input id="endTime" name="endTime"
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

                    <div class="param">
    <%--                    <i id="reportSettings" class="fa fa-caret-right"></i>Report Settings--%>
    <%--                    <button id="reportSettings" onclick="$('#radioButtons').toggle();"></button>--%>
                        Report Settings

                        <div id="radioButtons">
                            <input type="radio" name="stopType" id="timePointsOnly"><label for="timePointsOnly">Timepoints Only</label>
                            <input type="radio" name="stopType" checked="checked" id="allStops"><label for="allStops">All stops</label>
                        </div>
                    </div>

                    <hr>
                    OTP Definition
                    <br>

                    <div class="param" style="display: inline-block">
                        <label for="early">Min Early:</label>
                        <input type="number" id="early" name="early" min="0" max="1440" step="0.5" value="1.5">
                    </div>
                    <div class="param" style="display: inline-block">
                        <label for="late">Min Late:</label>
                        <input type="number" id="late" name="late" min="0" max="1440" step="0.5" value="2.5">
                    </div>

                    <%--<hr>--%>

                    <div class="param">
                        <label for="serviceDayType">Service Day Type</label>
                        <select id="serviceDayType" name="serviceDayType">
                            <option value="">All</option>
                            <option value="weekday">Weekday</option>
                            <option value="saturday">Saturday</option>
                            <option value="sunday">Sunday</option>
                        </select>
                    </div>

                    <hr>
                </div>

                <input type="button" id="submit" value="Submit" style="margin-top: 36px; margin-bottom: 10px;">

            </div>

            <script src="https://cdn.jsdelivr.net/npm/chart.js@2.8.0"></script>
            <script src="https://cdn.jsdelivr.net/gh/emn178/chartjs-plugin-labels/src/chartjs-plugin-labels.js"></script>
        </div>
        <div id="reportResults">
            <h2 style="text-align: center; margin-top: 20px;">On Time Performance By Route</h2>
            <div class="closeIcon">&times;</div>
            <canvas id="chartCanvas" style="margin-top: 10px;"></canvas>
            <div id="chartTotal"></div>

        </div>
    </div>
    </body>
</html>

    <script>
        $("#route").attr("style", "width: 200px");

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



        $(document).on('click', ".closeIcon", function(){closeSplit();});

        $("#submit").click(function() {
            $("#submit").attr("disabled","disabled");

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
        $("#submit").removeAttr("disabled");
        var values = response.data.datasets[0].data
        pieChart.data.datasets[0].data = values;
        pieChart.update();
        $("#chartTotal").html("Total count: " + values.reduce(function(total, num) {return total + num}));
        showSplit();
    }
</script>