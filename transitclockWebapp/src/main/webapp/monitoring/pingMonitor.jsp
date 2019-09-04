<%
// Determine if can communicate with all configured agencies.
// Doesn't actually do the low-level monitoring such as checking memory
// and whether AVL feed is working though. That is done by the 
// MonitoringModule that runs as part of the core system.
String errorMessage = org.transitclock.ipc.clients.AgencyMonitorClient.pingAllAgencies();

// If there is an error then return an error code and the message
if (errorMessage != null) {
    response.sendError(503 /* Service Unavailable */, 
	    errorMessage);
    return;
}
%>
All configured active agencies are running 