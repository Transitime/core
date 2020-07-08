<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <%@include file="/template/includes.jsp" %>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <title>Service Delivery</title>

        <!-- Load in Select2 files so can create fancy route selector -->
        <link href="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/css/select2.min.css" rel="stylesheet" />
        <script src="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/js/select2.min.js"></script>

        <link href="params/reportParams.css" rel="stylesheet"/>
        <style>
            hr {
                height: 2px;
                background-color: darkgray;
            }
        </style>
    </head>
    <body>
        <%@include file="/template/header.jsp" %>
        <div id="paramsSidebar" style="width: 20%; margin-left: 10px;">
            <div id="title" style="text-align: left; font-size:x-large">
                Service Delivery Report
            </div>

            <div id="paramsFields">
                <%-- For passing agency param to the report --%>
                <input type="hidden" name="a" value="<%= request.getParameter("a")%>">

                <jsp:include page="params/routeAllOrSingle.jsp" />

                <jsp:include page="params/fromDateNumDaysTime.jsp" />

                <div class="param">
<%--                    <i id="reportSettings" class="fa fa-caret-right"></i>Report Settings--%>
<%--                    <button id="reportSettings" onclick="$('#radioButtons').toggle();"></button>--%>
                    Report Settings

                    <div id="radioButtons" style="margin-top: 10px;">
                        <input type="radio" name="settings">Timepoints (selected)
                        <br>
                        <input type="radio" name="settings">All stops
                    </div>
                </div>

                <hr>
                OTP Definition
                <br>

                <div class="param" style="display: inline-block">
                    <label for="early">Min Early:</label>
                    <input type="number" id="early" name="early" min="0" max="1440" step="0.5" value="1.5">
                </div>
                <div class="param" style="display: inline-block">
                    <label for="late">Min Late:</label>
                    <input type="number" id="late" name="late" min="0" max="1440" step="0.5" value="2.5">
                </div>

                <hr>

                <div class="param">
                    <select id="serviceDayType" name="serviceDayType">
                        <option value="">Service Day Type</option>
                        <option value="">All</option>
                        <option value="weekday">Weekday</option>
                        <option value="saturday">Saturday</option>
                        <option value="sunday">Sunday</option>
                    </select>
                </div>

                <hr>
            </div>

            <input type="button" id="submit" value="Submit" style="margin-top: 10px;">

        </div>
    </body>
</html>

<script>
    $("label").attr("style", "text-align: left; width: auto;")
    $("#route").attr("style", "width: 200px");
    $("#beginTime").attr("size", "5");
    $("#endTime").attr("size", "5");
</script>