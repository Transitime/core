<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%@include file="/template/includes.jsp" %>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Specify Parameters</title>

  <!-- Load in Select2 files so can create fancy route selector -->
  <link href="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.1/css/select2.min.css" rel="stylesheet" />
  <script src="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.1/js/select2.min.js"></script>

  <link href="params/reportParams.css" rel="stylesheet"/>  
</head>
<body>
<%@include file="/template/header.jsp" %>
<div id="title">
   Select Parameters for Schedule Adherence by Stop Chart
</div>

<div id="mainDiv">
<form action="schAdhByStopChart.jsp" method="POST">
   <%-- For passing agency param to the report --%>
   <input type="hidden" name="a" value="<%= request.getParameter("a")%>">
   
   <jsp:include page="params/routeSingle.jsp" />
 
   <jsp:include page="params/fromToDateTime.jsp" />
     
   <div class="param">
    <label for="allowableEarly">Allowable Early:</label>
    <input id="allowableEarly" name="allowableEarly"
    	title="How early a vehicle can arrive compared to the prediction
    	and still be acceptable. Must be a negative number to indicate
    	early." 
    	size="1"
    	value="1.0" /> <span class="note">minutes</span>
  </div>
 
   <div class="param">
    <label for="allowableLate">Allowable Late:</label>
    <input id="allowableLate" name="allowableLate"
    	title="How late a vehicle can arrive compared to the prediction
    	and still be acceptable. Must be a positive number to indicate
    	late." 
    	size="1" 
    	value="4.0"/> <span class="note">minutes</span>
  </div>
    
    <jsp:include page="params/submitReport.jsp" />
  </form>
</div>

</body>
</html>