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

package org.transitime.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.utils.EmailSender;

/**
 * For monitoring whether the core system is working properly. For calling
 * all of the specific monitoring functions.
 *
 * @author SkiBu Smith
 *
 */
public class AgencyMonitor {

	// So can send out notification email if monitor triggered
	private final EmailSender emailSender;
	
	// All the types of monitoring to do
	private final AvlFeedMonitor avlFeedMonitor;
	private final PredictabilityMonitor predictabilityMonitor;
	private final SystemMonitor systemMonitor;
	private final DatabaseMonitoring databaseMonitor;
	
	private static final Logger logger = LoggerFactory
			.getLogger(AgencyMonitor.class);

	/********************** Member Functions **************************/

	public AgencyMonitor(String agencyId) {
		emailSender = new EmailSender();
		
		avlFeedMonitor = new AvlFeedMonitor(emailSender, agencyId);
		predictabilityMonitor = new PredictabilityMonitor(emailSender, agencyId);
		systemMonitor = new SystemMonitor(emailSender, agencyId);
		databaseMonitor = new DatabaseMonitoring(emailSender, agencyId);
	}
	
	/**
	 * Checks the core system to make sure it is working properly. If it is then
	 * null is returned. If there is a problem then returns an error message.
	 * Sends out notification e-mails if there is an issue via MonitorBase
	 * class. To be called periodically via Inter Process Communication.
	 * 
	 * @return Null if system OK, or the last error message for all the
	 *         monitoring if there is a problem.
	 */
	public String checkAll() {
		logger.info("Monitoring agency for problems...");
		
		String errorMessage = null;
		
		// Check all the monitors. 
		if (databaseMonitor.checkAndNotify())
			errorMessage = databaseMonitor.getMessage();
		
		if (avlFeedMonitor.checkAndNotify())
			errorMessage = avlFeedMonitor.getMessage();

		if (systemMonitor.checkAndNotify())
			errorMessage = systemMonitor.getMessage();
		
		if (predictabilityMonitor.checkAndNotify())
			errorMessage = predictabilityMonitor.getMessage();
		
		// Return the last error message if there was one
		return errorMessage;
	}
	
	public static void main(String[] args) {
		String agencyId = "mbta";
		AgencyMonitor agencyMonitor = new AgencyMonitor(agencyId);
		String resultStr = agencyMonitor.checkAll();
		System.out.println("resultStr=" + resultStr);
	}
}
