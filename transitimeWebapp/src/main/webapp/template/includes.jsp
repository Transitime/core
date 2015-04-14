<%-- This file contains includes that can be included with every file --%>

<%-- Load in JQuery --%>
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>

<%-- Load in JQuery UI javascript and css to set general look and feel, such as for tooltips --%>
<script src="../jquery-ui/jquery-ui.js"></script>
<link rel="stylesheet" href="../jquery-ui/jquery-ui.css">
  
<%-- Load in Transitime css and javascript libraries. Do this after jquery files
     loaded so can override those parameters as necessary. --%>
<link rel="stylesheet" href="../css/general.css">
<script src="../javascript/transitime.js"></script>

<script>
//This needs to match the API key in the database
var apiKey = "8a3273b0";
//Note: the agency is not included because sometimes it this
//script is called due to a form getting posted and then
//the form parameters are not available.
var apiUrlPrefix = "http://127.0.0.1:8080/api/v1/key/" + apiKey + "/agency/<%= request.getParameter("a") %>";
</script>