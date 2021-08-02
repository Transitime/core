<%-- For specifying a begin date & time and an end date & time --%>

<script src="../javascript/jquery-timepicker/jquery.timepicker.min.js"></script>
<link rel="stylesheet" type="text/css" href="../javascript/jquery-timepicker/jquery.timepicker.css"></link>

<link rel="stylesheet" type="text/css" href="../jquery.datepick.package-5.1.0/css/jquery.datepick.css">
<script type="text/javascript" src="../jquery.datepick.package-5.1.0/js/jquery.plugin.js"></script>
<script type="text/javascript" src="../jquery.datepick.package-5.1.0/js/jquery.datepick.js"></script>
<script src="<%= request.getContextPath() %>/javascript/date-picker.js"></script>


<%
    String currentDateStr = org.transitclock.utils.Time.dateStr(new java.util.Date());
%>

<div class="param">
    <label for="beginDate">Begin Date:</label>
    <input type="text" id="beginDate" name="beginDate" class="date-picker-input"
           title="The first day of the range you want to examine data for.
    	<br><br> Begin date must be before the end date."
           size="10"
           value="<%= currentDateStr%>" />
</div>

<div class="param">
    <label for="endDate">End Date:</label>
    <input type="text" id="endDate" name="endDate" class="time-picker-input"
           title="The last date of the range you want to examine data for.
    	<br/><br/> End date must be after the begin date."
           size="10"
           value="<%= currentDateStr%>" />
</div>

<div class="param">
    <label for="beginTime">Begin Time:</label>
    <input id="beginTime" name="beginTime" class="time-picker-input"
           title="Optional begin time of day to limit query to. Useful if
    	    want to see result just for rush hour, for example. Leave blank 
    		if want data for entire day. 
    		<br/><br/>Format: hh:mm, as in '07:00' for 7AM."
           size="3"
           value="" /> <span class="note">(hh:mm)</span>
</div>

<div class="param">
    <label for="endTime">End Time:</label>
    <input id="endTime" name="endTime"
           title="Optional end time of day to limit query to. Useful if
    	    want to see result just for rush hour, for example. Leave blank 
    		if want data for entire day. 
    		<br/><br/>Format: hh:mm, as in '09:00' for 9AM. 
    		Use '23:59' for midnight."
           size="3"
           value="" /> <span class="note">(hh:mm)</span>
</div>
    <script type="text/javascript">
        datePickerIntialization();
    </script>