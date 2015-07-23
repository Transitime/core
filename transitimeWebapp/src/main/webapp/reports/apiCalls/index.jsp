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
<title>API Calls</title>
</head>
<body>
<%@include file="/template/header.jsp" %>
<div id="mainDiv">
<div id="title">API Calls for <%= WebAgency.getCachedWebAgency(agencyId).getAgencyName() %></div>
This is a only a partial list of the API calls 
<ul class="choicesList">
  <li><a href="routeApiParams.jsp?a=<%= agencyId %>"
    title="Data for a route">
      Route</a></li>
  <li><a href="vehiclesApiParams.jsp?a=<%= agencyId %>"
    title="Data for vehicles, including GPS info, for a route">
      Vehicles</a></li>
  <li><a href="vehiclesDetailsApiParams.jsp?a=<%= agencyId %>"
    title="Detailed data for vehicles, including GPS info, for a route">
      Vehicles Details</a></li>
  <li><a href="blocksApiParams.jsp?a=<%= agencyId %>"
    title="Data for a block assignment">
      Block</a></li>
</ul>
</div>
</body>
</html>