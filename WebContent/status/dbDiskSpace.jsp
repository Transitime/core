<%@ page import="org.transitime.reports.GenericJsonQuery" %>
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
  <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script> 
  
  <link rel="stylesheet" href="/api/css/general.css">
  
    <script type="text/javascript" src="https://www.google.com/jsapi"></script>
    <script type="text/javascript">
      google.load("visualization", "1", {packages:["table"]});
      google.setOnLoadCallback(drawTable);

      // Sets the property for the entire column, except for the header.
      // Need to use this for Table since it ignores column wide properties.
      // Can be used to set propertyName of 'style' or 'className'.
      function setColumnProperty(dataTable, columnIndex, propertyName, propertyValue) {
    	  for (var rowIndex=0; rowIndex<dataTable.getNumberOfRows(); ++rowIndex) {
    		  dataTable.setProperty(rowIndex, columnIndex, propertyName, propertyValue);
    	  }
      }
      
      function setRowProperty(dataTable, rowIndex, propertyName, propertyValue) {
    	  for (var columnIndex=0; columnIndex<dataTable.getNumberOfColumns(); ++columnIndex) {
    		  dataTable.setProperty(rowIndex, columnIndex, propertyName, propertyValue);
    	  }
      }
      
      function drawTable() {
    	<%
    	  // This query is rather complicated. Want the values in order but also
    	  // want total at end. Using two queries and a union to do this but
    	  // need to also use an ordering column so that the total will always
    	  // be at the end. And then need to do a select on the whole result
    	  // to get rid of the ordering column and to provde human readable 
    	  // column titles like "Table Size".
      	  String sql = 
          	  	"SELECT relname AS \"Table Name\", "
          	  	+ "     total_size AS \"Total Size\", "
          	  	+ "     total_bytes AS \"Total Bytes\" "
          	  	+ " FROM "
          	    + "((SELECT 1 AS ordering, "
          	    + "        relname , "
          	    + "        pg_size_pretty(pg_total_relation_size(C.oid)) AS total_size, "
          	    + "        pg_total_relation_size(C.oid) AS total_bytes "
          	    + "   FROM pg_class C " 
          	    + "   LEFT JOIN pg_namespace N ON (N.oid = C.relnamespace) " 
          	    + "  WHERE nspname NOT IN ('pg_catalog', 'information_schema') "
          	    + "    AND C.relkind <> 'i' "
          	    + "    AND nspname !~ '^pg_toast' "
          	    +") "
          	    + "UNION "
          	    + "SELECT 2 as ordering, " 
          	    + "       'Total:', "
          	    + "       pg_size_pretty(SUM(pg_relation_size(C.oid))), "
          	    + "       SUM(pg_relation_size(C.oid)) "
          	    + "  FROM pg_class C "
          	    + "  LEFT JOIN pg_namespace N ON (N.oid = C.relnamespace) "
          	    + " WHERE nspname NOT IN ('pg_catalog', 'information_schema') "
          	    + ") AS needed_alias_name "
          	    + "ORDER BY ordering, \"Total Bytes\" DESC";
    	%>
    	var jsonData = <%= GenericJsonQuery.getJsonString(agencyId, sql) %>;
        var data = new google.visualization.DataTable(jsonData);
        // Make total size cells right justified. When setting to class
        // totalSizeCell also need to set to google-visualization-table-td
        // because otherwise the default properties for those cells are erased.
        setColumnProperty(data, 1, 'className', 'totalSizeCell google-visualization-table-td');
        
        // Make the total bold so it stands out
        setRowProperty(data, data.getNumberOfRows()-1, 'style', 'font-weight: bold;');
        
        // Use special formatter for the bytes column, column #2, to add commas 
        // between thousands
        var formatter = new google.visualization.NumberFormat({pattern:'#,###'});
        formatter.format(data, 2);
        
        // Actually create and draw the table
        var table = new google.visualization.Table(document.getElementById('table_div'));
        table.draw(data, {showRowNumber: false, allowHtml: true});

        // Create second table that also breaks out sizes of indices and keys 
        // for tables
        <%
        String sql2 = 
        	"SELECT relname AS \"Table Name\", "
    	    + "pg_size_pretty(pg_total_relation_size(C.oid)) AS \"Total Size\", "
    	    + "pg_total_relation_size(C.oid) AS \"Total Bytes\" "
    	    + "FROM pg_class C " 
    	    + "LEFT JOIN pg_namespace N ON (N.oid = C.relnamespace) " 
    	    + "WHERE nspname NOT IN ('pg_catalog', 'information_schema') "
    	    + "ORDER BY pg_total_relation_size(C.oid) DESC";
    	%>
    	var jsonData2 = <%= GenericJsonQuery.getJsonString(agencyId, sql2) %>;
        var data2 = new google.visualization.DataTable(jsonData2);
        
        // Make total size cells right justified. When setting to class
        // totalSizeCell also need to set to google-visualization-table-td
        // because otherwise the default properties for those cells are erased.
        setColumnProperty(data2, 1, 'className', 'totalSizeCell google-visualization-table-td');
        
        // Use special formatter for the bytes column, column #2, to add commas 
        // between thousands
        formatter.format(data2, 2);

        // Actually create and draw the table
        var table2 = new google.visualization.Table(document.getElementById('table_div2'));
        table2.draw(data2, {showRowNumber: false, allowHtml: true});

        // Kludge to get the Google Charts Table to only take up as much
        // width as actually needed. By doing this the table can be centered
        // in the page without having to set it to an arbitrary width.
        // This works by removing the width of the actually html tables
        // and then setting margin-left and margin-right to auto to actually
        // center the table.
        google.visualization.events.addListener(table, 'sort', sortHandler);
        google.visualization.events.addListener(table2, 'sort', sortHandler);
        sortHandler();
      }
      
      // Kludge to get the Google Charts Table to only take up as much
      // width as actually needed.
      function sortHandler() {
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
  </style>
  
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Database Disk Space Utilization</title>
</head>
<body>
<%@include file="/template/header.jsp" %>
<div id="title">Database Disk Space Utilization</div>
<div id="table_div"></div>

<div id="title">Disk Space Details</div>
<div id="table_div2"></div>
</body>
</html>