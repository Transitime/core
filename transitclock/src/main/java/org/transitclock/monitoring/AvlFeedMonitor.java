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
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.core.AvlProcessor;
import org.transitclock.core.BlocksInfo;
import org.transitclock.db.structs.Block;
import org.transitclock.utils.EmailSender;
import org.transitclock.utils.Time;

import java.util.List;

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

    private MonitoringService monitoringService;

	private static IntegerConfigValue allowableNoAvlSecs =
			new IntegerConfigValue(
					"transitclock.monitoring.allowableNoAvlSecs", 
					5 * Time.SEC_PER_MIN, 
					"How long in seconds that can not receive valid AVL data "
					+ "before monitoring triggers an alert.");

	private static StringConfigValue avlFeedEmailRecipients =
			new StringConfigValue(
					"transitclock.monitoring.avlFeedEmailRecipients", 
					"monitoring@transitclock.org", 
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
	public AvlFeedMonitor(MonitoringService monitoringService, EmailSender emailSender, String agencyId) {
		super(emailSender, agencyId);
        this.monitoringService = monitoringService;
	}
	
	/**
	 * Checks GPS time of last AVL report from the AVL feed. If it is recent, as
	 * specified by transitclock.monitoring.allowableAvlFeedTimeNoDataSecs, then
	 * this method returns 0. If no GPS data or the data is too old then returns
	 * age of last AVL report in seconds.
	 * 
	 * @return 0 if have recent valid GPS data or age of last AVL report, in
	 *         seconds.
	 */
	private int avlFeedOutageSecs() {
		// Determine age of AVL report
		long lastAvlReportTime = AvlProcessor.getInstance().lastAvlReportTime();
		long ageOfAvlReport = System.currentTimeMillis() - lastAvlReportTime;
		Double ageOfAvlReportInSecs = new Double(ageOfAvlReport / Time.MS_PER_SEC );
        monitoringService.averageMetric("PredictionLatestAvlReportAgeInSeconds", ageOfAvlReportInSecs);

		logger.debug("When monitoring AVL feed last AVL report={}",
				AvlProcessor.getInstance().getLastAvlReport());
		
		setMessage("Last valid AVL report was " 
				+ ageOfAvlReport / Time.MS_PER_SEC
				+ " secs old while allowable age is " 
				+ allowableNoAvlSecs.getValue()	+ " secs as specified by "
				+ "parameter " + allowableNoAvlSecs.getID() + " .",
				ageOfAvlReport / Time.MS_PER_SEC);
		
		if (ageOfAvlReport > 
				allowableNoAvlSecs.getValue() * Time.MS_PER_SEC) {
			// last AVL report is too old
			return (int) (ageOfAvlReport / Time.MS_PER_SEC);
		} else {
			// Last AVL report is not too old
			return 0;
		}
	}

	/* (non-Javadoc)
	 * @see org.transitclock.monitoring.MonitorBase#triggered()
	 */
	@Override
	protected boolean triggered() {
		// Check AVL feed
		int avlFeedOutageSecs = avlFeedOutageSecs();
		return avlFeedOutageSecs != 0;
	}

	/**
	 * Returns true if there are no currently active blocks indicating that it
	 * doesn't matter if not getting AVL data.
	 * 
	 * @return true if no currently active blocks
	 */
	@Override
	protected boolean acceptableEvenIfTriggered() {
		List<Block> activeBlocks = BlocksInfo.getCurrentlyActiveBlocks();
		if (activeBlocks.size() == 0) {
			setAcceptableEvenIfTriggeredMessage("No currently active blocks "
					+ "so AVL feed considered to be OK.");
			return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.transitclock.monitoring.MonitorBase#type()
	 */
	@Override
	protected String type() {
		return "AVL feed";
	}
	
	/**
	 * Returns comma separated list of who should be notified via e-mail when
	 * trigger state changes for the monitor. Specified by the Java property
	 * transitclock.monitoring.emailRecipients . Can be overwritten by an
	 * implementation of a monitor if want different list for a monitor.
	 * 
	 * @return E-mail addresses of who to notify
	 */
	protected String recipients() {
		return avlFeedEmailRecipients.getValue();
	}

}
