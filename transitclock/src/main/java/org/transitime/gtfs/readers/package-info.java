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
 * For reading in a GTFS file in CSV format. These classes extend 
 * org.transitime.utils.csv.CsvBaseReader<GtfsXXXX> class which uses 
 * the org.apache.commons.csv package for actually processing CSV files.
 * <p>
 * A key feature of these readers is that they handle additional columns
 * that are not part of the GTFS standard. In this way can easily add 
 * useful information such as route ordering for a user interface, whether
 * a stop is a timepoint, etc. 
 * <p>
 * Also, the CSV readers can merge in supplemental
 * CSV files joined by a key. For example, you can specify a supplemental 
 * routes.txt file that has just the route_short_name and a route_order
 * column defined. This file can reside in a separate location that doesn't
 * need to be updated. It can, for as many routes as desired, define
 * the route order for the routes in the user interface. When new GTFS
 * data is obtained from the agency most likely the routes haven't changed. 
 * Therefore the same unchanged supplemental file can be used when processing
 * the new GTFS data to get the route order.
 * <p>
 * Note: CsvBaseReader specifies that a line is a comment if it
 * starts with '-' character. This means can comment out lines by
 * starting them with "--", as with SQL. 
 * 
 * @author SkiBu Smith
 *
 */
package org.transitime.gtfs.readers;
