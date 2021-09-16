
var canvas = $("#chartCanvas");

$("#timePointsOnly").prop("checked", true);
$(".toggle-chart").addClass("d-none");
var pieChart = new Chart(canvas, {
    type: 'pie',
    data: {
        datasets: [{
            data: [],
            backgroundColor: ['#fbcf37', '#dd1f1f', '#2760c3']
        }],
        labels: ['Early', 'Late', 'On time']
    },
    options: {
        legend: {
            position: 'bottom'
        },
        plugins: {
            labels: {
                render: function(args) {
                    return args.value + "\n(" + args.percentage + "%)";
                },
                fontSize: 24,
                fontColor: '#000000',
                position: 'border',
                precision: 1
            }
        }
    }
});

function showSplit(){
    $(".wrapper").addClass("split");
}

function closeSplit(){
    $(".wrapper").removeClass("split");
}

function removeSpinner() {
    $(".spinner").hide();
}



$(document).on('click', ".closeIcon", function(){closeSplit();});

$("#submit").click(function() {
    $("#submit").attr("disabled","disabled");
    $("#submit").html("Loading...");
    // $("body").addClass("loader");
    $("#overlay").show();
    $("#bars1").show();
    $("#reportResults").addClass("inactive-split");


    if ($("#beginDate").val() == "Date range") {
        var today = new Date();
        var beginDate = endDate = today.getFullYear() + "-"
            + (today.getMonth() <= 10 ? "0" + (today.getMonth() + 1) : (today.getMonth() + 1))
            + "-" + (today.getDate() < 10 ? "0" + today.getDate() : today.getDate());
    }
    else {
        var dateRangeStrings = $("#beginDate").val().replace(/\s/g, "").split("-");
        var beginYear = "20" + dateRangeStrings[0];
        var endYear = "20" + dateRangeStrings[3];
        var beginDate = [beginYear,  dateRangeStrings[1],  dateRangeStrings[2]].join("-");
        var endDate = [endYear, dateRangeStrings[4], dateRangeStrings[5]].join("-");
    }

    var beginTime = $("#beginTime").val() == "" ? "00:00:00" : $("#beginTime").val() + ":00";
    var endTime = $("#endTime").val() == "" ? "23:59:59" : $("#endTime").val() + ":00";

    request = {};
    request.beginDate = beginDate;
    request.beginTime = beginTime;
    request.endDate = endDate;
    request.endTime = endTime;
    request.r = $("#route").val();
    request.minEarlyMSec = $("#early").val() * 60000;
    request.minLateMSec = $("#late").val() * 60000;
    request.serviceType = $("#serviceDayType").val();
    request.timePointsOnly = $("#timePointsOnly")[0].checked;
    $.ajax({
        url: apiUrlPrefix + "/report/chartjs/onTimePerformanceByRoute",
        // Pass in query string parameters to page being requested
        data: request,
        // Needed so that parameters passed properly to page being requested
        traditional: true,
        dataType:"json",
        success: drawChart
    })
})

function drawChart(response) {
    $("#submit").removeAttr("disabled")
    $("#submit").html("Submit");
    $("#overlay").hide();
    $("#bars1").hide();
    $(".toggle-chart").removeClass("d-none");
    $(".image-container").addClass("d-none");
    $("#reportResults").removeClass("inactive-split");
    var values = response.data.datasets[0].data
    pieChart.data.datasets[0].data = values;
    pieChart.update();
    $("#chartTotal").html("Total count: " + values.reduce(function(total, num) {return total + num}));

    showSplit();

}

if(datePickerIntialization){
    datePickerIntialization();
}

