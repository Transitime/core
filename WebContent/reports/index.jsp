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
  <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script> 

  <!-- Load in JQuery UI javascript and css to set general look and feel -->
  <script src="/api/jquery-ui/jquery-ui.js"></script>
  <link rel="stylesheet" href="/api/jquery-ui/jquery-ui.css">

  <script>
  // Enable JQuery tooltips. In order to use html in tooltip need to 
  // specify content function. Turning off 'focusin' events is important
  // so that tooltip doesn't popup again if previous or next month
  // buttons are clicked in a datepicker.
  $(function() {
	  $( document ).tooltip({
          content: function () {
              return $(this).prop('title');
          }
      }).off('focusin');
	  });
  </script>
  
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
    
    /* Make links look better. No underlining, keep colors consistent,
       but bold when hovered over. */
    a:link {
    	text-decoration: none;
    	color: #0000FF;
    }
    a:visited {
    	color: #0000FF;
    }
    a:hover {
    	font-weight: bold;
    }
    
  .ui-tooltip {
	/* Change background color of tooltips a bit and use a reasonable font size */
  	background: #F7EEAB;
	font-size: small;
	padding: 4px;
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
  <li><a href="predAccuracyRangeParams.jsp?a=<%= agencyId %>"
    title="Shows percentage of predictions that were accurate
    to within the specified limits.">
      Prediction Accuracy Range Chart</a></li>
  <li><a href="predAccuracyIntervalsParams.jsp?a=<%= agencyId %>"
    title="Shows average prediction accuracy for each prediction length. Also 
hows upper and lower bounds. Allows one to see for a specified percentage 
what the prediction accuracy is for predictions that lie between the 
specified accuracy range.">
      Prediction Accuracy Interval Chart</a></li>
  <li><a href="predAccuracyScatterParams.jsp?a=<%= agencyId %>" 
    title="Shows each individual datapoint for prediction accuracy. Useful for 
finding specific issues with predictions.">
      Prediction Accuracy Scatter Plot</a></li>
</ul>
</div>
</body>
</html>