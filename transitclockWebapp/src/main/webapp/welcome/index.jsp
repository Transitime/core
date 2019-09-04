<%@page import="org.transitclock.db.webstructs.WebAgency"%>
<%@page import="java.util.Collection"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <%@include file="/template/includes.jsp" %>
  
  <style>
  /* center the table */
  #agencyList {
    margin-left: auto;
    margin-right: auto;
  }
  
  table {
  	border-spacing: 0px;
  }
  /* adjust text in table */
  td {
  	padding-left: 16px;
  	padding-right: 16px;
  	padding-top: 4px;
  	padding-bottom: 4px;
  	text-align: left;
  }
  
  /* Alternate row colors to make table more readable */
  tr:nth-child(odd) {background: #F6F6F6}
  tr:nth-child(even) {background: #EBEBEB}

  /* for handling names that are too long */
  #agencyName {
  	width: 300px;
  	overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  	
  </style>
    
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Agencies</title>
</head>

<body>
<%@include file="/template/header.jsp" %>
<div id="mainDiv">
<div id="title">Agencies</div>
<table id="agencyList">
<%
// Output links for all the agencies
Collection<WebAgency> webAgencies = WebAgency.getCachedOrderedListOfWebAgencies();
for (WebAgency webAgency : webAgencies) {
	// Only output active agencies
	if (!webAgency.isActive())
		continue;
	%>
	<tr>
	  <td><div id=agencyName><%= webAgency.getAgencyName() %></div></td>
	  <td><a href="<%= request.getContextPath() %>/maps/index.jsp?a=<%= webAgency.getAgencyId() %>" title="Real-time maps">Maps</a></td>
	  <td><a href="<%= request.getContextPath() %>/reports/index.jsp?a=<%= webAgency.getAgencyId() %>" title="Reports on historic information">Reports</a></td>
	  <td><a href="<%= request.getContextPath() %>/reports/apiCalls/index.jsp?a=<%= webAgency.getAgencyId() %>" title="API calls">API</a></td>
	  <td><a href="<%= request.getContextPath() %>/status/index.jsp?a=<%= webAgency.getAgencyId() %>" title="Pages showing current status of system">Status</a></td>
	  <td><a href="<%= request.getContextPath() %>/synoptic/index.jsp?a=<%= webAgency.getAgencyId() %>" title="Real-time synoptic">Synoptic</a></td>
	  <td><a href="<%= request.getContextPath() %>/extensions/index.jsp?a=<%= webAgency.getAgencyId() %>" title="Page of links to extension to the system">Extensions</a></td>
	</tr>
	<%
}
%>
</table>

</div>

</body>
</html>