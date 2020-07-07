<%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="org.transitclock.reports.AvlJsonQuery" %>
<%
// Get the params
String agencyId = request.getParameter("a");
String vehicleId = request.getParameter("v");
String routeId = request.getParameter("r");
String beginDate = request.getParameter("beginDate");
String beginTime = request.getParameter("beginTime");
String endTime = request.getParameter("endTime");
String includeHeadway = request.getParameter("includeHeadway");
String earlyMsec = request.getParameter("early");
String lateMsec = request.getParameter("late");



// Query db and get JSON string
String jsonString = AvlJsonQuery.getAvlJson(agencyId, vehicleId, beginDate, beginTime, endTime, routeId, includeHeadway,earlyMsec,lateMsec);

// Respond with the JSON string
response.setContentType("application/json");
response.setHeader("Access-Control-Allow-Origin", "*");
response.getWriter().write(jsonString);
%>
