<%-- This file contains includes that can be included with every file --%>

<%-- Load in JQuery --%>
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>

<%-- Load in JQuery UI javascript and css to set general look and feel, such as for tooltips --%>
<script src="<%= request.getContextPath() %>/jquery-ui/jquery-ui.js"></script>
<link rel="stylesheet" href="<%= request.getContextPath() %>/jquery-ui/jquery-ui.css">
  
<%-- Load in Transitime css and javascript libraries. Do this after jquery files
     loaded so can override those parameters as necessary. --%>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/general.css">
<script src="<%= request.getContextPath() %>/javascript/transitime.js"></script>

<script>
// This needs to match the API key in the database
var apiKey = "<%=System.getProperty("transitime.apikey")%>"

// For accessing the api for an agency command
var apiUrlPrefixAllAgencies = "/api/v1/key/" + apiKey;
var apiUrlPrefix = apiUrlPrefixAllAgencies + "/agency/<%= request.getParameter("a") %>";
</script>