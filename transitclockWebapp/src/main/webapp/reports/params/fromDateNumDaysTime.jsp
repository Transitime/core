<%-- For specifying a begin date, number of days, begin time, and end time --%>


<script src="<%= request.getContextPath() %>/jquery-ui/jquery-ui.js"></script>

<script src="../javascript/jquery-timepicker/jquery.timepicker.min.js"></script>
<link rel="stylesheet" type="text/css" href="../javascript/jquery-timepicker/jquery.timepicker.css"></link>


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
        jQueryLatest('.beginDate').daterangepicker({
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

        $("#beginTime, #endTime").timepicker({timeFormat: "H:i"})
            .on('change', function(evt) {
                if (evt.originalEvent) { // manual change
                    // validate that this looks like HH:MM
                    if (!evt.target.value.match(/^(([0,1][0-9])|(2[0-3])):[0-5][0-9]$/))
                        evt.target.value = evt.target.oldval ? evt.target.oldval : "";
                }
                evt.target.oldval = evt.target.value;
            });

    });
</script>

<%
    String currentDateStr = org.transitclock.utils.Time.dateStr(new java.util.Date());
%>

<div class="row param">
    <div class="col-sm-6 label">Date:</div>
    <div class="col-sm-6 d-flex justify-content-between date-icon-wrapper align-items-center">
        <input type="text" id="beginDate" style="margin-right: 7px;" name="beginDate" class="form-control beginDate"
               title="The first day of the range you want to examine data for.
            <br><br> Begin date must be before the end date." />
        <i class="bi bi-calendar3 calendar-icon"></i>
    </div>
</div>


<div class="row time-place-holder param">
    <div class="col-sm-6 label">Begin Time:</div>
    <div class="col-sm-6">
        <input type="text" class="form-control time-input time-input-box"
               id="beginTime" name="beginTime"
               title="Optional begin time of day to limit query to. Useful if
                want to see result just for rush hour, for example. Leave blank
                if want data for entire day.
                <br/><br/>Format: hh:mm, as in '07:00' for 7AM."
               size="3"
               placeholder="(hh:mm)"
               value=""
        />
    </div>
</div>

<div class="row  align-items-center time-place-holder param">
    <div class="col-sm-6 label">End Time:</div>
    <div class="col-sm-6">
        <input type="text" class="form-control time-input time-input-box"
               id="endTime" name="endTime"
               title="Optional end time of day to limit query to. Useful if
                want to see result just for rush hour, for example. Leave blank
                if want data for entire day.
                <br/><br/>Format: hh:mm, as in '09:00' for 9AM.
                Use '23:59' for midnight."
               size="3"
               placeholder="(hh:mm)"
               value=""
        />
    </div>
</div>

<input type="hidden" id="custId" name="custId" value="3487">