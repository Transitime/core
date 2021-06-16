/*
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License (GPL) as published by the
 * Free Software Foundation, either version 3 of the License, or any later
 * version.
 * 
 * Transitime.org is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Transitime.org . If not, see <http://www.gnu.org/licenses/>.
 */

package org.transitclock.api.rootResources;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.transitclock.api.gtfsRealtime.GtfsRtTripFeed;
import org.transitclock.api.gtfsRealtime.GtfsRtVehicleFeed;
import org.transitclock.api.utils.StandardParameters;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.feed.gtfsRt.OctalDecoder;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.servers.Servers;

/**
 * Contains API commands for the GTFS-realtime API.
 *
 * @author SkiBu Smith
 *
 */
public abstract class AbstractGtfsRealtimeApi {

	private final static int DEFAULT_MAX_GTFS_RT_CACHE_SECS = 15;

	private static IntegerConfigValue gtfsRtCacheSeconds =
			new IntegerConfigValue(
					"transitclock.api.gtfsRtCacheSeconds", 
					DEFAULT_MAX_GTFS_RT_CACHE_SECS,
					"How long to cache GTFS Realtime");

	
	/********************** Member Functions **************************/

	/**
	 * For getting GTFS-realtime Vehicle Positions data for all vehicles.
	 * 
	 * @param stdParameters
	 * @param format
	 *            if set to "human" then will output GTFS-rt data in human
	 *            readable format. Otherwise will output data in binary format.
	 * @return
	 * @throws WebApplicationException
	 */
	public Response getGtfsRealtimeVehiclePositionsFeed(StandardParameters stdParameters, String format)
			throws WebApplicationException {

		// Make sure request is valid
		stdParameters.validate();

		// Determine if output should be in human readable format or in
		// standard binary GTFS-realtime format.
		final boolean humanFormatOutput = "human".equals(format);

		// Determine the appropriate output format. For plain text best to use
		// MediaType.TEXT_PLAIN so that output is formatted properly in web
		// browser instead of newlines being removed. For binary output should
		// use MediaType.APPLICATION_OCTET_STREAM.
		String mediaType =
				humanFormatOutput ? MediaType.TEXT_PLAIN
						: MediaType.APPLICATION_OCTET_STREAM;

		// Prepare a StreamingOutput object so can write using it
		StreamingOutput stream = new StreamingOutput() {
			public void write(OutputStream outputStream) throws IOException,
					WebApplicationException {
				try {
					FeedMessage message =
							GtfsRtVehicleFeed.getPossiblyCachedMessage(
									stdParameters.getAgencyId(),
									gtfsRtCacheSeconds.getValue());

					// Output in human readable format or in standard binary
					// format
					if (humanFormatOutput) {
						// Output data in human readable format. First, convert
						// the octal escaped message to regular UTF encoding.
						String decodedMessage =
								OctalDecoder.convertOctalEscapedString(message
										.toString());
						outputStream.write(decodedMessage.getBytes());
					} else {
						// Standard binary output
						message.writeTo(outputStream);
					}
				} catch (Exception e) {
					throw new WebApplicationException(e);
				}
			}
		};

		// Write out the data using the output stream
		return Response.ok(stream).type(mediaType).build();
	}

	/**
	 * For getting GTFS-realtime for all trips.
	 * 
	 * @param stdParameters
	 * @param format
	 *            if set to "human" then will output GTFS-rt data in human
	 *            readable format. Otherwise will output data in binary format.
	 * @return
	 * @throws WebApplicationException
	 */
	public Response getGtfsRealtimeTripFeed(StandardParameters stdParameters, String format)
			throws WebApplicationException {

		// Make sure request is valid
		stdParameters.validate();

		// Determine if output should be in human readable format or in
		// standard binary GTFS-realtime format.
		final boolean humanFormatOutput = "human".equals(format);

		// Determine the appropriate output format. For plain text best to use
		// MediaType.TEXT_PLAIN so that output is formatted properly in web
		// browser instead of newlines being removed. For binary output should
		// use MediaType.APPLICATION_OCTET_STREAM.
		String mediaType =
				humanFormatOutput ? MediaType.TEXT_PLAIN
						: MediaType.APPLICATION_OCTET_STREAM;

		// Prepare a StreamingOutput object so can write using it
		StreamingOutput stream = new StreamingOutput() {
			public void write(OutputStream outputStream) throws IOException,
					WebApplicationException {
				try {
					FeedMessage message =
							GtfsRtTripFeed.getPossiblyCachedMessage(
									stdParameters.getAgencyId(),
									gtfsRtCacheSeconds.getValue());

					// Output in human readable format or in standard binary
					// format
					if (humanFormatOutput) {
						// Output data in human readable format. First, convert
						// the octal escaped message to regular UTF encoding.
						String decodedMessage =
								OctalDecoder.convertOctalEscapedString(message
										.toString());
						outputStream.write(decodedMessage.getBytes());
					} else {
						// Standard binary output
						message.writeTo(outputStream);
					}
				} catch (Exception e) {
					throw new WebApplicationException(e);
				}
			}
		};

		// Write out the data using the output stream
		return Response.ok(stream).type(mediaType).build();
	}

}
