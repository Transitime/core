<%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="org.transitclock.reports.StopsForRouteDirectionJsonQuery"%>

<%
// Get the params
String agencyId = request.getParameter("a");
String routeId = request.getParameter("r").replaceAll("&amp;", "&");
String headsign = request.getParameter("headsign").replaceAll("&amp;", "&");



// Query db and get JSON string
String jsonString = StopsForRouteDirectionJsonQuery.getStopsJson(agencyId, routeId, headsign);

// Respond with the JSON string
response.setContentType("application/json");
response.setHeader("Access-Control-Allow-Origin", "*");
response.getWriter().write(jsonString);
%>
