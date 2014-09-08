/*
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Transitime.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Transitime.org .  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * The API consists of there feeds: 1) a Transitime JSON/XML feed intended
 * for user interface applications; 2) GTFS-realtime feed for large applications
 * such as Google; and 3) SIRI feed simply because it is considered a standard.
 * <p>
 * ApiApplication class also specifies which package contains all the 
 * root-resource classes that specify the individual commands. There is a
 * separate root-resource class for each of the feeds.
 * <p>
 * The ApiApplication class indicates that the application path is "v1" which
 * means that the URIs for the feed are the application name, "v1", and then what
 * is specified in the root-resource classes. Each command requires an application
 * key and an agency. This means that URIs will be
 * something like /api/V1/key/TEST_KEY/agency/sfmta/command/predictionsByLoc 
 * where "api" is the webapp application name.
 *  
 * <p>
 * <b>Transitime commands.</b>  
 * <p>
 * The vehicle command for outputting location and other information for vehicles.
 * In query string can optionally specify list of vehicles or of routes. Can specify
 * vehicle IDs via v=123&v=456&etc. Can specify route via route IDs or route short
 * names. For route IDs use r=2341&r=9382&etc or for route short names use
 * rShortName=A1&rShortName=B2&etc . If vehicles or routes not specified then data
 * for all vehicles for agency is returned.
 * <p> 
 * Default output is in XML. Can specify JSON by setting the accept header in
 * the request to "application/json" or by adding format=json to the query string.
 * <p>
 * Note: you must set the Accept: header in the request. Otherwise the API will
 * return Error 400 Bad Request. This is true even if you set the format
 * query string parameter to json or xml. Browsers automatically set the Accept
 * header but wget does not so you have to use wget --header='Accept: application/xml' URL.
 * <p>
 * http://DOMAIN/api/v1/key/TEST_KEY/agency/sfmta/command/vehicles?OPTIONS
 * <p>
 * The vehiclesDetails command for outputting location and other information for vehicles.
 * Includes additional information such as block assignment information.
 * In query string can optionally specify list of vehicles or of routes. Can specify
 * vehicle IDs via v=123&v=456&etc. Can specify route via route IDs or route short
 * names. For route IDs use r=2341&r=9382&etc or for route short names use
 * rShortName=A1&rShortName=B2&etc . If vehicles or routes not specified then data
 * for all vehicles for agency is returned. 
 * Default output is in XML. Can specify JSON by setting the accept header in
 * the request or by adding format=json to the query string.
 * <p>
 * http://DOMAIN/api/v1/key/TEST_KEY/agency/sfmta/command/vehiclesDetails?OPTIONS
 * <p>
 * The predictions command for outputting prediction information for a set of stops.
 * Need to specify list of route/stops to get predictions for.
 * The route specifier is the route short
 * name for consistency across configuration changes (route ID is
 * not consistent for many agencies). Each route/stop is
 * separated by the "|" character so for example the query string
 * could have "rs=43|2029&rs=43|3029"
 * Can optionally specify
 * maximum number of predictions via numPreds=2 command. Default 
 * numPreds is 3. 
 * Default output is in XML. Can specify JSON by setting the accept header in
 * the request or by adding format=json to the query string.
 * <p>
 * http://DOMAIN/api/v1/key/TEST_KEY/agency/sfmta/command/predictions?OPTIONS
 * <p>
 * The predictionsByLoc command for outputting prediction information for stops
 * near a specified latitude/longitude.
 * Query string parameters include lat & lon such as lat=37.3824&lon=-122.391945 .
 * Can also optionally specify maximum distance in meters nearest stop on route can be from 
 * lat/lon, such as maxDistance=1200 . Default value is 1200 meters.
 * Can optionally specify
 * maximum number of predictions via numPreds=2 command. Default 
 * numPreds is 3. 
 * Default output is in XML. Can specify JSON by setting the accept header in
 * the request or by adding format=json to the query string.
 * <p>
 * http://DOMAIN/api/v1/key/TEST_KEY/agency/sfmta/command/predictionsByLoc?OPTIONS
 * <p>
 * The routes command for outputting list of routes for agency.
 * Default output is in XML. Can specify JSON by setting the accept header in
 * the request or by adding format=json to the query string.
 * <p>
 * http://DOMAIN/api/v1/key/TEST_KEY/agency/sfmta/command/routes?OPTIONS
 * <p>
 * The route command for outputting detailed information for specified routes.
 * Specify the route short name for the desired route using the route short name
 * via rShortName=XX or the route ID via r=XX.
 * Default output is in XML. Can specify JSON by setting the accept header in
 * the request or by adding format=json to the query string.
 * <p>
 * http://DOMAIN/api/v1/key/TEST_KEY/agency/sfmta/command/route?OPTIONS
 * <p>
 * The stops command for outputting list of stops for each direction
 * for a route.
 * Specify the route short name for the desired route using rShortName=38.
 * Default output is in XML. Can specify JSON by setting the accept header in
 * the request or by adding format=json to the query string.
 * <p>
 * http://DOMAIN/api/v1/key/TEST_KEY/agency/sfmta/command/stops?OPTIONS
 * <p>
 * The block command for outputting detailed information for specified
 * block assignment, including information for each trip with schedule times.
 * Need to specify block ID and service ID using for example blockId=1234&serviceId=2 .
 * Default output is in XML. Can specify JSON by setting the accept header in
 * the request or by adding format=json to the query string.
 * <p>
 * http://DOMAIN/api/v1/key/TEST_KEY/agency/sfmta/command/block?OPTIONS
 * <p>
 * The trip command for outputting detailed information for specified
 * trip, including schedule information.
 * Specify trip ID using for example tripId=43213 .
 * Default output is in XML. Can specify JSON by setting the accept header in
 * the request or by adding format=json to the query string.
 * <p>
 * http://DOMAIN/api/v1/key/TEST_KEY/agency/sfmta/command/trip?OPTIONS
 * <p>
 * The trip patterns command for outputting trip pattern information
 * for specified route.
 * Specify the route short name for the desired route using rShortName=38.
 * Default output is in XML. Can specify JSON by setting the accept header in
 * the request or by adding format=json to the query string.
 * <p>
 * http://DOMAIN/api/v1/key/TEST_KEY/agency/sfmta/command/tripPatterns?OPTIONS
 * <p>
 * The agencies command for outputting GTFS information for the agency. Note that
 * with each GTFS based system can actually be for multiple agencies.
 * Default output is in XML. Can specify JSON by setting the accept header in
 * the request or by adding format=json to the query string.
 * <p>
 * http://DOMAIN/api/v1/key/TEST_KEY/agency/sfmta/command/agencies?OPTIONS
 * <p>
 * 
 * <b>GTFS-realtime commands.</b> 
 * <p>
 * VehiclePositions command for outputting vehicle location information
 * in GTFS-realtime format. Can use query string option "format=human" for human readable output.
 * <p>
 * http://DOMAIN/api/v1/key/TEST_KEY/agency/sfmta/command/gtfs-rt/vehiclePositions?OPTIONS
 * <p>
 * TripUpdates command for outputting prediction information
 * in GTFS-realtime format. Can use query string option "format=human" for human readable output.
 * <p>
 * http://DOMAIN/api/v1/key/TEST_KEY/agency/sfmta/command/gtfs-rt/tripUpdates?OPTIONS
 * <p>
 *
 *
 * <b>SIRI commands.</b>
 * <p>
 * VehicleMonitoring command for outputting vehicle information in
 * SIRI format. In query string can optionally specify vehicle 
 * IDs via v=123&v=456&etc. and 
 * route via r=2341&r=9382&etc or rShortName=A1&rShortName=B2&etc . If vehicles
 * nor routes specified then data is returned for all vehicles for agency.
 * Default output is in XML. Can specify JSON by setting the accept header in
 * the request or by adding format=json to the query string.
 * <p>
 * http://DOMAIN/api/v1/key/TEST_KEY/agency/sfmta/command/siri/vehicleMonitoring?OPTIONS
 * <p>
 * StopMonitoring command for outputting prediction information
 * in SIRI format. In query string need to specify route and stop.
 * Can specify route via route ID via r=2341 or route short name via
 * rShortName=A1. Specify stop ID via s=1234. Can optionally specify
 * maximum number of predictions via numPreds=2 command. Default 
 * numPreds is 3.
 * Default output is in XML. Can specify JSON by setting the accept header in
 * the request or by adding format=json to the query string.
 * <p>
 * http://DOMAIN/api/v1/key/TEST_KEY/agency/sfmta/command/siri/vehicleMonitoring?OPTIONS
 * <p>
 *  
 */

package org.transitime.api;