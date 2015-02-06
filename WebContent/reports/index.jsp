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

  <link rel="stylesheet" href="/api/css/general.css">
  
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
    
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Reports</title>
</head>
<body>
<%@include file="/template/header.jsp" %>
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
  <li><a href="predAccuracyCsvParams.jsp?a=<%= agencyId %>"
    title="For downloading prediction accuracy data in CSV format.">
      Prediction Accuracy CSV Download</a></li>
</ul>
</div>
</body>
</html>