<%@ page language="java" contentType="application/json; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="org.transitclock.reports.GenericJsonQuery" %>
<%@ page import="org.transitclock.reports.SqlUtils" %>
<%
try
{
String agencyId = request.getParameter("a");
String vehicleId = request.getParameter("v");

String sql = "select * from vehicleevents ve where ve.vehicleid=ve.vehicleid";
// If only want data for single vehicle then specify so in SQL
if (vehicleId != null && !vehicleId.isEmpty()&&!vehicleId.startsWith(" "))
{
	sql += " AND ve.vehicleId='" + vehicleId + "' ";
}
sql +=  SqlUtils.timeRangeClause(request, "ve.time", 5) + "\n";
sql += " order by ve.time desc";

String jsonString = GenericJsonQuery.getJsonString(agencyId, sql);
response.setHeader("Access-Control-Allow-Origin", "*");
response.getWriter().write(jsonString);
} catch (Exception e) {
	response.setStatus(400);
	response.getWriter().write(e.getMessage());
	return;
}
%>