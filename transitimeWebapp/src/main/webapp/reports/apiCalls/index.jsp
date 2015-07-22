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
<ul class="choicesList">
  <li><a href="blocksApiParams.jsp?a=<%= agencyId %>"
    title="Details for block assignment specified by block ID">
      Block</a></li>
</ul>
</div>
</body>
</html>