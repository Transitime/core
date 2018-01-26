<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="org.transitclock.utils.web.WebUtils" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%@include file="/template/includes.jsp" %>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link href="params/reportParams.css" rel="stylesheet"/>  
<title>Route Performance Report</title>
</head>

<body>

<%@include file="/template/header.jsp" %>

<div id="mainDiv">

<div id="title">Route Performance Table</div>

<div id="menu">

	<form>
		<input type="hidden" name="a" value="<%= request.getParameter("a")%>">
		<jsp:include page="params/fromDateNumDaysTime.jsp" />
		
		<jsp:include page="params/predictionSource.jsp" />
		
		 <div class="param">
		     <label for="predictionType">Prediction Type:</label> 
		     <select id="predictionType" name="predictionType" 
		     	title="Specifies whether or not to show prediction accuracy for 
		     	predictions that were affected by a layover. Select 'All' to show
		     	data for predictions, 'Affected by layover' to only see data where
		     	predictions affected by when a driver is scheduled to leave a layover, 
		     	or 'Not affected by layover' if you only want data for predictions 
		     	that were not affected by layovers.">
		       <option value="">All</option>
		       <option value="AffectedByWaitStop">Affected by layover</option>
		       <option value="NotAffectedByWaitStop">Not affected by layover</option>
		     </select>
		  </div>
		 	
		  <div class="param">
		    <label for="allowableEarly">Allowable Early:</label>
		    <input id="allowableEarly" name="allowableEarly"
		    	title="How early a vehicle can arrive compared to the prediction
		    	and still be acceptable. Must be a negative number to indicate
		    	early." 
		    	type="number"
		    	value="1.0"
		    	step="0.1" /> <span class="note">minutes</span>
		 </div>
		 
		 <div class="param">
		    <label for="allowableLate">Allowable Late:</label>
		    <input id="allowableLate" name="allowableLate"
		    	title="How late a vehicle can arrive compared to the prediction
		    	and still be acceptable. Must be a positive number to indicate
		    	late." 
		    	type="number"
		    	value="4.0"
		    	step="0.1" /> <span class="note">minutes</span>
		 </div>
		  
	</form>

	<div class="submitDiv">
		<input type="button" id="submit" value="Update report"></input>
	</div>
	
</div>

<div id="title">
	<img src="images/page-loader.gif" id="loading"></img>
</div>

<div id="tableDiv"></div>

</div>
</body>



<script type="text/javascript" src="https://www.google.com/jsapi"></script>

<script type="text/javascript" src="javascript/routePerformanceTable.js"></script>

</html>