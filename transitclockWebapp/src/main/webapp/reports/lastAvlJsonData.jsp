<%@ page language="java" contentType="application/json; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="org.transitclock.reports.GenericJsonQuery" %>
<%@ page import="org.transitclock.db.webstructs.WebAgency" %>
<%@ page import="org.transitclock.utils.Time" %>
<%@ page import="java.text.ParseException" %>
<%
String agencyId = request.getParameter("a");
WebAgency agency = WebAgency.getCachedWebAgency(agencyId);
String dbtype = agency.getDbType();
String sql = null;
if(dbtype.equals("mysql")){
	sql = 
	"SELECT a.vehicleId, maxTime, lat, lon "
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
	+ "ON v.vehicleId=a.vehicleId";;
}
if(dbtype.equals("postgresql"))
{
	sql =
        "select a.vehicleId as \"vehicleId\", a.maxTime as \"maxTime\", lat, lon from ( "
        + "SELECT vehicleId, max(time) AS maxTime "
        + "FROM avlreports WHERE time > now() + '-24 hours' "
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
}
	
	
String jsonString = GenericJsonQuery.getJsonString(agencyId, sql);
response.setHeader("Access-Control-Allow-Origin", "*");
response.getWriter().write(jsonString);
%>