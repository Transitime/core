<%@ page language="java" contentType="application/json; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="org.transitime.reports.GenericJsonQuery" %>
<%


String agencyId = request.getParameter("a");
String vehicleId = request.getParameter("v");

String sql =
"select * from vehicleevents where vehicleid='"+vehicleId+"' order by time desc";

String jsonString = GenericJsonQuery.getJsonString(agencyId, sql);
response.setHeader("Access-Control-Allow-Origin", "*");
response.getWriter().write(jsonString);
%>