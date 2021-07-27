<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page import="org.transitclock.web.WebConfigParams"%>
<html>
<head>
    <%@include file="/template/includes.jsp" %>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Real-time Operations</title>

    <link href="params/reportParams.css" rel="stylesheet"/>

    <link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/1.10.21/css/jquery.dataTables.css">

    <script type="text/javascript" charset="utf8" src="https://cdn.datatables.net/1.10.21/js/jquery.dataTables.js"></script>

</head>
<body class="map-screen real-time-live-map real-time-schedule-adhrence real-time-dispatcher">
<%@include file="/template/header.jsp" %>
<div id="paramsSidebar">
    <div class="header-title">
        Dispatcher View
    </div>

    <div id="paramsFields">
        <%-- For passing agency param to the report --%>
        <input type="hidden" name="a" value="<%= request.getParameter("a")%>">

        <div id="search">
            <div class="paramLabel">Search</div>
            <div class="param">
                <input type="text" id="vehiclesSearch" placeholder="Vehicles" name="vehiclesSearch">
            </div>
        </div>
        <div id="assigned" style="margin-top: 30px;">
            <div class="paramCheckbox">
                <label for="assignedFilter">
                    <span>Assigned Only</span>
                    <input type="checkbox" id="assignedFilter" name="assignedFilter">
                </label>
            </div>
        </div>
    </div>
    <div id="links">
        <div id="liveMapLink">
            <a href="realTimeLiveMap.jsp?a=1">Live Map View >></a>
        </div>
        <div id="schAdhLink">
            <a href="realTimeScheduleAdherence.jsp?a=1">Schedule Adherence View >></a>
        </div>
    </div>
</div>

<div id="mainPage" style="width: 79%; height: 100%; display: inline-block;">
    <button id="tableRefresh" onclick="refreshTable()" style="float: right; margin-right: 30px; margin-top: 20px;">Refresh</button>
    <div id="tableContainer" style="width:90%; margin-left: 30px; margin-top: 40px;">
        <table id="resultsTable" class="display">
            <thead>
            <tr>
                <th>Vehicle</th>
                <th>Last Report</th>
                <th>Block</th>
                <th>Speed</th>
                <th>Route</th>
                <th>Sched Adherence</th>
                <th></th>
                <th></th>
                <th>Headway</th>
                <th>View on Map</th>
            </tr>
            </thead>
            <tbody>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>

<script>

    $(document).ready(function () {

        $('#resultsTable').DataTable({
            "lengthMenu": [[15, 50, 100, -1], [15, 50, 100, "All"]],
            columns: [
                { data: 'vehicle', defaultContent: "" },
                { data: 'last_report', defaultContent: "" },
                { data: 'block_id', defaultContent: ""},
                { data: 'speed', defaultContent: "", render: function(data,type, row) {
                        if ( type === "sort" || type === 'type' ) {
                            return data;
                        }
                        if(row['speed']) {
                            return row['speed'] + ' mph';
                        }
                        return "";
                    }},
                { data: 'route_assignment', defaultContent: "" },
                { data: 'schedule_adherence', defaultContent: "" },
                { data: 'schedule_adherence_time_diff', defaultContent: ""},
                { data: 'assigned', defaultContent: ""},
                { data: 'headway', defaultContent: "", render: function(data,type,row) {
                        if ( type === "sort" || type === 'type' ) {
                            return data;
                        }
                        if(row['headway'] > 0) {
                            return msToHMS(row['headway']);
                        }
                        return "";
                    }},
                { data: 'map_link', render: function (data, type, row) {
                        return '<a href="realTimeLiveMap.jsp?a=1&v=' + row['vehicle'] + '">>></a>';
                    }}
            ],
            columnDefs: [
                { orderData:[6], targets: [5] },
                {
                    targets: [6, 7],
                    visible: false,
                }
            ],
            ajax: {
                url: apiUrlPrefix + "/report/live/dispatch",
                data: {
                    speedFormat: 'mph'
                },
                type: "get"
            },
            dom: 'lrtip'
        });
    });

    function refreshTable() {
        $("#resultsTable").DataTable().ajax.reload();
    }

    $('#vehiclesSearch').on( 'keyup', function () {
        $("#resultsTable").DataTable().columns( 0 ).search( this.value ).draw();
    } );

    $('#assignedFilter').on( 'change', function () {

        if($("#assignedFilter").is(':checked'))
            $("#resultsTable").DataTable().columns( 7 ).search('true').draw();
        else{
            $("#resultsTable").DataTable().columns( 7 ).search('').draw();
        }
    } );
</script>