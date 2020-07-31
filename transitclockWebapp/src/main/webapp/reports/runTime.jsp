<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <%@include file="/template/includes.jsp" %>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Run Time Analysis</title>

    <!-- Load in Select2 files so can create fancy route selector -->
    <link href="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/css/select2.min.css" rel="stylesheet" />
    <script src="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/js/select2.min.js"></script>

    <link href="params/reportParams.css" rel="stylesheet"/>
    <style>
        label {
            text-align: left;
            width: auto;
        }

        #paramsSidebar {
            width: 20%;
            height: 100vh;
            margin-left: 10px;
            float:left;
            border-right: 1px solid black;
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
            Run Time Analysis
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

                    $( "#datepicker" ).datepick({
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
                <label for="datepicker">Date:</label>
                <input type="text" id="datepicker" name="datepicker"
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
        </div>

        <input type="button" id="submit" class="submit" value="Submit" style="margin-top: 10px; margin-bottom: 10px;">
    </div>

    <div id="mainPage" style="width: 79%; height: 100%; display: inline-block;">
        <div id="paramDetails" class="paramDetails" style="height: 3%; float: left; margin-left: 20px; width: 60%;">
            <p style='font-size: 0.8em;'></p>
        </div>
    </div>

<script>

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

    $("#submit").click(function() {
        $("#submit").attr("disabled", "disabled");

        request = {}

        if ($("#datepicker").val() == "Date range") {
            var today = new Date();
            var beginDate = endDate = today.getFullYear() + "-"
                + (today.getMonth() <= 10 ? "0" + (today.getMonth() + 1) : (today.getMonth() + 1))
                + "-" + (today.getDate() < 10 ? "0" + today.getDate() : today.getDate());
        } else {
            var dateRangeStrings = $("#datepicker").val().replace(/\s/g, "").split("-");
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
        request.startStop = $("#startStop").val();
        request.endStop = $("#endStop").val();

        $.ajax({
            url: apiUrlPrefix + "/report/speedmap/runTime",
            // Pass in query string parameters to page being requested
            data: request,
            // Needed so that parameters passed properly to page being requested
            traditional: true,
            dataType: "json",
            success: function (response) {
                // var compareLink = "<a id='compareLink' style='font-size: 0.8em; margin-bottom: 1em; color: blue; text-decoration: underline; cursor: pointer'>Compare</a>";

                // // if (response.numberOfTrips == 0) {
                // //     $("#avgRunTimeTop").html("<p style='font-size: 0.8em; margin-bottom: 0em;'>No average run time data.</p>" + compareLink);
                // // }
                // else {
                //     var runTimeMinutes = parseInt(response.averageRunTime / 60000).toString();
                //     var runTimeSeconds = parseInt(response.averageRunTime % 60000 / 1000).toString();
                //     if (runTimeSeconds.length == 1) {
                //         runTimeSeconds = "0" + runTimeSeconds;
                //     }
                //     $("#avgRunTimeTop").html("<p style='font-size: 0.8em; margin-bottom: 0em;'>Average Trip Run Time: " + runTimeMinutes + ":" + runTimeSeconds + "</p>" + compareLink);
                // }

                $("#submit").removeAttr("disabled");
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

                alert("Success");
            },
            error: function () {
                $("#submit").removeAttr("disabled");
                alert("Error processing average trip run time.");
            }
        })
    })

</script>