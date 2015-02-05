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

import org.transitime.config.LongConfigValue;
import org.transitime.utils.EmailSender;
import org.transitime.utils.StringUtils;

/**
 * Monitors to make sure there is sufficient disk space.
 *
 * @author SkiBu Smith
 *
 */
public class SystemDiskSpaceMonitor extends MonitorBase {

	private LongConfigValue usableDiskSpaceThreshold = new LongConfigValue(
			"transitime.monitoring.usableDiskSpaceThreshold", 
			500 * 1024 * 1024L, // ~500 MB 
			"If usable disk space is less than this "
			+ "value then file space monitoring is triggered.");

	private static LongConfigValue usableDiskSpaceThresholdGap =
			new LongConfigValue(
					"transitime.monitoring.usableDiskSpaceThresholdGap", 
					100 * 1024 * 1024L, // ~100 MB 
					"When transitioning from triggered to untriggered don't "
					+ "want to send out an e-mail right away if actually "
					+ "dithering. Therefore will only send out OK e-mail if the "
					+ "value is now above usableDiskSpaceThreshold + "
					+ "usableDiskSpaceThresholdGap ");

	/********************** Member Functions **************************/

	/**
	 * Simple constructor
	 * 
	 * @param emailSender
	 * @param agencyId
	 */
	public SystemDiskSpaceMonitor(EmailSender emailSender, String agencyId) {
		super(emailSender, agencyId);
	}

	/* (non-Javadoc)
	 * @see org.transitime.monitoring.MonitorBase#triggered()
	 */
	/**
	 * Checks whether file system getting too full, beyond
	 * usableDiskSpaceThreshold.
	 * 
	 * @return True if file system getting too full
	 */
	@Override
	protected boolean triggered() {
		long usableSpace = new File("/").getUsableSpace();
		
		// Provide message explaining situation
		setMessage("Usable disk space is " 
				+ StringUtils.memoryFormat(usableSpace) 
				+ " while the minimum limit is " 
				+ StringUtils.memoryFormat(usableDiskSpaceThreshold.getValue())
				+ ".",
				usableSpace);
		
		// Determine the threshold for triggering. If already triggered
		// then raise the threshold by usableDiskSpaceThresholdGap in order
		// to prevent lots of e-mail being sent out if the value is
		// dithering around usableDiskSpaceThreshold.
		long threshold = usableDiskSpaceThreshold.getValue();
		if (wasTriggered())
			threshold += usableDiskSpaceThresholdGap.getValue();

		// Return true if usable disk space problem found
		return usableSpace < threshold;
	}

	/* (non-Javadoc)
	 * @see org.transitime.monitoring.MonitorBase#type()
	 */
	@Override
	protected String type() {
		return "System Disk Space";
	}

}
