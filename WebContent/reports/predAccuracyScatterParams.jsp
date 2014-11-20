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
  #mainDiv {margin-left: auto; margin-right: auto; width: 600px;}
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
   Select Parameters for Prediction Accuracy Scatter Chart
   </div>
   
<div id="mainDiv">
<form action="predAccuracyScatterChart.jsp" method="POST">
   <%-- For passing agency param to the report --%>
   <input type="hidden" name="a" value="<%= request.getParameter("a")%>">
   
   <jsp:include page="params/fromToDateTime.jsp" />
   
   <jsp:include page="params/route.jsp" />

   <jsp:include page="params/boolean.jsp">
    <jsp:param name="label" value="Provide tooltip info"/>
    <jsp:param name="name" value="tooltips"/>
    <jsp:param name="default" value="false"/>
   </jsp:include>
 
   <div class="param">
     <label for="source">Prediction Source:</label> 
     <select id="source" name="source">
       <option value="">All</option>
       <option value="Transitime">Transitime</option>
       <option value="Other">Other</option>
     </select>
   </div>
 
    <input id="submit" type="submit" value="Run Report" />
  </form>
</div>

</body>
</html>