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
      var latitude = $("#latitude").val();
      var longitude = $("#longitude").val();
      var maxDistance = $("#maxDistance").val();
      var numPreds = $("#numPreds").val();
      var format = $('input:radio[name=format]:checked').val();
      
  	  var url = apiUrlPrefixAllAgencies + "/command/predictionsByLoc?lat=" + latitude
  			  + "&lon=" + longitude
  			  + (maxDistance!=""?"&maxDistance=" + maxDistance:"")
  			  + (numPreds!=""?"&numPreds=" + numPreds:"")
  			  + "&format=" + format;

   	  // Actually do the API call
   	  location.href = url;
    }
  </script>
  
</head>
<body>

<%@include file="/template/header.jsp" %>

<div id="title">
   Select Parameters for Predictions by Location API
</div>
   
<div id="mainDiv">   
  <div class="param">
    <label for="latitude">Latitude:</label>
    <input type="text" id="latitude" size="10" />
  </div>
  <div class="param">
    <label for="longitude">Longitude:</label>
    <input type="text" id="longitude" size="10" />
  </div>
  <div class="param">
    <label for="maxDistance">Max Distance:</label>
    <input type="text" id="maxDistance" size="10" /> <span class="note">meters (default is 1500m)</span>
  </div>
  <div class="param">
    <label for="numPreds">Number Predictions:</label>
    <input type="text" id="numPreds" size="10" /> <span class="note">(default is 3 per stop)</span>
  </div>
   
   <%-- Create json/xml format radio buttons --%>
   <jsp:include page="../params/jsonXmlFormat.jsp" />
   
   <%-- Create submit button --%> 
   <jsp:include page="../params/submitApiCall.jsp" /> 
   
</div>

</body>
</html>