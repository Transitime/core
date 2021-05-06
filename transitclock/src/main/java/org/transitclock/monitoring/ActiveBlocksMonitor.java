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
import org.transitclock.applications.Core;
import org.transitclock.core.BlocksInfo;
import org.transitclock.db.structs.Block;
import org.transitclock.utils.EmailSender;

import java.util.Date;
import java.util.List;

/**
 * For monitoring active blocks.  Unlike the other monitors,
 * this one never triggers an alarm, it simply posts metrics to cloudwatch
 */
public class ActiveBlocksMonitor extends MonitorBase {

    private long reportingIntervalInMillis = 60l * 1000l;

    private Date lastUpdate = new Date();

    private MonitoringService monitoringService;

    private static final Logger logger = LoggerFactory
            .getLogger(ActiveBlocksMonitor.class);

	public ActiveBlocksMonitor(MonitoringService monitoringService, EmailSender emailSender, String agencyId) {
		super(emailSender, agencyId);
        this.monitoringService = monitoringService;
	}

	/* (non-Javadoc)
	 * @see org.transitclock.monitoring.MonitorBase#triggered()
	 */
	@Override
	protected boolean triggered() {
        Date now = new Date();
        if(now.getTime() - lastUpdate.getTime() > reportingIntervalInMillis){
            List<Block> blocks = BlocksInfo.getCurrentlyActiveBlocks();
            double activeBlockCount = (blocks != null ? blocks.size() : 0);
            double totalBlockCount = Core.getInstance().getDbConfig().getBlockCount();
            // cloudwatch metrics for active/total moved to PredictabilityMonitor
            double activeBlockCountPercentage = 0;
            if(activeBlockCount > 0){
                activeBlockCountPercentage = activeBlockCount / totalBlockCount;
            }
            monitoringService.averageMetric("PercentageActiveBlockCount", activeBlockCountPercentage);
            lastUpdate = new Date();
        }
        return false;
	}

	/* (non-Javadoc)
	 * @see org.transitclock.monitoring.MonitorBase#type()
	 */
	@Override
	protected String type() {
		return "Active Blocks";
	}
}
