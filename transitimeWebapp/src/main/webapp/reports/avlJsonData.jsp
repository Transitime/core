<%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="org.transitime.reports.AvlJsonQuery" %>
<%
// Get the params
String agencyId = request.getParameter("a");
String vehicleId = request.getParameter("v");
String beginDate = request.getParameter("beginDate");
String numDays = request.getParameter("numDays");
String beginTime = request.getParameter("beginTime");
String endTime = request.getParameter("endTime");

// Query db and get JSON string
String jsonString = AvlJsonQuery.getJson(agencyId, vehicleId, beginDate, numDays, beginTime, endTime);

// Respond with the JSON string
response.getWriter().write(jsonString);
%>