<%@page import="org.transitclock.db.webstructs.WebAgency"%>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
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
    <title>API Calls</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/page-details.css">
</head>
<body class="page-details">
<%@include file="/template/header.jsp" %>
<div id="mainDiv">
<div class="btn-title">API Calls for <%= WebAgency.getCachedWebAgency(agencyId).getAgencyName() %></div>
<div class="btn-sub-title">Note: This is a only a partial list of the API calls</div>

<section>

    <h5 >Agency Specific API calls</h5>
    <div class="list-group">

        <a  class="list-group-item list-group-item-action"
            href="routeApiParams.jsp?a=<%= agencyId %>"
            title="Summary data for all routes, listed in order. Useful for creating a UI selector for routes.">
            Routes
        </a>

        <a  class="list-group-item list-group-item-action"
            href="routeDetailsApiParams.jsp?a=<%= agencyId %>"
            title="Detailed data for selected routes. Includes stop and path information needed to show route on map.">
            Route Details
        </a>

        <a  class="list-group-item list-group-item-action"
            href="vehiclesApiParams.jsp?a=<%= agencyId %>"
            title="Data for vehicles, including GPS info, for a route. Useful for showing location of vehicles on map.">
            Vehicles
        </a>

        <a  class="list-group-item list-group-item-action"
            href="vehiclesDetailsApiParams.jsp?a=<%= agencyId %>"
            title="Detailed data for vehicles, including GPS info, for a route. Contains additional data such as schedule adherence and assignment information.">
            Vehicles Details
        </a>

        <a  class="list-group-item list-group-item-action"
            href="vehicleConfigsApiParams.jsp?a=<%= agencyId %>"
            title="Configuration data for vehicles. A way of getting list of vehicles configured for agency.">
            Vehicle Configurations
        </a>


        <a  class="list-group-item list-group-item-action"
            href="predsByRouteStopApiParams.jsp?a=<%= agencyId %>"
            title="Predictions for specified route and stop.">
            Predictions by Route/Stop
        </a>


        <a  class="list-group-item list-group-item-action"
            href="predsByLocApiParams.jsp?a=<%= agencyId %>"
            title="Predictions for stops near specified latitude, longitude for the agency.">
            Predictions by Location
        </a>


        <a  class="list-group-item list-group-item-action"
            href="tripApiParams.jsp?a=<%= agencyId %>"
            title="Data for a single trip. Includes trip pattern and schedule info.">
            Trip
        </a>


        <a  class="list-group-item list-group-item-action"
            href="tripWithTravelTimesApiParams.jsp?a=<%= agencyId %>"
            title="Data for a single trip. Includes trip pattern and schedule info as well as historic travel times used for generating predictions.">
            Trip With Travel Times
        </a>


        <a  class="list-group-item list-group-item-action"
            href="blocksTerseApiParams.jsp?a=<%= agencyId %>"
            title="Data for a block assignment. Shows each trip that makes up the block in a terse format, without trip pattern or schedule info.">
            Block
        </a>


        <a  class="list-group-item list-group-item-action"
            href="blocksApiParams.jsp?a=<%= agencyId %>"
            title="Data for a block assignment. Shows each trip that makes up the block in a verbose format, including trip pattern and schedule info.">
            Block Details
        </a>


        <a  class="list-group-item list-group-item-action"
            href="serviceIdsApiParams.jsp?a=<%= agencyId %>"
            title="Data for all service IDs configured for agency.">
            Service IDs
        </a>


        <a  class="list-group-item list-group-item-action"
            href="serviceIdsCurrentApiParams.jsp?a=<%= agencyId %>"
            title="Data for service IDs that are currently active for agency.">
            Service IDs Current
        </a>


        <a  class="list-group-item list-group-item-action"
            href="calendarsApiParams.jsp?a=<%= agencyId %>"
            title="Data for all calendars configured for agency.">
            Calendars
        </a>


        <a  class="list-group-item list-group-item-action"
            href="calendarsCurrentApiParams.jsp?a=<%= agencyId %>"
            title="Data for calendars that are currently active for agency.">
            Calendars Current
        </a>

        <a  class="list-group-item list-group-item-action"
            href="gtfsRealtimeTripUpdatesApiParams.jsp?a=<%= agencyId %>"
            title="GTFS-realtime Trip Updates includes prediction data for entire agency">
            GTFS-realtime Trip Updates
        </a>

        <a  class="list-group-item list-group-item-action"
            href="gtfsRealtimeVehiclePositionsApiParams.jsp?a=<%= agencyId %>"
            title="GTFS-realtime Vehicle Positions for entire agency">
            GTFS-realtime Vehicle Positions
        </a>

        <a  class="list-group-item list-group-item-action"
            href="siriVehicleMonitoringApiParams.jsp?a=<%= agencyId %>"
            title="SIRI Vehicle Monitoring for specified route or entire agency">
            SIRI Vehicle Monitoring
        </a>

        <a  class="list-group-item list-group-item-action"
            href="siriStopMonitoringApiParams.jsp?a=<%= agencyId %>"
            title="SIRI Stop Monitoring for specified route and stop">
            SIRI Stop Monitoring
        </a>

        <a  class="list-group-item list-group-item-action"
            href="horizStopsScheduleApiParams.jsp?a=<%= agencyId %>"
            title="Schedule for route. For displaying schedule with stops listed in horizontal direction">
            Schedule for Route, stops horizontal
        </a>

        <a  class="list-group-item list-group-item-action"
            href="vertStopsScheduleApiParams.jsp?a=<%= agencyId %>"
            title="Schedule for route. For displaying schedule with stops listed in vertical direction">
            Schedule for Route, stops vertical
        </a>

        <a  class="list-group-item list-group-item-action"
            href="resetVehicleApiParams.jsp?a=<%= agencyId %>""
        title="Reset specific vehicle">
        Reset vehicle
        </a>

    </div>

</section>



<section>
    <h5 >Not Agency Specific</h5>
    <div class="list-group">
    <a  class="list-group-item list-group-item-action"
        href="agenciesApiParams.jsp"
        title="List of all agencies available through the API">
        Agencies
    </a>

    <a  class="list-group-item list-group-item-action"
        href="predsByLocForAllAgenciesApiParams.jsp?a=<%= agencyId %>"
        title="Predictions for stops near specified latitude, longitude. Will return predictions for all agencies that have nearby stops.">
        Predictions by Location
    </a>
    </div>
</section>
</div>
</body>
</html>
