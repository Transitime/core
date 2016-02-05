<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="org.transitime.utils.web.WebUtils" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%@include file="/template/includes.jsp" %>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link href="params/reportParams.css" rel="stylesheet"/>  
<link href="../select2/select2.css" rel="stylesheet"/>
<script src="../select2/select2.min.js"></script>  
<title>Stop Schedule Adherence</title>
</head>

<body>

<%@include file="/template/header.jsp" %>

<div id="mainDiv">

<div id="title">Stop Schedule Adherence</div>

<div id="menu">

	<jsp:include page="params/fromToDateTime.jsp" />

	<div class="submitDiv">
		<input type="button" id="getStops" value="Get stop data"></input>
	</div>
	
	<span id="message"></span> <br>
	<div id="extra">
		
		<div class="param">
			Limit stops by number of data points: <input type="number" id="limitStop" />
		</div>
		<div class="param">
			<input type="button" id="fiveWorst" value="Five worst"></input>
		</div>
		<div class="param">
			<input type="button" id="fiveBest" value="Five best"></input>
		</div>
		<div class="param">
			Stops: 
			<select multiple="multiple" id="stops" style="width:500px;"></select> <br>
		</div>
		
		<input type="button" id="go" value="Plot" /> <br>
	</div>
	
</div>

<div id="box_plot"></div>

<div id="title">
	<img src="images/page-loader.gif" id="loading"></img>
</div>

</div>
</body>



<script type="text/javascript" src="https://www.google.com/jsapi"></script>

<script type="text/javascript" src="javascript/stopScheduleAdherence.js"></script>

</html>