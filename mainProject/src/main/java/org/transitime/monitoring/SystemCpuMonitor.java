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
import org.transitime.config.DoubleConfigValue;
import org.transitime.utils.EmailSender;
import org.transitime.utils.StringUtils;
import org.transitime.utils.Time;

/**
 * Monitors to make sure that server CPU is not too high.
 *
 * @author SkiBu Smith
 *
 */
public class SystemCpuMonitor extends MonitorBase {

	DoubleConfigValue cpuThreshold = new DoubleConfigValue(
			"transitime.monitoring.cpuThreshold", 
			0.9, 
			"If CPU load averaged over a minute exceeds this 0.0 - 1.0 "
			+ "value then CPU monitoring is triggered.");
	
	private static final Logger logger = LoggerFactory
			.getLogger(SystemCpuMonitor.class);

	/********************** Member Functions **************************/

	/**
	 * Simple constructor
	 * 
	 * @param emailSender
	 * @param agencyId
	 */
	public SystemCpuMonitor(EmailSender emailSender, String agencyId) {
		super(emailSender, agencyId);
	}

	/* (non-Javadoc)
	 * @see org.transitime.monitoring.MonitorBase#triggered()
	 */
	/**
	 * Sees if recent CPU load is higher than value specified by cpuThreshold.
	 * Since CPU loads spike this method checks a second time after a brief
	 * 1000msec sleep so can get an average CPU value.
	 * 
	 * @return True if CPU load higher than cpuThreshold. If CPU load lower or
	 *         can't determine CPU load then returns false.
	 */
	@Override
	protected boolean triggered() {
		Object resultObject = SystemMemoryMonitor
				.getOperatingSystemValue("getSystemCpuLoad");
		if (resultObject != null) {
			double cpuLoad = (Double) resultObject;

			// If cpuLoad too high take another reading after a brief sleep
			// and take the average. This is important because sometimes the
			// CPU will be temporarily spiked at 1.0 and don't want to send
			// out an alert for short lived spikes.
			if (cpuLoad >= cpuThreshold.getValue()) {
				logger.debug("CPU load was {} which is higher than threshold "
						+ "of {} so taking another reading.", 
						StringUtils.twoDigitFormat(cpuLoad),
						StringUtils.twoDigitFormat(cpuThreshold.getValue()));
				Time.sleep(1000);
				resultObject = SystemMemoryMonitor
						.getOperatingSystemValue("getSystemCpuLoad");
				double cpuLoad2 = (Double) resultObject;
				
				// Take average of cpuLoad
				cpuLoad = (cpuLoad + cpuLoad2) / 2.0;
			}
				
			setMessage("CPU load is " 
					+ StringUtils.twoDigitFormat(cpuLoad) 
					+ " while limit is " 
					+ StringUtils.twoDigitFormat(cpuThreshold.getValue()) 
					+ ".",
					cpuLoad);
						
			// Return true if CPU problem found
			return cpuLoad >= cpuThreshold.getValue();
		} 

		// Could not determine CPU load so have to return false
		return false;
	}

	/* (non-Javadoc)
	 * @see org.transitime.monitoring.MonitorBase#type()
	 */
	@Override
	protected String type() {
		return "System CPU";
	}

}
