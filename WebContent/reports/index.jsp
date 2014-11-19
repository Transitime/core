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

  <style>
    body {
    	font-family: sans-serif; 
    	font-size: large;
    }
    
    #mainDiv {
    	margin-left: auto; 
    	margin-right: auto; 
    	width: 500px; 
    }
    
    #title {
    	margin-top:50px;
    	font-weight: bold; 
    	font-size: x-large;
    	text-align: center;
    }
    
    ul {
    	width: 300px; margin-left: auto; margin-right: auto;
    }
    
    a:link {
    	text-decoration: none;
    }
}
  </style>
  
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Reports</title>
</head>
<body>
<div id="mainDiv">
<div id="title">Transitime Reports</div>
<ul>
  <li><a href="predAccuracyScatterParams.jsp?a=<%= agencyId %>">Prediction Accuracy Scatter Plot</a></li>
  <li><a href="predAccuracyIntervalsParams.jsp?a=<%= agencyId %>">Prediction Accuracy Interval Plot</a></li>
</ul>
</div>
</body>
</html>