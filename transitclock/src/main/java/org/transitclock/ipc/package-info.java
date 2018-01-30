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
 * Contains the classes for Interprocess Communication for both RMI and JMS.
 * Key use of RMI is for the web servers to quickly get and send data to the core 
 * prediction system. Key use of JMS is for handling AVL feeds such that the 
 * data can be received on a web server and then forwarded via a queue to the
 * core prediction system. Also, JMS enables multiple systems to use the same
 * feed, which is useful if want to setup multiple test systems running off
 * of a single AVL feed.
 * <p>
 * The data is serialized in order to be transmitted.
 * 
 * @author SkiBu Smith
 *
 */
package org.transitclock.ipc;