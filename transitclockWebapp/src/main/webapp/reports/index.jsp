<%@page import="org.transitclock.db.webstructs.WebAgency" %>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
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
    <title>Historical Reports</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/page-details.css">
</head>
<body class="page-details">
<%@include file="/template/header.jsp" %>
<div id="mainDiv">

    <div class="btn-title">Historical Reports for <%= WebAgency.getCachedWebAgency(agencyId).getAgencyName() %></div>
    <section>
        <h5> Prediction accuracy </h5>
        <p>(only for agencies where prediction accuracy stored to database)</p>

        <div class="list-group">

            <a  class="list-group-item list-group-item-action"
                href="predAccuracyBucketParams.jsp?a=<%= agencyId %>"
                title="Shows percentage of predictions that were accurate to within the specified limits.
                    Categorized into 5 buckets.">
                Prediction Accuracy Bucket Chart
            </a>

            <a  class="list-group-item list-group-item-action"
                href="predAccuracyRangeParams.jsp?a=<%= agencyId %>"
                title="Shows percentage of predictions that were accurate to within the specified limits.">
                Prediction Accuracy Range Chart
            </a>

            <a  class="list-group-item list-group-item-action"
                href="predAccuracyIntervalsParams.jsp?a=<%= agencyId %>"
                title="Shows average prediction accuracy for each prediction length. Also
                    hows upper and lower bounds. Allows one to see for a specified percentage
                    what the prediction accuracy is for predictions that lie between the
                    specified accuracy range.">
                Prediction Accuracy Interval Chart
            </a>

            <a  class="list-group-item list-group-item-action"
                href="predAccuracyScatterParams.jsp?a=<%= agencyId %>"
                title="Shows each individual datapoint for prediction accuracy. Useful for
                    finding specific issues with predictions.">
                Prediction Accuracy Scatter Plot
            </a>

            <a  class="list-group-item list-group-item-action"
                href="predAccuracyCsvParams.jsp?a=<%= agencyId %>"
                title="For downloading prediction accuracy data in CSV format.">
                Prediction Accuracy CSV Download
            </a>

            <!--             <a  class="list-group-item list-group-item-action"
                href="routePerformanceTable.jsp?a=<%= agencyId %>"
                title="Shows route performance, where performance is defined as the
                number of ontime predictions over the total number of predicitons for a
                given route.">
                Route Performance Table
            </a> -->
        </div>
    </section>

    <section>
        <h5> AVL Reports </h5>

        <div class="list-group">

            <a  class="list-group-item list-group-item-action"
                href="avlMap.jsp?a=<%= agencyId %>"
                title="Displays historic AVL data for a vehicle in a map.">
                Vehicle Location Playback
            </a>

            <!--             <a  class="list-group-item list-group-item-action"
                href="avlMapParams.jsp?a=<%= agencyId %>"
                title="Displays historic AVL data for a vehicle in a map.">
                AVL Data in Map (parameters page)
            </a> -->

            <a  class="list-group-item list-group-item-action"
                href="lastAvlReport.jsp?a=<%= agencyId %>"
                title="Displays the last time each vehicle reported its GPS position over the last 24 hours.">
                Last GPS Report by Vehicle
            </a>

        </div>
    </section>

    <!--     <div id="subtitle">Event Reports</div>
        <ul class="choicesList">
          <li><a href="vehicleEventParams.jsp?a=<%= agencyId %>"
            title="Check that all Events for vehicle.">
              Event for vehicle</a></li>
        </ul>
    </div> -->

    <section>
        <h5> Schedule Adherence Reports </h5>

        <div class="list-group">

            <a  class="list-group-item list-group-item-action"
                href="schAdhByRouteParams.jsp?a=<%= agencyId %>"
                title="Displays historic schedule adherence data by route in a bar chart.
                    Can compare schedule adherence for multiple routes.">
                Schedule Adherence by Route
            </a>

            <a  class="list-group-item list-group-item-action"
                href="schAdhByStopParams.jsp?a=<%= agencyId %>"
                title="Displays historic schedule adherence data for each stop for a
                    route in a bar chart. ">
                Schedule Adherence by Stop
            </a>

            <a  class="list-group-item list-group-item-action"
                href="schAdhByTimeParams.jsp?a=<%= agencyId %>"
                title="Displays historic schedule adherence data for a route grouped by
                    how early/late. The resulting bell curve shows the distribution of
                    early/late times. ">
                Schedule Adherence by how Early/Late
            </a>


            <a  class="list-group-item list-group-item-action"
                href="onTimePerformanceReport.jsp?a=<%=agencyId%>"
                title="Displays historic on time performance data in a chart for a given route.">
                On Time Performance
            </a>

        </div>
    </section>

    <section>
        <h5> Miscellaneous Reports </h5>

        <div class="list-group">

            <a  class="list-group-item list-group-item-action"
                href="scheduleHorizStopsParams.jsp?a=<%= agencyId %>"
                title="Displays in a table the schedule for a specified route.">
                Schedule for Route
            </a>

            <a  class="list-group-item list-group-item-action"
                href="scheduleVertStopsParams.jsp?a=<%= agencyId %>"
                title="Displays in a table the schedule for a specified route. Stops listed
                    vertically which is useful for when there are not that many trips per day.">
                Schedule for Route (vertical stops)
            </a>

            <!--             <a  class="list-group-item list-group-item-action"
                href="tripBlockRouteParams.jsp?a=<%= agencyId %>"
                title="Show all blocks assigned to a route for the selected day with trip times">
                Trip Blocks for Route
            </a>


            <a  class="list-group-item list-group-item-action"
                href="realTimeSchAdhByVehicleParams.jsp?a=<%= agencyId %>"
                title="Real time schedule adherence by vehicle">
                Real Time Schedule Adherence By Vehicle
            </a> -->

        </div>
    </section>

    <section>
        <h5> Speed Reports </h5>

        <div class="list-group">

            <a  class="list-group-item list-group-item-action"
                href="speedMap.jsp?a=<%=agencyId%>"
                title="Displays historic average speed data on a map for a given route and headsign.">
                Speed Map
            </a>

        </div>
    </section>

    <section>
        <h5> Run Time Reports </h5>

        <div class="list-group">

            <a  class="list-group-item list-group-item-action"
                href="runTime.jsp?a=<%=agencyId%>"
                title="Displays historic run time data for a given route and headsign.">
                Run Times
            </a>

            <a  class="list-group-item list-group-item-action"
                href="comparision.jsp?a=<%=agencyId%>"
                title="Compares historic run time data for a given route and headsign.">
                Run Time Comparison
            </a>

            <a  class="list-group-item list-group-item-action"
                href="prescriptiveRunTimes.jsp?a=<%=agencyId%>"
                title="Compares historic prescriptive time data for a given route and headsign.">
                Prescriptive Run Times
            </a>

        </div>
    </section>

    <section>
        <h5> Real Time Reports </h5>

        <div class="list-group">

            <a  class="list-group-item list-group-item-action"
                href="realTimeLiveMap.jsp?a=<%= agencyId %>"
                title="Displays real time vehicle and stop information on a map.">
                Live Map View
            </a>

            <a  class="list-group-item list-group-item-action"
                href="realTimeScheduleAdherence.jsp?a=<%= agencyId %>"
                title="Displays real time schedule adherence information on a map.">
                Schedule Adherence
            </a>

            <a  class="list-group-item list-group-item-action"
                href="realTimeDispatcher.jsp?a=<%= agencyId %>"
                title="Displays real time vehicle information in a table.">
                Dispatcher View
            </a>

        </div>
    </section>

    <section>
        <h5> Real Time Vehicle Monitoring </h5>

        <div class="list-group">

            <a  class="list-group-item list-group-item-action"
                href="../maps/map.jsp?verbose=true&a=<%= agencyId %>"
                title="Real-time map for selected route">
                Real Time Vehicle View
            </a>

        </div>
    </section>

    <section>
        <h5> Status Reports </h5>

        <div class="list-group">

            <a  class="list-group-item list-group-item-action"
                href="../status/activeBlocks.jsp?a=<%= agencyId %>"
                title="Shows how many block assignments are currently active and if they have assigned vehicles">
                Active Blocks</a>
            </a>

            <a  class="list-group-item list-group-item-action"
                href="../status/serverStatus.jsp?a=<%= agencyId %>"
                title="Shows how well system is running, including the AVL feed">
                Server Status
            </a>

        </div>
    </section>

</div>


</body>
</html>
