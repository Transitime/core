<%@ page import="org.transitclock.utils.web.WebUtils" %>
<%@ page import="org.transitclock.web.WebConfigParams" %>

<link rel="stylesheet" href="<%= request.getContextPath() %>/css/nav-header.css">
<nav class="navbar navbar-expand-lg navbar-light bg-light transit-clock-theme">
    <div class="container-fluid">
        <a class="navbar-brand" href="/web"><% out.print(WebUtils.getHeaderBrandingText()); %> Transit Clock</a>
        <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="navbar-nav">
                <li class="nav-item" <% if(!WebConfigParams.isShowLogout()) out.print("style=\"display:none;\"");%>>
                    <a class="nav-link" href="/api/v1/logout">Logout</a>
                </li>
            </ul>
        </div>
    </div>
</nav>