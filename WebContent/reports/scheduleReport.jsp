<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="org.transitime.utils.web.WebUtils" %>
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
    
  <script type="text/javascript" src="https://www.google.com/jsapi"></script>
  <script type="text/javascript">
      // Load in Google charts library
      google.load("visualization", "1", {packages:["table"]});
      google.setOnLoadCallback(getDataAndDrawChart);
      
      function dataReadCallback(jsonData) {
    	  alert("yes!");
      }
      
      function getData() {
    	  $.ajax({
    	      	// The page being requested
    		  	url: apiUrlPrefix + "/command/schedule",
    	      	// Pass in query string parameters to page being requested
    	      	data: {<%= WebUtils.getAjaxDataString(request) %>},
    	    	// Needed so that parameters passed properly to page being requested
    	    	traditional: true,
    	        dataType:"json",
				success: dataReadCallback
    	  });
      }
      
      function getDataAndDrawChart() {
    	getData();  
      }
      
  </script>

<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Schedule Report</title>
</head>
<body>
<%@include file="/template/header.jsp" %>
<div id="title">Schedule Report</div>

</body>
</html>