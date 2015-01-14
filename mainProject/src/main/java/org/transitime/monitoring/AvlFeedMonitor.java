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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.IntegerConfigValue;
import org.transitime.config.StringConfigValue;
import org.transitime.core.AvlProcessor;
import org.transitime.core.BlocksInfo;
import org.transitime.db.structs.Block;
import org.transitime.utils.EmailSender;
import org.transitime.utils.Time;

/**
 * For determining if the AVL feed is up. If not getting data when blocks are
 * active then the AVL feed is considered down. It is important to only expect
 * data when blocks are active because otherwise would always get false
 * positives.
 *
 * @author SkiBu Smith
 *
 */
public class AvlFeedMonitor extends MonitorBase {

	private static IntegerConfigValue allowableAvlFeedTimeNoDataSecs =
			new IntegerConfigValue(
					"transitime.monitoring.allowableAvlFeedTimeNoDataSecs", 
					5 * Time.SEC_PER_MIN, 
					"How long in seconds that can not receive valid AVL data "
					+ "before monitoring triggers an alert.");

	private static StringConfigValue avlFeedEmailRecipients =
			new StringConfigValue(
					"transitime.monitoring.avlFeed.emailRecipients", 
					"monitoring@transitime.org", 
					"Comma separated list of e-mail addresses indicating who "
					+ "should be e-mail when monitor state changes for AVL "
					+ "feed.");

	private static final Logger logger = LoggerFactory
			.getLogger(AvlFeedMonitor.class);

	/********************** Member Functions **************************/

	/**
	 * Simple constructor
	 * 
	 * @param emailSender
	 * @param agencyId
	 */
	public AvlFeedMonitor(EmailSender emailSender, String agencyId) {
		super(emailSender, agencyId);
	}
	
	/**
	 * Checks GPS time of last AVL report from the AVL feed. If it is recent, as
	 * specified by transitime.monitoring.allowableAvlFeedTimeNoDataSecs, then
	 * this method returns 0. If no GPS data or the data is too old then returns
	 * age of last AVL report in seconds.
	 * 
	 * @return 0 if have recent valid GPS data or age of last AVL report in
	 *         seconds.
	 */
	private static int avlFeedOutageSecs() {
		// If there are no currently active blocks then don't need to be
		// getting AVL data so return 0
		List<Block> activeBlocks = BlocksInfo.getCurrentlyActiveBlocks(0);
		if (activeBlocks.size() == 0) {
			logger.debug("No currently active blocks so AVL feed considered to "
					+ "be OK");
			return 0;
		}
		
		// Determine age of AVL report
		long lastAvlReportTime = AvlProcessor.getInstance().lastAvlReportTime();
		long allowableTime = System.currentTimeMillis()
				- allowableAvlFeedTimeNoDataSecs.getValue() * Time.MS_PER_SEC;
		long ageOfAvlReport = System.currentTimeMillis() - lastAvlReportTime;
		if (ageOfAvlReport > allowableTime) {
			// last AVL report is too old
			logger.error("Last valid AVL report is {} secs old which is not "
					+ "acceptable, indicating AVL feed is down.",
					ageOfAvlReport / Time.MS_PER_SEC);
			return (int) (ageOfAvlReport / Time.MS_PER_SEC);
		} else {
			// Last AVL report is not too old
			logger.debug(
					"Last valid AVL report is only {} secs old so AVL feed is "
					+ "OK.", ageOfAvlReport / Time.MS_PER_SEC);
			return 0;
		}
	}

	/* (non-Javadoc)
	 * @see org.transitime.monitoring.MonitorBase#triggered()
	 */
	@Override
	protected boolean triggered() {
		// Check AVL feed
		int avlFeedOutageSecs = AvlFeedMonitor.avlFeedOutageSecs();
		if (avlFeedOutageSecs != 0) {
			setMessage("No AVL data from feed in " + avlFeedOutageSecs 
					+ " secs");
			return true;
		} else {
			// Feed OK
			setMessage("Received AVL data from " + avlFeedOutageSecs 
					+ " secs ago");
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.transitime.monitoring.MonitorBase#type()
	 */
	@Override
	protected String type() {
		return "AVL feed";
	}
	
	/**
	 * Returns comma separated list of who should be notified via e-mail when
	 * trigger state changes for the monitor. Specified by the Java property
	 * transitime.monitoring.emailRecipients . Can be overwritten by an
	 * implementation of a monitor if want different list for a monitor.
	 * 
	 * @return E-mail addresses of who to notify
	 */
	protected String recipients() {
		return avlFeedEmailRecipients.getValue();
	}

}
