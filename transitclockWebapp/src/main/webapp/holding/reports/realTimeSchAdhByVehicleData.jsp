<%@ page language="java" contentType="application/json; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="org.transitclock.reports.SchAdhJsonQuery" %>
<%

// Get the params
String agencyId = request.getParameter("a");
String vehicleId = request.getParameter("v");

// Query db and get JSON string
String jsonString = SchAdhJsonQuery.getJson(agencyId, vehicleId);
response.setHeader("Access-Control-Allow-Origin", "*");
response.getWriter().write(jsonString);
%>