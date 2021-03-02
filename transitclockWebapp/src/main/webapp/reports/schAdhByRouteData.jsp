<%-- Provides schedule adherence data in JSON format. Provides for
     each route the route name, number arrivals/departures that
     are early, number late, number on time, and number total. 
     Request parameters are:
       a - agency ID
       r - route ID or route short name. Can specify multiple routes. 
           Not specifying route provides data for all routes.
       dateRange - in format "xx/xx/xx to yy/yy/yy"
       beginDate - date to begin query. For if dateRange not used.
       numDays - number of days can do query. Limited to 31 days. For if dateRange not used.
       beginTime - for optionally specifying time of day for query for each day
       endTime - for optionally specifying time of day for query for each day
       allowableEarlyMinutes - how early vehicle can be and still be OK.  Decimal format OK. 
       allowableLateMinutes - how early vehicle can be and still be OK. Decimal format OK.
--%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="org.transitclock.reports.GenericJsonQuery" %>
<%@ page import="org.transitclock.reports.SqlUtils" %>
<%@ page import="org.transitclock.db.webstructs.WebAgency" %>
<%
try {

String agencyId = request.getParameter("a");
WebAgency agency = WebAgency.getCachedWebAgency(agencyId);
String dbtype = agency.getDbType();
boolean isMysql = "mysql".equals(dbtype);

String epochCommandPre = "";
if (isMysql) {
	epochCommandPre = "UNIX_TIMESTAMP";
}

String allowableEarlyStr = request.getParameter("allowableEarly");
if (allowableEarlyStr == null || allowableEarlyStr.isEmpty())
	allowableEarlyStr = "1.0";
String allowableEarlyMinutesStr = "'" + SqlUtils.convertMinutesToSecs(allowableEarlyStr) + " seconds'";

String allowableLateStr = request.getParameter("allowableLate");
if (allowableLateStr == null || allowableLateStr.isEmpty())
	allowableLateStr = "4.0";
String allowableLateMinutesStr = "'" + SqlUtils.convertMinutesToSecs(allowableLateStr) + " seconds'";

String sql =
	"SELECT " 
	+ "     COUNT(CASE WHEN " + epochCommandPre + "(scheduledTime-time) > " + allowableEarlyMinutesStr + " THEN 1 ELSE null END) as early, \n"
	+ "     COUNT(CASE WHEN " + epochCommandPre + "(scheduledTime-time) <= " + allowableEarlyMinutesStr + " AND " + epochCommandPre + "(time-scheduledTime) <= "
				+ allowableLateMinutesStr + " THEN 1 ELSE null END) AS ontime, \n" 
    + "     COUNT(CASE WHEN " + epochCommandPre + "(time-scheduledTime) > " + allowableLateMinutesStr + " THEN 1 ELSE null END) AS late, \n"
    + "     COUNT(*) AS total, \n"
    + "     r.name \n"
    + "FROM ArrivalsDepartures ad, Routes r \n"
    + "WHERE "
    // For joining in route table to get route name
    + "ad.configrev = r.configrev \n"
    + " AND ad.routeShortName = r.shortName \n"
    // Only need arrivals/departures that have a schedule time
    + " AND ad.scheduledTime IS NOT NULL \n"
    // Specifies which routes to provide data for
    + SqlUtils.routeClause(request, "ad") + "\n"
    + SqlUtils.timeRangeClause(request, "ad.time", 7) + "\n"
    // Grouping needed since want to output route name
    + " GROUP BY r.name, r.routeOrder ORDER BY r.routeOrder, r.name;";

// Just for debugging
System.out.println("\nFor schedule adherence by route query sql=\n" + sql);
    		
// Do the query and return result in JSON format    
String jsonString  = GenericJsonQuery.getJsonString(agencyId, sql);
response.setContentType("application/json");
response.setHeader("Access-Control-Allow-Origin", "*");
response.getWriter().write(jsonString);
} catch (Exception e) {
	response.setStatus(400);
	response.getWriter().write(e.getMessage());
	return;
}
%>