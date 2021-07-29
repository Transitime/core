function datePickerIntialization () {
    var calendarIconTooltip = "Popup calendar to select date";

    $(".date-picker-input").datepick({
        dateFormat: "yy-mm-dd",
        showOtherMonths: true,
        selectOtherMonths: true,
        // Show button for calendar
        buttonImage: "img/calendar.gif",
        buttonImageOnly: true,
        showOn: "both",
        // Don't allow going past current date
        maxDate: 0,
        // onClose is for restricting end date to be after start date,
        // though it is potentially confusing to user
        rangeSelect: true,
        showTrigger: '<button type="button" class="trigger">' +
            '<img src="../jquery.datepick.package-5.1.0/img/calendar.gif" alt="Popup"></button>',
        onClose: function (selectedDate) {
            // Strangely need to set the title attribute for the icon again
            // so that don't revert back to a "..." tooltip
            // FIXME $(".ui-datepicker-trigger").attr("title", calendarIconTooltip);
        }
    });

    // Use a better tooltip than the default "..." for the calendar icon
    $(".ui-datepicker-trigger").attr("title", calendarIconTooltip);

    $(".time-picker-input").timepicker({timeFormat: "H:i"})
        .on('change', function (evt) {
            if (evt.originalEvent) { // manual change
                // validate that this looks like HH:MM
                if (!evt.target.value.match(/^(([0,1][0-9])|(2[0-3])):[0-5][0-9]$/))
                    evt.target.value = evt.target.oldval ? evt.target.oldval : "";
            }
            evt.target.oldval = evt.target.value;
        });

}