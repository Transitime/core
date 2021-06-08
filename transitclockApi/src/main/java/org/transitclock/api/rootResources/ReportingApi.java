package org.transitclock.api.rootResources;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.apache.commons.lang3.StringUtils;
import org.transitclock.api.data.ApiStopTimes;
import org.transitclock.api.data.*;
import org.transitclock.api.data.reporting.*;
import org.transitclock.api.data.reporting.chartjs.ChartType;
import org.transitclock.api.data.ApiDispatcher;
import org.transitclock.api.utils.StandardParameters;
import org.transitclock.api.utils.WebUtils;
import org.transitclock.config.BooleanConfigValue;
import org.transitclock.core.ServiceType;
import org.transitclock.ipc.data.*;
import org.transitclock.ipc.interfaces.ReportingInterface;
import org.transitclock.ipc.interfaces.VehiclesInterface;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
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

    private static BooleanConfigValue reportingReadOnlyDb =
            new BooleanConfigValue("transitclock.api.reportingReadOnlyDb", true,
                    "Use readOnly database for reporting rmi requests.");

    private boolean useReadOnlyDb(){
        return reportingReadOnlyDb.getValue();
    }

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
            @QueryParam(value = "minEarlyMSec") @DefaultValue("90000") int minEarlyMSec,
            @Parameter(description="Begin date to use for retrieving arrival departures",required=false)
            @QueryParam(value = "minLateMSec") @DefaultValue("150000") int minLateMSec,
            @Parameter(description="if set, retrives only arrivalDepartures belonging to the serviceType (Weekday, Saturday,Sunday)",required=false)
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
                                                                                                getDate(beginDate),
                                                                                                getDate(endDate),
                                                                                                getTime(beginTime),
                                                                                                getTime(endTime),
                                                                                                route,
                                                                                                serviceTypeEnum,
                                                                                                timePointsOnly,
                                                                                                headsign,
                                                                                                useReadOnlyDb());

            Object response = null;

            if(arrivalDepartures != null){
                if(ChartType.valueOf(chartType.toUpperCase()).equals(ChartType.PIE)){
                    response = OnTimePerformanceOutput.getOnTimePerformanceForRoutesPieChart(arrivalDepartures, minEarlyMSec, minLateMSec);
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
            @Parameter(description="if set, retrives only arrivalDepartures belonging to the serviceType (Weekday, Saturday,Sunday)",required=false)
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
                                                                                            getDate(beginDate),
                                                                                            getDate(endDate),
                                                                                            getTime(beginTime),
                                                                                            getTime(endTime),
                                                                                            route,
                                                                                            serviceTypeEnum,
                                                                                            timePointsOnly,
                                                                                            headsign,
                                                                                            useReadOnlyDb());

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
            @Parameter(description="if set, retrives only arrivalDepartures belonging to the serviceType (Weekday, Saturday,Sunday)",required=false)
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

            List<IpcStopPathWithSpeed> stopPaths = reportingInterface.getStopPathsWithSpeed(getDate(beginDate),
                                                                                            getDate(endDate),
                                                                                            getTime(beginTime),
                                                                                            getTime(endTime),
                                                                                            route,
                                                                                            serviceTypeEnum,
                                                                                            headsign,
                                                                                            useReadOnlyDb());

            Object response = null;

            ApiStopPathsWithSpeed apiStopPathsWithSpeed = new ApiStopPathsWithSpeed(stopPaths);
            response = apiStopPathsWithSpeed;

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
            @Parameter(description="if set, retrives only arrivalDepartures belonging to the serviceType (Weekday, Saturday,Sunday)",required=false)
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

            IpcDoubleSummaryStatistics summaryStatistics = reportingInterface.getAverageRunTime(getDate(beginDate),
                                                                                                getDate(endDate),
                                                                                                getTime(beginTime),
                                                                                                getTime(endTime),
                                                                                                route,
                                                                                                serviceTypeEnum,
                                                                                  false,
                                                                                                headsign,
                                                                                                useReadOnlyDb());

            Object response = null;

           ApiAverageRunTime apiAverageRunTime = new ApiAverageRunTime(summaryStatistics);
           response = apiAverageRunTime;

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
            @Parameter(description="Retrives only arrivalDepartures belonging to the headsign specified.",required=true)
            @QueryParam(value = "directionId") String directionId,
            @Parameter(description="Specifies the tripPatternId to filter by.",required=false)
            @QueryParam(value = "tripPattern") String tripPatternId,
            @Parameter(description="if set, retrives only arrivalDepartures belonging to the serviceType (Weekday, Saturday,Sunday)",required=false)
            @QueryParam(value = "serviceType") String serviceType)
            throws WebApplicationException {

        // Make sure request is valid
        stdParameters.validate();

        try {
            // Get active block data from server
            ReportingInterface reportingInterface =
                    stdParameters.getReportingInterface();

            ServiceType serviceTypeEnum = null;

            if (StringUtils.isNotBlank(serviceType)) {
                serviceTypeEnum = ServiceType.valueOf(serviceType.toUpperCase());
            }

            IpcRunTime ipcRunTime = reportingInterface.getRunTimeSummary(getDate(beginDate),
                                                                         getDate(endDate),
                                                                         getTime(beginTime),
                                                                         getTime(endTime),
                                                                         route,
                                                                         headsign,
                                                                         directionId,
                                                                         tripPatternId,
                                                                         serviceTypeEnum,
                                                                         useReadOnlyDb());

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
            @Parameter(description="Begin time of time-band to use for retrieving arrival departures")
            @QueryParam(value = "beginTime") TimeParam beginTime,
            @Parameter(description="End time of time-band to use for retrieving arrival departures")
            @QueryParam(value = "endTime") TimeParam endTime,
            @Parameter(description="Retrives only arrivalDepartures belonging to the route name specified.",required=true)
            @QueryParam(value = "r") String route,
            @Parameter(description="Retrives only arrivalDepartures belonging to the headsign specified.",required=true)
            @QueryParam(value = "headsign") String headsign,
            @Parameter(description="Specifies the tripPatternId to filter by.")
            @QueryParam(value = "tripPattern") String tripPatternId,
            @Parameter(description="Specifies the directionId to filter by.")
            @QueryParam(value = "directionId") String directionId,
            @Parameter(description="if set, retrives only arrivalDepartures belonging to the serviceType (Weekday, Saturday,Sunday)")
            @QueryParam(value = "serviceType") String serviceType,
            @Parameter(description="if set, retrives only arrivalDepartures with stops that are timePoints")
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

            IpcRunTimeForTripsAndDistribution ipcRunTimeForTripsAndDistribution = reportingInterface.getRunTimeForTrips(
                                                                                            getDate(beginDate),
                                                                                            getDate(endDate),
                                                                                            getTime(beginTime),
                                                                                            getTime(endTime),
                                                                                            route,
                                                                                            headsign,
                                                                                            tripPatternId,
                                                                                            directionId,
                                                                                            serviceTypeEnum,
                                                                                            timePointsOnly,
                                                                                            useReadOnlyDb());

            Object response = TripRunTimeOutput.getRunTimes(ipcRunTimeForTripsAndDistribution);

            return stdParameters.createResponse(response);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }

    }

    @Path("/report/runTime/avgStopPathRunTimes")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary="Gets arrival / departures for date range and route",
            description="Retrives a list of arrival departures for a specified date range "
                    + "Optionally can be filered according to routesIdOrShortNames params."
                    + "Every trip is associated with a block.",tags= {"prediction","trip","block","route","vehicle"})
    public Response getAvgStopPathRunTimes(
            @BeanParam StandardParameters stdParameters,
            @Parameter(description="Begin date to use for retrieving arrival departures",required=true)
            @QueryParam(value = "beginDate") DateParam beginDate,
            @Parameter(description="End date to use for retrieving arrival departures",required=true)
            @QueryParam(value = "endDate") DateParam endDate,
            @Parameter(description="Begin time of time-band to use for retrieving arrival departures")
            @QueryParam(value = "beginTime") TimeParam beginTime,
            @Parameter(description="End time of time-band to use for retrieving arrival departures")
            @QueryParam(value = "endTime") TimeParam endTime,
            @Parameter(description="Retrives only arrivalDepartures belonging to the route name specified.",required=true)
            @QueryParam(value = "r") String route,
            @Parameter(description="Specifies the tripId to filter by.",required=true)
            @QueryParam(value = "tripId") String tripId,
            @Parameter(description="if set, retrives only arrivalDepartures belonging to the serviceType (Weekday, Saturday,Sunday)")
            @QueryParam(value = "serviceType") String serviceType,
            @Parameter(description="if set, retrives only arrivalDepartures with stops that are timePoints")
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

            List<IpcRunTimeForStopPath> ipcRunTimeForStopPaths = reportingInterface.getRunTimeForStopPaths(
                                                                                                getDate(beginDate),
                                                                                                getDate(endDate),
                                                                                                getTime(beginTime),
                                                                                                getTime(endTime),
                                                                                                route,
                                                                                                tripId,
                                                                                                serviceTypeEnum,
                                                                                                timePointsOnly,
                                                                                                useReadOnlyDb());

            Object response = StopPathRunTimeOutput.getRunTimes(ipcRunTimeForStopPaths);

            return stdParameters.createResponse(response);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    @Path("/report/runTime/routeRunTimes")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary="Gets route run-times for date range",
            description="Retrives a list of route run-times for a specified date range "
                    + "Optionally can be filered according to routesIdOrShortNames params."
                    + "Every trip is associated with a block.",tags= {"prediction","trip","block","route","vehicle"})
    public Response getRouteRunTimes(
            @BeanParam StandardParameters stdParameters,
            @Parameter(description="Begin date to use for retrieving run-times",required=true)
            @QueryParam(value = "beginDate") DateParam beginDate,
            @Parameter(description="End date to use for retrieving run-times",required=true)
            @QueryParam(value = "endDate") DateParam endDate,
            @Parameter(description="Begin time of time-band to use for retrieving run-times")
            @QueryParam(value = "beginTime") TimeParam beginTime,
            @Parameter(description="End time of time-band to use for retrieving run-times")
            @QueryParam(value = "endTime") TimeParam endTime,
            @Parameter(description="if set, retrives only run-times belonging to the serviceType (Weekday, Saturday,Sunday)")
            @QueryParam(value = "serviceType") String serviceType,
            @Parameter(description="if set, minimum number of seconds that a route has to complete a run ahead of schedule to be considered early")
            @QueryParam(value = "minEarlySec") @DefaultValue("120") int minEarlySec,
            @Parameter(description="if set, minimum number of seconds that a route has to complete a run after the scheduled time to be considered late")
            @QueryParam(value = "minLateSec") @DefaultValue("120") int minLateSec)
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

            List<IpcRunTimeForRoute> ipcRunTimeForRoutes = reportingInterface.getRunTimeForRoutes(
                    getDate(beginDate),
                    getDate(endDate),
                    getTime(beginTime),
                    getTime(endTime),
                    serviceTypeEnum,
                    minEarlySec,
                    minLateSec,
                    useReadOnlyDb());

            Object response = RouteRunTimeOutput.getRunTimes(ipcRunTimeForRoutes);

            return stdParameters.createResponse(response);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }

    }

    @Path("/report/runTime/prescriptiveRunTimes")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary="Gets route run-times for date range",
            description="Retrives a list of route run-times for a specified date range "
                    + "Optionally can be filered according to routesIdOrShortNames params."
                    + "Every trip is associated with a block.",tags= {"prediction","trip","block","route","vehicle"})
    public Response getPrescriptiveRunTimes(
            @BeanParam StandardParameters stdParameters,
            @Parameter(description="Begin time of time-band to use for retrieving run-times")
            @QueryParam(value = "beginTime") TimeParam beginTime,
            @Parameter(description="End time of time-band to use for retrieving run-times")
            @QueryParam(value = "endTime") TimeParam endTime,
            @Parameter(description="if set, retrives only run-times belonging to the serviceType (Weekday, Saturday,Sunday)")
            @QueryParam(value = "serviceType") String serviceType,
            @Parameter(description="Retrives only arrivalDepartures belonging to the route name specified.",required=true)
            @QueryParam(value = "r") String route,
            @Parameter(description="Specifies the tripPatternId to filter by.")
            @QueryParam(value = "tripPattern") String tripPatternId,
            @Parameter(description="Retrives only runTimes belonging to the headsign specified.",required=true)
            @QueryParam(value = "headsign") String headsign,
            @Parameter(description="Retrives only runTimes belonging to the directionId specified.",required=true)
            @QueryParam(value = "directionId") String directionId
        )
            throws WebApplicationException {

        // Make sure request is valid
        stdParameters.validate();

        try {
            // Get active block data from server
            ReportingInterface reportingInterface = stdParameters.getReportingInterface();

            ServiceType serviceTypeEnum = null;
            if(StringUtils.isNotBlank(serviceType)){
                serviceTypeEnum = ServiceType.valueOf(serviceType.toUpperCase());
            }

            IpcPrescriptiveRunTimes ipcPrescriptiveRunTimes = reportingInterface.getPrescriptiveRunTimes(
                    getTime(beginTime),
                    getTime(endTime),
                    route,
                    headsign,
                    directionId,
                    tripPatternId,
                    serviceTypeEnum,
                    useReadOnlyDb());

            Object response = PrescriptiveRunTimeOutput.getRunTimes(ipcPrescriptiveRunTimes);

            return stdParameters.createResponse(response);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }

    }

    @Path("/report/runTime/prescriptiveRunTimesSchedule")
    @GET
    @Produces({ "text/csv", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary="Gets route run-times for date range",
            description="Retrives a list of route run-times for a specified date range "
                    + "Optionally can be filered according to routesIdOrShortNames params."
                    + "Every trip is associated with a block.",tags= {"prediction","trip","block","route","vehicle"})
    public List<ApiStopTime> getPrescriptiveRunTimesSchedule(
            @BeanParam StandardParameters stdParameters,
            @Parameter(description="Begin time of time-band to use for retrieving run-times")
            @QueryParam(value = "beginTime") TimeParam beginTime,
            @Parameter(description="End time of time-band to use for retrieving run-times")
            @QueryParam(value = "endTime") TimeParam endTime,
            @Parameter(description="if set, retrives only run-times belonging to the serviceType (Weekday, Saturday,Sunday)")
            @QueryParam(value = "serviceType") String serviceType,
            @Parameter(description="Retrives only arrivalDepartures belonging to the route name specified.",required=true)
            @QueryParam(value = "r") String route,
            @Parameter(description="Specifies the tripPatternId to filter by.")
            @QueryParam(value = "tripPattern") String tripPatternId,
            @Parameter(description="Retrives only runTimes belonging to the headsign specified.",required=true)
            @QueryParam(value = "headsign") String headsign,
            @Parameter(description="Retrives only runTimes belonging to the directionId specified.",required=true)
            @QueryParam(value = "directionId") String directionId
    )
            throws WebApplicationException {

        // Make sure request is valid
        stdParameters.validate();

        try {
            // Get active block data from server
            ReportingInterface reportingInterface = stdParameters.getReportingInterface();

            ServiceType serviceTypeEnum = null;
            if(StringUtils.isNotBlank(serviceType)){
                serviceTypeEnum = ServiceType.valueOf(serviceType.toUpperCase());
            }

            List<IpcStopTime> ipcStopTimes = reportingInterface.getPrescriptiveRunTimesSchedule(
                    getTime(beginTime),
                    getTime(endTime),
                    route,
                    headsign,
                    directionId,
                    tripPatternId,
                    serviceTypeEnum,
                    useReadOnlyDb());

            ApiStopTimes apiStopTimes = new ApiStopTimes(ipcStopTimes);

            return apiStopTimes.getApiStopTimes();
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }

    }

    @Path("/report/live/dispatch")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary="Gets arrival / departures for date range and route",
            description="Retrives a list of arrival departures for a specified date range "
                    + "Optionally can be filered accorditn to routesIdOrShortNames params."
                    + "Every trip is associated with a block.",tags= {"prediction","trip","block","route","vehicle"})
    public Response getLiveDispatchView(
            @BeanParam StandardParameters stdParameters, 
            @Parameter(description="if set, formats speed to the specified format (MS,KM,MPH)")
            @QueryParam(value = "speedFormat") @DefaultValue("MS") String speedFormat)
            throws WebApplicationException {

        // Make sure request is valid
        stdParameters.validate();

        try {
            // Convert speedFormat param to Enum
            SpeedFormat speedFormatEnum = SpeedFormat.valueOf(speedFormat.toUpperCase());
            
            // Get vehicles interface
            VehiclesInterface vehiclesInterface = stdParameters.getVehiclesInterface();
            Collection<IpcVehicle> vehicles = vehiclesInterface.get();

            Object response = null;
            ApiDispatcher dispatcher = new ApiDispatcher(vehicles, speedFormatEnum);
            response = dispatcher;

            return stdParameters.createResponse(response);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }

    }

    private LocalDate getDate(DateParam date) {
        if(date != null){
            return date.getDate();
        }
        return null;
    }

    private LocalTime getTime(TimeParam time) {
        if(time != null){
            return time.getTime();
        }
        return null;
    }
}
