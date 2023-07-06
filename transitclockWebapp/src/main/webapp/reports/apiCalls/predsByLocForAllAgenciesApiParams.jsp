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
  	  var key = $("#apiKey").val().trim();

      var isValid = true;

      // Validate key
      if (!key) {
          $("#apiKey").addClass('is-invalid');
          isValid = false;
      } else {
          $("#apiKey").removeClass('is-invalid');
      }

      // Validate Latitude
      if (!latitude) {
          $("#latitude").addClass('is-invalid');
          isValid = false;
      } else {
          $("#latitude").removeClass('is-invalid');
      }

      // Validate Longitude
      if (!latitude) {
          $("#longitude").addClass('is-invalid');
          isValid = false;
      } else {
          $("#longitude").removeClass('is-invalid');
      }

      if(!isValid){
          return;
      }

      var url = apiUrlKeyPrefix + key + apiUrlAgencyPrefixAllAgencies + "/command/predictionsByLoc?lat=" + latitude
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
  <%-- API Key Input --%>
  <jsp:include page="../params/apiKeyInput.jsp" />

  <div class="row param">
    <div class="col-sm-5 label">Latitude:</div>
    <div class="col-sm-7">
      <input type="text" id="latitude" name="latitude" required />
      <div class="invalid-feedback">Please enter the latitude.</div>
    </div>
  </div>

  <div class="row param">
    <div class="col-sm-5 label">Longitude:</div>
    <div class="col-sm-7">
      <input type="text" id="longitude" name="longitude" required />
      <div class="invalid-feedback">Please enter the longitude.</div>
    </div>
  </div>

  <div class="row param">
    <div class="col-sm-5 label">Max Distance (meters):</div>
    <div class="col-sm-7">
      <input type="number" id="maxDistance" name="maxDistance" placeholder="1500" />
    </div>
  </div>

  <div class="row param">
    <div class="col-sm-5 label">Number of Predicitons:</div>
    <div class="col-sm-7">
      <input type="number" id="numPreds" placeholder="3" />
    </div>
  </div>
   
   <%-- Create json/xml format radio buttons --%>
   <jsp:include page="../params/jsonXmlFormat.jsp" />
   
   <%-- Create submit button --%> 
   <jsp:include page="../params/submitApiCall.jsp" /> 
   
</div>

</body>
</html>