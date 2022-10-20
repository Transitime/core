<%@ page import="org.transitclock.utils.web.WebUtils" %>
<%@ page import="org.transitclock.web.WebConfigParams" %>


<%-- Loading nav header --%>

<link rel="stylesheet" href="<%= request.getContextPath() %>/css/nav-header.css">
<nav class="navbar navbar-expand-lg navbar-light bg-light transit-clock-theme">
    <div class="container-fluid">
        <a class="navbar-brand" href="/web"><% out.print(WebUtils.getHeaderBrandingText()); %> Transit Clock</a>
        <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="navbar-nav">
                <li class="nav-item">
                    <a class="nav-link" href="<%= request.getContextPath() %>/maps/index.jsp?a=<%= request.getParameter("a")%>" title="Real-time maps">Maps</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="<%= request.getContextPath() %>/reports/index.jsp?a=<%= request.getParameter("a")%>" title="Reports on historic information">Reports</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="<%= request.getContextPath() %>/reports/apiCalls/index.jsp?a=<%= request.getParameter("a")%>" title="API calls">API</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link"  href="<%= request.getContextPath() %>/status/index.jsp?a=<%=request.getParameter("a")%>" title="Pages showing current status of system">Status</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link"  href="<%= request.getContextPath() %>/synoptic/index.jsp?a=<%= request.getParameter("a") %>" title="Real-time synoptic">Synoptic</a>
                </li>
                <li class="nav-item" <% if(!WebConfigParams.isShowLogout()) out.print("style=\"display:none;\"");%>>
                    <a class="nav-link" href="/api/v1/logout">Logout</a>
                </li>
            </ul>
        </div>
    </div>
</nav>