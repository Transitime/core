<%@page import="org.transitclock.db.webstructs.WebAgency"%>

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
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/page-details.css">
</head>
<body class="page-details">
<%@include file="/template/header.jsp" %>
<div id="mainDiv">

    <div class="btn-title">Real-time Maps for <%= WebAgency.getCachedWebAgency(agencyId).getAgencyName() %></div>
    <section>

        <div class="list-group">

            <a  class="list-group-item list-group-item-action"
                href="../maps/map.jsp?verbose=true&a=<%= agencyId %>"
                title="Real-time map for selected route">
                Map for Selected Route
            </a>

            <a  class="list-group-item list-group-item-action"
                href="../maps/map.jsp?verbose=true&a=<%= agencyId %>&showUnassignedVehicles=true"
                title="Real-time map for selected route but also shows vehicles not currently assigned to a route">
                Map Including Unassigned Vehicles
            </a>

            <a  class="list-group-item list-group-item-action"
                href="../maps/schAdhMap.jsp?a=<%= agencyId %>"
                title="Shows current real-time schedule adherence of vehicles in map">
                Schedule Adherence Map
            </a>

        </div>

    </section>

</div>
</body>
</html>