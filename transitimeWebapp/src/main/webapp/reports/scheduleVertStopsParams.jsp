<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%@include file="/template/includes.jsp" %>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Specify Parameters</title>
  
  <!-- Load in Select2 files so can create fancy route selector -->
  <link href="../select2/select2.css" rel="stylesheet"/>
  <script src="../select2/select2.min.js"></script>
    
  <style>
  label {width: 200px; float: left; text-align: right; margin-top: 4px; margin-right: 10px;}
  .param {margin-top: 10px;}
  #route {width:380px;}
  #submit {margin-top: 40px; margin-left: 250px;}
  .note {font-size: small;}
  
  </style>
</head>
<body>

<%@include file="/template/header.jsp" %>

<div id="title">
   Select Parameters for Schedule Report
</div>
   
<div id="mainDiv">
<form action="scheduleVertStopsReport.jsp" method="POST">
   <%-- For passing agency param to the report --%>
   <input type="hidden" name="a" value="<%= request.getParameter("a")%>">
   
   <jsp:include page="params/routeSingle.jsp" />
    
    <input id="submit" type="submit" value="Run Report" />
  </form>
</div>

</body>
</html>