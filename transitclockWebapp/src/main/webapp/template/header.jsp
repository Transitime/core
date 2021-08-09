


<%-- Loading nav header --%>

<link rel="stylesheet" href="<%= request.getContextPath() %>/css/nav-header.css">
<nav class="navbar navbar-expand-lg navbar-light bg-light transit-clock-theme">
    <div class="container-fluid">
        <a class="navbar-brand" href="/web">The Transit Clock </a>
        <!--
        <div id="header"><a href="/web">The Transit Clock - new nav</a></div>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button> -->
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
            </ul>
        </div>
    </div>
</nav>