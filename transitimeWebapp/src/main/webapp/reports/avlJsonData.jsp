<%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="org.transitime.reports.AvlJsonQuery" %>
<%
// Get the params
String agencyId = request.getParameter("a");
String vehicleId = request.getParameter("v");
String beginDate = request.getParameter("beginDate");
String endDate = request.getParameter("endDate");
String beginTime = request.getParameter("beginTime");
String endTime = request.getParameter("endTime");

String startDateTime = beginDate + (beginTime != null && !beginTime.isEmpty() ? 
		" " + beginTime : "");
String endDateTime = endDate + (endTime != null && !endTime.isEmpty() ? 
		" " + endTime : " 23:59:59");

// Query db and get JSON string
String jsonString = AvlJsonQuery.getJson(agencyId, vehicleId, startDateTime, endDateTime);

// Respond with the JSON string
response.getWriter().write(jsonString);
%>