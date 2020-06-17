package org.transitclock.api.rootResources;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.transitclock.api.data.ApiArrivalDepartures;
import org.transitclock.api.data.reporting.OnTimePerformanceData;
import org.transitclock.api.data.reporting.chartjs.piechart.PieChart;
import org.transitclock.api.utils.StandardParameters;
import org.transitclock.api.utils.WebUtils;
import org.transitclock.ipc.data.IpcArrivalDeparture;
import org.transitclock.ipc.interfaces.ScheduleAdherenceInterface;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

/**
 * Contains the API commands for the Transitime API for getting data for reports.
 * <p>
 * The data output can be in either JSON or XML. The output format is specified
 * by the accept header or by using the query string parameter "format=json" or
 * "format=xml".
 *
 * @author carabalb
 *
 */
@Path("/key/{key}/agency/{agency}")
public class ReportingApi {

    @Path("/report/chartjs/arrivalDeparturesByRoute")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary="Gets arrival / departures for date range and route",
            description="Retrives a list of arrival departures for a specified date range "
                    + "Optionally can be filered accorditn to routesIdOrShortNames params."
                    + "Every trip is associated with a block.",tags= {"prediction","trip","block","route","vehicle"})
    public Response getArrivalDeparturesByRoute(
            @BeanParam StandardParameters stdParameters,
            @Parameter(description="Begin date to use for retrieving arrival departures",required=true)
            @QueryParam(value = "beginDate") DateTimeParam beginDate,
            @Parameter(description="End date to use for retrieving arrival departures",required=true)
            @QueryParam(value = "endDate") DateTimeParam endDate,
            @Parameter(description="if set, retrives only arrivalDepartures belonging to the route name specified.",required=false)
            @QueryParam(value = "r") String route,
            @Parameter(description="Begin date to use for retrieving arrival departures",required=false)
            @QueryParam(value = "minEarlySec")  @DefaultValue("90") int minEarlySec,
            @Parameter(description="Begin date to use for retrieving arrival departures",required=false)
            @QueryParam(value = "minLateSec")  @DefaultValue("150") int minLateSec,
            @Parameter(description="Specify chart data type where applicable.",required=false)
            @QueryParam(value = "chartType")  @DefaultValue("pie") String chartType)
            throws WebApplicationException {

        // Make sure request is valid
        stdParameters.validate();

        try {
            // Get active block data from server
            ScheduleAdherenceInterface scheduleAdherenceInterface =
                    stdParameters.getScheduleAdherenceInterface();

            Date beginDateTime = new Date(beginDate.getTimeStamp());
            Date endDateTime = new Date(endDate.getTimeStamp());

            List<IpcArrivalDeparture> arrivalDepartures = scheduleAdherenceInterface.getArrivalsDeparturesForRoute(beginDateTime, endDateTime, route, false);
            OnTimePerformanceData otpd = new OnTimePerformanceData();
            PieChart pieChart = otpd.getOnTimePerformanceForRoutesPieChart(arrivalDepartures, minEarlySec, minLateSec);
            return stdParameters.createResponse(pieChart);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }

    }
}
