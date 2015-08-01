<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%@include file="/template/includes.jsp" %>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Specify Parameters</title>

  <!-- Load in Select2 files so can create fancy route selector -->
  <link href="../../select2/select2.css" rel="stylesheet"/>
  <script src="../../select2/select2.min.js"></script>
  
  <link href="../params/reportParams.css" rel="stylesheet"/>

  <script>
    function execute() {
      var selectedRouteId = $("#route").val();
      var format = $('input:radio[name=format]:checked').val();
  	  var url = apiUrlPrefix + "/command/gtfs-rt/tripUpdates?format=" + format;

   	  // Actually do the API call
   	  location.href = url;
    }
  </script>
  
</head>
<body>

<%@include file="/template/header.jsp" %>

<div id="title">
   Select Parameters for GTFS-Realtime Trip Updates API
</div>
   
<div id="mainDiv">   
   <div id="radioButtonsDiv">
     <input type="radio" name="format" value="binary" checked>Binary
     <input type="radio" name="format" value="human">Human Readable
   </div>
   
   <%-- Create submit button --%> 
   <jsp:include page="../params/submitApiCall.jsp" /> 
   
</div>

</body>
</html>