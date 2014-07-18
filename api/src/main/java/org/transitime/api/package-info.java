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
 * The ApiApplication class indicates that the application path is "" which
 * means that the URIs for the feed are the application name and then what
 * is specified in the root-resource classes. This means that URIs will be
 * something like /api/key/TEST_KEY/agency/sfmta/command/predictionsByLoc 
 * where "api" is the application name. 
 * <p>
 * ApiApplication class also specifies which package contains all the 
 * root-resource classes that specify the individual commands. There is a
 * separate root-resource class for each of the feeds.
 */

package org.transitime.api;