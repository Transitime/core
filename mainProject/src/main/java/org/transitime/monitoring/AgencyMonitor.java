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
	private final SystemMonitor systemMonitor;
	
	private static final Logger logger = LoggerFactory
			.getLogger(AgencyMonitor.class);

	/********************** Member Functions **************************/

	public AgencyMonitor(String agencyId) {
		emailSender = new EmailSender();
		
		avlFeedMonitor = new AvlFeedMonitor(emailSender, agencyId);
		systemMonitor = new SystemMonitor(emailSender, agencyId);
	}
	
	/**
	 * Checks the core system to make sure it is working properly. If it is then
	 * null is returned. If there is a problem then returns an error message.
	 * Sends out notification e-mails if there is an issue. To be called
	 * periodically via Inter Process Communication.
	 * 
	 * @return Null if system OK, or error message if there is a problem.
	 */
	private String checkAll() {
		// Check all the monitors
		if (avlFeedMonitor.checkAndNotify())
			return avlFeedMonitor.getMessage();
		
		if (systemMonitor.checkAndNotify())
			return systemMonitor.getMessage();
		
		// No issue so return OK
		return null;
	}
	
	/**
	 * Checks the core system to make sure it is working properly and logs any
	 * problems. If it is then null is returned. If there is a problem then
	 * returns an error message. Sends out notification e-mails if there is an
	 * issue. To be called periodically via Inter Process Communication.
	 * 
	 * @return Null if system OK, or error message if there is a problem.
	 */
	public String checkAllAndLog() {
		logger.info("Monitoring agency for problems...");
		
		String errorMessage = checkAll();
		if (errorMessage != null)
			logger.error(errorMessage);
		
		return errorMessage;
	}
	
	public static void main(String[] args) {
		String agencyId = "foo";
		AgencyMonitor agencyMonitor = new AgencyMonitor(agencyId);
		String resultStr = agencyMonitor.checkAllAndLog();
		System.out.println("resultStr=" + resultStr);
	}
}
