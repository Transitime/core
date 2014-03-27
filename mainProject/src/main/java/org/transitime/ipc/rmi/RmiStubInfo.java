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

import org.transitime.projectConfig.ProjectConfig;

/**
 * Contains the info needed for creating an RMI stub.
 * This information exists on the client side.
 * 
 * @author SkiBu Smith
 *
 */
public class RmiStubInfo {

	final String projectId;	
	final String className;
	
	/********************** Member Functions **************************/

	public RmiStubInfo(String projectId, String className) {
		this.projectId = projectId;
		this.className = className;
	}
	
	public String getProjectId() {
		return projectId;
	}
	
	public String getClassName() {
		return className;
	}
	
	public String getHostName() {
		return ProjectConfig.getRmiHost(projectId);
	}
}
