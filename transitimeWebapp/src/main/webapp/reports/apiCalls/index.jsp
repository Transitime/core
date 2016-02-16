<%@page import="org.transitime.db.webstructs.WebAgency"%>

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
</head>
<body>
<%@include file="/template/header.jsp" %>
<div id="mainDiv">
<div id="title">API Calls for <%= WebAgency.getCachedWebAgency(agencyId).getAgencyName() %></div>
<div id="subtitle" style="margin-bottom: 20px;">Note: This is a only a partial list of the API calls</div>
<div id="subtitle">Agency Specific API calls</div>
<ul class="choicesList">
  <li><a href="routeApiParams.jsp?a=<%= agencyId %>"
    title="Summary data for all routes, listed in order. Useful for creating a UI selector for routes.">
      Routes</a></li>
  <li><a href="routeDetailsApiParams.jsp?a=<%= agencyId %>"
    title="Detailed data for selected routes. Includes stop and path information needed to show route on map.">
      Route Details</a></li>
      
  <li><a href="vehiclesApiParams.jsp?a=<%= agencyId %>"
    title="Data for vehicles, including GPS info, for a route. Useful for showing location of vehicles on map.">
      Vehicles</a></li>
  <li><a href="vehiclesDetailsApiParams.jsp?a=<%= agencyId %>"
    title="Detailed data for vehicles, including GPS info, for a route. Contains additional data such as schedule adherence and assignment information.">
      Vehicles Details</a></li>
  <li><a href="vehicleConfigsApiParams.jsp?a=<%= agencyId %>"
    title="Configuration data for vehicles. A way of getting list of vehicles configured for agency.">
      Vehicle Configurations</a></li>
      
  <li><a href="predsByRouteStopApiParams.jsp?a=<%= agencyId %>"
    title="Predictions for specified route and stop.">
      Predictions by Route/Stop</a></li>
  <li><a href="predsByLocApiParams.jsp?a=<%= agencyId %>"
    title="Predictions for stops near specified latitude, longitude for the agency.">
      Predictions by Location</a></li>
  <li><a href="tripApiParams.jsp?a=<%= agencyId %>"
    title="Data for a single trip. Includes trip pattern and schedule info.">
      Trip</a></li>
  <li><a href="tripWithTravelTimesApiParams.jsp?a=<%= agencyId %>"
    title="Data for a single trip. Includes trip pattern and schedule info as well as historic travel times used for generating predictions.">
      Trip With Travel Times</a></li>
  <li><a href="blocksTerseApiParams.jsp?a=<%= agencyId %>"
    title="Data for a block assignment. Shows each trip that makes up the block in a terse format, without trip pattern or schedule info.">
      Block</a></li>
  <li><a href="blocksApiParams.jsp?a=<%= agencyId %>"
    title="Data for a block assignment. Shows each trip that makes up the block in a verbose format, including trip pattern and schedule info.">
      Block Details</a></li>

  <li><a href="serviceIdsApiParams.jsp?a=<%= agencyId %>"
    title="Data for all service IDs configured for agency.">
      Service IDs</a></li>
  <li><a href="serviceIdsCurrentApiParams.jsp?a=<%= agencyId %>"
    title="Data for service IDs that are currently active for agency.">
      Service IDs Current</a></li>

  <li><a href="calendarsApiParams.jsp?a=<%= agencyId %>"
    title="Data for all calendars configured for agency.">
      Calendars</a></li>
  <li><a href="calendarsCurrentApiParams.jsp?a=<%= agencyId %>"
    title="Data for calendars that are currently active for agency.">
      Calendars Current</a></li>

  <li><a href="gtfsRealtimeTripUpdatesApiParams.jsp?a=<%= agencyId %>"
    title="GTFS-realtime Trip Updates includes prediction data for entire agency">
      GTFS-realtime Trip Updates</a></li>
  <li><a href="gtfsRealtimeVehiclePositionsApiParams.jsp?a=<%= agencyId %>"
    title="GTFS-realtime Vehicle Positions for entire agency">
      GTFS-realtime Vehicle Positions</a></li>

  <li><a href="siriVehicleMonitoringApiParams.jsp?a=<%= agencyId %>"
    title="SIRI Vehicle Monitoring for specified route or entire agency">
      SIRI Vehicle Monitoring</a></li>
  <li><a href="siriStopMonitoringApiParams.jsp?a=<%= agencyId %>"
    title="SIRI Stop Monitoring for specified route and stop">
      SIRI Stop Monitoring</a></li>

  <li><a href="horizStopsScheduleApiParams.jsp?a=<%= agencyId %>"
    title="Schedule for route. For displaying schedule with stops listed in horizontal direction">
      Schedule for Route, stops horizontal</a></li>
  <li><a href="vertStopsScheduleApiParams.jsp?a=<%= agencyId %>"
    title="Schedule for route. For displaying schedule with stops listed in vertical direction">
      Schedule for Route, stops vertical</a></li>
</ul>

<div id="subtitle">Not Agency Specific</div>
<ul class="choicesList">
  <li><a href="agenciesApiParams.jsp"
    title="List of all agencies available through the API">
      Agencies</a></li>
  <li><a href="predsByLocForAllAgenciesApiParams.jsp?a=<%= agencyId %>"
    title="Predictions for stops near specified latitude, longitude. Will return predictions for all agencies that have nearby stops.">
      Predictions by Location</a></li>
</ul>
</div>
</body>
</html>