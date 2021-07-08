<%
// Runs low-level monitoring for all configured agencies
String errorMessage = org.transitclock.ipc.clients.AgencyMonitorClient.monitorAllAgencies();

// If there is an error then return an error code and the message
if (errorMessage != null) {
    response.sendError(503 /* Service Unavailable */, 
	    errorMessage);
    return;
}
%>
All configured active agencies are OK 