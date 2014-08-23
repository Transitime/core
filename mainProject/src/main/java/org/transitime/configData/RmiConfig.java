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

package org.transitime.configData;

import org.transitime.config.IntegerConfigValue;
import org.transitime.config.StringConfigValue;

/**
 * Config params for RMI
 *
 * @author SkiBu Smith
 *
 */
public class RmiConfig {

	/**
	 * For a client that needs to connect to an agency server. Usually would get
	 * RMI host name from the WebAgencies table in the web database. But when
	 * doing simple calls using this parameter eliminates the need to connect to
	 * the web database, speeding up testing.
	 * 
	 * @return
	 */
	public static String rmiHost() {
		return rmiHost.getValue();
	}
	private static StringConfigValue rmiHost =
			new StringConfigValue("transitime.rmi.rmiHost",
					null,
					"For a client that needs to connect to an agency server. "
					+ "Usually would get RMI host name from the WebAgencies "
					+ "table in the web database. But when doing simple calls "
					+ "using this parameter eliminates the need to connect to "
					+ "the web database, speeding up testing.");
	
	/**
	 * Which port to use for RMI calls. Usually RMI uses port 1099 but using
	 * default of 2099 to not interfere with other RMI based applications
	 * 
	 * @return
	 */
	public static int rmiPort() {
		return rmiPort.getValue();
	}
	private static IntegerConfigValue rmiPort =
			new IntegerConfigValue("transitime.rmi.rmiPort",
					2099,
					"Which port to use for RMI calls. Usually RMI uses port "
					+ "1099 but using default of 2099 to not interfere with "
					+ "other RMI based applications.");

	/**
	 * Which secondary port to use for RMI calls, for once initial communication
	 * has been established. Usually RMI uses port 0 which means any port. But
	 * then can't configure firewall to limit access to specific ports.
	 * Therefore using default value of 3099 so that the port is consistent.
	 * 
	 * @return
	 */
	public static int secondaryRmiPort() {
		return secondaryRmiPort.getValue();
	}
	private static IntegerConfigValue secondaryRmiPort =
			new IntegerConfigValue("transitime.rmi.secondaryRmiPort",
					3099,
					"Which secondary port to use for RMI calls, for once "
					+ "initial communication has been established. Usually "
					+ "RMI uses port 0 which means any port. But then can't "
					+ "configure firewall to limit access to specific ports. "
					+ "Therefore using default value of 3099 so that the port " 
					+ "is consistent.");

}
