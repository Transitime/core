<%@page import="org.transitime.db.webstructs.WebAgency"%>
<%@page import="java.util.Collection"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script> 

  <!-- Load in JQuery UI javascript and css to set general look and feel -->
  <script src="/api/jquery-ui/jquery-ui.js"></script>
  <link rel="stylesheet" href="/api/jquery-ui/jquery-ui.css">

  <!-- Load in general transitime javascript library -->
  <script src="/api/javascript/transitime.js"></script>

  <link rel="stylesheet" href="/api/css/general.css">

  <style>
  /* center the table */
  #agencyList {
    margin-left: auto;
    margin-right: auto;
  }
  
  /* adjust text in table */
  th {
  	padding-left: 20px;
  	padding-right: 20px;
  	padding-top: 5px;
  	text-align: left;
  }
  </style>
    
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Agencies</title>
</head>

<body>
<%@include file="/template/header.jsp" %>
<div id="mainDiv">
<div id="title">Transitime Agencies</div>
<table id="agencyList">
<%
// Output links for all the agencies
Collection<WebAgency> webAgencies = WebAgency.getCachedWebAgencies();
for (WebAgency webAgency : webAgencies) {
	// Only output active agencies
	if (!webAgency.isActive())
		continue;
	%>
	<tr>
	  <th><%= webAgency.getAgencyName() %></th>
	  <th><a href="map.jsp?a=<%= webAgency.getAgencyId() %>" title="Real-time map">Map</a></th>
	  <th><a href="reports/index.jsp?a=<%= webAgency.getAgencyId() %>" title="Reports on historic information">Reports</a></th>
	  <th><a href="status/index.jsp?a=<%= webAgency.getAgencyId() %>" title="Pages showing current status of system">Status</a></th>
	</tr>
	<%
}
%>
</table>

</div>

</body>
</html>