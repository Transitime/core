<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page import="org.transitclock.web.WebConfigParams"%>
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
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Real-time Operations</title>

    <link rel="stylesheet" href="//unpkg.com/leaflet@0.7.3/dist/leaflet.css" />
    <script src="//unpkg.com/leaflet@0.7.3/dist/leaflet.js"></script>
    <script src="<%= request.getContextPath() %>/javascript/jquery-dateFormat.min.js"></script>

    <script src="<%= request.getContextPath() %>/maps/javascript/leafletRotatedMarker.js"></script>
    <script src="<%= request.getContextPath() %>/maps/javascript/mapUiOptions.js"></script>

    <!-- Load in Select2 files so can create fancy selectors -->
    <link href="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/css/select2.min.css" rel="stylesheet" />
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;700&display=swap" rel="stylesheet">
    <script src="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/js/select2.min.js"></script>

    <link rel="stylesheet" href="<%= request.getContextPath() %>/maps/css/mapUi.css" />
    <link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/1.10.21/css/jquery.dataTables.css">

    <script type="text/javascript" charset="utf8" src="https://cdn.datatables.net/1.10.21/js/jquery.dataTables.js"></script>


    <%--        <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>--%>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/page-panels.css">
    <title>TransitClock Map</title>
</head>
<body class="real-time-live-map">
<%@include file="/template/header.jsp" %>
<div class="panel split">
    <div class="left-panel">
        <h4 class="page-title">
            Dispatcher View
        </h4>
        <form class="row" novalidate>

            <div class="row mb-0">
                <label class="col-sm-12 col-form-label">Search</label>

            </div>

            <div class="row">
                <div class="col-sm-9">
                    <input type="text" class="form-control" id="vehiclesSearch" placeholder="Vehicles" name="vehiclesSearch">
                </div>
                <div class="col-sm-3 pad-left-0">
                    <button class="btn btn-primary submit-button refresh-button "  type="button" value="show" onclick="showVehicle()">Show</button>
                </div>
            </div>

            <div class="form-check">
                <input class="form-check-input" type="checkbox"  id="assignedFilter">
                <label class="form-check-label" for="assignedFilter">
                    Assigned Only
                </label>
            </div>

        </form>
        <div class="list-group">
            <a  class="list-group-item list-group-item-action secondary-btn"
                href="realTimeLiveMap.jsp?a=<%= agencyId %>" >
                Live Map View
            </a>
            <a  class="list-group-item list-group-item-action secondary-btn"
                href="realTimeScheduleAdherence.jsp?a=<%= agencyId %>" >

                Schedule Adherence View
            </a>
            <button  class="list-group-item list-group-item-action">

                Dispatcher View
            </button>
        </div>
    </div>
    <div class="right-panel  no-borders">
        <div class="gap-2 box-shadow">
            <div class="p-5">
                <div class="d-flex justify-content-end">
                    <button class="btn btn-primary refresh-button " id="tableRefresh" onclick="refreshTable()">Refresh</button>
                </div>
                <div class="table-responsive">
                    <table class="table display " id="resultsTable" width="100%">
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
            </div>
        </div>
</div>
</div>


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
                        return '<div class="d-flex justify-content-center"><a class="" href="realTimeLiveMap.jsp?a=1&v=' + row['vehicle'] + '"><i class="bi bi-arrow-right-circle-fill inherit"></i></a></div>';
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

        $(".dataTables_length select").addClass("form-select dispatcher-select-drop-down");
    });

    function refreshTable() {
        $("#resultsTable").DataTable().ajax.reload();
    }


    $("#vehiclesSearch").keydown(function(e){
        if(e.which == 13) {
          e.preventDefault();
          e.stopPropagation();
        }
        $("#resultsTable").DataTable().columns( 0 ).search( this.value ).draw();

    });

    $('#assignedFilter').on( 'change', function () {

        if($("#assignedFilter").is(':checked'))
            $("#resultsTable").DataTable().columns( 7 ).search('true').draw();
        else{
            $("#resultsTable").DataTable().columns( 7 ).search('').draw();
        }
    } );
</script>

</body>