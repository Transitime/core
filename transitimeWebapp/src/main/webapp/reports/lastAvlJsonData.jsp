<%@ page language="java" contentType="application/json; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="org.transitime.reports.GenericJsonQuery" %>
<%

String sql = 
	"SELECT a.vehicleId, maxTime, lat, lon "
	+ "FROM " 
	+ "(SELECT vehicleId, max(time) AS maxTime " 
	+ "FROM AvlReports WHERE time > date_sub(now(), interval 1 day) "
	+ "GROUP BY vehicleId) a "
	+ "JOIN AvlReports b ON a.vehicleId=b.vehicleId AND a.maxTime = b.time";
	
	
String agencyId = request.getParameter("a");
String jsonString = GenericJsonQuery.getJsonString(agencyId, sql);
response.setHeader("Access-Control-Allow-Origin", "*");
response.getWriter().write(jsonString);
%>