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
 * Contains the classes for logging debug, warning, error, and info information
 * to logfiles using logback.
 * <p>
 * See the logback documentation at http://logback.qos.ch/documentation.html for 
 * more details.
 * <p>
 * Key addition to Transitime logging is that can easily generate an e-mail
 * when there is an error by using an e-mail "marker". An example is:
 * <code>
 * logger.error(Markers.email(), message);
 * </code>
 * 
 * @author SkiBu Smith
 *
 */
package org.transitime.logging;