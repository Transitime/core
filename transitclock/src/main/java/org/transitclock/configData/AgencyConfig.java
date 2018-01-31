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

package org.transitclock.configData;

import org.transitclock.config.StringConfigValue;

/**
 * Configuration data commonly used for an agency. By splitting out these
 * commonly used parameters they can be accessed without needing to access
 * CoreConfig which logs lots and lots of params and clutters things up.
 *
 * @author SkiBu Smith
 *
 */
public class AgencyConfig {
	/**
	 * Specifies the ID of the agency. Used for the database name and in the
	 * logback configuration to specify the directory where to put the log
	 * files.
	 * 
	 * @return
	 */
	public static String getAgencyId() {
		return projectId.getValue();
	}
	private static StringConfigValue projectId = 
			new StringConfigValue("transitclock.core.agencyId", 
					null,
					"Specifies the ID of the agency. Used for the database " +
					"name and in the logback configuration to specify the " +
					"directory where to put the log files.");
}
