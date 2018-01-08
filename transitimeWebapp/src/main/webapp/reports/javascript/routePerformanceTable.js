function getDataAndDrawChart() {
	$("#loading").show();
	
	$.ajax({
		"url": "routePerformanceData.jsp?" + $("#menu form").serialize(),
		"success": drawChart
	});
}

function drawChart(resp) {
	$("#loading").hide();
	
	var url = "predAccuracyRangeChart.jsp?" + $("#menu form").serialize();
	var rows = resp.map(function(d) { 
		
		var id = d.routeId;
		var link = $("<a>").attr("href", url+"&r="+id).text(id);
		
		var route = {v: id, f:link[0].outerHTML}
		
		var pv = parseFloat(d.performance);
		var pf = pv*100 + "%";
		var perf = {v: pv, f: pf}
		
		return [route, perf];
	})
	
	var data = new google.visualization.DataTable();
    data.addColumn('string', 'Route');
    data.addColumn('number', 'Performance');
   	data.addRows(rows);
   	
   	var table = new google.visualization.Table(document.getElementById('tableDiv'));
   	table.draw(data, {showRowNumber: true, width: '100%', height: '100%', page: "enable", allowHtml: true});
}

google.load("visualization", "1", {packages:["table"]});

$("#submit").click(getDataAndDrawChart);
$("#loading").hide()