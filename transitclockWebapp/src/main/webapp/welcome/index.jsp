<%@page import="org.transitclock.db.webstructs.WebAgency"%>
<%@page import="java.util.Collection"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%--  <%@include file="/template/includes.jsp" %>--%>

	<!-- CSS only -->
	<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css" rel="stylesheet" />
	<link rel="stylesheet" href="<%= request.getContextPath() %>/css/welcome.css">
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Agencies</title>
</head>

<body class="welcome">
<%@include file="/template/welcomeheader.jsp" %>
<div id="mainDiv">
<div class="btn-title">Dart</div>
<!-- <table id="agencyList"> -->
	<%
			// Output links for all the agencies
			Collection<WebAgency> webAgencies = WebAgency.getCachedOrderedListOfWebAgencies();
			for (WebAgency webAgency : webAgencies) {
				// Only output active agencies
				if (!webAgency.isActive())
					continue;
	%>
	<div class="d-grid gap-2 d-md-flex space-between">
		<a class="btn btn-primary btn-lg " role="button"  href="<%= request.getContextPath() %>/maps/index.jsp?a=<%= webAgency.getAgencyId() %>" title="Real-time maps">Maps</a>
		<a class="btn btn-primary btn-lg " role="button" href="<%= request.getContextPath() %>/reports/index.jsp?a=<%= webAgency.getAgencyId() %>" title="Reports on historic information">Reports</a>
		<a class="btn btn-primary btn-lg " role="button" href="<%= request.getContextPath() %>/reports/apiCalls/index.jsp?a=<%= webAgency.getAgencyId() %>" title="API calls">API</a>
		<a class="btn btn-primary btn-lg " role="button" href="<%= request.getContextPath() %>/status/index.jsp?a=<%= webAgency.getAgencyId() %>" title="Pages showing current status of system">Status</a>
		<a class="btn btn-primary btn-lg " role="button" href="<%= request.getContextPath() %>/synoptic/index.jsp?a=<%= webAgency.getAgencyId() %>" title="Real-time synoptic">Synoptic</a>

	</div>

	<!-- <tr>
	  <td><div id=agencyName><%= webAgency.getAgencyName() %></div></td>
	  <td><a class="btn btn-primary btn-lg " role="button"  href="<%= request.getContextPath() %>/maps/index.jsp?a=<%= webAgency.getAgencyId() %>" title="Real-time maps">Maps</a></td>
	  <td><a class="btn btn-primary btn-lg " role="button" href="<%= request.getContextPath() %>/reports/index.jsp?a=<%= webAgency.getAgencyId() %>" title="Reports on historic information">Reports</a></td>
	  <td><a class="btn btn-primary btn-lg " role="button" href="<%= request.getContextPath() %>/reports/apiCalls/index.jsp?a=<%= webAgency.getAgencyId() %>" title="API calls">API</a></td>
	  <td><a class="btn btn-primary btn-lg " role="button" href="<%= request.getContextPath() %>/status/index.jsp?a=<%= webAgency.getAgencyId() %>" title="Pages showing current status of system">Status</a></td>
	  <td><a class="btn btn-primary btn-lg " role="button" href="<%= request.getContextPath() %>/synoptic/index.jsp?a=<%= webAgency.getAgencyId() %>" title="Real-time synoptic">Synoptic</a></td>

	</tr> -->
	<%
}
%>
</table>
	<%--	  <td><a href="<%= request.getContextPath() %>/extensions/index.jsp?a=<%= webAgency.getAgencyId() %>" title="Page of links to extension to the system">Extensions</a></td>--%>
</div>

</body>
</html>