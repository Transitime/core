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
package org.transitime.ipc.rmi;

/**
 *
 * @author SkiBu Smith
 *
 */
public class RmiParams {
	// Use the standard port 1099 but can change that here if necessary
	public static int rmiPort = 2099;
	
	public static int getRmiPort() { 
		return rmiPort;
	}
	
	public static void setRmiPort(int newRmiPort) {
		rmiPort = newRmiPort;
	}
}
