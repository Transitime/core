package org.transitclock.api.rootResources;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.transitclock.api.utils.StandardParameters;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/agency/{agency}")
public class GtfsRealtimeHeaderAuthApi extends AbstractGtfsRealtimeApi{

    @Path("/command/gtfs-rt/vehiclePositions")
    @GET
    @Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_OCTET_STREAM })
    @Operation(summary="GTFS-realtime Vehicle Positions data for all vehicles.",description="Gets real time vehicle position feed. It might be in human readeable format or binary.",tags= {"GTFS","feed"})
    public Response getGtfsRealtimeVehiclePositionsFeed(
            final @BeanParam StandardParameters stdParameters,
            @Parameter(description="If specified as human, it will get the output in human readable format. Otherwise will output data in binary format", required=false)
            @QueryParam(value = "format") String format)
            throws WebApplicationException {
        return super.getGtfsRealtimeVehiclePositionsFeed(stdParameters,format);
    }

    @Path("/command/gtfs-rt/tripUpdates")
    @GET
    @Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_OCTET_STREAM })
    @Operation(summary="GTFS-realtime trip data.",description="Gets real time trip feed. It might be in human readeable format or binary.",tags= {"GTFS","feed"})
    public Response getGtfsRealtimeTripFeed(
            final @BeanParam StandardParameters stdParameters,
            @Parameter(description="If specified as human, it will get the output in human readable format. Otherwise will output data in binary format", required=false)
            @QueryParam(value = "format") String format)
            throws WebApplicationException {
        return super.getGtfsRealtimeTripFeed(stdParameters, format);
    }


}
