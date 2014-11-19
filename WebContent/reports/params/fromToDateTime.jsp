<%-- For specifying a begin date & time and an end date & time --%> 

<script>
$(function() {
  $( "#beginDate" ).datepicker({
	dateFormat: "mm-dd-yy",
    showOtherMonths: true,
    selectOtherMonths: true,
    // Show button for calendar
    buttonImage: "images/calendar.png",
    buttonImageOnly: true,
    showOn: "both"
    // onClose is for restricting end date to be after start date, but it is potentially confusing to user
    //onClose: function( selectedDate ) {
    //  $( "#endDate" ).datepicker( "option", "minDate", selectedDate );
    //}
  });
  $( "#endDate" ).datepicker({
	dateFormat: "mm-dd-yy",
    showOtherMonths: true,
    selectOtherMonths: true,
    // Show button for calendar
    buttonImage: "images/calendar.png",
    buttonImageOnly: true,
    showOn: "both"
    // onClose is for restricting end date to be after start date, but it is potentially confusing to user
    //onClose: function( selectedDate ) {
    //  $( "#beginDate" ).datepicker( "option", "maxDate", selectedDate );
    //}
  });
});
</script>

<%
String currentDateStr = org.transitime.utils.Time.dateStr(new java.util.Date());
%>
  
  <div class="param">
    <label for="beginDate">Begin Date:</label>
    <input type="text" id="beginDate" name="beginDate" value="<%= currentDateStr%>" />
  </div>

  <div class="param">
    <label for="beginTime">Begin Time:</label>
    <input id="beginTime" name="beginTime" value="00:00" /> <span class="note">(hh:mm)</span>
  </div>

  <div class="param">
    <label for="endDate">End Date:</label>
    <input type="text" id="endDate" name="endDate" value="<%= currentDateStr%>" />
  </div>
  
  <div class="param">
    <label for="endTime">End Time:</label>
    <input id="endTime" name="endTime" value="23:59" /> <span class="note">(hh:mm)</span>
  </div>