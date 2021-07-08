// Code for creating the box plot is based on
// https://developers.google.com/chart/interactive/docs/gallery/intervals#box-plot

// This code should work for routes or stops depending on the value of the "group" and "groupId" variables

google.load('visualization', 1, {'packages':['corechart']});

$("#loading, #extra, #boxPlotInfo").hide()

var today = new Date().toISOString().slice(0,10)
$("#fromDate, #toDate").val(today);
$("#fromTime, #toTime").val("09:00")

$("#groups").select2()

var group = $("#_group").text();
var groupId = $("#_groupId").text();
var dataUrl = "data/" + group + "ScheduleAdherence.jsp?";

$("#getGroups").click(function() {
	
	$("#message").text("Loading...")
	
	var params = $("#params").serialize();
	
	$.get(dataUrl + params + "&byGroup=true", function(groups) {
	
		$("#extra").show();
		initGroups(groups);
		
		$("#go").off("click");
		$("#go").click(function() {
			$("#loading").show()
			var r = $("#groups").val()
			$.get(dataUrl + params + "&" + groupId + "s=" + r.join(","), main);
		})
	
		$("#limitGroup").off("change");
		$("#limitGroup").on("change", function(evt) {
			var limit = evt.target.value;
			var filterGroups = groups.filter(function(d) { return d.count > limit })
			initGroups(filterGroups);
		})
	})
})



function initGroups(groups) {
	$("#groups *").remove();
	
	$("#message").text("" + groups.length + " " + group + "s");
	
	groups.forEach(function(x) {
		var option = $("<option>")
		option.attr("value", x[groupId])
		option.text(x[groupId])
		$("#groups").append(option)
	})
	$("#groups").select2()
	
	var sortedGroups = groups.slice(0);
	sortedGroups.sort(function(a, b) { return a.scheduleAdherence - b.scheduleAdherence })
	
	var sortedGroupsAbs = groups.slice(0);
	sortedGroupsAbs.sort(function(a, b) { return Math.abs(a.scheduleAdherence) - Math.abs(b.scheduleAdherence) })
	
	$("#fiveBest, #fiveWorst, #fiveEarly, #fiveLate").off("click");
	
	$("#fiveBest").on("click", function() {
		var num = $("#numberGroups").val()
		var ids = sortedGroupsAbs.slice(0, num).map(function(d) { return d[groupId] });
		$("#groups").val(ids).trigger("change");
	})
	
	$("#fiveWorst").on("click", function() {
		var len = groups.length;
		var num = $("#numberGroups").val()
		var ids = sortedGroupsAbs.slice(len-num, len).map(function(d) { return d[groupId] });
		$("#groups").val(ids).trigger("change");
	})
	
	$("#fiveEarly").on("click", function() {
		var num = $("#numberGroups").val()
		var ids = sortedGroups.slice(0, num).map(function(d) { return d[groupId] });
		$("#groups").val(ids).trigger("change");
	})
	
	$("#fiveLate").on("click", function() {
		var len = groups.length;
		var num = $("#numberGroups").val()
		var ids = sortedGroups.slice(len-num, len).map(function(d) { return d[groupId] });
		$("#groups").val(ids).trigger("change");
	})
	
}

function main(data) {
	
	$("#loading").hide();
	$("#boxPlotInfo").show();
	
	var rowById = {};
	var array = [];
	data.forEach(function(d) {
		var row = rowById[d[groupId]]
		if (row == null) {
			row = [d[groupId]]
			rowById[d[groupId]] = row
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
      title: $("#title").text(),
      height: 500,
      legend: {position: 'none'},
      hAxis: {
        gridlines: {color: '#fff'}
      },
      vAxis: {
        title: "Adherence (sec)"
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

  var chart = new google.visualization.LineChart(document.getElementById('boxPlot'));

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
