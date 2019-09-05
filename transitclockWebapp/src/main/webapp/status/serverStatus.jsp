<%@page import="org.transitclock.db.webstructs.WebAgency"%>
<%@page import="java.rmi.RemoteException"%>
<%@page import="org.transitclock.ipc.interfaces.ServerStatusInterface"%>
<%@page import="org.transitclock.ipc.clients.ServerStatusInterfaceFactory"%>
<%@page import="org.transitclock.monitoring.*"%>
<%@page import="java.util.List"%>

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
  
  <style>
  	h3, .content {
  		margin-left: 20%;
  		margin-right: 20%;
  	}
  </style>
  
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Server Status</title>
</head>
<body>
<%@include file="/template/header.jsp" %>
<div id="title">Server Status for <%=
WebAgency.getCachedWebAgency(agencyId).getAgencyName() %></div>

<%
ServerStatusInterface serverStatusInterface = 
org.transitclock.ipc.clients.ServerStatusInterfaceFactory.get(agencyId);
try {
  List<MonitorResult> monitorResults = serverStatusInterface.get().getMonitorResults();
  for (MonitorResult monitorResult : monitorResults) {
    if (monitorResult.getMessage() != null) {
	%>
	<h3><%= monitorResult.getType() %></h3>
	<div class="content"><%= monitorResult.getMessage() %></div>
	<%
    }
  }
} catch (RemoteException e) {
	%><%= e.getMessage() %><%
}
%>
</body>
</html>