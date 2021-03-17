<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <%@include file="/template/includes.jsp" %>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Specify Parameters</title>

    <!-- Load in Select2 files so can create fancy route selector -->
    <link href="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/css/select2.min.css" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;700&display=swap" rel="stylesheet">
    <script src="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/js/select2.min.js"></script>

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
        <input type="hidden" name="numDays" value="1"/>

        <jsp:include page="params/routeAllOrSingle.jsp"/>

        <jsp:include page="params/fromDateNumDaysTime.jsp"/>

        <jsp:include page="params/submitReport.jsp"/>
    </form>
</div>

</body>
</html>