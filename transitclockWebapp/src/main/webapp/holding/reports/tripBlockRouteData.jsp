<%@ page language="java" contentType="application/json; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="org.transitclock.reports.TripBlockJsonQuery" %>
<%

// Get the params
String agencyId = request.getParameter("a");
String routeId = request.getParameter("r");
String date = request.getParameter("date");
String beginTime = request.getParameter("beginTime");
String endTime = request.getParameter("endTime");

// Query db and get JSON string
String jsonString = TripBlockJsonQuery.getJson(agencyId, date, beginTime, endTime, routeId);
response.setHeader("Access-Control-Allow-Origin", "*");
response.getWriter().write(jsonString);
%>