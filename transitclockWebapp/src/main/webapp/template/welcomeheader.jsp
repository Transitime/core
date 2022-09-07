<%@ page import="org.transitclock.utils.web.WebUtils" %>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/nav-header.css">
<nav class="navbar navbar-expand-lg navbar-light bg-light transit-clock-theme">
    <div class="container-fluid">
        <a class="navbar-brand" href="/web"><% out.print(WebUtils.getHeaderBrandingText()); %> Transit Clock</a>
    </div>
</nav>