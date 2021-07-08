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
 * Contains the classes for the servers, such as the core prediction system,
 * that provide data via RMI. The core prediction system actually has several
 * servers: the ConfigServer that can provide static configuration information,
 * the VehiclesServer which provides vehicle status info such as vehicles 
 * current location, and the PredictionsServer which provides prediction 
 * information.
 * 
 * @author SkiBu Smith
 *
 */
package org.transitclock.ipc.servers;