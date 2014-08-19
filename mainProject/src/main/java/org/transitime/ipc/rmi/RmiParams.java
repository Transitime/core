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

import org.transitime.configData.CoreConfig;

/**
 * For defining port numbers to be used for RMI
 * 
 * @author SkiBu Smith
 *
 */
public class RmiParams {
	// Usually will use special port 2099 in case another app is using 
	// standard RMI port 1099
	public static int rmiPort = CoreConfig.rmiPort();
	
	// For secondary communication. Usually RMI uses port 0, which means any old
	// random port, but to deal with firewalls it is better to use a fixed port.
	public static int secondaryRmiPort = CoreConfig.secondaryRmiPort();
	
	public static int getRmiPort() { 
		return rmiPort;
	}

	public static void setRmiPort(int newRmiPort) {
		rmiPort = newRmiPort;
	}

	public static int getSecondaryRmiPort() {
		return secondaryRmiPort;
	}
	
	public static void setSecondaryRmiPort(int newSecondaryRmiPort) {
		secondaryRmiPort = newSecondaryRmiPort;
	}
	
}
