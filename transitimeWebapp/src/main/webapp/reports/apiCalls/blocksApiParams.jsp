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
</head>
<body>

<%@include file="/template/header.jsp" %>

<div id="title">
   Select Parameters for Blocks API
</div>
   
<div id="mainDiv">   
   <jsp:include page="../params/block.jsp" />
    
   <!-- <button id="submit" type="submit" value="Run Report" />  --> 
    <script>
      function execute() {
    	  var url = apiUrlPrefix + "/command/blocks?blockId=" + $("block").value();
      }
    </script>
  </form>
</div>

</body>
</html>