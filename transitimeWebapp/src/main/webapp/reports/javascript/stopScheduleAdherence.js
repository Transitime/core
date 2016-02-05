// Code for creating the box plot is based on
// https://developers.google.com/chart/interactive/docs/gallery/intervals#box-plot

google.load('visualization', 1, {'packages':['corechart']});


$("#loading").hide()
$("#extra").hide()

var today = new Date().toISOString().slice(0,10)
$("#fromDate, #toDate").val(today);
$("#fromTime, #toTime").val("09:00")

$("#stops").select2()

$("#getStops").click(function() {
	var startDate = $("#beginDate").val()
	var endDate = $("#endDate").val()
	var startTime = $("#beginTime").val();
	var endTime = $("#endTime").val()
	
	$("#message").text("Loading...")
	
	var dateString = "startDate="+startDate+"&endDate="+endDate+"&startTime="+startTime+"&endTime="+endTime;
	console.log(dateString)
	$.get("data/stopScheduleAdherence.jsp?"+dateString+"&byStop=true", function(stops) {
	
		$("#extra").show();
		initStops(stops);
		
		$("#go").click(function() {
			$("#loading").show()
			var r = $("#stops").val()
			$.get("data/stopScheduleAdherence.jsp?"+dateString+"&stopIds=" + r.join(","), main);
		})
	
		$("#limitStop").on("change", function(evt) {
			var limit = evt.target.value;
			var filterStops = stops.filter(function(d) { return d.count > limit })
			initStops(filterStops);
		})
	})
})



function initStops(stops) {
	console.log("init stops: " + stops.length)
	$("#stops *").remove();
	
	$("#message").text("" + stops.length + " stops");
	
	stops.forEach(function(stop) {
		var option = $("<option>")
		option.attr("value", stop.stopId)
		option.text(stop.stopId)
		$("#stops").append(option)
	})
	$("#stops").select2()
	
	
	var sortedStops = stops.slice(0);
	sortedStops.sort(function(a, b) { return b.scheduleAdherence - a.scheduleAdherence })
	
	$("#fiveBest").off("click");
	$("#fiveBest").on("click", function() {
		var num = $("#numberStops").val()
		var stopIds = sortedStops.slice(0, num).map(function(d) { return d.stopId });
		$("#stops").val(stopIds).trigger("change");
	})
	
	$("#fiveWorst").off("click");
	$("#fiveWorst").on("click", function() {
		var len = stops.length;
		var num = $("#numberStops").val()
		var stopIds = sortedStops.slice(len-num, len).map(function(d) { return d.stopId });
		$("#stops").val(stopIds).trigger("change");
	})
	
}

function main(data) {
	
	$("#loading").hide();
	
	var rowById = {};
	var array = [];
	data.forEach(function(d) {
		var row = rowById[d.stopId]
		if (row == null) {
			row = [d.stopId]
			rowById[d.stopId] = row
			array.push(row)
		}
		row.push(d.scheduleAdherence)
	})
	
	var maxLength = Math.max.apply(null, array.map(function(d) { return d.length}))

	for (var i = 0; i < array.length; i++) {

		var arr = array[i].slice(1).sort(function (a, b) {
	    	return a - b;
	    });

	    var max = arr[arr.length - 1];
	    var min = arr[0];
	    var median = getMedian(arr);

	    // First Quartile is the median from lowest to overall median.
	    var firstQuartile = getMedian(arr.slice(0, 4));

	    // Third Quartile is the median from the overall median to the highest.
	    var thirdQuartile = getMedian(arr.slice(3));

	    for (var j = array[i].length; j < maxLength; j++)
	    	array[i][j] = median;
	    	
	    array[i][maxLength] = max;
	    array[i][maxLength+1] = min
	    array[i][maxLength+2] = firstQuartile;
	    array[i][maxLength+3] = median;
	    array[i][maxLength+4] = thirdQuartile;
	}
	
	drawBoxPlot(array, maxLength);

}

function drawBoxPlot(array, dataLen) {

  var data = new google.visualization.DataTable();
  data.addColumn('string', 'x');
  for (var i = 1; i < dataLen; i++)
  	data.addColumn('number', 'series'+i);
  

  data.addColumn({id:'max', type:'number', role:'interval'});
  data.addColumn({id:'min', type:'number', role:'interval'});
  data.addColumn({id:'firstQuartile', type:'number', role:'interval'});
  data.addColumn({id:'median', type:'number', role:'interval'});
  data.addColumn({id:'thirdQuartile', type:'number', role:'interval'});

  data.addRows(array);



  var options = {
      title:'Stop Schedule Adherence',
      height: 500,
      legend: {position: 'none'},
      hAxis: {
        gridlines: {color: '#fff'}
      },
      lineWidth: 0,
      series: [{'color': '#D3362D'}],
      intervals: {
        barWidth: 1,
        boxWidth: 1,
        lineWidth: 2,
        style: 'boxes'
      },
      interval: {
        max: {
          style: 'bars',
          fillOpacity: 1,
          color: '#777'
        },
        min: {
          style: 'bars',
          fillOpacity: 1,
          color: '#777'
        }
      }
  };

  var chart = new google.visualization.LineChart(document.getElementById('box_plot'));

  chart.draw(data, options);
}

/*
 * Takes an array and returns
 * the median value.
 */
function getMedian(array) {
  var length = array.length;

  /* If the array is an even length the
   * median is the average of the two
   * middle-most values. Otherwise the
   * median is the middle-most value.
   */
  if (length % 2 === 0) {
    var midUpper = length / 2;
    var midLower = midUpper - 1;

    return (array[midUpper] + array[midLower]) / 2;
  } else {
    return array[Math.floor(length / 2)];
  }
}
