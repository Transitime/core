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

        <style>
            label {
                text-align: left;
                width: auto;
            }

            #paramsSidebar {
                width: 20%;
                height: 100vh;
                margin-left: 10px;
                float:left;
                border-right: 1px solid black;
            }

            #links {
                margin-top: 200px;
            }

            #links div {
                margin-bottom: 30px;
            }

            td {
                text-align: center;
            }
        </style>
    </head>
    <body>
        <%@include file="/template/header.jsp" %>
        <div id="paramsSidebar">
            <div id="title" style="text-align: left; font-size:x-large">
                Real-time Operations Dispatcher View
            </div>

            <div id="paramsFields">
                <%-- For passing agency param to the report --%>
                <input type="hidden" name="a" value="<%= request.getParameter("a")%>">

                <div id="search" style="margin-top: 20px;">
                    Search
                    <br>
                    <div class="param">
                        <input type="text" id="vehiclesSearch" placeholder="Vehicles" name="vehiclesSearch">
                    </div>
                </div>
            </div>
            <div id="links">
                <div id="liveMapLink">
                    <a href="realTimeLiveMap.jsp?a=1">Live Map View >></a>
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
                            <th>Heading</th>
                            <th>Speed</th>
                            <th>Route Assignment</th>
                            <th>Sched Adherence</th>
                            <th>Operator ID</th>
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
            columns: [
                { data: 'vehicle', defaultContent: "" },
                { data: 'last_report', defaultContent: "" },
                { data: 'heading', defaultContent: ""},
                { data: 'speed', defaultContent: "" },
                { data: 'route_assignment', defaultContent: "" },
                { data: 'schedule_adherence', defaultContent: "" },
                { data: 'operator_id', defaultContent: "" },
                { data: 'map_link', render: function (data, type, row) {
                    return '<a href="realTimeLiveMap.jsp?a=1&v=' + row['vehicle'] + '">>></a>'
                }}
            ],
            ajax: {
                url: apiUrlPrefix + "/report/live/dispatch",
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
</script>