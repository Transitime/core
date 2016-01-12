<%-- For specifying a begin date & time and an end date & time --%> 

<link href="../javascript/air-datepicker/css/datepicker.css" rel="stylesheet" type="text/css">
<script src="../javascript/air-datepicker/js/datepicker.min.js"></script>

<!-- Include English language -->
<script src="../javascript/air-datepicker/js/i18n/datepicker.en.js"></script>


<!-- air-datepicker has a bug where need to set the following css style
     to prevent days in calendar from wrapping to next line when mouse
     is hovered over a date. -->
<style>
*, *::after, *::before {
    box-sizing: border-box;
}

/* Change color of dates between from date and to date so more visible */
.datepicker--cell.-in-range- {
    background: rgba(92, 196, 239, 0.5);
    }
 
/* Round the corners even more to emphasize that showing a date range */
.datepicker--cell.-range-from- {
	border-radius: 6px 0 0 6px; 
}
.datepicker--cell.-range-to- {
	border-radius: 0 6px 6px 0; 
}
     
</style>
     
<script>
$(function() {
  $( "#dateRange" ).datepicker({
	  // Note: tried using inline but positioned calendar to left
	  range: true,
	  
	  multipleDatesSeparator: ' to ',
	  
	  // Default date format uses '/' but want to use '-' because looks
	  // better and is consistent with the date formater used to specify 
	  // initial value.
	  dateFormat: 'mm-dd-yyyy',
	  
	  // Don't allow selection into the future
	  maxDate: new Date(),
	  clearButton: true,
	  
	  // Default language is Russian
	  language: 'en'
  });
});
</script>

<%
String currentDateStr = org.transitime.utils.Time.dateStr(new java.util.Date());
%>
  
  <div class="param">
    <label for="dateRange">Date Range:</label>
    <input type="text" id="dateRange" name="dateRange"  class="datepicker-here"
    	title="Range of dates report should be run for" 
    	size="24"
    	value="<%= currentDateStr%>" />
  </div>

  <div class="param">
    <label for="beginTime">Begin Time:</label>
    <input id="beginTime" name="beginTime"
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