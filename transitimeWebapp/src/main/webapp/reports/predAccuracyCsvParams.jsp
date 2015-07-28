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
  
  <link href="params/reportParams.css" rel="stylesheet"/>
</head>
<body>

<%@include file="/template/header.jsp" %>

<div id="title">
   Select Parameters for Prediction Accuracy CSV Download
</div>
   
<div id="mainDiv">
<form action="predAccuracyCsv.jsp" method="POST">
   <%-- For passing agency param to the report --%>
   <input type="hidden" name="a" value="<%= request.getParameter("a")%>">
   
   <jsp:include page="params/fromToDateTime.jsp" />
   
   <jsp:include page="params/route.jsp" />
    
   <jsp:include page="params/submitReport.jsp" />
  </form>
</div>

</body>
</html>