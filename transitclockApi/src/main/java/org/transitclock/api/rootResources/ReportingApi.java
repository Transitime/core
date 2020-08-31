package org.transitclock.api.rootResources;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.apache.commons.lang3.StringUtils;
import org.transitclock.api.data.*;
import org.transitclock.api.data.reporting.OnTimePerformanceOutput;
import org.transitclock.api.data.reporting.TripRunTimeOutput;
import org.transitclock.api.data.reporting.chartjs.ChartType;
import org.transitclock.api.utils.StandardParameters;
import org.transitclock.api.utils.WebUtils;
import org.transitclock.core.ServiceType;
import org.transitclock.ipc.data.*;
import org.transitclock.ipc.interfaces.ReportingInterface;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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

    @Path("/report/chartjs/onTimePerformanceByRoute")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary="Gets arrival / departures for date range and route",
            description="Retrives a list of arrival departures for a specified date range "
                    + "Optionally can be filered accorditn to routesIdOrShortNames params."
                    + "Every trip is associated with a block.",tags= {"prediction","trip","block","route","vehicle"})
    public Response getArrivalDeparturesByRoute(
            @BeanParam StandardParameters stdParameters,
            @Parameter(description="Begin date to use for retrieving arrival departures",required=true)
            @QueryParam(value = "beginDate") DateParam beginDate,
            @Parameter(description="End date to use for retrieving arrival departures",required=true)
            @QueryParam(value = "endDate") DateParam endDate,
            @Parameter(description="Begin time of time-band to use for retrieving arrival departures",required=true)
            @QueryParam(value = "beginTime") TimeParam beginTime,
            @Parameter(description="End time of time-band to use for retrieving arrival departures",required=true)
            @QueryParam(value = "endTime") TimeParam endTime,
            @Parameter(description="if set, retrives only arrivalDepartures belonging to the route name specified.",required=false)
            @QueryParam(value = "r") String route,
            @Parameter(description="Retrives only arrivalDepartures belonging to the headsign specified.",required=false)
            @QueryParam(value = "headSign") String headsign,
            @Parameter(description="Begin date to use for retrieving arrival departures",required=false)
            @QueryParam(value = "minEarlySec") @DefaultValue("90") int minEarlySec,
            @Parameter(description="Begin date to use for retrieving arrival departures",required=false)
            @QueryParam(value = "minLateSec") @DefaultValue("150") int minLateSec,
            @Parameter(description="if set, retrives only arrivalDepartures belonging to the serviceType (Weekday, Saturday,Sunday",required=false)
            @QueryParam(value = "serviceType") String serviceType,
            @Parameter(description="if set, retrives only arrivalDepartures with stops that are timePoints",required=false)
            @QueryParam(value = "timePointsOnly") @DefaultValue("false") boolean timePointsOnly,
            @Parameter(description="Specify chart data type where applicable.",required=false)
            @QueryParam(value = "chartType") @DefaultValue("pie") String chartType)
            throws WebApplicationException {

        // Make sure request is valid
        stdParameters.validate();

        try {
            // Get active block data from server
            ReportingInterface reportingInterface =
                    stdParameters.getReportingInterface();

            ServiceType serviceTypeEnum = null;

            if(StringUtils.isNotBlank(serviceType)){
                serviceTypeEnum = ServiceType.valueOf(serviceType.toUpperCase());
            }

            List<IpcArrivalDepartureScheduleAdherence> arrivalDepartures = reportingInterface.getArrivalsDeparturesForOtp(
                    beginDate.getDate(), endDate.getDate(), beginTime.getTime(), endTime.getTime(), route,
                    serviceTypeEnum, timePointsOnly, headsign,false);

            Object response = null;

            if(arrivalDepartures != null){
                if(ChartType.valueOf(chartType.toUpperCase()).equals(ChartType.PIE)){
                    response = OnTimePerformanceOutput.getOnTimePerformanceForRoutesPieChart(arrivalDepartures, minEarlySec, minLateSec);
                }
            }

            return stdParameters.createResponse(response);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }

    }

    @Path("/report/speedmap/stops")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary="Gets arrival / departures for date range and route",
            description="Retrives a list of arrival departures for a specified date range "
                    + "Optionally can be filered accorditn to routesIdOrShortNames params."
                    + "Every trip is associated with a block.",tags= {"prediction","trip","block","route","vehicle"})
    public Response getSpeedMapStops(
            @BeanParam StandardParameters stdParameters,
            @Parameter(description="Begin date to use for retrieving arrival departures",required=true)
            @QueryParam(value = "beginDate") DateParam beginDate,
            @Parameter(description="End date to use for retrieving arrival departures",required=true)
            @QueryParam(value = "endDate") DateParam endDate,
            @Parameter(description="Begin time of time-band to use for retrieving arrival departures",required=true)
            @QueryParam(value = "beginTime") TimeParam beginTime,
            @Parameter(description="End time of time-band to use for retrieving arrival departures",required=true)
            @QueryParam(value = "endTime") TimeParam endTime,
            @Parameter(description="Retrives only arrivalDepartures belonging to the route name specified.",required=true)
            @QueryParam(value = "r") String route,
            @Parameter(description="Retrives only arrivalDepartures belonging to the headsign specified.",required=true)
            @QueryParam(value = "headsign") String headsign,
            @Parameter(description="if set, retrives only arrivalDepartures belonging to the serviceType (Weekday, Saturday,Sunday",required=false)
            @QueryParam(value = "serviceType") String serviceType,
            @Parameter(description="if set, retrives only arrivalDepartures with stops that are timePoints",required=false)
            @QueryParam(value = "timePointsOnly") @DefaultValue("false") boolean timePointsOnly)
            throws WebApplicationException {

        // Make sure request is valid
        stdParameters.validate();

        try {
            // Get active block data from server
            ReportingInterface reportingInterface =
                    stdParameters.getReportingInterface();

            ServiceType serviceTypeEnum = null;

            if(StringUtils.isNotBlank(serviceType)){
                serviceTypeEnum = ServiceType.valueOf(serviceType.toUpperCase());
            }

            List<IpcStopWithDwellTime> stopsWithDwellTime = reportingInterface.getStopsWithAvgDwellTimes(
                    beginDate.getDate(), endDate.getDate(), beginTime.getTime(), endTime.getTime(), route,
                    serviceTypeEnum, timePointsOnly, headsign, false);

            Object response = null;

            if(stopsWithDwellTime != null){
                ApiStopsWithDwellTime apiStopsWithDwellTime = new ApiStopsWithDwellTime(stopsWithDwellTime);
                response = apiStopsWithDwellTime;
            }
            return stdParameters.createResponse(response);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }

    }


    @Path("/report/speedmap/runTime")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary="Gets arrival / departures for date range and route",
            description="Retrives a list of arrival departures for a specified date range "
                    + "Optionally can be filered accorditn to routesIdOrShortNames params."
                    + "Every trip is associated with a block.",tags= {"prediction","trip","block","route","vehicle"})
    public Response getSpeedMapRunTime(
            @BeanParam StandardParameters stdParameters,
            @Parameter(description="Begin date to use for retrieving arrival departures",required=true)
            @QueryParam(value = "beginDate") DateParam beginDate,
            @Parameter(description="End date to use for retrieving arrival departures",required=true)
            @QueryParam(value = "endDate") DateParam endDate,
            @Parameter(description="Begin time of time-band to use for retrieving arrival departures",required=true)
            @QueryParam(value = "beginTime") TimeParam beginTime,
            @Parameter(description="End time of time-band to use for retrieving arrival departures",required=true)
            @QueryParam(value = "endTime") TimeParam endTime,
            @Parameter(description="Retrives only arrivalDepartures belonging to the route name specified.",required=true)
            @QueryParam(value = "r") String route,
            @Parameter(description="Retrives only arrivalDepartures belonging to the headsign specified.",required=true)
            @QueryParam(value = "headsign") String headsign,
            @Parameter(description="if set, retrives only arrivalDepartures belonging to the serviceType (Weekday, Saturday,Sunday",required=false)
            @QueryParam(value = "serviceType") String serviceType)
            throws WebApplicationException {

        // Make sure request is valid
        stdParameters.validate();

        try {
            // Get active block data from server
            ReportingInterface reportingInterface =
                    stdParameters.getReportingInterface();

            ServiceType serviceTypeEnum = null;

            if(StringUtils.isNotBlank(serviceType)){
                serviceTypeEnum = ServiceType.valueOf(serviceType.toUpperCase());
            }

            IpcDoubleSummaryStatistics summaryStatistics = reportingInterface.getAverageRunTime(beginDate.getDate(), endDate.getDate(),
                    beginTime.getTime(), endTime.getTime(), route, serviceTypeEnum, false,
                    headsign, false);

            Object response = null;

           ApiAverageRunTime apiAverageRunTime = new ApiAverageRunTime(summaryStatistics);
           response = apiAverageRunTime;

            return stdParameters.createResponse(response);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }

    }

    @Path("/report/speedmap/stopPathsSpeed")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary="Gets arrival / departures for date range and route",
            description="Retrives a list of arrival departures for a specified date range "
                    + "Optionally can be filered accorditn to routesIdOrShortNames params."
                    + "Every trip is associated with a block.",tags= {"prediction","trip","block","route","vehicle"})
    public Response getStopPathsSpeed(
            @BeanParam StandardParameters stdParameters,
            @Parameter(description="Begin date to use for retrieving arrival departures",required=true)
            @QueryParam(value = "beginDate") DateParam beginDate,
            @Parameter(description="End date to use for retrieving arrival departures",required=true)
            @QueryParam(value = "endDate") DateParam endDate,
            @Parameter(description="Begin time of time-band to use for retrieving arrival departures",required=true)
            @QueryParam(value = "beginTime") TimeParam beginTime,
            @Parameter(description="End time of time-band to use for retrieving arrival departures",required=true)
            @QueryParam(value = "endTime") TimeParam endTime,
            @Parameter(description="Retrives only arrivalDepartures belonging to the route name specified.",required=true)
            @QueryParam(value = "r") String route,
            @Parameter(description="Retrives only arrivalDepartures belonging to the headsign specified.",required=true)
            @QueryParam(value = "headsign") String headsign,
            @Parameter(description="if set, retrives only arrivalDepartures belonging to the serviceType (Weekday, Saturday,Sunday",required=false)
            @QueryParam(value = "serviceType") String serviceType)
            throws WebApplicationException {

        // Make sure request is valid
        stdParameters.validate();

        try {
            // Get active block data from server
            ReportingInterface reportingInterface =
                    stdParameters.getReportingInterface();

            ServiceType serviceTypeEnum = null;
            if(StringUtils.isNotBlank(serviceType)){
                serviceTypeEnum = ServiceType.valueOf(serviceType.toUpperCase());
            }

            List<IpcStopPathWithSpeed> stopPaths = reportingInterface.getStopPathsWithSpeed(
                    beginDate.getDate(), endDate.getDate(), beginTime.getTime(), endTime.getTime(),
                    route, serviceTypeEnum, headsign, false);

            Object response = null;

            ApiStopPathsWithSpeed apiStopPathsWithSpeed = new ApiStopPathsWithSpeed(stopPaths);
            response = apiStopPathsWithSpeed;

            return stdParameters.createResponse(response);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }

    }

    @Path("/report/runTime/avgRunTime")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary="Gets arrival / departures for date range and route",
            description="Retrives a list of arrival departures for a specified date range "
                    + "Optionally can be filered accorditn to routesIdOrShortNames params."
                    + "Every trip is associated with a block.",tags= {"prediction","trip","block","route","vehicle"})
    public Response getRunTimeAvgRunTime(
            @BeanParam StandardParameters stdParameters,
            @Parameter(description="Begin date to use for retrieving arrival departures",required=true)
            @QueryParam(value = "beginDate") DateParam beginDate,
            @Parameter(description="End date to use for retrieving arrival departures",required=true)
            @QueryParam(value = "endDate") DateParam endDate,
            @Parameter(description="Begin time of time-band to use for retrieving arrival departures",required=true)
            @QueryParam(value = "beginTime") TimeParam beginTime,
            @Parameter(description="End time of time-band to use for retrieving arrival departures",required=true)
            @QueryParam(value = "endTime") TimeParam endTime,
            @Parameter(description="Retrives only arrivalDepartures belonging to the route name specified.",required=true)
            @QueryParam(value = "r") String route,
            @Parameter(description="Retrives only arrivalDepartures belonging to the headsign specified.",required=true)
            @QueryParam(value = "headsign") String headsign,
            @Parameter(description="Specifies to the starting stop Id for the trips to filter by.",required=true)
            @QueryParam(value = "startStop") String startStop,
            @Parameter(description="Specifies to the ending stop Id for the trips to filter by.",required=true)
            @QueryParam(value = "endStop") String endStop,
            @Parameter(description="if set, retrives only arrivalDepartures belonging to the serviceType (Weekday, Saturday,Sunday",required=false)
            @QueryParam(value = "serviceType") String serviceType,
            @Parameter(description="if set, retrives only arrivalDepartures with stops that are timePoints",required=false)
            @QueryParam(value = "timePointsOnly") @DefaultValue("false") boolean timePointsOnly)
            throws WebApplicationException {

        // Make sure request is valid
        stdParameters.validate();

        try {
            // Get active block data from server
            ReportingInterface reportingInterface =
                    stdParameters.getReportingInterface();

            ServiceType serviceTypeEnum = null;
            if(StringUtils.isNotBlank(serviceType)){
                serviceTypeEnum = ServiceType.valueOf(serviceType.toUpperCase());
            }

            IpcRunTime ipcRunTime = reportingInterface.getRunTimeSummary(beginDate.getDate(), endDate.getDate(),
                    beginTime.getTime(), endTime.getTime(), route, headsign, startStop, endStop,
                    serviceTypeEnum, timePointsOnly, false, false);

            Object response = null;

            ApiRunTimeSummary apiRunTimeSummary = new ApiRunTimeSummary(ipcRunTime);
            response = apiRunTimeSummary;

            return stdParameters.createResponse(response);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }

    }

    @Path("/report/runTime/avgTripRunTimes")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary="Gets arrival / departures for date range and route",
            description="Retrives a list of arrival departures for a specified date range "
                    + "Optionally can be filered accorditn to routesIdOrShortNames params."
                    + "Every trip is associated with a block.",tags= {"prediction","trip","block","route","vehicle"})
    public Response getRunTimeTripAvgRunTime(
            @BeanParam StandardParameters stdParameters,
            @Parameter(description="Begin date to use for retrieving arrival departures",required=true)
            @QueryParam(value = "beginDate") DateParam beginDate,
            @Parameter(description="End date to use for retrieving arrival departures",required=true)
            @QueryParam(value = "endDate") DateParam endDate,
            @Parameter(description="Begin time of time-band to use for retrieving arrival departures",required=true)
            @QueryParam(value = "beginTime") TimeParam beginTime,
            @Parameter(description="End time of time-band to use for retrieving arrival departures",required=true)
            @QueryParam(value = "endTime") TimeParam endTime,
            @Parameter(description="Retrives only arrivalDepartures belonging to the route name specified.",required=true)
            @QueryParam(value = "r") String route,
            @Parameter(description="Retrives only arrivalDepartures belonging to the headsign specified.",required=true)
            @QueryParam(value = "headsign") String headsign,
            @Parameter(description="Specifies to the starting stop Id for the trips to filter by.",required=true)
            @QueryParam(value = "startStop") String startStop,
            @Parameter(description="Specifies to the ending stop Id for the trips to filter by.",required=true)
            @QueryParam(value = "endStop") String endStop,
            @Parameter(description="if set, retrives only arrivalDepartures belonging to the serviceType (Weekday, Saturday,Sunday",required=false)
            @QueryParam(value = "serviceType") String serviceType,
            @Parameter(description="if set, retrives only arrivalDepartures with stops that are timePoints",required=false)
            @QueryParam(value = "timePointsOnly") @DefaultValue("false") boolean timePointsOnly)
            throws WebApplicationException {

        // Make sure request is valid
        stdParameters.validate();

        try {
            // Get active block data from server
            ReportingInterface reportingInterface =
                    stdParameters.getReportingInterface();

            ServiceType serviceTypeEnum = null;
            if(StringUtils.isNotBlank(serviceType)){
                serviceTypeEnum = ServiceType.valueOf(serviceType.toUpperCase());
            }

            List<IpcRunTimeForTrip> ipcRunTimeTrips = reportingInterface.getRunTimeForTrips(
                    beginDate.getDate(), endDate.getDate(), beginTime.getTime(), endTime.getTime(),
                    route, headsign, startStop, endStop, serviceTypeEnum, timePointsOnly,
                    false, false);

            Object response = TripRunTimeOutput.getAvgTripRunTimes(ipcRunTimeTrips);

            return stdParameters.createResponse(response);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }

    }
}
