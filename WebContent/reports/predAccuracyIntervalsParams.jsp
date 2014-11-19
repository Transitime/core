<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Specify Parameters</title>

  <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
  <script src="//code.jquery.com/ui/1.11.2/jquery-ui.js"></script>
  
   <link rel="stylesheet" href="//code.jquery.com/ui/1.11.2/themes/smoothness/jquery-ui.css">
  
  <!-- Load in Select2 files so can create fancy selectors -->
  <link href="/api/select2/select2.css" rel="stylesheet"/>
  <script src="/api/select2/select2.min.js"></script>
  
  <style>
  #mainDiv {margin-left: auto; margin-right: auto; width: 600px; }
  body {font-family: sans-serif; font-size: large;}
  #title {font-weight: bold; font-size: x-large; 
          margin-top: 40px; margin-bottom: 20px;
          margin-left: auto; margin-right: auto; width: 70%; text-align: center;}
  input, select {font-size: large;}
  label {width: 200px; float: left; text-align: right; margin-top: 4px; margin-right: 10px;}
  .param {margin-top: 10px;}
  #route {width:300px;}
  #submit {margin-top: 40px; margin-left: 200px;}
  .note {font-size: small;}
  </style>
</head>
<body>

   <div id="title">
   Select Parameters for Prediction Accuracy Intervals Chart
   </div>
   
<div id="mainDiv">
<form action="predAccuracyIntervalsChart.jsp" method="POST">
   <%-- For passing agency param to the report --%>
   <input type="hidden" name="a" value="<%= request.getParameter("a")%>">
   
   <jsp:include page="params/fromToDateTime.jsp" />
   
   <jsp:include page="params/route.jsp" />

<%--
   <jsp:include page="params/boolean.jsp">
    <jsp:param name="label" value="Test Boolean"/>
    <jsp:param name="name" value="testBoolean"/>
    <jsp:param name="default" value="false"/>
   </jsp:include>
 --%>
 
   <div class="param">
     <label for="source">Prediction Source:</label> 
     <select id="source" name="source">
       <option value="">All</option>
       <option value="Transitime">Transitime</option>
       <option value="Other">Other</option>
     </select>
   </div>
 
   <div class="param">
     <label for="intervalsType">Intervals Type:</label> 
     <select id="intervalsType" name="intervalsType">
       <option value="FRACTION">Fraction only</option>
       <option value="STD_DEV">Standard Deviation only</option>
       <option value="BOTH">Both Fraction and Standard Deviation</option>
     </select>
   </div>
 
   <jsp:include page="params/text.jsp">
    <jsp:param name="label" value="Interval Fraction 1"/>
    <jsp:param name="name" value="intervalFraction1"/>
    <jsp:param name="default" value="0.68"/>
   </jsp:include>

   <jsp:include page="params/text.jsp">
    <jsp:param name="label" value="Interval Fraction 2"/>
    <jsp:param name="name" value="intervalFraction2"/>
    <jsp:param name="default" value="0.80"/>
   </jsp:include>

    <input id="submit" type="submit" value="Run Report" />
  </form>
</div>

</body>
</html>