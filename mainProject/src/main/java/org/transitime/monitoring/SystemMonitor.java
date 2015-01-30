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

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import javax.management.MBeanServerConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.DoubleConfigValue;
import org.transitime.config.LongConfigValue;
import org.transitime.utils.EmailSender;
import org.transitime.utils.StringUtils;
import org.transitime.utils.Time;

/**
 * For monitoring CPU, available memory, and available disk space.
 *
 * @author SkiBu Smith
 *
 */
public class SystemMonitor extends MonitorBase {

	DoubleConfigValue cpuThreshold = new DoubleConfigValue(
			"transitime.monitoring.cpuThreshold", 
			0.9, 
			"If CPU load averaged over a minute exceeds this 0.0 - 1.0 "
			+ "value then CPU monitoring is triggered.");
	
	LongConfigValue usableDiskSpaceThreshold = new LongConfigValue(
			"transitime.monitoring.usableDiskSpaceThreshold", 
			500 * 1024 * 1024L, // ~500 MB 
			"If usable disk space is less than this "
			+ "value then file space monitoring is triggered.");

	LongConfigValue availableFreePhysicalMemoryThreshold = new LongConfigValue(
			"transitime.monitoring.availableFreePhysicalMemoryThreshold", 
			100 * 1024 * 1024L, // ~100 MB 
			"If available free physical memory is less than this "
			+ "value then free memory monitoring is triggered.");

	private static final Logger logger = LoggerFactory
			.getLogger(SystemMonitor.class);

	/********************** Member Functions **************************/

	/**
	 * Simple constructor
	 * 
	 * @param emailSender
	 * @param agencyId
	 */
	public SystemMonitor(EmailSender emailSender, String agencyId) {
		super(emailSender, agencyId);
	}

	/**
	 * Gets an operating system value via reflection. Yes, this is a rather
	 * obtuse way of getting such values but it appears to work.
	 * 
	 * @param methodName
	 *            Name of the special internal
	 *            com.sun.management.OperatingSystemMXBean method to call
	 * @return The result from invoking the specified method
	 */
	private Object getOperatingSystemValue(String methodName) {
		OperatingSystemMXBean operatingSystemMxBean = 
				ManagementFactory.getOperatingSystemMXBean();
		try {
			// Get the getSystemCpuLoad() method using reflection
			Method method = 
					operatingSystemMxBean.getClass().getMethod(methodName);
			
			// Need to declare the method as accessible so that can 
			// invoke it
			method.setAccessible(true);

			// Get and return the result by invoking the specified method
			Object result = method.invoke(operatingSystemMxBean);
			return result;
		} catch (Exception e) {
			logger.error("Could not execute "
					+ "OperatingSystemMXBean.{}(). {}", 
					methodName, e.getMessage());
			return null;
		}
	}
	
	/**
	 * Sees if recent CPU load is higher than value specified by cpuThreshold.
	 * Since CPU loads spike this method checks a second time after a brief
	 * 1000msec sleep so can get an average CPU value.
	 * 
	 * @return True if CPU load higher than cpuThreshold. If CPU load lower or
	 *         can't determine CPU load then returns false.
	 */
	private boolean cpuTriggered() {
		Object resultObject = getOperatingSystemValue("getSystemCpuLoad");
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
				resultObject = getOperatingSystemValue("getSystemCpuLoad");
				double cpuLoad2 = (Double) resultObject;
				
				// Take average of cpuLoad
				cpuLoad = (cpuLoad + cpuLoad2) / 2.0;
			}
				
			setMessage("CPU load is " 
					+ StringUtils.twoDigitFormat(cpuLoad) 
					+ " while limit is " 
					+ StringUtils.twoDigitFormat(cpuThreshold.getValue()) 
					+ ".");
						
			// Return true if CPU problem found
			return cpuLoad >= cpuThreshold.getValue();
		} 

		// Could not determine CPU load so have to return false
		return false;
	}
	
	/**
	 * Sees if recent available memory is lower than value specified by
	 * availableFreePhysicalMemoryThreshold.
	 * 
	 * @return True if available memory is lower than
	 *         availableFreePhysicalMemoryThreshold. If available memory is
	 *         higher or can't determine available memory then returns false.
	 */
	private boolean availableMemoryTriggered() {
		Object resultObject = getOperatingSystemValue("getFreePhysicalMemorySize");
		if (resultObject != null) {
			long freePhysicalMemory = (Long) resultObject;
				
			// Provide message explaining situation
			setMessage("Free physical memory is " 
					+ StringUtils.memoryFormat(freePhysicalMemory) 
					+ " while the limit is " 
					+ StringUtils.memoryFormat(availableFreePhysicalMemoryThreshold.getValue())
					+ ".");
			
			// Return true if problem detected
			return freePhysicalMemory < availableFreePhysicalMemoryThreshold.getValue();
		} 
		
		// Could not determine available memory so have to return false
		return false;
	}	
	
	/**
	 * Determines if CPU load average is too high, higher than cpuThreshold.
	 * <p>
	 * Deprecated because was returning -1 on Windows. Need to use fancier method
	 * from 
	 * @return True if CPU load average is too high
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private boolean cpuLoadAverageTriggered() {		
		MBeanServerConnection mbsc = ManagementFactory.getPlatformMBeanServer();
		try {
			// Determine system load average
			OperatingSystemMXBean osMBean = ManagementFactory
					.newPlatformMXBeanProxy(mbsc,
							ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME,
							OperatingSystemMXBean.class);
			double loadAverage = osMBean.getSystemLoadAverage();

			// Provide message explaining situation
			setMessage("CPU load is " 
					+ StringUtils.twoDigitFormat(loadAverage) 
					+ " while limit is " 
					+ StringUtils.twoDigitFormat(cpuThreshold.getValue()));
			
			// Return true if CPU problem found
			return loadAverage > cpuThreshold.getValue();
		} catch (IOException e) {
			logger.debug("Could not determing system load. {}", e.getMessage());
			return false;
		}
	}
	
	/**
	 * Checks whether file system getting too full, beyond
	 * usableDiskSpaceThreshold.
	 * 
	 * @return True if file system getting too full
	 */
	private boolean fileSystemTriggered() {
		long usableSpace = new File("/").getUsableSpace();
		
		// Provide message explaining situation
		setMessage("Usable disk space is " 
				+ StringUtils.memoryFormat(usableSpace) 
				+ " while the minimum limit is " 
				+ StringUtils.memoryFormat(usableDiskSpaceThreshold.getValue())
				+ ".");
		
		// Return true if usable disk space problem found
		return usableSpace < usableDiskSpaceThreshold.getValue();
	}
	
	/* (non-Javadoc)
	 * @see org.transitime.monitoring.MonitorBase#triggered()
	 */
	@Override
	protected boolean triggered() {
		// Check each system value and return true if there is a problem.
		if (cpuTriggered())
			return true;
		
		if (availableMemoryTriggered())
			return true;
		
		if (fileSystemTriggered())
			return true;
		
		// No problems detected so return false
		return false;
	}

	/* (non-Javadoc)
	 * @see org.transitime.monitoring.MonitorBase#type()
	 */
	@Override
	protected String type() {
		return "System";
	}

}
