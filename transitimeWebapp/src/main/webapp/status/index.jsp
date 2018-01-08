<%@page import="org.transitime.db.webstructs.WebAgency"%>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%
String agencyId = request.getParameter("a");
if (agencyId == null || agencyId.isEmpty()) {
    response.getWriter().write("You must specify agency in query string (e.g. ?a=mbta)");
    return;
}
%>
<html>
<head>
<%@include file="/template/includes.jsp" %> 
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Status Pages</title>
</head>
<body>
<%@include file="/template/header.jsp" %>
<div id="mainDiv">
<div id="title">Status Reports for <%= WebAgency.getCachedWebAgency(agencyId).getAgencyName() %></div>
<ul class="choicesList">
  <li><a href="activeBlocks.jsp?a=<%= agencyId %>"
    title="Shows how many block assignments are currently active and if they have assigned vehicles">
      Active Blocks</a></li>
  <li><a href="../maps/schAdhMap.jsp?a=<%= agencyId %>"
    title="Shows current real-time schedule adherence of vehicles in map">
      Schedule Adherence Map</a></li>
  <li><a href="serverStatus.jsp?a=<%= agencyId %>"
    title="Shows how well system is running, including the AVL feed">
      Server Status</a></li>
  <li><a href="dbDiskSpace.jsp?a=<%= agencyId %>"
    title="Shows how much disk space is being used by the database. Currently only works for agencies where PostgreSQL database is used.">
      Database Disk Space Utilization</a></li>
</ul>
</div>
</body>
</html>
