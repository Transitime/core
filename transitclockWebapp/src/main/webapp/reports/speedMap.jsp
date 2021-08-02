<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <%@include file="/template/includes.jsp" %>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Speed Map</title>

    <!-- Load in Select2 files so can create fancy route selector -->
    <link href="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/css/select2.min.css" rel="stylesheet" />
    <script src="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/js/select2.min.js"></script>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;700&display=swap" rel="stylesheet">


    <link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.3/leaflet.css" />
    <script src="//cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.3/leaflet.js"></script>

    <script src="<%= request.getContextPath() %>/maps/javascript/leafletRotatedMarker.js"></script>
    <script src="<%= request.getContextPath() %>/maps/javascript/mapUiOptions.js"></script>
    <link rel="stylesheet" type="text/css" href="../jquery.datepick.package-5.1.0/css/jquery.datepick.css">

    <script type="text/javascript" src="../jquery.datepick.package-5.1.0/js/jquery.plugin.js"></script>
    <script type="text/javascript" src="../jquery.datepick.package-5.1.0/js/jquery.datepick.js"></script>

    <link rel="stylesheet" href="<%= request.getContextPath() %>/maps/css/mapUi.css" />
    <link rel="stylesheet" type="text/css" href="../javascript/jquery-timepicker/jquery.timepicker.css"></link>
    <link href="params/reportParams.css" rel="stylesheet"/>



    <script src="https://cdn.jsdelivr.net/npm/chart.js@2.8.0"></script>
    <script src="https://cdn.jsdelivr.net/gh/emn178/chartjs-plugin-labels/src/chartjs-plugin-labels.js"></script>
    <script src="../javascript/jquery-timepicker/jquery.timepicker.min.js"></script>

</head>
<body class="run-time-screen speed-map-page">
<%@include file="/template/header.jsp" %>
<div class="wrapper">

    <div class="paramsWrapper">

        <div id="paramsSidebar">
            <div class="header-title">
                Speed Map
            </div>

            <div id="paramsFields">
                <%-- For passing agency param to the report --%>
                <input type="hidden" name="a" value="<%= request.getParameter("a")%>">

                <jsp:include page="params/routeAllOrSingle.jsp" />

                <div class="param">
                    <label for="direction">Direction:</label>
                    <select id="direction" name="direction" disabled="true">

                    </select>
                </div>


                <div class="param">
                    <label for="mainDatepicker">Date:</label>
                    <input type="text" id="mainDatepicker" class="date-picker-input" name="mainDatepicker"
                           title="The range of dates that you want to examine data for.
                                   <br><br> Begin date must be before the end date."
                           value="Date range" style:/>
                </div>

                <div class="param">
                    <label for="beginTime">Begin Time:</label>
                    <input id="beginTime" name="beginTime" class="time-picker-input"
                           title="Optional begin time of day to limit query to. Useful if
                                    want to see result just for rush hour, for example. Leave blank
                                    if want data for entire day.
                                    <br/><br/>Format: hh:mm, as in '07:00' for 7AM."
                           size="5"
                           placeholder="(hh:mm)"
                           value="" />
                </div>

                <div class="param">
                    <label for="endTime">End Time:</label>
                    <input id="endTime" name="endTime"  class="time-picker-input"
                           title="Optional end time of day to limit query to. Useful if
                                    want to see result just for rush hour, for example. Leave blank
                                    if want data for entire day.
                                    <br/><br/>Format: hh:mm, as in '09:00' for 9AM.
                                    Use '23:59' for midnight."
                           size="5"
                           placeholder="(hh:mm)"
                           value="" />
                </div>

                <div class="param">
                    <label for="serviceDayType">Service Day:</label>
                    <select id="serviceDayType" name="serviceDayType">
                        <option value="">All</option>
                        <option value="weekday">Weekday</option>
                        <option value="saturday">Saturday</option>
                        <option value="sunday">Sunday</option>
                    </select>
                </div>



                <div id="speedParams">
                    <div class = "param vertical">
                        <div class="pair">
                            <div class="speedLegend" style="background-color: red;"></div>
                            <span>Low Speed (mph max)</span>
                        </div>
                        <div class="pair">
                            <input id='lowSpeedSlider' name='lowSpeedSlider' type="range" min="0" max="98" step="0.1" value="0" oninput="lowSpeedSlider(this.value)">
                            <input type="number" id="lowSpeedManual" name="lowSpeedManual" min="0" max="98" step="0.1" value="0" oninput="lowSpeedManual(this.value)">
                        </div>
                    </div>
                    <div class = "param vertical">
                        <div class="pair">
                            <div class="speedLegend" style="background-color: yellow;"></div>
                            <span>Mid Speed (mph max)</span>
                        </div>
                        <div class="pair">
                            <input id='midSpeedSlider' name='midSpeedSlider' type="range" min="1" max="99" step="0.1" value="10" oninput="midSpeedSlider(this.value)">
                            <input type="number" id="midSpeedManual" name="midSpeedManual" min="1" max="99" step="0.1" value="10" oninput="midSpeedManual(this.value)">

                        </div>
                    </div>
                    <div class = "param vertical">
                        <div class="pair">
                            <div class="speedLegend" style="background-color: green;"></div>
                            <span>High Speed (mph max)</span>
                        </div>
                    </div>
                </div>
            </div>

            <input type="button" id="mainSubmit" class="submit" value="Submit">

        </div>

    </div>

    <div id="mainPage">

        <div id="paramDetailsTop" class="paramDetails" style="height: 3%; float: left; margin-left: 20px; width: 60%;">
            <p style='font-size: 0.8em;'></p>
        </div>
        <div id="avgRunTimeTop" class="avgRunTime" style="display: inline-block; float: right; margin-right: 20px; margin-bottom: 20px; width: 30%; text-align: right"></div>
        <div id="runTimesFlyout" hidden="true">
            <div id="flyoutContents" style="margin-right: 10px; margin-left: 20px; margin-top: 10px;">
                <div id="runTimesHeader" style="text-align: left; vertical-align: middle; font-size: medium">
                    Trip Run Time Comparison
                    <button id='closeFlyout' type='button' style='float:right;'>&times;</button>
                </div>
                <div id="paramDetailsFlyout" class="paramDetails" style="margin-top: 20px; margin-bottom: 20px;"></div>
                <div id="avgRunTimeFlyout" class="avgRunTime" style="margin-top: 20px; margin-bottom: 20px;"></div>



                <div class="param">
                    <label for="flyoutDatepicker">Date:</label>
                    <input type="text" id="flyoutDatepicker" name="flyoutDatepicker"
                           class="date-picker-input"
                           title="The range of dates that you want to examine data for.
                               <br><br> Begin date must be before the end date."
                           size="18"
                           value="Date range" />
                </div>

                <input type="button" id="runTimeSubmit" class="submit" value="Submit" style="margin-top: 20px; margin-bottom: 20px;">

                <canvas id="comparisonChart" height="400" style="margin-top: 40px;"></canvas>
            </div>
        </div>
        <div id="map" style="height: 90%; width: 90%; margin: auto;">
            <div class="loader" hidden="true">
                <div id="overlay"></div>
                <div id="bars1">
                    <span></span>
                    <span></span>
                    <span></span>
                    <span></span>
                    <span></span>
                </div>
            </div>
        </div>
    </div>

</div>

<script src="<%= request.getContextPath() %>/javascript/date-picker.js"></script>
<script src="<%= request.getContextPath() %>/reports/javascript/speedmap.js"></script>
