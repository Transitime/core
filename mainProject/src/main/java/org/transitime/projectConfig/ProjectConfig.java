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
package org.transitime.projectConfig;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides configuration information for multiple projects. Intended for
 * website, not the individual project applications since those can use
 * the regular per-project configurations. 
 * 
 * FIXME Should be dynamic so that if config is changed it can be
 * reloaded and have an effect.
 * 
 * @author SkiBu Smith
 *
 */
public class ProjectConfig {
	private final String rmiHost;
	private final String dbHost;
	private final String dbUsername;
	private final String dbPassword;
	
	// Contains all the ProjectConfig info
	private static HashMap<String, ProjectConfig> configs =
			new HashMap<String, ProjectConfig>();

	// This needs to be after the configs HashMap is declared
	// so that initialization works properly.
	// FIXME Shouldn't have to add each db manually!
	static {
		new ProjectConfig("testProjectId", null, "localhost", "root", "transitime");		
		new ProjectConfig("mbta", null, "localhost", "root", "transitime");		
		new ProjectConfig("sf-muni", null, "localhost", "root", "transitime");		
		new ProjectConfig("zhengzhou", null, "localhost", "root", "transitime");		
	}
	
	private static final Logger logger = 
			LoggerFactory.getLogger(ProjectConfig.class);

	/********************** Member Functions **************************/

	private ProjectConfig(String projectId, String rmiHost, String dbHost, 
			String dbUsername, String dbPassword) {
		this.rmiHost = rmiHost;
		this.dbHost = dbHost;
		this.dbUsername = dbUsername;
		this.dbPassword = dbPassword;
		
		configs.put(projectId, this);
	}
	
	/**
	 * Gets the ProjectConfig object for the projectId specified.
	 * 
	 * @param projectId
	 * @return
	 */
	private static ProjectConfig get(String projectId) {
		ProjectConfig projectConfig = configs.get(projectId);
		
		if (projectConfig == null) {
			logger.error("Trying to access ProjectConfig information for " +
					"projectId={} but that project is not configured in " +
					"org.transitime.projectConfig.ProjectConfig",
					projectId);
		}
		
		return projectConfig;
	}
	
	/**
	 * Each project should have an associated hostname so that RMI
	 * calls can go to the right host. This info should come from
	 * a config file or database.
	 * 
	 * @param projectId
	 * @return
	 */
	public static String getRmiHost(String projectId) {
		return get(projectId).rmiHost;		
	}
	
	public static String getDbHost(String projectId) {
		return get(projectId).dbHost;		
	}
	
	public static String getDbUsername(String projectId) {
		return get(projectId).dbUsername;
	}
	
	public static String getDbPassword(String projectId) {
		return get(projectId).dbPassword;
	}
	
}
