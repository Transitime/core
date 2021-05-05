function millisToMinutesAndSeconds(millis) {
    var minutes = Math.floor(millis / 60000);
    var seconds = ((millis % 60000) / 1000).toFixed(0);
    return (
        seconds == 60 ?
            (minutes+1) + ":00" :
            minutes + ":" + (seconds < 10 ? "0" : "") + seconds
    );
}

function convertSecInDayToString(secInDay) {
    var timeStr = "";
    if (secInDay < 0) {
        timeStr="-";
        secInDay = -secInDay;
    }
    var hours = Math.floor(secInDay / (60*60));
    var minutes = Math.floor((secInDay % (60*60)) / 60);

    if (hours<10) timeStr += "0";
    timeStr += hours + ":";
    if (minutes < 10) timeStr += "0";
    timeStr += minutes;
    return timeStr;
}

function msToHMS( ms ) {
    // 1- Convert to seconds:
    var seconds = ms / 1000;
    // 2- Extract hours:
    var hours = parseInt( seconds / 3600 ).toLocaleString('en-US', {minimumIntegerDigits: 2, useGrouping:false}); // 3,600 seconds in 1 hour
    seconds = seconds % 3600; // seconds remaining after extracting hours
    // 3- Extract minutes:
    var minutes = parseInt( seconds / 60 ).toLocaleString('en-US', {minimumIntegerDigits: 2, useGrouping:false}); // 60 seconds in 1 minute
    // 4- Keep only seconds not extracted to minutes:
    seconds = parseInt(seconds % 60).toLocaleString('en-US', {minimumIntegerDigits: 2, useGrouping:false});
    return ( hours+":"+minutes+":"+seconds);
}
