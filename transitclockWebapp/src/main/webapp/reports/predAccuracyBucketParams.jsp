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
    <script src="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/js/select2.min.js"></script>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;700&display=swap" rel="stylesheet">

    <link href="params/reportParams.css" rel="stylesheet"/>
</head>
<body>
<%@include file="/template/header.jsp" %>
<div id="title">
    Select Parameters for Prediction Accuracy Bucket Chart
</div>

<div id="mainDiv">
    <form action="predAccuracyBucketChart.jsp" method="POST">
        <%-- For passing agency param to the report --%>
        <input type="hidden" name="a" value="<%= request.getParameter("a")%>"/>
        <input type="hidden" name="numDays" value="1"/>

        <jsp:include page="params/routeMultiple.jsp"/>

        <jsp:include page="params/fromDateNumDaysTime.jsp"/>

        <jsp:include page="params/predictionSource.jsp"/>

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

        <jsp:include page="params/stopMultiple.jsp"/>

        <div class="param, bucketParam">
            <fieldset>
                <legend>0-3 Minute Bucket</legend>
                <div class="multiAllowableSec">
                    <label class="bucketLabel" for="allowableEarly1">Allowable Early:</label>
                    <input id="allowableEarly1" name="allowableEarly1"
                           title="How early a vehicle can arrive compared to the prediction
            and still be acceptable. Must be a positive number to indicate
            early."
                           value="1.0"
                           step="0.5"
                           placeholder="minute"
                           type="number"
                    />
                </div>
                <div class="multiAllowableSec">
                    <label class="bucketLabel" for="allowableLate1">Allowable Late:</label>
                    <input id="allowableLate1" name="allowableLate1"
                           title="How late a vehicle can arrive compared to the prediction and still be acceptable. Must be a positive number to indicate late."
                           value="1.0"
                           step="0.5"
                           placeholder="minute"
                           type="number"
                    />
                </div>
            </fieldset>
            <fieldset>
                <legend>3-6 Minute Bucket</legend>
                <div class="multiAllowableSec">
                    <label class="bucketLabel" for="allowableEarly2">Allowable Early:</label>
                    <input id="allowableEarly2" name="allowableEarly2"
                           title="How early a vehicle can arrive compared to the prediction
            and still be acceptable. Must be a positive number to indicate
            early."
                           value="1.5"
                           step="0.5"
                           placeholder="minute"
                           type="number"
                    />
                </div>
                <div class="multiAllowableSec">
                    <label class="bucketLabel" for="allowableLate2">Allowable Late:</label>
                    <input id="allowableLate2" name="allowableLate2"
                           title="How late a vehicle can arrive compared to the prediction and still be acceptable. Must be a positive number to indicate late."
                           value="2.0"
                           step="0.5"
                           type="number"
                           placeholder="minute"
                    />

                </div>
            </fieldset>
            <fieldset>
                <legend>6-12 Minute Bucket</legend>
                <div class="multiAllowableSec">
                    <label class="bucketLabel" for="allowableEarly3">Allowable Early:</label>
                    <input id="allowableEarly3" name="allowableEarly3"
                           title="How early a vehicle can arrive compared to the prediction
            and still be acceptable. Must be a positive number to indicate
            early."
                           value="2.5"
                           step="0.5"
                           placeholder="minute"
                           type="number"
                    />
                </div>
                <div class="multiAllowableSec">
                    <label class="bucketLabel" for="allowableLate3">Allowable Late:</label>
                    <input id="allowableLate3" name="allowableLate3"
                           title="How late a vehicle can arrive compared to the prediction and still be acceptable. Must be a positive number to indicate late."
                           value="3.5"
                           step="0.5"
                           type="number"
                           placeholder="minute"
                    />

                </div>
            </fieldset>
            <fieldset>
                <legend>12 - 20 Minute Bucket</legend>
                <div class="multiAllowableSec">
                    <label class="bucketLabel" for="allowableEarly4">Allowable Early:</label>
                    <input id="allowableEarly4" name="allowableEarly4"
                           title="How early a vehicle can arrive compared to the prediction
            and still be acceptable. Must be a positive number to indicate
            early."
                           value="4.0"
                           step="0.5"
                           type="number"
                           placeholder="minute"
                    />
                </div>
                <div class="multiAllowableSec">
                    <label class="bucketLabel" for="allowableLate4">Allowable Late:</label>
                    <input id="allowableLate4" name="allowableLate4"
                           title="How late a vehicle can arrive compared to the prediction and still be acceptable. Must be a positive number to indicate late."
                           value="6.0"
                           step="0.5"
                           type="number"
                           placeholder="minute"
                    />

                </div>
            </fieldset>
            <fieldset>
                <legend>20 - 30 Minute Bucket</legend>
                <div class="multiAllowableSec">
                    <label class="bucketLabel" for="allowableEarly5">Allowable Early:</label>
                    <input id="allowableEarly5" name="allowableEarly5"
                           title="How early a vehicle can arrive compared to the prediction
            and still be acceptable. Must be a positive number to indicate
            early."
                           value="4.0"
                           step="0.5"
                           type="number"
                           placeholder="minute"
                    />
                </div>
                <div class="multiAllowableSec">
                    <label class="bucketLabel" for="allowableLate5">Allowable Late:</label>
                    <input id="allowableLate5" name="allowableLate5"
                           title="How late a vehicle can arrive compared to the prediction and still be acceptable. Must be a positive number to indicate late."
                           value="6.0"
                           step="0.5"
                           type="number"
                           placeholder="minute"
                    />
                </div>
            </fieldset>
        </div>

        <jsp:include page="params/submitReport.jsp"/>
    </form>
</div>

</body>
</html>