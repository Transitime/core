<%@ page import="org.transitclock.reports.ChartGenericJsonQuery" %>
<%@ page import="org.transitclock.db.webstructs.WebAgency" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%
String agencyId = request.getParameter("a");
if (agencyId == null || agencyId.isEmpty()) {
    response.getWriter().write("You must specify agency in query string (e.g. ?a=mbta)");
    return;
}
%>
<html>
<head>
  <%@include file="/template/includes.jsp" %>

  
    <script type="text/javascript" src="https://www.google.com/jsapi"></script>
    <script type="text/javascript">
        <%
        WebAgency agency = WebAgency.getCachedWebAgency(agencyId);
        // we show different charts for mysql vs postgres
        boolean isMysqlJava = agency == null || "mysql".equals(agency.getDbType());
        %>
        var isMysql = <%=(isMysqlJava?"true":"false") %>;
      // Load in Google charts library
      google.load("visualization", "1", {packages:["table", "corechart"]});
      
      // When charts library ready then actually draw the tables/charts
      google.setOnLoadCallback(drawCharts);

      // Sets the property for the entire column, except for the header.
      // Need to use this for Table since it ignores column wide properties.
      // Can be used to set propertyName of 'style' or 'className'.
      function setColumnProperty(dataTable, columnIndex, propertyName, propertyValue) {
    	  for (var rowIndex=0; rowIndex<dataTable.getNumberOfRows(); ++rowIndex) {
    		  dataTable.setProperty(rowIndex, columnIndex, propertyName, propertyValue);
    	  }
      }
      
      // Sets the property for the entire row.
      // Need to use this for Table since it ignores row wide properties.
      // Can be used to set propertyName of 'style' or 'className'.
      function setRowProperty(dataTable, rowIndex, propertyName, propertyValue) {
    	  for (var columnIndex=0; columnIndex<dataTable.getNumberOfColumns(); ++columnIndex) {
    		  dataTable.setProperty(rowIndex, columnIndex, propertyName, propertyValue);
    	  }
      }
      
      // To be called at page load. Renders the tables and charts
      function drawCharts() {
    	<%
        String sql;
        if (isMysqlJava) {
          sql =
          	  	"SELECT table_name AS \"Table Name\", "
          	  	+ "     total_size AS \"Total Size\", "
          	  	+ "     total_bytes AS \"Total Bytes\", "
          	  	+ "     table_schema AS \"Schema\" "
          	  	+ " FROM "
                + "((SELECT table_name AS \"table_name\", "
                + "  CONCAT(ROUND(table_rows / 1000000, 2), ' MB') AS \"total_size\", "
                + "  CONCAT(ROUND(( data_length + index_length ) / ( 1024 * 1024 * 1024 ), 2), ' G') AS \"total_bytes\","
                + " table_schema,"
                + "  1 as ordering "
                + "  FROM   information_schema.TABLES"
                + " ORDER  BY data_length + index_length DESC"
                + " LIMIT 20"
                + ") "
          	    + "UNION "
          	    + "SELECT 'Total:', "
                + "  CONCAT(ROUND(sum(table_rows) / 1000000, 2), ' MB') AS \"Total Size\", "
                + "  CONCAT(ROUND(( sum(data_length + index_length) ) / ( 1024 * 1024 * 1024 ), 2), ' G') AS \"Total Bytes \","
                + " 'ALL', "
                + "2 as ordering "
                + "  FROM   information_schema.TABLES"
                + ") as needed_alias_name "
                + "ORDER BY ordering, \"Total Bytes\" DESC";
        } else {
    	  // This query is rather complicated. Want the values in order but also
    	  // want total at end. Using two queries and a union to do this but
    	  // need to also use an ordering column so that the total will always
    	  // be at the end. And then need to do a select on the whole result
    	  // to get rid of the ordering column and to provde human readable 
    	  // column titles like "Table Size".
      	  sql =
          	  	"SELECT relname AS \"Table Name\", "
          	  	+ "     total_size AS \"Total Size\", "
          	  	+ "     total_bytes AS \"Total Bytes\" "
          	  	+ " FROM "
          	    + "((SELECT relname , "
          	    + "        pg_size_pretty(pg_total_relation_size(C.oid)) AS total_size, "
          	    + "        pg_total_relation_size(C.oid) AS total_bytes, "
          	    + "        1 AS ordering"
          	    + "   FROM pg_class C " 
          	    + "   LEFT JOIN pg_namespace N ON (N.oid = C.relnamespace) " 
          	    + "  WHERE nspname NOT IN ('pg_catalog', 'information_schema') "
          	    + "    AND C.relkind <> 'i' "
          	    + "    AND nspname !~ '^pg_toast' "
          	    +") "
          	    + "UNION "
          	    + "SELECT 'Total:', "
          	    + "       pg_size_pretty(SUM(pg_relation_size(C.oid))), "
          	    + "       SUM(pg_relation_size(C.oid)), "
          	    + "       2 as ordering "
          	    + "  FROM pg_class C "
          	    + "  LEFT JOIN pg_namespace N ON (N.oid = C.relnamespace) "
          	    + " WHERE nspname NOT IN ('pg_catalog', 'information_schema') "
          	    + ") AS needed_alias_name "
          	    + "ORDER BY ordering, \"Total Bytes\" DESC";
          }
    	%>
    	var jsonData = <%= ChartGenericJsonQuery.getJsonString(agencyId, sql, null) %>;
        var data1 = new google.visualization.DataTable(jsonData);
        // Make total size cells right justified. When setting to class
        // totalSizeCell also need to set to google-visualization-table-td
        // because otherwise the default properties for those cells are erased.
        setColumnProperty(data1, 1, 'className', 'totalSizeCell google-visualization-table-td');
        
        // Make the total bold so it stands out
        var FOO = data1.getNumberOfRows();
        setRowProperty(data1, data1.getNumberOfRows()-1, 'style', 'font-weight: bold;');
        data1.setProperty(data1.getNumberOfRows()-1, 0, 'style', 'text-align: right; font-weight: bold;');
        
        // Use special formatter for the bytes column, column #2, to add commas 
        // between thousands
        var formatter = new google.visualization.NumberFormat({pattern:'#,###'});
        formatter.format(data1, 2);
        
        // Create the bar chart view
        var chartView = new google.visualization.DataView(data1);
        chartView.setColumns([0,2]);
        // Only show data for biggest db tables
        chartView.setRows(0,7);
        
        // Create the actual bar chart
        var chart = new google.visualization.BarChart(document.getElementById("chart_div"));
        var chartOptions = {
        	legend: {position: 'none'},
        	// Take up most of the space with the actual chart
        	chartArea: {left: '20%', width: '80%', top: 0, height: '90%'},
        	// 2/23/2015 Unfortunately startup animation not working. Get 
        	// message "g is undefined" from Google library
        	// I believe this problem is temporary and will be 
        	// fixed by Google at some point. 
        	//animation: {duration: 1500, startup: true },        	
        };
        if (!isMysql) {
            // this chart doesn't work with mysql
            chart.draw(chartView, chartOptions);
        }
        
        
        // Create a view for table chart so can eliminate unneeded columns and do sorting 
        // properly even with the Totals: row at bottom.
        var view1 = new google.visualization.DataView(data1);
        view1.setColumns([0,1,2]);
        
        // Actually create and draw the table
        var table1 = new google.visualization.Table(document.getElementById('table1_div'));
        var table1Options = {showRowNumber: false, allowHtml: true, sort: 'event'};
        table1.draw(view1, table1Options);

        // Create second table that also breaks out sizes of indices and keys 
        // for tables
        <%
        String sql2;
        if (isMysqlJava) {
         sql2 =
          	  	"SELECT table_name AS \"Table Name\", "
          	  	+ "     total_size AS \"Total Size\", "
          	  	+ "     total_bytes AS \"Index Bytes\", "
          	  	+ "     table_schema AS \"Schema\" "
          	  	+ " FROM "
                + "((SELECT table_name AS \"table_name\", "
                + "  CONCAT(ROUND(table_rows / 1000000, 2), ' MB') AS \"total_size\", "
                + "  CONCAT(ROUND(( index_length ) / ( 1024 * 1024 * 1024 ), 2), ' G') AS \"total_bytes\","
                + " table_schema,"
                + "  1 as ordering "
                + "  FROM   information_schema.TABLES"
                + " ORDER  BY data_length + index_length DESC"
                + " LIMIT 20"
                + ") "
          	    + "UNION "
          	    + "SELECT 'Total:', "
                + "  CONCAT(ROUND(sum(table_rows) / 1000000, 2), ' MB') AS \"Total Size\", "
                + "  CONCAT(ROUND(( sum(data_length + index_length) ) / ( 1024 * 1024 * 1024 ), 2), ' G') AS \"Total Bytes \","
                + " 'ALL', "
                + "2 as ordering "
                + "  FROM   information_schema.TABLES"
                + ") as needed_alias_name "
                + "ORDER BY ordering, \"Total Bytes\" DESC";
        } else {
        sql2 =
        	"SELECT relname AS \"Table Name\", "
    	    + "pg_size_pretty(pg_total_relation_size(C.oid)) AS \"Total Size\", "
    	    + "pg_total_relation_size(C.oid) AS \"Total Bytes\" "
    	    + "FROM pg_class C " 
    	    + "LEFT JOIN pg_namespace N ON (N.oid = C.relnamespace) " 
    	    + "WHERE nspname NOT IN ('pg_catalog', 'information_schema') "
      	    + "    AND nspname !~ '^pg_toast' "
    	    + "ORDER BY pg_total_relation_size(C.oid) DESC";
        }
    	%>
    	var jsonData2 = <%= ChartGenericJsonQuery.getJsonString(agencyId, sql2, null) %>;
        var data2 = new google.visualization.DataTable(jsonData2);
        
        // Make total size cells right justified. When setting to class
        // totalSizeCell also need to set to google-visualization-table-td
        // because otherwise the default properties for those cells are erased.
        setColumnProperty(data2, 1, 'className', 'totalSizeCell google-visualization-table-td');
        
        // Use special formatter for the bytes column, column #2, to add commas 
        // between thousands
        formatter.format(data2, 2);

        // Actually create and draw the table
        var table2 = new google.visualization.Table(document.getElementById('table2_div'));
        table2.draw(data2, {showRowNumber: false, allowHtml: true});

        // Kludge to get the Google Charts Table to only take up as much
        // width as actually needed. By doing this the table can be centered
        // in the page without having to set it to an arbitrary width.
        // This works by removing the width of the actually html tables
        // and then setting margin-left and margin-right to auto to actually
        // center the table. Using an inline function so can pass in additional
        // parameters to the function that actually does the sorting.
        google.visualization.events.addListener(table1, 'sort', function(sortProperties) {
        	sortWithTotalOnBottom(sortProperties, table1, table1Options, view1)
        });
        google.visualization.events.addListener(table2, 'sort', sortWidthHandler);
        sortWidthHandler();
      }
      
      // For sorting table with Total: row at bottom. Makes sure that row
      // stays at the bottom
      function sortWithTotalOnBottom(sortProperties, table, tableOptions, view) {
    	  var sortedRows = view.getSortedRows({column: sortProperties.column, 
    		                                   desc: !sortProperties.ascending});
    	  
    	  // Find index of where the Totals row is
    	  for (var i=0; i<sortedRows.length; ++i) {
    		  // If found the last Totals row...
    		  if (sortedRows[i] == sortedRows.length-1) {
    			  // Move the Total row to the end by moving the indixes around
    			  sortedRows.splice(i, 1);
    			  sortedRows.push(sortedRows.length);
    			  break;	  
    		  }
    	  }
    	  
    	  // Actually update the table with the proper order. First had
    	  // tried just updating sortProperties.sortedIndexes and that almost
    	  // worked but often the Totals row was placed in the middle of the table.
    	  // Note that also need to reset the table options. Otherwise seem
    	  // to lose ability to use HTML in cells.
    	  view.setRows(sortedRows);
    	  table.draw(view, tableOptions);
    	  
    	  // Get the width to be correct. Otherwise after sorting the table will
    	  // take up all width available.
    	  sortWidthHandler();
      }
      
      // Kludge to get the Google Charts Table to only take up as much
      // width as actually needed.
      function sortWidthHandler() {
          $(".google-visualization-table-table").css("width", "");
          $(".google-visualization-table-table").css("margin-left", "auto");
          $(".google-visualization-table-table").css("margin-right", "auto");
          $(".google-visualization-table").css("width", "");    	  
      }
    </script>
  
  <style>
  
  	.totalSizeCell {
  		text-align: right;
  	}
  	
  	#chart_div {
  		width: 80%;
        <% if (isMysqlJava) { %>
        height: 0px;
        <% } else { %>
        height: 250px;
        <% } %>
  		margin-left: auto;
  		margin-right: auto;
  		margin-bottom: 30px;
  	}
  	
  	/* For centering google chart tables horizontally */
  	.google-visualization-table {
  		width: 100%;
  	}
  	
  </style>
  
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Database Disk Space Utilization</title>
</head>
<body>
<%@include file="/template/header.jsp" %>
<% if ( !isMysqlJava) { %>
<!-- the main chart doesn't work with with mysql -->
<div id="title">Database Disk Space for Largest Tables</div>
<% } %>
<div id="chart_div"></div>

<div id="title">Database Disk Space by Table</div>
<div id="table1_div"></div>

<div id="title">Database Disk Space Details</div>
<div id="table2_div"></div>
</body>
</html>