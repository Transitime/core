<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>

    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Run Time Analysis</title>

    <!-- Load in Select2 files so can create fancy route selector -->
    <link href="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/css/select2.min.css" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;700&display=swap" rel="stylesheet" />
    <link rel="stylesheet" type="text/css" href="../jquery.datepick.package-5.1.0/css/jquery.datepick.css" />
    <link rel="stylesheet" type="text/css" href="../javascript/jquery-timepicker/jquery.timepicker.css" />


</head>
<body class="run-time-screen">
<%@include file="/template/header.jsp" %>
<div class="wrapper">
    <div class="paramsWrapper">
        <div id="paramsSidebar">
            <div class="header-title">
                Run Time Analysis
            </div>

            <div id="paramsFields">
                <%-- For passing agency param to the report --%>
                <input type="hidden" name="a" value="<%= request.getParameter("a")%>">

                <jsp:include page="params/routeAllOrSingle.jsp"/>

                <div class="param individual-route-only">
                    <label for="direction">Direction:</label>
                    <select id="direction" name="direction" disabled="true"></select>
                </div>

                <div class="param individual-route-only">
                    <label for="tripPattern">Trip Pattern:</label>
                    <select id="tripPattern" name="tripPattern" disabled="true"></select>
                </div>

                <div class="param">
                    <label for="datepicker">Date:</label>
                    <input type="text" id="datepicker" name="datepicker" class="date-picker-input"
                           title="The range of dates that you want to examine data for.
                                       <br><br> Begin date must be before the end date."
                           size="18"
                           value="Date range"/>
                </div>

                <div class="param">
                    <label for="beginTime">Begin Time:</label>
                    <input id="beginTime" name="beginTime" class="time-picker-input"
                           title="Optional begin time of day to limit query to. Useful if
                                        want to see result just for rush hour, for example. Leave blank
                                        if want data for entire day.
                                        <br/><br/>Format: hh:mm, as in '07:00' for 7AM."
                           placeholder="(hh:mm)"
                           style="width:100%;"
                           value=""/>
                </div>

                <div class="param">
                    <label for="endTime">End Time:</label>
                    <input id="endTime" name="endTime" class="time-picker-input"
                           title="Optional end time of day to limit query to. Useful if
                                        want to see result just for rush hour, for example. Leave blank
                                        if want data for entire day.
                                        <br/><br/>Format: hh:mm, as in '09:00' for 9AM.
                                        Use '23:59' for midnight."
                           placeholder="(hh:mm)"
                           style="width:100%;"
                           value=""/>
                </div>

                <div class="param">
                    <label for="serviceDayType">Service Day:</label>
                    <select id="serviceDayType" name="serviceDayType">
                        <option value="">All</option>
                        <option value="weekday">Weekday</option>
                        <option value="saturday">Saturday</option>
                        <option value="sunday">Sunday</option>
                        <span class="select2-selection__arrow">
                                    <b role="presentation"></b>
                                </span>
                    </select>
                </div>
                <div class="param vertical route-settings">
                    <span>Route Settings</span>
                    <div id="radioButtons" class="custom-radioButtons">
                        <input type="radio" name="stopType"  checked="checked"  id="timePointsOnly"><label for="timePointsOnly" id="timePointsOnlyLabel">Time Points</label>
                        <input type="radio" name="stopType" id="allStops"><label for="allStops">All Stops</label>
                    </div>
                </div>
            </div>

            <div class="submitDiv">
                <button id="submit" class="submit" >Submit</button>
            </div>
        </div>
    </div>
    <div id="mainPage" class="scrollable-element inner-spacing">
        <div id="overlay"></div>
        <div id="bars1">
            <span></span>
            <span></span>
            <span></span>
            <span></span>
            <span></span>
        </div>
        <div id="paramDetails" class="paramDetails param-detail-content">
            <p style='font-size: 0.8em;'></p>
        </div>
        <br>

        <div id="run-time-tabs">
            <ul class="only-individual-route">
                <li><a href="#component">Component</a></li>

                <li><a href="#percentage">Percentile</a></li>

                <li><a href="#distribution">Distribution</a></li>
            </ul>

            <div id="component">
                <div id="mainResults">
                    <div class="individual-route">
                        <h3>Trip Run Time Summary</h3>
                        <table class="border-table">
                            <tr>
                                <th>Average</th>
                                <th>Fixed</th>
                                <th>Variable</th>
                                <th>Dwell</th>
                            </tr>
                            <tr class="average-time-details">
                            </tr>
                        </table>
                    </div>
                </div>
                <div class="individual-route trip-block " >
                    <div class="param" id="trips-container"></div>
                </div>
                <div class="all-routes" hidden="true">
                    <h3 id="visualization-container-header">Route RunTime Performance</h3>
                </div>
                <div class="visualization-container">
                    <div id="runTimeVisualization" hidden="true"></div>
                </div>
                <br>
                <br>
            </div>

            <div id="percentage" class="only-individual-route">
                <div class="percentile-select-container" id="percentile-select-container"></div>
                <h3>Average Percentile RunTime</h3>
                <div id="percentile-summary-content"></div>
                <h3>Trip Run Time For Percentile</h3>
                <table class="border-table percentile-summary-details">

                </table>
            </div>

            <div id="distribution" class="only-individual-route">
                <div id="distributionVisualization">

                </div>

            </div>

        </div>

    </div>

    <link href="params/reportParams.css" rel="stylesheet"/>
    
    <script src="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/js/select2.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/chart.js@2.9.4"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/chartjs-chart-box-and-violin-plot/2.4.0/Chart.BoxPlot.js"></script>
    <script type="text/javascript" src="../jquery.datepick.package-5.1.0/js/jquery.plugin.js"></script>
    <script type="text/javascript" src="../jquery.datepick.package-5.1.0/js/jquery.datepick.js"></script>
    <script src="../javascript/jquery-timepicker/jquery.timepicker.min.js"></script>

    <script type="text/javascript" src="javascript/run-time-helper.js"> </script>
    <script type="text/javascript" src="javascript/run-times.js"> </script>


</div>
</body>
</html>
