<%@ page language="java" contentType="application/json; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="org.transitime.reports.GenericJsonQuery" %>
<%
String sql =
	"SELECT vehicleId, max(time) AS maxTime "
	+ "FROM avlreports WHERE time > now() + '-24 hours' "
	+ "GROUP BY vehicleId;";
String agencyId = request.getParameter("a");
String jsonString = GenericJsonQuery.getJsonString(agencyId, sql);
response.setHeader("Access-Control-Allow-Origin", "*");
response.getWriter().write(jsonString);
%>