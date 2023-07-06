<%-- This file contains includes that can be included with every file --%>

<%-- Load in JQuery --%>
<script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>

<%-- Load in JQuery UI javascript and css to set general look and feel, such as for tooltips --%>
<script src="<%= request.getContextPath() %>/jquery-ui/jquery-ui.js"></script>
<link rel="stylesheet" href="<%= request.getContextPath() %>/jquery-ui/jquery-ui.css">
  
<%-- Load in Transitime css and javascript libraries. Do this after jquery files
     loaded so can override those parameters as necessary. --%>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/general.css">
<script src="<%= request.getContextPath() %>/javascript/transitime.js"></script>
<script src="<%= request.getContextPath() %>/javascript/time.js"></script>

<!-- CSS only -->
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css" rel="stylesheet" />

<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.5.0/font/bootstrap-icons.css">

<!-- JavaScript Bundle with Popper -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.bundle.min.js" ></script>

<script>

// This needs to match the API key in the database
var apiKey = "<%=System.getProperty("transitclock.apikey")%>"

// For accessing the api for an agency command
var apiUrlKeyPrefix = "/api/v1/key/";
var apiUrlAgencyPrefix = "/agency/<%= request.getParameter("a") %>";
var apiUrlPrefix = apiUrlKeyPrefix + apiKey + apiUrlAgencyPrefix;
</script>
