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
 * This package contains the low-level objects that are transmitted to
 * the clients via RMI. 
 * <p>
 * There are many goals with these objects:
 * <ul>
 * <li> Objects should be Immutable so that they are always coherent and 
 *      threadsafe.</li>
 * <li> Objects must be serializable so can be used with RMI.
 * <li> Have a version number for the class so that can handle objects of
 *      different versions. This is key because won't always be able to
 *      update the servers and clients at same time.</li>
 * </ul>
 * <p>
 * The way to achieve the goals is to use a inner "SerializationProxy" 
 * class as described by Joshua Bloch in "Effective Java 2nd Edition" in
 * item 78. See the org.transitime.ipc.data.Vehicle class for an example.
 * Yes, this is more complicated then using the default serialization but
 * the default is simply not adequate because some of the structures will
 * change over time and the default serialization cannot handle all such
 * changes.
 * 
 * @author SkiBu Smith
 *
 */
package org.transitime.ipc.data;