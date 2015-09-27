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

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.LongConfigValue;
import org.transitime.utils.EmailSender;
import org.transitime.utils.StringUtils;

/**
 * For monitoring CPU, available memory, and available disk space.
 * <p>
 * Note: Linux will use a great deal of RAM for caching and such when memory is
 * available. Therefore most of the time the free memory will be quite low. But
 * this is OK since the operating system will give up the RAM being used for
 * caching and such if a process needs it. Therefore the allowable free memory
 * should be set to quite a low value.
 *
 * @author SkiBu Smith
 *
 */
public class SystemMemoryMonitor extends MonitorBase {

	LongConfigValue availableFreePhysicalMemoryThreshold = new LongConfigValue(
			"transitime.monitoring.availableFreePhysicalMemoryThreshold", 
			25 * 1024 * 1024L, // ~25 MB 
			"If available free physical memory is less than this "
			+ "value then free memory monitoring is triggered. This should be "
			+ "relatively small since on Linux the operating system will use "
			+ "most of the memory for buffers and such when it is available. "
			+ "Therefore even when only a small amount of memory is available "
			+ "the system is still OK.");

	private static LongConfigValue availableFreePhysicalMemoryThresholdGap =
			new LongConfigValue(
					"transitime.monitoring.availableFreePhysicalMemoryThresholdGap", 
					150 * 1024 * 1024L, // ~150 MB 
					"When transitioning from triggered to untriggered don't "
					+ "want to send out an e-mail right away if actually "
					+ "dithering. Therefore will only send out OK e-mail if the "
					+ "value is now above availableFreePhysicalMemoryThreshold + "
					+ "availableFreePhysicalMemoryThresholdGap ");

	private static final Logger logger = LoggerFactory
			.getLogger(SystemMemoryMonitor.class);

	/********************** Member Functions **************************/

	/**
	 * Simple constructor
	 * 
	 * @param emailSender
	 * @param agencyId
	 */
	public SystemMemoryMonitor(EmailSender emailSender, String agencyId) {
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
	public static Object getOperatingSystemValue(String methodName) {
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
	
	/* (non-Javadoc)
	 * @see org.transitime.monitoring.MonitorBase#triggered()
	 */
	/**
	 * Sees if recent available memory is lower than value specified by
	 * availableFreePhysicalMemoryThreshold.
	 * 
	 * @return True if available memory is lower than
	 *         availableFreePhysicalMemoryThreshold. If available memory is
	 *         higher or can't determine available memory then returns false.
	 */
	@Override
	protected boolean triggered() {
		Object resultObject = 
				getOperatingSystemValue("getFreePhysicalMemorySize");
		if (resultObject != null) {
			long freePhysicalMemory = (Long) resultObject;
				
			// Provide message explaining situation
			setMessage("Free physical memory is " 
					+ StringUtils.memoryFormat(freePhysicalMemory) 
					+ " while the limit is " 
					+ StringUtils.memoryFormat(
							availableFreePhysicalMemoryThreshold.getValue())
					+ ".",
					freePhysicalMemory);
			
			// Determine the threshold for triggering. If already triggered
			// then raise the threshold by availableFreePhysicalMemoryThresholdGap
			// in order to prevent lots of e-mail being sent out if the value
			// is dithering around availableFreePhysicalMemoryThreshold.
			long threshold = availableFreePhysicalMemoryThreshold.getValue();
			if (wasTriggered())
				threshold += availableFreePhysicalMemoryThresholdGap.getValue();

			// Return true if problem detected
			return freePhysicalMemory < threshold;
		} 
		
		// Could not determine available memory so have to return false
		return false;

	}

	/* (non-Javadoc)
	 * @see org.transitime.monitoring.MonitorBase#type()
	 */
	@Override
	protected String type() {
		return "System Memory";
	}

}
