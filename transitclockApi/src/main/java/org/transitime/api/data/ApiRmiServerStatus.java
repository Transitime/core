/*
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License (GPL) as published by the
 * Free Software Foundation, either version 3 of the License, or any later
 * version.
 * 
 * Transitime.org is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Transitime.org . If not, see <http://www.gnu.org/licenses/>.
 */

package org.transitime.api.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.transitime.ipc.rmi.RmiCallInvocationHandler;

/**
 *
 *
 * @author SkiBu Smith
 *
 */
@XmlRootElement(name = "rmiServerStatus")
public class ApiRmiServerStatus {

	@XmlElement(name = "agency")
	private List<ApiAgencyRmiServerStatus> agenciesData;

	/**
	 * Sub API class that actually contains all data for each agency
	 */
	private static class ApiAgencyRmiServerStatus {
		@XmlAttribute(name = "id")
		private String agencyId;

		@XmlAttribute
		private int rmiCallsInProcess;

		@XmlAttribute
		private long rmiTotalCalls;

		@SuppressWarnings("unused")
		protected ApiAgencyRmiServerStatus() {
		}

		public ApiAgencyRmiServerStatus(String agencyId, int rmiCallsInProcess,
				long rmiTotalCalls) {
			this.agencyId = agencyId;
			this.rmiCallsInProcess = rmiCallsInProcess;
			this.rmiTotalCalls = rmiTotalCalls;
		}
	}

	/********************** Member Functions **************************/

	/**
	 * Constructors a ApiRmiServerStatus object
	 * 
	 * @param agencyId
	 * @param ipcServerStatus
	 */
	public ApiRmiServerStatus() {
		agenciesData = new ArrayList<ApiAgencyRmiServerStatus>();

		// For each agency that has had RMI calls
		Set<String> agencyIds = RmiCallInvocationHandler.getAgencies();
		for (String agencyId : agencyIds) {
			// Create an API object for this agency
			ApiAgencyRmiServerStatus agencyStatus =
					new ApiAgencyRmiServerStatus(agencyId,
							RmiCallInvocationHandler.getCount(agencyId),
							RmiCallInvocationHandler.getTotalCount(agencyId));
			agenciesData.add(agencyStatus);
		}
	}

}
