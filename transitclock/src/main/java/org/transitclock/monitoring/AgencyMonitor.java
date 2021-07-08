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

package org.transitclock.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.utils.EmailSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * For monitoring whether the core system is working properly. For calling
 * all of the specific monitoring functions.
 *
 * @author SkiBu Smith
 *
 */
public class AgencyMonitor {

    private final MonitoringService monitoringService;

	// So can send out notification email if monitor triggered
	private final EmailSender emailSender;
	
	// List of all the monitoring to do
	private final List<MonitorBase> monitors;
	
	// For being able to reuse AgencyMonitors. This is important because
	// each monitor maintains state, such as if notification e-mail sent out.
	// Keyed on agencyId.
	private static final Map<String, AgencyMonitor> agencyMonitorMap = 
			new HashMap<String, AgencyMonitor>();
	
	private static final Logger logger = LoggerFactory
			.getLogger(AgencyMonitor.class);

    private static final String enableSystemMonitoring = System.getProperty("transitclock.enableSystemMonitoring");

	/********************** Member Functions **************************/

	/**
	 * Constructor declared private so have to use getInstance() to get an
	 * AgencyMonitor. Like a singleton, but one AgencyMonitor for every
	 * agencyId.
	 * 
	 * @param agencyId
	 */
	private AgencyMonitor(String agencyId) {
		emailSender = new EmailSender();
        monitoringService = MonitoringService.getInstance();

		// Create all the monitors and add them to the monitors list
		monitors = new ArrayList<MonitorBase>();
		monitors.add(new AvlFeedMonitor(monitoringService, emailSender, agencyId));
		monitors.add(new PredictabilityMonitor(monitoringService, emailSender, agencyId));
        monitors.add(new DatabaseQueueMonitor(monitoringService, emailSender, agencyId));
        monitors.add(new ActiveBlocksMonitor(monitoringService, emailSender, agencyId));
        if(enableSystemMonitoring != null && enableSystemMonitoring.equalsIgnoreCase("true")){
            monitors.add(new SystemMemoryMonitor(emailSender, agencyId));
            monitors.add(new SystemCpuMonitor(emailSender, agencyId));
            monitors.add(new SystemDiskSpaceMonitor(emailSender,
                    agencyId));
            monitors.add(new DatabaseMonitor(emailSender, agencyId));
        }
	}
	
	/**
	 * Returns the AgencyMonitor for the specified agencyId. If the
	 * AgencyMonitor for that agency hasn't been created yet it is created. This
	 * is important because each monitor maintains state, such as if
	 * notification e-mail sent out.
	 * 
	 * @param agencyId
	 *            Which agency get AgencyMonitor for
	 * @return The AgencyMonitor for the agencyId
	 */
	public static AgencyMonitor getInstance(String agencyId) {
		synchronized (agencyMonitorMap) {
			AgencyMonitor agencyMonitor = agencyMonitorMap.get(agencyId);
			if (agencyMonitor == null) {
				agencyMonitor = new AgencyMonitor(agencyId);
				agencyMonitorMap.put(agencyId, agencyMonitor);
			}
			return agencyMonitor;
		}
	}
	
	/**
	 * Checks all the monitors for the agency and returns all resulting messages
	 * whether a monitor is triggered or not. Useful for showing current status
	 * of system.
	 * 
	 * @return List of results of monitoring
	 */
	public List<MonitorResult> getMonitorResults() {
		// Check all the monitors, which will set their message
		checkAll();
		
		// For all the monitors return the results
		List<MonitorResult> monitorResults = new ArrayList<MonitorResult>();
		for (MonitorBase monitor : monitors) {
			MonitorResult monitorResult = 
					new MonitorResult(monitor.type(), monitor.getMessage());
			monitorResults.add(monitorResult);
		}
		return monitorResults;
	}
	
	/**
	 * Checks the core system to make sure it is working properly. If it is then
	 * null is returned. If there are any problems then returns the
	 * concatenation of all the error messages. Sends out notification e-mails
	 * if there is an issue via MonitorBase class. To be called periodically via
	 * a MonitoringModule or via Inter Process Communication.
	 * 
	 * @return Null if system OK, or the concatenation of the error message for
	 *         all the monitoring if there are any problems.
	 */
	public String checkAll() {
		logger.info("Monitoring agency for problems...");
		
		String errorMessage = "";
		
		// Check all the monitors. 
		for (MonitorBase monitor : monitors) {
			if (monitor.checkAndNotify())
				errorMessage += " " + monitor.getMessage();			
		}
				
		// Return the concatenated error message if there were any
		if (errorMessage.length() > 0)
			return errorMessage;
		else
			return null;
	}
	
	public static void main(String[] args) {
		String agencyId = "mbta";
		AgencyMonitor agencyMonitor = AgencyMonitor.getInstance(agencyId);
		String resultStr = agencyMonitor.checkAll();
		System.out.println("resultStr=" + resultStr);
	}
}
