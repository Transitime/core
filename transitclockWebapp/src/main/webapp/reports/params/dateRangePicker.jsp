<%-- For specifying a begin date, number of days, begin time, and end time --%>
<script type="text/javascript" src="https://cdn.jsdelivr.net/jquery/latest/jquery.min.js"></script>

<script type="text/javascript" src="https://cdn.jsdelivr.net/momentjs/latest/moment.min.js"></script>
<script type="text/javascript" src="https://cdn.jsdelivr.net/npm/daterangepicker/daterangepicker.min.js"></script>
<link rel="stylesheet" type="text/css" href="https://cdn.jsdelivr.net/npm/daterangepicker/daterangepicker.css" />

<script>
    var jQueryLatest = $.noConflict(true);
    var isDateRangePicker = jQueryLatest(".isDateRangePicker");
    var singleDatePicker = true;
    var yearFormat = 'YYYY-MM-DD';
    if(isDateRangePicker && isDateRangePicker.val() == 'true'){
        singleDatePicker = false;
        yearFormat = 'YY-MM-DD';
    }
    jQueryLatest(function() {
        jQueryLatest('#dateRangePicker').daterangepicker({
            singleDatePicker: singleDatePicker,
            showDropdowns: singleDatePicker,
            minYear: 1901,
            locale:{
                format: yearFormat
            }
        }, function (start, end, label) {
            var years = moment().diff(start, 'years');
        });
    });


</script>
<script>
    $(function() {
        var calendarIconTooltip = "Popup calendar to select date";
        /*
                $( "#beginDate" ).datepicker({
                    dateFormat: "yy-mm-dd",
                    showOtherMonths: true,
                    selectOtherMonths: true,
                    // Show button for calendar
                    buttonImage: "images/calendar.gif",
                    buttonImageOnly: false,
                    showOn: "button",
                    // Don't allow going past current date
                    maxDate: 0,
                    // onClose is for restricting end date to be after start date,
                    // though it is potentially confusing to user
                    onClose: function( selectedDate ) {
                        // Strangely need to set the title attribute for the icon again
                        // so that don't revert back to a "..." tooltip
                        // FIXME $(".ui-datepicker-trigger").attr("title", calendarIconTooltip);
                    }
                });

            */


        // Use a better tooltip than the default "..." for the calendar icon
        $(".ui-datepicker-trigger").attr("title", calendarIconTooltip);



    });
</script>

<%
    String currentDateStr = org.transitclock.utils.Time.dateStr(new java.util.Date());
%>

<div class="row">
    <label class="col-sm-12 col-form-label">Date</label>
    <div class="col-sm-12 d-flex justify-content-between date-icon-wrapper align-items-center">

        <input type="text" id="dateRangePicker" name="dateRangePicker" class="form-control"
               title="The first day of the range you want to examine data for.
    	<br><br> Begin date must be before the end date." />
        <i class="bi bi-calendar3 calendar-icon"></i>
    </div>
</div>
