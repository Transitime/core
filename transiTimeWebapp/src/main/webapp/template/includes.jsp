<%-- This file contains includes that can be included with every file --%>

<%-- Load in JQuery --%>
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>

<%-- Load in JQuery UI javascript and css to set general look and feel, such as for tooltips --%>
<script src="/api/jquery-ui/jquery-ui.js"></script>
<link rel="stylesheet" href="/api/jquery-ui/jquery-ui.css">
  
<%-- Load in Transitime css and javascript libraries. Do this after jquery files
     loaded so can override those parameters as necessary. --%>
<link rel="stylesheet" href="/api/css/general.css">
<script src="/api/javascript/transitime.js"></script>

<script>
//This needs to match the API key in the database
var apiKey = "5ec0de94";
//Note: the agency is not included because sometimes it this
//script is called due to a form getting posted and then
//the form parameters are not available.
var apiUrlPrefix = "/api/v1/key/" + apiKey + "/agency/<%= request.getParameter("a") %>";
</script>