<%-- Provides schedule adherence data in JSON format. Provides for
     the specified route the number arrivals/departures that
     are early, number late, number on time, and number total for each
     direction for each stop. 
     Request parameters are:
       a - agency ID
       r - route ID or route short name. 
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

	String timeRequest = SqlUtils.timeRangeClause(request, "ad.time", 7);

	String sql =
	"SELECT " 
	+ "     COUNT(CASE WHEN " + epochCommandPre + "(scheduledTime) -" + epochCommandPre + "(time) > " + allowableEarlyMinutesStr + " THEN 1 ELSE null END) as early, \n"
	+ "     COUNT(CASE WHEN " + epochCommandPre + "(scheduledTime) -" + epochCommandPre + "(time) <= " + allowableEarlyMinutesStr + " AND " + epochCommandPre + "(time) -" + epochCommandPre + "(scheduledTime) <= "
			+ allowableLateMinutesStr + " THEN 1 ELSE null END) AS ontime, \n"
    + "     COUNT(CASE WHEN " + epochCommandPre + "(time) -" + epochCommandPre + "(scheduledTime) > " + allowableLateMinutesStr + " THEN 1 ELSE null END) AS late, \n"
    + "     COUNT(*) AS total, \n"
    + "     s.name AS stop_name, \n"
    + "     ad.directionid AS direction_id \n"
    + "FROM ArrivalsDepartures ad, Stops s \n"
    + "WHERE "
    // To get stop name
    + " ad.configRev = s.configRev \n"
    + " AND ad.stopId = s.id \n"
    // Only need arrivals/departures that have a schedule time
    + " AND ad.scheduledTime IS NOT NULL \n"
    // Specifies which routes to provide data for
    + SqlUtils.routeClause(request, "ad") + "\n"
    + timeRequest + "\n"
    // Grouping and ordering is a bit complicated since might also be looking
    // at old arrival/departure data that doen't have stoporder defined. Also,
    // when configuration changes happen then the stop order can change. 
    // Therefore want to group by directionId and stop name. Need to also 
    // group by stop order so that can output it, which can be useful for 
    // debugging, plus need to order by stop order. For the ORDER BY clause
    // need to order by direction id and stop order, but also the stop name
    // as a backup for if stoporder not defined for data and is therefore 
    // always the same and doesn't provide any ordering info.
    + " GROUP BY directionid, s.name, ad.stopOrder \n"
    + " ORDER BY directionid, ad.stopOrder, s.name";

// Just for debugging
System.out.println("\nFor schedule adherence by stop query sql=\n" + sql);
    		
// Do the query and return result in JSON format
String jsonString = GenericJsonQuery.getJsonString(agencyId, sql);
response.setContentType("application/json");
response.setHeader("Access-Control-Allow-Origin", "*");
response.getWriter().write(jsonString);
} catch (Exception e) {
	response.setStatus(400);
	response.getWriter().write(e.getMessage());
	return;
}%>