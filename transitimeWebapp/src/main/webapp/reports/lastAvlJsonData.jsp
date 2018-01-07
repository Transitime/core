<%@ page language="java" contentType="application/json; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="org.transitime.reports.GenericJsonQuery" %>
<%

String sql = 
	"SELECT a.vehicleId, maxTime, lat, lon, IFNULL(assignmentId, '') AS assignmentId, IFNULL(v.routeShortName, '') as routeShortName "
	+ "FROM " 
	+ "(SELECT vehicleId, max(time) AS maxTime " 
	+ "FROM AvlReports WHERE time > date_sub(now(), interval 1 day) "
	+ "GROUP BY vehicleId) a "
	+ "JOIN AvlReports b ON a.vehicleId=b.vehicleId AND a.maxTime = b.time "
	+ "JOIN "
	+ "(SELECT i.vehicleId, j.routeShortName FROM"
	+ " (SELECT vehicleId, MAX(avlTime) AS avlTIme "
	+ "  FROM VehicleStates "
	+ "  GROUP BY vehicleId) i "
	+ " JOIN VehicleStates j ON j.avlTime=i.avlTime AND j.vehicleId=i.vehicleId"
	+ ") v "
	+ "ON v.vehicleId=a.vehicleId";
	
	
String agencyId = request.getParameter("a");
String jsonString = GenericJsonQuery.getJsonString(agencyId, sql);
response.setHeader("Access-Control-Allow-Origin", "*");
response.getWriter().write(jsonString);
%>